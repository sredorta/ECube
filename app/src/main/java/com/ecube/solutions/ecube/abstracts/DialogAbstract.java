package com.ecube.solutions.ecube.abstracts;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.ecube.solutions.ecube.R;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sredorta on 2/28/2017.
 */
public abstract class DialogAbstract extends DialogFragment implements OnBackPressed {
    //Logs
    private final String TAG =  DialogAbstract.class.getSimpleName();
    private final boolean DEBUG = true;

    //Container where the fragment needs to be expanded
    private @IdRes
    int mContainer;

    //Animation for transitions
    private @AnimRes
    int mAnimEnter     = R.anim.enter_from_right;
    private @AnimRes int mAnimExit      = R.anim.exit_fade;
    private @AnimRes int mAnimPopEnter  = R.anim.enter_from_left;
    private @AnimRes int mAnimPopExit   = R.anim.exit_fade;

    //Define if addToBackStack is required
    private boolean mAddToBackStack;
    protected FragmentActivity mActivity;

    //Save of dialog for accessing without null during transactions
    public Dialog mDialog;

    //Map for parsing input parameters
    private Map<String,Object> inputParams;

    //Map for parsing output parameters
    private Map<String,Object> outputParams;

    //View for access
    protected View mView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize all input arguments
        defineInputParams();
        defineOutputParams();
        getInputArgs();
        //Defaults
        mContainer = R.id.fragment_container;
        mAddToBackStack = true;
        mAnimEnter     = R.anim.enter_from_right;
        mAnimExit      = R.anim.exit_fade;
        mAnimPopEnter  = R.anim.enter_from_left;
        mAnimPopExit   = R.anim.exit_fade;
    }


    @Override
    public void dismiss() {
        //We need to access mDialog in case transaction is not completed, otherwise it would be null
        if (getFragmentManager() != null) super.dismiss();
        else
          mDialog.dismiss();
    }

    //To store the activity holding the fragment... and avoid nulls when transaction has not been completed
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i("SERGI", "onAttach.. setting mDialog");
        mDialog = getDialog();
        mActivity = (FragmentActivity) getActivity();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mDialog = getDialog(); //Save mDialog
        mView = v;
        Log.i("mView", "Saved at onCreateView !");
        return v;
    }

    @Override
    public void onBackPressed() {
        if (DEBUG) Log.i("SERGI", "OnBackPressed was done, and we are now sending RESULT_CANCELED");
        sendResult(Activity.RESULT_CANCELED);
        //  removeFragment(this, true);
    }

    private void putInputParam( String key, Object value ) {
        inputParams.put( key, value );
    }


    //Get the value of an input parameter (needs to be casted to right type)
    public Object getInputParam( String key ) {
        return inputParams.get(key);
    }


    private List<Field> getAllModelFields(Class aClass) {
        List<Field> fields = new ArrayList<>();
        do {
            Collections.addAll(fields, aClass.getDeclaredFields());
            aClass = aClass.getSuperclass();
        } while (aClass != null);
        return fields;
    }

    //We initialize the inputParams HashMap with empty Strings just in case
    public void defineInputParams() {
        Pattern p = Pattern.compile("^FRAGMENT_INPUT_PARAM.*");
        Matcher m;

        List<Field> fields = getAllModelFields(getClass());
        Map<String,Object> temp = new HashMap<String,Object>();
        for (Field field : fields) {
            m = p.matcher(field.getName());
            if (m.matches()) {
                try {
                    temp.put(field.get(field.getName()).toString(), null);
                } catch (IllegalAccessException e) {
                    Log.i(TAG, "Caught exception: " + e);
                }
            }
        }
        inputParams = temp;
    }

    //We initialize the inputParams HashMap with empty Strings just in case
    public void defineOutputParams() {
        Pattern p = Pattern.compile("^FRAGMENT_OUTPUT_PARAM.*");
        Matcher m;

        List<Field> fields = getAllModelFields(getClass());
        Map<String,Object> temp = new HashMap<String,Object>();
        for (Field field : fields) {
            m = p.matcher(field.getName());
            if (m.matches()) {
                try {
                    temp.put(field.get(field.getName()).toString(), new String(""));
                } catch (IllegalAccessException e) {
                    Log.i(TAG, "Caught exception: " + e);
                }
            }
        }
        outputParams = temp;
    }

    //Sets the parameter as response
    public void putOutputParam( String key, Object value ) {
        outputParams.put( key, value );
    }


    //Send result to the calling Fragment
    public void sendResult(int resultCode) {
        if(getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        for (String key : outputParams.keySet()) {
            Object value = outputParams.get(key);
            if (DEBUG) Log.i(TAG, "Sending argument : " + key + " : " + value.toString());
            try {
                intent.putExtra(key, (Serializable) value);
            } catch (java.lang.ClassCastException e) {
                if (DEBUG) Log.i(TAG, "Sending it as parcelable !");
                intent.putExtra(key, (Parcelable) value);
            }
        }
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode, intent);
    }

    //Parsing of input arguments and setting inputParams
    public void getInputArgs() {
        if (getArguments() != null) {
            Set<String> keys = getArguments().keySet();
            for (String key : keys) {
                Object o = getArguments().get(key);
                putInputParam(key, o);
                if (o != null) if (DEBUG) Log.i(TAG, "Got input argument : " + key + " : " + o.toString());
            }
        }
    }

    //Sets the fragment container
    public void setContainer(@IdRes int container) {
        if (DEBUG) Log.i(TAG, "Set container to another id");
        mContainer = container;
    }

    public void setAddToBackStack(boolean add) {
        mAddToBackStack = add;
    }


    //Sets the fragment animations
    protected void setAnimations(@AnimRes int enter, @AnimRes int exit, @AnimRes int popenter, @AnimRes int popexit) {
        mAnimEnter = enter;
        mAnimExit = exit;
        mAnimPopEnter = popenter;
        mAnimPopExit = exit;
    }

    //Setter for the current view
    protected void setCurrentView(View view) {
        mView = view;
    }

    //Getter for the current view
    protected View getCurrentView() { return mView;}


    //Replace container with new fragment
    public void replaceFragment(Fragment fragment, String tag, boolean animation){
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        if (animation)
            transaction.setCustomAnimations(mAnimEnter, mAnimExit, mAnimPopEnter, mAnimPopExit);
        transaction.replace(mContainer, fragment);
        if (mAddToBackStack)
            transaction.addToBackStack(tag);
        transaction.commit();

    }

    //Remove a fragment
    public void removeFragment(Fragment fragment, boolean animation) {
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        if (animation)
            transaction.setCustomAnimations(mAnimEnter, mAnimExit, mAnimPopEnter, mAnimPopExit);
        transaction.remove(fragment);
        transaction.commit();
    }




    //Hide input keyboard
    public void hideInputKeyBoard() {
        // Check if no view has focus:
        if (mActivity != null) {
            View view = mActivity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

}
