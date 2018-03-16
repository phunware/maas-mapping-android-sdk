package com.phunware.kotlin.sample

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import java.util.*

object BitmapUtils {

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

    fun getBitmap(mainActivity: MainActivity, drawableRes: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(mainActivity, drawableRes)
        val canvas = Canvas()
        val bitmap = Bitmap
                .createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)

        return bitmap
    }
}
