package com.zlf.appmaster;

import android.net.Uri;
import android.provider.MediaStore.MediaColumns;

public class Constants {
    public static final int VERSION_CODE_TO_HIDE_BATTERY_FLOW_AND_WIFI = 71;

    // 3.6版本
    public static final int VER_CODE_3_6 = 71;

    public static final String AUTHORITY = "com.leo.appmaster.provider";
    public static final String ID = "_id";



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


    /**
     * Image Loader
     */
    public static final int MAX_MEMORY_CACHE_SIZE = 10 * (1 << 20);// 5M
    public static final int MAX_DISK_CACHE_SIZE = 100 * (1 << 20);// 10 Mb
    public static final int MAX_THREAD_POOL_SIZE = 3;

    /*
     * LockerTheme
     */
    public static final String ACTION_NEW_THEME = "com.leo.appmaster.newtheme";
    public static final String DEFAULT_THEME = "com.leo.theme.default";// default

    /**
     * online theme url
     */
    public static final String ONLINE_THEME_URL = "/appmaster/themes";
    public static final String CHECK_NEW_THEME = "/appmaster/themesupdatecheck";

    public static final String MSG_CENTER_URL = "/appmaster/activity";



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

    public static boolean business_app_tip = false;

    // ================= File hide table ====================
    public static final String TABLE_IMAGE_HIDE = "hide_image_leo";

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
    public static final String ISWIPE_PACKAGE = "com.leo.iswipe";;
    public static final String GOOGLE_HOME_PACKAGE = "com.google.android.launcher";

    public static final String PKG_WHAT_EVER = "what ever.";
    public static final String PKG_LENOVO_SCREEN = "com.lenovo.coverapp.simpletime2";

    public static final String PKG_FACEBOOK = "com.facebook.katana";
    public static final String PKG_GOOLEPLAY = "com.android.vending";

    /**
     * FaceBook相关
     */
    public static final String FACEBOOK_PKG_NAME = "com.facebook.katana";
    /*pg分享闪屏，二维码图路径*/
    public static final String SPL_SHARE_QR_NAME = "spl_share_qr.png";

    /**
     * LEO产品白名单
     */
    public static final String LEO_FAMILY_PG = "com.leo.appmaster";
    public static final String LEO_FAMILY_PL = "com.leo.privacylock";
    public static final String LEO_FAMILY_SWIFTY = "com.leo.iswipe";
    public static final String LEO_FAMILY_CB = "com.cool.coolbrowser";
    public static final String LEO_FAMILY_WIFI = "com.leo.wifi";
    public static final String LEO_FAMILY_THEMES = "com.leo.theme.";


    public static final String CHROME_PACKAGE_NAME = "com.android.chrome";


    // deep link相关
    public static final String DP_SCHEMA = "privacyguard";
    public static final String DP_HOST = "com.leo.appmaster";

    public static final String DP_APP_SCHEMA = "http";
    public static final String DP_APP_HOST = "leomaster.com";


    public static final String CRYPTO_SUFFIX = ".leotmi";
    public static final String CRYPTO_SUFFIX_OLD = ".leotmp";



    public static final String MY_DATA_URL = "http://120.24.14.76:1688/work?proname=";
    public static final String JIN_GUI_INFO_PRONAME = "changjiang";
    public static final String QI_LU_INFO_PRONAME = "gold";
    public static final String WAI_HUI_INFO_PRONAME = "waihui";
    public static final String ZHI_GOLD_INFO_PRONAME = "zhigold";
    public static final String SHANG_HAI_FUTURES_INFO_PRONAME = "shf";

    public static final String LME_INFO_PRONAME = "lme";
    public static final String NYMEX_INFO_PRONAME = "nymex";
    public static final String COMEX_INFO_PRONAME = "comex";

    public static final String JIN_GUI_INFO_MINUTE_PRONAME = "changjiang_detils";
    public static final String QI_LU_INFO_MINUTE_PRONAME = "gold_detils";
    public static final String WAI_HUI_INFO_MINUTE_PRONAME = "waihui_detils";
    public static final String ZHI_GOLD_INFO_MINUTE_PRONAME = "zhigold_detils";
    public static final String SHF_INFO_MINUTE_PRONAME = "shf_detils";
    public static final String LME_INFO_MINUTE_PRONAME = "lme_detils";
    public static final String NYMEX_INFO_MINUTE_PRONAME = "nymex_detils";
    public static final String COMEX_INFO_MINUTE_PRONAME = "comex_detils";

