package com.phunware.java.sample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.phunware.core.PwCoreSession;
import com.phunware.core.PwLog;
import com.phunware.location.provider_managed.ManagedProviderFactory;
import com.phunware.location.provider_managed.PwManagedLocationProvider;
import com.phunware.location_core.PwLocationProvider;
import com.phunware.mapping.MapFragment;
import com.phunware.mapping.OnPhunwareMapReadyCallback;
import com.phunware.mapping.PhunwareMap;
import com.phunware.mapping.manager.Callback;
import com.phunware.mapping.manager.Navigator;
import com.phunware.mapping.manager.PhunwareMapManager;
import com.phunware.mapping.manager.Router;
import com.phunware.mapping.model.Building;
import com.phunware.mapping.model.FloorOptions;
import com.phunware.mapping.model.PointOptions;
import com.phunware.mapping.model.RouteManeuverOptions;
import com.phunware.mapping.model.RouteOptions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class RoutingActivity extends AppCompatActivity implements OnPhunwareMapReadyCallback,
        Navigator.OnManeuverChangedListener {
    private static final String TAG = LocationModesActivity.class.getSimpleName();
    private static final int ITEM_ID_LOCATION = -2;

    private PhunwareMapManager mapManager;
    private Building currentBuilding;
    private RelativeLayout content;
    private Spinner floorSpinner;
    private ArrayAdapter<FloorOptions> floorSpinnerAdapter;

    // Navigation Views
    private FloatingActionButton fab;
    private Navigator navigator;
    private View navOverlayContainer;
    private NavigationOverlayView navOverlay;
    private Spinner startPicker;
    private Spinner endPicker;
    private CheckBox accessible;

    View.OnClickListener selectRouteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showRoutingDialog();
        }
    };
    View.OnClickListener exitNavListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopNavigating();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routing);
        content = findViewById(R.id.content);

        // Initialize views for routing
        fab = findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(selectRouteListener);
        navOverlayContainer = findViewById(R.id.nav_overlay_container);
        navOverlay = findViewById(R.id.nav_overlay);

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

        // Register the Phunware API keys
        PwCoreSession.getInstance().setEnvironment(PwCoreSession.Environment.DEV); // FIXME: REMOVE
        PwCoreSession.getInstance().registerKeys(this);

        // Create the map manager and fragment used to load the building
        mapManager = PhunwareMapManager.create(this);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getPhunwareMapAsync(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (navOverlayContainer.getVisibility() == View.VISIBLE) {
            stopNavigating();
        } else {
            super.onBackPressed();
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

                        // Enabled fab for routing
                        showFab(true);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.d(TAG, "Error when loading building -- " + throwable.getMessage());
                        showFab(false);
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

    private void showFab(final boolean show) {
        final float start = show ? 0 : 1;
        final float end = show ? 1 : 0;
        List<Animator> anims = new ArrayList<>(2);
        anims.add(ObjectAnimator.ofFloat(fab, View.SCALE_X, start, end));
        anims.add(ObjectAnimator.ofFloat(fab, View.SCALE_Y, start, end));
        AnimatorSet s = new AnimatorSet();
        s.playTogether(anims);
        s.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (show) fab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) fab.setVisibility(View.GONE);
            }
        });
        s.start();
    }

    private void showRoutingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Load custom dialog view
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_route_selection, null);
        initDialogUI(dialogView);

        builder.setView(dialogView)
                .setTitle("Select a Route")
                .setMessage("Choose two points to route between")
                .setCancelable(false)
                .setPositiveButton("Route", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        getRoutes();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do Nothing - Close Dialog
                    }
                });

        Dialog d = builder.create();
        d.show();
    }

    private void initDialogUI(View dialogView) {
        startPicker = dialogView.findViewById(R.id.start);
        endPicker = dialogView.findViewById(R.id.end);
        accessible = dialogView.findViewById(R.id.accessible);
        ImageButton reverse = dialogView.findViewById(R.id.reverse);

        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReverseClicked();
            }
        });

        List<PointOptions> points = new ArrayList<>();
        if (currentBuilding != null) {
            for (FloorOptions floorOptions : currentBuilding.getFloorOptions()) {
                if (floorOptions != null && floorOptions.getPoiOptions() != null) {
                    points.addAll(floorOptions.getPoiOptions());
                }
            }
        }

        boolean hasCurrentLocation = false;
        if (mapManager.isMyLocationEnabled() && mapManager.getCurrentLocation() != null) {
            Location myLocation = mapManager.getCurrentLocation();
            LatLng currentLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            long currentFloorId = mapManager.getCurrentBuilding().getSelectedFloor().getId();
            if (myLocation.getExtras() != null && myLocation.getExtras()
                    .containsKey(PwLocationProvider.LOCATION_EXTRAS_KEY_FLOOR_ID)) {
                currentFloorId = myLocation.getExtras()
                        .getLong(PwLocationProvider.LOCATION_EXTRAS_KEY_FLOOR_ID);
            }
            points.add(0, new PointOptions()
                    .id(ITEM_ID_LOCATION)
                    .location(currentLocation)
                    .level(currentFloorId)
                    .name(getString(R.string.current_location)));
            hasCurrentLocation = true;
        }

        startPicker.setAdapter(new BuildingAdapter(this, points, getString(R.string.start_prompt)));
        endPicker.setAdapter(new BuildingAdapter(this, points, getString(R.string.end_prompt)));

        if (hasCurrentLocation) {
            startPicker.setSelection(1, false);
        }
    }

    private void getRoutes() {
        long startId = startPicker.getSelectedItemId();
        long endId = endPicker.getSelectedItemId();
        boolean isAccessible = accessible.isChecked();

        Router router = mapManager.findRoutes(startId, endId, isAccessible);
        if (router != null) {
            RouteOptions route = router.shortestRoute();
            if (route == null) {
                PwLog.e(TAG, "Couldn't find route.");
                Snackbar.make(content, R.string.no_route,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                startNavigating(route);
            }
        }
    }

    private void onReverseClicked() {
        int startPos = startPicker.getSelectedItemPosition();
        int endPos = endPicker.getSelectedItemPosition();

        startPicker.setSelection(endPos);
        endPicker.setSelection(startPos);
    }

    private void startNavigating(RouteOptions route) {
        if (navigator != null) {
            navigator.terminate();
        }
        navigator = mapManager.navigate(route);
        navigator.addOnManeuverChangedListener(this);

        navOverlay.setNavigator(navigator);
        navOverlayContainer.setVisibility(View.VISIBLE);
        fab.setImageResource(R.drawable.ic_clear_white);
        fab.setOnClickListener(exitNavListener);

        navigator.start();
    }

    private void stopNavigating() {
        if (navigator != null) {
            navigator.terminate();
            navigator = null;
        }
        navOverlayContainer.setVisibility(View.GONE);
        fab.setImageResource(R.drawable.ic_navigation);
        fab.setOnClickListener(selectRouteListener);
    }

    @Override
    public void onManeuverChanged(Navigator navigator, int position) {
        // Update the selected floor when the maneuver floor changes
        RouteManeuverOptions maneuver = navigator.getManeuvers().get(position);
        int selectedPosition = floorSpinner.getSelectedItemPosition();
        for (int i = 0; i < floorSpinnerAdapter.getCount(); i++) {
            FloorOptions floor = floorSpinnerAdapter.getItem(i);
            if (selectedPosition != i && floor != null && floor.getId() == maneuver.getFloorId()) {
                floorSpinner.setSelection(i);
            }
        }
    }

    @Override
    public void onRouteSnapFailed() {
        // Do Nothing
    }
}
