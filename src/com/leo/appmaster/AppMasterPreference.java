
package com.leo.appmaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.leo.appmaster.applocker.AppLockListActivity;

public class AppMasterPreference implements OnSharedPreferenceChangeListener {

    // about lock
    private static final String PREF_APPLICATION_LIST = "application_list";
    private static final String PREF_LOCK_TYPE = "lock_type";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_GESTURE = "gesture";
    private static final String PREF_LOCK_POLICY = "lock_policy";
    private static final String PREF_HAVE_PSWD_PROTECTED = "have_setted_pswd";
    private static final String PREF_PASSWD_QUESTION = "passwd_question";
    private static final String PREF_PASSWD_ANWSER = "passwd_anwser";
    private static final String PREF_PASSWD_TIP = "passwd_tip";
    public static final String PREF_LOCKER_THEME = "set_locker_theme";
    public static final String PREF_RELOCK_TIME = "relock_time";
    public static final String PREF_AUTO_LOCK = "set_auto_lock";
    public static final String PREF_SET_PROTECT = "set_passwd_protect";
    public static final String PREF_FORBIND_UNINSTALL = "set_forbid_uninstall";
    public static final String PREF_FIRST_USE_LOCKER = "first_use_locker";
    public static final String PREF_SORT_TYPE = "sort_type";
    public static final String PREF_NEW_APP_LOCK_TIP = "new_app_lock_tip";
    public static final String PREF_LAST_PULL_LOCK_LIST_TIME = "last_pull_lock_list_time";
    public static final String PREF_PULL_INTERVAL = "pull_interval";
    public static final String PREF_RECOMMEND_LOCK_LIST = "recommend_app_lock_list";
    public static final String PREF_LAST_ALARM_SET_TIME = "last_alarm_set_time";
    public static final String PREF_RECOMMEND_LOCK_PERCENT = "recommend_lock_percent";
    public static final String PREF_UNLOCK_COUNT = "unlock_count";
    public static final String PREF_GUIDE_TIP_SHOW = "google_play_guide_tip_show";
    public static final String PREF_HIDE_THEME_PKGS = "hide_theme_packages";
    public static final String PREF_HAVE_EVER_LOAD_APPS = "have_ever_load_apps";
    public static final String PREF_SETTING_LOCKER_CLEAN = "setting_locker_clean";
    public static final String PREF_THEME_LOCK_GUIDE = "theme_locker_guide";
    public static final String PREF_USE_LOCK_THEME_GUIDE = "use_lock_theme_guid";
    public static final String PREF_LAUNCH_OTHER_APP = "launch_other_app";

    public static final String PREF_UNLOCK_ALL_APP = "lock_setting_unlock_all";
    public static final String PREF_LOCK_SETTING = "lock_setting";
    public static final String PREF_LOCK_SETTING_CHANGE_PASSWORD = "lock_setting_chanage_password";

    // online theme
    public static final String PREF_ONLINE_THEME_SERIAL = "online_theme_serialnumber";
    public static final String PREF_LOCAL_THEME_SERIAL = "local_theme_serialnumber";
    public static final String PREF_LAST_CHECK_NEW_THEME = "last_check_new_theme_time";

    // applist business
    public static final String PREF_LAST_SYNC_BUSINESS_TIME = "last_sync_business_time";
    public static final String PREF_LAST_CHECK_NEW_BUSINESS_APP_TIME = "last_check_new_business_app_time";
    public static final String PREF_ONLINE_BUSINESS_SERIAL = "online_business_serialnumber";
    public static final String PREF_LOCAL_BUSINESS_SERIAL = "local_business_serialnumber";

    // other
    public static final String PREF_LAST_VERSION = "last_version";
    public static final String PREF_LAST_VERSION_INSTALL_TIME = "last_version_install_tiem";
    public static final String PREF_LOCK_REMIND = "lock_remind";
    public static final String PREF_RECOMMENT_TIP_LIST = "recomment_tip_list";

    // home page
    public static final String PREF_HOME_BUSINESS_NEW_TIP_CLICK = "home_business_tip_click";
    public static final String PREF_HOME_LOCKED = "home_locked";
    public static final String PREF_FIRST_USE_APP = "first_use_privacy_guard";

    
    public static final String PREF_APP_MANAGER_FRAGMENT_FIRST_IN = "fragment_first_in";
    // flow calulate
    // public static final String PREF_APP_MANAGER_FLOW_TODAY_GPRS = "today_ll";
    public static final String PREF_APP_MANAGER_FLOW_TOTAL_TRAFFIC = "totalflow";
    // public static final String PREF_APP_MANAGER_FLOW_MONTH_DAY_CLEAN =
    // "day_clean";
    public static final String PREF_APP_MANAGER_FLOW_MONTH_USED_TRAFFIC = "used_traffic";
    public static final String PREF_APP_MANAGER_FLOW_MAKE_ITSELF_MONTH_TRAFFIC = "make_itself_month_traffic";
    public static final String PREF_APP_MANAGER_FLOW_MAKE_ITSELF_TODAY_BASE = "make_itself_today_base_traffic";
    
