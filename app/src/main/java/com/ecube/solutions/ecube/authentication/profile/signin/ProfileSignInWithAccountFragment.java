package com.ecube.solutions.ecube.authentication.profile.signin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.dialogs.WaitDialogFragment;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateStartFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.authentication.profile.update.ProfileUpdateResetPasswordFragment;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.helpers.IconHelper;
import com.ecube.solutions.ecube.widgets.TextInputLayoutAppWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sredorta on 3/1/2017.
 */
public class ProfileSignInWithAccountFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileSignInWithAccountFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Request to other fragments
    private static final int REQ_SIGNUP = 1;
    private static final int REQ_SIGNIN = 2;
    private static final int REQ_FORGOT_PASSWORD = 3;
    // In case of rotation
    private final String KEY_CURRENT_USER = "user.current";

    //RecycleView variables
    private AccountListAdapter mAdapter;
    private RecyclerView mAccountsRecycleView;
    private List<Account> mAccounts = new ArrayList<>();
    private AccountAuthenticator myAccountAuthenticator;
    private boolean mUpdatePostitions = true;

    private User mUser;

    // Constructor
    public static ProfileSignInWithAccountFragment newInstance() {
        return new ProfileSignInWithAccountFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // We are using async task so we need to retain it

        myAccountAuthenticator = new AccountAuthenticator(getContext());

        mUser = new User();
        //We should init the user from preferences... but now I'm just setting it

        //If there was no account from preferences we just pick one
        Account account = myAccountAuthenticator.getAccount(mUser);
        if (account == null) account = myAccountAuthenticator.getAccount();
        mUser = myAccountAuthenticator.getDataFromDeviceAccount(account);

        //mUser.setEmail("sergi.redorta@hotmail.com");
        if (savedInstanceState != null) {
            mUser.setEmail(savedInstanceState.getString(KEY_CURRENT_USER));
            account = myAccountAuthenticator.getAccount(mUser);
            if (account == null) account = myAccountAuthenticator.getAccount();
            mUser = myAccountAuthenticator.getDataFromDeviceAccount(account);
            mUpdatePostitions = false;
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_signin_with_account, container, false);
        setCurrentView(v);
        mAccountsRecycleView = (RecyclerView) v.findViewById(R.id.profile_signin_with_account_display_account_list_RecyclerView);
        mAccountsRecycleView.setLayoutManager(new LinearLayoutManager(mActivity));
        updateUI();

        //Go for SignIn without accounts
        v.findViewById(R.id.profile_signin_with_account_TextView_use_without_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileSignInWithEmailFragment fragment = ProfileSignInWithEmailFragment.newInstance();
                fragment.setTargetFragment(ProfileSignInWithAccountFragment.this, REQ_SIGNIN);
                replaceFragment(fragment);  //This comes from abstract
            }
        });
        //Go for signup
        v.findViewById(R.id.profile_signin_with_account_TextView_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileCreateStartFragment fragment = ProfileCreateStartFragment.newInstance();
                fragment.setTargetFragment(ProfileSignInWithAccountFragment.this, REQ_SIGNUP);
                replaceFragment(fragment);  //This comes from abstract
            }
        });

        //Submit
        final TextInputLayoutAppWidget passwordTextInputLayout = (TextInputLayoutAppWidget) v.findViewById(R.id.profile_signin_with_account_TextInputLayoutAppWidget_password);
        v.findViewById(R.id.profile_signin_with_account_Button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) Log.i(TAG, "Submitting credentials to account manager !");
                //hide input keyboard
                hideInputKeyBoard();
                if (passwordTextInputLayout.isValidInput()) {
                    User myUser = new User();
                    myUser.setEmail(mUser.getEmail());
                    myUser.setPassword(passwordTextInputLayout.getEditText().getText().toString());
                    Log.i(TAG, "Restoring user...");
                    AccountAuthenticator ag = new AccountAuthenticator(getContext(), myUser);
                    ag.submitCredentials(new AsyncTaskInterface<Intent>() {
                        WaitDialogFragment dialog;
                        @Override
                        public void processStart() {
                            FragmentManager fm = getFragmentManager();
                            dialog = WaitDialogFragment.newInstance();
                            dialog.show(fm,"DIALOG");
                        }
                        @Override
                        public void processFinish(Intent result) {
                            dialog.dismiss();
                            if (result.hasExtra(AccountAuthenticator.KEY_ERROR_CODE)) {
                                if (result.getStringExtra(AccountAuthenticator.KEY_ERROR_CODE).equals(AppGeneral.KEY_CODE_ERROR_INVALID_PASSWORD)) {
                                    passwordTextInputLayout.setError("Invalid password");
                                } else {
                                    passwordTextInputLayout.setError("");
                                    Toast.makeText(mActivity, result.getStringExtra(AccountAuthenticator.KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }, mActivity);
                }
            }
        });

        //Go for forgot password
        v.findViewById(R.id.profile_signin_with_account_TextView_forgot_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle data = new Bundle();
                data.putString(ProfileUpdateResetPasswordFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT, mUser.getEmail());
                ProfileUpdateResetPasswordFragment fragment = ProfileUpdateResetPasswordFragment.newInstance(data);
                fragment.setTargetFragment(ProfileSignInWithAccountFragment.this, REQ_FORGOT_PASSWORD);
                replaceFragment(fragment);  //This comes from abstract
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== REQ_FORGOT_PASSWORD) {
            replaceFragment(this,this.getTag(),true);
        }
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
        mAccounts = new ArrayList<>();
        for (Account account : myAccountAuthenticator.getAccounts()) {
            mAccounts.add(account);
        }
        //Do the swap to make sure that we start with last login as first element
        if (mUpdatePostitions) {
            Account myAccount;
            if (mUser.getEmail() != null)
                for (Account account : myAccountAuthenticator.getAccounts()) {
                    if (mUser.getEmail().equals(mAccountManager.getUserData(account, AccountAuthenticator.PARAM_USER_EMAIL))) {
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
        private ImageView buttonViewOption;

        private AccountListHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mUserFullNameTextView = (TextView) itemView.findViewById(R.id.profile_signin_with_account_display_account_TextView_user);
            mAccountNameTextView =  (TextView) itemView.findViewById(R.id.profile_signin_with_account_display_account_TextView_account);
            mAvatarImageView = (ImageView) itemView.findViewById(R.id.profile_signin_with_account_display_account_ImageView_avatar);
            buttonViewOption = (ImageView) itemView.findViewById(R.id.profile_signin_with_account_display_account_ImageView_more);

            //Set default drawable to grey
            IconHelper.colorize(getContext(), buttonViewOption, R.color.md_grey_300);
        }


        @Override
        public void onClick(View view) {
            Log.i(TAG, "Clicked !" + mAccount.name);
            //We need to update the user with the account data that has been selected
            mUser = myAccountAuthenticator.getDataFromDeviceAccount(mAccount);
            mAdapter.notifyDataSetChanged();
        }

        private void deleteItem() {
            mAccounts.remove(getAdapterPosition());
            //After removing one account we select the upper on the list
            if (mAccounts.size()>0) {

                mAccount = mAccounts.get(0);
                mUser = myAccountAuthenticator.getDataFromDeviceAccount(mAccount);
                mAdapter.notifyDataSetChanged();
            } else {
                //If there are no accounts left we start the sign-in activity
                //Change fragment as we have removed all accounts
                Log.i(TAG,"Removed latest account:");
                ProfileSignInWithEmailFragment fragment = ProfileSignInWithEmailFragment.newInstance();
                fragment.setTargetFragment(ProfileSignInWithAccountFragment.this, REQ_SIGNIN);
                replaceFragment(fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1,true);

            }
        }

        public void bindAccount(Account account, AccountListHolder holder ) {
            AccountManager mAccountManager;
            mAccountManager = AccountManager.get(mActivity.getApplicationContext());

            mAccountManager.getUserData(account, AccountAuthenticator.PARAM_USER_EMAIL);
            String fullName = mAccountManager.getUserData(account, AccountAuthenticator.PARAM_USER_FIRST_NAME);
            fullName = fullName + " " + mAccountManager.getUserData(account, AccountAuthenticator.PARAM_USER_LAST_NAME);
            mAccount = account;
            mUserFullNameTextView.setText(fullName);
            mAccountNameTextView.setText(mAccountManager.getUserData(account, AccountAuthenticator.PARAM_USER_EMAIL));
            mUser.setAvatar(mAccountManager.getUserData(account, AccountAuthenticator.PARAM_USER_AVATAR));
            mAvatarImageView.setImageBitmap(mUser.getAvatar(getContext()));

            //Define color for active or not active account (last log-in)
            if (mUser.getEmail() != null) {
                if (mUser.getEmail().equals(account.name)) {
                    itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_lime_50));
                    buttonViewOption.setEnabled(true);
                    mUserFullNameTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    mAccountNameTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    IconHelper.colorize(getContext(), buttonViewOption, R.color.md_lime_300);
                } else {
                    itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_grey_100));
                    mUserFullNameTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.md_grey_500));
                    mAccountNameTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.md_grey_500));
                    IconHelper.colorize(getContext(), buttonViewOption, R.color.md_grey_300);
                    buttonViewOption.setEnabled(false);
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
                                    User myUser = new User();
                                    myUser.setEmail(mUser.getEmail());
                                    myAccountAuthenticator.removeAccount(myUser);
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