package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zlf.appmaster.model.topic.TopicFavoriteItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * Created by Huang on 2015/6/16.
 */
public class TopicFavoriteTable extends SyncBaseTable {
    public static final String TABLE_NAME = "topic_favorite";

    public static final String COLNAME_TOPIC_ID = "topic_id";				    // 题材ID
    public static final String COLNAME_SORT_CODE = "sort_code";                 // 排序码
    public static final String COLNAME_TYPE = "type";                           // 类型


    /**
     * 数据库操作SQL
     */
    public static final String OTHER_STRUCTURE =
            ","
            + COLNAME_TOPIC_ID +     "	INT,"
            + COLNAME_SORT_CODE  +    "  INT,"
            + COLNAME_TYPE + "  INT";
    // 选出最小的排序码
    public static final String SELECTION_MIN_SORT_CODE_ROW = "MIN("+COLNAME_SORT_CODE+")";

    public static final String WHERE_ROW = COLNAME_TOPIC_ID + " = ?";
    public static final String WHERE_ROW_NO_DELETE = COLNAME_TOPIC_ID + " = ? AND " + WHERE_NO_DELETE;

    public TopicFavoriteTable(Context context) {
        super(context, TABLE_NAME, OTHER_STRUCTURE);
    }

    public boolean isExist(String topicID){
        Cursor c = db.query(TABLE_NAME, null, WHERE_ROW,
                new String[] { topicID}, null, null, null);
        boolean ret = c.moveToFirst();
        c.close();
        return ret;
    }

    public void saveItem(ContentValues contentValues){
        String id = contentValues.getAsString(COLNAME_TOPIC_ID);
        int deleteFlag = contentValues.getAsInteger(COLNAME_DELFLAG);

        if (isExist(id)) {
            if (deleteFlag == DELETE_FLAG_DELETE){
                //删除数据
                db.delete(TABLE_NAME,WHERE_ROW,
                        new String[]{id});
            }else {
                //更新
                db.update(TABLE_NAME, contentValues, WHERE_ROW,
                        new String[]{id});
            }
        }else {
            //插入
            db.insert(TABLE_NAME,null,contentValues);
        }
    }

