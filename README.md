#MaaS Mapping

[Android MaaS Mapping Documentation](http://phunware.github.io/maas-mapping-android-sdk/)

**v 2.3.0**
________________

##Overview
MaaS Mapping is an all-inclusive Android SDK for Mapping, Blue Dot, and Navigation services provided by Phunware. 
###Build requirements
* Latest MaaS Core
* Latest MaaS Location
* AndroidSVG 1.2.1
* Picasso 2.3.2

##Prerequisites
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
```XML
    <integer name="building_id"><!-- PwBuilding ID from MAAS --></integer>
    <integer name="initial_floor"><!-- PwFloor ID from MAAS --></integer>

    <integer name="minimum_floor_zoom_level"><!-- Minimum floor zoom level --></integer>
    <integer name="minimum_marker_zoom_level"><!-- Minimum marker zoom level --></integer>
```


####Building Data
Get building data for the map with `PwMappingModule.getBuildingData(context, long, PwOnBuildingDataListener)`. Building data contains all of the meta data for a `PwBuilding`, it's `PwFloor`s, and each LOD (level of detail) for the floors. LODs are referred to as `PwFloorResource`s. A `PwMapView` uses this data to draw a map and otherwise to function.

Once building data is obtained, use the map view's method `setMapData(pwBuilding)` to pass the data to the map. It will begin loading assets and resources immediately. 

An example of retrieving and using building data:
```Java
PwMappingModule.getInstance().getBuildingData(this, BUILDING_ID, new PwOnBuildingDataListener() {
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
Get a list Points of Interest (POI) with `getPOIs(context, long, PwOnPOIDataListener)`. Once this list is given to a `PwMapView` it can draw points on the map when relevant. This means that if a building has multiple floors then the map will only display the POIs that are on the current floor. POIs can also have a minimum zoom level specified so that they will show only once that zoom level has been reached.

Once POI data is obtained, use the map view's method `setPOIList(pois)` to pass the data to the map.

An example of retrieving and using POI data:
```Java
PwMappingModule.getInstance().getPOIs(this, BUILDING_ID, new PwOnPOIDataListener() {
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

####Point of Interest Type
Get all type of Points of Interest (POI) with `getPOITypes(context, PwOnPOITypesDownloadListener)`. Once the list of Points of Interest (POI) type is downloaded, it will call `onSuccess(SparseArray<PwPointType>)` method in PwOnPointOfInterestTypesDownloadListener, otherwise it will call onFailed instead.

An example of retrieving and using POI type data:
```Java
PwMappingModule.getInstance().retrievePointsOfInterestTypes(getActivity().getApplicationContext(), new PwOnPointOfInterestTypesDownloadListener() {
    @Override
    public void onSuccess(SparseArray<PwPointType> poiTypes) {
        Toast.makeText(context, "POI types: " + poiTypes, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailed() {
        Toast.makeText(context, R.string.text_retrieve_poi_types_failed, Toast.LENGTH_SHORT).show();
    }
});
```

####Event Callbacks
This view provides the option to set callbacks for certain events:

* `PwMapOverlayManagerBuilder.pwOnFloorChangedCallback(PwOnFloorChangedCallback)` registers a callback that be called once the map has completed the floor change.
* `PwMapOverlayManagerBuilder.pwOnMapDataLoadedCallback(PwOnMapDataLoadedCallback)` registers a callback that be called once the map data loaded.
* `PwMapOverlayManagerBuilder.pwOnBuildingDataLoadedCallback(PwOnBuildingDataLoadedCallback)` registers a callback that be called once the building data loaded.
* `PwMapOverlayManagerBuilder.pwOnBuildingPOIDataLoadedCallback(PwOnBuildingPOIDataLoadedCallback)` registers a callback that be called once the building POI data loaded.
* `PwMapOverlayManagerBuilder.pwSvgDownloadCallback(PwSvgDownloadCallback)` registers a callback that be called once the SVG url are download.
* `PwMapOverlayManagerBuilder.pwSnapToRouteCallback(PwSnapToRouteCallback)` registers a callback that be called once "Snap to route" started or stopped.

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

###Enable/Disable Blue Dot smoothing

Blue Dot is no longer as simple as moving an marker to a different coordinate when a location provider sends in a location update.  In order to provide a more enjoyable experience for users, we manipulate the user location marker in such a manner as to smoothly animate it as the user moves.  

The Blue Dot smoothing is enabled by default, but you can change it on the fly.

An example to disable Blue Dot smoothing:
```java
    mPwBuildingMapManager.setBlueDotSmoothingEnabled(false);
```

An example to enablue Blue Dot smoothing:
```java
    mPwBuildingMapManager.setBlueDotSmoothingEnabled(true);
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

###
When in routing mode, the user's location can be forced to a route line as long as the reported, averaged or interpolated user location is within range of the route line based on some multiple of the horizontal accuracy.  The tolerance factor that is multiplied to the horizontal accuracy is configurable, but restricted to values of `PwRouteSnappingTolerance.Off`(0), `PwRouteSnappingTolerance.Normal(1.0)`, `PwRouteSnappingTolerance.Medium(1.5)` and `PwRouteSnappingTolerance.Large(2.0)`.

An example to turn it off:
```java
    mPwBuildingMapManager.setRouteSnappingTolerance(PwRouteSnappingTolerance.Off);
```

An example to tune the tolerance, you can also change it to `Medium` or `Large`:
```java
    mPwBuildingMapManager.setRouteSnappingTolerance(PwRouteSnappingTolerance.Normal);
```


####Marker
Get a list of `PwBuildingMarker` there are two ways:
* In subclass of `PwMappingFragment`, `getPwMap()` will returning the `PwMap` object, use `getBuildingMarkers(floorId)` will returning a list of `PwBuildingMarker` on the given floor.
* Implement `PwOnBuildingPOIDataLoadedCallback` interface, when the building POI are loaded, the callback function `onBuildingPOILoaded(List<PwPoint>)` will be called. You can use `getBuildingMarkerByPointId` in `PwMap` with the id of `PwPoint`.

For add and remove marker from map, it's the same way Google map does.
To remove the marker from map with `remove()` in `PwBuildingMarker`.

To add the marker back to map with `getPwMap().addMarker(PwMarkerOptions)`, you can get the `PwMarkerOptions` with `getMarkerOptions` in `PwBuildingMarker`.

After add or remove marker from map, you should force refresh the map with `getPwMap().invalidate();`

```java
// Implement `PwOnBuildingPOIDataLoadedCallback` interface
public class MappingSampleFragment extends PwMappingFragment implements PwOnBuildingPOIDataLoadedCallback {

	// Holds all PwPoints instances
	private List<PwPoint> mPwPoints;
	
	// Store points into mPwPoints
    @Override
    public void onBuildingPOILoaded(final List<PwPoint> points) {
        try {
            Toast.makeText(getActivity().getApplicationContext(), points.size() + " POI loaded", Toast.LENGTH_SHORT).show();
            this.mPwPoints = points;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	// Remove building marker from map
    private void removePOIs() {
        
        int size = mPwPoints.size();
        PwPoint point;
        for (int i = 0; i <size; i++) {
            point = mPwPoints.get(i);
            if (point.getPoiType() == 5000) { // Business Facility
                PwBuildingMarker marker = mPwBuildingMapManager.getBuildingMarkerFromPoint(point.getId());
                mBuildingMarkers.add(marker);
                marker.remove();
            }
        }

        // Force redraw the map
        getPwMap().invalidate();
    }
    
    // Add building marker to map
    private void addPOIs() {
        int size = mBuildingMarkers.size();
        for (int i = 0; i < size; i++) {
            final PwBuildingMarker buildingMarker = mBuildingMarkers.get(i);
            getPwMap().addMarker(buildingMarker.getMarkerOptions());
        }

        // Force redraw the map
        getPwMap().invalidate();
    }
}
```



----------


ts. In the Mapping SDK, a segment is a path between two points that are marked as an exit or a portal. Use the methods `getPreviousFloorId()` and `getNextFloorId()` to get the floorId on a `PwMapRoute`, use `showFloor` on `PwBuildingMapManager` to show a highlighted path along the route. The highlighted path will progress to the next segment if one exists, or the previous segment if one exists.

Attribution
-----------
MaaS Mapping uses the following 3rd party components. 

| Component     | Description   | License  |
| ------------- |:-------------:| -----:|
| [Picasso](https://github.com/square/picasso)      | A powerful image downloading and caching library for Android      |   [Apache 2.0](https://github.com/square/picasso/blob/master/LICENSE.txt) |
| [AndroidSVG](https://code.google.com/p/androidsvg/)      | A SVG parser and renderer for Android      |   [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0) |
