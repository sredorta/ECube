package com.ecube.solutions.ecube.authentication.profile.create;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ecube.solutions.ecube.MainFragment;
import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.WaitDialogFragment;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.dao.Internationalization;
import com.ecube.solutions.ecube.authentication.profile.dao.User;


/**
 * Created by sredorta on 2/21/2017.
 */
public class ProfileCreateStartFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileCreateStartFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static String KEY_SAVED_USER = "user.saved";
    private static int REQUEST_DEFINE_NAMES = 1;
    private static int REQUEST_DEFINE_PHONE = 2;
    private static int REQUEST_DEFINE_EMAIL = 3;
    private static int REQUEST_DEFINE_PASSWORD = 4;
    private static int REQUEST_DEFINE_AVATAR = 5;

    private User myUser = new User();                       //Stores all data from the user

    // Constructor
    public static ProfileCreateStartFragment newInstance() {
        return new ProfileCreateStartFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //If we had an user on the bundle it means that screen was rotated, so we restore
        if (savedInstanceState != null) {
            if (savedInstanceState.getParcelable(KEY_SAVED_USER)!= null)
                myUser = savedInstanceState.getParcelable(KEY_SAVED_USER);
        }


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_create_start_fragment, container, false);
        setCurrentView(v);  //mView = v;
        Button nextButton = (Button) v.findViewById(R.id.profile_create_start_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileCreateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_AVATAR_BITMAP, myUser.getAvatar(getContext()));
                ProfileCreateAvatarFragment fragment = ProfileCreateAvatarFragment.newInstance(bundle);
                fragment.setTargetFragment(ProfileCreateStartFragment.this, REQUEST_DEFINE_AVATAR);
                replaceFragment(fragment);  //This comes from abstract
            }
        });
        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_DEFINE_AVATAR) {
                myUser.setAvatar((Bitmap) data.getParcelableExtra(ProfileCreateAvatarFragment.FRAGMENT_OUTPUT_PARAM_USER_AVATAR_BITMAP));
                Bundle bundle = new Bundle();
                bundle.putSerializable(ProfileCreateNamesFragment.FRAGMENT_INPUT_PARAM_USER_FIRST_NAME, myUser.getFirstName());
                bundle.putSerializable(ProfileCreateNamesFragment.FRAGMENT_INPUT_PARAM_USER_LAST_NAME, myUser.getLastName());
                ProfileCreateNamesFragment fragment = ProfileCreateNamesFragment.newInstance(bundle);
                fragment.setTargetFragment(ProfileCreateStartFragment.this, REQUEST_DEFINE_NAMES);
                replaceFragment(fragment);  //This comes from abstract
            } else if (requestCode == REQUEST_DEFINE_NAMES) {
                myUser.setFirstName((String) data.getSerializableExtra(ProfileCreateNamesFragment.FRAGMENT_OUTPUT_PARAM_USER_FIRST_NAME));
                myUser.setLastName((String) data.getSerializableExtra(ProfileCreateNamesFragment.FRAGMENT_OUTPUT_PARAM_USER_LAST_NAME));
                Bundle bundle = new Bundle();
                bundle.putSerializable(ProfileCreatePhoneFragment.FRAGMENT_INPUT_PARAM_USER_PHONE_NUMBER, myUser.getPhone());
                ProfileCreatePhoneFragment fragment = ProfileCreatePhoneFragment.newInstance(bundle);
                fragment.setTargetFragment(ProfileCreateStartFragment.this, REQUEST_DEFINE_PHONE);
                replaceFragment(fragment);  //This comes from abstract
            } else if( requestCode == REQUEST_DEFINE_PHONE) {
                myUser.setPhone((String) data.getSerializableExtra(ProfileCreatePhoneFragment.FRAGMENT_OUTPUT_PARAM_USER_PHONE_NUMBER));
                Bundle bundle = new Bundle();
                bundle.putSerializable(ProfileCreateEmailFragment.FRAGMENT_INPUT_PARAM_USER_EMAIL, myUser.getEmail());
                ProfileCreateEmailFragment fragment = ProfileCreateEmailFragment.newInstance(bundle);
                fragment.setTargetFragment(ProfileCreateStartFragment.this, REQUEST_DEFINE_EMAIL);
                replaceFragment(fragment);
            } else if( requestCode == REQUEST_DEFINE_EMAIL) {
                myUser.setEmail((String) data.getSerializableExtra(ProfileCreateEmailFragment.FRAGMENT_OUTPUT_PARAM_USER_EMAIL));
                ProfileCreatePasswordFragment fragment = ProfileCreatePasswordFragment.newInstance();
                fragment.setTargetFragment(ProfileCreateStartFragment.this, REQUEST_DEFINE_PASSWORD);
                replaceFragment(fragment);
            } else if( requestCode == REQUEST_DEFINE_PASSWORD) {
                myUser.setPassword((String) data.getSerializableExtra(ProfileCreatePasswordFragment.FRAGMENT_OUTPUT_PARAM_USER_PASSWORD));
                myUser.setAccountAccess(AccountAuthenticator.AUTHTOKEN_TYPE_STANDARD);  //We always create account with standard access first

                //Do the job !
                Log.i(TAG, "Creating user :" + myUser.getEmail());
                AccountAuthenticator ag = new AccountAuthenticator(getContext(), myUser);
                ag.createServerAndDeviceAccount(new AsyncTaskInterface() {
                    WaitDialogFragment dialog;
                    @Override
                    public void processStart() {
                        FragmentManager fm = getFragmentManager();
                        dialog = WaitDialogFragment.newInstance();
                        dialog.show(fm,"DIALOG");
                    }
                    @Override
                    public void processFinish() {
                        dialog.dismiss();
                    }
                }, mActivity);
            }
        } else {
            // Reload our fragment
            replaceFragment(this);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (DEBUG) Log.i(TAG, "Saving user in bundle...");
        outState.putParcelable(KEY_SAVED_USER, myUser);
    }
}
