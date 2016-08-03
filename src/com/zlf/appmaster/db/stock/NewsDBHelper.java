package com.zlf.appmaster.db.stock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zlf.appmaster.utils.QLog;

/**
 * 新闻数据库
 * Created by Yushian on 2014/12/1.
 */
public class NewsDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "NewsDBHelper";

    public static final String NEWS_DBNAME = "News.db";
    public static final int NEWS_DBVERSION = 4;
    private static NewsDBHelper mInstance;

    public NewsDBHelper(Context context) {
        super(context, NEWS_DBNAME, null, NEWS_DBVERSION);
        QLog.i(TAG,"NewsDBHelper");
    }

    /**
     * 获取单例 防止泄露
     * @param context
     * @return
     */
    public static NewsDBHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new NewsDBHelper(context);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + NewsFlashTable.TABLE_NAME_NEWS_FLASH);
        db.execSQL("DROP TABLE " + NewsRecentTable.TABLE_NAME_NEWS_RECENT);
    }
}
