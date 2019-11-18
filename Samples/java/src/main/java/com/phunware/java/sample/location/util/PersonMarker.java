package com.phunware.java.sample.location.util;

/* Copyright (C) 2018 Phunware, Inc.

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL Phunware, Inc. BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Phunware, Inc. shall
not be used in advertising or otherwise to promote the sale, use or
other dealings in this Software without prior written authorization
from Phunware, Inc. */

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

