package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zlf.appmaster.model.news.NewsRecentItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻要订阅，所以改用QiNiuDBHelper db  2015/8/19
 * Created by Huang on 2015/6/2.
 */
public class NewsRecentTable extends SyncBaseTable {

    public static final String TABLE_NAME_NEWS_RECENT = "news_recent";


    public static final String COLNAME_KEY      = "Key";              //
    public static final String COLNAME_HAS_NEWS = "HasNews";          //
    public static final String COLNAME_TITLE    = "Title";            //

    private static final String OTHER_STRUCTURE =
                    ","
                    + COLNAME_KEY       + "   TEXT,"
                    + COLNAME_HAS_NEWS  + "   INT,"
                    + COLNAME_TITLE     + "   TEXT";

    public static final String WHERE_ROW = COLNAME_KEY + " = ?";

    public NewsRecentTable(Context context) {
//        QLog.i(TAG,"NewsFlashTable");
        super(context, TABLE_NAME_NEWS_RECENT, OTHER_STRUCTURE);

    }

    private boolean isExist(String key) {
        db = m_dbHelper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME_NEWS_RECENT, null, WHERE_ROW,
                new String[]{key}, null, null, null);
        boolean ret = c.moveToFirst();
        c.close();
        return ret;
    }



    public void close() {

    }


    /**
     * 查询本表是否存在
     * @return
     */
    public boolean isDataExist() {
        boolean ret = false;
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME_NEWS_RECENT, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst())    // 有结果集则认为数据表正常
                ret = true;
            cursor.close();
            db.close();
        }
        return ret;
    }


    /**
     * 加载预置数据
     */
    public static void loadPreInitData(Context context) {
        NewsRecentTable table = new NewsRecentTable(context);

        if (!table.isDataExist()) {    // 不存在数据时才需加载
            List<NewsRecentItem> items = new ArrayList<NewsRecentItem>();

            // 内置两个订阅的
            {
                NewsRecentItem recentItem = new NewsRecentItem();
                recentItem.setKey(NewsRecentItem.KEY_MACRO_ECONOMY_NEWS);
                items.add(recentItem);
            }
            {
                NewsRecentItem recentItem = new NewsRecentItem();
                recentItem.setKey(NewsRecentItem.KEY_MARKET_NEWS_LIVE);
                items.add(recentItem);
            }

            table.saveInitItems(items);
        }

    }

    /**
     * 保存预置数据
     * @param items
     */
    private void saveInitItems(List<NewsRecentItem> items){
        synchronized (m_dbHelper) {
            if (items.size() == 0) {
                return;
            }

            db = m_dbHelper.getWritableDatabase();
            db.beginTransaction();
            for (NewsRecentItem item : items) {
                ContentValues contentValues = new ContentValues();

                contentValues.put(COLNAME_CREATE_TIME, 0);
                contentValues.put(COLNAME_HAS_NEWS, 0);
                contentValues.put(COLNAME_TITLE, "");
                contentValues.put(COLNAME_LOCAL_SYNC_FLAG,BaseDB.SYNC_FLAG_NO);
                contentValues.put(COLNAME_DELFLAG, DELETE_FLAG_NORMAL);

                String key = item.getKey();
                if (isExist(key)) {    // 更新
                    db.update(TABLE_NAME_NEWS_RECENT, contentValues, WHERE_ROW, new String[]{key});
                } else {    // 插入
                    contentValues.put(COLNAME_KEY, key);
                    db.insert(TABLE_NAME_NEWS_RECENT, null, contentValues);
                }

            }

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }

    /**
     * 保存订阅信息
     */
    public void saveSubscribeItems(List<NewsRecentItem> items) {
        synchronized (m_dbHelper) {
            if (items.size() == 0) {
                return;
            }

            db = m_dbHelper.getWritableDatabase();
            db.beginTransaction();
            for (NewsRecentItem item : items) {
                ContentValues contentValues = new ContentValues();
                boolean hasNews = item.isHasNews();

                if (hasNews){
                    contentValues.put(COLNAME_CREATE_TIME, item.getCreateTime());
                    contentValues.put(COLNAME_TITLE, item.getTitle());
                }

                contentValues.put(COLNAME_HAS_NEWS, hasNews);
                contentValues.put(COLNAME_LOCAL_SYNC_FLAG,BaseDB.SYNC_FLAG_NO);
                contentValues.put(COLNAME_DELFLAG, DELETE_FLAG_NORMAL);


                String key = item.getKey();
                if (isExist(key)) {    // 更新
                    db.update(TABLE_NAME_NEWS_RECENT, contentValues, WHERE_ROW, new String[]{key});
                } else {    // 插入
                    contentValues.put(COLNAME_KEY, key);
                    db.insert(TABLE_NAME_NEWS_RECENT, null, contentValues);
                }

            }


            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }

    }
    /**
     * 批量保存数据项
     * @param items
     */
    public void saveItems(List<NewsRecentItem> items) {
        synchronized (m_dbHelper) {
            if (items.size() == 0) {
                return;
            }

            db = m_dbHelper.getWritableDatabase();
            db.beginTransaction();
            for (NewsRecentItem item : items) {
                ContentValues contentValues = new ContentValues();
                boolean hasNews = item.isHasNews();
                if (!hasNews){  // 此分类无新消息
                    continue;
                }

                contentValues.put(COLNAME_CREATE_TIME, item.getCreateTime());
                contentValues.put(COLNAME_HAS_NEWS, hasNews);
                contentValues.put(COLNAME_TITLE, item.getTitle());


                String key = item.getKey();
                if (isExist(key)) {    // 更新
                    db.update(TABLE_NAME_NEWS_RECENT, contentValues, WHERE_ROW, new String[]{key});
                } else {    // 插入
                    contentValues.put(COLNAME_KEY, key);
                    db.insert(TABLE_NAME_NEWS_RECENT, null, contentValues);
                }

            }


            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }


    public List<NewsRecentItem> getItems(){
        synchronized (m_dbHelper) {
            List<NewsRecentItem> items = new ArrayList<NewsRecentItem>();
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME_NEWS_RECENT, null, null, null, null, null, null);
            if (null != c){
                while (c.moveToNext()){
                    NewsRecentItem item = new NewsRecentItem();
                    item.setKey(c.getString(c.getColumnIndex(COLNAME_KEY)));
                    item.setHasNews(c.getInt(c.getColumnIndex(COLNAME_HAS_NEWS)));
                    item.setTitle(c.getString(c.getColumnIndex(COLNAME_TITLE)));
                    item.setCreateTime(c.getLong(c.getColumnIndex(COLNAME_CREATE_TIME)));
                    item.setIsSubscribe(c.getInt(c.getColumnIndex(COLNAME_DELFLAG)));

                    /*if (!item.isHasNews() && TextUtils.isEmpty(item.getTitle())){
                        // 无最新消息，标题已为空，则说明无此分类的消息
                        continue;
                    }*/

                    items.add(item);

                }

                c.close();
            }
            db.close();
            return items;
        }
    }

    public void clearNewsFlagByKey(String key){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLNAME_HAS_NEWS, false);
            db.update(TABLE_NAME_NEWS_RECENT, contentValues, WHERE_ROW, new String[]{key});
            db.close();
        }
    }



    /**
     * 添加订阅
     * @param id
     */
    public void addSubscribeByLocal(String id){
        synchronized (m_dbHelper){
            db = m_dbHelper.getWritableDatabase();

            if (isExist(id)){       // 已存在的新闻类型才修改标记
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLNAME_LOCAL_SYNC_FLAG,BaseDB.SYNC_FLAG_NO);
                contentValues.put(COLNAME_DELFLAG, DELETE_FLAG_NORMAL);
                //更新
                db.update(TABLE_NAME_NEWS_RECENT, contentValues, WHERE_ROW,
                        new String[]{id});
            }
        }
    }

    /**
     * 删除订阅
     * @param id
     */
    public void deleteSubscribeByLocal(String id){
        synchronized (m_dbHelper){
            db = m_dbHelper.getWritableDatabase();

            if (isExist(id)){       // 已存在的新闻类型才修改标记
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLNAME_LOCAL_SYNC_FLAG, BaseDB.SYNC_FLAG_NO);
                contentValues.put(COLNAME_DELFLAG, DELETE_FLAG_DELETE);
                //更新
                db.update(TABLE_NAME_NEWS_RECENT, contentValues, WHERE_ROW,
                        new String[]{id});
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
            Cursor c = db.query(TABLE_NAME_NEWS_RECENT, new String[]{COLNAME_KEY}, where_row, null, null, null, null);
            return c;
        }
    }

    /**
     * 获取待提交删除的信息
     * @return
     */
    public Cursor getCommitDeleteCursor(){
        String where_row = WHERE_COMMIT_ADD_ROW
                + " AND " + COLNAME_DELFLAG + " = "+DELETE_FLAG_DELETE;
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME_NEWS_RECENT, new String[]{COLNAME_KEY}, where_row, null, null, null, null);
            return c;
        }
    }

}
