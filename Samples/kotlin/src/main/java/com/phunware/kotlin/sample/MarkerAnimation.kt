package com.phunware.kotlin.sample

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.annotation.TargetApi
import android.os.Build
import android.util.Property
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

internal object MarkerAnimation {

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun animateMarkerTo(marker: Marker,
                        finalPosition: LatLng,
                        latLngInterpolator: LatLngInterpolator) {
        val typeEvaluator = TypeEvaluator<LatLng> { fraction, startValue, endValue -> latLngInterpolator.interpolate(fraction, startValue, endValue) }
        val property = Property.of(Marker::class.java, LatLng::class.java, "position")
        val animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
        animator.duration = 500
        animator.start()
    }
}
