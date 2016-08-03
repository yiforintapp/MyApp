package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 同步数据表
 * @author Deping Huang
 */
public class SyncVersionTable {
	private final static String TAG = "SyncBasicTable";
	
	public static final String COLNAME_ID = "_id";
	public static final String COLNAME_SYNC_ID = "sync_id";				// 同步ID
	public static final String COLNAME_SYNC_VERSION = "version";				// 本地版本号

	private static final String TABLE_NAME_SYNCBASIC = "sync_basic";
	
	private static final String CREATE_TABLE =
			"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_SYNCBASIC
			+ "(" 
			+ COLNAME_ID 				+ 	 "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
			+ COLNAME_SYNC_ID 			+	 "	INTEGER    NOT NULL ," 
			+ COLNAME_SYNC_VERSION 		+	 "	INTEGER"
			+ ")";
	private static final String WHERE_ROW = COLNAME_SYNC_ID + " = ?";
	
	private Context mContext;
	private SQLiteOpenHelper m_dbHelper;
	private SQLiteDatabase m_db;
	
	public SyncVersionTable(Context context, SQLiteOpenHelper attachDBHelper){
		mContext = context;
		m_dbHelper = attachDBHelper;
        createTable();
	}

    private  void createTable(){
        synchronized (m_dbHelper){
            m_db = m_dbHelper.getWritableDatabase();
            m_db.execSQL(CREATE_TABLE);
            m_db.close();
        }
    }
	
	private boolean isExist(long syncID){
		
		Cursor c = m_db.query(TABLE_NAME_SYNCBASIC, new String[]{COLNAME_SYNC_ID}, WHERE_ROW, new String[]{String.valueOf(syncID)}, null, null, null);
		boolean ret = c.moveToFirst();
		c.close();
		
		return ret;
	}
	
	
	public void close() {
//		if (db != null)
//			db.close();
	}
	
	/**
	 * 获得版本号
	 * @param syncID
	 * @return
	 */
	public  long getVersionByID(long syncID){
        synchronized (m_dbHelper){
            m_db = m_dbHelper.getWritableDatabase();
            long versionCode = 0;	// 默认为0
            Cursor c = m_db.query(TABLE_NAME_SYNCBASIC, new String[]{COLNAME_SYNC_VERSION}, WHERE_ROW, new String[]{String.valueOf(syncID)}, null, null, null);

            if(c.moveToFirst())
                versionCode = c.getLong(0);

            c.close();
            m_db.close();
            return versionCode;
        }
	}
	
	/**
	 * 设置版本号
	 * @param syncID
	 * @param versionCode
	 */
	public synchronized void setVersion(long syncID, long versionCode){
        synchronized (m_dbHelper){
            m_db = m_dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLNAME_SYNC_VERSION, versionCode);
            if(isExist(syncID)){		// update
                m_db.update(TABLE_NAME_SYNCBASIC, contentValues, WHERE_ROW, new String[]{String.valueOf(syncID)});
            }
            else{						// add
                contentValues.put(COLNAME_SYNC_ID, syncID);
                m_db.insert(TABLE_NAME_SYNCBASIC, null, contentValues);
            }
            m_db.close();
        }

	}
	
	
	
}
