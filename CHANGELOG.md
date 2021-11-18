# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.1.0][] - TBD

### Added

- Added support for manual camera updates via `PhunwareMapManager.animateCamera(CameraPosition cameraPosition)` and `PhunwareMapManager.animateCamera(CameraUpdate cameraUpdate)`.
- Added support for displaying connected building floor maps while routing.

### Changed

- Optimized campus loading and caching.
- Improved memory management when loading multiple floor maps across multiple buildings.
- Updated Phunware Location dependency to 4.1.0.

### Removed

- Removed unnecessary logging.

### Fixed

- Fixed an issue where `PhunwareMapManager.isMyLocationEnabled` would incorrectly return true in some scenarios.

## [4.0.0][] - 2021-08-11

### Added

- Multi-building routing

### Changed

- Migrated to AndroidX
- Bumped minSdkVersion to API 23 (Android 6.0)
- Bumped targetSdkVersion to API 30 (Android 11)
- Updated to modern Google play services
- Updated to modern Firebase dependencies
- Updated Phunware dependencies to 4.0.0

### Removed

- Removed all existing deprecated API

### Fixed

- `PhunwareMapManager.isMyLocationEnabled` getter now returns true after stopping location updates
- Fixed several crashes
- Fixed several performance issues

## [3.9.12][] - 2020-06-15

### Added

- Added new callback to support POI image override

### Fixed

- Fixed an issue that caused floor map to not load occasionally

## [3.9.11][] - 2020-01-23

### Added

Added landmarks to last maneuver in a route

## [3.9.10][] - 2020-01-03

### Changed

- Improved routing experience with smooth transitions for turn by turn cards
- Improved route snapping

## [3.9.9][] - 2019-11-25

### Changed

- Improved error handling

## [3.9.8][] - 2019-11-15

### Changed

- Optimizations in map loading time

## [3.9.7][] - 2019-10-11

### Fixed

- Fixed an issue with floor switching when curent location is available

## [3.9.6][] - 2019-09-25

### Changed

- Updated Location to 3.7.4

## [3.9.5][] - 2019-09-19

### Fixed

- Added unique names for modules to address issues with duplicate kotlin_module files

## [3.9.4][] - 2019-09-03

### Changed

- Updated android svg library to 1.4
- Updated bundles library to 1.1.4

## [3.9.3][] - 2019-08-16

### Added

- Added API to perform cleanup on PhunwareMapManager

### Fixed

- Fixed `OnManeuverChangedListener` not getting invoked when walking backwards
- Memory Optimizations

## [3.9.2][] - 2019-08-07

### Changed

- Improved map animation

### Fixed

- Fixed performance issues

## [3.9.1][] - 2019-06-26

### Added

- Added API to retreive map marker object

## [3.9.0][] - 2019-04-02

### Added

- Initial release of Managed Compass -- an alternate heading experience for sites that suffer from inaccurate heading
- Ability to blend GPS with indoor location providers based on configuration in the MaaS portal
- Ability to load buildings without attaching it to a map
- Support for one way escalators in Routing
- Support for attaching a loaded building to map
- Support for detaching buildings from map

### Changed

- Updated to Location 3.7.0

## [3.8.1][] - 2019-02-21

### Changed

- Upgraded to Mapping API 3.0

## [3.8.0][] - 2019-01-04

### Changed

- Updated to Google Play Services 16.0.0
- Updated to Core 3.5.0
- Built with Android Support Library 28.0.0

## [3.7.1][] - 2018-09-27

### Fixed

- Fixed crash that would occur if `PhunwareMapManager.setLocationMode` was never called

## [3.7.0][] - 2018-09-19

### Changed

- Updated debug dot mechanism

### Fixed

- Various bug fixes

## [3.6.1][] - 2018-08-30

### Changed

- Updated routing algorithm to include weighting for floor switches
- Improved building load time by adding parallel downloading of floor resources

### Fixed

- Minor fix for content provider

## [3.6.0][] - 2018-07-31

### Added

- Added API to change route line color and width

### Changed

- Optimizations in map loading time

### Fixed

- Various bug fixes

## [3.5.0][] - 2018-06-05

### Added

- Added `setCacheFallbackTimeout()`
- Added touch listener to MapView and MapFragment

### Changed

- Locate Me routing enhancements

### Fixed

- Fixed icon resizing

## [3.4.0][] - 2018-04-05

### Added

- Added customization of route lines and stroke

### Changed

- Updated to Google Play Services 11.8.0

## [3.3.0][] - 2018-02-22

### Changed

- Migrated to JWT authentication
- Added additional logging for troubleshooting

### Fixed

- Fixed routing bug involving portals (e.g. escalator, stairs)

