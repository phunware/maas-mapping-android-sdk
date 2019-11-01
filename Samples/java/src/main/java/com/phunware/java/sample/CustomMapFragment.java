package com.phunware.java.sample;

import android.os.Bundle;
import android.util.TypedValue;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CameraPosition.Builder;
import com.google.android.gms.maps.model.LatLng;
import com.phunware.mapping.MapFragment;
import com.phunware.mapping.OnPhunwareMapReadyCallback;
import com.phunware.mapping.SupportMapFragment;

import androidx.appcompat.app.AppCompatActivity;


/**
 * Example of extending {@link MapFragment} to customise
 * the initial camera location and the zooming level of the map.
 */
public class CustomMapFragment extends SupportMapFragment {

    public static CustomMapFragment getInstance(AppCompatActivity activity, OnPhunwareMapReadyCallback callback) {

        TypedValue typedValue = new TypedValue();
        activity.getResources().getValue(R.integer.building_latitude, typedValue, true);
        double lat = typedValue.getFloat();
        activity.getResources().getValue(R.integer.building_longitude, typedValue, true);
        double lng = typedValue.getFloat();
        float zoomLevel = 16f;

        CustomMapFragment mf =  new CustomMapFragment();
        mf.getPhunwareMapAsync(callback);
        GoogleMapOptions options = new GoogleMapOptions();
        CameraPosition cameraOptions = new Builder()
            .target(new LatLng(lat, lng))
            .zoom(zoomLevel)
            .build();

        Bundle args = new Bundle();
        args.putParcelable(MAP_OPTIONS_KEY, options.camera(cameraOptions));

        mf.setArguments(args);
        return mf;
    }

    // "MapOptions" is the argument key that Google is using right now when you use their MapFragment directly.
    // This is an implementation detail of the Google Maps library that is subject to change without notice
    private static final String MAP_OPTIONS_KEY = "MapOptions";
}