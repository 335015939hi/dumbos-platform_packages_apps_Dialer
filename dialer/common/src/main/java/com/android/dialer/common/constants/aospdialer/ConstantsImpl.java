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
 * limitations under the License.
 */

package com.android.dialer.common.constants;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.dialer.common.BuildConfig;
import com.android.dialer.common.proguard.UsedByReflection;

/** Provider config values for AOSP Dialer. */
@UsedByReflection(value = "Constants.java")
public class ConstantsImpl extends Constants {

  private String getPackageName() {
    if (BuildConfig.DEBUG) {
      return "com.android.dialer.debug";
    } else {
      return "com.android.dialer";
    }
  }

  @Override
  @NonNull
  public String getFilteredNumberProviderAuthority() {
    return getPackageName() + ".filterednumberprovider";
  }

  @Override
  @NonNull
  public String getFileProviderAuthority() {
    return getPackageName() + ".files";
  }

  @NonNull
  @Override
  public String getAnnotatedCallLogProviderAuthority() {
    return getPackageName() + ".annotatedcalllog";
  }

  @NonNull
  @Override
  public String getPhoneLookupHistoryProviderAuthority() {
    return getPackageName() + ".phonelookuphistory";
  }

  @NonNull
  @Override
  public String getPreferredSimFallbackProviderAuthority() {
    return getPackageName() + ".preferredsimfallback";
  }

  @Override
  public String getUserAgent(Context context) {
    return null;
  }

  @NonNull
  @Override
  public String getSettingsActivity() {
    return "com.android.dialer.app.settings.DialerSettingsActivity";
  }
}
