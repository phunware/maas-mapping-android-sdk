package com.phunware.mapping.sample.providers;

import android.content.Context;

import com.phunware.location.PwLocationProviderConnectivityDetector;
import com.phunware.location.manager.PwSlLocationManager;
import com.phunware.location.provider.PwLocationProvider;
import com.phunware.location.provider.PwLocationProviderFactory;
import com.phunware.location.provider.interceptor.PwFloorIdMapLocationInterceptor;
import com.phunware.mapping.model.PwBuilding;
import com.phunware.mapping.model.PwFloor;
import com.phunware.mapping.sample.R;

import java.util.HashMap;
import java.util.Map;

public final class SenionLocationProviderFactory {

    private static SenionLocationProviderFactory instance;
    private static PwLocationProviderFactory pwLocationProviderFactory;
    private Context mContext;

    private SenionLocationProviderFactory(Context context) {
        mContext = context;
        pwLocationProviderFactory = new PwLocationProviderFactory();
    }

    public static final SenionLocationProviderFactory getInstance(Context context) {
        if (instance == null) {
            instance = new SenionLocationProviderFactory(context);
        }

        return instance;
    }

    private static PwLocationProvider locationProvider;
    public PwLocationProvider createLocationProvider(PwBuilding building) {
        if (locationProvider == null) {
            locationProvider = pwLocationProviderFactory.getPwSlLocationProvider(mContext,
                    new PwLocationProviderConnectivityDetector(), new PwFloorIdMapLocationInterceptor(getFloorIdMapFromBuilding(building)),
                    new PwSlLocationManager(mContext, mContext.getResources().getString(R.string.pw_sl_map_id), mContext.getResources().getString(R.string.pw_sl_customer_id)));
        }
        return locationProvider;
    }

    /*
     * TODO This may not right for Senionlab, revisit
     */
    private final Map<String, Long> getFloorIdMapFromBuilding(PwBuilding building) {
        Map<String, Long> floorIdMap = new HashMap<String, Long>();

        if (building == null) return floorIdMap;

        for (PwFloor floor : building.getFloors()) {
            floorIdMap.put("<YOUR_PW_SL_FLOOR_IDs>", floor.getId());
        }

        return floorIdMap;
    }
}
