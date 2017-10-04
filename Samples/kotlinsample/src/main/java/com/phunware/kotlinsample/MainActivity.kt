package com.phunware.kotlinsample

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

class MainActivity : AppCompatActivity(), DemoAdapter.DemoOnClickListener {
    private var demoAdapter: DemoAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        // Instantiate all demo info
        val demoDetails = DemoDetailsList(this)

        // Create list of all demos
        val demoRecyclerView = findViewById<RecyclerView>(R.id.demo_list)
        val layoutManager = LinearLayoutManager(this)
        demoRecyclerView.layoutManager = layoutManager
        demoAdapter = DemoAdapter(demoDetails.getDemos(), this)
        demoRecyclerView.adapter = demoAdapter
    }

    override fun onItemClicked(title: String) {
        val demo = demoAdapter!!.getItem(title)
        if (demo != null) {
            startActivity(Intent(this, demo.activityClass))
        }
    }
}
