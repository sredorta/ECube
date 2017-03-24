package com.ecube.solutions.ecube.widgets;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;
import java.lang.reflect.Field;

import static android.content.ContentValues.TAG;

/**
 * Extension of standard TextInputLayout that allows to set a color on ErrorText
 * Additionally it handles rotations for text saving
 */

public class TextInputLayoutExtended extends TextInputLayout {

    //Handle rotations
    private static final String KEY_SAVE_COLOR_ERROR_TEXT = "error_color.save";
    private static final String KEY_CURRENT_TEXT = "text.current";
    private int mErrorColor = 0;   //ErrorText color


    public TextInputLayoutExtended(Context context) {
        this(context, null);
        setErrorEnabled(true);
        mErrorColor = getErrorTextColor();
        this.clearFocus();
    }

    public TextInputLayoutExtended(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        setErrorEnabled(true);
        mErrorColor = getErrorTextColor();
        this.clearFocus();
    }

    public TextInputLayoutExtended(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        setErrorEnabled(true);
        mErrorColor = getErrorTextColor();
        this.clearFocus();
    }

    //Set error textcolor by reflection
    public void setErrorTextColor(int color) {
        try {
            Field fErrorView = TextInputLayout.class.getDeclaredField("mErrorView");
            fErrorView.setAccessible(true);
            TextView mErrorView = (TextView) fErrorView.get(this);
            Field fCurTextColor = TextView.class.getDeclaredField("mCurTextColor");
            fCurTextColor.setAccessible(true);
            fCurTextColor.set(mErrorView, color);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mErrorColor = color;
        this.setError(this.getError());
    }


    //Set error textcolor by reflection
    public int getErrorTextColor() {
        try {
            Field fErrorView = TextInputLayout.class.getDeclaredField("mErrorView");
            fErrorView.setAccessible(true);
            TextView mErrorView = (TextView) fErrorView.get(this);
            Field fCurTextColor = TextView.class.getDeclaredField("mCurTextColor");
            fCurTextColor.setAccessible(true);
            return (int) fCurTextColor.get(mErrorView);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //setError with color
    public void setError(@Nullable CharSequence error, int color) {
        super.setError(error);
        this.setErrorTextColor(color);
    }

    //Get the editText text
    public String getText() {
        if (this.getEditText() == null) return "";
        return this.getEditText().getText().toString();
    }

    //Set the editText text
    public void setText(String text) {
        if (this.getEditText() != null) {
            this.getEditText().setText(text);
            this.getEditText().setSelection(text.length());
        }
    }


    //When we rotate device we come back here in onLayout so we reset everything
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mErrorColor!=0) {
            this.setErrorTextColor(mErrorColor);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Save contents on rotation hierarchically
    /////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        TextInputLayoutExtended.SavedState ss = new TextInputLayoutExtended.SavedState(superState);
        ss.childrenStates = new SparseArray();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).saveHierarchyState(ss.childrenStates);
        }

        //Our View arguments we Save
        Bundle data = new Bundle();
        data.putInt(KEY_SAVE_COLOR_ERROR_TEXT, mErrorColor);
        data.putString(KEY_CURRENT_TEXT, this.getText());
        ss.myDataToSave = data;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        TextInputLayoutExtended.SavedState ss = (TextInputLayoutExtended.SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).restoreHierarchyState(ss.childrenStates);
        }

        //Our View arguments we restore
        Bundle data = ss.myDataToSave;
        mErrorColor = data.getInt(KEY_SAVE_COLOR_ERROR_TEXT);
        this.setText(data.getString(KEY_CURRENT_TEXT));
        setErrorTextColor(mErrorColor);
        this.refreshDrawableState();
    }
    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }


    static class SavedState extends BaseSavedState {
        SparseArray childrenStates;          // Handle hierarchies
        Bundle myDataToSave = new Bundle();  // Bundle used to transfer our View arguments

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenStates = in.readSparseArray(classLoader);                   //Hierarchical read
            myDataToSave = in.readParcelable(Bundle.class.getClassLoader());    //Our data read
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSparseArray(childrenStates);   //Hierarchical write to Parcel
            out.writeParcelable(myDataToSave,0);    //OurView write to Parcel
        }
        public static final ClassLoaderCreator<SavedState> CREATOR
                = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}