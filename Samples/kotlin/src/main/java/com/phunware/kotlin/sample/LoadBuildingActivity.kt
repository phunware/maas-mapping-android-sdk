package com.phunware.kotlin.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

import com.google.android.gms.maps.CameraUpdateFactory
import com.phunware.core.PwCoreSession

import com.phunware.mapping.MapFragment
import com.phunware.mapping.OnPhunwareMapReadyCallback
import com.phunware.mapping.PhunwareMap
import com.phunware.mapping.manager.Callback
import com.phunware.mapping.manager.PhunwareMapManager
import com.phunware.mapping.model.Building
import com.phunware.mapping.model.FloorOptions

class LoadBuildingActivity : AppCompatActivity(), OnPhunwareMapReadyCallback {
    private lateinit var mapManager: PhunwareMapManager
    private lateinit var currentBuilding: Building
    private lateinit var spinnerAdapter: ArrayAdapter<FloorOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.load_building)

        val floorSpinner = findViewById<Spinner>(R.id.floorSpinner)
        spinnerAdapter = FloorAdapter(this)
        floorSpinner.adapter = spinnerAdapter
        floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val floor = spinnerAdapter.getItem(id.toInt())
                if (floor != null) {
                    currentBuilding.selectFloor(floor.level)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Create the map manager used to load the building
        mapManager = PhunwareMapManager.create(this)

        // Register the Phunware API keys
        PwCoreSession.getInstance().environment = PwCoreSession.Environment.DEV // FIXME: REMOVE
        PwCoreSession.getInstance().registerKeys(this)

        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getPhunwareMapAsync(this)
    }

    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        // Retrieve buildingId from integers.xml
        val buildingId = resources.getInteger(R.integer.buildingId)

        mapManager.setPhunwareMap(phunwareMap)
        mapManager.addBuilding(buildingId.toLong(),
                object : Callback<Building> {
                    override fun onSuccess(building: Building) {
                        Log.d(TAG, "Building loaded successfully")
                        currentBuilding = building

                        // Populate floor spinner
                        spinnerAdapter.clear()
                        spinnerAdapter.addAll(building.buildingOptions.floors)

                        // Set building to initial floor value
                        val initialFloor = building.initialFloor()
                        building.selectFloor(building.initialFloor().level)

                        // Animate the camera to the building at an appropriate zoom level
                        val cameraUpdate = CameraUpdateFactory
                                .newLatLngBounds(initialFloor.bounds, 4)
                        phunwareMap.googleMap.animateCamera(cameraUpdate)
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.d(TAG, "Error when loading building -- " + throwable.message)
                    }
                })
    }

    companion object {
        private val TAG = LoadBuildingActivity::class.java.simpleName
    }
}
