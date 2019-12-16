package com.phunware.kotlin.sample.sensors.location

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
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.phunware.core.PwCoreSession
import com.phunware.core.PwLog
import com.phunware.kotlin.sample.sensors.CustomMapFragment.Companion.newInstance
import com.phunware.kotlin.sample.sensors.R
import com.phunware.kotlin.sample.sensors.building.adapter.FloorAdapter
import com.phunware.location.provider_managed.ManagedProviderFactory
import com.phunware.location.provider_managed.PwManagedLocationProvider
import com.phunware.mapping.MapFragment
import com.phunware.mapping.OnPhunwareMapReadyCallback
import com.phunware.mapping.PhunwareMap
import com.phunware.mapping.manager.Callback
import com.phunware.mapping.manager.PhunwareMapManager
import com.phunware.mapping.model.Building
import com.phunware.mapping.model.FloorOptions
import java.lang.ref.WeakReference

class BluedotLocationActivity : AppCompatActivity(), OnPhunwareMapReadyCallback,
        Building.OnFloorChangedListener {

    private lateinit var mapManager: PhunwareMapManager
    private lateinit var mapFragment: MapFragment
    private lateinit var currentBuilding: Building
    private lateinit var floorSpinner: Spinner
    private lateinit var floorSpinnerAdapter: ArrayAdapter<FloorOptions>
    private lateinit var content: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluedot_location)
        content = findViewById(R.id.content)

        floorSpinner = findViewById(R.id.floorSpinner)
        floorSpinnerAdapter = FloorAdapter(this)
        floorSpinner.adapter = floorSpinnerAdapter
        floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val floor = floorSpinnerAdapter.getItem(id.toInt())
                if (floor != null) {
                    currentBuilding.selectFloor(floor.level)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Register the Phunware API keys
        PwCoreSession.getInstance().environment = PwCoreSession.Environment.STAGE
        PwCoreSession.getInstance().registerKeys(this)

        // Create the map manager and fragment used to load the building
        mapManager = PhunwareMapManager.create(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.map, newInstance(this, this))
                .commit()
        }
    }

    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        // Retrieve buildingId from integers.xml
        val buildingId = resources.getInteger(R.integer.buildingId)

        phunwareMap.googleMap.uiSettings.isMapToolbarEnabled = false
        phunwareMap.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this@BluedotLocationActivity, R.raw.map_style))

        mapManager.setPhunwareMap(phunwareMap)
        mapManager.addBuilding(buildingId.toLong(),
                object : Callback<Building> {
                    override fun onSuccess(building: Building) {
                        Log.d(TAG, "Building loaded successfully")
                        currentBuilding = building

                        // Populate floor spinner
                        floorSpinnerAdapter.clear()
                        floorSpinnerAdapter.addAll(building.buildingOptions.floors)

                        // Add a listener to monitor floor switches
                        mapManager.addFloorChangedListener(this@BluedotLocationActivity)

                        // Initialize a location provider
                        setManagedLocationProvider(building)

                        // Set building to initial floor value
                        val initialFloor = building.initialFloor()
                        building.selectFloor(initialFloor.level)

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

    private fun setManagedLocationProvider(building: Building) {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Check for required Sensors
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null &&
            sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) != null &&
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null &&
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null ) {

            // All required sensors for location services are present
            val builder = ManagedProviderFactory.ManagedProviderFactoryBuilder()
            builder.application(application)
                .context(WeakReference(application))
                .buildingId(building.id.toString())
            val factory = builder.build()
            val managedProvider = factory.createLocationProvider() as PwManagedLocationProvider
            mapManager.setLocationProvider(managedProvider, building)
            mapManager.isMyLocationEnabled = true
        } else {
            Toast.makeText(this, "This device does not have  all sensors " +
                    "(Gyroscope / Compass / Accelorometer) " +
                    "required for using Location services. ", Toast.LENGTH_LONG).show()
        }
    }

    override fun onFloorChanged(building: Building?, floorId: Long) {
        for (index in 0 until floorSpinnerAdapter.count) {
            val floor = floorSpinnerAdapter.getItem(index)
            if (floor != null && floor.id == floorId) {
                if (floorSpinner.selectedItemPosition != index) {
                    runOnUiThread { floorSpinner.setSelection(index) }
                    break
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapManager?.let {
            it.onDestroy()
        }
    }

    companion object {
        private val TAG = BluedotLocationActivity::class.java.simpleName
    }
}
