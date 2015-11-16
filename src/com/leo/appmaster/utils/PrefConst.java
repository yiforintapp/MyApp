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
    public static final String KEY_SWITCH_FOR_INTRUDER_PROTECTION = "switch_for_intruder_protection";
    public static final String KEY_TIMES_OF_CATCH_INTRUDER = "times_of_catch_intruder";
    public static final String KEY_FAILURE_TIMES_TO_CATCH = "failure_times_to_catch";
    public static final String KEY_AD_TYPE_IN_INTRUDER_VIEW = "ad_type_in_intruder_view";
    public static final String KEY_IS_DELAY_TO_SHOW_CATCH = "is_delay_to_show_catch";
    public static final String KEY_ORIENTATION_OF_CAMERA_FACING_FRONT = "orientation_of_camera_facing_front";
    /**
     * 手机防盗
     */
    //开启手机防盗功能时的时间
    public static final String KEY_OPEN_PHONE_SECRITY_TIME="OPEN_SECRITY_TIME";
    //手机防盗号码
    public static final String KEY_PHONE_SECURITY_TELPHONE_NUMBER="SECURITY_TELPHONE_NUMBER";
    //手机防盗状态
    public static  final String KEY_PHONE_SECURITY_STATE="PHONE_SECURITY_STATE";
    //手机卡imei
    public static final String KEY_SIM_IMEI="SIM_IMEI";
    //设置使用手机防盗人数
    public static final String KEY_USE_SECUR_NUMBER="USE_SECUR_NUMBER";
    //手机防盗：锁定手机指令使用状态
    public static final String KEY_LOCK_INSTUR_EXECU_STATUE="LOCK_INSTUR_EXECU_STATUE";
    //手机防盗本次执行指令过的短信的id号
    public static final String  KEY_INSTRU_MSM_ID="INSTRU_MSM_ID";

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
}