    public static final String PREF_APP_MANAGER_FLOW_MONTH_ALL = "mouth_gprs_all";
    public static final String PREF_APP_MANAGER_FLOW_MONTH_BASE = "mouth_gprs_base";
    // public static final String PREF_APP_MANAGER_FLOW_FIRST_IN = "firstin";
    // public static final String PREF_APP_MANAGER_FLOW_YEAR = "year";
    // public static final String PREF_APP_MANAGER_FLOW_MONTH = "month";
    // public static final String PREF_APP_MANAGER_FLOW_DAY = "day";
    public static final String PREF_APP_MANAGER_FLOW_YEAR_TRAF = "year_app_traf";
    public static final String PREF_APP_MANAGER_FLOW_MONTH_TRAF = "month_app_traf";
    // public static final String PREF_APP_MANAGER_FLOW_DAY_TRAF =
    // "day_app_traf";
    public static final String PREF_APP_MANAGER_FLOW_GPRS_SEND = "gprs_send";
    public static final String PREF_APP_MANAGER_FLOW_GPRS_REV = "gprs_rev";
    public static final String PREF_APP_MANAGER_FLOW_BE_SEND = "be_send";
    public static final String PREF_APP_MANAGER_FLOW__BE_REV = "be_rev";
    public static final String PREF_APP_MANAGER_FLOW_RENEWDAY = "renewday";
    public static final String PREF_APP_MANAGER_FLOW_WIFI_SEND = "wifi_send";
    public static final String PREF_APP_MANAGER_FLOW_WIFI_REV = "wifi_rev";
    public static final String PREF_APP_MANAGER_FLOW_SETTING_SWTICH = "flow_setting_swtich";
    public static final String PREF_APP_MANAGER_FLOW_SETTING_SEEKBAR = "flow_setting_seekbar";
    public static final String PREF_APP_MANAGER_FLOW_ALOT_NOTICE = "flow_setting_alot_notice";
    public static final String PREF_APP_MANAGER_FLOW_FINISH_NOTICE = "flow_setting_finish_notice";
    public static final String PREF_APP_MANAGER_FLOW_BROADCAST_FIRST_IN = "flow_setting_broadcast_first_in";
    public static final String PREF_APP_HOME_APP_FRAGMENT_RED_TIP = "home_app_fragment_red_tip";
    public static final String PREF_APP_HOT_APP_ACTIVITY_RED_TIP = "hot_app_activity_red_tip";
    
    public static final String PREF_APP_PRIVACY_MESSAGE_RED_TIP = "privacy_message_red_tip";
    public static final String PREF_APP_PRIVACY_CALL_LOG_RED_TIP = "privacy_call_log_red_tip";

    public static final String PREF_SHOW_TIP_KEY = "last_show_tip_time";
    public static final String PREF_THEME_SUCCESS_STRATEGY = "theme_success_strategy";
    public static final String PREF_THEME_FAIL_STRATEGY = "theme_fail_strategy";
    public static final String PREF_CURRENT_THEME_STRATEGY = "theme_current_strategy";
    public static final String PREF_BUSINESS_SUCCESS_STRATEGY = "business_success_strategy";
    public static final String PREF_BUSINESS_FAIL_STRATEGY = "business_fail_strategy";
    public static final String PREF_CURRENT_BUSINESS_STRATEGY = "business_current_strategy";
    public static final String PREF_MESSAGE_ITEM_RUNING = "message_item_runing";
    public static final String PREF_CALL_LOG_ITEM_RUNING = "call_log_item_runing";
    
    //weizhuang
    public static final String PREF_WEIZHUANG_SELECTED = "weizhuang_selected";
    
    private boolean mLaunchOtherApp = false;
    // lock mode
    public static final String PREF_FIRST_USE_LOCK_MODE = "first_use_lock_mode";

    private List<String> mLockedAppList;
    private List<String> mRecommendList;
    private List<String> mHideThemeList;
    private String mPassword;
    private String mGesture;
    private String mLockPolicy;
    private List<String> mRecommentAppList;
    // private boolean mLockerScreenThemeGuide = false;

    public static final int LOCK_TYPE_NONE = -1;
    public static final int LOCK_TYPE_PASSWD = 0;
    public static final int LOCK_TYPE_GESTURE = 1;
    private int mLockType = LOCK_TYPE_NONE;

    private boolean mUnlocked = false;
    private String mDoubleCheck = null;
    private boolean mFromOther = false;

    private long mLastShowTime = -1;

    private long mThemeSuccessStrategy = -1;
    private long mThemeFailStrategy = -1;
    private long mCurrentThemeStrategy = -1;

    private long mBusinessSuccessStrategy = -1;
    private long mBusinessFailStrategy = -1;
    private long mCurrentBusinessStrategy = -1;

    private SharedPreferences mPref;
    private static AppMasterPreference mInstance;

    private AppMasterPreference(Context context) {
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        mPref.registerOnSharedPreferenceChangeListener(this);
        loadPreferences();
    }

    public static synchronized AppMasterPreference getInstance(Context context) {
        return mInstance == null ? (mInstance = new AppMasterPreference(context))
                : mInstance;
    }

    public boolean getUseThemeGuide() {
        return mPref.getBoolean(PREF_USE_LOCK_THEME_GUIDE, false);
    }

    public void setUseThemeGuide(boolean flag) {
        mPref.edit().putBoolean(PREF_USE_LOCK_THEME_GUIDE, flag).commit();
    }

    public long getBusinessSuccessStrategy() {
        if (mBusinessSuccessStrategy < 0) {
            mBusinessSuccessStrategy = mPref.getLong(PREF_BUSINESS_SUCCESS_STRATEGY,
                    AppMasterConfig.TIME_12_HOUR);
        }
        return mBusinessSuccessStrategy;
    }

    public long getBusinessFailStrategy() {
        if (mBusinessFailStrategy < 0) {
            mBusinessFailStrategy = mPref.getLong(PREF_BUSINESS_FAIL_STRATEGY,
                    AppMasterConfig.TIME_2_HOUR);
        }
        return mBusinessFailStrategy;
    }

