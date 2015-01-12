package com.phunware.mapping.sample.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.phunware.core.PwLog;
import com.phunware.location.provider.PwLocationProvider;
import com.phunware.location.provider.PwMockLocationProvider;
import com.phunware.mapping.PwMappingModule;
import com.phunware.mapping.PwOnPOITypesDownloadListener;
import com.phunware.mapping.PwRouteCallback;
import com.phunware.mapping.library.maps.MyLocationLayer;
import com.phunware.mapping.library.maps.PwBuildingMapManager;
import com.phunware.mapping.library.maps.PwMapOverlayManagerBuilder;
import com.phunware.mapping.library.maps.PwOnBuildingPOIDataLoadedCallback;
import com.phunware.mapping.library.maps.PwOnSnapToRouteCallback;
import com.phunware.mapping.library.maps.PwRouteSnappingTolerance;
import com.phunware.mapping.library.ui.PwBuildingMarker;
import com.phunware.mapping.library.ui.PwMappingFragment;
import com.phunware.mapping.model.PwBuilding;
import com.phunware.mapping.model.PwPoint;
import com.phunware.mapping.model.PwPointType;
import com.phunware.mapping.model.PwRoute;
import com.phunware.mapping.sample.R;
import com.phunware.mapping.sample.providers.FusedLocationProviderFactory;
import com.phunware.mapping.sample.providers.LocationProvider;
import com.phunware.mapping.sample.providers.MockLocationProviderFactory;
import com.phunware.mapping.sample.providers.MseLocationProviderFactory;
import com.phunware.mapping.sample.providers.QualcommLocationProviderFactory;
import com.phunware.mapping.sample.providers.SenionLocationProviderFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows a map of the venue.
 */
public class MappingSampleFragment extends PwMappingFragment implements PwMockLocationProvider.MockLocationsDisabledListener, PwOnSnapToRouteCallback, PwOnBuildingPOIDataLoadedCallback {
    private static final String TAG = MappingSampleFragment.class.getSimpleName();

    private static final String KEY_MAP_TYPE = "map_type";
    private static final String KEY_PROVIDER = "provider";
    private static final String KEY_BUILDING = "building";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_REPEAT = "repeat";
    // Assumes order of Google map types array is same as order of R.array.mapping_types
    private static final int[] MAP_TYPES = new int[]{GoogleMap.MAP_TYPE_NORMAL, GoogleMap.MAP_TYPE_HYBRID, GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_NONE};
    private static final String KEY_MAP_TYPE_NAMES = "map_type_names";
    private static final String KEY_BUILDING_ID_LIST = "building_id_list";
    private static final String MOCK_LOCATION_DIRECTORY = "mocklocation";
    private static final float MINIMUM_MARKER_ZOOM_LEVEL = 17f;
    private static final float MINIMUM_FLOOR_ZOOM_LEVEL = 13f;

    public static final int DEFAULT_MENU_ITEM_SELECTION = 0;

    private String[] mMapTypeNames;
    private String[] mFileList;
    private int[] mBuildingList;

    private int mCurrentMapType;
    private LocationProvider mCurrentProvider = LocationProvider.SENION;
    private int mCurrentBuilding;
    private String mCurrentMockFileName;
    private boolean mRepeat;

