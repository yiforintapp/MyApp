
package com.leo.appmaster.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.leo.appmaster.applocker.IntruderPhotoInfo;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.imageloader.utils.IoUtils;

/**
 * 入侵者防护所抓拍照片的信息表 Created by Chenfs on 2015/9/29
 */
public class IntruderPhotoTable extends BaseTable {
    private static final byte[] LOCK = new byte[1];

    protected static final String TABLE_NAME = "intruder_photo";
    protected static final String COL_PIC_PATH = "pic_path";
    protected static final String COL_TIME_STAMP = "time_stamp";
    protected static final String COL_FROM_APP_PACKAGENAME = "from_app_packagename";

    @Override
    public void createTable(SQLiteDatabase db) {
//        if (BuildProperties.isZTEAndApiLevel14())
//            return;

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "( _id INTEGER PRIMARY KEY," +
                COL_PIC_PATH + " INTEGER," +
                COL_TIME_STAMP + " TEXT," +
                COL_FROM_APP_PACKAGENAME + " TEXT);");
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (BuildProperties.isZTEAndApiLevel14()) return;

        if (oldVersion <= 7 && newVersion >= 8) {
            // 版本8才加入此表
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    "( _id INTEGER PRIMARY KEY," +
                    COL_PIC_PATH + " INTEGER," +
                    COL_TIME_STAMP + " TEXT," +
                    COL_FROM_APP_PACKAGENAME + " TEXT);");
        }
    }

    // 添加一条记录
    public void insertIntruderPhotoInfo(IntruderPhotoInfo info) {
        if (BuildProperties.isApiLevel14()) {
            synchronized (LOCK) {
                if (info == null)
                    return;
                SQLiteDatabase db = getHelper().getWritableDatabase();
                if (db == null)
                    return;

                ContentValues values = new ContentValues();
                values.put(COL_FROM_APP_PACKAGENAME, info.getFromAppPackage());
                values.put(COL_PIC_PATH, info.getFilePath());
                values.put(COL_TIME_STAMP, info.getTimeStamp());
                try {
                    db.insert(TABLE_NAME, null, values);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (info == null)
                return;
            SQLiteDatabase db = getHelper().getWritableDatabase();
            if (db == null)
                return;

            ContentValues values = new ContentValues();
            values.put(COL_FROM_APP_PACKAGENAME, info.getFromAppPackage());
            values.put(COL_PIC_PATH, info.getFilePath());
            values.put(COL_TIME_STAMP, info.getTimeStamp());
            try {
                db.insert(TABLE_NAME, null, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //更新一条记录
    public void updateIntruderPhotoInfo(IntruderPhotoInfo info) {
        if (info == null) return;
        if (BuildProperties.isApiLevel14()) {
            synchronized (LOCK) {
                SQLiteDatabase db = getHelper().getWritableDatabase();
                if (db == null)
                    return;

                ContentValues values = new ContentValues();
                values.put(COL_FROM_APP_PACKAGENAME, info.getFromAppPackage());
                values.put(COL_PIC_PATH, info.getFilePath());
                values.put(COL_TIME_STAMP, info.getTimeStamp());
                try {
                    db.update(TABLE_NAME, values, COL_FROM_APP_PACKAGENAME + "= ?", new String[]{info.getFilePath()});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            SQLiteDatabase db = getHelper().getWritableDatabase();
            if (db == null)
                return;

            ContentValues values = new ContentValues();
            values.put(COL_FROM_APP_PACKAGENAME, info.getFromAppPackage());
            values.put(COL_PIC_PATH, info.getFilePath());
            values.put(COL_TIME_STAMP, info.getTimeStamp());
            try {
                db.update(TABLE_NAME, values, COL_FROM_APP_PACKAGENAME + "= ?", new String[]{info.getFilePath()});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 删除相同filePath的数据
    public void deleteIntruderPhotoInfo(String filePath) {
        if (BuildProperties.isApiLevel14()) {
            synchronized (LOCK) {
                try {
                    SQLiteDatabase db = getHelper().getWritableDatabase();
                    db.delete(TABLE_NAME, COL_PIC_PATH + " = ?", new String[]{
                            filePath
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                SQLiteDatabase db = getHelper().getWritableDatabase();
                db.delete(TABLE_NAME, COL_PIC_PATH + " = ?", new String[]{
                        filePath
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //根据filepath判断是否已经存在同样的记录
    public boolean isExist(IntruderPhotoInfo info) {
        List<IntruderPhotoInfo> allInfo = queryIntruderPhotoInfo();
        for (int i = 0; i < allInfo.size(); i++) {
            if (allInfo.get(i).getFilePath() != null && allInfo.get(i).getFilePath().equals(info.getFilePath())) {
                return true;
            }
        }
        return false;
    }

    // 删除所有数据
    public void deleteAllInfo() {
        if (BuildProperties.isApiLevel14()) {
            synchronized (LOCK) {
                SQLiteDatabase db = getHelper().getWritableDatabase();
                db.beginTransaction();
                try {
                    db.delete(TABLE_NAME, null, null);
                    db.setTransactionSuccessful();
                } catch (Throwable e) {

                } finally {
                    db.endTransaction();
                }
            }
        } else {
            SQLiteDatabase db = getHelper().getWritableDatabase();
            db.beginTransaction();
            try {
                db.delete(TABLE_NAME, null, null);
                db.setTransactionSuccessful();
            } catch (Throwable e) {

            } finally {
                db.endTransaction();
            }
        }
    }

    // 查询所有数据
    public ArrayList<IntruderPhotoInfo> queryIntruderPhotoInfo() {
        ArrayList<IntruderPhotoInfo> resultList = new ArrayList<IntruderPhotoInfo>();
        if (BuildProperties.isApiLevel14()) {
            synchronized (LOCK) {
                SQLiteDatabase db = getHelper().getReadableDatabase();
                if (db == null)
                    return resultList;
                Cursor cursor = null;
                IntruderPhotoInfo info;
                try {
                    cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            info = new IntruderPhotoInfo(null, null, null);
                            info.setFilePath(cursor.getString(cursor.getColumnIndex(COL_PIC_PATH)));
                            info.setFromAppPackage(cursor.getString(cursor
                                    .getColumnIndex(COL_FROM_APP_PACKAGENAME)));
                            info.setTimeStamp(cursor.getString(cursor.getColumnIndex(COL_TIME_STAMP)));
                            resultList.add(info);
                        } while (cursor.moveToNext());
                    }
                } catch (Throwable e) {
                } finally {
                    // IoUtils.closeSilently(cursor);
                }
            }
        } else {
            SQLiteDatabase db = getHelper().getReadableDatabase();
            if (db == null)
                return resultList;
            Cursor cursor = null;
            IntruderPhotoInfo info;
            try {
                cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        info = new IntruderPhotoInfo(null, null, null);
                        info.setFilePath(cursor.getString(cursor.getColumnIndex(COL_PIC_PATH)));
                        info.setFromAppPackage(cursor.getString(cursor
                                .getColumnIndex(COL_FROM_APP_PACKAGENAME)));
                        info.setTimeStamp(cursor.getString(cursor.getColumnIndex(COL_TIME_STAMP)));
                        resultList.add(info);
                    } while (cursor.moveToNext());
                }
            } catch (Throwable e) {
            } finally {
                IoUtils.closeSilently(cursor);
            }
        }
        return resultList;
    }
}