    public static final String JIN_GUI_INFO_KLINE_PRONAME = "changjiang_detils_k";
    public static final String QI_LU_INFO_KLINE_PRONAME = "gold_detils_k";
    public static final String WAI_HUI_INFO_KLINE_PRONAME = "waihui_detils_k";
    public static final String ZHI_GOLD_INFO_KLINE_PRONAME = "zhigold_detils_k";
    public static final String SHF_INFO_KLINE_PRONAME = "shf_detils_k";
    public static final String LME_INFO_KLINE_PRONAME = "lme_detils_k";
    public static final String NYMEX_INFO_KLINE_PRONAME = "nymex_detils_k";
    public static final String COMEX_INFO_KLINE_PRONAME = "comex_detils_k";


    public static final String ZHI_BO_ADDRESS = "http://sp2.18zhibo.com/d9067d28-41f6-4bad-a4ca-c43a88b617b6/stream.m3u8";


    public static final String ADDRESS= "http://120.24.14.76:1688/";
    public static final String APPSERVLET= "appwork?";
    public static final String SEVLET= "work?";
    public static final String DATA= "proname=";


    public static final String LOGIN_ADDRESS= "http://120.24.14.76:1688/work?proname=";
    public static final String LOGIN_TAG= "applogin&";
    public static final String REGISTER_TAG = "register&";
    public static final String RESET_NAME_TAG = "reset_nic&";
    public static final String RESET_TAG = "reset&";
    public static final String KEY_TAG = "zlfmaster";

    public static final String NEWS_TYPE_ANALY = "analy";
    public static final String NEWS_TYPE_INTER = "international";
    public static final String NEWS_TYPE_MARKET = "market";

    public static final String FROM_WHERE = "from_where";
    public static final String CJLH_PACKAGENAME = "com.zscfappview.cjlh";
    public static final String CJLH_DOWNLOAD_URL = "http://res.cjmex.cn:8088/uploads/20151211/CJJYB_Android.apk";

    public static final String FEEDBACK_TYPE = "proname";
    public static final String FEEDBACK_CONTENT = "feedback_content";
    public static final String FEEDBACK_CONTACT = "feedback_contact";

    public static final String GET_CODE_HOST = "http://api.app2e.com/smsBigSend.api.php";
    public static final String GET_CODE_PWD_TAG = "pwd";
    public static final String GET_CODE_USER_TAG = "username";
    public static final String GET_CODE_PHONE_TAG = "p";
    public static final String GET_CODE_MSG_TAG = "msg";
    public static final String GET_CODE_STYLE_TAG = "charSetStr";

    public static final String HOME_PAGE_DATA = "http://120.24.14.76:1688/appwork?proname=mainpage";
    public static final String HOME_PAGE_DATA_BANNER = "toppic";
    public static final String HOME_PAGE_DATA_BANNER_IVURL = "imgurl";
    public static final String HOME_PAGE_DATA_BANNER_ONURL = "openurl";
    public static final String HOME_PAGE_DATA_WINTOP = "wintop";
    public static final String HOME_PAGE_DATA_DAYNEWS = "daynews";
    public static final String HOME_PAGE_DATA_DAYNEWS_ID = "id";
    public static final String HOME_PAGE_DATA_DAYNEWS_TIME = "newstime";
    public static final String HOME_PAGE_DATA_DAYNEWS_TITLE = "title";
    public static final String HOME_PAGE_DATA_DAYNEWS_DESC = "desc";

    public static final String HOME_PAGE_DATA_MAIN = "http://120.24.14.76:1688/appwork?proname=daynews";
    public static final String DAYNEWS_DETAILS_ID = "daynews_id";
    public static final String DAYNEWS_DETAILS_TITLE = "daynews_title";
    public static final String DAYNEWS_DETAILS_TIME = "daynews_time";

    public static final String DEFAULT_WIN_TOP = "ki****aax_32541.55;yi****js_30012.21;ka****bb_27654.25;ac****21_18154.01;mn****mn_11546.65;mn****oi_9654.23;ij****na_4865.88;qq****aa_3332.33;ip****pz_1596.84;wa****mn_985.99;";

    public static final String PUSH_KEY = "push_key";
    public static final String PUSH_KEY_NORMAL = "normal";
    public static final String PUSH_KEY_DAYNEWS = "daynews";
    public static final String PUSH_KEY_HANGQING = "hangqing";
    public static final String PUSH_KEY_DEAL = "deal";
    public static final String PUSH_KEY_CONSULT = "consult";
    public static final String PUSH_KEY_UPDATE = "update";

    public static final String PUSH_TAG = "zlf";
    public static final String DEFAULT_SELECT_STOCK = "CJAG1_CJOIL3";

    public static final String CHAT_DOMAIN = "http://120.24.18.214:8080/";
    public static final String CHAT_SERVLET = "appwork?";
    public static final String CHAT_MARK = "proname=";

    public static final String CLIENT_PHONE = "075536914014";
}
