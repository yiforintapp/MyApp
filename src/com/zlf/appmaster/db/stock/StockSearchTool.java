package com.zlf.appmaster.db.stock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.zlf.appmaster.model.search.StockSearchItem;
import com.zlf.appmaster.utils.QLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索股票工具类
 * 股票基本信息表与用户无关，存储在stock.db中
 * 而其他与用户相关的表存储在用户库中
 * @author Deping Huang
 *
 */
public class StockSearchTool {
    private static final String TAG = StockSearchTool.class.getSimpleName();
	
	// 列名索引位
	public static final int COLNAME_ID_INDEX = 0;
	public static final int COLNAME_CODE_INDEX = 1;
	public static final int COLNAME_NAME_INDEX = 2;
	public static final int COLNAME_PINYIN_INDEX = 3;
    public static final int COLNAME_CODE_TYPE_INDEX = 4;
   // public static final int COLNAME_FAVORITE_FLAG_INDEX = 6;

	private static final String STOCK_DB_NAME = "stock_db";
	private static final String USER_DB_NAME = "user_db";

    private static boolean mSearchStockOnly = false;    // 只搜索股票
	
	private Context mContext;
	SQLiteDatabase m_db;
    private StockDBHelper m_dbStockHelper;
    private QiNiuDBHelper m_dbUserHelper;
	public StockSearchTool(Context context, boolean bStockOnly){
		init(context, bStockOnly);
	}

    public StockSearchTool(Context context){
        init(context, false);
    }

    private void init(Context context, boolean bStockOnly){
        mContext = context;
        mSearchStockOnly = bStockOnly;
        m_dbStockHelper = StockDBHelper.getInstance(context);
        m_dbUserHelper = QiNiuDBHelper.getInstance(context);

        synchronized (m_dbStockHelper) {
            // 股票数据路径
            String stockDBPath = context.getDatabasePath(StockDBHelper.STOCKINFO_DBNAME).getAbsolutePath();
            // 用户数据表
            String userDBPath = context.getDatabasePath(m_dbUserHelper.getDBName()).getAbsolutePath();

            // 连接以上两个库的SQL语句
            String SQL_ATTACH_STOCK_DB = String.format("ATTACH DATABASE '%s' AS '%s';", stockDBPath, STOCK_DB_NAME);
            String SQL_ATTACH_USER_DB = String.format("ATTACH DATABASE '%s' AS '%s';", userDBPath, USER_DB_NAME);

            m_db = SQLiteDatabase.create(null);
            m_db.execSQL(SQL_ATTACH_STOCK_DB);
            m_db.execSQL(SQL_ATTACH_USER_DB);

            createStockFavoriteTable();
        }
    }

    /**
     * 创建自选股表（容错，有的机型会报找不到表的错误，原因暂未知）
     */
    public void createStockFavoriteTable(){
        QLog.i(TAG, "createStockFavoriteTable for temp");
        String createTable = String.format(StockFavoriteTable.CREATE_TABLE_S, USER_DB_NAME);
        m_db.execSQL(createTable);
    }



