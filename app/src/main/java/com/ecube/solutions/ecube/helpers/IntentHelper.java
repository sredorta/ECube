package com.ecube.solutions.ecube.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by sredorta on 3/6/2017.
 */
public class IntentHelper {
    //Logs
    private static final String TAG = IntentHelper.class.getSimpleName();
    private static final boolean DEBUG = true;

    //Dump all extras of an intent
    public static void dumpIntent(Intent i){
        Log.i(TAG, "------- Dumping Intent start");
        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                Log.i(TAG, "[" + key + "=" + bundle.get(key)+"]");
            }
        }
        Log.i(TAG, "------- Dumping Intent end");
    }

}
