package com.phunware.kotlinsample

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import com.phunware.kotlinsample.LocationModesActivity.Companion.REQUEST_PERMISSION_LOCATION_FINE

internal object PermissionUtils {
    fun checkPermissions(activity: Activity?) {
        if (activity != null && !canAccessLocation(activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE), REQUEST_PERMISSION_LOCATION_FINE)
            }
        }
    }

    fun canAccessLocation(activity: Activity): Boolean =
            hasPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)

    private fun hasPermission(activity: Activity, perm: String): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(perm)
        } else true
    }
}