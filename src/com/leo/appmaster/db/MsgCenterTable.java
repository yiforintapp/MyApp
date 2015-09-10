package com.leo.appmaster.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.msgcenter.Message;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/9/10.
 */
public class MsgCenterTable extends SQLiteOpenHelper {
    private static final String TAG = "MsgCenterTable";

    protected static final String DATABASE_NAME = AppMasterDBHelper.DB_NAME;
    protected static final String TABLE_NAME = "msg_center";
    private static final int DATABASE_VERSION = 1;

    protected static final String COL_MSG_ID = "msg_id";
    protected static final String COL_TIME = "activity_time";
    protected static final String COL_NAME = "category_name";
    protected static final String COL_DESCRIPTION = "description";
    protected static final String COL_IMAGE_URL = "image_url";
    protected static final String COL_LINK = "link";
    protected static final String COL_OFFLINE_TIME = "offline_time";
    protected static final String COL_TITLE = "title";
    protected static final String COL_TYPE_ID = "type_id";

    protected static MsgCenterTable sInstance;

    public static synchronized MsgCenterTable getInstance() {
        if (sInstance == null) {
            AppMasterApplication ctx = AppMasterApplication.getInstance();
            sInstance = new MsgCenterTable(ctx);
        }

        return sInstance;
    }

    public MsgCenterTable(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "( _id INTEGER PRIMARY KEY," +
                COL_MSG_ID + " INTEGER," +
                COL_TIME + " TEXT," +
                COL_NAME + " TEXT," +
                COL_DESCRIPTION + " TEXT," +
                COL_IMAGE_URL + " TEXT," +
                COL_LINK + " TEXT," +
                COL_OFFLINE_TIME + " TEXT," +
                COL_TITLE + " TEXT," +
                COL_TYPE_ID + " TEXT");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * 插入消息列表
     *
     * @param msgList
     */
    public void insertMsgList(List<Message> msgList) {
        if (msgList == null || msgList.isEmpty()) return;

        SQLiteDatabase db = getWritableDatabase();
        if (db == null) return;

        // 先清空数据
        db.delete(TABLE_NAME, null, null);
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();

            for (Message message : msgList) {
                values.clear();

                values.put(COL_DESCRIPTION, message.description);
                values.put(COL_IMAGE_URL, message.imageUrl);
                values.put(COL_LINK, message.jumpUrl);
                values.put(COL_MSG_ID, message.id);
                values.put(COL_NAME, message.name);
                values.put(COL_OFFLINE_TIME, message.offlineTime);
                values.put(COL_TIME, message.time);
                values.put(COL_TYPE_ID, message.typeId);
                db.insert(TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 获取消息列表
     * @return
     */
    public List<Message> queryMsgList() {
        ArrayList<Message> result = new ArrayList<Message>();
        SQLiteDatabase db = getWritableDatabase();
        if (db == null) return result;
        
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    Message message = new Message();
                    message.description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
                    message.imageUrl = cursor.getString(cursor.getColumnIndex(COL_IMAGE_URL));
                    message.jumpUrl = cursor.getString(cursor.getColumnIndex(COL_LINK));
                    message.id = cursor.getInt(cursor.getColumnIndex(COL_MSG_ID));
                    message.name = cursor.getString(cursor.getColumnIndex(COL_NAME));
                    message.offlineTime = cursor.getString(cursor.getColumnIndex(COL_OFFLINE_TIME));
                    message.time = cursor.getString(cursor.getColumnIndex(COL_TIME));
                    message.title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                    message.typeId = cursor.getString(cursor.getColumnIndex(COL_TYPE_ID));
                    
                    result.add(message);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LeoLog.e(TAG, "queryMsgList ex.", e);
        } finally {
            IoUtils.closeSilently(cursor);
        }
        
        return result;
    }
}
