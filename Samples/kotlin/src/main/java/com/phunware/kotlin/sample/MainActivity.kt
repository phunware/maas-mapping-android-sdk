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
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
    private var permissionsSnackbar: Snackbar? = null
    private var clickedDemo: Demo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        content = findViewById(R.id.content)

        // Instantiate all demo info
        val demoDetails = DemoDetailsList(this)

        // Create list of all demos
        val demoRecyclerView = findViewById<RecyclerView>(R.id.demo_list)
        val layoutManager = LinearLayoutManager(this)
        demoRecyclerView.layoutManager = layoutManager
        demoAdapter = DemoAdapter(demoDetails.getDemos(), this)
        demoRecyclerView.adapter = demoAdapter

        checkPermissions(this)
    }

    override fun onItemClicked(title: String) {
        clickedDemo = demoAdapter.getItem(title)
        if (clickedDemo != null) {
            if (canAccessLocation()) {
                startDemo(clickedDemo!!)
            } else {
                checkPermissions(this)
            }
        }
    }

    private fun startDemo(demo: Demo) {
        startActivity(Intent(this, demo.activityClass))
    }

    /**
     * Permission Checks
     */

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION_FINE) {
            if (canAccessLocation()) {
                if (permissionsSnackbar != null) {
                    permissionsSnackbar!!.dismiss()
                }
                if (clickedDemo != null) {
                    startDemo(clickedDemo!!)
                    clickedDemo = null
                }
            } else if (!canAccessLocation()) {
                permissionsSnackbar = Snackbar.make(content, R.string.permission_snackbar_message,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_settings, {
                            startActivityForResult(
                                    Intent(android.provider.Settings.ACTION_SETTINGS),
                                    REQUEST_PERMISSION_LOCATION_FINE)
                        })
                permissionsSnackbar!!.show()
            }
        }
    }

    private fun checkPermissions(activity: Activity) : Boolean {
        if (!canAccessLocation()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE),
                        REQUEST_PERMISSION_LOCATION_FINE)
                return false
            }
        }
        return true
    }

    private fun canAccessLocation(): Boolean =
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun hasPermission(perm: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm)
        } else true
    }

    companion object {
        private val REQUEST_PERMISSION_LOCATION_FINE = 1
    }
}
