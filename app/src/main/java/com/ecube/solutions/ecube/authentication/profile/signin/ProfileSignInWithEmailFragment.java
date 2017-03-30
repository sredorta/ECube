package com.ecube.solutions.ecube.authentication.profile.signin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.dialogs.WaitDialogFragment;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateStartFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.authentication.profile.update.ProfileUpdateResetPasswordFragment;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.widgets.TextInputLayoutAppWidget;

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
    private final int REQ_FORGOT_PASSWORD = 3;

    // Constructor
    public static ProfileSignInWithEmailFragment newInstance() {
        return new ProfileSignInWithEmailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // We are using async task so we need to retain it

        //Get account details from Singleton either from intent or from account of the device
        myAccountAuthenticator = new AccountAuthenticator(getContext());
        mUser = new User();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_signin_with_email, container, false);
        setCurrentView(v);
        final TextInputLayoutAppWidget emailTextInputLayout = (TextInputLayoutAppWidget) v.findViewById(R.id.profile_signin_with_email_TextInputLayoutAppWidget_email);
        final TextInputLayoutAppWidget passwordTextInputLayout = (TextInputLayoutAppWidget) v.findViewById(R.id.profile_signin_with_email_TextInputLayoutAppWidget_password);

        //Re-enter credentials
        v.findViewById(R.id.profile_signin_with_email_Button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) Log.i(TAG, "Submitting credentials to account manager !");
                //hide input keyboard
                hideInputKeyBoard();
                if (emailTextInputLayout.isValidInput()) {
                    if (passwordTextInputLayout.isValidInput()) {
                        User myUser = new User();
                        myUser.setEmail(emailTextInputLayout.getText());
                        myUser.setPassword(passwordTextInputLayout.getText());
                        Log.i(TAG, "Restoring user...");
                        AccountAuthenticator ag = new AccountAuthenticator(getContext(), myUser);
                        ag.submitCredentials(new AsyncTaskInterface<Intent>() {
                            WaitDialogFragment dialog;
                            @Override
                            public void processStart() {
                                emailTextInputLayout.setError("");
                                passwordTextInputLayout.setError("");
                                FragmentManager fm = getFragmentManager();
                                dialog = WaitDialogFragment.newInstance();
                                dialog.show(fm,"DIALOG");
                            }
                            @Override
                            public void processFinish(Intent result) {
                                dialog.dismiss();
                                if (result.hasExtra(AccountAuthenticator.KEY_ERROR_CODE)) {
                                    if (result.getStringExtra(AccountAuthenticator.KEY_ERROR_CODE).equals(AppGeneral.KEY_CODE_ERROR_INVALID_USER)) {
                                        emailTextInputLayout.setError("Email not registered");
                                    } else if (result.getStringExtra(AccountAuthenticator.KEY_ERROR_CODE).equals(AppGeneral.KEY_CODE_ERROR_INVALID_PASSWORD)) {
                                        passwordTextInputLayout.setError("Invalid password");
                                    } else {
                                        Toast.makeText(mActivity, result.getStringExtra(AccountAuthenticator.KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }, mActivity);
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
        //Go for forgot password
        v.findViewById(R.id.profile_signin_with_email_TextView_forgot_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emailTextInputLayout.isValidInput()) {
                    Bundle data = new Bundle();
                    data.putString(ProfileUpdateResetPasswordFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT, emailTextInputLayout.getText());
                    ProfileUpdateResetPasswordFragment fragment = ProfileUpdateResetPasswordFragment.newInstance(data);
                    fragment.setTargetFragment(ProfileSignInWithEmailFragment.this, REQ_FORGOT_PASSWORD);
                    replaceFragment(fragment);  //This comes from abstract
                } else {
                    Snackbar.make(mView,"Enter the email of the account you want to reset the password",Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== REQ_FORGOT_PASSWORD) {
            replaceFragment(this,this.getTag(),true);
        }
    }
}

