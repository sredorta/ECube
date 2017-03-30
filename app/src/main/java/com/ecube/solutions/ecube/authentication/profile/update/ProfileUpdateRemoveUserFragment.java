package com.ecube.solutions.ecube.authentication.profile.update;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.dialogs.WaitDialogFragment;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.network.JsonItem;
import com.ecube.solutions.ecube.widgets.TextInputLayoutAppWidget;

/**
 * Created by sredorta on 3/28/2017.
 */

public class ProfileUpdateRemoveUserFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileUpdateEmailFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_CURRENT = "user.current.in";
    public static final String FRAGMENT_OUTPUT_PARAM_USER_EMAIL = "user.email.out";    //String

    //In case of rotations
    public static final String KEY_CURRENT_USER = "user.save";

    private String mUserEmail;
    private AccountAuthenticator myAccountAuthenticator;

    // Constructor
    public static ProfileUpdateRemoveUserFragment newInstance() {
        return new ProfileUpdateRemoveUserFragment();
    }

    public static ProfileUpdateRemoveUserFragment newInstance(Bundle data) {
        ProfileUpdateRemoveUserFragment fragment = ProfileUpdateRemoveUserFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // We are using async task so we need to retain it
        mUserEmail = (String) getInputParam(ProfileUpdateStartFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT);
        //Get account details from the device
        //Restore user in case of rotation
        if (savedInstanceState != null) {
            mUserEmail = (String) savedInstanceState.getString(KEY_CURRENT_USER);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_update_remove_user, container, false);
        setCurrentView(v);
        final TextView emailTextView  = (TextView) v.findViewById(R.id.profile_update_remove_user_TextView_email);
        final Button cancelButton  = (Button) v.findViewById(R.id.profile_update_remove_user_Button_cancel);
        final Button submitButton  = (Button) v.findViewById(R.id.profile_update_remove_user_Button_submit);

        emailTextView.setText(mUserEmail);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_CANCELED);
            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        final WaitDialogFragment dialog = WaitDialogFragment.newInstance();
                        //Check that old password is correct
                        User myUserToRemove = new User();
                        myUserToRemove.setEmail(mUserEmail);
                        AccountAuthenticator ag = new AccountAuthenticator(getContext(),myUserToRemove);
                        ag.removeServerAndDeviceAccount(new AsyncTaskInterface<Intent>() {
                            @Override
                            public void processStart() {
                                FragmentManager fm = getFragmentManager();
                                dialog.show(fm, "DIALOG");
                            }

                            @Override
                            public void processFinish(Intent result) {
                                dialog.dismiss();
                                if (result.hasExtra(AccountAuthenticator.KEY_ERROR_MESSAGE)) {
                                    Toast.makeText(mActivity, result.getStringExtra(AccountAuthenticator.KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                                } else {
                                    //If we are here we are exiting activity so we do not need to do anything else
                                    Toast.makeText(mActivity, "User removed succesfully !", Toast.LENGTH_SHORT).show();
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
        outState.putString(KEY_CURRENT_USER, mUserEmail);
    }

}

