package com.leo.appmaster.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.ThreadManager;
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
    private static final String TAG = "PreferenceTable";
    private static final byte[] LOCK = new byte[1];
    private static PreferenceTable sInstance;

    public static final String TABLE_NAME = "pref_data";

    public static final String COL_KEY = "key";
    public static final String COL_VALUE = "value";

    private static final int BOOL_TRUE = 1;
    private static final int BOOL_FALSE = 0;

    private HashMap<String, Object> mValues;
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

    public PreferenceTable() {
        mValues = new HashMap<String, Object>();
        mSerialExecutor = ThreadManager.newSerialExecutor();
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                loadPreference();
            }
        });
    }

    public void loadPreference() {
        if (mLoaded) return;

        LeoLog.d(TAG, "start to load loadPreference");
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
        LeoLog.d(TAG, "end to load loadPreference");
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
        Object value = getString(key, null);
        if (value == null) {
            return def;
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
        }

        return def;
    }

    public long getLong(String key, long def) {
        Object value = getString(key, null);
        if (value == null) {
            return def;
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
        }

        return def;
    }

    public double getDouble(String key, double def) {
        Object value = getString(key, null);
        if (value == null) {
            return def;
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
        }

        return def;
    }

    public float getFloat(String key, float def) {
        Object value = getString(key, null);
        if (value == null) {
            return def;
        }

        try {
            return Float.parseFloat(value.toString());
        } catch (NumberFormatException e) {
        }

        return def;
    }

    public boolean getBoolean(String key, boolean def) {
        int value = getInt(key, def ? BOOL_TRUE : BOOL_FALSE);

        return value == BOOL_TRUE;
    }

    public synchronized String getString(String key, String def) {
//        awaitLoadedLocked();
        Object v = mValues.get(key);
        return v != null ? v.toString() : def;
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

    public void putString(final String key, String value) {
        if (TextUtils.isEmpty(key) || value == null) return;

        // true和false统一转为 1 和 0
        if (value.equals("true")) {
            value = String.valueOf(BOOL_TRUE);
        } else if (value.equals("false")) {
            value = String.valueOf(BOOL_FALSE);
        }
        final String finalValue = value;
        mSerialExecutor.execute(new Runnable() {
            @Override
            public void run() {
                insertOrUpdate(key, finalValue);
            }
        });
    }

    public void putBundleMap(final Map<String, Object> map, final ISettings.OnBundleSavedListener listener) {
        if (map == null || map.size() == 0) {
            return;
        }

        mValues.putAll(map);
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (BuildProperties.isApiLevel14()) {
                    try {
                        mSerialExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences sp = AppMasterApplication.getInstance().getSharedPreferences(TABLE_NAME, Context.MODE_PRIVATE);
                                final SharedPreferences.Editor editor = sp.edit();
                                for (String key : map.keySet()) {
                                    String value = String.valueOf(map.get(key));
                                    editor.putString(key, value);

                                    checkBooleanAndChange(key, value);
                                }
                                editor.commit();
                                if (listener != null) {
                                    listener.onBundleSaved();
                                }
                            }
                        });
                    } catch (Exception e) {
                    }
                } else {
                    final SQLiteDatabase db = getHelper().getWritableDatabase();
                    if (db == null) return;

                    LeoLog.d(TAG, "<ls> putBundleMap database before exe.");
                    mSerialExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            LeoLog.d(TAG, "<ls> putBundleMap database before update db.");
                            db.beginTransaction();
                            try {
                                ContentValues contentValues = new ContentValues();
                                for (String key : map.keySet()) {
                                    // true和false统一转为 1 和 0
                                    String value = String.valueOf(map.get(key));
                                    contentValues.put(COL_KEY, key);
                                    contentValues.put(COL_VALUE, value);
                                    int rows = db.update(TABLE_NAME, contentValues, COL_KEY + " = ? ", new String[]{key});
                                    if (rows <= 0) {
                                        db.insert(TABLE_NAME, null, contentValues);
                                    }
                                    checkBooleanAndChange(key, value);
                                }
                                db.setTransactionSuccessful();
                                if (listener != null) {
                                    listener.onBundleSaved();
                                }
                                LeoLog.d(TAG, "<ls> putBundleMap database after update db.");
                            } catch (Exception e) {
                                LeoLog.e(TAG, "<ls> putBundleMap ex.", e);
                            } finally {
                                db.endTransaction();
                            }
                        }
                    });
                }
            }
        }, 2000);
    }

    private void insertOrUpdate(String key, String value) {
        mValues.put(key, value);
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
                int rows = db.update(TABLE_NAME, contentValues, COL_KEY + " = ?", new String[]{key});
                if (rows <= 0) {
                    db.insert(TABLE_NAME, null, contentValues);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkBooleanAndChange(String key, String value) {
        if ("true".equals(value)) {
            mValues.put(key, String.valueOf(BOOL_TRUE));
        } else if ("false".equals(value)) {
            mValues.put(key, String.valueOf(BOOL_FALSE));
        }
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
