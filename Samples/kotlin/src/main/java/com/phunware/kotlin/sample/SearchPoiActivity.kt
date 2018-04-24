package com.phunware.kotlin.sample

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.phunware.core.PwCoreSession
import com.phunware.mapping.MapFragment
import com.phunware.mapping.OnPhunwareMapReadyCallback
import com.phunware.mapping.PhunwareMap
import com.phunware.mapping.manager.Callback
import com.phunware.mapping.manager.PhunwareMapManager
import com.phunware.mapping.model.Building
import com.phunware.mapping.model.FloorOptions
import com.phunware.mapping.model.PointOptions
import java.util.*

class SearchPoiActivity : AppCompatActivity(), OnPhunwareMapReadyCallback {
    private lateinit var mapManager: PhunwareMapManager
    private lateinit var phunwareMap: PhunwareMap
    private lateinit var floorSpinner: Spinner
    private lateinit var currentBuilding: Building
    private lateinit var spinnerAdapter: ArrayAdapter<FloorOptions>
    private lateinit var fab: FloatingActionButton
    private var searchPoiDialog: Dialog? = null
    private var poiListAdapter: PoiListAdapter? = null

    private var searchPoiListener: View.OnClickListener = View.OnClickListener { showSearchDialog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_poi)

        fab = findViewById(R.id.fab)
        fab.visibility = View.GONE
        fab.setOnClickListener(searchPoiListener)

        floorSpinner = findViewById(R.id.floorSpinner)
        spinnerAdapter = FloorAdapter(this)
        floorSpinner.adapter = spinnerAdapter
        floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val floor = spinnerAdapter.getItem(id.toInt())
                if (floor != null) {
                    currentBuilding.selectFloor(floor.level)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Create the map manager used to load the building
        mapManager = PhunwareMapManager.create(this)

        // Register the Phunware API keys
        PwCoreSession.getInstance().registerKeys(this)

        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getPhunwareMapAsync(this)
    }

    override fun onPhunwareMapReady(phunwareMap: PhunwareMap) {
        // Retrieve buildingId from integers.xml
        val buildingId = resources.getInteger(R.integer.buildingId)

        phunwareMap.googleMap.uiSettings.isMapToolbarEnabled = false
        phunwareMap.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this@SearchPoiActivity, R.raw.map_style))

        this.phunwareMap = phunwareMap
        mapManager.setPhunwareMap(phunwareMap)
        mapManager.addBuilding(buildingId.toLong(),
                object : Callback<Building> {
                    override fun onSuccess(building: Building) {
                        Log.d(TAG, "Building loaded successfully")
                        currentBuilding = building

                        // Populate floor spinner
                        spinnerAdapter.clear()
                        spinnerAdapter.addAll(building.buildingOptions.floors)

                        // Set building to initial floor value
                        val initialFloor = building.initialFloor()
                        building.selectFloor(initialFloor.level)

                        // Animate the camera to the building at an appropriate zoom level
                        val cameraUpdate = CameraUpdateFactory
                                .newLatLngBounds(initialFloor.bounds, 4)
                        phunwareMap.googleMap.animateCamera(cameraUpdate)

                        showFab(true)
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.d(TAG, "Error when loading building -- " + throwable.message)
                        showFab(false)
                    }
                })
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

    private fun showSearchDialog() {
        val builder = AlertDialog.Builder(this)

        // Load custom dialog view
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_search_poi, null)
        initDialogUI(dialogView)

        builder.setView(dialogView)
                .setTitle("Search POIs")
                .setMessage("Type to search for POIs and click to center the map to that POIs" +
                        " location")
                .setNegativeButton("Close", { _, _ ->
                    // Do Nothing -- Close Dialog
                })
                .setCancelable(true)
        searchPoiDialog = builder.create()
        searchPoiDialog!!.show()
    }

    private fun initDialogUI(dialogView: View) {
        val searchPoi = dialogView.findViewById<EditText>(R.id.search_poi)
        val searchPoiRecyclerView = dialogView.findViewById<RecyclerView>(R.id.poi_recycler_view)
        val recyclerViewLayoutManager = LinearLayoutManager(this)
        searchPoiRecyclerView.layoutManager = recyclerViewLayoutManager

        val points = ArrayList<PointOptions>()
        currentBuilding.floorOptions
                .filter { it != null && it.poiOptions != null }
                .forEach { points.addAll(it.poiOptions) }

        poiListAdapter = PoiListAdapter(points)
        searchPoiRecyclerView.adapter = poiListAdapter

        searchPoi.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                refreshList(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })

    }

    private fun refreshList(newText: String) {
        poiListAdapter!!.setFilter(newText)
        poiListAdapter!!.notifyDataSetChanged()
    }

    private inner class PoiListAdapter internal constructor(pois: List<PointOptions>) : RecyclerView.Adapter<PoiListAdapter.ViewHolder>() {

        private val arrAllPointOptions = ArrayList<PointOptions>()
        private val arrPointOptions = ArrayList<PointOptions>()

        init {
            arrAllPointOptions.addAll(pois)
            arrPointOptions.addAll(pois)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val poi = arrPointOptions[position]
            if (poi.name != null) {
                holder.poiNameTextView.text = poi.name
            }
        }

        override fun getItemCount(): Int = arrPointOptions.size

        internal fun setFilter(filter: String?) {
            if (filter != null) {
                arrPointOptions.clear()
                arrAllPointOptions.filterTo(arrPointOptions) {
                    it.name.toUpperCase().startsWith(filter.toUpperCase())
                }
            }
        }

        internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
            var poiNameTextView: TextView = itemView.findViewById(android.R.id.text1)

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                val position = adapterPosition
                val selectedPoi = arrPointOptions[position]
                val poiLocation = selectedPoi.location

                // Center the camera to the poi that was selected
                val bounds = LatLngBounds(poiLocation, poiLocation)
                val cameraUpdate = CameraUpdateFactory
                        .newLatLngBounds(bounds, 4)
                phunwareMap.googleMap.animateCamera(cameraUpdate)

                // Switch floors if necessary
                if (selectedPoi.level != currentBuilding.selectedFloor.level) {
                    currentBuilding.selectFloor(selectedPoi.level)

                    // Update floor spinner
                    val selectedPosition = floorSpinner.selectedItemPosition
                    for (i in 0 until spinnerAdapter.count) {
                        val floor = spinnerAdapter.getItem(i)
                        if (selectedPosition != i && floor != null && floor.id == selectedPoi.floorId) {
                            floorSpinner.setSelection(i)
                        }
                    }
                }

                if (searchPoiDialog != null) {
                    searchPoiDialog!!.dismiss()
                }
            }
        }
    }

    companion object {
        private val TAG = SearchPoiActivity::class.java.simpleName
    }
}
