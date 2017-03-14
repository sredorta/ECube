package com.ecube.solutions.ecube.abstracts;

import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.util.Log;

import java.lang.reflect.*;

/**
 * Created by sredorta on 3/13/2017.
 */
public abstract class AsyncTaskAbstract<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements AsyncTaskInterface {
    public AsyncTaskInterface delegate = null;//Call back interface
    private long tStart;
    private long minWaitTime;

    public AsyncTaskAbstract(AsyncTaskInterface<Result> asyncResponse) {
            delegate = asyncResponse;   //Assigning call back interfacethrough constructor
            minWaitTime = 0;         // By default 2000ms of wait time minimal
    }

    //Constructor in case we want a minimum duration in miliseconds
    public AsyncTaskAbstract(AsyncTaskInterface<Result> asyncResponse, long minDuration) {
        delegate = asyncResponse;   //Assigning call back interfacethrough constructor
        minWaitTime = minDuration;         // By default 2000ms of wait time minimal
    }

    @Override
    public void processFinish(Object result) {
        delegate.processFinish((Result) result);
    }
/*   public void processFinish(Result... result) {
        delegate.processFinish(result);
    }*/

    public void processStart() {
        delegate.processStart();
    }

    @Override
    protected Result doInBackground(Params... params) {
        //Make sure that we wait a minimum period
        long tWait = System.currentTimeMillis() - tStart;
        if (tWait < minWaitTime) {
            tWait = minWaitTime - tWait;
            try {
                Thread.currentThread();
                Thread.sleep(tWait);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        tStart = System.currentTimeMillis();
        delegate.processStart();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Result result) {
        delegate.processFinish(result);
        super.onPostExecute(result);
    }
}
