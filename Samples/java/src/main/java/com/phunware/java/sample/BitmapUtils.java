package com.phunware.java.sample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import java.util.Random;

public final class BitmapUtils {
    private BitmapUtils() {
    }

    public static Bitmap createTextBitmap(Activity activity, String text) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(48);
        paint.setColor(ContextCompat.getColor(activity, android.R.color.black));
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent();
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    public static Bitmap createDotBitmap(Activity activity, @ColorRes int color) {
        int setColor = ContextCompat.getColor(activity, color);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(setColor);

        Bitmap image = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawCircle(25, 25, 25, paint);
        return image;
    }

    public static int getRandomColor() {
        int[] colors = {R.color.green,
                R.color.orange,
                R.color.pink,
                R.color.teal,
                R.color.purple,
                R.color.yellow};

        return colors[new Random().nextInt(colors.length)];
    }

    public static Bitmap getBitmap(MainActivity mainActivity, int drawableRes) {
        Drawable drawable = ContextCompat.getDrawable(mainActivity, drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap
                .createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
