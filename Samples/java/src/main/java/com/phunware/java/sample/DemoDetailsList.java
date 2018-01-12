package com.phunware.java.sample;

import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

class DemoDetailsList {
    private List<Demo> demos = new ArrayList<>();

    DemoDetailsList(Context context) {
        Resources resources = context.getResources();

        demos.add(new Demo(resources.getString(R.string.demo_load_building_title),
                resources.getString(R.string.demo_load_building_description),
                LoadBuildingActivity.class));

        demos.add(new Demo(resources.getString(R.string.demo_bluedot_title),
                resources.getString(R.string.demo_bluedot_description),
                BluedotLocationActivity.class));

        demos.add(new Demo(resources.getString(R.string.demo_location_modes_title),
                resources.getString(R.string.demo_location_modes_description),
                LocationModesActivity.class));

        demos.add(new Demo(resources.getString(R.string.demo_custom_poi_title),
                resources.getString(R.string.demo_custom_poi_description),
                CustomPOIActivity.class));

        demos.add(new Demo(resources.getString(R.string.demo_search_poi_title),
                resources.getString(R.string.demo_search_poi_description),
                SearchPoiActivity.class));

        demos.add(new Demo(resources.getString(R.string.demo_routing_title),
                resources.getString(R.string.demo_routing_description),
                RoutingActivity.class));
    }

    List<Demo> getDemos() {
        return demos;
    }
}