    private DialogInterface.OnClickListener mMapTypeSelectionOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                updateMapType(which);
                dialog.dismiss();
            } catch (Exception ex) {
                PwLog.e(TAG, "Error occurs in mMapTypeSelectionOnClickListener: " + ex.getMessage(), ex);
            }
        }
    };

    private DialogInterface.OnClickListener mProviderSelectionOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                dialog.dismiss();
                if (mCurrentProvider == LocationProvider.values()[which]) {
                    PwLog.d(TAG, "Change to the same location provider, ignored.");
                    return;
                }
                mCurrentProvider = LocationProvider.values()[which];
                requestLocationUpdates(mPwBuildingMapManager.getBuilding());

                // Update menu items if necessary
                if (mMenu != null) updateMenuItems(mMenu);
            } catch (Exception ex) {
                PwLog.e(TAG, "Error occurs in mProviderSelectionOnClickListener: " + ex.getMessage(), ex);
            }
        }
    };

    private DialogInterface.OnClickListener mBuildingSelectionOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                dialog.dismiss();
                mCurrentBuilding = mBuildingList[which];
                createPwMapOverlayManagerBuilder(null);
            } catch (Exception ex) {
                PwLog.e(TAG, "Error occurs in mBuildingSelectionOnClickListener: " + ex.getMessage(), ex);
            }
        }
    };

    private DialogInterface.OnClickListener mFileNameSelectionOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                dialog.dismiss();

                if (mCurrentMockFileName != null && mCurrentMockFileName.equals(mFileList[which])) {
                    PwLog.d(TAG, "Change to the same moc location file, ignored.");
                    return;
                }

                mCurrentMockFileName = mFileList[which];
                requestLocationUpdates(mPwBuildingMapManager.getBuilding());
            } catch (Exception ex) {
                PwLog.e(TAG, "Error occurs in mFileNameSelectionOnClickListener: " + ex.getMessage(), ex);
            }
        }
    };

    private DialogInterface.OnClickListener mPOITypeSelectionOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    private DialogInterface.OnClickListener mRouteSnapperToleranceSelectionOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            mPwBuildingMapManager.setRouteSnappingTolerance(PwRouteSnappingTolerance.values()[which]);
            updateMenuItems(mMenu);
        }
    };

    private Menu mMenu;
    private List<PwPoint> mPwPoints;
    private List<PwBuildingMarker> mBuildingMarkers = new ArrayList<PwBuildingMarker>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mCurrentMapType = GoogleMap.MAP_TYPE_NORMAL;
            mMapTypeNames = getResources().getStringArray(R.array.mapping_types);
            mBuildingList = getResources().getIntArray(R.array.building_id);
            mCurrentBuilding = mBuildingList[0];
            mFileList = getFileList();
            if (mFileList != null && mFileList.length > 0) {
                mCurrentMockFileName = mFileList[0];
            }
        } else {
            mCurrentMapType = savedInstanceState.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
            mMapTypeNames = savedInstanceState.getStringArray(KEY_MAP_TYPE_NAMES);
            mBuildingList = savedInstanceState.getIntArray(KEY_BUILDING_ID_LIST);
            mCurrentProvider = LocationProvider.values()[savedInstanceState.getInt(KEY_PROVIDER)];
            mCurrentBuilding = savedInstanceState.getInt(KEY_BUILDING);
            mCurrentMockFileName = savedInstanceState.getString(KEY_FILE_NAME);
            mRepeat = savedInstanceState.getBoolean(KEY_REPEAT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        final DrawInsetsFrameLayout drawInsetsFrameLayout = (DrawInsetsFrameLayout) view.findViewById(R.id.draw_insets_frameLayout);
        drawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                // Update the map padding (inset the compass, zoom buttons, attribution, etc.)
                drawInsetsFrameLayout.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                if (getMap() != null) {
                    getMap().setPadding(insets.left, insets.top, insets.right, insets.bottom);
                }
            }
        });

        return view;
    }

    private String[] getFileList() {
        try {
            return getActivity().getAssets().list(MOCK_LOCATION_DIRECTORY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private DialogFragment createMapTypeSelectionDialogFragment() {
        final MenuItemSelectionDialogFragment menuItemSelectionDialogFragment = MenuItemSelectionDialogFragment.newInstance(findMapTypeIndexById(mCurrentMapType), R.array.mapping_types, R.string.mapping_choose_map_type);
        menuItemSelectionDialogFragment.setOnClickListener(mMapTypeSelectionOnClickListener);
        return menuItemSelectionDialogFragment;
    }

    private DialogFragment createProviderSelectionDialogFragment() {
        LocationProvider[] providerList = LocationProvider.values();
        ArrayList<CharSequence> itemArray = new ArrayList<CharSequence>();
        for (LocationProvider provider : providerList) {
            itemArray.add(provider.getDisplayName());
        }
        final MenuItemSelectionDialogFragment menuItemSelectionDialogFragment = MenuItemSelectionDialogFragment.newInstance(findProviderIndex
            (mCurrentProvider), itemArray.toArray(new CharSequence[itemArray.size()]), R.string.mapping_provider_choose);
        menuItemSelectionDialogFragment.setOnClickListener(mProviderSelectionOnClickListener);
        return menuItemSelectionDialogFragment;
    }

    private DialogFragment createBuildingSelectionDialogFragment() {
        final MenuItemSelectionDialogFragment menuItemSelectionDialogFragment = MenuItemSelectionDialogFragment.newInstance(findBuildingIndex
            (mCurrentBuilding), R.array.building_names, R.string.mapping_building_choose);
        menuItemSelectionDialogFragment.setOnClickListener(mBuildingSelectionOnClickListener);
        return menuItemSelectionDialogFragment;
    }

    private DialogFragment createMockLocationFileSelectionDialogFragment() {
        if (mFileList == null) {
            mFileList = getFileList();
        }
        final MenuItemSelectionDialogFragment menuItemSelectionDialogFragment = MenuItemSelectionDialogFragment.newInstance(findFileIndex
            (mCurrentMockFileName), mFileList, R.string.mapping_file_choose);
        menuItemSelectionDialogFragment.setOnClickListener(mFileNameSelectionOnClickListener);
        return menuItemSelectionDialogFragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_MAP_TYPE, mCurrentMapType);
        outState.putStringArray(KEY_MAP_TYPE_NAMES, mMapTypeNames);
        outState.putIntArray(KEY_BUILDING_ID_LIST, mBuildingList);
        outState.putInt(KEY_PROVIDER, mCurrentProvider.ordinal());
        outState.putInt(KEY_BUILDING, mCurrentBuilding);
        outState.putString(KEY_FILE_NAME, mCurrentMockFileName);
        outState.putBoolean(KEY_REPEAT, mRepeat);
    }

    @Override
    public PwBuildingMapManager onCreatePwBuildingMapManager() {
        final GoogleMap map = getMap();
        map.getUiSettings().setTiltGesturesEnabled(true);
        if (mPwBuildingMapManager != null) {
            mPwBuildingMapManager.removeLocationUpdates();
        }

        final PwRouteCallback mRouteCallback = getPwRouteCallback();
        PwRouteCallback routeCallbackWithToast = new PwRouteCallback() {
            @Override
            public void onSuccess(List<PwRoute> routes) {
                mRouteCallback.onSuccess(routes);
            }

            @Override
            public void onFail(int errorCode, String errorMessage) {
                Toast.makeText(getActivity().getApplicationContext(), "No route available", Toast.LENGTH_SHORT).show();
                mRouteCallback.onFail(errorCode, errorMessage);
            }
        };

        mPwBuildingMapManager = new PwMapOverlayManagerBuilder(getActivity().getApplicationContext(), getPwMap())
                .buildingId(mCurrentBuilding)
                .initialFloor(getResources().getIntArray(R.array.initial_floor)[findBuildingIndex(mCurrentBuilding)])
                .minimumFloorZoomLevel(MINIMUM_FLOOR_ZOOM_LEVEL)
                .minimumMarkerZoomLevel(MINIMUM_MARKER_ZOOM_LEVEL)
                .routeCallback(routeCallbackWithToast)
                .showMyLocation(true)
                .floorChangedCallback(this)
                .pwOnBuildingDataLoadedCallback(this)
                .pwOnBuildingPOIDataLoadedCallback(this)
                .mapLoadedCallback(this)
                .pwSnapToRouteCallback(this)
                .build();

        // Modify Google Map after PwBuildingMapManager has applied its defaults
        map.setMapType(mCurrentMapType);

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                hideInfoWindow(marker);
                if (mPwBuildingMapManager != null) {
                    final PwPoint point = mPwBuildingMapManager.findPointByMarker(marker);
                    if (point != null) {
                        long pointId = point.getId();
                        showRouteSelectionFragment(pointId);
                    }
                }
            }
        });

        return mPwBuildingMapManager;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;

        super.onPrepareOptionsMenu(menu);
        updateMenuItems(menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapTypeSelectionOnClickListener = null;
        mPwBuildingMapManager = null;
        mMapTypeNames = null;
        mCurrentMockFileName = null;
        mFileList = null;
        mBuildingList = null;
        mCurrentProvider = null;
    }

    private void updateMenuItems(final Menu menu) {
        if (menu == null) return;

        final MenuItem mapTypesMenuItem = menu.findItem(R.id.menu_map_types);
        final String title = getResources().getString(R.string.mapping_type_menuitem, findMapTypeNameById(mCurrentMapType));
        mapTypesMenuItem.setTitle(title);

        menu.setGroupVisible(R.id.group_mock, mCurrentProvider == LocationProvider.MOCK);

        final MenuItem itemRepeat = menu.findItem(R.id.menu_repeat);
        itemRepeat.setChecked(mRepeat);

        final MenuItem itemBlueDotSmoothing = menu.findItem(R.id.menu_bluedot_smoothing);
        itemBlueDotSmoothing.setChecked(mPwBuildingMapManager.isBlueDotSmoothingEnabled());

        final MenuItem itemRouteSnapping = menu.findItem(R.id.menu_route_snapping);

        int selectedRouteSnapperTolerance = 0;
        if (mPwBuildingMapManager.getRouteSnappingTolerance() != null) selectedRouteSnapperTolerance = mPwBuildingMapManager.getRouteSnappingTolerance().ordinal();

        final String routeSnappingTitle = getResources().getString(R.string.menu_route_snapping, PwRouteSnappingTolerance.values()[selectedRouteSnapperTolerance]);
        itemRouteSnapping.setTitle(routeSnappingTitle);
    }

    private String findMapTypeNameById(int mapType) {
        if (mMapTypeNames == null) {
            mMapTypeNames = getResources().getStringArray(R.array.mapping_types);
        }
        return mMapTypeNames[findMapTypeIndexById(mapType)];
    }

    private int findMapTypeIndexById(int mapType) {
        for (int i = 0; i < MAP_TYPES.length; i++) {
            if (MAP_TYPES[i] == mapType) {
                return i;
            }
        }
        return DEFAULT_MENU_ITEM_SELECTION;
    }

    private int findProviderIndex(LocationProvider provider) {
        for (int i = 0; i < LocationProvider.values().length; i++) {
            if (LocationProvider.values()[i] == provider) {
                return i;
            }
        }
        return DEFAULT_MENU_ITEM_SELECTION;
    }

    private int findFileIndex(String fileName) {
        for (int i = 0; i < mFileList.length; i++) {
            if (TextUtils.equals(mFileList[i], fileName)) {
                return i;
            }
        }
        return DEFAULT_MENU_ITEM_SELECTION;
    }

    private int findBuildingIndex(int buildingId) {
        int[] buildingIdList = getResources().getIntArray(R.array.building_id);
        for (int i = 0; i < buildingIdList.length; i++) {
            if (buildingIdList[i] == buildingId) {
                return i;
            }
        }
        return DEFAULT_MENU_ITEM_SELECTION;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_goto_building && mPwBuildingMapManager != null) {
            mPwBuildingMapManager.centerOnBuilding();
        } else if (item.getItemId() == R.id.menu_map_types) {
            createMapTypeSelectionDialogFragment().show(getChildFragmentManager(), TAG);
        } else if (item.getItemId() == R.id.menu_provider) {
            createProviderSelectionDialogFragment().show(getChildFragmentManager(), TAG);
        } else if (item.getItemId() == R.id.menu_building) {
            createBuildingSelectionDialogFragment().show(getChildFragmentManager(), TAG);
        } else if (item.getItemId() == R.id.menu_mock_file) {
            createMockLocationFileSelectionDialogFragment().show(getChildFragmentManager(), TAG);
        } else if (item.getItemId() == R.id.menu_repeat) {
            item.setChecked(!item.isChecked());
            mRepeat = item.isChecked();
            if (mCurrentProvider == LocationProvider.MOCK) {
                requestLocationUpdates(mPwBuildingMapManager.getBuilding());
            }
        } else if (item.getItemId() == R.id.menu_clear_cache) {
            mPwBuildingMapManager.clearCache(getActivity().getApplicationContext());
            Toast.makeText(getActivity().getApplicationContext(), R.string.text_clear_cache, Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.menu_poi_types) {
            showPOITypeDialogFragment();
        } else if (item.getItemId() == R.id.menu_remove_pois) {
            removePOIs();
        } else if (item.getItemId() == R.id.menu_add_pois) {
            addPOIs();
        } else if (item.getItemId() == R.id.menu_bluedot_smoothing) {
            item.setChecked(!item.isChecked());
            mPwBuildingMapManager.setBlueDotSmoothingEnabled(item.isChecked());
        } else if (item.getItemId() == R.id.menu_route_snapping) {
            showRouteSnapperTolerance();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showRouteSnapperTolerance() {

        final PwRouteSnappingTolerance[] toleranceValues = PwRouteSnappingTolerance.values();
        final String[] tolerances = new String[toleranceValues.length];

        int selectedIndex = 0;
        for (int i = 0; i < tolerances.length; i++) {
            tolerances[i] = toleranceValues[i].toString();
            if (toleranceValues[i].equals(mPwBuildingMapManager.getRouteSnappingTolerance())) selectedIndex = i;
        }

        final MenuItemSelectionDialogFragment menuItemSelectionDialogFragment = MenuItemSelectionDialogFragment.newInstance(
            selectedIndex,
            tolerances,
            R.string.menu_route_snapping_tolerance);
        menuItemSelectionDialogFragment.setOnClickListener(mRouteSnapperToleranceSelectionOnClickListener);
        menuItemSelectionDialogFragment.show(getFragmentManager(), TAG);
    }

    private void removePOIs() {
        if (mPwPoints == null) {
            Toast.makeText(getActivity().getApplicationContext(), "No PwPoint, nothing to remove.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBuildingMarkers != null && mBuildingMarkers.size() > 0) {
            Toast.makeText(getActivity().getApplicationContext(), "Already removed.", Toast.LENGTH_SHORT).show();
            return;
        }

        mBuildingMarkers = new ArrayList<PwBuildingMarker>();

        int size = mPwPoints.size();
        PwPoint point;
        for (int i = 0; i <size; i++) {
            point = mPwPoints.get(i);
            if (point.getPoiType() == 5000) { // Business Facility
                PwBuildingMarker marker = mPwBuildingMapManager.getBuildingMarkerFromPoint(point.getId());
                mBuildingMarkers.add(marker);
                marker.remove();
            }
        }

        // Force redraw the map
        getPwMap().invalidate();
    }

    private void addPOIs() {
        if (mBuildingMarkers == null || mBuildingMarkers.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "No POI removed, nothing to add.", Toast.LENGTH_SHORT).show();
            return;
        }

        int size = mBuildingMarkers.size();
        for (int i = 0; i < size; i++) {
            final PwBuildingMarker buildingMarker = mBuildingMarkers.get(i);
            getPwMap().addMarker(buildingMarker.getMarkerOptions());
        }

        mBuildingMarkers.clear();
        mBuildingMarkers = null;

        // Force redraw the map
        getPwMap().invalidate();
    }

    private void showPOITypeDialogFragment() {
        PwMappingModule.getInstance().getPOITypes(getActivity().getApplicationContext(), new PwOnPOITypesDownloadListener() {
            @Override
            public void onSuccess(SparseArray<PwPointType> poiTypes) {
                int size = poiTypes.size();
                if (size > 0) {
                    CharSequence[] list = new CharSequence[size];

                    StringBuilder sb = new StringBuilder();
                    PwPointType type;
                    for (int i = 0; i < size; i++) {
                        sb.setLength(0);
                        type = poiTypes.valueAt(i);
                        sb.append(type.getId()).append(": ").append(type.getDescription());
                        list[i] = sb.toString();
                    }

                    final MenuItemSelectionDialogFragment menuItemSelectionDialogFragment = MenuItemSelectionDialogFragment.newInstance(
                        0,
                        list,
                        R.string.menu_poi_types);
                    menuItemSelectionDialogFragment.setOnClickListener(mPOITypeSelectionOnClickListener);
                    menuItemSelectionDialogFragment.show(getFragmentManager(), TAG);
                }
            }

            @Override
            public void onFailed() {
                Toast.makeText(getActivity().getApplicationContext(), R.string.text_retrieve_poi_types_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMapType(final int position) {
        final GoogleMap map = getMap();

        if (map != null) {
            final int mapType = MAP_TYPES[position];

            if (map.getMapType() != mapType) {
                map.setMapType(mapType);
                mCurrentMapType = mapType;
            }
        }
    }

    // Re-enable location updates because they are removed in PwMappingFragment.onPause()
    @Override
    public void onResume() {
        super.onResume();
        if (mPwBuildingMapManager != null && mPwBuildingMapManager.getBuilding() != null) {
            requestLocationUpdates(mPwBuildingMapManager.getBuilding());
        }
    }

    @Override
    public void onFinishLoadingBuilding(PwBuilding pwBuilding) {
        //Don't call center if the view has not yet been laid out.
        if (getView().getHeight() > 0) {
            mPwBuildingMapManager.centerOnBuilding();
        }
        setupActionBar();
        requestLocationUpdates(pwBuilding);
    }

    private void requestLocationUpdates(PwBuilding pwBuilding) {
        try {
            mPwBuildingMapManager.removeLocationUpdates();
            final Context applicationContext = getActivity().getApplicationContext();
            PwLocationProvider locationProvider = null;
            switch (mCurrentProvider) {
                case FUSED:
                    locationProvider = FusedLocationProviderFactory.getInstance(applicationContext).createLocationProvider(pwBuilding);
                    break;
                case MOCK:
                    locationProvider = MockLocationProviderFactory.getInstance(applicationContext).createLocationProvider(MOCK_LOCATION_DIRECTORY + '/' + mCurrentMockFileName, this, mRepeat);
                    break;
                case MSE:
                    locationProvider = MseLocationProviderFactory.getInstance(applicationContext).createLocationProvider(pwBuilding);
                    break;
                case QUALCOMM:
                    locationProvider = QualcommLocationProviderFactory.getInstance(applicationContext).createLocationProvider(pwBuilding);
                    break;
                case SENION:
                    locationProvider = SenionLocationProviderFactory.getInstance(applicationContext).createLocationProvider(pwBuilding);
                    break;
            }

            if (locationProvider == null) {
                PwLog.e(TAG, "locationProvider is null, this should never happen, go check the code");
            } else {
                mPwBuildingMapManager.requestLocationUpdates(applicationContext, locationProvider);
                setupMyLocationButton();
            }
        } catch (Exception ex) {
            PwLog.e(TAG, "Error occurs in requestLocationUpdates", ex);
        }
    }

    //Used with the MockLocationProvider to notify the user that Mock Locations are disabled on his device
    @Override
    public void onMockLocationsDisabled() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enable Mock Locations");
        builder.setMessage("Select Allow mock locations from the Debugging section");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                startActivity(intent);
            }
        });
        builder.show();
    }

    @Override
    public void snapToRouteStarted() {
        Toast.makeText(getActivity().getApplicationContext(), "Snap to route started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void snapToRouteStopped() {
        Toast.makeText(getActivity().getApplicationContext(), "Snap to route stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBuildingPOILoaded(final List<PwPoint> points) {
        try {
            Toast.makeText(getActivity().getApplicationContext(), points.size() + " POI loaded", Toast.LENGTH_SHORT).show();
            this.mPwPoints = points;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onBuildingPOIFailed(String errorMessage) {
        Toast.makeText(getActivity().getApplicationContext(), "POI load failed: " + errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void showInfoWindow(final Marker marker) {
        marker.setSnippet(getString(com.phunware.mapping.library.R.string.mapping_click_to_route));
        marker.setVisible(true);
        marker.showInfoWindow();
    }

    protected void showRouteSelectionFragment(final long pointId) {
        final RouteEndPointsDialogFragment fragment = RouteEndPointsDialogFragment.newInstance(mPwBuildingMapManager.getBuilding(),
            (ArrayList<PwPoint>) mPwBuildingMapManager.getBuildingPoints(), findPointById(pointId, mPwBuildingMapManager.getBuildingPoints()),
            mPwBuildingMapManager.getMyLocation() != null);
        fragment.setPwRouteRequestedListener(new RouteEndPointsDialogFragment.PwRouteRequestedListener() {
            @Override
            public void onRouteRequested(PwPoint startPoint, PwPoint endPoint, boolean isAccessible) {

                if (mPwBuildingMapManager != null && startPoint.getId() != Long.MIN_VALUE) {
                    mRouteStartPoint = startPoint;
                    mPwBuildingMapManager.createRoute(getActivity(), startPoint.getId(), endPoint.getId(), isAccessible);
                    setSupportProgressBarIndeterminateVisibility(true);
                } else if (mPwBuildingMapManager != null) {
                    mRouteStartPoint = new PwPoint(getString(com.phunware.mapping.library.R.string.mapping_my_location));
                    //User chose "My Location" as start Point.
                    mPwBuildingMapManager.createRoute(getActivity(), endPoint.getId(), isAccessible);
                    setSupportProgressBarIndeterminateVisibility(true);
                }
                // Set follow mode back to Normal
                applyMode(MyLocationLayer.MODE_NORMAL);

                hideInfoWindow(mSelectedMarker);
                mRouteEndPoint = endPoint;
            }
        });
        fragment.show(getChildFragmentManager(), RouteEndPointsDialogFragment.TAG);
    }
}

