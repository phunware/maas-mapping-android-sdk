package com.phunware.mapping.sample.providers;

import android.content.Context;
import android.location.LocationManager;

import com.phunware.location.PwLocationProviderConnectivityDetector;
import com.phunware.location.manager.PwQcLocationManager;
import com.phunware.location.provider.PwLocationProvider;
import com.phunware.location.provider.PwLocationProviderFactory;
import com.phunware.location.provider.interceptor.PwFloorIdMapLocationInterceptor;
import com.phunware.mapping.model.PwBuilding;
import com.phunware.mapping.model.PwFloor;

import java.util.HashMap;
import java.util.Map;

public final class QualcommLocationProviderFactory {

    private static QualcommLocationProviderFactory instance;
    private static PwLocationProviderFactory pwLocationProviderFactory;
    private Context mContext;

    private QualcommLocationProviderFactory(Context context) {
        mContext = context;
        pwLocationProviderFactory = new PwLocationProviderFactory();
    }

    public static final QualcommLocationProviderFactory getInstance(Context context) {
        if (instance == null) {
            instance = new QualcommLocationProviderFactory(context);
        }

        return instance;
    }

    private static PwLocationProvider locationProvider;
    public PwLocationProvider createLocationProvider(PwBuilding building) {
        if (locationProvider == null) {
            PwLocationProviderConnectivityDetector connectivityDetector = new PwLocationProviderConnectivityDetector();
            PwFloorIdMapLocationInterceptor floorIdMapLocationInterceptor = new PwFloorIdMapLocationInterceptor(getFloorIdMapFromBuilding(building));
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            PwQcLocationManager pwQcLocationManager = new PwQcLocationManager(mContext);
            locationProvider = pwLocationProviderFactory.getPwQcLocationProvider(mContext, connectivityDetector, floorIdMapLocationInterceptor, locationManager, pwQcLocationManager);
        }
        return locationProvider;
    }

    private final Map<String, Long> getFloorIdMapFromBuilding(PwBuilding building) {
        Map<String, Long> floorIdMap = new HashMap<String, Long>();

        if (building == null) return floorIdMap;
        for (PwFloor floor : building.getFloors()) {
            floorIdMap.put(floor.getLocationMapHierarchy(), floor.getId());
        }

        return floorIdMap;
    }
}
