package com.ecube.solutions.ecube;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ecube.solutions.ecube.abstracts.AsyncTaskAbstract;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.authenticator.AuthenticatorActivity;
import com.ecube.solutions.ecube.authentication.profile.dao.Internationalization;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.dialogs.WaitDialogFragment;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.network.JsonItem;
import com.ecube.solutions.ecube.widgets.TextInputLayoutAppWidget;
import com.ecube.solutions.ecube.widgets.TextInputLayoutExtended;

/**
 * Created by sredorta on 3/1/2017.
 */
public class MainFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final boolean DEBUG = true;
    boolean kk = true;
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

    @Override
    public void onResume() {
        super.onResume();
        final EditText accountEditText = (EditText) mView.findViewById(R.id.editText);
        Account account = myAccountAuthenticator.getActiveAccount();
        if (account != null) {
            accountEditText.setText(account.name);
        } else {
            accountEditText.setText("no account");
        }
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
        Button removeButton = (Button) v.findViewById(R.id.buttonRemoveAll);
        Button restoreButton = (Button) v.findViewById(R.id.buttonRestore);
        Button waitButton = (Button) v.findViewById(R.id.buttonWait);
        //Init with account if there is one
        Account account = myAccountAuthenticator.getActiveAccount();
        if (account != null) {
            accountEditText.setText(account.name);
        } else {
            accountEditText.setText("no account");
        }
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
                if (account!= null) {
                    am.setAuthToken(account,AccountAuthenticator.AUTHTOKEN_TYPE_STANDARD, null);
                    //If there is at least one account then we should go to confirm credentials
                    getAuthToken(account,"Standard access");
                } else {
                    Toast.makeText(mActivity,"Invalid account",Toast.LENGTH_LONG).show();
                }
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If there is at least one account then we should go to confirm credentials
                AccountAuthenticator myAccountGeneral = new AccountAuthenticator(getContext());
                Account account = myAccountGeneral.getActiveAccount();
                Log.i(TAG, "hrere confirm");
                if (account!= null) {
                    confirmCredentials(account);
                } else {
                    //Pick frist account
                    account = myAccountGeneral.getAccount();
                    if (account == null) {
                        Toast.makeText(mActivity, "No accounts found", Toast.LENGTH_LONG).show();
                    } else {
                        confirmCredentials(account);
                    }
                }
            }
        });


        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = accountEditText.getText().toString();
                AccountAuthenticator myAccountGeneral = new AccountAuthenticator(getContext());

                Account account = myAccountAuthenticator.getAccount(user);
                Log.i(TAG, "hrere ");
                if (account != null) {
                    User myUser = myAccountAuthenticator.getDataFromDeviceAccount(account);
                    updateCredentials(account, myUser.getAccountAccess());
                } else {
                    Toast.makeText(mActivity,"Invalid account",Toast.LENGTH_LONG).show();
                }
            }
        });


        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccountAuthenticator myAccountGeneral = new AccountAuthenticator(getContext());
                myAccountGeneral.removeAllAccounts();
            }
        });

        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(getContext(), AuthenticatorActivity.class);
                startActivity(intent);
            }
        });


        waitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final WaitDialogFragment dialog = WaitDialogFragment.newInstance();
                AsyncTaskInterface<JsonItem> myInterface= new AsyncTaskInterface<JsonItem>() {
                    @Override
                    public void processStart() {
                        FragmentManager fm = getFragmentManager();
                        dialog.show(fm, "DIALOG");
                    }

                    @Override
                    public void processFinish(JsonItem result) {
                        dialog.dismiss();
                    }
                };
                new AsyncTaskAbstract<Void, Void, JsonItem>(myInterface,5000) {
                    @Override
                    protected JsonItem doInBackground(Void... params) {
                        super.doInBackground();

                        JsonItem res = new JsonItem();
                        return res;
                    }

                    @Override
                    protected void onPostExecute(JsonItem intent) {
                        super.onPostExecute(intent);
                    }
                }.execute();
            }
        });

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

private class waitSomeTime extends AsyncTask<Void,Void,Void> {
    WaitDialogFragment dialog;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        FragmentManager fm = getFragmentManager();
        dialog = WaitDialogFragment.newInstance();
        dialog.show(fm,"DIALOG");
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            wait(10000);
        } catch (Exception e) {
            Log.i("TEST", "Caught exception : " + e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        dialog.dismiss();
    }
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