    public long getBusinessCurrentStrategy() {
        if (mCurrentBusinessStrategy < 0) {
            mCurrentBusinessStrategy = mPref.getLong(PREF_CURRENT_BUSINESS_STRATEGY,
                    AppMasterConfig.TIME_2_HOUR);
        }
        return mCurrentBusinessStrategy;
    }

    public void setBusinessStrategy(long currentStrategy, long successStrategy, long failStrategy) {
        Editor editor = null;
        if (mCurrentBusinessStrategy != currentStrategy) {
            mCurrentBusinessStrategy = currentStrategy;
            editor = mPref.edit().putLong(PREF_CURRENT_BUSINESS_STRATEGY, currentStrategy);
        }
        if (mBusinessSuccessStrategy != successStrategy) {
            mBusinessSuccessStrategy = successStrategy;
            if (editor == null) {
                editor = mPref.edit().putLong(PREF_BUSINESS_SUCCESS_STRATEGY, successStrategy);
            } else {
                editor.putLong(PREF_BUSINESS_SUCCESS_STRATEGY, successStrategy);
            }
        }
        if (mBusinessFailStrategy != failStrategy) {
            mBusinessFailStrategy = failStrategy;
            if (editor == null) {
                editor = mPref.edit().putLong(PREF_BUSINESS_FAIL_STRATEGY, failStrategy);
            } else {
                editor.putLong(PREF_BUSINESS_FAIL_STRATEGY, failStrategy);
            }
        }
        if (editor != null) {
            editor.commit();
        }
    }

    public long getThemeSuccessStrategy() {
        if (mThemeSuccessStrategy < 0) {
            mThemeSuccessStrategy = mPref.getLong(PREF_THEME_SUCCESS_STRATEGY,
                    AppMasterConfig.TIME_12_HOUR);
        }
        return mThemeSuccessStrategy;
    }

    public long getThemeFailStrategy() {
        if (mThemeFailStrategy < 0) {
            mThemeFailStrategy = mPref.getLong(PREF_THEME_FAIL_STRATEGY,
                    AppMasterConfig.TIME_2_HOUR);
        }
        return mThemeFailStrategy;
    }

    public long getThemeCurrentStrategy() {
        if (mCurrentThemeStrategy < 0) {
            mCurrentThemeStrategy = mPref.getLong(PREF_CURRENT_THEME_STRATEGY,
                    AppMasterConfig.TIME_2_HOUR);
        }
        return mCurrentThemeStrategy;
    }

    public void setThemeStrategy(long currentStrategy, long successStrategy, long failStrategy) {
        Editor editor = null;
        if (mCurrentThemeStrategy != currentStrategy) {
            mCurrentThemeStrategy = currentStrategy;
            editor = mPref.edit().putLong(PREF_CURRENT_THEME_STRATEGY, currentStrategy);
        }
        if (mThemeSuccessStrategy != successStrategy) {
            mThemeSuccessStrategy = successStrategy;
            if (editor == null) {
                editor = mPref.edit().putLong(PREF_THEME_SUCCESS_STRATEGY, successStrategy);
            } else {
                editor.putLong(PREF_THEME_SUCCESS_STRATEGY, successStrategy);
            }
        }
        if (mThemeFailStrategy != failStrategy) {
            mThemeFailStrategy = failStrategy;
            if (editor == null) {
                editor = mPref.edit().putLong(PREF_THEME_FAIL_STRATEGY, failStrategy);
            } else {
                editor.putLong(PREF_THEME_FAIL_STRATEGY, failStrategy);
            }
        }
        if (editor != null) {
            editor.commit();
        }

        if (mCurrentThemeStrategy != currentStrategy) {
            mCurrentThemeStrategy = currentStrategy;
            mPref.edit().putLong(PREF_CURRENT_THEME_STRATEGY, currentStrategy).commit();
        }
    }

    public long getLastShowTime() {
        if (mLastShowTime < 0) {
            mLastShowTime = mPref.getLong(PREF_SHOW_TIP_KEY, 0);
        }
        return mLastShowTime;
    }

    public void setLastShowTime(long lastShowTime) {
        mLastShowTime = lastShowTime;
        mPref.edit().putLong(PREF_SHOW_TIP_KEY, lastShowTime).commit();
    }

    public boolean getMessageRedTip() {
        return mPref.getBoolean(PREF_APP_PRIVACY_MESSAGE_RED_TIP, false);
    }

    public void setMessageRedTip(boolean flag) {
        mPref.edit().putBoolean(PREF_APP_PRIVACY_MESSAGE_RED_TIP, flag).commit();
    }

    public boolean getCallLogRedTip() {
        return mPref.getBoolean(PREF_APP_PRIVACY_CALL_LOG_RED_TIP, false);
    }

    public void setCallLogRedTip(boolean flag) {
        mPref.edit().putBoolean(PREF_APP_PRIVACY_CALL_LOG_RED_TIP, flag).commit();
    }
    

    public boolean getHomeFragmentRedTip() {
        return mPref.getBoolean(PREF_APP_HOME_APP_FRAGMENT_RED_TIP, false);
    }
    public void setHomeFragmentRedTip(boolean flag) {
        mPref.edit().putBoolean(PREF_APP_HOME_APP_FRAGMENT_RED_TIP, flag).commit();
    }
    
