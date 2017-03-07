package com.ecube.solutions.ecube;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;

import com.ecube.solutions.ecube.abstracts.ActivityAbstract;
import com.ecube.solutions.ecube.general.AppGeneral;

public class MainActivity extends ActivityAbstract {
    //Logs
    private final String TAG = this.getClass().getSimpleName();
    private final boolean DEBUG = true;

    private Fragment mFragmentDispatcher;

    public Fragment createFragment() {
        return MainFragment.newInstance();
    }


    @Override
    public void onBackPressed() {

        //If our visible fragment is LEVEL_1 then we ask for exit or no
        Fragment myFragment = getVisibleFragment();
        if (myFragment.getTag().equals(AppGeneral.KEY_FRAGMENT_STACK_LEVEL_1)) {
            //Ask for confirmation first
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit")
                    .setTitle("Exit application");
            builder.setCancelable(true);
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.finish();
                    dialogInterface.cancel();

                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.create();
            builder.show();

        } else {
            super.onBackPressed();
        }
    }




}
