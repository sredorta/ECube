package com.ecube.solutions.ecube.authentication.profile.update;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
 * Created by sredorta on 3/27/2017.
 */

public class ProfileResetPasswordFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileUpdateEmailFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_CURRENT = "user.current.in";

    //In case of rotations
    public static final String KEY_CURRENT_USER = "user.save";

    private User mUser;
    private AccountAuthenticator myAccountAuthenticator;

    // Constructor
    public static ProfileResetPasswordFragment newInstance() {
        return new ProfileResetPasswordFragment();
    }

    public static ProfileResetPasswordFragment newInstance(Bundle data) {
        ProfileResetPasswordFragment fragment = ProfileResetPasswordFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String email = (String) getInputParam(ProfileResetPasswordFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT);
        mUser = new User();
        mUser.setEmail(email);

        //Restore user in case of rotation
        if (savedInstanceState != null) {
            mUser.setEmail((String) savedInstanceState.getString(KEY_CURRENT_USER));
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_reset_password, container, false);
        setCurrentView(v);
        TextView emailTextView = (TextView) v.findViewById(R.id.profile_reset_password_email);
        Button resetButton = (Button) v.findViewById(R.id.profile_reset_password_Button_submit);
        Button cancelButton = (Button) v.findViewById(R.id.profile_reset_password_Button_cancel);
        emailTextView.setText(mUser.getEmail());

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_CANCELED);
            }
        });

        //send email with new automatic generated password
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountAuthenticator ag = new AccountAuthenticator(getContext(), mUser);
                ag.resetPassword(new AsyncTaskInterface<JsonItem>() {
                    WaitDialogFragment dialog;
                    @Override
                    public void processStart() {
                        FragmentManager fm = getFragmentManager();
                        dialog = WaitDialogFragment.newInstance();
                        dialog.show(fm,"DIALOG");
                    }


                    @Override
                    public void processFinish(JsonItem result) {
                        dialog.dismiss();
                        if (!result.getKeyError().equals(AppGeneral.KEY_CODE_SUCCESS)) {
                            Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            sendResult(Activity.RESULT_OK);
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
        outState.putString(KEY_CURRENT_USER, mUser.getEmail());
    }

}

