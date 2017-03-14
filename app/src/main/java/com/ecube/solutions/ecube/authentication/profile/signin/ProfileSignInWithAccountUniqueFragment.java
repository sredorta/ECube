package com.ecube.solutions.ecube.authentication.profile.signin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.WaitDialogFragment;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.general.AppGeneral;


/**
 * Created by sredorta on 3/9/2017.
 */
public class ProfileSignInWithAccountUniqueFragment extends FragmentAbstract{
    //Logs
    private static final String TAG = ProfileSignInWithEmailFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_CURRENT = "user.current.in";

    //In case of rotations
    public static final String KEY_CURRENT_USER = "user.save";

    //Request to connect to another account in case several accounts are available
    private static final int REQ_SIGNIN_WITH_ACCOUNTS = 1;

    private AccountAuthenticator myAccountAuthenticator;
    private User mUser;



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
        //final EditText passwordEditText = (EditText) v.findViewById(R.id.profile_signin_with_account_unique_EditText_password);
        final TextInputLayout passwordTextInputLayout = (TextInputLayout) v.findViewById(R.id.profile_signin_with_account_unique_TextInputLayout_password);
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
                passwordTextInputLayout.setError("");
                passwordTextInputLayout.refreshDrawableState();
                //hide input keyboard
                hideInputKeyBoard();
                    if (User.checkPasswordInput(passwordTextInputLayout,mView,mActivity)) {
                        if (DEBUG) Log.i(TAG, "We are now checking with server !");
                        //TODO do the actual login with the server
                        User myUser = new User();
                        myUser.setEmail(mUser.getEmail());
                        myUser.setPassword(passwordTextInputLayout.getEditText().getText().toString());
                        AccountAuthenticator ag = new AccountAuthenticator(getContext(), myUser);
                        ag.submitCredentials(new AsyncTaskInterface<Intent>() {
                            WaitDialogFragment dialog;
                            @Override
                            public void processStart() {
                                FragmentManager fm = getFragmentManager();
                                dialog = WaitDialogFragment.newInstance();
                                dialog.show(fm,"DIALOG");
                            }
                            @Override
                            public void processFinish(Intent result) {
                                dialog.dismiss();
                                if (result.hasExtra(AccountAuthenticator.KEY_ERROR_CODE)) {
                                    if (result.getStringExtra(AccountAuthenticator.KEY_ERROR_CODE).equals(AppGeneral.KEY_CODE_ERROR_INVALID_PASSWORD)) {
                                         passwordTextInputLayout.setError("Invalid password");
                                    } else {
                                        passwordTextInputLayout.setError("");
                                        Toast.makeText(getContext(), result.getStringExtra(AccountAuthenticator.KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }, mActivity);
                        //If we get to this point is that we could not authenticate !
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

    //Save user in case of rotation
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_CURRENT_USER, mUser);
    }


}

