package com.appoapp.sukhmanisahib.utlis;

import android.content.Context;
import android.content.SharedPreferences;

public class LanguagePref {

    private static final String PREF_NAME = "app_language_pref";
    private static final String KEY_LANGUAGE = "language_code";

    public static void setLanguage(Context context, String code) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, code).apply();
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "pa"); // default Punjabi
    }
}
