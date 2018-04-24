package com.phunware.java.sample;

import com.google.android.gms.maps.model.Marker;

/**
 * A wrapper around a maker that associates a
 */
public class PersonMarker {
    private Marker marker;
    private String name;
    private String userType;

    public PersonMarker(String name, String userType, Marker marker) {
        super();
        this.name = name;
        this.userType = userType;
        this.marker = marker;
    }

    public Marker getMarker() {
        return marker;
    }

    public String getName() {
        return name;
    }

    public String getUserType() {
        return userType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersonMarker that = (PersonMarker) o;

        if (marker != null ? !marker.equals(that.marker) : that.marker != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return userType != null ? userType.equals(that.userType) : that.userType == null;

    }

    @Override
    public int hashCode() {
        int result = marker != null ? marker.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (userType != null ? userType.hashCode() : 0);
        return result;
    }
}

