package com.ecube.solutions.ecube.authentication.profile.create;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.profile.dao.User;


/**
 * Created by sredorta on 2/23/2017.
 */
public class ProfileCreateEmailFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileCreateEmailFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment parameters
    public static final String FRAGMENT_INPUT_PARAM_USER_EMAIL = "user.email.in";    //String
    public static final String FRAGMENT_OUTPUT_PARAM_USER_EMAIL = "user.email.out";    //String

    private String mEmail;

    // Constructor
    public static ProfileCreateEmailFragment newInstance() {
        return new ProfileCreateEmailFragment();
    }
    // Constructor with input arguments
    public static ProfileCreateEmailFragment newInstance(Bundle data) {
        ProfileCreateEmailFragment fragment = ProfileCreateEmailFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEmail = (String) getInputParam(ProfileCreateEmailFragment.FRAGMENT_INPUT_PARAM_USER_EMAIL);
        if (mEmail == null) mEmail = "";

        //mLastName = (String) getInputParam(ProfileCreateNamesFragment.FRAGMENT_INPUT_PARAM_USER_LAST_NAME);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_create_email_fragment, container, false);
        setCurrentView(v);
        final EditText emailEditText = (EditText) v.findViewById(R.id.profile_create_email_editText_email);
        final TextInputLayout emailTextInputLayout = (TextInputLayout) v.findViewById(R.id.profile_create_email_TextInputLayout_email);
        emailEditText.setText(mEmail);
        Button nextButton = (Button) v.findViewById(R.id.profile_create_email_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Trim the text in case we copy from suggestion
                emailEditText.setText(emailEditText.getText().toString().trim());
                //Hide keyboard if exists
                hideInputKeyBoard();
                if (User.checkEmailInput(emailTextInputLayout)) {
                    putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_EMAIL, emailEditText.getText().toString());
                    sendResult(Activity.RESULT_OK);
                    removeFragment(ProfileCreateEmailFragment.this,true);
                }
            }
        });
        return v;
    }



}
