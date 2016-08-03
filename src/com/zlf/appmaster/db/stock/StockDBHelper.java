package com.zlf.appmaster.db.stock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StockDBHelper extends SQLiteOpenHelper {
	public static final String STOCKINFO_DBNAME = "Stock.db";
	public static final int STOCKINFO_DBVERSION = 4;

    private static StockDBHelper mInstance = null;

	public StockDBHelper(Context context) {
		super(context, STOCKINFO_DBNAME, null, STOCKINFO_DBVERSION);
	}

    /**
     * 单例化
     * @param context
     * @return
     */
    public static StockDBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StockDBHelper(context);
        }
        return mInstance;
    }

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion == 4){
//            db.execSQL("DROP TABLE "+IndustryTable.TABLE_NAME_INDUSTRY_INFO);
//            db.execSQL("DROP TABLE "+ StockTable.TABLE_NAME_STOCKINFO);
//            db.execSQL("DROP TABLE "+StockTradeTable.TABLE_NAME_STOCK_TRADE_INFO);
            QiNiuDBHelper.dropAllTable(db);
        }

        onCreate(db);

	}

}
