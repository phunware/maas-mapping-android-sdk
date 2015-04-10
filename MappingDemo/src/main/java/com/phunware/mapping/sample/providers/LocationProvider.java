package com.phunware.mapping.sample.providers;

public enum LocationProvider {
    NONE ("None"),
    MOCK ("Mock"),
    MSE ("MSE"),
    BLE("BLE");

    private final String displayName;
    LocationProvider(String name) {
        displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }
}
