package com.ecube.solutions.ecube.abstracts;
import android.database.CursorJoiner;
import android.os.AsyncTask;



/**
 * Created by sredorta on 3/13/2017.
 */
public interface AsyncTaskInterface<T> {
    void processFinish(T result);
    void processStart();
}
