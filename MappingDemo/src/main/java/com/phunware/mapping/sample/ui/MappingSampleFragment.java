package com.phunware.mapping.sample.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ImageButton;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.phunware.core.PwLog;
import com.phunware.location.provider.PwLocationProvider;
import com.phunware.location.provider.PwMockLocationProvider;
import com.phunware.mapping.PwMappingModule;
import com.phunware.mapping.PwOnPOITypesDownloadListener;
import com.phunware.mapping.PwRouteCallback;
import com.phunware.mapping.library.maps.MapUtils;
import com.phunware.mapping.library.maps.MyLocationLayer;
import com.phunware.mapping.library.maps.PWOnManeuverChangedCallBack;
import com.phunware.mapping.library.maps.PWOnRouteStepChangedCallBack;
import com.phunware.mapping.library.maps.PwBuildingMapManager;
import com.phunware.mapping.library.maps.PwOnBuildingPOIDataLoadedCallback;
import com.phunware.mapping.library.maps.PwOnSnapToRouteCallback;
import com.phunware.mapping.library.maps.PwRouteSnappingTolerance;
import com.phunware.mapping.library.maps.directions.PwDirections;
import com.phunware.mapping.library.maps.directions.PwDirectionsCalculateCallback;
import com.phunware.mapping.library.maps.directions.PwDirectionsItem;
import com.phunware.mapping.library.maps.directions.PwDirectionsOptions;
import com.phunware.mapping.library.maps.directions.PwDirectionsRequest;
import com.phunware.mapping.library.maps.directions.PwDirectionsResponse;
import com.phunware.mapping.library.ui.PwBuildingMarker;
import com.phunware.mapping.library.ui.PwMappingFragment;
import com.phunware.mapping.model.PWRouteManeuver;
import com.phunware.mapping.model.PwBuilding;
import com.phunware.mapping.model.PwPoint;
import com.phunware.mapping.model.PwPointType;
import com.phunware.mapping.model.PwRoute;
import com.phunware.mapping.model.RouteStep;
import com.phunware.mapping.sample.R;
import com.phunware.mapping.sample.maps.MapOverlayManagerBuilder;
import com.phunware.mapping.sample.providers.LocationProvider;
import com.phunware.mapping.sample.providers.MockLocationProviderFactory;
import com.phunware.mapping.sample.providers.MseLocationProviderFactory;
import com.phunware.mapping.sample.providers.SenionLocationProviderFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Shows a map of the venue.
 */
public class MappingSampleFragment extends PwMappingFragment implements PwMockLocationProvider.MockLocationsDisabledListener, PwOnSnapToRouteCallback, PwOnBuildingPOIDataLoadedCallback ,PWOnManeuverChangedCallBack{
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
    private static final float MINIMUM_FLOOR_ZOOM_LEVEL = 17f;

    public static final int DEFAULT_MENU_ITEM_SELECTION = 0;
    public static final long FLAT_MARKER_ID = 90000001L;

    private String[] mMapTypeNames;
    private String[] mFileList;
    private int[] mBuildingList;

    private int mCurrentMapType;
    private LocationProvider mCurrentProvider = LocationProvider.NONE;
    private int mCurrentBuilding;
    private String mCurrentMockFileName;
    private boolean mRepeat;
    private Button changeManeuver;
    private ViewFlipper mManeuverFlipper;
    private RelativeLayout setHeadManeuver;
    private TextView txtTotalDistance;
    private TextView txtEstimatedTime;
    private ListView lstManeuvers;
    private ManeuverAdapter mManeuverAdapter;
    private RelativeLayout setOfDirections;
    private ImageButton btnShowList;
    private ImageButton btnVoiceOut;
    private Button btnClose;
    private float lastX;
    ArrayList<PWRouteManeuver> maneuvers;
    private RelativeLayout headerRouteDisplay;
    private ManeuverDisplayHelper maneuverDisplayHelper;
    TextToSpeech textToSpeech;


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
    private Marker mFlatMarker;

