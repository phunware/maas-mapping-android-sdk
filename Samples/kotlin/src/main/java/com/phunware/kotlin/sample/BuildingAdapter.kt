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
