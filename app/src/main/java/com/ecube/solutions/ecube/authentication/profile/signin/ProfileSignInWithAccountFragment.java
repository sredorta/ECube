package com.ecube.solutions.ecube.authentication.profile.signin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountGeneral;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateStartFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sredorta on 3/1/2017.
 */
public class ProfileSignInWithAccountFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileSignInWithAccountFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    private int REQ_SIGNUP = 1;

    // In case of rotation
    private final String KEY_CURRENT_USER = "user.current";

    //RecycleView variables
    private AccountListAdapter mAdapter;
    private RecyclerView mAccountsRecycleView;
    private List<Account> mAccounts = new ArrayList<>();
    private AccountGeneral myAccountGeneral;
    private boolean mUpdatePostitions = true;

    private User mUser;

    // Constructor
    public static ProfileSignInWithAccountFragment newInstance() {
        return new ProfileSignInWithAccountFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myAccountGeneral = new AccountGeneral(getContext());

        mUser = new User();
        if (savedInstanceState != null) {
            mUser.setEmail(savedInstanceState.getString(KEY_CURRENT_USER));
            mUser = myAccountGeneral.getDataFromDeviceAccount(myAccountGeneral.getAccount(mUser));
            mUpdatePostitions = false;
        }
        //We should init the user from preferences... but now I'm just setting it
        //mUser.setEmail("sergi.redorta@hotmail.com");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_signin_with_account, container, false);
        setCurrentView(v);
        mAccountsRecycleView = (RecyclerView) v.findViewById(R.id.profile_signin_with_account_display_account_list_RecyclerView);
        mAccountsRecycleView.setLayoutManager(new LinearLayoutManager(mActivity));
        updateUI();

        //Go for signup
        v.findViewById(R.id.profile_signin_with_account_TextView_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileCreateStartFragment fragment = ProfileCreateStartFragment.newInstance();
                fragment.setTargetFragment(ProfileSignInWithAccountFragment.this, REQ_SIGNUP);
                replaceFragment(fragment,"test",true);  //This comes from abstract
            }
        });

        //Submit
        final EditText passwordEditText = (EditText) v.findViewById(R.id.profile_signin_with_account_editText_password);
        v.findViewById(R.id.profile_signin_with_account_Button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) Log.i(TAG, "Submitting credentials to account manager !");
                //hide input keyboard
                hideInputKeyBoard();
                if (User.checkPasswordInput(passwordEditText,mView,mActivity)) {
                        if (DEBUG) Log.i(TAG, "We are now checking with server !");
                        //TODO do the actual login with the server
                }
            }
        });


        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_USER, mUser.getEmail());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //RecycleView part
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public AccountListAdapter getAdapter() {return mAdapter;}

    //Updates the recycleview
    private void updateUI() {
        AccountManager mAccountManager;
        mAccountManager = AccountManager.get(mActivity.getApplicationContext());

        for (Account account : myAccountGeneral.getAccounts()) {
            mAccounts.add(account);
        }
        //Do the swap to make sure that we start with last login as first element
        if (mUpdatePostitions) {
            Account myAccount;
            if (mUser.getEmail() != null)
                for (Account account : myAccountGeneral.getAccounts()) {
                    if (mUser.getEmail().equals(mAccountManager.getUserData(account, AccountGeneral.PARAM_USER_EMAIL))) {
                        int index = mAccounts.indexOf(account);
                        if (index != 0) {
                            myAccount = mAccounts.get(0);
                            mAccounts.set(0, account);
                            mAccounts.set(index, myAccount);
                        }
                        break;
                    }
                }
        }
        mAdapter = new AccountListAdapter(mAccounts);
        mAccountsRecycleView.setAdapter(mAdapter);

    }



    private class AccountListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Account mAccount;
        private TextView mUserFullNameTextView;
        private TextView mAccountNameTextView;
        private ImageView mAvatarImageView;
        public ImageView buttonViewOption;
        public ImageView buttonActiveView;

        private AccountListHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mUserFullNameTextView = (TextView) itemView.findViewById(R.id.profile_signin_with_account_display_account_TextView_user);
            mAccountNameTextView =  (TextView) itemView.findViewById(R.id.profile_signin_with_account_display_account_TextView_account);
            mAvatarImageView = (ImageView) itemView.findViewById(R.id.profile_signin_with_account_display_account_ImageView_avatar);
            buttonViewOption = (ImageView) itemView.findViewById(R.id.profile_signin_with_account_display_account_ImageView_more);

            PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.md_green_500),PorterDuff.Mode.SRC_ATOP);
            buttonViewOption.getDrawable().setColorFilter(porterDuffColorFilter);
            buttonViewOption.setBackgroundColor(Color.TRANSPARENT);
        }


        @Override
        public void onClick(View view) {
            //We need to update the user with the account data that has been selected
            mUser = myAccountGeneral.getDataFromDeviceAccount(mAccount);
            mAdapter.notifyDataSetChanged();
            //Send result to master fragment
//            putOutputParam(FRAGMENT_OUTPUT_PARAM_SELECTED_USER, user.getName());
//            sendResult(Activity.RESULT_OK);

            //buttonViewOption.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.md_red_500), PorterDuff.Mode.LIGHTEN);
           /* Drawable bgDrawable = buttonViewOption.getDrawable();
            bgDrawable.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.md_red_500), PorterDuff.Mode.OVERLAY);
            buttonViewOption.setImageDrawable(bgDrawable);*/
        }

        private void deleteItem() {
            mAccounts.remove(getAdapterPosition());
            //After removing one account we select the upper on the list
            if (mAccounts.size()>0) {

                mAccount = mAccounts.get(0);
                mUser = myAccountGeneral.getDataFromDeviceAccount(mAccount);

//                putOutputParam(FRAGMENT_OUTPUT_PARAM_SELECTED_USER,user.getName());
//                sendResult(Activity.RESULT_OK);
                mAdapter.notifyDataSetChanged();
            } else {
                //If there are no accounts left we start the sign-in activity
                //Change fragment as we have removed all accounts
                Log.i(TAG,"Removed latest account:");
                ProfileSignInWithEmailFragment fragment = ProfileSignInWithEmailFragment.newInstance();
                replaceFragment(fragment,"test",true);  //This comes from abstract
            }
        }

        public void bindAccount(Account account, AccountListHolder holder ) {
            AccountManager mAccountManager;
            mAccountManager = AccountManager.get(mActivity.getApplicationContext());

            mAccountManager.getUserData(account, AccountGeneral.PARAM_USER_EMAIL);
            String fullName = mAccountManager.getUserData(account, AccountGeneral.PARAM_USER_FIRST_NAME);
            fullName = fullName + " " + mAccountManager.getUserData(account, AccountGeneral.PARAM_USER_LAST_NAME);
            mAccount = account;
            mUserFullNameTextView.setText(fullName);
            mAccountNameTextView.setText(mAccountManager.getUserData(account, AccountGeneral.PARAM_USER_EMAIL));
            mUser.setAvatarString(mAccountManager.getUserData(account,AccountGeneral.PARAM_USER_AVATAR), getContext());
            mAvatarImageView.setImageBitmap(mUser.getAvatarBitmap());
            //Define color for active or not active account (last log-in)
            if (mUser.getEmail()!= null) {
                if (mUser.getEmail().equals(mAccountManager.getUserData(account, AccountGeneral.PARAM_USER_EMAIL))) {
                    itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_lime_50));
                     buttonViewOption.setEnabled(true);
                } else {
                    buttonViewOption.setEnabled(false);
                    itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_white_1000));
                }
            }

            //Handle here the options menu of each account
            buttonViewOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(getContext(), buttonViewOption);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.options_menu_account_item);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.options_menu_account_item_edit:
/////                                    Intent i = new Intent(mActivity.getBaseContext(), QueryPreferences.class);
/////                                    startActivity(i);
                                    //handle menu1 click
                                    break;
                                case R.id.options_menu_account_item_remove:
                                    if (myAccountGeneral.removeAccount(mUser))
                                        deleteItem();
                                    break;
                            }
                            return false;
                        }
                    });
                    //displaying the popup
                    popup.show();
                }
            });
        }
    }

    private class AccountListAdapter extends RecyclerView.Adapter<AccountListHolder> {

        public AccountListAdapter(List<Account> accounts) {
            mAccounts = accounts;
        }

        @Override
        public AccountListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
            View view = layoutInflater.inflate(R.layout.profile_signin_with_account_display_account, parent,false);
            return new AccountListHolder(view);
        }

        @Override
        public void onBindViewHolder(AccountListHolder holder, int position) {
            Account account = mAccounts.get(position);
            holder.bindAccount(account,holder);
        }

        @Override
        public int getItemCount() {
            return mAccounts.size();
        }

    }


}