    public boolean getHotAppActivityRedTip() {
        return mPref.getBoolean(PREF_APP_HOT_APP_ACTIVITY_RED_TIP, false);
    }
    public void setHotAppActivityRedTip(boolean flag) {
        mPref.edit().putBoolean(PREF_APP_HOT_APP_ACTIVITY_RED_TIP, flag).commit();
    }
    
    public boolean getLockerScreenThemeGuid() {
        return mPref.getBoolean(PREF_THEME_LOCK_GUIDE, false);
    }

    public void setLockerScreenThemeGuide(boolean flag) {
        mPref.edit().putBoolean(PREF_THEME_LOCK_GUIDE, flag).commit();
    }

    public boolean getHomeBusinessTipClick() {
        return mPref.getBoolean(PREF_HOME_BUSINESS_NEW_TIP_CLICK, false);
    }

    public boolean isFirstUseLockMode() {
        return mPref.getBoolean(PREF_FIRST_USE_LOCK_MODE, true);
    }

    public void setFirstUseLockMode(boolean firstUse) {
        mPref.edit().putBoolean(PREF_FIRST_USE_LOCK_MODE, firstUse).commit();
    }

    public boolean getHomeLocked() {
        if (mLockedAppList.contains(AppMasterApplication.getInstance().getPackageName())) {
            return true;
        } else {
            return false;
        }
    }

    public void setHomeBusinessTipClick(boolean flag) {
        mPref.edit().putBoolean(PREF_HOME_BUSINESS_NEW_TIP_CLICK, flag).commit();
    }

    public List<String> getRecommentTipList() {
        return mRecommentAppList;
    }

    public void setRecommentTipList(List<String> appList) {
        mRecommentAppList = appList;
        String combined = "";
        for (String string : appList) {
            combined = combined + string + ";";
        }
        // if (appList == null || appList.isEmpty()) {
        // // Intent serviceIntent = new Intent(
        // // AppMasterApplication.getInstance(), TaskDetectService.class);
        // // serviceIntent.putExtra("lock_service", false);
        // // AppMasterApplication.getInstance().startService(serviceIntent);
        // LockManager.getInstatnce().stopLockService();
        // } else {
        // // Intent serviceIntent = new Intent(
        // // AppMasterApplication.getInstance(), TaskDetectService.class);
        // // serviceIntent.putExtra("lock_service", true);
        // // AppMasterApplication.getInstance().startService(serviceIntent);
        // LockManager.getInstatnce().startLockService();
        // }
        mPref.edit().putString(PREF_RECOMMENT_TIP_LIST, combined).commit();
    }

    public void setHaveEverAppLoaded(boolean loaded) {
        mPref.edit().putBoolean(PREF_HAVE_EVER_LOAD_APPS, loaded).commit();
    }

    public boolean haveEverAppLoaded() {
        return mPref.getBoolean(PREF_HAVE_EVER_LOAD_APPS, false);
    }

    public String getOnlineThemeSerialNumber() {
        return mPref.getString(PREF_ONLINE_THEME_SERIAL, "");
    }

    public void setOnlineThemeSerialNumber(String serial) {
        mPref.edit().putString(PREF_ONLINE_THEME_SERIAL, serial).commit();
    }

    public boolean getLaunchOtherApp() {
        return mLaunchOtherApp;
    }

    public void setLaunchOtherApp(boolean flag) {
        mLaunchOtherApp = flag;
    }

    public String getLocalThemeSerialNumber() {
        return mPref.getString(PREF_LOCAL_THEME_SERIAL, "");
    }

    public void setLocalThemeSerialNumber(String serial) {
        mPref.edit().putString(PREF_LOCAL_THEME_SERIAL, serial).commit();
    }

    public long getLastCheckThemeTime() {
        return mPref.getLong(PREF_LAST_CHECK_NEW_THEME, 0);
    }

    public void setLastCheckThemeTime(long lastTime) {
        mPref.edit().putLong(PREF_LAST_CHECK_NEW_THEME, lastTime).commit();
    }

    public long getLastSyncBusinessTime() {
        return mPref.getLong(PREF_LAST_SYNC_BUSINESS_TIME, 0);
    }

    public void setLastSyncBusinessTime(long lastTime) {
        mPref.edit().putLong(PREF_LAST_SYNC_BUSINESS_TIME, lastTime).commit();
    }

    public long getLastCheckBusinessTime() {
        return mPref.getLong(PREF_LAST_CHECK_NEW_BUSINESS_APP_TIME, 0);
    }

    public void setLastCheckBusinessTime(long lastTime) {
        mPref.edit().putLong(PREF_LAST_CHECK_NEW_BUSINESS_APP_TIME, lastTime)
                .commit();
    }

    public String getOnlineBusinessSerialNumber() {
        return mPref.getString(PREF_ONLINE_BUSINESS_SERIAL, "");
    }

    public void setOnlineBusinessSerialNumber(String serial) {
        mPref.edit().putString(PREF_ONLINE_BUSINESS_SERIAL, serial).commit();
    }

    public String getLocalBusinessSerialNumber() {
        return mPref.getString(PREF_LOCAL_BUSINESS_SERIAL, "");
    }

    public void setLocalBusinessSerialNumber(String serial) {
        mPref.edit().putString(PREF_LOCAL_BUSINESS_SERIAL, serial).commit();
    }

    public void setHideThemeList(List<String> themeList) {
        mHideThemeList = themeList;
        String combined = "";
        for (String string : mHideThemeList) {
            combined = combined + string + ";";
        }
        mPref.edit().putString(PREF_HIDE_THEME_PKGS, combined).commit();
    }

