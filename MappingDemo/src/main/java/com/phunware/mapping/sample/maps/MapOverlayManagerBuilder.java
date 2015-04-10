package com.phunware.mapping.sample.maps;

import android.content.Context;

import com.phunware.mapping.library.maps.PwMapOverlayManagerBuilder;
import com.phunware.mapping.library.ui.PwMap;

public class MapOverlayManagerBuilder extends PwMapOverlayManagerBuilder {

    public MapOverlayManagerBuilder(Context context, PwMap pwMap) {
        super(context, pwMap);
    }

    private SetupMapDataListener mSetupMapDataListener;

    public void setSetupMapDataListener(SetupMapDataListener mSetupMapDataListener) {
        this.mSetupMapDataListener = mSetupMapDataListener;
    }

    public SetupMapDataListener getSetupMapDataListener() {
        return mSetupMapDataListener;
    }

    /**
     * Builds the PwBuildingMapManager
     *
     * @return the built PwBuildingMapManager object
     */
    @Override
    public BuildingMapManager build() {
        // Instantiate the Map Overlay manager
        final BuildingMapManager pwMapOverlayManager = new BuildingMapManager(this);

        // Setup the Map
        if (getPwMap() != null) {
            pwMapOverlayManager.setupMap(getPwMap());
        }

        // Setup the Map data if building was specified
        if (getPwBuildingId() != -1) {
            pwMapOverlayManager.setupMapData(getContext(), getPwBuildingId(), getMarkerIcon(), getMarkerSnippet());
        }

        return pwMapOverlayManager;
    }

    public interface SetupMapDataListener {
        void onSetupMapData();
    }
}
