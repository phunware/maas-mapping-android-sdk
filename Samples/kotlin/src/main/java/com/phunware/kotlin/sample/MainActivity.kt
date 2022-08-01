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
import android.os.Build
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import com.phunware.kotlin.sample.config.DemoAdapter
import com.phunware.kotlin.sample.config.DemoDetailsList

internal class MainActivity : AppCompatActivity() {

    private lateinit var contentView: RelativeLayout

    private var onActivityResultSuccess = {}

    private val isAndroidSDKVersion31OrHigher: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    private val permissions = mutableSetOf<String>().apply {
        if (isAndroidSDKVersion31OrHigher) {
            add(Manifest.permission.BLUETOOTH_SCAN)
        }

        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
    }.toTypedArray()

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        RequestMultiplePermissions()
    ) { permissions ->
        permissions.filter {
            !it.value
        }.keys.filter {
            shouldShowRequestPermissionRationale(it)
        }.run {
            if (isNotEmpty() || isMissingRequiredPermissions()) {
                showMissingPermissionsSnackbar()
            } else {
                onActivityResultSuccess()
            }
        }
    }

    private val startActivityForResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) {
        onActivityResultSuccess()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        contentView = findViewById(R.id.content)
        val demoList = DemoDetailsList(this)
        val demoItemOnClickListener = DemoAdapter.DemoOnClickListener {
            requestPermissions {
                startActivity(Intent(this, it.activityClass))
            }
        }
        val demoRecyclerView = findViewById<RecyclerView>(R.id.demo_list)
        val layoutManager = LinearLayoutManager(this)
        demoRecyclerView.layoutManager = layoutManager
        demoRecyclerView.adapter = DemoAdapter(demoList.getDemos(), demoItemOnClickListener)
        (application as App).initMapManager()

        permissions.filter {
            !hasPermission(it)
        }.filter {
            shouldShowRequestPermissionRationale(it)
        }.run {
            if (isNotEmpty() || isMissingRequiredPermissions()) {
                showMissingPermissionsSnackbar()
            } else {
                requestPermissions()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as App).stopObservingApplication()
    }

    private fun requestPermissions(onSuccess: () -> (Unit) = {}) {
        onActivityResultSuccess = onSuccess
        requestMultiplePermissionsLauncher.launch(permissions)
    }

    private fun startAndroidSettingsActivity() {
        onActivityResultSuccess = {
            requestPermissions()
        }
        startActivityForResultLauncher.launch(Intent(android.provider.Settings.ACTION_SETTINGS))
    }

    private fun showMissingPermissionsSnackbar() {
        if (isAndroidSDKVersion31OrHigher) {
            if (!canAccessPreciseLocation() && !canLookForBluetoothDevices()) {
                R.string.permission_location_and_bluetooth_scan_snackbar_message
            } else if (!canAccessPreciseLocation()) {
                R.string.permission_location_snackbar_message
            } else if (!canLookForBluetoothDevices()) {
                R.string.permission_bluetooth_scan_snackbar_message
            } else {
                null
            }
        } else {
            if (!canAccessPreciseLocation()) {
                R.string.permission_snackbar_message
            } else {
                null
            }
        }?.let { messageResourceId ->
            Snackbar.make(
                contentView,
                messageResourceId,
                Snackbar.LENGTH_LONG
            ).setAction(
                R.string.action_settings
            ) {
                startAndroidSettingsActivity()
            }.show()
        }
    }

    private fun canAccessPreciseLocation() =
        hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun canLookForBluetoothDevices() = if (isAndroidSDKVersion31OrHigher) {
        hasPermission(Manifest.permission.BLUETOOTH_SCAN)
    } else {
        true
    }

    private fun hasPermission(perm: String) =
        PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm)

    private fun isMissingRequiredPermissions() = if (isAndroidSDKVersion31OrHigher) {
        !canAccessPreciseLocation() || !canLookForBluetoothDevices()
    } else {
        !canAccessPreciseLocation()
    }
}
