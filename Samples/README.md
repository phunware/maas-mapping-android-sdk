# Android Mapping SDK Use Case Samples

## Overview

* Use cases that exhibit different applications of the Mapping SDK.
* Each use case is contained within it's own Activity class.

## Samples

### Load Building

Code Samples: [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/building/LoadBuildingActivity.kt)

Loads the building that is set up in the MaaS portal. The camera should pan and zoom automatically to the location of the building.

#### Usage:
Configure building on the MaaS portal

### Custom POI Image

Code Samples:  [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/building/CustomPoiImageActivity.kt)

Loads the building ans uses a custom POI icon provided by the app to render the POIs . The camera should pan and zoom automatically to the location of the building.

#### Usage:
Configure building on the MaaS portal and then use PhunwareMapManager.setIconProvider to set the callback that returns the POI icon.

```
mapManager.setIconProvider(object: IconProvider {
            override fun getImage(pointOptions: PointOptions): Bitmap? {
                // Return the bitmap for this POI icon from here
                ...
                return bitmap
            }
        })
```

### Bluedot Location

Code Samples: [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/location/BluedotLocationActivity.kt)

Display current location in your building as reported by the location provider configured on MaaS

#### Usage:

Configure location provider on building's edit page in MaaS portal, then configure for each floor

### Location Modes
Code Samples: [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/location/LocationModesActivity.kt)

Illustrates the three location modes supported by the Mapping SDK (Normal, Locate Me and Follow Me). Locate Me mode centers the bluedot on the screen, while the Follow Me mode centers the bluedot and adjusts the camera based on the device's heading. Normal mode does not perform any automatic centering or panning.  

#### Usage:
Click the floating action button in the bottom right corner to toggle between modes.

### Create Custom POIs
Code Samples: [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/poi/CustomPOIActivity.kt)

Adds a POI to the building map that is not specified in the MaaS portal.

#### Usage:
Long press the map anywhere within the building boundaries. A dialog will appear and allow you to enter a name for the custom POI. Click ok and your POI will appear on the map.


### Search for POIs
Code Samples: [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/poi/SearchPoiActivity.kt)

Search through the list of POIs contained in your building. Note that at least one POI must be present in your building in order for this sample to work.

#### Usage:
Click the floating action button in the bottom right corner to open up a dialog that lists all the POIs in a scrollable list. Either scroll through the list or type in the search field to find specific POI matches in the list. Click the desired POI and the map will pan to that POI's location.

### Routing Between POIs
Code Samples: [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/routing/RoutingActivity.kt)

Route between two POIs or from your current location to a POI (if you have a bluedot). Note that for this sample to work you will need to connect your POIs with segments in the MaaS portal.

#### Usage:
Click the floating action button in the bottom right corner to open up a routing dialog that allows you to select a start and end point for your route. If you have acquired a bluedot, the start field will be automatically populated with your current location. Click `ROUTE` to display the path between the selected points (if one exists)

### Landmark Routing
Code Samples:  [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/routing/LandmarkRoutingActivity.kt)

Route between two POIs or from your current location to a POI while using landmarks in navigation instructions . Note that for this sample to work, you will need to setup landmarks in the MaaS portal as described here (https://pw-lbs.phunware.com/docs/landmark-based-routing).

#### Usage:
Click the floating action button in the bottom right corner to open up a routing dialog that allows you to select a start and end point for your route. If you have acquired a bluedot, the start field will be automatically populated with your current location. Click `ROUTE` to display the path between the selected points (if one exists)

### Location Sharing
Code Samples: [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/location/LocationSharingActivity.kt)

Show your current location in a building as well as others in the same building.

#### Usage:
Open this sample two or more devices (iOS or Android) to view other user's locations. Note that the samples must be configured to point at the same building and must both be open simultaneously to work properly. You can click the `SET USER INFO` button in the top right corner to create a friendly name and device type that other users will see.

### Voice Prompt
Code Samples:  [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/VoicePrompt.md))

Read route instruction aloud to the user as they swipe through the route instructions or as they traverse them with indoor location.

### Walk Time
Code Samples:  [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/WalkTime.md))

Display walk time at the bottom of map.

### Off Route
Code Samples:  [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/OffRouteAlerts.md))

Monitor the users location updates and alert the user if they deviated from the route using a predetermined distance and time threshold.

### Privacy
You understand and consent to Phunware’s Privacy Policy located at www.phunware.com/privacy. If your use of Phunware’s software requires a Privacy Policy of your own, you also agree to include the terms of Phunware’s Privacy Policy in your Privacy Policy to your end users.

### Terms
Use of this software requires review and acceptance of our terms and conditions for developer use located at http://www.phunware.com/terms/
