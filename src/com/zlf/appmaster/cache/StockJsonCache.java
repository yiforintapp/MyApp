package com.zlf.appmaster.cache;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 股票相关的json数据缓存
 * @author Deping
 *
 */
public class StockJsonCache {
	private static String TAG = "StockJsonCache";
	// 数据模块
	public static final int CACHEID_FAVORITE = 2; 						// 自选股
	public static final int CACHEID_FAVORITE_STOCK_LIST = 3; 			// 自选股详细股票信息
	public static final int CACHEID_HOTTEST_STOCK_LIST = 4;				// 最热个股详细股票信息
	public static final int CACHEID_MARKET_STOCK_LIST = 5;				// 股票交易详细股票信息
	public static final int CACHEID_MARKET_PRICE	=	6;				// 行情
	public static final int CACHEID_NEWS = 7;							// 快讯
	public static final int CACHEID_ALL_STOCK_ITEMS = 8;				// 股票列表
	public static final int CACHEID_USER_PROFIT = 9;					// 用户收益率
	public static final int CACHEID_TRADE_PROFIT = 10;					// 股票盈亏与持仓市值

	public static final int CACHEID_EXTRA_INFO_NEWS	= 10;				// 新闻
	public static final int CACHEID_EXTRA_INFO_ANNOUCEMENT	= 11;		// 公告
	public static final int CACHEID_EXTRA_INFO_REPORT	= 12;			// 研报
	public static final int CACHEID_EXTRA_INFO_MONEY	= 13;			// 资金
	public static final int CACHEID_EXTRA_INFO_SUMMARY	= 14;			// 概况
	public static final int CACHEID_EXTRA_INFO_FINANCE	= 15;			// 财务

	public static final int CACHEID_EXTRA_INFO_INDEX_DETAIL = 16;              // 指数

	public static final int CACHEID_QUOTATIONS_INDEX = 17;
	public static final int CACHEID_QUOTATIONS_INDUSTRY = 18;
	public static final int CACHEID_QUOTATIONS_RISE_INFO = 19;
	public static final int CACHEID_QUOTATIONS_HOT_STOCK = 20;
	public static final int CACHEID_QUOTATIONS_RZRQ = 21;
	public static final int CACHEID_QUOTATIONS_AH = 22;
	public static final int CACHEID_QUOTATIONS_TOPIC = 23;
	public static final int CACHEID_TOPIC_FAVORITE = 24;

	//discovery modules add by liuhm
	public static final int CACHEID_DISCOVERY_TODAY_TOPIC = 25;
	public static final int CACHEID_DISCOVERY_NOTICE= 26;
	public static final int CACHEID_DISCOVERY_60DAYS_HOT = 27;
	public static final int CACHEID_DISCOVERY_20DAYS_HOT = 28;
	public static final int CACHEID_DISCOVERY_5DAYS_HOT = 29;

	public static final int CACHEID_DISCOVERY_RZJM_RCJE = 30;
	public static final int CACHEID_DISCOVERY_RZYE_LTSZ = 31;
	public static final int CACHEID_DISCOVERY_DRRZJMRE = 32 ;

	public static final int CACHEID_COMBINATION_SMART = 33 ;  			//迷你基金

	public static final int CACHEID_STOCK_STRATEGY_LIST = 100;          // 股票策略列表

	public static final int CACHEID_PREPARE_STOCK_LIST = 34; 			// 自选股详细股票信息

	// 数据路径
	private static final String DATA_PATH_ROOT = "/StockInfo/";
	private static final String DATA_PATH_FAVORITE = "json_favorite";
	private static final String DATA_PATH_FAVORITE_STOCK_LIST = "json_favorite_detail";
	private static final String DATA_PATH_HOTTEST_STOCK_LIST = "json_hottest_detail";
	private static final String DATA_PATH_MARKET_STOCK_LIST = "json_market_detail";
	private static final String DATA_PATH_MARKET_PRICE = "json_market_price";
	private static final String DATA_PATH_NEWS = "json_news";
	public static final String DATA_PATH_ALL_STOCK_ITEMS = "json_all_items";
	public static final String DATA_PATH_ALL_INDUSTRY_ITEMS = "json_all_industry_items";
	public static final String DATA_PATH_ALL_TOPIC_ITEMS = "json_all_topic_items";
	private static final String DATA_PATH_USER_PROFIT = "json_user_profit";