    //保存同步数据
    public void syncData(JSONArray syncData){
        try {
            if (syncData != null) {
                synchronized (m_dbHelper) {
                    db = m_dbHelper.getWritableDatabase();
                    db.beginTransaction();
                    for (int i = 0; i < syncData.length(); i++) {
                        JSONObject itemObject = syncData.getJSONObject(i);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(COLNAME_TOPIC_ID, itemObject.optInt("IID"));
                        contentValues.put(COLNAME_SEQ, itemObject.optInt("Seq"));
                        contentValues.put(COLNAME_UPDATE_TIME, itemObject.optInt("Utime"));
                        contentValues.put(COLNAME_CREATE_TIME, itemObject.optInt("Ctime"));
                        contentValues.put(COLNAME_DELFLAG, itemObject.optInt("DelFlag"));
                        contentValues.put(COLNAME_TYPE, itemObject.optInt("Type"));
                        contentValues.put(COLNAME_LOCAL_SYNC_FLAG, String.valueOf(BaseDB.SYNC_FLAG_YES));
                        saveItem(contentValues);
                    }
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.close();
                }

            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * 获取当前最小的排序码
     * @return
     */
    private int getMinSortCode(SQLiteDatabase db){
        int minSortCode = 0;
        Cursor cursor = db.query(TABLE_NAME, new String[]{SELECTION_MIN_SORT_CODE_ROW}, null, null, null, null, null);
        if (null != cursor){
            if (cursor.moveToFirst()){
                minSortCode = cursor.getInt(0);
            }
            cursor.close();
        }
        return minSortCode;
    }

    /**
     * 添加自选
     * @param id
     */
    public int addByLocal(String id){
        synchronized (m_dbHelper){
            db = m_dbHelper.getWritableDatabase();
            long currentTime = System.currentTimeMillis();
            // 添加排序码（最新加入的要排在最前）
            int sortCode = getMinSortCode(db) - 1;
            if (sortCode == 0){ // 0为默认值，不使用
                sortCode -= 1;
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLNAME_TOPIC_ID,id);
            contentValues.put(COLNAME_LOCAL_SYNC_FLAG,BaseDB.SYNC_FLAG_NO);
            contentValues.put(COLNAME_DELFLAG, DELETE_FLAG_NORMAL);
            contentValues.put(COLNAME_SORT_CODE,sortCode);
            contentValues.put(COLNAME_TYPE, 0);

            if (isExist(id)){
                contentValues.put(COLNAME_UPDATE_TIME, currentTime);
                //更新
                db.update(TABLE_NAME,contentValues, WHERE_ROW,
                        new String[]{id});
            }else {
                contentValues.put(COLNAME_CREATE_TIME, currentTime);
                //插入
                db.insert(TABLE_NAME, null, contentValues);
            }

            return sortCode;

        }
    }

    public boolean isFavorite(String id) {
        synchronized (m_dbHelper) {
            boolean ret = false;
            db = m_dbHelper.getReadableDatabase();
            // 有此股票且标记不为0
            Cursor cursor = db.query(TABLE_NAME, null, WHERE_ROW_NO_DELETE, new String[]{id}, null, null, null);
            if (cursor != null && cursor.moveToFirst())
                ret = true;

            cursor.close();
            db.close();
            return ret;

        }
    }

    /**
     * 删除自选
     * @param id
     */
    public void deleteByLocal(String id){
        synchronized (m_dbHelper){
            db = m_dbHelper.getWritableDatabase();
            long currentTime = System.currentTimeMillis();

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLNAME_TOPIC_ID,id);
            contentValues.put(COLNAME_LOCAL_SYNC_FLAG, BaseDB.SYNC_FLAG_NO);
            contentValues.put(COLNAME_DELFLAG, DELETE_FLAG_DELETE);

            if (isExist(id)) {
                contentValues.put(COLNAME_UPDATE_TIME, currentTime);
                //更新
                db.update(TABLE_NAME, contentValues, WHERE_ROW, new String[]{id});
            }

        }
    }

    /**
     * 获取待提交的新增的信息
     * @return
     */
    public Cursor getCommitAddCursor(){
        String where_row = WHERE_COMMIT_ADD_ROW
                + " AND " + COLNAME_DELFLAG + " = "+DELETE_FLAG_NORMAL ;
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME, new String[]{COLNAME_TOPIC_ID,COLNAME_SORT_CODE,COLNAME_TYPE}, where_row, null, null, null, null);
            return c;
        }
    }

    /**
     * 获取待提交删除的信息
     * @return
     */
    public Cursor getCommitDeleteCursor(){
        String where_row = WHERE_COMMIT_ADD_ROW
                + " AND " + COLNAME_DELFLAG + " = "+DELETE_FLAG_DELETE ;
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME, new String[]{COLNAME_TOPIC_ID,COLNAME_TYPE}, where_row, null, null, null, null);
            return c;
        }
    }


    /**
     * 获取所有的自选题材ID
     */
    public List<TopicFavoriteItem> getAllTopicIdStr(){
        synchronized (m_dbHelper) {
            List<TopicFavoriteItem> items = new LinkedList<TopicFavoriteItem>();
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME, new String[]{COLNAME_TOPIC_ID, COLNAME_SORT_CODE}, WHERE_NO_DELETE, null, null, null, COLNAME_SORT_CODE);
            if (null != c){
                while (c.moveToNext()){
                    TopicFavoriteItem item = new TopicFavoriteItem();
                    item.setTopicID(c.getString(0));
                    item.setSortCode(c.getInt(1));
                    items.add(item);
                }
                c.close();
            }
            return items;
        }
    }

    /**
     * 获取当前自选题材的集合
     * @return
     */
    public Set<String> getAllTopicIds(){
        Set<String> ret = new HashSet<String>();
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME, new String[]{COLNAME_TOPIC_ID}, WHERE_NO_DELETE, null, null, null, null);
            if (null != c){
                while (c.moveToNext()){
                    ret.add(c.getString(0));
                }
                c.close();
                return ret;
            }
        }

        return ret;
    }

}
