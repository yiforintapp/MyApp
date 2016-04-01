
package com.leo.appmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.manager.ADShowTypeRequestManager;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.db.LockRecommentTable;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 旧版本已数据存储，新版本不再继续使用，新数据统一使用LeoSettings
 */
@Deprecated
public class AppMasterPreference implements OnSharedPreferenceChangeListener {

    // about lock
    private static final String PREF_APPLICATION_LIST = "application_list";
    public static final String PREF_LOCK_TYPE = "lock_type";
    public static final String PREF_PASSWORD = "password";
    public static final String PREF_GESTURE = "gesture";
    private static final String PREF_LOCK_POLICY = "lock_policy";
    private static final String PREF_HAVE_PSWD_PROTECTED = "have_setted_pswd";
    private static final String PREF_PASSWD_QUESTION = "passwd_question";
    private static final String PREF_PASSWD_ANWSER = "passwd_anwser";
    private static final String PREF_PASSWD_TIP = "passwd_tip";
    public static final String PREF_LOCKER_THEME = "set_locker_theme";
    public static final String PREF_HIDE_LOCK_LINE = "app_hide_lockline";
    public static final String PREF_RELOCK_TIME = "relock_time";
    public static final String PREF_AUTO_LOCK = "set_auto_lock";
    public static final String PREF_SET_PROTECT = "set_passwd_protect";
    public static final String PREF_FORBIND_UNINSTALL = "set_forbid_uninstall";
    public static final String PREF_NEW_APP_LOCK_TIP = "new_app_lock_tip";
    public static final String PREF_LAST_ALARM_SET_TIME = "last_alarm_set_time";
    public static final String PREF_RECOMMEND_LOCK_PERCENT = "recommend_lock_percent";
    public static final String PREF_UNLOCK_COUNT = "unlock_count";
    public static final String PREF_NEW_USER_UNLOCK_COUNT = "new_user_unlock_count";
    public static final String PREF_GUIDE_TIP_SHOW = "google_play_guide_tip_show";
    public static final String PREF_HIDE_THEME_PKGS = "hide_theme_packages";
    public static final String PREF_HAVE_EVER_LOAD_APPS = "have_ever_load_apps";
    public static final String PREF_SETTING_LOCKER_CLEAN = "setting_locker_clean";
    public static final String PREF_THEME_LOCK_GUIDE = "theme_locker_guide";
    public static final String PREF_USE_LOCK_THEME_GUIDE = "use_lock_theme_guid";
    public static final String PREF_NEED_CUT_BACKUP_UNINSTALL_AND_PRIVACYCONTRACT = "need_cut_backup_uninstall_and_privacycontract";

    public static final String PREF_LOCK_SETTING = "lock_setting";

    public static final String PREF_LAST_FILTER_SELF_TIME = "last_filter_self_time";

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

    public static final String PREF_APP_MANAGER_FLOW_TOTAL_TRAFFIC = "totalflow";
    public static final String PREF_APP_MANAGER_FLOW_MAKE_ITSELF_MONTH_TRAFFIC = "make_itself_month_traffic";
    public static final String PREF_APP_MANAGER_FLOW_MAKE_ITSELF_TODAY_BASE = "make_itself_today_base_traffic";

    public static final String PREF_APP_MANAGER_FLOW_MONTH_ALL = "mouth_gprs_all";
    public static final String PREF_APP_MANAGER_FLOW_MONTH_BASE = "mouth_gprs_base";
    public static final String PREF_APP_MANAGER_FLOW_YEAR_TRAF = "year_app_traf";
    public static final String PREF_APP_MANAGER_FLOW_MONTH_TRAF = "month_app_traf";
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
    public static final String PREF_SHOW_TIP_KEY = "last_show_tip_time";
    public static final String PREF_SHOW_TIP_HOT_APP_KEY = "last_hot_app_show_tip_time";
    public static final String PREF_THEME_SUCCESS_STRATEGY = "theme_success_strategy";
    public static final String PREF_THEME_FAIL_STRATEGY = "theme_fail_strategy";
    public static final String PREF_CURRENT_THEME_STRATEGY = "theme_current_strategy";
    public static final String PREF_BUSINESS_SUCCESS_STRATEGY = "business_success_strategy";
    public static final String PREF_BUSINESS_FAIL_STRATEGY = "business_fail_strategy";
    public static final String PREF_CURRENT_BUSINESS_STRATEGY = "business_current_strategy";
    public static final String PREF_MESSAGE_ITEM_RUNING = "message_item_runing";
    public static final String PREF_CALL_LOG_ITEM_RUNING = "call_log_item_runing";
    public static final String PREF_SPLASH_START_SHOW_TIME = "splash_start_show_time";
    public static final String PREF_SPLASH_END_SHOW_TIME = "splash_end_show_time";
    public static final String PREF_CURRENT_SPLASH_STRATEGY = "current_splash_strategy";
    public static final String PREF_SUCCESS_SPLASH_STRATEGY = "success_splash_strategy";
    public static final String PREF_FAIL_SPLASH_STRATEGY = "fail_splash_strategy";
    public static final String PREF_LAST_LOAD_SPLASH_TIME = "last_load_splash_time";
    public static final String PREF_MESSAGE_NO_READ_COUNT = "message_no_read_count";
    public static final String PREF_CALL_LOG_NO_READ_COUNT = "call_log_no_read_count";
    public static final String PREF_SPLASH_LOAD_FAIL_DATE = "splash_load_fail_date";
    public static final String PREF_SPLASH_LOAD_FAIL_NUMBER = "splash_load_fail_number";
    // weizhuang
    public static final String PREF_DETERMIN_CLOSE_BEAUTY = "determin_close_beauty";
    public static final String PREF_WEIZHUANG_FIRST_IN = "weizhuang_first_in";
    public static final String PREF_CUR_PRETNED_LOCK = "cur_pretend_lock";
    public static final String PREF_NEED_CLOSE_BEAUTY = "need_close_beauty";

    // hideVideo
    public static final String PREF_HIDE_VIDEO_LAST_DIR = "hide_video_last_dir";
    public static final String PREF_HIDE_VIDEO_SECOND_DIR = "hide_video_second_dir";

    // ad icon -- set click time
    public static final String PREF_AD_ICON_CLICK_TIME = "ad_icon_click_time";
    public static final String PREF_AD_ICON_ET_CLICK_TIME = "et_icon_click_time";
    public static final String PREF_AD_BANNER_BOTTOM = "bottom_banner";
    public static final String PREF_AD_HALF_SCREEN_BANNER = "half_screen_banner";
    public static final String PREF_AD_ICON_JUMP_CLICKED = "click_jump_adicon";
    public static final String PREF_AD_ICON_FROM_HOME = "click_home_ad_icom";

    // ad desk icon
    public static final String PREF_AD_ICON_DESK = "ad_icon_desk";

    public static final String PREF_AD_LOCK_WALL = "ad_lock_wall";
    public static final String PREF_LOCK_AND_AUTO_START_GUIDE = "lock_and_auto_start_guide";
    public static final String PREF_SUPERMAN_AD_BANNER_BOTTOM = "superman_bottom_banner";
    public static final String PREF_SUBMARIN_AD_CLICK_TIME = "submarine_ad_click_time";

    // time to show notify that clean memory
    public static final String PREF_SHOW_NOTIFY_CLEAN_MEMORY = "show_notify_clean_memory";
    // lock mode
    public static final String PREF_FIRST_USE_LOCK_MODE = "first_use_lock_mode";
    private static final String PREF_TIME_LOCK_MODE_GUIDE_USER_CLICKED = "time_lock_mode_guide_user_clicked";
    private static final String PREF_LOCATION_LOCK_MODE_GUIDE_USER_CLICKED = "location_lock_mode_guide_user_clicked";
    public static final String PREF_SWITCH_MODE_COUNT = "switch_lock_mode_count";
    public static final String PREF_SPLASH_URL_FLAG = "splash_url_flag";
    public static final String PREF_REMOVE_UNLOCK_ALL_SHORTCUT_FLAG = "remove_unlock_all_shortcut";
    public static final String PREF_SAVE_SPLASH_MEMERY_NO_ENOUGH = "save_splash_memery_no_enough";
    public static final String PREF_TIME_LOCK_MODE_SET_OVER = "time_lock_mode_set_over";
    public static final String PREF_LOCATION_LOCK_MODE_SET_OVER = "location_lock_mode_set_over";
    public static final String PREF_FIRST_INSTALL_APP = "first_install_app";
    public static final String PREF_APP_VERSION_NAME = "app_version_name";
    public static final String PREF_SWITCH_OPEN_NO_READ_MESSAGE_TIP = "switch_open_no_read_message_tip";
    public static final String PREF_SWITCH_OPEN_RECENTLY_CONTACT = "switch_open_recently_contact";
    public static final String PREF_SWITCH_OPEN_PRIVACY_CONTACT_MESSAGE_TIP = "switch_open_privacy_contact_message_tip";
    public static final String PREF_QUICK_FIRST_SLIDING_TIP = "quick_first_sliding_tip";
    public static final String PREF_UPDATE_QUICK_GESTURE_USER = "update_quick_gesture_user";
    public static final String PREF_LAST_BUSINESS_RED_TIP = "last_business_red_tip";
    public static final String PREF_QUICK_NO_MSM_TIP = "quick_no_msm_tip";
    public static final String PREF_QUICK_NO_CALL_LOG_TIP = "quick_no_call_log_tip";
    public static final String PREF_ENTER_HOME_TIMES = "enter_home_times";
    public static final String PREF_IF_LOCK_SCREEN_MENU_CLICKED = "if_menu_clicked";
    public static final String PREF_LAST_BOOST_TIMES = "last_boost_times";
    public static final String PREF_LAST_BOOST_WITH_AD_TIMES = "last_boost_with_ad_times";
    public static final String PREF_SPLASH_SKIP_URL = "splash_skip_url";
    public static final String PREF_SPLASH_SKIP_MODE = "splash_skip_mode";
    public static final String PREF_SPLASH_DElAY_TIME = "splash_delay_time";
    public static final String PREF_SPLASH_SKIP_TO_CLIENT = "splash_skip_to_client";

