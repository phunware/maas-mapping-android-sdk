package com.phunware.java.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CameraPosition.Builder;
import com.google.android.gms.maps.model.LatLng;
import com.phunware.mapping.MapFragment;
import com.phunware.mapping.OnPhunwareMapReadyCallback;

/**
 * Example of extending {@link MapFragment} to customise
 * the initial camera location and the zooming level of the map.
 */
public class CustomMapFragment extends MapFragment {

    public static MapFragment getInstance(AppCompatActivity activity, OnPhunwareMapReadyCallback callback) {

        TypedValue typedValue = new TypedValue();
        activity.getResources().getValue(R.integer.building_latitude, typedValue, true);
        double lat = typedValue.getFloat();
        activity.getResources().getValue(R.integer.building_longitude, typedValue, true);
        double lng = typedValue.getFloat();
        float zoomLevel = 16f;

        MapFragment mf =  new CustomMapFragment();
        mf.getPhunwareMapAsync(callback);
        GoogleMapOptions options = new GoogleMapOptions();
        CameraPosition cameraOptions = new Builder()
            .target(new LatLng(lat, lng))
            .zoom(zoomLevel)
            .build();

        // "MapOptions" is the argument key that Google is using right now when you use their MapFragment directly.
        // This is an implementation detail of the Google Maps library that is subject to change without notice
        Bundle args = new Bundle();
        args.putParcelable("MapOptions", options.camera(cameraOptions));

        mf.setArguments(args);
        return mf;
    }
}