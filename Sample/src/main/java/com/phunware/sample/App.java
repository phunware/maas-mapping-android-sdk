package com.phunware.sample;

import android.app.Application;
import android.content.Context;

import com.phunware.core.PwActivityLifecycleCallback;
import com.phunware.mapping.manager.PhunwareMapManager;

public class App extends Application {

    private PhunwareMapManager mapManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mapManager = PhunwareMapManager.create(this.getApplicationContext());
        this.registerActivityLifecycleCallbacks(new PwActivityLifecycleCallback());
    }

    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }

    public PhunwareMapManager getMapManager() {
        return mapManager;
    }
}
