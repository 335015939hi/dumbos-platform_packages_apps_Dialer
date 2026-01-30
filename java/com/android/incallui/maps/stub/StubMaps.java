package com.android.incallui.maps.stub;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.android.incallui.maps.Maps;

// TODO: Stub implementation of Maps - Google Play Services Maps not available
public class StubMaps implements Maps {
  @Override
  public boolean isAvailable() {
    return false;
  }

  @Override
  @NonNull
  public Fragment createStaticMapFragment(@NonNull Location location) {
    throw new UnsupportedOperationException("Maps not available");
  }
}
