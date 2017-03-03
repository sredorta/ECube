package com.ecube.solutions.ecube.authentication.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.network.Encryption;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by sredorta on 3/1/2017.
 */
public class AccountGeneral {
    //Logs
    private static final String TAG = AccountGeneral.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static final String ACCOUNT_TYPE = "com.locker.ilockapp.auth_locker";
    //com.ecube.solutions.ecube.auth_ecube";  //Defines our account
    //
    //Access types
    public static final String AUTHTOKEN_TYPE_STANDARD = "Standard access";
    public static final String AUTHTOKEN_TYPE_STANDARD_LABEL = "Standard access to eCube account";

    public static final String AUTHTOKEN_TYPE_FULL = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_LABEL = "Full access to eCube account";

    //Parameters that we save in the account
    public final static String PARAM_USER_ACCOUNT_TYPE = "USER_ACCOUNT_TYPE";
    public final static String PARAM_USER_ACCOUNT_AUTH_TYPE = "USER_ACCOUNT_AUTH_TYPE";
    public final static String PARAM_USER_ACCOUNT_ID = "USER_ACCOUNT_ID";
    public final static String PARAM_USER_FIRST_NAME = "USER_FIRST_NAME";
    public final static String PARAM_USER_LAST_NAME = "USER_LAST_NAME";
    public final static String PARAM_USER_EMAIL = "USER_EMAIL";
    public final static String PARAM_USER_PHONE = "USER_PHONE";
    public final static String PARAM_USER_AVATAR = "USER_AVATAR";

    private Context mContext;
    private User mUser;
    private AccountManager mAccountManager;


    public AccountGeneral(Context context) {
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }
    public AccountGeneral(Context context, User user) {
        mContext = context;
        mAccountManager = AccountManager.get(context);
        mUser = user;
    }

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

    //Gets the first account found wmattching accountType
    public Account getAccount() {
        //Find if there is an account with the correct accountName and get its token
        for (Account account : mAccountManager.getAccountsByType(ACCOUNT_TYPE)) {
            return account;
        }
        return null;
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
            if (mUser.getAvatarBitmap() != null)
                mAccountManager.setUserData(account, PARAM_USER_AVATAR, mUser.getAvatarString());
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
        mUser.setAvatarString(mAccountManager.getUserData(myAccount, PARAM_USER_AVATAR),mContext);

        //System parameters
        mUser.setPassword(mAccountManager.getPassword(myAccount));
        mUser.setAccountAccess(mAccountManager.getUserData(myAccount, PARAM_USER_ACCOUNT_AUTH_TYPE));
        if (mUser.getAccountAccess() != null){
            mUser.setToken(mAccountManager.peekAuthToken(myAccount,mUser.getAccountAccess()));
            if(DEBUG) Log.i(TAG, "We restored the token:" + mUser.getToken());
        }
        return mUser;
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
}
