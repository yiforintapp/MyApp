package com.leo.appmaster.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.utils.LeoLog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jasper on 2016/3/26.
 */
public class LeoSettings {
    private static final String TAG = "LeoSettings";
    private static final byte[] LOCK = new byte[1];
    public static final String BOOL_TRUE = "true";
    public static final String BOOL_FALSE = "false";

    private static ISettings mDatabase = new DatabaseSettings();
    private static ISettings mPreference = new SharedSettings();

    private static boolean sDatabaseInited;
    private static boolean sPreferenceInited;

    public static synchronized void initialize() {
        Context ctx = AppMasterApplication.getInstance();
        final File file = ctx.getSharedPrefsFile(ctx.getPackageName() + "_preferences");
        if (!file.exists()) {
            return;
        }

        LeoLog.d(TAG, "<ls>start initialize....");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        Map<String, ?> values = preferences.getAll();

        Map<String, Object> highPriority = new HashMap<String, Object>();
        Map<String, Object> normal = new HashMap<String, Object>();
        for (String s : values.keySet()) {
            Object obj = values.get(s);
            if (obj == null) {
                continue;
            }
            if (ISettings.isHighPriority(s)) {
                highPriority.put(s, obj);
            } else {
                normal.put(s, obj);
            }
        }

        LeoLog.d(TAG, "<ls>initialize....high size: " + highPriority.size() + " | normal size: " + normal.size());
        if (highPriority.size() > 0) {
            mPreference.setBundleMap(highPriority, new ISettings.OnBundleSavedListener() {
                @Override
                public void onBundleSaved() {
                    LeoLog.d(TAG, "<ls>initialize, onBundleSaved preference...");
                    sPreferenceInited = true;
                    deleteIfNeeded(file);
                }
            });
        } else {
            sPreferenceInited = true;
        }
        if (normal.size() > 0) {
            mDatabase.setBundleMap(normal, new ISettings.OnBundleSavedListener() {
                @Override
                public void onBundleSaved() {
                    LeoLog.d(TAG, "<ls>initialize, onBundleSaved database...");
                    sDatabaseInited = true;
                    deleteIfNeeded(file);
                }
            });
        } else {
            sDatabaseInited = true;
        }
        deleteIfNeeded(file);

    }

    private synchronized static void deleteIfNeeded(File file) {
        if (file == null) {
            return;
        }
        if (sPreferenceInited && sDatabaseInited) {
            try {
                if (file.exists()) {
                    file.delete();
                    LeoLog.d(TAG, "<ls>deleteIfNeeded, file: " + file.toString());
                } else {
                    LeoLog.d(TAG, "<ls>deleteIfNeeded, file is not exist.");
                }
                File backupFile = new File(file.getPath() + ".bak");
                if (backupFile.exists()) {
                    backupFile.delete();
                    LeoLog.d(TAG, "<ls>deleteIfNeeded, backupFile: " + backupFile.toString());
                } else {
                    LeoLog.d(TAG, "<ls>deleteIfNeeded, backupFile is not exist.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setInteger(String key, int value) {
        getSettings(key).setInteger(key, value);
    }

    public static void setLong(String key, long value) {
        getSettings(key).setLong(key, value);
    }

    public static void setFloat(String key, float value) {
        getSettings(key).setFloat(key, value);
    }

    public static void setDouble(String key, double value) {
        getSettings(key).setDouble(key, value);
    }

    public static void setBoolean(String key, boolean value) {
        getSettings(key).setBoolean(key, value);
    }

    public static void setString(String key, String value) {
        getSettings(key).setString(key, value);
    }

    public static long getLong(String key, long def) {
        return getSettings(key).getLong(key, def);
    }

    public static float getFloat(String key, float def) {
        return getSettings(key).getFloat(key, def);
    }

    public static double getDouble(String key, double def) {
        return getSettings(key).getDouble(key, def);
    }

    public static boolean getBoolean(String key, boolean def) {
        return getSettings(key).getBoolean(key, def);
    }

    public static String getString(String key, String def) {
        return getSettings(key).getString(key, def);
    }

    public static int getInteger(String key, int def) {
        return getSettings(key).getInteger(key, def);
    }

    private static ISettings getSettings(String key) {
        if (ISettings.isHighPriority(key)) {
            return mPreference;
        }

        return mDatabase;
    }

}
