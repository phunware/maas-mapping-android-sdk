package com.phunware.mapping.sample.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.phunware.core.OkHttpClientCached;
import com.phunware.location.provider.PwMockLocationProvider;
import com.phunware.mapping.library.maps.PwBuildingMapManager;
import com.phunware.mapping.library.maps.PwMapOverlayManagerBuilder;
import com.phunware.mapping.library.ui.PwMappingFragment;
import com.phunware.mapping.model.PwBuilding;
import com.phunware.mapping.sample.R;
import com.phunware.mapping.sample.providers.FusedLocationProviderFactory;
import com.phunware.mapping.sample.providers.LocationProvider;
import com.phunware.mapping.sample.providers.MockLocationProviderFactory;
import com.phunware.mapping.sample.providers.MseLocationProviderFactory;
import com.phunware.mapping.sample.providers.QualcommLocationProviderFactory;
import com.phunware.mapping.sample.providers.SenionLocationProviderFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Shows a map of the venue.
 */
public class MappingSampleFragment extends PwMappingFragment implements PwMockLocationProvider.MockLocationsDisabledListener {
    private static final String TAG = MappingSampleFragment.class.getSimpleName();

    private static final String KEY_MAP_TYPE = "map_type";
    private static final String KEY_PROVIDER = "provider";
    private static final String KEY_BUILDING = "building";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_REPEAT = "repeat";
    // Assumes order of Google map types array is same as order of R.array.mapping_types
    private static final int[] MAP_TYPES = new int[]{GoogleMap.MAP_TYPE_NORMAL, GoogleMap.MAP_TYPE_HYBRID, GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_NONE};
    private static final String KEY_MAP_TYPE_NAMES = "map_type_names";
    private static final String MOCK_LOCATION_DIRECTORY = "mocklocation";
    private static final float MINIMUM_MARKER_ZOOM_LEVEL = 17f;
    private static final float MINIMUM_FLOOR_ZOOM_LEVEL = 13f;

    public static final int DEFAULT_MENU_ITEM_SELECTION = 0;

    private String[] mMapTypeNames;
    private String[] mFileList;
    private int[] mBuildingList;

    private int mCurrentMapType;
    private LocationProvider mCurrentProvider = LocationProvider.values()[0];
    private int mCurrentBuilding;
    private String mCurrentMockFileName;
    private boolean mRepeat;

    private DialogInterface.OnClickListener mMapTypeSelectionOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            updateMapType(which);
            dialog.dismiss();
        }
    };

    private DialogInterface.OnClickListener mProviderSelectionOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mCurrentProvider = LocationProvider.values()[which];
            requestLocationUpdates(mPwBuildingMapManager.getBuilding());
            setupMyLocationButton();
            dialog.dismiss();
        }
    };

    private DialogInterface.OnClickListener mBuildingSelectionOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mCurrentBuilding = mBuildingList[which];
            getMap().clear();
            createPwMapOverlayManagerBuilder(null);
            dialog.dismiss();
        }
    };

    private DialogInterface.OnClickListener mFileNameSelectionOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mCurrentMockFileName = mFileList[which];
            requestLocationUpdates(mPwBuildingMapManager.getBuilding());
            dialog.dismiss();
        }
    };

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

        mPwBuildingMapManager = new PwMapOverlayManagerBuilder(getActivity().getApplicationContext(), map)
                .buildingId(mCurrentBuilding)
                .initialFloor(getResources().getIntArray(R.array.initial_floor)[findBuildingIndex(mCurrentBuilding)])
                .minimumFloorZoomLevel(MINIMUM_FLOOR_ZOOM_LEVEL)
                .minimumMarkerZoomLevel(MINIMUM_MARKER_ZOOM_LEVEL)
                .routeCallback(getPwRouteCallback())
                .showMyLocation(true)
                .floorChangedCallback(this)
                .pwOnBuildingDataLoadedCallback(this)
                .mapLoadedCallback(this)
                .build();

        // Modify Google Map after PwBuildingMapManager has applied its defaults
        map.setMapType(mCurrentMapType);
        return mPwBuildingMapManager;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
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
        final MenuItem mapTypesMenuItem = menu.findItem(R.id.menu_map_types);
        final String title = getResources().getString(R.string.mapping_type_menuitem, findMapTypeNameById(mCurrentMapType));
        mapTypesMenuItem.setTitle(title);

        menu.setGroupVisible(R.id.group_mock, mCurrentProvider == LocationProvider.MOCK);

        final MenuItem item = menu.findItem(R.id.menu_repeat);
        item.setChecked(mRepeat);
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
            OkHttpClientCached.flushCache(getActivity().getApplicationContext());
            Toast.makeText(getActivity().getApplicationContext(), R.string.text_clear_cache, Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
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
        mPwBuildingMapManager.removeLocationUpdates();
        switch (mCurrentProvider) {
            case FUSED:
                mPwBuildingMapManager.requestLocationUpdates(getActivity().getApplicationContext(), FusedLocationProviderFactory.getInstance(getActivity()
                        .getApplicationContext()).createLocationProvider(pwBuilding));
                break;
            case MOCK:
                if (TextUtils.isEmpty(mCurrentMockFileName)) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.mapping_no_mock_file, Toast.LENGTH_LONG).show();
                } else {
                    mPwBuildingMapManager.requestLocationUpdates(getActivity().getApplicationContext(), MockLocationProviderFactory.getInstance(getActivity()
                        .getApplicationContext()).createLocationProvider(MOCK_LOCATION_DIRECTORY + '/' + mCurrentMockFileName, this, mRepeat));
                }
                break;
            case MSE:
                mPwBuildingMapManager.requestLocationUpdates(getActivity().getApplicationContext(), MseLocationProviderFactory.getInstance(getActivity()
                        .getApplicationContext()).createLocationProvider(pwBuilding));
                break;
            case QUALCOMM:
                mPwBuildingMapManager.requestLocationUpdates(getActivity().getApplicationContext(), QualcommLocationProviderFactory.getInstance(getActivity()
                        .getApplicationContext()).createLocationProvider(pwBuilding));
                break;
            case SENION:
                mPwBuildingMapManager.requestLocationUpdates(getActivity().getApplicationContext(), SenionLocationProviderFactory.getInstance(getActivity()
                        .getApplicationContext()).createLocationProvider(pwBuilding));
                break;
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
}

