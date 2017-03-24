package com.ecube.solutions.ecube.authentication.profile.update;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.WaitDialogFragment;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateAvatarFragment;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateEmailFragment;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateNamesFragment;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreatePasswordFragment;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreatePhoneFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.authentication.profile.signin.ProfileSignInWithEmailFragment;
import com.ecube.solutions.ecube.helpers.IconHelper;


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

    //Requests to other fragments
    private static int REQUEST_DEFINE_PHONE = 2;
    private static int REQUEST_DEFINE_EMAIL = 3;
    private static int REQUEST_DEFINE_PASSWORD = 4;

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

        //Fields part
        final ImageView emailSettings = (ImageView) v.findViewById(R.id.profile_update_start_ImageView_email);
        IconHelper.colorize(getContext(), emailSettings, R.color.md_lime_700);
        final TextView emailSettingsTextView = (TextView) v.findViewById(R.id.profile_update_start_TextView_email_change);
        emailSettingsTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.md_grey_900));


        final ImageView phoneSettings = (ImageView) v.findViewById(R.id.profile_update_start_ImageView_phone);
        IconHelper.colorize(getContext(), phoneSettings, R.color.md_lime_700);
        final TextView phoneSettingsTextView = (TextView) v.findViewById(R.id.profile_update_start_TextView_phone);
        phoneSettingsTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.md_grey_900));

        final ImageView passwordSettings = (ImageView) v.findViewById(R.id.profile_update_start_ImageView_password);
        IconHelper.colorize(getContext(), passwordSettings, R.color.md_lime_700);
        final TextView passSettingsTextView = (TextView) v.findViewById(R.id.profile_update_start_TextView_password);
        passSettingsTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.md_grey_900));

        final ImageView accessTypeSettings = (ImageView) v.findViewById(R.id.profile_update_start_ImageView_access);
        IconHelper.colorize(getContext(), accessTypeSettings, R.color.md_lime_700);
        final TextView accessTypeSettingsTextView = (TextView) v.findViewById(R.id.profile_update_start_TextView_access);
        accessTypeSettingsTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.md_grey_900));

        //Listeners
        v.findViewById(R.id.profile_update_start_email_cardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(ProfileUpdateEmailFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT, mUser.getEmail());
                ProfileUpdateEmailFragment fragment = ProfileUpdateEmailFragment.newInstance(bundle);
                fragment.setTargetFragment(ProfileUpdateStartFragment.this, REQUEST_DEFINE_EMAIL);
                replaceFragment(fragment);  //This comes from abstract
            }
        });

        //Listeners
        v.findViewById(R.id.profile_update_start_phone_cardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(ProfileUpdatePhoneFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT, mUser.getEmail());
                ProfileUpdatePhoneFragment fragment = ProfileUpdatePhoneFragment.newInstance(bundle);
                fragment.setTargetFragment(ProfileUpdateStartFragment.this, REQUEST_DEFINE_PHONE);
                replaceFragment(fragment);  //This comes from abstract
            }
        });

        //Listeners
        v.findViewById(R.id.profile_update_start_password_cardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(ProfileUpdatePasswordFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT, mUser.getEmail());
                ProfileUpdatePasswordFragment fragment = ProfileUpdatePasswordFragment.newInstance(bundle);
                fragment.setTargetFragment(ProfileUpdateStartFragment.this, REQUEST_DEFINE_PASSWORD);
                replaceFragment(fragment);  //This comes from abstract
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean needToUpdate = false;
        if (resultCode == Activity.RESULT_OK) {
            if( requestCode == REQUEST_DEFINE_PHONE) {
                mUser.setPhone((String) data.getSerializableExtra(ProfileCreatePhoneFragment.FRAGMENT_OUTPUT_PARAM_USER_PHONE_NUMBER));
                needToUpdate = true;
            } else if( requestCode == REQUEST_DEFINE_EMAIL) {
                mUser.setEmail((String) data.getSerializableExtra(ProfileCreateEmailFragment.FRAGMENT_OUTPUT_PARAM_USER_EMAIL));
                needToUpdate = true;
            } else if( requestCode == REQUEST_DEFINE_PASSWORD) {
                mUser.setPassword((String) data.getSerializableExtra(ProfileCreatePasswordFragment.FRAGMENT_OUTPUT_PARAM_USER_PASSWORD));
                needToUpdate = true;
            }
            if (needToUpdate) {
                //Do the job !
                Log.i(TAG, "Updating user phone :" + mUser.getPhone());
                AccountAuthenticator ag = new AccountAuthenticator(getContext(), mUser);
/*                ag.updateServerAndDeviceAccount(new AsyncTaskInterface() {
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
                }, mActivity);*/
            }
        }
        // Reload our fragment
        replaceFragment(this,this.getTag(),true);


    }




}

