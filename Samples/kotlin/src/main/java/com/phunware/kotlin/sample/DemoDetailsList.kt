package com.phunware.kotlin.sample

import android.content.Context
import java.util.*

internal class DemoDetailsList(context: Context) {
    private val demos = ArrayList<Demo>()

    init {
        val resources = context.resources

        demos.add(Demo(resources.getString(R.string.demo_load_building_title),
                resources.getString(R.string.demo_load_building_description),
                LoadBuildingActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_bluedot_title),
                resources.getString(R.string.demo_bluedot_description),
                BluedotLocationActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_location_modes_title),
                resources.getString(R.string.demo_location_modes_description),
                LocationModesActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_custom_poi_title),
                resources.getString(R.string.demo_custom_poi_description),
                CustomPOIActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_search_poi_title),
                resources.getString(R.string.demo_search_poi_description),
                SearchPoiActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_routing_title),
                resources.getString(R.string.demo_routing_description),
                RoutingActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_location_sharing_title),
                resources.getString(R.string.demo_location_sharing_description),
                LocationSharingActivity::class.java))
    }

    fun getDemos(): List<Demo> = demos
}
