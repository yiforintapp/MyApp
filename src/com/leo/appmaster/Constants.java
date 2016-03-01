
package com.leo.appmaster;

import android.net.Uri;
import android.provider.MediaStore.MediaColumns;

public class Constants {

    public static final String DATABASE_NAME = "appmaster.db";
    public static final int DATABASE_VERSION = 2;

    public static final String AUTHORITY = "com.leo.appmaster.provider";
    public static final String ID = "_id";

    public static final String MOBVISTA_APPID = "19242";
    public static final String MOBVISTA_APPKEY = "8c8f18965dfd4377892a458f3b854401";
    public static final String MOBVISTA_UNITID = "25";
    public static final String MOBVISTA_FACEBOOK_ID = "1060111710674878_1060603623959020";

    // mobvista 对应的广告位id
    public static final String UNIT_ID_58 = "58";
    // ids for lock screen - begin
    public static final String UNIT_ID_59 = "59";
    public static final String UNIT_ID_178 = "178";
    public static final String UNIT_ID_179 = "179";
    // ids for lock screen - end
    public static final String UNIT_ID_60 = "60";
    public static final String UNIT_ID_61 = "61";
    public static final String UNIT_ID_62 = "62";
    public static final String UNIT_ID_63 = "63";
    public static final String UNIT_ID_67 = "67";

    // 3.2 intruder
    public static final String UNIT_ID_244 = "244";
    // 3.2 scan result
    public static final String UNIT_ID_243 = "243";
    // 3.3 charging keyguard
    public static final String UNIT_ID_CHARGING = "298";

    public static final String PLACEMENT_ID_58 = "1474823452846141_1475968499398303";
    // placement ids for lock screen - begin
    public static final String PLACEMENT_ID_59 = "1474823452846141_1486339348361218";
    public static final String PLACEMENT_ID_178 = "1474823452846141_1501677476827405";
    public static final String PLACEMENT_ID_179 = "1474823452846141_1501677663494053";
    // placement ids for lock screen - end
    public static final String PLACEMENT_ID_60 = "1474823452846141_1475969139398239";
    public static final String PLACEMENT_ID_61 = "1474823452846141_1475955509399602";
    public static final String PLACEMENT_ID_62 = "1474823452846141_1475966676065152";
    public static final String PLACEMENT_ID_63 = "1474823452846141_1475968946064925";
    public static final String PLACEMENT_ID_67 = "1474823452846141_1475970436064776";
    // 3.2 intruder
    public static final String PLACEMENT_ID_244 = "1474823452846141_1501677286827424";
    // 3.2 scan result
    public static final String PLACEMENT_ID_243 = "1474823452846141_1501676993494120";
    // 3.3 charging keyguard
    public static final String PLACEMENT_ID_CHARGING = "1474823452846141_1530414607287025";

    public static final String INTRUDER_PHOTO_TIMESTAMP_FORMAT = "yyyy/MM/dd-HH:mm:ss";
    public static final long TOTAL_MEMORY_JUDGE_AS_LOW_MEMORY = 512 * 1024;

