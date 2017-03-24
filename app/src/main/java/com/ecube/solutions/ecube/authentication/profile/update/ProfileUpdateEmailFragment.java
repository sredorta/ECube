package com.ecube.solutions.ecube.authentication.profile.update;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.WaitDialogFragment;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.network.JsonItem;
import com.ecube.solutions.ecube.widgets.TextInputLayoutAppWidget;

/**
 * Created by sredorta on 3/22/2017.
 */

public class ProfileUpdateEmailFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileUpdateEmailFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_CURRENT = "user.current.in";
    public static final String FRAGMENT_OUTPUT_PARAM_USER_EMAIL = "user.email.out";    //String

    //In case of rotations
    public static final String KEY_CURRENT_USER = "user.save";

    private User mUser;
    private AccountAuthenticator myAccountAuthenticator;

    // Constructor
    public static ProfileUpdateEmailFragment newInstance() {
        return new ProfileUpdateEmailFragment();
    }

    public static ProfileUpdateEmailFragment newInstance(Bundle data) {
        ProfileUpdateEmailFragment fragment = ProfileUpdateEmailFragment.newInstance();
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
        if (savedInstanceState != null) {
            mUser = (User) savedInstanceState.getParcelable(KEY_CURRENT_USER);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_update_email, container, false);
        setCurrentView(v);
        final TextInputLayoutAppWidget passwordTextInputLayout  = (TextInputLayoutAppWidget) v.findViewById(R.id.profile_update_email_TextInputLayoutAppWidget_password);
        final TextInputLayoutAppWidget emailTextInputLayout = (TextInputLayoutAppWidget) v.findViewById(R.id.profile_update_email_TextInputLayoutAppWidget_email);

        final Button submitButton = (Button) v.findViewById(R.id.profile_update_email_Button_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordTextInputLayout.isValidInput()) {
                    if (emailTextInputLayout.isValidInput()) {
                        final WaitDialogFragment dialog = WaitDialogFragment.newInstance();
                        //Check that old password is correct
                        mUser.setPassword(passwordTextInputLayout.getText());
                        AccountAuthenticator ag = new AccountAuthenticator(getContext(), mUser);
                        ag.changeEmail(emailTextInputLayout.getText(),new AsyncTaskInterface<JsonItem>() {
                            @Override
                            public void processStart() {
                                FragmentManager fm = getFragmentManager();
                                dialog.show(fm, "DIALOG");
                            }

                            @Override
                            public void processFinish(JsonItem result) {
                                dialog.dismiss();
                                if (result.getKeyError().equals(AppGeneral.KEY_CODE_ERROR_INVALID_PASSWORD)) {
                                    passwordTextInputLayout.setError("Invalid password");
                                } else {
                                    passwordTextInputLayout.setError("");
                                    if (!result.getKeyError().equals(AppGeneral.KEY_CODE_SUCCESS)) {
                                        Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_EMAIL, emailTextInputLayout.getText());
                                        sendResult(Activity.RESULT_OK);
                                    }
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
    }

}

