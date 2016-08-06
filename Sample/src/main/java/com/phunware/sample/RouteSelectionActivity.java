package com.phunware.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.phunware.location.provider.PwLocationProvider;
import com.phunware.mapping.manager.PhunwareMapManager;
import com.phunware.mapping.manager.Router;
import com.phunware.mapping.model.BuildingOptions;
import com.phunware.mapping.model.FloorOptions;
import com.phunware.mapping.model.PointOptions;
import com.phunware.mapping.model.RouteOptions;

import java.util.ArrayList;
import java.util.List;

public class RouteSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_ROUTE = "route";
    private static final String EXTRA_POINTS = "points";
    private static final String EXTRA_FLOOR_ID = "floor_id";
    private static final String EXTRA_DEST_POINT = "dest_point";
    private static final String EXTRA_DEST_LOC = "dest_loc";
    private static final int ITEM_ID_PIN = -3;
    private static final int ITEM_ID_LOCATION = -2;

    private Spinner startPicker;
    private Spinner endPicker;
    private CheckBox accessible;
    private Button cancel;
    private Button next;
    private ImageButton reverse;

    private ArrayList<PointOptions> points;
    private LatLng customLocation;
    private LatLng currentLocation;
    private long floorId;
    private long currentFloorId;

    public static void startForResult(Activity activity, BuildingOptions building,
            int requestCode) {
        activity.startActivityForResult(getBaseIntent(activity, building), requestCode);
    }

    public static void startForResult(Activity activity, BuildingOptions building,
            PointOptions dest, long floorId, int requestCode) {
        Intent intent = getBaseIntent(activity, building);
        intent.putExtra(EXTRA_DEST_POINT, dest);
        intent.putExtra(EXTRA_FLOOR_ID, floorId);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startForResult(Activity activity, BuildingOptions building,
            LatLng dest, long floorId, int requestCode) {
        Intent intent = getBaseIntent(activity, building);
        intent.putExtra(EXTRA_DEST_LOC, dest);
        intent.putExtra(EXTRA_FLOOR_ID, floorId);
        activity.startActivityForResult(intent, requestCode);
    }

    private static Intent getBaseIntent(Activity activity, BuildingOptions building) {
        Intent intent = new Intent(activity, RouteSelectionActivity.class);

        ArrayList<PointOptions> list = new ArrayList<>();
        for (FloorOptions floor : building.getFloors()) {
            list.addAll(floor.getPoiOptions());
        }
        intent.putParcelableArrayListExtra(EXTRA_POINTS, new ArrayList<>(list));

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_route_select);
        PhunwareMapManager mapManager = App.get(this).getMapManager();

        startPicker = (Spinner) findViewById(R.id.start);
        endPicker = (Spinner) findViewById(R.id.end);
        accessible = (CheckBox) findViewById(R.id.accessible);
        cancel = (Button) findViewById(R.id.btn_cancel);
        next = (Button) findViewById(R.id.btn_confirm);
        reverse = (ImageButton) findViewById(R.id.reverse);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextClicked();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClicked();
            }
        });
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReverseClicked();
            }
        });

        Bundle extras = getIntent().getExtras();
        points = extras.getParcelableArrayList(EXTRA_POINTS);

        PointOptions destPoint = null;
        if (extras.containsKey(EXTRA_DEST_LOC)) {
            customLocation = extras.getParcelable(EXTRA_DEST_LOC);
            floorId = extras.getLong(EXTRA_FLOOR_ID);
        }
        if (extras.containsKey(EXTRA_DEST_POINT)) {
            destPoint = extras.getParcelable(EXTRA_DEST_POINT);
        }

        if (customLocation != null) {
            points.add(0, new PointOptions()
                    .id(ITEM_ID_PIN)
                    .level(floorId)
                    .location(customLocation)
                    .name(getString(R.string.custom_location_title)));
        }

        boolean hasCurrentLocation = false;
        if (mapManager.isMyLocationEnabled() && mapManager.getCurrentLocation() != null) {
            Location mylocation = mapManager.getCurrentLocation();
            currentLocation = new LatLng(mylocation.getLatitude(), mylocation.getLongitude());
            currentFloorId = mapManager.getCurrentBuilding().getSelectedFloor().getId();
            if (mylocation.getExtras() != null && mylocation.getExtras().containsKey(PwLocationProvider.LOCATION_EXTRAS_KEY_FLOOR_ID)) {
                currentFloorId = mylocation.getExtras().getLong(PwLocationProvider.LOCATION_EXTRAS_KEY_FLOOR_ID);
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

        if (customLocation != null) {
            endPicker.setSelection(hasCurrentLocation ? 2 : 1, false);
        } else if (destPoint != null) {
            for (int i = 0; i < points.size(); i++) {
                if (points.get(i).equals(destPoint)) {
                    endPicker.setSelection(i + 1, false);
                    break;
                }
            }
        }
        if (hasCurrentLocation) {
            startPicker.setSelection(1, false);
        }
    }

    private void onNextClicked() {
        PhunwareMapManager mapManager = App.get(this).getMapManager();

        // the user might have set either the start or end to a custom marker (id == -1)
        // so we have to account for that.
        long startId = startPicker.getSelectedItemId();
        long endId = endPicker.getSelectedItemId();
        boolean isAccessible = accessible.isChecked();

        Router router;
        if (startId == ITEM_ID_PIN && endId == ITEM_ID_LOCATION) {
            router = mapManager.findRoutes(customLocation, currentLocation, floorId, isAccessible);
        } else if (startId == ITEM_ID_PIN) {
            router = mapManager.findRoutes(customLocation, endId, floorId, isAccessible);
        } else if (startId == ITEM_ID_LOCATION && endId == ITEM_ID_PIN) {
            router = mapManager.findRoutes(currentLocation, customLocation, floorId, isAccessible);
        } else if (startId == ITEM_ID_LOCATION) {
            router = mapManager.findRoutes(currentLocation, endId, currentFloorId, isAccessible);
        } else if (endId == ITEM_ID_PIN) {
            router = mapManager.findRoutes(startId, customLocation, floorId, isAccessible);
        } else if (endId == ITEM_ID_LOCATION) {
            router = mapManager.findRoutes(startId, currentLocation, currentFloorId, isAccessible);
        } else {
            router = mapManager.findRoutes(startId, endId, isAccessible);
        }

        Intent data = new Intent();
        RouteOptions route = null;
        if (router != null) {
            route = router.shortestRoute();
        }
        data.putExtra(EXTRA_ROUTE, route);
        setResult(RESULT_OK, data);
        finish();
    }

    private void onCancelClicked() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void onReverseClicked() {
        int startPos = startPicker.getSelectedItemPosition();
        int endPos = endPicker.getSelectedItemPosition();

        startPicker.setSelection(endPos);
        endPicker.setSelection(startPos);
    }

    private class BuildingAdapter extends ArrayAdapter<PointOptions> {
        private final String prompt;

        public BuildingAdapter(Context context, List<PointOptions> pointList, String prompt) {
            super(context, 0, new ArrayList<PointOptions>());
            addAll(pointList);
            this.prompt = prompt;
        }

        @Override
        public int getCount() {
            return super.getCount() + 1;
        }

        @Override
        public PointOptions getItem(int position) {
            return position == 0 ? null : super.getItem(position - 1);
        }

        @Override
        public long getItemId(int position) {
            return position == 0 ? -1 : getItem(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;
            if (v == null) {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_spinner_item, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) v.findViewById(android.R.id.text1);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            if (position == 0) {
                holder.text.setText("");
                holder.text.setHint(prompt);
            } else {
                holder.text.setText(getItem(position).getName());
            }

            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;
            if (v == null) {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) v.findViewById(android.R.id.text1);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            if (position == 0) {
                holder.text.setText("");
                holder.text.setHint(prompt);
            } else {
                holder.text.setText(getItem(position).getName());
            }

            return v;
        }
    }

    private static final class ViewHolder {
        TextView text;
    }
}
