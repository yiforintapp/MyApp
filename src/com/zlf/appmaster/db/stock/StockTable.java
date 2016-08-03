package com.zlf.appmaster.db.stock;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.zlf.appmaster.model.stock.StockItem;
import com.zlf.appmaster.model.industry.IndustryItem;
import com.zlf.appmaster.model.sync.SyncBaseBean;
import com.zlf.appmaster.model.topic.TopicItem;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.utils.QLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 股票数据库
 * @author Deping Huang
 */
public class StockTable {
	private final static String TAG = "StockTable";

	
	public static final String COLNAME_ID = "_id";
	public static final String COLNAME_CODE = "code";		// 股票代码
	public static final String COLNAME_NAME = "name";		// 名称
	public static final String COLNAME_PINYIN = "pinyin";	// 拼音首字母
    public static final String COLNAME_INDUSTRY_ID = "industry_id";
    public static final String COLNAME_TYPE = "code_type";       // 类型(参见stockItem)
    public static final String COLNAME_TOPICS = "topics";       // 题材
	
	public static final String TABLE_NAME_STOCKINFO = "stock_info";
	
	private static final String CREATE_TABLE =
			"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_STOCKINFO
			+ "(" 
			+ COLNAME_ID 				+ 	 "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
			+ COLNAME_CODE 				+	 "	TEXT    NOT NULL ," 
			+ COLNAME_NAME 				+	 "	TEXT,"
			+ COLNAME_PINYIN			+	 "	TEXT,"
            + COLNAME_INDUSTRY_ID       +    "  TEXT,"
            + COLNAME_TYPE              +    "  INT,"
            + COLNAME_TOPICS            +    "  TEXT"
			+ ")";
	
	private static final String INSERT_INTO_DATA =
			"INSERT INTO " + TABLE_NAME_STOCKINFO 
			+ "("
			+ COLNAME_CODE + ","
			+ COLNAME_NAME + ","
			+ COLNAME_PINYIN + ","
            + COLNAME_INDUSTRY_ID + ","
            + COLNAME_TYPE  + ","
            + COLNAME_TOPICS
			+ ")" 
			+ " VALUES(?,?,?,?,?,?)";

    public static final String WHERE_ROW = COLNAME_CODE + " = ? AND " + COLNAME_TYPE + " = ?";
	
	// 插入列
	//	alter table stock_info add column is_favorite INT; 
	
	
	private static final String CLEAR_ALL_DATA = "DELETE FROM " + TABLE_NAME_STOCKINFO;
	
	private SQLiteDatabase db;
	private Context mContext;
	private StockDBHelper m_dbHelper;
	
	public StockTable(Context context){
		mContext = context;
        m_dbHelper = StockDBHelper.getInstance(context);
		//m_dbHelper = new StockDBHelper(context);
        createTable();
	}
	
	public void createTable(){
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
	
	
	@SuppressLint("DefaultLocale")
	public Cursor searchStockList(String keyValue){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();

            Cursor cursor = null;
            if (TextUtils.isEmpty(keyValue)) {
                cursor = db.query(TABLE_NAME_STOCKINFO, null, null, null, null, null, null);
            } else if (TextUtils.isDigitsOnly(keyValue)) {        // 查股票代码
                cursor = db.query(TABLE_NAME_STOCKINFO, null,
                        COLNAME_CODE + " like ?", new String[]{"%" + keyValue + "%"},
                        null, null, null);
            } else {
                // 英文的全部转换成大写
                String keyStr = "%" + keyValue.toUpperCase() + "%";
                cursor = db.query(TABLE_NAME_STOCKINFO, null,
                        COLNAME_NAME + " like ? OR " + COLNAME_PINYIN + " like ?", new String[]{keyStr, keyStr},
                        null, null, null);

                if (!cursor.moveToFirst()) {    // 如果没有查找到，考虑到数据库中关键字为 "银 之 杰"的情况

                    keyStr = appendSpace(keyValue);
                    keyStr = keyStr.toUpperCase();
                    //Log.e("ttt", "加空格：\""+keyStr+"\"");
                    cursor.close();
                    cursor = db.query(TABLE_NAME_STOCKINFO, null,
                            COLNAME_NAME + " like ?", new String[]{"%" + keyStr + "%"}, null, null, null);

                }
            }

            return cursor;
        }
	}


