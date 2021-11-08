package com.phunware.kotlin.sample

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.phunware.mapping.manager.PhunwareMapManager
import com.phunware.mapping.model.Campus

class App : Application() {

    private lateinit var applicationObserver: ApplicationObserver

    lateinit var mapManager: PhunwareMapManager
        private set

    var campus: Campus? = null

    fun initMapManager() {
        this.mapManager = PhunwareMapManager.create(this)
        applicationObserver = ApplicationObserver(mapManager)
        ProcessLifecycleOwner
            .get()
            .lifecycle
            .addObserver(applicationObserver)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun stopObservingApplication() {
        ProcessLifecycleOwner
                .get()
                .lifecycle
                .removeObserver(applicationObserver)
    }
}