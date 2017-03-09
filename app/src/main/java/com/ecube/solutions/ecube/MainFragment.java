package com.ecube.solutions.ecube;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.NetworkErrorException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.dao.User;

/**
 * Created by sredorta on 3/1/2017.
 */
public class MainFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //The dispatcher starts signin/signup... and all returns the user
    public static final String FRAGMENT_OUTPUT_PARAM_USER = "param.user";  //Fragment output parameter for all fragments

    private User mUser;

    //Fragment start requests
    private static final int REQ_SIGNIN = 1;
    private static final int REQ_SIGNIN_WITH_ACCOUNTS = 2;
    private static final int REQ_SIGNUP = 3;

    //Actions that all the fragments will be sending
    //TODO move this to user
    public static final String KEY_ACTION_SIGNUP = "user.signup";
    public static final String KEY_ACTION_SIGNIN_EMAIL = "user.signin.email";
    public static final String KEY_ACTION_SIGNIN_PHONE = "user.signin.phone";
    public static final String KEY_ACTION_REMOVE_FROM_DEVICE = "user.remove.from.device";

    AccountAuthenticator myAccountAuthenticator = null;

    // Constructor
    public static MainFragment newInstance() {
        return new MainFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //We check what we need to do if start SignIn/SignInWithAccounts/SignUp
        myAccountAuthenticator = new AccountAuthenticator(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        setCurrentView(v);
        //TODO we should first check if we can do fast login and if not then go for signup/signin...
        final EditText accountEditText = (EditText) v.findViewById(R.id.editText);
        Button addButton = (Button) v.findViewById(R.id.buttonAdd);
        Button confirmButton = (Button) v.findViewById(R.id.buttonConfirm);
        Button getAuthTokenButton = (Button) v.findViewById(R.id.buttonGetAuthToken);
        Button updateButton = (Button) v.findViewById(R.id.buttonUpdateCredentials);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewAccount(AccountAuthenticator.ACCOUNT_TYPE, AccountAuthenticator.AUTHTOKEN_TYPE_STANDARD);
            }
        });

        getAuthTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the account
                User myUser = new User();
                myUser.setEmail(accountEditText.getText().toString());
                AccountAuthenticator myAccountGeneral = new AccountAuthenticator(getContext());

                Account account = myAccountAuthenticator.getAccount(myUser);
                AccountManager am = myAccountAuthenticator.getAccountManager();
                am.setAuthToken(account,AccountAuthenticator.AUTHTOKEN_TYPE_STANDARD, null);
                if (account!= null) {
                    //If there is at least one account then we should go to confirm credentials
                    getAuthToken(account,"Standard access");
                } else {
                    Toast.makeText(getContext(),"Invalid account",Toast.LENGTH_LONG).show();
                }
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If there is at least one account then we should go to confirm credentials
                AccountAuthenticator myAccountGeneral = new AccountAuthenticator(getContext());
                confirmCredentials(myAccountGeneral.getAccount());
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = accountEditText.getText().toString();
                AccountAuthenticator myAccountGeneral = new AccountAuthenticator(getContext());

                Account account = myAccountAuthenticator.getAccount(user);
                User myUser = myAccountAuthenticator.getDataFromDeviceAccount(account);
                if (account != null) {
                    updateCredentials(account, myUser.getAccountAccess());
                } else {
                    Toast.makeText(getContext(),"Invalid account",Toast.LENGTH_LONG).show();
                }
            }
        });



