package com.leo.appmaster.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.UFOActivity;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by Jasper on 2016/3/29.
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

    static PreferenceTable getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new PreferenceTable();
                }
            }
        }

        return sInstance;
    }

    PreferenceTable() {
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

    public synchronized void putString(final String key, String value) {
        if (TextUtils.isEmpty(key) || value == null) return;

        // true和false统一转为 1 和 0
        if (value.equals("true")) {
            value = String.valueOf(BOOL_TRUE);
        } else if (value.equals("false")) {
            value = String.valueOf(BOOL_FALSE);
        }
        mValues.put(key, value);

        final String finalValue = value;
        if (BuildProperties.isApiLevel14()) {
            if (mSerialExecutor == null) {
                mSerialExecutor = ThreadManager.newSerialExecutor();
            }
            mSerialExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    insertOrUpdate(key, finalValue);
                }
            });
        } else {
            if (mSerialExecutor == null) {
                mSerialExecutor = ThreadManager.newSerialExecutor();
            }
            mSerialExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    insertOrUpdate(key, finalValue);
                }
            });
        }
    }

    public synchronized void putBundleMap(final Map<String, Object> map) {
        if (map == null || map.size() == 0) {
            return;
        }

        for (String key : map.keySet()) {
            String value = String.valueOf(map.get(key));

            // true和false统一转为 1 和 0
            boolean change = false;
            if (value.equals("true")) {
                value = String.valueOf(BOOL_TRUE);
                change = true;
            } else if (value.equals("false")) {
                value = String.valueOf(BOOL_FALSE);
                change = true;
            }
            if (change) {
                map.put(key, value);
            }
            mValues.put(key, value);
        }
        if (BuildProperties.isApiLevel14()) {
            try {
                SharedPreferences sp = AppMasterApplication.getInstance().getSharedPreferences(TABLE_NAME, Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = sp.edit();
                for (String key : map.keySet()) {
                    String value = String.valueOf(map.get(key));
                    editor.putString(key, value);
                }
                mSerialExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        editor.commit();
                    }
                });
            } catch (Exception e) {
            }
        } else {
            final SQLiteDatabase db = getHelper().getWritableDatabase();
            if (db == null) return;

            mSerialExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    db.beginTransaction();
                    try {
                        ContentValues contentValues = new ContentValues();
                        for (String key : map.keySet()) {
                            // true和false统一转为 1 和 0
                            String value = String.valueOf(map.get(key));
                            contentValues.put(key, value);
                            int rows = db.update(TABLE_NAME, contentValues, key + " = ? ", new String[]{key});
                            if (rows <= 0) {
                                db.insert(TABLE_NAME, null, contentValues);
                            }
                        }
                        db.setTransactionSuccessful();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        db.endTransaction();
                    }
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
