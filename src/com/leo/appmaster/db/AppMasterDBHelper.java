
package com.leo.appmaster.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.leo.appmaster.AppMasterApplication;

public class AppMasterDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "appmaster.db";
    private static final int DB_VERSION = 1;
    public static final String COLUMN_ID = "_id";
    
    private static AppMasterDBHelper sInstance;
    
    public static synchronized AppMasterDBHelper getInstance() {
        if(sInstance == null) {
            sInstance = new AppMasterDBHelper();
        }
        return sInstance;
    }

    private AppMasterDBHelper() {
        super(AppMasterApplication.getInstance(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS feedback (" +
                "_id INTEGER PRIMARY KEY," +
                "email TEXT," +
                "content TEXT," +
                "category TEXT," +
                "submit_date TEXT" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    }
    
    public  long insert(String table, String nullColumnHack, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        long id = -1;
        db.beginTransaction();
        try {
            id = db.insert(table, nullColumnHack, values);
            db.setTransactionSuccessful();
        } catch(Exception e) {            
        } finally {
            db.endTransaction();
        }
        return id;
    }
    
    public  int delete(String table, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        int result = -1;
        try {
            db.beginTransaction();
            result = db.delete(table, whereClause, whereArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
        
        return result;
    }
    
    public  Cursor query(String table, String[] columns, String selection, String[] 
            selectionArgs, String groupBy, String having, String orderBy) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        return c;
    }


}
