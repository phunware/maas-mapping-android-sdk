package com.phunware.kotlin.sample.routing.util

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
import com.phunware.kotlin.sample.R
import com.phunware.mapping.model.LandmarkManeuverOptions

import com.phunware.mapping.model.RouteManeuverOptions

import java.util.Locale

class ManeuverDisplayHelper {

    fun stringForDirection(context: Context?, maneuver: RouteManeuverOptions?): String {
        if (context == null || maneuver == null || maneuver.direction == null) {
            return ""
        }

        var landmarkStringTurn = ""
        var landmarkStringMain = ""
        if (maneuver.landmarks.isNotEmpty()) {
            val landmark = maneuver.landmarks[maneuver.landmarks.size - 1]
            if (landmark.position === LandmarkManeuverOptions.POSITION.AFTER) {
                landmarkStringTurn = SPACE + context.getString(R.string.after) + SPACE
            } else if (landmark.position === LandmarkManeuverOptions.POSITION.AT) {
                landmarkStringTurn = SPACE + context.getString(R.string.at) + SPACE
            }
            landmarkStringTurn += landmark.name
            landmarkStringMain += landmark.name
        }
        val directionString = StringBuilder()
        when (maneuver.direction) {
            RouteManeuverOptions.Direction.FLOOR_CHANGE ->
                directionString.append(floorChangeDescriptionForManeuver(context, maneuver)).append("\n")
            RouteManeuverOptions.Direction.BEAR_LEFT -> {
                directionString.append(context.getString(R.string.bear_left))
                directionString.append(landmarkStringTurn)
            }
            RouteManeuverOptions.Direction.BEAR_RIGHT -> {
                directionString.append(context.getString(R.string.bear_right))
                directionString.append(landmarkStringTurn)
            }
            RouteManeuverOptions.Direction.LEFT -> {
                directionString.append(context.getString(R.string.turn_left))
                directionString.append(landmarkStringTurn)
            }
            RouteManeuverOptions.Direction.RIGHT -> {
                directionString.append(context.getString(R.string.turn_right))
                directionString.append(landmarkStringTurn)
            }
            RouteManeuverOptions.Direction.STRAIGHT ->
                if (landmarkStringMain.isEmpty()) {
                    directionString.append(
                        context.getString(R.string.continue_straight)
                    )
                } else {
                    directionString.append(
                        context.getString(R.string.continue_straight)
                    )
                }
            else -> directionString.append(context.getString(R.string.unknown))
        }
        return directionString.toString()
    }

    /**
     * Build a string in the form "in 27 feet" or "for 27 feet" using the distance of the maneuver.
     */
    fun distanceForDirection(context: Context?, maneuver: RouteManeuverOptions?, prep: String): String {
        if (context == null || maneuver == null || maneuver.direction == null) {
            return ""
        }
        return String.format(Locale.US, context.getString(R.string.section_distance), prep, getStringDistanceInFeet(maneuver.distance))
    }

    /**
     * Converts the distance from meters to feet
     * @param distance Double distance in meters
     * @return String object containing the converted number of feet (rounded up)
     */
    private fun getStringDistanceInFeet(distance: Double): String {
        if (distance < 0) {
            return ""
        }
        var res = distance * NUM_FEET_PER_METER
        res = Math.ceil(res)
        return res.toInt().toString()
    }

    fun getImageResourceForDirection(context: Context,
        maneuver: RouteManeuverOptions): Int {
        var resource = 0
        when (maneuver.direction) {
            RouteManeuverOptions.Direction.STRAIGHT -> resource = R.drawable.ic_route_straight
            RouteManeuverOptions.Direction.LEFT -> resource = R.drawable.ic_route_left
            RouteManeuverOptions.Direction.RIGHT -> resource = R.drawable.ic_route_right
            RouteManeuverOptions.Direction.BEAR_LEFT -> resource = R.drawable.ic_route_bear_left
            RouteManeuverOptions.Direction.BEAR_RIGHT -> resource = R.drawable.ic_route_bear_right
            RouteManeuverOptions.Direction.FLOOR_CHANGE -> {
                val changeDescription = floorChangeDescriptionForManeuver(context, maneuver)
                if (changeDescription.toLowerCase()
                        .contains(context.getString(R.string.elevator))) {
                    resource = if (changeDescription.toLowerCase()
                            .contains(context.getString(R.string.down))) {
                        R.drawable.ic_route_elevatordown
                    } else {
                        R.drawable.ic_route_elevatorup
                    }
                } else {
                    resource = if (changeDescription.toLowerCase()
                            .contains(context.getString(R.string.down))) {
                        R.drawable.ic_route_stairsdown
                    } else {
                        R.drawable.ic_route_stairsup
                    }
                }
            }

            else -> {
            }
        }

        return resource
    }

    private fun floorChangeDescriptionForManeuver(context: Context,
        maneuver: RouteManeuverOptions): String {
        val endPoint = maneuver.points[maneuver.points.size - 1]
        val endPointName = endPoint.name
        var methodOfChange = ""
        if (endPointName != null && endPointName.toLowerCase()
                .contains(context.getString(R.string.elevator))) {
            methodOfChange = context.getString(R.string.elevator)
        } else if (endPointName != null && endPointName.toLowerCase()
                .contains(context.getString(R.string.stairs))) {
            methodOfChange = context.getString(R.string.stairs)
        }

        val direction = directionForManeuver(maneuver)
        val directionMessage: String
        directionMessage = when (direction) {
            FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionUp ->
                context.getString(R.string.up_to_level)
            FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionDown ->
                context.getString(R.string.down_to_level)
            else -> context.getString(R.string.to)
        }
        return String.format(Locale.US, context.getString(R.string.floor_change_message_format),
            methodOfChange, directionMessage, endPoint.level)
    }

    private fun directionForManeuver(maneuver: RouteManeuverOptions): FloorChangeDirection {
        val startPoint = maneuver.points[0]
        val endPoint = maneuver.points[maneuver.points.size - 1]
        return if (startPoint.level < endPoint.level)
            FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionUp
        else if (startPoint.level > endPoint.level)
            FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionDown
        else
            FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionSameFloor
    }

    private enum class FloorChangeDirection {
        PWManeuverDisplayHelperFloorChangeDirectionSameFloor,
        PWManeuverDisplayHelperFloorChangeDirectionUp,
        PWManeuverDisplayHelperFloorChangeDirectionDown
    }

    companion object {
        private const val NUM_FEET_PER_METER = 3.28084
        private const val SPACE = " "
    }
}
