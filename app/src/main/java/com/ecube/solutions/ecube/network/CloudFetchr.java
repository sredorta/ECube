package com.ecube.solutions.ecube.network;


import android.net.Uri;
import android.util.Log;

import com.ecube.solutions.ecube.authentication.authenticator.AccountAuthenticator;
import com.ecube.solutions.ecube.general.AppGeneral;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by sredorta on 11/24/2016.
 */


public class CloudFetchr {
    //Logs
    private static final String TAG = CloudFetchr.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static final String URI_BASE_GOOGLE = "http://clients3.google.com/generate_204";    // Only required to check if internet is available
    private static final String PHP_CONNECTION_CHECK = "locker.connection.check.php";           // Params required : none
    private static final String PHP_USER_REMOVE = "user.remove.php";                            // Params required : email,language
    private static final String PHP_USER_SIGNIN = "user.signin.php";                            // Params required : user,password,user_table and returns token
    private static final String PHP_USER_SIGNUP = "user.signup.php";                            // Params required : user,password,email,user_table and returns token
    private static final String PHP_USER_CHECK_PASSWORD = "user.check_password.php";            //Params required: id, token,password...
    private static final String PHP_USER_CHANGE_PASSWORD = "user.change_password.php";          //Params required: id, token,password, new pass,...
    private static final String PHP_USER_CHANGE_EMAIL = "user.change_email.php";                //Params required: id, token,password, new pass,...
    private static final String PHP_USER_CHANGE_PHONE = "user.change_phone.php";                //Params required: id, token,password, new pass,...
    private static final String PHP_USER_CHANGE_NAMES = "user.change_names.php";                //Params required: id, lang, firstName, lastName,...
    private static final String PHP_USER_CHANGE_AVATAR = "user.change_avatar.php";                //Params required: id, lang, firstName, lastName,...

    private static final String PHP_USER_RESET_PASSWORD = "user.reset_password.php";            //Params required: email,language...

    private static final String PHP_USER_PASSWORD = "locker.users.setpassword.php";                    // Params required : user,password,email,user_table and returns token
    private static final String PHP_USER_TOKEN = "locker.users.checktoken.php";                 // Params required : email,token and returns if token is valid or not
    private static final String PHP_USER_TEST = "test.php";
    private String SEND_METHOD = "POST";                                                        // POST or GET method

    public static final String URI_BASE_DEBUG = "http://10.0.2.2/EcubeServer/api/";                //localhost controlled by prefs
    public static final String URI_BASE_PROD = "http://ibikestation.000webhostapp.com/api/";        //realserver controlled by prefs
    public static String URI_BASE = URI_BASE_PROD;


