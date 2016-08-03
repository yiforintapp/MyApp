package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.model.stock.StockTradeInfo.BuyOrSellInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 股票交易信息记录表
 * @author Deping Huang
 *
 */
public class StockTradeTable {
	public static final String COLNAME_ID = "_id";
	public static final String COLNAME_CODE = "code";		// 股票代码
	public static final String COLNAME_NAME = "name";		// 名称
	public static final String COLNAME_HQJRKP = "HQJRKP";	// 今日开盘价
	public static final String COLNAME_HQZRSP = "HQZRSP";	// 昨日收盘价
	public static final String COLNAME_HQZJCJ = "HQZJCJ";	// 按盘价
	public static final String COLNAME_HQZGCJ = "HQZGCJ";	// 最高成交价
	public static final String COLNAME_HQZDCJ = "HQZDCJ";	// 最低成交价
	public static final String COLNAME_HQCJSL = "HQCJSL";	// 成交数量
	public static final String COLNAME_HQCJJE = "HQCJJE";	// 成交金额
	public static final String COLNAME_circulationNum = "circulationNum";	// 总股数
	public static final String COLNAME_circulateNum = "circulateNum";		// 发行总股数
	public static final String COLNAME_HQSYL1 = "HQSYL1";		// 市盈率1
	
	// 买数量和价位
	public static final String COLNAME_HQBSL1 = "HQBSL1";
	public static final String COLNAME_HQBJW1 = "HQBJW1";
	public static final String COLNAME_HQBSL2 = "HQBSL2";
	public static final String COLNAME_HQBJW2 = "HQBJW2";
	public static final String COLNAME_HQBSL3 = "HQBSL3";
	public static final String COLNAME_HQBJW3 = "HQBJW3";
	public static final String COLNAME_HQBSL4 = "HQBSL4";
	public static final String COLNAME_HQBJW4 = "HQBJW4";
	public static final String COLNAME_HQBSL5 = "HQBSL5";
	public static final String COLNAME_HQBJW5 = "HQBJW5";
	
	// 卖数量和价位
	public static final String COLNAME_HQSSL1 = "HQSSL1";
	public static final String COLNAME_HQSJW1 = "HQSJW1";
	public static final String COLNAME_HQSSL2 = "HQSSL2";
	public static final String COLNAME_HQSJW2 = "HQSJW2";
	public static final String COLNAME_HQSSL3 = "HQSSL3";
	public static final String COLNAME_HQSJW3 = "HQSJW3";
	public static final String COLNAME_HQSSL4 = "HQSSL4";
	public static final String COLNAME_HQSJW4 = "HQSJW4";
	public static final String COLNAME_HQSSL5 = "HQSSL5";
	public static final String COLNAME_HQSJW5 = "HQSJW5";

	// 股票状态和数据版本时间
	public static final String COLNAME_STOCK_STATUS = "STOCK_STATUS";
	public static final String COLNAME_DATA_TIME ="DATA_TIME";
	
	// 市净率
    public static final String COLNAME_BOOK_VALUE = "book_value";
	
	/**
	 * 加快查找速度直接定义索引列
	 */
	//------------------------------------------------------------------------------------------//
	public static final int COLNAME_CODE_INDEX = 1;		// 股票代码 
	public static final int COLNAME_NAME_INDEX = 2;		// 名称	
	public static final int COLNAME_HQJRKP_INDEX = 3;	// 今日开盘价
	public static final int COLNAME_HQZRSP_INDEX = 4;	// 昨日收盘价
	public static final int COLNAME_HQZJCJ_INDEX = 5;	// 按盘价
	public static final int COLNAME_HQZGCJ_INDEX = 6;	// 最高成交价
	public static final int COLNAME_HQZDCJ_INDEX = 7;	// 最低成交价
	public static final int COLNAME_HQCJSL_INDEX = 8;	// 成交数量
	public static final int COLNAME_HQCJJE_INDEX = 9;	// 成交金额
	public static final int COLNAME_circulationNum_INDEX = 10;	// 总股数
	public static final int COLNAME_circulateNum_INDEX = 11;		// 发行总股数
	public static final int COLNAME_HQSYL1_INDEX = 12;		// 市盈率1
	public static final int COLNAME_HQBSL1_INDEX = 13;
	public static final int COLNAME_HQBJW1_INDEX = 14;		
	public static final int COLNAME_HQBSL2_INDEX = 15;
	public static final int COLNAME_HQBJW2_INDEX = 16;
	public static final int COLNAME_HQBSL3_INDEX = 17;
	public static final int COLNAME_HQBJW3_INDEX = 18;
	public static final int COLNAME_HQBSL4_INDEX = 19;
	public static final int COLNAME_HQBJW4_INDEX = 20;
	public static final int COLNAME_HQBSL5_INDEX = 21;
	public static final int COLNAME_HQBJW5_INDEX = 22;
	public static final int COLNAME_HQSSL1_INDEX = 23;
	public static final int COLNAME_HQSJW1_INDEX = 24;		
	public static final int COLNAME_HQSSL2_INDEX = 25;
	public static final int COLNAME_HQSJW2_INDEX = 26;
	public static final int COLNAME_HQSSL3_INDEX = 27;
	public static final int COLNAME_HQSJW3_INDEX = 28;
	public static final int COLNAME_HQSSL4_INDEX = 29;
	public static final int COLNAME_HQSJW4_INDEX = 30;
	public static final int COLNAME_HQSSL5_INDEX = 31;
	public static final int COLNAME_HQSJW5_INDEX = 32;
	public static final int COLNAME_STOCK_STATUS_INDEX = 33;
	public static final int COLNAME_DATA_TIME_INDEX = 34;
    public static final int COLNAME_BOOK_VALUE_IDNEX  = 35;
	//------------------------------------------------------------------------------------------//
	
