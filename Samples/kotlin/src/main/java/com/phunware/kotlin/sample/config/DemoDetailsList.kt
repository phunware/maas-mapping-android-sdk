package com.phunware.kotlin.sample.config

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

import android.content.Context
import com.phunware.kotlin.sample.R
import com.phunware.kotlin.sample.building.LoadBuildingActivity
import com.phunware.kotlin.sample.building.LoadBuildingWithoutMapActivity
import com.phunware.kotlin.sample.location.BluedotLocationActivity
import com.phunware.kotlin.sample.location.LocationModeManagedCompassActivity
import com.phunware.kotlin.sample.location.LocationModesActivity
import com.phunware.kotlin.sample.location.LocationSharingActivity
import com.phunware.kotlin.sample.poi.CustomPOIActivity
import com.phunware.kotlin.sample.poi.SearchPoiActivity
import com.phunware.kotlin.sample.routing.OffRouteActivity
import com.phunware.kotlin.sample.routing.OneWayRoutingActivity
import com.phunware.kotlin.sample.routing.RoutingActivity
import com.phunware.kotlin.sample.routing.VoicePromptActivity
import com.phunware.kotlin.sample.routing.WalkTimeActivity
import java.util.ArrayList

internal class DemoDetailsList(context: Context) {
    private val demos = ArrayList<Demo>()

    init {
        val resources = context.resources

        demos.add(Demo(resources.getString(R.string.demo_load_building_title),
                resources.getString(R.string.demo_load_building_description),
                LoadBuildingActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_load_building_no_map_title),
                resources.getString(R.string.demo_load_building_no_map_description),
                LoadBuildingWithoutMapActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_bluedot_title),
                resources.getString(R.string.demo_bluedot_description),
                BluedotLocationActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_location_modes_title),
                resources.getString(R.string.demo_location_modes_description),
                LocationModesActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_location_mode_managed_compass),
                resources.getString(R.string.demo_location_mode_managed_description),
                LocationModeManagedCompassActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_custom_poi_title),
                resources.getString(R.string.demo_custom_poi_description),
                CustomPOIActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_search_poi_title),
                resources.getString(R.string.demo_search_poi_description),
                SearchPoiActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_routing_title),
                resources.getString(R.string.demo_routing_description),
                RoutingActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_one_way_routing_title),
                resources.getString(R.string.demo_one_way_routing_description),
                OneWayRoutingActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_location_sharing_title),
                resources.getString(R.string.demo_location_sharing_description),
                LocationSharingActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_voice_prompt_title),
                resources.getString(R.string.demo_voice_prompt_description),
                VoicePromptActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_walk_time_title),
                resources.getString(R.string.demo_walk_time_description),
                WalkTimeActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_off_route_title),
                resources.getString(R.string.demo_off_route_description),
                OffRouteActivity::class.java))
    }

    fun getDemos(): List<Demo> = demos
}