    //We try to see if we can connect to google for example
    public  Boolean isNetworkConnected() {
        URL url = null;
        Uri ENDPOINT = Uri
                .parse(URI_BASE_GOOGLE)
                .buildUpon()
                .build();
        Uri.Builder uriBuilder = ENDPOINT.buildUpon();
        try {
            url = new URL(uriBuilder.toString());
        } catch(MalformedURLException e) {
            Log.i(TAG, "Malformed URL !");
        }
        HttpURLConnection connection;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Android");
            connection.setRequestProperty("Connection", "close");
            connection.setConnectTimeout(2000);
            connection.connect();
            if (connection.getResponseCode() == 204 && connection.getContentLength() == 0) {
                if (DEBUG) Log.i(TAG, "Connected !");
                return true;
            }
            Log.i(TAG, "Not connected !");
            return false;
        } catch (IOException e) {
            //Network not connected
            Log.i(TAG, "Caught IOE : "+ e);
            return false;
        }
    }



    //Build http string besed on method and query
    private URL buildUrl(String Action,HashMap<String, String> params) {
        Uri ENDPOINT = Uri
                .parse(URI_BASE + Action)
                .buildUpon()
                .build();

        URL url = null;
        Uri.Builder uriBuilder = ENDPOINT.buildUpon();
        if (this.SEND_METHOD.equals("GET")) {
            //Add GET query parameters using the HashMap in the URL
            try {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    uriBuilder.appendQueryParameter(URLEncoder.encode(entry.getKey(), "utf-8"), URLEncoder.encode(entry.getValue(), "utf-8"));
                }
            } catch (UnsupportedEncodingException e) {
                // do nothing
            }
        }
        String result = uriBuilder.build().toString();
        try {
            url = new URL(result);
        } catch(MalformedURLException e) {
            //Do nothing
        }
        Log.i(TAG, "Final URL :" + url.toString());
        return url;
    }

    //Gets the data from the server and aditionally sends POST parameters if SEND_METHOD is set to POST
    private byte[] getURLBytes(URL url,HashMap<String,String> parametersPOST) throws IOException {

        HttpURLConnection connection;
        OutputStreamWriter request = null;
        byte[] response = null;
        JsonItem json = new JsonItem();  //json answer in case network not available


        json.setSuccess(false);
        json.setResult(false);
        try {
            connection = (HttpURLConnection) url.openConnection();
            //Required to enable input stream, otherwise we get EOF (When using POST DoOutput is required
            connection.setDoInput(true);
            if (this.SEND_METHOD.equals("POST")) connection.setDoOutput(true);
            connection.setReadTimeout(4000);
            connection.setConnectTimeout(4000);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            //connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestMethod(this.SEND_METHOD);
            connection.connect();

            //Write the POST parameters
            if (this.SEND_METHOD.equals("POST")) {
                OutputStream os = connection.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
                String postData;
                if (AppGeneral.KEY_ENCRYPTION_ENABLED) {
                    postData = getPostDataStringEncrypted(parametersPOST);
                } else{
                    postData = getPostDataString(parametersPOST);
                }
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();
            }

            switch(connection.getResponseCode())
            {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                    Log.i(TAG, "Timout !");
                    json.setMessage("ERROR: Server timeout !");
                    break;
                case HttpURLConnection.HTTP_UNAVAILABLE:
                    Log.i(TAG, "Server not available");
                    json.setMessage("ERROR: Server not available !");
                    break;
                default:
                    Log.i(TAG, "Not connected  ! : " + connection.getResponseMessage());
                    json.setMessage("ERROR: Not connected to server !");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in            =  connection.getInputStream();
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer))>0) {
                out.write(buffer,0,bytesRead);
            }
            out.close();

            // Response from server after login process will be stored in response variable.
            response = out.toByteArray();
            Log.i(TAG, "START SERVER SAYS: ");
            if (out != null)
               Log.i(TAG, out.toString().replaceAll("<br>" , "\n"));
            Log.i(TAG, "END SERVER SAYS: ");
            // You can perform UI operations here
            //Log.i(TAG, "Message from Server: \n" + response);
        } catch (IOException e) {
            // Error
            Log.i(TAG, "Caught exception :" + e);
        }
        // In case that response is null we output the json we have created
        if (response == null) {
            Log.i(TAG, "Error during access to server");
            response = json.encodeJSON().getBytes();
        }
        //Log.i(TAG, new String(response));
        return response;
    }

    //Get string data from URL
    public String getURLString(URL url,HashMap<String,String> parametersPOST) throws IOException {
        byte[] test =  getURLBytes(url,parametersPOST);
        if (test == null) {
            return "";
        } else {
            return new String(test);
        }
    }

    // Converts a HashMap of string parameter pairs into a string for POST send
    private String getPostDataStringEncrypted(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append(";");

            result.append("$_POST['" + entry.getKey() +"']");
            result.append("=");
            result.append("\"" + entry.getValue() + "\"");
        }
        if (result.toString().length()==0) {
            if (DEBUG) Log.i(TAG, "POST HEADER :");
            return "";
        } else {
            result.append(";");
            if (DEBUG) Log.i(TAG, "POST HEADER :" + result.toString());
            //Encrypt the parameters
            Encryption cipher = new Encryption();
            String encryptedStr = new String();
            try {
                encryptedStr = Encryption.bytesToHex(cipher.encrypt(result.toString()));
            } catch (Exception e) {
                Log.i(TAG, "Caught exception: " + e);
            }
            String resultEncrypted = "mydata=" + encryptedStr;
            if (DEBUG) Log.i(TAG, "POST HEADER encrypted: " + resultEncrypted);
            return resultEncrypted;
        }
    }

    // Converts a HashMap of string parameter pairs into a string for POST send
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            Log.i(TAG, "Entry:" + entry.getKey());
            Log.i(TAG,"Value:" + entry.getValue());
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        //For debug we replace &avatar=.*& by &avatar=<data>&
        //String tmp = result.toString().replaceFirst("&avatar=.*&", "&avatar=<data>&");
        if (DEBUG) Log.i(TAG, "POST HEADER :" + result.toString());
        return result.toString();
    }





    // Sends PHP request and returns JSON object
    private JsonItem getJSON(URL url,HashMap<String,String> parametersPOST){
        JsonItem item = new JsonItem();
        //Get the input from the network
        String network;
        String jsonString;
        try {
            network = getURLString(url, parametersPOST);
        } catch (IOException ioe) {
            item.setSuccess(false);
            item.setResult(false);
            item.setMessage("ERROR: Failed to fetch input !");
            Log.i(TAG, "Falied to fetch input ! :" + ioe);
            return item;
        }

        if (AppGeneral.KEY_ENCRYPTION_ENABLED) {
            //Decrypt the input
            String netStr = network;
            netStr = Encryption.hexToString(netStr);
            Log.i(TAG, "Encrypted JSON: " + netStr);

            Encryption mcrypt = new Encryption();
            try {
                jsonString = new String(mcrypt.decrypt(netStr));
            } catch (Exception e) {
                Log.i(TAG, "Caught decryption exception: " + e);
                item.setSuccess(false);
                item.setResult(false);
                item.setMessage("ERROR: Connection to server failed !");
                return item;
            }
        } else {
            jsonString = network;
        }
        //Parse the JSON string
        try {
            JSONObject jsonBody = new JSONObject(jsonString);
            Log.i (TAG, "Final JSON:\n" + jsonBody.toString(1)); // We want to see always this message for now
            item = JsonItem.parseJSON(jsonBody.toString());
        } catch (JSONException je) {
            Log.i(TAG, "Failed to parse JSON :" + je);
            item.setSuccess(false);
            item.setResult(false);
            item.setMessage("ERROR: Connection to server failed !");
        }
        return item;
    }



