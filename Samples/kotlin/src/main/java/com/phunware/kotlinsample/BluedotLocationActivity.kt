package com.phunware.kotlinsample

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Spinner

import com.google.android.gms.maps.CameraUpdateFactory
import com.phunware.core.PwCoreSession
import com.phunware.kotlinsample.PermissionUtils.canAccessLocation
import com.phunware.kotlinsample.PermissionUtils.checkPermissions
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

class BluedotLocationActivity : AppCompatActivity(), OnPhunwareMapReadyCallback {

    private var mapManager: PhunwareMapManager? = null
    private var mapFragment: MapFragment? = null
    private var currentBuilding: Building? = null
    private var floorSpinnerAdapter: ArrayAdapter<FloorOptions>? = null
    private var content: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bluedot_location)
        content = findViewById(R.id.content)

        val floorSpinner = findViewById<Spinner>(R.id.floorSpinner)
        floorSpinnerAdapter = FloorAdapter(this)
        floorSpinner.adapter = floorSpinnerAdapter
        floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val floor = floorSpinnerAdapter!!.getItem(id.toInt())
                if (currentBuilding != null && floor != null) {
                    currentBuilding!!.selectFloor(floor.level)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Register the Phunware API keys
        PwCoreSession.getInstance().environment = PwCoreSession.Environment.DEV // FIXME: REMOVE
        PwCoreSession.getInstance().registerKeys(this)

        // Create the map manager and fragment used to load the building
        mapManager = PhunwareMapManager.create(this)
        mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment

        checkPermissions(this)
    }

    override fun onResume() {
        super.onResume()

        // Load the building if location permission has been granted
        if (canAccessLocation(this)) {
            if (mapFragment != null) {
                mapFragment!!.getPhunwareMapAsync(this)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION_FINE) {
            if (!canAccessLocation(this) && content != null) {
                Snackbar.make(content!!, R.string.permission_snackbar_message,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_settings, View.OnClickListener {
                            startActivityForResult(
                                    Intent(android.provider.Settings.ACTION_SETTINGS),
                                    REQUEST_PERMISSION_LOCATION_FINE)
                        }).show()
            }
        }
    }

    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        // Retrieve buildingId from integers.xml
        val buildingId = resources.getInteger(R.integer.buildingId)

        mapManager!!.setPhunwareMap(phunwareMap)
        mapManager!!.addBuilding(buildingId.toLong(),
                object : Callback<Building> {
                    override fun onSuccess(building: Building) {
                        Log.d(TAG, "Building loaded successfully")
                        currentBuilding = building

                        // Populate floor spinner
                        floorSpinnerAdapter!!.clear()
                        floorSpinnerAdapter!!.addAll(building.buildingOptions.floors)

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
        val builder = ManagedProviderFactory.ManagedProviderFactoryBuilder()
        builder.application(application)
                .context(WeakReference(application))
                .buildingId(building.id.toString())
        val factory = builder.build()
        val managedProvider = factory.createLocationProvider() as PwManagedLocationProvider
        mapManager!!.setLocationProvider(managedProvider, building)
        mapManager!!.isMyLocationEnabled = true
    }

    companion object {
        private val TAG = LocationModesActivity::class.java!!.getSimpleName()
        val REQUEST_PERMISSION_LOCATION_FINE = 1
    }
}
