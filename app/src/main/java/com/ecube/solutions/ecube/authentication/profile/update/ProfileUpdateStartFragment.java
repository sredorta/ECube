package com.ecube.solutions.ecube.authentication.profile.update;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionValues;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateEmailFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.helpers.IconHelper;
import com.ecube.solutions.ecube.widgets.AvatarAppWidget;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sredorta on 3/9/2017.
 */
public class ProfileUpdateStartFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileUpdateStartFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_CURRENT = "user.current.in";

    //In case of rotations
    public static final String KEY_CURRENT_USER = "user.save";

    //Requests to other fragments
    private static int REQUEST_DEFINE_NAMES = 1;
    private static int REQUEST_DEFINE_PHONE = 2;
    private static int REQUEST_DEFINE_EMAIL = 3;
    private static int REQUEST_DEFINE_PASSWORD = 4;
    private static int REQUEST_REMOVE_USER = 5;
    private static int REQUEST_DEFINE_AVATAR = 6;

    private AccountAuthenticator myAccountAuthenticator;
    private User mUser;

    //GUI elements
    private AvatarAppWidget avatarAvatarAppWidget;

    //RecycleViewer part
    private ProfileUpdateStartFragment.MenuListAdapter mAdapter;
    private RecyclerView mMenuRecycleViewer;
    private List<MenuItem> mMenuItems = new ArrayList<>();



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
        mUser = new User();
        mUser.setEmail(email);
        //Get account details from the device
        myAccountAuthenticator = new AccountAuthenticator(getContext());
        //Check that we have an account with current user and if not exit

        //Restore user in case of rotation
        if (savedInstanceState!= null) {
            mUser.setEmail((String) savedInstanceState.getString(KEY_CURRENT_USER));
        }
        mUser = myAccountAuthenticator.getDataFromDeviceAccount(myAccountAuthenticator.getAccount(mUser));

        //Init the List of items
        initMenuItems();
    }

    private void initMenuItems() {
        MenuItem myItem = new MenuItem();
        myItem.setText(getContext().getString(R.string.profile_update_change_name));
        myItem.setDrawable(IconHelper.colorize(getContext(),R.drawable.icon_settings_names,R.color.md_lime_700));
        myItem.setAction("names");
        mMenuItems.add(myItem);

        myItem = new MenuItem();
        myItem.setText(getContext().getString(R.string.profile_update_change_avatar));
        myItem.setDrawable(IconHelper.colorize(getContext(),R.drawable.icon_settings_avatar,R.color.md_lime_700));
        myItem.setAction("avatar");
        mMenuItems.add(myItem);

        myItem = new MenuItem();
        myItem.setText(getContext().getString(R.string.profile_update_change_email));
        myItem.setDrawable(IconHelper.colorize(getContext(),R.drawable.icon_settings_email,R.color.md_lime_700));
        myItem.setAction("email");
        mMenuItems.add(myItem);

        myItem = new MenuItem();
        myItem.setText(getContext().getString(R.string.profile_update_change_phone));
        myItem.setDrawable(IconHelper.colorize(getContext(),R.drawable.icon_settings_phone,R.color.md_lime_700));
        myItem.setAction("phone");
        mMenuItems.add(myItem);

        myItem = new MenuItem();
        myItem.setText(getContext().getString(R.string.profile_update_change_password));
        myItem.setDrawable(IconHelper.colorize(getContext(),R.drawable.icon_settings_password,R.color.md_lime_700));
        myItem.setAction("password");
        mMenuItems.add(myItem);

        myItem = new MenuItem();
        myItem.setText(getContext().getString(R.string.profile_update_change_access_type));
        myItem.setDrawable(IconHelper.colorize(getContext(),R.drawable.icon_settings_account,R.color.md_lime_700));
        myItem.setAction("access");
        mMenuItems.add(myItem);

        myItem = new MenuItem();
        myItem.setText(getContext().getString(R.string.profile_update_remove_account));
        myItem.setDrawable(IconHelper.colorize(getContext(),R.drawable.icon_settings_remove_account,R.color.md_lime_700));
        myItem.setAction("remove");
        mMenuItems.add(myItem);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_update_start, container, false);
        setCurrentView(v);
        initHeaderAccount();

        //Update the RecycleView
        mMenuRecycleViewer = (RecyclerView) v.findViewById(R.id.profile_update_start_RecycleViewer);
        mMenuRecycleViewer.setLayoutManager(new LinearLayoutManager(mActivity));
        updateMenuRecycleViewer();

        return v;
    }

    public void initHeaderAccount() {
        final TextView nameTextView = (TextView) getCurrentView().findViewById(R.id.profile_update_start_TextView_name_header);
        final TextView emailTextView = (TextView) getCurrentView().findViewById(R.id.profile_update_start_TextView_email_header);
        final TextView phoneTextView = (TextView) getCurrentView().findViewById(R.id.profile_update_start_TextView_phone_header);
        final TextView creationTextView = (TextView) getCurrentView().findViewById(R.id.profile_update_start_TextView_creation);
        avatarAvatarAppWidget = (AvatarAppWidget) getCurrentView().findViewById(R.id.profile_update_start_AvatarAppWidget_header);

        nameTextView.setText(mUser.getFirstName() + " " + mUser.getLastName());
        emailTextView.setText(mUser.getEmail());
        phoneTextView.setText(mUser.getPhone());
        creationTextView.setText(mUser.getCreationTimeFormatted());
        avatarAvatarAppWidget.setImageBitmap(mUser.getAvatar(getContext()));
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_DEFINE_NAMES) {
                mUser.setFirstName(data.getStringExtra(ProfileUpdateNamesFragment.FRAGMENT_OUTPUT_PARAM_USER_FIRST_NAME));
                mUser.setLastName(data.getStringExtra(ProfileUpdateNamesFragment.FRAGMENT_OUTPUT_PARAM_USER_LAST_NAME));
                mUser.print("Comming back from define names:");
                replaceFragment(this,this.getTag(),true);
            } else if ( requestCode == REQUEST_DEFINE_AVATAR) {
                mUser.setAvatar((Bitmap) data.getParcelableExtra(ProfileUpdateAvatarFragment.FRAGMENT_OUTPUT_PARAM_USER_AVATAR_BITMAP));
                Log.i(TAG, "Refreshed drawable !!!!!!!!!!!!!!!!!!!");
            } else if ( requestCode == REQUEST_DEFINE_PHONE) {
                mUser.setPhone(data.getStringExtra(ProfileUpdatePhoneFragment.FRAGMENT_OUTPUT_PARAM_USER_PHONE));
                replaceFragment(this,this.getTag(),true);
            } else if( requestCode == REQUEST_DEFINE_EMAIL) {
                mUser.setEmail(data.getStringExtra(ProfileCreateEmailFragment.FRAGMENT_OUTPUT_PARAM_USER_EMAIL));
                replaceFragment(this,this.getTag(),true);
            } else if( requestCode == REQUEST_DEFINE_PASSWORD) {
                mUser.setPassword(data.getStringExtra(ProfileUpdatePasswordFragment.FRAGMENT_OUTPUT_PARAM_USER_PASSWORD));
                replaceFragment(this,this.getTag(),true);
            }
        }
        hideInputKeyBoard();
        // Reload our fragment in any case


    }

    @Override
    public void onResume() {
        super.onResume();
        initHeaderAccount();
    }

    //Save user in case of rotation
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_USER, mUser.getEmail());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // RecycleViewer part
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Updates the recycleview
    private void updateMenuRecycleViewer() {
        mAdapter = new ProfileUpdateStartFragment.MenuListAdapter(mMenuItems);
        mMenuRecycleViewer.setAdapter(mAdapter);
    }

    private class MenuListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView mMenuCardView;
        private TextView mMenuTextView;
        private ImageView mMenuImageView;
        private String mAction = "default";


        private MenuListHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mMenuCardView = (CardView) itemView.findViewById(R.id.general_menu_item_cardView);
            mMenuTextView = (TextView) itemView.findViewById(R.id.general_menu_item_TextView_title);
            mMenuImageView = (ImageView) itemView.findViewById(R.id.general_menu_item_ImageView);
        }


        @Override
        public void onClick(View view) {

            if (mAction.equals("names")) {
                Bundle bundle = new Bundle();
                bundle.putString(ProfileUpdateNamesFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT, mUser.getEmail());
                ProfileUpdateNamesFragment fragment = ProfileUpdateNamesFragment.newInstance(bundle);
                fragment.setTargetFragment(ProfileUpdateStartFragment.this, REQUEST_DEFINE_NAMES);
                replaceFragment(fragment);  //This comes from abstract
            } else if (mAction.equals("avatar")) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileUpdateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_AVATAR_BITMAP, mUser.getAvatar(getContext()));
                bundle.putBoolean(ProfileUpdateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_UPDATE_SERVER, true);
                bundle.putString(ProfileUpdateAvatarFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT, mUser.getEmail());
                ProfileUpdateAvatarFragment fragment = ProfileUpdateAvatarFragment.newInstance(bundle);
                fragment.setTargetFragment(ProfileUpdateStartFragment.this, REQUEST_DEFINE_AVATAR);

