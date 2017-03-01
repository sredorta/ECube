package com.ecube.solutions.ecube.authentication.profile.create;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.profile.dao.Internationalization;
import com.ecube.solutions.ecube.authentication.profile.dialogs.CountryPickerFragment;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

/**
 * Created by sredorta on 2/21/2017.
 */
public class ProfileCreatePhoneFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileCreatePhoneFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_PHONE_NUMBER = "user.phone_number.in";
    public static final String FRAGMENT_OUTPUT_PARAM_USER_PHONE_NUMBER = "user.phone_number.out";

    //For rotation
    private static String KEY_CURRENT_LOCALE = "user.current.locale";

    //Request for the CountryPicker
    private static int REQUEST_COUNTRY = 0;

    private String mPhoneNumber;                //Contains input phone number
    private String mFinalPhoneNumber = new String();

    private boolean isPhoneNumberCorrect;       //Contains if typed in number is correct
    private Locale mLocale;                     //Current country Locale of the phone number
    private CountryPickerFragment dialog;       //Dialog to choose another country

    // Constructor
    public static ProfileCreatePhoneFragment newInstance() {
        return new ProfileCreatePhoneFragment();
    }
    // Constructor with input arguments
    public static ProfileCreatePhoneFragment newInstance(Bundle data) {
        ProfileCreatePhoneFragment fragment = ProfileCreatePhoneFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }

    private Locale getLocaleFromNumber(String number) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String country = "";
        try {
            Phonenumber.PhoneNumber inputNumber = phoneUtil.parse(number, null);
            country = phoneUtil.getRegionCodeForNumber(inputNumber);
        } catch (NumberParseException e) {
            Log.i(TAG, "Caught exception : " + e);

        }
        if (country.equals("")) return null;
        return new Locale("",country);
    }

    private String getNumberWithoutCountry(String number) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber inputNumber = phoneUtil.parse(number, null);
            return String.valueOf(inputNumber.getNationalNumber());
        } catch (NumberParseException e) {
            Log.i(TAG, "Caught exception : " + e);
            return "";
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocale = getLocaleFromNumber((String) getInputParam(ProfileCreatePhoneFragment.FRAGMENT_INPUT_PARAM_USER_PHONE_NUMBER));
        mPhoneNumber = getNumberWithoutCountry((String) getInputParam(ProfileCreatePhoneFragment.FRAGMENT_INPUT_PARAM_USER_PHONE_NUMBER));
        //In case we could not parse the phone or it was empty
        if (mLocale == null) {
            mLocale = Locale.getDefault();
            mPhoneNumber = "";
        }

        if (savedInstanceState!= null) {
            mLocale = (Locale) savedInstanceState.getSerializable(KEY_CURRENT_LOCALE);
        }

        //We start with wrong number
        isPhoneNumberCorrect = false;
    }

    private void updateCurrentCountry() {
        final TextView mCountryTextView = (TextView) mView.findViewById(R.id.profile_create_country_display_TextView_country);
        mCountryTextView.setText(mLocale.getDisplayCountry());

        final ImageView mCountryFlag = (ImageView) mView.findViewById(R.id.profile_create_country_display_ImageView_flag);
        mCountryFlag.setImageBitmap(Internationalization.getCountryFlagBitmapFromAsset(getContext(),mLocale));

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_create_phone_fragment, container, false);
        mView =v;
        //Update the current country
        updateCurrentCountry();

        //Show CountryPicker Dialog if we click
        final LinearLayout mLinearCountry = (LinearLayout) v.findViewById(R.id.profile_create_country_display_LinearLayout);
        mLinearCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(CountryPickerFragment.FRAGMENT_INPUT_PARAM_CURRENT_PHONE_COUNTRY, mLocale);
                FragmentManager fm = getFragmentManager();
                dialog = CountryPickerFragment.newInstance(bundle);
                dialog.setTargetFragment(ProfileCreatePhoneFragment.this, REQUEST_COUNTRY);
                dialog.show(fm,"DIALOG");

            }
        });


        final EditText mNumberEditText = (EditText) v.findViewById(R.id.profile_create_phone_editText_number);
        if (DEBUG) Log.i(TAG, "Setting number to: " + mPhoneNumber);
        mNumberEditText.setText(mPhoneNumber);

        final Button nextButton = (Button) v.findViewById(R.id.profile_create_phone_button);

        //We add a listener to verify that we have correct number
        mNumberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                //Return if no characters are here
                if (s.toString().length()== 0) return;
                if ((s.toString().matches("[0-9]+") && s.toString().length() > 0)) {
                    mNumberEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                    mNumberEditText.setTypeface(mNumberEditText.getTypeface(), Typeface.NORMAL);
                    //We only start checking if number is valid once length is larger than 2 to avoid crash
                    if (s.toString().length()>2) {
                        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                        Phonenumber.PhoneNumber inputNumber;
                        try {
                            inputNumber = phoneUtil.parse(s.toString(), mLocale.getCountry());
                        } catch (NumberParseException e) {
                            inputNumber = null;
                            Log.i(TAG, "Caught exception :" + e);
                        }
                        isPhoneNumberCorrect = phoneUtil.isValidNumber(inputNumber);
                        if (isPhoneNumberCorrect) {
                            mNumberEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                            mNumberEditText.setTypeface(mNumberEditText.getTypeface(), Typeface.BOLD);
                            mFinalPhoneNumber = phoneUtil.format(inputNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
                            hideInputKeyBoard();
                            if (DEBUG) Log.i(TAG, "Is Valid = : " + isPhoneNumberCorrect);
                        }
                    }
                } else {
                    //The input character was not a number so we remove it
                    s.delete(s.length()-1,s.length());
                }
            }

        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Hide keyboard if exists
                hideInputKeyBoard();
                if (!isPhoneNumberCorrect) {
                    Snackbar snackbar = Snackbar.make(mView, "Invalid telephone format !", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_PHONE_NUMBER, mFinalPhoneNumber);
                    sendResult(Activity.RESULT_OK);
                }
            }
        });
        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == ProfileCreatePhoneFragment.REQUEST_COUNTRY) {
            mLocale = (Locale) data.getSerializableExtra(CountryPickerFragment.FRAGMENT_OUTPUT_PARAM_SELECTED_PHONE_COUNTRY);
            dialog.dismiss();
            updateCurrentCountry();
            //In order to validate if with new country the phone is correct
            final EditText mNumberEditText = (EditText) mView.findViewById(R.id.profile_create_phone_editText_number);
            mNumberEditText.setText(mNumberEditText.getText().toString());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_CURRENT_LOCALE, mLocale);
    }
}
