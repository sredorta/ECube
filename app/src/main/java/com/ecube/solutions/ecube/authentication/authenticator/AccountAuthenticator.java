package com.ecube.solutions.ecube.authentication.authenticator;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.NetworkErrorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.ecube.solutions.ecube.abstracts.AsyncTaskAbstract;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.authentication.profile.dao.Internationalization;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.helpers.IntentHelper;
import com.ecube.solutions.ecube.network.Encryption;
import com.ecube.solutions.ecube.network.JsonItem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

/**
 * Created by sredorta on 3/1/2017.
 */
public class AccountAuthenticator extends AbstractAccountAuthenticator {
    //Logs
    private static final String TAG = AccountAuthenticator.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static final String ACCOUNT_TYPE = "com.ecube.solutions.ecube.auth_ecube";  //Defines our account
    //
    //Access types
    public static final String AUTHTOKEN_TYPE_STANDARD = "Standard access";
    public static final String AUTHTOKEN_TYPE_STANDARD_LABEL = "Standard access to eCube account";

    public static final String AUTHTOKEN_TYPE_FULL = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_LABEL = "Full access to eCube account";

    //Parameters for Intent
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_ACCOUNT_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";                             //Argument to go to signup
    public final static String ARG_IS_UPDATING_CREDENTIALS_ACCOUNT = "IS_UPDATING_CREDENTIALS_ACCOUNT";     //Argument to go to edit profile
    public final static String ARG_IS_CONFIRM_CREDENTIALS_ACCOUNT = "IS_CONFIRM_CREDENTIALS_ACCOUNT";       //Argument to go to confirm credentials

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";
    public static final String KEY_ERROR_CODE = "ERR_CODE";

    //Parameters that we save in the account
    public final static String PARAM_USER_ACCOUNT_TYPE = "USER_ACCOUNT_TYPE";
    public final static String PARAM_USER_ACCOUNT_AUTH_TYPE = "USER_ACCOUNT_AUTH_TYPE";
    public final static String PARAM_USER_ACCOUNT_ID = "USER_ACCOUNT_ID";
    public final static String PARAM_USER_FIRST_NAME = "USER_FIRST_NAME";
    public final static String PARAM_USER_LAST_NAME = "USER_LAST_NAME";
    public final static String PARAM_USER_EMAIL = "USER_EMAIL";
    public final static String PARAM_USER_PHONE = "USER_PHONE";
    public final static String PARAM_USER_AVATAR = "USER_AVATAR";
    public final static String PARAM_USER_CREATION_TIME = "USER_CREATION_TIMESTAMP";
    public final static String PARAM_USER_ACCOUNT_ACTIVE = "ACTIVE_ACCOUNT";    //Stores latest used account
    public final static String ACCOUNT_INACTIVE = "account.inactive";           //Defines account as inactive
    public final static String ACCOUNT_ACTIVE = "account.active";               //Defines account as active (latest logged in)

    //Get access to the server part functions
    public static final ServerAuthenticate sServerAuthenticate = new ServerAuthenticateClass();


    private Context mContext;
    private User mUser;
    private AccountManager mAccountManager;


