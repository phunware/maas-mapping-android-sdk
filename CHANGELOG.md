Change Log
==========

Version 2.0.6 *(2014-09-30)*
* Updated MaaSLocation SDK to '0.9.1'

Version 2.0.5 *(2014-09-22)*
----------------------------
 * Migrating to use Google Maps v2 API from Google Play Services (4.x).
 * Added MaaS MappingLibrary module that is an Android library for wrapping support for Google Play Services (Google Maps) and MaaS Mapping SDK..
 * Added Location SDK as a dependency for MappingLibrary.
 * Add "Clear Cache" into Mapping Sample
 * Updated "minSdkVersion" to "10" since Google Play Services (4.x) only support API level 10 and above.
 * Updated version of Picasso to '2.3.4'
 * Updated MaaSCore to v1.3.7
 * Performance improvement, Refactor & Bug fixes

Version 1.2.3 *(2014-02-26)*
----------------------------
 * Fixed builds to produce Java 6 compatible binaries using 'sourceCompatibility' and 'targetCompatibility' equal to '1.6'.
 * Requires MaaSCore v1.3.5

Version 1.2.1 *(2013-12-27)*
----------------------------
 * Fixing bug around resetting the current floor. The entire view is reset now, not just the map.
 * Fixing bug where map wouldn't respond if onMapTouchListener was not set.

Version 1.2.0 *(2013-12-18)*
----------------------------
 * Updating API to get allow getting routes from a location.
 * Allowing ability to set a custom center point for the map to center around.
 * Allowing ability to ignore boundaries and lock the map's touch controls.
 * Adding ability to add markers to the map.
 * Minor bug fixes
 * Updated Javadocs

Version 1.1.7 *(2013-11-21)*
----------------------------
 * Fixing callback for blue dot availability.

Version 1.1.6 *(2013-11-15)*
----------------------------
 * Location API 1.1 Hotfix.

Version 1.1.5 *(2013-11-13)*
----------------------------
 * Custom Icon URLs can be set on a point.
 * A custom scale can be set on a Point's icon.
 * Location API uses 1.1 schema

Version 1.1.4
----------------------------
 * POI Layer is always at the top.
 * Supporting two finger tap on the map view.
 * Adding convenience method to get value from `PwPoint` meta data.
 * Adding listener for when Blue Dot is received.

Version 1.1.3 *(2013-10-23)*
----------------------------
 * Maintenance and bug fixes.
 * Not forcing hardware acceleration.
 * Pointing POI icon urls to the right environment.

Version 1.1.0 *(2013-10-16)*
----------------------------
 * Enhancing PwMapView to have better interaction (panning and zooming)