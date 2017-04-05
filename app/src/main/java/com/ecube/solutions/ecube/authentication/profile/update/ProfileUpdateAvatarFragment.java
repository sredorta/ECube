package com.ecube.solutions.ecube.authentication.profile.update;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.authenticator.AuthenticatorActivity;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateAvatarFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.Avatar;
import com.ecube.solutions.ecube.authentication.profile.dao.Internationalization;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.dialogs.NoInternetDialogFragment;
import com.ecube.solutions.ecube.dialogs.WaitDialogFragment;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.helpers.IconHelper;
import com.ecube.solutions.ecube.network.JsonItem;
import com.ecube.solutions.ecube.widgets.AvatarAppWidget;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by sredorta on 3/29/2017.
 */

public class ProfileUpdateAvatarFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileUpdateAvatarFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_AVATAR_BITMAP = "avatar.bitmap.in";
    public static final String FRAGMENT_INPUT_PARAM_USER_UPDATE_SERVER = "user.server.update.in";
    public static final String FRAGMENT_INPUT_PARAM_USER_CURRENT = "user.current.in";
    public static final String FRAGMENT_OUTPUT_PARAM_USER_AVATAR_BITMAP = "avatar.bitmap.out";    //Bitmap

    //Intent requests
    private static final int REQUEST_SELECT_PICTURE = 1; //Intent to ask for gallery
    private static final int REQUEST_TAKE_PICTURE = 2;   //Intent to ask for photo

    //In case of rotations
    public static final String KEY_AVATAR_BITMAP = "bitmap.save";
    public static final String KEY_SERVER_UPDATES = "server.save";
    public static final String KEY_CURRENT_USER = "user.save";

    //Avatar object to store bitmap
    private Avatar mAvatar;
    private User mUser;
    private AccountAuthenticator myAccountAuthenticator;

    //Graphic elements
    private AvatarAppWidget avatarAvatarAppWidget;

    //Boolean to determine if we do the update in the server and the account or not
    private boolean makeUpdateOnServer;


    // Constructor
    public static ProfileUpdateAvatarFragment newInstance() {
        return new ProfileUpdateAvatarFragment();
    }

    public static ProfileUpdateAvatarFragment newInstance(Bundle data) {
        ProfileUpdateAvatarFragment fragment = ProfileUpdateAvatarFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // We are using async task so we need to retain it

        mAvatar = new Avatar(getContext());
        mUser = new User();

        //Get the input BASE64 encoded String and convert into bitmap
        mAvatar.setBitmap((Bitmap) getInputParam(ProfileUpdateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_AVATAR_BITMAP));
        makeUpdateOnServer = (Boolean) getInputParam(ProfileUpdateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_UPDATE_SERVER);
        String email = ((String) getInputParam(ProfileUpdateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT));
        mUser.setEmail(email);
       if (savedInstanceState != null) {
            mAvatar.setBitmap((Bitmap) savedInstanceState.getParcelable(KEY_AVATAR_BITMAP) );
            mUser.setEmail(savedInstanceState.getString(KEY_CURRENT_USER));
            makeUpdateOnServer = savedInstanceState.getBoolean(KEY_SERVER_UPDATES);

        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_update_avatar, container, false);
        setCurrentView(v);
        avatarAvatarAppWidget = (AvatarAppWidget) v.findViewById(R.id.profile_update_avatar_AvatarAppWidget);
        avatarAvatarAppWidget.setImageBitmap(mAvatar.getBitmap());
        final FloatingActionButton fabCamera = (FloatingActionButton) v.findViewById(R.id.profile_update_avatar_FloatingActionButton_camera);
        final FloatingActionButton fabGallery = (FloatingActionButton) v.findViewById(R.id.profile_update_avatar_FloatingActionButton_gallery);
        final FloatingActionButton fabDelete = (FloatingActionButton) v.findViewById(R.id.profile_update_avatar_FloatingActionButton_delete);



        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAvatar = new Avatar(getContext());
                fadeInNewBitmap(mAvatar.getBitmap());
            }
        });

        fabGallery.setOnClickListener(new View.OnClickListener() {
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

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final boolean canTakePhoto = mAvatar.getPhotoFile() != null && captureImage.resolveActivity(mActivity.getPackageManager()) != null;
        fabCamera.setOnClickListener(new View.OnClickListener() {
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




        final Button nextButton = (Button) v.findViewById(R.id.profile_update_avatar_Button_apply);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (makeUpdateOnServer) {
                    Log.i(TAG, "updates required !!!!!!!!!!!!!!!!!!!");
                    final WaitDialogFragment dialog = WaitDialogFragment.newInstance();
                    myAccountAuthenticator = new AccountAuthenticator(getContext());
                    mUser = myAccountAuthenticator.getDataFromDeviceAccount(myAccountAuthenticator.getAccount(mUser.getEmail()));
                    mUser.setAvatar(mAvatar.getBitmap());
                    AccountAuthenticator ag = new AccountAuthenticator(getContext(), mUser);
                    ag.changeAvatar(mAvatar.getBitmap(), new AsyncTaskInterface<JsonItem>() {
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
                                putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_AVATAR_BITMAP, mAvatar.getBitmap());
                                sendResult(Activity.RESULT_OK);
                                mActivity.onBackPressed();
                            }
                        }
                    }, mActivity);

                } else {
                    putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_AVATAR_BITMAP, mAvatar.getBitmap());
                    sendResult(Activity.RESULT_OK);
                    mActivity.onBackPressed();
                }
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final FloatingActionButton fabCamera = (FloatingActionButton) mView.findViewById(R.id.profile_update_avatar_FloatingActionButton_camera);
        final FloatingActionButton fabGallery = (FloatingActionButton) mView.findViewById(R.id.profile_update_avatar_FloatingActionButton_gallery);
        final FloatingActionButton fabDelete = (FloatingActionButton) mView.findViewById(R.id.profile_update_avatar_FloatingActionButton_delete);

        fabCamera.setImageDrawable(IconHelper.colorize(getContext(),R.drawable.icon_add_photo,R.color.colorWhite));
        fabGallery.setImageDrawable(IconHelper.colorize(getContext(),R.drawable.icon_gallery,R.color.colorWhite));
        fabDelete.setImageDrawable(IconHelper.colorize(getContext(),R.drawable.icon_delete,R.color.colorWhite));

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(fabCamera, "alpha", 0, 1).setDuration(500);
        fadeIn.setStartDelay(300);
        fadeIn.start();

        fadeIn = ObjectAnimator.ofFloat(fabGallery, "alpha", 0, 1).setDuration(500);
        fadeIn.setStartDelay(300);
        fadeIn.start();

        fadeIn = ObjectAnimator.ofFloat(fabDelete, "alpha", 0, 1).setDuration(500);
        fadeIn.setStartDelay(300);
        fadeIn.start();
    }

    //Swap current avatar with the new one in the UI
    private void fadeInNewBitmap(final Bitmap bitmap) {
        final AvatarAppWidget avatar = (AvatarAppWidget) mView.findViewById(R.id.profile_update_avatar_AvatarAppWidget);
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

    //Save user in case of rotation
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_AVATAR_BITMAP, mAvatar.getBitmap());
        outState.putBoolean(KEY_SERVER_UPDATES, makeUpdateOnServer);
        outState.putString(KEY_CURRENT_USER, mUser.getEmail());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    mAvatar.getAvatarFromUri(selectedImageUri);
                    fadeInNewBitmap(mAvatar.getBitmap());
                }
            } else if (requestCode == REQUEST_TAKE_PICTURE) {
                mAvatar.getAvatarFromFile(mAvatar.getPhotoFile());
                fadeInNewBitmap(mAvatar.getBitmap());
            }
        }

    }
}
