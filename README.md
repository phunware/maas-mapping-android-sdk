MaaSMapping Android SDK
================

Version 1.2.0

MaaS Mapping is an all-inclusive Android SDK for Mapping, Blue Dot and Navigation services provided by Phunware. Visit http://maas.phunware.com/ for more details and to sign up.



Requirements
------------

* Latest MaaS Core
* OkHttp 1.2.1
* AndroidSVG 2.1.1



Documentation 
--------------

MaaSMapping documentation is included in the Documents folder in the repository as both HTML and as a .jar. You can also find the latest documentation here: http://phunware.github.io/maas-mapping-android-sdk/



Prerequisites
-------------

Install the module in the `Application` `onCreate` method before registering keys. For example:
``` Java
@Override
public void onCreate() {
    super.onCreate();
    /* Other code */
    PwCoreSession.getInstance().installModules(PwAnalyticsModule.getInstance(), ...);
    /* Other code */
}
```



Mapping
----------

Mapping starts with the `PwMapView` class. It is a custom view that can be defined in an XML layout or in code. The view will not draw anything by default. However, it will respect the bounds it's given.

### Life Cycle
Users of this class must forward some life cycle methods from the activity or fragment containing this view to the corresponding methods in this class. In particular, the following methods must be forwarded:

* `onCreate(android.app.Activity, android.os.Bundle)`
* `onSaveInstanceState(android.os.Bundle)`
* `onDestroy(android.app.Activity)`
* `onLowMemory()`

### Building Data
Get building data for the map with `PwMappingModule.getBuildingDataByIdInBackground(context, long, PwOnBuildingDataListener)`. Building data contains all of the metadata for a `PwBuilding`, its `PwFloor`s and each Level of Detail (LOD) for the floors. LODs are referred to as `PwFloorResource`s. A `PwMapView` uses this data to draw a map and otherwise to function.

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
            // No building data found or a network error occured.
            Toast.makeText(this, "Error loading building data.", Toast.LENGTH_SHORT).show();
        }
    }
});
```

### Point of Interest Data
Get a list Points of Interest (POIs) with `getPOIDataInBackground(context, long, PwOnPOIDataListener)`. Once this list is given to a `PwMapView`, it can draw points on the map when relevant. This means that if a building has multiple floors, the map will only display the POIs that are located on the current floor. POIs can also have a minimum zoom level specified so that they will show only once that zoom level has been reached.

Once POI data is obtained, use the map view's method `setPOIList(pois)` to pass the data to the map.

An example of retreiving and using POI data:
```Java
PwMappingModule.getInstance().getPOIDataInBackground(this, BUILDING_ID, new PwOnPOIDataListener() {
    @Override
    public void onSuccess(List<PwPoint> pois) {
        if (pois != null) {
            pwMapView.setPOIList(pois);
        } else {
            Toast.makeText(SVGCanvas.this, "Error loading poi data.", Toast.LENGTH_SHORT).show();
        }
    }
});
```

### Event Callbacks
This view provides the option to set callbacks for certain events:

* `setOnMapViewStateChangedListener(PwOnMapViewStateChangedListener)` signals when the view has changed floors or zoom levels.
* `setOnPOIClickListener(PwOnPOIClickListener)` is called any time a POI is clicked.
* `setOnMapLoadCompleteListener(PwOnMapLoadCompleteListener)` is called once the first image has loaded and is drawn.

### Layers
Layers can be added to the map through `addLayer(PwMapViewLayer)` and removed with `removeLayer(PwMapViewLayer)`. Predefined layers include `PwRouteLayer` and `PwBlueDotLayer`. Custom layers can be added by using extending `PwMapViewLayer`. Some life cycle events are forwarded to layers. However, currently, the only way to restore state is by making sure layers are added again after calling `onCreate(activity, bundle)`. In `onDestroy(activity)`, layers are asked to save state and are then removed from the view.

#### PwMarkerLayer
`PwMarkerLayer` is a special layer that allows developers to add markers anywhere on the map. `PwMarker` objects can be added to or removed from this layer with `PwMarkerLayer#addMarker(pwMarker)` and `PwMarkerLayer#removeMarker(pwMarker)`, respectively.
An example of creating a `PwMarker`:

