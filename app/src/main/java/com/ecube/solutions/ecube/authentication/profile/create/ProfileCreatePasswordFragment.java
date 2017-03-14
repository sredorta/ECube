package com.ecube.solutions.ecube.authentication.profile.create;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
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
import com.ecube.solutions.ecube.helpers.TextInputLayoutHelper;


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

        final TextInputLayout passwordTextInputLayout = (TextInputLayout) v.findViewById(R.id.profile_create_password_TextInputLayout);
        final EditText passwordEditText = (EditText) v.findViewById(R.id.profile_create_password_editText);
        final TextInputLayout passwordShadowTextInputLayout = (TextInputLayout) v.findViewById(R.id.profile_create_password_TextInputLayout_shadow);
        final EditText passwordShadowEditText = (EditText) v.findViewById(R.id.profile_create_password_editText_shadow);

        Button nextButton = (Button) v.findViewById(R.id.profile_create_password_button);

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                passwordTextInputLayout.setError("Password quality");
                User.getPasswordQuality(editable.toString(), passwordTextInputLayout, mView);
                passwordTextInputLayout.setError(String.format("Password quality %s", User.getPasswordQuality(editable.toString()))+ "%");
                passwordEditText.refreshDrawableState();
                passwordShadowEditText.setText("");
                passwordShadowTextInputLayout.setError("");

            }
        });

        passwordShadowEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                //If Shadow is longer than password we don't accept more chars
                if (passwordShadowEditText.getText().length()> passwordEditText.getText().length()) {
                    editable.delete(editable.length() - 1, editable.length());
                }

                if (passwordShadowEditText.getText().length() > 0 && passwordEditText.getText().length() > 0) {
                    //Check if both strings matches
                    String tmpPassword = passwordEditText.getText().toString().substring(0, passwordShadowEditText.getText().length());
                    Log.i(TAG, "tmpPassword = " + tmpPassword);
                    if (passwordShadowEditText.getText().toString().equals(tmpPassword)) {
                        passwordShadowTextInputLayout.setError("Matching passwords");
                        TextInputLayoutHelper.setErrorTextColor(passwordShadowTextInputLayout, ContextCompat.getColor(mView.getContext(), R.color.md_green_500));
                    } else {
                        passwordShadowTextInputLayout.setError("Passwords not matching");
                        TextInputLayoutHelper.setErrorTextColor(passwordShadowTextInputLayout, ContextCompat.getColor(mView.getContext(), R.color.md_red_500));
                    }
                    //passwordShadowTextInputLayout.refreshDrawableState();

                    if (passwordEditText.getText().toString().equals(passwordShadowEditText.getText().toString()))
                        if (User.checkPasswordInput(passwordEditText.getText().toString())) {
                            passwordShadowTextInputLayout.setError("");
                            passwordTextInputLayout.setError("");
                            //Hide keyboard if exists
                            hideInputKeyBoard();
                        }
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInputKeyBoard();
                if (User.checkShadowPasswordInput(passwordTextInputLayout, passwordShadowTextInputLayout,mView, mActivity)) {
                    putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_PASSWORD, passwordEditText.getText().toString());
                    sendResult(Activity.RESULT_OK);
                }
            }
        });
        return v;
    }

}
