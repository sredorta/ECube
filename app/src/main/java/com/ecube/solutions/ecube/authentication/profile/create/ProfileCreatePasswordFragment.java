package com.ecube.solutions.ecube.authentication.profile.create;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.profile.dao.User;


/**
 * Created by sredorta on 2/23/2017.
 */
public class ProfileCreatePasswordFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileCreatePasswordFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_OUTPUT_PARAM_USER_PASSWORD = "user.password.out";    //String


    // Constructor
    public static ProfileCreatePasswordFragment newInstance() {
        return new ProfileCreatePasswordFragment();
    }
    // Constructor with input arguments
    public static ProfileCreateNamesFragment newInstance(Bundle data) {
        ProfileCreateNamesFragment fragment = ProfileCreateNamesFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_create_password_fragment, container, false);
        mView =v;

        final EditText passwordEditText = (EditText) v.findViewById(R.id.profile_create_password_editText);

        final EditText passwordShadowEditText = (EditText) v.findViewById(R.id.profile_create_password_editText_shadow);
        final ImageView passwordShadowImageView = (ImageView) v.findViewById(R.id.profile_create_password_imageView_password_shadow);

        final ProgressBar passwordQualityProgressBar = (ProgressBar) v.findViewById(R.id.profile_create_password_ProgressBar_quality);

        Button nextButton = (Button) v.findViewById(R.id.profile_create_password_button);

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                Log.i("SERGI:", "Password quality : " + editable.toString() +" ::" + User.getPasswordQuality(editable.toString()));
                User.getPasswordQuality(editable.toString(), passwordQualityProgressBar, mView);

            }
        });

        passwordShadowEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                passwordShadowImageView.setVisibility(View.INVISIBLE);
                if (passwordEditText.getText().toString().equals(passwordShadowEditText.getText().toString()))
                    if (User.checkPasswordInput(passwordEditText.getText().toString())) {
                        passwordShadowImageView.setVisibility(View.VISIBLE);
                        //Hide keyboard if exists
                        hideInputKeyBoard();
                    }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInputKeyBoard();
                if (User.checkShadowPasswordInput(passwordEditText, passwordShadowEditText,mView, mActivity)) {
                    putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_PASSWORD, passwordEditText.getText().toString());
                    sendResult(Activity.RESULT_OK);
                }
            }
        });
        return v;
    }

}
