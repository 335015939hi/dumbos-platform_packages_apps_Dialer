package com.android.dialer.callrecord.impl;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

/**
 * Records audio using {@link android.media.AudioRecord} which is the same framework API that Google
 * Dialer uses for call recording / call notes. Subclasses decide what to do with the PCM data
 * from AudioRecord.
 */
public abstract class BaseCallRecorder implements Closeable {

  private static final String TAG = "BaseCallRecorder";

  private static final int DURATION_TO_READ_MS = 1000;

  /**
   * Whether recording is active. This is used to control the main loop in the recordingTask.
   */
  private volatile boolean mIsRecording;

  protected final OutputFormat mOutputFormat;
  protected final AudioFormat mAudioFormat;
  private final AudioRecord mAudioRecord;
  protected final ContentResolver mContentResolver;

  /**
   * The URI pointing to the call recording file that we are writing to.
   */
  protected final Uri mUri;

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  @GuardedBy("this")
  private Future<?> mRecordingTask;

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
  }

  protected final long computePresentationTimeUs(int bytesRead) {
    int framesRead = bytesRead / mAudioFormat.getFrameSizeInBytes();
    return (framesRead * 1_000_000L) / mAudioFormat.getSampleRate();
  }

  public final boolean isRecording() {
    return mIsRecording && mRecordingTask != null;
  }

  public final synchronized void startRecording() {
    if (mRecordingTask != null) {
      Log.d(TAG, "existing recording task running");
      return;
    }
    if (executorService.isShutdown()) {
      Log.e(TAG, "cannot start recording after close() called");
      return;
    }
    mRecordingTask = executorService.submit(() -> {
      try {
        runRecordJob();
      } catch (Throwable t) {
        Log.e(TAG, "error when running record job", t);
        throw t;
      }
      return null;
    });
  }

  public final synchronized void stopRecordingBlocking() {
    mIsRecording = false;
    if (mRecordingTask == null) {
      return;
    }
    try {
      mRecordingTask.get(5, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      Log.w(TAG, "failed to wait for recording task to finish", e);
    }
    mRecordingTask = null;
  }

  private void runRecordJob() throws IOException {
    try (ParcelFileDescriptor pfd = mContentResolver.openFileDescriptor(mUri, "w")) {
      if (pfd == null) {
        throw new IOException("pfd for " + mUri + " failed to open");
      }
      onRecordingStart(pfd);

      final int framesInDurationMs = mAudioRecord.getSampleRate() * DURATION_TO_READ_MS / 1000;

      final int bufSize = Math.max(
              mAudioFormat.getFrameSizeInBytes() * framesInDurationMs,
              AudioRecord.getMinBufferSize(
                      mAudioRecord.getSampleRate(), mAudioRecord.getChannelConfiguration(),
                      mAudioRecord.getAudioFormat()));
      final byte[] pcmBuffer = new byte[bufSize];

      Log.d(TAG, "start recording");
      mAudioRecord.startRecording();
      mIsRecording = true;

      int bytesRead = 0;
      while (!Thread.currentThread().isInterrupted()
              && mIsRecording
              && mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
        int read = mAudioRecord.read(pcmBuffer, 0, pcmBuffer.length);
        if (read <= 0) {
          Log.e(TAG, "error on AudioRecord read: " + read);
          break;
        }

        bytesRead += read;

        onPcmBufferRead(bytesRead, read, pcmBuffer);
      }
      Log.d(TAG, "AudioRecord loop finished");
      mAudioRecord.stop();
      onRecordingStop();
    } finally {
      if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
        mAudioRecord.stop();
      }
      mIsRecording = false;
      reset();
    }
  }

  /**
   * Called before the buffer loop to prepare for recording. After this, the buffer reading loop is
   * entered and {@link #onPcmBufferRead(int, int, byte[])} will be called repeatedly.
   *
   * @param pfd A file descriptor for the call recording file in storage to write to. This is open
   *            in "w" mode, so seeking isn't possible.
   * @throws IOException if an error occurs when preparing for recording
   */
  protected abstract void onRecordingStart(ParcelFileDescriptor pfd) throws IOException;

  /**
   * Repeatedly called after {@link android.media.AudioRecord#read} in a loop.
   * @param totalRead The total number of bytes read so far
   * @param read The number of bytes that was read into the buffer after
   * {@link android.media.AudioRecord#read} was called
   * @param pcmBuffer The buffer containing PCM data from call audio
   * @throws IOException
   */
  protected abstract void onPcmBufferRead(int totalRead, int read, byte[] pcmBuffer) throws IOException;

  /**
   * Called after recording should done and the file should be completed. The file descriptor from
   * {@link #onRecordingStart} is still active here and will be closed after this method returns.
   * @throws IOException
   */
  protected abstract void onRecordingStop() throws IOException;

  /**
   * Resets the call recorder so that {@link #startRecording()} can be called again. In practice,
   * {@link #startRecording()} is never called again, and a new object is remade.
   */
  protected abstract void reset();

  /**
   * Called when subclasses should clean up resources.
   */
  protected abstract void onClose();

  @Override
  public final void close() {
    mIsRecording = false;
    onClose();
    mAudioRecord.release();
    executorService.shutdownNow();
  }
}
