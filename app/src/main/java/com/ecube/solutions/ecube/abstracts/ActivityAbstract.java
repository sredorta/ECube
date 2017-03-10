package com.ecube.solutions.ecube.abstracts;

import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ecube.solutions.ecube.R;
import com.ecube.solutions.ecube.general.AppGeneral;

import java.util.List;


/**
 * Created by sredorta on 9/19/2016.
 */
public abstract class ActivityAbstract extends AppCompatActivity {
    //Logs
    private final String TAG =  ActivityAbstract.class.getSimpleName();
    private final boolean DEBUG = true;
    //Container where the fragment needs to be expanded
    private @LayoutRes  int mContentView    = R.layout.activity_fragment;
    private @IdRes      int mContainer      = R.id.fragment_container;

    //Animation for transitions
    private @AnimRes int mAnimEnter     = R.anim.enter_from_right;
    private @AnimRes int mAnimExit      = R.anim.exit_fade;
    private @AnimRes int mAnimPopEnter  = R.anim.enter_from_left;
    private @AnimRes int mAnimPopExit   = R.anim.exit_fade;


    protected abstract Fragment createFragment();
    Fragment fragment;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(mContentView);
        FragmentManager fm = getSupportFragmentManager();
        fragment = fm.findFragmentById(mContainer);
        if (fragment == null) {
            fragment = createFragment();
            replaceFragment(fragment, AppGeneral.KEY_FRAGMENT_STACK_LEVEL_0,true,true);
        }

    }

    //Replace container with new fragment
    public void replaceFragment(Fragment fragment, @Nullable String tag, boolean animation, boolean addToBackStack){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animation)
            transaction.setCustomAnimations(mAnimEnter, mAnimExit, mAnimPopEnter, mAnimPopExit);
        transaction.replace(mContainer, fragment, tag);
        if (addToBackStack)
            transaction.addToBackStack(tag);
        if (DEBUG) Log.i(TAG, "Added fragment with tag:" + tag);

        transaction.commit();
    }
    //Replace container with new fragment and add to backStack
    public void replaceFragment(Fragment fragment, @Nullable String tag, boolean animation){
        replaceFragment(fragment, tag, animation, true);
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
        transaction.setCustomAnimations(mAnimEnter,mAnimExit,mAnimPopEnter,mAnimPopExit);
        transaction.remove(fragment);
        transaction.commit();
        super.finish();
    }

    //Returns current visible fragment
    public <F extends Fragment> F getVisibleFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible()) {
                    return (F) fragment;
                }
            }
        }
        return null;
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


}
