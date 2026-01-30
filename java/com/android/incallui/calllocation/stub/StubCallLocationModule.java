package com.android.incallui.calllocation.stub;

import com.android.incallui.calllocation.CallLocation;
import dagger.Module;
import dagger.Provides;

// Stub module for CallLocation (GPS location without Play Services)
@Module
public class StubCallLocationModule {
  @Provides
  static CallLocation provideCallLocation() {
    return new StubCallLocation();
  }
}
