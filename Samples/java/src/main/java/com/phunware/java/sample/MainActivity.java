package com.phunware.java.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements DemoAdapter.DemoOnClickListener {
    private DemoAdapter demoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Instantiate all demo info
        DemoDetailsList demoDetails = new DemoDetailsList(this);

        // Create list of all demos
        RecyclerView demoRecyclerView = findViewById(R.id.demo_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        demoRecyclerView.setLayoutManager(layoutManager);
        demoAdapter = new DemoAdapter(demoDetails.getDemos(), this);
        demoRecyclerView.setAdapter(demoAdapter);
    }

    @Override
    public void onItemClicked(String title) {
        Demo demo = demoAdapter.getItem(title);
        if (demo != null) {
            startActivity(new Intent(this, demo.getActivityClass()));
        }
    }
}
