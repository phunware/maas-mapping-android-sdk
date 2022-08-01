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
import android.location.Location
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.phunware.kotlin.sample.R
import com.phunware.kotlin.sample.routing.fragment.OffRouteDialogFragment
import com.phunware.mapping.model.RouteManeuverOptions
import java.util.Timer
import java.util.TimerTask
import kotlin.math.min

internal class OffRouteActivity : RoutingActivity(),
        OffRouteDialogFragment.OffRouteDialogListener {

    private var dontShowOffRouteAgain: Boolean = false
    private var modalVisible: Boolean = false
    private var offRouteTimer: Timer? = null
    private var previousTimeDialogDismissed = 0L

    companion object {
        private val TAG = OffRouteActivity::class.java.simpleName
        private const val offRouteDistanceThreshold = 10.0 //distance in meters
        private const val offRouteTimeThreshold: Long = 5000 //time in milliseconds
        private const val timeBetweenDialogPromptThreshold = 10000 //time in milliseconds
    }

    /**
     * LocationListener - reports the post route snapping blue dot location.
     */
    override fun onLocationUpdate(p0: Location?) {
        super.onLocationUpdate(p0)

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
                Log.e(TAG, "Couldn't find route.")
                Snackbar.make(findViewById(R.id.map), R.string.no_route,
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

    override fun stopNavigating() {
        super.stopNavigating()

        dontShowOffRouteAgain = false
        modalVisible = false
    }

}