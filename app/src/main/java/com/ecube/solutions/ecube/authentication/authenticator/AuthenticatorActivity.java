package com.ecube.solutions.ecube.authentication.authenticator;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.ActivityAbstract;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateStartFragment;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.helpers.IntentHelper;


/**
 * Created by sredorta on 1/25/2017.
 */

//We get here by two means:
//    When using settings then we get here thanks to the service
//    When using the app we will send an intent to check if valid auth
//    In order to use app.v4 fragments we have copied the AccountAuthenticatorActivity abstract
public class AuthenticatorActivity extends ActivityAbstract {
    //Logs
    private static final String TAG = AuthenticatorActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    public static Bundle mResultBundle = null;
    private Fragment fragment;


 //   @Override
    public Fragment createFragment() {
        return AuthenticatorFragment.newInstance();
    }

    //Sets the bundle with the results that are sent to the authenticator (LockerAuthenticator)
    public static final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }


    @Override
    public void finish() {
        Log.i(TAG,"Input intent before finish:");
        //Save the last successfull login account into the preferences
//        if (getIntent().hasExtra(AccountManager.KEY_ACCOUNT_NAME)) {
//            QueryPreferences.setPreference(getApplicationContext(),QueryPreferences.PREFERENCE_ACCOUNT_NAME,getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
//        }
        AuthenticatorActivity.setAccountAuthenticatorResult(getIntent().getExtras());

        IntentHelper.dumpIntent(getIntent());
        Log.i(TAG, "Finish of AuthenticatorActivity !");
        if (mAccountAuthenticatorResponse != null) {
             // send the result bundle back if set, otherwise send an error.
             if (mResultBundle != null) {
                    mAccountAuthenticatorResponse.onResult(mResultBundle);
                    Log.i (TAG, "Account that we should save : " + getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_NAME));

             } else {
                    mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                                     "canceled");
             }
             mAccountAuthenticatorResponse = null;
        }
        //We return to the main activity the response
        Intent i = new Intent();
        if (mResultBundle != null) {
            i.putExtras(mResultBundle);
            this.setResult(RESULT_OK, i);
        } else {
            this.setResult(RESULT_CANCELED, i);
        }
        super.finish();
     }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "We are on onCreate of AuthActivity");
        super.onCreate(savedInstanceState);

        // Part of the accountAuthActivity
        mAccountAuthenticatorResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }
    }


    @Override
    public void onBackPressed() {
        //If our visible fragment is LEVEL_1 then we ask for exit or no
        Fragment myFragment = getVisibleFragment();
        if (myFragment.getTag().equals(AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1) || myFragment.getTag().equals(AppGeneral.KEY_FRAGMENT_STACK_LEVEL_0)) {
            //Ask for confirmation first
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit")
                    .setTitle("Exit application");
            builder.setCancelable(true);
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    AuthenticatorActivity.this.finish();
                    dialogInterface.cancel();

                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.create();
            builder.show();

        } else {
            super.onBackPressed();
        }
    }

}
