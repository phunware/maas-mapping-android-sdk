package com.phunware.kotlin.sample.routing

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
import android.bluetooth.BluetoothAdapter
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.phunware.core.PwCoreSession
import com.phunware.core.PwLog
import com.phunware.kotlin.sample.R
import com.phunware.kotlin.sample.building.adapter.FloorAdapter
import com.phunware.kotlin.sample.routing.fragment.RouteSummaryFragment
import com.phunware.kotlin.sample.routing.fragment.RoutingDialogFragment
import com.phunware.kotlin.sample.routing.fragment.RoutingDialogFragment.Companion.CURRENT_LOCATION_ITEM_ID
import com.phunware.kotlin.sample.routing.view.NavigationOverlayView
import com.phunware.location.provider_managed.ManagedProviderFactory
import com.phunware.location.provider_managed.PwManagedLocationProvider
import com.phunware.mapping.OnPhunwareMapReadyCallback
import com.phunware.mapping.PhunwareMap
import com.phunware.mapping.SupportMapFragment
import com.phunware.mapping.bluedot.LocationManager
import com.phunware.mapping.manager.Callback
import com.phunware.mapping.manager.Navigator
import com.phunware.mapping.manager.PhunwareMapManager
import com.phunware.mapping.manager.Router
import com.phunware.mapping.model.Building
import com.phunware.mapping.model.FloorOptions
import com.phunware.mapping.model.RouteOptions
import java.lang.ref.WeakReference
import java.util.ArrayList