	/**
	 * 获取带自选股标识的股票列表
	 * @param keyValue
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public Cursor getFavoriteStockCursor(String keyValue){
        Cursor cursor = null;
		if(TextUtils.isEmpty(keyValue)){
			//cursor = m_db.query(getJoinTable(StockFavoriteTable.TABLE_NAME_FAVORITE_STOCKINFO),
			//		getBaseColumn(StockFavoriteTable.TABLE_NAME_FAVORITE_STOCKINFO,StockFavoriteTable.COLNAME_UPDATE_TIME), null, null, null, null, null);
		}
		else if(TextUtils.isDigitsOnly(keyValue)){		// 查股票代码
			cursor = m_db.query(getJoinTable(StockFavoriteTable.TABLE_NAME_FAVORITE_STOCKINFO), 
					getBaseColumn(StockFavoriteTable.TABLE_NAME_FAVORITE_STOCKINFO,StockFavoriteTable.COLNAME_LOCAL_SYNC_TYPE),
					"stock_db.stock_info.code" + " like ?" + getSelectionByStock(), new String[]{"%"+keyValue + "%"},
						null, null, null);
		}
		else {
			// 英文的全部转换成大写
			String keyStr = "%"+ keyValue.toUpperCase() + "%";
			cursor = m_db.query(getJoinTable(StockFavoriteTable.TABLE_NAME_FAVORITE_STOCKINFO), 
					getBaseColumn(StockFavoriteTable.TABLE_NAME_FAVORITE_STOCKINFO,StockFavoriteTable.COLNAME_LOCAL_SYNC_TYPE),
					"stock_db.stock_info.name" + " like ? OR " + "stock_db.stock_info.pinyin" + " like ?" + getSelectionByStock(), new String[]{ keyStr,  keyStr},
					null, null, null);
			
			if(cursor.getCount() == 0){	// 如果没有查找到，考虑到数据库中关键字为 "银 之 杰"的情况
				keyStr = appendSpace(keyValue);
				keyStr = keyStr.toUpperCase();
				//Log.e("ttt", "加空格：\""+keyStr+"\"");
				cursor.close();
				cursor = m_db.query(getJoinTable(StockFavoriteTable.TABLE_NAME_FAVORITE_STOCKINFO), 
						getBaseColumn(StockFavoriteTable.TABLE_NAME_FAVORITE_STOCKINFO,StockFavoriteTable.COLNAME_LOCAL_SYNC_TYPE),
						"stock_db.stock_info.name" + " like ?" + getSelectionByStock(), new String[]{"%"+keyStr + "%"}, null, null, null);
			}

		}
		
		return cursor;
	}


	
	public static String appendSpace(String para){
		   int length = para.length();  
		   char[] value = new char[length << 1];  
		   for (int i=0, j=0; i<length; ++i, j = i << 1) {  
		         value[j] = para.charAt(i);  
		         value[1 + j] = ' ';  
		   }
		   String ret = new String(value);

		   return ret.trim();  
	}
	
	/**
	 * 获取需要与主库连接的表名
	 * @param tableName
	 * @return
	 */
	public static String getJoinTable(String tableName){
         return String.format("stock_db.stock_info LEFT JOIN user_db.%s ON stock_db.stock_info.code = user_db.%s.code AND stock_db.stock_info.code_type=user_db.%s.code_type  AND user_db.%s.local_sync_type != 2", tableName, tableName, tableName, tableName);
	}

    /**
     * 关于股票的附加搜索条件
     * @return
     */
    public static String getSelectionByStock(){
        if (mSearchStockOnly){
            return " AND stock_db.stock_info.code_type = 0";
        }
        else {
            return "";
        }
    }

	/**
	 * 获取最常用的所需的列字段
	 * @param tableName
	 * @param colName
	 * @return
	 */
	public static String[] getBaseColumn(String tableName, String colName){
		String colFormat = String.format("user_db.%s.%s", tableName, colName);
		String[] ret =  new String[]{"stock_db.stock_info._id", "stock_db.stock_info.code", "stock_db.stock_info.name", "stock_db.stock_info.pinyin", "stock_db.stock_info.code_type", colFormat};
		
		return ret;
	}

	
	public void close() {
		if (m_db != null)
			m_db.close();
	}

    public List<StockSearchItem> getAllSearchItems(String keyValue){
        List<StockSearchItem> allItem = new ArrayList<StockSearchItem>();

        Cursor c = getFavoriteStockCursor(keyValue);
        if (c != null){
            while (c.moveToNext()){
                StockSearchItem item = new StockSearchItem();
                item.setStockCode(c.getString(StockSearchTool.COLNAME_CODE_INDEX));
                item.setStockName(c.getString(StockSearchTool.COLNAME_NAME_INDEX));
                item.setType(c.getInt(StockSearchTool.COLNAME_CODE_TYPE_INDEX));

                int stockFavoriteSynType = c.getInt(c.getColumnIndex(StockFavoriteTable.COLNAME_LOCAL_SYNC_TYPE));//COLNAME_FAVORITE_FLAG_INDEX
                if (stockFavoriteSynType == BaseDB.SYNC_LOCAL_TYPE_ADD){ // 为新增的才算自选股（有部分是待同步的删除项）
                    item.setFavorite(true);
                }
                else{
                    item.setFavorite(false);
                }

                allItem.add(item);
            }
           c.close();
        }

        return allItem;
    }

}