    public List<String> getHideThemeList() {
        return mHideThemeList;
    }

    public void setGoogleTipShowed(boolean show) {
        mPref.edit().putBoolean(PREF_GUIDE_TIP_SHOW, show).commit();
    }

    public boolean getGoogleTipShowed() {
        return mPref.getBoolean(PREF_GUIDE_TIP_SHOW, false);
    }

    public void setUnlockCount(long count) {
        mPref.edit().putLong(PREF_UNLOCK_COUNT, count).commit();
    }

    public long getUnlockCount() {
        return mPref.getLong(PREF_UNLOCK_COUNT, 0);
    }

    public void setRecommendLockPercent(float percent) {
        mPref.edit().putFloat(PREF_RECOMMEND_LOCK_PERCENT, percent).commit();
    }

    public float getRecommendLockPercent() {
        return mPref.getFloat(PREF_RECOMMEND_LOCK_PERCENT, 0.0f);
    }

    public void setReminded(boolean reminded) {
        mPref.edit().putBoolean(PREF_LOCK_REMIND, reminded).commit();
    }

    public boolean isReminded() {
        return mPref.getBoolean(PREF_LOCK_REMIND, false);
    }

    public void setLastVersion(String lastVersion) {
        mPref.edit().putString(PREF_LAST_VERSION, lastVersion).commit();
    }

    public String getLastVersion() {
        return mPref.getString(PREF_LAST_VERSION, "");
    }

    public void setLastVersionInstallTime(long time) {
        mPref.edit().putLong(PREF_LAST_VERSION_INSTALL_TIME, time).commit();
    }

    public long getLastVersionInstallTime() {
        return mPref.getLong(PREF_LAST_VERSION_INSTALL_TIME, 0l);
    }

    public boolean isNewAppLockTip() {
        return mPref.getBoolean(PREF_NEW_APP_LOCK_TIP, true);
    }

    public void setSortType(int type) {
        mPref.edit().putInt(PREF_SORT_TYPE, type).commit();
    }

    public int getSortType() {
        return mPref.getInt(PREF_SORT_TYPE, AppLockListActivity.DEFAULT_SORT);
    }

    public boolean isFisrtUseLocker() {
        return mPref.getBoolean(PREF_FIRST_USE_LOCKER, true);
    }

    public void setLockerUsed() {
        mPref.edit().putBoolean(PREF_FIRST_USE_LOCKER, false).commit();
    }

    public boolean getFirstUse() {
        return mPref.getBoolean(PREF_FIRST_USE_APP, true);
    }

    public void setFirstUse(boolean flag) {
        mPref.edit().putBoolean(PREF_FIRST_USE_APP, flag).commit();
    }

    public String getLockPolicy() {
        return mLockPolicy;
    }

    public void setLockPolicy(String policy) {
        mLockPolicy = policy;
        mPref.edit().putString(PREF_LOCK_POLICY, policy).commit();
    }

    public int getRelockTimeout() {
        String time = mPref.getString(PREF_RELOCK_TIME, "0");
        return Integer.parseInt(time) * 1000;
    }

    public void setRelockTimeout(String timeout) {
        mPref.edit().putString(PREF_RELOCK_TIME, timeout + "").commit();
    }

    public void setUnlockAllApp(boolean flag) {
        mPref.edit().putBoolean(PREF_UNLOCK_ALL_APP, flag).commit();
    }

    public boolean isUnlockAll() {
        return mPref.getBoolean(PREF_UNLOCK_ALL_APP, false);
    }

    public String getPassword() {
        return mPassword;
    }

    public String getGesture() {
        return mGesture;
    }

    public void savePassword(String password) {
        mPassword = "";
        if (password != null) {
            mPassword = password.trim();
        }
        mPref.edit().putString(PREF_PASSWORD, password).commit();
        mPref.edit().putInt(PREF_LOCK_TYPE, LOCK_TYPE_PASSWD).commit();
        mLockType = LOCK_TYPE_PASSWD;
    }

    public void saveGesture(String gesture) {
        mGesture = gesture;
        mPref.edit().putString(PREF_GESTURE, gesture).commit();
        mPref.edit().putInt(PREF_LOCK_TYPE, LOCK_TYPE_GESTURE).commit();
        mLockType = LOCK_TYPE_GESTURE;

    }

    public int getLockType() {
        return mLockType;
    }

    public boolean hasPswdProtect() {
        return !mPref.getString(PREF_PASSWD_QUESTION, "").equals("");
    }

    public String getPpQuestion() {
        return mPref.getString(PREF_PASSWD_QUESTION, "");
    }

    public String getPpAnwser() {
        return mPref.getString(PREF_PASSWD_ANWSER, "");
    }

    public String getPasswdTip() {
        return mPref.getString(PREF_PASSWD_TIP, "");
    }

    // public List<String> getLockedAppList() {
    // return mLockedAppList;
    // }

    // public void setLockedAppList(List<String> applicationList) {
    // mLockedAppList = applicationList;
    // String combined = "";
    // for (String string : applicationList) {
    // combined = combined + string + ";";
    // }
    //
    // if (applicationList == null || applicationList.isEmpty()) {
    // LockManager.getInstatnce().stopLockService();
    // } else {
    // if (LockManager.getInstatnce().serviceBound()) {
    // LockManager.getInstatnce().startLockService();
    // } else {
    // LockManager.getInstatnce().bindService();
    // }
    // }
    //
    // mPref.edit().putString(PREF_APPLICATION_LIST, combined).commit();
    // }

