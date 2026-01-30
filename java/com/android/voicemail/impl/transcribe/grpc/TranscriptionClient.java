/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.android.voicemail.impl.transcribe.grpc;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import com.android.dialer.common.LogUtil;
import com.google.internal.communications.voicemailtranscription.v1.GetTranscriptRequest;
import com.google.internal.communications.voicemailtranscription.v1.SendTranscriptionFeedbackRequest;
import com.google.internal.communications.voicemailtranscription.v1.TranscribeVoicemailAsyncRequest;
import com.google.internal.communications.voicemailtranscription.v1.TranscribeVoicemailRequest;

/**
 * Wrapper around Grpc transcription server stub.
 *
 * <p>DISABLED: Google's voicemail transcription service has been removed to eliminate
 * Google Play Services dependency. This class is now a stub that returns null for all requests.
 *
 * <p>To re-enable transcription with an open-source backend:
 * 1. Implement a speech-to-text service (Whisper, Vosk, etc.)
 * 2. Implement the actual gRPC client code
 * 3. Re-enable in TranscriptionConfigProvider
 */
public class TranscriptionClient {

  // Stub constructor - transcription is disabled
  TranscriptionClient() {}

  /**
   * Stub method - transcription is disabled.
   * @return null to indicate failure
   */
  @WorkerThread
  @Nullable
  public TranscriptionResponseSync sendSyncRequest(TranscribeVoicemailRequest request) {
    LogUtil.i("TranscriptionClient.sendSyncRequest", "Transcription is disabled - returning null");
    return null;
  }

  /**
   * Stub method - transcription is disabled.
   * @return null to indicate failure
   */
  @WorkerThread
  @Nullable
  public TranscriptionResponseAsync sendUploadRequest(TranscribeVoicemailAsyncRequest request) {
    LogUtil.i("TranscriptionClient.sendUploadRequest", "Transcription is disabled - returning null");
    return null;
  }

  /**
   * Stub method - transcription is disabled.
   * @return null to indicate failure
   */
  @WorkerThread
  @Nullable
  public GetTranscriptResponseAsync sendGetTranscriptRequest(GetTranscriptRequest request) {
    LogUtil.i("TranscriptionClient.sendGetTranscriptRequest", "Transcription is disabled - returning null");
    return null;
  }

  /**
   * Stub method - transcription is disabled.
   * @return null to indicate failure
   */
  @WorkerThread
  @Nullable
  public TranscriptionFeedbackResponseAsync sendTranscriptFeedbackRequest(
      SendTranscriptionFeedbackRequest request) {
    LogUtil.i("TranscriptionClient.sendTranscriptFeedbackRequest", "Transcription is disabled - returning null");
    return null;
  }

  /*
   * Original implementation commented out - requires Google's transcription service
   *
  private final VoicemailTranscriptionServiceGrpc.VoicemailTranscriptionServiceBlockingStub stub;

  TranscriptionClient(
      VoicemailTranscriptionServiceGrpc.VoicemailTranscriptionServiceBlockingStub stub) {
    this.stub = stub;
  }

  @WorkerThread
  public TranscriptionResponseSync sendSyncRequest(TranscribeVoicemailRequest request) {
    try {
      return new TranscriptionResponseSync(stub.transcribeVoicemail(request));
    } catch (StatusRuntimeException e) {
      return new TranscriptionResponseSync(e.getStatus());
    }
  }

  @WorkerThread
  public TranscriptionResponseAsync sendUploadRequest(TranscribeVoicemailAsyncRequest request) {
    try {
      return new TranscriptionResponseAsync(stub.transcribeVoicemailAsync(request));
    } catch (StatusRuntimeException e) {
      return new TranscriptionResponseAsync(e.getStatus());
    }
  }

  @WorkerThread
  public GetTranscriptResponseAsync sendGetTranscriptRequest(GetTranscriptRequest request) {
    try {
      return new GetTranscriptResponseAsync(stub.getTranscript(request));
    } catch (StatusRuntimeException e) {
      return new GetTranscriptResponseAsync(e.getStatus());
    }
  }

  @WorkerThread
  public TranscriptionFeedbackResponseAsync sendTranscriptFeedbackRequest(
      SendTranscriptionFeedbackRequest request) {
    try {
      return new TranscriptionFeedbackResponseAsync(stub.sendTranscriptionFeedback(request));
    } catch (StatusRuntimeException e) {
      return new TranscriptionFeedbackResponseAsync(e.getStatus());
    }
  }
  */
}
