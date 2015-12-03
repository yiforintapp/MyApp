package com.leo.appmaster.phoneSecurity;

/**
 * Created by runlee on 15-10-14.
 */
public class PhoneSecurityConstants {
    /*国外：Google map url("?mrt=loc&q=纬度,经度")*/
    public static final String GOOGLE_MAP_URI = "http://www.google.com/maps";
    /*国内：Google map url("?mrt=loc&q=纬度,经度")*/
    public static final String GOOGLE_MAP_URI_CN = "http://www.google.cn/maps";
    /*转换为小时*/
    public static final long HOURS_TIME = 60 * 60 * 1000;
    /*转换为天*/
    public static final long DAY_TIME = 24 * 60 * 60 * 1000;
    /*手机防盗返回天数和小时的分隔符“：”*/
    public static final String DAY_SEPARATE_HOUR = ":";
    /*开启手机防盗的分数*/
    public static final int PHONE_SECURITY_SCORE = 6;
    /*返回频繁意思联系人的数量*/
    public static final int FREQUENT_CONTACT_COUNT = 5;
    /*指令短信超时时间:单位毫秒*/
    public static final long MESSAGE_INSTRUCT_TIME_OUT = 2 * 60 * 1000;
    /*手机防盗访问服务器URL*/
    public static final String PHONE_SECUR_URL = "/appmaster/datasecurity/d.html";
    /*使用手机防盗本地的默认人数*/
    public static final int USE_SECUR_NUMBER = 82117;
    /*执行防盗指令时，上报手机数据的ID*/
    public static final String UPLOAD_PHONE_DATA_ID = "lost_upload";
    /*一键防盗指令执行清除数据时延迟执行时间*/
    public static final long ONKEY_FORMATE_EXECU_DELAT_TIME = 3000;
    /*从防盗帮助页面到用户反馈标志*/
    public static final String SECUR_HELP_TO_FEEDBACK = "SECUR_HELP_TO_FEEDBACK";
    /*尝试读取系统短信，判断是否有读取短信权限*/
    public static final String TYY_READ_MSM_COUNT = "2";
    /*中文代号：zh*/
    public static final String ZH = "zh";
    /*延迟移除位置请求监听时间*/
    public static final long DELAY_REMOVE_LOCATION_TIME = 10000;
    /*主页跳转到防盗引导页KEY*/
    public static final String KEY_FORM_HOME_SECUR = "FORM_HOME_SECUR";
    /*位置精度，1米检测*/
    public static final int LOCATION_MIN_DISTANCE = 1;
    /*时间精度,1秒检测*/
    public static final int LOCATION_MIN_TIME = 1000;
    /*添加失败*/
    public static final int ADD_SECUR_NUMBER_FAIL = 0;
    /*输入的为本机号码*/
    public static final int ADD_SECUR_NUMBER_SELT = 1;
    /*添加成功*/
    public static final int ADD_SECUR_NUMBER_SUCESS = 2;
    public static final int MAX_SCORE = 6;

}
