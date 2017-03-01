package com.ecube.solutions.ecube.authentication.profile.create;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.profile.dao.Avatar;
import com.ecube.solutions.ecube.authentication.profile.dao.User;

import java.io.File;


/**
 * Created by sredorta on 2/21/2017.
 */
public class ProfileCreateStartFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileCreateStartFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    private View mView;
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
            if (savedInstanceState.getSerializable(KEY_SAVED_USER)!= null)
                myUser = (User) savedInstanceState.getSerializable(KEY_SAVED_USER);
            myUser.print("Back from bundle : ");
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
                Bitmap myBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.icon_camera);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileCreateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_AVATAR_BITMAP, myBitmap);
                ProfileCreateAvatarFragment fragment = ProfileCreateAvatarFragment.newInstance(bundle);
                fragment.setTargetFragment(ProfileCreateStartFragment.this, REQUEST_DEFINE_AVATAR);
                //Now replace the AuthenticatorFragment with the SignInFragment
                replaceFragment(fragment,"test",true);  //This comes from abstract
            }
        });
        return v;
    }


    @Override
    public void onBackPressed() {
        //if (hiddenPanel.getVisibility() == View.VISIBLE) slideUpDown(mView);
       // else {
            super.onBackPressed();
        //}
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_DEFINE_AVATAR) {
                Log.i(TAG, "Back from AVATAR");
                Bitmap myBitmap = (Bitmap) data.getParcelableExtra(ProfileCreateAvatarFragment.FRAGMENT_OUTPUT_PARAM_USER_AVATAR_BITMAP);
                myUser.setAvatarBitmap((Bitmap) data.getParcelableExtra(ProfileCreateAvatarFragment.FRAGMENT_OUTPUT_PARAM_USER_AVATAR_BITMAP));
                myUser.print("This is what we have after names:");
                ProfileCreateNamesFragment fragment = ProfileCreateNamesFragment.newInstance();
                fragment.setTargetFragment(ProfileCreateStartFragment.this, REQUEST_DEFINE_NAMES);
                //Now replace the AuthenticatorFragment with the SignInFragment
                replaceFragment(fragment,"test",true);  //This comes from abstract
            } else if (requestCode == REQUEST_DEFINE_NAMES) {
                myUser.setFirstName((String) data.getSerializableExtra(ProfileCreateNamesFragment.FRAGMENT_OUTPUT_PARAM_USER_FIRST_NAME));
                myUser.setLastName((String) data.getSerializableExtra(ProfileCreateNamesFragment.FRAGMENT_OUTPUT_PARAM_USER_LAST_NAME));
                myUser.print("This is what we have after names:");
                //We now start the fragment for entering phone details
                ProfileCreatePhoneFragment fragment = ProfileCreatePhoneFragment.newInstance();
                fragment.setTargetFragment(ProfileCreateStartFragment.this, REQUEST_DEFINE_PHONE);
                //Now replace the AuthenticatorFragment with the SignInFragment
                replaceFragment(fragment,"test",true);  //This comes from abstract
            } else if( requestCode == REQUEST_DEFINE_PHONE) {
                myUser.setPhone((String) data.getSerializableExtra(ProfileCreatePhoneFragment.FRAGMENT_OUTPUT_PARAM_USER_PHONE_NUMBER));
                myUser.print("after phone :");
                //ProfileCreateEmailFragment fragment = ProfileCreateEmailFragment.newInstance();
                //fragment.setTargetFragment(ProfileCreateStartFragment.this, REQUEST_DEFINE_EMAIL);
                //replaceFragment(fragment, "test", true);

            }
            /*else if( requestCode == REQUEST_DEFINE_EMAIL) {
                myUser.setEmail((String) data.getSerializableExtra(ProfileCreateEmailFragment.FRAGMENT_OUTPUT_PARAM_USER_EMAIL));
                myUser.print("after email :");
                ProfileCreatePasswordFragment fragment = ProfileCreatePasswordFragment.newInstance();
                fragment.setTargetFragment(ProfileCreateStartFragment.this, REQUEST_DEFINE_PASSWORD);
                replaceFragment(fragment, "test", true);
            } else if( requestCode == REQUEST_DEFINE_PASSWORD) {
                myUser.setPassword((String) data.getSerializableExtra(ProfileCreatePasswordFragment.FRAGMENT_OUTPUT_PARAM_USER_PASSWORD));
                myUser.print("after password :");
                User finalUser = new User();
                finalUser.initEmpty(getContext());
                finalUser.update(myUser);
                finalUser.print("Before account creation :");
                // Now we create the account with all the data
                AccountGeneral myAccountGeneral = new AccountGeneral(getContext());
                myAccountGeneral.createServerAndDeviceAccount(mActivity, finalUser);

            }
*/
        } else {
            // Reload our fragment
            replaceFragment(this, "test", true);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (DEBUG) Log.i(TAG, "Saving user in bundle...");
        outState.putSerializable(KEY_SAVED_USER, myUser);
    }
}
