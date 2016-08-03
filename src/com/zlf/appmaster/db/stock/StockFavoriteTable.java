package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zlf.appmaster.model.stock.StockFavoriteItem;
import com.zlf.appmaster.model.stock.StockItem;
import com.zlf.appmaster.model.stock.RecentItem;
import com.zlf.appmaster.model.industry.IndustryItem;
import com.zlf.appmaster.model.news.NewsRecentItem;
import com.zlf.appmaster.model.topic.TopicItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * 股票收藏---DB
 * @author Deping
 *
 */
public class StockFavoriteTable {


    public static final String COLNAME_ID = "_id";
	public static final String COLNAME_CODE = "code";				    // 股票代码
    public static final String COLNAME_CODE_TYPE = "code_type";                   // 自选的类型（参见stockItem 股票0、指数1）
    public static final String COLNAME_CREATE_TIME = "create_time";     // 创建时间
	public static final String COLNAME_UPDATE_TIME = "update_time";	    // 加入时间
    public static final String COLNAME_LOCAL_SYNC_TYPE = "local_sync_type";      //  0 保留值 1、新增    2、删除
    public static final String COLNAME_LOCAL_SYNC_FLAG = "local_sync_flag";      //  是否已同步 0 保留值 1已同步 2 未同步
    public static final String COLNAME_INDUSTRY_ID = "industry_id";
    public static final String COLNAME_INDUSTRY_NAME = "industry_name";
    public static final String COLNAME_SORT_CODE = "sort_code";                 // 排序码

    public static final int COLNAME_CODE_INDEX = 1;
    public static final int COLNAME_CODE_TYPE_INDEX =2;
    public static final int COLNAME_ADD_TIME_INDEX = 3;
	
	public static final String TABLE_NAME_FAVORITE_STOCKINFO = "stock_favorite_info";
    public static final String SELECTION_MIN_SORT_CODE_ROW = "MIN("+COLNAME_SORT_CODE+")";

	public static final String WHERE_ROW = COLNAME_CODE + " = ? AND " + COLNAME_CODE_TYPE + " = ?";
    public static final String WHERE_ROW_NO_DELETE = COLNAME_CODE + " = ? AND " + COLNAME_LOCAL_SYNC_TYPE + " != " + BaseDB.SYNC_LOCAL_TYPE_DELETE;
    public static final String WHERE_NO_DELETE_ROW = COLNAME_LOCAL_SYNC_TYPE + " != " + BaseDB.SYNC_LOCAL_TYPE_DELETE;
    public static final String WHERE_NO_DELETE_STOCK_ROW = COLNAME_CODE_TYPE + " = 0 AND " + COLNAME_LOCAL_SYNC_TYPE + " != " + BaseDB.SYNC_LOCAL_TYPE_DELETE;
    //public static final String WHERE_COMMIT_DELETE_ROW = COLNAME_LOCAL_SYNC_TYPE + " = " + BaseDB.SYNC_LOCAL_TYPE_DELETE +" AND " + COLNAME_LOCAL_SYNC_FLAG + " = " + BaseDB.SYNC_FLAG_NO;
    //public static final String WHERE_COMMIT_ADD_ROW = COLNAME_LOCAL_SYNC_TYPE + " = " + BaseDB.SYNC_LOCAL_TYPE_ADD +" AND " + COLNAME_LOCAL_SYNC_FLAG + " = " + BaseDB.SYNC_FLAG_NO;
    public static final String WHERE_COMMIT_OPERATOR_ROW =  COLNAME_LOCAL_SYNC_FLAG + " = " + BaseDB.SYNC_FLAG_NO;

    private static HashSet<String> mMapKeyID = null;

