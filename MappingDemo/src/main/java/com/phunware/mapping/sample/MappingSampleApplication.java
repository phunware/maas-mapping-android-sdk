package com.phunware.mapping.sample;

import android.app.Application;

import com.phunware.core.PwCoreSession;
import com.phunware.core.PwLog;
import com.phunware.mapping.PwMappingModule;

public class MappingSampleApplication extends Application {

    private static final int DEF_TTL_MAPPING = 900; // 30 minutes

    @Override
    public void onCreate() {
        super.onCreate();

        String appId = getString(R.string.app_id);
        String accessKey = getString(R.string.app_access_key);
        String signatureKey = getString(R.string.app_signature_key);
        String encryptionKey = getString(R.string.app_encryption_key);

        PwLog.setShowLog(true);

        PwCoreSession.getInstance().installModules(PwMappingModule.getInstance());

        PwCoreSession.getInstance().registerKeys(this, appId, accessKey, signatureKey, encryptionKey);

        PwMappingModule.getInstance().setModuleHttpCacheTtl(DEF_TTL_MAPPING); // Zero means no caching
    }
}