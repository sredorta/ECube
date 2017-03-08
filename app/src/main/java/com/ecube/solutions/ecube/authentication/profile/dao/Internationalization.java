package com.ecube.solutions.ecube.authentication.profile.dao;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.ecube.solutions.ecube.general.AppGeneral;

import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sredorta on 2/22/2017.
 */
public class Internationalization {
    //Logs
    private static final String TAG = Avatar.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static final String FOLDER_COUNTRY_FLAGS = "country_flags";

    private AssetManager mAssets;
    private List<Bitmap> countryFlagsBitmap;

    public Internationalization(Context context) {
        mAssets = context.getAssets();
        //loadCountryFlags();
    }

    public static Bitmap getFlagBitmapFromAsset(String filePath, Context context, Locale country) {
        AssetManager assetManager = context.getAssets();
        String folderPath;

        Pattern r;
        Matcher m;
        r = Pattern.compile("/.*");
        m = r.matcher(filePath);
        folderPath = m.replaceAll("");
        if (DEBUG) Log.i(TAG, "Folder path is :" + folderPath);

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }
        if (bitmap == null) {
            filePath = folderPath + "/Default.png";
            try {
                istr = assetManager.open(filePath);
                bitmap = BitmapFactory.decodeStream(istr);
            } catch (IOException e) {
                // handle exception
            }
        }

        return bitmap;
    }


    public static Bitmap getCountryFlagBitmapFromAsset(Context context, Locale country) {
        AssetManager assetManager = context.getAssets();
        String filePath;

        //Our assets are written with country names in english so we need to get the country name in english
        filePath = FOLDER_COUNTRY_FLAGS + "/" + country.getDisplayCountry(Locale.US).toString() + ".png";
        filePath = filePath.replace(" ","_");
        return getFlagBitmapFromAsset(filePath,context,country);
    }

    /*
    public static Bitmap getLanguageFlagBitmapFromAsset(Context context, Locale country) {
        AssetManager assetManager = context.getAssets();
        String filePath;

        //Our assets are written with country names in english so we need to get the country name in english
        filePath = FOLDER_LANGUAGE_FLAGS + "/" + country.getDisplayLanguage(Locale.US).toString() + ".png";
        filePath = filePath.replace(" ","_");
        Logs.i("Looking for : " + filePath);
        return getFlagBitmapFromAsset(filePath,context,country);
    }
*/




    public static List<Locale> getSortedAvailableLocales(Context context, Locale country) {
        List<Locale> mLocales = new ArrayList<>();

        for (String cnt : Locale.getISOCountries()) {
//            Log.i(TAG, "ISO : " + cnt + " Name : " + (new Locale(Locale.FRENCH.getLanguage(),cnt)).getDisplayCountry());
            if (AppGeneral.SupportedCountries.contains(cnt)) {
                mLocales.add(new Locale("",cnt));
                Log.i(TAG, "Adding locale : " + cnt);
            }
        }

        final Collator collator = Collator.getInstance(country);
        Collections.sort(mLocales, new Comparator<Locale>() {
            @Override
            public int compare(Locale locale, Locale t1) {
                return collator.compare(locale.getDisplayCountry(),t1.getDisplayCountry());
            }
        });
        return mLocales;
    }

    //Gets locale language and if not supported returns "en"
    public static String getLanguage(Context context) {
        Log.i(TAG, "Trying to find current language..." + Locale.getDefault().getISO3Language());
        if (AppGeneral.SupportedLanguages.contains(Locale.getDefault().getISO3Language())) {
            Log.i(TAG, "Yes supported !");
            return Locale.getDefault().getISO3Language();
        }
        return Locale.ENGLISH.getISO3Language();
    }

}
