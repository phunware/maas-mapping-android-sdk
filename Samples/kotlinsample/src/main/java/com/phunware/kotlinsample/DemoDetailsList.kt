package com.phunware.kotlinsample

import android.content.Context

import java.util.ArrayList

internal class DemoDetailsList(context: Context) {
    private val demos = ArrayList<Demo>()

    init {
        val resources = context.resources

        demos.add(Demo(resources.getString(R.string.demo_load_building_title),
                resources.getString(R.string.demo_load_building_description),
                LoadBuildingActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_location_modes_title),
                resources.getString(R.string.demo_location_modes_description),
                LocationModesActivity::class.java))

        demos.add(Demo(resources.getString(R.string.demo_custom_poi_title),
                resources.getString(R.string.demo_custom_poi_description),
                CustomPOIActivity::class.java))
    }

    fun getDemos(): List<Demo> = demos
}
