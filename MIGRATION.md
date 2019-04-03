# Mapping SDK Migration Guide
## Upgrade from 3.8.x to 3.9.0

##### Upgrade Steps

1. Open the `build.gradle` from your project and change the compile statement to `com.phunware.mapping:mapping:3.9.0` and then sync the project.

## Upgrade from 3.8.0 to 3.8.1

##### Upgrade Steps

1. Open the `build.gradle` from your project and change the compile statement to `com.phunware.mapping:mapping:3.8.1` and then sync the project.

## Upgrade from 3.7.1 to 3.8.0

#### General

This release has library updates. See CHANGELOG.md for more info.

#### Upgrade Steps

1. Open the `build.gradle` from your project and change the compile statement to `com.phunware.mapping:mapping:3.8.0` and then sync the project.

### Library updates
- compileSdkVersion - 28
- targetSdkVersion - 28
- Support Library version - 28.0.0

## 3.7.0 to 3.7.1

##### Upgrade Steps

1. Open the `build.gradle` from your project and change the compile statement to `com.phunware.mapping:mapping:3.7.1` and then sync the project.

## 3.6.x to 3.7.0

#### General

This release updates the debug dot mechanism that is used in the Location SDK. It also contains bug fixes and enhancements.

##### Upgrade Steps

1. Open the `build.gradle` from your project and change the compile statement to `com.phunware.mapping:mapping:3.7.0` and then sync the project.
2. If using debug dots, you can enable/disable them by calling the showDebugDots(boolean) method in `PhunwareMapManager`. Note that in order for the debug dots to work properly, you must also be using the Phunware Location SDK (v3.5.0 or later).

## 3.5.x to 3.6.0

#### General

This release has bug fixes and feature enhancements.  See CHANGELOG.md for more info.

##### Upgrade Steps

1. Open the `build.gradle` from your project and change the compile statement to `com.phunware.mapping:mapping:3.6.0` and then sync the project.

## 3.4.x to 3.5.0

#### General

This release has bug fixes and feature enhancements.  See CHANGELOG.md for more info.

##### Upgrade Steps

1. Open the `build.gradle` from your project and change the compile statement to `com.phunware.mapping:mapping:3.5.0` and then sync the project.

## 3.3.x to 3.4.0

### Library updates
- compileSdkVersion - 27
- buildToolsVersion - 27.0.3
- targetSdkVersion - 26
- Support Library version - 27.1.0
- Google Play Services version - 11.8.0
- Okhttp version - 3.10.0
- Gson version - 2.8.2

#### Improvements
##### Customization of Route Line Colors
###### To change the Route Line colors the SDK uses:
Add the following `color` definitions to your `colors.xml` file:

```java
<color name="pw_route_color">#FF0007EF</color>
<color name="pw_maneuver_color">#FFFFFFFF</color>
<color name="pw_maneuver_direction_color">#FFA3BA50</color>
```
Default values are shown above.

##### Customization of Route Stroke Widths
Add the following `integer` definitions to your `integers.xml` file:

```java
<integer name="pw_route_stroke_width">10</color>
<integer name="pw_maneuver_stroke_width">8</color>
<integer name="pw_maneuver_direction_stroke_width">24</color>
```
Default values are shown above.

## Upgrade from 3.2.x to 3.3.0

#### General

This release has some changes to support our new Location BLE provider.

##### Upgrade Steps

1. Open the `build.gradle` from your project and change the compile statement to `com.phunware.mapping:mapping:3.3.0` and then sync the project.