    public static final String TABLE_DOWNLOAD = "download";
    public static final String TABLE_FEEDBACK = "feedback";
    public static final String TABLE_APPLIST_BUSINESS = "applist_business";
    public static final String TABLE_MONTH_TRAFFIC = "countflow";
    public static final String TABLE_APP_TRAFFIC = "countappflow";
    public static final Uri DOWNLOAD_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_DOWNLOAD);

    public static final Uri FEEDBACK_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_FEEDBACK);

    public static final Uri APPLIST_BUSINESS_URI = Uri.parse("content://"
            + AUTHORITY + "/" + TABLE_APPLIST_BUSINESS);

    public static final Uri MONTH_TRAFFIC_URI = Uri.parse("content://"
            + AUTHORITY + "/" + TABLE_MONTH_TRAFFIC);

    public static final Uri APP_TRAFFIC_URI = Uri.parse("content://"
            + AUTHORITY + "/" + TABLE_APP_TRAFFIC);

    // download table
    public static final String COLUMN_DOWNLOAD_FILE_NAME = "file_name";
    public static final String COLUMN_DOWNLOAD_DESTINATION = "dest";
    public static final String COLUMN_DOWNLOAD_URL = "url";
    public static final String COLUMN_DOWNLOAD_MIME_TYPE = "mime_type";
    public static final String COLUMN_DOWNLOAD_TOTAL_SIZE = "total_size";
    public static final String COLUMN_DOWNLOAD_CURRENT_SIZE = "current_size";
    public static final String COLUMN_DOWNLOAD_STATUS = "status";
    public static final String COLUMN_DOWNLOAD_DATE = "download_date";
    public static final String COLUMN_DOWNLOAD_TITLE = "title";
    public static final String COLUMN_DOWNLOAD_DESCRIPTION = "description";
    public static final String COLUMN_DOWNLOAD_WIFIONLY = "wifionly";

    // download status
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILED = 1;
    public static final int RESULT_CANCELLED = 2;
    public static final int RESULT_FAILED_SDCARD = 3;
    public static final int RESULT_FAILED_NO_NETWORK = 4;
    public static final int RESULT_FAILED_SDCARD_INSUFFICIENT = 5;

    // download parameter
    public static final String PARAMETER_NOTIFY = "notify";

    // download type
    public static final String MIME_TYPE_THEME_ICON = "ICON";
    // public static final String MIME_TYPE_WALLPAPER = "wallpaper";
    public static final String MIME_TYPE_PUSH = "apk";

    // download action
    public static final String ACTION_DOWNLOAD_ADD = "com.leo.appmaster.download.add";
    public static final String ACTION_DOWNLOAD_STOP = "com.leo.appmaster.download.stop";
    public static final String ACTION_DOWNLOAD_PAUSE = "com.leo.appmaster.download.pause";
    public static final String ACTION_DOWNLOAD_START = "com.leo.appmaster.download_start";
    public static final String ACTION_DOWNLOAD_PROGRESS = "com.leo.appmaster.download_progress";
    public static final String ACTION_DOWNLOAD_COMPOLETED = "com.leo.appmaster.download_completed";

    public static final String TYPE = "type";
    public static final String EXTRA_TIME = "extra_time";
    public static final int PROGRESS_INTERVAL = 1000;

    // mime type
    public static final String MIME_TYPE_BUSINESS_APK = "application";

    // download notify
    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_TOTAL = "extra_total";
    public static final String EXTRA_CURRENT = "extra_current";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_PROGRESS = "extra_progress";
    public static final String EXTRA_RESULT = "extra_result";
    public static final String EXTRA_NOTIFY_TYPE = "extra_notify_type";
    public static final String EXTRA_DEST_PATH = "extra_dest_path";
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_MIMETYPE = "extra_mimetype";

    // Message
    public static final int MESSAGE_SHORTCUT_INSTALLED = 100;
    public static final int MESSAGE_SHORTCUT_NOSPACE = 101;
    public static final int MESSAGE_SHORTCUT_UNINSTALLED = 102;
    public static final int MESSAGE_DOWNLOAD_FAILED = 103;

    /**
     * Image Loader
     */
    public static final int MAX_MEMORY_CACHE_SIZE = 10 * (1 << 20);// 5M
    public static final int MAX_DISK_CACHE_SIZE = 100 * (1 << 20);// 10 Mb
    public static final int MAX_THREAD_POOL_SIZE = 3;

    /*
     * Server URL
     */
