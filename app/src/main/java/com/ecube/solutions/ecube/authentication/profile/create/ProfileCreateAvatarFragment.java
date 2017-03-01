package com.ecube.solutions.ecube.authentication.profile.create;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
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


/**
 * Created by sredorta on 2/23/2017.
 */
public class ProfileCreateAvatarFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileCreateStartFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_AVATAR_BITMAP = "user.avatar.in"; //Stream in
    public static final String FRAGMENT_OUTPUT_PARAM_USER_AVATAR_BITMAP = "user.avatar.out"; //Stream out

    //Intent requests
    private static final int REQUEST_SELECT_PICTURE = 1; //Intent to ask for gallery
    private static final int REQUEST_TAKE_PICTURE = 2;   //Intent to ask for photo

    //For rotation save
    private static final String KEY_PANEL_STATUS = "key.panel.status";
    private static final String KEY_AVATAR_BITMAP = "key.avatar.bitmap";

    private Boolean mPanelVisible; //Visibility of the panel
    private Avatar mAvatar;
    private View hiddenPanel;

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
        mAvatar = new Avatar(getContext());

        //Get the input BASE64 encoded String and convert into bitmap
        mAvatar.setBitmap((Bitmap) getInputParam(ProfileCreateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_AVATAR_BITMAP));
        mPanelVisible = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_create_avatar_fragment, container, false);
        setCurrentView(v);    //       mView = v;

        hiddenPanel =  v.findViewById(R.id.profile_create_avatar_hidden_panel);
        //In case the device was rotated
        if (savedInstanceState != null) {
            mPanelVisible = savedInstanceState.getBoolean(KEY_PANEL_STATUS);
            if (mPanelVisible)
                hiddenPanel.setVisibility(View.VISIBLE);
            else
                hiddenPanel.setVisibility(View.GONE);
            mAvatar = new Avatar(getContext(), (Bitmap) savedInstanceState.getParcelable(KEY_AVATAR_BITMAP));
        } else {
            hiddenPanel.setVisibility(View.INVISIBLE);
            mPanelVisible = false;
        }

        final ImageView avatar = (ImageView) mView.findViewById(R.id.profile_create_avatar_imageView_avatar);
        avatar.setImageBitmap(mAvatar.getBitmap());

        final CardView cardView = (CardView) v.findViewById(R.id.profile_create_avatar_cardView);



        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideUpDown(hiddenPanel);
                //Here we pop-up the bottom side where we can choose between gallery/picture...
            }
        });



        final Button nextButton = (Button) v.findViewById(R.id.profile_create_avatar_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_AVATAR_BITMAP, mAvatar.getBitmap());
                sendResult(Activity.RESULT_OK);
            }
        });

        //Handle reset Avatar
        final ImageView removeAvatar = (ImageView) v.findViewById(R.id.profile_create_avatar_imageView_delete);
        removeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView avatar = (ImageView) mView.findViewById(R.id.profile_create_avatar_imageView_avatar);
                mAvatar = new Avatar(getContext());
                fadeInNewBitmap(mAvatar.getBitmap());
            }
        });

        //Handle open galery
        final ImageView openGallery = (ImageView) v.findViewById(R.id.profile_create_avatar_imageView_gallery);
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // in onCreate or any event where your want the user to
                // select a file
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_SELECT_PICTURE);
            }
        });

        //Handle take photo
        final ImageView openCamera = (ImageView) v.findViewById(R.id.profile_create_avatar_imageView_photo);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final boolean canTakePhoto = mAvatar.getPhotoFile() != null && captureImage.resolveActivity(mActivity.getPackageManager()) != null;
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if we can open photo
                if (canTakePhoto) {
                    final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri uri = Uri.fromFile(mAvatar.getPhotoFile());
                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(captureImage, REQUEST_TAKE_PICTURE);
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    mAvatar.getAvatarFromUri(selectedImageUri);
                    slideUpDown(hiddenPanel);
                    fadeInNewBitmap(mAvatar.getBitmap());
                }
            } else if (requestCode == REQUEST_TAKE_PICTURE) {
                    mAvatar.getAvatarFromFile(mAvatar.getPhotoFile());
                    slideUpDown(hiddenPanel);
                    fadeInNewBitmap(mAvatar.getBitmap());
            }
        }

    }

    //In case of rotation we save the hiden panel visibility and the bitmap of the avatar
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_PANEL_STATUS, mPanelVisible);
        outState.putParcelable(KEY_AVATAR_BITMAP, mAvatar.getBitmap());

    }

    //Show / hide the bottom view
    public void slideUpDown(final View view) {
        if (hiddenPanel.getVisibility() == View.INVISIBLE || hiddenPanel.getVisibility() == View.GONE) {
            // Show the panel
            Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_bottom);

            hiddenPanel.startAnimation(bottomUp);
            hiddenPanel.setVisibility(View.VISIBLE);
            mPanelVisible = true;
        }
        else {
            // Hide the Panel
            Animation bottomDown = AnimationUtils.loadAnimation(getContext(), R.anim.exit_to_bottom);
            hiddenPanel.startAnimation(bottomDown);
            hiddenPanel.setVisibility(View.GONE);
            mPanelVisible = false;
        }
    }

    //Swap current avatar with the new one in the UI
    private void fadeInNewBitmap(final Bitmap bitmap) {
        final ImageView avatar = (ImageView) mView.findViewById(R.id.profile_create_avatar_imageView_avatar);
        Animation fadeIn;

        fadeIn = AnimationUtils.loadAnimation(getContext(),R.anim.enter_fade);
        fadeIn.setDuration(1000);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                avatar.setImageBitmap(bitmap);
            }
            @Override
            public void onAnimationEnd(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        avatar.startAnimation(fadeIn);
    }

}

