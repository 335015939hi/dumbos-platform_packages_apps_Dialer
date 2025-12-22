package com.android.incallui.calllocation.stub;

import android.content.Context;
import androidx.fragment.app.Fragment;
import com.android.incallui.calllocation.CallLocation;

// TODO: Stub implementation of CallLocation - Google Play Services not available
public class StubCallLocation implements CallLocation {
  @Override
  public boolean canGetLocation(Context context) {
    return false;
  }

  @Override
  public Fragment getLocationFragment(Context context) {
    return null;
  }

  @Override
  public void close() {
    // No-op
  }
}
