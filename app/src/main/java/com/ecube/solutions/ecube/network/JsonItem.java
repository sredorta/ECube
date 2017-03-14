package com.ecube.solutions.ecube.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;


/**
 * JsonItem class:
 *    This class is used to parse Json answers from the Cloud
 */
public class JsonItem {
        @SerializedName("success")
        private String mSuccess = "false";

        @SerializedName("message")
        private String mMessage = "Could not connect to cloud !";

        @SerializedName("result")
        private String mResult = "false";

        @SerializedName("account")
        private String mAccount;

        @SerializedName("key_error")
        private String mKeyError = "nothing";


        public String getAccountDetails() {
            return mAccount;
        }



    public boolean getSuccess() {
            if (mSuccess.equals("1")) {
                mSuccess = "true";
            }
            if (mSuccess.equals("0")) {
                mSuccess = "false";
            }
            return Boolean.parseBoolean(mSuccess);
        }
        public void setSuccess(boolean success) {
            mSuccess = String.valueOf(success);
        }


         public boolean getResult() {
            if (mResult.equals("1")) {
                mResult = "true";
            }
            if (mResult.equals("0")) {
                mResult = "false";
            }
            //We need success to be true to check result
            if (!this.getSuccess()) {
                mResult = "false";
            }
            return Boolean.parseBoolean(mResult);
        }
        public void setResult(boolean result) {
            mResult = String.valueOf(result);
        }

        public String getMessage() {
            return mMessage;
        }

        public void setMessage(String message) {
            mMessage = message;
        }

        public String getKeyError() {
        return mKeyError;
    }

        public void setKeyError(String code) {
        mKeyError = code;
    }


        public static JsonItem parseJSON(String response) {
            Gson gson = new GsonBuilder().create();
            JsonItem answer = gson.fromJson(response, JsonItem.class);
            return(answer);
        }

        public String encodeJSON() {
            Gson gson = new GsonBuilder().create();
            return gson.toJson(this);
        }
}
