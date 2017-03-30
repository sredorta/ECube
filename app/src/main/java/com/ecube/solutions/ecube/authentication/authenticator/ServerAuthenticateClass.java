package com.ecube.solutions.ecube.authentication.authenticator;

import android.util.Log;

import com.ecube.solutions.ecube.authentication.profile.dao.Internationalization;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.network.CloudFetchr;
import com.ecube.solutions.ecube.network.Encryption;
import com.ecube.solutions.ecube.network.JsonItem;

import java.util.Locale;

import static android.content.ContentValues.TAG;


/**
 * Defines the commands required to request server for authentication
 */
public class ServerAuthenticateClass implements ServerAuthenticate {

    //Send to the Server the user name and the token we have stored in our device and check if the token is valid
    @Override
    public JsonItem isTokenValid(User user) {
        JsonItem item = new CloudFetchr().isTokenValid(user.getId(),user.getToken(),"users");
        //Here in the case of account details more extensive we need to parse jsonobject with AccountDetails.parseJson with field item.account
        return item;
    }



    @Override
    public JsonItem userRemove(User user) {
        JsonItem item = new CloudFetchr().userRemove(user.getEmail(), user.getLanguage());
        return item;
    }

    @Override
    public JsonItem userSignUp(User user) {
        user.print("Before JsonParse");
        JsonItem item = new CloudFetchr().userSignUp(
                user.getPhone(),
                user.getEmail(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatar(),
                AccountAuthenticator.ACCOUNT_TYPE,
                user.getAccountAccess(),
                user.getLanguage());
        if (item.getAccountDetails() != null) {
            User myUser = User.parseJSON(item.getAccountDetails());
            //Update the fields that we have got from the server
            user.update(myUser);
        }
        user.print("After Json parse");
        return item;
    }

    //Send to the Server username and password and get corresponding token
    @Override
    public JsonItem userSignIn(User user) {
        user.print("Before userSignIn:");
        String userID = null;
        String userEmail = null;
        String userPhone = null;
        if (user.getId() != null)
            userID = user.getId();
        if (user.getEmail() != null)
                userEmail = user.getEmail();
        if(user.getPhone() != null)
                userPhone = user.getPhone();

        //It could be that we are restoring user from email... so it means that language is not set
        if (user.getLanguage()== null)
             user.setLanguage(Locale.getDefault().getISO3Language());

        user.print("User before signIn :");
        JsonItem item =  new CloudFetchr().userSignIn(userID, userEmail, userPhone, user.getPassword(),
                AccountAuthenticator.ACCOUNT_TYPE, user.getAccountAccess(), user.getLanguage());

        if (item.getAccountDetails() != null) {
            User myUser = User.parseJSON(item.getAccountDetails());
            //Update the fields that we have got from the server
            user.update(myUser);
        }
        user.print("After Json parse");
        return item;
    }

    //Send to the Server id,token and password (sha1) and check if matches with server
    @Override
    public JsonItem userCheckPassword(User user) {
        JsonItem item =  new CloudFetchr().userCheckPassword(user.getId(), user.getPassword(),
                AccountAuthenticator.ACCOUNT_TYPE, user.getAccountAccess(), user.getLanguage());

        return item;
    }
    //Send to the Server id,token and password (sha1) + newPassword (sha1) and update if matches with server
    @Override
    public JsonItem userChangePassword(User user, String newPassword) {
        JsonItem item =  new CloudFetchr().userChangePassword(user.getId(), user.getPassword(), newPassword,
                AccountAuthenticator.ACCOUNT_TYPE, user.getAccountAccess(), user.getLanguage());

        return item;
    }

    //Send to the Server id,token and password (sha1) + newPassword (sha1) and update if matches with server
    @Override
    public JsonItem userChangeEmail(User user, String newEmail) {
        JsonItem item =  new CloudFetchr().userChangeEmail(user.getId(), user.getPassword(), newEmail,
                AccountAuthenticator.ACCOUNT_TYPE, user.getAccountAccess(), user.getLanguage());

        return item;
    }
    //Send to the Server id,token and password (sha1) phone and update if matches with server
    @Override
    public JsonItem userChangePhone(User user, String newPhone) {
        JsonItem item =  new CloudFetchr().userChangePhone(user.getId(), user.getPassword(), newPhone,
                AccountAuthenticator.ACCOUNT_TYPE, user.getAccountAccess(), user.getLanguage());

        return item;
    }

    //Send to the Server id,token and password (sha1) phone and update if matches with server
    @Override
    public JsonItem userChangeNames(User user) {
        JsonItem item =  new CloudFetchr().userChangeNames(user.getId(), user.getLanguage(), user.getFirstName(), user.getLastName());

        return item;
    }


    //Send to the Server request of reset password and get new password
    @Override
    public JsonItem userResetPassword(User user) {
        JsonItem item =  new CloudFetchr().userResetPassword(user.getEmail(), user.getLanguage());
        if (item.getAccountDetails() != null) {
            User myUser = User.parseJSON(item.getAccountDetails());
            //Update the fields that we have got from the server we will get the new password
            user.update(myUser);
        }
        user.print("After Json parse");
        return item;
    }
}
