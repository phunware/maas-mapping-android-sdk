package com.phunware.mapping.sample.providers;

import android.content.Context;

import com.phunware.location.provider.PwLocationProvider;
import com.phunware.location.provider.PwLocationProviderFactory;
import com.phunware.mapping.model.PwBuilding;

public final class FusedLocationProviderFactory {

    private static FusedLocationProviderFactory instance;
    private static PwLocationProviderFactory pwLocationProviderFactory;
    private Context mContext;

    private FusedLocationProviderFactory(Context context) {
        mContext = context;
        pwLocationProviderFactory = new PwLocationProviderFactory();
    }

    public static final FusedLocationProviderFactory getInstance(Context context) {
        if (instance == null) {
            instance = new FusedLocationProviderFactory(context);
        }

        return instance;
    }

    private static PwLocationProvider locationProvider;
    public PwLocationProvider createLocationProvider(PwBuilding building) {
        if (locationProvider == null) {
            locationProvider = pwLocationProviderFactory.getPwFusedLocationProvider(mContext);
        }
        return locationProvider;
    }
}
