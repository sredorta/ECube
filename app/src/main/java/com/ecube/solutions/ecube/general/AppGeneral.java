package com.ecube.solutions.ecube.general;

import java.util.Arrays;
import java.util.List;

/**
 * Contains all general settings that are app wide
 */
public class AppGeneral {
    //Variables for Fragment stack handling
    public static final String KEY_FRAGMENT_STACK_LEVEL_0 = "key.fragment.level.0"; //Stack level 0
    public static final String KEY_FRAGMENT_STACK_LEVEL_1 = "key.fragment.level.1"; //Stack level 1
    public static final String KEY_FRAGMENT_STACK_LEVEL_UNDEFINED = "key.fragment.level.undefined"; //Stack level 1

    public static final List<String> SupportedCountries = Arrays.asList("FR", "ES" , "IT" , "MC", "AT", "CH", "DE","DK", "FI", "GB", "GI", "GR", "IS", "NL", "NO", "AD");   //Supported countries
    public static final List<String> SupportedLanguages = Arrays.asList("fra", "eng");   //Supported ISO3 languages

    public static final Boolean KEY_ENCRYPTION_ENABLED = false;

    //Error codes comming from the server
    public static final String KEY_CODE_SUCCESS = "success";
    public static final String KEY_CODE_ERROR_UNKNOWN = "error.unknown";
    public static final String KEY_CODE_ERROR_DATABASE = "error.database";
    public static final String KEY_CODE_ERROR_INVALID_PASSWORD = "error.password.invalid";
    public static final String KEY_CODE_ERROR_INVALID_USER = "error.user.invalid";
    public static final String KEY_CODE_ERROR_CONNECTION_ERROR = "error.connection";
}
