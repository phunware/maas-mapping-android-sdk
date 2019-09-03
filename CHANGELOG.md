# Mapping SDK Changelog
## 3.9.4 (Tuesday, Sep 3rd, 2019)
#### Bug fixes / performance enhancements
* Updated android svg library to 1.4
* Updated bundles library to 1.1.4

## 3.9.3 (Friday, Aug 16th, 2019)
#### Bug fixes / performance enhancements
* Bug fix for OnManeuverChangedListener not getting invoked when walking backwards
* Memory Optimizations
* Added an API to perform cleanup on PhunwareMapManager

## 3.9.2 (Wednesday, Aug 7th, 2019)
#### Bug fixes / performance enhancements
* Performance improvements in map animation

## 3.9.1 (Wednesday, Jun 26th, 2019)
#### Bug fixes / performance enhancements
* Added API to retreive map marker object

## 3.9.0 (Tuesday, Apr 2nd, 2019)
#### Features
* Update for Location 3.7.0 compatibility
* Managed Compass -- an alternate heading experience for sites that suffer from inaccurate heading
* Ability to blend GPS with indoor location providers based on configuration in the MaaS portal
* Added support for one way escalators in Routing
* Ability to load buildings without attaching it to a map
* Support for attaching a loaded building to map
* Support for detaching buildings from map

## 3.8.1 (Thursday, Feb 21st, 2019)
#### Features
* Upgraded to Mapping v3.0 API

## 3.8.0 (Friday, Jan 4th, 2019)
#### Features
* Updated to Google Play Services 16.0.0
* Updated to Core 3.5.0
* Built with Android Support Library 28.0.0

## 3.7.1 (Thursday, Sept 27th, 2018)
#### Features
* Fixed crash if setLocationMode is never called on PhunwareMapManager

## 3.7.0 (Wednesday, Sept 19th, 2018)
#### Features
* Updated debug dot mechanism

#### Bug fixes / performance enhancements
* Various bug fixes

## 3.6.1 (Thursday, Aug 30th, 2018)
#### Bug fixes / performance enhancements
* Updated routing algorithm to include weighting for floor switches
* Minor fix for content provider
* Improved building load time by adding parallel downloading of floor resources

## 3.6.0 (Tuesday, Jul 31st, 2018)
#### Features
* Added APIs to change route line color and width

#### Bug fixes / performance enhancements
* Optimized map image load
* Various bug fixes

## 3.5.0 (Tuesday, Jun 5th, 2018)
#### Features
* fixed icon resizing
* Locate Me routing enhancements
* added setCacheFallbackTimeout() 
* added touch listener to MapView and MapFragment - See sample app for example

## 3.4.0 (Thursday, Apr 5th, 2018)
#### Features
* Updated to Google Play Services 11.8.0
* Added customization of route lines and stroke

## 3.3.0 (Thursday, Feb 22nd, 2018)
#### Features
* added JWT authentication

#### Bug fixes / performance enhancements
* fixed routing bug involving portals

#### Developer stuff
* Added additional logging for troubleshooting

## Version 3.2.1 (2018-1-10)

#### Bug fixes / performance enhancements
* metadata properly persistent in content provider
* various bug fixes

## Version 3.2.0 (2017-10-16)

#### Bug fixes / performance enhancements
* updated Google Play Services to 11.4.0
* honor isActive flag for POI display and routing
* added pressure change bluedot extinguish

## Version 3.1.3 (2017-10-03)

#### Bug fixes / performance enhancements
* fixed authorities name for content provider

## Version 3.1.2 (2017-09-11)

#### Bug fixes / performance enhancements
* added Location Sharing API
* added ContentProvider for map data
* Various bug fixes

## Version 3.1.1 (2017-03-28)

#### Bug fixes / performance enhancements
* Various bug fixes

## Version 3.1.0 (2017-01-30)

#### Bug fixes / performance enhancements
* Building bundles implemented
* Changed interface for setting location provider
* Updated to Core 3.0.3
* Various bug fixes

## Version 3.0.1 (2016-10-07)

#### Bug fixes / performance enhancements
* Updated Google Play Services to 9.6.1
* Updated to Core 3.0.2
* Various bug fixes

## Version 3.0.0 (2016-08-04)

#### Bug fixes / performance enhancements
* Complete re-write of Mapping SDK

## Version 2.5.0 (2015-08-31)

#### Bug fixes / performance enhancements
* Added support for turn-by-turn directions. Turn-by-turn maneuvers can be accessed by accessing the maneuvers property on a PWRoute object. You can plot a route maneuver on a PWMapView intance by calling setRouteManeuver: with a valid PWRouteManeuver object. All previous route behavior is still present and unaffected. Please see the turn-by-turn sample as an example of how to implement turn-by-turn.
 **NOTE**: You will need to plot a route maneuever in order to enter turn-by-turn mode.
