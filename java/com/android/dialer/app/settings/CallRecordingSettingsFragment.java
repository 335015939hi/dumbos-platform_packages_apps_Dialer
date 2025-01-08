package com.android.dialer.app.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragment;

import com.android.R;

public class CallRecordingSettingsFragment extends PreferenceFragment {

  @Override
  public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.call_recording_settings);
  }
}
