package com.phunware.kotlin.sample

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.phunware.mapping.manager.PhunwareMapManager

class ApplicationObserver(private val phunwareMapManager: PhunwareMapManager) : LifecycleObserver {

    private var didDisableLocationUpdatesOnBackground = false

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onBackground() {
        val shouldStopLocationUpdates = phunwareMapManager.isMyLocationEnabled

        if (shouldStopLocationUpdates) {
            phunwareMapManager.isMyLocationEnabled = false
            didDisableLocationUpdatesOnBackground = true
        } else {
            didDisableLocationUpdatesOnBackground = false
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onForeground() {
        val shouldStartLocationUpdates = !phunwareMapManager.isMyLocationEnabled && didDisableLocationUpdatesOnBackground

        if (shouldStartLocationUpdates) {
            phunwareMapManager.isMyLocationEnabled = true
        }
    }

}