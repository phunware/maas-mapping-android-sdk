package com.phunware.kotlin.sample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.RelativeLayout

class MainActivity : AppCompatActivity(), DemoAdapter.DemoOnClickListener {
    private lateinit var demoAdapter: DemoAdapter
    private lateinit var content: RelativeLayout
    private var permissionsSnackbar: Snackbar? = null
    private var clickedDemo: Demo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
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