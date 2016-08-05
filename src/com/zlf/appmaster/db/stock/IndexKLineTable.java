package com.zlf.appmaster.db.stock;

import android.content.Context;

public class IndexKLineTable extends StockKLineTable {

    //指数k线表
    public static final String TABLE_INDEX_KLINE = "index_kline";
    private static final String CREATE_INDEX_KLINE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_INDEX_KLINE
                    + "("
                    + COLNAME_ID 				+ 	 "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
                    + COLNAME_CODE 				+	 "	TEXT    NOT NULL ,"
                    + COLNAME_KLINE				+	 "	BLOB,"
                    + COLNAME_TIME				+	 "	INTEGER "
                    + ")";

	public IndexKLineTable(Context context) {
		super(context);
		mTableName = TABLE_INDEX_KLINE;

        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();
            m_db.execSQL(CREATE_INDEX_KLINE_TABLE);
            m_db.close();
        }
	}

}
