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

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.phunware.kotlin.sample.R
import java.util.Random

internal object BitmapUtils {

    val randomColor: Int
        get() {
            val colors = intArrayOf(R.color.green, R.color.orange, R.color.pink, R.color.teal, R.color.purple, R.color.yellow)

            return colors[Random().nextInt(colors.size)]
        }

    fun createTextBitmap(activity: Activity, text: String): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = 48f
        paint.color = ContextCompat.getColor(activity, android.R.color.black)
        paint.textAlign = Paint.Align.LEFT
        val baseline = -paint.ascent()
        val width = (paint.measureText(text) + 0.5f).toInt() // round
        val height = (baseline + paint.descent() + 0.5f).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(text, 0f, baseline, paint)
        return image
    }

    fun createDotBitmap(activity: Activity, @ColorRes color: Int): Bitmap {
        val setColor = ContextCompat.getColor(activity, color)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = setColor

        val image = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawCircle(25f, 25f, 25f, paint)
        return image
    }
}
