package com.ecube.solutions.ecube.authentication.profile.create;

import android.app.Activity;
import android.content.res.ColorStateList;
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
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.abstracts.FragmentAbstract;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.helpers.TextInputLayoutHelper;
import com.ecube.solutions.ecube.widgets.TextInputLayoutAppWidget;


/**
 * Created by sredorta on 2/23/2017.
 */
public class ProfileCreatePasswordFragment extends FragmentAbstract {
    //Logs
    private static final String TAG = ProfileCreatePasswordFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Fragment arguments
    public static final String FRAGMENT_OUTPUT_PARAM_USER_PASSWORD = "user.password.out";    //String


    // Constructor
    public static ProfileCreatePasswordFragment newInstance() {
        return new ProfileCreatePasswordFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_create_password_fragment, container, false);
        mView =v;

        final TextInputLayoutAppWidget passwordTextInputLayout = (TextInputLayoutAppWidget) v.findViewById(R.id.profile_create_password_TextInputLayoutAppWidget_password);
        final Button nextButton = (Button) v.findViewById(R.id.profile_create_password_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInputKeyBoard();
                if (passwordTextInputLayout.isValidInput()) {
                    putOutputParam(FRAGMENT_OUTPUT_PARAM_USER_PASSWORD, passwordTextInputLayout.getText());
                    sendResult(Activity.RESULT_OK);
                }
            }
        });
        return v;
    }

}
