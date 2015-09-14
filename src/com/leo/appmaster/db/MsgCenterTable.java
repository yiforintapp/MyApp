package com.leo.appmaster.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.leo.appmaster.msgcenter.Message;
import com.leo.appmaster.schedule.MsgCenterFetchJob;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息中心数据库表
 *  插入列表、删除列表、获取未读计数、获取列表、标记已读
 * Created by Jasper on 2015/9/10.
 */
public class MsgCenterTable extends BaseTable {
    private static final String TAG = "MsgCenterTable";

    protected static final String TABLE_NAME = "msg_center";

    protected static final String COL_MSG_ID = "msg_id";
    protected static final String COL_TIME = "activity_time";
    protected static final String COL_CATEGORY_NAME = "category_name";
    protected static final String COL_CATEGORY_CODE = "category_code";
    protected static final String COL_DESCRIPTION = "description";
    protected static final String COL_IMAGE_URL = "image_url";
    protected static final String COL_LINK = "link";
    protected static final String COL_OFFLINE_TIME = "offline_time";
    protected static final String COL_TITLE = "title";
    // 资源包地址
    protected static final String COL_RES = "res";
    // 1:未读  0:已读
    protected static final String COL_UNREAD = "unread";

    protected static final String COL_RES_PATH = "respath";

    private static final int READED = 0;
    private static final int UNREADED = 1;

    public MsgCenterTable() {
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "( _id INTEGER PRIMARY KEY," +
                COL_MSG_ID + " INTEGER," +
                COL_TIME + " TEXT," +
                COL_CATEGORY_NAME + " TEXT," +
                COL_CATEGORY_CODE + " TEXT," +
                COL_DESCRIPTION + " TEXT," +
                COL_IMAGE_URL + " TEXT," +
                COL_LINK + " TEXT," +
                COL_OFFLINE_TIME + " TEXT," +
                COL_TITLE + " TEXT," +
                COL_RES + " TEXT," +
                COL_RES_PATH + " TEXT," +
                COL_UNREAD + " INTEGER);");
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        createTable(db);
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
        List<Message> oldList = queryMsgList();
        for (Message oldMsg : oldList) {
            for (Message message : msgList) {
                if (message.msgId == oldMsg.msgId) {
                    // 仅仅保存已读/未读状态
                    message.unread = oldMsg.unread;
                    break;
                }
            }
        }
        deleteMsgList(oldList, msgList);
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();

            for (Message message : msgList) {
                values.clear();

                values.put(COL_DESCRIPTION, message.description);
                values.put(COL_IMAGE_URL, message.imageUrl);
                values.put(COL_LINK, message.jumpUrl);
                values.put(COL_MSG_ID, message.msgId);
                values.put(COL_CATEGORY_NAME, message.categoryName);
                values.put(COL_CATEGORY_CODE, message.categoryCode);
                values.put(COL_TITLE, message.title);
                values.put(COL_OFFLINE_TIME, message.offlineTime);
                values.put(COL_TIME, message.time);
                values.put(COL_RES, message.resUrl);
                values.put(COL_UNREAD, message.unread ? UNREADED : READED);
                db.insert(TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            e.printStackTrace();
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
        SQLiteDatabase db = getHelper().getReadableDatabase();
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
                    message.msgId = cursor.getInt(cursor.getColumnIndex(COL_MSG_ID));
                    message.categoryName = cursor.getString(cursor.getColumnIndex(COL_CATEGORY_NAME));
                    message.categoryCode = cursor.getString(cursor.getColumnIndex(COL_CATEGORY_CODE));
                    message.offlineTime = cursor.getString(cursor.getColumnIndex(COL_OFFLINE_TIME));
                    message.time = cursor.getString(cursor.getColumnIndex(COL_TIME));
                    message.title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                    message.resUrl = cursor.getString(cursor.getColumnIndex(COL_RES));
                    message.unread = cursor.getInt(cursor.getColumnIndex(COL_UNREAD)) == UNREADED;
                    result.add(message);
                } while (cursor.moveToNext());
            }
        } catch (Throwable e) {
            LeoLog.e(TAG, "queryMsgList ex.", e);
        } finally {
            IoUtils.closeSilently(cursor);
        }
        
        return result;
    }

    /**
     * 标记消息为已读
     * @param msg
     */
    public void readMessage(Message msg) {
        if (msg == null) return;

        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) return;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_UNREAD, READED);
            db.update(TABLE_NAME, values, COL_MSG_ID + " = ?", new String[] { msg.msgId + "" });

            msg.unread = false;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void deleteMsgList(List<Message> delList, List<Message> list) {
        if (delList == null || delList.isEmpty()) return;

        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) return;

        db.beginTransaction();
        try {
            for (Message message : delList) {
                db.delete(TABLE_NAME, COL_MSG_ID + " = ?", new String[] { message.msgId + "" });
                if (list == null || !message.isCategoryUpdate()) continue;

                for (Message msg : list) {
                    if (msg.msgId == message.msgId) {
                        // 如果url不一致了，需要把旧的缓存文件删掉
                        String name = MsgCenterFetchJob.getFileName(message.jumpUrl);
                        String path = MsgCenterFetchJob.getFilePath(name);
                        if (!msg.jumpUrl.equals(message.jumpUrl)) {
                            File htmlFile = new File(path);
                            if (htmlFile.exists()) {
                                htmlFile.delete();
                            }
                        }
                        if (!msg.resUrl.equals(message.resUrl)) {
                            File zipFile = new File(path + ".zip");
                            if (zipFile.exists()) {
                                zipFile.delete();
                            }
                        }
                    }
                }
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 获取更新日志
     * @return
     */
    public Message getUpdateMessage() {
        Message message = null;

        SQLiteDatabase db = getHelper().getReadableDatabase();
        if (db == null) return message;

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                message = new Message();
                message.description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
                message.imageUrl = cursor.getString(cursor.getColumnIndex(COL_IMAGE_URL));
                message.jumpUrl = cursor.getString(cursor.getColumnIndex(COL_LINK));
                message.msgId = cursor.getInt(cursor.getColumnIndex(COL_MSG_ID));
                message.categoryName = cursor.getString(cursor.getColumnIndex(COL_CATEGORY_NAME));
                message.categoryCode = cursor.getString(cursor.getColumnIndex(COL_CATEGORY_CODE));
                message.offlineTime = cursor.getString(cursor.getColumnIndex(COL_OFFLINE_TIME));
                message.time = cursor.getString(cursor.getColumnIndex(COL_TIME));
                message.title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                message.resUrl = cursor.getString(cursor.getColumnIndex(COL_RES));
                message.unread = cursor.getInt(cursor.getColumnIndex(COL_UNREAD)) == UNREADED;
            }
        } finally {
            IoUtils.closeSilently(cursor);
        }

        return message;
    }

    /**
     * 获取未读计数
     * @return
     */
    public int getUnreadCount() {
        SQLiteDatabase db = getHelper().getReadableDatabase();
        if (db == null) return 0;

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, new String[] { COL_TITLE },
                    COL_UNREAD + " = ?", new String[] { UNREADED + "" }, null, null, null);
            if (cursor != null) {
                return cursor.getCount();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeSilently(cursor);
        }

        return 0;
    }

}
