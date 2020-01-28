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
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.phunware.kotlin.sample.R
import com.phunware.mapping.manager.Navigator
import com.phunware.mapping.model.RouteOptions
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

open class WalkTimeActivity : RoutingActivity() {
    companion object {
        private const val UPDATE_DELAY = 5000L
        private const val AVERAGE_WALK_SPEED = 0.7 //units in meters per second
        private const val POSITION_UPDATE_TICK_TIME = .515 // seconds
    }

    private var exitNavListener: View.OnClickListener = View.OnClickListener { stopNavigating() }

    //Walk Time Views
    private lateinit var walkTimeView: ConstraintLayout
    private lateinit var walkTimeTextview: TextView
    private lateinit var arrivalTimeTextview: TextView
    private lateinit var exitRouteButton: Button

    private val gpsPositionList: MutableList<Location> = ArrayList()
    private val walkspeedList: MutableList<Double> = ArrayList()

    private var calculatedWalkSpeed = 0.0
    private val dateFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    private var currentManeuverIndex = -1
    private val handler = Handler()
    private val timeUpdater = Runnable { updateWalkTime() }
    private var userLastReportedLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialize WalkTime Views
        walkTimeView = findViewById(R.id.walk_time_view)
        walkTimeTextview = findViewById(R.id.walk_time_textview)
        arrivalTimeTextview = findViewById(R.id.arrival_time_textview)
        exitRouteButton = findViewById(R.id.button_exit_route)
        exitRouteButton.setOnClickListener(exitNavListener)
    }

    /**
     * RoutingActivity.dispatchManeuverChanged
     *
     *  Handle valid maneuver change from RoutingActivity.
     */
    override fun dispatchManeuverChanged(navigator: Navigator, position: Int) {
        this.currentManeuverIndex = position

        //Make call to update walk time
        updateWalkTime()
    }

    /**
     * LocationListener
     */
    override fun onLocationUpdate(p0: Location?) {
        super.onLocationUpdate(p0)

        if (p0 != null) {
            userLastReportedLocation = p0
            gpsPositionList.add(p0)
            while (gpsPositionList.count() > 5) {
                gpsPositionList.removeAt(0)
            }

            val firstLocation = gpsPositionList.first()
            val lastLocation = gpsPositionList.last()
            val distanceCovered = firstLocation.distanceTo(lastLocation)

            walkspeedList.add((distanceCovered / 2.5))
            while (walkspeedList.count() > 15) {
                walkspeedList.removeAt(0)
            }
            if (walkspeedList.count() < 15) return
            Log.d("DUSTINw", "walkspeedList: $walkspeedList")

            calculatedWalkSpeed =
                    walkspeedList.sum() / 15 //Get location updates about every half second and we cache the last 5
            Log.d("DUSTINw", "calculatedWalkSpeed: $calculatedWalkSpeed")
        }
    }

    /**
     * Private Methods
     */
    private fun updateWalkTime() {
        var distance = 0.0
        for (i in currentManeuverIndex until navigator!!.maneuvers.count()) {
            val maneuver = navigator!!.maneuvers[i]
            if (i == currentManeuverIndex && userLastReportedLocation != null) {
                val results = floatArrayOf(0f)
                val maneuverEndPoint = maneuver.points[maneuver.points.size - 1]
                Location.distanceBetween(userLastReportedLocation!!.latitude, userLastReportedLocation!!.longitude,
                        maneuverEndPoint.location.latitude, maneuverEndPoint.location.longitude, results)
                distance += results[0]
            } else {
                distance += maneuver.distance
            }
        }

        Log.d("DUSTIN", "calculatedWalkSpeed: $calculatedWalkSpeed")

        //TODO: try if (gpsLocationList.size == 5)

        val estimateTimeInSeconds = if (calculatedWalkSpeed >= AVERAGE_WALK_SPEED) {
            Log.d("DUSTIN", "Using calculated walkspeed: ${distance / calculatedWalkSpeed} secs")
            distance / calculatedWalkSpeed
        } else {
            Log.d("DUSTIN", "Using average walkspeed: ${distance / AVERAGE_WALK_SPEED} secs")
            distance / AVERAGE_WALK_SPEED
        }
        Log.d("DUSTIN", "minutes in double: ${estimateTimeInSeconds / 60.0}")
        val numMinutes: Int = if (estimateTimeInSeconds < 60) 1 else ceil(estimateTimeInSeconds / 60.0).toInt()
        Log.d("DUSTIN", "minutes in after round up: ${numMinutes}")
        walkTimeTextview.text = resources.getQuantityString(
                R.plurals.demo_walk_time_minutes, numMinutes, numMinutes)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, estimateTimeInSeconds.toInt())
        val formattedArrivalTime: String = dateFormatter.format(calendar.time)
        val formattedArrivalTimeText =
                String.format(getString(R.string.demo_arrival_time_title), formattedArrivalTime)
        arrivalTimeTextview.text = formattedArrivalTimeText

        handler.removeCallbacks(timeUpdater)
        handler.postDelayed(timeUpdater, UPDATE_DELAY)
    }


    override fun startNavigating(route: RouteOptions) {
        gpsPositionList.clear()
        super.startNavigating(route)

        walkTimeView.visibility = View.VISIBLE
    }

    override fun stopNavigating() {
        super.stopNavigating()

        walkTimeView.visibility = View.GONE
        handler.removeCallbacks(timeUpdater)
    }
}