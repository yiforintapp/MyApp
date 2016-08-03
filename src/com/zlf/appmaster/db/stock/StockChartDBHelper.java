package com.zlf.appmaster.db.stock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 股票表格数据库
 * Created by think on 2014/12/3.
 */
public class StockChartDBHelper extends SQLiteOpenHelper {
    public static final String STOCKCHART_DBNAME = "StockChart.db";
    public static final int STOCKCHART_DBVERSION = 2;

    private static StockChartDBHelper mInstance = null;

    public StockChartDBHelper(Context context) {
        super(context, STOCKCHART_DBNAME, null, STOCKCHART_DBVERSION);
    }

    /**
     * 单例化
     * @param context
     * @return
     */
    public static StockChartDBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StockChartDBHelper(context);
        }
        return mInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1){//针对分时表 升级到2版本
            db.execSQL( StockMinuteTable.UPDATE_1TO2 );
        }
    }
}
