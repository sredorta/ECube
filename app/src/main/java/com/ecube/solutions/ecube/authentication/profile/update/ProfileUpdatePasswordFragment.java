package com.ecube.solutions.ecube.authentication.profile.update;

import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.WaitDialogFragment;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateEmailFragment;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreatePasswordFragment;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreatePhoneFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.authentication.profile.signin.ProfileSignInWithEmailFragment;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.helpers.IconHelper;
import com.ecube.solutions.ecube.network.Encryption;
import com.ecube.solutions.ecube.network.JsonItem;

/**
 * Created by sredorta on 3/14/2017.
 */
public class ProfileUpdatePasswordFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileUpdatePasswordFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_CURRENT = "user.current.in";

    //In case of rotations
    public static final String KEY_CURRENT_USER = "user.save";

    private User mUser;
    private AccountAuthenticator myAccountAuthenticator;

    // Constructor
    public static ProfileUpdatePasswordFragment newInstance() {
        return new ProfileUpdatePasswordFragment();
    }

    public static ProfileUpdatePasswordFragment newInstance(Bundle data) {
        ProfileUpdatePasswordFragment fragment = ProfileUpdatePasswordFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String email = (String) getInputParam(ProfileUpdateStartFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT);
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
        View v = inflater.inflate(R.layout.profile_update_password, container, false);
        setCurrentView(v);
        final TextInputLayout   passwordOldTextInputLayout  = (TextInputLayout) v.findViewById(R.id.profile_update_password_TextInputLayout_old);
        final EditText          passwordOldEditText         = (EditText)        v.findViewById(R.id.profile_update_password_EditText_old);
        final TextInputLayout   passwordNewTextInputLayout  = (TextInputLayout) v.findViewById(R.id.profile_update_password_TextInputLayout_new);
        final EditText          passwordNewEditText         = (EditText)        v.findViewById(R.id.profile_update_password_EditText_new);
        final TextInputLayout   passwordNewShadowTextInputLayout  = (TextInputLayout) v.findViewById(R.id.profile_update_password_TextInputLayout_new_shadow);
        final EditText          passwordNewShadowEditText         = (EditText)        v.findViewById(R.id.profile_update_password_EditText_new_shadow);


        final Button            submitButton                = (Button)          v.findViewById(R.id.profile_update_password_Button_submit);

/*
        passwordOldEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                Log.i("SERGI:", "Password quality : " + editable.toString() +" ::" + User.getPasswordQuality(editable.toString()));
                User.getPasswordQuality(editable.toString(), passwordQualityProgressBar, mView);
            }
        });
*/

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Need to check if fields are ok...
                final WaitDialogFragment dialog = WaitDialogFragment.newInstance();
                //Check that old password is correct
                mUser.setPassword(passwordOldEditText.getText().toString());
                AccountAuthenticator ag = new AccountAuthenticator(getContext(), mUser);
                ag.checkPassword(new AsyncTaskInterface<JsonItem>() {
                    @Override
                    public void processStart() {
                        FragmentManager fm = getFragmentManager();
                        dialog.show(fm,"DIALOG");
                    }
                    @Override
                    public void processFinish(JsonItem result) {
                        dialog.dismiss();
                        if (result.getKeyError().equals(AppGeneral.KEY_CODE_ERROR_INVALID_PASSWORD)){
                            passwordOldTextInputLayout.setError("Invalid password");
                        } else {
                            passwordOldTextInputLayout.setError("");
                            if (!result.getKeyError().equals(AppGeneral.KEY_CODE_SUCCESS)) {
                                Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }, mActivity);

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
