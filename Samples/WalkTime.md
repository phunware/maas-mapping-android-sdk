## Sample - Walk Time

### Overview
- Display walk time at the bottom of map.

### Usage

- Need to fill out `applicationId`, `accessKey`, `signatureKey`, and `buildingIdentifier` in `strings.xml` and `integers.xml`

### Sample Code
- [WalkTimeActivity.kt](https://github.com/phunware/maas-mapping-android-sdk/blob/sample_code_updates/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/routing/WalkTimeActivity.kt)

**Step 1: Copy the following files to your project**

- WalkTimeActivity.kt

**Step 2: Pay attention to `WalkTimeActivity.onLocationUpdate()` which is called every 0.5 seconds**
Save a list of reported locations into a queue (FIFO) and calculate the distance between the head and tail of that queue. Use that distance to calculate the average `calculatedWalkSpeed` as follow.

```
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
```

**Step 3: Update the walk time on each maneuver change by calling `updateWalkTime()`**

When the user changes the maneuver, the walk time will be updated for the new maneuver.
```
override fun onManeuverChanged(navigator: Navigator, position: Int) {
        super.onManeuverChanged(navigator, position)
        this.currentManeuverIndex = position

        //Make call to update walk time
        updateWalkTime()
    }
```

**Step 4: The `updateWalkTime()` method calculates the time and updates the view.**

The `averageWalkSpeed` is set to 0.7m/s. You can change this constant.
Change the logic in this method to suit your needs.

```
    private fun updateWalkTime(maneuverIndex: Int) {
            var distance = 0.0
        for (i in currentManeuverIndex until navigator!!.maneuvers.count()) {
            val maneuver = navigator!!.maneuvers[i]
            distance += maneuver.distance
        }

        val estimateTimeInSeconds = if (routingFromCurrentLocation &&
                calculatedWalkSpeed >= averageWalkSpeed) {
            distance / calculatedWalkSpeed
        } else {
            distance / averageWalkSpeed
        }

        if (estimateTimeInSeconds < 60) {
            walkTimeTextview.setText(R.string.demo_walk_time_less_than_one_minute)
        } else {
            val numMinutesTemp = estimateTimeInSeconds / 60.0
            // Provide slop of 1 to 1.2 minutes where eta will be set at 1 minute and not rounded up
            val numMinutes: Int = if (numMinutesTemp >= 1.0 && numMinutesTemp < 1.2) {
                1
            } else {
                ceil(estimateTimeInSeconds / 60.0).toInt()
            }
            val numMinutesString: String
            numMinutesString = if (numMinutes == 1) {
                resources.getString(R.string.demo_walk_time_one_minute, numMinutes)
            } else {
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
```

# Privacy
You understand and consent to Phunware’s Privacy Policy located at www.phunware.com/privacy. If your use of Phunware’s software requires a Privacy Policy of your own, you also agree to include the terms of Phunware’s Privacy Policy in your Privacy Policy to your end users.

# Terms
Use of this software requires review and acceptance of our terms and conditions for developer use located at http://www.phunware.com/terms/