/*******************************************************************************************/



    public Boolean isCloudConnected() {
        //Define the POST/GET parameters in a HashMap
        this.SEND_METHOD="POST";
        HashMap<String, String> parameters = new HashMap<>();

        URL url = buildUrl(PHP_CONNECTION_CHECK,parameters);
        JsonItem networkAnswer = getJSON(url,parameters);
        return (networkAnswer.getResult());
    }



    //Checks if the user is registered and returns all details of the answer with full JsonItem
    public JsonItem userSignUp(String phone, String email, String password, String firstName, String lastName, String avatar, String type, String auth_type, String lang) {
        this.SEND_METHOD="POST";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("phone", phone);
        parameters.put("email", email);
        parameters.put("first_name", firstName);
        parameters.put("last_name", lastName);
        parameters.put("avatar", avatar);
        parameters.put("password", password);
        parameters.put("account_type", type);
        parameters.put("account_access", auth_type);
        parameters.put("language", lang);

        URL url = buildUrl(PHP_USER_SIGNUP,parameters);
        return getJSON(url,parameters);
    }




    //Checks if the user is registered and returns all Details
    public JsonItem userSignIn(String userID, String userEmail, String userPhone,  String password, String type, String auth_type, String lang) {
        this.SEND_METHOD="POST";
        HashMap<String, String> parameters = new HashMap<>();
        if (userID != null)
            parameters.put("id", userID);
        if (userEmail != null)
            parameters.put("email", userEmail);
        if (userPhone != null)
            parameters.put("phone", userPhone);
        if (auth_type == null)
            auth_type = AccountAuthenticator.AUTHTOKEN_TYPE_STANDARD;

        parameters.put("password", password);
        parameters.put("account_type", type);
        parameters.put("account_access", auth_type);
        parameters.put("language", lang);

        URL url = buildUrl(PHP_USER_SIGNIN,parameters);
        return getJSON(url,parameters);
    }

    //Checks if the user is registered and returns all Details
    public JsonItem userCheckPassword(String userID, String password, String type, String auth_type, String lang) {
        this.SEND_METHOD="POST";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("id", userID);
        parameters.put("password", password);
        parameters.put("account_type", type);
        parameters.put("account_access", auth_type);
        parameters.put("language", lang);

        URL url = buildUrl(PHP_USER_CHECK_PASSWORD,parameters);
        return getJSON(url,parameters);
    }
    //Checks if the user is registered and returns all Details
    public JsonItem userChangePassword(String userID, String password, String newPassword, String type, String auth_type, String lang) {
        this.SEND_METHOD="POST";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("id", userID);
        parameters.put("password", password);
        parameters.put("new_password", newPassword);
        parameters.put("account_type", type);
        parameters.put("account_access", auth_type);
        parameters.put("language", lang);

        URL url = buildUrl(PHP_USER_CHANGE_PASSWORD,parameters);
        return getJSON(url,parameters);
    }

    //Checks if the user is registered and returns all Details
    public JsonItem userChangeEmail(String userID, String password, String newEmail, String type, String auth_type, String lang) {
        this.SEND_METHOD="POST";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("id", userID);
        parameters.put("password", password);
        parameters.put("new_email", newEmail);
        parameters.put("account_type", type);
        parameters.put("account_access", auth_type);
        parameters.put("language", lang);

        URL url = buildUrl(PHP_USER_CHANGE_EMAIL,parameters);
        return getJSON(url,parameters);
    }
    //Checks if the user is registered and returns all Details
    public JsonItem userChangePhone(String userID, String password, String newPhone, String type, String auth_type, String lang) {
        this.SEND_METHOD="POST";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("id", userID);
        parameters.put("password", password);
        parameters.put("new_phone", newPhone);
        parameters.put("account_type", type);
        parameters.put("account_access", auth_type);
        parameters.put("language", lang);

        URL url = buildUrl(PHP_USER_CHANGE_PHONE,parameters);
        return getJSON(url,parameters);
    }

    //Checks if the user is registered and returns all Details
    public JsonItem userChangeNames(String userID, String lang, String firstName, String lastName) {
        this.SEND_METHOD="POST";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("id", userID);
        parameters.put("language", lang);
        parameters.put("first_name", firstName);
        parameters.put("last_name", lastName);

        URL url = buildUrl(PHP_USER_CHANGE_NAMES,parameters);
        return getJSON(url,parameters);
    }

    //Checks if the user is registered and returns all Details
    public JsonItem userChangeAvatar(String userID, String lang, String avatar) {
        this.SEND_METHOD="POST";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("id", userID);
        parameters.put("language", lang);
        parameters.put("avatar", avatar);

        URL url = buildUrl(PHP_USER_CHANGE_AVATAR,parameters);
        return getJSON(url,parameters);
    }

    //Checks if the user is registered and returns all Details
    public JsonItem userResetPassword(String userEmail, String lang) {
        this.SEND_METHOD="POST";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("email", userEmail);
        parameters.put("language", lang);

        URL url = buildUrl(PHP_USER_RESET_PASSWORD, parameters);
        return getJSON(url,parameters);
    }

    //Remove a user
    public JsonItem userRemove(String userEmail, String lang) {
        this.SEND_METHOD="POST";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("email", userEmail);
        parameters.put("language", lang);

        URL url = buildUrl(PHP_USER_REMOVE,parameters);
        return getJSON(url,parameters);
    }






    //Checks if the user is registered and returns all (non critical) details of the answer with full JsonItem
    public JsonItem isTokenValid(String account, String token, String table) {
        this.SEND_METHOD="POST";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("account", account);
        parameters.put("token", token);
        parameters.put("table_users", table);

        URL url = buildUrl(PHP_USER_TOKEN,parameters);
        return getJSON(url,parameters);
    }


}

