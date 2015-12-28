package com.leo.appmaster.utils;

/**
 * Preference相关key
 * Created by Jasper on 2015/9/30.
 */
public class PrefConst {
    // 测试相关key
    public static final String KEY_TEST_INT = "TEST_INT";
    public static final String KEY_TEST_DOUBLE = "TEST_DOUBLE";
    public static final String KEY_TEST_STRING = "TEST_STRING";
    public static final String KEY_TEST_FLOAT = "TEST_FLOAT";
    // 急速模式
    public static final String KEY_SPEED_MODE = "SPEED_MODE";

    public final static String KEY_ROOT_CHECK = "root_check";
    //新增图片
    public final static String KEY_NEW_ADD_PIC = "new_add_pic";
    //新增视频
    public final static String KEY_NEW_ADD_VID = "new_add_vid";

    //当前最后一张图片的id
    public final static String KEY_NEW_LAST_ADD_PIC = "new_last_add_pic";
    //当前最后一张视频的id
    public final static String KEY_NEW_LAST_ADD_VID = "new_last_add_vid";

    //入侵者防护相关
    public static final String KEY_HAS_REQUEST_CAMERA = "has_request_camera";
    public static final String KEY_SWITCH_FOR_INTRUDER_PROTECTION = "switch_for_intruder_protection";
    public static final String KEY_TIMES_OF_CATCH_INTRUDER = "times_of_catch_intruder";
    public static final String KEY_FAILURE_TIMES_TO_CATCH = "failure_times_to_catch";
    public static final String KEY_AD_TYPE_IN_INTRUDER_VIEW = "ad_type_in_intruder_view";
    public static final String KEY_IS_DELAY_TO_SHOW_CATCH = "is_delay_to_show_catch";
    public static final String KEY_ORIENTATION_OF_CAMERA_FACING_FRONT = "orientation_of_camera_facing_front";

    public static final String KEY_HOME_NEED_CHANGE_TO_CHRISMAS_THEME = "need_home_change_to_chrismas_theme";
    public static final String KEY_LOCK_NEED_CHANGE_TO_CHRISMAS_THEME = "need_lock_change_to_chrismas_theme";
    /**
     * 手机防盗
     */
    //开启手机防盗功能时的时间
    public static final String KEY_OPEN_PHONE_SECRITY_TIME = "OPEN_SECRITY_TIME";
    //手机防盗号码
    public static final String KEY_PHONE_SECURITY_TELPHONE_NUMBER = "SECURITY_TELPHONE_NUMBER";
    //手机防盗状态
    public static final String KEY_PHONE_SECURITY_STATE = "PHONE_SECURITY_STATE";
    //手机卡imei
    public static final String KEY_SIM_IMEI = "SIM_IMEI";
    //设置使用手机防盗人数
    public static final String KEY_USE_SECUR_NUMBER = "USE_SECUR_NUMBER";
    //手机防盗：锁定手机指令使用状态
    public static final String KEY_LOCK_INSTUR_EXECU_STATUE = "LOCK_INSTUR_EXECU_STATUE";
    //手机防盗本次执行指令过的短信的id号
    public static final String KEY_INSTRU_MSM_ID = "INSTRU_MSM_ID";

    // 隐私等级扫描
    public static final String KEY_SCANNED_APP = "scanned_app";
    public static final String KEY_SCANNED_PIC = "scanned_pic";
    public static final String KEY_SCANNED_VID = "scanned_vid";

    // 隐私状态监控开关
    public static final String KEY_NOTIFY_APP = "notify_app";
    public static final String KEY_NOTIFY_PIC = "notify_pic";
    public static final String KEY_NOTIFY_VID = "notify_vid";
    public static final String KEY_NOTIFY_TIME = "notify_time";
    public static final String KEY_DECREASE_TIME = "decrease_time";

    // 首页更多按钮上拉是否有拉出来过
    public static final String KEY_MORE_PULLED = "more_pulled";

