package com.ecube.solutions.ecube.dialogs;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.DialogAbstract;

/**
 * Created by sredorta on 4/3/2017.
 */

public class NoInternetDialogFragment extends DialogAbstract {

    // Constructor
    public static NoInternetDialogFragment newInstance() {
        return new NoInternetDialogFragment();
    }

    private LinearLayout linearLayoutMain;
    private int requestedOrientationSave;
    private int width;
    private int height;
    private int origin_x;
    private int origin_y;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);        //Set dialog to be retained to avoid issues of rotation !
        View v = LayoutInflater.from(mActivity).inflate(R.layout.dialog_no_internet_fragment,null);

        mView = v;
        linearLayoutMain = (LinearLayout) v.findViewById(R.id.dialog_no_internet_fragment_LinearLayout_main);


        //Get position and size of dialog
        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Here you can get the size

                width = linearLayoutMain.getMeasuredWidth();
                height = linearLayoutMain.getMeasuredHeight();
                origin_y = linearLayoutMain.getBottom();
                origin_x = linearLayoutMain.getLeft();
                Log.i("TEST", "Measured sizes !!!!!!!!!!");
                Log.i("TEST", "width : " + width);
                Log.i("TEST", "height : " + height);
                Log.i("TEST", "origin_x : " + origin_x);
                Log.i("TEST", "origin_y : " + origin_y);

            }
        });
        setCancelable(true);
        return new AlertDialog.Builder(mActivity)
                .setView(v)
                .create();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        //mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return v;
    }
    


    @Override
    public void onStart() {
        super.onStart();

        final View decorView = getDialog()
                .getWindow()
                .getDecorView();
        decorView.setScaleX(new Float(0.01));
        decorView.setScaleY(new Float(0.01));
        decorView.animate().scaleX(new Float(1.0)).scaleY(new Float(1.0))
                .setStartDelay(0)
                .setDuration(500)
                .start();

    }

    @Override
    public void dismiss() {
        final View decorView = getDialog()
                .getWindow()
                .getDecorView();

        decorView.animate().scaleX(new Float(0.01)).scaleY(new Float(0.01))
                .setStartDelay(0)
                .setDuration(300)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //We dismiss the dialog once the animation is finished
                        NoInternetDialogFragment.super.dismiss();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

}

