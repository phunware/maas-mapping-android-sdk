package com.phunware.kotlin.sample

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

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phunware.kotlin.sample.building.adapter.FloorAdapter
import com.phunware.mapping.manager.Callback
import com.phunware.mapping.manager.PhunwareMapManager
import com.phunware.mapping.model.Building
import com.phunware.mapping.model.FloorOptions
import com.phunware.mapping.model.PointOptions

class LoadBuildingWithoutMapActivity : AppCompatActivity() {
    private lateinit var mapManager: PhunwareMapManager
    private lateinit var currentBuilding: Building
    private lateinit var spinnerAdapter: ArrayAdapter<FloorOptions>
    private val poiAdapter = PoiAdapter()
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_building_no_map)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = poiAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL))

        val floorSpinner = findViewById<Spinner>(R.id.floorSpinner)
        spinnerAdapter = FloorAdapter(this)
        floorSpinner.adapter = spinnerAdapter
        floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val floor = spinnerAdapter.getItem(id.toInt())
                if (floor != null) {
                    currentBuilding.selectFloor(floor.level)
                    val floorOptions: FloorOptions = currentBuilding.floorOptions.first { it.level == floor.level }
                    poiAdapter.setPois(floorOptions.poiOptions)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Create the map manager used to load the building
        mapManager = PhunwareMapManager.create(this)

        // Retrieve buildingId from integers.xml
        val buildingId = resources.getInteger(R.integer.buildingId)
        loadBuilding(buildingId.toLong())

    }

    /**
     * Load building with the specified Id
     */
    private fun loadBuilding(buildingId: Long) {
        mapManager.loadBuilding(buildingId, object : Callback<Building> {

            override fun onSuccess(building: Building) {
                Log.d(TAG, "Building " + building.id + " fully loaded")
                currentBuilding = building

                // Populate floor spinner
                spinnerAdapter.clear()
                spinnerAdapter.addAll(building.buildingOptions.floors)

                val initialFloorOptions: FloorOptions = building.initialFloor
                building.selectFloor(initialFloorOptions.level)
                Log.d(TAG, "Selected floor : " + initialFloorOptions.level)
                poiAdapter.setPois(initialFloorOptions.poiOptions)
            }

            override fun onFailure(throwable: Throwable?) {
                Log.d(TAG, "Error loading building : " + throwable.toString())
                handler.post {
                    val error = getString(R.string.building_not_loaded) + " : " + throwable?.message
                    Toast.makeText(this@LoadBuildingWithoutMapActivity, error, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        })
    }

    internal class PoiAdapter : RecyclerView.Adapter<PoiAdapter.ViewHolder>() {

        private val pois: MutableList<PointOptions> = mutableListOf()

        fun setPois(pois: List<PointOptions>) {
            this.pois.clear()
            this.pois.addAll(pois)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_info, parent, false)
            val viewHolder = ViewHolder(view)
            return viewHolder
        }

        override fun getItemCount(): Int {
            return pois.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val poi = pois.get(position)
            holder.name.setText(poi.name)
        }

        internal class ViewHolder : RecyclerView.ViewHolder {
            var name: TextView
            var direction: ImageView

            constructor(itemView: View) : super(itemView) {
                name = itemView.findViewById(R.id.name)
                direction = itemView.findViewById(R.id.direction)
                direction.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapManager?.let {
            it.onDestroy()
        }
    }

    companion object {
        private val TAG = LoadBuildingWithoutMapActivity::class.java.simpleName
    }
}