    public AccountAuthenticator(Context context) {
        super(context);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    public AccountAuthenticator(Context context, User user) {
        super(context);
        mContext = context;
        mAccountManager = AccountManager.get(context);
        mUser = user;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // AbstractAuthenticator part
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Create a new Account request... mainly we say that when we want to create an account to the service we go to addAccount
    //   then we set all the parameters in the bundle and start AuthenticatorActivity.class
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.i(TAG, "addAccount");
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        Log.i(TAG,"Details of the intent sent to AuthenticatorActivity : ");
        IntentHelper.dumpIntent(intent);
        return bundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        //Android gets a token from the account and if there is no token then we get here !
        Log.i(TAG, "getAuthToken");

        //Init the user from the account data
        mUser = this.getDataFromDeviceAccount(account);
        if (!authTokenType.equals(AUTHTOKEN_TYPE_STANDARD) && !authTokenType.equals(AUTHTOKEN_TYPE_FULL)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(ARG_IS_CONFIRM_CREDENTIALS_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(ARG_ACCOUNT_AUTH_TYPE, authTokenType);
        intent.putExtra(ARG_ACCOUNT_NAME, account.name);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }


    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) {
        Log.i(TAG, "getAccountRemovalAllowed");
        Bundle result = new Bundle();
        boolean allowed = true; // or whatever logic you want here
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, allowed);
        return result;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Log.i(TAG, "getAuthTokenLabel");
        if (AUTHTOKEN_TYPE_FULL.equals(authTokenType))
            return AUTHTOKEN_TYPE_FULL_LABEL;
        else if (AUTHTOKEN_TYPE_STANDARD.equals(authTokenType))
            return AUTHTOKEN_TYPE_STANDARD_LABEL;
        else
            return authTokenType + " (Label)";
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        Log.i(TAG, "hasFeatures");
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        Log.i(TAG, "editProperties");
        return null;
    }

    //Ask to confirm credentials and not create a new account
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        Log.i(TAG, "confirmCredentials");
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(ARG_IS_CONFIRM_CREDENTIALS_ACCOUNT, true);

        User tmp = getDataFromDeviceAccount(account);
        intent.putExtra(ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(ARG_ACCOUNT_AUTH_TYPE, tmp.getAccountAccess());
        intent.putExtra(ARG_ACCOUNT_NAME, account.name);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        Log.i(TAG,"Details of the intent sent to AuthenticatorActivity : ");
        IntentHelper.dumpIntent(intent);
        return bundle;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.i(TAG, "updateCredentials");
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(ARG_IS_UPDATING_CREDENTIALS_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(ARG_ACCOUNT_AUTH_TYPE, authTokenType);
        intent.putExtra(ARG_ACCOUNT_NAME, account.name);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);

        Log.i(TAG,"Details of the intent sent to AuthenticatorActivity : ");
        IntentHelper.dumpIntent(intent);
        return bundle;
    }

    //In this case we have email + password... and we restore the account in the user with addAccountFromCredentials
    @Override
    public Bundle getAccountCredentialsForCloning(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
        return super.getAccountCredentialsForCloning(response, account);
    }

    //Creates an account from email + password or phone + password
    @Override
    public Bundle addAccountFromCredentials(AccountAuthenticatorResponse response, Account account, Bundle accountCredentials) throws NetworkErrorException {
        return super.addAccountFromCredentials(response, account, accountCredentials);
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Extension to help
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //Returns the Account manager
    public AccountManager getAccountManager() {
        return mAccountManager;
    }


    // Gets the "current account" on the device matching our accountType and accountName
    public Account getAccount(User user) {
        if (user.getEmail() == null)
            return null;
        //Find if there is an account with the correct accountName and get its token
        for (Account account : mAccountManager.getAccountsByType(ACCOUNT_TYPE)) {
            if (account.name.equals(user.getEmail())) {
                return account;
            }
        }
        return null;
    }

    // Gets the "current account" on the device matching our accountType and accountName with a String
    public Account getAccount(String accountName) {
        if (accountName == null)
            return null;
        //Find if there is an account with the correct accountName and get its token
        for (Account account : mAccountManager.getAccountsByType(ACCOUNT_TYPE)) {
            if (account.name.equals(accountName)) {
                return account;
            }
        }
        return null;
    }

    //Gets the first account found wmattching accountType
    public Account getAccount() {
        //Find if there is an account with the correct accountName and get its token
        for (Account account : mAccountManager.getAccountsByType(ACCOUNT_TYPE)) {
            return account;
        }
        return null;
    }

    //Only for debug
    public void removeAllAccounts() {
        for (Account account : mAccountManager.getAccountsByType(ACCOUNT_TYPE)) {
            Log.i(TAG, "Removing account:" +account.name);
            User myUser = new User();
            myUser.setEmail(account.name);
            removeAccount(myUser);
        }
    }


    //Returns all accounts of our type
    public Account[] getAccounts() {
        //Find if there is an account with the correct accountName and get its token
        return mAccountManager.getAccountsByType(ACCOUNT_TYPE);
    }

    //Returns count of accounts of our type
    public Integer getAccountsCount() {
        return getAccounts().length;
    }



    // Creates an account on the Device
    public Boolean createAccount() {
        Log.i(TAG, "createAccount");
        Account account;
        mUser.print("Creating account with following data:");
        if (getAccount(mUser) == null) {
            account = new Account(mUser.getEmail(), ACCOUNT_TYPE);
            mAccountManager.addAccountExplicitly(account, mUser.getPassword(), null);
            if (DEBUG) Log.i(TAG, "Created account...");
        } else {
            if (DEBUG) Log.i(TAG, "Account existing, skip creation....");
            account = getAccount(mUser);
        }
        setUserDataToDeviceAccount();
        if (account == null)
            return false;
        else
            return true;
    }

    public void setUserDataToDeviceAccount() {
        Account account = getAccount(mUser);
        if (account != null) {
            //Just in case for now
            mAccountManager.setUserData(account, PARAM_USER_ACCOUNT_TYPE, ACCOUNT_TYPE);

            if (mUser.getPassword() != null)
                mAccountManager.setPassword(account, Encryption.getSHA1(mUser.getPassword()));
            if (mUser.getAccountAccess() != null)
                mAccountManager.setUserData(account, PARAM_USER_ACCOUNT_AUTH_TYPE, mUser.getAccountAccess());
            if (mUser.getToken() != null)
                mAccountManager.setAuthToken(account, mUser.getAccountAccess(), mUser.getToken());
            if (mUser.getId() != null)
                mAccountManager.setUserData(account, PARAM_USER_ACCOUNT_ID, mUser.getId());
            if (mUser.getEmail() != null)
                mAccountManager.setUserData(account, PARAM_USER_EMAIL, mUser.getEmail());
            if (mUser.getPhone() != null)
                mAccountManager.setUserData(account, PARAM_USER_PHONE, mUser.getPhone());
            if (mUser.getFirstName() != null)
                mAccountManager.setUserData(account, PARAM_USER_FIRST_NAME, mUser.getFirstName());
            if (mUser.getLastName() != null)
                mAccountManager.setUserData(account, PARAM_USER_LAST_NAME, mUser.getLastName());
            if (mUser.getAvatar() != null) {
                mAccountManager.setUserData(account, PARAM_USER_AVATAR, mUser.getAvatar());
            }
            if (mUser.getCreationTime() != null) {
                mAccountManager.setUserData(account, PARAM_USER_CREATION_TIME, String.valueOf(mUser.getCreationTime()));
            }
        }
    }

    //Queries all the data from the device account and sets the singleton
    public User getDataFromDeviceAccount(Account myAccount) {
        User mUser = new User();
        if (DEBUG) Log.i(TAG, "Populating AccountDetails from device account !");
        mUser.setId(mAccountManager.getUserData(myAccount, PARAM_USER_ACCOUNT_ID));
        mUser.setFirstName(mAccountManager.getUserData(myAccount, PARAM_USER_FIRST_NAME));
        mUser.setLastName(mAccountManager.getUserData(myAccount, PARAM_USER_LAST_NAME));
        mUser.setEmail(mAccountManager.getUserData(myAccount, PARAM_USER_EMAIL));
        mUser.setPhone(mAccountManager.getUserData(myAccount, PARAM_USER_PHONE));
        mUser.setAvatar(mAccountManager.getUserData(myAccount, PARAM_USER_AVATAR));
        mUser.setCreationTime(Integer.valueOf(mAccountManager.getUserData(myAccount,PARAM_USER_CREATION_TIME)));
        //System parameters
        mUser.setPassword(mAccountManager.getPassword(myAccount));
        mUser.setAccountAccess(mAccountManager.getUserData(myAccount, PARAM_USER_ACCOUNT_AUTH_TYPE));
        if (mUser.getAccountAccess() != null){
            mUser.setToken(mAccountManager.peekAuthToken(myAccount,mUser.getAccountAccess()));
        }
        return mUser;
    }

    //Sets account as active
    public void setActiveAccount(Account account) {
        if (account!= null) {
            for (Account myAccount : mAccountManager.getAccountsByType(ACCOUNT_TYPE)) {
                mAccountManager.setUserData(myAccount, PARAM_USER_ACCOUNT_ACTIVE, ACCOUNT_INACTIVE);
            }
            mAccountManager.setUserData(account, PARAM_USER_ACCOUNT_ACTIVE, ACCOUNT_ACTIVE);
        }
    }
    public void setActiveAccount(String accountName) {
        setActiveAccount(getAccount(accountName));
    }
    //Gets active account
    public Account getActiveAccount() {
        for (Account myAccount : mAccountManager.getAccountsByType(ACCOUNT_TYPE)) {
            if (mAccountManager.getUserData(myAccount, PARAM_USER_ACCOUNT_ACTIVE)!= null)
              if (mAccountManager.getUserData(myAccount, PARAM_USER_ACCOUNT_ACTIVE).equals(ACCOUNT_ACTIVE)) {
                return myAccount;
              }
        }
        return null;
    }



    //Remove account on the Device
    public Boolean removeAccount(User user) {
        if (DEBUG) Log.i(TAG, "Removing account name: " + user.getEmail());

        if (getAccount(user) == null)
            return false;
        Boolean isDone = false;
        if (Build.VERSION.SDK_INT<22) {
            @SuppressWarnings("deprecation")
            final AccountManagerFuture<Boolean> booleanAccountManagerFuture = mAccountManager.removeAccount(getAccount(user), null, null);
            try {
                isDone = booleanAccountManagerFuture.getResult(1, TimeUnit.SECONDS);
            } catch (OperationCanceledException e) {
                Log.i(TAG,"Caught exception : " + e);
            } catch (IOException e) {
                Log.i(TAG,"Caught exception : " + e);
            } catch (AuthenticatorException e) {
                Log.i(TAG,"Caught exception : " + e);
            }
        } else
            isDone = mAccountManager.removeAccountExplicitly(getAccount(user));
        if (isDone) {
            if (DEBUG) Log.i(TAG, "Successfully removed account ! ");
        }
        return isDone;
    }


    //Creates the Server and Device account and exits activity if successfull
    public void createServerAndDeviceAccount(AsyncTaskInterface<Intent> myAsyncTaskInterface,final Activity activity) {
        new AsyncTaskAbstract<Void, Void, Intent>(myAsyncTaskInterface,700) {
            @Override
            protected Intent doInBackground(Void... params) {
                super.doInBackground();
                mUser.setLanguage(Internationalization.getLanguage(mContext));
                Log.i(TAG,"Set language to :" + mUser.getLanguage());
                Bundle data = new Bundle();
                JsonItem item = sServerAuthenticate.userSignUp(mUser);
                if(!item.getResult()) {
                    data.putString(KEY_ERROR_MESSAGE, item.getMessage());
                    data.putString(KEY_ERROR_CODE, item.getKeyError());
                } else {
                    Log.i(TAG,"Creating now the account on the device !");
                    if (!createAccount()) {
                        //We could not create the device account so removing the server account
                        sServerAuthenticate.userRemove(mUser);
                        Log.i(TAG,"Removing server account as we could not create device account !");
                        data.putString(KEY_ERROR_MESSAGE, "Could not create device account !");
                        data.putString(KEY_ERROR_CODE, AppGeneral.KEY_CODE_ERROR_UNKNOWN);
                    }
                }

                //Settings for the Account AuthenticatorActivity
                data.putString(AccountManager.KEY_ACCOUNT_NAME, mUser.getEmail());
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
                data.putString(AccountManager.KEY_AUTHTOKEN, mUser.getToken());

                final Intent res = new Intent();
                res.putExtras(data);
                return res;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                super.onPostExecute(intent);
                if (!intent.hasExtra(KEY_ERROR_MESSAGE)) {
                    activity.getIntent().putExtras(intent.getExtras());
                    activity.setResult(Activity.RESULT_OK, intent);
                    activity.finish();
                }
            }
        }.execute();
    }


    //Submits credentials to the server and exits activity if successfull
    public void submitCredentials(AsyncTaskInterface<Intent> myAsyncTaskInterface, final Activity activity) {
        new AsyncTaskAbstract<Void, Void, Intent>(myAsyncTaskInterface,700) {
            JsonItem item;
            @Override
            protected Intent doInBackground(Void... params) {
                super.doInBackground();
                mUser.setLanguage(Internationalization.getLanguage(mContext));
                Log.i(TAG,"Set language to :" + mUser.getLanguage());
                Log.i(TAG, "Started authenticating");
                Bundle data = new Bundle();
                //If we have an account we try to create a new session and get the new token sending ID + password
                //The server function directly updates the singleton token,id fields
                item = sServerAuthenticate.userSignIn(mUser);
                if(!item.getResult()) {
                    data.putString(KEY_ERROR_MESSAGE, item.getMessage());
                    data.putString(KEY_ERROR_CODE, item.getKeyError());
                } else {
                    if (getAccount(mUser)==null) {
                        createAccount();
                    }
                    setUserDataToDeviceAccount();
                }

                //Settings for the Account AuthenticatorActivity
                data.putString(AccountManager.KEY_ACCOUNT_NAME, mUser.getEmail());
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
                data.putString(AccountManager.KEY_AUTHTOKEN, mUser.getToken());

                final Intent res = new Intent();
                res.putExtras(data);
                return res;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                super.onPostExecute(intent);
                if (!intent.hasExtra(KEY_ERROR_MESSAGE)) {
                    activity.getIntent().putExtras(intent.getExtras());
                    //Finish and send to AuthenticatorActivity that we where successfull
                    activity.setResult(Activity.RESULT_OK, intent);
                    activity.finish();
                }
            }
        }.execute();
    }


    //Submits credentials to the server and exits activity if successfull
    public void checkPassword(AsyncTaskInterface<JsonItem> myAsyncTaskInterface, final Activity activity) {
        new AsyncTaskAbstract<Void, Void, JsonItem>(myAsyncTaskInterface,700) {
            JsonItem item;
            @Override
            protected JsonItem doInBackground(Void... params) {
                super.doInBackground();
                Log.i(TAG, "Started checking password");

                item = sServerAuthenticate.userCheckPassword(mUser);
                //Check that account and user password matches
                String oldPassword = Encryption.getSHA1(mUser.getPassword());
                if (!oldPassword.equals(mAccountManager.getPassword(getAccount(mUser)))) {
                    Log.i(TAG, "Passwords between entered and local account dont match !");
                    item.setKeyError(AppGeneral.KEY_CODE_ERROR_INVALID_PASSWORD);
                }

                Log.i(TAG, "Passwords between entered and local account match !");
                //Check that server has same password
                return item;
            }

            @Override
            protected void onPostExecute(JsonItem result) {
                super.onPostExecute(result);

            }
        }.execute();
    }

    //Change password
    public void changePassword(final String newPassword, AsyncTaskInterface<JsonItem> myAsyncTaskInterface, final Activity activity) {
        new AsyncTaskAbstract<Void, Void, JsonItem>(myAsyncTaskInterface,700) {
            JsonItem item;
            @Override
            protected JsonItem doInBackground(Void... params) {
                super.doInBackground();
                Log.i(TAG, "Started checking password");

                item = sServerAuthenticate.userChangePassword(mUser,newPassword);
                //Check that account and user password matches
                if (item.getResult()) {
                    //We need to now set the password in the account locally
                    mAccountManager.setPassword(getAccount(mUser), Encryption.getSHA1(newPassword));
                    Log.i(TAG, "Password set also in local account ! to : " + Encryption.getSHA1(newPassword));
                    //item.setKeyError(AppGeneral.KEY_CODE_ERROR_INVALID_PASSWORD);
                }
                //Check that server has same password
                return item;
            }

            @Override
            protected void onPostExecute(JsonItem result) {
                super.onPostExecute(result);

            }
        }.execute();
    }

    //Change password
    public void changeEmail(final String newEmail, AsyncTaskInterface<JsonItem> myAsyncTaskInterface, final Activity activity) {
        new AsyncTaskAbstract<Void, Void, JsonItem>(myAsyncTaskInterface,700) {
            JsonItem item;
            @Override
            protected JsonItem doInBackground(Void... params) {
                super.doInBackground();
                Log.i(TAG, "Started checking password");

                item = sServerAuthenticate.userChangeEmail(mUser,newEmail);
                getDataFromDeviceAccount(getAccount(mUser));
                //Check that account and user password matches
                if (item.getResult()) {
                    //We need to now set the password in the account locally
                    final AccountManagerFuture<Account> future = mAccountManager.renameAccount(getAccount(mUser),newEmail, new AccountManagerCallback<Account>() {
                        @Override
                        public void run(AccountManagerFuture<Account> future) {
                            try {
                                Account newAccount = future.getResult();
                                //We need to store all data we had before
                                mUser.setEmail(newEmail);
                                setUserDataToDeviceAccount();
                            } catch (Exception e) {
                                Log.i(TAG, "Caught exception : " + e);
                            }
                        }
                    }, null);
                    //item.setKeyError(AppGeneral.KEY_CODE_ERROR_INVALID_PASSWORD);
                }
                //Check that server has same password
                return item;
            }

            @Override
            protected void onPostExecute(JsonItem result) {
                super.onPostExecute(result);

            }
        }.execute();
    }
    //Change password
    public void changePhone(final String newPhone, AsyncTaskInterface<JsonItem> myAsyncTaskInterface, final Activity activity) {
        new AsyncTaskAbstract<Void, Void, JsonItem>(myAsyncTaskInterface,700) {
            JsonItem item;
            @Override
            protected JsonItem doInBackground(Void... params) {
                super.doInBackground();
                Log.i(TAG, "Started checking password");

                item = sServerAuthenticate.userChangePhone(mUser,newPhone);

                //Check that account and user password matches
                if (item.getResult()) {
                    mUser.setPhone(newPhone);
                    mAccountManager.setUserData(getAccount(mUser), PARAM_USER_PHONE, mUser.getPhone());
                }
                //Check that server has same password
                return item;
            }

            @Override
            protected void onPostExecute(JsonItem result) {
                super.onPostExecute(result);

            }
        }.execute();
    }
    //Reset password and send email with new password
    public void resetPassword(AsyncTaskInterface<JsonItem> myAsyncTaskInterface, final Activity activity) {
        new AsyncTaskAbstract<Void, Void, JsonItem>(myAsyncTaskInterface,700) {
            JsonItem item;
            @Override
            protected JsonItem doInBackground(Void... params) {
                super.doInBackground();
                Log.i(TAG, "Started checking password");

                item = sServerAuthenticate.userResetPassword(mUser);
                //Check that account and user password matches
                if (item.getResult()) {
                    //We need to now set the password in the account locally
                    mAccountManager.setPassword(getAccount(mUser), mUser.getPassword());
                    mAccountManager.setAuthToken(getAccount(mUser),AccountAuthenticator.AUTHTOKEN_TYPE_STANDARD, null); //Reset token
                    Log.i(TAG, "Password set also in local account ! to : " + mUser.getPassword());
                    //item.setKeyError(AppGeneral.KEY_CODE_ERROR_INVALID_PASSWORD);
                }
                //Check that server has same password
                return item;
            }

            @Override
            protected void onPostExecute(JsonItem result) {
                super.onPostExecute(result);

            }
        }.execute();
    }
    //Removes user from device and from server
    public void removeServerAndDeviceAccount(AsyncTaskInterface<Intent> myAsyncTaskInterface,final Activity activity) {
        new AsyncTaskAbstract<Void, Void, Intent>(myAsyncTaskInterface,700) {
            @Override
            protected Intent doInBackground(Void... params) {
                super.doInBackground();
                Bundle data = new Bundle();
                boolean removeResult;
                mUser.setLanguage(Internationalization.getLanguage(mContext));
                JsonItem item = sServerAuthenticate.userRemove(mUser);
                if (!item.getResult()) {
                    data.putString(KEY_ERROR_MESSAGE, item.getMessage());
                    data.putString(KEY_ERROR_CODE, item.getKeyError());
                } else {
                    if (!removeAccount(mUser)) {
                        data.putString(KEY_ERROR_MESSAGE, "Could not remove device account !");
                        data.putString(KEY_ERROR_CODE, "account.remove.error");
                    }
                }
                //Settings for the Account AuthenticatorActivity, we return no account
                data.putString(AccountManager.KEY_ACCOUNT_NAME, null);
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, null);
                data.putString(AccountManager.KEY_AUTHTOKEN, null);

                final Intent res = new Intent();
                res.putExtras(data);
                return res;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                super.onPostExecute(intent);
                if (!intent.hasExtra(KEY_ERROR_MESSAGE)) {
                    activity.getIntent().putExtras(intent.getExtras());
                    activity.setResult(Activity.RESULT_OK, intent);
                    activity.finish();
                }
            }
        }.execute();
    }
    //Change names
    public void changeNames(final String firstName, final String lastName,AsyncTaskInterface<JsonItem> myAsyncTaskInterface, final Activity activity) {
        new AsyncTaskAbstract<Void, Void, JsonItem>(myAsyncTaskInterface,700) {
            JsonItem item;
            @Override
            protected JsonItem doInBackground(Void... params) {
                super.doInBackground();
                Log.i(TAG, "Started checking password");

                item = sServerAuthenticate.userChangeNames(mUser);
                getDataFromDeviceAccount(getAccount(mUser));
                //Update local account
                if (item.getResult()) {
                    mUser.setFirstName(firstName);
                    mUser.setLastName(lastName);
                    mAccountManager.setUserData(getAccount(mUser), PARAM_USER_FIRST_NAME, firstName);
                    mAccountManager.setUserData(getAccount(mUser), PARAM_USER_LAST_NAME, lastName);
                }
                //Check that server has same password
                return item;
            }

            @Override
            protected void onPostExecute(JsonItem result) {
                super.onPostExecute(result);

            }
        }.execute();
    }

    //Change names
    public void changeAvatar(final Bitmap avatar, AsyncTaskInterface<JsonItem> myAsyncTaskInterface, final Activity activity) {
        new AsyncTaskAbstract<Void, Void, JsonItem>(myAsyncTaskInterface,700) {
            JsonItem item;
            @Override
            protected JsonItem doInBackground(Void... params) {
                super.doInBackground();
                Log.i(TAG, "Started checking password");

                item = sServerAuthenticate.userChangeAvatar(mUser);
                getDataFromDeviceAccount(getAccount(mUser));
                //Update local account
                if (item.getResult()) {
                    mUser.setAvatar(avatar);
                    mAccountManager.setUserData(getAccount(mUser), PARAM_USER_AVATAR, mUser.getAvatar());
                }
                //Check that server has same password
                return item;
            }

            @Override
            protected void onPostExecute(JsonItem result) {
                super.onPostExecute(result);

            }
        }.execute();
    }








    //Only for Debug
    public String getPassword() {
        return mAccountManager.getPassword(getAccount(mUser));
    }

}
