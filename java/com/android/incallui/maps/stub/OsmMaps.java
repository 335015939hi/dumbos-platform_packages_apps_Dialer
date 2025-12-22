package com.android.incallui.maps.stub;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.android.incallui.maps.Maps;

// OpenStreetMap implementation using osmdroid
public class OsmMaps implements Maps {
  @Override
  public boolean isAvailable() {
    // OSM is always available (no Play Services required)
    return true;
  }

  @Override
  @NonNull
  public Fragment createStaticMapFragment(@NonNull Location location) {
    return OsmStaticMapFragment.newInstance(location.getLatitude(), location.getLongitude());
  }
}
