package com.phunware.java.sample.routing.adapter;

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

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.phunware.mapping.model.PointOptions;

import java.util.ArrayList;
import java.util.List;

public class BuildingAdapter extends ArrayAdapter<PointOptions> {
    private final String prompt;

    public BuildingAdapter(Context context, List<PointOptions> pointList, String prompt) {
        super(context, 0, new ArrayList<PointOptions>());
        addAll(pointList);
        this.prompt = prompt;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    public PointOptions getItem(int position) {
        return position == 0 ? null : super.getItem(position - 1);
    }

    @Override
    public long getItemId(int position) {
        PointOptions currentItem = getItem(position);
        if (currentItem != null) {
            return position == 0 ? -1 : currentItem.getId();
        }

        return -1;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_spinner_item, parent, false);
            holder = new ViewHolder();
            holder.text = v.findViewById(android.R.id.text1);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        if (position == 0) {
            holder.text.setText("");
            holder.text.setHint(prompt);
        } else {
            PointOptions currentItem = getItem(position);
            if (currentItem != null) {
                holder.text.setText(currentItem.getName());
            }
        }

        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            holder = new ViewHolder();
            holder.text = v.findViewById(android.R.id.text1);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        if (position == 0) {
            holder.text.setText("");
            holder.text.setHint(prompt);
        } else {
            PointOptions currentItem = getItem(position);
            if (currentItem != null) {
                holder.text.setText(currentItem.getName());
            }
        }

        return v;
    }
}

final class ViewHolder {
    TextView text;
}
