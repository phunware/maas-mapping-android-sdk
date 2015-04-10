package com.phunware.mapping.sample.maps;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.phunware.mapping.library.maps.PwBuildingMapManager;

public class BuildingMapManager extends PwBuildingMapManager {

    private MapOverlayManagerBuilder.SetupMapDataListener mSetupMapDataListener;

    public void setSetupMapDataListener(MapOverlayManagerBuilder.SetupMapDataListener mSetupMapDataListener) {
        this.mSetupMapDataListener = mSetupMapDataListener;
    }

    public BuildingMapManager(MapOverlayManagerBuilder builder) {
        super(builder);

        this.setSetupMapDataListener(builder.getSetupMapDataListener());
    }

    /**
     * Sets up the map data
     *
     * @param context       the application context
     * @param buildingId    the ID of the building
     * @param markerIcon    the marker icon to use
     * @param markerSnippet the marker text snippet to use
     */
    @Override
    public void setupMapData(Context context, long buildingId, BitmapDescriptor markerIcon, String markerSnippet) {
        if (mSetupMapDataListener != null) mSetupMapDataListener.onSetupMapData();
        super.setupMapData(context, buildingId, markerIcon, markerSnippet);
    }
}
