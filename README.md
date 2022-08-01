# Phunware Mapping SDK for Android

[![Nexus](https://img.shields.io/nexus/r/com.phunware.mapping/mapping?color=brightgreen&server=https%3A%2F%2Fnexus.phunware.com)](https://nexus.phunware.com/content/groups/public/com/phunware/mapping/mapping/)

Phunware's Mapping SDK for Android. Visit https://www.phunware.com/ for more information or [sign into the MaaS Portal](http://maas.phunware.com/) to set up your venue.

### Requirements
* minSdk 23.
* AndroidX.

### Download
Add the following repository to your top level `build.gradle` file.
 ```groovy
repositories {
    maven {
            url "https://nexus.phunware.com/content/groups/public/"
        }
}
 ```

 Add the following dependency to your app level `build.gradle` file.
```groovy
dependencies {
    implementation "com.phunware.mapping:mapping:<version>"
}
```

### Android project setup
##### Keys
To use any of the Phunware MaaS SDKs you'll need to add the following entries to your AndroidManifest.xml, making sure to replace the `value` properties with your actual App ID and Access Key:

``` xml
<meta-data
    android:name="com.phunware.maas.APPLICATION_ID"
    android:value="YOUR_APP_ID"/>

<meta-data
    android:name="com.phunware.maas.ACCESS_KEY"
    android:value="YOUR_ACCESS_KEY"/>
```

##### Permissions
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission
        android:name="android.permission.BLUETOOTH"
        android:maxSdkVersion="30" />
<uses-permission
        android:name="android.permission.BLUETOOTH_ADMIN"
        android:maxSdkVersion="30" />
<!-- Optional: Derive physical location updates when app is not visible -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<!-- Optional: Cache floor map tiles to external cache if internal cache is unavailable -->
<uses-permission
        android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />
```

### Usage
#### Creating the Map Manager
The entry point of communication with the Mapping SDK is through the Phunware Map Manager.
Here's how you can obtain an instance of the Map Manager:
```kotlin
mapManager = PhunwareMapManager.create(this)
```
We strongly recommend that you hold your Map Manager instance in your Application class, so that it can survive configuration changes like orientation changes.

#### Adding the Map Fragment
To integrate the Phunware Map in your app, add the map fragment in your desired screen's XML file:
```xml
<fragment
    android:id="@+id/map"
    android:name="com.phunware.mapping.SupportMapFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

Get the map fragment instance in your host Activity or Fragment by calling:
```kotlin
val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
```

#### Loading the Map Fragment
Load the map on the fragment by calling `getPhunwareMapAsync()`. You'll need to provide an implementation of `OnPhunwareMapReadyCallback` as a parameter:
```kotlin
mapFragment.getPhunwareMapAsync(callback: OnPhunwareMapReadyCallback)
```

Your `OnPhunwareMapReadyCallback` implementation will require you to override `onPhunwareMapReady()`. When this method is called, it means your map instance is ready for use.
```kotlin
override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
}
```

#### Linking the Map with the Map Manager
In order to perform operations on the map through the Map Manager, like adding a building, we need to link the Map Manager with the Map we just loaded. You should do so inside your `onPhunwareMapReady()`:
```kotlin
override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
    mapManager.setPhunwareMap(phunwareMap)
}
```

#### Adding a Building
Now that the Map Manager has a Map instance that's visible on your screen, you can add your building. You should do so inside the same callback. For example:
```kotlin
override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
    mapManager.setPhunwareMap(phunwareMap)
    mapManager.addBuilding(buildingId.toLong(), object : Callback<Building> {
        override fun onSuccess(building: Building) {
            // Handle loaded Building.
        }
        
        override fun onFailure(throwable: Throwable) {
            // Handle failure.
        }
    })
}
```

#### Enabling real time location on the Map
In order to see your location on the map, you need to integrate with [Phunware's Location SDK](https://github.com/phunware/maas-location-android-sdk).
To enable your location, simply create a `PwManagedLocationProvider` and pass it to the Map Manager along with the building you just added to the map. Then, enable location on the Map Manager by setting `mapManager.isMyLocationEnabled` to `true`:
```kotlin
mapManager.addBuilding(buildingId.toLong(), object : Callback<Building> {
    override fun onSuccess(building: Building) {
        val provider = PwManagedLocationProvider(application, building.id, null)
        mapManager.setLocationProvider(managedProvider, building)
        mapManager.isMyLocationEnabled = true
    }
        
    override fun onFailure(throwable: Throwable) {
        // Handle failure.
    }
})
```

You're all set to see your building on the map with your real time location.
You can do much more with the Mapping SDK. For more information, check the Samples in this repository.

### Privacy
You understand and consent to Phunware’s Privacy Policy located at www.phunware.com/privacy. If your use of Phunware’s software requires a Privacy Policy of your own, you also agree to include the terms of Phunware’s Privacy Policy in your Privacy Policy to your end users.

### Terms
Use of this software requires review and acceptance of our terms and conditions for developer use located at http://www.phunware.com/terms/