	public static final String TABLE_NAME_STOCK_TRADE_INFO = "stock_trade_info";

	private static final String CREATE_TABLE =
			"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_STOCK_TRADE_INFO
			+ "(" 
			+ COLNAME_ID 				+ 	 "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
			+ COLNAME_CODE 				+	 "	TEXT    NOT NULL ," 
			+ COLNAME_NAME 				+	 "	TEXT,"
			+ COLNAME_HQJRKP			+	 "	REAL,"
			+ COLNAME_HQZRSP			+	 " 	REAL,"
			+ COLNAME_HQZJCJ			+ 	 "	REAL,"
			+ COLNAME_HQZGCJ			+ 	 "	REAL,"
			+ COLNAME_HQZDCJ			+ 	 "	REAL,"
			+ COLNAME_HQCJSL			+ 	 "	INT,"
			+ COLNAME_HQCJJE			+ 	 "	INT,"
			+ COLNAME_circulationNum	+ 	 "	INT,"
			+ COLNAME_circulateNum		+ 	 "	INT,"
			+ COLNAME_HQSYL1			+ 	 "	REAL,"
			
			+ COLNAME_HQBSL1		+ 	 "	INT,"
			+ COLNAME_HQBJW1		+ 	 "	REAL,"
			+ COLNAME_HQBSL2		+ 	 "	INT,"
			+ COLNAME_HQBJW2		+ 	 "	REAL,"
			+ COLNAME_HQBSL3		+ 	 "	INT,"
			+ COLNAME_HQBJW3		+ 	 "	REAL,"
			+ COLNAME_HQBSL4		+ 	 "	INT,"
			+ COLNAME_HQBJW4		+ 	 "	REAL,"
			+ COLNAME_HQBSL5		+ 	 "	INT,"
			+ COLNAME_HQBJW5		+ 	 "	REAL,"
			
			+ COLNAME_HQSSL1		+ 	 "	INT,"
			+ COLNAME_HQSJW1		+ 	 "	REAL,"
			+ COLNAME_HQSSL2		+ 	 "	INT,"
			+ COLNAME_HQSJW2		+ 	 "	REAL,"
			+ COLNAME_HQSSL3		+ 	 "	INT,"
			+ COLNAME_HQSJW3		+ 	 "	REAL,"
			+ COLNAME_HQSSL4		+ 	 "	INT,"
			+ COLNAME_HQSJW4		+ 	 "	REAL,"
			+ COLNAME_HQSSL5		+ 	 "	INT,"
			+ COLNAME_HQSJW5		+ 	 "	REAL,"
			+ COLNAME_STOCK_STATUS	+	 "	INT,"
			+ COLNAME_DATA_TIME 	+	 "	INT,"
            + COLNAME_BOOK_VALUE	+ 	 "	REAL"
			+ ")";
	
	public static final String WHERE_ROW = COLNAME_CODE + " = ?";
	
	private StockDBHelper m_dbHelper;
	private SQLiteDatabase m_db;
	public StockTradeTable(Context context){
		m_dbHelper = StockDBHelper.getInstance(context);
        createTable();
	}

    private void createTable(){
        synchronized (m_dbHelper){
            m_db = m_dbHelper.getWritableDatabase();
            m_db.execSQL(CREATE_TABLE);
        }

    }
	
