package com.phunware.kotlin.sample

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Spinner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.PolyUtil
import com.phunware.core.PwCoreSession
import com.phunware.core.PwLog
import com.phunware.location.provider_managed.ManagedProviderFactory
import com.phunware.location.provider_managed.PwManagedLocationProvider
import com.phunware.location_core.PwLocationProvider
import com.phunware.mapping.MapFragment
import com.phunware.mapping.OnPhunwareMapReadyCallback
import com.phunware.mapping.PhunwareMap
import com.phunware.mapping.bluedot.LocationManager
import com.phunware.mapping.manager.Callback
import com.phunware.mapping.manager.Navigator
import com.phunware.mapping.manager.PhunwareMapManager
import com.phunware.mapping.manager.Router
import com.phunware.mapping.model.Building
import com.phunware.mapping.model.FloorOptions
import com.phunware.mapping.model.PointOptions
import com.phunware.mapping.model.RouteManeuverOptions
import com.phunware.mapping.model.RouteOptions
import java.lang.ref.WeakReference
import java.util.Timer
import java.util.TimerTask
import kotlin.math.min

class OffRouteActivity : AppCompatActivity(), OnPhunwareMapReadyCallback,
        Building.OnFloorChangedListener, Navigator.OnManeuverChangedListener,
        LocationManager.LocationListener,
        OffRouteDialogFragment.OffRouteDialogListener {

    private lateinit var mapManager: PhunwareMapManager
    private lateinit var mapFragment: MapFragment
    private lateinit var currentBuilding: Building
    private lateinit var floorSpinner: Spinner
    private lateinit var floorSpinnerAdapter: ArrayAdapter<FloorOptions>
    private lateinit var content: RelativeLayout

    // Navigation Views
    private lateinit var fab: FloatingActionButton
    private var navigator: Navigator? = null
    private lateinit var navOverlayContainer: View
    private lateinit var navOverlay: NavigationOverlayView
    private lateinit var startPicker: Spinner
    private lateinit var endPicker: Spinner
    private lateinit var accessible: CheckBox

    private var selectRouteListener: View.OnClickListener = View.OnClickListener { showRoutingDialog() }
    private var exitNavListener: View.OnClickListener = View.OnClickListener { stopNavigating() }
    private var dontShowOffRouteAgain: Boolean = false
    private var modalVisible: Boolean = false
    private var offRouteTimer: Timer? = null
    private var previousTimeDialogDismissed = 0L

    companion object {
        private val TAG = OffRouteActivity::class.java.simpleName
        private const val ITEM_ID_LOCATION = -2
        private const val offRouteDistanceThreshold = 10.0 //distance in meters
        private const val offRouteTimeThreshold: Long = 5000 //time in milliseconds
        private const val timeBetweenDialogPromptThreshold = 10000 //time in milliseconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.off_route)
        content = findViewById(R.id.content)

        // Initialize views for routing
        fab = findViewById(R.id.fab)
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
        mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getPhunwareMapAsync(this)
    }

    override fun onBackPressed() {
        if (navOverlayContainer.visibility == View.VISIBLE) {
            stopNavigating()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * OnPhunwareMapReadyCallback
     */
    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        // Retrieve buildingId from integers.xml
        val buildingId = resources.getInteger(R.integer.buildingId)

        phunwareMap.googleMap.uiSettings.isMapToolbarEnabled = false
        phunwareMap.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this@OffRouteActivity, R.raw.map_style))

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
                        mapManager.addFloorChangedListener(this@OffRouteActivity)

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
     * LocationListener - reports the post route snapping blue dot location.
     */
    override fun onLocationUpdate(p0: Location?) {
        if (!modalVisible && !dontShowOffRouteAgain && checkTimeBetweenShowingDialog()) {
            if (p0 != null) {
                var minDistanceInMeters = Double.MAX_VALUE
                for (maneuver: RouteManeuverOptions in navigator!!.maneuvers) {
                    for (i in 0..maneuver.points.size - 2) {
                        val ptA = maneuver.points[i]
                        val ptB = maneuver.points[i + 1]
                        minDistanceInMeters = min(minDistanceInMeters, PolyUtil.distanceToLine(LatLng(p0.latitude, p0.longitude), ptA.location, ptB.location))
                    }
                }

                if (minDistanceInMeters.toInt() > 0) {
                    if (minDistanceInMeters >= offRouteDistanceThreshold) {
                        offRouteTimer?.cancel()
                        showModal()
                    } else {
                        if (offRouteTimer == null) {
                            offRouteTimer = Timer()
                            offRouteTimer?.schedule(object : TimerTask() {
                                override fun run() {
                                    showModal()
                                    offRouteTimer = null
                                }
                            }, offRouteTimeThreshold)
                        }
                    }
                } else {
                    if (offRouteTimer != null) {
                        offRouteTimer?.cancel()
                        offRouteTimer = null
                    }
                }
            }
        }
    }

    /**
     * OffRouteDialogFragment.OffRouteDialogListener
     */
    override fun onDismiss(dontShowAgain: Boolean) {
        previousTimeDialogDismissed = System.currentTimeMillis()
        modalVisible = false
        dontShowOffRouteAgain = dontShowAgain
    }

    private fun checkTimeBetweenShowingDialog(): Boolean {
        return System.currentTimeMillis() - previousTimeDialogDismissed > timeBetweenDialogPromptThreshold
    }

    override fun onReroute() {
        modalVisible = false
        val currentRouteEndID = navigator?.route!!.endPointId
        val currentRouteIsAccessible = navigator?.route!!.isAccessible
        val currentLocation = LatLng(mapManager.currentLocation.latitude, mapManager.currentLocation.longitude)
        val router = mapManager.findRoutes(currentLocation, currentRouteEndID, mapManager.currentBuilding.selectedFloor.id, currentRouteIsAccessible)

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

    /**
     * Private Methods
     */
    private fun showModal() {
        if (!modalVisible) {
            modalVisible = true
            val offRouteDialog = OffRouteDialogFragment()
            offRouteDialog.show(supportFragmentManager, "")
        }
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

    @SuppressLint("RestrictedApi")
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
                    if (show) fab.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!show) fab.visibility = View.GONE
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
                .setPositiveButton("Route") { _, _ -> getRoutes() }
                .setNegativeButton("Cancel") { _, _ ->
                    // Do Nothing - Close Dialog
                }

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
            points.add(0, PointOptions()
                    .id(ITEM_ID_LOCATION.toLong())
                    .location(currentLocation)
                    .level(currentFloorId)
                    .name(getString(R.string.current_location)))
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

        val router: Router
        router = if (startId.compareTo(ITEM_ID_LOCATION) == 0) {
            val currentLocation = LatLng(mapManager.currentLocation.latitude, mapManager.currentLocation.longitude)
            mapManager.findRoutes(currentLocation, endId, mapManager.currentBuilding.selectedFloor.id, isAccessible)
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
        mapManager.addLocationUpdateListener(this)
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
        mapManager.removeLocationUpdateListener(this)
        navOverlayContainer.visibility = View.GONE
        fab.setImageResource(R.drawable.ic_navigation)
        fab.setOnClickListener(selectRouteListener)

        dontShowOffRouteAgain = false
        modalVisible = false
    }

}