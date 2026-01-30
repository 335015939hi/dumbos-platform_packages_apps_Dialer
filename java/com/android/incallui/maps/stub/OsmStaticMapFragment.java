package com.android.incallui.maps.stub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

// OpenStreetMap static map fragment using osmdroid
public class OsmStaticMapFragment extends Fragment {
  private static final String ARG_LATITUDE = "latitude";
  private static final String ARG_LONGITUDE = "longitude";
  private static final double DEFAULT_ZOOM = 15.0;

  private MapView mapView;
  private double latitude;
  private double longitude;

  public static OsmStaticMapFragment newInstance(double latitude, double longitude) {
    OsmStaticMapFragment fragment = new OsmStaticMapFragment();
    Bundle args = new Bundle();
    args.putDouble(ARG_LATITUDE, latitude);
    args.putDouble(ARG_LONGITUDE, longitude);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      latitude = getArguments().getDouble(ARG_LATITUDE);
      longitude = getArguments().getDouble(ARG_LONGITUDE);
    }
    
    // Configure osmdroid
    Configuration.getInstance().setUserAgentValue(
        requireContext().getPackageName());
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    mapView = new MapView(requireContext());
    mapView.setTileSource(TileSourceFactory.MAPNIK);
    mapView.setMultiTouchControls(true);
    
    // Set map center and zoom
    GeoPoint startPoint = new GeoPoint(latitude, longitude);
    mapView.getController().setZoom(DEFAULT_ZOOM);
    mapView.getController().setCenter(startPoint);
    
    // Add marker at location
    Marker marker = new Marker(mapView);
    marker.setPosition(startPoint);
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    mapView.getOverlays().add(marker);
    
    return mapView;
  }

  @Override
  public void onResume() {
    super.onResume();
    if (mapView != null) {
      mapView.onResume();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mapView != null) {
      mapView.onPause();
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (mapView != null) {
      mapView.onDetach();
    }
  }
}
