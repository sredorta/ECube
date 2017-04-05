package com.ecube.solutions.ecube.authentication.profile.create;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.widgets.TextInputLayoutAppWidget;


/**
 * Created by sredorta on 2/21/2017.
 */
public class ProfileCreateNamesFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileCreateStartFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_INPUT_PARAM_USER_FIRST_NAME = "user.first_name.in";    //String
    public static final String FRAGMENT_INPUT_PARAM_USER_LAST_NAME = "user.last_name.in";
    public static final String FRAGMENT_OUTPUT_PARAM_USER_FIRST_NAME = "user.first_name.out";    //String
    public static final String FRAGMENT_OUTPUT_PARAM_USER_LAST_NAME = "user.last_name.out";

    private String mFirstName;
    private String mLastName;

    // Constructor
    public static ProfileCreateNamesFragment newInstance() {
        return new ProfileCreateNamesFragment();
    }
    // Constructor with input arguments
    public static ProfileCreateNamesFragment newInstance(Bundle data) {
        ProfileCreateNamesFragment fragment = ProfileCreateNamesFragment.newInstance();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mFirstName = (String) getInputParam(ProfileCreateNamesFragment.FRAGMENT_INPUT_PARAM_USER_FIRST_NAME);
        mLastName = (String) getInputParam(ProfileCreateNamesFragment.FRAGMENT_INPUT_PARAM_USER_LAST_NAME);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_create_names_fragment, container, false);
        setCurrentView(v);

        if (DEBUG) Log.i(TAG,"firstName :" + mFirstName);
        final TextInputLayoutAppWidget firstNameTextInputLayout = (TextInputLayoutAppWidget) v.findViewById(R.id.profile_create_names_TextInputLayoutAppWidget_firstName);
        final TextInputLayoutAppWidget lastNameTextInputLayout = (TextInputLayoutAppWidget) v.findViewById(R.id.profile_create_names_TextInputLayoutAppWidget_lastName);

        Button nextButton = (Button) v.findViewById(R.id.profile_create_names_button);

        firstNameTextInputLayout.getEditText().setText(mFirstName);
        lastNameTextInputLayout.getEditText().setText(mLastName);



        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Hide keyboard if exists
                mFirstName = firstNameTextInputLayout.getText();
                mLastName = lastNameTextInputLayout.getText();
                if (firstNameTextInputLayout.isValidInput() && lastNameTextInputLayout.isValidInput()) {
                        putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_FIRST_NAME, mFirstName);
                        putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_LAST_NAME, mLastName);
                        sendResult(Activity.RESULT_OK);
                }
            }
        });
        return v;
    }


}