    public static final String PREF_FOREGROUND_SCORE = "foreground_score";

    public static final String PREF_ISWIP_UPDATE_FALG = "iswip_update";
    public static final String PREF_ISWIP_UPDATE_TIP_FRE = "iswip_update_tip_fre";
    public static final String PREF_ISWIP_UPDATE_TIP_NUMBER = "iswip_update_tip_number";
    public static final String PREF_ISWIP_UPDATE_GP_URL = "iswip_update_gp_url";
    public static final String PREF_ISWIP_UPDATE_BROWSER_URL = "iswip_update_browser_url";
    public static final String PREF_ISWIP_UPDATE_DOWN_TYPE = "iswip_update_down_type";
    public static final String PREF_ISWIP_UPDATE_LOADING_LAST_TIME = "iswip_last_loading_time";
    public static final String PREF_ISWIP_UPDATE_LOADING_STRATEGT = "iswip_loading_strategy";
    public static final String PREF_ISWIP_UPDATE_LOADING_NUMBER = "iswip_update_loading_number";
    public static final String PREF_ISWIP_LOAD_FAIL_DATE = "iswip_load_fial_date";
    public static final String PREF_ISWIPE_ALARM_NORI_NUMBER = "iswipe_alarm_noti_number";
    public static final String PREF_ISWIPE_TIP_LAST_TIME = "iswipe_tip_last_time";
    // About AD
    public static final String PREF_AD_AT_APPLOCK_FRAGMENT = "ad_at_applock_fragment";
    public static final String PREF_AD_AT_THEME = "ad_at_theme";
    public static final String PREF_GIFTBOX_UPDATE = "giftbox_update";
    public static final String PREF_VERSION_UPDATE_AFTER_UNLOCK = "version_update_after_unlock";

    public static final String PREF_APP_STATISTICS = "app_statistic";
    public static final String PREF_APP_STATISTICS_LASTTIME = "app_statistics_lasttime";

    public static final String PREF_APP_WIFI_STATISTICS = "wifi_statistics";

    public static final String PREF_AD_AFTER_PRIVACY_PROTECTION = "ad_after_privacy_protection";
    public static final String PREF_AD_AFTER_ACCELERATING = "ad_after_accelerating";
    public static final String PREF_AD_WIFI_SCAN_RESULT = "ad_wifi_scan_result";
    public static final String PREF_THEME_CHANCE_AFTER_UFO = "theme_chance_after_ufo";
    public static final String PREF_AD_REQUEST_SHOWTYPE_LAST_TIME = "ad_request_showtype_last_time";
    public static final String PREF_AD_REQUEST_SHOWTYPE_FAIL_TIMES_CURRENT_DAY = "ad_request_showtype_fail_times_current_day";
    public static final String PREF_AD_REQUEST_SHOWTYPE_NEXT_TIME_SPACING = "ad_request_showtype_next_time_spacing";
    public static final String PREF_AD_SHOW_TYPE = "ad_show_type";
    public static final String PREF_AD_FETCH_INTERVAL = "ad_fetch_interval";
    public static final String PREF_AD_MAINLAND_SWITCHER = "ad_mainland_switcher";
    public static final String PREF_LARGE_AD_SHOW_PROBABILITY = "ad_large_show_probability";
    public static final String PREF_AD_APPWAL_UPDATE = "ad_appwall_update";
    // 3.2 ad
    public static final String PREF_AD_INTRUDER = "pref_ad_intruder";
    public static final String PREF_AD_AFTER_SCAN = "pref_ad_after_scan";
    // 3.3 屏保广告位开关
    public static final String PREF_AD_ON_SCREEN_SAVER = "pref_ad_on_screen_saver";
    // 3.3 耗电app数量阈值
    public static final String PREF_BATTERY_APP_NUM = "pref_battery_app_num";
    /* 3.3.1 气泡出现次数 */
    public static final String PREF_SCREEN_SAVE_BUBBLE_COUNT = "pref_screen_save_bubble_count";


    /* 3.3.2 外部广告sdk源*/
    public static final int AD_SDK_SOURCE_USE_3TH = 1;
    /* 3.3.2 max广告sdk源 */
    public static final int AD_SDK_SOURCE_USE_MAX = 2;
    /* 3.3.2 LockScreen  广告位1 使用广告sdk源 */
    public static final String AD_IN_LOCK_SCREEN_SDK_SOURCE = "ad_in_lock_screen_sdk_source";
    /* 3.3.2 acceleration 桌面加速 广告位 使用广告sdk源 */
    public static final String AD_IN_ACCELERATION_SDK_SOURCE = "ad_in_acceleration_sdk_source";
    /* 3.3.2 invader 入侵者 广告位 使用广告sdk源 */
    public static final String AD_IN_INVADER_SDK_SOURCE = "ad_in_invader_sdk_source";
    /* 3.3.2 charging 充电 广告位 使用广告sdk 源 */
    public static final String AD_CHARGING_SDK_SOURCE = "ad_charging_sdk_source";

    public static final String PREF_MOBVISTA_LOADED = "mobvista_loaded";
    public static final String PREF_UNLOCK_UPDATE_FIRST_RANDOM = "unlock_update_first_random";
    public static final String PREF_UNLOCK_UPDATE_TIP_COUNT = "unlock_update_tip_count";
    public static final String PREF_UNLOCK_UPDATE_TIP_COUNT_RECORD = "unlock_update_count_record";
    public static final String PREF_UNLOCK_SUCCESS_TIP_RANDOM = "unlock_success_tip_random";
    public static final String PREF_UPDATE_TIP_DATE = "update_tip_date";
    public static final String PREF_UPDATE_SECOND_TIP_FLAG = "update_second_tip";
    public static final String PREF_RECORD_FIRST_UNLOCK_COUNT = "record_first_unlock_count";
    public static final String PREF_RECORD_CHANGE_DATE_UNLOCK_COUNT = "record_change_date_unlock_count";
    public static final String PREF_UPDATE_RECOVERY_DEFAULT_DATA = "update_recovery_defatult_data";
    public static final String PREF_PG_IS_FORCE_UPDATE = "pg_is_force_update";
    public static final String PREF_RANDOM_IN_30_WITHIN = "randoom_in_30_within";
    public static final String PREF_ADVANCE_PROTECT_OPEN_SUCCESSDIALOG_TIP = "advance_protect_open_success_dialog_tip";
    public static final int OPEN_FLAG = 1;
    public static final int DEFAULT_FETCH_INTERAL = 10; // in minutes
    public static final int DEFAULT_SWITCHER_STATE = 0;
    // 3.3 耗电app数量阈值
    private static final int DEFAULT_APP_NUM_THRESHOLD = 20;
    private List<String> mRecommendList;
    private List<String> mRecommendNumList;
    private List<String> mHideThemeList;
    private String mPassword;
    private String mGesture;
    private List<String> mRecommentAppList;
    // private boolean mLockerScreenThemeGuide = false;
    public static final int LOCK_TYPE_NONE = -1;
    public static final int LOCK_TYPE_PASSWD = 0;
    public static final int LOCK_TYPE_GESTURE = 1;
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
    private String mOnlineThemeSerial = null;
    private String mLocalThemeSerial = null;
    private String mOnlineBusinessSerial = null;
    private String mLocalBusinessSerial = null;
    private long mLastCheckBusinessTime = -1;
    private long mLastCheckThemeTime = -1;
    private long mLastSyncBusinessTime = -1;
    private long mUnlockCount = -1;
    private long mLastUBCTime = -1;
    private int mRelockTimeOut = -1;
    private long mMonthGprsAll = -1;
    private long mItSelfTodayBase = -1;
    private long mMonthGprsBase = -1;
    private int mYearAppTraf = -1;
    private int mMonthAppTraf = -1;
    private long mGprsSend = -1;
    private long mGprsRev = -1;
    private long mBaseSend = -1;
    private long mBaseRev = -1;
    private int mRenewDay = -1;
    private int mTotalTraffic = -1;
    private int mUsedTraffic = -1;
    private long mItselfMonthTraffic = -1;
    private int mPretendLock = -1;
    private long mNewUserUnlockCount = -1;
    //    private SharedPreferences mPref;
    private static AppMasterPreference mInstance;
    private int mEnterHomeTimes = -1;
    private int mUseStrengthModeTimes, mGestureSlideAnimShowTimes, mLastTimeLayout = -1;

