package com.phunware.kotlin.sample

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.phunware.mapping.manager.PhunwareMapManager

internal class App : Application() {

    private lateinit var applicationObserver: ApplicationObserver

    lateinit var mapManager: PhunwareMapManager
        private set

    fun initMapManager() {
        mapManager = PhunwareMapManager.create(this)
        applicationObserver = ApplicationObserver(mapManager)
        ProcessLifecycleOwner
            .get()
            .lifecycle
            .addObserver(applicationObserver)
    }
}