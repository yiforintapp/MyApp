
package com.zlf.appmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.db.LockRecommentTable;
import com.zlf.appmaster.utils.LeoLog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 旧版本已数据存储，新版本不再继续使用，新数据统一使用LeoSettings
 */
@Deprecated
public class AppMasterPreference implements OnSharedPreferenceChangeListener {

    // about lock
    public static final String PREF_LOCK_TYPE = "lock_type";
    public static final String PREF_PASSWORD = "password";
    public static final String PREF_GESTURE = "gesture";
    public static final String PREF_HIDE_LOCK_LINE = "app_hide_lockline";
    public static final String PREF_RELOCK_TIME = "relock_time";
    public static final String PREF_NEW_APP_LOCK_TIP = "new_app_lock_tip";
    public static final String PREF_LAST_ALARM_SET_TIME = "last_alarm_set_time";
    public static final String PREF_UNLOCK_COUNT = "unlock_count";
    public static final String PREF_NEW_USER_UNLOCK_COUNT = "new_user_unlock_count";
    public static final String PREF_GUIDE_TIP_SHOW = "google_play_guide_tip_show";
    public static final String PREF_HIDE_THEME_PKGS = "hide_theme_packages";
    public static final String PREF_HAVE_EVER_LOAD_APPS = "have_ever_load_apps";
    public static final String PREF_NEED_CUT_BACKUP_UNINSTALL_AND_PRIVACYCONTRACT = "need_cut_backup_uninstall_and_privacycontract";

    // online theme
    public static final String PREF_ONLINE_THEME_SERIAL = "online_theme_serialnumber";
    public static final String PREF_LOCAL_THEME_SERIAL = "local_theme_serialnumber";
    public static final String PREF_LAST_CHECK_NEW_THEME = "last_check_new_theme_time";
    public static final String PREF_LAST_UBC = "last_ubc_time";

    // applist business
    public static final String PREF_LAST_SYNC_BUSINESS_TIME = "last_sync_business_time";
    public static final String PREF_LAST_CHECK_NEW_BUSINESS_APP_TIME = "last_check_new_business_app_time";
    public static final String PREF_ONLINE_BUSINESS_SERIAL = "online_business_serialnumber";
    public static final String PREF_LOCAL_BUSINESS_SERIAL = "local_business_serialnumber";

    // other
    public static final String PREF_LAST_VERSION = "last_version";
    public static final String PREF_LAST_GUIDE_VERSION = "last_guide_version";
    public static final String PREF_LOCK_REMIND = "lock_remind";
    public static final String PREF_RECOMMENT_TIP_LIST = "recomment_tip_list";

    // home page
    public static final String PREF_HOME_BUSINESS_NEW_TIP_CLICK = "home_business_tip_click";
    public static final String PREF_FIRST_USE_APP = "first_use_new_version";


    public static final String PREF_SHOW_TIP_KEY = "last_show_tip_time";
    public static final String PREF_SHOW_TIP_HOT_APP_KEY = "last_hot_app_show_tip_time";
    public static final String PREF_THEME_SUCCESS_STRATEGY = "theme_success_strategy";
    public static final String PREF_THEME_FAIL_STRATEGY = "theme_fail_strategy";
    public static final String PREF_CURRENT_THEME_STRATEGY = "theme_current_strategy";
    public static final String PREF_BUSINESS_SUCCESS_STRATEGY = "business_success_strategy";
    public static final String PREF_BUSINESS_FAIL_STRATEGY = "business_fail_strategy";
    public static final String PREF_CURRENT_BUSINESS_STRATEGY = "business_current_strategy";
    public static final String PREF_SPLASH_START_SHOW_TIME = "splash_start_show_time";
    public static final String PREF_SPLASH_END_SHOW_TIME = "splash_end_show_time";
    public static final String PREF_CURRENT_SPLASH_STRATEGY = "current_splash_strategy";
    public static final String PREF_SUCCESS_SPLASH_STRATEGY = "success_splash_strategy";
    public static final String PREF_FAIL_SPLASH_STRATEGY = "fail_splash_strategy";
    public static final String PREF_LAST_LOAD_SPLASH_TIME = "last_load_splash_time";
    public static final String PREF_SPLASH_LOAD_FAIL_DATE = "splash_load_fail_date";
    public static final String PREF_SPLASH_LOAD_FAIL_NUMBER = "splash_load_fail_number";

