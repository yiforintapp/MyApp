package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.zlf.appmaster.model.news.MyFavoritesItem;
import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.utils.TimeUtil;
import com.zlf.appmaster.model.news.NewsFlashItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * Created by yushian on 15-4-8.
 */
public class NewsFavoriteTable extends SyncBaseTable{

    private static final String TAG = "NewsFavoriteTable";
    private static final int MAX_SAVE_NUM = 500;//显示最多存储500条
    /**
     * 收藏类型
     */
//    private static final int default_type = 1;


    public static final String TABLE_NAME = "news_favorite";
    public static final String COLNAME_IID = "iid";//(用户id、组合id、群组id)
    public static final String COLNAME_TYPE = "type";//类型（用户、组合、群组）

    //新闻内容
    public static final String COLNAME_TIME = "time";       //时间点
    public static final String COLNAME_TITLE = "title";     //标题
    public static final String COLNAME_STOCK = "stock";     //相关股票
    public static final String COLNAME_SUMMARY = "summary";     //摘要
    public static final String COLNAME_NEWS_TYPE = "news_type";       //类型

    /**
     * 数据库操作SQL
     */
    public static final String OTHER_STRUCTURE =
            ","
            + COLNAME_IID +     "	INT,"
            + COLNAME_TYPE +    "   INT,"

            + COLNAME_TIME + "   INT,"
            + COLNAME_NEWS_TYPE + "   INT,"
            + COLNAME_TITLE + "   TEXT,"
            + COLNAME_STOCK + "   TEXT,"
            + COLNAME_SUMMARY + "   TEXT"
            ;

    public static final String WHERE_ROW = COLNAME_IID + " = ? ";

    public NewsFavoriteTable(Context context) {
        super(context, TABLE_NAME, OTHER_STRUCTURE);
    }

