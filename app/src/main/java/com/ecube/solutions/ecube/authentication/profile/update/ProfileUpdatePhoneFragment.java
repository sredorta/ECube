package com.ecube.solutions.ecube.authentication.profile.update;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.WaitDialogFragment;
import com.ecube.solutions.ecube.abstracts.AsyncTaskInterface;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreatePhoneFragment;
import com.ecube.solutions.ecube.authentication.profile.dao.Internationalization;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.authentication.profile.dialogs.CountryPickerFragment;
import com.ecube.solutions.ecube.general.AppGeneral;
import com.ecube.solutions.ecube.network.JsonItem;
import com.ecube.solutions.ecube.widgets.TextInputLayoutAppWidget;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

/**
 * Created by sredorta on 3/23/2017.
 */

public class ProfileUpdatePhoneFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileUpdatePhoneFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_CURRENT = "user.current.in";
    public static final String FRAGMENT_OUTPUT_PARAM_USER_PHONE = "user.phone.out";    //String

    //In case of rotations
    public static final String KEY_CURRENT_USER = "user.save";
    private static String KEY_CURRENT_LOCALE = "user.current.locale";

    //Request for the CountryPicker
    private static int REQUEST_COUNTRY = 0;

    private User mUser;
    private Locale mLocale;
    private CountryPickerFragment dialog;       //Dialog to choose another country
    private String mPhoneNumber;
    private AccountAuthenticator myAccountAuthenticator;

    // Constructor
    public static ProfileUpdatePhoneFragment newInstance() {
        return new ProfileUpdatePhoneFragment();
    }

    public static ProfileUpdatePhoneFragment newInstance(Bundle data) {
        ProfileUpdatePhoneFragment fragment = ProfileUpdatePhoneFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String email = (String) getInputParam(ProfileUpdateStartFragment.FRAGMENT_INPUT_PARAM_USER_CURRENT);
        //Get account details from the device
        myAccountAuthenticator = new AccountAuthenticator(getContext());
        //Restore user in case of rotation
        mUser = new User();
        if (savedInstanceState != null) {
            mUser = (User) savedInstanceState.getParcelable(KEY_CURRENT_USER);
            mLocale = (Locale) savedInstanceState.getSerializable(KEY_CURRENT_LOCALE);
        } else {
            mUser = myAccountAuthenticator.getDataFromDeviceAccount(myAccountAuthenticator.getAccount(email));
        }

        //Check that we have an account with current user and if not exit
        mLocale = getLocaleFromNumber(mUser.getPhone());
        mPhoneNumber = getNumberWithoutCountry(mUser.getPhone());
        //In case we could not parse the phone or it was empty
        if (mLocale == null) {
            mLocale = Locale.getDefault();
            mPhoneNumber = "";
        }


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_update_phone, container, false);
        setCurrentView(v);
        final TextInputLayoutAppWidget passwordTextInputLayout = (TextInputLayoutAppWidget) v.findViewById(R.id.profile_update_phone_TextInputLayoutAppWidget_password);
        final TextInputLayoutAppWidget phoneTextInputLayout = (TextInputLayoutAppWidget) v.findViewById(R.id.profile_update_phone_TextInputLayoutAppWidget_phone);
        phoneTextInputLayout.setLocale(mLocale);
        phoneTextInputLayout.setText(mPhoneNumber.toString());
        phoneTextInputLayout.getEditText().setSelection(phoneTextInputLayout.getText().length());

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
                dialog.setTargetFragment(ProfileUpdatePhoneFragment.this, REQUEST_COUNTRY);
                dialog.show(fm,"DIALOG");
            }
        });

        final Button submitButton = (Button) v.findViewById(R.id.profile_update_phone_Button_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordTextInputLayout.isValidInput()) {
                    if (phoneTextInputLayout.isValidInput()) {
                        final WaitDialogFragment dialog = WaitDialogFragment.newInstance();
                        //Check that old password is correct
                        mUser.setPassword(passwordTextInputLayout.getText());
                        AccountAuthenticator ag = new AccountAuthenticator(getContext(), mUser);
                        ag.changePhone(phoneTextInputLayout.getFinalPhoneNumber(),new AsyncTaskInterface<JsonItem>() {
                            @Override
                            public void processStart() {
                                FragmentManager fm = getFragmentManager();
                                dialog.show(fm, "DIALOG");
                            }

                            @Override
                            public void processFinish(JsonItem result) {
                                dialog.dismiss();
                                if (result.getKeyError().equals(AppGeneral.KEY_CODE_ERROR_INVALID_PASSWORD)) {
                                    passwordTextInputLayout.setError("Invalid password");
                                } else {
                                    passwordTextInputLayout.setError("");
                                    if (!result.getKeyError().equals(AppGeneral.KEY_CODE_SUCCESS)) {
                                        Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_PHONE, phoneTextInputLayout.getFinalPhoneNumber());
                                        sendResult(Activity.RESULT_OK);
                                    }
                                }
                            }
                        }, mActivity);
                    }
                }
            }
        });

        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == ProfileUpdatePhoneFragment.REQUEST_COUNTRY) {
            mLocale = (Locale) data.getSerializableExtra(CountryPickerFragment.FRAGMENT_OUTPUT_PARAM_SELECTED_PHONE_COUNTRY);
            updateCurrentCountry();
            //In order to validate if with new country the phone is correct
            final TextInputLayoutAppWidget phoneTextInputLayout = (TextInputLayoutAppWidget) mView.findViewById(R.id.profile_update_phone_TextInputLayoutAppWidget_phone);
            phoneTextInputLayout.setLocale(mLocale);
            phoneTextInputLayout.setText(phoneTextInputLayout.getText());
            phoneTextInputLayout.getEditText().setSelection(phoneTextInputLayout.getText().length());
        }
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_CURRENT_LOCALE, mLocale);
        outState.putParcelable(KEY_CURRENT_USER, mUser);
    }


}

