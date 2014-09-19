package com.phunware.mapping.sample.providers;

import android.content.Context;

import com.phunware.location.PwLocationProviderConnectivityDetector;
import com.phunware.location.provider.PwLocationProvider;
import com.phunware.location.provider.PwLocationProviderFactory;
import com.phunware.location.provider.interceptor.PwFloorIdMapLocationInterceptor;
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
            PwFloorIdMapLocationInterceptor floorIdMapLocationInterceptor = new PwFloorIdMapLocationInterceptor(null);
            locationProvider = pwLocationProviderFactory.getPwMseLocationProvider(mContext, connectivityDetector, floorIdMapLocationInterceptor, building.getVenueGuid());
        }
        return locationProvider;
    }
}
