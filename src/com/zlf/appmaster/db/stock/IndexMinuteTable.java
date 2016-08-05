package com.zlf.appmaster.db.stock;

import android.content.Context;

public class IndexMinuteTable extends StockMinuteTable {

    //指数分时表
    public static final String TABLE_INDEX_MINUTE = "index_minutes";
    private static final String CREATE_INDEX_MINUTE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_INDEX_MINUTE
                    + "("
                    + COLNAME_ID 				+ 	 "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
                    + COLNAME_CODE 				+	 "	TEXT    NOT NULL ,"
                    + COLNAME_MINUTE			+	 "	BLOB,"
                    + COLNAME_TIME				+	 "	INTEGER "
                    + ")";

    public IndexMinuteTable(Context context) {
		super(context);
		mTableName = TABLE_INDEX_MINUTE;

        m_db = m_dbHelper.getWritableDatabase();
        m_db.execSQL(CREATE_INDEX_MINUTE_TABLE);
        m_db.close();
	}

}
