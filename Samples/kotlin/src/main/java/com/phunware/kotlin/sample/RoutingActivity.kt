package com.phunware.kotlin.sample

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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.phunware.core.PwCoreSession
import com.phunware.core.PwLog
import com.phunware.location.provider_managed.ManagedProviderFactory
import com.phunware.location.provider_managed.PwManagedLocationProvider
import com.phunware.location_core.PwLocationProvider
import com.phunware.mapping.MapFragment
import com.phunware.mapping.OnPhunwareMapReadyCallback
import com.phunware.mapping.PhunwareMap
import com.phunware.mapping.manager.Callback
import com.phunware.mapping.manager.Navigator
import com.phunware.mapping.manager.PhunwareMapManager
import com.phunware.mapping.manager.Router
import com.phunware.mapping.model.Building
import com.phunware.mapping.model.FloorOptions
import com.phunware.mapping.model.PointOptions
import com.phunware.mapping.model.RouteOptions
import java.lang.ref.WeakReference
import java.util.*

class RoutingActivity : AppCompatActivity(), OnPhunwareMapReadyCallback,
        Navigator.OnManeuverChangedListener, Building.OnFloorChangedListener {

    private lateinit var mapManager: PhunwareMapManager
    private lateinit var currentBuilding: Building
    private lateinit var content: RelativeLayout
    private lateinit var floorSpinner: Spinner
    private lateinit var floorSpinnerAdapter: ArrayAdapter<FloorOptions>

    // Navigation Views
    private lateinit var fab: FloatingActionButton
    private var navigator: Navigator? = null
    private lateinit var navOverlayContainer: View
    private lateinit var navOverlay: NavigationOverlayView
    private lateinit var startPicker: Spinner
    private lateinit var endPicker: Spinner
    private lateinit var accessible: CheckBox
    private lateinit var currentLocationPoint : PointOptions

    private var selectRouteListener: View.OnClickListener = View.OnClickListener { showRoutingDialog() }
    private var exitNavListener: View.OnClickListener = View.OnClickListener { stopNavigating() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.routing)
        content = findViewById(R.id.content)

        // Initialize views for routing
        fab = findViewById(R.id.fab)
        fab.hide()
        fab.setOnClickListener(selectRouteListener)
        navOverlayContainer = findViewById(R.id.nav_overlay_container)
        navOverlay = findViewById(R.id.nav_overlay)

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
        PwCoreSession.getInstance().registerKeys(this)

        // Create the map manager and fragment used to load the building
        mapManager = PhunwareMapManager.create(this)

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                .replace(R.id.map, CustomMapFragment.newInstance(this, this))
                .commit()
        }
    }

    override fun onBackPressed() {
        if (navOverlayContainer.visibility == View.VISIBLE) {
            stopNavigating()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        // Retrieve buildingId from integers.xml
        val buildingId = resources.getInteger(R.integer.buildingId)

        phunwareMap.googleMap.uiSettings.isMapToolbarEnabled = false
        phunwareMap.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this@RoutingActivity, R.raw.map_style))

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
                        mapManager.addFloorChangedListener(this@RoutingActivity)

                        // Initialize a location provider
                        setManagedLocationProvider(building)

                        // Set building to initial floor value
                        val initialFloor = building.initialFloor()
                        building.selectFloor(initialFloor.level)

                        // Animate the camera to the building at an appropriate zoom level
                        val cameraUpdate = CameraUpdateFactory
                                .newLatLngBounds(initialFloor.bounds, 4)
                        phunwareMap.googleMap.animateCamera(cameraUpdate)

                        // Enabled fab for routing
                        showFab(true)
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.d(TAG, "Error when loading building -- " + throwable.message)
                        showFab(false)
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
        mapManager.setLocationProvider(managedProvider, building)
        mapManager.isMyLocationEnabled = true
    }

    private fun showFab(show: Boolean) {
        runOnUiThread {
            val start = (if (show) 0 else 1).toFloat()
            val end = (if (show) 1 else 0).toFloat()
            val anims = ArrayList<Animator>(2)
            anims.add(ObjectAnimator.ofFloat(fab, View.SCALE_X, start, end))
            anims.add(ObjectAnimator.ofFloat(fab, View.SCALE_Y, start, end))
            val s = AnimatorSet()
            s.playTogether(anims)
            s.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    if (show) fab.show()
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!show) fab.hide()
                }
            })
            s.start()
        }
    }

    private fun showRoutingDialog() {
        val builder = AlertDialog.Builder(this)

        // Load custom dialog view
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_route_selection, null)
        initDialogUI(dialogView)

        builder.setView(dialogView)
                .setTitle("Select a Route")
                .setMessage("Choose two points to route between")
                .setCancelable(false)
                .setPositiveButton("Route", { _, _ -> getRoutes() })
                .setNegativeButton("Cancel", { _, _ ->
                    // Do Nothing - Close Dialog
                })

        val d = builder.create()
        d.show()
    }

    private fun initDialogUI(dialogView: View) {
        startPicker = dialogView.findViewById(R.id.start)
        endPicker = dialogView.findViewById(R.id.end)
        accessible = dialogView.findViewById(R.id.accessible)
        val reverse = dialogView.findViewById<ImageButton>(R.id.reverse)

        reverse.setOnClickListener { onReverseClicked() }

        val points = ArrayList<PointOptions>()

        currentBuilding.floorOptions
                .filter { it != null && it.poiOptions != null }
                .forEach { points.addAll(it.poiOptions) }


        var hasCurrentLocation = false
        if (mapManager.isMyLocationEnabled && mapManager.currentLocation != null) {
            val myLocation = mapManager.currentLocation
            val currentLocation = LatLng(myLocation.latitude, myLocation.longitude)
            var currentFloorId = mapManager.currentBuilding.selectedFloor.id
            if (myLocation.extras != null && myLocation.extras
                            .containsKey(PwLocationProvider.LOCATION_EXTRAS_KEY_FLOOR_ID)) {
                currentFloorId = myLocation.extras
                        .getLong(PwLocationProvider.LOCATION_EXTRAS_KEY_FLOOR_ID)
            }

            currentLocationPoint = PointOptions()
                    .id(ITEM_ID_LOCATION)
                    .location(currentLocation)
                    .floorId(currentFloorId)
                    .name(getString(R.string.current_location))
            points.add(0, currentLocationPoint)
            hasCurrentLocation = true
        }

        startPicker.adapter = BuildingAdapter(this, points, getString(R.string.start_prompt))
        endPicker.adapter = BuildingAdapter(this, points, getString(R.string.end_prompt))

        if (hasCurrentLocation) {
            startPicker.setSelection(1, false)
        }
    }

    private fun getRoutes() {
        val startId = startPicker.selectedItemId
        val endId = endPicker.selectedItemId
        val isAccessible = accessible.isChecked

        val router = if (startId == ITEM_ID_LOCATION) {
            mapManager.findRoutes(currentLocationPoint.location, endId,
                    currentLocationPoint.floorId, isAccessible)
        } else if (endId == ITEM_ID_LOCATION) {
            mapManager.findRoutes(startId, currentLocationPoint.location,
                    currentLocationPoint.floorId, isAccessible)
        } else {
            mapManager.findRoutes(startId, endId, isAccessible)
        }

        if (router != null) {
            val route = router.shortestRoute()
            if (route == null) {
                PwLog.e(TAG, "Couldn't find route.")
                Snackbar.make(content, R.string.no_route,
                        Snackbar.LENGTH_SHORT).show()
            } else {
                startNavigating(route)
            }
        }
    }

    private fun onReverseClicked() {
        val startPos = startPicker.selectedItemPosition
        val endPos = endPicker.selectedItemPosition

        startPicker.setSelection(endPos)
        endPicker.setSelection(startPos)
    }

    private fun startNavigating(route: RouteOptions) {
        if (navigator != null) {
            navigator!!.stop()
        }
        navigator = mapManager.navigate(route)
        navigator!!.addOnManeuverChangedListener(this)

        navOverlay.setNavigator(navigator!!)
        navOverlayContainer.visibility = View.VISIBLE
        fab.setImageResource(R.drawable.ic_clear_white)
        fab.setOnClickListener(exitNavListener)

        navigator!!.start()
    }

    private fun stopNavigating() {
        if (navigator != null) {
            navigator!!.stop()
            navigator = null
        }
        navOverlayContainer.visibility = View.GONE
        fab.setImageResource(R.drawable.ic_navigation)
        fab.setOnClickListener(selectRouteListener)
    }

    override fun onManeuverChanged(navigator: Navigator, position: Int) {
        // Update the selected floor when the maneuver floor changes
        val maneuver = navigator.maneuvers[position]
        val selectedPosition = floorSpinner.selectedItemPosition
        for (i in 0 until floorSpinnerAdapter.count) {
            val floor = floorSpinnerAdapter.getItem(i)
            if (selectedPosition != i && floor != null && floor.id == maneuver.floorId) {
                floorSpinner.setSelection(i)
            }
        }
    }

    override fun onRouteSnapFailed() {
        // Do Nothing
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
        private val TAG = LocationModesActivity::class.java.simpleName
        private val ITEM_ID_LOCATION = -2L
    }
}
