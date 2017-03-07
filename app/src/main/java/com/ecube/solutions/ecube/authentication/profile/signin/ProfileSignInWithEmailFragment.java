package com.ecube.solutions.ecube.authentication.profile.signin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ecube.solutions.ecube.MainFragment;
import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateStartFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.User;

/**
 * Created by sredorta on 2/2/2017.
 */
public class ProfileSignInWithEmailFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileSignInWithEmailFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    private AccountAuthenticator myAccountAuthenticator;
    private User mUser;
    private final int REQ_SIGNUP = 1;
    private final int REQ_SIGNIN_WITH_PHONE = 2;


    // Constructor
    public static ProfileSignInWithEmailFragment newInstance() {
        return new ProfileSignInWithEmailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get account details from Singleton either from intent or from account of the device
        myAccountAuthenticator = new AccountAuthenticator(getContext());
        mUser = new User();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_signin_with_email, container, false);
        setCurrentView(v);
        final EditText emailEditText = (EditText) v.findViewById(R.id.profile_signin_with_email_editText_email);

        final EditText passwordEditText = (EditText) v.findViewById(R.id.profile_signin_with_email_editText_password);
        //Re-enter credentials
        v.findViewById(R.id.profile_signin_with_email_Button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) Log.i(TAG, "Submitting credentials to account manager !");
                //hide input keyboard
                hideInputKeyBoard();

                if (User.checkEmailInput(emailEditText,mView)) {
                    if (User.checkPasswordInput(passwordEditText,mView,mActivity)) {
                        if (DEBUG) Log.i(TAG, "We are now checking with server !");
                        //TODO do the actual login with the server
                        User myUser = new User();
                        myUser.setEmail(emailEditText.getText().toString());
                        myUser.setPassword(passwordEditText.getText().toString());
                        myUser.setAction(MainFragment.KEY_ACTION_SIGNIN_EMAIL);
                        putOutputParam(MainFragment.FRAGMENT_OUTPUT_PARAM_USER, myUser);
                        sendResult(Activity.RESULT_OK);
                    }
                }
            }
        });

        //Login using phone
        v.findViewById(R.id.profile_signin_with_email_TextView_use_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileSignInWithPhoneFragment fragment = ProfileSignInWithPhoneFragment.newInstance();
                fragment.setTargetFragment(ProfileSignInWithEmailFragment.this, REQ_SIGNIN_WITH_PHONE);
                replaceFragment(fragment);
            }
        });

        //Create new user account
        v.findViewById(R.id.profile_signin_with_email_TextView_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) Log.i(TAG, "Starting new activity to create account !");
                ProfileCreateStartFragment fragment = ProfileCreateStartFragment.newInstance();
                fragment.setTargetFragment(ProfileSignInWithEmailFragment.this, REQ_SIGNUP);
                replaceFragment(fragment);
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(MainFragment.FRAGMENT_OUTPUT_PARAM_USER)) {
                putOutputParam(MainFragment.FRAGMENT_OUTPUT_PARAM_USER, (User) data.getParcelableExtra(MainFragment.FRAGMENT_OUTPUT_PARAM_USER));
                sendResult(Activity.RESULT_OK);
            }
        }
    }
}

