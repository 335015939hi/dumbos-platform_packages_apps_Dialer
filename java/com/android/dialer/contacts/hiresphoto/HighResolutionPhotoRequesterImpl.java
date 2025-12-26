/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.dialer.contacts.hiresphoto;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import androidx.annotation.VisibleForTesting;
import com.android.dialer.common.LogUtil;
import com.android.dialer.common.concurrent.Annotations.BackgroundExecutor;
import com.android.dialer.common.database.Selection;
import com.android.dialer.inject.ApplicationContext;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Use the contacts sync adapter to load high resolution photos for a Google account.
 *
 * <p>TODO: Replace with open-source alternative. This feature previously relied on Google Play
 * Services (com.google.android.gms.people.sync) to sync high-resolution contact photos.
 * Potential alternatives:
 * - Use contact photos from local storage (already works without this feature)
 * - Implement direct photo fetching from contact providers
 * - Use Glide/Coil to load and cache photos from contact URIs
 */
public class HighResolutionPhotoRequesterImpl implements HighResolutionPhotoRequester {

  // GMS dependency removed - high-res photo sync disabled
  // private static final ComponentName SYNC_HIGH_RESOLUTION_PHOTO_SERVICE =
  //     new ComponentName(
  //         "com.google.android.gms",
  //         "com.google.android.gms.people.sync.focus.SyncHighResPhotoIntentOperation");

  @SuppressWarnings("unused")
  private final Context appContext;
  private final ListeningExecutorService backgroundExecutor;

  @Inject
  HighResolutionPhotoRequesterImpl(
      @ApplicationContext Context appContext,
      @BackgroundExecutor ListeningExecutorService backgroundExecutor) {
    this.appContext = appContext;
    this.backgroundExecutor = backgroundExecutor;
  }

  @Override
  public ListenableFuture<Void> request(Uri contactUri) {
    // TODO: Implement open-source high-res photo loading alternative
    // GMS-based high-res photo sync has been disabled to remove Google Play Services dependency.
    // Contact photos will still display using locally cached versions.
    LogUtil.i(
        "HighResolutionPhotoRequesterImpl.request",
        "High-res photo sync disabled (GMS dependency removed). Using local photos.");
    return backgroundExecutor.submit(() -> null);
  }

  /*
   * The following methods were used for GMS-based photo sync and have been commented out.
   * They can be removed entirely or repurposed for an open-source alternative.
   *
  private static class RequestFailedException extends Exception {
    RequestFailedException(String message) {
      super(message);
    }

    RequestFailedException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  private void requestInternal(Uri contactUri) throws RequestFailedException {
    for (Long rawContactId : getGoogleRawContactIds(getContactId(contactUri))) {
      Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
      Intent intent = new Intent();
      intent.setAction("com.google.android.gms.people.sync.focus.SYNC_HIGH_RES_PHOTO");
      intent.setComponent(SYNC_HIGH_RESOLUTION_PHOTO_SERVICE);
      intent.setDataAndType(rawContactUri, RawContacts.CONTENT_ITEM_TYPE);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      try {
        LogUtil.i(
            "HighResolutionPhotoRequesterImpl.requestInternal",
            "requesting photo for " + rawContactUri);
        appContext.sendBroadcast(intent);
      } catch (IllegalStateException | SecurityException e) {
        throw new RequestFailedException("unable to start sync adapter", e);
      }
    }
  }

  private long getContactId(Uri contactUri) throws RequestFailedException {
    try (Cursor cursor =
        appContext
            .getContentResolver()
            .query(contactUri, new String[] {Contacts._ID}, null, null, null)) {
      if (cursor == null || !cursor.moveToFirst()) {
        throw new RequestFailedException("cannot get contact ID");
      }
      return cursor.getLong(0);
    }
  }

  private List<Long> getGoogleRawContactIds(long contactId) throws RequestFailedException {
    List<Long> result = new ArrayList<>();
    Selection selection =
        Selection.column(RawContacts.CONTACT_ID)
            .is("=", contactId)
            .buildUpon()
            .and(Selection.column(RawContacts.ACCOUNT_TYPE).is("=", "com.google"))
            .build();
    try (Cursor cursor =
        appContext
            .getContentResolver()
            .query(
                RawContacts.CONTENT_URI,
                new String[] {RawContacts._ID, RawContacts.ACCOUNT_TYPE},
                selection.getSelection(),
                selection.getSelectionArgs(),
                null)) {
      if (cursor == null) {
        throw new RequestFailedException("null cursor from raw contact IDs");
      }
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        result.add(cursor.getLong(0));
      }
    }
    return result;
  }
  */
}