	private static final String DATA_PATH_EXTRA_INFO_NEWS = "json_extra_info_news";
	private static final String DATA_PATH_EXTRA_INFO_ANNOUCEMENT = "json_extra_info_annoucement";
	private static final String DATA_PATH_EXTRA_INFO_REPORT = "json_extra_info_report";
	private static final String DATA_PATH_EXTRA_INFO_MONEY = "json_extra_info_money";
	private static final String DATA_PATH_EXTRA_INFO_SUMMARY = "json_extra_info_summary";
	private static final String DATA_PATH_EXTRA_INFO_FINANCE = "json_extra_info_finance";

	private static final String DATA_PATH_EXTRA_INFO_INDEX_DETAIL = "json_extra_info_index_detail";

	public static final String DATA_PATH_QUOTATIONS_INDEX = "stock_quotations_index";
	public static final String DATA_PATH_QUOTATIONS_INDUSTRY = "stock_quotations_industry";
	public static final String DATA_PATH_QUOTATIONS_RISE_INFO = "stock_quotations_rise_info";
	public static final String DATA_PATH_QUOTATIONS_HOT_STOCK = "stock_quotations_hot_stock";
	public static final String DATA_PATH_QUOTATIONS_RZRQ = "stock_quotations_rzrq";
	public static final String DATA_PATH_QUOTATIONS_AH = "stock_quotations_ah";
	public static final String DATA_PATH_QUOTATIONS_TOPIC = "stock_quotations_topic";
	public static final String DATA_PATH_TOPIC_FAVORITE = "stock_topic_favorite";

	private static final String DATA_PATH_STOCK_STRATEGY_LIST = "json_stock_strategy_list";

	private static final String DATA_PATH_DISCOVERY_TODAY_TOPIC = "discovery_today_topic";
	private static final String DATA_PATH_DISCOVERY_NOTICE = "discovery_notice";
	private static final String DATA_PATH_DISCOVERY_60DAYS_HOT = "60days_hot";
	private static final String DATA_PATH_DISCOVERY_20DAYS_HOT = "20days_hot";
	private static final String DATA_PATH_DISCOVERY_5DAYS_HOT = "5days_hot";
	private static final String DATA_PATH_DISCOVERY_RZJM_RCJE = "rzrq_rzjm_rcje";
	private static final String DATA_PATH_DISCOVERY_RZYE_LTSZ= "rzrq_rzye_ltsz";
	private static final String DATA_PATH_DISCOVERY_DRRZJMRE= "rzrq_drrzjmre";

	private static final String DATA_PATH_COMBINATION_SMART = "smart_combination";
	private static final String DATA_PATH_PREPARE_STOCK= "prepare_stock";

