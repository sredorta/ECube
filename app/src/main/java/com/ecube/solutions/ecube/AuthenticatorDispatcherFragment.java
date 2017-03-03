package com.ecube.solutions.ecube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountGeneral;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.authentication.profile.signin.ProfileSignInWithAccountFragment;
import com.ecube.solutions.ecube.authentication.profile.signin.ProfileSignInWithEmailFragment;
import com.ecube.solutions.ecube.general.AppGeneral;

/**
 * Created by sredorta on 3/1/2017.
 */
public class AuthenticatorDispatcherFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = AuthenticatorDispatcherFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //The dispatcher starts signin/signup... and all returns the user
    public static final String FRAGMENT_OUTPUT_PARAM_USER = "param.user";  //Fragment output parameter for all fragments

    private User mUser;

    //Fragment start requests
    private static final int REQ_SIGNIN = 1;
    private static final int REQ_SIGNIN_WITH_ACCOUNTS = 2;
    private static final int REQ_SIGNUP = 3;

    //Actions that all the fragments will be sending
    //TODO move this to user
    public static final String KEY_ACTION_SIGNUP = "user.signup";
    public static final String KEY_ACTION_SIGNIN_EMAIL = "user.signin.email";
    public static final String KEY_ACTION_SIGNIN_PHONE = "user.signin.phone";
    public static final String KEY_ACTION_REMOVE_FROM_DEVICE = "user.remove.from.device";

    AccountGeneral myAccountGeneral = null;

    // Constructor
    public static AuthenticatorDispatcherFragment newInstance() {
        return new AuthenticatorDispatcherFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //We check what we need to do if start SignIn/SignInWithAccounts/SignUp
        myAccountGeneral = new AccountGeneral(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        setCurrentView(v);
        //TODO we should first check if we can do fast login and if not then go for signup/signin...

        Log.i(TAG, "Found accounts :" + myAccountGeneral.getAccountsCount());
        if (myAccountGeneral.getAccountsCount() >0) {
            ProfileSignInWithAccountFragment fragment = ProfileSignInWithAccountFragment.newInstance();
            fragment.setTargetFragment(AuthenticatorDispatcherFragment.this, REQ_SIGNIN_WITH_ACCOUNTS);
            replaceFragment(fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1,true);  //This comes from abstract
        } else {
            ProfileSignInWithEmailFragment fragment = ProfileSignInWithEmailFragment.newInstance();
            fragment.setTargetFragment(AuthenticatorDispatcherFragment.this, REQ_SIGNIN);
            replaceFragment(fragment,AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1,true);  //This comes from abstract
        }
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //We do restore the user from the different fragments response
            if (data.hasExtra(FRAGMENT_OUTPUT_PARAM_USER)) {
                mUser = (User) data.getParcelableExtra(FRAGMENT_OUTPUT_PARAM_USER);
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
        Log.i(TAG, "Action required : " + mUser.getAction());
        mUser.print("Dispatcher:");
        if (mUser.getAction().equals(KEY_ACTION_REMOVE_FROM_DEVICE)) {
            myAccountGeneral.removeAccount(mUser);

        } else if (mUser.getAction().equals(KEY_ACTION_SIGNUP)) {
            Log.i(TAG, "Creating user :" + mUser.getEmail());
            AccountGeneral ag = new AccountGeneral(getContext(), mUser);
            ag.createAccount();
        }
    }

}