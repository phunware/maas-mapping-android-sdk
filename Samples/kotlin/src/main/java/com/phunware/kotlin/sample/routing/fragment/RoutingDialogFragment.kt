package com.phunware.kotlin.sample.routing.fragment

/* Copyright (C) 2018 Phunware, Inc.

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL Phunware, Inc. BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Phunware, Inc. shall
not be used in advertising or otherwise to promote the sale, use or
other dealings in this Software without prior written authorization
from Phunware, Inc. */
import android.annotation.SuppressLint
import android.app.Dialog
import android.location.Location
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Spinner
import com.google.android.gms.maps.model.LatLng
import com.phunware.kotlin.sample.R
import com.phunware.kotlin.sample.routing.adapter.BuildingAdapter
import com.phunware.location_core.PwLocationProvider
import com.phunware.mapping.model.Building
import com.phunware.mapping.model.PointOptions
import java.util.ArrayList

/**
 * A DialogFragment that implements POI selection based on the current building.
 */
class RoutingDialogFragment : DialogFragment() {
    interface RoutingDialogListener {
        fun onGetBuildingForRouting(): Building
        fun onGetRoutes(startId: Long, endId: Long, isAccessible: Boolean)
    }

    companion object {
        private const val ARG_LOCATION_ENABLED = "arg_location_enabled"
        private const val ARG_CURRENT_LOCATION = "arg_current_location"
        const val CURRENT_LOCATION_ITEM_ID = -2L

        fun newInstance(locationEnabled: Boolean,
                        currentLocation: Location?) = RoutingDialogFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_LOCATION_ENABLED, locationEnabled)
                putParcelable(ARG_CURRENT_LOCATION, currentLocation)
            }
        }
    }

    internal var callback: RoutingDialogListener? = null
    private var startPicker: Spinner? = null
    private var endPicker: Spinner? = null
    private var accessible: CheckBox? = null

    private val locationEnabled: Boolean by lazy {
        arguments?.getBoolean(ARG_LOCATION_ENABLED) ?: false
    }
    private val currentLocation: Location? by lazy {
        arguments?.getParcelable<Location>(ARG_CURRENT_LOCATION)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val dialogLayout = LayoutInflater.from(requireContext()).inflate(
                R.layout.dialog_route_selection,
                null
        )

        builder.setView(dialogLayout)
                .setTitle("Select a Route")
                .setMessage("Choose two points to route between")
                .setCancelable(false)
                .setPositiveButton("Route") { _, _ ->
                    callback?.onGetRoutes(
                            startId = startPicker?.selectedItemId ?: 0L,
                            endId = endPicker?.selectedItemId ?: 0L,
                            isAccessible = accessible?.isChecked ?: false
                    )
                }
                .setNegativeButton("Cancel") { _, _ ->
                    // Do Nothing - Close Dialog
                }

        initPoiSelection(dialogLayout)

        return builder.create()
    }

    fun setRoutingDialogListener(listener: RoutingDialogListener) {
        callback = listener
    }

    private fun initPoiSelection(dialogLayout: View) {
        startPicker = dialogLayout.findViewById(R.id.start)
        endPicker = dialogLayout.findViewById(R.id.end)
        accessible = dialogLayout.findViewById(R.id.accessible)
        val reverse = dialogLayout.findViewById<ImageButton>(R.id.reverse)

        reverse?.setOnClickListener {
            val startPos = startPicker?.selectedItemPosition
            val endPos = endPicker?.selectedItemPosition

            startPicker?.setSelection(endPos ?: 0)
            endPicker?.setSelection(startPos ?: 0)
        }

        val points = ArrayList<PointOptions>()

        val currentBuilding = callback?.onGetBuildingForRouting()

        var hasCurrentLocation = false

        currentBuilding?.let { building ->

            // build up a flat list of all of the POIs for each floor
            building.floorOptions
                    ?.filter { it != null && it.poiOptions != null }
                    ?.forEach { points.addAll(it.poiOptions) }

            if (locationEnabled) {
                currentLocation?.let { location ->
                    val currentLocationLatLng = LatLng(location.latitude, location.longitude)

                    // default to the buildings selected floor as the current floor
                    var currentFloorId = building.selectedFloor.id

                    // if our location object contains a floor (such as when the device as acquired
                    // a BLE bluedot) use any floor information it provides as our current floor.
                    if (location.extras != null
                            && location.extras.containsKey(
                                    PwLocationProvider.LOCATION_EXTRAS_KEY_FLOOR_ID
                            )) {
                        currentFloorId = location.extras
                                .getLong(PwLocationProvider.LOCATION_EXTRAS_KEY_FLOOR_ID)
                    }

                    // Add a new POI to the list that represents the device's "Current Location"
                    points.add(
                            0, PointOptions()
                            .id(CURRENT_LOCATION_ITEM_ID)
                            .location(currentLocationLatLng)
                            .level(currentFloorId)
                            .name(getString(R.string.current_location))
                    )
                    hasCurrentLocation = true
                }
            }
        }

        startPicker?.adapter = BuildingAdapter(
                requireContext(),
                points,
                getString(R.string.start_prompt)
        )
        endPicker?.adapter = BuildingAdapter(
                requireContext(),
                points,
                getString(R.string.end_prompt)
        )

        // If we have the devices current location, preselect that POI in the start POI picker.
        if (hasCurrentLocation) {
            startPicker?.setSelection(1, false)
        }
    }
}