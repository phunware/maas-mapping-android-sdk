package com.phunware.kotlin.sample.util.extensions

import android.content.Context
import android.util.TypedValue
import com.phunware.kotlin.sample.R

/**
 * Returns the accent color as a ColorInt for the theme on the current context.
 */
internal fun Context.accentColor(): Int {
    val typedValue = TypedValue()

    val a = obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
    val color = a.getColor(0, 0)

    a.recycle()

    return color
}