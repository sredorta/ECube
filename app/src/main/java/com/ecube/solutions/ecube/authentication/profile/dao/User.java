package com.ecube.solutions.ecube.authentication.profile.dao;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;


import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.helpers.TextInputLayoutHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sredorta on 2/28/2017.
 */
public class User implements Parcelable {
    //Logs
    private static final String TAG = User.class.getSimpleName();
    private static final boolean DEBUG = true;

    @SerializedName("id")
    @Expose(serialize = true, deserialize = true)
    private String mId = null;             //User id

    @SerializedName("email")
    @Expose(serialize = true, deserialize = true)
    private String mEmail = null;          //User email

    @SerializedName("phone")
    @Expose(serialize = true, deserialize = true)
    private String mPhone = null;          //User phone

    @SerializedName("firstName")
    @Expose(serialize = true, deserialize = true)
    private String mFirstName = null;      //User first name

    @SerializedName("lastName")
    @Expose(serialize = true, deserialize = true)
    private String mLastName = null;       //User last name

    @SerializedName("account_access")
    @Expose(serialize = true, deserialize = true)
    private String mAccountAccess = null;  //Defines type of access of the user

    @SerializedName("token")
    @Expose(serialize = true, deserialize = true)
    private String mToken = null;          //User token

    @SerializedName("creation_timestamp")
    @Expose(serialize = true, deserialize = true)
    private Integer mCreationTimeStamp;

    @SerializedName("login_timestamp")
    private Integer mLastLoginTimeStamp;

    @SerializedName("password")
    @Expose(serialize = true, deserialize = true)
    private String mPassword = null;       //User password

    @SerializedName("latitude")
    @Expose(serialize = true, deserialize = true)
    private String mUserLatitude = null;;

    @SerializedName("longitude")
    @Expose(serialize = true, deserialize = true)
    private String mUserLongitude = null;;

    @SerializedName("avatar")
    @Expose(serialize = true, deserialize = true)
    private String mAvatar;

    @SerializedName("language")
    @Expose(serialize = true, deserialize = true)
    private String mLanguage;

//    private Bitmap mAvatarBitmap = null;   //User avatar bitmap




    public User() {}



    public String getAccountAccess() {
        return mAccountAccess;
    }

    public void setAccountAccess(String mAccountAccess) {
        this.mAccountAccess = mAccountAccess;
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

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String mLanguage) {
        this.mLanguage = mLanguage;
    }




    // Gets the Avatar string
    public String getAvatar() {return mAvatar;}

    //Sets the Avatar string from String
    public void setAvatar(String avatar) { mAvatar = avatar;}

    //sets the Avatar string from bitmap
    public void setAvatar(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOS);
        this.mAvatar = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    //Gets the Avatar string into bitmap format
    public Bitmap getAvatar(Context context) {
        if (this.mAvatar != null) {
            byte[] bitmapBytes = Base64.decode(this.mAvatar, Base64.DEFAULT);
            if (bitmapBytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                return bitmap;
            }
        }
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.profile_user_default);
    }




    //Update from a user object all non-null variables
    public void update(User data){
        if (data.getId() != null) this.setId(data.getId());
        if (data.getFirstName() != null) this.setFirstName(data.getFirstName());
        if (data.getLastName() != null) this.setLastName(data.getLastName());
        if (data.getEmail() != null) this.setEmail(data.getEmail());
        if (data.getPhone() != null) this.setPhone(data.getPhone());
        if (data.getAvatar() != null) this.setAvatar(data.getAvatar());
        if (data.getPassword() != null) this.setPassword(data.getPassword());
        if (data.getToken() != null) this.setToken(data.getToken());
        if (data.getAccountAccess() != null) this.setAccountAccess(data.getAccountAccess());
        if (data.getLanguage() != null) this.setLanguage(data.getLanguage());

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
            Log.i(TAG, "mLanguage      =   " + mLanguage);
            Log.i(TAG, "mAvatar        =   " + mAvatar);
            Log.i(TAG, "mPassword      =   " + mPassword);
            Log.i(TAG, "mAccountAccess =   " + mAccountAccess);
            Log.i(TAG, "mToken         =   " + mToken);
            Log.i(TAG, "-----------------------------------------");
        }
    }


   //Parses a JSON with the fields and returns a User object
    public static User parseJSON(String jsonString) {
        User myUser = new User();
        try {
            JSONObject jsonBody = new JSONObject(jsonString);
            Gson gson = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
            myUser = gson.fromJson(jsonBody.toString(), User.class);
            Log.i(TAG, "User details from JSON: " + jsonBody.toString(1));
            myUser.print("User fields we got from JSON:");
        }  catch (JSONException je) {
            Log.i(TAG,"Caught exception: " + je);
        }
        return myUser;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // PARCELABLE PART
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mEmail);
        parcel.writeString(mPhone);
        parcel.writeString(mLanguage);
        parcel.writeString(mFirstName);
        parcel.writeString(mLastName);
        parcel.writeString(mAccountAccess);
        parcel.writeString(mToken);
        parcel.writeString(mPassword);
        parcel.writeString(mAvatar);
        //parcel.writeParcelable(mAvatarString, i);
    }
    //Constructor for CREATOR of parcel
    private User(Parcel in) {
        mId = in.readString();
        mEmail = in.readString();
        mPhone = in.readString();
        mLanguage = in.readString();
        mFirstName = in.readString();
        mLastName = in.readString();
        mAccountAccess = in.readString();
        mToken = in.readString();
        mPassword = in.readString();
        mAvatar = in.readString();
        //mAvatarBitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }
        @Override
        public User[] newArray(int i) {
            return new User[i];
        }
    };

}