## [3.2.1][] - 2018-01-10

### Fixed

- Metadata now persists in content provider
- Various bug fixes

## [3.2.0][] - 2017-10-16

### Changed

- Updated to Google Play Services 11.4.0
- Blue dot is now extinguished with pressure changes

### Fixed

- `isActive` flag is now honored for POI display and routing

## [3.1.3][] - 2017-10-03

### Fixed

- Fixed authorities name for content provider

## [3.1.2][] - 2017-09-11

### Added

- Added Location Sharing API
- Added ContentProvider for map data

### Fixed

- Various bug fixes

## [3.1.1][] - 2017-03-28

### Fixed

- Various bug fixes

## [3.1.0][] - 2017-01-30

### Added

- Building bundles

### Changed

- Changed interface for setting location provider
- Updated to Core 3.0.3

### Fixed

- Various bug fixes

## [3.0.1][] - 2016-10-07

### Changed

- Updated to Google Play Services 9.6.1
- Updated to Core 3.0.2

### Fixed

- Various bug fixes

## [3.0.0][] - 2016-08-04

### Added

- Complete rewrite

## [2.5.0][] - 2015-08-31

### Added

- Added support for turn-by-turn directions. Turn-by-turn maneuvers can be accessed by accessing the maneuvers property on a PWRoute object. You can plot a route maneuver on a PWMapView intance by calling setRouteManeuver: with a valid PWRouteManeuver object. All previous route behavior is still present and unaffected. Please see the turn-by-turn sample as an example of how to implement turn-by-turn. NOTE: You will need to plot a route maneuever in order to enter turn-by-turn mode.
- Added new class, PWRouteManeuever. A route maneuver encapsulates information related to given maneuver such as turn direction, distance and other information.
- Added PwBuildingMapManager callback PwBuildingMapManager:onPwRouteStepChangedCallback:onRouteStepChanged. This method is called whenever the PWRouteStep being displayed by the map view changes.
- Added PwBuildingMapManager callback PwBuildingMapManager:onmaneuverchangedCallback:onManeuverChanged. This method is called whenever the PWRouteManeuver being displayed by the map changes.
- Added property to PWDirectionsOptions called excludedPointIdentifiers. Specify an array of point identifiers that you would like to exclude from routing. Please see PWDirectionsOptions header for more information.

### Changed

- Route maneuvers switch automatically when location upates are available and the indoor user tracking mode is set to PWIndoorUserTrackingModeFollow or PWIndoorUserTrackingModeFollowWithHeading. You can manually set route maneuvers if desired but that will set the indoor user tracking mode to PWIndoorUserTrackingModeNone.
- Route steps are now automatically selected by the SDK in response to user initiated floor changes or location updates. A new callback has been created when a route step changes.
- When registering a PWGPSLocationManager with the map view the location will now show on all floors regardless of whether or not there is a valid floorIDMapping match.
- Updated to MaaSLocation SDK 1.2.0

### Fixed

- Fixed Bug for getting route from and to same floor number in different buildings.

## 2.4.0 - 2015-04-10

### Added

- Added options that allow routing from any point of interest, user location or dropped pin annotation to any other point of interest, user location or dropped pin marker (in other words, any type of route endpoint is now allowed on either end of the route).
- Added `setMarkerZoomLevel` method to `PwBuildingMapManager` for toggling POI zoom level.
- Added `setBlueDotSmoothingEnabled` method to `PwBuildingMapManager` for toggling blue dot.
- Added DiskLRUCache to cache SVG tiles, any tile will only be render once.

### Changed

- Updated to MaaSLocation 1.0.1

### Removed

- Replaced `onPreDraw` listeners with `RenderThread` to handle real time occlusion detection.

## [2.3.0][] - 2015-02-02

### Added

- Added new "blue dot smoothing" functionality to provide a better user location tracking experience.
- Added `setBlueDotSmoothingEnabled` method to `PwBuildingMapManager` for turning blue dot smoothing on and off.
- Added `setRouteSnappingTolerance` method to `PwBuildingMapManager` for turning off route snapping or setting a different tolerance.
- Added `retrievePointsOfInterestTypes` method to `PwMappingModule` for fetching all POI types.
- Added `pwOnBuildingPOIDataLoadedCallback` method to `PwMapOverlayManagerBuilder` for getting callback when POI are loaded.
- Added remove method to `PwBuildingMarker` for remove itself from PwMap.
- Added `addMarker` to `PwMap` for adding a `PwBuildingMarker` to `PwMap`.

### Fixed

- Various bug fixes

## [2.0.6][] - 2014-09-30

### Changed

