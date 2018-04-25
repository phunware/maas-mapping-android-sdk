package com.phunware.kotlin.sample

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Spinner

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.phunware.core.PwCoreSession
import com.phunware.core.PwLog
import com.phunware.location.provider_managed.ManagedProviderFactory
import com.phunware.location.provider_managed.PwManagedLocationProvider
import com.phunware.mapping.MapFragment
import com.phunware.mapping.OnPhunwareMapReadyCallback
import com.phunware.mapping.PhunwareMap
import com.phunware.mapping.manager.Callback
import com.phunware.mapping.manager.PhunwareMapManager
import com.phunware.mapping.manager.PhunwareMapManager.MODE_FOLLOW_ME
import com.phunware.mapping.manager.PhunwareMapManager.MODE_LOCATE_ME
import com.phunware.mapping.model.Building
import com.phunware.mapping.model.FloorOptions

import java.lang.ref.WeakReference

class LocationModesActivity : AppCompatActivity(), OnPhunwareMapReadyCallback,
        Building.OnFloorChangedListener {

    private lateinit var mapManager: PhunwareMapManager
    private lateinit var mapFragment: MapFragment
    private lateinit var currentBuilding: Building
    private lateinit var floorSpinner: Spinner
    private lateinit var floorSpinnerAdapter: ArrayAdapter<FloorOptions>

    private lateinit var content: RelativeLayout

    // Location Mode Variables
    private lateinit var locationModeFab: FloatingActionButton
    private lateinit var trackingModeHandler: Handler
    private lateinit var trackingModeRunnable: Runnable
    private var isTrackingModeTimerRunning = false
    private lateinit var previousTrackingMode: String

    private var locationModeListener: View.OnClickListener = View.OnClickListener { onLocationModeFabClicked() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_modes)
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

        locationModeFab = findViewById(R.id.location_mode_fab)
        locationModeFab.setOnClickListener(locationModeListener)

        trackingModeHandler = Handler(mainLooper)
        trackingModeRunnable = Runnable {
            isTrackingModeTimerRunning = false
            if (mapManager.isBluedotVisibleOnMap && mapManager.isBluedotVisibleOnFloor) {
                PwLog.d(TAG, "Bluedot is visible -- resetting tracking mode")
                when (previousTrackingMode) {
                    PREF_LOCATION_LOCATE -> mapManager.myLocationMode = MODE_LOCATE_ME
                    PREF_LOCATION_FOLLOW -> mapManager.myLocationMode = MODE_FOLLOW_ME
                    else -> mapManager.myLocationMode = PhunwareMapManager.MODE_NORMAL
                }
                setSavedLocationMode(previousTrackingMode)
                updateLocationModeFab()
            } else {
                PwLog.d(TAG, "Bluedot is not visible -- breaking tracking mode")
            }
        }

        // Register the Phunware API keys
        PwCoreSession.getInstance().registerKeys(this)

        // Create the map manager and fragment used to load the building
        mapManager = PhunwareMapManager.create(this)
        mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.addOnTouchListener {
            if (mapManager.isBluedotVisibleOnFloor) {
                val trackingMode = mapManager.myLocationMode
                if (isTrackingModeTimerRunning || trackingMode == MODE_LOCATE_ME
                        || trackingMode == MODE_FOLLOW_ME) {
                    updateLocationModeBehavior()
                }
            }
        }
        mapFragment.getPhunwareMapAsync(this)

    }

    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        // Retrieve buildingId from integers.xml
        val buildingId = resources.getInteger(R.integer.buildingId)

        phunwareMap.googleMap.uiSettings.isMapToolbarEnabled = false
        phunwareMap.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this@LocationModesActivity, R.raw.map_style))

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
                        mapManager.addFloorChangedListener(this@LocationModesActivity)

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
                .context(WeakReference<Context>(application))
                .buildingId(building.id.toString())
        val factory = builder.build()
        val managedProvider = factory.createLocationProvider() as PwManagedLocationProvider
        mapManager.setLocationProvider(managedProvider, building)
        mapManager.isMyLocationEnabled = true
    }

    private fun onLocationModeFabClicked() {
        val mode = getSavedLocationMode()

        // Rotate to the next location mode
        when {
            mode.equals(PREF_LOCATION_FOLLOW, ignoreCase = true) -> {
                mapManager.myLocationMode = PhunwareMapManager.MODE_NORMAL
                setSavedLocationMode(PREF_LOCATION_NORMAL)
            }
            mode.equals(PREF_LOCATION_LOCATE, ignoreCase = true) -> {
                mapManager.myLocationMode = MODE_FOLLOW_ME
                setSavedLocationMode(PREF_LOCATION_FOLLOW)
            }
            else -> {
                mapManager.myLocationMode = MODE_LOCATE_ME
                setSavedLocationMode(PREF_LOCATION_LOCATE)
            }
        }

        updateLocationModeFab()
    }

    private fun updateLocationModeFab() {
        val mode = getSavedLocationMode()

        // Update fab to match current location mode
        when {
            mode.equals(PREF_LOCATION_FOLLOW, ignoreCase = true) -> {
                locationModeFab.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_compass))
                locationModeFab.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.colorAccent))
            }
            mode.equals(PREF_LOCATION_LOCATE, ignoreCase = true) -> {
                locationModeFab.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_my_location))
                locationModeFab.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.colorAccent))
            }
            else -> {
                locationModeFab.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_my_location))
                locationModeFab.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.inactive))
            }
        }
    }

    fun updateLocationModeBehavior() {
        // Save current tracking mode and break tracking mode
        if (!isTrackingModeTimerRunning) {
            PwLog.d(TAG, "Breaking tracking mode while timer is running")
            previousTrackingMode = getSavedLocationMode()
            mapManager.myLocationMode = PhunwareMapManager.MODE_NORMAL
            setSavedLocationMode(PREF_LOCATION_NORMAL)
            updateLocationModeFab()
        }

        // Cancel task if it is already running
        if (isTrackingModeTimerRunning) {
            PwLog.d(TAG, "Cancelling existing tracking mode timer")
            trackingModeHandler.removeCallbacks(trackingModeRunnable)
            isTrackingModeTimerRunning = false
        }

        PwLog.d(TAG, "Starting tracking mode timer")
        isTrackingModeTimerRunning = true
        val trackingModeSwitchInterval = 10000 // 10 seconds by default
        trackingModeHandler.postDelayed(trackingModeRunnable, trackingModeSwitchInterval.toLong())
    }

    private fun getSavedLocationMode(): String {
        val preferences = getSharedPreferences(PREFERENCE_NAME, 0)
        return preferences.getString(PREF_LOCATION_MODE, PREF_LOCATION_NORMAL)
    }

    private fun setSavedLocationMode(mode: String) {
        val preferences = getSharedPreferences(PREFERENCE_NAME, 0)
        preferences.edit()
                .putString(PREF_LOCATION_MODE, mode)
                .apply()
    }

    override fun onFloorChanged(buildingId: Building?, floorId: Long) {
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

    companion object {
        private val TAG = LocationModesActivity::class.java.simpleName
        private const val PREFERENCE_NAME = "location_mode_sample"
        private const val PREF_LOCATION_MODE = "location_mode"
        private const val PREF_LOCATION_FOLLOW = "follow me"
        private const val PREF_LOCATION_LOCATE = "locate me"
        private const val PREF_LOCATION_NORMAL = "normal"
    }
}
