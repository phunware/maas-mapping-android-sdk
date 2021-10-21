package com.phunware.kotlin.sample.poi

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

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.phunware.kotlin.sample.R
import com.phunware.kotlin.sample.building.adapter.FloorAdapter
import com.phunware.mapping.OnPhunwareMapReadyCallback
import com.phunware.mapping.PhunwareMap
import com.phunware.mapping.SupportMapFragment
import com.phunware.mapping.manager.Callback
import com.phunware.mapping.manager.PhunwareMapManager
import com.phunware.mapping.model.Building
import com.phunware.mapping.model.FloorOptions
import com.phunware.mapping.model.PointOptions

class CustomPOIActivity : AppCompatActivity(), OnPhunwareMapReadyCallback {

    private lateinit var mapManager: PhunwareMapManager
    private lateinit var currentBuilding: Building
    private lateinit var spinnerAdapter: ArrayAdapter<FloorOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_poi)

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

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getPhunwareMapAsync(this)
    }

    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        // Retrieve buildingId from integers.xml
        val buildingId = resources.getInteger(R.integer.buildingId)

        phunwareMap.googleMap.uiSettings.isMapToolbarEnabled = false
        phunwareMap.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this@CustomPOIActivity, R.raw.map_style))

        // Setup long click listener to create Custom POIs
        setupMapListeners(phunwareMap)

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
                        val initialFloor = building.initialFloor
                        building.selectFloor(initialFloor.id)

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

    private fun setupMapListeners(map: PhunwareMap) {
        map.googleMap.setOnMapLongClickListener(GoogleMap.OnMapLongClickListener { latLng ->
            val currentFloor = currentBuilding.selectedFloor
            val currentFloorOptions: FloorOptions? = currentBuilding.floorOptions.lastOrNull { it.id == currentFloor.id }
            if (currentFloorOptions != null && !currentFloorOptions.bounds.contains(latLng)) {
                Toast.makeText(this@CustomPOIActivity, R.string.custom_poi_pin_drop_error,
                        Toast.LENGTH_LONG).show()
                return@OnMapLongClickListener
            }
            showCustomPOIDialog(currentFloorOptions, latLng)
        })
    }

    /**
     * Show a dialog asking for the name of a custom POI, if the EditText is not empty a new
     * [PointOptions] will be added to the given floor
     *
     * @param floor    The floor to which the new Point of Interest will be added
     * @param location The coordinates of the point that we're adding to the floor
     */
    private fun showCustomPOIDialog(floor: FloorOptions?, location: LatLng) {
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_create_poi, null)
        val poiNameInput = dialogView.findViewById<EditText>(R.id.poi_name_text_input)

        AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.custom_poi_dialog_title)
                .setMessage(R.string.custom_poi_dialog_message)
                .setView(dialogView)
                .setPositiveButton(R.string.button_ok) { dialog, _ ->
                    val poiName = poiNameInput.text.toString()
                    if (!TextUtils.isEmpty(poiName)) {
                        val customPoint = PointOptions()
                            .id(ITEM_ID_CUSTOM_POI.toLong())
                            .name(poiName)
                            .buildingId(currentBuilding.id)
                            .floorId(floor!!.id)
                            .location(location)

                        // Add a custom POI to this floor (flagged as custom with id)
                        floor.poiOptions.add(customPoint)
                        // Reload current floor
                        currentBuilding.selectFloor(floor.level)
                    }
                    dialog.dismiss()
                }
            .setNegativeButton(R.string.button_cancel) { dialog, _ -> dialog.dismiss() }.show()
    }

    companion object {
        private val TAG = CustomPOIActivity::class.java.simpleName
        private val ITEM_ID_CUSTOM_POI = -3 // This ID is required for custom POIs
    }
}