	public void close(){
//		if(m_db != null)
//			m_db.close();
	}
	/**
	 * 保存单个股票信息，如果有多个请用saveItems
	 * @param item
	 */
	public void saveItem(StockTradeInfo item){
        synchronized (m_dbHelper) {
            if (item == null || TextUtils.isEmpty(item.getCode())) {
                return;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLNAME_CODE, item.getCode());
            contentValues.put(COLNAME_NAME, item.getName());
            contentValues.put(COLNAME_HQJRKP, item.getTodayPrice());
            contentValues.put(COLNAME_HQZRSP, item.getYestodayPrice());
            contentValues.put(COLNAME_HQZJCJ, item.getCurPrice());
            contentValues.put(COLNAME_HQZGCJ, item.getHighestPrice());
            contentValues.put(COLNAME_HQZDCJ, item.getLowestPrice());

            contentValues.put(COLNAME_HQCJSL, item.getTradeCount());
            contentValues.put(COLNAME_HQCJJE, item.getTradeMoney());

            contentValues.put(COLNAME_circulationNum, item.getStockAsset().getTotalShares());
            contentValues.put(COLNAME_circulateNum, item.getStockAsset().getAFloatListed());

            contentValues.put(COLNAME_STOCK_STATUS, item.getStockStatus());
            contentValues.put(COLNAME_DATA_TIME, item.getDataTime());

            // 市盈率和市净率
            contentValues.put(COLNAME_HQSYL1, item.getStockAsset().getPeRatio());
            contentValues.put(COLNAME_BOOK_VALUE, item.getStockAsset().getBookValue());

            // 买手信息
            BuyOrSellInfo[] buyInfos = item.getBuyInfo();
            if (buyInfos != null && buyInfos.length <= 5) {
                int len = buyInfos.length;
                for (int i = 0; i < len; i++) {
                    contentValues.put("HQBSL" + String.valueOf(i + 1), buyInfos[i].getCount());
                    contentValues.put("HQBJW" + String.valueOf(i + 1), buyInfos[i].getPrice());
                }
            }

            // 卖手信息
            BuyOrSellInfo[] sellInfos = item.getSellInfo();
            if (buyInfos != null && sellInfos.length <= 5) {
                int len = sellInfos.length;
                for (int i = 0; i < len; i++) {
                    contentValues.put("HQSSL" + String.valueOf(i + 1), sellInfos[i].getCount());
                    contentValues.put("HQSJW" + String.valueOf(i + 1), sellInfos[i].getPrice());
                }
            }

            if (isExist(item.getCode())) {    // 更新
                m_db.update(TABLE_NAME_STOCK_TRADE_INFO, contentValues, WHERE_ROW, new String[]{item.getCode()});
            } else {    // 插入
                m_db.insert(TABLE_NAME_STOCK_TRADE_INFO, null, contentValues);
            }
        }
	}
	
