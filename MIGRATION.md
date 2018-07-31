# Mapping SDK Migration Guide

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
