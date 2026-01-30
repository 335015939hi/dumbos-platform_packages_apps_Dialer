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
package com.android.dialer.backup;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;
import com.android.dialer.common.LogUtil;
import com.android.dialer.logging.DialerImpression;
import com.android.dialer.logging.Logger;
import java.io.IOException;

// TODO: This class previously used Google Play Services backup library (PersistentBackupAgentHelper)
// which has been removed to avoid GPS dependencies. An alternative backup solution should be
// implemented using Android's BackupAgentHelper with SharedPreferencesBackupHelper.
// Shared preferences to backup: "com.google.android.dialer_preferences", "com.google.android.dialer", "com.android.dialer"
public class DialerPersistentBackupAgent extends BackupAgent {

  @Override
  public void onBackup(
      ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState)
      throws IOException {
    Logger.get(this).logImpression(DialerImpression.Type.BACKUP_KEY_VALUE_ON_BACKUP);
    LogUtil.i("DialerPersistentBackupAgent.onBackup", "onBackup called - backup not implemented");
    // TODO: Implement backup using BackupAgentHelper or custom solution
  }

  @Override
  public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState)
      throws IOException {
    Logger.get(this).logImpression(DialerImpression.Type.BACKUP_KEY_VALUE_ON_RESTORE);
    LogUtil.i("DialerPersistentBackupAgent.onRestore", "restore called - restore not implemented");
    // TODO: Implement restore using BackupAgentHelper or custom solution
  }
}
