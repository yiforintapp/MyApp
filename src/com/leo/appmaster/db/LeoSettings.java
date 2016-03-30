package com.leo.appmaster.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;

import java.io.File;
import java.util.Map;

/**
 * Created by Jasper on 2016/3/26.
 */
public class LeoSettings {
    private static final byte[] LOCK = new byte[1];
    public static final String BOOL_TRUE = "true";
    public static final String BOOL_FALSE = "false";

    private static ISettings mDatabase = new DatabaseSettings();
    private static ISettings mPreference = new SharedSettings();

    public static synchronized void initialize() {
        Context ctx = AppMasterApplication.getInstance();
        File file = ctx.getSharedPrefsFile(ctx.getPackageName() + "_preferences");
        if (!file.exists()) {
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        Map<String, ?> values = preferences.getAll();
        for (String s : values.keySet()) {
            Object obj = values.get(s);
            if (obj != null) {
                LeoSettings.setString(s, String.valueOf(obj));
            }
        }

        file.delete();

    }

    public static void setInteger(String key, int value) {
        setString(key, value + "");
    }

    public static void setLong(String key, long value) {
        setString(key, value + "");
    }

    public static void setFloat(String key, float value) {
        setString(key, value + "");
    }

    public static void setDouble(String key, double value) {
        setString(key, value + "");
    }

    public static void setBoolean(String key, boolean value) {
        setString(key, value ? BOOL_TRUE : BOOL_FALSE);
    }

    public static void setString(String key, String value) {
        getSettings(key).set(key, value);
    }

    public static void setBundleMap(Map<String, Object> map) {

    }

    public static long getLong(String key, long def) {
        String value = getString(key, null);
        if (value == null) {
            return def;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
        }
        return def;
    }

    public static float getFloat(String key, float def) {
        String value = getString(key, null);
        if (value == null) {
            return def;
        }

        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
        }
        return def;
    }

    public static double getDouble(String key, double def) {
        String value = getString(key, null);
        if (value == null) {
            return def;
        }

        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
        }
        return def;
    }

    public static boolean getBoolean(String key, boolean def) {
        String value = getString(key, def ? BOOL_TRUE : BOOL_FALSE);

        return BOOL_TRUE.equals(value);
    }

    public static String getString(String key, String def) {
        return getSettings(key).get(key, def);
    }

    public static int getInteger(String key, int def) {
        String value = getString(key, null);
        if (value == null) {
            return def;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
        }
        return def;
    }

    private static ISettings getSettings(String key) {
        if (ISettings.isHighPriority(key)) {
            return mPreference;
        }

        return mDatabase;
    }

}
