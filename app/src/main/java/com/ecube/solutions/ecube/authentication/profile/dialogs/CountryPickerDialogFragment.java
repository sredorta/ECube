package com.ecube.solutions.ecube.authentication.profile.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.DialogAbstract;
import com.ecube.solutions.ecube.authentication.profile.dao.Internationalization;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by sredorta on 2/22/2017.
 */
public class CountryPickerDialogFragment extends DialogAbstract {

    public static final String FRAGMENT_INPUT_PARAM_CURRENT_PHONE_COUNTRY = "user.current.phone.country";    //Locale
    public static final String FRAGMENT_OUTPUT_PARAM_SELECTED_PHONE_COUNTRY = "user.selected.phone.country"; //Locale

    private CountryListAdapter mAdapter;
    private RecyclerView mCountryRecycleView;
    private List<Locale> mLocales = new ArrayList<>();
    private Locale mCurrentLocale;

    // Constructor
    public static CountryPickerDialogFragment newInstance() {
        return new CountryPickerDialogFragment();
    }

    // Constructor with input arguments
    public static CountryPickerDialogFragment newInstance(Bundle data) {
        CountryPickerDialogFragment fragment = CountryPickerDialogFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.profile_create_country_display_list,null);

        //Get input Locale
        mCurrentLocale = (Locale) getInputParam(FRAGMENT_INPUT_PARAM_CURRENT_PHONE_COUNTRY);

        //Set the current Country selected
        final TextView mCurrentCountryTextView = (TextView) v.findViewById(R.id.profile_create_country_display_TextView_country);
        mCurrentCountryTextView.setText(mCurrentLocale.getDisplayCountry());

        //Set the current Country prefix
        final TextView mCurrentPrefixTextView = (TextView) v.findViewById(R.id.profile_create_country_display_TextView_prefix);

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        int prefix =  phoneUtil.getCountryCodeForRegion(mCurrentLocale.getCountry());
        mCurrentPrefixTextView.setText("+" + prefix);


        final ImageView mCurrentCountryImageView = (ImageView) v.findViewById(R.id.profile_create_country_display_ImageView_flag);
        mCurrentCountryImageView.setImageBitmap(Internationalization.getCountryFlagBitmapFromAsset(getContext(),mCurrentLocale));

        final LinearLayout ll = (LinearLayout) v.findViewById(R.id.profile_create_country_display_LinearLayout);
        ll.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.md_green_50));

        //Update the RecycleView
        mCountryRecycleView = (RecyclerView) v.findViewById(R.id.internationalization_country_recycleview);
        mCountryRecycleView.setLayoutManager(new LinearLayoutManager(mActivity));
        updateUI();
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();

    }

    //Updates the recycleview
    private void updateUI() {
        mLocales = Internationalization.getSortedAvailableLocales(getContext(),Locale.FRANCE);
        mAdapter = new CountryListAdapter(mLocales);
        mCountryRecycleView.setAdapter(mAdapter);
    }

    private class CountryListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Locale mLocale;
        private TextView mCountryTextView;
        private TextView mPrefixTextView;
        private ImageView mCountryFlagImageView;


        private CountryListHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mCountryTextView = (TextView) itemView.findViewById(R.id.profile_create_country_display_TextView_country);
            mPrefixTextView = (TextView) itemView.findViewById(R.id.profile_create_country_display_TextView_prefix);
            mCountryFlagImageView = (ImageView) itemView.findViewById(R.id.profile_create_country_display_ImageView_flag);
        }


        @Override
        public void onClick(View view) {
            //We need to update the user with the account data that has been selected
            putOutputParam(CountryPickerDialogFragment.FRAGMENT_OUTPUT_PARAM_SELECTED_PHONE_COUNTRY, mLocale);
            sendResult(Activity.RESULT_OK);
            mDialog.dismiss();
        }


        public void bindAccount(Locale locale, CountryListHolder holder ) {
            mLocale = locale;
            mCountryTextView.setText(locale.getDisplayCountry());
            mCountryFlagImageView.setImageBitmap(Internationalization.getCountryFlagBitmapFromAsset(getContext(),locale));

           PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
           int prefix =  phoneUtil.getCountryCodeForRegion(locale.getCountry());
            mPrefixTextView.setText("+" + prefix);
        }


    }

    private class CountryListAdapter extends RecyclerView.Adapter<CountryListHolder> {

        public CountryListAdapter(List<Locale> locales) {
            mLocales = locales;
        }

        @Override
        public CountryListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
            View view = layoutInflater.inflate(R.layout.profile_create_country_display, parent,false);
            return new CountryListHolder(view);
        }

        @Override
        public void onBindViewHolder(CountryListHolder holder, int position) {
            Locale locale = mLocales.get(position);
            holder.bindAccount(locale,holder);
        }

        @Override
        public int getItemCount() {
            return mLocales.size();
        }

    }


}