/////////////////////// Fragment replacement with sharedElement !
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
                ViewCompat.setTransitionName(avatarAvatarAppWidget, getString(R.string.transitionSharedProfileUpdateStartProfileUpdateAvatar));
                transaction.addSharedElement(avatarAvatarAppWidget, getString(R.string.transitionSharedProfileUpdateStartProfileUpdateAvatar));

                try {
                    transaction.commit();
                } catch (IllegalStateException e) {
                    //It means that the activity is gone in rotation also... so we need to wait that activity is back and then commit
                    Log.i(TAG, "Exception during transaction commit !!! This fragment should be retained !!!!!!!!!!!!!");
                }

///////////////////////                replaceFragment(fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_UNDEFINED,false);  //This comes from abstract
            } else if (mAction.equals("email")) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ProfileUpdateEmailFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT, mUser.getEmail());
                    ProfileUpdateEmailFragment fragment = ProfileUpdateEmailFragment.newInstance(bundle);
                    fragment.setTargetFragment(ProfileUpdateStartFragment.this, REQUEST_DEFINE_EMAIL);
                    replaceFragment(fragment);  //This comes from abstract
            } else if( mAction.equals("phone")) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ProfileUpdatePhoneFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT, mUser.getEmail());
                    ProfileUpdatePhoneFragment fragment = ProfileUpdatePhoneFragment.newInstance(bundle);
                    fragment.setTargetFragment(ProfileUpdateStartFragment.this, REQUEST_DEFINE_PHONE);
                    replaceFragment(fragment);  //This comes from abstract
            } else if (mAction.equals("password")) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ProfileUpdatePasswordFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT, mUser.getEmail());
                    ProfileUpdatePasswordFragment fragment = ProfileUpdatePasswordFragment.newInstance(bundle);
                    fragment.setTargetFragment(ProfileUpdateStartFragment.this, REQUEST_DEFINE_PASSWORD);
                    replaceFragment(fragment);  //This comes from abstract
            } else if (mAction.equals("remove")) {
                //Case of remove account... in this case we need to be carefull as we lose mUser after
                Bundle bundle = new Bundle();
                bundle.putString(ProfileUpdateRemoveUserFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT, mUser.getEmail());
                ProfileUpdateRemoveUserFragment fragment = ProfileUpdateRemoveUserFragment.newInstance(bundle);
                fragment.setTargetFragment(ProfileUpdateStartFragment.this, REQUEST_REMOVE_USER);
                replaceFragment(fragment);  //This comes from abstract
            }
        }


        public void bindMenuItem(MenuItem myItem, ProfileUpdateStartFragment.MenuListHolder holder ) {
            mMenuTextView.setText(myItem.getText());
            mMenuImageView.setImageDrawable(myItem.getDrawable());
            mAction = myItem.getAction();
        }


    }

    private class MenuListAdapter extends RecyclerView.Adapter<ProfileUpdateStartFragment.MenuListHolder> {

        public MenuListAdapter(List<MenuItem> menuItems) {
            mMenuItems = menuItems;
        }

        @Override
        public ProfileUpdateStartFragment.MenuListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
            View view = layoutInflater.inflate(R.layout.general_menu_item, parent,false);
            return new ProfileUpdateStartFragment.MenuListHolder(view);
        }

        @Override
        public void onBindViewHolder(ProfileUpdateStartFragment.MenuListHolder holder, int position) {
            MenuItem menuItem = mMenuItems.get(position);
            holder.bindMenuItem(menuItem,holder);
        }

        @Override
        public int getItemCount() {
            return mMenuItems.size();
        }
    }

    private class MenuItem {
        Drawable mDrawable;
        String mText;
        String mAction;

        public void setDrawable(Drawable drawable) {
            mDrawable = drawable;
        }
        public Drawable getDrawable() {
            return mDrawable;
        }
        public void setText(String text) {
            mText = text;
        }

        public String getText() {
            return mText;
        }
        public void setAction(String text) {
            mAction = text;
        }

        public String getAction() {
            return mAction;
        }

    }


}

