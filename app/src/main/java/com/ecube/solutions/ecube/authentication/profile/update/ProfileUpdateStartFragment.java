package com.ecube.solutions.ecube.authentication.profile.update;

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
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.authentication.profile.signin.ProfileSignInWithAccountFragment;
import com.ecube.solutions.ecube.authentication.profile.signin.ProfileSignInWithEmailFragment;
import com.ecube.solutions.ecube.general.AppGeneral;

/**
 * Created by sredorta on 3/9/2017.
 */
public class ProfileUpdateStartFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileSignInWithEmailFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_CURRENT = "user.current.in";

    //In case of rotations
    public static final String KEY_CURRENT_USER = "user.save";

    //Request to connect to another account in case several accounts are available
    //private static final int REQ_SIGNIN_WITH_ACCOUNTS = 1;

    private AccountAuthenticator myAccountAuthenticator;
    private User mUser;



    // Constructor
    public static ProfileUpdateStartFragment newInstance() {
        return new ProfileUpdateStartFragment();
    }

    public static ProfileUpdateStartFragment newInstance(Bundle data) {
        ProfileUpdateStartFragment fragment = ProfileUpdateStartFragment.newInstance();
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
        View v = inflater.inflate(R.layout.profile_update_start, container, false);
        setCurrentView(v);
        final TextView nameTextView = (TextView) v.findViewById(R.id.profile_update_start_TextView_name);
        final TextView emailTextView = (TextView) v.findViewById(R.id.profile_update_start_TextView_email);
        final ImageView avatarImageView = (ImageView) v.findViewById(R.id.profile_update_start_ImageView_avatar);


        nameTextView.setText(mUser.getFirstName() + " " + mUser.getLastName());
        emailTextView.setText(mUser.getEmail());
        avatarImageView.setImageBitmap(mUser.getAvatar(getContext()));


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

