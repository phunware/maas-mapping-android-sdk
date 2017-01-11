package com.phunware.sample;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.phunware.core.PwCoreSession;
import com.phunware.core.PwLog;
import com.phunware.location.PwLocationModule;
import com.phunware.mapping.Analytics;
import com.phunware.mapping.MapFragment;
import com.phunware.mapping.OnPhunwareMapReadyCallback;
import com.phunware.mapping.PhunwareMap;
import com.phunware.mapping.manager.Callback;
import com.phunware.mapping.manager.Navigator;
import com.phunware.mapping.manager.PhunwareMapManager;
import com.phunware.mapping.model.Building;
import com.phunware.mapping.model.BuildingOptions;
import com.phunware.mapping.model.FloorOptions;
import com.phunware.mapping.model.PoiType;
import com.phunware.mapping.model.PoiTypeOptions;
import com.phunware.mapping.model.PointOptions;
import com.phunware.mapping.model.RouteManeuverOptions;
import com.phunware.mapping.model.RouteOptions;
import com.phunware.mapping.provider.gps.GpsProviderFactory;
import com.phunware.mapping.provider.mse.MseProviderFactory;
import com.phunware.mapping.provider.senion.SenionProviderFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnPhunwareMapReadyCallback,
        Navigator.OnManeuverChangedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int RC_ROUTE = 0x01;
    private static final int RC_PROVIDERS = 0x02;
    private static final int RC_PERM = 2330;
    private static final int RC_LOCATION = 0x03;

    private static final String PREFERENCE_NAME = "sample";
    private static final String PREF_PROVIDER_NAME = "provider";
    private static final String PREF_PROVIDER_SENION = "senion";
    private static final String PREF_PROVIDER_MSE = "mse";
    private static final String PREF_PROVIDER_GPS = "gps";
    private static final String PREF_LOCATION_MODE = "loc_mode";
    private static final String PREF_LOCATION_FOLLOW = "follow me";
    private static final String PREF_LOCATION_LOCATE = "locate me";
    private static final String PREF_LOCATION_DEFAULT = "default";

    private CoordinatorLayout content;
    private FloatingActionButton fab;
    private View navOverlayContainer;
    private NavigationOverlayView navOverlay;

    private PhunwareMapManager mapManager;
    private Navigator navigator;
    private Spinner floorSpinner;
    private ArrayAdapter<FloorOptions> spinnerAdapter;
    private Building building;
    private BuildingOptions options;
    private MenuItem filter;
    private PopupMenu filterPopupMenu;

    private final List<PoiType> poiTypes = new LinkedList<>();
    private Toolbar toolbar;

    View.OnClickListener selectRouteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RouteSelectionActivity.startForResult(MainActivity.this, options, RC_ROUTE);
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

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mapManager = App.get(this).getMapManager();
        content = (CoordinatorLayout) findViewById(R.id.content);
        navOverlayContainer = findViewById(R.id.nav_overlay_container);
        navOverlay = (NavigationOverlayView) findViewById(R.id.nav_overlay);

        floorSpinner = (Spinner) findViewById(R.id.floorSpinner);

        spinnerAdapter = new FloorAdapter(this);
        floorSpinner.setAdapter(spinnerAdapter);
        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FloorOptions floor = spinnerAdapter.getItem((int)id);
                Analytics.sendFloorLoaded(getApplicationContext(), building, floor);

                building.selectFloor(floor.getLevel());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        PwLog.setShowLog(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(selectRouteListener);

        filterPopupMenu = new PopupMenu(this, toolbar, GravityCompat.END);
        filterPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item == null) {
                    return false;
                }

                PoiTypeOptions typeOptions = new PoiTypeOptions()
                        .description((String)item.getTitle())
                        .id(item.getItemId());

                PoiType type = new PoiType(typeOptions);

                if (item.isChecked()) {
                    item.setChecked(false);
                    poiTypes.remove(type);
                } else {
                    item.setChecked(true);
                    poiTypes.add(type);
                }
                if (poiTypes.size() > 0) {
                    filterPois();
                } else {
                    clearPoiFilter();
                }
                return true;
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        checkPermissions();
    }

    private void checkPermissions() {
        if (!canAccessLocation()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, RC_PERM);
            }
        } else {
            onLocationPermissionGranted();
        }
    }

    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return(PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_PERM) {
            if (canAccessLocation()) {
                onLocationPermissionGranted();
            } else {
               Snackbar.make(content, R.string.permission_snackbar_message,
                       Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), RC_PERM);
                    }
                })
                .show();
            }
        }
    }

    private void onLocationPermissionGranted() {
        PwCoreSession.getInstance().registerKeys(this);
        PwCoreSession.getInstance().installModules(PwLocationModule.getInstance());
        if (mapManager != null) {
            String provider = getSavedProvider();
            if (provider.equals(PREF_PROVIDER_SENION)) {
                mapManager.setLocationProviderFactory(SenionProviderFactory.create(this,
                        getString(R.string.sl_customer_id),
                        getString(R.string.sl_map_id),
                        getSenionFloorMap()));
            } else if (provider.equals(PREF_PROVIDER_MSE)) {
                mapManager.setLocationProviderFactory(MseProviderFactory.create(this,
                        getString(R.string.mse_venue_guid), getMseFloorMap()));
            } else if (provider.equals(PREF_PROVIDER_GPS)) {
                mapManager.setLocationProviderFactory(GpsProviderFactory.create(this));
            }
            Toast.makeText(MainActivity.this, "Using location provider: " + provider,
                    Toast.LENGTH_SHORT).show();
        }
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getPhunwareMapAsync(this);
        }
    }

    private Map<String, Long> getSenionFloorMap() {
        Map<String, Long> floorIdMap = new HashMap<>();

        String[] senionKeys = getResources().getStringArray(
                com.phunware.mapping.provider.senion.R.array.senion_floor_nrs);
        int[] maasIds = getResources()
                .getIntArray(com.phunware.mapping.provider.senion.R.array.maas_floor_ids);
        if (senionKeys.length == 0 || maasIds.length == 0) {
            PwLog.e(TAG, "Senion floor IDs and MaaS floor IDs need to be set in arrays.xml");
        }
        if (senionKeys.length != maasIds.length) {
            PwLog.e(TAG, "Senion floor IDs and Maas floor IDs have different numbers of values");
        }
        for (int i = 0; i < senionKeys.length && i < maasIds.length; i++) {
            floorIdMap.put(senionKeys[i], (long)maasIds[i]);
        }

        return floorIdMap;
    }

    private Map<String, Long> getMseFloorMap() {
        Map<String, Long> floorIdMap = new HashMap<String, Long>();

        String[] mseKeys = getResources()
                .getStringArray(com.phunware.mapping.provider.mse.R.array.mse_floor_nrs);
        int[] maasIds = getResources()
                .getIntArray(com.phunware.mapping.provider.mse.R.array.maas_floor_ids);
        if (mseKeys.length == 0 || maasIds.length == 0) {
            PwLog.e(TAG, "MSE floor IDs and MaaS floor IDs need to be set in arrays.xml");
        }
        if (mseKeys.length != maasIds.length) {
            PwLog.e(TAG, "MSE floor IDs and Maas floor IDs have different numbers of values");
        }
        for (int i = 0; i < mseKeys.length && i < maasIds.length; i++) {
            floorIdMap.put(mseKeys[i], (long)maasIds[i]);
        }

        return floorIdMap;
    }

    private void setPoiFilterVisible() {
        poiTypes.clear();
        poiTypes.addAll(mapManager.getAllPoiTypes());
        if (poiTypes.size() > 0) {
            Collections.sort(poiTypes, new Comparator<PoiType>() {
                @Override
                public int compare(PoiType lhs, PoiType rhs) {
                    return lhs.getDescription().compareTo(rhs.getDescription());
                }
            });
            filterPopupMenu.getMenu().clear();
            for (PoiType type : poiTypes) {
                filterPopupMenu.getMenu().add(0, (int)type.getId(), 0, type.getDescription());
                filterPopupMenu.getMenu().setGroupCheckable(0, true, false);
                filterPopupMenu.getMenu().findItem((int)type.getId()).setChecked(true);
            }
            filter.setVisible(true);
        }
    }

    private void filterPois() {
        building.filterPoisByType(poiTypes);
    }

    private void clearPoiFilter() {
        building.clearPoiTypeFilter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_ROUTE && resultCode == RESULT_OK) {
            RouteOptions route = data.getParcelableExtra(RouteSelectionActivity.EXTRA_ROUTE);
            if (route == null) {
                Log.e(TAG, "Couldn't find route.");
                Snackbar.make(content, R.string.no_route,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Analytics.sendRouteRequested(this.getApplicationContext(), route);
                startNavigating(route);
            }
        } else if (requestCode == RC_PROVIDERS && resultCode == RESULT_OK) {
            String provider = data.getStringExtra(DropDownSelectionActivity.EXTRA_SELECTED_OPTION);
            if (!provider.equals(getSavedProvider())) {
                Analytics.sendLocationProvider(getApplicationContext(), building.getId(),
                        building.getName(), provider);
                setSavedProvider(provider);
                ProcessPhoenix.triggerRebirth(MainActivity.this);
            }
        }
        else if (requestCode == RC_PERM) {
            if (canAccessLocation()) {
                onLocationPermissionGranted();
            }
        } else if (requestCode == RC_LOCATION && resultCode == RESULT_OK) {
            String mode = data.getStringExtra(DropDownSelectionActivity.EXTRA_SELECTED_OPTION);
            String oldMode = getSavedLocationMode();
            setSavedLocationMode(mode);
            Analytics.sendChangeTrackingMode(getApplicationContext(), building.getId(), building.getName(), oldMode, mode);
            if (mode.equalsIgnoreCase(PREF_LOCATION_FOLLOW)) {
                mapManager.setMyLocationMode(PhunwareMapManager.MODE_FOLLOW_ME);
            } else if (mode.equalsIgnoreCase(PREF_LOCATION_LOCATE)) {
                mapManager.setMyLocationMode(PhunwareMapManager.MODE_LOCATE_ME);
            } else {
                mapManager.setMyLocationMode(PhunwareMapManager.MODE_NORMAL);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (navOverlayContainer.getVisibility() == View.VISIBLE) {
            stopNavigating();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mapManager != null) {
            mapManager.setMyLocationEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mapManager != null) {
            mapManager.setMyLocationEnabled(true);
            String mode = getSavedLocationMode();
            if (mode.equalsIgnoreCase(PREF_LOCATION_FOLLOW)) {
                mapManager.setMyLocationMode(PhunwareMapManager.MODE_FOLLOW_ME);
            } else if (mode.equalsIgnoreCase(PREF_LOCATION_LOCATE)) {
                mapManager.setMyLocationMode(PhunwareMapManager.MODE_LOCATE_ME);
            } else {
                mapManager.setMyLocationMode(PhunwareMapManager.MODE_NORMAL);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        filter = menu.findItem(R.id.action_filter);
        setPoiFilterVisible();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            filterPopupMenu.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startNavigating(RouteOptions route) {
        if (navigator != null) {
            navigator.terminate();
        }
        navigator = mapManager.navigate(route);
        navigator.addOnManeuverChangedListener(this);

        navOverlay.setNavigator(options, navigator);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_first_update_time) {
            Toast.makeText(MainActivity.this, String.format(Locale.US,
                    "first update time = %.3f secs",
                    mapManager.getBlueDotAcquisitionTime() / 1000.0f), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_select_provider) {
            ArrayList<String> providers = new ArrayList<>();
            providers.add(PREF_PROVIDER_SENION);
            providers.add(PREF_PROVIDER_MSE);
            providers.add(PREF_PROVIDER_GPS);
            String selectedProvider = getSavedProvider();
            DropDownSelectionActivity.startForResult(MainActivity.this, providers,
                    selectedProvider, RC_PROVIDERS);
        } else if (id == R.id.nav_select_mode) {
            ArrayList<String> modes = new ArrayList<>();
            modes.add(PREF_LOCATION_DEFAULT);
            modes.add(PREF_LOCATION_FOLLOW);
            modes.add(PREF_LOCATION_LOCATE);
            String currentMode = getSavedLocationMode();
            DropDownSelectionActivity.startForResult(MainActivity.this, modes,
                    currentMode, RC_LOCATION);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onPhunwareMapReady(final PhunwareMap map) {
        setupMapListeners(map);

        /*
            Create a custom info window that is displayed when the user clicks on a POI.
            This is an optional step,  if you don't need a custom info window, remove this
            code.
         */
        map.getGoogleMap().setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                String title = marker.getTitle();
                if (title == null || title.isEmpty()) {
                    return null;
                }

                View v = getLayoutInflater().inflate(R.layout.info_window, null);
                TextView titleTv = (TextView)v.findViewById(R.id.title);
                titleTv.setText(marker.getTitle());
                return v;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

        mapManager.setPhunwareMap(map);
        mapManager.addBuilding(getResources().getInteger(R.integer.building_id),
                new Callback<Building>() {
                    @Override
                    public void onSuccess(final Building building) {
                        if (building == null) {
                            Toast.makeText(MainActivity.this, "No building", Toast.LENGTH_LONG)
                                    .show();
                            return;
                        }
                        options = building.getBuildingOptions();

                        MainActivity.this.building = building;
                        spinnerAdapter.clear();
                        spinnerAdapter.addAll(building.getBuildingOptions().getFloors());

                        // animate the camera to the new building
                        final CameraUpdate cameraUpdate;
                        FloorOptions initialFloor = building.initialFloor();
                        if (initialFloor == null) {
                            // if we don't have a floor with reference points, just zoom in on the
                            // building, at a relatively safe distance
                            cameraUpdate = CameraUpdateFactory
                                    .newLatLngZoom(building.getLocation(), 17);
                        } else {
                            building.selectFloor(building.getInitialFloor());
                            cameraUpdate = CameraUpdateFactory
                                    .newLatLngBounds(initialFloor.getBounds(), 4);
                        }
                        map.getGoogleMap().setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            // Wait to animate the camera until the map is rendered.
                            @Override
                            public void onMapLoaded() {
                                map.getGoogleMap().animateCamera(cameraUpdate);
                            }
                        });

                        // enable my location (blue dot)
                        mapManager.setMyLocationEnabled(true);

                        String mode = getSavedLocationMode();
                        if (mode.equalsIgnoreCase(PREF_LOCATION_FOLLOW)) {
                            mapManager.setMyLocationMode(PhunwareMapManager.MODE_FOLLOW_ME);
                        } else if (mode.equalsIgnoreCase(PREF_LOCATION_LOCATE)) {
                            mapManager.setMyLocationMode(PhunwareMapManager.MODE_LOCATE_ME);
                        } else {
                            mapManager.setMyLocationMode(PhunwareMapManager.MODE_NORMAL);
                        }
                        showFab(true);
                        // set up filter menu item now that we have data
                        supportInvalidateOptionsMenu();
                        
                        mapManager.setFloorChangedListener(new Building.OnFloorChangedListener() {
                            @Override
                            public void onFloorChanged(Building building, long floorId) {
                                long level = (building.getFloorLevelFromId(floorId));
                                // find the level in our spinner and set it
                                for (int index = 0; index < spinnerAdapter.getCount(); index++) {
                                    FloorOptions floor = spinnerAdapter.getItem(index);
                                    if (floor.getLevel() == level) {
                                        floorSpinner.setSelection(index);
                                        break;
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(final Throwable e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showFab(false);
                                Toast.makeText(MainActivity.this, e.getLocalizedMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });


    }

    @Override
    public void onManeuverChanged(Navigator navigator, int position) {
        // update the selected floor when the maneuver floor changes
        RouteManeuverOptions maneuver = navigator.getManeuvers().get(position);
        int selectedPosition = floorSpinner.getSelectedItemPosition();
        for (int i = 0; i < spinnerAdapter.getCount(); i++) {
            FloorOptions floor = spinnerAdapter.getItem(i);
            if (selectedPosition != i && floor.getId() == maneuver.getFloorId()) {
                floorSpinner.setSelection(i);
            }
        }
    }

    @Override
    public void onRouteSnapFailed() {
        Log.d(TAG, "Route snap failed. Consider rerouting here");
    }

    private Marker customMarker;
    private void setupMapListeners(final PhunwareMap map) {
        // When the user taps an info window, we present the routing dialog to allow them to
        // route to the destination
        map.getGoogleMap().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (options != null) {
                    long floorId = spinnerAdapter.getItem(floorSpinner.getSelectedItemPosition())
                            .getId();

                    if (customMarker != null && marker.getId().equals(customMarker.getId())) {
                        RouteSelectionActivity.startForResult(MainActivity.this, options,
                                marker.getPosition(), floorId, RC_ROUTE);
                    } else {
                        PointOptions point = mapManager.getPointByTitle(marker.getTitle());
                        if (point != null) {
                            RouteSelectionActivity.startForResult(MainActivity.this, options,
                                    point, floorId, RC_ROUTE);
                        }
                    }
                }
            }
        });

        // Setup long click listener
        map.getGoogleMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                if (!spinnerAdapter.getItem((int) floorSpinner.getSelectedItemId()).getBounds()
                        .contains(latLng)) {
                    Toast.makeText(MainActivity.this, R.string.pin_drop_error, Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                // If we already have a custom marker, replace it with a new one
                if (customMarker != null) {
                    customMarker.remove();
                    customMarker = null;
                }

                // When the user long clicks the map, we drop a custom marker,
                // display it's info window allowing the user to navigate to/from
                // the custom marker.
                customMarker = map.getGoogleMap().addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.custom_location_title)));
            }
        });
        map.getGoogleMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // if there is already a custom marker on the map, remove it
                if (customMarker != null) {
                    customMarker.remove();
                    customMarker = null;
                }
            }
        });
    }

    private String getSavedProvider() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, 0);
        return preferences.getString(PREF_PROVIDER_NAME, PREF_PROVIDER_SENION);
    }

    private void setSavedProvider(final String provider) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, 0);
        preferences.edit()
                .putString(PREF_PROVIDER_NAME, provider)
                .apply();
    }

    private String getSavedLocationMode() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, 0);
        return preferences.getString(PREF_LOCATION_MODE, PREF_LOCATION_DEFAULT);
    }

    private void setSavedLocationMode(final String mode) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, 0);
        preferences.edit()
                .putString(PREF_LOCATION_MODE, mode)
                .apply();
    }

    private static class FloorAdapter extends ArrayAdapter<FloorOptions> {
        FloorAdapter(Context context) {
            super(context, 0, new ArrayList<FloorOptions>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;
            if (v == null) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.spinner_row, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) v.findViewById(android.R.id.text1);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            holder.text.setText(getItem(position).getName());

            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }

        private static class ViewHolder {
            TextView text;
        }
    }
}
