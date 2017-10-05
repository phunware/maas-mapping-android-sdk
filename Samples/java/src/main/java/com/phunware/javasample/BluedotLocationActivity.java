package com.phunware.javasample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.phunware.core.PwCoreSession;
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

import static com.phunware.javasample.PermissionUtils.canAccessLocation;
import static com.phunware.javasample.PermissionUtils.checkPermissions;

public class BluedotLocationActivity extends AppCompatActivity
        implements OnPhunwareMapReadyCallback {
    private static final String TAG = LocationModesActivity.class.getSimpleName();
    public static final int REQUEST_PERMISSION_LOCATION_FINE = 1;

    private PhunwareMapManager mapManager;
    private MapFragment mapFragment;
    private Building currentBuilding;
    private ArrayAdapter<FloorOptions> floorSpinnerAdapter;
    private RelativeLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluedot_location);
        content = findViewById(R.id.content);

        Spinner floorSpinner = findViewById(R.id.floorSpinner);
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

        // Register the Phunware API keys
        PwCoreSession.getInstance().setEnvironment(PwCoreSession.Environment.DEV); // FIXME: REMOVE
        PwCoreSession.getInstance().registerKeys(this);

        // Create the map manager and fragment used to load the building
        mapManager = PhunwareMapManager.create(this);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        checkPermissions(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Load the building if location permission has been granted
        if (canAccessLocation(this)) {
            if (mapFragment != null) {
                mapFragment.getPhunwareMapAsync(this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_LOCATION_FINE) {
            if (!canAccessLocation(this) && content != null) {
                Snackbar.make(content, R.string.permission_snackbar_message,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivityForResult(
                                        new Intent(android.provider.Settings.ACTION_SETTINGS),
                                        REQUEST_PERMISSION_LOCATION_FINE);
                            }
                        }).show();
            }
        }
    }

    @Override
    public void onPhunwareMapReady(final PhunwareMap phunwareMap) {
        // Retrieve buildingId from integers.xml
        int buildingId = getResources().getInteger(R.integer.buildingId);

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
}
