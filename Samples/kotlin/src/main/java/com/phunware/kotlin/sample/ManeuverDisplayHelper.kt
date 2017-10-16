package com.phunware.kotlin.sample

import android.content.Context

import com.phunware.mapping.model.RouteManeuverOptions

import java.util.Locale

internal class ManeuverDisplayHelper {

    fun stringForDirection(context: Context?, maneuver: RouteManeuverOptions?): String {
        if (context == null || maneuver == null || maneuver.direction == null) {
            return ""
        }
        val directionString = StringBuilder()
        when (maneuver.direction) {
            RouteManeuverOptions.Direction.FLOOR_CHANGE ->
                directionString.append(floorChangeDescriptionForManeuver(context, maneuver))
            RouteManeuverOptions.Direction.BEAR_LEFT ->
                directionString.append(context.getString(R.string.bear_left))
            RouteManeuverOptions.Direction.BEAR_RIGHT ->
                directionString.append(context.getString(R.string.bear_right))
            RouteManeuverOptions.Direction.LEFT ->
                directionString.append(context.getString(R.string.turn_left))
            RouteManeuverOptions.Direction.RIGHT ->
                directionString.append(context.getString(R.string.turn_right))
            RouteManeuverOptions.Direction.STRAIGHT ->
                directionString.append(String.format(Locale.US,
                    context.getString(R.string.continue_straight_distance),
                    getStringDistanceInFeet(maneuver.distance)))
            else -> directionString.append(context.getString(R.string.unknown))
        }
        return directionString.toString()
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
            RouteManeuverOptions.Direction.STRAIGHT -> resource = R.drawable.ic_arrow_straight
            RouteManeuverOptions.Direction.LEFT -> resource = R.drawable.ic_arrow_left
            RouteManeuverOptions.Direction.RIGHT -> resource = R.drawable.ic_arrow_right
            RouteManeuverOptions.Direction.BEAR_LEFT -> resource = R.drawable.ic_arrow_bear_left
            RouteManeuverOptions.Direction.BEAR_RIGHT -> resource = R.drawable.ic_arrow_bear_right
            RouteManeuverOptions.Direction.FLOOR_CHANGE -> {
                val changeDescription = floorChangeDescriptionForManeuver(context, maneuver)
                if (changeDescription.toLowerCase()
                        .contains(context.getString(R.string.elevator))) {
                    resource = if (changeDescription.toLowerCase()
                            .contains(context.getString(R.string.down))) {
                        R.drawable.ic_elevator_down
                    } else {
                        R.drawable.ic_elevator_up
                    }
                } else {
                    resource = if (changeDescription.toLowerCase()
                            .contains(context.getString(R.string.down))) {
                        R.drawable.ic_stairs_down
                    } else {
                        R.drawable.ic_stairs_up
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
        private val NUM_FEET_PER_METER = 3.28084
    }
}
