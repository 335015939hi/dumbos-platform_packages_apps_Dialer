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
package com.android.voicemail.impl.transcribe;

import android.content.Context;
// import android.os.Build;
import com.android.dialer.configprovider.ConfigProviderComponent;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Provides configuration values needed to connect to the transcription server.
 *
 * <p>TODO: Replace Google's voicemail transcription service with an open-source alternative.
 * The transcription feature has been disabled to remove the Google Play Services dependency.
 * Potential open-source alternatives:
 * - OpenAI Whisper (can run locally on-device or via API)
 * - Mozilla DeepSpeech (local, open-source speech recognition)
 * - Vosk (offline speech recognition, supports Android)
 * - Coqui STT (open-source, formerly Mozilla DeepSpeech)
 *
 * <p>To re-enable transcription with an open-source backend:
 * 1. Implement a new TranscriptionClient that uses the chosen speech-to-text service
 * 2. Update isVoicemailTranscriptionAvailable() to return true
 * 3. Update getServerAddress() to point to your transcription service
 */
public class TranscriptionConfigProvider {
  @SuppressWarnings("unused")
  private final Context context;

  public TranscriptionConfigProvider(Context context) {
    this.context = context;
  }

  public boolean isVoicemailTranscriptionAvailable() {
    // TODO: Re-enable when open-source transcription backend is implemented
    // GMS-based transcription has been disabled to remove Google Play Services dependency.
    // Original implementation:
    // return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    //     && ConfigProviderComponent.get(context)
    //         .getConfigProvider()
    //         .getBoolean("voicemail_transcription_available", false);
    return false;
  }

  public String getServerAddress() {
    // TODO: Update to open-source transcription server address when implemented
    // Original Google service: "voicemailtranscription-pa.googleapis.com"
    return "";
  }

  public String getApiKey() {
    // TODO: Update API key for open-source transcription service when implemented
    // Original Google API key has been removed
    return "";
  }

  public String getAuthToken() {
    return null;
  }

  public boolean shouldUsePlaintext() {
    return ConfigProviderComponent.get(context)
        .getConfigProvider()
        .getBoolean("voicemail_transcription_server_use_plaintext", false);
  }

  public boolean shouldUseSyncApi() {
    return ConfigProviderComponent.get(context)
        .getConfigProvider()
        .getBoolean("voicemail_transcription_server_use_sync_api", false);
  }

  public long getMaxTranscriptionRetries() {
    return ConfigProviderComponent.get(context)
        .getConfigProvider()
        .getLong("voicemail_transcription_max_transcription_retries", 2L);
  }

  public int getMaxGetTranscriptPolls() {
    return (int)
        ConfigProviderComponent.get(context)
            .getConfigProvider()
            .getLong("voicemail_transcription_max_get_transcript_polls", 20L);
  }

  public long getInitialGetTranscriptPollDelayMillis() {
    return ConfigProviderComponent.get(context)
        .getConfigProvider()
        .getLong(
            "voicemail_transcription_get_initial_transcript_poll_delay_millis",
            TimeUnit.SECONDS.toMillis(1));
  }

  public long getMaxGetTranscriptPollTimeMillis() {
    return ConfigProviderComponent.get(context)
        .getConfigProvider()
        .getLong(
            "voicemail_transcription_get_max_transcript_poll_time_millis",
            TimeUnit.MINUTES.toMillis(20));
  }

  public boolean isVoicemailDonationAvailable() {
    return ConfigProviderComponent.get(context)
        .getConfigProvider()
        .getBoolean("voicemail_transcription_donation_available", false);
  }

  public boolean useClientGeneratedVoicemailIds() {
    return ConfigProviderComponent.get(context)
        .getConfigProvider()
        .getBoolean("voicemail_transcription_client_generated_voicemail_ids", false);
  }

  @Override
  public String toString() {
    return String.format(
        Locale.US,
        "{ address: %s, api key: %s, auth token: %s, plaintext: %b, sync: %b, retries: %d, polls:"
            + " %d, poll ms: %d }",
        getServerAddress(),
        getApiKey(),
        getAuthToken(),
        shouldUsePlaintext(),
        shouldUseSyncApi(),
        getMaxTranscriptionRetries(),
        getMaxGetTranscriptPolls(),
        getMaxGetTranscriptPollTimeMillis());
  }
}
