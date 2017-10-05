package com.phunware.javasample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.phunware.core.PwCoreSession;
import com.phunware.mapping.MapFragment;
import com.phunware.mapping.OnPhunwareMapReadyCallback;
import com.phunware.mapping.PhunwareMap;
import com.phunware.mapping.manager.Callback;
import com.phunware.mapping.manager.PhunwareMapManager;
import com.phunware.mapping.model.Building;
import com.phunware.mapping.model.FloorOptions;

public class LoadBuildingActivity extends AppCompatActivity implements OnPhunwareMapReadyCallback {
    private static final String TAG = LoadBuildingActivity.class.getSimpleName();
    private PhunwareMapManager mapManager;
    private Building currentBuilding;
    private ArrayAdapter<FloorOptions> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_building);

        Spinner floorSpinner = findViewById(R.id.floorSpinner);
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

        // Create the map manager used to load the building
        mapManager = PhunwareMapManager.create(this);

        // Register the Phunware API keys
        PwCoreSession.getInstance().setEnvironment(PwCoreSession.Environment.DEV); // FIXME: REMOVE
        PwCoreSession.getInstance().registerKeys(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getPhunwareMapAsync(this);
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
                        spinnerAdapter.clear();
                        spinnerAdapter.addAll(building.getBuildingOptions().getFloors());

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
}
