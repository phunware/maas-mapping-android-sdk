#MaaS Mapping Demo

##Overview
This is a basic example of how to setup and use the Mapping SDK.
###Build requirements
* Latest MaaS Core
* Latest MaaS Mapping
* OkHttp 1.2.1
* AndroidSVG 2.1.1
* Picasso 2.1.1

##Prerequisites
* The `build.gradle` script includes most of the required dependencies from Maven. It is setup by default to look for required MaaS libraries in the `libs` folder.
* The sample will show a building and it's points of interest in the main activity. To show these replace "YOUR_BUILDING_ID_HERE" with your building id.
    It can be found in `MainActivity.java` in the line
    ```
    private static final long BUILDING_ID = YOUR_BUILDING_ID_HERE;
    ```