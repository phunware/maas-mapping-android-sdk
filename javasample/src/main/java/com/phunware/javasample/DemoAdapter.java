package com.phunware.javasample;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

class DemoAdapter extends RecyclerView.Adapter<DemoAdapter.ViewHolder> {
    private List<Demo> demos;
    private DemoOnClickListener listener;

    interface DemoOnClickListener {
        void onItemClicked(String title);
    }

    DemoAdapter(List<Demo> demos, DemoOnClickListener listener) {
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

    Demo getItem(String title) {
        if (!TextUtils.isEmpty(title)) {
            for (Demo demo : demos) {
                if (demo.getTitle().equals(title)) {
                    return demo;
                }
            }
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
