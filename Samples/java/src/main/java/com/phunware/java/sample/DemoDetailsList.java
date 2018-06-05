package com.phunware.java.sample;

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

        demos.add(new Demo(resources.getString(R.string.demo_location_sharing_title),
                resources.getString(R.string.demo_location_sharing_description),
                LocationSharingActivity.class));
    }

    List<Demo> getDemos() {
        return demos;
    }
}