	private static String getCacheFilePath(int cacheId) {
		String filename = "";
		switch (cacheId) {
			case CACHEID_FAVORITE:
				filename = DATA_PATH_FAVORITE;
				break;
			case CACHEID_NEWS:
				filename = DATA_PATH_NEWS;
				break;
			case CACHEID_FAVORITE_STOCK_LIST:
				filename = DATA_PATH_FAVORITE_STOCK_LIST;
				break;
			case CACHEID_HOTTEST_STOCK_LIST:
				filename = DATA_PATH_HOTTEST_STOCK_LIST;
				break;
			case CACHEID_MARKET_STOCK_LIST:
				filename = DATA_PATH_MARKET_STOCK_LIST;
				break;
			case CACHEID_MARKET_PRICE:
				filename = DATA_PATH_MARKET_PRICE;
				break;
			case CACHEID_ALL_STOCK_ITEMS:
				filename = DATA_PATH_ALL_STOCK_ITEMS;
				break;
			case CACHEID_USER_PROFIT:
				filename = DATA_PATH_USER_PROFIT;
				break;

			case CACHEID_EXTRA_INFO_NEWS:
				filename = DATA_PATH_EXTRA_INFO_NEWS;
				break;
			case CACHEID_EXTRA_INFO_ANNOUCEMENT:
				filename = DATA_PATH_EXTRA_INFO_ANNOUCEMENT;
				break;
			case CACHEID_EXTRA_INFO_REPORT:
				filename = DATA_PATH_EXTRA_INFO_REPORT;
				break;
			case CACHEID_EXTRA_INFO_MONEY:
				filename = DATA_PATH_EXTRA_INFO_MONEY;
				break;
			case CACHEID_EXTRA_INFO_SUMMARY:
				filename = DATA_PATH_EXTRA_INFO_SUMMARY;
				break;
			case CACHEID_EXTRA_INFO_FINANCE:
				filename = DATA_PATH_EXTRA_INFO_FINANCE;
				break;
			case CACHEID_EXTRA_INFO_INDEX_DETAIL:
				filename = DATA_PATH_EXTRA_INFO_INDEX_DETAIL;
				break;
			case CACHEID_STOCK_STRATEGY_LIST:
				filename = DATA_PATH_STOCK_STRATEGY_LIST;
				break;
			case CACHEID_QUOTATIONS_INDEX:
				filename = DATA_PATH_QUOTATIONS_INDEX;
				break;
			case CACHEID_QUOTATIONS_INDUSTRY:
				filename = DATA_PATH_QUOTATIONS_INDUSTRY;
				break;
			case CACHEID_QUOTATIONS_RISE_INFO:
				filename = DATA_PATH_QUOTATIONS_RISE_INFO;
				break;
			case CACHEID_QUOTATIONS_HOT_STOCK:
				filename = DATA_PATH_QUOTATIONS_HOT_STOCK;
				break;
			case CACHEID_QUOTATIONS_RZRQ:
				filename = DATA_PATH_QUOTATIONS_RZRQ;
				break;
			case CACHEID_QUOTATIONS_AH:
				filename = DATA_PATH_QUOTATIONS_AH;
				break;
			case CACHEID_QUOTATIONS_TOPIC:
				filename = DATA_PATH_QUOTATIONS_TOPIC;
				break;
			case CACHEID_TOPIC_FAVORITE:
				filename = DATA_PATH_TOPIC_FAVORITE;
				break;
			case CACHEID_DISCOVERY_TODAY_TOPIC:
				filename = DATA_PATH_DISCOVERY_TODAY_TOPIC;
				break ;
			case CACHEID_DISCOVERY_NOTICE:
				filename = DATA_PATH_DISCOVERY_NOTICE;
				break ;
			case CACHEID_DISCOVERY_60DAYS_HOT:
				filename = DATA_PATH_DISCOVERY_60DAYS_HOT;
				break ;
			case CACHEID_DISCOVERY_20DAYS_HOT:
				filename = DATA_PATH_DISCOVERY_20DAYS_HOT;
				break ;
			case CACHEID_DISCOVERY_5DAYS_HOT:
				filename = DATA_PATH_DISCOVERY_5DAYS_HOT;
				break ;
			case CACHEID_DISCOVERY_RZJM_RCJE:
				filename = DATA_PATH_DISCOVERY_RZJM_RCJE;
				break ;
			case CACHEID_DISCOVERY_RZYE_LTSZ:
				filename = DATA_PATH_DISCOVERY_RZYE_LTSZ;
				break ;
			case CACHEID_DISCOVERY_DRRZJMRE:
				filename = DATA_PATH_DISCOVERY_DRRZJMRE;
				break ;
			case CACHEID_COMBINATION_SMART:
				filename = DATA_PATH_COMBINATION_SMART;

			case CACHEID_PREPARE_STOCK_LIST:
				filename = DATA_PATH_PREPARE_STOCK;
				break ;

		}
		return filename;
	}

