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
import android.widget.ProgressBar;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.DialogAbstract;

/**
 * Created by sredorta on 3/10/2017.
 */
public class WaitDialogFragment extends DialogAbstract {

    // Constructor
    public static WaitDialogFragment newInstance() {
        return new WaitDialogFragment();
    }

    private CardView dialogCardView;
    private ProgressBar dialogProgressBar;
    private int requestedOrientationSave;
    private int width;
    private int height;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //setRetainInstance(true);        //Set dialog to be retained to avoid issues of rotation !
        View v = LayoutInflater.from(mActivity).inflate(R.layout.dialog_wait_fragment,null);

        //Avoid rotation of this fragment
        requestedOrientationSave = mActivity.getRequestedOrientation();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        mView = v;
        dialogCardView = (CardView)v.findViewById(R.id.wait_fragment_CardView);
        dialogProgressBar = (ProgressBar) v.findViewById(R.id.wait_fragment_ProgressBar);


        //Get widht of the progress bar and reset cardView accordingly
        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Here you can get the size

                width = dialogProgressBar.getMeasuredWidth();
                height = dialogProgressBar.getMeasuredHeight();
                int radius;
                if (width < height) {
                    radius = dialogProgressBar.getMeasuredWidth();

                } else {
                    radius = dialogProgressBar.getMeasuredHeight();
                }
                radius = radius / 2;
                dialogCardView.setRadius(radius);
                Log.i("TEST", "Measured sizes !!!!!!!!!!");

            }
        });
        setCancelable(false);
        return new AlertDialog.Builder(mActivity)
                .setView(v)
                .create();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return v;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Restore Orientation savings
        mActivity.setRequestedOrientation(requestedOrientationSave);
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
                        WaitDialogFragment.super.dismiss();
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
