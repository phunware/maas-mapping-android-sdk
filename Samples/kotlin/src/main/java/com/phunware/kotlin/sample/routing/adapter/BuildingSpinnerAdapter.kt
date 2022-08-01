package com.phunware.kotlin.sample.routing.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.phunware.kotlin.sample.R
import com.phunware.mapping.model.Building
import java.util.ArrayList

internal class BuildingSpinnerAdapter(context: Context) : ArrayAdapter<Building>(context, 0, ArrayList()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v: View
        val holder: ViewHolder
        if (convertView == null) {
            v = LayoutInflater.from(context)
                .inflate(R.layout.item_building_spinner_row, parent, false)
            holder = ViewHolder()
            holder.text = v.findViewById(android.R.id.text1)
            holder.text?.setTextColor(context.getColor(android.R.color.black))
            v.tag = holder
        } else {
            v = convertView
            holder = v.tag as ViewHolder
        }
        val building = getItem(position)
        if (building != null) {
            holder.text?.text = building.name
        }
        return v
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    class ViewHolder {
        var text: TextView? = null
    }

}