/*
        Log.i(TAG, "Found accounts :" + myAccountGeneral.getAccountsCount());
        if (myAccountGeneral.getAccountsCount() >0) {
            ProfileSignInWithAccountFragment fragment = ProfileSignInWithAccountFragment.newInstance();
            fragment.setTargetFragment(MainFragment.this, REQ_SIGNIN_WITH_ACCOUNTS);
            replaceFragment(fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1,true);  //This comes from abstract
        } else {
            ProfileSignInWithEmailFragment fragment = ProfileSignInWithEmailFragment.newInstance();
            fragment.setTargetFragment(MainFragment.this, REQ_SIGNIN);
            replaceFragment(fragment,AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1,true);  //This comes from abstract
        }
        */

        //


        return v;
    }

    //Calling our authenticator example  to create a new account!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private void addNewAccount(String accountType, String authTokenType) {
        AccountAuthenticator mAccountAuthenticator = new AccountAuthenticator(mActivity.getApplicationContext());
        AccountManager mAccountManager = mAccountAuthenticator.getAccountManager();
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(accountType, authTokenType, null, null, mActivity, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    Log.i(TAG, "AddNewAccount Bundle is " + bnd);
                    Log.i(TAG, "account name :" + bnd.getString(AccountManager.KEY_ACCOUNT_NAME));
                    Log.i(TAG, "account type :" + bnd.getString(AccountManager.KEY_ACCOUNT_TYPE));
                    Log.i(TAG, "token :" + bnd.getString(AccountManager.KEY_AUTHTOKEN));
                    Log.i(TAG, "message :" + bnd.getString(AccountManager.KEY_ERROR_MESSAGE));

                } catch (Exception e) {
                    Log.i(TAG, "Caught exception : " + e);
                }
            }
        }, null);
    }
    //Calling our authenticator example  to create a new account!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private void getAuthToken(Account account, String authTokenType) {
        AccountAuthenticator mAccountAuthenticator = new AccountAuthenticator(mActivity.getApplicationContext());
        AccountManager mAccountManager = mAccountAuthenticator.getAccountManager();
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account,authTokenType,null,mActivity,new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    Log.i(TAG, "GetAuthToken Bundle is " + bnd);
                    Log.i(TAG, "account name :" + bnd.getString(AccountManager.KEY_ACCOUNT_NAME));
                    Log.i(TAG, "account type :" + bnd.getString(AccountManager.KEY_ACCOUNT_TYPE));
                    Log.i(TAG, "token :" + bnd.getString(AccountManager.KEY_AUTHTOKEN));
                    Log.i(TAG, "message :" + bnd.getString(AccountManager.KEY_ERROR_MESSAGE));

                } catch (Exception e) {
                    Log.i(TAG, "Caught exception : " + e);
                }
            }
        }, null);
    }


    //Calling our authenticator example  to create a new account!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private void confirmCredentials(Account account) {
        AccountManager mAccountManager = AccountManager.get(mActivity.getApplicationContext());
        final AccountManagerFuture<Bundle> future = mAccountManager.confirmCredentials(account,null, mActivity, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    Log.i(TAG, "ConfirmCredentials Bundle is " + bnd);
                    Log.i(TAG, "account name :" + bnd.getString(AccountManager.KEY_ACCOUNT_NAME));
                    Log.i(TAG, "account type :" + bnd.getString(AccountManager.KEY_ACCOUNT_TYPE));
                    Log.i(TAG, "token :" + bnd.getString(AccountManager.KEY_AUTHTOKEN));

                } catch (Exception e) {
                    Log.i(TAG, "Caught exception : " + e);
                }
            }
        }, null);
    }

    //Calling our authenticator example  to create a new account!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private void updateCredentials(Account account, String authTokenType) {
        AccountManager mAccountManager = AccountManager.get(mActivity.getApplicationContext());

        final AccountManagerFuture<Bundle> future = mAccountManager.updateCredentials(account,authTokenType, null, mActivity, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    Log.i(TAG, "UpdateCredentials Bundle is " + bnd);
                    Log.i(TAG, "account name :" + bnd.getString(AccountManager.KEY_ACCOUNT_NAME));
                    Log.i(TAG, "account type :" + bnd.getString(AccountManager.KEY_ACCOUNT_TYPE));
                    Log.i(TAG, "token :" + bnd.getString(AccountManager.KEY_AUTHTOKEN));

                } catch (Exception e) {
                    Log.i(TAG, "Caught exception : " + e);
                }
            }
        }, null);
    }



/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //We do restore the user from the different fragments response
            if (data.hasExtra(FRAGMENT_OUTPUT_PARAM_USER)) {
                mUser = (User) data.getParcelableExtra(FRAGMENT_OUTPUT_PARAM_USER);
                handleAccountRequest();
            }
        } else {
            Log.i(TAG, "OnBackPressed at Dispatcher !");
        }

    }

    //////////////////////////////////////////////////////////////////////////////////
    //Does any action required on the account : signin with email, signin with phone, signup
    //////////////////////////////////////////////////////////////////////////////////
    private void handleAccountRequest() {
        Log.i(TAG, "Action required : " + mUser.getAction());
        mUser.print("Dispatcher:");
        if (mUser.getAction().equals(KEY_ACTION_REMOVE_FROM_DEVICE)) {
            myAccountGeneral.removeAccount(mUser);

        } else if (mUser.getAction().equals(KEY_ACTION_SIGNUP)) {
            Log.i(TAG, "Creating user :" + mUser.getEmail());
            AccountGeneral ag = new AccountGeneral(getContext(), mUser);
            ag.createAccount();
        }
    }
*/

}