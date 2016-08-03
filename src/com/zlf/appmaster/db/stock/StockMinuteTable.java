package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 分时线
 * @author Yushian
 *
 */
public class StockMinuteTable {
	private static final int MAX_SAVE_NUM = 50;

    /**
     * 分时数据表
     */
    public static final String TABLE_STOCK_MINUTE = "stock_minutes";
    public static final String COLNAME_MINUTE = "minutes";		// k线数据
    public static final String COLNAME_ID = "_id";
    public static final String COLNAME_CODE = "code";		// 股票代码
    public static final String COLNAME_TIME = "time";
    public static final String COLNAME_HANDICAP = "handicap";

    //升级 1版本到2
    public static final String UPDATE_1TO2 =
            "alter table " + TABLE_STOCK_MINUTE +
                    " ADD COLUMN " + COLNAME_HANDICAP + " TEXT";

    private static final String CREATE_STOCK_MINUTE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_STOCK_MINUTE
                    + "( "
                    + COLNAME_ID 				+ 	 "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
                    + COLNAME_CODE 				+	 "	TEXT    NOT NULL ,"
                    + COLNAME_MINUTE			+	 "	BLOB,"
                    + COLNAME_TIME				+	 "	INTEGER,"
                    + COLNAME_HANDICAP          +    "  TEXT "
                    + " )";

    public static final String WHERE_ROW = COLNAME_CODE + " = ?";
	public static final String ORDER_BY_TIME = COLNAME_TIME + " asc" ;//按时间升序排列


    StockChartDBHelper m_dbHelper;
	SQLiteDatabase m_db;
	protected String mTableName;
	
	public StockMinuteTable(Context context) {
		m_dbHelper = StockChartDBHelper.getInstance(context);
		mTableName = TABLE_STOCK_MINUTE;

        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();
            m_db.execSQL(CREATE_STOCK_MINUTE_TABLE);
            m_db.close();
        }
	}
	
	public void close(){
//		if(m_db != null)
//			m_db.close();
	}
	
	private boolean isExist(String stockCode){
        m_db = m_dbHelper.getReadableDatabase();
		Cursor c = m_db.query(mTableName, null, WHERE_ROW, new String[] { stockCode }, null, null, null);
		boolean ret = c.moveToFirst();
		c.close();
		
		return ret;
	}
	
	/**
	 * 保存到数据库
	 */
	public void saveMintueData(String stockId, byte[] data, long time) {
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLNAME_MINUTE, data);
            contentValues.put(COLNAME_TIME, time);
            contentValues.put(COLNAME_CODE, stockId);

            if (isExist(stockId)) {    // 更新
                m_db.update(mTableName, contentValues, WHERE_ROW, new String[]{stockId});
            } else {    // 插入
                m_db.insert(mTableName, null, contentValues);

                //是否超过50条，删除时间最久的一条
                //。。。
                Cursor c = m_db.query(mTableName, new String[]{COLNAME_CODE}, WHERE_ROW, new String[]{stockId}, null, null, ORDER_BY_TIME);
                if (c != null) {
                    if (c.getCount() > MAX_SAVE_NUM) {
                        //删除掉第一条
                        m_db.delete(mTableName, COLNAME_CODE, new String[]{c.getString(0)});
                    }
                    c.close();
                }
            }
            m_db.close();
        }
	}
	
	/**
	 * 获取数据库
	 */
	public byte[] getMintueData(String stockId) {
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getReadableDatabase();
            if (isExist(stockId)) {
                Cursor c = m_db.query(mTableName, new String[]{COLNAME_MINUTE}, WHERE_ROW,
                        new String[]{stockId}, null, null, null);
                if (c != null) {
                    if (c.moveToNext()) {
                        byte[] b = c.getBlob(0);
                        c.close();
                        return b;
                    }
                    c.close();
                }
            }
            return null;
        }
	}

    /**
     * 获取盘口数据
     */
    public String getHandicapData(String stockId){
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getReadableDatabase();
            if (isExist(stockId)) {
                Cursor c = m_db.query(mTableName, new String[]{COLNAME_HANDICAP}, WHERE_ROW,
                        new String[]{stockId}, null, null, null);
                if (c != null) {
                    if (c.moveToNext()) {
                        String s = c.getString(0);
                        c.close();
                        return s;
                    }
                    c.close();
                }
            }
            return "";
        }
    }

    /**
     * 保存盘口数据
     */
    public void saveHandicapData(String stockId, String data){
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLNAME_HANDICAP, data);
            contentValues.put(COLNAME_CODE, stockId);

            if (isExist(stockId)) {    // 更新
                m_db.update(mTableName, contentValues, WHERE_ROW, new String[]{stockId});
            } else {    // 插入
                m_db.insert(mTableName, null, contentValues);
            }
            m_db.close();
        }
    }
}
