#MaaS Mapping SDK for Android

Version 2.0.6

##Overview
MaaS Mapping is an all-inclusive Android SDK for Mapping, Blue Dot and Navigation services provided by Phunware. Visit http://maas.phunware.com/ for more details and to sign up.


###Documentation

MaaS Mapping documentation is included in the Docs folder in the repository as both HTML and as a .jar. You can also find the latest documentation here: http://phunware.github.io/maas-mapping-android-sdk/


###Build requirements
* Android SDK 4.0+ (API level 14) or above
* Latest MaaS Core
* Latest MaaS Mapping (MaaSMapping.jar and MaaSMappingLibrary.aar)
* OkHttp 1.6.0
* OkHttp-urlconnection 1.6.0
* AndroidSVG 1.2.1
* Picasso 2.3.4
* Android-maps-utils 0.3
* Androidsvg 1.2.1
* Retrofit 1.6.0


##Prerequisites
The sample will show a building and it's points of interest in the main activity. 
Config resource files are in different directories, for example: use src/main for dev environment, use src/stage for stage.

In order to communicate with APIs, replace the APP_ID, APP_ACCESS_KEY, APP_SIGNATURE_KEY and APP_ENCRYPTION_KEY in res/values/strings.xml, those values can be found in MaaS portal. 
```xml
    <string name="app_id">APP_ID</string>
    <string name="app_access_key">APP_ACCESS_KEY</string>
    <string name="app_signature_key">APP_SIGNATURE_KEY</string>
    <string name="app_encryption_key">APP_ENCRYPTION_KEY</string>
```

Install the module in the `Application` `onCreate` method before registering keys. For example:
``` Java
@Override
public void onCreate() {
    super.onCreate();
    /* Other Code */
    PwCoreSession.getInstance().installModules(PwMappingModule.getInstance(), ...);
    /* Other code */
}
```

##Mapping
Mapping starts with the `PwMappingFragment` class. It subclasses SupportMapFragment to provide a convenient interface that downloads, stores and displays indoor maps and associated points of interest. 

####Life cycle
Users of this class must forward some life cycle methods from the activity or fragment containing this view to the corresponding methods in this class. In particular the following methods must be forwarded:

* `onCreate(android.app.Activity, android.os.Bundle)`
* `onSaveInstanceState(android.os.Bundle)`
* `onDestroy(android.app.Activity)`
* `onLowMemory()`

####Adding Indoor Maps to a Activity
Mapping SDK provides `PwDefaultMappingFragment` a default implementation of PwMappingFragment, it creates a map fragment, displaying indoor map and markers, and routing between points.

Define a layout: activity_main.xml
```XML
<fragment xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/map"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:name="com.phunware.mapping.library.ui.PwDefaultMappingFragment" />
```
In the MainActivity.java
```Java
public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
    }
}
```
Update you res/values/integers.xml,

To show these replace `BUILDING_ID` and `INITIAL_FLOOR` with your ids in res/values/integers.xml.    
```xml
    <integer-array name="building_id">
        <item>BUILDING_ID</item>
    </integer-array>

    <integer-array name="initial_floor">
        <item>INITIAL_FLOOR</item>
    </integer-array>

    <integer name="minimum_floor_zoom_level"><!-- Minimum floor zoom level --></integer>
    <integer name="minimum_marker_zoom_level"><!-- Minimum marker zoom level --></integer>
```


####Building Data
Get building data for the map with `PwMappingModule.getBuildingDataByIdInBackground(context, long, PwOnBuildingDataListener)`. Building data contains all of the meta data for a `PwBuilding`, it's `PwFloor`s, and each LOD (level of detail) for the floors. LODs are referred to as `PwFloorResource`s. A `PwMapView` uses this data to draw a map and otherwise to function.

Once building data is obtained, use the map view's method `setMapData(pwBuilding)` to pass the data to the map. It will begin loading assets and resources immediately. 

An example of retrieving and using building data:
```Java
PwMappingModule.getInstance().getBuildingDataByIdInBackground(this, BUILDING_ID, new PwOnBuildingDataListener() {
    @Override
    public void onSuccess(PwBuilding pwBuilding) {
        if (pwBuilding != null) {
            // Building data exists!
            pwMapView.setMapData(pwBuilding);
        } else {
            // No building data found or a network error occurred.
            Toast.makeText(context, "Error loading building data.", Toast.LENGTH_SHORT).show();
        }
    }
});
```
Or you can use callbacks to get building data. `PwOnBuildingDataLoadedCallback` provides callback functions, that calls when `PwBuilding` load success or fail.    

####Point of Interest Data
Get a list Points of Interest (POI) with `getPOIDataInBackground(context, long, PwOnPOIDataListener)`. Once this list is given to a `PwMapView` it can draw points on the map when relevant. This means that if a building has multiple floors then the map will only display the POIs that are on the current floor. POIs can also have a minimum zoom level specified so that they will show only once that zoom level has been reached.

Once POI data is obtained, use the map view's method `setPOIList(pois)` to pass the data to the map.

An example of retrieving and using POI data:
```Java
PwMappingModule.getInstance().getPOIDataInBackground(this, BUILDING_ID, new PwOnPOIDataListener() {
    @Override
    public void onSuccess(List<PwPoint> pois) {
        if (pois != null) {
            pwMapView.setPOIList(pois);
        } else {
            Toast.makeText(context, "Error loading poi data.", Toast.LENGTH_SHORT).show();
        }
    }
});
```
After you call `setupMapData`, you can get all points for current building by `getBuildingPoints`.

