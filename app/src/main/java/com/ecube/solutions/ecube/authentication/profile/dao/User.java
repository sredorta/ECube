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
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;


import com.ecube.solutions.ecube.R;

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

    private String mId = null;             //User id
    private String mEmail = null;          //User email
    private String mPhone = null;          //User phone
    private String mFirstName = null;      //User first name
    private String mLastName = null;       //User last name
    private String mAccountAccess = null;  //Defines type of access of the user
    private String mToken = null;          //User token
    private String mPassword = null;       //User password
    private Bitmap mAvatarBitmap = null;   //User avatar bitmap



    public User() {}

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

    //Converts bitmap into string
    public String getAvatarString() {
        if (this.getAvatarBitmap() == null) return null;
        String result = null;
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        getAvatarBitmap().compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }
    public void setAvatarString(String avatar, Context context) {
        byte[] bitmapBytes = Base64.decode(avatar, Base64.DEFAULT);
        if (bitmapBytes != null) {
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0, bitmapBytes.length);
            this.setAvatarBitmap(bitmap);
        } else
            this.setAvatarBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.profile_user_default));
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
    // Input checks
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

    //checks email input and acts on the EditText and shows snackBar if wrong
    public static boolean checkEmailInput(EditText email, final View v) {
        boolean result = checkEmailInput(email.getText().toString());
        if (!result) {
            email.setHintTextColor(ContextCompat.getColor(v.getContext(), R.color.colorAccent));
            email.setTextColor(ContextCompat.getColor(v.getContext(), R.color.colorAccent));
            Snackbar snackbar = Snackbar.make(v, "Invalid email format", Snackbar.LENGTH_LONG);
            snackbar.show();
        } else {
            email.setHintTextColor(ContextCompat.getColor(v.getContext(), R.color.colorPrimary));
        }
        return result;
    }

    //Check that email meets the required format
    public static boolean checkEmailInput(String email) {
        CharSequence cs = email;
        return Patterns.EMAIL_ADDRESS.matcher(cs).matches();
    }

    //Get strength of password and update progressBar accordingly
    public static int getPasswordQuality(String password, ProgressBar progressBar, final View v) {
        int quality = getPasswordQuality(password);
        progressBar.setProgress(quality);
        Drawable bgDrawable = progressBar.getProgressDrawable();
        if (quality < 50)
            bgDrawable.setColorFilter(ContextCompat.getColor(v.getContext(), R.color.md_red_500), PorterDuff.Mode.MULTIPLY);
        else if (quality >= 50 && quality < 70)
            bgDrawable.setColorFilter(ContextCompat.getColor(v.getContext(), R.color.md_orange_500), PorterDuff.Mode.MULTIPLY);
        else if (quality >= 70)
            bgDrawable.setColorFilter(ContextCompat.getColor(v.getContext(), R.color.md_green_500), PorterDuff.Mode.MULTIPLY);
        progressBar.setProgressDrawable(bgDrawable);
        return quality;
    }

    //Get stringth of password
    public static int getPasswordQuality(String password) {
        int length = 0, uppercase = 0, lowercase = 0, digits = 0, symbols = 0, bonus = 0, requirements = 0;

        int lettersonly = 0, numbersonly = 0, cuc = 0, clc = 0;

        length = password.length();
        for (int i = 0; i < password.length(); i++) {
            if (Character.isUpperCase(password.charAt(i)))
                uppercase++;
            else if (Character.isLowerCase(password.charAt(i)))
                lowercase++;
            else if (Character.isDigit(password.charAt(i)))
                digits++;

            symbols = length - uppercase - lowercase - digits;
        }

        for (int j = 1; j < password.length() - 1; j++) {
            if (Character.isDigit(password.charAt(j)))
                bonus++;
        }
        for (int k = 0; k < password.length(); k++) {
            if (Character.isUpperCase(password.charAt(k))) {
                k++;
                if (k < password.length()) {
                    if (Character.isUpperCase(password.charAt(k))) {
                        cuc++;
                        k--;
                    }
                }
            }
        }

        for (int l = 0; l < password.length(); l++) {
            if (Character.isLowerCase(password.charAt(l))) {
                l++;
                if (l < password.length()) {
                    if (Character.isLowerCase(password.charAt(l))) {
                        clc++;
                        l--;
                    }
                }
            }
        }

        if (length > 7) { requirements++;}
        if (uppercase > 0) { requirements++; }
        if (lowercase > 0) { requirements++; }
        if (digits > 0) { requirements++; }
        if (symbols > 0) { requirements++; }
        if (bonus > 0) { requirements++;}
        if (digits == 0 && symbols == 0) { lettersonly = 1; }
        if (lowercase == 0 && uppercase == 0 && symbols == 0) { numbersonly = 1;}

        int Total = (length * 4) + ((length - uppercase) * 2)
                + ((length - lowercase) * 2) + (digits * 4) + (symbols * 6)
                + (bonus * 2) + (requirements * 2) - (lettersonly * length*2)
                - (numbersonly * length*3) - (cuc * 2) - (clc * 2);
        if (Total > 100) Total = 100;
        return Total;
    }

    //Check that password meets the required strength
    public static boolean checkPasswordInput(String password) {
        if (getPasswordQuality(password) > 70) return true;
        else return false;
    }


    //Check that password meets the required strength and updates editText if is not the case
    public static boolean checkPasswordInput(EditText password, final View v, final Activity activity) {
        boolean result = checkPasswordInput(password.getText().toString());
        if (!result) {
            password.setText("");
            password.setHintTextColor(ContextCompat.getColor(v.getContext(), R.color.colorAccent));
            Snackbar snackbar = Snackbar.make(v, "Invalid password", Snackbar.LENGTH_LONG).setAction("DETAILS", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage("A minimum password strength is required. By combining lowercase, uppercase and digits the strength is increased.")
                            .setTitle("Password requirements:");
                    builder.setCancelable(true);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    builder.create();
                    builder.show();
                }
            });
            snackbar.show();
        } else {
            password.setHintTextColor(ContextCompat.getColor(v.getContext(), R.color.colorPrimary));
        }
        return result;
    }
    //Check that password meets the required strength and updates editText if is not the case
    public static boolean checkShadowPasswordInput(EditText password, EditText passwordShadow, final View v, final Activity activity) {

        boolean result = checkPasswordInput(password, v, activity);
        if (result)
            if (password.getText().toString().equals(passwordShadow.getText().toString())) {
                return true;
            } else {
                passwordShadow.setText("");
                passwordShadow.setHintTextColor(ContextCompat.getColor(v.getContext(), R.color.colorAccent));
                Snackbar snackbar = Snackbar.make(v, "Passwords don't match", Snackbar.LENGTH_LONG);
                snackbar.show();
                return false;
            }
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // PARCELABLE PART
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mEmail);
        parcel.writeString(mPhone);
        parcel.writeString(mFirstName);
        parcel.writeString(mLastName);
        parcel.writeString(mAccountAccess);
        parcel.writeString(mToken);
        parcel.writeString(mPassword);
        parcel.writeParcelable(mAvatarBitmap, i);
    }
    //Constructor for CREATOR of parcel
    private User(Parcel in) {
        mId = in.readString();
        mEmail = in.readString();
        mPhone = in.readString();
        mFirstName = in.readString();
        mLastName = in.readString();
        mAccountAccess = in.readString();
        mToken = in.readString();
        mPassword = in.readString();
        mAvatarBitmap = in.readParcelable(Bitmap.class.getClassLoader());
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
