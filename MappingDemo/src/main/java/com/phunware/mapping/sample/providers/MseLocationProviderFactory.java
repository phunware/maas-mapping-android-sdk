package com.phunware.mapping.sample.providers;

import android.content.Context;

import com.phunware.location.PwLocationProviderConnectivityDetector;
import com.phunware.location.provider.PwLocationProvider;
import com.phunware.location.provider.PwLocationProviderFactory;
import com.phunware.mapping.model.PwBuilding;

public final class MseLocationProviderFactory {

    private static MseLocationProviderFactory instance;
    private static PwLocationProviderFactory pwLocationProviderFactory;
    private Context mContext;

    private MseLocationProviderFactory(Context context) {
        mContext = context;
        pwLocationProviderFactory = new PwLocationProviderFactory();
    }

    public static final MseLocationProviderFactory getInstance(Context context) {
        if (instance == null) {
            instance = new MseLocationProviderFactory(context);
        }

        return instance;
    }

    private static PwLocationProvider locationProvider;
    public PwLocationProvider createLocationProvider(PwBuilding building) {
        if (locationProvider == null) {
            PwLocationProviderConnectivityDetector connectivityDetector = new PwLocationProviderConnectivityDetector();
            locationProvider = pwLocationProviderFactory.getPwMseLocationProvider(mContext, connectivityDetector, building.getVenueGuid());
        }
        return locationProvider;
    }
}
