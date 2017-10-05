package com.phunware.javasample;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.phunware.mapping.model.FloorOptions;

import java.util.ArrayList;

class FloorAdapter extends ArrayAdapter<FloorOptions> {

    FloorAdapter(Context context) {
        super(context, 0, new ArrayList<FloorOptions>());
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            v = LayoutInflater.from(
                    getContext()).inflate(R.layout.spinner_row, parent, false);
            holder = new ViewHolder();
            holder.text = v.findViewById(android.R.id.text1);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        FloorOptions floor = getItem(position);
        if (floor != null) {
            holder.text.setText(floor.getName());
        }

        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    private static class ViewHolder {
        TextView text;
    }
}
