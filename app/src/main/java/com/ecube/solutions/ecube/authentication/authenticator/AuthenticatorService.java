package com.ecube.solutions.ecube.authentication.authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by sredorta on 1/12/2017.
 */
public class AuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        AccountAuthenticator authenticator = new AccountAuthenticator(this);
        return authenticator.getIBinder();
    }

}
