package com.ecube.solutions.ecube.abstracts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.general.AppGeneral;

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
 * Created by sredorta on 2/10/2017.
 */
public abstract class FragmentAbstract extends Fragment implements OnBackPressed {
    //Logs
    private final String TAG =  FragmentAbstract.class.getSimpleName();
    private final boolean DEBUG = true;

    //Container where the fragment needs to be expanded
    private @IdRes int mContainer;

    //Animation for transitions
    private @AnimRes int mAnimEnter     = R.anim.enter_from_right;
    private @AnimRes int mAnimExit      = R.anim.exit_fade;
    private @AnimRes int mAnimPopEnter  = R.anim.enter_from_left;
    private @AnimRes int mAnimPopExit   = R.anim.exit_fade;

    protected FragmentActivity mActivity;

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
        mAnimEnter     = R.anim.enter_from_right;
        mAnimExit      = R.anim.exit_fade;
        mAnimPopEnter  = R.anim.enter_from_right;
        mAnimPopExit   = R.anim.exit_fade;
    }

    //To store the activity holding the fragment... and avoid nulls when transaction has not been completed
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (FragmentActivity) getActivity();
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
    public void replaceFragment(Fragment fragment, @Nullable String tag, boolean animation, boolean addToBackStack){
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        if (animation)
                transaction.setCustomAnimations(mAnimEnter, mAnimExit, mAnimPopEnter, mAnimPopExit);
        transaction.replace(mContainer, fragment, tag);
        if (addToBackStack)
               transaction.addToBackStack(tag);

        try {
               transaction.commit();
        } catch (IllegalStateException e) {
                //It means that the activity is gone in rotation also... so we need to wait that activity is back and then commit
                Log.i(TAG, "Exception during transaction commit !!! This fragment should be retained !!!!!!!!!!!!!");
                Log.i(TAG, "Exception : " + e);
        }
        if (DEBUG)
            Log.i(TAG, "Added fragment " + fragment.getClass().getSimpleName() + " with tag:" + tag);

        if (DEBUG) Log.i(TAG, "Current fragment stack count :" + mActivity.getSupportFragmentManager().getBackStackEntryCount());

    }
    //Replace container with new fragment and add to backStack
    public void replaceFragment(Fragment fragment, @Nullable String tag, boolean animation){
        replaceFragment(fragment, tag, animation, true);
    }
    //Replace container with new fragment and add to backStack with animation and default tag
    public void replaceFragment(Fragment fragment){
        replaceFragment(fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_UNDEFINED, true, true);
    }



    //Remove a fragment
    public void removeFragment(Fragment fragment, boolean animation) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
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
