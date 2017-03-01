package com.ecube.solutions.ecube;

import android.support.v4.app.Fragment;

import com.ecube.solutions.ecube.abstracts.ActivityAbstract;
import com.ecube.solutions.ecube.authentication.profile.create.ProfileCreateStartFragment;

public class MainActivity extends ActivityAbstract {
    //Logs
    private final String TAG = this.getClass().getSimpleName();
    private final boolean DEBUG = true;

    public Fragment createFragment() {
        return MainFragment.newInstance();
    }

/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    */
}
