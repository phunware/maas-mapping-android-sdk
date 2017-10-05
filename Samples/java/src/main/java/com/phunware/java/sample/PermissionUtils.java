package com.phunware.java.sample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

final class PermissionUtils {
    private static final int REQUEST_PERMISSION_LOCATION_FINE = 1;

    // Class should not be instantiated
    private PermissionUtils() {
    }

    static void checkPermissions(Activity activity) {
        if (activity != null && !canAccessLocation(activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_PHONE_STATE},
                        REQUEST_PERMISSION_LOCATION_FINE);
            }
        }
    }

    static boolean canAccessLocation(Activity activity) {
        return (hasPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private static boolean hasPermission(Activity activity, String perm) {
        //noinspection SimplifiableIfStatement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(perm));
        }
        return true;
    }
}
