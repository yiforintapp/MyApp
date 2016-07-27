package com.zlf.appmaster.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zlf.appmaster.AppMasterApplication;

/**
 * Created by Jasper on 2015/9/11.
 */
public abstract class BaseTable {
    public abstract void createTable(SQLiteDatabase db);
    public abstract void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion);

    public SQLiteOpenHelper getHelper() {
        Context ctx = AppMasterApplication.getInstance();
        return com.zlf.appmaster.db.AppMasterDBHelper.getInstance(ctx);
    }
}
