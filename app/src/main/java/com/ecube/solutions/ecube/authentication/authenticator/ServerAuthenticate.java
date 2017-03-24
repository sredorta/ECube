package com.ecube.solutions.ecube.authentication.authenticator;

import com.ecube.solutions.ecube.authentication.profile.dao.User;
import com.ecube.solutions.ecube.network.JsonItem;

/**
 * Defines the interface with the server for authentication
 */
public interface ServerAuthenticate {
    public JsonItem isTokenValid(User user);
    public Boolean userRemove(User user);
    public JsonItem userSignUp(User user);
    public JsonItem userSignIn(User user);
    public JsonItem userCheckPassword(User user);
    public JsonItem userChangePassword(User user, String newPassword);
    public JsonItem userChangeEmail(User user, String newEmail);
    public JsonItem userChangePhone(User user, String newPhone);
}