	/**
	 * 数据库操作SQL
	 */
    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_FAVORITE_STOCKINFO
                    + "("
                    + COLNAME_ID + "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
                    + COLNAME_CODE + "	TEXT    NOT NULL ,"
                    + COLNAME_CODE_TYPE + "  INT,"
                    + COLNAME_CREATE_TIME + "	INT,"
                    + COLNAME_UPDATE_TIME + "	INT,"
                    + COLNAME_LOCAL_SYNC_TYPE + "	INT,"
                    + COLNAME_LOCAL_SYNC_FLAG + "	INT,"
                    + COLNAME_INDUSTRY_ID + "	TEXT,"
                    + COLNAME_INDUSTRY_NAME + "	TEXT,"
                    + COLNAME_SORT_CODE         +    "  INT"
                    + ")";
    public static final String CREATE_TABLE_S =
            "CREATE TABLE IF NOT EXISTS " + "%s."+TABLE_NAME_FAVORITE_STOCKINFO
                    + "("
                    + COLNAME_ID + "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
                    + COLNAME_CODE + "	TEXT    NOT NULL ,"
                    + COLNAME_CODE_TYPE + "  INT,"
                    + COLNAME_CREATE_TIME + "	INT,"
                    + COLNAME_UPDATE_TIME + "	INT,"
                    + COLNAME_LOCAL_SYNC_TYPE + "	INT,"
                    + COLNAME_LOCAL_SYNC_FLAG + "	INT,"
                    + COLNAME_INDUSTRY_ID + "	TEXT,"
                    + COLNAME_INDUSTRY_NAME + "	TEXT,"
                    + COLNAME_SORT_CODE         +    "  INT"
                    + ")";


	private SQLiteDatabase db;
    private QiNiuDBHelper m_dbHelper;
    private Context mContext;

	public StockFavoriteTable(Context context) {
        mContext = context;
        m_dbHelper = QiNiuDBHelper.getInstance(context);
        createTable();
	}

    private void createTable(){
        synchronized (m_dbHelper){
            db = m_dbHelper.getWritableDatabase();
            db.execSQL(CREATE_TABLE);
            db.close();
        }
    }

	public void close() {
//		if (db != null)
//			db.close();
	}
	
	/**
	 * 清除所有数据
	 */
	public void clearAll(){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getWritableDatabase();
            db.execSQL("delete from " + TABLE_NAME_FAVORITE_STOCKINFO);
            db.close();
        }
	}

