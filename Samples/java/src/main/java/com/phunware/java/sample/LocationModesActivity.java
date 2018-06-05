package com.phunware.java.sample;

/* Copyright (C) 2018 Phunware, Inc.

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL Phunware, Inc. BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Phunware, Inc. shall
not be used in advertising or otherwise to promote the sale, use or
other dealings in this Software without prior written authorization
from Phunware, Inc. */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.phunware.core.PwCoreSession;
import com.phunware.core.PwLog;
import com.phunware.location.provider_managed.ManagedProviderFactory;
import com.phunware.location.provider_managed.PwManagedLocationProvider;
import com.phunware.mapping.MapFragment;
import com.phunware.mapping.OnPhunwareMapReadyCallback;
import com.phunware.mapping.PhunwareMap;
import com.phunware.mapping.manager.Callback;
import com.phunware.mapping.manager.PhunwareMapManager;
import com.phunware.mapping.model.Building;
import com.phunware.mapping.model.FloorOptions;

import java.lang.ref.WeakReference;

import static com.phunware.mapping.manager.PhunwareMapManager.MODE_FOLLOW_ME;
import static com.phunware.mapping.manager.PhunwareMapManager.MODE_LOCATE_ME;

public class LocationModesActivity extends AppCompatActivity implements OnPhunwareMapReadyCallback,
        Building.OnFloorChangedListener {
    private static final String TAG = LocationModesActivity.class.getSimpleName();
    private static final String PREFERENCE_NAME = "location_mode_sample";
    private static final String PREF_LOCATION_MODE = "location_mode";
    private static final String PREF_LOCATION_FOLLOW = "follow me";
    private static final String PREF_LOCATION_LOCATE = "locate me";
    private static final String PREF_LOCATION_NORMAL = "normal";

    private PhunwareMapManager mapManager;
    private Building currentBuilding;
    private Spinner floorSpinner;
    private ArrayAdapter<FloorOptions> floorSpinnerAdapter;

    // Location Mode Variables
    private FloatingActionButton locationModeFab;
    private Handler trackingModeHandler;
    private Runnable trackingModeRunnable;
    private boolean isTrackingModeTimerRunning = false;
    private String previousTrackingMode;

    View.OnClickListener locationModeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onLocationModeFabClicked();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_modes);

        floorSpinner = findViewById(R.id.floorSpinner);
        floorSpinnerAdapter = new FloorAdapter(this);
        floorSpinner.setAdapter(floorSpinnerAdapter);
        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FloorOptions floor = floorSpinnerAdapter.getItem((int) id);
                if (currentBuilding != null && floor != null) {
                    currentBuilding.selectFloor(floor.getLevel());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        locationModeFab = findViewById(R.id.location_mode_fab);
        locationModeFab.setOnClickListener(locationModeListener);

        trackingModeHandler = new Handler(getMainLooper());
        trackingModeRunnable = new Runnable() {
            @Override
            public void run() {
                isTrackingModeTimerRunning = false;
                if (mapManager.isBluedotVisibleOnMap() && mapManager.isBluedotVisibleOnFloor()) {
                    PwLog.d(TAG, "Bluedot is visible -- resetting tracking mode");
                    switch (previousTrackingMode) {
                        case PREF_LOCATION_LOCATE:
                            mapManager.setMyLocationMode(MODE_LOCATE_ME);
                            break;
                        case PREF_LOCATION_FOLLOW:
                            mapManager.setMyLocationMode(MODE_FOLLOW_ME);
                            break;
                        default:
                            mapManager.setMyLocationMode(PhunwareMapManager.MODE_NORMAL);
                            break;
                    }
                    setSavedLocationMode(previousTrackingMode);
                    updateLocationModeFab();
                } else {
                    PwLog.d(TAG, "Bluedot is not visible -- breaking tracking mode");
                }
            }
        };

        // Register the Phunware API keys
        PwCoreSession.getInstance().registerKeys(this);

        // Create the map manager and fragment used to load the building
        mapManager = PhunwareMapManager.create(this);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.addOnTouchListener(new MapFragment.OnMapMovedListener() {
                @Override
                public void onMapMoved() {
                    if (mapManager != null && mapManager.isBluedotVisibleOnFloor()) {
                        int trackingMode = mapManager.getMyLocationMode();
                        if (isTrackingModeTimerRunning || trackingMode == MODE_LOCATE_ME
                                || trackingMode == MODE_FOLLOW_ME) {
                            updateLocationModeBehavior();
                        }
                    }
                }
            });
            mapFragment.getPhunwareMapAsync(this);
        }
    }

    @Override
    public void onPhunwareMapReady(final PhunwareMap phunwareMap) {
        // Retrieve buildingId from integers.xml
        int buildingId = getResources().getInteger(R.integer.buildingId);

        phunwareMap.getGoogleMap().getUiSettings().setMapToolbarEnabled(false);
        phunwareMap.getGoogleMap().setMapStyle(MapStyleOptions.loadRawResourceStyle(
                LocationModesActivity.this, R.raw.map_style));

        mapManager.setPhunwareMap(phunwareMap);
        mapManager.addBuilding(buildingId,
                new Callback<Building>() {
                    @Override
                    public void onSuccess(Building building) {
                        Log.d(TAG, "Building loaded successfully");
                        currentBuilding = building;

                        // Populate floor spinner
                        floorSpinnerAdapter.clear();
                        floorSpinnerAdapter.addAll(building.getBuildingOptions().getFloors());

                        // Add a listener to monitor floor switches
                        mapManager.addFloorChangedListener(LocationModesActivity.this);

                        // Initialize a location provider
                        setManagedLocationProvider(building);

                        // Set building to initial floor value
                        FloorOptions initialFloor = building.initialFloor();
                        building.selectFloor(initialFloor.getLevel());

                        // Animate the camera to the building at an appropriate zoom level
                        CameraUpdate cameraUpdate = CameraUpdateFactory
                                .newLatLngBounds(initialFloor.getBounds(), 4);
                        phunwareMap.getGoogleMap().animateCamera(cameraUpdate);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.d(TAG, "Error when loading building -- " + throwable.getMessage());
                    }
                });
    }

    private void setManagedLocationProvider(Building building) {
        ManagedProviderFactory.ManagedProviderFactoryBuilder builder =
                new ManagedProviderFactory.ManagedProviderFactoryBuilder();
        builder.application(getApplication())
                .context(new WeakReference<Context>(getApplication()))
                .buildingId(String.valueOf(building.getId()));
        ManagedProviderFactory factory = builder.build();
        PwManagedLocationProvider managedProvider
                = (PwManagedLocationProvider) factory.createLocationProvider();
        mapManager.setLocationProvider(managedProvider, building);
        mapManager.setMyLocationEnabled(true);
    }

    private void onLocationModeFabClicked() {
        String mode = getSavedLocationMode();

        // Rotate to the next location mode
        if (mode.equalsIgnoreCase(PREF_LOCATION_FOLLOW)) {
            mapManager.setMyLocationMode(PhunwareMapManager.MODE_NORMAL);
            setSavedLocationMode(PREF_LOCATION_NORMAL);
        } else if (mode.equalsIgnoreCase(PREF_LOCATION_LOCATE)) {
            mapManager.setMyLocationMode(MODE_FOLLOW_ME);
            setSavedLocationMode(PREF_LOCATION_FOLLOW);
        } else {
            mapManager.setMyLocationMode(MODE_LOCATE_ME);
            setSavedLocationMode(PREF_LOCATION_LOCATE);
        }

        updateLocationModeFab();
    }

    private void updateLocationModeFab() {
        if (locationModeFab != null) {
            String mode = getSavedLocationMode();

            // Update fab to match current location mode
            if (mode.equalsIgnoreCase(PREF_LOCATION_FOLLOW)) {
                locationModeFab.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_compass));
                locationModeFab.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.colorAccent)));
            } else if (mode.equalsIgnoreCase(PREF_LOCATION_LOCATE)) {
                locationModeFab.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_my_location));
                locationModeFab.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.colorAccent)));
            } else {
                locationModeFab.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_my_location));
                locationModeFab.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.inactive)));
            }

        }
    }

    public void updateLocationModeBehavior() {
        // Save current tracking mode and break tracking mode
        if (!isTrackingModeTimerRunning) {
            PwLog.d(TAG, "Breaking tracking mode while timer is running");
            previousTrackingMode = getSavedLocationMode();
            mapManager.setMyLocationMode(PhunwareMapManager.MODE_NORMAL);
            setSavedLocationMode(PREF_LOCATION_NORMAL);
            updateLocationModeFab();
        }

        // Cancel task if it is already running
        if (isTrackingModeTimerRunning) {
            PwLog.d(TAG, "Cancelling existing tracking mode timer");
            trackingModeHandler.removeCallbacks(trackingModeRunnable);
            isTrackingModeTimerRunning = false;
        }

        PwLog.d(TAG, "Starting tracking mode timer");
        isTrackingModeTimerRunning = true;
        int trackingModeSwitchInterval = 10000; // 10 seconds by default
        trackingModeHandler.postDelayed(trackingModeRunnable, trackingModeSwitchInterval);
    }

    private String getSavedLocationMode() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, 0);
        return preferences.getString(PREF_LOCATION_MODE, PREF_LOCATION_NORMAL);
    }

    private void setSavedLocationMode(final String mode) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, 0);
        preferences.edit()
                .putString(PREF_LOCATION_MODE, mode)
                .apply();
    }

    @Override
    public void onFloorChanged(Building building, long floorId) {
        for (int index = 0; index < floorSpinnerAdapter.getCount(); index++) {
            FloorOptions floor = floorSpinnerAdapter.getItem(index);
            if (floor != null && floor.getId() == floorId) {
                if (floorSpinner.getSelectedItemPosition() != index) {
                    final int indexFinal = index;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            floorSpinner.setSelection(indexFinal);
                        }
                    });
                    break;
                }
            }
        }
    }
}
