package org.kinjeng.apmpilot.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by sblaksono on 18/11/2016.
 */

public class Settings {

    private static SharedPreferences preferences = null;

    public static void init(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getString(String key, String defValue) {
        try {
            return preferences.getString(key, defValue);
        }
        catch (Exception e) {
            return defValue;
        }
    }

    public static int getInt(String key, int defValue) {
        try {
            return Integer.parseInt(preferences.getString(key, Integer.valueOf(defValue).toString()));
        }
        catch (Exception e) {
            return defValue;
        }
    }

    public static boolean getBoolean(String key, boolean defValue) {
        try {
            return preferences.getBoolean(key, defValue);
        }
        catch (Exception e) {
            return defValue;
        }
    }



}
