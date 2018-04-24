package com.phunware.kotlin.sample

import com.google.android.gms.maps.model.Marker

/**
 * A wrapper around a maker that associates a
 */
class PersonMarker(val name: String?, val userType: String?, val marker: Marker?) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as PersonMarker?

        if (if (marker != null) marker != that!!.marker else that!!.marker != null) return false
        if (if (name != null) name != that.name else that.name != null) return false
        return if (userType != null) userType == that.userType else that.userType == null

    }

    override fun hashCode(): Int {
        var result = marker?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (userType?.hashCode() ?: 0)
        return result
    }
}

