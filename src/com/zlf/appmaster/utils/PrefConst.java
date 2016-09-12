package com.zlf.appmaster.utils;

/**
 * Preference相关key
 * Created by Jasper on 2015/9/30.
 */
public class PrefConst {
    public static final String KEY_ACTIVITY_TS = "activity_ts";
    public static final String KEY_ACTIVITY_TIMES = "activity_times";

    public static final String KEY_HOME_MORE_CONSUMED = "is_home_tab_more_consumed";
    public static final String KEY_IS_NEW_INSTALL = "is_new_install";
    public static final String KEY_NEED_HIDE_BATTERY_FLOW_AND_WIFI = "need_hide_battery_flow_and_wifi";

    //新增图片
    public final static String KEY_NEW_ADD_PIC = "new_add_pic";
    //新增视频
    public final static String KEY_NEW_ADD_VID = "new_add_vid";


    //手机卡imei
    public static final String KEY_SIM_IMEI = "SIM_IMEI";




    // 隐私扫描处理图片、视频处理，小红点消费记录
    public static final String KEY_PIC_COMSUMED = "pic_comsumed";
    public static final String KEY_VID_COMSUMED = "vid_comsumed";

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


  // 3.3.2加入黑名单弹窗分享
    public static final String KEY_ADD_TO_BLACKLIST_SHARE = "blacklist";
    public static final String KEY_ADD_TO_BLACKLIST_SHARE_CONTENT = "blacklist_content";
    public static final String KEY_ADD_TO_BLACKLIST_SHARE_URL = "blacklist_url";
    public static final String KEY_ADD_TO_BLACKLIST_SHARE_DIALOG_CONTENT = "blacklist_dialog_content";

    public static final String KEY_IS_OLD_USER = "is_old_user";
    // 应用内是否显示充电屏保
    public static final String KEY_SHOW_INSIDE_APP = "ss_inside_app";
    // 是否显示屏保广告的忽略按钮
    public static final String KEY_SHOW_IGNORE_COC = "ss_ingore_coc";
    // 再次显示屏保广告的时间间隔
    public static final String KEY_SHOW_IGNORE_COC_TS = "ss_ignore_ts";
    // 显示屏保省电动画的时间间隔
    public static final String KEY_SHOW_BOOST_TS = "ss_boost_ts";
    // 显示屏保省电动画的内存阀值
    public static final String KEY_SHOW_BOOST_MEM = "ss_boost_mem";
    // 上次执行省电屏保动画的时间
    public static final String KEY_LAST_BOOST_TS = "ss_last_boost_ts";

    // 3.3.2 电量系数
    public static final String KEY_BATTERY_REMAINING_COE = "battery_remaning_time_coe";


    public static final String KEY_HOME_MORE_TIP ="home_more_tip";

    public static final String USER_NAME = "user_name"; // 用户昵称  为空证明未登录
    public static final String LAST_LOGIN_TIME = "last_login_time"; // 上次登录时间

    //跳转长江联合
    public static final String KEY_CJLH_GET_TIME = "cjlh_get_time";
    public static final String KEY_CJLH_PCK_NAME = "cjlh_pck_name";
    public static final String KEY_CJLH_DOWNLOAD_URL = "cjlh_download_url";
}
