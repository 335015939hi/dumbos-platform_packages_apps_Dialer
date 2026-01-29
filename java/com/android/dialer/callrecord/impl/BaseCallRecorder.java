package com.android.dialer.callrecord.impl;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.GuardedBy;

/**
 * Records audio using {@link android.media.AudioRecord} which is the same framework API that Google
 * Dialer uses for call recording / call notes. Subclasses decide what to do with the PCM data
 * from AudioRecord.
 */
public abstract class BaseCallRecorder implements Closeable {

  private static final String TAG = "BaseCallRecorder";

  private static final int DURATION_TO_READ_MS = 1000;
  private static final int BUFFER_POOL_NUM_BUFFERS = 15;
  private static final long RECORD_JOB_LOOP_PERIOD_MS = 10;

  private volatile boolean mIsRecording;
  private volatile boolean mIsClosed;

  protected final OutputFormat mOutputFormat;
  protected final AudioFormat mAudioFormat;
  private final AudioRecord mAudioRecord;
  protected final ContentResolver mContentResolver;

  /**
   * The URI pointing to the call recording file that we are writing to.
   */
  protected final Uri mUri;

  private final ScheduledExecutorService mAudioBufferProducerExecutor =
          Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(r, "AudioRec-HighPrio");
            thread.setPriority(Thread.NORM_PRIORITY + 1);
            return thread;
          });
  private final ExecutorService mAudioBufferConsumerExecutor = Executors.newSingleThreadExecutor();
  private ByteArrayPool mAudioBufArrayPool;

  @GuardedBy("this")
  private Future<?> mWritingTask;

  private final int mPcmBufferSize;

  protected BaseCallRecorder(Context context, int audioSource, Uri uri, OutputFormat outputFormat) {
    mOutputFormat = outputFormat;

    this.mContentResolver = context.getApplicationContext().getContentResolver();
    this.mAudioFormat = new AudioFormat.Builder()
            .setChannelMask(outputFormat.channelMask)
            .setSampleRate(outputFormat.sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build();
    // Google Dialer apparently uses mAudioFormat.getSampleRate() for the AudioRecord buffer size.
    final int bufferSizeInBytes = mAudioFormat.getSampleRate();
    mAudioRecord = new AudioRecord(audioSource, mAudioFormat.getSampleRate(),
            mAudioFormat.getChannelMask(), mAudioFormat.getEncoding(), bufferSizeInBytes);
    mUri = uri;

    final int framesInDurationMs = mAudioRecord.getSampleRate() * DURATION_TO_READ_MS / 1000;
    mPcmBufferSize = Math.max(
            mAudioFormat.getFrameSizeInBytes() * framesInDurationMs,
            AudioRecord.getMinBufferSize(
                    mAudioRecord.getSampleRate(), mAudioRecord.getChannelConfiguration(),
                    mAudioRecord.getAudioFormat()));
    Log.d(TAG, "mPcmBufferSize " + mPcmBufferSize);
    mAudioBufArrayPool = new ByteArrayPool(BUFFER_POOL_NUM_BUFFERS, mPcmBufferSize);
  }

  protected final long computePresentationTimeUs(int bytesRead) {
    int framesRead = bytesRead / mAudioFormat.getFrameSizeInBytes();
    return (framesRead * 1_000_000L) / mAudioFormat.getSampleRate();
  }

  public final boolean isRecording() {
    return mIsRecording && mWritingTask != null;
  }

  public final synchronized void startRecording() {
    if (mWritingTask != null) {
      Log.d(TAG, "existing recording task running");
      return;
    }
    if (mIsClosed || mAudioBufferConsumerExecutor.isShutdown()
            || mAudioBufferProducerExecutor.isShutdown() ) {
      Log.e(TAG, "cannot start recording after close() called");
      return;
    }

    mBytesRead.set(0);
    if (mAudioBufArrayPool.isClosed()) {
      mAudioBufArrayPool = new ByteArrayPool(BUFFER_POOL_NUM_BUFFERS, mPcmBufferSize);
    }

    mRecordLoopJob.set(
      mAudioBufferProducerExecutor.schedule(() -> {
        try {
          runInitialRecordJob();
        } catch (Throwable t) {
          Log.e(TAG, "error when running initial record job", t);
          throw t;
        }
        return null;
      }, 0, TimeUnit.MILLISECONDS));
    mWritingTask = mAudioBufferConsumerExecutor.submit(() -> {
      try {
        runConsumerJob();
      } catch (Throwable t) {
        Log.e(TAG, "error when running consumer job", t);
        throw t;
      }
      return null;
    });
  }

  private ParcelFileDescriptor mWritePfd;
  private final AtomicReference<ScheduledFuture<?>> mRecordLoopJob = new AtomicReference<>(null);

  private void runInitialRecordJob() throws Exception {
    mWritePfd = mContentResolver.openFileDescriptor(mUri, "w");
    if (mWritePfd == null) {
      throw new IOException("pfd for " + mUri + " failed to open");
    }

    try {
      onRecordingStart(mWritePfd);
    } catch (Throwable t) {
      try {
        mWritePfd.close();
      } catch (IOException ignored) {
      }
      throw t;
    }

    Log.d(TAG, "start recording");
    mAudioRecord.startRecording();
    mIsRecording = true;

    final long startTimeMs = SystemClock.elapsedRealtime();
    new RecordJobLoop(startTimeMs, RECORD_JOB_LOOP_PERIOD_MS).call();
  }

  /**
   * A job reads from AudioRecord periodically via {@link #mAudioBufferProducerExecutor}.
   * This roughly follows Google Dialer logic, although it's not clear why they don't use
   * {@link ScheduledExecutorService#scheduleAtFixedRate}. Perhaps because of
   * https://github.com/GrapheneOS/platform_libcore/commit/b1c2d048e84146ed5d17d72ab633f06faa9a2869
   * or some other reason.
   */
  class RecordJobLoop implements Callable<Object> {
    private final long mStartTimeMs;
    private final long mPeriodMs;
    // This might be left here if the job is cancelled, but that's fine since the pool this
    // buffer belongs to gets closed if this is cancelled anyway.
    private ByteArrayPool.Buffer mBuffer;

    RecordJobLoop(long startTimeMs, long periodMs) {
      mStartTimeMs = startTimeMs;
      mPeriodMs = periodMs;
    }

    /**
     * Computes an aligned delay based on starting at mStartTime modulo the period. The purpose is
     * to ensure that the loop is executed only at the times `startTime + k*period`
     */
    private long computeAlignedDelayMs() {
      if (mPeriodMs <= 0) return 0L;
      long now = SystemClock.elapsedRealtime();
      if (now < mStartTimeMs) {
        return (mStartTimeMs + mPeriodMs) - now;
      }

      // align at next slot at startTime + k*period
      long elapsedSinceStart = now - mStartTimeMs;
      long remainder = elapsedSinceStart % mPeriodMs;
      return remainder == 0 ? mPeriodMs : mPeriodMs - remainder;
    }

    private boolean shouldContinue() {
      return mIsRecording && mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
    }

    private void closeQuietly() {
      Log.d(TAG, "closeQuietly: AudioRecord loop finished");
      if (mBuffer != null) {
        // not needed since we're going to close the pool and only one producer
        mAudioBufArrayPool.release(mBuffer);
        mBuffer = null;
      }
      stopAudioRecordResources();
    }

    @Override
    public Object call() throws Exception {
      try {
        final boolean shouldContinue = callInner();
        if (shouldContinue) {
          // since we use AudioRecord.READ_NON_BLOCKING, reschedule periodically
          long delay = computeAlignedDelayMs();
          mRecordLoopJob.set(
                  mAudioBufferProducerExecutor.schedule(this, delay, TimeUnit.MILLISECONDS));
        } else {
          closeQuietly();
        }
      } catch (Throwable t) {
        closeQuietly();
        throw t;
      }
      return null;
    }

    // Returns whether we should continue periodically
    public boolean callInner() throws Exception {
      if (!shouldContinue()) {
        return false;
      }

      if (mBuffer == null) {
        mBuffer = mAudioBufArrayPool.acquire();
        if (mBuffer == null) {
          // pool is closed
          return false;
        }
      }

      int read = 0;

      // Google Dialer passes a byte[] instead of short[] for ENCODING_PCM_16BIT, despite this
      // method documenting that byte[] is only for ENCODING_PCM_8BIT and that using byte[] here
      // is deprecated.
      read = mAudioRecord.read(mBuffer.data, 0, mBuffer.data.length,
              AudioRecord.READ_NON_BLOCKING);
      if (read < 0) {
        mAudioBufArrayPool.release(mBuffer);
        mBuffer = null;
        Log.e(TAG, "error on AudioRecord read: " + read);
        throw new IOException("error on AudioRecord read: " + read);
      } else if (read > 0) {
        if (!mAudioBufArrayPool.produce(mBuffer, read)) {
          // pool is closed
          return false;
        }
        mBuffer = null;
      }
      // If we read 0 bytes, keep the buffer for the next scheduled run to avoid polling for a
      // free one again


      return shouldContinue();
    }
  }

  private void stopAudioRecordResources() {
    try {
      mAudioRecord.stop();
    } catch (IllegalStateException e) {
    }
    mAudioBufferPool.close();
  }

  public final synchronized void stopRecordingBlocking() {
    mIsRecording = false;
    mAudioBufArrayPool.close();
    if (mWritingTask == null) {
      return;
    }
    try {
      mWritingTask.get(5, TimeUnit.SECONDS);
    } catch (ExecutionException | TimeoutException e) {
      Log.w(TAG, "failed to wait for writing task to finish", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    mWritingTask = null;

    final ScheduledFuture<?> recordLoopJob = mRecordLoopJob.getAndSet(null);
    if (recordLoopJob == null) {
      return;
    }
    recordLoopJob.cancel(false);
    try {
      recordLoopJob.get(5, TimeUnit.SECONDS);
    } catch (CancellationException ignored) {
      // good
    } catch (ExecutionException | TimeoutException e) {
      Log.w(TAG, "failed to wait for recording task to finish", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    Log.d(TAG, "stopRecordingBlocking finished waiting for tasks");
    stopAudioRecordResources();
    try {
      onRecordingStop();
    } catch (IOException e) {
      Log.e(TAG, "error in onRecordingStop", e);
    } finally {
      if (mWritePfd != null) {
        try {
          mWritePfd.close();
        } catch (IOException ignored) {
        }
      }
      reset();
    }
  }

  protected final AtomicInteger mBytesRead = new AtomicInteger(0);

  private void runConsumerJob() throws IOException, InterruptedException {
    while (true) {
      try (ByteArrayPool.Buffer newPcmAudio = mAudioBufArrayPool.consume()) {
        if (newPcmAudio == null) {
          Log.d(TAG, "consumer job exit");
          break;
        }

        mBytesRead.addAndGet(newPcmAudio.length);
        onPcmBufferRead(newPcmAudio.length, newPcmAudio.data);
      }
    }
  }

  /**
   * Called before the buffer loop to prepare for recording. After this, the buffer reading loop is
   * entered and {@link #onPcmBufferRead(int, byte[])} will be called repeatedly.
   *
   * @param pfd A file descriptor for the call recording file in storage to write to. This is open
   *            in "w" mode, so seeking isn't possible. Do not close this.
   * @throws IOException if an error occurs when preparing for recording
   */
  protected abstract void onRecordingStart(ParcelFileDescriptor pfd) throws IOException;

  /**
   * Repeatedly called after {@link android.media.AudioRecord#read} in a loop.
   * This will be called in a different thread from the thread that's reading from AudioRecord.
   *
   * @param read The number of bytes that was read into the buffer after
   * {@link android.media.AudioRecord#read} was called
   * @param pcmBuffer The buffer containing PCM data from call audio
   * @throws IOException
   */
  protected abstract void onPcmBufferRead(int read, byte[] pcmBuffer) throws IOException;

  /**
   * Called after recording is done and the file should be completed. The file descriptor from
   * {@link #onRecordingStart} is still active here and will be closed after this method returns, so
   * any resources using that file descriptor can still use it.
   * @throws IOException
   */
  protected abstract void onRecordingStop() throws IOException;

  /**
   * Resets the call recorder so that {@link #startRecording()} can be called again. In practice,
   * {@link #startRecording()} is never called again, and a new BaseCallRecorder is remade.
   */
  protected abstract void reset();

  /**
   * Called when subclasses should clean up resources.
   */
  protected abstract void onClose();

  @Override
  public final void close() {
    if (mIsClosed) {
      return;
    }
    mIsClosed = true;
    mIsRecording = false;
    onClose();
    mAudioBufArrayPool.close();
    mAudioRecord.release();
    mAudioBufferProducerExecutor.shutdownNow();
    mAudioBufferConsumerExecutor.shutdownNow();
    if (mWritePfd != null) {
      try {
        mWritePfd.close();
      } catch (IOException ignored) {
      }
    }
  }
}
