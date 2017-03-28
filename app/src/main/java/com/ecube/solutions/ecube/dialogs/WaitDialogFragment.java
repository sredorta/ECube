package com.ecube.solutions.ecube.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);        //Set dialog to be retained to avoid issues of rotation !
        View v = LayoutInflater.from(mActivity).inflate(R.layout.wait_fragment,null);
        mView = v;
        dialogCardView = (CardView)v.findViewById(R.id.wait_fragment_CardView);
        dialogProgressBar = (ProgressBar) v.findViewById(R.id.wait_fragment_ProgressBar);

        //Get widht of the progress bar and reset cardView accordingly
        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                 // Here you can get the size
                int width = dialogProgressBar.getMeasuredWidth();
                int height = dialogProgressBar.getMeasuredHeight();
                int radius;
                if (width < height) {
                    radius = dialogProgressBar.getMeasuredWidth();

                } else {
                    radius = dialogProgressBar.getMeasuredHeight();
                }
                radius = radius / 2;
                dialogCardView.setRadius(radius);
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

}
