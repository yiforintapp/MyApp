package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zlf.appmaster.model.search.StockSearchItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 股票搜索历史表
 * @author Deping Huang
 */
public class StockSearchHistoryTable {
	public static final String COLNAME_ID = "_id";
	public static final String COLNAME_CODE = "code";				    // 股票代码
    public static final String COLNAME_NAME = "name";                   // 股票名称（当股票名称有改变时会遗留bug，应同步改变信息,comment By Deping Huang 20150108）
    public static final String COLNAME_SEARCH_COUNT = "search_count";   // 更新次数
	public static final String COLNAME_UPDATE_TIME = "update_time";	    // 更新时间
    public static final String COLNAME_CREATE_TIME = "create_time";     // 创建时间
    public static final String COLNAME_CODE_TYPE = "code_type";                   // 类型

    public static final int COLNAME_ID_INDEX = 0;
    public static final int COLNAME_CODE_INDEX = 1;
    public static final int COLNAME_NAME_INDEX = 2;
    public static final int COLNAME_SEARCH_COUNT_INDEX = 3;
    public static final int COLNAME_UPDATE_TIME_INDEX = 4;
    public static final int COLNAME_CREATE_TIME_INDEX = 5;
    public static final int COLNAME_CODE_TYPE_INDEX = 6;

    //public static final int COLNAME_STOCK_FAVORITE_CODE_INDEX = COLNAME_CODE_TYPE_INDEX + 7;  // 加几参看StockFavoriteTable

	
	private static final String TAG = "StockSearchHistoryTable";
	
	public static final String TABLE_NAME_STOCK_SEARCH_HISTORY = "stock_search_history_info";
	
	private static final String CREATE_TABLE =
			"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_STOCK_SEARCH_HISTORY
			+ "(" 
			+ COLNAME_ID 				+ 	 "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
			+ COLNAME_CODE 				+	 "	TEXT    NOT NULL,"
            + COLNAME_NAME 				+	 "	TEXT,"
            + COLNAME_SEARCH_COUNT		+	 "	INT,"
			+ COLNAME_UPDATE_TIME		+	 "	INT,"
            + COLNAME_CREATE_TIME		+	 "	INT,"
            + COLNAME_CODE_TYPE		    +	 "	INT"
            + ")";
	
	public static final String WHERE_ROW = COLNAME_CODE + " = ? AND " + COLNAME_CODE_TYPE + " = ? ";
	
	private SQLiteDatabase m_db;
	private Context mContext;
	
	public StockSearchHistoryTable(Context context){
		mContext = context;
		 m_db = QiNiuDBHelper.getInstance(context).getWritableDatabase();
		 m_db.execSQL(CREATE_TABLE);
	}
	
	/**
	 * 增加单条数据
	 * @param stockCode
	 */
	public void saveItem(String stockCode, String stockName, int codeType){
		ContentValues contentValues = new ContentValues();
        long currentTime = System.currentTimeMillis();
		contentValues.put(COLNAME_UPDATE_TIME, currentTime);

        StockSearchItem item = getItem(stockCode, codeType);

		if(null != item){		// 更新
            contentValues.put(COLNAME_SEARCH_COUNT,item.getSearchCount()+1);
			m_db.update(TABLE_NAME_STOCK_SEARCH_HISTORY, contentValues, WHERE_ROW, new String[] { stockCode, String.valueOf(codeType) });
		}
		else{	// 插入
			contentValues.put(COLNAME_CODE, stockCode);
            contentValues.put(COLNAME_SEARCH_COUNT, 0);
            contentValues.put(COLNAME_NAME, stockName);
            contentValues.put(COLNAME_UPDATE_TIME, currentTime);
            contentValues.put(COLNAME_CODE_TYPE, codeType);
			m_db.insert(TABLE_NAME_STOCK_SEARCH_HISTORY, null, contentValues);
		}
	}

	/**
	 * 清除所有数据
	 */
	public void clearAll(){
		m_db.execSQL("delete from "+ TABLE_NAME_STOCK_SEARCH_HISTORY);
	}
	
	private boolean isExist(String stockCode, int codeType){
		
		Cursor c = m_db.query(TABLE_NAME_STOCK_SEARCH_HISTORY, null, WHERE_ROW, new String[] { stockCode, String.valueOf(codeType) }, null, null, null);
		boolean ret = c.moveToFirst();

		c.close();
		
		return ret;
	}

    public StockSearchItem getItem(String stockCode, int codeType){

        Cursor c = m_db.query(TABLE_NAME_STOCK_SEARCH_HISTORY, null, WHERE_ROW, new String[] { stockCode, String.valueOf(codeType) }, null, null, null);
        if (c.moveToFirst()){
            StockSearchItem item = new StockSearchItem();
            item.setStockCode(c.getString(COLNAME_CODE_INDEX));
            item.setStockName(c.getString(COLNAME_NAME_INDEX));
            item.setSearchCount(c.getInt(COLNAME_SEARCH_COUNT_INDEX));
            item.setUpdateTime(c.getLong(COLNAME_UPDATE_TIME_INDEX));
            item.setType(c.getInt(COLNAME_CODE_TYPE_INDEX));
            return item;
        }

        return null;
    }


    /**
     * 获取历史搜索的所有项（要与自选股表连接查询）
     * @return
     */
    public List<StockSearchItem> getAllItem(){
        List<StockSearchItem> allItem = new ArrayList<StockSearchItem>();
        // 与自选股表联合查询(只取3个)
        Cursor c = m_db.query("stock_search_history_info LEFT JOIN stock_favorite_info ON stock_search_history_info.code = stock_favorite_info.code AND stock_search_history_info.code_type = stock_favorite_info.code_type",
                null, null, null, null, null, COLNAME_UPDATE_TIME + " desc","3");
        while (c.moveToNext()){
            StockSearchItem item = new StockSearchItem();
            item.setStockCode(c.getString(COLNAME_CODE_INDEX));
            item.setStockName(c.getString(COLNAME_NAME_INDEX));
            item.setSearchCount(c.getInt(COLNAME_SEARCH_COUNT_INDEX));
            item.setUpdateTime(c.getLong(COLNAME_UPDATE_TIME_INDEX));
            item.setType(c.getInt(COLNAME_CODE_TYPE_INDEX));

            int stockFavoriteSynType = c.getInt(c.getColumnIndex(StockFavoriteTable.COLNAME_LOCAL_SYNC_TYPE));//COLNAME_STOCK_FAVORITE_CODE_INDEX
            if (stockFavoriteSynType == BaseDB.SYNC_LOCAL_TYPE_ADD){ // 为新增的才算自选股（有部分是待同步的删除项）
                item.setFavorite(true);
            }
            else{
                item.setFavorite(false);
            }


            allItem.add(item);
        }
        return  allItem;
    }

}
