package com.phunware.kotlin.sample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.phunware.mapping.model.PointOptions

import java.util.ArrayList

internal class BuildingAdapter(context: Context, pointList: List<PointOptions>,
                               private val prompt: String)
    : ArrayAdapter<PointOptions>(context, 0, ArrayList()) {

    init {
        addAll(pointList)
    }

    override fun getCount(): Int = super.getCount() + 1

    override fun getItem(position: Int): PointOptions? =
            if (position == 0) null else super.getItem(position - 1)

    override fun getItemId(position: Int): Long {
        val currentItem = getItem(position)
        return if (currentItem != null) {
            if (position == 0) -1 else currentItem.id
        } else -1

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val holder: ViewHolder
        if (v == null) {
            v = LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_spinner_item, parent, false)
            holder = ViewHolder()
            holder.text = v!!.findViewById(android.R.id.text1)
            v.tag = holder
        } else {
            holder = v.tag as ViewHolder
        }

        if (position == 0) {
            holder.text.text = ""
            holder.text.hint = prompt
        } else {
            val currentItem = getItem(position)
            if (currentItem != null) {
                holder.text.text = currentItem.name
            }
        }

        return v
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val holder: ViewHolder
        if (v == null) {
            v = LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
            holder = ViewHolder()
            holder.text = v!!.findViewById(android.R.id.text1)
            v.tag = holder
        } else {
            holder = v.tag as ViewHolder
        }

        if (position == 0) {
            holder.text.text = ""
            holder.text.hint = prompt
        } else {
            val currentItem = getItem(position)
            if (currentItem != null) {
                holder.text.text = currentItem.name
            }
        }

        return v
    }
}

internal class ViewHolder {
    lateinit var text: TextView
}