* Route maneuvers switch automatically when location upates are available and the indoor user tracking mode is set to PWIndoorUserTrackingModeFollow or PWIndoorUserTrackingModeFollowWithHeading. You can manually set route maneuvers if desired but that will set the indoor user tracking mode to PWIndoorUserTrackingModeNone.
* Added new class, PWRouteManeuever. A route maneuver encapsulates information related to given maneuver such as turn direction, distance and other information.
* Route steps are now automatically selected by the SDK in response to user initiated floor changes or location updates. A new callback has been created when a route step changes.
* Added PwBuildingMapManager callback PwBuildingMapManager:onPwRouteStepChangedCallback:onRouteStepChanged. This method is called whenever the PWRouteStep being displayed by the map view changes.
* Added PwBuildingMapManager callback PwBuildingMapManager:onmaneuverchangedCallback:onManeuverChanged. This method is called whenever the PWRouteManeuver being displayed by the map changes.
* When registering a PWGPSLocationManager with the map view the location will now show on all floors regardless of whether or not there is a valid floorIDMapping match.
*  Updated MaaSLocation SDK to '1.2.0'
* Fixed Bug for getting route from  and to same floor number in different buildings.
* Added property to PWDirectionsOptions called excludedPointIdentifiers. Specify an array of point identifiers that you would like to exclude from routing. Please see PWDirectionsOptions header for more information.

## Version 2.4.0 (2015-04-10)

#### Bug fixes / performance enhancements
* Added options that allow routing from any point of interest, user location or dropped pin annotation to any other point of interest, user location or dropped pin marker (in other words, any type of route endpoint is now allowed on either end of the route).
* Added `setMarkerZoomLevel` method to `PwBuildingMapManager` for toggling POI zoom level.
* Added `setBlueDotSmoothingEnabled` method to `PwBuildingMapManager` for toggling blue dot.
* Added DiskLRUCache to cache SVG tiles, any tile will only be render once.
* Removed `onPreDraw` listeners, using `RenderThread` handle real time occlusion detection.
* Updated MaaSLocation SDK to '1.0.1'

## Version 2.3.0 (2015-02-02)

#### Bug fixes / performance enhancements
* Added new "blue dot smoothing" functionality to provide a better user location tracking experience.
* Added `setBlueDotSmoothingEnabled` method to `PwBuildingMapManager` for turning blue dot smoothing on and off.
* Added `setRouteSnappingTolerance` method to `PwBuildingMapManager` for turning off route snapping or setting a different tolerance.
* Added `retrievePointsOfInterestTypes` method to `PwMappingModule` for fetching all POI types.
* Added `pwOnBuildingPOIDataLoadedCallback` method to `PwMapOverlayManagerBuilder` for getting callback when POI are loaded.
* Added `remove` method to `PwBuildingMarker` for remove itself from PwMap.
* Added `addMarker` to `PwMap` for add a `PwBuildingMarker` to PwMap.
* Updated MaaSLocation SDK to '1.0.0'
* Bug fixes and performance enhancements

## Version 2.0.6 (2014-09-30)

#### Bug fixes / performance enhancements
* Merged JavaDoc for Mapping and MappingLibrary
* Fixed MappingLibrary-javadoc.jar and MappingLibrary-sources.jar
* Fixed building warnings
* Updated MaaSLocation SDK to '0.9.1'

## Version 2.0.5 (2014-09-22)

#### Bug fixes / performance enhancements
 * Migrating to use Google Maps v2 API from Google Play Services (4.x).
 * Added MaaS MappingLibrary module that is an Android library for wrapping support for Google Play Services (Google Maps) and MaaS Mapping SDK..
 * Added Location SDK as a dependency for MappingLibrary.
 * Add "Clear Cache" into Mapping Sample
 * Updated "minSdkVersion" to "10" since Google Play Services (4.x) only support API level 10 and above.
 * Updated version of Picasso to '2.3.4'
 * Updated MaaSCore to v1.3.7
 * Performance improvement, Refactor & Bug fixes

## Version 1.2.3 (2014-02-26)

#### Bug fixes / performance enhancements
 * Fixed builds to produce Java 6 compatible binaries using 'sourceCompatibility' and 'targetCompatibility' equal to '1.6'.
 * Requires MaaSCore v1.3.5

## Version 1.2.1 (2013-12-27)

#### Bug fixes / performance enhancements
 * Fixing bug around resetting the current floor. The entire view is reset now, not just the map.
 * Fixing bug where map wouldn't respond if onMapTouchListener was not set.

## Version 1.2.0 (2013-12-18)

#### Bug fixes / performance enhancements
 * Updating API to get allow getting routes from a location.
 * Allowing ability to set a custom center point for the map to center around.
 * Allowing ability to ignore boundaries and lock the map's touch controls.
 * Adding ability to add markers to the map.
 * Minor bug fixes
 * Updated Javadocs

## Version 1.1.7 (2013-11-21)

#### Bug fixes / performance enhancements
 * Fixing callback for blue dot availability.

## Version 1.1.6 (2013-11-15)

#### Bug fixes / performance enhancements
 * Location API 1.1 Hotfix.

## Version 1.1.5 (2013-11-13)

#### Bug fixes / performance enhancements
 * Custom Icon URLs can be set on a point.
 * A custom scale can be set on a Point's icon.
 * Location API uses 1.1 schema

## Version 1.1.4

#### Bug fixes / performance enhancements
 * POI Layer is always at the top.
 * Supporting two finger tap on the map view.
 * Adding convenience method to get value from `PwPoint` meta data.
 * Adding listener for when Blue Dot is received.

## Version 1.1.3 (2013-10-23)

#### Bug fixes / performance enhancements
 * Maintenance and bug fixes.
 * Not forcing hardware acceleration.
 * Pointing POI icon urls to the right environment.

## Version 1.1.0 (2013-10-16)

#### Bug fixes / performance enhancements
 * Enhancing PwMapView to have better interaction (panning and zooming)
