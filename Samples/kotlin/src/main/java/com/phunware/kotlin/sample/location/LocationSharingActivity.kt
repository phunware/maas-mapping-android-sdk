package com.phunware.kotlin.sample.location

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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ui.IconGenerator
import com.phunware.core.PwCoreSession
import com.phunware.core.PwLog
import com.phunware.kotlin.sample.R
import com.phunware.kotlin.sample.location.util.BitmapUtils
import com.phunware.kotlin.sample.location.util.LatLngInterpolator
import com.phunware.kotlin.sample.location.util.MarkerAnimation
import com.phunware.kotlin.sample.location.util.PersonMarker
import com.phunware.kotlin.sample.poi.CustomPOIActivity
import com.phunware.kotlin.sample.building.adapter.FloorAdapter
import com.phunware.location.provider_managed.ManagedProviderFactory
import com.phunware.location.provider_managed.PwManagedLocationProvider
import com.phunware.mapping.MapFragment
import com.phunware.mapping.OnPhunwareMapReadyCallback
import com.phunware.mapping.PhunwareMap
import com.phunware.mapping.bluedot.SharedLocationCallback
import com.phunware.mapping.manager.Callback
import com.phunware.mapping.manager.PhunwareMapManager
import com.phunware.mapping.model.Building
import com.phunware.mapping.model.FloorOptions
import com.phunware.mapping.model.SharedLocation
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

class LocationSharingActivity : AppCompatActivity(), OnPhunwareMapReadyCallback, Building.OnFloorChangedListener, SharedLocationCallback {

    private lateinit var phunwareMap: PhunwareMap
    private lateinit var mapFragment: MapFragment
    private lateinit var mapManager: PhunwareMapManager
    private var currentBuilding: Building? = null
    private lateinit var floorSpinner: Spinner
    private lateinit var spinnerAdapter: ArrayAdapter<FloorOptions>

