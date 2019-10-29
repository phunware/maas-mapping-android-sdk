# Android Mapping SDK Use Case Samples

## Overview

* Use cases that exhibit different applications of the Mapping SDK
* Written in both Java and Kotlin
* Each use case is contained within it's own Activity class

## Setup

In order to see these use cases in action, you will need to set up the sample to point at your Phunware org/building. You will also need to add a Google Maps API Key to use the Location/Mapping SDKs. Please note that all keys must be entered for both the Java and the Kotlin samples -- they are technically separate apps so they each have their own sets of keys.

1. To obtain the Phunware credentials you will need to login to the MaaS portal and obtain your app's `appId`, `accessKey`, and `signatureKey`. Find instructions on how to obtain these keys [here](https://developer.phunware.com/display/DD/Phunware+SDK+On-boarding#PhunwareSDKOn-boarding-GeneralOn-boardingStepsforALLAndroidSDKs). Steps 3 and 4 detail how to add these keys to the `strings.xml`

2. There is also a string resource for `google_maps_key` in the `strings.xml` folder. If you need help getting a Google Maps API Key,  please find instructions [here](https://developers.google.com/maps/documentation/android-sdk/start#step_4_get_a_google_maps_api_key).

3. Lastly, you will need to retrieve your `buildingId` from the MaaS portal. to do this, you'll need to navigate to the Mapping section and open the settings for your building. This can be done by drilling down the menu to the building level (Venue-->Campus-->Building). Hover your mouse over the desired building and click the gear icon to see your building configuration. Once you have your `buildingId`, add it to `integers.xml` to point the sample at your building.

After you have these keys you should be all set to run the use case samples!

## Samples

### Load Building

Code Samples:  [Java](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/java/src/main/java/com/phunware/java/sample/building/LoadBuildingActivity.java) // [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/building/LoadBuildingActivity.kt)

Loads the building that is set up in the MaaS portal. The camera should pan and zoom automatically to the location of the building.

#### Usage:
Configure building on the MaaS portal

### Bluedot Location

Code Samples:  [Java](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/java/src/main/java/com/phunware/java/sample/location/BluedotLocationActivity.java) // [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/location/BluedotLocationActivity.kt)

Display current location in your building as reported by the location provider configured on MaaS

#### Usage:

Configure location provider on building's edit page in MaaS portal, then configure for each floor

### Location Modes
Code Samples:  [Java](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/java/src/main/java/com/phunware/java/sample/location/LocationModesActivity.java) // [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/location/LocationModesActivity.kt)

Illustrates the three location modes supported by the Mapping SDK (Normal, Locate Me and Follow Me). Locate Me mode centers the bluedot on the screen, while the Follow Me mode centers the bluedot and adjusts the camera based on the device's heading. Normal mode does not perform any automatic centering or panning.  

#### Usage:
Click the floating action button in the bottom right corner to toggle between modes.

### Create Custom POIs
Code Samples:  [Java](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/java/src/main/java/com/phunware/java/sample/poi/CustomPOIActivity.java) // [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/poi/CustomPOIActivity.kt)

Adds a POI to the building map that is not specified in the MaaS portal.

#### Usage:
Long press the map anywhere within the building boundaries. A dialog will appear and allow you to enter a name for the custom POI. Click ok and your POI will appear on the map.


### Search for POIs
Code Samples: [Java](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/java/src/main/java/com/phunware/java/sample/poi/SearchPoiActivity.java) // [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/poi/SearchPoiActivity.kt)

Search through the list of POIs contained in your building. Note that at least one POI must be present in your building in order for this sample to work.

#### Usage:
Click the floating action button in the bottom right corner to open up a dialog that lists all the POIs in a scrollable list. Either scroll through the list or type in the search field to find specific POI matches in the list. Click the desired POI and the map will pan to that POI's location.

### Routing Between POIs
Code Samples:  [Java](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/java/src/main/java/com/phunware/java/sample/routing/RoutingActivity.java) // [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/routing/RoutingActivity.kt)

Route between two POIs or from your current location to a POI (if you have a bluedot). Note that for this sample to work you will need to connect your POIs with segments in the MaaS portal.

#### Usage:
Click the floating action button in the bottom right corner to open up a routing dialog that allows you to select a start and end point for your route. If you have acquired a bluedot, the start field will be automatically populated with your current location. Click `ROUTE` to display the path between the selected points (if one exists)

### Location Sharing
Code Samples:  [Java](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/java/src/main/java/com/phunware/java/sample/location/LocationSharingActivity.java) // [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/master/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/location/LocationSharingActivity.kt)

Show your current location in a building as well as others in the same building.

#### Usage:
Open this sample two or more devices (iOS or Android) to view other user's locations. Note that the samples must be configured to point at the same building and must both be open simultaneously to work properly. You can click the `SET USER INFO` button in the top right corner to create a friendly name and device type that other users will see.

### Voice Prompt
Code Samples:  [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/sample_code_updates/Samples/VoicePrompt.md))

Read route instruction aloud to the user as they swipe through the route instructions or as they traverse them with indoor location.

### Walk Time
Code Samples:  [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/sample_code_updates/Samples/WalkTime.md))

Display walk time at the bottom of map.

### Off Route
Code Samples:  [Kotlin](https://github.com/phunware/maas-mapping-android-sdk/blob/sample_code_updates/Samples/OffRouteAlerts.md))

Monitor the users location updates and alert the user if they deviated from the route using a predetermined distance and time threshold.

## Privacy

### You understand and consent to Phunware’s Privacy Policy located at www.phunware.com/privacy. If your use of Phunware’s software requires a Privacy Policy of your own, you also agree to include the terms of Phunware’s Privacy Policy in your Privacy Policy to your end users.

Use of this software requires review and acceptance of our terms and conditions for developer use located at http://www.phunware.com/terms/
