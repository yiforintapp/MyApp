package com.zlf.appmaster.db.stock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * 同步基础表结构
 * Created by yushian on 15-4-2.
 */
public class SyncBaseTable {

    /**
     * 删除标识位：正常
     */
    public static int DELETE_FLAG_NORMAL = 1;

    /**
     * 删除标识位：已删除
     */
    public static int DELETE_FLAG_DELETE = 2;


    public static final String COLNAME_ID = "_id";
    public static final String COLNAME_DELFLAG = "del_flag";//删除标记 0否 1删除 2正常
    public static final String COLNAME_CREATE_TIME = "create_time";     // 创建时间
    public static final String COLNAME_UPDATE_TIME = "update_time";	    // 修改时间
    public static final String COLNAME_SEQ = "seq";	    // 版本号
    public static final String COLNAME_LOCAL_SYNC_FLAG = "local_sync_flag";      //  是否已同步 0 保留值 1已同步 2 未同步
    public static final String ORDER_BY_DESC_TIME = COLNAME_CREATE_TIME + " DESC";//按时间降序排列
    /**
     * 数据库操作SQL
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + " %s "          //table name
                    + "("
                    + COLNAME_ID +      "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
                    + COLNAME_DELFLAG + "   INT,"
                    + COLNAME_CREATE_TIME + "	INT,"
                    + COLNAME_UPDATE_TIME + "	INT,"
                    + COLNAME_SEQ +         "   INT,"
                    + COLNAME_LOCAL_SYNC_FLAG + "	INT"
                    + " %s "                                // other structure
                    + ")";

    public static final String WHERE_COMMIT_ADD_ROW =
            COLNAME_LOCAL_SYNC_FLAG + " = " + BaseDB.SYNC_FLAG_NO;
    public static final String WHERE_NO_DELETE = COLNAME_DELFLAG + " != " + BaseDB.SYNC_LOCAL_TYPE_DELETE;



    public String getCreateTable(String tableName, String otherStructure){
        return String.format(CREATE_TABLE,tableName,otherStructure);
    }

    SQLiteDatabase db;
    QiNiuDBHelper m_dbHelper;

    public SyncBaseTable(Context context, String tableName, String otherStructure) {

        m_dbHelper = QiNiuDBHelper.getInstance(context);
        createTable(tableName,otherStructure);
    }

    private void createTable(String tableName, String otherStructure){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getWritableDatabase();
            db.execSQL(getCreateTable(tableName, otherStructure));
            db.close();
        }
    }



}
