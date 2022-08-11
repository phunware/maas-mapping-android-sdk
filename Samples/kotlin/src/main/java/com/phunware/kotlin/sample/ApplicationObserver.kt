package com.phunware.kotlin.sample

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.phunware.mapping.manager.PhunwareMapManager

internal class ApplicationObserver(private val phunwareMapManager: PhunwareMapManager) :
    DefaultLifecycleObserver {

    private var didDisableLocationUpdatesOnBackground = false

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)

        if (phunwareMapManager.isMyLocationEnabled) {
            phunwareMapManager.isMyLocationEnabled = false
            didDisableLocationUpdatesOnBackground = true
        } else {
            didDisableLocationUpdatesOnBackground = false
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        if (!phunwareMapManager.isMyLocationEnabled && didDisableLocationUpdatesOnBackground) {
            phunwareMapManager.isMyLocationEnabled = true
        }
    }

}