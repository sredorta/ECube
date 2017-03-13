package com.ecube.solutions.ecube.authentication.profile.signin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.WaitDialogFragment;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
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
                        Log.i(TAG, "Restoring user...");
                        AccountAuthenticator ag = new AccountAuthenticator(getContext(), myUser);
                        ag.submitCredentials(new AsyncTaskInterface() {
                            WaitDialogFragment dialog;
                            @Override
                            public void processStart() {
                                FragmentManager fm = getFragmentManager();
                                dialog = WaitDialogFragment.newInstance();
                                dialog.show(fm,"DIALOG");
                            }
                            @Override
                            public void processFinish() {
                                dialog.dismiss();
                            }
                        }, mActivity);
                        //If we get to this point is that we could not authenticate !
                        passwordEditText.setText("");
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
}

