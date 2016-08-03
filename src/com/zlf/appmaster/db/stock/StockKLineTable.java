package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * k线数据表
 * @author Yushian
 *
 */
public class StockKLineTable {

//    private static final String TAG = "StockKLineTable";

    /**
     * k线数据表
     */
    public static final String TABLE_STOCK_KLINE = "stock_kline";
    public static final String COLNAME_ID = "_id";
    public static final String COLNAME_CODE = "code";		// 股票代码
    public static final String COLNAME_KLINE = "kline";		// k线数据
    public static final String COLNAME_TIME = "time";

    private static final String CREATE_STOCK_KLINE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_STOCK_KLINE
                    + "("
                    + COLNAME_ID 				+ 	 "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
                    + COLNAME_CODE 				+	 "	TEXT    NOT NULL ,"
                    + COLNAME_KLINE				+	 "	BLOB,"
                    + COLNAME_TIME				+	 "	INTEGER "
                    + ")";


    private static final int MAX_SAVE_NUM = 50;

	public static final String WHERE_ROW = COLNAME_CODE + " = ?";
	public static final String ORDER_BY_TIME = COLNAME_TIME + " asc" ;//按时间升序排列
	
	StockChartDBHelper m_dbHelper;
	SQLiteDatabase m_db;
	protected String mTableName;
	
	public StockKLineTable(Context context) {
		m_dbHelper = StockChartDBHelper.getInstance(context);
		mTableName = TABLE_STOCK_KLINE;

        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();
            m_db.execSQL(CREATE_STOCK_KLINE_TABLE);
            m_db.close();
        }
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
	 * stockId、数据blob
	 */
	public void saveKLineData(String stockId, byte[] data, long time) {
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLNAME_KLINE, data);
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
        //test
//        SQLiteDatabase db = Connector.getDatabase();
//        QLog.i(TAG,"litepal test");
//        KLine kLine = new KLine();
//        kLine.setCode(stockId);
//        kLine.setData(data);
//        kLine.setTime(time);
//        kLine.save();

	}
	
	
	/**
	 * 从数据库中读出来
	 * stockId
	 */
	public byte[] getKLineData(String stockId) {
        //test
//        List<KLine> kLines = DataSupport.where("code == ?", stockId).find(KLine.class);
//        if (kLines != null && kLines.size() > 0){
//            KLine item = kLines.get(0);
//            if (item != null){
//                QLog.i(TAG,"kline:"+item.getCode()+",time:"+item.getTime());
//                if (item.getData() != null){
//                    QLog.i(TAG,"size:"+item.getData().length);
//                }
//            }else {
//                QLog.i(TAG,"can not get kline");
//            }
//        }else {
//            QLog.i(TAG,"can not get kline");
//        }
        synchronized (m_dbHelper) {

            m_db = m_dbHelper.getReadableDatabase();
            if (isExist(stockId)) {
                Cursor c = m_db.query(mTableName, new String[]{COLNAME_KLINE}, WHERE_ROW, new String[]{stockId}, null, null, null);
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
	
}
