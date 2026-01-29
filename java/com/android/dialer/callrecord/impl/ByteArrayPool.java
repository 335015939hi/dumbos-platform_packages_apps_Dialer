package com.android.dialer.callrecord.impl;

import java.util.ArrayDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.concurrent.GuardedBy;

/**
 * Fixed-size pool of reusable byte[] buffers
 */
public final class ByteArrayPool {
  public final class Buffer implements AutoCloseable {
    public final byte[] data;
    public int length;
    private boolean released;

    private Buffer(byte[] data) {
      this.data = data;
      this.released = true;
    }

    @Override
    public void close() {
      if (released) {
        return;
      }
      released = true;
      ByteArrayPool.this.release(this);
    }
  }

  @GuardedBy("mLock")
  private final ArrayDeque<Buffer> mFree;
  @GuardedBy("mLock")
  private final ArrayDeque<Buffer> mFilled;
  private final ReentrantLock mLock = new ReentrantLock();
  private final Condition mNotEmpty = mLock.newCondition();
  private final Condition mHasFree = mLock.newCondition();
  @GuardedBy("mLock")
  private boolean mClosed;

  public ByteArrayPool(int poolSize, int bufferSize) {
    mFree = new ArrayDeque<>(poolSize);
    mFilled = new ArrayDeque<>(poolSize);
    for (int i = 0; i < poolSize; i++) {
      mFree.addLast(new Buffer(new byte[bufferSize]));
    }
  }

  /** Producer: get a free buffer to fill. If null is returned, buffer is closed */
  public Buffer acquire() throws InterruptedException {
    mLock.lock();
    try {
      while (mFree.isEmpty() && !mClosed) {
        mHasFree.await();
      }
      // If it is closed, don't allow producer to get a buffer
      if (mClosed) {
        return null;
      }
      Buffer buffer = mFree.removeFirst();
      buffer.released = false;
      return buffer;
    } finally {
      mLock.unlock();
    }
  }

  /** Producer: hand off a filled buffer. */
  public boolean produce(Buffer buffer, int length) {
    if (length <= 0) {
      return release(buffer);
    }

    mLock.lock();
    try {
      if (mClosed) {
        return false;
      }
      buffer.length = length;
      mFilled.addLast(buffer);
      mNotEmpty.signal();
      return true;
    } finally {
      mLock.unlock();
    }
  }

  /** Consumer: take a filled buffer. Returns null if closed and empty. */
  public Buffer consume() throws InterruptedException {
    mLock.lock();
    try {
      while (mFilled.isEmpty() && !mClosed) {
        mNotEmpty.await();
      }
      // If it is closed, don't check for mClosed so that we drain the filled buffers first
      if (mFilled.isEmpty()) {
        return null;
      }
      Buffer buffer = mFilled.removeFirst();
      buffer.released = false;
      return buffer;
    } finally {
      mLock.unlock();
    }
  }

  /** Consumer: return a buffer to the free pool. */
  public boolean release(Buffer buffer) {
    mLock.lock();
    try {
      if (mClosed) {
        return false;
      }
      buffer.length = 0;
      // Arrays.fill(buffer.data, (byte) 0);
      mFree.addLast(buffer);
      mHasFree.signal();
      return true;
    } finally {
      mLock.unlock();
    }
  }

  public void close() {
    mLock.lock();
    try {
      mClosed = true;
      mNotEmpty.signalAll();
      mHasFree.signalAll();
    } finally {
      mLock.unlock();
    }
  }

  public boolean isClosed() {
    return mClosed;
  }
}