    private lateinit var friendLocationMap: MutableMap<String, PersonMarker>
    private lateinit var friendColorMap: Map<String, Int>
    private val linearInterpolator = LatLngInterpolator.Linear()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_sharing)

        PwLog.setShowLog(true)

        friendLocationMap = HashMap()
        friendColorMap = HashMap()

        val changeDeviceInfoButton = findViewById<Button>(R.id.change_device_info_button)
        changeDeviceInfoButton.setOnClickListener { showDeviceInfoDialog() }

        initFloorSpinner()

        // Create the map manager used to load the building
        mapManager = PhunwareMapManager.create(this)

        // Register the Phunware API keys
        PwCoreSession.getInstance().registerKeys(this)

        mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getPhunwareMapAsync(this)

    }

    private fun initFloorSpinner() {
        floorSpinner = findViewById(R.id.floorSpinner)
        spinnerAdapter = FloorAdapter(this)
        floorSpinner.adapter = spinnerAdapter
        floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val floor = spinnerAdapter.getItem(id.toInt())
                if (floor != null) {
                    currentBuilding?.selectFloor(floor.level)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onResume() {
        super.onResume()

        if (currentBuilding == null) {
            initFloorSpinner()
            mapFragment.getPhunwareMapAsync(this)
        }


        mapManager.isMyLocationEnabled = true
        startLocationSharing()
        mapManager.startRetrievingSharedLocations(this)

    }

    override fun onPause() {
        super.onPause()

        mapManager.stopSharingUserLocation()
        mapManager.stopRetrievingSharedLocations()
        mapManager.isMyLocationEnabled = false

    }

    override fun onFloorChanged(building: Building, floorId: Long) {
        // Find the floorId in our spinner and set it (if it differs from the currently set value)
        for (index in 0 until spinnerAdapter.count) {
            val floor = spinnerAdapter.getItem(index)
            if (floor != null && floor.id == floorId) {
                if (floorSpinner.selectedItemPosition != index) {
                    floorSpinner.setSelection(index)
                    break
                }
            }
        }

        // Stop location sharing on the previous floor and resume on the new floor
        mapManager.stopRetrievingSharedLocations()
        mapManager.startRetrievingSharedLocations(floorId, this)

    }

    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        // Retrieve buildingId from integers.xml
        val buildingId = resources.getInteger(R.integer.buildingId)
        this.phunwareMap = phunwareMap

        phunwareMap.googleMap.uiSettings.isMapToolbarEnabled = false
        phunwareMap.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this@LocationSharingActivity, R.raw.map_style))

        mapManager.setPhunwareMap(phunwareMap)
        mapManager.addBuilding(buildingId.toLong(),
                object : Callback<Building> {
                    override fun onSuccess(building: Building) {
                        Log.d(TAG, "Building loaded successfully")
                        currentBuilding = building

                        // Populate floor spinner
                        spinnerAdapter.clear()
                        spinnerAdapter.addAll(building.buildingOptions.floors)

                        // Add a listener to monitor floor switches
                        mapManager.addFloorChangedListener(this@LocationSharingActivity)

                        // Initialize a location provider
                        setManagedLocationProvider(building)

                        // Set building to initial floor value
                        val initialFloor = building.initialFloor()
                        building.selectFloor(initialFloor.level)

                        // Animate the camera to the building at an appropriate zoom level
                        val cameraUpdate = CameraUpdateFactory
                                .newLatLngBounds(initialFloor.bounds, 4)
                        phunwareMap.googleMap.animateCamera(cameraUpdate)


                        // Start sharing location with other users
                        startLocationSharing()
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.d(TAG, "Error when loading building -- " + throwable.message)
                    }
                })
    }

    private fun setManagedLocationProvider(building: Building) {
        val builder = ManagedProviderFactory.ManagedProviderFactoryBuilder()
        builder.application(application)
                .context(WeakReference(application))
                .buildingId((building.id).toString())
        val factory = builder.build()
        val managedProvider = factory.createLocationProvider() as PwManagedLocationProvider
        mapManager.setLocationProvider(managedProvider, building)
        mapManager.isMyLocationEnabled = true
    }

    private fun startLocationSharing() {
        mapManager.startSharingUserLocation(getDeviceName(this),
                getDeviceType(this))
    }

    private fun showDeviceInfoDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(getString(R.string.action_set_device_info))
        alertDialogBuilder.setMessage(getString(R.string.set_user_info_dialog_message))
        alertDialogBuilder.setCancelable(false)

        val nullParent: ViewGroup? = null
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_location_sharing, nullParent)
        val deviceNameInput = view.findViewById<EditText>(R.id.set_device_name_input)
        deviceNameInput.setText(getDeviceName(this))
        deviceNameInput.setSelection(deviceNameInput.text.length)

        val deviceTypeInput = view.findViewById<EditText>(R.id.set_device_type_input)
        deviceTypeInput.setText(getDeviceType(this))

        alertDialogBuilder.setView(view)
        alertDialogBuilder.setPositiveButton(getString(R.string.button_ok)
        ) { _, _ ->
            val deviceName = deviceNameInput.text.toString()
            val deviceType = deviceTypeInput.text.toString()
            setDeviceName(this@LocationSharingActivity, deviceName)
            setDeviceType(this@LocationSharingActivity, deviceType)
            mapManager.updateDeviceName(deviceName)
            mapManager.updateDeviceType(deviceType)

        }
        alertDialogBuilder.setNegativeButton(getString(R.string.button_cancel)
        ) { _, _ ->
            // Do Nothing
        }

        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    /**
     * Shared Location Listener Callbacks
     */

    override fun onSuccess(sharedLocationList: List<SharedLocation>) {
        PwLog.d(TAG, "Successfully retrieved other user's locations")

        // Remove ourself from this list
        val sharedLocationListWithoutSelf = ArrayList<SharedLocation>()
        for (location in sharedLocationList) {
            val deviceId = PwCoreSession.getInstance().sessionData.deviceId
            if (deviceId != location.deviceId) {
                sharedLocationListWithoutSelf.add(location)
            }
        }

        // Create a set of the new device id's
        val updatedDeviceIds = HashSet<String>()
        for (friend in sharedLocationListWithoutSelf) {
            updatedDeviceIds.add(friend.deviceId)
        }

        val staleDeviceIds = ArrayList<String>()

        // Use the set to diff against our current map
        // And remove old markers on our map
        for ((key) in friendLocationMap) {
            if (!updatedDeviceIds.contains(key)) {
                staleDeviceIds.add(key)
            }
        }

        runOnUiThread {
            for (staleDevice in staleDeviceIds) {
                removePersonDot(staleDevice)
            }

            for (userLocation in sharedLocationListWithoutSelf) {
                updatePersonDot(userLocation)
            }
        }
    }

    override fun onFailure() {
        PwLog.e(TAG, "Failed to get other user's locations")
    }

    private fun updatePersonDot(personLocation: SharedLocation?) {
        if (personLocation == null || TextUtils.isEmpty(personLocation.deviceName)) {
            PwLog.e(TAG, "Received an empty PersonLocation update")
            return
        }

        if (friendLocationMap.containsKey(personLocation.deviceId)) {
            val personMarker = friendLocationMap[personLocation.deviceId]

            if ((personLocation.deviceName == personMarker!!.name && personLocation.deviceType == personMarker.userType)) {
                val markerPosition = personMarker.marker!!.position
                val res = floatArrayOf(0f)
                Location.distanceBetween(markerPosition.latitude,
                        markerPosition.longitude,
                        personLocation.latitude,
                        personLocation.longitude,
                        res)

                // If the new location is > 1 meter away animate it,
                // otherwise just set its new position
                if (res[0] > 1) {
                    MarkerAnimation.animateMarkerTo(personMarker.marker,
                            LatLng(personLocation.latitude, personLocation.longitude),
                            linearInterpolator)
                } else {
                    personMarker.marker.position = LatLng(personLocation.latitude,
                            personLocation.longitude)
                }

                return
            } else {
                // Device has a new name, remove the old marker and we'll build a new one
                personMarker.marker!!.remove()
            }
        }

        // Build the marker icon to show on the map
        val markerIcon = buildPersonIcon(personLocation.deviceId,
                personLocation.deviceName, personLocation.deviceType)

        val personOptions = MarkerOptions()
                .position(LatLng(personLocation.latitude, personLocation.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(markerIcon))
                .draggable(false)
                .visible(true)

        val newMarker = phunwareMap.googleMap.addMarker(personOptions)
        val newPersonMarker = PersonMarker(personLocation.deviceName,
                personLocation.deviceType, newMarker)

        friendLocationMap[personLocation.deviceId] = newPersonMarker
    }

    private fun removePersonDot(deviceId: String) {
        if (!TextUtils.isEmpty(deviceId) && friendLocationMap.containsKey(deviceId)) {
            val m = friendLocationMap[deviceId]
            m!!.marker!!.remove()
            friendLocationMap.remove(deviceId)
        }
    }

    private fun buildPersonIcon(deviceId: String, name: String, userType: String): Bitmap {
        val factory = IconGenerator(this)

        factory.setTextAppearance(R.style.markerIconText)

        val color: Int
        color = if (friendColorMap.containsKey(deviceId)) {
            val colorTmp = friendColorMap[deviceId]
            colorTmp!!
        } else {
            BitmapUtils.randomColor
        }

        val textBitmap = BitmapUtils.createTextBitmap(this, "$name ($userType)")
        val dotBitmap = BitmapUtils.createDotBitmap(this, color)

        val compositeIcon = Bitmap.createBitmap(textBitmap.width,
                textBitmap.height + dotBitmap.height,
                textBitmap.config)

        val canvas = Canvas(compositeIcon)
        canvas.drawBitmap(dotBitmap,
                ((compositeIcon.width / 2) - (dotBitmap.width / 2)).toFloat(),
                0f, null)

        canvas.drawBitmap(textBitmap, 0f, (compositeIcon.height - textBitmap.height).toFloat(), null)

        return compositeIcon
    }

    companion object {
        private val TAG = CustomPOIActivity::class.java.simpleName
        private const val PREFERENCE_NAME = "location sharing sample"
        private const val PREF_DEVICE_NAME = "device name"
        private const val PREF_DEVICE_TYPE = "device type"

        fun setDeviceType(context: Context, deviceType: String) {
            val preferences = context.getSharedPreferences(PREFERENCE_NAME, 0)
            preferences.edit()
                    .putString(PREF_DEVICE_TYPE, deviceType)
                    .apply()
        }

        fun getDeviceType(context: Context): String {
            // Get device type from shared preferences
            // Default to empty string if no type is set
            val preferences = context.getSharedPreferences(PREFERENCE_NAME, 0)
            return preferences.getString(PREF_DEVICE_TYPE, "")
        }

        fun setDeviceName(context: Context, deviceName: String) {
            val preferences = context.getSharedPreferences(PREFERENCE_NAME, 0)
            preferences.edit()
                    .putString(PREF_DEVICE_NAME, deviceName)
                    .apply()
        }

        fun getDeviceName(context: Context): String? {
            // Get device name from shared preferences
            // Default to device manufacturer/model info if no device name is set
            val preferences = context.getSharedPreferences(PREFERENCE_NAME, 0)
            return preferences.getString(PREF_DEVICE_NAME, deviceModelInfo)
        }

        private val deviceModelInfo: String
            get() {
                val manufacturer = Build.MANUFACTURER
                val model = Build.MODEL
                return if (model.startsWith(manufacturer)) {
                    model
                } else "$manufacturer $model"
            }
    }
}