//    public static final String APP_LOCK_LIST_URL = "/appmaster/applockerrecommend";
    public static final String APP_LOCK_LIST_URL = "/appmaster/applockerrecommend/d.html";

    // public static final String APP_LOCK_LIST_DEBUG2 =
    // "http://192.168.1.142:8080/appmaster/appmaster/applockerrecommend";
    public static final String GP_PACKAGE = "com.android.vending";// GP package
    // name
    public static final int LOCK_TIP_INTERVAL_OF_DATE = 3;
    public static final int LOCK_TIP_INTERVAL_OF_MS = 3 * 24 * 60 * 60 * 1000;
    // public static final int LOCK_TIP_INTERVAL_OF_MS = 1 * 60 * 1000;
    /*
     * AppWall RequestTime
     */
    public static final int REQUEST_TIMEOUT = 5 * 1000;
    public static final int SO_TIMEOUT = 5 * 1000;
    /*
     * LockerTheme
     */
    public static final String ACTION_NEW_THEME = "com.leo.appmaster.newtheme";
    public static final String DEFAULT_THEME = "com.leo.theme.default";// default
    // theme
    /**
     * theme type
     */
    public static final int THEME_TYPE_DEFAULT = 0;
    public static final int THEME_TYPE_LOCAL = 1;
    public static final int THEME_TYPE_ONLINE = 2;

    /**
     * theme tag
     */
    public static final int THEME_TAG_NONE = 0;
    public static final int THEME_TAG_NEW = 1;
    public static final int THEME_TAG_HOT = 2;

    /**
     * online theme url
     */
    public static final String ONLINE_THEME_URL = "/appmaster/themes";
    public static final String CHECK_NEW_THEME = "/appmaster/themesupdatecheck";

    public static final String MSG_CENTER_URL = "/appmaster/activity";

    /**
     * 应用推荐游戏
     */
    public static final String PATH_GAME_DATA = "/appmaster/appwall";

    /**
     * for compat first version theme preview url
     */
    public static final String THEME_MOONNIGHT_URL = "http://files.leostat.com/theme/img/night.jpg";
    public static final String THEME_CHRISTMAS_URL = "http://files.leostat.com/theme/img/christmas.jpg";
    public static final String THEME_FRUIT_URL = "http://files.leostat.com/theme/img/fruit.jpg";
    public static final String THEME_SPATIAL_URL = "http://files.leostat.com/theme/img/spatial.jpg";

    /**
     * compat theme package
     */
    public static final String THEME_PACKAGE_NIGHT = "com.leo.theme.moonnight";
    public static final String THEME_PACKAGE_CHRITMAS = "com.leo.theme.christmas";
    public static final String THEME_PACKAGE_FRUIT = "com.leo.theme.orange";
    public static final String THEME_PACKAGE_SPATIAL = "com.leo.theme.contradict";
    /**
     * HideVideo
     */
    public static final int CANCLE_HIDE_MODE = 0;
    public static final int SELECT_HIDE_MODE = 1;
    public static final String VIDEO_FORMAT = MediaColumns.DATA
            + " LIKE '%.mp4'" + " or " + MediaColumns.DATA + " LIKE '%.avi'"
            + " or " + MediaColumns.DATA + " LIKE '%.mpe'" + " or "
            + MediaColumns.DATA + " LIKE '%.rm'" + " or " + MediaColumns.DATA
            + " LIKE '%.wmv'" + " or " + MediaColumns.DATA + " LIKE '%.vob'"
            + " or " + MediaColumns.DATA + " LIKE '%.mov'" + " or "
            + MediaColumns.DATA + " LIKE '%.mpg'" + " or " + MediaColumns.DATA
            + " LIKE '%.mpeg'" + " or " + MediaColumns.DATA + " LIKE '%.flv'"
            + " or " + MediaColumns.DATA + " LIKE '%.f4v'" + " or "
            + MediaColumns.DATA + " LIKE '%.3gp'" + " or " + MediaColumns.DATA
            + " LIKE '%.asf'" + " or " + MediaColumns.DATA + " LIKE '%.m4v'"
            + " or " + MediaColumns.DATA + " LIKE '%.rmvb'" + " or "
            + MediaColumns.DATA + " LIKE '%.mkv'" + " or " + MediaColumns.DATA
            + " LIKE '%.ts'";
    public static final String VIDEO_PLUS_GP_URL = "https://play.google.com/store/apps/details?id=com.leomaster.videomaster&referrer=utm_source%3Dad_amtuiguang_01market://details?id= com.leomaster.videomaster&referrer=utm_source%3Dad_amtuiguang_01";
    public static final String VIDEO_PLUS_GP = "market://details?id=com.leomaster.videomaster&referrer=utm_source%3Dad_amtuiguang_01";

    public static final String RATING_ADDRESS_MARKET = "market://details?id=com.leo.appmaster&referrer=utm_source=AppMaster";
    public static final String RATING_ADDRESS_BROWSER = "https://play.google.com/store/apps/details?id=com.leo.appmaster&referrer=utm_source=AppMaster";

    // =======================Privacy Contact========================
    /**
     * Privacy Contact
     */
    public static final String TABLE_MESSAGE = "message_leo";
    public static final String TABLE_CONTACT = "contact_leo";
    public static final String TABLE_CALLLOG = "call_log_leo";
    public static final Uri PRIVACY_MESSAGE_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_MESSAGE);
    public static final Uri PRIVACY_CONTACT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_CONTACT);
    public static final Uri PRIVACY_CALL_LOG_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_CALLLOG);
    /**
     * MessageTable
     */
    public static String COLUMN_MESSAGE_ID = "_id";
    public static String COLUMN_MESSAGE_CONTACT_NAME = "contact_name";
    public static String COLUMN_MESSAGE_PHONE_NUMBER = "contact_phone_number";
    public static String COLUMN_MESSAGE_BODY = "message_body";
    public static String COLUMN_MESSAGE_DATE = "message_date";
    public static String COLUMN_MESSAGE_TYPE = "message_type";// 短信类型1是接收到的，2是已发出
    public static String COLUMN_MESSAGE_IS_READ = "message_is_read";
    public static String COLUMN_MESSAGE_PROTCOL = "message_protcol";
    public static String COLUMN_MESSAGE_THREAD_ID = "message_thread_id";
    /**
     * ContactTable
     */
    public static String COLUMN_CONTACT_ID = "_id";
    public static String COLUMN_CONTACT_NAME = "contact_name";
    public static String COLUMN_PHONE_NUMBER = "contact_phone_number";
    public static String COLUMN_PHONE_ANSWER_TYPE = "contact_phone_answer_type";
    public static String COLUMN_ICON = "contact_icon";
    /**
     * CallLogTable
     */
    public static String COLUMN_CALL_LOG_ID = "_id";
    public static String COLUMN_CALL_LOG_CONTACT_NAME = "call_log_contact_name";
    public static String COLUMN_CALL_LOG_PHONE_NUMBER = "call_log_phone_number";
    public static String COLUMN_CALL_LOG_DURATION = "call_log_duration";
    public static String COLUMN_CALL_LOG_DATE = "call_log_date";
    public static String COLUMN_CALL_LOG_TYPE = "call_log_type";
    public static String COLUMN_CALL_LOG_IS_READ = "call_log_is_read";
    /**
     * LockMessageActivity
     */
    public static String LOCK_MESSAGE_THREAD_ID = "message_thread_id";

    // =======================LOCK MODE ==========================
    /**
     * lock mode table
     */
    public static final String TABLE_LOCK_MODE = "lock_mode";
    public static final String TABLE_TIME_LOCK = "time_lock";
    public static final String TABLE_LOCATION_LOCK = "location_lock";
    /**
     * lock mode uri
     */
    public static final Uri LOCK_MODE_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_LOCK_MODE);
    public static final Uri TIME_LOCK_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_TIME_LOCK);
    public static final Uri LOCATION_LOCK_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_LOCATION_LOCK);
    /**
     * lock mode
     */
    public static final String COLUMN_LOCK_MODE_ID = "_id";
    public static final String COLUMN_LOCK_MODE_NAME = "lock_mode_name";
    public static final String COLUMN_LOCKED_LIST = "locked_list";
    public static final String COLUMN_MODE_ICON = "mode_icon";
    public static final String COLUMN_DEFAULT_MODE_FLAG = "unlock_all";
    public static final String COLUMN_CURRENT_USED = "current_used";
    public static final String COLUMN_OPENED = "have_opened";
    /**
     * time lock
     */
    public static final String COLUMN_TIME_LOCK_ID = "_id";
    public static final String COLUMN_TIME_LOCK_NAME = "time_lock_name";
    public static final String COLUMN_LOCK_MODE = "lock_mode";
    public static final String COLUMN_LOCK_TIME = "lock_time";
    public static final String COLUMN_REPREAT_MODE = "repreate_mode";
    public static final String COLUMN_TIME_LOCK_USING = "time_lock_using";
    /**
     * location lock
     */
    public static final String COLUMN_LOCATION_LOCK_ID = "_id";
    public static final String COLUMN_LOCATION_LOCK_NAME = "location_name";
    public static final String COLUMN_WIFF_NAME = "ssid";
    public static final String COLUMN_ENTRANCE_MODE = "entrance_mode";
    public static final String COLUMN_ENTRANCE_MODE_NAME = "entance_mode_name";
    public static final String COLUMN_QUITE_MODE = "quit_mode";
    public static final String COLUMN_QUITE_MODE_NAME = "quit_mode_name";
    public static final String COLUMN_LOCATION_LOCK_USING = "location_lock_using";

    public static final String HOME_TO_APP_WALL_FLAG = "jump_appwall_flag";
    public static final String HOME_TO_APP_WALL_FLAG_VALUE = "from_home_to_appwall";
    public static final String PUSH_TO_APP_WALL_FLAG_VALUE = "push";
    public static boolean business_app_tip = false;

    // ================= File hide table ====================
    public static final String TABLE_IMAGE_HIDE = "hide_image_leo";

    public static final Uri IMAGE_HIDE_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_IMAGE_HIDE);
    // =================Splash url=======================
    public static final String SPLASH_URL = "/appmaster/flushscreen/";
    public static final String SPLASH_PATH = "appmaster/backup/";
    public static final String SPLASH_NAME = "splash_image.9.png";
    public static final String REQUEST_SPLASH_SHOW_ENDDATE = "c";
    public static final String REQUEST_SPLASH_IMAGEURL = "a";
    public static final String REQUEST_SPLASH_SHOW_STARTDATE = "b";
    public static final String SPLASH_FLAG = "splash_flag";
    public static final String SPLASH_REQUEST_FAIL_DATE = "splash_fail_default_date";
    public static final int SPLASH_DELAY_TIME = 2000;
    public static final String REQUEST_SPLASH_DELAY_TIME = "d";
    public static final String REQUEST_SPLASH_SKIP_URL = "e";
    public static final String REQUEST_SPLASH_SKIP_FLAG = "f";
    public static final String SPLASH_SKIP_TO_CLIENT_URL = "g";
    public static final String SPLASH_SKIP_PG_WEBVIEW = "0";
    public static final String SPLASH_SKIP_PG_CLIENT = "1";
    public static final String SPLASH_BUTTON_TEXT = "h";
    // ============== default home mode list =================
    public static String[] sDefaultHomeModeList = new String[]{
            "com.whatsapp",
            "com.facebook.orca",
            "com.android.mms",
            "com.facebook.katana",
            "com.tencent.mm"
    };

    public static final String CP_PACKAGE = "com.leomaster.launcher3";
    public static final String ISWIPE_PACKAGE = "com.leo.iswipe";
    public static final String SEARCH_BOX_PACKAGE = "com.google.android.googlequicksearchbox";
    public static final String GOOGLE_HOME_PACKAGE = "com.google.android.launcher";
    public static final String PKG_BAIDU_SERVICE = "com.baidu.superservice";
    /* Iswipe到GP客户端的链接 */
    public static final String ISWIPE_TO_GP_CLIENT_RUL = "market://details?id=com.leo.iswipe&referrer=utm_source%3Dam_shengji_01;;com.android.vending";
    /* Iswipe到浏览器的链接 */
    public static final String ISWIPE_TO_GP_BROWSER_RUL = "https://play.google.com/store/apps/details?id=com.leo.iswipe";

    public static final String LOCK_LOG = "lock_setting.txt";

    public static final String PKG_WHAT_EVER = "what ever.";
    public static final String PKG_LENOVO_SCREEN = "com.lenovo.coverapp.simpletime2";

    public static final String PKG_FACEBOOK = "com.facebook.katana";
    public static final String PKG_GOOLEPLAY = "com.android.vending";
    public static final String PKG_SETTINGS = "com.android.settings";
    public static final String PKG_GPLUS = "com.google.android.apps.plus";

    public static final String PKG_DIALER = "com.android.dialer";
    public static final String PKG_CONTACTS = "com.android.contacts";


    // 一些重用时间
    public final static long TIME_ONE_DAY = 24 * 60 * 60 * 1000; // 一天

    public static final String PRIVACY_ENTER_SCAN = "enter_scan";
    public static final String PRIVACY_ENTER_SCAN_TYPE = "enter_scan_type";
    public static final String FROM_WHERE = "from";
    public static final String FROM_PUSH = "push";

    /* 数组中为不显示开启高级保护的渠道 */
    public static final String[] FILTER_CHANNEL = {
            "0001a"
    };

    public static final String NEW_APP_NUM = "new_app_num";