```Java
// Create a marker to show on top of a POI.
final PwMarker marker = new PwMarker(
        R.drawable.circle_marker,
        poi.getFloorId(),
        poi.x,
        poi.y);
// Calculate and set X and Y position offset for the marker.
final BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(marker.getDrawableResId());
marker.xOffset = -drawable.getBitmap().getWidth() / 2f;
marker.yOffset = -drawable.getBitmap().getHeight() / 2f;
mPwMarkerLayer.addMarker(marker);
```

It is suggested to calculate an X and Y offset for a marker in pixels. The offsets are 0 by default. Since any image can be set, it is up to the developer to set the appropriate offset. With no offset, the top left corner of the image will be drawn at the specified X and Y.



Blue Dot
----------------

A user's location in a venue can be retrieved with `PwBlueDotLayer`. This is a layer that should be added to a `PwMapView`. Once added, the layer manages polling for a location. A Blue Dot will be displayed on the map automatically to represent the end user's location (if available).

An example of using `PwBlueDotLayer`:
```Java
PwBlueDotLayer pwBlueDotLayer = new PwBlueDotLayer();

public void toggleBlueDot(final boolean enabled) {
    if(enabled) {
        pwMapView.addLayer(pwBlueDotLayer);
    }
    else /*disabled*/ {
        pwMapView.removeLayer(pwBlueDotLayer);
    }
}
```

Receive a callback once Blue Dot has been acquired:
```java
pwBlueDotLayer.setBlueDotListener(new PwBlueDotLayer.PwBlueDotListener() {
    @Override
    public void onBlueDotAvailable() {
        Toast.makeText(SVGCanvas.this, "Blue Dot is available!", Toast.LENGTH_SHORT).show();
    }
});
```



Navigation
------------
The `PwMapView` can display routes by using a `PwRouteLayer`. Routes can be a path between any two points that are defined in the MaaS portal. A route can go from a Point of Interest (POI) to a Point of Interest or from any waypoint to a POI.

Get data for a route (if available) with `PwMappingModule#getRoute(context, long, long)` or one of its overloaded methods. There are also methods to help get route data in the background.
There is a special method to help get a route from a point on the map: `getRouteFromLocation(context, float, float, long, long)`. This will find a route with the starting point near an `X` and `Y` coordinate. The actual starting point depends on the closest waypoint that is set in the MaaS portal.

An example of retrieving and using route data:
```java
// Note that the start and end point Ids should come from a call to PwPoint#getId()
PwMappingModule.getInstance().getRouteInBackground(this, startingPointId, endingPointId, new PwRouteCallback() {
    @Override
    public void onSuccess(ArrayList<PwRoute> routes) {
        // A list of potential routes may be returned. We're only interested in the first one.
        mRouteLayer = new PwRouteLayer(routes.get(0));
        pwMapView.addLayer(mRouteLayer);
    }

    @Override
    public void onFail(int errorCode, String errorMessage) {
        Toast.makeText(this, errorCode + ": " + errorMessage, Toast.LENGTH_SHORT).show();
    }
});
```

Routes are comprised of segments. In the Mapping SDK, a segment is a path between two points that are marked as an exit or a portal. Use the methods `nextRouteSegment()` and `previousRouteSegment()` on a `PwRouteLayer` to show a highlighted path along the route. The highlighted path will progress to the next segment if one exists, or the previous segment if one exists.



Attribution
-----------
MaaSMapping uses the following 3rd party components. 

| Component     | Description   | License  |
| ------------- |:-------------:| -----:|
| [OkHttp](https://github.com/square/okhttp)      | An HTTP & SPDY client for Android and Java applications. | [Apache 2.0](https://github.com/square/okhttp/blob/master/LICENSE.txt) |
| [Picasso](https://github.com/square/picasso)      | A powerful image downloading and caching library for Android.      |   [Apache 2.0](https://github.com/square/picasso/blob/master/LICENSE.txt) |
| [AndroidSVG](https://code.google.com/p/androidsvg/)      | A SVG parser and renderer for Android.      |   [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0) |
