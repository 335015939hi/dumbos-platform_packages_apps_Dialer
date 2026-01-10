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

package com.android.dialer.phonenumbergeoutil.impl;

import android.content.Context;
import com.android.dialer.phonenumbergeoutil.PhoneNumberGeoUtil;
import javax.inject.Inject;

/**
 * Implementation of {@link PhoneNumberGeoUtil}.
 *
 * <p>Note: Geo description lookup is disabled to reduce APK size by ~7MB.
 * The libphonenumber geocoder dependency was removed.
 */
public class PhoneNumberGeoUtilImpl implements PhoneNumberGeoUtil {

  @Inject
  public PhoneNumberGeoUtilImpl() {}

  @Override
  public String getGeoDescription(Context context, String number, String countryIso) {
    // Geo description disabled - geocoder library removed to reduce APK size
    return null;
  }
}