open class RoutingActivity : AppCompatActivity(), OnPhunwareMapReadyCallback,
        Building.OnFloorChangedListener, Navigator.OnManeuverChangedListener,
        LocationManager.LocationListener, RoutingDialogFragment.RoutingDialogListener {
    companion object {
        private val TAG = RoutingActivity::class.java.simpleName
    }

    lateinit var mapManager: PhunwareMapManager
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var currentBuilding: Building
    private lateinit var floorSpinner: Spinner
    private lateinit var floorSpinnerAdapter: ArrayAdapter<FloorOptions>
    private lateinit var content: ConstraintLayout
    private lateinit var floorSpinnerView: LinearLayout

    // Navigation Views
    private lateinit var fab: FloatingActionButton
    var navigator: Navigator? = null // public so that other samples that extend this activity can access
    lateinit var navOverlay: NavigationOverlayView // public so that other samples that extend this activity can access
    private lateinit var routeSummaryFragment: RouteSummaryFragment

    private val gpsPositionList: MutableList<Location> = ArrayList()
    private var routingFromCurrentLocation = false
    private var maneuverPosition = -1
    private val handler = Handler()
    private var dwellTimer: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routing)
        content = findViewById(R.id.content)
        floorSpinnerView = findViewById(R.id.floor_switcher_layout)


        // Initialize views for routing
        fab = findViewById(R.id.fab)
        fab.hide()
        fab.setOnClickListener {
            RoutingDialogFragment.newInstance(
                    locationEnabled = mapManager.isMyLocationEnabled,
                    currentLocation = mapManager.currentLocation
            ).show(supportFragmentManager, "frag_routing_dialog")
        }
        navOverlay = findViewById(R.id.nav_overlay)
        navOverlay.setOnClickListener {
            routeSummaryFragment.show()
        }

        floorSpinner = findViewById(R.id.floorSpinner)
        floorSpinnerAdapter = FloorAdapter(this)
        floorSpinner.adapter = floorSpinnerAdapter
        floorSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                    ) {
                        val floor = floorSpinnerAdapter.getItem(id.toInt())
                        if (floor != null) {
                            currentBuilding.selectFloor(floor.level)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

        routeSummaryFragment = supportFragmentManager.findFragmentById(R.id.routeSummaryFragment) as RouteSummaryFragment

        // Route Summary should be hidden initially.
        supportFragmentManager.beginTransaction().hide(routeSummaryFragment).commit()

        // Register the Phunware API keys
        PwCoreSession.getInstance().registerKeys(this)

        // Create the map manager and fragment used to load the building
        mapManager = PhunwareMapManager.create(this)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getPhunwareMapAsync(this)
    }

    override fun onAttachFragment(fragment: Fragment) {
        // Attach a callback for the [RoutingDialogFragment] so we don't have to manage
        // multiple [PhunwareMapManager]s
        if (fragment is RoutingDialogFragment) {
            fragment.setRoutingDialogListener(this)
        }
    }

    override fun onBackPressed() {
        if (!routeSummaryFragment.isHidden) {
            routeSummaryFragment.hide()
        } else if (navOverlay.visibility == View.VISIBLE) {
            stopNavigating()
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            routeSummaryFragment.hide()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * OnPhunwareMapReadyCallback
     */
    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        // Retrieve buildingId from integers.xml
        val buildingId = resources.getInteger(R.integer.buildingId)

        phunwareMap.googleMap.uiSettings.isMapToolbarEnabled = false
        phunwareMap.googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.map_style
                )
        )

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

    /**
     * Navigator.OnManeuverChangedListener
     */
    override fun onManeuverChanged(navigator: Navigator, position: Int) {
        //TODO: Remove log statements before merge
        Log.d("VoiceRepeatDebug", "OnManeuverChanged (RoutingActivity) called with position: $position")
        if (System.currentTimeMillis() - dwellTimer >= 2_000 && maneuverPosition != position) {
            maneuverPosition = position
            dwellTimer = System.currentTimeMillis()
            navOverlay.dispatchManeuverChanged(navigator, position)

            // Update the selected floor when the maneuver floor changes
            val maneuver = navigator.maneuvers[position]
            val selectedPosition = floorSpinner.selectedItemPosition
            for (i in 0 until floorSpinnerAdapter.count) {
                val floor = floorSpinnerAdapter.getItem(i)
                if (selectedPosition != i && floor != null && floor.id == maneuver.floorId) {
                    floorSpinner.setSelection(i)
                }
            }
            dispatchManeuverChanged(navigator, position)
        }
    }

    open fun dispatchManeuverChanged(navigator: Navigator, position: Int) {
        
    }

    override fun onRouteSnapFailed() {
        // Do Nothing
    }

    /**
     * Building.OnFloorChangedListener
     */
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

    /**
     * LocationListener
     */
    override fun onLocationUpdate(p0: Location?) {
        mapManager.myLocationMode = PhunwareMapManager.MODE_FOLLOW_ME
    }

    /**
     * Private Methods
     */

    private fun setManagedLocationProvider(building: Building) {
        if (BluetoothAdapter.getDefaultAdapter() != null) {
            val builder = ManagedProviderFactory.ManagedProviderFactoryBuilder()
            builder.application(application)
                    .context(WeakReference(this))
                    .buildingId(building.id.toString())
            val factory = builder.build()
            val managedProvider = factory.createLocationProvider() as PwManagedLocationProvider
            mapManager.setLocationProvider(managedProvider, building)
            mapManager.isMyLocationEnabled = true
        }
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

    open fun startNavigating(route: RouteOptions) {
        navigator?.stop()
        navigator = mapManager.navigate(route)
        mapManager.addLocationUpdateListener(this)
        navigator?.let {
            it.addOnManeuverChangedListener(this)
            navOverlay.setNavigator(it)
        }

        navOverlay.visibility = View.VISIBLE
        fab.hide()
        floorSpinnerView.visibility = View.GONE

        navigator?.start()

        routeSummaryFragment.setRoute(route)
    }

    open fun stopNavigating() {
        navigator?.stop()
        navigator = null

        mapManager.removeLocationUpdateListener(this)
        navOverlay.visibility = View.GONE
        floorSpinnerView.visibility = View.VISIBLE
        fab.show()
        routingFromCurrentLocation = false
    }

    /**
     * RoutingDialogListener
     */
    override fun onGetRoutes(startId: Long, endId: Long, isAccessible: Boolean) {
        val router: Router?

        if (startId.compareTo(CURRENT_LOCATION_ITEM_ID) == 0) {
            val currentLocation =
                    LatLng(mapManager.currentLocation.latitude, mapManager.currentLocation.longitude)
            router = mapManager.findRoutes(
                    currentLocation,
                    endId,
                    mapManager.currentBuilding.selectedFloor.id,
                    isAccessible
            )
            routingFromCurrentLocation = true
        } else {
            router = mapManager.findRoutes(startId, endId, isAccessible)
        }

        val route: RouteOptions? = router?.shortestRoute()

        if (route == null) {
            PwLog.e(TAG, "Couldn't find route.")
            Snackbar.make(
                    content, R.string.no_route,
                    Snackbar.LENGTH_SHORT
            ).show()
        } else {
            startNavigating(route)
        }
    }

    override fun onGetBuildingForRouting(): Building = mapManager.currentBuilding
}