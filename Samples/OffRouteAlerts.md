## Sample - Off Route
====================

### Overview
- This feature will monitor the users location updates alert the user if they deviated from the route using a predetermined distance and time threshold.

### Usage

- Need to fill out `applicationId`, `accessKey`, `signatureKey`, and `buildingId`in strings.xml and integers.xml.

### Sample Code
- [OffRouteActivity.kt](kotlin/src/main/java/com/phunware/kotlin/sample/routing/OffRouteActivity.kt)
- [OffRouteDialogFragment.kt](kotlin/src/main/java/com/phunware/kotlin/sample/routing/fragment/OffRouteDialogFragment.kt)

**Step 1: Copy the following files to your project**

- OffRouteActivity.kt
- OffRouteDialogFragment.kt

**Step 2: In OffRouteActivity.kt, pay attention to the `onLocationUpdate()` method**

```
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
```

Calculate the `minDistanceInMeters` to show a dialog alert when user is off the route using `showModal()`, which will show the [OffRouteDialogFragment.kt](https://github.com/phunware/maas-mapping-android-sdk/blob/sample_code_updates/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/OffRouteDialogFragment.kt).
If the `minDistanceInMeters` is greater than the hard-coded value `offRouteDistanceThreshold` then call `showModal()`

```
private fun showModal() {
        if (!modalVisible) {
            modalVisible = true
            val offRouteDialog = OffRouteDialogFragment()
            offRouteDialog.show(supportFragmentManager, "")
        }
    }
```

To avoid spamming your users with the modal if they were to stay off route for an extended period of time, utilize helper function  checkTimeBetweenShowingDialog() to suspend/resume off routing detection logic. This checks if the difference in time from when the user previously dismissed the dialog to the current time is greater than the threshold field set as `timeBetweenDialogPromptThreshold` (Default: 10 seconds)

```
private fun checkTimeBetweenShowingDialog(): Boolean {
        return System.currentTimeMillis() - previousTimeDialogDismissed > timeBetweenDialogPromptThreshold
    }
```

# Privacy
You understand and consent to Phunware’s Privacy Policy located at www.phunware.com/privacy. If your use of Phunware’s software requires a Privacy Policy of your own, you also agree to include the terms of Phunware’s Privacy Policy in your Privacy Policy to your end users.

# Terms
Use of this software requires review and acceptance of our terms and conditions for developer use located at http://www.phunware.com/terms/