//	/**
//	 * 增加单条数据
//	 * @param stockCode
//	 */
  /* private void saveItem(String stockCode, int type, boolean local) {
        ContentValues contentValues = new ContentValues();
        long currentTime = System.currentTimeMillis();
        contentValues.put(COLNAME_UPDATE_TIME, currentTime);

        if (local) {
            contentValues.put(COLNAME_LOCAL_SYNC_TYPE, BaseDB.SYNC_LOCAL_TYPE_ADD);
            contentValues.put(COLNAME_LOCAL_SYNC_FLAG, BaseDB.SYNC_FLAG_NO);
        } else {
            contentValues.put(COLNAME_LOCAL_SYNC_TYPE, BaseDB.SYNC_LOCAL_TYPE_DEFAULT);
            contentValues.put(COLNAME_LOCAL_SYNC_FLAG, BaseDB.SYNC_FLAG_YES);
        }


        if (isExist(stockCode, type)) {        // 更新
            db.update(TABLE_NAME_FAVORITE_STOCKINFO, contentValues, WHERE_ROW, new String[]{stockCode, String.valueOf(type)});
        } else {    // 插入
            contentValues.put(COLNAME_CREATE_TIME, currentTime);
            contentValues.put(COLNAME_CODE, stockCode);
            db.insert(TABLE_NAME_FAVORITE_STOCKINFO, null, contentValues);
        }
    }*/
    private void saveItem(StockFavoriteItem item, boolean local){

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLNAME_UPDATE_TIME, item.getUpdateTime());
        contentValues.put(COLNAME_CODE_TYPE, item.getType());
        contentValues.put(COLNAME_INDUSTRY_ID, item.getIndustryID());
        contentValues.put(COLNAME_INDUSTRY_NAME, item.getIndustryName());
        contentValues.put(COLNAME_SORT_CODE, item.getSortCode());

        if (local) {
            contentValues.put(COLNAME_LOCAL_SYNC_TYPE, BaseDB.SYNC_LOCAL_TYPE_ADD);
            contentValues.put(COLNAME_LOCAL_SYNC_FLAG, BaseDB.SYNC_FLAG_NO);
        } else {
            contentValues.put(COLNAME_LOCAL_SYNC_TYPE, BaseDB.SYNC_LOCAL_TYPE_ADD);
            contentValues.put(COLNAME_LOCAL_SYNC_FLAG, BaseDB.SYNC_FLAG_YES);
        }

        if (isExist(item.getStockCode(), item.getType())) {        // 更新
            db.update(TABLE_NAME_FAVORITE_STOCKINFO, contentValues, WHERE_ROW, new String[]{item.getStockCode(), String.valueOf(item.getType())});
        } else {    // 插入
            contentValues.put(COLNAME_CREATE_TIME, item.getAddTime());
            contentValues.put(COLNAME_CODE, item.getStockCode());
            db.insert(TABLE_NAME_FAVORITE_STOCKINFO, null, contentValues);
        }
    }


    private boolean isExist(String stockCode, int type){
		
		Cursor c = db.query(TABLE_NAME_FAVORITE_STOCKINFO, null, WHERE_ROW, new String[] { stockCode, String.valueOf(type) }, null, null, null);
		boolean ret = c.moveToFirst();
		c.close();
		
		return ret;
	}

    /**
     * 获取所有自选选项（包含指数和股票）
     * @return
     */
	public List<StockFavoriteItem> getFavoriteItems(){

        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();

            List<StockFavoriteItem> items = new LinkedList<StockFavoriteItem>();

            Cursor c = db.query(TABLE_NAME_FAVORITE_STOCKINFO, null, WHERE_NO_DELETE_ROW,
                    null, null, null, COLNAME_SORT_CODE);
            if (null != c) {
                while (c.moveToNext()) {
                    StockFavoriteItem item = new StockFavoriteItem(c.getString(StockFavoriteTable.COLNAME_CODE_INDEX),c.getInt(StockFavoriteTable.COLNAME_CODE_TYPE_INDEX));
                    item.setAddTime(c.getLong(StockFavoriteTable.COLNAME_ADD_TIME_INDEX));
                    items.add(item);
                }
                c.close();
            }
            db.close();

            return items;
        }
    }

    /**
     * 获取自选股的组
     * @return
     */
    public List<StockFavoriteItem> getStockFavoriteItems(){

        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();

            List<StockFavoriteItem> items = new LinkedList<StockFavoriteItem>();

            Cursor c = db.query(TABLE_NAME_FAVORITE_STOCKINFO, null, WHERE_NO_DELETE_STOCK_ROW,
                    null, null, null, COLNAME_SORT_CODE);
            if (null != c) {
                while (c.moveToNext()) {
                    StockFavoriteItem item = new StockFavoriteItem(c.getString(StockFavoriteTable.COLNAME_CODE_INDEX),c.getInt(StockFavoriteTable.COLNAME_CODE_TYPE_INDEX));
                    item.setAddTime(c.getLong(StockFavoriteTable.COLNAME_ADD_TIME_INDEX));
                    items.add(item);
                }
                c.close();
            }
            db.close();

            return items;
        }
    }


    /**
     * 从本地添加数据, 返回新的排序码
     * @param stockCode
     */
    public int addByLocal(String stockCode){
        return addByLocal(stockCode, StockItem.CODE_TYPE_STOCK);
    }
    public int addByLocal(String stockCode, int type){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getWritableDatabase();
            StockFavoriteItem item = new StockFavoriteItem(stockCode,type);

            // 添加排序码（最新加入的要排在最前）
            int sortCode = getMinSortCode(db) - 1;
            if (sortCode == 0){ // 0为默认值，不使用
                sortCode -= 1;
            }
            item.setSortCode(sortCode);

            // 保存行业信息
            StockTable stockTable = new StockTable(mContext);
            IndustryItem industryItem = stockTable.getStockIndustry(stockCode);
            if (null != industryItem){
                item.setIndustryID(industryItem.getID());
                item.setIndustryName(industryItem.getName());
            }
            saveItem(item, true);

            db.close();

            return sortCode;
        }
    }

    /**
     * 从远程添加数据
     * @param items
     */
    public void addArrayByRemote(List<StockFavoriteItem> items){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getWritableDatabase();
            db.beginTransaction();
            // 保存行业信息
            StockTable stockTable = new StockTable(mContext);

            for (StockFavoriteItem item : items) {
                IndustryItem industryItem = stockTable.getStockIndustry(item.getStockCode());
                if (null != industryItem){
                    item.setIndustryID(industryItem.getID());
                    item.setIndustryName(industryItem.getName());
                }
                saveItem(item, false);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }


    /**
     * 本地删除操作（假删除）
     * @param stockCode
     */
    public void deleteByLocal(String stockCode){
        deleteByLocal(stockCode, StockItem.CODE_TYPE_STOCK);
    }
    public void deleteByLocal(String stockCode, int type){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLNAME_LOCAL_SYNC_TYPE, BaseDB.SYNC_LOCAL_TYPE_DELETE);
            contentValues.put(COLNAME_LOCAL_SYNC_FLAG, BaseDB.SYNC_FLAG_NO);
            if (isExist(stockCode, type)) {
                db.update(TABLE_NAME_FAVORITE_STOCKINFO, contentValues, WHERE_ROW, new String[]{stockCode, String.valueOf(type)});
            }

            if (null != mMapKeyID) {
                mMapKeyID.remove(stockCode);
            }
            db.close();
        }

        deleteRecentMsgPrompt();
    }
    /**
     * 从远程删除数据
     * @param items
     */
    public void deleteByRemote(List<StockFavoriteItem> items){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getWritableDatabase();

            for (StockFavoriteItem item : items ) {
                deleteConfirmed(item.getStockCode(), item.getType());
            }

            db.close();
        }

        deleteRecentMsgPrompt();
    }


    /**
     * 删除确认
     * @param stockCode
     */
    private void deleteConfirmed(String stockCode, int type) {

        db.delete(TABLE_NAME_FAVORITE_STOCKINFO, WHERE_ROW, new String[]{stockCode, String.valueOf(type)});

        if (null != mMapKeyID) {
            mMapKeyID.remove(stockCode);
        }

    }

    public boolean isFavorite(String stockCode) {
        synchronized (m_dbHelper) {
            boolean ret = false;
            db = m_dbHelper.getReadableDatabase();
            // 有此股票且标记不为0
            Cursor cursor = db.query(TABLE_NAME_FAVORITE_STOCKINFO, null, WHERE_ROW_NO_DELETE, new String[]{stockCode}, null, null, null);
            if (cursor != null && cursor.moveToFirst())
                ret = true;

            cursor.close();
            db.close();
            return ret;

        }
    }

    /**
     * 获取当前最小的排序码
     * @return
     */
    private int getMinSortCode(SQLiteDatabase db){
        int minSortCode = 0;
        Cursor cursor = db.query(TABLE_NAME_FAVORITE_STOCKINFO, new String[]{SELECTION_MIN_SORT_CODE_ROW}, null, null, null, null, null);
        if (null != cursor){
            if (cursor.moveToFirst()){
                minSortCode = cursor.getInt(0);
            }
            cursor.close();
        }
        return minSortCode;
    }

    /**
     * 保存别名数组方便查询
     */
    /*public void updateStrategyFavoriteIDMap(){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            if (mMapKeyID == null) {
                mMapKeyID = new HashSet<String>();
            } else {
                mMapKeyID.clear();
            }

            // 查询所有数据映射到map
            Cursor c = db.query(TABLE_NAME_FAVORITE_STOCKINFO, new String[]{COLNAME_CODE}, WHERE_NO_DELETE_ROW, null, null, null, null);
            while (c.moveToNext()) {
                mMapKeyID.add(c.getString(0));
            }
            c.close();
            db.close();
        }
    }*/


