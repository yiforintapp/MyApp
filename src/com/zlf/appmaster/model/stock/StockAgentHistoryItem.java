package com.zlf.appmaster.model.stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class StockAgentHistoryItem {
	private static final String TAG = "StockAgentHistoryItem";
	private String mCode;
	private String mName;
	private int mStockNum;				// 	成交股数
	private float mStockPrice;			//  成交价格
	private int mOrderType;
	private int mTradingType;
	private int mStatus;
	private long mTime;
	
	private StockAgentHistoryItem(String code, String name, int stockNum, float stockPrice, int orderType,
								  int tradingType, int status, long time){
		mCode = code;
		mName = name;
		mStockNum = stockNum;				// 	成交股数
		mStockPrice = stockPrice;			//  成交价格
		mOrderType = orderType;
		mTradingType = tradingType;
		mStatus = status;
		mTime  = time;
	}
	
	public String getCode(){
		return mCode;
	}
	public String getName(){
		return mName;
	}
	public String getPriceFormat(){
		return new DecimalFormat("0.00").format(mStockPrice);
	}
	public String getNumFormat(){
		return String.valueOf(mStockNum);
	}
	
	public long getTime(){
		return mTime;
	}
	
	public int getTradingType(){
		return mTradingType;
	}
	public int getStatus(){
		return mStatus;
	}
	
	
	public static ArrayList<StockAgentHistoryItem> resolveJSONObjectArray(JSONObject reponse) throws JSONException {
		ArrayList<StockAgentHistoryItem> stockAgentHistoryItemArray = new ArrayList<StockAgentHistoryItem>();
		
		JSONObject data = reponse.getJSONObject("data");
		
		JSONObject jsonStockMap = null;
		try {
			 jsonStockMap = data.getJSONObject("stockMap");
		}
		 catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		if(jsonStockMap == null)	//	数据为空
			return null;
		 
		HashMap<String, String> stockMap = new HashMap<String, String>();
		// 获取股票列表
		for(Iterator iterator = jsonStockMap.keys(); iterator.hasNext();){
			String key = (String) iterator.next();
			String value =jsonStockMap.getString(key);
			stockMap.put(key, value);
			//Log.i(TAG, "resolveJSONObjectArray--->:"+key+"("+value+")");
		}
		
		JSONArray list = data.getJSONArray("list");
		int len = list.length();
		for(int i = 0; i < len; i++){
			JSONObject perObject = list.getJSONObject(i);
			String code = perObject.getString("stockId");
			String name = stockMap.get(code);
			int stockNum = perObject.getInt("stockNum");			
			float stockPrice = (float) perObject.getDouble("stockPrice");
			int orderType = perObject.getInt("orderType");
			int tradingType = perObject.getInt("tradingType");
			int status = perObject.getInt("status");
			JSONObject timeObject = perObject.getJSONObject("addTime");
			long time = Long.valueOf(timeObject.getString("time"));
			
			StockAgentHistoryItem stockAgentHistoryItem = 
					new StockAgentHistoryItem(code, name, stockNum, stockPrice, orderType, tradingType, status, time);
			
			stockAgentHistoryItemArray.add(stockAgentHistoryItem);
		}
		
		return stockAgentHistoryItemArray;
	}
	
}
