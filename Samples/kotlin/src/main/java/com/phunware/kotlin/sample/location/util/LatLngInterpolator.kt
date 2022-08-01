package com.phunware.kotlin.sample.location.util

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

import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import java.lang.Math.toDegrees
import java.lang.Math.toRadians

internal interface LatLngInterpolator {
    fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng

    /**
     * Simple linear interpolator, works well for short distances
     */
    class Linear : LatLngInterpolator {
        override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
            val lat = (b.latitude - a.latitude) * fraction + a.latitude
            val lng = (b.longitude - a.longitude) * fraction + a.longitude
            return LatLng(lat, lng)
        }
    }

    /**
     * Will interpolate correctly across the 180th meridian
     */
    class LinearFixed : LatLngInterpolator {
        override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
            val lat = (b.latitude - a.latitude) * fraction + a.latitude
            var lngDelta = b.longitude - a.longitude

            // Take the shortest path across the 180th meridian.
            if (abs(lngDelta) > 180) {
                lngDelta -= sign(lngDelta) * 360
            }
            val lng = lngDelta * fraction + a.longitude
            return LatLng(lat, lng)
        }
    }

    /**
     * Will interpolate correctly across distances and areas where the curvature of the earth
     * affects the path over which we're animating (i.e. near the poles)
     */
    class Spherical : LatLngInterpolator {

        /* From github.com/googlemaps/android-maps-utils */
        override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
            // http://en.wikipedia.org/wiki/Slerp
            val fromLat = toRadians(a.latitude)
            val fromLng = toRadians(a.longitude)
            val toLat = toRadians(b.latitude)
            val toLng = toRadians(b.longitude)
            val cosFromLat = cos(fromLat)
            val cosToLat = cos(toLat)

            // Computes Spherical interpolation coefficients.
            val angle = computeAngleBetween(fromLat, fromLng, toLat, toLng)
            val sinAngle = sin(angle)
            if (sinAngle < 1E-6) {
                return a
            }
            val a2 = sin((1 - fraction) * angle) / sinAngle
            val b2 = sin(fraction * angle) / sinAngle

            // Converts from polar to vector and interpolate.
            val x = a2 * cosFromLat * cos(fromLng) + b2 * cosToLat * cos(toLng)
            val y = a2 * cosFromLat * sin(fromLng) + b2 * cosToLat * sin(toLng)
            val z = a2 * sin(fromLat) + b2 * sin(toLat)

            // Converts interpolated vector back to polar.
            val lat = atan2(z, sqrt(x * x + y * y))
            val lng = atan2(y, x)
            return LatLng(toDegrees(lat), toDegrees(lng))
        }

        private fun computeAngleBetween(fromLat: Double,
                                        fromLng: Double,
                                        toLat: Double,
                                        toLng: Double): Double {
            // Haversine's formula
            val dLat = fromLat - toLat
            val dLng = fromLng - toLng
            return 2 * asin(sqrt(sin(dLat / 2).pow(2.0) + cos(fromLat) * cos(toLat) * sin(dLng / 2).pow(2.0)))
        }
    }
}
