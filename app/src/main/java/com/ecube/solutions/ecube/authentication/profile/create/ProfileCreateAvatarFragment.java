package com.ecube.solutions.ecube.authentication.profile.create;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.profile.dao.Avatar;
import com.ecube.solutions.ecube.authentication.profile.update.ProfileUpdateAvatarFragment;
import com.ecube.solutions.ecube.authentication.profile.update.ProfileUpdateNamesFragment;
import com.ecube.solutions.ecube.authentication.profile.update.ProfileUpdatePasswordFragment;
import com.ecube.solutions.ecube.authentication.profile.update.ProfileUpdatePhoneFragment;
import com.ecube.solutions.ecube.authentication.profile.update.ProfileUpdateStartFragment;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.widgets.AvatarAppWidget;


/**
 * Created by sredorta on 2/23/2017.
 */
public class ProfileCreateAvatarFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileCreateStartFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_AVATAR_BITMAP = "user.avatar.create.in"; //Stream in
    public static final String FRAGMENT_OUTPUT_PARAM_USER_AVATAR_BITMAP = "user.avatar.create.out"; //Stream out

    //Requests
    private static final int REQUEST_UPDATE_AVATAR = 1; //Intent to ask for gallery

    //For rotation save
    private static final String KEY_AVATAR_BITMAP = "key.avatar.bitmap";

    private Avatar mAvatar;

    // Constructor
    public static ProfileCreateAvatarFragment newInstance() {
        return new ProfileCreateAvatarFragment();
    }
    // Constructor with input arguments
    public static ProfileCreateAvatarFragment newInstance(Bundle data) {
        ProfileCreateAvatarFragment fragment = ProfileCreateAvatarFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "OnCreate");
        setRetainInstance(true);
        mAvatar = new Avatar(getContext());

        //Get the input BASE64 encoded String and convert into bitmap
        if (savedInstanceState==null) {
            mAvatar.setBitmap((Bitmap) getInputParam(ProfileCreateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_AVATAR_BITMAP));
        } else {
            mAvatar.setBitmap((Bitmap) savedInstanceState.getParcelable(KEY_AVATAR_BITMAP));
            Log.i(TAG, "Resotred bitmap !");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_create_avatar_fragment, container, false);
        setCurrentView(v);    //       mView = v;
        final AvatarAppWidget avatar = (AvatarAppWidget) mView.findViewById(R.id.profile_create_avatar_AvatarAppWidget);

        //Go to update avatar
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileUpdateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_AVATAR_BITMAP, mAvatar.getBitmap());
                bundle.putBoolean(ProfileUpdateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_UPDATE_SERVER, false);
                bundle.putString(ProfileUpdateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT, ""); //Send dummy user
                ProfileUpdateAvatarFragment fragment = ProfileUpdateAvatarFragment.newInstance(bundle);
                fragment.setTargetFragment(ProfileCreateAvatarFragment.this, REQUEST_UPDATE_AVATAR);

                //Fragment replacement with Shared Element
                setSharedElementReturnTransition(TransitionInflater.from(
                        mActivity).inflateTransition(R.transition.transition_shared_bound_and_scale).setDuration(300));
                setExitTransition(TransitionInflater.from(
                        mActivity).inflateTransition(android.R.transition.fade).setDuration(300));

                fragment.setSharedElementEnterTransition(TransitionInflater.from(
                        mActivity).inflateTransition(R.transition.transition_shared_bound_and_scale).setDuration(300));
                fragment.setEnterTransition(TransitionInflater.from(
                        mActivity).inflateTransition(android.R.transition.fade).setDuration(300));

                FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_UNDEFINED);
                transaction.addToBackStack(AppGeneral.KEY_FRAGMENT_STACK_LEVEL_UNDEFINED);
                ViewCompat.setTransitionName(avatar, getString(R.string.transitionSharedProfileUpdateStartProfileUpdateAvatar));
                transaction.addSharedElement(avatar, getString(R.string.transitionSharedProfileUpdateStartProfileUpdateAvatar));

                try {
                    transaction.commit();
                } catch (IllegalStateException e) {
                    //It means that the activity is gone in rotation also... so we need to wait that activity is back and then commit
                    Log.i(TAG, "Exception during transaction commit !!! This fragment should be retained !!!!!!!!!!!!!");
                }

                //End of fragment replacement
            }
        });

        final Fragment fm = this;

        final Button nextButton = (Button) v.findViewById(R.id.profile_create_avatar_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_AVATAR_BITMAP, mAvatar.getBitmap());
                sendResult(Activity.RESULT_OK);
                //removeFragment(fm,true);
            }
        });


        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_UPDATE_AVATAR) {
                Bitmap bmp = (Bitmap) data.getParcelableExtra(ProfileUpdateAvatarFragment.FRAGMENT_OUTPUT_PARAM_USER_AVATAR_BITMAP);
                Log.i(TAG, "Recieved bitmap of : " + bmp.getByteCount());
                mAvatar.setBitmap(bmp);
            }
        }
    }


    @Override
    public void onResume() {
        Log.i(TAG, "OnResume !!!");
        super.onResume();
        final AvatarAppWidget avatar = (AvatarAppWidget) mView.findViewById(R.id.profile_create_avatar_AvatarAppWidget);
        avatar.setImageBitmap(mAvatar.getBitmap());

    }

    //In case of rotation we save the hiden panel visibility and the bitmap of the avatar
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "OnsAveInstance");
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_AVATAR_BITMAP, mAvatar.getBitmap());

    }

}