    /**
     * 根据删除标记 获取待提交的信息
     * @param isDelete
     * @return Cursor
     */
    public Cursor getCommitCursor(boolean isDelete){
        String where_row = WHERE_COMMIT_ADD_ROW
//                + " AND " + COLNAME_TYPE + " = " + contactType
                + " AND " + COLNAME_DELFLAG ;
        if (isDelete){
            where_row += " = "+DELETE_FLAG_DELETE;
        }else {
            where_row += " = "+DELETE_FLAG_NORMAL;
        }

        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME,
                    new String[]{COLNAME_IID, COLNAME_TYPE},
                    where_row, null, null, null, null);
            return c;
        }
    }

    /**
     * 根据类型与删除标记 获取待提交的信息
     * @param isDelete
     * @return JSONArray
     */
    public JSONArray getCommitJsonArray(boolean isDelete){
//        int contactType = default_type;
//        Cursor cursor = getCommitCursor(contactType, isDelete);
        Cursor cursor = getCommitCursor(isDelete);
        JSONArray jsonNewArray = new JSONArray();
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("LlId", cursor.getLong(0));
                    jsonObject.put("Type", cursor.getInt(1));
                    jsonNewArray.put(jsonObject);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return jsonNewArray;
    }

    public boolean isFavorite(long iid,int type){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            String where_row = WHERE_ROW + " AND " + COLNAME_DELFLAG + " =" + DELETE_FLAG_NORMAL;

            Cursor c = db.query(TABLE_NAME, null, where_row,
                    new String[]{String.valueOf(iid)}, null, null, null);//, String.valueOf(type)
            boolean ret = c.moveToFirst();
            c.close();
            return ret;
        }
    }

    /**
     * 判断是否存在此id
     * @param iid
     * @return
     */
    public boolean isExist(long iid){
        Cursor c = db.query(TABLE_NAME, null, WHERE_ROW,
                new String[]{String.valueOf(iid)}, null, null, null);
        boolean ret = c.moveToFirst();
        c.close();
        return ret;
    }

    /**
     * 同步时保存项
     * @param contentValues
     */
    public void saveItem(ContentValues contentValues){
        long id = contentValues.getAsLong(COLNAME_IID);
        int deleteFlag = contentValues.getAsInteger(COLNAME_DELFLAG);
        int type = contentValues.getAsInteger(COLNAME_TYPE);


        if (isExist(id)){//,type
            if (deleteFlag == DELETE_FLAG_DELETE){
                //删除数据
                db.delete(TABLE_NAME,WHERE_ROW,
                        new String[]{String.valueOf(id)});//,String.valueOf(type)
            }else {
                //更新
                db.update(TABLE_NAME, contentValues, WHERE_ROW,
                        new String[]{String.valueOf(id)});//,String.valueOf(type)
            }
        }else {
            //插入
            db.insert(TABLE_NAME,null,contentValues);
        }
    }

    /**
     * 新闻收藏
     */
    public void saveNewsFavorite(long id,int type,boolean isFavorite){
        QLog.i(TAG,"id:"+id+",type:"+type+",isFavorite:"+isFavorite);

        synchronized (m_dbHelper){
            db = m_dbHelper.getWritableDatabase();
            long currentTime = System.currentTimeMillis();


            ContentValues contentValues = new ContentValues();
            contentValues.put(COLNAME_IID,id);
            contentValues.put(COLNAME_TYPE,type);
            contentValues.put(COLNAME_LOCAL_SYNC_FLAG,BaseDB.SYNC_FLAG_NO);

            if (isFavorite){
                contentValues.put(COLNAME_DELFLAG, DELETE_FLAG_NORMAL);
            }else {
                contentValues.put(COLNAME_DELFLAG, DELETE_FLAG_DELETE);
            }

            if (isExist(id)){//,type
                contentValues.put(COLNAME_UPDATE_TIME, currentTime);
                //更新
                db.update(TABLE_NAME,contentValues, WHERE_ROW,
                        new String[]{String.valueOf(id)});//,String.valueOf(type)
            }else {
                contentValues.put(COLNAME_CREATE_TIME, currentTime);
                //插入
                db.insert(TABLE_NAME, null, contentValues);

            }

        }
    }


    /**
     * 获取新闻收藏的信息
     * @return Cursor
     */
    public Cursor getNewsCursor(int startIndex, int num){
        String limitString = startIndex + "," + num;//前面是偏移，后面是数量
        String where_row =
//                COLNAME_TYPE + " = " + default_type
//                        + " AND " +
                        COLNAME_DELFLAG + " = " + DELETE_FLAG_NORMAL;

        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(false,TABLE_NAME,
                    null,where_row,
                    null, null, null,
                    ORDER_BY_DESC_TIME, limitString);
            return c;
        }
    }


    /**
     * 获取收藏的新闻map
     * @return
     */
    public TreeMap<Long,MyFavoritesItem> getFavoritesMap(int startIndex, int num){
        Cursor cursor = getNewsCursor(startIndex,num);
        TreeMap<Long,MyFavoritesItem> newsMap = new TreeMap<Long, MyFavoritesItem>(new Comparator() {//逆序
            @Override
            public int compare(Object lhs, Object rhs) {
                long l = (Long) lhs;
                long r = (Long)rhs;
                return (int)(r-l);
            }

        });
        while (cursor.moveToNext()){
            NewsFlashItem item = new NewsFlashItem();
            item.setId(cursor.getLong(cursor.getColumnIndex(COLNAME_IID)));
            if (item.getId() <0){//过滤id错误的
                continue;
            }

            item.setTime(cursor.getLong(cursor.getColumnIndex(COLNAME_TIME)));
            item.setClassify(cursor.getInt(cursor.getColumnIndex(COLNAME_TYPE)));
//            item.setIsFavorite(true);//是否自选股新闻

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

            //收藏时间
            long favoriteTime = TimeUtil.formatDayTime(cursor.getLong(cursor.getColumnIndex(COLNAME_CREATE_TIME)));
            MyFavoritesItem myFavoritesItem = newsMap.get(favoriteTime);
            if(myFavoritesItem == null){
                myFavoritesItem = new MyFavoritesItem();
                myFavoritesItem.setTime(favoriteTime);
                newsMap.put(favoriteTime,myFavoritesItem);
            }
            myFavoritesItem.addNewsItem(item);
        }

        return newsMap;
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
                        contentValues.put(COLNAME_IID, itemObject.optLong("LlId"));
                        contentValues.put(COLNAME_SEQ, itemObject.optLong("Seq"));
                        contentValues.put(COLNAME_UPDATE_TIME, itemObject.optLong("Utime"));
                        contentValues.put(COLNAME_CREATE_TIME, itemObject.optLong("Ctime"));
                        contentValues.put(COLNAME_DELFLAG, itemObject.optInt("DelFlag"));
                        contentValues.put(COLNAME_TYPE, itemObject.optInt("Type"));
                        contentValues.put(COLNAME_TITLE, itemObject.optString("Title"));
                        contentValues.put(COLNAME_TIME, itemObject.optLong("Nutime"));  // 新闻的更新时间
                        String aboutStock = "";
                        try {
                            aboutStock = itemObject.getJSONArray("AboutStock").toString();
                        }
                        catch (JSONException e){

                        }
                        contentValues.put(COLNAME_STOCK, aboutStock);    // 相关股票
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
     * 保存新闻信息
     */
    public void saveNewsFlashItemArray(ArrayList<NewsFlashItem> items) {
        synchronized (m_dbHelper) {
            if (items.size() == 0) {
                return;
            }

            db = m_dbHelper.getWritableDatabase();
            db.beginTransaction();
            for (NewsFlashItem item : items) {
                ContentValues contentValues = new ContentValues();

                contentValues.put(COLNAME_IID, item.getId());
                contentValues.put(COLNAME_TIME, item.getTime());
                contentValues.put(COLNAME_TYPE, item.getClassify());//default_type);//
                //contentValues.put(COLNAME_NEWS_TYPE, item.getNewsType());

                if (!TextUtils.isEmpty(item.getTitle())) {
                    contentValues.put(COLNAME_TITLE, item.getTitle());
                    contentValues.put(COLNAME_STOCK, item.getStockJsonArray());
                    contentValues.put(COLNAME_SUMMARY, item.getSummary());
                }

                if (isExist(item.getId())){
                    //更新
                    db.update(TABLE_NAME, contentValues, WHERE_ROW,
                            new String[]{String.valueOf(item.getId())});
                }else {
                    //插入
                    db.insert(TABLE_NAME, null, contentValues);
                }

            }


            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }


}