    public List<String> getRecommendList() {
        return mRecommendList;
    }

    public void setRecommendList(List<String> applicationList) {
        mRecommendList = applicationList;
        String combined = "";
        for (String string : applicationList) {
            combined = combined + string + ";";
        }
        mPref.edit().putString(PREF_RECOMMEND_LOCK_LIST, combined).commit();
    }

    private void loadPreferences() {
        String lockList = mPref.getString(PREF_APPLICATION_LIST, "");
        if (lockList.equals("")) {
            mLockedAppList = new ArrayList<String>(0);
        } else {
            mLockedAppList = Arrays.asList(mPref.getString(
                    PREF_APPLICATION_LIST, "").split(";"));
        }
        mRecommendList = Arrays.asList(mPref.getString(
                PREF_RECOMMEND_LOCK_LIST, "").split(";"));
        mRecommentAppList = Arrays.asList(mPref.getString(
                PREF_RECOMMENT_TIP_LIST, "").split(";"));
        String themeList = mPref.getString(PREF_HIDE_THEME_PKGS, "");
        if (themeList.equals("")) {
            mHideThemeList = new ArrayList<String>(0);
        } else {
            mHideThemeList = Arrays.asList(themeList.split(";"));
        }
        mLockType = mPref.getInt(PREF_LOCK_TYPE, LOCK_TYPE_NONE);
        mLockPolicy = mPref.getString(PREF_LOCK_POLICY, null);
        if (mLockType == LOCK_TYPE_GESTURE) {
            mGesture = mPref.getString(PREF_GESTURE, null);
        } else if (mLockType == LOCK_TYPE_PASSWD) {
            mPassword = mPref.getString(PREF_PASSWORD, null);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (PREF_APPLICATION_LIST.equals(key)) {
            String lockList = mPref.getString(PREF_APPLICATION_LIST, "");
            if (lockList.equals("")) {
                mLockedAppList = new ArrayList<String>(0);
            } else {
                mLockedAppList = Arrays.asList(mPref.getString(
                        PREF_APPLICATION_LIST, "").split(";"));
            }
        } else if (PREF_PASSWORD.equals(key)) {
            mPassword = mPref.getString(PREF_PASSWORD, "1234");
        } else if (PREF_LOCK_POLICY.equals(key)) {
            mLockPolicy = mPref.getString(PREF_LOCK_POLICY, null);
        } else if (PREF_RELOCK_TIME.equals(key)) {
            // String s = mPref.getString(PREF_RELOCK_TIME, "-1");
            int re = getRelockTimeout();
        }
    }

    public void savePasswdProtect(String qusetion, String answer, String tip) {

        if (qusetion != null)
            qusetion = qusetion.trim();
        if (answer != null)
            answer = answer.trim();
        if (tip != null)
            tip = tip.trim();

        mPref.edit().putBoolean(PREF_HAVE_PSWD_PROTECTED, true).commit();
        mPref.edit().putString(PREF_PASSWD_QUESTION, qusetion).commit();
        mPref.edit().putString(PREF_PASSWD_ANWSER, answer).commit();
        mPref.edit().putString(PREF_PASSWD_TIP, tip).commit();
    }

    public void setAtuoLock(boolean autoLock) {
        mPref.edit().putBoolean(PREF_AUTO_LOCK, autoLock).commit();
    }

    public boolean isAutoLock() {
        return mPref.getBoolean(PREF_AUTO_LOCK, true);
    }

    public boolean isLockerClean() {
        return mPref.getBoolean(PREF_SETTING_LOCKER_CLEAN, false);
    }

    public void setMonthGprsAll(long value) {
        mPref.edit().putLong(PREF_APP_MANAGER_FLOW_MONTH_ALL, value).commit();
    }

    public long getMonthGprsAll() {
        return mPref.getLong(PREF_APP_MANAGER_FLOW_MONTH_ALL, 0);
    }
    
    public void setItSelfTodayBase(long value) {
        mPref.edit().putLong(PREF_APP_MANAGER_FLOW_MAKE_ITSELF_TODAY_BASE, value).commit();
    }

    public long getItSelfTodayBase() {
        return mPref.getLong(PREF_APP_MANAGER_FLOW_MAKE_ITSELF_TODAY_BASE, 0);
    }

    public void setMonthGprsBase(long value) {
        mPref.edit().putLong(PREF_APP_MANAGER_FLOW_MONTH_BASE, value).commit();
    }

    public long getMonthGprsBase() {
        return mPref.getLong(PREF_APP_MANAGER_FLOW_MONTH_BASE, 0);
    }

    // public void setFirstIn(boolean value) {
    // mPref.edit().putBoolean(PREF_APP_MANAGER_FLOW_FIRST_IN, value).commit();
    // }

    // public boolean getFirstIn() {
    // return mPref.getBoolean(PREF_APP_MANAGER_FLOW_FIRST_IN, true);
    // }
    //
    // public void setYear(int value) {
    // mPref.edit().putInt(PREF_APP_MANAGER_FLOW_YEAR, value).commit();
    // }
    //
    // public int getYear() {
    // return mPref.getInt(PREF_APP_MANAGER_FLOW_YEAR, 2015);
    // }
    //
    // public void setMonth(int value) {
    // mPref.edit().putInt(PREF_APP_MANAGER_FLOW_MONTH, value).commit();
    // }
    //
    // public int getMonth() {
    // return mPref.getInt(PREF_APP_MANAGER_FLOW_MONTH, 1);
    // }
    //
    // public void setDay(int value) {
    // mPref.edit().putInt(PREF_APP_MANAGER_FLOW_DAY, value).commit();
    // }
    //
    // public int getDay() {
    // return mPref.getInt(PREF_APP_MANAGER_FLOW_DAY, 1);
    // }

    public void setYearAppTraf(int value) {
        mPref.edit().putInt(PREF_APP_MANAGER_FLOW_YEAR_TRAF, value).commit();
    }

    public int getYearAppTraf() {
        return mPref.getInt(PREF_APP_MANAGER_FLOW_YEAR_TRAF, 2015);
    }

    public void setMonthAppTraf(int value) {
        mPref.edit().putInt(PREF_APP_MANAGER_FLOW_MONTH_TRAF, value).commit();
    }

    public int getMonthAppTraf() {
        return mPref.getInt(PREF_APP_MANAGER_FLOW_MONTH_TRAF, 1);
    }

    // public void setDayAppTraf(int value) {
    // mPref.edit().putInt(PREF_APP_MANAGER_FLOW_DAY_TRAF, value).commit();
    // }
    //
    // public int getDayAppTraf() {
    // return mPref.getInt(PREF_APP_MANAGER_FLOW_DAY_TRAF, 1);
    // }

    public void setGprsSend(long value) {
        mPref.edit().putLong(PREF_APP_MANAGER_FLOW_GPRS_SEND, value).commit();
    }

    public long getGprsSend() {
        return mPref.getLong(PREF_APP_MANAGER_FLOW_GPRS_SEND, 0);
    }

    public void setGprsRev(long value) {
        mPref.edit().putLong(PREF_APP_MANAGER_FLOW_GPRS_REV, value).commit();
    }

    public long getGprsRev() {
        return mPref.getLong(PREF_APP_MANAGER_FLOW_GPRS_REV, 0);
    }

    public void setBaseSend(long value) {
        mPref.edit().putLong(PREF_APP_MANAGER_FLOW_BE_SEND, value).commit();
    }

    public long getBaseSend() {
        return mPref.getLong(PREF_APP_MANAGER_FLOW_BE_SEND, 0);
    }

    public void setBaseRev(long value) {
        mPref.edit().putLong(PREF_APP_MANAGER_FLOW__BE_REV, value).commit();
    }

    public long getBaseRev() {
        return mPref.getLong(PREF_APP_MANAGER_FLOW__BE_REV, 0);
    }

    public void setRenewDay(int value) {
        mPref.edit().putInt(PREF_APP_MANAGER_FLOW_RENEWDAY, value).commit();
    }

    public int getRenewDay() {
        return mPref.getInt(PREF_APP_MANAGER_FLOW_RENEWDAY, 1);
    }

    public void setTotalTraffic(int value) {
        mPref.edit().putInt(PREF_APP_MANAGER_FLOW_TOTAL_TRAFFIC, value).commit();
    }

    public int getTotalTraffic() {
        return mPref.getInt(PREF_APP_MANAGER_FLOW_TOTAL_TRAFFIC, 0);
    }

    // public void setMonthDayClean(int value) {
    // mPref.edit().putInt(PREF_APP_MANAGER_FLOW_MONTH_DAY_CLEAN,
    // value).commit();
    // }
    //
    // public int getMonthDayClean() {
    // return mPref.getInt(PREF_APP_MANAGER_FLOW_MONTH_DAY_CLEAN, 1);
    // }

    public void setUsedTraffic(int value) {
        mPref.edit().putInt(PREF_APP_MANAGER_FLOW_MONTH_USED_TRAFFIC,
                value).commit();
    }

    public long getUsedTraffic() {
        return mPref.getInt(PREF_APP_MANAGER_FLOW_MONTH_USED_TRAFFIC, 0);
    }
    
    public void setItselfMonthTraffic(long value) {
        mPref.edit().putLong(PREF_APP_MANAGER_FLOW_MAKE_ITSELF_MONTH_TRAFFIC,
                value).commit();
    }

    public long getItselfMonthTraffic() {
        return mPref.getLong(PREF_APP_MANAGER_FLOW_MAKE_ITSELF_MONTH_TRAFFIC, 0);
    }
    
    // public void setTodayGprs(float value) {
    // mPref.edit().putFloat(PREF_APP_MANAGER_FLOW_TODAY_GPRS, value).commit();
    // }
    //
    // public float getTodayGprs() {
    // return mPref.getFloat(PREF_APP_MANAGER_FLOW_TODAY_GPRS, 0);
    // }

    // Single App Flow
    public void setAppBaseSend(int uid, long value) {
        mPref.edit().putLong(uid + "_app_base_send", value).commit();
    }

    public long getAppBaseSend(int uid) {
        return mPref.getLong(uid + "_app_base_send", 0);
    }

    public void setAppBaseRev(int uid, long value) {
        mPref.edit().putLong(uid + "_app_base_rev", value).commit();
    }

    public long getAppBaseRev(int uid) {
        return mPref.getLong(uid + "_app_base_rev", 0);
    }

    public void setAppGprsSend(int uid, long value) {
        mPref.edit().putLong(uid + "_app_gprs_send", value).commit();
    }

    public long getAppGprsSend(int uid) {
        return mPref.getLong(uid + "_app_gprs_send", 0);
    }

    public void setAppGprsRev(int uid, long value) {
        mPref.edit().putLong(uid + "_app_gprs_rev", value).commit();
    }

    public long getAppGprsRev(int uid) {
        return mPref.getLong(uid + "_app_gprs_rev", 0);
    }

    public void setWifiSend(int uid, long value) {
        mPref.edit().putLong(uid + PREF_APP_MANAGER_FLOW_WIFI_SEND, value).commit();
    }

    public long getWifiSend(int uid) {
        return mPref.getLong(uid + PREF_APP_MANAGER_FLOW_WIFI_SEND, 0);
    }

    public void setWifiRev(int uid, long value) {
        mPref.edit().putLong(uid + PREF_APP_MANAGER_FLOW_WIFI_REV, value).commit();
    }

    public long getWifiRev(int uid) {
        return mPref.getLong(uid + PREF_APP_MANAGER_FLOW_WIFI_REV, 0);
    }

    public void setFlowSetting(boolean mSwtich) {
        mPref.edit().putBoolean(PREF_APP_MANAGER_FLOW_SETTING_SWTICH, mSwtich).commit();
    }

    public boolean getFlowSetting() {
        return mPref.getBoolean(PREF_APP_MANAGER_FLOW_SETTING_SWTICH, false);
    }

    public void setFlowSettingBar(int progress) {
        mPref.edit().putInt(PREF_APP_MANAGER_FLOW_SETTING_SEEKBAR, progress).commit();
    }

    public int getFlowSettingBar() {
        return mPref.getInt(PREF_APP_MANAGER_FLOW_SETTING_SEEKBAR, 50);
    }

    public void setAlotNotice(boolean Alot) {
        mPref.edit().putBoolean(PREF_APP_MANAGER_FLOW_ALOT_NOTICE, Alot).commit();
    }

    public boolean getAlotNotice() {
        return mPref.getBoolean(PREF_APP_MANAGER_FLOW_ALOT_NOTICE, false);
    }

    public void setFinishNotice(boolean Alot) {
        mPref.edit().putBoolean(PREF_APP_MANAGER_FLOW_FINISH_NOTICE, Alot).commit();
    }

    public boolean getFinishNotice() {
        return mPref.getBoolean(PREF_APP_MANAGER_FLOW_FINISH_NOTICE, false);
    }

    public long getFirstTime() {
        return mPref.getLong(PREF_APP_MANAGER_FLOW_BROADCAST_FIRST_IN, 0);
    }

    public void setFirstTime(long time) {
        mPref.edit().putLong(PREF_APP_MANAGER_FLOW_BROADCAST_FIRST_IN, time).commit();
    }
    
    public long getFragmentFirstIn() {
        return mPref.getLong(PREF_APP_MANAGER_FRAGMENT_FIRST_IN, 0);
    }

    public void setFragmentFirstIn(long time) {
        mPref.edit().putLong(PREF_APP_MANAGER_FRAGMENT_FIRST_IN, time).commit();
    }
    
    
    public void setLockerClean(boolean lockerClean) {
        mPref.edit().putBoolean(PREF_SETTING_LOCKER_CLEAN, lockerClean)
                .commit();
    }

    public void setLastLocklistPullTime(long time) {
        mPref.edit().putLong(PREF_LAST_PULL_LOCK_LIST_TIME, time).commit();
    }

    public void setPullInterval(long interval) {
        mPref.edit().putLong(PREF_PULL_INTERVAL, interval).commit();
    }

    public long getLastLocklistPullTime() {
        return mPref.getLong(PREF_LAST_PULL_LOCK_LIST_TIME, 0l);
    }

    public long getPullInterval() {
        return mPref.getLong(PREF_PULL_INTERVAL, 0l);
    }

    public void setLastAlarmSetTime(long currentTimeMillis) {
        mPref.edit().putLong(PREF_LAST_ALARM_SET_TIME, currentTimeMillis)
                .commit();
    }

    public long getInstallTime() {
        return mPref.getLong(PREF_LAST_ALARM_SET_TIME, 0l);
    }

    // public void setIsHelpSettingChangeSucess(boolean flag){
    // mPref.edit().putBoolean(PREF_LOCK_SETTING_CHANGE_PASSWORD,
    // flag).commit();
    // }
    // public boolean getIsHelpSettingChangeSucess(){
    // return mPref.getBoolean(PREF_LOCK_SETTING_CHANGE_PASSWORD, false);
    // }

    public boolean getUnlocked() {
        return mUnlocked;
    }

    public void setUnlocked(boolean flag) {
        mUnlocked = flag;
    }

    public String getDoubleCheck() {
        return mDoubleCheck;
    }

    public void setDoubleCheck(String pkg) {
        mDoubleCheck = pkg;
    }

    public boolean getFromOther() {
        return mFromOther;
    }

    public void setFromOther(boolean flag) {
        mFromOther = flag;
    }

    public boolean getMessageItemRuning() {

        return mPref.getBoolean(PREF_MESSAGE_ITEM_RUNING, true);
    }

    public void setMessageItemRuning(boolean flag) {
        mPref.edit().putBoolean(PREF_MESSAGE_ITEM_RUNING, flag).commit();
    }

    public boolean getCallLogItemRuning() {

        return mPref.getBoolean(PREF_CALL_LOG_ITEM_RUNING, true);
    }

    public void setCallLogItemRuning(boolean flag) {
        mPref.edit().putBoolean(PREF_CALL_LOG_ITEM_RUNING, flag).commit();
    }
    
    public void setWeiZhuang(int selected){
        mPref.edit().putInt(PREF_WEIZHUANG_SELECTED, selected).commit();
    }
    
    public int getWeiZhuang(){
        return mPref.getInt(PREF_WEIZHUANG_SELECTED, 0);
    }

}
