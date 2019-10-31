package com.phunware.java.sample.config;

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

import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.phunware.java.sample.R;

import java.util.List;

public class DemoAdapter extends RecyclerView.Adapter<DemoAdapter.ViewHolder> {
    private List<Demo> demos;
    private DemoOnClickListener listener;

    public interface DemoOnClickListener {
        void onItemClicked(String title);
    }

    public DemoAdapter(List<Demo> demos, DemoOnClickListener listener) {
        this.demos = demos;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.demo_row, parent, false);
        return new ViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Demo d = demos.get(position);
        holder.title.setText(d.getTitle());
        holder.description.setText(d.getDescription());
    }

    @Override
    public int getItemCount() {
        return demos.size();
    }

    public Demo getItem(String title) {
        if (!TextUtils.isEmpty(title)) {
            for (Demo demo : demos) {
                if (demo.getTitle().equals(title)) {
                    return demo;
                }
            }
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView description;
        DemoOnClickListener listener;

        ViewHolder(View itemView, DemoOnClickListener listener) {
            super(itemView);
            this.listener = listener;
            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onItemClicked(title.getText().toString());
            }
        }
    }
}
