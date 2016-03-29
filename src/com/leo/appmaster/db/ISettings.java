package com.leo.appmaster.db;

import com.leo.appmaster.AppMasterPreference;

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

    public abstract void set(String key, String value);
    public abstract String get(String key, String def);

    static boolean isHighPriority(String key) {
        return false;
    }

    static boolean isEncrypto() {
        return false;
    }

}