####Event Callbacks
This view provides the option to set callbacks for certain events:

* `PwMapOverlayManagerBuilder.pwOnFloorChangedCallback(PwOnFloorChangedCallback)` registers a callback that be called once the map has completed the floor change.
* `PwMapOverlayManagerBuilder.pwOnMapDataLoadedCallback(PwOnMapDataLoadedCallback)` registers a callback that be called once the map data loaded.
* `PwMapOverlayManagerBuilder.pwOnBuildingDataLoadedCallback(PwOnBuildingDataLoadedCallback)` registers a callback that be called once the building data loaded.
* `PwMapOverlayManagerBuilder.pwSvgDownloadCallback(PwSvgDownloadCallback)` registers a callback that be called once the SVG url are download.

##Blue Dot
A user's location in a venue can be retrieved with `MyLocationLayer`. To use this layer should set showMyLocation to true, and pass the PwLocationProvider into onBuildingRetrievedSuccessful callback. A Blue Dot will be displayed on the map automatically, if available, representing the end-user's location.

An example of using `MyLocationLayer`:
```Java
    new PwMapOverlayManagerBuilder(getActivity().getApplicationContext(), map)
        .buildingId(getResources().getInteger(R.integer.building_id))
        .initialFloor(getResources().getInteger(R.integer.initial_floor))
        .minimumFloorZoomLevel(MINIMUM_FLOOR_ZOOM_LEVEL)
        .minimumMarkerZoomLevel(MINIMUM_MARKER_ZOOM_LEVEL)
        .routeCallback(getPwRouteCallback())
        .showMyLocation(true)
        .floorChangedCallback(this)
        .pwOnBuildingRetrievedCallback(this)
        .mapLoadedCallback(this)
        .build();
```

Receive a callback once building data retrieved successfully, pass the location provider in requestLocationUpdates:
```java
    @Override
    public void onBuildingRetrievedSuccessful(PwBuilding pwBuilding) {
        mPwBuildingMapManager.requestLocationUpdates(getActivity().getApplicationContext(), LocationProviderFactory.getInstance(getActivity().getApplicationContext()).createLocationProvider(pwBuilding));
    }
```

##Routing
The `PwMappingFragment` can display routes by using a `PwMapRoute`. Routes can be a path between any two points that are defined in the MaaS Portal. A route can go from a Point of Interest (POI) to a Point of Interest, or from any way-point to a POI, or "My Location" to POI.

Get data for a route (if available) with `PwMappingModule#getRoute(context, long, long, boolean)` or one of it's overloaded methods. There are also methods to help get route data in the background.
There is a special method to help get a route from a point on the map; `getRouteFromLocation(context, float, float, long, long, boolean)`. This will find a route with the starting point near an `X` and `Y` coordinate. The actual starting point depends on the closest way-point that is set on the MaaS portal. 
The last boolean is flag for accessible route, pass true if want to get accessible route.

An example of retrieving and using route data:
```java
// Note that the start and end point Ids should come from a call to PwPoint#getId()
PwMappingModule.getInstance().getRouteInBackground(this, startingPointId, endingPointId, new PwRouteCallback() {
    @Override
    public void onSuccess(ArrayList<PwRoute> routes) {
        //We currently only get bag one route, so use it. Figure out how to handle multiple routes in the future.
        ArrayList<PwPoint> routePoints = routes.get(0).getPoints();
        mMapRoute = new MapRoute(routePoints);

        // Add route, then toggle markers
        mMapRoute.addRoute(routePoints, mPwBuilding, mGoogleMap, mRouteColor, mRouteStrokeWidth);
        toggleMarkers(mCurrentZoom >= mMinimumMarkerZoomLevel, mCurrentFloorId);

        // Animate Camera back to starting point.
        final PwRoute startRoute = routes.get(0);
        if (!startRoute.getPoints().isEmpty()) {
            final PwPoint startPoint = startRoute.getPoints().get(0);
            if (startPoint != null) {
                changeCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(startPoint.getLocation(), mCurrentZoom))); // DEFAULT_CAMERA_ZOOM
            }
        }
        if (mPwRouteCallback != null) {
            mPwRouteCallback.onSuccess(routes);
        }
    }

    @Override
    public void onFail(int errorCode, String errorMessage) {
        Toast.makeText(this, errorCode + ": " + errorMessage, Toast.LENGTH_SHORT).show();
    }
}, false);
```


Routes are comprised of segments.


----------


ts. In the Mapping SDK, a segment is a path between two points that are marked as an exit or a portal. Use the methods `getPreviousFloorId()` and `getNextFloorId()` to get the floorId on a `PwMapRoute`, use `showFloor` on `PwBuildingMapManager` to show a highlighted path along the route. The highlighted path will progress to the next segment if one exists, or the previous segment if one exists.

Attribution
-----------
MaaS Mapping uses the following 3rd party components. 

| Component     | Description   | License  |
| ------------- |:-------------:| -----:|
| [Picasso](https://github.com/square/picasso)      | A powerful image downloading and caching library for Android      |   [Apache 2.0](https://github.com/square/picasso/blob/master/LICENSE.txt) |
| [AndroidSVG](https://code.google.com/p/androidsvg/)      | A SVG parser and renderer for Android      |   [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0) |

