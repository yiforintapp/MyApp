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
            AppMasterPreference.PREF_UNLOCK_SUCCESS_TIP_RANDOM
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

    public abstract void set(String key, String value);
    public abstract String get(String key, String def);

    public abstract void setBundleMap(Map<String, Object> map);

    static boolean isHighPriority(String key) {
        return sGroupHigh.containsKey(key);
    }

    static boolean isEncrypto() {
        return false;
    }

}