	private static void writeFile(String filepath, JSONObject jsonObject){
		try {
			File file = new File(filepath);

			//FileOutputStream fos =  context.openFileOutput(getCacheFilePath(cacheId), Context.MODE_PRIVATE);	// 传路径会报错,所以采用路径方式
			FileOutputStream fos = new FileOutputStream(file);

			fos.write(jsonObject.toString().getBytes("UTF8"));
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static JSONObject readFile(String filepath){
		int len = 1024;
		byte[] buffer = new byte[len];
		try {
			File file = new File(filepath);

			//FileInputStream fis = context.openFileInput(getCacheFilePath(cacheId));
			FileInputStream fis = new FileInputStream(file);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int nrb = fis.read(buffer, 0, len); // read up to len bytes
			while (nrb != -1) {
				baos.write(buffer, 0, nrb);
				nrb = fis.read(buffer, 0, len);
			}
			buffer = baos.toByteArray();
			fis.close();
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject jsonObject = null;
		try {
			String json = new String( buffer ,"UTF-8");
			jsonObject = new JSONObject(json);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return jsonObject;
	}

	private static void deleteDir(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}

		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}

			for (int i = 0; i < childFiles.length; i++) {
				deleteDir(childFiles[i]);
			}
			file.delete();
		}
	}


	/**
	 * 保存JSON对象至文件
	 * @param context
	 * @param cacheId		缓存ID，参见头部
	 * @param jsonObject
	 */
	public static void saveToFile(Context context, int cacheId, JSONObject jsonObject) {
		String pathRoot = context.getFilesDir().getAbsolutePath()+DATA_PATH_ROOT;
		File dir = new File(pathRoot);
		//Log.e(TAG, "path：" + context.getFilesDir().getAbsolutePath()+DATA_PATH_ROOT);

		if(!dir.exists()){
			dir.mkdirs();
		}

		//Log.i(TAG, "JSON SaveToFile:"+cacheId);
		writeFile(pathRoot + getCacheFilePath(cacheId),  jsonObject);
	}

	/**
	 * 从文件中读取JSON对象
	 * @param context
	 * @param cacheId		缓存ID，参见头部
	 * @return
	 */
	public static JSONObject loadFromFile(Context context, int cacheId) {
		String pathRoot = context.getFilesDir().getAbsolutePath()+DATA_PATH_ROOT;

		return readFile(pathRoot + getCacheFilePath(cacheId));
	}

	/**
	 * 根据股票代码创建一个文件夹来存信息
	 * @param stockCode
	 * @param context
	 * @param cacheId
	 * @param jsonObject
	 */
	public static void saveToFile(String stockCode, Context context, int cacheId, JSONObject jsonObject){
		String pathRoot = context.getFilesDir().getAbsolutePath() + DATA_PATH_ROOT + stockCode +"/";
		File dir = new File(pathRoot);

		if(!dir.exists()){
			dir.mkdirs();
		}

		writeFile(pathRoot + getCacheFilePath(cacheId),  jsonObject);
	}

	/**
	 * 从股票代码文件夹中读取相关cacheId
	 * @param stockCode
	 * @param context
	 * @param cacheId
	 * @return
	 */
	public static JSONObject loadFromFile(String stockCode, Context context, int cacheId) {
		String pathRoot = context.getFilesDir().getAbsolutePath()+DATA_PATH_ROOT + stockCode +"/";

		return readFile(pathRoot + getCacheFilePath(cacheId));
	}



	/**
	 * 删除股票文件夹
	 * @param context
	 * @param stockCode
	 */
	public static void deleteStockDir(Context context, String stockCode){
		String pathRoot = context.getFilesDir().getAbsolutePath()+DATA_PATH_ROOT + stockCode +"/";
		File file = new File(pathRoot);
		deleteDir(file);
	}


	/**
	 * 判断文件是否存在
	 * @param context
	 * @param cacheId
	 * @return
	 */
	public static boolean isFileExist(Context context, int cacheId){
		String pathRoot = context.getFilesDir().getAbsolutePath()+DATA_PATH_ROOT;

		return new File(pathRoot + getCacheFilePath(cacheId)).exists();
	}
	/**
	 * 获取文件上次修改的时间，无此文件则返回0
	 * @param context
	 * @param cacheId
	 * @return
	 */
	public static long getFileLastModified(Context context, int cacheId){
		String pathRoot = context.getFilesDir().getAbsolutePath()+DATA_PATH_ROOT;
		File file = new File(pathRoot + getCacheFilePath(cacheId));
		if(!file.exists())
			return 0;

		return file.lastModified();
	}

}