	/**
	 * 保存股票组（提交一次事务处理）
	 * @param mapItems
	 */
	public void saveItems(Map<String, StockTradeInfo> mapItems){
        synchronized (m_dbHelper) {
            m_db.beginTransaction(); // 事务批量处理
            Set<String> key = mapItems.keySet();
            for (Iterator<String> it = key.iterator(); it.hasNext(); ) {
                saveItem(mapItems.get(it.next()));
            }
            m_db.setTransactionSuccessful();
            m_db.endTransaction();
        }
	}
	public void saveItems(List<StockTradeInfo> items){
        synchronized (m_dbHelper) {
            m_db.beginTransaction();
            for (StockTradeInfo item : items) {
                saveItem(item);
            }

            m_db.setTransactionSuccessful();
            m_db.endTransaction();
        }
	}
	
	
	public StockTradeInfo getItem(String stockCode){
        synchronized (m_dbHelper) {
            if (!m_db.isOpen()){
                m_db = m_dbHelper.getReadableDatabase();
            }
            Cursor c = m_db.query(TABLE_NAME_STOCK_TRADE_INFO, null, WHERE_ROW, new String[]{stockCode}, null, null, null);
            StockTradeInfo stockTradeInfo = null;
            if (c != null) {
                if (c.moveToFirst()) {
                    String name = c.getString(COLNAME_NAME_INDEX);
                    String code = c.getString(COLNAME_CODE_INDEX);
                    float todayPrice = c.getFloat(COLNAME_HQJRKP_INDEX);            // 今日开盘价
                    float yesterdayPrice = c.getFloat(COLNAME_HQZRSP_INDEX);        // 昨日收盘价
                    float nowPrice = c.getFloat(COLNAME_HQZJCJ_INDEX);                // 按盘价
                    float highestPrice = c.getFloat(COLNAME_HQZGCJ_INDEX);            // 最高成交价
                    float lowestPrice = c.getFloat(COLNAME_HQZDCJ_INDEX);            // 最低成交价
                    long tradeCount = c.getLong(COLNAME_HQCJSL_INDEX);                // 成交数量
                    long tradeMoney = c.getLong(COLNAME_HQCJJE_INDEX);            // 成交金额
                    long circulationNum = c.getLong(COLNAME_circulationNum_INDEX);    // 总股数
                    long circulateNum = c.getLong(COLNAME_circulateNum_INDEX);        // 发行总股数
                    float PERatio1 = c.getFloat(COLNAME_HQSYL1_INDEX);                // 市盈率1
                    float bookValue = c.getFloat(COLNAME_BOOK_VALUE_IDNEX);           // 市净率
                    boolean isAH = false;               // 数据库中暂未存该字段 comment by Deping Huang Date:20141219


                    BuyOrSellInfo buy1 = new BuyOrSellInfo(c.getLong(COLNAME_HQBSL1_INDEX), c.getFloat(COLNAME_HQBJW1_INDEX), yesterdayPrice); // 买卖数量和价位
                    BuyOrSellInfo buy2 = new BuyOrSellInfo(c.getLong(COLNAME_HQBSL2_INDEX), c.getFloat(COLNAME_HQBJW2_INDEX), yesterdayPrice);
                    BuyOrSellInfo buy3 = new BuyOrSellInfo(c.getLong(COLNAME_HQBSL3_INDEX), c.getFloat(COLNAME_HQBJW3_INDEX), yesterdayPrice);
                    BuyOrSellInfo buy4 = new BuyOrSellInfo(c.getLong(COLNAME_HQBSL4_INDEX), c.getFloat(COLNAME_HQBJW4_INDEX), yesterdayPrice);
                    BuyOrSellInfo buy5 = new BuyOrSellInfo(c.getLong(COLNAME_HQBSL5_INDEX), c.getFloat(COLNAME_HQBJW5_INDEX), yesterdayPrice);

                    BuyOrSellInfo sell1 = new BuyOrSellInfo(c.getLong(COLNAME_HQSSL1_INDEX), c.getFloat(COLNAME_HQSJW1_INDEX), yesterdayPrice);
                    BuyOrSellInfo sell2 = new BuyOrSellInfo(c.getLong(COLNAME_HQSSL2_INDEX), c.getFloat(COLNAME_HQSJW2_INDEX), yesterdayPrice);
                    BuyOrSellInfo sell3 = new BuyOrSellInfo(c.getLong(COLNAME_HQSSL3_INDEX), c.getFloat(COLNAME_HQSJW3_INDEX), yesterdayPrice);
                    BuyOrSellInfo sell4 = new BuyOrSellInfo(c.getLong(COLNAME_HQSSL4_INDEX), c.getFloat(COLNAME_HQSJW4_INDEX), yesterdayPrice);
                    BuyOrSellInfo sell5 = new BuyOrSellInfo(c.getLong(COLNAME_HQSSL5_INDEX), c.getFloat(COLNAME_HQSJW5_INDEX), yesterdayPrice);


                    int stockStatus = c.getInt(COLNAME_STOCK_STATUS_INDEX);
                    int marketStatus = StockTradeInfo.MARKET_STATUS_NORMAL;//市场状态不应该和某只股票相关，StockTradeInfo类待修改 Date:20140828
                    long time = c.getLong(COLNAME_DATA_TIME_INDEX);

                    stockTradeInfo = new StockTradeInfo(code, name, isAH, todayPrice, yesterdayPrice, nowPrice,
                            highestPrice, lowestPrice, tradeCount, tradeMoney,
                            new BuyOrSellInfo[]{buy1, buy2, buy3, buy4, buy5},
                            new BuyOrSellInfo[]{sell1, sell2, sell3, sell4, sell5}, stockStatus, marketStatus,
                            time);

                    stockTradeInfo.getStockAsset().setPeRatio(PERatio1);
                    stockTradeInfo.getStockAsset().setBookValue(bookValue);
                    stockTradeInfo.getStockAsset().setTotalShares(circulationNum);
                }
                c.close();
            }
            return stockTradeInfo;
        }
	}

	
	
	private boolean isExist(String stockCode){
		
		Cursor c = m_db.query(TABLE_NAME_STOCK_TRADE_INFO, null, WHERE_ROW, new String[] { stockCode }, null, null, null);
		boolean ret = c.moveToFirst();
		c.close();
		
		return ret;
	}
	
	/**
	 * 判断数据库是否已打开
	 * @return
	 */
	public boolean isDBOpen(){
		return m_db.isOpen();
	}
	
	
//	private class StockTradeDBHelper extends SQLiteOpenHelper{
//
//		public StockTradeDBHelper(Context context) {
//			super(context, BaseDB.STOCKINFO_DBNAME, null, BaseDB.STOCKINFO_DBVERSION);
//			// TODO Auto-generated constructor stub
//		}
//
//		@Override
//		public void onCreate(SQLiteDatabase db) {
//			// TODO Auto-generated method stub
//			db.execSQL(CREATE_TABLE);
//		}
//
//		@Override
//		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//			// TODO Auto-generated method stub
//
//		}
//	}
}
