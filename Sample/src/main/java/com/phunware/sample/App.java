package com.phunware.sample;

import android.app.Application;
import android.content.Context;

import com.phunware.mapping.manager.PhunwareMapManager;
import com.phunware.mapping.manager.SharedPreferenceCache;

public class App extends Application {

    private PhunwareMapManager mapManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mapManager = PhunwareMapManager.create(this.getApplicationContext());
    }

    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }

    public PhunwareMapManager getMapManager() {
        return mapManager;
    }
}
