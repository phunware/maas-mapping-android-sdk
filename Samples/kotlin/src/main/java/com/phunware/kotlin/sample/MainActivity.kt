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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.RelativeLayout
import com.phunware.kotlin.sample.config.Demo
import com.phunware.kotlin.sample.config.DemoAdapter
import com.phunware.kotlin.sample.config.DemoDetailsList

class MainActivity : AppCompatActivity(), DemoAdapter.DemoOnClickListener {
    private lateinit var demoAdapter: DemoAdapter
    private lateinit var content: RelativeLayout

    private var permissionSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        content = findViewById(R.id.content)
        val demoList = DemoDetailsList(this)
        val demoRecyclerView = findViewById<RecyclerView>(R.id.demo_list)
        val layoutManager = LinearLayoutManager(this)
        demoRecyclerView.layoutManager = layoutManager
        demoAdapter = DemoAdapter(demoList.getDemos(), this)
        demoRecyclerView.adapter = demoAdapter

        if (!canAccessLocation()) {
            requestLocationPermission()
        }
    }

    override fun onItemClicked(demo: Demo) {
        if (canAccessLocation()) {
            startActivity(Intent(this, demo.activityClass))
        } else {
            requestLocationPermission()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION_FINE) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showPermissionSnackbar()
            }
        }
    }

    private fun showPermissionSnackbar() {
        permissionSnackbar = Snackbar.make(content, R.string.permission_snackbar_message, Snackbar.LENGTH_LONG)
            .setAction(R.string.action_settings) {
                startActivityForResult(
                    Intent(android.provider.Settings.ACTION_SETTINGS),
                    REQUEST_PERMISSION_LOCATION_FINE
                )
            }
        permissionSnackbar?.show()
    }

    private fun requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionSnackbar()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE),
                REQUEST_PERMISSION_LOCATION_FINE
            )
        }
    }

    private fun canAccessLocation(): Boolean = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val REQUEST_PERMISSION_LOCATION_FINE = 1
    }

}
