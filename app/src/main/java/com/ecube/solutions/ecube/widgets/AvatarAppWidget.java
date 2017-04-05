package com.ecube.solutions.ecube.widgets;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.transition.Transition;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ecube.solutions.ecube.R;



/**
 * Created by sredorta on 4/4/2017.
 */


public class AvatarAppWidget extends LinearLayout {
    private static final String TAG = AvatarAppWidget.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Drawable
    Drawable mySrc;

    //Graphic elements
    private CardView avatarCardView;
    private ImageView avatarImageView;
    private LinearLayout avatarLinearLayout;

    private int avatar_width;
    private int avatar_height;


    //Handle rotations
    private static final String KEY_SAVE_IMAGE = "AvatarAppWidget.save.image";
    private static final String KEY_SAVE_TRANSITION = "AvatarAppWidget.save.transition";


    private Bitmap mImageBitmap;
    private String myTransition;

    public AvatarAppWidget(Context context) {
        this(context, null);
    }

    public AvatarAppWidget(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarAppWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Get input attributes from XML
        ////////////////////////////////////////////////////////////////////////////////////////////

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AvatarAppWidget, 0, 0);
        int n = typedArray.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.AvatarAppWidget_src:
                    mySrc = typedArray.getDrawable(R.styleable.AvatarAppWidget_src);
                    break;
                case R.styleable.AvatarAppWidget_transitionName:
                    myTransition = typedArray.getString(R.styleable.AvatarAppWidget_transitionName);
                    break;

                default:
                    //Do nothing
            }
        }
        typedArray.recycle();

        final View root = LayoutInflater.from(context).inflate(R.layout.widget_avatar, this, true);


        avatarLinearLayout = (LinearLayout) root.findViewById(R.id.widget_avatar_LinearLayout);
        avatarCardView = (CardView) root.findViewById(R.id.widget_avatar_CardView);
        avatarImageView = (ImageView) root.findViewById(R.id.widget_avatar_ImageView);
        if (myTransition != null) {
            avatarCardView.setTransitionName(myTransition);
            avatarImageView.setTransitionName(myTransition);
            avatarLinearLayout.setTransitionName(myTransition);
        }


        //Get width of the linearLayout containing the avatar reset cardView accordingly to have nice rounded
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // Here you can get the size
                    avatar_width = avatarCardView.getMeasuredWidth();
                    avatar_height = avatarCardView.getMeasuredHeight();
                    int radius;
                    int gap;
                    if (avatar_width < avatar_height) {
                        radius = avatarCardView.getMeasuredWidth();
                        gap = avatar_height - avatar_width;

                        avatarLinearLayout.setPadding(avatarLinearLayout.getPaddingLeft() + 0,
                                avatarLinearLayout.getPaddingTop() + gap / 2,
                                avatarLinearLayout.getPaddingRight() + 0,
                                avatarLinearLayout.getPaddingBottom() + gap / 2);
                    } else {
                        gap = avatar_width - avatar_height;
                        radius = avatarCardView.getMeasuredHeight();
                        avatarLinearLayout.setPadding(avatarLinearLayout.getPaddingLeft() + gap / 2,
                                avatarLinearLayout.getPaddingTop() + 0,
                                avatarLinearLayout.getPaddingRight() + gap / 2,
                                avatarLinearLayout.getPaddingBottom() + 0);
                    }
                    radius = radius / 2;
                    avatarCardView.setRadius(radius);
            }
        });
    }

    //Return full ImageView in case we need special methods
    public ImageView getImageView() {
        return avatarImageView;
    }

   //Upscope basic methods
    public Drawable getDrawable() {
        return avatarImageView.getDrawable();
    }


    public void setImageDrawable(Drawable drawable) {
        avatarImageView.setImageDrawable(drawable);
    }

    public void setImageBitmap(Bitmap bitmap) {
        mImageBitmap = bitmap;
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        roundedBitmapDrawable.setCircular(true);
        roundedBitmapDrawable.mutate();
        avatarImageView.setImageDrawable(roundedBitmapDrawable);
    }

    public Bitmap getImageBitmap() {
        if (mImageBitmap != null) return mImageBitmap;
        return ((BitmapDrawable) avatarImageView.getDrawable()).getBitmap();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////
    // Save contents on rotation hierarchically
    /////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        AvatarAppWidget.SavedState ss = new AvatarAppWidget.SavedState(superState);
        ss.childrenStates = new SparseArray();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).saveHierarchyState(ss.childrenStates);
        }

        //Our View arguments we Save
        Bundle data = new Bundle();
        data.putParcelable(KEY_SAVE_IMAGE, getImageBitmap());
        data.putString(KEY_SAVE_TRANSITION, myTransition);
        ss.myDataToSave = data;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        AvatarAppWidget.SavedState ss = (AvatarAppWidget.SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).restoreHierarchyState(ss.childrenStates);
        }

        //Our View arguments we restore
        Bundle data = ss.myDataToSave;
        setImageBitmap((Bitmap) data.getParcelable(KEY_SAVE_IMAGE));
        myTransition = data.getString(KEY_SAVE_TRANSITION);
        if (myTransition != null) {
            avatarCardView.setTransitionName(myTransition);
            avatarImageView.setTransitionName(myTransition);
            avatarLinearLayout.setTransitionName(myTransition);
        }
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

        public static final ClassLoaderCreator<AvatarAppWidget.SavedState> CREATOR
                = new ClassLoaderCreator<AvatarAppWidget.SavedState>() {
            @Override
            public AvatarAppWidget.SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new AvatarAppWidget.SavedState(source, loader);
            }

            @Override
            public AvatarAppWidget.SavedState createFromParcel(Parcel source) {
                return createFromParcel(null);
            }

            public AvatarAppWidget.SavedState[] newArray(int size) {
                return new AvatarAppWidget.SavedState[size];
            }
        };
    }


}