/*    *//**
     * 获取待提交删除的信息
     * @return
     *//*
    public Cursor getCommitDeleteCursor(){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME_FAVORITE_STOCKINFO, new String[]{COLNAME_CODE, COLNAME_CODE_TYPE}, WHERE_COMMIT_DELETE_ROW, null, null, null, null);
            return c;
        }
    }

    *//**
     * 获取待提交的新增的信息
     * @return
     *//*
    public Cursor getCommitAddCursor(){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME_FAVORITE_STOCKINFO, new String[]{COLNAME_CODE, COLNAME_CODE_TYPE}, WHERE_COMMIT_ADD_ROW, null, null, null, null);
            return c;
        }
    }*/


    /**
     * 获取待提交改动的信息
     * @return
     */
    public Cursor getCommitOperatorCursor(){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME_FAVORITE_STOCKINFO, new String[]{COLNAME_CODE, COLNAME_CODE_TYPE, COLNAME_LOCAL_SYNC_TYPE}, WHERE_COMMIT_OPERATOR_ROW, null, null, null, null);
            return c;
        }
    }

    /**
     * 根据行业ID获取股票组
     * @return
     */
    public List<StockFavoriteItem> getStockFavoriteByIndustryID(String industryID){
        synchronized (m_dbHelper) {
            List<StockFavoriteItem> stockIDs = new ArrayList<StockFavoriteItem>();

            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME_FAVORITE_STOCKINFO, new String[]{COLNAME_CODE}, WHERE_NO_DELETE_STOCK_ROW + " AND " + COLNAME_INDUSTRY_ID + "= ?",
                    new String[]{industryID}, null, null, null);
            while (c.moveToNext()){
                String stockCode = c.getString(0);
                StockFavoriteItem item = new StockFavoriteItem(stockCode);

                stockIDs.add(item);
            }
            c.close();
            db.close();

            return stockIDs;
        }
    }

    /**
     * 获取自选股相关的行业统计信息
     * @return
     */
    public List<IndustryItem> getStockFavoriteIndustryInfo(){
      //  select industry_id,count(*) from stock_favorite_info group by industry_id;
        List<IndustryItem> items = new ArrayList<IndustryItem>();
        synchronized (m_dbHelper) {
            IndustryTable industryTable = new IndustryTable(mContext);// 这里直接用数据跨库连接更好

            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME_FAVORITE_STOCKINFO, new String[]{COLNAME_INDUSTRY_ID, "count(*)"}, WHERE_NO_DELETE_STOCK_ROW, null, COLNAME_INDUSTRY_ID, null, "count(*) DESC");
            while (c.moveToNext()){
                String industryID = c.getString(0);
                int count = c.getInt(1);

                IndustryItem industryItem = industryTable.getItem(industryID);
                if (null != industryItem){
                    industryItem.setSubStockIDs(this.getStockFavoriteByIndustryID(industryID));


                    items.add(industryItem);
                }

            }
            c.close();
            db.close();
        }
        return  items;
    }
    /**
     * 获取自选股相关的题材统计信息
     * @return
     */
    public List<TopicItem> getStockFavoriteTopicInfo(){
        List<TopicItem> items = new ArrayList<TopicItem>();
  /*      synchronized (m_dbHelper) {
            IndustryTable industryTable = new IndustryTable(mContext);// 这里直接用数据跨库连接更好

            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME_FAVORITE_STOCKINFO, new String[]{COLNAME_INDUSTRY_ID, "count(*)"}, WHERE_NO_DELETE_STOCK_ROW, null, COLNAME_INDUSTRY_ID, null, "count(*) DESC");
            while (c.moveToNext()){
                String industryID = c.getString(0);
                int count = c.getInt(1);

                TopicItem industryItem = industryTable.getItem(industryID);
                if (null != industryItem){
                    industryItem.setSubStockCount(count);
                    industryItem.setSubStockIDs(this.getStockFavoriteByIndustryID(industryID));


                    items.add(industryItem);
                }

            }
            c.close();
            db.close();
        }*/
        return  items;
    }

    /**
     * 删除所有自选股后清理最近列表中的的自选股新闻标示
     */
    private void deleteRecentMsgPrompt(){
        List<StockFavoriteItem> items = this.getStockFavoriteItems();
        if (items.size() == 0) {
            RecentTableTool mRecentTable = new RecentTableTool(mContext);
            mRecentTable.delRecent(RecentItem.TYPE_NEWS, NewsRecentItem.KEY_GOODS_STOCK_NEWS, "0");
        }
    }

}
