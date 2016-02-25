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
    
    //提醒添加快捷方式相关
    public static final String KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_PIC = "accumulative_total_enter_hide_pic";
    public static final String KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_VIDEO = "accumulative_total_enter_hide_video";
    public static final String KEY_ACCUMULATIVE_TOTAL_ENTER_CALLFILTER = "accumulative_total_enter_callfilter";
    public static final String KEY_ACCUMULATIVE_TOTAL_ENTER_WIFI_SECURITY = "accumulative_total_enter_wifi_security";
    public static final String KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_PIC = "has_ask_create_shotcut_hide_pic";
    public static final String KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_VID = "has_ask_create_shotcut_hide_vid";
    public static final String KEY_HAS_ASK_CREATE_SHOTCUT_CALLFILTER = "has_ask_create_shotcut_callfilter";
    public static final String KEY_HAS_ASK_CREATE_SHOTCUT_WIFI_SECURITY= "has_ask_create_shotcut_wifi_security";

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
    public static final String KEY_USED_PICSIZE_INDEX = "used_picsize_index";

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
    
    //充电屏保的开关状态
    public static final String KEY_BATTERY_SCREEN_VIEW_STATUS = "battery_screen_view_status";
    //耗电应用通知的开关状态
    public static final String KEY_BATTERY_NOTIFICATION_STATUS = "battery_notification_status";
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

    //  手机防盗分享
    public static final String KEY_PHONE_SHARE = "phonesecurity";
    public static final String KEY_PHONE_SHARE_CONTENT = "phone_security_content";
    public static final String KEY_PHONE_SHARE_URL = "phone_security_url";

    // 入侵者防护分享
    public static final String KEY_INTRUDER_SHARE = "intruder";
    public static final String KEY_INTRUDER_SHARE_CONTENT = "intruder_content";
    public static final String KEY_INTRUDER_SHARE_URL = "intruder_url";

    // 骚扰拦截分享
    public static final String KEY_CALL_FILTER_SHARE = "callfilter";
    public static final String KEY_CALL_FILTER_SHARE_CONTENT = "call_filter_content";
    public static final String KEY_CALL_FILTER_SHARE_URL = "call_filter_url";

    // 入侵者防护抓拍界面swifty
    public static final String KEY_INTRUDER_SWIFTY = "intruderswifty";
    public static final String KEY_INTRUDER_SWIFTY_CONTENT = "intruder_swifty_content";
    public static final String KEY_INTRUDER_SWIFTY_GP_URL = "intruder_swifty_gp_url";
    public static final String KEY_INTRUDER_SWIFTY_IMG_URL = "intruder_swifty_img_url";
    public static final String KEY_INTRUDER_SWIFTY_TYPE = "intruder_swifty_type";
    public static final String KEY_INTRUDER_SWIFTY_URL = "intruder_swifty_url";
    public static final String KEY_INTRUDER_SWIFTY_TITLE = "intruder_swifty_title";

    // 3.3 清理耗电应用界面swifty - 省电加速结果页推广位
    public static final String KEY_CLEAN_SWIFTY = "cleanswifty";
    public static final String KEY_CLEAN_SWIFTY_CONTENT = "clean_swifty_content";
    public static final String KEY_CLEAN_SWIFTY_GP_URL = "clean_swifty_gp_url";
    public static final String KEY_CLEAN_SWIFTY_IMG_URL = "clean_swifty_img_url";
    public static final String KEY_CLEAN_SWIFTY_TYPE = "clean_swifty_type";
    public static final String KEY_CLEAN_SWIFTY_URL = "clean_swifty_url";
    public static final String KEY_CLEAN_SWIFTY_TITLE = "clean_swifty_title";

    // 3.3 充电屏保界面swifty - 充电屏保推广位一
    public static final String KEY_CHARGE_SWIFTY = "chargeswifty";
    public static final String KEY_CHARGE_SWIFTY_CONTENT = "charge_swifty_content";
    public static final String KEY_CHARGE_SWIFTY_GP_URL = "charge_swifty_gp_url";
    public static final String KEY_CHARGE_SWIFTY_IMG_URL = "charge_swifty_img_url";
    public static final String KEY_CHARGE_SWIFTY_TYPE = "charge_swifty_type";
    public static final String KEY_CHARGE_SWIFTY_URL = "charge_swifty_url";
    public static final String KEY_CHARGE_SWIFTY_TITLE = "charge_swifty_title";

    // 3.3 充电屏保界面预留推广位数据 - 充电屏保推广位二
    public static final String KEY_CHARGE_EXTRA = "chargeextra";
    public static final String KEY_CHARGE_EXTRA_CONTENT = "charge_extra_content";
    public static final String KEY_CHARGE_EXTRA_GP_URL = "charge_extra_gp_url";
    public static final String KEY_CHARGE_EXTRA_IMG_URL = "charge_extra_img_url";
    public static final String KEY_CHARGE_EXTRA_TYPE = "charge_extra_type";
    public static final String KEY_CHARGE_EXTRA_URL = "charge_extra_url";
    public static final String KEY_CHARGE_EXTRA_TITLE = "charge_extra_title";

    // 骚扰拦截弹出分享框需要次数
    public static final String KEY_CALL_FILTER_SHARE_TIMES = "call_filter_share_times";

    public static final String VIRTUAL_IMG_HASH_CODE = "virtual_img_hash_code";
    // 是否显示过手机防盗页分享框
    public static final String PHONE_SECURITY_SHOW = "phone_security_show";
    // 是否显示过骚扰拦截页分享框
    public static final String CALL_FILTER_SHOW = "call_filter_show";
    // 当前已经进入骚扰拦截的次数
    public static final String ENTER_CALL_FILTER_TIMES = "enter_call_filter_times";
    //是否上传了该用户的视频信息
    public static final String KEY_REPORT_VIDEO_SIZE = "REPORT_VIDEO_SIZE";

  // 3.3.2加入黑名单弹窗分享
    public static final String KEY_ADD_TO_BLACKLIST_SHARE = "blacklist";
    public static final String KEY_ADD_TO_BLACKLIST_SHARE_CONTENT = "blacklist_content";
    public static final String KEY_ADD_TO_BLACKLIST_SHARE_URL = "blacklist_url";
    public static final String KEY_ADD_TO_BLACKLIST_SHARE_DIALOG_CONTENT = "blacklist_dialog_content";

    public static final String KEY_IS_OLD_USER = "is_old_user";
}
