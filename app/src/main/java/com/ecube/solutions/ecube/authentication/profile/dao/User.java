package com.ecube.solutions.ecube.authentication.profile.dao;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


import com.ecube.solutions.ecube.R;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Created by sredorta on 2/28/2017.
 */
public class User implements Serializable {
    //Logs
    private static final String TAG = User.class.getSimpleName();
    private static final boolean DEBUG = true;

    private String mId = null;             //User id
    private String mEmail = null;          //User email
    private String mPhone = null;          //User phone
    private String mFirstName = null;      //User first name
    private String mLastName = null;       //User last name
    private String mAccountAccess = null;  //Defines type of access of the user
    private String mToken = null;          //User token
    private String mPassword = null;       //User password
    private Bitmap mAvatarBitmap = null;   //User avatar bitmap


    public String getAccountAccess() {
        return mAccountAccess;
    }

    public void setAccountAccess(String mAccountAccess) {
        this.mAccountAccess = mAccountAccess;
    }

    public Bitmap getAvatarBitmap() {
        return mAvatarBitmap;
    }

    public void setAvatarBitmap(Bitmap mAvatarBitmap) {
        this.mAvatarBitmap = mAvatarBitmap;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String mToken) {
        this.mToken = mToken;
    }

    //Prints status of user
    public void print(String s) {
        if (DEBUG) {
            Log.i(TAG,s);
            Log.i(TAG, "mId            =   " + mId);
            Log.i(TAG, "mFirstName     =   " + mFirstName);
            Log.i(TAG, "mLastName      =   " + mLastName);
            Log.i(TAG, "mEmail         =   " + mEmail);
            Log.i(TAG, "mPhone         =   " + mPhone);
            if (mAvatarBitmap != null)
                Log.i(TAG, "mAvatar        =   " + mAvatarBitmap.toString());
            else
                Log.i(TAG, "mAvatar        =   null");
            Log.i(TAG, "mPassword      =   " + mPassword);
            Log.i(TAG, "mAccountAccess =   " + mAccountAccess);
            Log.i(TAG, "mToken         =   " + mToken);
            Log.i(TAG, "-----------------------------------------");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean checkFirstNameInput(EditText myEditText, final View v) {
        if (myEditText.getText().toString().equals("") || myEditText.length() < 3) {
            myEditText.setHintTextColor(ContextCompat.getColor(v.getContext(), R.color.colorAccent));
            Snackbar snackbar = Snackbar.make(v, "Please insert correct name", Snackbar.LENGTH_LONG);
            snackbar.show();
            return false;
        } else {
            return true;
        }
    }

    public static boolean checkLastNameInput(EditText myEditText, final View v) {
        return checkFirstNameInput(myEditText,v);
    }


}
