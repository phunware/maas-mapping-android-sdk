package com.phunware.java.sample;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.phunware.core.PwCoreSession;
import com.phunware.mapping.MapFragment;
import com.phunware.mapping.OnPhunwareMapReadyCallback;
import com.phunware.mapping.PhunwareMap;
import com.phunware.mapping.manager.Callback;
import com.phunware.mapping.manager.PhunwareMapManager;
import com.phunware.mapping.model.Building;
import com.phunware.mapping.model.Floor;
import com.phunware.mapping.model.FloorOptions;
import com.phunware.mapping.model.PointOptions;

public class CustomPOIActivity extends AppCompatActivity implements OnPhunwareMapReadyCallback {
    private static final String TAG = CustomPOIActivity.class.getSimpleName();
    private static final int ITEM_ID_CUSTOM_POI = -3; // This ID is required for custom POIs

    private PhunwareMapManager mapManager;
    private Building currentBuilding;
    private ArrayAdapter<FloorOptions> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_poi);

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

        // Setup long click listener to create Custom POIs
        setupMapListeners(phunwareMap);

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

    private void setupMapListeners(final PhunwareMap map) {
        map.getGoogleMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (currentBuilding != null) {
                    Floor currentFloor = currentBuilding.getSelectedFloor();
                    FloorOptions currentFloorOptions = null;
                    for (FloorOptions fo : currentBuilding.getFloorOptions()) {
                        if (fo.getId() == currentFloor.getId()) {
                            currentFloorOptions = fo;
                        }
                    }
                    if (currentFloorOptions != null
                            && !currentFloorOptions.getBounds().contains(latLng)) {
                        Toast.makeText(CustomPOIActivity.this, R.string.custom_poi_pin_drop_error,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    showCustomPOIDialog(currentFloorOptions, latLng);
                }
            }
        });
    }

    /**
     * Show a dialog asking for the name of a custom POI, if the EditText is not empty a new
     * {@link PointOptions} will be added to the given floor
     *
     * @param floor    The floor to which the new Point of Interest will be added
     * @param location The coordinates of the point that we're adding to the floor
     */
    private void showCustomPOIDialog(final FloorOptions floor, final LatLng location) {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_poi, null);
        final EditText poiNameInput = dialogView.findViewById(R.id.poi_name_text_input);

        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.custom_poi_dialog_title)
                .setMessage(R.string.custom_poi_dialog_message)
                .setView(dialogView)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String poiName = poiNameInput.getText().toString();
                        if (!TextUtils.isEmpty(poiName)) {
                            PointOptions customPoint = new PointOptions()
                                    .id(ITEM_ID_CUSTOM_POI)
                                    .name(poiName)
                                    .buildingId(currentBuilding.getId())
                                    .floorId(floor.getId())
                                    .location(location);

                            // Add a custom POI to this floor (flagged as custom with id)
                            floor.getPoiOptions().add(customPoint);
                            // Reload current floor
                            currentBuilding.selectFloor(floor.getLevel());
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
