package com.phunware.mapping.demo;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.phunware.core.PwCoreSession;
import com.phunware.mapping.PwMapView;
import com.phunware.mapping.PwMappingModule;
import com.phunware.mapping.PwOnBuildingDataListener;
import com.phunware.mapping.PwOnMapLoadCompleteListener;
import com.phunware.mapping.PwOnMapViewStateChangedListener;
import com.phunware.mapping.PwOnPOIDataListener;
import com.phunware.mapping.model.PwBuilding;
import com.phunware.mapping.model.PwPoint;

import java.util.List;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private PwMapView mPwMapView;
    private ProgressBar mProgressBar;
    private TextView mDebugText;

    private PwBuilding mPwBuilding;

    private static final long BUILDING_ID = YOUR_BUILDING_ID_HERE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPwMapView = (PwMapView) findViewById(R.id.pwMapView);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mDebugText = (TextView) findViewById(R.id.debug_text);

        findViewById(R.id.refresh).setOnClickListener(this);
        findViewById(R.id.reset).setOnClickListener(this);

        mPwMapView.onCreate(this, savedInstanceState);

        fetchMapData();

        mPwMapView.setOnMapLoadCompleteListener(new PwOnMapLoadCompleteListener() {
            @Override
            public void onLoad() {
                hideLoading();
            }
        });

        mPwMapView.setOnMapViewStateChangedListener(new PwOnMapViewStateChangedListener() {
            @Override
            public void onFloorChange(int previousFloor, int currentFloor) {
                setDebugText();
            }

            @Override
            public void onZoomLevelChange(int previousLevel, int currentLevel) {
                setDebugText();
            }
        });
    }

    /**
     * Helper method to fetch all building and point data for a building. The data will then be passed
     * to the PwMapView in this layout. Toasts will be shown if there are any problems.
     */
    private void fetchMapData() {
        showLoading();
        PwMappingModule.getInstance().getBuildingDataByIdInBackground(this, BUILDING_ID, new PwOnBuildingDataListener() {
            @Override
            public void onSuccess(PwBuilding pwBuilding) {
                mPwBuilding = pwBuilding;
                if (pwBuilding != null) {
                    // Building data exists!
                    mPwMapView.setMapData(pwBuilding);
                } else {
                    // No building data found or a network error occurred.
                    Toast.makeText(MainActivity.this, "Error loading building data.", Toast.LENGTH_SHORT).show();
                }
                setDebugText();
            }
        });

        PwMappingModule.getInstance().getPOIDataInBackground(this, BUILDING_ID, new PwOnPOIDataListener() {
            @Override
            public void onSuccess(List<PwPoint> pois) {
                if (pois != null) {
                    // POI data exists!
                    mPwMapView.setPOIList(pois);
                } else {
                    // No poi data found or a network error occurred.
                    Toast.makeText(MainActivity.this, "Error loading poi data.", Toast.LENGTH_SHORT).show();
                }
                setDebugText();
            }
        });
    }

    /**
     * Method to grab meta-data from the map view and display it in debug text.
     */
    private void setDebugText() {
        if (mPwBuilding != null) {
            final int floorCount = (mPwBuilding == null || mPwBuilding.getFloors() == null ? -1 : mPwBuilding.getFloors().size());
            String text =
                    mPwMapView.getCurrentFloorName() + ": " + mPwMapView.getCurrentFloorLevel() + "\n" +
                            "Indices: " + mPwMapView.getPreviousFloorIndex() + " / " + mPwMapView.getCurrentFloorIndex() + " / " + mPwMapView.getNextFloorIndex() + " of " + floorCount + "\n" +
                            "Zoom Level: " + mPwMapView.getCurrentZoomLevel();
            mDebugText.setText(text);
            setTitle((mPwBuilding == null ? "" : mPwBuilding.getName()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.floor_switch, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final int next = mPwMapView.getNextFloorIndex();
        menu.findItem(R.id.menu_plus).setEnabled(next != -1);
        menu.findItem(R.id.menu_minus).setEnabled(mPwMapView.getPreviousFloorIndex() != -1);
        setDebugText();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.menu_plus) {
            mPwMapView.goUpOneFloor();
            ActivityCompat.invalidateOptionsMenu(this);
            return true;
        }
        if (id == R.id.menu_minus) {
            mPwMapView.goDownOneFloor();
            ActivityCompat.invalidateOptionsMenu(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PwCoreSession.getInstance().activityStartSession(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PwCoreSession.getInstance().activityStopSession(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPwMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mPwMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPwMapView.onDestroy(this);
    }

    /**
     * Helper method to hide the progress bar and show the map view.
     */
    private void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
        mPwMapView.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to show the progress bar and hide the map view.
     */
    private void showLoading() {
        mPwMapView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.refresh) {
            fetchMapData();
        } else if (id == R.id.reset) {
            mPwMapView.resetCurrentFloor();
        }
        setDebugText();
    }
}
