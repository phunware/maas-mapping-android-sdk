package com.phunware.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.phunware.mapping.manager.PhunwareMapManager;
import java.util.ArrayList;
import java.util.List;

public class DropDownSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_OPTION = "selected";
    private static final String EXTRA_OPTIONS = "options";

    private Spinner optionSpinner;
    private Button cancel;
    private Button next;

    private ArrayList<String> options;
    private String selectedOption;
    public static void startForResult(Activity activity, List<String> providers,
            String selectedProvider, int requestCode) {
        Intent intent = new Intent(activity, DropDownSelectionActivity.class);

        intent.putStringArrayListExtra(EXTRA_OPTIONS, new ArrayList<>(providers));
        intent.putExtra(EXTRA_SELECTED_OPTION, selectedProvider);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_dropdown_select);
        optionSpinner = (Spinner) findViewById(R.id.dropdownSpinner);
        cancel = (Button) findViewById(R.id.btn_cancel);
        next = (Button) findViewById(R.id.btn_confirm);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextClicked();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClicked();
            }
        });

        options = getIntent().getExtras().getStringArrayList(EXTRA_OPTIONS);
        optionSpinner.setAdapter(new ProviderAdapter(this, options));
        selectedOption = getIntent().getExtras().getString(EXTRA_SELECTED_OPTION);

        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).equals(selectedOption)) {
                optionSpinner.setSelection(i);
                break;
            }
        }
    }

    private void onNextClicked() {
        PhunwareMapManager mapManager = App.get(this).getMapManager();

        Intent data = new Intent();
        data.putExtra(EXTRA_SELECTED_OPTION, (String) optionSpinner.getSelectedItem());
        setResult(RESULT_OK, data);
        finish();
    }

    private void onCancelClicked() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private class ProviderAdapter extends ArrayAdapter<String> {
        public ProviderAdapter(Context context, List<String> pointList) {
            super(context, 0, new ArrayList<String>());
            addAll(pointList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;
            if (v == null) {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_spinner_item, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) v.findViewById(android.R.id.text1);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            holder.text.setText(getItem(position));

            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;
            if (v == null) {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) v.findViewById(android.R.id.text1);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            holder.text.setText(getItem(position));

            return v;
        }
    }

    private static final class ViewHolder {
        TextView text;
    }
}
