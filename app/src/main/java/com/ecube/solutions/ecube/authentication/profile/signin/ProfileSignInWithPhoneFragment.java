package com.ecube.solutions.ecube.authentication.profile.signin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.WaitDialogFragment;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateStartFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.Internationalization;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.authentication.profile.dialogs.CountryPickerFragment;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

/**
 * Created by sredorta on 3/1/2017.
 */
public class ProfileSignInWithPhoneFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileSignInWithPhoneFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    private AccountAuthenticator myAccountAuthenticator;
    private User mUser;
    private final int REQ_SIGNUP = 1;
    private final int REQ_SIGNIN_WITH_EMAIL = 2;


    //For rotation
    private static String KEY_CURRENT_LOCALE = "user.current.locale";

    //Request for the CountryPicker
    private static int REQUEST_COUNTRY = 0;

    private String mPhoneNumber;                //Contains input phone number
    private Locale mLocale;                     //Current country Locale of the phone number
    private CountryPickerFragment dialog;       //Dialog to choose another country




    // Constructor
    public static ProfileSignInWithPhoneFragment newInstance() {
        return new ProfileSignInWithPhoneFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get account details from Singleton either from intent or from account of the device
        myAccountAuthenticator = new AccountAuthenticator(getContext());
        mUser = new User();

        if (mLocale == null) {
            mLocale = Locale.getDefault();
            mPhoneNumber = "";
        }

        if (savedInstanceState!= null) {
            mLocale = (Locale) savedInstanceState.getSerializable(KEY_CURRENT_LOCALE);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_signin_with_phone, container, false);
        setCurrentView(v);
        //Update the current country
        updateCurrentCountry();
        final EditText passwordEditText = (EditText) v.findViewById(R.id.profile_signin_with_phone_editText_password);
        //Show CountryPicker Dialog if we click
        final LinearLayout mLinearCountry = (LinearLayout) v.findViewById(R.id.profile_create_country_display_LinearLayout);
        mLinearCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(CountryPickerFragment.FRAGMENT_INPUT_PARAM_CURRENT_PHONE_COUNTRY, mLocale);
                FragmentManager fm = getFragmentManager();
                dialog = CountryPickerFragment.newInstance(bundle);
                dialog.setTargetFragment(ProfileSignInWithPhoneFragment.this, REQUEST_COUNTRY);
                dialog.show(fm,"DIALOG");
            }
        });

        //Listener on Phone number
        final EditText mNumberEditText = (EditText) v.findViewById(R.id.profile_signin_with_phone_editText_number);
        if (DEBUG) Log.i(TAG, "Setting number to: " + mPhoneNumber);
        mNumberEditText.setText(mPhoneNumber);
        //We add a listener to verify that we have correct number
        mNumberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                //Return if no characters are here
                if (checkPhoneNumber(mNumberEditText.getText().toString())) {
                    mNumberEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    mNumberEditText.setTypeface(mNumberEditText.getTypeface(), Typeface.BOLD);
                    hideInputKeyBoard();
                } else {
                    mNumberEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                    mNumberEditText.setTypeface(mNumberEditText.getTypeface(), Typeface.NORMAL);
                }
                if (s.toString().length()== 0) return;
                if ((s.toString().matches("[0-9]+") && s.toString().length() > 0)) {
                    //Do nothing
                } else {
                    //The input character was not a number so we remove it
                    s.delete(s.length()-1,s.length());
                }
            }

        });

        //Submit button
        v.findViewById(R.id.profile_signin_with_phone_Button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) Log.i(TAG, "Submitting credentials to account manager !");
                //hide input keyboard
                hideInputKeyBoard();
                if (checkPhoneNumber(mNumberEditText.getText().toString())) {
                    if (User.checkPasswordInput(passwordEditText,mView,mActivity)) {
                        if (DEBUG) Log.i(TAG, "We are now checking with server !");
                        //TODO do the actual login with the server
                        User myUser = new User();
                        myUser.setPhone(getFinalPhoneNumber());
                        myUser.setPassword(passwordEditText.getText().toString());
                        Log.i(TAG, "Restoring user...");
                        AccountAuthenticator ag = new AccountAuthenticator(getContext(), myUser);
                        ag.submitCredentials(new AsyncTaskInterface() {
                            WaitDialogFragment dialog;
                            @Override
                            public void processStart() {
                                FragmentManager fm = getFragmentManager();
                                dialog = WaitDialogFragment.newInstance();
                                dialog.show(fm,"DIALOG");
                            }
                            @Override
                            public void processFinish() {
                                dialog.dismiss();
                            }
                        }, mActivity);
                        //If we get to this point is that we could not authenticate !
                        passwordEditText.setText("");
                    }
                }
            }
        });



        //Login using email
        v.findViewById(R.id.profile_signin_with_phone_TextView_use_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileSignInWithEmailFragment fragment = ProfileSignInWithEmailFragment.newInstance();
                fragment.setTargetFragment(ProfileSignInWithPhoneFragment.this, REQ_SIGNIN_WITH_EMAIL);
                replaceFragment(fragment);  //This comes from abstract
            }
        });

        //Signup
        v.findViewById(R.id.profile_signin_with_phone_TextView_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileCreateStartFragment fragment = ProfileCreateStartFragment.newInstance();
                fragment.setTargetFragment(ProfileSignInWithPhoneFragment.this, REQ_SIGNUP);
                replaceFragment(fragment);  //This comes from abstract
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == ProfileSignInWithPhoneFragment.REQUEST_COUNTRY) {
            mLocale = (Locale) data.getSerializableExtra(CountryPickerFragment.FRAGMENT_OUTPUT_PARAM_SELECTED_PHONE_COUNTRY);
            dialog.dismiss();
            updateCurrentCountry();
            //In order to validate if with new country the phone is correct
            final EditText mNumberEditText = (EditText) mView.findViewById(R.id.profile_signin_with_phone_editText_number);
            mNumberEditText.setText(mNumberEditText.getText().toString());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_CURRENT_LOCALE, mLocale);
    }


    private void updateCurrentCountry() {
        final TextView mCountryTextView = (TextView) mView.findViewById(R.id.profile_create_country_display_TextView_country);
        mCountryTextView.setText(mLocale.getDisplayCountry());

        final TextView mPrefixTextView = (TextView) mView.findViewById(R.id.profile_create_country_display_TextView_prefix);
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        int prefix =  phoneUtil.getCountryCodeForRegion(mLocale.getCountry());
        mPrefixTextView.setText("+" + prefix);

        final ImageView mCountryFlag = (ImageView) mView.findViewById(R.id.profile_create_country_display_ImageView_flag);
        mCountryFlag.setImageBitmap(Internationalization.getCountryFlagBitmapFromAsset(getContext(),mLocale));
    }

    private boolean checkPhoneNumber(String phone) {
        Boolean isValid = false;
        if ((phone.matches("[0-9]+") && phone.length() > 0)) {
            //We only start checking if number is valid once length is larger than 2 to avoid crash
            if (phone.length()>2) {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber inputNumber;
                try {
                    inputNumber = phoneUtil.parse(phone, mLocale.getCountry());
                } catch (NumberParseException e) {
                    inputNumber = null;
                    Log.i(TAG, "Caught exception :" + e);
                }
                isValid = phoneUtil.isValidNumber(inputNumber);
            }
        }
        return isValid;
    }

    private String getFinalPhoneNumber() {
        final EditText mNumberEditText = (EditText) mView.findViewById(R.id.profile_signin_with_phone_editText_number);
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber inputNumber;
        try {
            inputNumber = phoneUtil.parse(mNumberEditText.getText().toString(), mLocale.getCountry());
        } catch (NumberParseException e) {
            inputNumber = null;
            Log.i(TAG, "Caught exception :" + e);
        }
        return phoneUtil.format(inputNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
    }
}