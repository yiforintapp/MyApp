package com.leo.appmaster.db;

import android.content.Context;
import android.util.SparseArray;

import com.leo.appmaster.AppMasterApplication;

import com.leo.appmaster.AppMasterPreference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jasper on 2016/3/26.
 */
public abstract class ISettings {
    static final int PRIORITY_HIGH = 0;
    static final int PRIORITY_LOW = 1;

    static final String[] GROUP_HIGH = new String[]{
            AppMasterPreference.PREF_LAST_VERSION,
            AppMasterPreference.PREF_LAST_SYNC_BUSINESS_TIME,
            AppMasterPreference.PREF_LAST_BUSINESS_RED_TIP,
            AppMasterPreference.PREF_LOCK_TYPE,
            AppMasterPreference.PREF_GESTURE,
            AppMasterPreference.PREF_PASSWORD,
            AppMasterPreference.PREF_FIRST_INSTALL_APP,
            AppMasterPreference.PREF_LOCK_REMIND,
            AppMasterPreference.PREF_HAVE_EVER_LOAD_APPS,
            AppMasterPreference.PREF_LAST_ALARM_SET_TIME,
            AppMasterPreference.PREF_NEED_CUT_BACKUP_UNINSTALL_AND_PRIVACYCONTRACT,
            AppMasterPreference.PREF_REMOVE_UNLOCK_ALL_SHORTCUT_FLAG,
            AppMasterPreference.PREF_UPDATE_QUICK_GESTURE_USER,
            AppMasterPreference.PREF_UPDATE_RECOVERY_DEFAULT_DATA,
            AppMasterPreference.PREF_LAST_GUIDE_VERSION,
            AppMasterPreference.PREF_FIRST_USE_APP,
            AppMasterPreference.PREF_UNLOCK_COUNT,
            AppMasterPreference.PREF_GUIDE_TIP_SHOW,
            AppMasterPreference.PREF_SPLASH_SKIP_URL,
            AppMasterPreference.PREF_SPLASH_SKIP_TO_CLIENT,
            AppMasterPreference.PREF_SPLASH_DElAY_TIME,
            AppMasterPreference.PREF_UNLOCK_SUCCESS_TIP_RANDOM,
            AppMasterPreference.PREF_SPLASH_START_SHOW_TIME,
            AppMasterPreference.PREF_SPLASH_END_SHOW_TIME
    };
    static Map<String, String> sGroupHigh = null;

    protected Context mContext;

    ISettings() {
        mContext = AppMasterApplication.getInstance();

        sGroupHigh = new HashMap<String, String>();
        for (String key : GROUP_HIGH) {
            sGroupHigh.put(key, key);
        }
    }

    public abstract void setBoolean(String key, boolean value);
    public abstract void setInteger(String key, int value);
    public abstract void setDouble(String key, double value);
    public abstract void setFloat(String key, float value);
    public abstract void setLong(String key, long value);
    public abstract void setString(String key, String value);
    public abstract boolean getBoolean(String key, boolean def);
    public abstract int getInteger(String key, int def);
    public abstract long getLong(String key, long def);
    public abstract float getFloat(String key, float def);
    public abstract double getDouble(String key, double def);
    public abstract String getString(String key, String def);

    protected abstract void setBundleMap(Map<String, Object> map, OnBundleSavedListener listener);

    static boolean isHighPriority(String key) {
        return sGroupHigh.containsKey(key);
    }

    static boolean isEncrypto() {
        return false;
    }

    public interface OnBundleSavedListener {
        public void onBundleSaved();
    }
}
