package com.ecube.solutions.ecube.authentication.authenticator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateEmailFragment;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateStartFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.Internationalization;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.authentication.profile.signin.ProfileSignInWithAccountFragment;
import com.ecube.solutions.ecube.authentication.profile.signin.ProfileSignInWithAccountUniqueFragment;
import com.ecube.solutions.ecube.authentication.profile.signin.ProfileSignInWithEmailFragment;
import com.ecube.solutions.ecube.authentication.profile.update.ProfileUpdateStartFragment;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.helpers.IntentHelper;

/**
 * Created by sredorta on 2/6/2017.
 */
public class AuthenticatorFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = AuthenticatorFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //The dispatcher starts signin/signup... and all returns the user
    public static final String FRAGMENT_OUTPUT_PARAM_USER = "param.user";  //Fragment output parameter for all fragments

    //Fragment start requests
    private static final int REQ_SIGNIN = 1;
    private static final int REQ_SIGNIN_WITH_ACCOUNTS = 2;
    private static final int REQ_SIGNIN_WITH_ACCOUNT_UNIQUE = 3;
    private static final int REQ_SIGNUP = 4;
    private static final int REQ_UPDATE = 5;

    //Actions that all the fragments will be sending
    public static final String KEY_ACTION_SIGNUP = "user.signup";
    public static final String KEY_ACTION_SIGNIN_EMAIL = "user.signin.email";
    public static final String KEY_ACTION_SIGNIN_PHONE = "user.signin.phone";
    public static final String KEY_ACTION_REMOVE_FROM_DEVICE = "user.remove.from.device";
    public static final String KEY_ACTION_UPDATE = "user.update";


    private User mUser;
    private AccountAuthenticator myAccountAuthenticator = null;

    // Constructor
    public static AuthenticatorFragment newInstance() {
        return new AuthenticatorFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myAccountAuthenticator = new AccountAuthenticator(getContext());
        //We check first if in the incomming intent there is an account
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.authenticator_fragment, container, false);
        //If input intent has ARG_IS_ADDING_NEW_ACCOUNT it means that we come from "create new account" and then we start profile create
        if (mActivity.getIntent().hasExtra(AccountAuthenticator.ARG_IS_ADDING_NEW_ACCOUNT)) {
            if (DEBUG) Log.i(TAG, "Starting new activity to create account !");
            ProfileCreateStartFragment fragment = ProfileCreateStartFragment.newInstance();
            fragment.setTargetFragment(AuthenticatorFragment.this, REQ_SIGNUP);
            replaceFragment(fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1,true);
        } else if (mActivity.getIntent().hasExtra(AccountAuthenticator.ARG_IS_UPDATING_ACCOUNT)){
            Bundle bundle = new Bundle();
            bundle.putSerializable(ProfileSignInWithAccountUniqueFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT,
                    mActivity.getIntent().getStringExtra(AccountAuthenticator.ARG_ACCOUNT_NAME));
            ProfileUpdateStartFragment fragment = ProfileUpdateStartFragment.newInstance(bundle);
            fragment.setTargetFragment(AuthenticatorFragment.this, REQ_UPDATE);
            replaceFragment(fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1,true);
        } else {
            if (myAccountAuthenticator.getAccountsCount() >0) {
                if (myAccountAuthenticator.getAccountsCount() == 1) {
                    //There is only one account we open the fragment to validate credentials of the unique account
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ProfileSignInWithAccountUniqueFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT,
                            mActivity.getIntent().getStringExtra(AccountAuthenticator.ARG_ACCOUNT_NAME));
                    ProfileSignInWithAccountUniqueFragment fragment = ProfileSignInWithAccountUniqueFragment.newInstance(bundle);
                    fragment.setTargetFragment(AuthenticatorFragment.this, REQ_SIGNIN_WITH_ACCOUNT_UNIQUE);
                    replaceFragment(fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1, true);  //This comes from abstract
                } else {
                    if (mActivity.getIntent().hasExtra(AccountAuthenticator.ARG_ACCOUNT_NAME)) {
                        if (myAccountAuthenticator.getAccount(mActivity.getIntent().getStringExtra(AccountAuthenticator.ARG_ACCOUNT_NAME)) != null) {
                            //In such case we open the fragment to validate credentials of the current account from the intent
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(ProfileSignInWithAccountUniqueFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT,
                                    mActivity.getIntent().getStringExtra(AccountAuthenticator.ARG_ACCOUNT_NAME));
                            ProfileSignInWithAccountUniqueFragment fragment = ProfileSignInWithAccountUniqueFragment.newInstance(bundle);
                            fragment.setTargetFragment(AuthenticatorFragment.this, REQ_SIGNIN_WITH_ACCOUNT_UNIQUE);
                            replaceFragment(fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1, true);  //This comes from abstract
                        }
                    } else {
                        ProfileSignInWithAccountFragment fragment = ProfileSignInWithAccountFragment.newInstance();
                        fragment.setTargetFragment(AuthenticatorFragment.this, REQ_SIGNIN_WITH_ACCOUNTS);
                        replaceFragment(fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1, true);  //This comes from abstract
                    }
                }
            } else {
                ProfileSignInWithEmailFragment fragment = ProfileSignInWithEmailFragment.newInstance();
                fragment.setTargetFragment(AuthenticatorFragment.this, REQ_SIGNIN);
                replaceFragment(fragment,AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1,true);  //This comes from abstract
            }
        }

        return v;
    }

    //We have called signin/signup... and we get an User as result we need to process here what to do
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //We do restore the user from the different fragments response
            if (data.hasExtra(FRAGMENT_OUTPUT_PARAM_USER)) {
                mUser = (User) data.getParcelableExtra(FRAGMENT_OUTPUT_PARAM_USER);
                Log.i(TAG, "Got user onActivityResult");
                handleAccountRequest();
            }
        } else {
            Log.i(TAG, "OnBackPressed at Dispatcher !");
        }

    }

    //////////////////////////////////////////////////////////////////////////////////
    //Does any action required on the account : signin with email, signin with phone, signup
    //////////////////////////////////////////////////////////////////////////////////
    private void handleAccountRequest() {
        mUser.setLanguage(Internationalization.getLanguage(getContext()));
        Log.i(TAG,"Set language to :" + mUser.getLanguage());

        Log.i(TAG, "Action required : " + mUser.getAction());
        mUser.print("Dispatcher:");
        if (mUser.getAction().equals(KEY_ACTION_REMOVE_FROM_DEVICE)) {
            myAccountAuthenticator.removeAccount(mUser);

        } else if (mUser.getAction().equals(KEY_ACTION_SIGNUP)) {
            Log.i(TAG, "Creating user :" + mUser.getEmail());
            AccountAuthenticator ag = new AccountAuthenticator(getContext(), mUser);
            ag.createServerAndDeviceAccount(mActivity);
        } else if ((mUser.getAction().equals(KEY_ACTION_SIGNIN_EMAIL)) || (mUser.getAction().equals(KEY_ACTION_SIGNIN_PHONE))) {
            Log.i(TAG, "Restoring user...");
            AccountAuthenticator ag = new AccountAuthenticator(getContext(), mUser);
            ag.submitCredentials(mActivity, mView);
        } else if (mUser.getAction().equals(KEY_ACTION_UPDATE)) {
            //TODO do the update part
            Log.i(TAG, "Updating profile");
        }
    }









