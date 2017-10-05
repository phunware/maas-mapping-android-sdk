package com.phunware.kotlinsample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.phunware.mapping.model.FloorOptions

import java.util.ArrayList

internal class FloorAdapter(context: Context) : ArrayAdapter<FloorOptions>(context, 0, ArrayList()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val holder: ViewHolder
        if (v == null) {
            v = LayoutInflater.from(
                    context).inflate(R.layout.floor_spinner_row, parent, false)
            holder = ViewHolder()
            holder.text = v!!.findViewById(android.R.id.text1)
            v.tag = holder
        } else {
            holder = v.tag as ViewHolder
        }
        val floor = getItem(position)
        if (floor != null) {
            holder.text!!.text = floor.name
        }

        return v
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    private class ViewHolder {
        internal var text: TextView? = null
    }
}
