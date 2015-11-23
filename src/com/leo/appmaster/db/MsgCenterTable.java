package com.leo.appmaster.db;

import android.accounts.ChooseAccountTypeActivity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.MsgCenterEvent;
import com.leo.appmaster.msgcenter.Message;
import com.leo.appmaster.schedule.MsgCenterFetchJob;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息中心数据库表
 * 插入列表、删除列表、获取未读计数、获取列表、标记已读
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
    // 失效时间
    protected static final String COL_END_TIME = "end_time";
    // 生效时间
    protected static final String COL_START_TIME = "start_time";


    private static final int READED = 0;
    private static final int UNREADED = 1;

    public MsgCenterTable() {
    }

    @Override
    public void createTable(SQLiteDatabase db) {
//        if (BuildProperties.isApiLevel14()) return;

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
                COL_START_TIME + " TEXT," +
                COL_END_TIME + " TEXT," +
                COL_UNREAD + " INTEGER);");
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
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
        List<Message> oldList = queryMsgList(true);
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

        int count = 0;
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

                if (message.unread) {
                    count++;
                }
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            MsgCenterEvent msgCenterEvent = new MsgCenterEvent(MsgCenterEvent.ID_MSG);
            msgCenterEvent.count = count;
            LeoEventBus.getDefaultBus().post(msgCenterEvent);
        }
    }

    /**
     * 获取消息列表
     *
     * @param includeNoCacheMsg 强制返回所有数据, 包含更新日志没有cache的
     * @return
     */
    public List<Message> queryMsgList(boolean includeNoCacheMsg) {
        ArrayList<Message> result = new ArrayList<Message>();
        SQLiteDatabase db = getHelper().getReadableDatabase();
        if (db == null) return result;

        List<Message> offlineList = new ArrayList<Message>();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    Message message = getMessage(cursor);
                    boolean offline = message.isOffline();
                    if (offline) {
                        offlineList.add(message);
                        continue;
                    }
                    if (message.isCategoryUpdate() && !message.hasCacheFile() && !includeNoCacheMsg) {
                        continue;
                    }
                    result.add(message);
                } while (cursor.moveToNext());
            }
        } catch (Throwable e) {
            LeoLog.e(TAG, "queryMsgList ex.", e);
        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }
        // 删除已下线的活动
        deleteMsgList(offlineList, null);

        return result;
    }

    /**
     * 标记消息为已读
     *
     * @param msg
     */
    public void readMessage(Message msg) {
        if (msg == null) return;

        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) return;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_UNREAD, READED);
            db.update(TABLE_NAME, values, COL_MSG_ID + " = ?", new String[]{msg.msgId + ""});

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
                db.delete(TABLE_NAME, COL_MSG_ID + " = ?", new String[]{message.msgId + ""});
                if (list == null || !message.isCategoryUpdate()) continue;

                if (list == null) continue;

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

    public void clear() {
        List<Message> list = queryMsgList(true);
        if (list != null && list.size() > 0) {
            SQLiteDatabase db = getHelper().getWritableDatabase();
            db.beginTransaction();
            try {
                for (Message message : list) {
                    db.delete(TABLE_NAME, COL_MSG_ID + " = ?", new String[]{message.msgId + ""});
                }
                db.setTransactionSuccessful();
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
                MsgCenterEvent msgCenterEvent = new MsgCenterEvent(MsgCenterEvent.ID_MSG);
                msgCenterEvent.count = 0;
                LeoEventBus.getDefaultBus().post(msgCenterEvent);
            }
        }

        String filePath = MsgCenterFetchJob.getFilePath("name");
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (!parent.exists()) return;

        // 递归删除缓存文件
        deleteRecursively(parent);
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] listFile = file.listFiles();
            if (listFile != null) {
                for (File f : listFile) {
                    deleteRecursively(f);
                }
            }
        } else {
            file.delete();
        }
    }

    /**
     * 获取更新日志
     *
     * @return
     */
    public List<Message> getUpdateMessage() {
        List<Message> result = new ArrayList<Message>();

        SQLiteDatabase db = getHelper().getReadableDatabase();
        if (db == null) return result;

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    Message message = getMessage(cursor);
                    if (!message.isCategoryUpdate()) continue;

                    result.add(message);
                } while (cursor.moveToNext());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }

        return result;
    }

    /**
     * 获取未读计数
     *
     * @return
     */
    public int getUnreadCount() {
        SQLiteDatabase db = getHelper().getReadableDatabase();
        if (db == null) return 0;

        List<Message> offlineList = new ArrayList<Message>();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, null,
                    COL_UNREAD + " = ?", new String[]{UNREADED + ""}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int result = cursor.getCount();
                cursor.moveToFirst();
                do {
                    Message message = getMessage(cursor);
                    boolean offline = message.isOffline();
                    if ((message.isCategoryUpdate() && !message.hasCacheFile()) || offline) {
                        result--;
                    }
                    if (offline) {
                        offlineList.add(message);
                    }
                } while (cursor.moveToNext());
                return result;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }
        // 删除已经下线的活动
        deleteMsgList(offlineList, null);

        return 0;
    }

    private Message getMessage(Cursor cursor) {
        Message message = new Message();
        if (cursor == null) return message;

        message.categoryCode = cursor.getString(cursor.getColumnIndex(COL_CATEGORY_CODE));
        message.description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
        message.imageUrl = cursor.getString(cursor.getColumnIndex(COL_IMAGE_URL));
        message.jumpUrl = cursor.getString(cursor.getColumnIndex(COL_LINK));
        message.msgId = cursor.getInt(cursor.getColumnIndex(COL_MSG_ID));
        message.categoryName = cursor.getString(cursor.getColumnIndex(COL_CATEGORY_NAME));
        message.offlineTime = cursor.getString(cursor.getColumnIndex(COL_OFFLINE_TIME));
        message.time = cursor.getString(cursor.getColumnIndex(COL_TIME));
        message.title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
        message.resUrl = cursor.getString(cursor.getColumnIndex(COL_RES));
        message.unread = cursor.getInt(cursor.getColumnIndex(COL_UNREAD)) == UNREADED;

        return message;
    }

}
