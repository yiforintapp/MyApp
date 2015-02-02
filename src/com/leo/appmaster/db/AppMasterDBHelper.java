
package com.leo.appmaster.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.leo.appmaster.Constants;

public class AppMasterDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "appmaster.db";
    private static final int DB_VERSION = 4;

    private static final String CREATE_DOWNLOAD_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Constants.TABLE_DOWNLOAD
            + " ( "
            + Constants.ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Constants.COLUMN_DOWNLOAD_FILE_NAME
            + " TEXT,"
            + Constants.COLUMN_DOWNLOAD_DESTINATION
            + " TEXT,"
            + Constants.COLUMN_DOWNLOAD_URL
            + " TEXT,"
            + Constants.COLUMN_DOWNLOAD_MIME_TYPE
            + " TEXT,"
            + Constants.COLUMN_DOWNLOAD_TOTAL_SIZE
            + " INTEGER NOT NULL DEFAULT 0,"
            + Constants.COLUMN_DOWNLOAD_CURRENT_SIZE
            + " INTEGER,"
            + Constants.COLUMN_DOWNLOAD_STATUS
            + " INTEGER,"
            + Constants.COLUMN_DOWNLOAD_DATE
            + " INTEGER,"
            + Constants.COLUMN_DOWNLOAD_TITLE
            + " TEXT, "
            + Constants.COLUMN_DOWNLOAD_DESCRIPTION
            + " TEXT, "
            + Constants.COLUMN_DOWNLOAD_WIFIONLY
            + " INTEGER NOT NULL DEFAULT -1" + ");";

    public AppMasterDBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS feedback ("
                + "_id INTEGER PRIMARY KEY," + "email TEXT," + "content TEXT,"
                + "category TEXT," + "submit_date TEXT" + ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS applist_business ("
                + "_id INTEGER PRIMARY KEY," + "lebal TEXT,"
                + "package_name TEXT," + "icon_url TEXT,"
                + "download_url TEXT," + "icon BLOB," + "container_id INTEGER,"
                + "rating TEXT," + "download_count TEXT," + "desc TEXT,"
                + "gp_priority INTEGER," + "gp_url TEXT," + "app_size INTEGER,"
                + "icon_status INTEGER" + ");");

        db.execSQL(CREATE_DOWNLOAD_TABLE);
        db.execSQL("CREATE TABLE IF NOT EXISTS hide_image_leo ("
                + "_id INTEGER PRIMARY KEY," + "image_dir TEXT," + "image_path TEXT" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS applist_business ("
                    + "_id INTEGER PRIMARY KEY," + "lebal TEXT,"
                    + "package_name TEXT," + "icon_url TEXT,"
                    + "download_url TEXT," + "icon BLOB,"
                    + "container_id INTEGER," + "gp_priority INTEGER,"
                    + "gp_url TEXT," + "app_size INTEGER,"
                    + "icon_status INTEGER" + ");");

            db.execSQL(CREATE_DOWNLOAD_TABLE);
        } else if (newVersion == 3) {
            db.execSQL("DROP TABLE IF EXISTS " + "applist_business");
            db.execSQL("CREATE TABLE IF NOT EXISTS applist_business ("
                    + "_id INTEGER PRIMARY KEY," + "lebal TEXT,"
                    + "package_name TEXT," + "icon_url TEXT,"
                    + "download_url TEXT," + "icon BLOB,"
                    + "container_id INTEGER," + "rating TEXT,"
                    + "download_count TEXT," + "desc TEXT,"
                    + "gp_priority INTEGER," + "gp_url TEXT,"
                    + "app_size INTEGER," + "icon_status INTEGER" + ");");
        } else if (newVersion == 4) {
            db.execSQL("CREATE TABLE IF NOT EXISTS hide_image_leo ("
                    + "_id INTEGER PRIMARY KEY," + "image_dir TEXT," + "image_path TEXT" + ");");
        }
    }

    public long insert(String table, String nullColumnHack, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        long id = -1;
        db.beginTransaction();
        try {
            id = db.insert(table, nullColumnHack, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
        return id;
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
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

    public Cursor query(String table, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having,
            String orderBy) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(table, columns, selection, selectionArgs, groupBy,
                having, orderBy);
        return c;
    }

}
