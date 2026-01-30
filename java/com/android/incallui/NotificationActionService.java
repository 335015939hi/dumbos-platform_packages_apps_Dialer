/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.incallui;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.android.dialer.common.LogUtil;

/**
 * Service for handling notification actions. This is necessary on Android 12+
 * where activities cannot be reliably started from notification actions due to
 * background activity launch restrictions. Services have fewer restrictions.
 */
public class NotificationActionService extends Service {

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && intent.getAction() != null) {
      LogUtil.i("NotificationActionService.onStartCommand",
          "Received notification action: " + intent.getAction());

      // Forward the action to NotificationBroadcastReceiver
      NotificationBroadcastReceiver receiver = new NotificationBroadcastReceiver();
      receiver.onReceive(this, intent);
    } else {
      LogUtil.e("NotificationActionService.onStartCommand", "No action in intent");
    }

    // Stop the service immediately after handling the action
    stopSelf(startId);
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
