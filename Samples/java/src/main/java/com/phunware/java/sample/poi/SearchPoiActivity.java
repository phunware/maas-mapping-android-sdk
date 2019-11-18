package com.phunware.java.sample.poi;

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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.phunware.core.PwCoreSession;
import com.phunware.java.sample.R;
import com.phunware.java.sample.adapter.FloorAdapter;
import com.phunware.mapping.MapFragment;
import com.phunware.mapping.OnPhunwareMapReadyCallback;
import com.phunware.mapping.PhunwareMap;
import com.phunware.mapping.SupportMapFragment;
import com.phunware.mapping.manager.Callback;
import com.phunware.mapping.manager.PhunwareMapManager;
import com.phunware.mapping.model.Building;
import com.phunware.mapping.model.FloorOptions;
import com.phunware.mapping.model.PointOptions;

import java.util.ArrayList;
import java.util.List;

public class SearchPoiActivity extends AppCompatActivity implements OnPhunwareMapReadyCallback {
    private static final String TAG = SearchPoiActivity.class.getSimpleName();
    private PhunwareMapManager mapManager;
    private PhunwareMap phunwareMap;
    private Spinner floorSpinner;
    private Building currentBuilding;
    private ArrayAdapter<FloorOptions> spinnerAdapter;
    private FloatingActionButton fab;
    private Dialog searchPoiDialog = null;
    private PoiListAdapter poiListAdapter = null;

    View.OnClickListener searchPoiListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showSearchDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_poi);

        fab = findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(searchPoiListener);

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

        // Create the map manager used to load the building
        mapManager = PhunwareMapManager.create(this);

        // Register the Phunware API keys
        PwCoreSession.getInstance().registerKeys(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getPhunwareMapAsync(this);
        }
    }

    @Override
    public void onPhunwareMapReady(final PhunwareMap phunwareMap) {
        // Retrieve buildingId from maas_integers.xml
        int buildingId = getResources().getInteger(R.integer.buildingId);

        phunwareMap.getGoogleMap().getUiSettings().setMapToolbarEnabled(false);
        phunwareMap.getGoogleMap().setMapStyle(MapStyleOptions.loadRawResourceStyle(
                SearchPoiActivity.this, R.raw.map_style));

        this.phunwareMap = phunwareMap;
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

                        showFab(true);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.d(TAG, "Error when loading building -- " + throwable.getMessage());
                        showFab(false);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapManager != null) {
            mapManager.onDestroy();
        }
    }

    private void showFab(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Load custom dialog view
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_search_poi, null);
        initDialogUI(dialogView);

        builder.setView(dialogView)
                .setTitle("Search POIs")
                .setMessage("Type to search for POIs and click to center the map to that POIs" +
                        " location")
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do Nothing -- Close Dialog
                    }
                })
                .setCancelable(true);
        searchPoiDialog = builder.create();
        searchPoiDialog.show();
    }

    private void initDialogUI(View dialogView) {
        EditText searchPoi = dialogView.findViewById(R.id.search_poi);
        RecyclerView searchPoiRecyclerView = dialogView.findViewById(R.id.poi_recycler_view);
        RecyclerView.LayoutManager recyclerViewLayoutManager = new LinearLayoutManager(this);
        searchPoiRecyclerView.setLayoutManager(recyclerViewLayoutManager);

        ArrayList<PointOptions> points = new ArrayList<>();
        for (FloorOptions floor : currentBuilding.getFloorOptions()) {
            if (floor != null) {
                if (floor.getPoiOptions() != null) {
                    points.addAll(floor.getPoiOptions());
                }
            }
        }

        poiListAdapter = new PoiListAdapter(points);
        searchPoiRecyclerView.setAdapter(poiListAdapter);

        searchPoi.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void refreshList(String newText) {
        poiListAdapter.setFilter(newText);
        poiListAdapter.notifyDataSetChanged();
    }

    private class PoiListAdapter extends RecyclerView.Adapter<PoiListAdapter.ViewHolder> {

        private ArrayList<PointOptions> arrAllPointOptions = new ArrayList<>();
        private ArrayList<PointOptions> arrPointOptions = new ArrayList<>();

        PoiListAdapter(List<PointOptions> pois) {
            arrAllPointOptions.addAll(pois);
            arrPointOptions.addAll(pois);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final PointOptions poi = arrPointOptions.get(position);
            if (poi.getName() != null) {
                holder.poiNameTextView.setText(poi.getName());
            }
        }

        @Override
        public int getItemCount() {
            return arrPointOptions.size();
        }

        void setFilter(String filter) {
            if (filter != null) {
                arrPointOptions.clear();
                for (PointOptions poi : arrAllPointOptions) {
                    if (poi.getName().toUpperCase().startsWith(filter.toUpperCase())) {
                        arrPointOptions.add(poi);
                    }
                }
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView poiNameTextView = null;

            ViewHolder(View itemView) {
                super(itemView);
                poiNameTextView = itemView.findViewById(android.R.id.text1);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                PointOptions selectedPoi = arrPointOptions.get(position);
                LatLng poiLocation = selectedPoi.getLocation();

                // Center the camera to the poi that was selected
                LatLngBounds bounds = new LatLngBounds(poiLocation, poiLocation);
                CameraUpdate cameraUpdate = CameraUpdateFactory
                        .newLatLngBounds(bounds, 4);
                phunwareMap.getGoogleMap().animateCamera(cameraUpdate);

                // Switch floors if necessary
                if (selectedPoi.getLevel() != currentBuilding.getSelectedFloor().getLevel()) {
                    currentBuilding.selectFloor(selectedPoi.getLevel());

                    // Update floor spinner
                    int selectedPosition = floorSpinner.getSelectedItemPosition();
                    for (int i = 0; i < spinnerAdapter.getCount(); i++) {
                        FloorOptions floor = spinnerAdapter.getItem(i);
                        if (selectedPosition != i && floor != null && floor.getId()
                                == selectedPoi.getFloorId()) {
                            floorSpinner.setSelection(i);
                        }
                    }
                }

                if (searchPoiDialog != null) {
                    searchPoiDialog.dismiss();
                }
            }
        }
    }
}
