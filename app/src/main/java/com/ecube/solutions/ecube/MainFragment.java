package com.ecube.solutions.ecube;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountGeneral;
import com.ecube.solutions.ecube.authentication.profile.signin.ProfileSignInWithAccountFragment;
import com.ecube.solutions.ecube.authentication.profile.signin.ProfileSignInWithEmailFragment;

/**
 * Created by sredorta on 3/1/2017.
 */
public class MainFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    private final int REQ_SIGNIN = 1;
    private final int REQ_SIGNIN_WITH_ACCOUNTS = 2;
    private final int REQ_SIGNUP = 3;

    AccountGeneral myAccountGeneral = null;

    // Constructor
    public static MainFragment newInstance() {
        return new MainFragment();
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
        //TODO check if we can log with account from preferences

        /*
        //Start SignUp
        ProfileCreateStartFragment fragment = ProfileCreateStartFragment.newInstance();
        fragment.setTargetFragment(MainFragment.this, REQ_SIGNUP);
        replaceFragment(fragment, "test", true);  //This comes from abstract
        */

        Log.i(TAG, "Found accounts :" +myAccountGeneral.getAccountsCount());
        if (myAccountGeneral.getAccountsCount() >0) {
            ProfileSignInWithAccountFragment fragment = ProfileSignInWithAccountFragment.newInstance();
            fragment.setTargetFragment(MainFragment.this, REQ_SIGNIN_WITH_ACCOUNTS);
            replaceFragment(fragment,"test",true);  //This comes from abstract
        } else {
            ProfileSignInWithEmailFragment fragment = ProfileSignInWithEmailFragment.newInstance();
            fragment.setTargetFragment(MainFragment.this, REQ_SIGNIN);
            replaceFragment(fragment,"test",true);  //This comes from abstract
        }

        return v;
    }
}