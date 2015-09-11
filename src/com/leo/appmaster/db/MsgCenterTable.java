package com.leo.appmaster.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.leo.appmaster.msgcenter.Message;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息中心数据库表
 * Created by Jasper on 2015/9/10.
 */
public class MsgCenterTable extends BaseTable {
    private static final String TAG = "MsgCenterTable";

    protected static final String TABLE_NAME = "msg_center";

    protected static final String COL_MSG_ID = "msg_id";
    protected static final String COL_TIME = "activity_time";
    protected static final String COL_NAME = "category_name";
    protected static final String COL_DESCRIPTION = "description";
    protected static final String COL_IMAGE_URL = "image_url";
    protected static final String COL_LINK = "link";
    protected static final String COL_OFFLINE_TIME = "offline_time";
    protected static final String COL_TITLE = "title";
    protected static final String COL_TYPE_ID = "type_id";
    // 1:未读  0:已读
    protected static final String COL_UNREAD = "unread";

    private static final int READED = 0;
    private static final int UNREADED = 1;

    public MsgCenterTable() {
    }

    /**
     * 插入消息列表
     *
     * @param msgList
     */
    public void insertMsgList(List<Message> msgList) {
        if (msgList == null || msgList.isEmpty()) return;

        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) return;

        // 先清空数据
//        db.execSQL("delete from " + TABLE_NAME);

        List<Message> oldList = queryMsgList();
        for (Message oldMsg : oldList) {
            for (Message message : msgList) {
                if (message.id == oldMsg.id) {
                    message.unread = oldMsg.unread;
                    break;
                }
            }
        }
        deleteMsgList(oldList);
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
                values.put(COL_TITLE, message.title);
                values.put(COL_OFFLINE_TIME, message.offlineTime);
                values.put(COL_TIME, message.time);
                values.put(COL_TYPE_ID, message.typeId);
                values.put(COL_UNREAD, message.unread ? UNREADED : READED);
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
        SQLiteDatabase db = getHelper().getWritableDatabase();
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
                    message.unread = cursor.getInt(cursor.getColumnIndex(COL_UNREAD)) == UNREADED;
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

    public void readMessage(Message msg) {
        if (msg == null) return;

        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) return;

        ContentValues values = new ContentValues();
        values.put(COL_UNREAD, READED);
        db.update(TABLE_NAME, values, COL_MSG_ID + " = ?", new String[] { msg.id + "" });
    }

    public void deleteMsgList(List<Message> list) {
        if (list == null || list.isEmpty()) return;

        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) return;

        db.beginTransaction();
        try {
            for (Message message : list) {
                db.delete(TABLE_NAME, COL_MSG_ID + " = ?", new String[] { message.id + "" });
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void createTable(SQLiteDatabase db) {
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
                COL_UNREAD + " INTEGER," +
                COL_TYPE_ID + " TEXT);");
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        createTable(db);
    }
}