    private int mADShowType = -1;
    private int mADFetchInterval = -1;
    private int mMainlandSwitcher = -1;
    private int mLargeADShowProbability = -1;
    private int mADRequestFailTimes = -1;
    private long mADRequestInternal = -1;
    private long mADLastRequestTime = -1;
    private long mADLastLoadTime = -1;
    private long mISwipeLoadTime = -1;
    private int mISwipeAlarm = -1;
    private String mSplashSkipMode = null;
    private String mSplashSkipUrl = null;
    private Boolean mIsHideLine = null;
    private String mLastDir = null;
    private String mSecondDir = null;
    private long mSplashStartShowTime = -1;
    private long mSplashEndShowTime = -1;
    private int mMessageNoReadCount = -1;
    private int mCallLogNoReadCount = -1;
    private int mForegroundScore = -1;
    private int mForegroundMinScore = -1;
    private long mFilterTime = -1;
    // 3.3 耗电app数量阈值
    private int mAppThreshold = -1;
    // 3.3.1 充电屏保气泡出现次数
    private int mScreenSaveBubbleCount = -1;

    private Executor mSerialExecutor;
    private HashMap<String, Object> mValues;

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

                if (lockName != null && !lockName.isEmpty()) {
                    LeoLog.d("EngineLock", "lockName = [" + lockName + "]");
                    mRecommendList = Arrays.asList(lockName.split(";"));
                }
                if (lockNameNum != null && !lockNameNum.isEmpty()) {
                    LeoLog.d("EngineLock", "lockNameNum = [" + lockNameNum + "]");
                    mRecommendNumList = Arrays.asList(lockNameNum.split(";"));
                }
            }
        });
    }

    public static synchronized AppMasterPreference getInstance(Context context) {
        return mInstance == null ? (mInstance = new AppMasterPreference(context))
                : mInstance;
    }

    public boolean getUseThemeGuide() {
        return LeoSettings.getBoolean(PREF_USE_LOCK_THEME_GUIDE, false);
    }

    public void setUseThemeGuide(boolean flag) {
        commitAsync(PREF_USE_LOCK_THEME_GUIDE, flag);
    }

    public void setLastFilterSelfTime(long time) {
        mFilterTime = time;
        commitAsync(PREF_LAST_FILTER_SELF_TIME, time);
    }

    public long getLastFilterSelfTime() {
        if (mFilterTime < 0) {
            mFilterTime = LeoSettings.getLong(PREF_LAST_FILTER_SELF_TIME, 0);
        }
        return mFilterTime;
    }

    public long getBusinessSuccessStrategy() {
        if (mBusinessSuccessStrategy < 0) {
            mBusinessSuccessStrategy = LeoSettings.getLong(PREF_BUSINESS_SUCCESS_STRATEGY,
                    AppMasterConfig.TIME_12_HOUR);
        }
        return mBusinessSuccessStrategy;
    }

    public long getBusinessFailStrategy() {
        if (mBusinessFailStrategy < 0) {
            mBusinessFailStrategy = LeoSettings.getLong(PREF_BUSINESS_FAIL_STRATEGY,
                    AppMasterConfig.TIME_2_HOUR);
        }
        return mBusinessFailStrategy;
    }

    public long getBusinessCurrentStrategy() {
        if (mCurrentBusinessStrategy < 0) {
            mCurrentBusinessStrategy = LeoSettings.getLong(PREF_CURRENT_BUSINESS_STRATEGY,
                    AppMasterConfig.TIME_2_HOUR);
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
                    AppMasterConfig.TIME_12_HOUR);
        }
        return mThemeSuccessStrategy;
    }

    public long getThemeFailStrategy() {
        if (mThemeFailStrategy < 0) {
            mThemeFailStrategy = LeoSettings.getLong(PREF_THEME_FAIL_STRATEGY,
                    AppMasterConfig.TIME_2_HOUR);
        }
        return mThemeFailStrategy;
    }

    public long getThemeCurrentStrategy() {
        if (mCurrentThemeStrategy < 0) {
            mCurrentThemeStrategy = LeoSettings.getLong(PREF_CURRENT_THEME_STRATEGY,
                    AppMasterConfig.TIME_2_HOUR);
        }
        return mCurrentThemeStrategy;
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

    public boolean getIsNeedCloseBeauty() {
        return LeoSettings.getBoolean(PREF_NEED_CLOSE_BEAUTY, false);
    }

    public void setIsNeedCloseBeauty(boolean flag) {
        commitAsync(PREF_NEED_CLOSE_BEAUTY, flag);
    }

    public boolean getIsDeterminCloseBeautyFirstly() {
        return LeoSettings.getBoolean(PREF_DETERMIN_CLOSE_BEAUTY, false);
    }

    public void setIsDeterminCloseBeautyFirstly(boolean flag) {
        commitAsync(PREF_DETERMIN_CLOSE_BEAUTY, flag);
    }

    public void setHomeFragmentRedTip(boolean flag) {
        commitAsync(PREF_APP_HOME_APP_FRAGMENT_RED_TIP, flag);
    }

    public boolean getHotAppActivityRedTip() {
        return LeoSettings.getBoolean(PREF_APP_HOT_APP_ACTIVITY_RED_TIP, false);
    }

    public void setHotAppActivityRedTip(boolean flag) {
        commitAsync(PREF_APP_HOT_APP_ACTIVITY_RED_TIP, flag);
    }

    public void setLockerScreenThemeGuide(boolean flag) {
        commitAsync(PREF_THEME_LOCK_GUIDE, flag);
    }

    public boolean isFirstUseLockMode() {
        return LeoSettings.getBoolean(PREF_FIRST_USE_LOCK_MODE, true);
    }

    public void setFirstUseLockMode(boolean firstUse) {
        commitAsync(PREF_FIRST_USE_LOCK_MODE, firstUse);
    }

    public void setHomeBusinessTipClick(boolean flag) {
        commitAsync(PREF_HOME_BUSINESS_NEW_TIP_CLICK, flag);
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
        commitAsync(PREF_RECOMMENT_TIP_LIST, combined);
    }

    public void setHaveEverAppLoaded(boolean loaded) {
        commitAsync(PREF_HAVE_EVER_LOAD_APPS, loaded);
    }

    public boolean haveEverAppLoaded() {
        return LeoSettings.getBoolean(PREF_HAVE_EVER_LOAD_APPS, false);
    }

    public String getOnlineThemeSerialNumber() {
        if (mOnlineThemeSerial == null) {
            mOnlineThemeSerial = LeoSettings.getString(PREF_ONLINE_THEME_SERIAL, "");
        }
        return mOnlineThemeSerial;
    }

    public void setOnlineThemeSerialNumber(String serial) {
        mOnlineThemeSerial = serial;
        commitAsync(PREF_ONLINE_THEME_SERIAL, serial);
    }

    public String getLocalThemeSerialNumber() {
        if (mLocalThemeSerial == null) {
            mLocalThemeSerial = LeoSettings.getString(PREF_LOCAL_THEME_SERIAL, "");
        }
        return mLocalThemeSerial;
    }

    public void setLocalThemeSerialNumber(String serial) {
        mLocalThemeSerial = serial;
        commitAsync(PREF_LOCAL_THEME_SERIAL, serial);
    }

    public long getLastCheckThemeTime() {
        if (mLastCheckThemeTime < 0) {
            mLastCheckThemeTime = LeoSettings.getLong(PREF_LAST_CHECK_NEW_THEME, 0);
        }
        return mLastCheckThemeTime;
    }

    public void setLastCheckThemeTime(long lastTime) {
        mLastCheckThemeTime = lastTime;
        commitAsync(PREF_LAST_CHECK_NEW_THEME, lastTime);
    }

    public long getLastSyncBusinessTime() {
        if (mLastSyncBusinessTime < 0) {
            mLastSyncBusinessTime = LeoSettings.getLong(PREF_LAST_SYNC_BUSINESS_TIME, 0);
        }
        return mLastSyncBusinessTime;
    }

    public void setLastSyncBusinessTime(long lastTime) {
        mLastSyncBusinessTime = lastTime;
        commitAsync(PREF_LAST_SYNC_BUSINESS_TIME, lastTime);
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

    public String getOnlineBusinessSerialNumber() {
        if (mOnlineBusinessSerial == null) {
            mOnlineBusinessSerial = LeoSettings.getString(PREF_ONLINE_BUSINESS_SERIAL, "");
        }
        return mOnlineBusinessSerial;
    }

    public void setOnlineBusinessSerialNumber(String serial) {
        mOnlineBusinessSerial = serial;
        commitAsync(PREF_ONLINE_BUSINESS_SERIAL, serial);
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

    public void setHideThemeList(List<String> themeList) {
        mHideThemeList = themeList;
        String combined = "";
        for (String string : mHideThemeList) {
            combined = combined + string + ";";
        }
        commitAsync(PREF_HIDE_THEME_PKGS, combined);
    }

    public List<String> getHideThemeList() {
        return mHideThemeList;
    }

    public void setGoogleTipShowed(boolean show) {
        commitAsync(PREF_GUIDE_TIP_SHOW, show);
    }

    public boolean getGoogleTipShowed() {
        return LeoSettings.getBoolean(PREF_GUIDE_TIP_SHOW, false);
    }

    public void setUnlockCount(long count) {
        mUnlockCount = count;
        commitAsync(PREF_UNLOCK_COUNT, count);
    }

    public long getUnlockCount() {
        if (mUnlockCount < 0) {
            mUnlockCount = LeoSettings.getLong(PREF_UNLOCK_COUNT, 0);
        }
        return mUnlockCount;
    }

    // TODO
    public void setNewUserUnlockCount(long count) {
        mNewUserUnlockCount = count;
        commitAsync(PREF_NEW_USER_UNLOCK_COUNT, count);
    }

    public long getNewUserUnlockCount() {
        if (mNewUserUnlockCount < 0) {
            mNewUserUnlockCount = LeoSettings.getLong(PREF_NEW_USER_UNLOCK_COUNT, 0);
        }
        return mNewUserUnlockCount;
    }

    public float getRecommendLockPercent() {
        return LeoSettings.getFloat(PREF_RECOMMEND_LOCK_PERCENT, 0.0f);
    }

    public void setReminded(boolean reminded) {
        commitAsync(PREF_LOCK_REMIND, reminded);
    }

    public boolean isReminded() {
        return LeoSettings.getBoolean(PREF_LOCK_REMIND, false);
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
        return LeoSettings.getBoolean(PREF_NEW_APP_LOCK_TIP, true);
    }

    public void setHideLine(boolean isHide) {
        mIsHideLine = isHide;
        commitAsync(PREF_HIDE_LOCK_LINE, isHide);
    }

    public boolean getIsHideLine() {
        if (mIsHideLine == null) {
            mIsHideLine = LeoSettings.getBoolean(PREF_HIDE_LOCK_LINE, false);
        }
        return mIsHideLine;
    }

    public boolean getGuidePageFirstUse() {
        return LeoSettings.getBoolean(PREF_FIRST_USE_APP, true);
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

    public void setRelockTimeout(String timeout) {
        try {
            mRelockTimeOut = Integer.parseInt(timeout) * 1000;
        } catch (Exception e) {
            mRelockTimeOut = 0;
        }
        commitAsync(PREF_RELOCK_TIME, timeout + "");
    }

    public String getRelockStringTime() {
        return LeoSettings.getString(PREF_RELOCK_TIME, "0");
    }

    public String getPassword() {
        return mPassword;
    }

    public String getGesture() {
        return mGesture;
    }

    public void savePassword(String password) {
        if (mLockType == LOCK_TYPE_NONE) {
            LockManager.sendFirstUseLockModeToISwipe();
        }
        mPassword = "";
        if (password != null) {
            mPassword = password.trim();
        }
        LeoSettings.setString(PREF_PASSWORD, password);
        LeoSettings.setInteger(PREF_LOCK_TYPE, LOCK_TYPE_PASSWD);

        mLockType = LOCK_TYPE_PASSWD;
    }

    public void saveGesture(String gesture) {
        if (mLockType == LOCK_TYPE_NONE) {
            LockManager.sendFirstUseLockModeToISwipe();
        }
        mGesture = gesture;

        LeoSettings.setString(PREF_GESTURE, gesture);
        LeoSettings.setInteger(PREF_LOCK_TYPE, LOCK_TYPE_GESTURE);

        mLockType = LOCK_TYPE_GESTURE;

    }

    public int getLockType() {
        return mLockType;
    }

    public boolean hasPswdProtect() {
        return !LeoSettings.getString(PREF_PASSWD_QUESTION, "").equals("");
    }

    public String getPpQuestion() {
        return LeoSettings.getString(PREF_PASSWD_QUESTION, "");
    }

    public String getPpAnwser() {
        return LeoSettings.getString(PREF_PASSWD_ANWSER, "");
    }

    public String getPasswdTip() {
        return LeoSettings.getString(PREF_PASSWD_TIP, "");
    }

    public List<String> getRecommendNumList() {
        return mRecommendNumList;
    }

    public List<String> getRecommendList() {
        return mRecommendList;
    }

    private void loadPreferences() {
        mRecommentAppList = Arrays.asList(LeoSettings.getString(
                PREF_RECOMMENT_TIP_LIST, "").split(";"));
        String themeList = LeoSettings.getString(PREF_HIDE_THEME_PKGS, "");
        if (themeList.equals("")) {
            mHideThemeList = new ArrayList<String>(0);
        } else {
            mHideThemeList = Arrays.asList(themeList.split(";"));
        }
        mLockType = LeoSettings.getInteger(PREF_LOCK_TYPE, LOCK_TYPE_NONE);
        if (mLockType == LOCK_TYPE_GESTURE) {
            mGesture = LeoSettings.getString(PREF_GESTURE, null);
        } else if (mLockType == LOCK_TYPE_PASSWD) {
            mPassword = LeoSettings.getString(PREF_PASSWORD, null);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (PREF_PASSWORD.equals(key)) {
            mPassword = LeoSettings.getString(PREF_PASSWORD, "1234");
        } else if (PREF_RELOCK_TIME.equals(key)) {
            // String s = LeoSettings.getString(PREF_RELOCK_TIME, "-1");
            getRelockTimeout();
        }
    }

    public void savePasswdProtect(String qusetion, String answer, String tip) {

        if (qusetion != null)
            qusetion = qusetion.trim();
        if (answer != null)
            answer = answer.trim();
        if (tip != null)
            tip = tip.trim();

        LeoSettings.setBoolean(PREF_HAVE_PSWD_PROTECTED, true);
        LeoSettings.setString(PREF_PASSWD_QUESTION, qusetion);
        LeoSettings.setString(PREF_PASSWD_ANWSER, answer);
        LeoSettings.setString(PREF_PASSWD_TIP, tip);
    }

    public void setAutoLock(boolean value) {
        LeoSettings.setBoolean(PREF_AUTO_LOCK, value);
    }

    public boolean isAutoLock() {
        return LeoSettings.getBoolean(PREF_AUTO_LOCK, true);
    }

    public boolean isLockerClean() {
        return LeoSettings.getBoolean(PREF_SETTING_LOCKER_CLEAN, false);
    }

    public void setMonthGprsAll(long value) {
        mMonthGprsAll = value;
        commitAsync(PREF_APP_MANAGER_FLOW_MONTH_ALL, value);
    }

    public long getMonthGprsAll() {
        if (mMonthGprsAll < 0) {
            mMonthGprsAll = LeoSettings.getLong(PREF_APP_MANAGER_FLOW_MONTH_ALL, 0);
        }
        return mMonthGprsAll;
    }

    public void setItSelfTodayBase(long value) {
        mItSelfTodayBase = value;
        commitAsync(PREF_APP_MANAGER_FLOW_MAKE_ITSELF_TODAY_BASE, value);
    }

    public long getItSelfTodayBase() {
        if (mItSelfTodayBase < 0) {
            mItSelfTodayBase = LeoSettings.getLong(PREF_APP_MANAGER_FLOW_MAKE_ITSELF_TODAY_BASE, 0);
        }
        return mItSelfTodayBase;
    }

    public void setMonthGprsBase(long value) {
        mMonthGprsBase = value;
        commitAsync(PREF_APP_MANAGER_FLOW_MONTH_BASE, value);
    }

    public long getMonthGprsBase() {
        if (mMonthGprsBase < 0) {
            mMonthGprsBase = LeoSettings.getLong(PREF_APP_MANAGER_FLOW_MONTH_BASE, 0);
        }
        return mMonthGprsBase;
    }

    public void setYearAppTraf(int value) {
        mYearAppTraf = value;
        commitAsync(PREF_APP_MANAGER_FLOW_YEAR_TRAF, value);
    }

    public void setMonthAppTraf(int value) {
        mMonthAppTraf = value;
        commitAsync(PREF_APP_MANAGER_FLOW_MONTH_TRAF, value);
    }

    public int getMonthAppTraf() {
        if (mMonthAppTraf < 0) {
            mMonthAppTraf = LeoSettings.getInteger(PREF_APP_MANAGER_FLOW_MONTH_TRAF, 1);
        }
        return mMonthAppTraf;
    }

    public void setGprsSend(long value) {
        mGprsSend = value;
        commitAsync(PREF_APP_MANAGER_FLOW_GPRS_SEND, value);
    }

    public long getGprsSend() {
        if (mGprsSend < 0) {
            mGprsSend = LeoSettings.getLong(PREF_APP_MANAGER_FLOW_GPRS_SEND, 0);
        }
        return mGprsSend;
    }

    public void setGprsRev(long value) {
        mGprsRev = value;
        commitAsync(PREF_APP_MANAGER_FLOW_GPRS_REV, value);
    }

    public long getGprsRev() {
        if (mGprsRev < 0) {
            mGprsRev = LeoSettings.getLong(PREF_APP_MANAGER_FLOW_GPRS_REV, 0);
        }
        return mGprsRev;
    }

    public void setBaseSend(long value) {
        mBaseSend = value;
        commitAsync(PREF_APP_MANAGER_FLOW_BE_SEND, value);
    }

    public long getBaseSend() {
        if (mBaseSend < 0) {
            mBaseSend = LeoSettings.getLong(PREF_APP_MANAGER_FLOW_BE_SEND, 0);
        }
        return mBaseSend;
    }

    public void setBaseRev(long value) {
        mBaseRev = value;
        commitAsync(PREF_APP_MANAGER_FLOW__BE_REV, value);
    }

    public long getBaseRev() {
        if (mBaseRev < 0) {
            mBaseRev = LeoSettings.getLong(PREF_APP_MANAGER_FLOW__BE_REV, 0);
        }
        return mBaseRev;
    }

    public void setRenewDay(int value) {
        mRenewDay = value;
        commitAsync(PREF_APP_MANAGER_FLOW_RENEWDAY, value);
    }

    public int getRenewDay() {
        if (mRenewDay < 0) {
            mRenewDay = LeoSettings.getInteger(PREF_APP_MANAGER_FLOW_RENEWDAY, 1);
        }
        return mRenewDay;
    }

    public void setTotalTraffic(int value) {
        mTotalTraffic = value;
        commitAsync(PREF_APP_MANAGER_FLOW_TOTAL_TRAFFIC, value);
    }

    public int getTotalTraffic() {
        if (mTotalTraffic < 0) {
            mTotalTraffic = LeoSettings.getInteger(PREF_APP_MANAGER_FLOW_TOTAL_TRAFFIC, 0);
        }
        return mTotalTraffic;
    }

    public void setItselfMonthTraffic(long value) {
        //KB
        mItselfMonthTraffic = value;
        commitAsync(PREF_APP_MANAGER_FLOW_MAKE_ITSELF_MONTH_TRAFFIC, value);
    }

    public long getItselfMonthTraffic() {
        if (mItselfMonthTraffic < 0) {
            mItselfMonthTraffic = LeoSettings.getLong(PREF_APP_MANAGER_FLOW_MAKE_ITSELF_MONTH_TRAFFIC, 0);
        }
        return mItselfMonthTraffic;
    }

    // Single App Flow
    public void setAppBaseSend(int uid, long value) {
        commitAsync(uid + "_app_base_send", value);
    }

    public long getAppBaseSend(int uid) {
        return LeoSettings.getLong(uid + "_app_base_send", 0);
    }

    public void setAppBaseRev(int uid, long value) {
        commitAsync(uid + "_app_base_rev", value);
    }

    public long getAppBaseRev(int uid) {
        return LeoSettings.getLong(uid + "_app_base_rev", 0);
    }

    public void setWifiSend(int uid, long value) {
        commitAsync(uid + PREF_APP_MANAGER_FLOW_WIFI_SEND, value);
    }

    public long getWifiSend(int uid) {
        return LeoSettings.getLong(uid + PREF_APP_MANAGER_FLOW_WIFI_SEND, 0);
    }

    public void setWifiRev(int uid, long value) {
        commitAsync(uid + PREF_APP_MANAGER_FLOW_WIFI_REV, value);
    }

    public long getWifiRev(int uid) {
        return LeoSettings.getLong(uid + PREF_APP_MANAGER_FLOW_WIFI_REV, 0);
    }

    public void setFlowSetting(boolean mSwtich) {
        commitAsync(PREF_APP_MANAGER_FLOW_SETTING_SWTICH, mSwtich);
    }

    public boolean getFlowSetting() {
        return LeoSettings.getBoolean(PREF_APP_MANAGER_FLOW_SETTING_SWTICH, false);
    }

    public void setFlowSettingBar(int progress) {
        commitAsync(PREF_APP_MANAGER_FLOW_SETTING_SEEKBAR, progress);
    }

    public int getFlowSettingBar() {
        return LeoSettings.getInteger(PREF_APP_MANAGER_FLOW_SETTING_SEEKBAR, 50);
    }

    public void setAlotNotice(boolean Alot) {
        commitAsync(PREF_APP_MANAGER_FLOW_ALOT_NOTICE, Alot);
    }

    public boolean getAlotNotice() {
        return LeoSettings.getBoolean(PREF_APP_MANAGER_FLOW_ALOT_NOTICE, false);
    }

    public void setFinishNotice(boolean Alot) {
        commitAsync(PREF_APP_MANAGER_FLOW_FINISH_NOTICE, Alot);
    }

    public boolean getFinishNotice() {
        return LeoSettings.getBoolean(PREF_APP_MANAGER_FLOW_FINISH_NOTICE, false);
    }

    public long getFirstTime() {
        return LeoSettings.getLong(PREF_APP_MANAGER_FLOW_BROADCAST_FIRST_IN, 0);
    }

    public void setFirstTime(long time) {
        commitAsync(PREF_APP_MANAGER_FLOW_BROADCAST_FIRST_IN, time);
    }

    public void setLockerClean(boolean lockerClean) {
        commitAsync(PREF_SETTING_LOCKER_CLEAN, lockerClean);
    }

    public void setLastAlarmSetTime(long currentTimeMillis) {
        commitAsync(PREF_LAST_ALARM_SET_TIME, currentTimeMillis);
    }

    public long getInstallTime() {
        return LeoSettings.getLong(PREF_LAST_ALARM_SET_TIME, 0l);
    }

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

    public void setFromOther(boolean flag) {
//        mFromOther = flag;
    }

    public boolean getMessageItemRuning() {

        return LeoSettings.getBoolean(PREF_MESSAGE_ITEM_RUNING, true);
    }

    public String getLastDir() {
        if (mLastDir == null) {
            mLastDir = LeoSettings.getString(PREF_HIDE_VIDEO_LAST_DIR, "");
        }
        return mLastDir;
    }

    public void setLastDir(String path) {
        mLastDir = path;
        commitAsync(PREF_HIDE_VIDEO_LAST_DIR, path);
    }

    public String getSecondDir() {
        if (mSecondDir == null) {
            mSecondDir = LeoSettings.getString(PREF_HIDE_VIDEO_SECOND_DIR, "");
        }
        return mSecondDir;
    }

    public void setSecondDi(String path) {
        mSecondDir = path;
        commitAsync(PREF_HIDE_VIDEO_SECOND_DIR, path);
    }

    public void setAdClickTime(long time) {
        commitAsync(PREF_AD_ICON_CLICK_TIME, time);
    }

    public long getAdClickTime() {
        return LeoSettings.getLong(PREF_AD_ICON_CLICK_TIME, 0);
    }

    public long getAdClickTimeFromHome() {
        return LeoSettings.getLong(PREF_AD_ICON_FROM_HOME, 0);
    }

    public void setAdDeskIcon(boolean value) {
        commitAsync(PREF_AD_ICON_DESK, value);
    }

    public boolean getAdDeskIcon() {
        return LeoSettings.getBoolean(PREF_AD_ICON_DESK, false);
    }

    public void setAdEtClickTime(long time) {
        commitAsync(PREF_AD_ICON_ET_CLICK_TIME, time);
    }

    public long getAdEtClickTime() {
        return LeoSettings.getLong(PREF_AD_ICON_ET_CLICK_TIME, 0);
    }

    public long getAdBannerClickTime() {
        return LeoSettings.getLong(PREF_AD_BANNER_BOTTOM, 0);
    }

    public long getHalfScreenBannerClickTime() {
        return LeoSettings.getLong(PREF_AD_HALF_SCREEN_BANNER, 0);
    }

    public void setJumpIcon(boolean isClick) {
        commitAsync(PREF_AD_ICON_JUMP_CLICKED, isClick);
    }

    public boolean getJumpIcon() {
        return LeoSettings.getBoolean(PREF_AD_ICON_JUMP_CLICKED, false);
    }

    public void setMessageItemRuning(boolean flag) {
        commitAsync(PREF_MESSAGE_ITEM_RUNING, flag);
    }

    public boolean getCallLogItemRuning() {

        return LeoSettings.getBoolean(PREF_CALL_LOG_ITEM_RUNING, true);
    }

    public void setCallLogItemRuning(boolean flag) {
        // 隐私通话记录是否查看详情状态，如果true，则发送通知，如果为false，不用发送通知
        commitAsync(PREF_CALL_LOG_ITEM_RUNING, flag);
    }

    public boolean getTimeLockModeGuideClicked() {
        return LeoSettings.getBoolean(PREF_TIME_LOCK_MODE_GUIDE_USER_CLICKED, false);
    }

    public void setTimeLockModeGuideClicked(boolean flag) {
        commitAsync(PREF_TIME_LOCK_MODE_GUIDE_USER_CLICKED, flag);
    }

    public boolean getLocationLockModeGuideClicked() {
        return LeoSettings.getBoolean(PREF_LOCATION_LOCK_MODE_GUIDE_USER_CLICKED, false);
    }

    public void setLocationLockModeGuideClicked(boolean flag) {
        commitAsync(PREF_LOCATION_LOCK_MODE_GUIDE_USER_CLICKED, flag);
    }

    public void setWeiZhuang(boolean isfirstin) {
        commitAsync(PREF_WEIZHUANG_FIRST_IN, isfirstin);
    }

    public boolean getWeiZhuang() {
        return LeoSettings.getBoolean(PREF_WEIZHUANG_FIRST_IN, true);
    }

    public int getPretendLock() {
        if (mPretendLock < 0) {
            mPretendLock = LeoSettings.getInteger(PREF_CUR_PRETNED_LOCK, 0);
        }
        return mPretendLock;
    }

    public void setPretendLock(int selected) {
        mPretendLock = selected;
        commitAsync(PREF_CUR_PRETNED_LOCK, selected);
    }

    public int getSwitchModeCount() {
        return LeoSettings.getInteger(PREF_SWITCH_MODE_COUNT, 0);
    }

    public void setSwitchModeCount(int count) {
        commitAsync(PREF_SWITCH_MODE_COUNT, count);
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
                    AppMasterConfig.TIME_12_HOUR);
        }
        return mSplashSuccessStrategy;
    }

    public long getSplashFailStrategy() {
        if (mSplashFailStrategy < 0) {
            mSplashFailStrategy = LeoSettings.getLong(PREF_FAIL_SPLASH_STRATEGY,
                    AppMasterConfig.TIME_2_HOUR);
        }

        return mSplashFailStrategy;
    }

    public long getSplashCurrentStrategy() {
        if (mCurrentSplashStrategy < 0) {
            mCurrentSplashStrategy = LeoSettings.getLong(PREF_CURRENT_SPLASH_STRATEGY,
                    AppMasterConfig.TIME_2_HOUR);
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

    public void setMessageNoReadCount(int count) {
        mMessageNoReadCount = count;
        commitAsync(PREF_MESSAGE_NO_READ_COUNT, count);
    }

    public int getMessageNoReadCount() {
        if (mMessageNoReadCount < 0) {
            mMessageNoReadCount = LeoSettings.getInteger(PREF_MESSAGE_NO_READ_COUNT, 0);
        }
        return mMessageNoReadCount;
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

    public void setRemoveUnlockAllShortcutFlag(boolean removed) {
        commitAsync(PREF_REMOVE_UNLOCK_ALL_SHORTCUT_FLAG, removed);
    }

    public boolean getRemoveUnlockAllShortcutFlag() {
        return LeoSettings.getBoolean(PREF_REMOVE_UNLOCK_ALL_SHORTCUT_FLAG, false);
    }

    public void setSaveSplashIsMemeryEnough(int flag) {
        commitAsync(PREF_SAVE_SPLASH_MEMERY_NO_ENOUGH, flag);
    }

    public int getSaveSplashIsMemeryEnough() {
        return LeoSettings.getInteger(PREF_SAVE_SPLASH_MEMERY_NO_ENOUGH, -1);
    }

    public void setCallLogNoReadCount(int count) {
        mCallLogNoReadCount = count;
        commitAsync(PREF_CALL_LOG_NO_READ_COUNT, count);
    }

    public int getCallLogNoReadCount() {
        if (mCallLogNoReadCount < 0) {
            mCallLogNoReadCount = LeoSettings.getInteger(PREF_CALL_LOG_NO_READ_COUNT, 0);
        }
        return mCallLogNoReadCount;
    }

    public void setTimeLockModeSetOver(boolean setted) {
        commitAsync(PREF_TIME_LOCK_MODE_SET_OVER, setted);
    }

    public boolean getTimeLockModeSetOVer() {
        return LeoSettings.getBoolean(PREF_TIME_LOCK_MODE_SET_OVER, false);
    }

    public void setLocationLockModeSetOver(boolean setted) {
        commitAsync(PREF_LOCATION_LOCK_MODE_SET_OVER, setted);
    }

    public boolean getLocationLockModeSetOVer() {
        return LeoSettings.getBoolean(PREF_LOCATION_LOCK_MODE_SET_OVER, false);
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

    public boolean getSwitchOpenNoReadMessageTip() {
        return LeoSettings.getBoolean(PREF_SWITCH_OPEN_NO_READ_MESSAGE_TIP, true);
    }

    public boolean getSwitchOpenRecentlyContact() {
        return LeoSettings.getBoolean(PREF_SWITCH_OPEN_RECENTLY_CONTACT, true);
    }

    public boolean getSwitchOpenPrivacyContactMessageTip() {
        return LeoSettings.getBoolean(PREF_SWITCH_OPEN_PRIVACY_CONTACT_MESSAGE_TIP, false);
    }

    public boolean getFristSlidingTip() {
        return LeoSettings.getBoolean(PREF_QUICK_FIRST_SLIDING_TIP, false);
    }

    public void setIsUpdateQuickGestureUser(boolean flag) {
        commitAsync(PREF_UPDATE_QUICK_GESTURE_USER,
                flag);
    }

    public void setLastBusinessRedTipShow(boolean b) {
        commitAsync(PREF_LAST_BUSINESS_RED_TIP, b);
    }

    public boolean getQuickGestureCallLogTip() {
        return LeoSettings.getBoolean(PREF_QUICK_NO_CALL_LOG_TIP, false);
    }

    public void setQuickGestureCallLogTip(boolean b) {
        commitAsync(PREF_QUICK_NO_CALL_LOG_TIP, b);

    }

    public void setQuickGestureMsmTip(boolean b) {
        commitAsync(PREF_QUICK_NO_MSM_TIP, b);
    }

    public void setEnterHomeTimes(int times) {
        mEnterHomeTimes = times;
        commitAsync(PREF_ENTER_HOME_TIMES, times);
    }

    public int getEnterHomeTimes() {
        if (mEnterHomeTimes < 0) {
            mEnterHomeTimes = LeoSettings.getInteger(PREF_ENTER_HOME_TIMES, 0);
        }
        return mEnterHomeTimes;
    }

    public void setLockScreenMenuClicked(boolean flag) {
        commitAsync(PREF_IF_LOCK_SCREEN_MENU_CLICKED, flag);
    }

    public long getLastBoostTime() {
        return LeoSettings.getLong(PREF_LAST_BOOST_TIMES, 0);
    }

    public void setLastBoostTime(long lastBoostTime) {
        commitAsync(PREF_LAST_BOOST_TIMES, lastBoostTime);
    }

    public void setLastBoostWithADTime(long lastBoostTime) {
        commitAsync(PREF_LAST_BOOST_WITH_AD_TIMES, lastBoostTime);
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

    public void setLastShowNotifyTime(long savetime) {
        commitAsync(PREF_SHOW_NOTIFY_CLEAN_MEMORY, savetime);
    }

    public long getLastShowNotifyTime() {
        return LeoSettings.getLong(PREF_SHOW_NOTIFY_CLEAN_MEMORY, 0);
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

    /* 保存iswip是否有更新 */
    public void setIswipUpdateFlag(int flag) {
        commitAsync(PREF_ISWIP_UPDATE_FALG, flag);
    }

    /* 保存iswip更新提醒频率 */
    public void setIswipUpdateFre(int flag) {
        commitAsync(PREF_ISWIP_UPDATE_TIP_FRE, flag);
    }

    /* 保存iswip更新提醒次数 */
    public void setIswipUpdateNumber(int flag) {
        commitAsync(PREF_ISWIP_UPDATE_TIP_NUMBER, flag);
    }

    /* 保存iswip的GP下载地址 */
    public void setIswipUpdateGpUrl(String url) {
        commitAsync(PREF_ISWIP_UPDATE_GP_URL, url);
    }

    public String getIswipUpdateGpUrl() {
        return LeoSettings.getString(PREF_ISWIP_UPDATE_GP_URL, null);
    }

    /* 保存iswip浏览器下载地址 */
    public void setIswipUpdateBrowserUrl(String url) {
        commitAsync(PREF_ISWIP_UPDATE_BROWSER_URL, url);
    }

    public String getIswipUpdateBrowserUrl() {
        return LeoSettings.getString(PREF_ISWIP_UPDATE_BROWSER_URL, null);
    }

    /* 保存iswip下载方式 */
    public void setIswipUpdateDownType(int flag) {
        commitAsync(PREF_ISWIP_UPDATE_DOWN_TYPE, flag);
    }

    /* 保存iswip更新最后拉取时间 */
    public void setIswipUpateLastLoadingTime(long time) {
        commitAsync(PREF_ISWIP_UPDATE_LOADING_LAST_TIME, time);
    }

    public long getIswipUpateLastLoadingTime() {
        return LeoSettings.getLong(PREF_ISWIP_UPDATE_LOADING_LAST_TIME, -1);
    }

    /* 保存iswip更新拉取数据的策略 */
    public void setIswipUpdateLoadingStrategy(long loadingStrategy) {
        commitAsync(PREF_ISWIP_UPDATE_LOADING_STRATEGT, loadingStrategy);
    }

    public long getIswipUpdateLoadingStrategy() {
        return LeoSettings.getLong(PREF_ISWIP_UPDATE_LOADING_STRATEGT, -1);
    }

    /* 保存iswip更新拉取失败次数 */
    public void setIswipUpdateLoadingNumber(int number) {
        commitAsync(PREF_ISWIP_UPDATE_LOADING_NUMBER, number);
    }

    public int getIswipUpdateLoadingNumber() {
        return LeoSettings.getInteger(PREF_ISWIP_UPDATE_LOADING_NUMBER, -1);
    }

    /* 保存本次拉取失败日期 */
    public void setIswipeLoadFailDate(String date) {
        commitAsync(PREF_ISWIP_LOAD_FAIL_DATE, date);
    }

    public String getIswipeLoadFailDate() {
        return LeoSettings.getString(PREF_ISWIP_LOAD_FAIL_DATE, null);
    }

    /* iswipe更新通知次数 */
    public void setIswipeAlarmNotifiNumber(int number) {
        mISwipeAlarm = number;
        commitAsync(PREF_ISWIPE_ALARM_NORI_NUMBER, number);
    }

    /* 上次iwipe更新提示时间 */
    public void setIswipeUpdateTipTime(long time) {
        mISwipeLoadTime = time;
        commitAsync(PREF_ISWIPE_TIP_LAST_TIME, time);
    }

    // 是否需要更新广告的appwall图标
    public void setIsADAppwallNeedUpdate(boolean flag) {
        commitAsync(PREF_AD_APPWAL_UPDATE, flag);
    }

    public boolean getIsADAppwallNeedUpdate() {
        return LeoSettings.getBoolean(PREF_AD_APPWAL_UPDATE, false);
    }

    public long getADRequestShowTypeLastTime() {
        if (mADLastRequestTime < 0) {
            mADLastRequestTime = LeoSettings.getLong(PREF_AD_REQUEST_SHOWTYPE_LAST_TIME, 0);
        }
        return mADLastRequestTime;

    }

    public void setADRequestShowTypeLastTime(long time) {
        mADLastRequestTime = time;
        commitAsync(PREF_AD_REQUEST_SHOWTYPE_LAST_TIME, time);
    }

    public void setADRequestShowTypeNextTimeSpacing(long time) {
        mADRequestInternal = time;
        commitAsync(PREF_AD_REQUEST_SHOWTYPE_NEXT_TIME_SPACING, time);
    }

    // 当天请求广告展示类型的失败次数
    public int getADRequestShowtypeFailTimesCurrentDay() {
        if (mADRequestFailTimes < 0) {
            mADRequestFailTimes = LeoSettings.getInteger(PREF_AD_REQUEST_SHOWTYPE_FAIL_TIMES_CURRENT_DAY, 0);
        }
        return mADRequestFailTimes;

    }

    public void setADRequestShowtypeFailTimesCurrentDay(int times) {
        mADRequestFailTimes = times;
        commitAsync(PREF_AD_REQUEST_SHOWTYPE_FAIL_TIMES_CURRENT_DAY, times);
    }


    // 中国大陆广告主开关
    public void setADMainlandSwticher(int value) {
        mMainlandSwitcher = value;
        commitAsync(PREF_AD_MAINLAND_SWITCHER, value);
    }

    public int getADMainlandSwticher() {
        if (mMainlandSwitcher < 0) {
            mMainlandSwitcher = LeoSettings.getInteger(PREF_AD_MAINLAND_SWITCHER, DEFAULT_SWITCHER_STATE);
        }
        return mMainlandSwitcher;
    }

    // 广告两次拉取的最小时间间隔，单位为分钟
    public void setADFetchInterval(int interval) {
        mADFetchInterval = interval;
        commitAsync(PREF_AD_FETCH_INTERVAL, interval);
    }

    public int getADFetchInterval() {
        if (mADFetchInterval < 0) {
            mADFetchInterval = LeoSettings.getInteger(PREF_AD_FETCH_INTERVAL, DEFAULT_FETCH_INTERAL);
        }
        return mADFetchInterval;
    }


    // 广告展示的形式
    public void setADShowType(int type) {
        mADShowType = type;
        commitAsync(PREF_AD_SHOW_TYPE, type);
    }

    public int getADShowType() {
        if (mADShowType < 0) {
            mADShowType = LeoSettings.getInteger(PREF_AD_SHOW_TYPE,
                    AppMasterConfig.IS_FOR_MAINLAND_CHINA ?
                            ADShowTypeRequestManager.CLOSE_LOCK_AD_SHOW :
                            ADShowTypeRequestManager.DEFAULT_AD_SHOW_TYPE);
        }
        return mADShowType;
    }

    // 锁界面直接展示大图banner的概率 n/10, 0 表示所有都不显示
    public void setLockBannerADShowProbability(int type) {
        mLargeADShowProbability = type;
        commitAsync(PREF_LARGE_AD_SHOW_PROBABILITY, type);
    }

    public int getLockBannerADShowProbability() {
        if (mLargeADShowProbability < 0) {
            mLargeADShowProbability = LeoSettings.getInteger(PREF_LARGE_AD_SHOW_PROBABILITY,
                    AppMasterConfig.IS_FOR_MAINLAND_CHINA ? 0 : 10);
            /* 3.3.1 修改大图默认值为10 */
        }
        return mLargeADShowProbability;
    }

    // UFO动画的展示形式 //暂时没有使用
    public int getThemeChanceAfterUFO() {
        return LeoSettings.getInteger(PREF_THEME_CHANCE_AFTER_UFO, 0);
    }

    //加速后出现广告的开关
    public void setADChanceAfterAccelerating(int flag) {
        commitAsync(PREF_AD_AFTER_ACCELERATING, flag);
    }

    public int getADChanceAfterAccelerating() {
        return LeoSettings.getInteger(PREF_AD_AFTER_ACCELERATING,
                AppMasterConfig.IS_FOR_MAINLAND_CHINA ? 0 : 1);
    }

    //wifi扫描页广告开关
    public void setADWifiScan(int flag) {
        commitAsync(PREF_AD_WIFI_SCAN_RESULT, flag);
    }

    public int getADWifiScan() {
        return LeoSettings.getInteger(PREF_AD_WIFI_SCAN_RESULT,
                AppMasterConfig.IS_FOR_MAINLAND_CHINA ? 0 : 0);
    }


    // 3.2 入侵者防护广告开关
    public void setADIntruder(int flag) {
        commitAsync(PREF_AD_INTRUDER, flag);
    }

    public int getADIntruder() {
        return LeoSettings.getInteger(PREF_AD_INTRUDER,
                AppMasterConfig.IS_FOR_MAINLAND_CHINA ? 0 : 1);
    }

    // 3.2 扫描完成页广告开关
    public void setADAfterScan(int flag) {
        commitAsync(PREF_AD_AFTER_SCAN, flag);
    }

    public int getADAfterScan() {
        return LeoSettings.getInteger(PREF_AD_AFTER_SCAN,
                AppMasterConfig.IS_FOR_MAINLAND_CHINA ? 0 : 0);
    }

    // 3.3 屏保广告开关
    public void setADOnScreenSaver(int flag) {
        commitAsync(PREF_AD_ON_SCREEN_SAVER, flag);
    }

    public int getADOnScreenSaver() {
        return LeoSettings.getInteger(PREF_AD_ON_SCREEN_SAVER,
                AppMasterConfig.IS_FOR_MAINLAND_CHINA ? 0 : 1);
    }

    //隐私防护出现广告的开关
    public void setIsADAfterPrivacyProtectionOpen(int value) {
        commitAsync(PREF_AD_AFTER_PRIVACY_PROTECTION, value);
    }

    public int getIsADAfterPrivacyProtectionOpen() {
        return LeoSettings.getInteger(PREF_AD_AFTER_PRIVACY_PROTECTION,
                AppMasterConfig.IS_FOR_MAINLAND_CHINA ? 0 : 0);
    }

    //主页出现钱钱的开关
    public void setIsADAtAppLockFragmentOpen(int value) {
        commitAsync(PREF_AD_AT_APPLOCK_FRAGMENT, value);
    }

    public int getIsADAtAppLockFragmentOpen() {
        return LeoSettings.getInteger(PREF_AD_AT_APPLOCK_FRAGMENT, 0);
    }

    //是否需要屏蔽应用备份/应用卸载和隐私联系人相关功能
    public boolean getIsNeedCutBackupUninstallAndPrivacyContact() {
        return LeoSettings.getBoolean(PREF_NEED_CUT_BACKUP_UNINSTALL_AND_PRIVACYCONTRACT, false);
    }

    public void setIsNeedCutBackupUninstallAndPrivacyContact(boolean value) {
        commitAsync(PREF_NEED_CUT_BACKUP_UNINSTALL_AND_PRIVACYCONTRACT, value);
    }

    //主题界面出现广告的开关
    public void setIsADAtLockThemeOpen(int value) {
        commitAsync(PREF_AD_AT_THEME, value);
    }

    public int getIsADAtLockThemeOpen() {
        return LeoSettings.getInteger(PREF_AD_AT_THEME, AppMasterConfig.IS_FOR_MAINLAND_CHINA ? 0 : 2);
    }

    //push时是否需要更新礼物盒状态，只在push时有效
    public void setIsGiftBoxNeedUpdate(int value) {
        commitAsync(PREF_GIFTBOX_UPDATE, value);
    }

    /* 解锁成功升级提示标志 */
    /* 解锁成功升级提示开关（1标示开，0标示关） */
    public void setVersionUpdateTipsAfterUnlockOpen(int value) {
        commitAsync(PREF_VERSION_UPDATE_AFTER_UNLOCK, value);
    }

    public boolean getVersionUpdateTipsAfterUnlockOpen() {
        int flag = LeoSettings.getInteger(PREF_VERSION_UPDATE_AFTER_UNLOCK, 0);
        if (flag == OPEN_FLAG) {
            return true;
        }
        return false;
    }

    public void setIsAppStatisticsOpen(int value) {
        commitAsync(PREF_APP_STATISTICS, value);
    }

    public int getIsAppStatisticsOpen() {
        return LeoSettings.getInteger(PREF_APP_STATISTICS, 0);
    }

    public void setIsWifiStatistics(int value) {
        commitAsync(PREF_APP_WIFI_STATISTICS, value);
    }

    public void setIsStatisticsLasttime(int value) {
        commitAsync(PREF_APP_STATISTICS_LASTTIME, value);
    }

    public int getIsStatisticsLasttime() {
        return LeoSettings.getInteger(PREF_APP_STATISTICS_LASTTIME, 0);
    }

    public void setMobvistaClicked() {
        commitAsync(PREF_MOBVISTA_LOADED, true);
    }

    /* 是否为首次生成解锁随机次数 */
    public void setUnlockUpdateFirstRandom(boolean flag) {
        commitAsync(PREF_UNLOCK_UPDATE_FIRST_RANDOM, flag);
    }

    public boolean getUnlockUpdateFirstRandom() {
        return LeoSettings.getBoolean(PREF_UNLOCK_UPDATE_FIRST_RANDOM, true);
    }

    /* 解锁成功升级提示的次数 */
    public void setUnlockUpdateTipCount(int flag) {
        commitAsync(PREF_UNLOCK_UPDATE_TIP_COUNT, flag);
    }

    public int getUnlockUpdateTipCount() {
        return LeoSettings.getInteger(PREF_UNLOCK_UPDATE_TIP_COUNT, 0);
    }

    /* 记录升级提示后本次总共解锁成功次数 */
    public void setRecordUpdateTipUnlockCount(int flag) {
        commitAsync(PREF_UNLOCK_UPDATE_TIP_COUNT_RECORD, flag);
    }

    public int getRecordUpdateTipUnlockCount() {
        return LeoSettings.getInteger(PREF_UNLOCK_UPDATE_TIP_COUNT_RECORD, 0);
    }

    /* 保存本次产生的随机数 */
    public void setUnlockSucessRandom(int flag) {
        commitAsync(PREF_UNLOCK_SUCCESS_TIP_RANDOM, flag);
    }

    public int getUnlockSucessRandom() {
        return LeoSettings.getInteger(PREF_UNLOCK_SUCCESS_TIP_RANDOM, 0);
    }

    /* 存储下升级的当前日期 */
    public void setUpdateTipDate(String date) {
        commitAsync(PREF_UPDATE_TIP_DATE, date);
    }

    public String getUpdateTipDate() {
        return LeoSettings.getString(PREF_UPDATE_TIP_DATE, null);
    }

    /* 保存升级第二天提示一次标志 */
    public void setSecondDayTip(boolean b) {
        commitAsync(PREF_UPDATE_SECOND_TIP_FLAG, b);
    }

    public boolean getSecondDayTip() {
        return LeoSettings.getBoolean(PREF_UPDATE_SECOND_TIP_FLAG, false);
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

    /* 保存不同版本首次使用时的初始解锁次数 */
    public void setFirstUnlockCount(int count) {
        commitAsync(PREF_RECORD_FIRST_UNLOCK_COUNT, count);
    }

    public int getFirstUnlockCount() {
        return LeoSettings.getInteger(PREF_RECORD_FIRST_UNLOCK_COUNT, 0);
    }

    /* 保存第二天首次解锁的初始次数 */
    public void setChanageDateUnlockCount(int count) {
        commitAsync(PREF_RECORD_CHANGE_DATE_UNLOCK_COUNT, count);
    }

    public int getChanageDateUnlockCount() {
        return LeoSettings.getInteger(PREF_RECORD_CHANGE_DATE_UNLOCK_COUNT, -1);
    }

    /* 保存有升级更新时是否已经对升级解锁提示数据初始化 */
    public void setUpdateRecoveryDefaultData(boolean flag) {
        commitAsync(PREF_UPDATE_RECOVERY_DEFAULT_DATA, flag);
    }

    public boolean getUpdateRecoveryDefaultData() {
        return LeoSettings.getBoolean(PREF_UPDATE_RECOVERY_DEFAULT_DATA, false);
    }

    /* 保存是否为强制升级 */
    public void setPGIsForceUpdate(boolean flag) {
        commitAsync(PREF_PG_IS_FORCE_UPDATE, flag);
    }

    public boolean getPGIsForceUpdate() {
        return LeoSettings.getBoolean(PREF_PG_IS_FORCE_UPDATE, false);
    }

    /* 保存第二天产生的随机数 */
    public void setRandomIn30Within(int random) {
        commitAsync(PREF_RANDOM_IN_30_WITHIN, random);
    }

    public int getRandomIn30Within() {
        return LeoSettings.getInteger(PREF_RANDOM_IN_30_WITHIN, -1);
    }


    /* 高级保护打开后在设置列表提示 */
    public void setAdvanceProtectOpenSuccessDialogTip(boolean flag) {
        commitAsync(PREF_ADVANCE_PROTECT_OPEN_SUCCESSDIALOG_TIP, flag);
    }

    public void setIsLockAppWallOpen(int value) {
        commitAsync(PREF_AD_LOCK_WALL, value);
    }

    public int getIsLockAppWallOpen() {
        return LeoSettings.getInteger(PREF_AD_LOCK_WALL, AppMasterConfig.IS_FOR_MAINLAND_CHINA ? 0 : 1);
    }

    public boolean getAdvanceProtectOpenSuccessDialogTip() {
        return LeoSettings.getBoolean(PREF_ADVANCE_PROTECT_OPEN_SUCCESSDIALOG_TIP, true);
    }

    /* 应用锁引导，自启动，后台运行引导 */
    public void setLockAndAutoStartGuide(boolean flag) {
        if (getLockAndAutoStartGuide() != flag) {
            commitAsync(PREF_LOCK_AND_AUTO_START_GUIDE, flag);
        }
    }

    public boolean getLockAndAutoStartGuide() {
        return LeoSettings.getBoolean(PREF_LOCK_AND_AUTO_START_GUIDE, false);
    }

    /* 超人广告Banner点击安装时间 */
    public long getAdSupermanBannerClickTime() {
        return LeoSettings.getLong(PREF_SUPERMAN_AD_BANNER_BOTTOM, 0);
    }

    /* 3.3 耗电app清理阈值 */
    public void setPowerConsumeAppThreshold(int threshold) {
        if (threshold != getPowerConsumeAppThreshold()) {
            commitAsync(PREF_BATTERY_APP_NUM, threshold);
        }
        mAppThreshold = threshold;
    }

    public int getPowerConsumeAppThreshold() {
        if (mAppThreshold < 0) {
            mAppThreshold = LeoSettings.getInteger(PREF_BATTERY_APP_NUM, DEFAULT_APP_NUM_THRESHOLD);
        }
        return mAppThreshold;
    }

    /* 3.3.1 气泡出现次数 */
    public int getScreenSaveBubbleCount() {
        if (mScreenSaveBubbleCount < 0) {
            mScreenSaveBubbleCount = LeoSettings.getInteger(PREF_SCREEN_SAVE_BUBBLE_COUNT, 0);
        }
        return mScreenSaveBubbleCount;
    }

    public void setScreenSaveBubbleCount(int count) {
        mScreenSaveBubbleCount = count;
        commitAsync(PREF_SCREEN_SAVE_BUBBLE_COUNT, count);
    }

    public long getAdSubmarineClickTime() {
        return LeoSettings.getLong(PREF_SUBMARIN_AD_CLICK_TIME, 0);
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

    /**
     * v:3.3.2
     * 锁屏广告大图1的配置
     */
    public void setLockBannerAdConfig(int sdk) {
        commitAsync(AD_IN_LOCK_SCREEN_SDK_SOURCE, sdk);
    }

    /**
     * v:3.3.2
     * 返回锁屏广告大图的配置
     *
     * @return 1外部第三方sdk广告源, 2为max
     */
    public int getLockBannerAdConfig() {
        return LeoSettings.getInteger(AD_IN_LOCK_SCREEN_SDK_SOURCE, AD_SDK_SOURCE_USE_3TH);
    }


    /**
     * v:3.3.2
     * 充电界面广告位的广告源配置
     */
    public void setChargingAdConfig(int sdk) {
        commitAsync(AD_CHARGING_SDK_SOURCE, sdk);
    }

    /**
     * v:3.3.2
     * 返回充电界面广告位的配置
     *
     * @return 0 广告关闭，1外部第三方sdk广告源, 2为max
     */
    public int getChargingAdConfig() {
        return LeoSettings.getInteger(AD_CHARGING_SDK_SOURCE, AD_SDK_SOURCE_USE_3TH);
    }

    /**
     * v:3.3.2
     * 入侵者界面广告位的广告源配置
     *
     * @param sdk
     */
    public void setInvaderAdConfig(int sdk) {
        commitAsync(AD_IN_INVADER_SDK_SOURCE, sdk);
    }

    /**
     * V:3.3.2
     * 返回入侵者界面广告位的广告源配置
     *
     * @return 0 广告关闭，1外部第三方sdk广告源，2为max
     */
    public int getInvaderAdConfig() {
        return LeoSettings.getInteger(AD_IN_INVADER_SDK_SOURCE, AD_SDK_SOURCE_USE_3TH);
    }

    /**
     * v:3.3.2
     * 设置加速广告界面，广告源配置
     *
     * @param sdk
     */
    public void setAccelerationAdConfig(int sdk) {
        commitAsync(AD_IN_ACCELERATION_SDK_SOURCE, sdk);
    }

    /**
     * v:3.3.2
     * 返回加速广告的广告源配置
     *
     * @return 0 广告关闭，1外部第三方sdk广告源，2为max
     */
    public int getAccelerationAdConfig() {
        return LeoSettings.getInteger(AD_IN_ACCELERATION_SDK_SOURCE, AD_SDK_SOURCE_USE_3TH);
    }
}
