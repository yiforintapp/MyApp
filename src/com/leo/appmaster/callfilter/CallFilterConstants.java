package com.leo.appmaster.callfilter;

import android.net.Uri;

import com.leo.appmaster.Constants;

/**
 * 骚扰拦截常量
 * Created by runlee on 15-12-18.
 */
public class CallFilterConstants {

    public final static String SETTING_FILTER_FLAG = "filter_setting";
    public final static String SETTING_NOTI_FLAG = "noti_setting";

    public final static String FROMWHERE = "from_where";
    public final static String FROM_BLACK_LIST = "from_black_list";

    public final static int BLACK_LIST_LOAD_DONE = 101;
    public final static int CALL_FILTER_LIST_LOAD_DONE = 102;
    public final static String ADAPTER_FLAG_BLACK_LIST = "black_list_adapter";
    public final static String ADAPTER_FLAG_CALL_FILTER = "call_filter_adapter";
    /*降序*/
    public final static String DESC = "desc";

    /*黑名单表名*/
    public static final String BLACK_LIST_TAB = "black_list";
    /*拦截分组表*/
    public static final String FILTER_GROUP_TAB = "filter_group";
    /*拦截详细表*/
    public static final String FILTER_DETAIL_TAB = "filter_detail";
    /*陌生人分组表*/
    public static final String STRANGER_GROUP_TAB = "stranger_group";
    /*陌生人详细表*/
    public static final String STRANGER_DETAIL_TAB = "stranger_detail";

    public static final Uri BLACK_LIST_URI = Uri.parse("content://" + Constants.AUTHORITY
            + "/" + BLACK_LIST_TAB);
    public static final Uri FILTER_GROUP_URI = Uri.parse("content://" + Constants.AUTHORITY
            + "/" + FILTER_GROUP_TAB);
    public static final Uri FILTER_DETAIL_URI = Uri.parse("content://" + Constants.AUTHORITY
            + "/" + FILTER_DETAIL_TAB);
    public static final Uri STRANGER_GROUP_URI = Uri.parse("content://" + Constants.AUTHORITY
            + "/" + STRANGER_GROUP_TAB);
    public static final Uri STRANGER_DETAIL_URI = Uri.parse("content://" + Constants.AUTHORITY
            + "/" + STRANGER_DETAIL_TAB);

    /**
     * 创建黑名单表
     */
    /*黑名单表字段*/
    public static final String BLACK_ID = "_id";
    public static final String BLACK_NAME = "name";
    public static final String BLACK_PHONE_NUMBER = "phone_number";
    public static final String BLACK_ICON = "icon";
    public static final String BLACK_NUMBER_AREA = "number_area";
    public static final String BLACK_ADD_NUMBER = "add_number";
    public static final String MARKER_TYPE = "marker_type";
    public static final String MARKER_NUMBER = "marker_number";
    public static final String BLACK_UPLOAD_STATE = "upload_state";
    public static final String BLACK_REMOVE_STATE = "remove_state";
    public static final String BLACK_READ_STATE = "read_state";
    public static final String BLACK_OTHER_FLAG = "other_flag";

    public static final String CREATE_BLACK_LIST_TAB = "CREATE TABLE IF NOT EXISTS "
            + BLACK_LIST_TAB
            + " ( "
            + BLACK_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + BLACK_NAME
            + " TEXT,"
            + BLACK_PHONE_NUMBER
            + " TEXT,"
            + BLACK_ICON
            + " BLOB,"
            + BLACK_NUMBER_AREA
            + " TEXT,"
            + BLACK_ADD_NUMBER
            + " INTEGER,"
            + MARKER_TYPE
            + " TEXT,"
            + MARKER_NUMBER
            + " INTEGER,"
            + BLACK_UPLOAD_STATE
            + " INTEGER,"
            + BLACK_REMOVE_STATE
            + " INTEGER,"
            + BLACK_READ_STATE
            + " INTEGER,"
            + BLACK_OTHER_FLAG
            + " TEXT"
            + ");";

    /**
     * 创建拦截分组表
     */
    /*拦截分组表字段*/
    public static final String FIL_GR_ID = "_id";
    public static final String FIL_GR_NAME = "name";
    public static final String FIL_GR_PH_NUMB = "phone_number";
    public static final String FIL_GR_NUM_AREA = "number_area";
    public static final String FIL_GR_TO_BLACK_ID = "black_id";
    public static final String FIL_NUMBER = "filter_number";
    public static final String FIL_GR_DATE = "fil_gr_date";
    public static final String FIL_CALL_DURATION = "call_duration";
    public static final String FIL_CALL_TYPE = "call_type";
    public static final String FIL_READ_STATE = "read_state";
    public static final String FIL_OTHER_FLAG = "other_flag";
    public static final String FIL_GR_TYPE = "fil_gr_type";

    public static final String CREATE_FILTER_GR_TAB = "CREATE TABLE IF NOT EXISTS "
            + FILTER_GROUP_TAB
            + " ( "
            + FIL_GR_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + FIL_GR_NAME
            + " TEXT,"
            + FIL_GR_PH_NUMB
            + " TEXT,"
            + FIL_GR_NUM_AREA
            + " TEXT,"
            + FIL_GR_TO_BLACK_ID
            + " INTEGER,"
            + FIL_NUMBER
            + " INTEGER,"
            + FIL_GR_DATE
            + " INTEGER,"
            + FIL_CALL_DURATION
            + " INTEGER,"
            + FIL_CALL_TYPE
            + " INTEGER,"
            + FIL_READ_STATE
            + " INTEGER,"
            + FIL_GR_TYPE
            + " INTEGER,"
            + FIL_OTHER_FLAG
            + " TEXT"
            + ");";