    public static final String PREF_AD_LOCK_WALL = "ad_lock_wall";

    // lock mode
    public static final String PREF_FIRST_USE_LOCK_MODE = "first_use_lock_mode";
    public static final String PREF_SPLASH_URL_FLAG = "splash_url_flag";
    public static final String PREF_REMOVE_UNLOCK_ALL_SHORTCUT_FLAG = "remove_unlock_all_shortcut";
    public static final String PREF_SAVE_SPLASH_MEMERY_NO_ENOUGH = "save_splash_memery_no_enough";
    public static final String PREF_FIRST_INSTALL_APP = "first_install_app";
    public static final String PREF_APP_VERSION_NAME = "app_version_name";
    public static final String PREF_QUICK_FIRST_SLIDING_TIP = "quick_first_sliding_tip";
    public static final String PREF_UPDATE_QUICK_GESTURE_USER = "update_quick_gesture_user";
    public static final String PREF_LAST_BUSINESS_RED_TIP = "last_business_red_tip";
    public static final String PREF_SPLASH_SKIP_URL = "splash_skip_url";
    public static final String PREF_SPLASH_SKIP_MODE = "splash_skip_mode";
    public static final String PREF_SPLASH_DElAY_TIME = "splash_delay_time";
    public static final String PREF_SPLASH_SKIP_TO_CLIENT = "splash_skip_to_client";

    public static final String PREF_FOREGROUND_SCORE = "foreground_score";



    public static final String PREF_UNLOCK_SUCCESS_TIP_RANDOM = "unlock_success_tip_random";
    // private boolean mLockerScreenThemeGuide = false;
    public static final int LOCK_TYPE_NONE = -1;
    private int mLockType = LOCK_TYPE_NONE;

    private boolean mUnlocked = true;
    private String mDoubleCheck = null;

    private long mLastShowTime = -1;

    private long mThemeSuccessStrategy = -1;
    private long mThemeFailStrategy = -1;
    private long mCurrentThemeStrategy = -1;

    private long mBusinessSuccessStrategy = -1;
    private long mBusinessFailStrategy = -1;
    private long mCurrentBusinessStrategy = -1;
    private long mSplashSuccessStrategy = -1;
    private long mSplashFailStrategy = -1;
    private long mCurrentSplashStrategy = -1;
    private String mLocalThemeSerial = null;
    private String mLocalBusinessSerial = null;
    private long mLastCheckBusinessTime = -1;
    private long mUnlockCount = -1;
    private long mLastUBCTime = -1;
    private int mRelockTimeOut = -1;
    private static AppMasterPreference mInstance;
    private String mSplashSkipMode = null;
    private String mSplashSkipUrl = null;
    private long mSplashStartShowTime = -1;
    private long mSplashEndShowTime = -1;
    private int mForegroundScore = -1;
    private Executor mSerialExecutor;
    private HashMap<String, Object> mValues;

    private long mNewUserUnlockCount = -1;

    private AppMasterPreference(Context context) {
        mSerialExecutor = ThreadManager.newSerialExecutor();

        mValues = new HashMap<String, Object>();
        Context ctx = context.getApplicationContext();
        loadPreferences();
        loadDb();
    }

