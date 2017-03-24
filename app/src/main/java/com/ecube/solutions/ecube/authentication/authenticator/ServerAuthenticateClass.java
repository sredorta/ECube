package com.ecube.solutions.ecube.authentication.authenticator;

import android.util.Log;

import com.ecube.solutions.ecube.authentication.profile.dao.Internationalization;
import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.network.CloudFetchr;
import com.ecube.solutions.ecube.network.Encryption;
import com.ecube.solutions.ecube.network.JsonItem;

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
    public Boolean userRemove(User user) {
        Boolean isUserRemoved = new CloudFetchr().userRemove(user.getId(),"users");
        return isUserRemoved;
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
        //TODO fix this... I don't know why language is null !
        user.setLanguage("fra");
        JsonItem item =  new CloudFetchr().userCheckPassword(user.getId(), user.getPassword(),
                AccountAuthenticator.ACCOUNT_TYPE, user.getAccountAccess(), user.getLanguage());

        return item;
    }
    //Send to the Server id,token and password (sha1) + newPassword (sha1) and update if matches with server
    @Override
    public JsonItem userChangePassword(User user, String newPassword) {
        //TODO fix this... I don't know why language is null !
        user.setLanguage("fra");
        JsonItem item =  new CloudFetchr().userChangePassword(user.getId(), user.getPassword(), newPassword,
                AccountAuthenticator.ACCOUNT_TYPE, user.getAccountAccess(), user.getLanguage());

        return item;
    }

    //Send to the Server id,token and password (sha1) + newPassword (sha1) and update if matches with server
    @Override
    public JsonItem userChangeEmail(User user, String newEmail) {
        //TODO fix this... I don't know why language is null !
        user.setLanguage("fra");
        JsonItem item =  new CloudFetchr().userChangeEmail(user.getId(), user.getPassword(), newEmail,
                AccountAuthenticator.ACCOUNT_TYPE, user.getAccountAccess(), user.getLanguage());

        return item;
    }
    //Send to the Server id,token and password (sha1) phone and update if matches with server
    @Override
    public JsonItem userChangePhone(User user, String newPhone) {
        //TODO fix this... I don't know why language is null !
        user.setLanguage("fra");
        JsonItem item =  new CloudFetchr().userChangePhone(user.getId(), user.getPassword(), newPhone,
                AccountAuthenticator.ACCOUNT_TYPE, user.getAccountAccess(), user.getLanguage());

        return item;
    }



    /*
    //Send to the server all fields and create a new user and get the token


    //Send to the server all fields and create a new user and get the token
    @Override
    public Boolean userSetPassword(String account, String password, String authType) {
        Boolean result = new CloudFetchr().userSetPassword(account,password,"users");
        return result;
    }


    //Send to the Server username and password and get corresponding token
    @Override
    public JsonItem userSignIn(String user, String password, String authType) {
        return new CloudFetchr().userSignIn(user,password, "users");

    }



    @Override
    public Boolean userRemove(String account, String authType) {
        Boolean isUserRemoved = new CloudFetchr().userRemove(account,"users");
        return isUserRemoved;
    }
*/
}
