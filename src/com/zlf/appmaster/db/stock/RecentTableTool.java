package com.zlf.appmaster.db.stock;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;

import com.zlf.appmaster.model.stock.MessageItem;
import com.zlf.appmaster.model.stock.RecentItem;
import com.zlf.appmaster.model.stock.RecentTableProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * 最近联系列表
 * @author Deping Huang
 *
 */
public class RecentTableTool {

	private static final String TAG = RecentTableTool.class.getSimpleName();
	private ContentResolver mContentResolver;
	private Context mContext;
	private SQLiteDatabase m_db;	// 查询语句直接操作表
    private QiNiuDBHelper m_dbHelper;

	public RecentTableTool(Context context) {
		mContext = context;
        m_dbHelper = QiNiuDBHelper.getInstance(context);
		mContentResolver = context.getContentResolver();
	}


	public void saveRecent(RecentItem item) {
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(RecentTableProvider.COLNAME_SUMMARY, item.getSummary());
            values.put(RecentTableProvider.COLNAME_UPDATE_TIME, item.getUpdateTime());
            values.put(RecentTableProvider.COLNAME_UNREAD_NUM, item.getNewNum());

            Cursor c = getRowInfo(item.getRectType(), item.getSubType(), item.getSubTypeID());
            int unReadNum = 1;
            if (c.moveToFirst()) {    // 有数据则更新信息（摘要、时间、未读数量）

                unReadNum = c.getInt(RecentTableProvider.COLNAME_UNREAD_NUM_INDEX);
                unReadNum++;

                if (!TextUtils.isEmpty(item.getTitle())) {
                    values.put(RecentTableProvider.COLNAME_TITLE, item.getTitle());
                }

                mContentResolver.update(RecentTableProvider.CONTENT_URI, values, RecentTableProvider.WHERE_ROW,
                        new String[]{String.valueOf(item.getRectType()), item.getSubType(), item.getSubTypeID()});
            } else {// 插入
                values.put(RecentTableProvider.COLNAME_UNREAD_NUM, unReadNum);
                values.put(RecentTableProvider.COLNAME_TITLE, item.getTitle());
                values.put(RecentTableProvider.COLNAME_RECT_TYPE, item.getRectType());
                values.put(RecentTableProvider.COLNAME_SUB_TYPE, item.getSubType());
                values.put(RecentTableProvider.COLNAME_SUB_TYPE_ID, item.getSubTypeID());
                mContentResolver.insert(RecentTableProvider.CONTENT_URI, values);
            }

            m_db.close();
            mContentResolver.notifyChange(RecentTableProvider.CONTENT_URI, null);

        }
	}

    /**
     * 有新消息来，更新最近消息记录
     * @param item
     * @param unReadNum 未读条数，有可能一次有多条未读，item只是显示一次，num可能为几条
     */
    public void saveMessageRecent(MessageItem item, int unReadNum){
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(RecentTableProvider.COLNAME_SUMMARY, item.getPushContent());
            values.put(RecentTableProvider.COLNAME_UPDATE_TIME, item.getUpdateTime());
            values.put(RecentTableProvider.COLNAME_TITLE, item.getPushTitle());


            int recentType = RecentItem.TYPE_MSG;
            String recentSubType = String.valueOf(item.getMsgType());
            String recentSubTypeID = String.valueOf(item.getMsgTypeID());


            Cursor c = getRowInfo(recentType, recentSubType, recentSubTypeID);
            if (c.moveToFirst()) {    // 有数据则更新信息（摘要、时间、未读数量）
                //if(!item.isSend()){// 为接收，处理未读数量的变化,若为发送则未读数量不变
                int dbUnReadNum = c.getInt(RecentTableProvider.COLNAME_UNREAD_NUM_INDEX);
                dbUnReadNum += unReadNum;
                values.put(RecentTableProvider.COLNAME_UNREAD_NUM, dbUnReadNum);
                //}

                //更新回去
                mContentResolver.update(RecentTableProvider.CONTENT_URI, values, RecentTableProvider.WHERE_ROW,
                        new String[]{String.valueOf(recentType), recentSubType, recentSubTypeID});


            } else {                    // 全新插入（包括标题、摘要、时间、图标、未读数量）
                values.put(RecentTableProvider.COLNAME_UNREAD_NUM, unReadNum);

                values.put(RecentTableProvider.COLNAME_RECT_TYPE, recentType);
                values.put(RecentTableProvider.COLNAME_SUB_TYPE, recentSubType);
                values.put(RecentTableProvider.COLNAME_SUB_TYPE_ID, recentSubTypeID);
                mContentResolver.insert(RecentTableProvider.CONTENT_URI, values);

            }

            c.close();
            m_db.close();

            mContentResolver.notifyChange(RecentTableProvider.CONTENT_URI, null);

        }
    }

	
	public void saveRecentArray(List<RecentItem> itemArray){
		if(itemArray == null) return;

        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();
            int len = itemArray.size();
            ContentValues[] valuesArray = new ContentValues[len];
            for (int i = 0; i < len; i++) {
                RecentItem item = itemArray.get(i);
                valuesArray[i] = new ContentValues();
                valuesArray[i].put(RecentTableProvider.COLNAME_SUMMARY, item.getSummary());
                valuesArray[i].put(RecentTableProvider.COLNAME_UPDATE_TIME, item.getUpdateTime());
                valuesArray[i].put(RecentTableProvider.COLNAME_TITLE, item.getTitle());
                valuesArray[i].put(RecentTableProvider.COLNAME_RECT_TYPE, item.getRectType());
                valuesArray[i].put(RecentTableProvider.COLNAME_SUB_TYPE, item.getSubType());
                valuesArray[i].put(RecentTableProvider.COLNAME_UNREAD_NUM, item.getNewNum());
            }

            // bulkInsert 里面已做了更新操作
            mContentResolver.bulkInsert(RecentTableProvider.CONTENT_URI, valuesArray);

            m_db.close();
            mContentResolver.notifyChange(RecentTableProvider.CONTENT_URI, null);

        }
	}
	

	
	/**
	 * 从最近联系人中 增加/删除股友
	 * @param friendUin
	 * @param friendName
	 */
	public void addFriendToRecent(long friendUin, String friendName){
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();

            if (!isExist(RecentItem.TYPE_MSG, String.valueOf(friendUin), "0")) {
                ContentValues values = new ContentValues();
                values.put(RecentTableProvider.COLNAME_SUMMARY, "你们已经成为好友，可以开始聊天了");
                values.put(RecentTableProvider.COLNAME_UPDATE_TIME, System.currentTimeMillis());
                values.put(RecentTableProvider.COLNAME_UNREAD_NUM, 0);
                values.put(RecentTableProvider.COLNAME_TITLE, friendName);
                values.put(RecentTableProvider.COLNAME_RECT_TYPE, RecentItem.TYPE_MSG);
                values.put(RecentTableProvider.COLNAME_SUB_TYPE, String.valueOf(MessageItem.MsgType.P2P));
                mContentResolver.insert(RecentTableProvider.CONTENT_URI, values);

                m_db.close();
                mContentResolver.notifyChange(RecentTableProvider.CONTENT_URI, null);
                return;
            }
            m_db.close();
            return;
        }
	}

    public List<RecentItem> getRecentItemArray(){
        List<RecentItem> ret = new ArrayList<RecentItem>();

        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getReadableDatabase();
            Cursor cursor =  getRecentList();

            if (null != cursor){
                while (cursor.moveToNext()){
                    int recentType = cursor.getInt(RecentTableProvider.COLNAME_RECT_TYPE_INDEX);
                    String subType = cursor.getString(RecentTableProvider.COLNAME_SUB_TYPE_INDEX);
                    String subTypeID = cursor.getString(RecentTableProvider.COLNAME_SUB_TYPE_ID_INDEX);
                    String title = cursor.getString(RecentTableProvider.COLNAME_TITLE_INDEX);
                    long time = cursor.getLong(RecentTableProvider.COLNAME_UPDATE_TIME_INDEX);
                    String summary = cursor.getString(RecentTableProvider.COLNAME_SUMMARY_INDEX);
                    int num = cursor.getInt(RecentTableProvider.COLNAME_UNREAD_NUM_INDEX);

                    RecentItem item = new RecentItem();
                    item.setTitle(title);
                    item.setSummary(summary);
                    item.setSubType(subType);
                    item.setNewNum(num);
                    item.setUpdateTime(time);
                    item.setRectType(recentType);
                    item.setSubTypeID(subTypeID);

                    ret.add(item);
                }
                cursor.close();
            }


            m_db.close();
        }

        return ret;
    }


    private Cursor getRecentList() {
        String selection = null;
        String[] selectionArgs = null;

        return m_db.query(RecentTableProvider.RECENT_TABLE_NAME, null, selection,
                selectionArgs, null, null, RecentTableProvider.COLNAME_UPDATE_TIME + " desc");
    }

    public void swapRecentList(SimpleCursorAdapter cursorAdapter, int type){

        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getReadableDatabase();

            cursorAdapter.swapCursor(getRecentList());
        }
    }

	 
	private boolean isExist(int rectType, String subType, String subTypeID) {

		Cursor c =m_db.query(RecentTableProvider.RECENT_TABLE_NAME, null, RecentTableProvider.WHERE_ROW,
				new String[] { String.valueOf(rectType), subType, subTypeID }, null, null, null);
		boolean ret = c.moveToFirst();
		c.close();
		return ret;
	}
	
	// 查询单行信息
	private Cursor getRowInfo(int rectType, String subType, String subTypeID){
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getReadableDatabase();
            return m_db.query(RecentTableProvider.RECENT_TABLE_NAME, null, RecentTableProvider.WHERE_ROW,
                    new String[] { String.valueOf(rectType), subType, subTypeID }, null, null, null);
        }
	}

    /**
     * 获取未读数量
     * @return
     */
    public int getUnReadCount(){
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getReadableDatabase();
            Cursor cursor = m_db.query(RecentTableProvider.RECENT_TABLE_NAME, new String[]{"sum("+RecentTableProvider.COLNAME_UNREAD_NUM+")"},
                    RecentTableProvider.WHERE_UNREAD_NUM_ROW,
                    null, null, null, null);
            if (null != cursor){
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0);
                }
            }

            return 0;
        }
    }
	


    public int delRecent(RecentItem item) {
        return  delRecent(item.getRectType(), item.getSubType(), item.getSubTypeID());
    }

    public int delRecent(int rectType, String subType, String subTypeID) {
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();
            int ret = mContentResolver.delete(RecentTableProvider.CONTENT_URI,
                    RecentTableProvider.WHERE_ROW, new String[]{String.valueOf(rectType), subType, subTypeID});

            m_db.close();
            mContentResolver.notifyChange(RecentTableProvider.CONTENT_URI, null);
            return ret;

        }
    }


	/**
	 * 清除未读数量
	 */
	public void clearUnReadNum(RecentItem item){
        clearUnReadNum(item.getRectType(), item.getSubType(), item.getSubTypeID());
	}

    public void clearUnReadNum(int rectType, String subType, String subTypeID){
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(RecentTableProvider.COLNAME_UNREAD_NUM, 0);
            mContentResolver.update(RecentTableProvider.CONTENT_URI, values, RecentTableProvider.WHERE_ROW,
                    new String[]{String.valueOf(rectType), subType, subTypeID});
            m_db.close();
            mContentResolver.notifyChange(RecentTableProvider.CONTENT_URI, null);
        }
    }
	
	
	
	public void registerContentObserver(ContentObserver observer){
		    mContentResolver.registerContentObserver(RecentTableProvider.CONTENT_URI, true, observer);
	}
	
	public void unregisterContentObserver(ContentObserver observer){
		    mContentResolver.unregisterContentObserver(observer);
	}



	public void close() {
//		if (m_db != null)
//			m_db.close();
	}


    /**
     * 测试聊天室
     */
    public static void testAddChatRoom(Context context){
        RecentTableTool recentTableTool = new RecentTableTool(context);

        RecentItem recentItem = new RecentItem();
        recentItem.setRectType(RecentItem.TYPE_MSG);
        recentItem.setSubType("100000");
        recentItem.setSummary("直播间测试…");
        recentItem.setUpdateTime(0L);
        recentItem.setTitle("王亚伟");
        recentItem.setNewNum(0);
        recentTableTool.saveRecent(recentItem);
    }

    /**
     * 测试点对点聊天
     * @param context
     */
    public static void testAddChatP2P(Context context){
        RecentTableTool recentTableTool = new RecentTableTool(context);

        RecentItem recentItem = new RecentItem();
        recentItem.setRectType(RecentItem.TYPE_MSG);
        recentItem.setSubType(String.valueOf(MessageItem.MsgType.P2P));
        recentItem.setSubTypeID("60240");
        recentItem.setSummary("聊天消息测试…");
        recentItem.setUpdateTime(0L);
        recentItem.setTitle("Ping_QQ");
        recentItem.setNewNum(0);
        recentTableTool.saveRecent(recentItem);
    }
	
}
