package com.leo.appmaster.db;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;

/**
 * Created by Jasper on 2015/9/29.
 */
public class PreferenceTable extends BaseTable {

    private static final byte[] LOCK = new byte[1];
    private static PreferenceTable sInstance;

    public static final String TABLE_NAME = "pref_data";

    public static final String COL_KEY = "key";
    public static final String COL_VALUE = "value";

    private static final int BOOL_TRUE = 1;
    private static final int BOOL_FALSE = 0;

    private HashMap<String, String> mValues;
    private Executor mSerialExecutor;

    private boolean mLoaded;

    public static PreferenceTable getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new PreferenceTable();
                }
            }
        }

        return sInstance;
    }

    public PreferenceTable() {
        mValues = new HashMap<String, String>();
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                synchronized (PreferenceTable.this) {
                    loadPreference();
                }
            }
        });
    }

    public void loadPreference() {
        if (mLoaded) return;

        // 确保能读取数据之前，数据库已经ready
        getHelper().getReadableDatabase();
        if (BuildProperties.isApiLevel14()) {
            try {
                SharedPreferences sp = AppMasterApplication.getInstance().getSharedPreferences(TABLE_NAME, Context.MODE_PRIVATE);
                Map<String, ?> all = sp.getAll();
                for (String key : all.keySet()) {
                    mValues.put(key, (String) all.get(key));
                }
            } catch (Exception e) {
            }
        } else {
            SQLiteDatabase db = getHelper().getReadableDatabase();
            if (db == null) return;

            Cursor cursor = null;
            try {
                cursor = db.query(TABLE_NAME, new String[]{COL_KEY, COL_VALUE}, null,
                        null, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        String key = cursor.getString(cursor.getColumnIndex(COL_KEY));
                        String value = cursor.getString(cursor.getColumnIndex(COL_VALUE));
                        LeoLog.d(TABLE_NAME, "[" + key + " : " + value + "]");
                        mValues.put(key, value);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IoUtils.closeSilently(cursor);
            }
        }

        mLoaded = true;
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        if (BuildProperties.isApiLevel14()) return;
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "( _id INTEGER PRIMARY KEY," +
                COL_KEY + " TEXT," +
                COL_VALUE + " TEXT);");
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildProperties.isApiLevel14()) return;
        if (oldVersion <= 7 && newVersion >= 8) {
            // 版本8才加入此表
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    "( _id INTEGER PRIMARY KEY," +
                    COL_KEY + " TEXT," +
                    COL_VALUE + " TEXT);");
        }
    }

    public int getInt(String key, int def) {
        String value = getString(key);
        if (value == null) {
            return def;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }

        return def;
    }

    public long getLong(String key, long def) {
        String value = getString(key);
        if (value == null) {
            return def;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
        }

        return def;
    }

    public double getDouble(String key, double def) {
        String value = getString(key);
        if (value == null) {
            return def;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
        }

        return def;
    }

    public float getFloat(String key, float def) {
        String value = getString(key);
        if (value == null) {
            return def;
        }

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
        }

        return def;
    }

    public boolean getBoolean(String key, boolean def) {
        int value = getInt(key, def ? BOOL_TRUE : BOOL_FALSE);

        return value == BOOL_TRUE;
    }

    public synchronized String getString(String key) {
        awaitLoadedLocked();
        return mValues.get(key);
    }

    public void putInt(String key, int value) {
        putString(key, value + "");
    }

    public void putDouble(String key, double value) {
        putString(key, Double.toString(value));
    }

    public void putFloat(String key, float value) {
        putString(key, Float.toString(value));
    }

    public void putBoolean(String key, boolean value) {
        putInt(key, value ? BOOL_TRUE : BOOL_FALSE);
    }

    public void putLong(String key, long value) {
        putString(key, value + "");
    }

    public synchronized void putString(final String key, final String value) {
        if (TextUtils.isEmpty(key) || value == null) return;
        if (BuildProperties.isApiLevel14()) {
            mValues.put(key, value);
            if (mSerialExecutor == null) {
                mSerialExecutor = ThreadManager.newSerialExecutor();
            }
            mSerialExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    insertOrUpdate(key, value);
                }
            });
        } else {
            mValues.put(key, value);
            if (mSerialExecutor == null) {
                mSerialExecutor = ThreadManager.newSerialExecutor();
            }
            mSerialExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    insertOrUpdate(key, value);
                }
            });
        }
    }

    private void insertOrUpdate(String key, String value) {
        if (BuildProperties.isApiLevel14()) {
            try {
                SharedPreferences sp = AppMasterApplication.getInstance().getSharedPreferences(TABLE_NAME, Context.MODE_PRIVATE);
                sp.edit().putString(key, value).commit();
            } catch (Exception e) {
            }
        } else {
            SQLiteDatabase db = getHelper().getWritableDatabase();
            if (db == null) return;

            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_KEY, key);
            contentValues.put(COL_VALUE, value);
            try {
                if (isKeyExist(key)) {
                    db.update(TABLE_NAME, contentValues, COL_KEY + " = ?", new String[]{key});
                } else {
                    db.insert(TABLE_NAME, null, contentValues);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isKeyExist(String key) {
        SQLiteDatabase sd = getHelper().getReadableDatabase();
        if (sd == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            cursor = sd.query(TABLE_NAME, new String[]{COL_KEY}, COL_KEY + " = ?",
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

    private void awaitLoadedLocked() {
        while (!mLoaded) {
            try {
                wait();
            } catch (InterruptedException unused) {
            }
        }
    }


}
