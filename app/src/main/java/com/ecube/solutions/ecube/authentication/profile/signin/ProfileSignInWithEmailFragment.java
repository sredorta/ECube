package com.ecube.solutions.ecube.authentication.profile.signin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountGeneral;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateStartFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.User;

/**
 * Created by sredorta on 2/2/2017.
 */
public class ProfileSignInWithEmailFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileSignInWithEmailFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    private AccountGeneral myAccountGeneral;
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
        myAccountGeneral = new AccountGeneral(getContext());
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
                replaceFragment(fragment,"test",true);  //This comes from abstract
            }
        });

        //Create new user account
        v.findViewById(R.id.profile_signin_with_email_TextView_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) Log.i(TAG, "Starting new activity to create account !");
                ProfileCreateStartFragment fragment = ProfileCreateStartFragment.newInstance();
                fragment.setTargetFragment(ProfileSignInWithEmailFragment.this, REQ_SIGNUP);
                replaceFragment(fragment,"test",true);  //This comes from abstract
            }
        });

        return v;
    }

}

