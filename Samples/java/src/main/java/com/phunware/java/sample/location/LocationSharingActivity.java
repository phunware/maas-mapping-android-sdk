package com.phunware.java.sample.location;

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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.phunware.core.PwCoreSession;
import com.phunware.core.PwLog;
import com.phunware.java.sample.R;
import com.phunware.java.sample.adapter.FloorAdapter;
import com.phunware.java.sample.location.util.BitmapUtils;
import com.phunware.java.sample.location.util.LatLngInterpolator;
import com.phunware.java.sample.location.util.MarkerAnimation;
import com.phunware.java.sample.location.util.PersonMarker;
import com.phunware.java.sample.poi.CustomPOIActivity;
import com.phunware.location.provider_managed.ManagedProviderFactory;
import com.phunware.location.provider_managed.PwManagedLocationProvider;
import com.phunware.mapping.OnPhunwareMapReadyCallback;
import com.phunware.mapping.PhunwareMap;
import com.phunware.mapping.SupportMapFragment;
import com.phunware.mapping.bluedot.SharedLocationCallback;
import com.phunware.mapping.manager.Callback;
import com.phunware.mapping.manager.PhunwareMapManager;
import com.phunware.mapping.model.Building;
import com.phunware.mapping.model.FloorOptions;
import com.phunware.mapping.model.SharedLocation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LocationSharingActivity extends AppCompatActivity implements OnPhunwareMapReadyCallback,
        Building.OnFloorChangedListener, SharedLocationCallback {
    private static final String TAG = CustomPOIActivity.class.getSimpleName();
    public static final String PREFERENCE_NAME = "location sharing sample";
    public static final String PREF_DEVICE_NAME = "device name";
    public static final String PREF_DEVICE_TYPE = "device type";

    private PhunwareMap phunwareMap;
    private SupportMapFragment mapFragment;
    private PhunwareMapManager mapManager;
    private Building currentBuilding;
    private Spinner floorSpinner;
    private ArrayAdapter<FloorOptions> spinnerAdapter;

    private Map<String, PersonMarker> friendLocationMap;
    private Map<String, Integer> friendColorMap;
    private LatLngInterpolator linearInterpolator = new LatLngInterpolator.Linear();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_sharing);

        PwLog.setShowLog(true);

        friendLocationMap = new HashMap<>();
        friendColorMap = new HashMap<>();

        Button changeDeviceInfoButton = findViewById(R.id.change_device_info_button);
        changeDeviceInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeviceInfoDialog();
            }
        });

        initFloorSpinner();

        // Create the map manager used to load the building
        mapManager = PhunwareMapManager.create(this);

        // Register the Phunware API keys
        PwCoreSession.getInstance().registerKeys(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getPhunwareMapAsync(this);
        }
    }

    private void initFloorSpinner() {
        floorSpinner = findViewById(R.id.floorSpinner);
        spinnerAdapter = new FloorAdapter(this);
        floorSpinner.setAdapter(spinnerAdapter);
        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FloorOptions floor = spinnerAdapter.getItem((int) id);
                if (currentBuilding != null && floor != null) {
                    currentBuilding.selectFloor(floor.getLevel());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentBuilding == null) {
            initFloorSpinner();
            if (mapFragment != null) {
                mapFragment.getPhunwareMapAsync(this);
            }
        }

        if (mapManager != null) {
            mapManager.setMyLocationEnabled(true);
            startLocationSharing();
            mapManager.startRetrievingSharedLocations(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mapManager != null) {
            mapManager.stopSharingUserLocation();
            mapManager.stopRetrievingSharedLocations();
            mapManager.setMyLocationEnabled(false);
        }
    }

    @Override
    public void onFloorChanged(Building building, long floorId) {
        // Find the floorId in our spinner and set it (if it differs from the currently set value)
        for (int index = 0; index < spinnerAdapter.getCount(); index++) {
            FloorOptions floor = spinnerAdapter.getItem(index);
            if (floor != null && floor.getId() == floorId) {
                if (floorSpinner.getSelectedItemPosition() != index) {
                    floorSpinner.setSelection(index);
                    break;
                }
            }
        }

        // Stop location sharing on the previous floor and resume on the new floor
        if (mapManager != null) {
            mapManager.stopRetrievingSharedLocations();
            mapManager.startRetrievingSharedLocations(floorId, this);
        }
    }

    @Override
    public void onPhunwareMapReady(final PhunwareMap phunwareMap) {
        // Retrieve buildingId from integers.xml
        int buildingId = getResources().getInteger(R.integer.buildingId);
        this.phunwareMap = phunwareMap;

        phunwareMap.getGoogleMap().getUiSettings().setMapToolbarEnabled(false);
        phunwareMap.getGoogleMap().setMapStyle(MapStyleOptions.loadRawResourceStyle(
                LocationSharingActivity.this, R.raw.map_style));

        mapManager.setPhunwareMap(phunwareMap);
        mapManager.addBuilding(buildingId,
                new Callback<Building>() {
                    @Override
                    public void onSuccess(Building building) {
                        Log.d(TAG, "Building loaded successfully");
                        currentBuilding = building;

                        // Populate floor spinner
                        spinnerAdapter.clear();
                        spinnerAdapter.addAll(building.getBuildingOptions().getFloors());

                        // Add a listener to monitor floor switches
                        mapManager.addFloorChangedListener(LocationSharingActivity.this);

                        // Initialize a location provider
                        setManagedLocationProvider(building);

                        // Set building to initial floor value
                        FloorOptions initialFloor = building.initialFloor();
                        building.selectFloor(initialFloor.getLevel());

                        // Animate the camera to the building at an appropriate zoom level
                        CameraUpdate cameraUpdate = CameraUpdateFactory
                                .newLatLngBounds(initialFloor.getBounds(), 4);
                        phunwareMap.getGoogleMap().animateCamera(cameraUpdate);


                        // Start sharing location with other users
                        startLocationSharing();
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

    private void startLocationSharing() {
        mapManager.startSharingUserLocation(getDeviceName(this),
                getDeviceType(this));
    }

    private void showDeviceInfoDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.action_set_device_info));
        alertDialogBuilder.setMessage(getString(R.string.set_user_info_dialog_message));
        alertDialogBuilder.setCancelable(false);

        final ViewGroup nullParent = null;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_location_sharing, nullParent);
        final EditText deviceNameInput = view.findViewById(R.id.set_device_name_input);
        deviceNameInput.setText(getDeviceName(this));
        deviceNameInput.setSelection(deviceNameInput.getText().length());

        final EditText deviceTypeInput = view.findViewById(R.id.set_device_type_input);
        deviceTypeInput.setText(getDeviceType(this));

        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton(getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String deviceName = deviceNameInput.getText().toString();
                        String deviceType = deviceTypeInput.getText().toString();
                        setDeviceName(LocationSharingActivity.this, deviceName);
                        setDeviceType(LocationSharingActivity.this, deviceType);
                        if (mapManager != null) {
                            mapManager.updateDeviceName(deviceName);
                            mapManager.updateDeviceType(deviceType);
                        }
                    }
                });
        alertDialogBuilder.setNegativeButton(getString(R.string.button_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do Nothing
                    }
                });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public static void setDeviceType(Context context, String deviceType) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, 0);
        preferences.edit()
                .putString(PREF_DEVICE_TYPE, deviceType)
                .apply();
    }

    public static String getDeviceType(Context context) {
        // Get device type from shared preferences
        // Default to empty string if no type is set
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, 0);
        return preferences.getString(PREF_DEVICE_TYPE, "");
    }

    public static void setDeviceName(Context context, String deviceName) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, 0);
        preferences.edit()
                .putString(PREF_DEVICE_NAME, deviceName)
                .apply();
    }

    public static String getDeviceName(Context context) {
        // Get device name from shared preferences
        // Default to device manufacturer/model info if no device name is set
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, 0);
        return preferences.getString(PREF_DEVICE_NAME, getDeviceModelInfo());
    }

    public static String getDeviceModelInfo() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }

    /**
     * Shared Location Listener Callbacks
     */

    @Override
    public void onSuccess(final List<SharedLocation> sharedLocationList) {
        PwLog.d(TAG, "Successfully retrieved other user's locations");

        // Remove ourself from this list
        final List<SharedLocation> sharedLocationListWithoutSelf = new ArrayList<>();
        for (int i = 0; i < sharedLocationList.size(); i++) {
            String deviceId = PwCoreSession.getInstance().getSessionData().getDeviceId();
            SharedLocation location = sharedLocationList.get(i);
            if (!deviceId.equals(location.getDeviceId())) {
                sharedLocationListWithoutSelf.add(location);
            }
        }

        // Create a set of the new device id's
        Set<String> updatedDeviceIds = new HashSet<>();
        for (SharedLocation friend : sharedLocationListWithoutSelf) {
            updatedDeviceIds.add(friend.getDeviceId());
        }

        final List<String> staleDeviceIds = new ArrayList<>();

        // Use the set to diff against our current map
        // And remove old markers on our map
        for (Map.Entry<String, PersonMarker> entry : friendLocationMap.entrySet()) {
            if (!updatedDeviceIds.contains(entry.getKey())) {
                staleDeviceIds.add(entry.getKey());
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (String staleDevice : staleDeviceIds) {
                    removePersonDot(staleDevice);
                }

                for (SharedLocation userLocation : sharedLocationListWithoutSelf) {
                    updatePersonDot(userLocation);
                }
            }
        });
    }

    @Override
    public void onFailure() {
        PwLog.e(TAG, "Failed to get other user's locations");
    }

    private void updatePersonDot(SharedLocation personLocation) {
        if (personLocation == null || TextUtils.isEmpty(personLocation.getDeviceName())) {
            PwLog.e(TAG, "Received an empty PersonLocation update");
            return;
        }

        if (friendLocationMap.containsKey(personLocation.getDeviceId())) {
            PersonMarker personMarker = friendLocationMap.get(personLocation.getDeviceId());

            if (personLocation.getDeviceName().equals(personMarker.getName())
                    && personLocation.getDeviceType().equals(personMarker.getUserType())) {
                LatLng markerPosition = personMarker.getMarker().getPosition();
                float[] res = {0f};
                Location.distanceBetween(markerPosition.latitude,
                        markerPosition.longitude,
                        personLocation.getLatitude(),
                        personLocation.getLongitude(),
                        res);

                // If the new location is > 1 meter away animate it,
                // otherwise just set its new position
                if (res[0] > 1) {
                    MarkerAnimation.animateMarkerTo(personMarker.getMarker(),
                            new LatLng(personLocation.getLatitude(), personLocation.getLongitude()),
                            linearInterpolator);
                } else {
                    personMarker.getMarker().setPosition(new LatLng(personLocation.getLatitude(),
                            personLocation.getLongitude()));
                }

                return;
            } else {
                // Device has a new name, remove the old marker and we'll build a new one
                personMarker.getMarker().remove();
            }
        }

        // Build the marker icon to show on the map
        Bitmap markerIcon = buildPersonIcon(personLocation.getDeviceId(),
                personLocation.getDeviceName(), personLocation.getDeviceType());

        MarkerOptions personOptions = new MarkerOptions()
                .position(new LatLng(personLocation.getLatitude(), personLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.fromBitmap(markerIcon))
                .draggable(false)
                .visible(true);

        Marker newMarker = phunwareMap.getGoogleMap().addMarker(personOptions);
        PersonMarker newPersonMarker = new PersonMarker(personLocation.getDeviceName(),
                personLocation.getDeviceType(), newMarker);

        friendLocationMap.put(personLocation.getDeviceId(), newPersonMarker);
    }

    private void removePersonDot(String deviceId) {
        if (!TextUtils.isEmpty(deviceId) && friendLocationMap.containsKey(deviceId)) {
            PersonMarker m = friendLocationMap.get(deviceId);
            m.getMarker().remove();
            friendLocationMap.remove(deviceId);
        }
    }

    private Bitmap buildPersonIcon(String deviceId, String name, String userType) {
        IconGenerator factory = new IconGenerator(this);

        factory.setTextAppearance(R.style.markerIconText);

        int color;
        if (friendColorMap.containsKey(deviceId)) {
            color = friendColorMap.get(deviceId);
        } else {
            color = BitmapUtils.getRandomColor();
        }

        Bitmap textBitmap = BitmapUtils.createTextBitmap(this, name + " (" + userType + ")");
        Bitmap dotBitmap = BitmapUtils.createDotBitmap(this, color);

        Bitmap compositeIcon = Bitmap.createBitmap(textBitmap.getWidth(),
                textBitmap.getHeight() + dotBitmap.getHeight(),
                textBitmap.getConfig());

        Canvas canvas = new Canvas(compositeIcon);
        canvas.drawBitmap(dotBitmap,
                (compositeIcon.getWidth() / 2) - (dotBitmap.getWidth() / 2),
                0,
                null);

        canvas.drawBitmap(textBitmap, 0, compositeIcon.getHeight() - textBitmap.getHeight(), null);

        return compositeIcon;
    }
}