    // 入侵者防护不可用的情况下，分数时间已经增加
    public static final String KEY_INTRUDER_ADDED = "intruder_score_added";

    //swifty卡片数据
    public static final String KEY_SWIFTY_CONTENT = "swifty_content";
    public static final String KEY_SWIFTY_GP_URL = "swifty_gp_url";
    public static final String KEY_SWIFTY_IMG_URL = "swifty_img_url";
    public static final String KEY_SWIFTY_TITLE = "swifty_title";
    public static final String KEY_SWIFTY_TYPE = "swifty_type";
    public static final String KEY_SWIFTY_URL = "swifty_url";

    // 隐私扫描处理图片、视频处理，小红点消费记录
    public static final String KEY_PIC_COMSUMED = "pic_comsumed";
    public static final String KEY_PIC_REDDOT_EXIST = "pic_dot_exist";
    public static final String KEY_VID_COMSUMED = "vid_comsumed";
    public static final String KEY_VID_REDDOT_EXIST = "vid_dot_exist";
    //高级保护开启首页提示
    public static final String KEY_OPEN_ADVA_PROTECT = "OPEN_ADVA_PROTECT";
    //进入应用锁引导提示
    public static final String KEY_IN_LOCK_GUIDE = "IN_LOCK_GUIDE";

    // 加速快捷方式
    public static final String IS_BOOST_CREAT = "is_boost_creat";

    public static final String KEY_HAS_LATEAST = "intruder_has_lateast";
    public static final String KEY_LATEAST_PATH = "intruder_lateast_path";

    //推广卡片8个Key值
    public static final String KEY_PRI_WIFIMASTER = "privacymaster";
    public static final String KEY_PRI_FB = "privacyfb";
    public static final String KEY_PRI_GRADE = "privacygp";
    public static final String KEY_WIFI_WIFIMASTER = "wifimaster";
    public static final String KEY_WIFI_SWIFTY = "wifiswifty";
    public static final String KEY_WIFI_FB = "wififb";
    public static final String KEY_WIFI_GRADE = "wifigp";

    //隐私页Wifimaster数据
    public static final String KEY_PRI_WIFIMASTER_CONTENT = "pri_wifimaster_content";
    public static final String KEY_PRI_WIFIMASTER_GP_URL = "pri_wifimaster_gp_url";
    public static final String KEY_PRI_WIFIMASTER_IMG_URL = "pri_wifimaster_img_url";
    public static final String KEY_PRI_WIFIMASTER_TYPE = "pri_wifimaster_type";
    public static final String KEY_PRI_WIFIMASTER_URL = "pri_wifimaster_url";
    public static final String KEY_PRI_WIFIMASTER_TITLE = "pri_wifimaster_title";

    //隐私页评分数据
    public static final String KEY_PRI_GRADE_CONTENT = "pri_grade_content";
    public static final String KEY_PRI_GRADE_IMG_URL = "pri_grade_img_url";
    public static final String KEY_PRI_GRADE_URL = "pri_grade_url";
    public static final String KEY_PRI_GRADE_TITLE = "pri_grade_title";

    //隐私页FB数据
    public static final String KEY_PRI_FB_CONTENT = "pri_fb_content";
    public static final String KEY_PRI_FB_IMG_URL = "pri_fb_img_url";
    public static final String KEY_PRI_FB_URL = "pri_fb_url";
    public static final String KEY_PRI_FB_TITLE = "pri_fb_title";

    //wifi页Wifimaster数据
    public static final String KEY_WIFI_WIFIMASTER_CONTENT = "wifi_wifimaster_content";
    public static final String KEY_WIFI_WIFIMASTER_GP_URL = "wifi_wifimaster_gp_url";
    public static final String KEY_WIFI_WIFIMASTER_IMG_URL = "wifi_wifimaster_img_url";
    public static final String KEY_WIFI_WIFIMASTER_TYPE = "wifi_wifimaster_type";
    public static final String KEY_WIFI_WIFIMASTER_URL = "wifi_wifimaster_url";
    public static final String KEY_WIFI_WIFIMASTER_TITLE = "wifi_wifimaster_title";