    /**
     * 创建拦截详细列表
     */

    /*拦截详细列表字段*/
    public static final String FIL_DET_ID = "_id";
    public static final String FIL_DET_PHONE_NUMBER = "phone_number";
    public static final String FIL_DET_NAME = "name";
    public static final String FIL_DET_NUM_AREA = "number_area";
    public static final String FIL_DET_TO_GR_ID = "fil_gr_id";
    public static final String FIL_DET_DATE = "fil_det_date";
    public static final String FIL_DET_DURATION = "fil_det_duration";
    public static final String FIL_DET_CALL_TYPE = "fil_det_call_type";
    public static final String FIL_DET_READ_STATE = "read_state";
    public static final String FIL_DET_OTHER = "fil_det_other";
    public static final String FIL_DET_TYPE = "fil_det_type";

    public static final String CREATE_FILTER_DET_TAB = "CREATE TABLE IF NOT EXISTS "
            + FILTER_DETAIL_TAB
            + " ( "
            + FIL_DET_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + FIL_DET_NAME
            + " TEXT,"
            + FIL_DET_PHONE_NUMBER
            + " TEXT,"
            + FIL_DET_NUM_AREA
            + " TEXT,"
            + FIL_DET_TO_GR_ID
            + " INTEGER,"
            + FIL_DET_DATE
            + " INTEGER,"
            + FIL_DET_DURATION
            + " INTEGER,"
            + FIL_DET_CALL_TYPE
            + " INTEGER,"
            + FIL_DET_READ_STATE
            + " INTEGER,"
            + FIL_DET_TYPE
            + " INTEGER,"
            + FIL_DET_OTHER
            + " TEXT"
            + ");";

    /**
     * 创建陌生人分组表
     */
     /*陌生人分组表字段*/
    public static final String STR_GR_ID = "_id";
    public static final String STR_GR_PHO_NUM = "phone_number";
    public static final String STR_GR_NUM_AREA = "number_area";
    public static final String STR_GR_CALL_DURATION = "call_duration";
    public static final String STR_GR_CALL_DATE = "call_date";
    public static final String STR_GR_CALL_TYPE = "call_type";
    public static final String STR_GR_NUMBER = "stranger_number";
    public static final String STR_GR_TIP_STATE = "tip_state";
    public static final String STR_GR_REMOVE_STATE ="remove_state";
    public static final String STR_GR_READ_STATE = "read_state";
    public static final String STR_GR_OTHER = "other";

    public static final String CREATE_STRANGER_GROUP_TAB = "CREATE TABLE IF NOT EXISTS "
            + STRANGER_GROUP_TAB
            + " ( "
            + STR_GR_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + STR_GR_NUM_AREA
            + " TEXT,"
            + STR_GR_PHO_NUM
            + " TEXT,"
            + STR_GR_CALL_DATE
            + " INTEGER,"
            + STR_GR_CALL_DURATION
            + " INTEGER,"
            + STR_GR_CALL_TYPE
            + " INTEGER,"
            + STR_GR_NUMBER
            + " INTEGER,"
            + STR_GR_TIP_STATE
            + " INTEGER,"
            + STR_GR_REMOVE_STATE
            + " INTEGER,"
            + STR_GR_READ_STATE
            + " INTEGER,"
            + STR_GR_OTHER
            + " TEXT"
            + ");";
    /**
     * 创建陌生人详情表
     */
     /*陌生人详情表字段*/
    public static final String STR_DET_ID = "_id";
    public static final String STR_DET_PHO_NUM = "phone_number";
    public static final String STR_DET_TO_GR_ID = "str_gr_id";
    public static final String STR_DET_NUM_AREA = "number_area";
    public static final String STR_DET_CALL_DURATION = "call_duration";
    public static final String STR_DET_CALL_DATE = "call_date";
    public static final String STR_DET_CALL_TYPE = "call_type";
    public static final String STR_DET_READ_STATE = "read_state";
    public static final String STR_DET_OHTER = "other";

    public static final String CREATE_STRANGER_DET_TAB = "CREATE TABLE IF NOT EXISTS "
            + STRANGER_DETAIL_TAB
            + " ( "
            + STR_DET_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + STR_DET_NUM_AREA
            + " TEXT,"
            + STR_DET_PHO_NUM
            + " TEXT,"
            + STR_DET_TO_GR_ID
            + " INTEGER,"
            + STR_DET_CALL_DATE
            + " INTEGER,"
            + STR_DET_CALL_DURATION
            + " INTEGER,"
            + STR_DET_CALL_TYPE
            + " INTEGER,"
            + STR_DET_READ_STATE
            + " INTEGER,"
            + STR_DET_OHTER
            + " TEXT"
            + ");";

    /*没有标记类型，只是黑名单*/
    public static final int BLACK_LIST_TYP = 0;
    /*骚扰电话*/
    public static final int FILTER_CALL_TYPE = 1;
    /*广告推销*/
    public static final int AD_SALE_TYPE = 2;
    /*诈骗电话*/
    public static final int CHEAT_NUM_TYPE = 3;

    /*黑名单未上传*/
    public static final int UPLOAD_NO = 0;
    /*黑名单已上传*/
    public static final int UPLOAD = 1;

    /*记录已删除*/
    public static final int REMOVE_NO = 0;
    /*记录未删除*/
    public static final int REMOVE = 1;

    /*记录读*/
    public static final int READ_NO = 0;
    /*记录未读*/
    public static final int READ = 1;


    public final static String ADD_BLACK_LIST_MODEL = "add_black_list_mode";
    /*黑名单，标记弹窗，已提示*/
    public static final int FILTER_TIP = 1;
    /*黑名单，标记弹窗，未提示*/
    public static final int FILTER_TIP_NO = 0;
}
