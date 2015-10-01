#MaaS Mapping SDK for Android

[Android MaaS Mapping Documentation](http://phunware.github.io/maas-mapping-android-sdk/)
=======
**Version 2.5.1**
________________


##Overview
This is Phunware's Android SDK for the Mapping module. Visit http://maas.phunware.com/ for more details and to sign up.

PWMapKit is a comprehensive indoor mapping and wayfinding SDK that allows easy integration with Phunware's indoor maps and location-based services.


###Build requirements
* Android SDK 4.0+ (API level 14) or above
* Latest MaaS Core
* Latest MaaS Location
* AndroidSVG 1.2.1
* Picasso 2.3.2
* Disklrucache 2.0.2

##Prerequisites
The sample will show a building and its points of interest in the main activity. 
Config resource files are in different directories; for example, use src/main for dev environment and use src/stage for stage.

In order to communicate with APIs, replace the APP_ID, APP_ACCESS_KEY, APP_SIGNATURE_KEY and APP_ENCRYPTION_KEY in res/values/strings.xml. Those values can be found in the MaaS portal. 
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

####Life Cycle
Users of this class must forward some life cycle methods from the activity or fragment containing this view to the corresponding methods in this class. In particular, the following methods must be forwarded:

* `onCreate(android.app.Activity, android.os.Bundle)`
* `onSaveInstanceState(android.os.Bundle)`
* `onDestroy(android.app.Activity)`
* `onLowMemory()`

####Adding Indoor Maps to a Activity
Mapping SDK provides `PwDefaultMappingFragment`——a default implementation of PwMappingFragment. It creates a map fragment and displays indoor maps, their markers and routing between points.

Define a layout in activity_main.xml:
```XML
<fragment xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/map"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:name="com.phunware.mapping.library.ui.PwDefaultMappingFragment" />
```
In the MainActivity.java:
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
Update your res/values/integers.xml. To show these, replace `BUILDING_ID` and `INITIAL_FLOOR` with your IDs in res/values/integers.xml.    
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
Get building data for the map with `PwMappingModule.getBuildingDataByIdInBackground(context, long, PwOnBuildingDataListener)`. Building data contains all of the metadata for a `PwBuilding`, its `PwFloor`s and each level of detail (LOD) for the floors. LODs are referred to as `PwFloorResource`s. A `PwMapView` uses this data to draw a map and to otherwise function.

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
Or you can use callbacks to get building data. `PwOnBuildingDataLoadedCallback` provides callback functions that call when `PwBuilding` loads successfully or fails.    

####Point of Interest Data
Get a list of points of interest (POI) with `getPOIDataInBackground(context, long, PwOnPOIDataListener)`. Once this list is given to a `PwMapView`, it can draw points on the map when relevant. This means that if a building has multiple floors, the map will only display the POIs that are on the current floor. POIs can also have a minimum zoom level specified so that they will show only once that zoom level has been reached.

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
After you call `setupMapData`, you can get all points for the current building by `getBuildingPoints`.

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
* `PwMapOverlayManagerBuilder.pwSnapToRouteCallback(PwOnSnapToRouteCallback)` registers a callback that be called once "Snap to route" started or stopped.
* `PwMapOverlayManagerBuilder.pwOnRouteStepChanged(PwOnRouteStepChangedCallback)` registers a callback that be called once routeStep is changed.
* `PwMapOverlayManagerBuilder.pwOnManeuverChanged(PwOnManeuverChangedCallback)` registers a callback that be called once maneuver is changed.


##Blue Dot
A user's location in a venue can be retrieved with `MyLocationLayer`. To use this layer, set showMyLocation to true and pass the PwLocationProvider into the onBuildingRetrievedSuccessful callback. A blue dot representing the end-user's location will be displayed on the map automatically, if available.

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

To receive a callback once building data has been retrieved successfully, pass the location provider in requestLocationUpdates:
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
The data model for routes is limited to a PWRoute object which is composed of one or more PWRouteStep objects. Each step represents a series of points on the route graph for a single building floor. It is possible to have multiple steps for the same floor, they just cannot be in succession.Each time the user needs to be asked to turn or to continue straight is not really a route step. It's more of a maneuver. Each maneuver has a relative direction (bear left, turn right, go straight, etc.), a distance (optional) and a sub-sequence of points.
The `PwMappingFragment` can display routes by using a `PwMapRoute`. Routes can be a path between any two points that are defined in the MaaS portal. A route can go from a point of interest (POI) to a point of interest, from any waypoint to a POI or from "My Location" to a POI.

The `PwMappingFragment` can display routes by using a `PwDirections` and `pwBuildingmapManager`. Route can be a path between any PwPoint and position on the map. A route can go from a point of interest (POI) to a point of interest, from any waypoint to a POI or from "My Location" to a POI.

Get data for a route (if available) with `PwDirections#calculate(PwDirectionsCalculateCallback)`.PwDirections default constructor will expect startPoiny,EndPoint and PwDirectionsOptions object `PwDirectionRequest(PwPoint start,PwPoint end,PwDirectionsOptions options)`. PwDirectionsOptions will have the methods to set route specifications like accessibility,points that need to be excluded from route calculation etc. start and end points can be either the current location or a POI/flatMarker. 

An example of retrieving and using route data:

```java
PwDirectionsOptions options = new PwDirectionsOptions();
options.setRequireAccessibleRoutes(isAccessible);
PwDirectionsRequest request = new PwDirectionsRequest(startItem, endItem, options);
PwDirections directions = new PwDirections(request);
directions.calculate(new PwDirectionsCalculateCallback() {
      @Override
      public void onSuccess(PwDirectionsResponse response) {
          mPwBuildingMapManager.plotRoute(response.getRoutes());
         }
      @Override
      public void onFailure(int errorCode, String errorMessage) {
         Toast.makeText(getActivity().getApplicationContext(), "Route calculation error: " + errorMessage, Toast.LENGTH_LONG).show();
        }
   });
}
});
```

###TURN-BY_TURN
Indoor turn-by-turn directions in our Mapping, Navigation, and Wayfinding can be enabled  using PwBuildingMapManager . PWBuildMapManager  maintains turn-by-turn state with a property called currentManeuver that is handled as PwBuildingmapManager.setCurrentManeuver(RouteManeuver).

 * When a route is plotted , no maneuver is used by default. 
 * In order to use turn-by-turn, the developer must set the currentManeuver and call for plotManeuver() method from PwBuildingMapManager . By default plotManeuver will use the first maneuver from CurrentRouteStep to desiplay.
 * if one of the user tracking modes is used ,then the maneuver is set immediately and will be changed automatically as the user moves.
 * Without user tracking, the maneuver is only displayed.

```java
PwDirectionsRequest request = new PwDirectionsRequest(startItem, endItem, options);
 PwDirections directions = new PwDirections(request);
 directions.calculate(new PwDirectionsCalculateCallback() {
       @Override
       public void onSuccess(PwDirectionsResponse response) {
             mPwBuildingMapManager.plotRoute(response.getRoutes());
            //Check if the Maneuvers array is not empty or null to call plotRouteManeuverOverlay
             if (mPwBuildingMapManager.getCurrentRoute().getManeuvers() != null && (!mPwBuildingMapManager.getCurrentRoute().getManeuvers().isEmpty())) {
            //set the default maneuver to display on the Route .By default it will be the first maneuver in currentRoute.
			mPwBuildingMapManager.setCurrentManeuver(maneuver);
			 mPwBuildingMapManager.plotManeuver();
                    }
            }
            @Override
            public void onFailure(int errorCode, String errorMessage) {
                Toast.makeText(getActivity().getApplicationContext(), "Route calculation error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
     }
});
```
in addition to this we have added two new callbacks which get trigerred after the maneuver and step are changed.These two call backs will help developer to handle post cleanup/UI work upon change of maneuver/routestep.

```java

 class MappingSampleFragment extends PwMappingFragment implementsPWOnManeuverChangedCallBack,PWOnRouteStepChangedCallBack
{
 
//add callbacks to PwBuildingManage to listen for the change events.
mPwBuildingMapManager = builder.buildingId(mCurrentBuilding)
						.........
						.pwOnManeuverChangedCallBack(this)
						.PwOnRouteStepChangedCallback(this)
						.build();
@Override
public void onManeuverChanged(PWRouteManeuver maneuver)
{
   //call to Update if any application UI is there or handle maneuver post change work.
}

@Override
public void onRouteStepChanged(RouteStep step)
{
//call to Update UI or handle RouteStep post change work.    
Toast.makeText(getActivity().getApplicationContext(),"changed routeStep is called",Toast.LENGTH_LONG).show();
}
 
}

```

###Route Snapping Tolerance
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
Routes are comprised of segments. In the Mapping SDK, a segment is a path between two points that are marked as an exit or a portal. Use the methods `getPreviousFloorId()` and `getNextFloorId()` to get the floorId on a `PwMapRoute`, use `showFloor` on `PwBuildingMapManager` to show a highlighted path along the route. The highlighted path will progress to the next segment if one exists, or the previous segment if one exists.


Attribution
-----------
MaaS Mapping uses the following third party components. 

| Component     | Description   | License  |
| ------------- |:-------------:| -----:|
| [Picasso](https://github.com/square/picasso)      | A powerful image downloading and caching library for Android      |   [Apache 2.0](https://github.com/square/picasso/blob/master/LICENSE.txt) |
| [AndroidSVG](https://code.google.com/p/androidsvg/)      | A SVG parser and renderer for Android      |   [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0) |
