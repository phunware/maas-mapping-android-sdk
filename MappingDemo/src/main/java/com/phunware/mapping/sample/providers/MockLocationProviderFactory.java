package com.phunware.mapping.sample.providers;

import android.content.Context;
import android.text.TextUtils;

import com.phunware.location.provider.PwLocationProvider;
import com.phunware.location.provider.PwLocationProviderFactory;
import com.phunware.location.provider.PwMockLocationProvider;

public final class MockLocationProviderFactory {

    private static MockLocationProviderFactory instance;
    private static PwLocationProviderFactory pwLocationProviderFactory;
    private Context mContext;

    private MockLocationProviderFactory(Context context) {
        mContext = context;
        pwLocationProviderFactory = new PwLocationProviderFactory();
    }

    public static final MockLocationProviderFactory getInstance(Context context) {
        if (instance == null) {
            instance = new MockLocationProviderFactory(context);
        }

        return instance;
    }

    private static PwMockLocationProvider locationProvider;
    public PwLocationProvider createLocationProvider(String jsonFileName, PwMockLocationProvider.MockLocationsDisabledListener listener, boolean repeat) {
        if (locationProvider == null) {
            locationProvider = (PwMockLocationProvider) pwLocationProviderFactory.getPwMockLocationProvider(mContext, jsonFileName, listener, repeat);
        }
        if (locationProvider.isRepeating() != repeat) {
            locationProvider.setRepeating(repeat);
        }
        if (!TextUtils.equals(jsonFileName, locationProvider.getJsonFile())) {
            locationProvider.setJsonFile(jsonFileName);
        }
        return locationProvider;
    }
}
