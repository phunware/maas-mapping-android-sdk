package com.phunware.kotlin.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.phunware.mapping.MapFragment
import com.phunware.mapping.OnPhunwareMapReadyCallback

/**
 * Example of extending [MapFragment] to customise
 * the initial camera location and the zooming level of the map.
 */
class CustomMapFragment : MapFragment() {

    companion object {
        fun newInstance(activity: AppCompatActivity, callback: OnPhunwareMapReadyCallback) : CustomMapFragment {

            val buildingLatLng = TypedValue().let {
                activity.resources.getValue(R.integer.building_latitude, it, true)
                val buildingLat = it.float.toDouble()
                activity.resources.getValue(R.integer.building_longitude, it, true)
                val buildingLng = it.float.toDouble()
                LatLng(buildingLat, buildingLng)
            }

            val zoomLevel = 16f

            return CustomMapFragment().apply {
                getPhunwareMapAsync(callback)
                arguments = Bundle().apply {
                    // ...
                    val options = GoogleMapOptions()
                    val cameraOptions = CameraPosition.Builder()
                        .target(buildingLatLng)
                        .zoom(zoomLevel)
                        .build()
                    putParcelable(MAP_OPTIONS_KEY, options.camera(cameraOptions))
                }
            }

        }

        // "MapOptions" is the argument key that Google is using right now when you use their MapFragment directly.
        // This is an implementation detail of the Google Maps library that is subject to change without notice
        private const val MAP_OPTIONS_KEY = "MapOptions"
    }

}