//    public static final String IS_CLICK_LOCK_TAB = "is_click_lock_tab";

    /**
     * FaceBook相关
     */
    public static final String FACEBOOK_PKG_NAME = "com.facebook.katana";
    /*pg分享闪屏，二维码图路径*/
    public static final String SPL_SHARE_QR_NAME = "spl_share_qr.png";


    public static final String FACEBOOK_URL = "fb://page/1709302419294051";
    public static final String FACEBOOK_CLASS = "com.facebook.katana.IntentUriHandler";
    public static final String FACEBOOK_PG_URL = "https://www.facebook.com/pages/App-Master/1709302419294051";

    /**
     * 旧Swifty卡片数据请求地址
     */
    public static final String SWIFTY_SECURITY_URL = "/appmaster/privacysuggest";

    public static final String EXTRA_IS_FROM_SCAN = "isFromScan";

    /**
     * Swifty使用浏览器
     */
    public static final String BROWSER_URL_TYPE = "1";
    public static final String GP_URL_TYPE = "2";
    /**
     * 判断手机是否为红米Note2
     */
    public static final String RED_MI_TWO_NAME = "Redmi Note 2";

    public static final String PG_TO_ISWIPE = "pg_to_iswipe";
    public static final String ISWIPE_FIRST_TIP = "iswipe_first_tip";
    public static final String ISWIPE_NO_FIRST_TIP = "iswipe_no_firt_tip";

    /**
     * 用于拼接Gp地址
     */
    public final static String GP_PKG_NAME = "com.android.vending";
    public final static String FIRST_GP_STRING = "market://details?id=";
    public final static String LAST_GP_STRING = "&referrer=utm_source=AppMaster";

    /**
     * 包名
     */
    public final static String WIFIMASTER_PKG_NAME = "com.leo.wifi";
    public final static String PL_PKG_NAME = "com.leo.privacylock";

    public final static String IS_CLICK_SWIFTY = "is_click_swifty";
    public final static String IS_CLICK_WIFIMASTER = "is_click_wifimaster";

    /**
     * 新的卡片数据接口
     */
    public static final String PRIVACY_WIFI_URL = "/appmaster/promotion";

    public static final String PATTERN_DATE = "yyyy/MM/dd HH:mm:ss";

    public static final String PG_DB_PATH = "/data/data/com.leo.appmaster/databases/" + DATABASE_NAME;


    /**
     * LEO产品白名单
     */
    public static final String LEO_FAMILY_PG = "com.leo.appmaster";
    public static final String LEO_FAMILY_PL = "com.leo.privacylock";
    public static final String LEO_FAMILY_SWIFTY = "com.leo.iswipe";
    public static final String LEO_FAMILY_CB = "com.cool.coolbrowser";
    public static final String LEO_FAMILY_WIFI = "com.leo.wifi";
    public static final String LEO_FAMILY_THEMES = "com.leo.theme.";

    public static final String BATTERY_FROM = "from_battery";
    public static final String FROM_BATTERY_PROTECT = "batteryprotect";

    public static final String VIRTUAL_DESKTOP_PIC = "/virtual_desktop_pic"; // 虚化图片名
    public static final String DEFAULT_SHARE_URL = "http://www.leomaster.com.cn/m/privacy/"; // 默认分享链接


    public static final String FROM_CONFIRM_FRAGMENT = "is_from_confirm";


    /***
     * 带有充电屏保的竞品
     */
    public static final String[] SCREEN_SAVE_PKG_NAMES = {
            "com.ijinshan.kbatterydoctor", // 金山电池医生
            "com.cleanmaster.mguard", // CM 国际版
            "com.cleanmaster.mguard_cn", // CM 中国版
            "com.cleanmaster.security", // CM security
            "com.qihoo.security",  // 360
            "com.qihoo.security.lite",  // 360 Lite
    };

    public static final String CHROME_PACKAGE_NAME = "com.android.chrome";

    public static final int TIME_FILTER_TARGET = 5000;

    // deep link相关
    public static final String DP_SCHEMA = "privacyguard";
    public static final String DP_HOST = "com.leo.appmaster";

    //屏保广告点击忽略时间
    public static final String AD_CLICK_IGNORE = "ad_click_ignore";

    //第一次点击按钮关闭屏幕保护
    public static final String CANCEL_BAY_FIRST_TIME = "first_cancel_bay_protect";
}
