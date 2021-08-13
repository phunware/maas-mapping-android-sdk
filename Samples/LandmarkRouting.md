## Sample - Landmark Routing
=============================

### Overview
- This feature will use landmarks in navigation instructions when user routes between two POIs.
Note that for this sample to work, you will need to setup landmarks in the MaaS portal as described here (https://pw-lbs.phunware.com/docs/landmark-based-routing)

### Sample Code
- [LandmarkRoutingActivity.kt](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/routing/LandmarkRoutingActivity.kt)

**Step 1: Enabling Landmarks within Mapping SDK**

```
phunwareMapManager.enableLandmarks(true)
```

**Step 2: Include landmarks in the navigation instructions**

```
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
```

The above code snippet uses the closest landmark to the next maneuver change in the routing instructions.

# Privacy
You understand and consent to Phunware’s Privacy Policy located at www.phunware.com/privacy. If your use of Phunware’s software requires a Privacy Policy of your own, you also agree to include the terms of Phunware’s Privacy Policy in your Privacy Policy to your end users.

# Terms
Use of this software requires review and acceptance of our terms and conditions for developer use located at http://www.phunware.com/terms/