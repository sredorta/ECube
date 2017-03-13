package com.ecube.solutions.ecube.helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.ecube.solutions.ecube.R;

/**
 * Created by sredorta on 3/13/2017.
 */
public class IconHelper {
    private static final String TAG = IconHelper.class.getSimpleName();
    private static final boolean DEBUG = true;


    public static void colorize(Context context, ImageView imageView, @ColorRes int color) {
        Drawable mDrawable = imageView.getDrawable().mutate();
        PorterDuffColorFilter porterDuffColorFilter;
        porterDuffColorFilter = new PorterDuffColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_ATOP);
        mDrawable.setColorFilter(porterDuffColorFilter);
        imageView.setImageDrawable(mDrawable);
    }

}
