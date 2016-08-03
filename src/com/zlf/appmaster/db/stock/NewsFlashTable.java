package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.zlf.appmaster.model.news.NewsFlashItem;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yushian on 2014/11/20.
 * 快讯数据库
 */
public class NewsFlashTable {
//    private static final String TAG = "NewsFlashTable";
    private static final int MAX_SAVE_NUM = 2000;//显示最多存储2K条


    public static final String TABLE_NAME_NEWS_FLASH = "news_flash";

    public static final String COLNAME_ID = "_id";              //自增长ID
    public static final String COLNAME_NEWSID = "news_id";      //新闻ID
    public static final String COLNAME_TIME = "time";           //时间点
    public static final String COLNAME_CLASSIFY = "classify";   // 分类
    public static final String COLNAME_NEWS_KEY = "news_key";   // 新闻类型
    public static final String COLNAME_TITLE = "title";         //标题
    public static final String COLNAME_STOCK = "stock";         //相关股票
    public static final String COLNAME_SUMMARY = "summary";     //摘要
    public static final String COLNAME_MEDIA    = "media";      // 相关媒体

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_NEWS_FLASH
                    + "("
                    + COLNAME_ID + "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
                    + COLNAME_NEWSID + "   INT,"
                    + COLNAME_TIME + "   INT,"
                    + COLNAME_CLASSIFY + "   INT,"
                    + COLNAME_NEWS_KEY + "   TEXT,"
                    + COLNAME_TITLE + "   TEXT,"
                    + COLNAME_STOCK + "   TEXT,"
                    + COLNAME_SUMMARY + "   TEXT,"
                    + COLNAME_MEDIA + "   TEXT"
                    + ")";

    public static final String WHERE_ROW = COLNAME_NEWSID + " = ?";
    public static final String ORDER_BY_DESC_TIME = COLNAME_TIME + " DESC";//按时间降序排列

    private Context mContext;
    private NewsDBHelper m_dbHelper;
    private SQLiteDatabase m_db;

    public NewsFlashTable(Context context) {
//        QLog.i(TAG,"NewsFlashTable");
        mContext = context;
        m_dbHelper = NewsDBHelper.getInstance(context);


        createTable();
    }

    private boolean isExist(long id) {
        m_db = m_dbHelper.getReadableDatabase();
        Cursor c = m_db.query(TABLE_NAME_NEWS_FLASH, null, WHERE_ROW,
                new String[]{String.valueOf(id)}, null, null, null);
        boolean ret = c.moveToFirst();
        c.close();
        return ret;
    }


    private void createTable() {
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();
            m_db.execSQL(CREATE_TABLE);
            m_db.close();
        }
    }

    public void close() {

    }


    /**
     * 写入
     */
    public void saveNewsFlashItemArray(List<NewsFlashItem> items) {
        synchronized (m_dbHelper) {
            if (items.size() == 0) {
                return;
            }

            m_db = m_dbHelper.getWritableDatabase();
            m_db.beginTransaction();
            for (NewsFlashItem item : items) {
                ContentValues contentValues = new ContentValues();

                if (!item.isChanged())  // 数据提示本条数据无改变
                    continue;

                contentValues.put(COLNAME_TIME, item.getTime());
                contentValues.put(COLNAME_CLASSIFY, item.getClassify());
                contentValues.put(COLNAME_NEWS_KEY, item.getNewsKey());

                contentValues.put(COLNAME_TITLE, item.getTitle());
                contentValues.put(COLNAME_STOCK, item.getStockJsonArray());
                contentValues.put(COLNAME_SUMMARY, item.getSummary());
                contentValues.put(COLNAME_MEDIA, item.getMedia());

                if (isExist(item.getId())) {    // 更新
                    m_db.update(TABLE_NAME_NEWS_FLASH, contentValues, WHERE_ROW, new String[]{String.valueOf(item.getId())});
                } else {    // 插入
                    contentValues.put(COLNAME_NEWSID, item.getId());
                    m_db.insert(TABLE_NAME_NEWS_FLASH, null, contentValues);
                }

            }


            m_db.setTransactionSuccessful();
            m_db.endTransaction();
            m_db.close();
        }
    }


    /**
     * 获取新闻类型的获取消息
     */
    public ArrayList<NewsFlashItem> getNewsByType(String newsKey, int startIndex, int num){
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getReadableDatabase();


                String limitString = startIndex + "," + num;//前面是偏移，后面是数量
                Cursor cursor = m_db.query(TABLE_NAME_NEWS_FLASH,
                        null,
                        COLNAME_NEWS_KEY + "=?",
                        new String[]{newsKey},
                        null, null,
                        ORDER_BY_DESC_TIME, limitString);

                ArrayList<NewsFlashItem> items = new ArrayList<NewsFlashItem>();
                while (cursor.moveToNext()) {
                    NewsFlashItem item = new NewsFlashItem();
                    item.setId(cursor.getLong(cursor.getColumnIndex(COLNAME_NEWSID)));
                    item.setTime(cursor.getLong(cursor.getColumnIndex(COLNAME_TIME)));
                    item.setNewsKey(cursor.getString(cursor.getColumnIndex(COLNAME_NEWS_KEY)));

                    item.setTitle(cursor.getString(cursor.getColumnIndex(COLNAME_TITLE)));
                    item.setSummary(cursor.getString(cursor.getColumnIndex(COLNAME_SUMMARY)));
                    item.setClassify(cursor.getInt(cursor.getColumnIndex(COLNAME_CLASSIFY)));
                    try {
                        String jsonString = cursor.getString(cursor.getColumnIndex(COLNAME_STOCK));
                        if (!TextUtils.isEmpty(jsonString)) {
                            item.setStockList(new JSONArray(jsonString));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    items.add(item);
                }
                cursor.close();

                return items;
            }
    }


    /**000062
     * 获取自选股新闻
     *
     * @return
     */
    public ArrayList<NewsFlashItem> getSelfStockNews(int start, int num) {

        UserStockNewsTable userStockNewsTable = new UserStockNewsTable(mContext);
        ArrayList<NewsFlashItem> items = userStockNewsTable.getNewsFlashItemArray(start,num);

        //设置数据
        for (int i=0;i<items.size();i++){
            NewsFlashItem item = items.get(i);
            Cursor cursor = m_db.query(TABLE_NAME_NEWS_FLASH, null, WHERE_ROW,
                    new String[]{String.valueOf(item.getId())}, null, null, null);
            boolean ret = cursor.moveToFirst();
            if (ret){
                item.setTitle(cursor.getString(cursor.getColumnIndex(COLNAME_TITLE)));
                item.setSummary(cursor.getString(cursor.getColumnIndex(COLNAME_SUMMARY)));
                try {
                    String jsonString = cursor.getString(cursor.getColumnIndex(COLNAME_STOCK));
                    if (!TextUtils.isEmpty(jsonString)) {
                        item.setStockList(new JSONArray(jsonString));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                //并置为自选股新闻
                item.setIsFavorite(true);

            }else {
                items.remove(i);
                i--;
            }
        }
        return items;
    }




}

