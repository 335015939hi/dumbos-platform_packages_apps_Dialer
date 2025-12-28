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

package com.android.dialer.main.impl.bottomnav;

import android.content.Context;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.android.dialer.common.Assert;
import com.android.dialer.common.LogUtil;
import com.android.dialer.logging.DialerImpression;
import com.android.dialer.logging.Logger;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import com.android.dialer.R;

/** Dialer Bottom Nav Bar for {@link MainActivity} using Material3 BottomNavigationView. */
public final class BottomNavBar extends BottomNavigationView {

  /** Index for each tab in the bottom nav. */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    TabIndex.SPEED_DIAL,
    TabIndex.CALL_LOG,
    TabIndex.CONTACTS,
    TabIndex.VOICEMAIL,
  })
  public @interface TabIndex {
    int SPEED_DIAL = 0;
    int CALL_LOG = 1;
    int CONTACTS = 2;
    int VOICEMAIL = 3;
  }

  private final List<OnBottomNavTabSelectedListener> listeners = new ArrayList<>();
  private @TabIndex int selectedTab;

  public BottomNavBar(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    // Set up Material3 BottomNavigationView listener
    setOnItemSelectedListener(item -> {
      int itemId = item.getItemId();
      @TabIndex int newTab;

      if (itemId == R.id.speed_dial_tab) {
        newTab = TabIndex.SPEED_DIAL;
        if (selectedTab != TabIndex.SPEED_DIAL) {
          Logger.get(getContext())
              .logImpression(DialerImpression.Type.MAIN_SWITCH_TAB_TO_FAVORITE);
        }
      } else if (itemId == R.id.call_log_tab) {
        newTab = TabIndex.CALL_LOG;
        if (selectedTab != TabIndex.CALL_LOG) {
          Logger.get(getContext())
              .logImpression(DialerImpression.Type.MAIN_SWITCH_TAB_TO_CALL_LOG);
        }
      } else if (itemId == R.id.contacts_tab) {
        newTab = TabIndex.CONTACTS;
        if (selectedTab != TabIndex.CONTACTS) {
          Logger.get(getContext())
              .logImpression(DialerImpression.Type.MAIN_SWITCH_TAB_TO_CONTACTS);
        }
      } else if (itemId == R.id.voicemail_tab) {
        newTab = TabIndex.VOICEMAIL;
        if (selectedTab != TabIndex.VOICEMAIL) {
          Logger.get(getContext())
              .logImpression(DialerImpression.Type.MAIN_SWITCH_TAB_TO_VOICEMAIL);
        }
      } else {
        return false;
      }

      selectedTab = newTab;
      updateListeners(selectedTab);
      return true;
    });
  }

  /**
   * Select tab for user and non-user click.
   *
   * @param tab {@link TabIndex}
   */
  public void selectTab(@TabIndex int tab) {
    selectedTab = tab;
    int menuItemId = getMenuItemIdForTab(tab);
    setSelectedItemId(menuItemId);
  }

  /**
   * Displays or hides the voicemail tab.
   *
   * <p>In the event that the voicemail tab was earlier visible but is now no longer visible, we
   * move to the speed dial tab.
   *
   * @param showTab whether to hide or show the voicemail
   */
  public void showVoicemail(boolean showTab) {
    LogUtil.i("OldMainActivityPeer.showVoicemail", "showing Tab:%b", showTab);
    MenuItem voicemailItem = getMenu().findItem(R.id.voicemail_tab);
    boolean wasVisible = voicemailItem.isVisible();
    voicemailItem.setVisible(showTab);

    if (wasVisible && !showTab && getSelectedTab() == TabIndex.VOICEMAIL) {
      LogUtil.i("OldMainActivityPeer.showVoicemail", "hid VM tab and moved to speed dial tab");
      selectTab(TabIndex.SPEED_DIAL);
    }
  }

  public void setNotificationCount(@TabIndex int tab, int count) {
    int menuItemId = getMenuItemIdForTab(tab);
    BadgeDrawable badge = getOrCreateBadge(menuItemId);

    if (count > 0) {
      badge.setVisible(true);
      badge.setNumber(count);
    } else {
      badge.setVisible(false);
      badge.clearNumber();
    }
  }

  public void addOnTabSelectedListener(OnBottomNavTabSelectedListener listener) {
    listeners.add(listener);
  }

  private void updateListeners(@TabIndex int tabIndex) {
    for (OnBottomNavTabSelectedListener listener : listeners) {
      switch (tabIndex) {
        case TabIndex.SPEED_DIAL:
          listener.onSpeedDialSelected();
          break;
        case TabIndex.CALL_LOG:
          listener.onCallLogSelected();
          break;
        case TabIndex.CONTACTS:
          listener.onContactsSelected();
          break;
        case TabIndex.VOICEMAIL:
          listener.onVoicemailSelected();
          break;
        default:
          throw Assert.createIllegalStateFailException("Invalid tab: " + tabIndex);
      }
    }
  }

  @TabIndex
  public int getSelectedTab() {
    return selectedTab;
  }

  /**
   * Maps tab index to menu item ID.
   */
  private int getMenuItemIdForTab(@TabIndex int tab) {
    switch (tab) {
      case TabIndex.SPEED_DIAL:
        return R.id.speed_dial_tab;
      case TabIndex.CALL_LOG:
        return R.id.call_log_tab;
      case TabIndex.CONTACTS:
        return R.id.contacts_tab;
      case TabIndex.VOICEMAIL:
        return R.id.voicemail_tab;
      default:
        throw new IllegalArgumentException("Invalid tab: " + tab);
    }
  }

  /** Listener for bottom nav tab's on click events. */
  public interface OnBottomNavTabSelectedListener {

    /** Speed dial tab was clicked. */
    void onSpeedDialSelected();

    /** Call Log tab was clicked. */
    void onCallLogSelected();

    /** Contacts tab was clicked. */
    void onContactsSelected();

    /** Voicemail tab was clicked. */
    void onVoicemailSelected();
  }
}
