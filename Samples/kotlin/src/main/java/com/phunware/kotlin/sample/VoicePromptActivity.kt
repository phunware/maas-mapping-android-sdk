package com.phunware.kotlin.sample

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.phunware.core.PwCoreSession
import com.phunware.core.PwLog
import com.phunware.location.provider_managed.ManagedProviderFactory
import com.phunware.location.provider_managed.PwManagedLocationProvider
import com.phunware.location_core.PwLocationProvider
import com.phunware.mapping.MapFragment
import com.phunware.mapping.OnPhunwareMapReadyCallback
import com.phunware.mapping.PhunwareMap
import com.phunware.mapping.manager.Callback
import com.phunware.mapping.manager.Navigator
import com.phunware.mapping.manager.PhunwareMapManager
import com.phunware.mapping.manager.Router
import com.phunware.mapping.model.*
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Locale

class VoicePromptActivity : AppCompatActivity(), OnPhunwareMapReadyCallback,
        Building.OnFloorChangedListener, Navigator.OnManeuverChangedListener, TextToSpeech.OnInitListener {

    private lateinit var mapManager: PhunwareMapManager
    private lateinit var mapFragment: MapFragment
    private lateinit var currentBuilding: Building
    private lateinit var floorSpinner: Spinner
    private lateinit var floorSpinnerAdapter: ArrayAdapter<FloorOptions>
    private lateinit var content: RelativeLayout

    // Navigation Views
    private lateinit var fab: FloatingActionButton
    private var navigator: Navigator? = null
    private lateinit var navOverlayContainer: View
    private lateinit var navOverlay: NavigationOverlayView
    private lateinit var startPicker: Spinner
    private lateinit var endPicker: Spinner
    private lateinit var accessible: CheckBox
    private lateinit var voice: ImageButton
    private lateinit var voiceStatusTextView: TextView

    private var selectRouteListener: View.OnClickListener = View.OnClickListener { showRoutingDialog() }
    private var exitNavListener: View.OnClickListener = View.OnClickListener { stopNavigating() }
    private var voiceListener: View.OnClickListener = View.OnClickListener { toggleVoice() }

    private var voiceEnabled: Boolean = false
    private var tts: TextToSpeech? = null
    private val displayHelper: ManeuverDisplayHelper = ManeuverDisplayHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.voice_prompt)
        content = findViewById(R.id.content)

        //Initialize Text-To-Speech
        tts = TextToSpeech(this, this)

        // Initialize views for routing
        fab = findViewById(R.id.fab)
        fab.visibility = View.GONE
        fab.setOnClickListener(selectRouteListener)
        navOverlayContainer = findViewById(R.id.nav_overlay_container)
        navOverlay = findViewById(R.id.nav_overlay)
        voice = findViewById(R.id.voice)
        voice.setOnClickListener(voiceListener)
        voiceStatusTextView = findViewById(R.id.voice_status)

        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        voiceEnabled = sharedPref.getBoolean("voice", false)

        floorSpinner = findViewById(R.id.floorSpinner)
        floorSpinnerAdapter = FloorAdapter(this)
        floorSpinner.adapter = floorSpinnerAdapter
        floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val floor = floorSpinnerAdapter.getItem(id.toInt())
                if (floor != null) {
                    currentBuilding.selectFloor(floor.level)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Register the Phunware API keys
        PwCoreSession.getInstance().registerKeys(this)

        // Create the map manager and fragment used to load the building
        mapManager = PhunwareMapManager.create(this)
        mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getPhunwareMapAsync(this)
    }

    override fun onBackPressed() {
        if (navOverlayContainer.visibility == View.VISIBLE) {
            stopNavigating()
        } else {
            super.onBackPressed()
        }
    }

    public override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    /**
     * TextToSpeech.OnInitListener
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    /**
     * OnPhunwareMapReadyCallback
     */
    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        // Retrieve buildingId from integers.xml
        val buildingId = resources.getInteger(R.integer.buildingId)

        phunwareMap.googleMap.uiSettings.isMapToolbarEnabled = false
        phunwareMap.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this@VoicePromptActivity, R.raw.map_style))

        mapManager.setPhunwareMap(phunwareMap)
        mapManager.addBuilding(buildingId.toLong(),
                object : Callback<Building> {
                    override fun onSuccess(building: Building) {
                        Log.d(TAG, "Building loaded successfully")
                        currentBuilding = building

                        // Populate floor spinner
                        floorSpinnerAdapter.clear()
                        floorSpinnerAdapter.addAll(building.buildingOptions.floors)

                        // Add a listener to monitor floor switches
                        mapManager.addFloorChangedListener(this@VoicePromptActivity)

                        // Initialize a location provider
                        setManagedLocationProvider(building)

                        // Set building to initial floor value
                        val initialFloor = building.initialFloor()
                        building.selectFloor(initialFloor.level)

                        // Animate the camera to the building at an appropriate zoom level
                        val cameraUpdate = CameraUpdateFactory
                                .newLatLngBounds(initialFloor.bounds, 4)
                        phunwareMap.googleMap.animateCamera(cameraUpdate)

                        // Enabled fab for routing
                        showFab(true)
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.d(TAG, "Error when loading building -- " + throwable.message)
                        showFab(false)
                    }
                })
    }

    /**
     * Navigator.OnManeuverChangedListener
     */
    override fun onManeuverChanged(navigator: Navigator, position: Int) {
        // Update the selected floor when the maneuver floor changes
        val maneuver = navigator.maneuvers[position]
        val selectedPosition = floorSpinner.selectedItemPosition
        for (i in 0 until floorSpinnerAdapter.count) {
            val floor = floorSpinnerAdapter.getItem(i)
            if (selectedPosition != i && floor != null && floor.id == maneuver.floorId) {
                floorSpinner.setSelection(i)
            }
        }

        if (voiceEnabled) {
            val pair = navOverlay.getManeuverPair()
            var text = displayHelper.stringForDirection(this, pair.mainManeuver)

            val turnManeuver  = pair.turnManeuver
            var turnable : Boolean  = false
            if(turnManeuver != null) {
                turnable = turnManeuver.isTurnManeuver
            }

            if(turnManeuver != null && position < navigator.maneuvers.size - 1 && turnable) {
                text += getString(R.string.demo_voice_prompt_then)
                text += displayHelper.stringForDirection(this, turnManeuver)
            }
            else {
                text += getString(R.string.demo_voice_prompt_arrive_at_destination)
            }
            textToVoice(text)
        }
    }

    override fun onRouteSnapFailed() {
        // Do Nothing
    }

    /**
     * Building.OnFloorChangedListener
     */
    override fun onFloorChanged(building: Building?, floorId: Long) {
        for (index in 0 until floorSpinnerAdapter.count) {
            val floor = floorSpinnerAdapter.getItem(index)
            if (floor != null && floor.id == floorId) {
                if (floorSpinner.selectedItemPosition != index) {
                    runOnUiThread { floorSpinner.setSelection(index) }
                    break
                }
            }
        }
    }

    /**
     * Private Methods
     */
    private fun setManagedLocationProvider(building: Building) {
        val builder = ManagedProviderFactory.ManagedProviderFactoryBuilder()
        builder.application(application)
                .context(WeakReference(application))
                .buildingId(building.id.toString())
        val factory = builder.build()
        val managedProvider = factory.createLocationProvider() as PwManagedLocationProvider
        mapManager.setLocationProvider(managedProvider, building)
        mapManager.isMyLocationEnabled = true
    }

    private fun showFab(show: Boolean) {
        runOnUiThread {
            val start = (if (show) 0 else 1).toFloat()
            val end = (if (show) 1 else 0).toFloat()
            val anims = ArrayList<Animator>(2)
            anims.add(ObjectAnimator.ofFloat(fab, View.SCALE_X, start, end))
            anims.add(ObjectAnimator.ofFloat(fab, View.SCALE_Y, start, end))
            val s = AnimatorSet()
            s.playTogether(anims)
            s.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    if (show) fab.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!show) fab.visibility = View.GONE
                }
            })
            s.start()
        }
    }

    private fun showRoutingDialog() {
        val builder = AlertDialog.Builder(this)

        // Load custom dialog view
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_route_selection, null)
        initDialogUI(dialogView)

        builder.setView(dialogView)
                .setTitle("Select a Route")
                .setMessage("Choose two points to route between")
                .setCancelable(false)
                .setPositiveButton("Route", { _, _ -> getRoutes() })
                .setNegativeButton("Cancel", { _, _ ->
                    // Do Nothing - Close Dialog
                })

        val d = builder.create()
        d.show()
    }

    private fun initDialogUI(dialogView: View) {
        startPicker = dialogView.findViewById(R.id.start)
        endPicker = dialogView.findViewById(R.id.end)
        accessible = dialogView.findViewById(R.id.accessible)
        val reverse = dialogView.findViewById<ImageButton>(R.id.reverse)

        reverse.setOnClickListener { onReverseClicked() }

        val points = ArrayList<PointOptions>()

        currentBuilding.floorOptions
                .filter { it != null && it.poiOptions != null }
                .forEach { points.addAll(it.poiOptions) }


        var hasCurrentLocation = false
        if (mapManager.isMyLocationEnabled && mapManager.currentLocation != null) {
            val myLocation = mapManager.currentLocation
            val currentLocation = LatLng(myLocation.latitude, myLocation.longitude)
            var currentFloorId = mapManager.currentBuilding.selectedFloor.id
            if (myLocation.extras != null && myLocation.extras
                            .containsKey(PwLocationProvider.LOCATION_EXTRAS_KEY_FLOOR_ID)) {
                currentFloorId = myLocation.extras
                        .getLong(PwLocationProvider.LOCATION_EXTRAS_KEY_FLOOR_ID)
            }
            points.add(0, PointOptions()
                    .id(VoicePromptActivity.ITEM_ID_LOCATION.toLong())
                    .location(currentLocation)
                    .level(currentFloorId)
                    .name(getString(R.string.current_location)))
            hasCurrentLocation = true
        }

        startPicker.adapter = BuildingAdapter(this, points, getString(R.string.start_prompt))
        endPicker.adapter = BuildingAdapter(this, points, getString(R.string.end_prompt))

        if (hasCurrentLocation) {
            startPicker.setSelection(1, false)
        }
    }

    private fun getRoutes() {
        val startId = startPicker.selectedItemId
        val endId = endPicker.selectedItemId
        val isAccessible = accessible.isChecked

        val router: Router?
        if (startId.compareTo(ITEM_ID_LOCATION) == 0) {
            val currentLocation = LatLng(mapManager.currentLocation.latitude, mapManager.currentLocation.longitude)
            router = mapManager.findRoutes(currentLocation, endId, mapManager.currentBuilding.selectedFloor.id, isAccessible)
        } else {
            router = mapManager.findRoutes(startId, endId, isAccessible)
        }

        if (router != null) {
            val route = router.shortestRoute()
            if (route == null) {
                PwLog.e(VoicePromptActivity.TAG, "Couldn't find route.")
                Snackbar.make(content, R.string.no_route,
                        Snackbar.LENGTH_SHORT).show()
            } else {
                startNavigating(route)
            }
        } else {
            Snackbar.make(content, R.string.no_route, Snackbar.LENGTH_SHORT).show()
            PwLog.e(TAG, "Couldn't find route.")
        }
    }

    private fun onReverseClicked() {
        val startPos = startPicker.selectedItemPosition
        val endPos = endPicker.selectedItemPosition

        startPicker.setSelection(endPos)
        endPicker.setSelection(startPos)
    }

    private fun startNavigating(route: RouteOptions) {
        if (navigator != null) {
            navigator!!.stop()
        }
        navigator = mapManager.navigate(route)
        navigator!!.addOnManeuverChangedListener(this)

        navOverlay.setNavigator(navigator!!)
        navOverlayContainer.visibility = View.VISIBLE
        fab.setImageResource(R.drawable.ic_clear_white)
        fab.setOnClickListener(exitNavListener)

        if (voiceEnabled) {
            voice.setImageResource(R.drawable.ic_unmuted)
            voiceStatusTextView.setText(R.string.demo_voice_prompt_unmuted)
        } else {
            voice.setImageResource(R.drawable.ic_muted)
            voiceStatusTextView.setText(R.string.demo_voice_prompt_muted)
        }

        voice.visibility = View.VISIBLE
        voiceStatusTextView.visibility = View.VISIBLE

        navigator!!.start()
    }

    private fun stopNavigating() {
        if (navigator != null) {
            navigator!!.stop()
            navigator = null
        }
        navOverlayContainer.visibility = View.GONE
        voice.visibility = View.GONE
        voiceStatusTextView.visibility = View.GONE
        fab.setImageResource(R.drawable.ic_navigation)
        fab.setOnClickListener(selectRouteListener)
    }

    private fun toggleVoice() {
        voiceEnabled = !voiceEnabled
        saveVoice(voiceEnabled)
        if (voiceEnabled) {
            voice.setImageResource(R.drawable.ic_unmuted)
            voiceStatusTextView.setText(R.string.demo_voice_prompt_unmuted)
        } else {
            voice.setImageResource(R.drawable.ic_muted)
            voiceStatusTextView.setText(R.string.demo_voice_prompt_muted)
            tts!!.stop();
        }
    }

    private fun saveVoice(enabled: Boolean) {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean("voice", enabled)
            commit()
        }
    }

    private fun textToVoice(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }

    companion object {
        private val TAG = VoicePromptActivity::class.java.simpleName
        private val ITEM_ID_LOCATION = -2
    }

}