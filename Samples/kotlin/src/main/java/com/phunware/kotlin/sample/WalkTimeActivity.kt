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
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.phunware.mapping.manager.Navigator
import com.phunware.mapping.model.RouteOptions
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale

open class WalkTimeActivity : RoutingActivity() {
    private var exitNavListener: View.OnClickListener = View.OnClickListener { stopNavigating() }

    //Walk Time Views
    private lateinit var walkTimeView: RelativeLayout
    private lateinit var walkTimeTextview: TextView
    private lateinit var arrivalTimeTextview: TextView
    private lateinit var exitRouteButton: Button

    private val gpsPositionList: MutableList<Location> = ArrayList()
    private val averageWalkSpeed = 0.7 //units in meters per second
    private var calculatedWalkSpeed = 0.0
    private val dateFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    private var routingFromCurrentLocation = false
    private var currentManeuverIndex = -1
    private val handler = Handler()
    private val timeUpdater = Runnable { updateWalkTime() }

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
     * Navigator.OnManeuverChangedListener
     */
    override fun onManeuverChanged(navigator: Navigator, position: Int) {
        super.onManeuverChanged(navigator, position)
        this.currentManeuverIndex = position

        //Make call to update walk time
        updateWalkTime()
    }

    override fun onRouteSnapFailed() {
        // Do Nothing
    }

    /**
     * LocationListener
     */
    override fun onLocationUpdate(p0: Location?) {
        super.onLocationUpdate(p0)

        if (p0 != null) {
            gpsPositionList.add(p0)
            while (gpsPositionList.count() > 5) {
                gpsPositionList.removeAt(0)
            }

            val firstLocation = gpsPositionList.first()
            val lastLocation = gpsPositionList.last()
            val distanceCovered = firstLocation.distanceTo(lastLocation)
            calculatedWalkSpeed =
                    distanceCovered / 2.5 //Get location updates about every half second and we cache the last 5
        }
    }

    /**
     * Private Methods
     */
    private fun updateWalkTime() {
        var distance = 0.0
        for (i in currentManeuverIndex until navigator!!.maneuvers.count()) {
            val maneuver = navigator!!.maneuvers[i]
            distance += maneuver.distance
        }

        val estimateTimeInSeconds: Double
        if (routingFromCurrentLocation) {
            if (calculatedWalkSpeed >= averageWalkSpeed) {
                estimateTimeInSeconds = (distance / calculatedWalkSpeed)
            } else {
                estimateTimeInSeconds = (distance / averageWalkSpeed)
            }
        } else {
            estimateTimeInSeconds = (distance / averageWalkSpeed)
        }

        if (estimateTimeInSeconds < 60) {
            walkTimeTextview.setText(R.string.demo_walk_time_less_than_one_minute)
        } else {
            val numMinutes: Int = (estimateTimeInSeconds / 60.0).toInt()
            val numMinutesString: String
            if (numMinutes == 1) {
                numMinutesString =
                        resources.getString(R.string.demo_walk_time_one_minute, numMinutes)
            } else {
                numMinutesString =
                        resources.getString(R.string.demo_walk_time_multiple_minutes, numMinutes)
            }
            walkTimeTextview.text = numMinutesString
        }

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
        super.startNavigating(route)

        walkTimeView.visibility = View.VISIBLE
    }

    override fun stopNavigating() {
        super.stopNavigating()

        walkTimeView.visibility = View.GONE
        handler.removeCallbacks(timeUpdater)
    }

    companion object {
        private const val UPDATE_DELAY = 5000L
    }
}