    /**
     * 获取股票名称
     * @param stockCode
     * @return
     */
	public String getStockName(String stockCode){
        return getName(stockCode, StockItem.CODE_TYPE_STOCK);
    }
    public String getName(String stockCode, int codeType){
        synchronized (m_dbHelper) {
            String ret = "";
            db = m_dbHelper.getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME_STOCKINFO, new String[]{COLNAME_NAME}, WHERE_ROW,
                    new String[]{stockCode, String.valueOf(codeType)}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst())
                    ret = cursor.getString(0);
                cursor.close();
            }
            return ret;
        }
    }

    /**
     * 根据股票代码获取所属行业信息
     * @param stockCode
     * @return 查不到返回null
     */
    public IndustryItem getStockIndustry(String stockCode){
        synchronized (m_dbHelper) {
            String industryID = "";
            db = m_dbHelper.getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME_STOCKINFO, new String[]{COLNAME_INDUSTRY_ID}, WHERE_ROW,
                    new String[]{stockCode, String.valueOf(StockItem.CODE_TYPE_STOCK)}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst())
                    industryID = cursor.getString(0);
                cursor.close();
            }

            return new IndustryTable(mContext).getItem(industryID);
        }
    }

    /**
     * 根据股票代码获取所属的题材信息
     * @param stockCode
     * @return
     */
    public List<TopicItem> getStockTopic(String stockCode){
        synchronized (m_dbHelper) {
            List<TopicItem> topicItems = new ArrayList<TopicItem>();
            JSONArray topicIDJSONs = null;
            db = m_dbHelper.getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME_STOCKINFO, new String[]{COLNAME_TOPICS}, WHERE_ROW,
                    new String[]{stockCode, String.valueOf(StockItem.CODE_TYPE_STOCK)}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()){
                    try {
                        topicIDJSONs = new JSONArray(cursor.getString(0));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                cursor.close();

                TopicTable topicTable = new TopicTable(mContext);
                if (null != topicIDJSONs){
                    for (int i = 0; i < topicIDJSONs.length(); i++){
                        String topicID = topicIDJSONs.optString(i);
                        if (!TextUtils.isEmpty(topicID)){
                            TopicItem item = topicTable.getItem(topicID);
                            if (null != item){
                                topicItems.add(item);
                            }
                        }
                    }
                }
            }
            db.close();

            return topicItems;
        }
    }
	

	public void refreshAllData(List<StockItem> StockArray){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getWritableDatabase();
            db.execSQL(CLEAR_ALL_DATA);
            db.beginTransaction(); // 用事务批量处理
            for (StockItem item : StockArray) {
                db.execSQL(INSERT_INTO_DATA, new Object[]{item.getCode(), item.getName(),
                        item.getJianPin(), item.getIndustryId(), item.getCodeType(), item.getTopicIDs()});
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
	}

    public void saveStockItemArray(List<StockItem> StockArray) {
        synchronized (m_dbHelper) {

            db = m_dbHelper.getWritableDatabase();
            db.beginTransaction();
            for (StockItem item : StockArray) {
                String stockCode = item.getCode();
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLNAME_NAME, item.getName());
                contentValues.put(COLNAME_PINYIN, item.getJianPin());
                contentValues.put(COLNAME_INDUSTRY_ID, item.getIndustryId());
                contentValues.put(COLNAME_TYPE, item.getCodeType());
                contentValues.put(COLNAME_TOPICS, item.getTopicIDs());
                if (isStockExist(stockCode, item.getCodeType())) {
                    db.update(TABLE_NAME_STOCKINFO, contentValues, WHERE_ROW, new String[]{stockCode, String.valueOf(item.getCodeType())});
                } else {
                    contentValues.put(COLNAME_CODE, stockCode);
                    db.insert(TABLE_NAME_STOCKINFO, null, contentValues);
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }

    public void deleteStockItems(List<StockItem> stockItems){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getWritableDatabase();
            db.beginTransaction();

            for (StockItem item: stockItems){
                db.delete(TABLE_NAME_STOCKINFO, WHERE_ROW, new String[]{item.getCode(), String.valueOf(item.getCodeType())});
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
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
	 * 查询本表是否存在
	 * @return
	 */
    public boolean isDataExist() {
        boolean ret = false;
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME_STOCKINFO, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst())    // 有结果集则认为数据表正常
                ret = true;
            cursor.close();
            db.close();
        }
        return ret;
    }

    private boolean isStockExist(String stockCode, int codeType){

        Cursor c = db.query(TABLE_NAME_STOCKINFO, null, WHERE_ROW, new String[] { stockCode , String.valueOf(codeType)}, null, null, null);
        boolean ret = c.moveToFirst();
        c.close();

        return ret;
    }



	
	
	/**
	 * 加载预置数据
	 */
    public static void loadPreInitData(Context context) {
        StockTable stockTable = new StockTable(context);

        if (!stockTable.isDataExist()) {    // 不存在数据时才需加载

            QLog.e(TAG, "数据不存在，从assert中加载");
            try {
                List<StockItem> stockItems = loadPreIndexData();    // 预留的指数
                int len = 1024;
                byte[] buffer = new byte[len];
                InputStream fis = context.getAssets().open(StockJsonCache.DATA_PATH_ALL_STOCK_ITEMS);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int nrb = fis.read(buffer, 0, len); // read up to len bytes
                while (nrb != -1) {
                    baos.write(buffer, 0, nrb);
                    nrb = fis.read(buffer, 0, len);
                }
                buffer = baos.toByteArray();
                fis.close();
                String json = new String(buffer, "UTF-8");
                JSONObject jsonObject = new JSONObject(json);
                 stockItems.addAll(StockItem.resolveJsonAllStockItems(jsonObject));

                QLog.e(TAG, "从assert刷新到数据库中");
                stockTable.refreshAllData(stockItems);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // 保存该数据的版本号
            try {
                // 获取Manifest中内置的版本号
                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                String versionCode = appInfo.metaData.getString("QINIU_DATA_VERSION_1").substring(1);
                long longVersionCode = Long.valueOf(versionCode);

                SyncVersionTable syncVersionTable = new SyncVersionTable(context, StockDBHelper.getInstance(context));
                syncVersionTable.setVersion(SyncBaseBean.SYNC_KEY_STOCK_BASE_DATA, longVersionCode);
                syncVersionTable.close();

            } catch (PackageManager.NameNotFoundException e) {
                 e.printStackTrace();
            }

            //

        }
    }



    // 预置的指数数据
    private static final String PRE_DATA_INDEX_NAME[] = {"上证指数","深证成指","创业板指", "中小板指","沪深300"};
    private static final String PRE_DATA_INDEX_CODE[] = {"000001","399001","399006", "399005","399300"};
    private static final String PRE_DATA_INDEX_JIANPIN[] = {"SZZS","SZCZ","CYBZ", "ZXBZ","HS300"};
    private static final int PRE_DATA_INDEX_INIT_LEN = PRE_DATA_INDEX_CODE.length;

    public static List<StockItem> loadPreIndexData(){

        List<StockItem> stockIndexItem = new ArrayList<StockItem>();
        for (int i = 0; i< PRE_DATA_INDEX_INIT_LEN; i++) {
            StockItem item = new StockItem();
            item.setCode(PRE_DATA_INDEX_CODE[i]);
            item.setName(PRE_DATA_INDEX_NAME[i]);
            item.setCodeType(StockItem.CODE_TYPE_INDEX);
            item.setJianPin(PRE_DATA_INDEX_JIANPIN[i]);
            stockIndexItem.add(item);
        }

        return  stockIndexItem;
    }






}
