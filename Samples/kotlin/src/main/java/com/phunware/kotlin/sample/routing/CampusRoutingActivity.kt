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
import android.bluetooth.BluetoothManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.phunware.kotlin.sample.R
import com.phunware.kotlin.sample.building.adapter.FloorAdapter
import com.phunware.kotlin.sample.routing.adapter.BuildingSpinnerAdapter
import com.phunware.kotlin.sample.routing.fragment.RouteSummaryFragment
import com.phunware.kotlin.sample.routing.fragment.RoutingDialogFragment
import com.phunware.kotlin.sample.routing.fragment.RoutingDialogFragment.Companion.CURRENT_LOCATION_ITEM_ID
import com.phunware.kotlin.sample.routing.view.NavigationOverlayView
import com.phunware.kotlin.sample.widget.CustomSpinner
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
import com.phunware.mapping.model.Campus
import com.phunware.mapping.model.FloorOptions
import com.phunware.mapping.model.RouteOptions
import com.phunware.kotlin.sample.App
import java.util.ArrayList

internal open class CampusRoutingActivity : AppCompatActivity(), OnPhunwareMapReadyCallback,
        Building.OnFloorChangedListener, Navigator.OnManeuverChangedListener,
        LocationManager.LocationListener, RoutingDialogFragment.RoutingDialogListener, FragmentOnAttachListener {
    companion object {
        private val TAG = CampusRoutingActivity::class.java.simpleName
    }

    lateinit var mapManager: PhunwareMapManager
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var currentBuilding: Building
    private lateinit var floorSpinner: CustomSpinner
    private lateinit var floorSpinnerAdapter: ArrayAdapter<FloorOptions>
    private lateinit var buildingSpinner: CustomSpinner
    private lateinit var buildingSpinnerAdapter: ArrayAdapter<Building>
    private lateinit var content: ConstraintLayout
    private lateinit var floorSpinnerView: LinearLayout

    // Navigation Views
    private lateinit var fab: FloatingActionButton
    open var navigator: Navigator? = null // public so that other samples that extend this activity can access
    open lateinit var navOverlay: NavigationOverlayView // public so that other samples that extend this activity can access
    private lateinit var routeSummaryFragment: RouteSummaryFragment

    private var routingFromCurrentLocation = false
    private var maneuverPosition = -1

    private var maneuverFromSwiping: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campus_routing)
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
        navOverlay.setOnManeuverSelectedListener(object : NavigationOverlayView.OnManeuverSelectedListener {
            override fun maneuverSelected(position: Int) {
                // maneuverFromSwiping: set flag to signal that the next onManeuverChanged callback
                // was from a pager swipe, and therefore the pager does not need to be updated.
                maneuverFromSwiping = true
                navigator?.setCurrentManeuver(position)
            }
        })

        floorSpinner = findViewById(R.id.floorSpinner)
        floorSpinnerAdapter = FloorAdapter(this)
        floorSpinner.adapter = floorSpinnerAdapter
        buildingSpinner = findViewById(R.id.buildingSpinner)
        buildingSpinnerAdapter = BuildingSpinnerAdapter(this)
        buildingSpinner.adapter = buildingSpinnerAdapter

        routeSummaryFragment = supportFragmentManager.findFragmentById(R.id.routeSummaryFragment) as RouteSummaryFragment

        // Route Summary should be hidden initially.
        supportFragmentManager.beginTransaction().hide(routeSummaryFragment).commit()

        // Create the map manager and fragment used to load the building
        mapManager = (application as App).mapManager
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getPhunwareMapAsync(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapManager.isMyLocationEnabled = false
        mapManager.removeLocationUpdateListener(this)
        mapManager.removeFloorChangedListener(this)
        mapManager.onDestroy()
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            routeSummaryFragment.hide()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * OnPhunwareMapReadyCallback
     */
    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        // Retrieve buildingId from integers.xml
        val campusId = resources.getInteger(R.integer.campusId)

        phunwareMap.googleMap.uiSettings.isMapToolbarEnabled = false
        phunwareMap.googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, R.raw.map_style
            )
        )

        mapManager.setPhunwareMap(phunwareMap)
        mapManager.loadCampus(campusId.toLong(),
            object : Callback<Campus> {
                override fun onSuccess(campus: Campus) {
                    Log.d(TAG, "Building loaded successfully")
                    currentBuilding = campus.campusBuilding

                    // Add a listener to monitor floor switches
                    mapManager.addFloorChangedListener(this@CampusRoutingActivity)

                    // Initialize a location provider
                    setManagedLocationProvider(currentBuilding)

                    phunwareMap.googleMap.setOnMapLoadedCallback {
                        buildingSpinnerAdapter.addAll(campus.buildings)
                        buildingSpinner.setSelection(0, false)
                        floorSpinner.setSelection(0, false)
                        buildingSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, l: Long) {
                                val selectedBuilding = buildingSpinnerAdapter.getItem(position)
                                floorSpinnerAdapter.clear()
                                if (selectedBuilding != null) {
                                    val sortedFloors = selectedBuilding.floorOptions.sortedBy { it.level }
                                    floorSpinnerAdapter.addAll(sortedFloors)
                                    floorSpinnerAdapter.notifyDataSetChanged()
                                    var selectedFloorIndex = 0
                                    val selectedFloorOptions = selectedBuilding.selectedFloor?.id?.let {
                                        getFloorOptionsFromSpinner(it)
                                    }
                                    if (selectedFloorOptions != null) {
                                        selectedFloorIndex = floorSpinnerAdapter.getPosition(selectedFloorOptions)
                                        if (selectedFloorIndex == -1) {
                                            selectedFloorIndex = 0
                                        }
                                    }

                                    floorSpinner.setSelection(selectedFloorIndex)
                                }
                            }

                            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                                floorSpinnerAdapter.clear()
                            }
                        }

                        floorSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, l: Long) {
                                val selectedFloor = floorSpinnerAdapter.getItem(position)
                                if (selectedFloor != null) {
                                    val currentFloorId = selectedFloor.id
                                    if (currentFloorId != currentBuilding.selectedFloor.id) {
                                        currentBuilding.selectFloor(currentFloorId)
                                    }
                                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(selectedFloor.bounds, 4)
                                    mapManager.animateCamera(cameraUpdate)
                                }
                            }

                            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
                        }

                        // Set building to initial floor value
                        var buildingIndex = campus.buildings.indexOfFirst { building ->
                            building.id == currentBuilding.id
                        }

                        if (buildingIndex == -1) {
                            buildingIndex = 0
                        }

                        buildingSpinner.setSelection(buildingIndex, false)

                        val initialFloorOptions = requireNotNull(currentBuilding.initialFloor ?: currentBuilding.buildingOptions.floors.firstOrNull())

                        // Animate the camera to the building at an appropriate zoom level
                        val cameraUpdate = CameraUpdateFactory
                            .newLatLngBounds(initialFloorOptions.bounds, 4)
                        mapManager.animateCamera(cameraUpdate)

                        // Enabled fab for routing
                        showFab(true)
                    }
                }

                override fun onFailure(throwable: Throwable) {
                    Log.d(TAG, "Error when loading building -- " + throwable.message)
                    showFab(false)
                }
            })
    }

    private fun getFloorOptionsFromSpinner(floorId: Long): FloorOptions? {
        for (index in 0 until floorSpinnerAdapter.count) {
            val floor: FloorOptions? = floorSpinnerAdapter.getItem(index)
            if (floor?.id == floorId) {
                return floor
            }
        }
        return null
    }

    /**
     * Navigator.OnManeuverChangedListener
     *
     * maneuverPosition: Added logic gate to avoid repeats from sensitive bluedot
     * maneuverFromSwiping: Avoid updating navOverlay for maneuver changes that originate from the
     * pager.
     */
    override fun onManeuverChanged(navigator: Navigator, position: Int) {
        if (maneuverPosition != position) {

            if (!maneuverFromSwiping) navOverlay.dispatchManeuverChanged(position)
            handleManeuverChange(position, navigator)
            maneuverPosition = position
            maneuverFromSwiping = false
        }
    }

    private fun handleManeuverChange(position: Int, navigator: Navigator) {
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
    /**
     * dispatchManeuverChanged
     *
     * Open function to be overridden by child Activities to handle valid maneuver changes.
     */
    open fun dispatchManeuverChanged(navigator: Navigator, position: Int) {

    }

    override fun onRouteSnapFailed() {
        // Do Nothing
    }

    /**
     * Building.OnFloorChangedListener
     */
    override fun onFloorChanged(building: Building?, floorId: Long) {
        for (index in 0 until buildingSpinner.count) {
            val buildingInCurrentIndex = buildingSpinnerAdapter.getItem(index)
            val newFloor = building?.getFloorOptionsById(floorId)
            if (buildingInCurrentIndex != null && buildingInCurrentIndex.id == newFloor?.buildingId) {
                if (buildingSpinner.selectedItemPosition == index) {
                    for (floorIndex in 0 until floorSpinnerAdapter.count) {
                        val floor = floorSpinnerAdapter.getItem(floorIndex)
                        if (floor != null && floor.id == floorId) {
                            if (floorSpinner.selectedItemPosition != floorIndex) {
                                floorSpinner.setSelection(floorIndex)
                                break
                            }
                        }
                    }
                } else {
                    buildingSpinner.setSelection(index)
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
        if ((getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter != null) {
            val managedProvider = PwManagedLocationProvider(application, building.id, null)
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
            navOverlay.setManeuvers(it.maneuvers)
            it.setCurrentManeuver(0)
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

        maneuverPosition = -1
        mapManager.removeLocationUpdateListener(this)
        navOverlay.clearPageChangeListeners()
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
            Log.e(TAG, "Couldn't find route.")
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