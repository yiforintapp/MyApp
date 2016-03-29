package com.leo.appmaster.db;

import android.content.Context;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;

import java.io.File;

/**
 * Created by Jasper on 2016/3/26.
 */
public class LeoSettings {
    public static final int BOOL_TRUE = 1;
    public static final int BOOL_FALSE = 0;

    private static ISettings mDatabase = new DatabaseSettings();
    private static ISettings mPreference = new SharedSettings();

    public static void initialize() {

    }

    private static File getPreferencesDir() {
        Context ctx = AppMasterApplication.getInstance();
        File file = ctx.getSharedPrefsFile(ctx.getPackageName() + "_preferences");
        if (!file.exists()) {
            return null;
        }

        AppMasterPreference pref = AppMasterPreference.getInstance(ctx);
//        pref.
        return null;
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
        setInteger(key, value ? BOOL_TRUE : BOOL_FALSE);
    }

    public static void setString(String key, String value) {
        getSettings(key).set(key, value);
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
        int value = getInteger(key, def ? BOOL_TRUE : BOOL_FALSE);

        return value == BOOL_TRUE;
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
