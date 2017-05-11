package com.ecube.solutions.ecube.authentication.profile.update;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
 * Created by sredorta on 3/29/2017.
 */

public class ProfileUpdateNamesFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileUpdateNamesFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_CURRENT = "user.current.in";
    public static final String FRAGMENT_OUTPUT_PARAM_USER_FIRST_NAME = "user.first_name.out";    //String
    public static final String FRAGMENT_OUTPUT_PARAM_USER_LAST_NAME = "user.last_name.out";    //String


    //In case of rotations
    public static final String KEY_CURRENT_USER = "user.save";

    private User mUser;
    private AccountAuthenticator myAccountAuthenticator;

    // Constructor
    public static ProfileUpdateNamesFragment newInstance() {
        return new ProfileUpdateNamesFragment();
    }

    public static ProfileUpdateNamesFragment newInstance(Bundle data) {
        ProfileUpdateNamesFragment fragment = ProfileUpdateNamesFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "OnCreate !");
        setRetainInstance(true); // We are using async task so we need to retain it
        String email = (String) getInputParam(ProfileUpdateStartFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT);
        //Check that we have an account with current user and if not exit

        //Restore user in case of rotation
        mUser = new User();
        if (savedInstanceState != null) {
            mUser = (User) savedInstanceState.getParcelable(KEY_CURRENT_USER);
            Log.i(TAG, "Restored user : " + mUser.getEmail());
        } else {
            //Get account details from the device
            myAccountAuthenticator = new AccountAuthenticator(getContext());
            mUser = myAccountAuthenticator.getDataFromDeviceAccount(myAccountAuthenticator.getAccount(email));
        }


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_update_names, container, false);
        setCurrentView(v);
        final TextInputLayoutAppWidget firstNameTextInputLayout  = (TextInputLayoutAppWidget) v.findViewById(R.id.profile_update_names_TextInputLayoutAppWidget_firstName);
        final TextInputLayoutAppWidget lastNameTextInputLayout = (TextInputLayoutAppWidget) v.findViewById(R.id.profile_update_names_TextInputLayoutAppWidget_lastName);
        //Set initial values if they exist
        if (mUser.getFirstName()!= null)
            firstNameTextInputLayout.setText(mUser.getFirstName());
        if(mUser.getLastName()!= null)
            lastNameTextInputLayout.setText(mUser.getLastName());

            final Button submitButton = (Button) v.findViewById(R.id.profile_update_names_Button_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SERGI SERGI:::", "Submitting !!!");
                if (firstNameTextInputLayout.isValidInput()) {
                    if (lastNameTextInputLayout.isValidInput()) {
                        final WaitDialogFragment dialog = WaitDialogFragment.newInstance();
                        mUser.setFirstName(firstNameTextInputLayout.getText());
                        mUser.setLastName(lastNameTextInputLayout.getText());
                        AccountAuthenticator ag = new AccountAuthenticator(getContext(), mUser);
                        ag.changeNames(firstNameTextInputLayout.getText(), lastNameTextInputLayout.getText(), new AsyncTaskInterface<JsonItem>() {
                            @Override
                            public void processStart() {
                                FragmentManager fm = getFragmentManager();
                                dialog.show(fm, "DIALOG");
                            }

                            @Override
                            public void processFinish(JsonItem result) {
                                dialog.dismiss();
                                    if (!result.getKeyError().equals(AppGeneral.KEY_CODE_SUCCESS)) {
                                        //Need to set the toast on mActivity and not context as we could get a crash during rotation
                                        Toast.makeText(mActivity, result.getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_FIRST_NAME, firstNameTextInputLayout.getText());
                                        putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_LAST_NAME, lastNameTextInputLayout.getText());
                                        sendResult(Activity.RESULT_OK);
                                    }
                            }
                        }, mActivity);
                    }
                }
            }
        });

        return v;
    }
    //Save user in case of rotation
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_CURRENT_USER, mUser);
        Log.i(TAG, "Saved user : " + mUser.getEmail());
    }

}

