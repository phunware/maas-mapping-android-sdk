package com.phunware.kotlin.sample

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.phunware.mapping.manager.PhunwareMapManager

internal class ApplicationObserver(private val phunwareMapManager: PhunwareMapManager) :
    DefaultLifecycleObserver {

    private var didDisableLocationUpdatesOnBackground = false

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)

        val shouldStopLocationUpdates = phunwareMapManager.isMyLocationEnabled

        if (shouldStopLocationUpdates) {
            phunwareMapManager.isMyLocationEnabled = false
            didDisableLocationUpdatesOnBackground = true
        } else {
            didDisableLocationUpdatesOnBackground = false
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        val shouldStartLocationUpdates = !phunwareMapManager.isMyLocationEnabled && didDisableLocationUpdatesOnBackground

        if (shouldStartLocationUpdates) {
            phunwareMapManager.isMyLocationEnabled = true
        }
    }

}