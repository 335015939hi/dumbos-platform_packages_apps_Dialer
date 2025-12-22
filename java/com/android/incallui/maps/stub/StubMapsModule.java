package com.android.incallui.maps.stub;

import com.android.incallui.maps.Maps;
import dagger.Module;
import dagger.Provides;

// OpenStreetMap implementation module using osmdroid
@Module
public class StubMapsModule {
  @Provides
  static Maps provideMaps() {
    return new OsmMaps();
  }
}
