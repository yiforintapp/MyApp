package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zlf.appmaster.model.news.NewsFlashItem;

import java.util.ArrayList;

/**
 * 用户自选股新闻表
 * Created by yushian on 15-4-27.
 */
public class UserStockNewsTable {

    private static final int MAX_SAVE_NUM = 2000;//显示最多存储500条
    public static final String TABLE_NAME = "user_stock_news";
    public static final String COLNAME_ID = "_id";          //自增长ID
    public static final String COLNAME_NEWSID = "news_id";  //新闻ID
    public static final String COLNAME_TIME = "time";       //时间点
    public static final String COLNAME_TYPE = "type";       //类型
    public static final String COLNAME_NEWS_TYPE = "news_type";       //类型
    
    
    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                    + "("
                    + COLNAME_ID + "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
                    + COLNAME_NEWSID + "   INT,"
                    + COLNAME_TIME + "   INT,"
                    + COLNAME_TYPE + "   INT,"
                    + COLNAME_NEWS_TYPE + "   INT"
                    + ")";
    public static final String WHERE_ROW = COLNAME_NEWSID + " = ?";
    public static final String ORDER_BY_DESC_TIME = COLNAME_TIME + " DESC";//按时间降序排列
    
    private Context mContext;
    private QiNiuDBHelper m_dbHelper;
    private SQLiteDatabase m_db;


    public UserStockNewsTable(Context context) {
        mContext = context;
        m_dbHelper = QiNiuDBHelper.getInstance(context);


        createTable();
    }

    private void createTable() {
        synchronized (m_dbHelper) {
            m_db = m_dbHelper.getWritableDatabase();
            m_db.execSQL(CREATE_TABLE);
            m_db.close();
        }
    }

    private boolean isExist(long id) {
        m_db = m_dbHelper.getReadableDatabase();
        Cursor c = m_db.query(TABLE_NAME, null, WHERE_ROW,
                new String[]{String.valueOf(id)}, null, null, null);
        boolean ret = c.moveToFirst();
        c.close();
        return ret;
    }
    
    /**
     * 写入
     */
    public void saveNewsFlashItemArray(ArrayList<NewsFlashItem> items) {
        synchronized (m_dbHelper) {
            if (items.size() == 0) {
                return;
            }
            /*Set<Integer> typeSet = new HashSet<>();*/

            m_db = m_dbHelper.getWritableDatabase();
            m_db.beginTransaction();
            for (NewsFlashItem item : items) {
                ContentValues contentValues = new ContentValues();

                contentValues.put(COLNAME_TIME, item.getTime());
                contentValues.put(COLNAME_TYPE, item.getClassify());
                /*contentValues.put(COLNAME_NEWS_TYPE, item.getNewsType());*/


                if (isExist(item.getId())) {    // 更新
                    m_db.update(TABLE_NAME, contentValues, WHERE_ROW, new String[]{String.valueOf(item.getId())});
                } else {    // 插入
                    contentValues.put(COLNAME_NEWSID, item.getId());
                    m_db.insert(TABLE_NAME, null, contentValues);
                }

                /*typeSet.add(item.getNewsType());*/
            }

/*            //保存未读信息标记
            Setting setting = new Setting(mContext);
            for (int type:typeSet){
                setting.setNewsReadFlag(type,false);
            }*/


            //删除掉多余的
            Cursor c = m_db.query(TABLE_NAME,
                    null, null, null, null, null,
                    ORDER_BY_DESC_TIME);
            if (c.getCount() >= MAX_SAVE_NUM) {
                int deleteNum = c.getCount() - MAX_SAVE_NUM;
                String limitString = "" + deleteNum;//数量
                Cursor dCursor = m_db.query(false, TABLE_NAME,
                        null, null, null, null, null,
                        COLNAME_TIME, limitString);//按时间升序排列
                while (dCursor.moveToNext()) {
                    m_db.delete(TABLE_NAME, WHERE_ROW,
                            new String[]{String.valueOf(dCursor.getLong(dCursor.getColumnIndex(COLNAME_NEWSID)))});
                }
                dCursor.close();
            }


            m_db.setTransactionSuccessful();
            m_db.endTransaction();
            m_db.close();
        }
    }


    /**
     * 读取最新的N条
     *
     * @param startIndex
     * @param num
     * @return
     */
    public ArrayList<NewsFlashItem> getNewsFlashItemArray(int startIndex, int num) {
        synchronized (m_dbHelper) {
            String limitString = startIndex + "," + num;//前面是偏移，后面是数量

            ArrayList<NewsFlashItem> items = new ArrayList<NewsFlashItem>();
            m_db = m_dbHelper.getReadableDatabase();

            Cursor cursor = m_db.query(false, TABLE_NAME,
                    null, null, null, null, null,
                    ORDER_BY_DESC_TIME, limitString);
            while (cursor.moveToNext()) {
                NewsFlashItem item = new NewsFlashItem();
                item.setId(cursor.getLong(cursor.getColumnIndex(COLNAME_NEWSID)));
                item.setTime(cursor.getLong(cursor.getColumnIndex(COLNAME_TIME)));
                item.setClassify(cursor.getInt(cursor.getColumnIndex(COLNAME_TYPE)));

                items.add(item);
            }
            cursor.close();
            return items;

        }
    }
}