- Merged JavaDoc for Mapping and MappingLibrary
- Updated to MaaSLocation 0.9.1

### Fixed

- Fixed MappingLibrary-javadoc.jar and MappingLibrary-sources.jar
- Fixed building warnings

## [2.0.5][] - 2014-09-22

### Added

- Added MaaS MappingLibrary module that is an Android library for wrapping support for Google Play Services (Google Maps) and MaaS Mapping SDK.
- Added Location SDK as a dependency for MappingLibrary.
- Added "Clear Cache" into Mapping Sample

### Changed

- Migrated to Google Maps API 2.0 (requires Google Play Services 4.x).
- Bumped minSdkVersion to 10 since Google Play Services 4.x only supports API 10+
- Updated to Picasso 2.3.4
- Updated to MaaSCore 1.3.7

### Fixed

- Fixed several performance issues
- Various bug fixes

## 1.2.3 - 2014-02-26

### Changed

- Requires MaaSCore 1.3.5
- Published as Java 6 compatible binaries

## 1.2.1 - 2013-12-27

### Fixed

- Fixed bug around resetting the current floor. The entire view is reset now, not just the map.
- Fixed bug where map wouldn't respond if `onMapTouchListener` was not set.

## 1.2.0 - 2013-12-18

### Added

- Ability to set a custom center point for the map to center around
- Ability to ignore boundaries and lock the map's touch controls
- Ability to add markers to the map

### Changed

- Updated Location API to allow for getting routes from a location.
- Updated Javadocs

### Fixed

- Various bug fixes

## 1.1.7 - 2013-11-21

### Fixed

- Fixed callback for blue dot availability

## 1.1.6 - 2013-11-15

### Fixed

- Location API 1.1 hotfix

## 1.1.5 - 2013-11-13

### Added

- Custom Icon URLs can be set on a point
- Custom scale can be set on a Point's icon

### Changed

- Location API uses 1.1 schema

## 1.1.4 - 2013-11-03

### Added

- Added convenience method to get value from `PwPoint` metadata
- Added listener for when blue dot is received
- Added support for two-finger tap on the map view

### Fixed

- POI layer is now always at the top

## 1.1.3 - 2013-10-23

### Changed

- Hardware acceleration is no longer required

### Fixed

- POI icon URLs now point to the right environment
- Various bug fixes

## 1.1.0 - 2013-10-16

### Changed

- `PwMapView` now has better interaction (e.g. panning and zooming)

[4.1.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/4.0.0...4.1.0-beta-06
[4.0.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.12...4.0.0
[3.9.12]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.11...v3.9.12
[3.9.11]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.10...v3.9.11
[3.9.10]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.9...v3.9.10
[3.9.9]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.8...v3.9.9
[3.9.8]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.7...v3.9.8
[3.9.7]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.6...v3.9.7
[3.9.6]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.5...v3.9.6
[3.9.5]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.4...v3.9.5
[3.9.4]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.3...v3.9.4
[3.9.3]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.2...v3.9.3
[3.9.2]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.1...v3.9.2
[3.9.1]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.9.0...v3.9.1
[3.9.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.8.1...v3.9.0
[3.8.1]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.8.0...v3.8.1
[3.8.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.7.1...v3.8.0
[3.7.1]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.7.0...v3.7.1
[3.7.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.6.1...v3.7.0
[3.6.1]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.6.0...v3.6.1
[3.6.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.5.0...v3.6.0
[3.5.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/3.4.0...v3.5.0
[3.4.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/3.3.0...3.4.0
[3.3.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/3.2.1...3.3.0
[3.2.1]: https://github.com/phunware/maas-mapping-android-sdk/compare/3.2.0...3.2.1
[3.2.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/3.1.3...3.2.0
[3.1.3]: https://github.com/phunware/maas-mapping-android-sdk/compare/3.1.2...3.1.3
[3.1.2]: https://github.com/phunware/maas-mapping-android-sdk/compare/3.1.1...3.1.2
[3.1.1]: https://github.com/phunware/maas-mapping-android-sdk/compare/3.1.0...3.1.1
[3.1.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/v.3.0.1...3.1.0
[3.0.1]: https://github.com/phunware/maas-mapping-android-sdk/compare/v3.0.0...v.3.0.1
[3.0.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/V2.5.0...v3.0.0
[2.5.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/v2.3.0...V2.5.0
[2.3.0]: https://github.com/phunware/maas-mapping-android-sdk/compare/v.2.0.6...v2.3.0
[2.0.6]: https://github.com/phunware/maas-mapping-android-sdk/compare/v.2.0.5...v.2.0.6
[2.0.5]: https://github.com/phunware/maas-mapping-android-sdk/tree/v.2.0.5
