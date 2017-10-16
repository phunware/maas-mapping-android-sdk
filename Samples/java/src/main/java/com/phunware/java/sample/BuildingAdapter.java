package com.phunware.java.sample;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.phunware.mapping.model.PointOptions;

import java.util.ArrayList;
import java.util.List;

class BuildingAdapter extends ArrayAdapter<PointOptions> {
    private final String prompt;

    BuildingAdapter(Context context, List<PointOptions> pointList, String prompt) {
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
