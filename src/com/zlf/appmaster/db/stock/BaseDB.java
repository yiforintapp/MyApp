package com.zlf.appmaster.db.stock;

public class BaseDB {
	public static final String STOCKINFO_DBNAME = "StockInfo.db";
	public static final int STOCKINFO_DBVERSION = 1;
	
    public static final String USER_ID = "id";
    public static final String USER_UIN = "uin";
    public static final String USER_NAME = "name";
    public static final String USER_MONEY = "money";
    public static final String USER_SIGN = "sign";//个性签名
    public static final String USER_RESUME = "resume";//个性签名
    public static final String USER_LEVEL = "level";
    public static final String USER_SEX = "sex";
    public static final String USER_HEADURL = "headurl";
    public static final String USER_HEADPIC = "headpic";//blob数据
    
	// How you want the results sorted in the resulting Cursor
	public static final String FRIEND_SORT_ORDER = USER_ID + " DESC";


    /**
     * 同步标记相关
     */
    public static final int SYNC_LOCAL_TYPE_DEFAULT = 0;
    public static final int SYNC_LOCAL_TYPE_ADD = 1;
    public static final int SYNC_LOCAL_TYPE_DELETE = 2;

    public static final int SYNC_FLAG_DEFAULT = 0;
    public static final int SYNC_FLAG_YES = 1;
    public static final int SYNC_FLAG_NO = 2;
}
