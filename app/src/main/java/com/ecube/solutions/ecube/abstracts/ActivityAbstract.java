package com.ecube.solutions.ecube.abstracts;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ecube.solutions.ecube.R;


/**
 * Created by sredorta on 9/19/2016.
 */
public abstract class ActivityAbstract extends AppCompatActivity {
    //Logs
    private final String TAG =  ActivityAbstract.class.getSimpleName();
    private final boolean DEBUG = true;

    protected abstract Fragment createFragment();
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        FragmentManager fm = getSupportFragmentManager();
        fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = createFragment();
            replaceFragmentWithAnimation(fragment,"test");
        }

    }
    public void replaceFragmentWithAnimation(android.support.v4.app.Fragment fragment, String tag){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void finish() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.remove(fragment);
        //transaction.replace(R.id.fragment_container, fragment);
        //transaction..addToBackStack(tag);
        transaction.commit();
        super.finish();
    }

/*
    @Override
    public void onBackPressed() {
        Log.i(TAG, "OnBackPressed: Current number of fragments : " + getSupportFragmentManager().getBackStackEntryCount());
        Fragment currentFragment = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getBackStackEntryCount() - 1);
        if (currentFragment != null)
            if (currentFragment instanceof OnBackPressed) {
                ((OnBackPressed) currentFragment).onBackPressed();
            } else
                super.onBackPressed();
        else
            super.onBackPressed();
    }
*/

/*
    @Override
    public void onBackPressed() {
        Log.i("SERGI", "OnBackPressed was done, and we are now sending RESULT_CANCELED");
        setResult(RESULT_CANCELED);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.remove(fragment);
        transaction.commit();
        super.finish();
    }
*/

}