/*
    //We have an account so we just need to validate that our token is still valid and if not go to SignIn
    private void checkFastLogin() {
        user.print("CheckFastLogin user details:");
        //Get the account token in the device

        new AsyncTask<String, Void, Bundle>() {
            @Override
            protected Bundle doInBackground(String... params) {

                Boolean isValidToken =false;
                if (user.getToken() !=null) {
                    JsonItem item = sServerAuthenticate.isTokenValid(user);
                    isValidToken = item.getResult();
                }
                Bundle data = new Bundle();
                data.putString(AccountManager.KEY_ACCOUNT_NAME, user.getName());
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, user.getType());
                data.putString(AccountManager.KEY_AUTHTOKEN, user.getToken());
                data.putBoolean("isValidToken", isValidToken);
                return data;
            }

            @Override
            protected void onPostExecute(Bundle data) {
                if (!mIsCanceled) {
                    user.print("This is the user we are trying to log in:");
                    Logs.i("We are checking if Token is valid ! onPostExecute", AuthenticatorActivity.class);
                    if (data.getBoolean("isValidToken", false)) {
                        mActivity.getIntent().putExtras(data);
                        mActivity.setResult(Activity.RESULT_OK);
                        mActivity.finish();
                    } else {
                        startSignIn();
                    }
                }
            }
        }.execute();

    }
*/

}