    private void loadDb() {
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                LeoLog.d("EngineLock", "get recomment from loadDb");
                LockRecommentTable table = new LockRecommentTable();
                HashMap<String, String> map = table.queryLockRecommentList();
                int i = 0;
                String lockName = "", lockNameNum = "";
                Iterator iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    if (i == 0) {
                        lockName = key;
                        lockNameNum = value;
                    } else {
                        lockName = lockName + ";" + key;
                        lockNameNum = lockNameNum + ";" + value;
                    }
                    i++;
                }

            }
        });
    }

    public static synchronized AppMasterPreference getInstance(Context context) {
        return mInstance == null ? (mInstance = new AppMasterPreference(context))
                : mInstance;
    }


    public long getBusinessSuccessStrategy() {
        if (mBusinessSuccessStrategy < 0) {
            mBusinessSuccessStrategy = LeoSettings.getLong(PREF_BUSINESS_SUCCESS_STRATEGY,
                    com.zlf.appmaster.AppMasterConfig.TIME_12_HOUR);
        }
        return mBusinessSuccessStrategy;
    }

    public long getBusinessFailStrategy() {
        if (mBusinessFailStrategy < 0) {
            mBusinessFailStrategy = LeoSettings.getLong(PREF_BUSINESS_FAIL_STRATEGY,
                    com.zlf.appmaster.AppMasterConfig.TIME_2_HOUR);
        }
        return mBusinessFailStrategy;
    }

    public long getBusinessCurrentStrategy() {
        if (mCurrentBusinessStrategy < 0) {
            mCurrentBusinessStrategy = LeoSettings.getLong(PREF_CURRENT_BUSINESS_STRATEGY,
                    com.zlf.appmaster.AppMasterConfig.TIME_2_HOUR);
        }
        return mCurrentBusinessStrategy;
    }

    public void setBusinessStrategy(long currentStrategy, long successStrategy, long failStrategy) {
        Editor editor = null;
        if (mCurrentBusinessStrategy != currentStrategy) {
            mCurrentBusinessStrategy = currentStrategy;
            LeoSettings.setLong(PREF_CURRENT_BUSINESS_STRATEGY, currentStrategy);
        }
        if (mBusinessSuccessStrategy != successStrategy) {
            mBusinessSuccessStrategy = successStrategy;
            LeoSettings.setLong(PREF_BUSINESS_SUCCESS_STRATEGY, successStrategy);
        }
        if (mBusinessFailStrategy != failStrategy) {
            mBusinessFailStrategy = failStrategy;
            LeoSettings.setLong(PREF_BUSINESS_FAIL_STRATEGY, failStrategy);
        }
    }

    public long getThemeSuccessStrategy() {
        if (mThemeSuccessStrategy < 0) {
            mThemeSuccessStrategy = LeoSettings.getLong(PREF_THEME_SUCCESS_STRATEGY,
                    com.zlf.appmaster.AppMasterConfig.TIME_12_HOUR);
        }
        return mThemeSuccessStrategy;
    }

    public long getThemeFailStrategy() {
        if (mThemeFailStrategy < 0) {
            mThemeFailStrategy = LeoSettings.getLong(PREF_THEME_FAIL_STRATEGY,
                    com.zlf.appmaster.AppMasterConfig.TIME_2_HOUR);
        }
        return mThemeFailStrategy;
    }


    public void setThemeStrategy(long currentStrategy, long successStrategy, long failStrategy) {
        Editor editor = null;
        if (mCurrentThemeStrategy != currentStrategy) {
            mCurrentThemeStrategy = currentStrategy;
            LeoSettings.setLong(PREF_CURRENT_THEME_STRATEGY, currentStrategy);
        }
        if (mThemeSuccessStrategy != successStrategy) {
            mThemeSuccessStrategy = successStrategy;
            LeoSettings.setLong(PREF_THEME_SUCCESS_STRATEGY, successStrategy);
        }
        if (mThemeFailStrategy != failStrategy) {
            mThemeFailStrategy = failStrategy;
            LeoSettings.setLong(PREF_THEME_FAIL_STRATEGY, failStrategy);
        }

        if (mCurrentThemeStrategy != currentStrategy) {
            mCurrentThemeStrategy = currentStrategy;
            commitAsync(PREF_CURRENT_THEME_STRATEGY, currentStrategy);
        }
    }

    public long getThemeLastShowTime() {
        if (mLastShowTime < 0) {
            mLastShowTime = LeoSettings.getLong(PREF_SHOW_TIP_KEY, 0);
        }
        return mLastShowTime;
    }

    public void setThemeLastShowTime(long lastShowTime) {
        mLastShowTime = lastShowTime;
        commitAsync(PREF_SHOW_TIP_KEY, lastShowTime);
    }

    public long getHotAppLastShowTime() {
        if (mLastShowTime < 0) {
            mLastShowTime = LeoSettings.getLong(PREF_SHOW_TIP_HOT_APP_KEY, 0);
        }
        return mLastShowTime;
    }

    public void setHotAppLastShowTime(long lastShowTime) {
        mLastShowTime = lastShowTime;
        commitAsync(PREF_SHOW_TIP_HOT_APP_KEY, lastShowTime);
    }

    public long getLastUBCTime() {
        if (mLastUBCTime < 0) {
            mLastUBCTime = LeoSettings.getLong(PREF_LAST_UBC, 0);
        }
        return mLastUBCTime;
    }

    public void setLastUBCTime(long time) {
        mLastUBCTime = time;
        commitAsync(PREF_LAST_UBC, time);
    }



    public void setHomeBusinessTipClick(boolean flag) {
        commitAsync(PREF_HOME_BUSINESS_NEW_TIP_CLICK, flag);
    }


    public String getLocalThemeSerialNumber() {
        if (mLocalThemeSerial == null) {
            mLocalThemeSerial = LeoSettings.getString(PREF_LOCAL_THEME_SERIAL, "");
        }
        return mLocalThemeSerial;
    }


    public long getLastCheckBusinessTime() {
        if (mLastCheckBusinessTime < 0) {
            mLastCheckBusinessTime = LeoSettings.getLong(PREF_LAST_CHECK_NEW_BUSINESS_APP_TIME, 0);
        }
        return mLastCheckBusinessTime;
    }

    public void setLastCheckBusinessTime(long lastTime) {
        mLastCheckBusinessTime = lastTime;
        commitAsync(PREF_LAST_CHECK_NEW_BUSINESS_APP_TIME, lastTime);
    }


    public void setOnlineBusinessSerialNumber(String serial) {

    }

    public String getLocalBusinessSerialNumber() {
        if (mLocalBusinessSerial == null) {
            mLocalBusinessSerial = LeoSettings.getString(PREF_LOCAL_BUSINESS_SERIAL, "");
        }
        return mLocalBusinessSerial;
    }

    public void setLocalBusinessSerialNumber(String serial) {
        mLocalBusinessSerial = serial;
        commitAsync(PREF_LOCAL_BUSINESS_SERIAL, serial);
    }


    public void setGoogleTipShowed(boolean show) {
        commitAsync(PREF_GUIDE_TIP_SHOW, show);
    }

    public void setUnlockCount(long count) {
        mUnlockCount = count;
        commitAsync(PREF_UNLOCK_COUNT, count);
    }


    public void setLastVersion(String lastVersion) {
        commitAsync(PREF_LAST_VERSION, lastVersion);
    }

    public String getLastVersion() {
        return LeoSettings.getString(PREF_LAST_VERSION, "");
    }

    public void setLastGuideVersion(int lastVersion) {
        commitAsync(PREF_LAST_GUIDE_VERSION, lastVersion);
    }

    public int getLastGuideVersion() {
        return LeoSettings.getInteger(PREF_LAST_GUIDE_VERSION, 0);
    }

    public boolean isNewAppLockTip() {
        return LeoSettings.getBoolean(PREF_NEW_APP_LOCK_TIP, false);
    }

    public void setNewAppLockTip(boolean b) {
         LeoLog.i("value", "b :" + b);
         LeoSettings.setBoolean(PREF_NEW_APP_LOCK_TIP, b);
    }


    public void setGuidePageFirstUse(boolean flag) {
        commitAsync(PREF_FIRST_USE_APP, flag);
    }

    public int getRelockTimeout() {
        if (mRelockTimeOut < 0) {
            String time = LeoSettings.getString(PREF_RELOCK_TIME, "0");
            try {
                mRelockTimeOut = Integer.parseInt(time) * 1000;
            } catch (Exception e) {
                mRelockTimeOut = 0;
            }
        }
        return mRelockTimeOut;
    }


    public int getLockType() {
        return mLockType;
    }




    private void loadPreferences() {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
    }



    public boolean getUnlocked() {
        return mUnlocked;
    }


    public String getDoubleCheck() {
        return mDoubleCheck;
    }


    public void setFromOther(boolean flag) {
//        mFromOther = flag;
    }


    public void setSplashStartShowTime(long time) {
        mSplashStartShowTime = time;
        commitAsync(PREF_SPLASH_START_SHOW_TIME, time);
    }

    public long getSplashStartShowTime() {
        if (mSplashStartShowTime < 0) {
            mSplashStartShowTime = LeoSettings.getLong(PREF_SPLASH_START_SHOW_TIME, -1);
        }
        return mSplashStartShowTime;
    }

    public void setSplashEndShowTime(long time) {
        mSplashEndShowTime = time;
        commitAsync(PREF_SPLASH_END_SHOW_TIME, time);
    }

    public long getSplashEndShowTime() {
        if (mSplashEndShowTime < 0) {
            mSplashEndShowTime = LeoSettings.getLong(PREF_SPLASH_END_SHOW_TIME, -1);
        }
        return mSplashEndShowTime;
    }

    public void setLoadSplashStrategy(long currentStrategy, long successStrategy, long failStrategy) {
        Editor editor = null;
        if (mCurrentSplashStrategy != currentStrategy) {
            mCurrentSplashStrategy = currentStrategy;
            LeoSettings.setLong(PREF_CURRENT_SPLASH_STRATEGY, currentStrategy);
        }
        if (mSplashSuccessStrategy != successStrategy) {
            mSplashSuccessStrategy = successStrategy;
            LeoSettings.setLong(PREF_SUCCESS_SPLASH_STRATEGY, successStrategy);
        }
        if (mSplashFailStrategy != failStrategy) {
            mSplashFailStrategy = failStrategy;
            LeoSettings.setLong(PREF_FAIL_SPLASH_STRATEGY, failStrategy);
        }
    }

    public long getSplashSuccessStrategy() {
        if (mSplashSuccessStrategy < 0) {
            mSplashSuccessStrategy = LeoSettings.getLong(PREF_SUCCESS_SPLASH_STRATEGY,
                    com.zlf.appmaster.AppMasterConfig.TIME_12_HOUR);
        }
        return mSplashSuccessStrategy;
    }

    public long getSplashFailStrategy() {
        if (mSplashFailStrategy < 0) {
            mSplashFailStrategy = LeoSettings.getLong(PREF_FAIL_SPLASH_STRATEGY,
                    com.zlf.appmaster.AppMasterConfig.TIME_2_HOUR);
        }

        return mSplashFailStrategy;
    }

    public long getSplashCurrentStrategy() {
        if (mCurrentSplashStrategy < 0) {
            mCurrentSplashStrategy = LeoSettings.getLong(PREF_CURRENT_SPLASH_STRATEGY,
                    com.zlf.appmaster.AppMasterConfig.TIME_2_HOUR);
        }
        return mCurrentSplashStrategy;
    }

    /* 加载闪屏失败的时间 */
    public void setLastLoadSplashTime(long lashTime) {
        commitAsync(PREF_LAST_LOAD_SPLASH_TIME, lashTime);
    }

    public long getLastLoadSplashTime() {
        return LeoSettings.getLong(PREF_LAST_LOAD_SPLASH_TIME, 0);
    }

    /* 加载闪屏首次失败的当天日期 */
    public void setSplashLoadFailDate(String date) {
        commitAsync(PREF_SPLASH_LOAD_FAIL_DATE, date);
    }

    public String getSplashLoadFailDate() {
        return LeoSettings.getString(PREF_SPLASH_LOAD_FAIL_DATE, Constants.SPLASH_REQUEST_FAIL_DATE);
    }

    /* 加载闪屏当天失败的次数 */
    public void setSplashLoadFailNumber(int number) {
        commitAsync(PREF_SPLASH_LOAD_FAIL_NUMBER, number);
    }

    public int getSplashLoadFailNumber() {
        return LeoSettings.getInteger(PREF_SPLASH_LOAD_FAIL_NUMBER, 0);
    }

    public void setSplashUriFlag(String string) {
        commitAsync(PREF_SPLASH_URL_FLAG, string);
    }

    public String getSplashUriFlag() {
        return LeoSettings.getString(PREF_SPLASH_URL_FLAG, Constants.SPLASH_FLAG);
    }


    public void setSaveSplashIsMemeryEnough(int flag) {
        commitAsync(PREF_SAVE_SPLASH_MEMERY_NO_ENOUGH, flag);
    }

    public int getSaveSplashIsMemeryEnough() {
        return LeoSettings.getInteger(PREF_SAVE_SPLASH_MEMERY_NO_ENOUGH, -1);
    }


    public void setIsFirstInstallApp(boolean flag) {
        commitAsync(PREF_FIRST_INSTALL_APP, flag);
    }

    public boolean getIsFirstInstallApp() {
        return LeoSettings.getBoolean(PREF_FIRST_INSTALL_APP, true);
    }

    public void setAppVersionName(String name) {
        commitAsync(PREF_APP_VERSION_NAME, name);
    }




    public void setIsUpdateQuickGestureUser(boolean flag) {
        commitAsync(PREF_UPDATE_QUICK_GESTURE_USER,
                flag);
    }




    /* 保存闪屏跳转链接 */
    public void setSplashSkipUrl(String url) {
        mSplashSkipUrl = url;
        commitAsync(PREF_SPLASH_SKIP_URL, url);
    }

    public String getSplashSkipUrl() {
        if (mSplashSkipUrl == null) {
            mSplashSkipUrl = LeoSettings.getString(PREF_SPLASH_SKIP_URL, null);
        }
        return mSplashSkipUrl;
        // return LeoSettings.getString(PREF_SPLASH_SKIP_URL,
        // "market://details?id=com.leo.appmaster&referrer=utm_source=AppMaster");
    }

    /* 保存闪屏跳转方式 */
    public void setSplashSkipMode(String flag) {
        mSplashSkipMode = flag;
        commitAsync(PREF_SPLASH_SKIP_MODE, flag);
    }

    public String getSplashSkipMode() {
        if (mSplashSkipMode == null) {
            mSplashSkipMode = LeoSettings.getString(PREF_SPLASH_SKIP_MODE,
                    Constants.SPLASH_SKIP_PG_WEBVIEW);
        }
        return mSplashSkipMode;
    }

    /* 保存闪屏延迟时间 */
    public void setSplashDelayTime(int delayTime) {
        commitAsync(PREF_SPLASH_DElAY_TIME, delayTime);
    }

    public int getSplashDelayTime() {
        return LeoSettings.getInteger(PREF_SPLASH_DElAY_TIME, Constants.SPLASH_DELAY_TIME);
    }

    /* 保存闪屏跳转的客户端的链接 */
    public void setSplashSkipToClient(String clientUrl) {
        commitAsync(PREF_SPLASH_SKIP_TO_CLIENT, clientUrl);
    }

    public String getSplashSkipToClient() {
        return LeoSettings.getString(PREF_SPLASH_SKIP_TO_CLIENT, null);
    }

    public int getForegroundScore() {
        if (mForegroundScore < 0) {
            mForegroundScore = LeoSettings.getInteger(PREF_FOREGROUND_SCORE, 0);
        }
        return mForegroundScore;
    }

    public void setForegroundScore(int score) {
        mForegroundScore = score;
        commitAsync(PREF_FOREGROUND_SCORE, score);
    }


    public void setIsNeedCutBackupUninstallAndPrivacyContact(boolean value) {
        commitAsync(PREF_NEED_CUT_BACKUP_UNINSTALL_AND_PRIVACYCONTRACT, value);
    }



    /**
     * 设置定时任务执行的时间
     *
     * @param jobKey
     * @param time
     */
    public void setScheduleTime(String jobKey, long time) {
        mValues.put(jobKey, time);
        commitAsync(jobKey, time);
    }

    public long getScheduleTime(String jobKey) {
        Long obj = (Long) mValues.get(jobKey);
        if (obj == null) {
            obj = LeoSettings.getLong(jobKey, 0);
        }
        mValues.put(jobKey, obj);
        return obj;
    }

    public void setScheduleValue(String key, int state) {
        mValues.put(key, state);
        commitAsync(key, state);
    }

    public int getScheduleValue(String key, int def) {
        Integer obj = (Integer) mValues.get(key);
        if (obj == null) {
            obj = LeoSettings.getInteger(key, 0);
        }
        mValues.put(key, obj);
        return obj;
    }

    public void commitAsync(final Editor editor) {
        if (editor == null)
            return;

        mSerialExecutor.execute(new Runnable() {
            @Override
            public void run() {
                editor.commit();
            }
        });
    }

    private void commitAsync(String key, Object value) {
        LeoSettings.setString(key, String.valueOf(value));
    }

    // TODO
    public void setNewUserUnlockCount(long count) {
        mNewUserUnlockCount = count;
        commitAsync(PREF_NEW_USER_UNLOCK_COUNT, count);
    }

    /* 保存有升级更新时是否已经对升级解锁提示数据初始化 */
    public void setUpdateRecoveryDefaultData(boolean flag) {

    }

}