    //wifi页Swifty数据
    public static final String KEY_WIFI_SWIFTY_CONTENT = "wifi_swifty_content";
    public static final String KEY_WIFI_SWIFTY_GP_URL = "wifi_swifty_gp_url";
    public static final String KEY_WIFI_SWIFTY_IMG_URL = "wifi_swifty_img_url";
    public static final String KEY_WIFI_SWIFTY_TYPE = "wifi_swifty_type";
    public static final String KEY_WIFI_SWIFTY_URL = "wifi_swifty_url";
    public static final String KEY_WIFI_SWIFTY_TITLE = "wifi_swifty_title";

    //wifi页评分数据
    public static final String KEY_WIFI_GRADE_CONTENT = "wifi_grade_content";
    public static final String KEY_WIFI_GRADE_IMG_URL = "wifi_grade_img_url";
    public static final String KEY_WIFI_GRADE_URL = "wifi_grade_url";
    public static final String KEY_WIFI_GRADE_TITLE = "wifi_grade_title";


    //wifi页FB数据
    public static final String KEY_WIFI_FB_CONTENT = "wifi_fb_content";
    public static final String KEY_WIFI_FB_IMG_URL = "wifi_fb_img_url";
    public static final String KEY_WIFI_FB_URL = "wifi_fb_url";
    public static final String KEY_WIFI_FB_TITLE = "wifi_fb_title";
    /*图片编辑按钮引导标志*/
    public static final String KEY_PIC_EDIT_GUIDE = "PIC_EDIT_GUIDE";
    /*视频编辑按钮引导标志*/
    public static final String KEY_VIDEO_EDIT_GUIDE = "VIDEO_EDIT_GUIDE";
    /*首页引导*/
    public static final String KEY_HOME_GUIDE = "HOME_GUIDE";
    /*首页三星提示*/
    public static final String KEY_HOME_SAMSUNG_TIP = "HOME_SAMSUNG_TIP";
    /*安装新应用，加锁三星提示*/
    public static final String KEY_LOCK_SAMSUNG_TIP = "LOCK_SAMSUNG_TIP";
    public static final String KEY_APP_COMSUMED = "app_comsumed";
    public static final String KEY_APP_LOCK_HANDLER = "APP_LOCK_HANDLER";
    /*骚扰拦截打开状态*/
    public static final String KEY_FIL_OP_STA = "FIL_OP_STA";
    /**
     * 黑名单提示人数
     */
    public static final String KEY_BLACK_TIP = "black_tip";
    /**
     * 标记提示人数
     */
    public static final String KEY_MARKER_TIP = "marker_tip";
    /**
     * 骚扰拦截用户数
     */
    public static final String KEY_FILTER_USER = "filter_user";
    /**
     * 骚扰拦截显示基数
     */
    public static final String KEY_FILTER_TIP_USER = "filter_tip_user";

    /**
     * 黑名单和标记的显示倍率
     */
    public static final String KEY_BLK_MARK_TIP = "blk_mark_tip";
    /**
     * 后台下发的黑名单下载链接
     */
    public static final String KEY_SER_BLK_PATH = "SER_BLK_PATH";
    /**
     * 陌生人提示倍率参数
     */
    public static final String KEY_STRA_NOTI_PAR = "STRA_NOTI_PAR";
    /**
     * 拦截挂断电话提示倍率参数
     */
    public static final String KEY_FIL_TIME_PAR = "FIL_TIME_PAR";

    /**
     * 拦截通知状态
     */
    public static final String KEY_FIL_NOTI_STATE = "FIL_NOTI_STATE";
}
