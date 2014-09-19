package com.phunware.mapping.sample.providers;

public enum LocationProvider {
    FUSED ("Fused"),
    MOCK ("Mock"),
    MSE ("MSE"),
    QUALCOMM ("Qualcomm"),
    SENION ("Senion");

    private final String displayName;
    LocationProvider(String name) {
        displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }
}
