package com.phunware.mapping.demo;

import android.app.Application;

import com.phunware.core.PwCoreSession;
import com.phunware.mapping.PwMappingModule;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PwCoreSession.getInstance().installModules(PwMappingModule.getInstance());
        PwCoreSession.getInstance().registerKeys(this,
                getString(R.string.app_id),
                getString(R.string.app_access_key),
                getString(R.string.app_signature_key),
                getString(R.string.app_encryption_key));
    }
}
