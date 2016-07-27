package com.zlf.appmaster.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zlf.appmaster.utils.BuildProperties;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.imageloader.utils.IoUtils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by qili on 15-10-16.
 */
public class LockRecommentTable extends BaseTable {
    protected static final String TABLE_NAME = "lock_recomment";

    protected static final String LOCK_REC_MSG_ID = "lock_id";
    protected static final String LOCK_REC_TIME = "lock_time";
    protected static final String LOCK_REC_PACKAGENAME = "lock_packagename";
    protected static final String LOCK_REC_NUM = "lock_num";

    @Override
    public void createTable(SQLiteDatabase db) {
        if (BuildProperties.isApiLevel14()) return;

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "( _id INTEGER PRIMARY KEY," +
                LOCK_REC_MSG_ID + " INTEGER," +
                LOCK_REC_TIME + " TEXT," +
                LOCK_REC_PACKAGENAME + " TEXT," +
                LOCK_REC_NUM + " TEXT);");
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildProperties.isApiLevel14()) return;

        if (oldVersion <= 7 && newVersion >= 8) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            createTable(db);
        }
    }

    /**
     * 插入Recomment列表
     *
     * @param lockRecommentList
     */
    public void insertLockRecommentList(List<String> lockRecommentList, List<String> lockRecommentNumList) {
        LeoLog.d("testLockDb", "come to insert");
        if (BuildProperties.isApiLevel14()) return;
        if (lockRecommentList == null || lockRecommentList.isEmpty()) return;
        if (lockRecommentNumList == null || lockRecommentNumList.isEmpty()) return;

        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) return;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (int i = 0; i < lockRecommentList.size(); i++) {
                values.clear();
                String lockName = lockRecommentList.get(i);
                String lockNameNum = lockRecommentNumList.get(i);
                values.put(LOCK_REC_PACKAGENAME, lockName);
                values.put(LOCK_REC_NUM, lockNameNum);
                if (isKeyExist(LOCK_REC_PACKAGENAME)) {
                    LeoLog.d("testLockDb", lockName + "--exist,update");
                    db.update(TABLE_NAME, values, LOCK_REC_PACKAGENAME + " = ?", new String[]{lockName});
                } else {
                    LeoLog.d("testLockDb", lockName + "--NOexist,insert");
                    db.insert(TABLE_NAME, null, values);
                }
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    private boolean isKeyExist(String key) {
        if (BuildProperties.isApiLevel14()) return false;
        SQLiteDatabase db = getHelper().getReadableDatabase();
        if (db == null) return false;

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, new String[]{LOCK_REC_PACKAGENAME}, LOCK_REC_PACKAGENAME + " = ?",
                    new String[]{key}, null, null, null);
            if (cursor != null) {
                return cursor.getCount() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeSilently(cursor);
        }

        return false;
    }

    public HashMap<String, String> queryLockRecommentList() {
        LeoLog.d("testLockDb", "come to query");
        HashMap<String, String> map = new HashMap<String, String>();
        if (BuildProperties.isApiLevel14()) return map;
        SQLiteDatabase db = getHelper().getReadableDatabase();
        if (db == null) return map;

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    String lockName = cursor.getString(cursor.getColumnIndex(LOCK_REC_PACKAGENAME));
                    String lockNameNum = cursor.getString(cursor.getColumnIndex(LOCK_REC_NUM));
                    LeoLog.d("testLockDb", "name:" + lockName + " , num:" + lockNameNum);

                    map.put(lockName, lockNameNum);
                } while (cursor.moveToNext());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeSilently(cursor);
        }
        return map;
    }
}