    private DialogFragment mStopLoadingBuildingDialogFragment = createStopLoadingBuildingDialogFragment();

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
        maneuverDisplayHelper=new ManeuverDisplayHelper();
        linkManeuverUItoCode(view);
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

        textToSpeech=new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });
        return view;
    }

    private void linkManeuverUItoCode(View view){
        btnClose = (Button)view.findViewById(R.id.btnClose);
        btnShowList = (ImageButton)view.findViewById(R.id.btnShowList);
        setOfDirections = (RelativeLayout)view.findViewById(R.id.setOfDirections);
        lstManeuvers = (ListView) view.findViewById(R.id.lstManeuvers);
        txtTotalDistance = (TextView)view.findViewById(R.id.txtTotalDistance);
        txtEstimatedTime = (TextView)view.findViewById(R.id.txtEstimatedTime);
        setHeadManeuver = (RelativeLayout)view.findViewById(R.id.mSetHeadManeuver);
        mManeuverFlipper = (ViewFlipper) view.findViewById(R.id.mManeuverFlipper);
        headerRouteDisplay= (RelativeLayout) view.findViewById(R.id.mSetHeadManeuver);
        btnVoiceOut=(ImageButton) view.findViewById(R.id.btnSound);
        btnVoiceOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnVoiceOut.isActivated())
                    btnVoiceOut.setActivated(false);
                else
                    btnVoiceOut.setActivated(true);
            }
        });

        mManeuverFlipper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (mPwBuildingMapManager.getMyLocationMode() != MyLocationLayer.MODE_FOLLOW_ME) {
                    switch (motionEvent.getAction()) {

                        case MotionEvent.ACTION_DOWN:
                            lastX = motionEvent.getX();
                            break;
                        case MotionEvent.ACTION_UP:
                            float currentX = motionEvent.getX();

                            // Handling left to right screen swap.
                            if (lastX < currentX) {

                                // If there aren't any other children, just break.
                                if (mManeuverFlipper.getDisplayedChild() == 0)
                                    break;

                                // Next screen comes in from left.
                                mManeuverFlipper.setInAnimation(getActivity(), R.anim.slide_in_from_left);
                                // Current screen goes out from right.
                                mManeuverFlipper.setOutAnimation(getActivity(), R.anim.slide_out_to_right);


                                // Display next screen.
                                mManeuverFlipper.showPrevious();
                                if(btnVoiceOut.isActivated()) {
                                    playTTS("message");
                                }
                                PWRouteManeuver previousManeuver = mPwBuildingMapManager.getPreviousManeuver();
                                if (previousManeuver != null) {
                                    mManeuverAdapter.decreaseIndexOfDirection();
                                    if (previousManeuver.isTurnManeuver && !previousManeuver.isPortalManeuver())
                                        mPwBuildingMapManager.changeManeuver(mPwBuildingMapManager.getPreviousManeuver());
                                    else
                                        mPwBuildingMapManager.changeManeuver(mPwBuildingMapManager.getCurrentManeuver());
                                }

                            }

                            // Handling right to left screen swap.
                            if (lastX > currentX) {

                                // If there is a child (to the left), kust break.
                                if (mManeuverFlipper.getDisplayedChild() == (mManeuverFlipper.getChildCount() - 1))
                                    break;

                                // Next screen comes in from right.
                                mManeuverFlipper.setInAnimation(getActivity(), R.anim.slide_in_from_right);
                                // Current screen goes out from left.
                                mManeuverFlipper.setOutAnimation(getActivity(), R.anim.slide_out_to_left);

                                // Display previous screen.

                                mManeuverFlipper.showNext();
                                if(btnVoiceOut.isActivated())
                                    playTTS("message");
                                PWRouteManeuver nextManeuver = mPwBuildingMapManager.getNextManeuver();
                                if (nextManeuver != null) {
                                    mManeuverAdapter.increaseIndexOfDirection();

                                    if (nextManeuver.isTurnManeuver && !nextManeuver.isPortalManeuver())
                                        mPwBuildingMapManager.changeManeuver(mPwBuildingMapManager.getNextManeuver());
                                    else
                                        mPwBuildingMapManager.changeManeuver(mPwBuildingMapManager.getCurrentManeuver());

                                }

                            }
                            break;
                    }


                    return true;
                } else
                    return false;
            }

        });

        btnShowList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManeuverFlipper.setVisibility(mManeuverFlipper.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                setHeadManeuver.setVisibility(setHeadManeuver.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                setOfDirections.setVisibility(setOfDirections.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManeuverFlipper.setVisibility(mManeuverFlipper.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                setHeadManeuver.setVisibility(setHeadManeuver.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                setOfDirections.setVisibility(setOfDirections.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void playTTS(String utteranceID) {
        View v = mManeuverFlipper.getCurrentView();
        TextView tv_tts = (TextView) v.findViewById(R.id.textInstruction);
        HashMap<String, String> ttsHashMap = new HashMap<String, String>();
        ttsHashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceID);
        textToSpeech.speak(tv_tts.getText().toString(), TextToSpeech.QUEUE_FLUSH, ttsHashMap);
    }



    private void fillManeuverFlipper(ArrayList<PWRouteManeuver> maneuversR){
        maneuverDisplayHelper.setBuilding(mPwBuildingMapManager.getBuilding());
        fillListOfManeuvers(maneuversR);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 145 );
        maneuvers= filterManeuvers(maneuversR);
        int size = maneuvers.size();
        double totalDistance = 0;

        View lefSeparator = null;
        View rigthSeparator = null;
        ImageView imgIndicator = null;
        ImageView imgIndicatorNext = null;
        TextView textInstruction = null;
        TextView textNextDirection = null;
        TextView textNext = null;

        for(int i = 0; i < size; i++) {
            View tempView = inflater.inflate(R.layout.card_view, mManeuverFlipper, false);
            tempView.setLayoutParams(params);

            PWRouteManeuver iter = maneuvers.get(i);

            lefSeparator = tempView.findViewById(R.id.lefSeparator);
            rigthSeparator = tempView.findViewById(R.id.rigthSeparator);
            imgIndicator = (ImageView)tempView.findViewById(R.id.imgIndicator);
            imgIndicatorNext = (ImageView)tempView.findViewById(R.id.imgIndicatorNext);
            textInstruction = (TextView)tempView.findViewById(R.id.textInstruction);
            textNextDirection = (TextView)tempView.findViewById(R.id.textNextDirection);
            textNext = (TextView)tempView.findViewById(R.id.textNext);

            lefSeparator.setVisibility(i == 0 ? View.INVISIBLE : View.VISIBLE);
            rigthSeparator.setVisibility(i == (size - 1) ? View.VISIBLE : View.INVISIBLE);

            if(i == (size - 1)){
                imgIndicator.setImageResource(R.drawable.arrow_final);
                imgIndicatorNext.setVisibility(View.GONE);
                textInstruction.setText( iter.getPoints().get(0).getName());
                textNextDirection.setVisibility(View.GONE);
                textNext.setVisibility(View.GONE);
            } else{
                imgIndicatorNext.setVisibility(View.VISIBLE);
                textNextDirection.setVisibility(View.VISIBLE);
                textNext.setVisibility(View.VISIBLE);
                imgIndicator.setImageResource(maneuverDisplayHelper.getImageResourceForDirection(iter));
                imgIndicatorNext.setImageResource(maneuverDisplayHelper.getImageResourceForDirection(iter.getNextManeuver()));
            }

            textInstruction.setText(maneuverDisplayHelper.stringForDirection(iter));

            if(iter.getNextManeuver() != null) {
                textNextDirection.setText(maneuverDisplayHelper.stringForDirection(iter.getNextManeuver()));
            }
            totalDistance += iter.distance;

            mManeuverFlipper.addView(tempView, i);
        }

        txtTotalDistance.setText( "Total distance: " + getDistanceInFeet(totalDistance));
        txtEstimatedTime.setText("Estimated time: " + MapUtils.estimatedTimeStringForDistance((totalDistance)));

        mManeuverFlipper.setVisibility(View.VISIBLE);
        setHeadManeuver.setVisibility(View.VISIBLE);
    }

    private String getDistanceInFeet(double totalDistance){
        double res = totalDistance * 3.28084;
        res = Math.ceil( res );
        return "" + res + " feet";
    }

    private void fillListOfManeuvers(ArrayList<PWRouteManeuver> maneuversR){
        ArrayList<PWRouteManeuver> maneuvers = cleanManeuversforList(maneuversR);

        if(mManeuverAdapter == null)
            mManeuverAdapter = new ManeuverAdapter(this.getActivity(),maneuvers,mPwBuildingMapManager );
        else{
            mManeuverAdapter.clear();
            mManeuverAdapter.addAll(maneuvers);
        }

        lstManeuvers.setAdapter(mManeuverAdapter);
        mManeuverAdapter.notifyDataSetChanged();
    }

    private ArrayList<PWRouteManeuver> filterManeuvers(ArrayList<PWRouteManeuver> maneuvers){
        ArrayList<PWRouteManeuver> array = new ArrayList<PWRouteManeuver>();

        for(PWRouteManeuver maneuver : maneuvers){
            if(maneuver != null && !maneuver.isTurnManeuver()){
                array.add(maneuver);
            }
        }

        return array;
    }

    private ArrayList<PWRouteManeuver> cleanManeuversforList(ArrayList<PWRouteManeuver>  maneuvers){
        ArrayList<PWRouteManeuver> array = new ArrayList<PWRouteManeuver>();

        for(PWRouteManeuver maneuver : maneuvers){
            if(maneuver != null && !maneuver.isTurnManeuver()){
                array.add(maneuver);
                array.add(maneuver.getNextManeuver());
            }
        }

        array.remove(array.size() - 1);

        return array;
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


   private DialogFragment createStopLoadingBuildingDialogFragment() {
        final StopLoadingBuildingDialogFragment fragment = new StopLoadingBuildingDialogFragment();
        fragment.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mPwBuildingMapManager.stopLoadingBuilding(getActivity().getApplicationContext());
                fragment.dismiss();
            }
        });
        return fragment;
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

        MapOverlayManagerBuilder builder =  new MapOverlayManagerBuilder(getActivity().getApplicationContext(), getPwMap());
        builder.setSetupMapDataListener(new MapOverlayManagerBuilder.SetupMapDataListener() {
            @Override
            public void onSetupMapData() {
                mStopLoadingBuildingDialogFragment.show(getFragmentManager(), TAG);
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        mPwBuildingMapManager = builder.buildingId(mCurrentBuilding)
                .initialFloor(getResources().getIntArray(R.array.initial_floor)[findBuildingIndex(mCurrentBuilding)])
                .minimumFloorZoomLevel(MINIMUM_FLOOR_ZOOM_LEVEL)
                .minimumMarkerZoomLevel(MINIMUM_MARKER_ZOOM_LEVEL)
                .routeCallback(routeCallbackWithToast)
                .showMyLocation(false)
                .floorChangedCallback(this)
                .pwOnBuildingDataLoadedCallback(this)
                .pwOnBuildingPOIDataLoadedCallback(this)
                .mapLoadedCallback(this)
                .pwSnapToRouteCallback(this)
                .pwOnManeuverChangedCallBack(this)
                .PwOnRouteStepChangedCallback(this)
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
                    } else {
                        if (marker.getId().equals(mFlatMarker.getId())) {
                            showRouteSelectionFragment(mFlatMarker.hashCode());
                        }
                    }
                }
            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (mFlatMarker != null) {
                    mFlatMarker.remove();
                }

                mFlatMarker = getPwMap().getMap().addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(getString(R.string.mapping_flat_marker))
                                .anchor(0.5f, 1f)
                );
            }
        });

        return mPwBuildingMapManager;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mFlatMarker != null && marker.getId().equals(mFlatMarker.getId())) return false;

        return super.onMarkerClick(marker);
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

    public void setManeuverFlipperToCurrentManeuver(PWRouteManeuver maneuver)
    {
        if(maneuvers!= null && mPwBuildingMapManager.isRouteAvailable()) {
            for (int i = 0; i < maneuvers.size(); i++) {
                if (maneuver.getIndex() == maneuvers.get(i).getIndex())
                    if (mManeuverFlipper != null)
                        mManeuverFlipper.setDisplayedChild(i);
            }
        }

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
        if (mPwBuildingMapManager != null) itemBlueDotSmoothing.setChecked(mPwBuildingMapManager.isBlueDotSmoothingEnabled());

        final MenuItem itemRouteSnapping = menu.findItem(R.id.menu_route_snapping);

        int selectedRouteSnapperTolerance = 0;
        if (mPwBuildingMapManager != null)  if (mPwBuildingMapManager.getRouteSnappingTolerance() != null) selectedRouteSnapperTolerance = mPwBuildingMapManager.getRouteSnappingTolerance().ordinal();

        final String routeSnappingTitle = getResources().getString(R.string.menu_route_snapping, PwRouteSnappingTolerance.values()[selectedRouteSnapperTolerance]);
        itemRouteSnapping.setTitle(routeSnappingTitle);

        final MenuItem itemPOIZoomLevel = menu.findItem(R.id.menu_poi_zoom_level);
        if (mPwBuildingMapManager != null) itemPOIZoomLevel.setChecked(mPwBuildingMapManager.isMarkerZoomLevelsEnabled());
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
        } else if (item.getItemId() == R.id.menu_stop_loading_building) {
            mPwBuildingMapManager.stopLoadingBuilding(getActivity().getApplicationContext());
        } else if (item.getItemId() == R.id.menu_poi_zoom_level) {
            item.setChecked(!item.isChecked());
            mPwBuildingMapManager.setMarkerZoomLevelsEnabled(item.isChecked());
        } else if (item.getItemId() == R.id.menu_bluedot) {
            item.setChecked(!item.isChecked());
            mPwBuildingMapManager.setBluedotEnabled(item.isChecked());
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

    @Override
    public void actionClearRoutes()
    {
        super.actionClearRoutes();
        mManeuverFlipper.removeAllViews();
        mManeuverFlipper.setVisibility(View.INVISIBLE);
        headerRouteDisplay.setVisibility(View.GONE);

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

    @Override
    public void onFailToLoadBuilding(String errorMessage) {
        super.onFailToLoadBuilding(errorMessage);

        mStopLoadingBuildingDialogFragment.dismiss();
    }

    private void requestLocationUpdates(PwBuilding pwBuilding) {
        try {
            final Context applicationContext = getActivity().getApplicationContext();
            PwLocationProvider locationProvider = null;
            switch (mCurrentProvider) {
                case MOCK:
                    locationProvider = MockLocationProviderFactory.getInstance(applicationContext).createLocationProvider(MOCK_LOCATION_DIRECTORY + '/' + mCurrentMockFileName, this, mRepeat);
                    break;
                case MSE:
                    locationProvider = MseLocationProviderFactory.getInstance(applicationContext).createLocationProvider(pwBuilding);
                    break;
                case BLE:
                    locationProvider = SenionLocationProviderFactory.getInstance(applicationContext).createLocationProvider(pwBuilding);
                    break;
                case NONE:
                    mPwBuildingMapManager.applyMode(MyLocationLayer.MODE_NORMAL);
                    mPwBuildingMapManager.removeLocationUpdates();
                    locationProvider = null;
                    break;
            }

            if (locationProvider == null) {
                PwLog.e(TAG, "locationProvider is null, this should never happen, go check the code");
            } else {
                mPwBuildingMapManager.removeLocationUpdates();

                mPwBuildingMapManager.requestLocationUpdates(applicationContext, locationProvider);
                mPwBuildingMapManager.applyMode(MyLocationLayer.MODE_FOLLOW_ME);

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

            mStopLoadingBuildingDialogFragment.dismiss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onBuildingPOIFailed(String errorMessage) {
        Toast.makeText(getActivity().getApplicationContext(), "POI load failed: " + errorMessage, Toast.LENGTH_SHORT).show();

        mStopLoadingBuildingDialogFragment.dismiss();
    }

    @Override
    protected void showInfoWindow(final Marker marker) {
        marker.setSnippet(getString(com.phunware.mapping.library.R.string.mapping_click_to_route));
        marker.setVisible(true);
        marker.showInfoWindow();
    }

    @Override
    public void onManeuverChanged(PWRouteManeuver maneuver)
    {
        setManeuverFlipperToCurrentManeuver(maneuver);
    }

    @Override
    public void onRouteStepChanged(RouteStep step)
    {
        super.onRouteStepChanged(step);
        Toast.makeText(getActivity().getApplicationContext(),"changed routeStep is called",Toast.LENGTH_LONG).show();
    }

    protected void showRouteSelectionFragment(final long pointId) {

        ArrayList<PwPoint> buildingPoints = (ArrayList<PwPoint>) mPwBuildingMapManager.getBuildingPoints();

        final RouteEndPointsDialogFragment fragment = RouteEndPointsDialogFragment.newInstance(
                mPwBuildingMapManager.getBuilding(),
                buildingPoints,
                findPointById(pointId, mPwBuildingMapManager.getBuildingPoints()),
                mPwBuildingMapManager.getMyLocation() != null,
                mFlatMarker != null);

        fragment.setPwRouteRequestedListener(new RouteEndPointsDialogFragment.PwRouteRequestedListener() {
            @Override
            public void onRouteRequested(PwPoint startPoint, PwPoint endPoint, boolean isAccessible) {

                final PwDirectionsItem startItem;
                if (startPoint.getId() == Long.MIN_VALUE) { // My Location or Flat Marker
                    if (startPoint.getName().equals(getString(R.string.mapping_my_location))) { // My Location
                        startItem = new PwDirectionsItem(mPwBuildingMapManager.getMyLocation());
                    } else { // Flat Marker
                        startItem = new PwDirectionsItem(mPwBuildingMapManager.getDisplayedFloorId(), mFlatMarker.getPosition());
                    }
                } else {
                    startItem = new PwDirectionsItem(startPoint);
                }

                final PwDirectionsItem endItem;
                if (endPoint.getId() == Long.MIN_VALUE) { // My Location or Flat Marker
                    if (endPoint.getName().equals(getString(R.string.mapping_my_location))) { // My Location
                        endItem = new PwDirectionsItem(mPwBuildingMapManager.getMyLocation());
                    } else { // Flat Marker
                        endItem = new PwDirectionsItem(mPwBuildingMapManager.getDisplayedFloorId(), mFlatMarker.getPosition());
                    }
                } else {
                    endItem = new PwDirectionsItem(endPoint);
                }

                PwDirectionsOptions options = new PwDirectionsOptions();
                options.setRequireAccessibleRoutes(isAccessible);

                PwDirectionsRequest request = new PwDirectionsRequest(startItem, endItem, options);

                PwDirections directions = new PwDirections(request);
                setSupportProgressBarIndeterminateVisibility(true);
                directions.calculate(new PwDirectionsCalculateCallback() {
                    @Override
                    public void onSuccess(PwDirectionsResponse response) {
                        mPwBuildingMapManager.plotRoute(response.getRoutes());
                        PwRoute startRoute=response.getRoutes().get(0);
                        //Check if the Maneuvers array is not empty or null to call plotRouteManeuverOverlay
                        if(startRoute.getManeuvers() != null && (!startRoute.getManeuvers().isEmpty())) {
                            PwLog.w("AAA", "Maneuvers size: " + startRoute.getManeuvers().size());
                            mPwBuildingMapManager.plotManeuver();
                            if(mManeuverFlipper!=null)
                                mManeuverFlipper.removeAllViews();
                            fillManeuverFlipper(response.getRoutes().get(0).getManeuvers());
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMessage) {
                        Toast.makeText(getActivity().getApplicationContext(), "Route calculation error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });

                // Set follow mode back to Normal
                // applyMode(MyLocationLayer.MODE_NORMAL);

                hideInfoWindow(mSelectedMarker);
                mRouteEndPoint = endPoint;
            }
        });
        fragment.show(getChildFragmentManager(), RouteEndPointsDialogFragment.TAG);
    }
}

