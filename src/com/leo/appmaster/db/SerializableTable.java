package com.leo.appmaster.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

/**
 * 通用序列化存储
 * @author Jasper
 *
 */
public class SerializableTable extends SQLiteOpenHelper {
	
	protected static final String DATABASE_NAME = AppMasterDBHelper.DB_NAME;
	protected static final String TABLE_NAME = "serialize_data";
	private static final int DATABASE_VERSION = 2;
	
	protected static final String COL_KEY = "key";
	protected static final String COL_BLOB = "blob";

	protected static final String[] COLS_SIZE = new String[] { COL_KEY };
	
	protected static SerializableTable sInstance;
	
	public static synchronized SerializableTable getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new SerializableTable(context.getApplicationContext());
		}
		
		return sInstance;
	}

	public SerializableTable(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + 
				"( _id INTEGER PRIMARY KEY," + 
				COL_KEY + " TEXT," + 
				COL_BLOB + " BLOB);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
	
	/**
	 * 保存数据
	 * @param key
	 * @param data
	 */
	public synchronized void saveData(String key, Serializable data) {
	    if (TextUtils.isEmpty(key) || data == null) return;
	    
	    SQLiteDatabase db = getWritableDatabase();
        if (db == null) return;
        
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_KEY, key);

            ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
            ObjectOutputStream out = null;
            try {
                out = new ObjectOutputStream(baos);
                out.writeObject(data);
            } catch (IOException ex) {
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            values.put(COL_BLOB, baos.toByteArray());
            db.insert(TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        } 
	}
	
	/**
	 * 获取数据
	 * @param key
	 * @return
	 */
	public synchronized Serializable getData(String key) {
	    SQLiteDatabase db = getReadableDatabase();
        if (db == null || TextUtils.isEmpty(key))
            return null;
        
        Cursor cursor = null;
        ObjectInputStream ois = null;
        try {
            cursor = db.query(TABLE_NAME, null, COL_KEY + " = ?", new String[] { key }, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                byte[] bs = cursor.getBlob(cursor.getColumnIndex(COL_BLOB));

                ByteArrayInputStream bais = new ByteArrayInputStream(bs);
                ObjectInputStream in = null;

                Serializable serializable = null;
                try {
                    // stream closed in the finally
                    in = new ObjectInputStream(bais);
                    serializable = (Serializable) in.readObject();
                } catch (Exception ex) {
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException ex) { // NOPMD
                        // ignore close exception
                    }
                    try {
                        bais.close();
                    } catch (IOException e) {
                        // ignore close exception
                    }
                }

                return serializable;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
	}
	
}
