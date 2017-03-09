package com.ecube.solutions.ecube.authentication.profile.signin;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecube.solutions.ecube.MainFragment;
import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateStartFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.general.AppGeneral;

import java.util.Locale;

/**
 * Created by sredorta on 3/9/2017.
 */
public class ProfileSignInWithAccountUniqueFragment extends FragmentAbstract{
    //Logs
    private static final String TAG = ProfileSignInWithEmailFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_CURRENT = "user.current.in";
    public static final String FRAGMENT_OUTPUT_PARAM_USER_PHONE_NUMBER = "user.phone_number.out";

    //In case of rotations
    public static final String KEY_CURRENT_USER = "user.save";

    //Request to connect to another account in case several accounts are available
    private static final int REQ_SIGNIN_WITH_ACCOUNTS = 1;

    private AccountAuthenticator myAccountAuthenticator;
    private User mUser;
    private final int REQ_SIGNUP = 1;
    private final int REQ_SIGNIN_WITH_PHONE = 2;


    // Constructor
    public static ProfileSignInWithAccountUniqueFragment newInstance() {
        return new ProfileSignInWithAccountUniqueFragment();
    }

    public static ProfileSignInWithAccountUniqueFragment newInstance(Bundle data) {
        ProfileSignInWithAccountUniqueFragment fragment = ProfileSignInWithAccountUniqueFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String email = (String) getInputParam(ProfileSignInWithAccountUniqueFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT);
        //Get account details from the device
        myAccountAuthenticator = new AccountAuthenticator(getContext());
        mUser = myAccountAuthenticator.getDataFromDeviceAccount(myAccountAuthenticator.getAccount(email));
        //Check that we have an account with current user and if not exit

        //Restore user in case of rotation
        if (savedInstanceState!= null) {
            mUser = (User) savedInstanceState.getParcelable(KEY_CURRENT_USER);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_signin_with_account_unique, container, false);
        setCurrentView(v);
        final TextView nameTextView = (TextView) v.findViewById(R.id.profile_signin_with_account_unique_name);
        final TextView emailTextView = (TextView) v.findViewById(R.id.profile_signin_with_account_unique_email);
        final ImageView avatarImageView = (ImageView) v.findViewById(R.id.profile_signin_with_account_unique_ImageView_avatar);
        final EditText passwordEditText = (EditText) v.findViewById(R.id.profile_signin_with_account_unique_EditText_password);
        final TextView otherAccountTextView = (TextView) v.findViewById(R.id.profile_signin_with_account_unique_TextView_other);

        //Do not show the see several accounts if there is only one
        if (myAccountAuthenticator.getAccountsCount()<2) {
            otherAccountTextView.setEnabled(false);
            otherAccountTextView.setVisibility(View.GONE);
        }

        nameTextView.setText(mUser.getFirstName() + " " + mUser.getLastName());
        emailTextView.setText(mUser.getEmail());
        avatarImageView.setImageBitmap(mUser.getAvatar(getContext()));

        v.findViewById(R.id.profile_signin_with_account_unique_Button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) Log.i(TAG, "Submitting credentials to account manager !");
                //hide input keyboard
                hideInputKeyBoard();
                    if (User.checkPasswordInput(passwordEditText,mView,mActivity)) {
                        if (DEBUG) Log.i(TAG, "We are now checking with server !");
                        //TODO do the actual login with the server
                        User myUser = new User();
                        myUser.setEmail(mUser.getEmail());
                        myUser.setPassword(passwordEditText.getText().toString());
                        myUser.setAction(MainFragment.KEY_ACTION_SIGNIN_EMAIL);
                        putOutputParam(MainFragment.FRAGMENT_OUTPUT_PARAM_USER, myUser);
                        sendResult(Activity.RESULT_OK);
                    }
            }
        });
        //If there are more than one account we might want to connect to another account so let's give possibility
        otherAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileSignInWithAccountFragment fragment = ProfileSignInWithAccountFragment.newInstance();
                fragment.setTargetFragment(ProfileSignInWithAccountUniqueFragment.this, REQ_SIGNIN_WITH_ACCOUNTS);
                replaceFragment(fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_UNDEFINED, true);  //This comes from abstract
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

    //Save user in case of rotation
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_CURRENT_USER, mUser);
    }


}

