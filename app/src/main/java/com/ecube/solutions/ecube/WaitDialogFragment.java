package com.ecube.solutions.ecube;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecube.solutions.ecube.abstracts.DialogAbstract;
import com.ecube.solutions.ecube.authentication.profile.dao.Internationalization;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        View v = LayoutInflater.from(mActivity).inflate(R.layout.wait_fragment,null);
        dialogCardView = (CardView)v.findViewById(R.id.wait_fragment_CardView);
        dialogProgressBar = (ProgressBar) v.findViewById(R.id.wait_fragment_ProgressBar);

        //Get widht of the progress bar and reset cardView accordingly
        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                 // Here you can get the size :)
                int radius = dialogProgressBar.getMeasuredWidth();
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
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

}
