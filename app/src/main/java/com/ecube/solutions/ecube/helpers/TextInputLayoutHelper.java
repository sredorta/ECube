package com.ecube.solutions.ecube.helpers;

import android.support.design.widget.TextInputLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Created by sredorta on 3/14/2017.
 */
public class TextInputLayoutHelper {
    public static void setErrorTextColor(TextInputLayout textInputLayout, int color) {
        try {
            Field fErrorView = TextInputLayout.class.getDeclaredField("mErrorView");
            fErrorView.setAccessible(true);
            TextView mErrorView = (TextView) fErrorView.get(textInputLayout);
            Field fCurTextColor = TextView.class.getDeclaredField("mCurTextColor");
            fCurTextColor.setAccessible(true);
            fCurTextColor.set(mErrorView, color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
