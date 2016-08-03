package com.zlf.appmaster.model.stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class StockAgentItem {
	private static final String TAG = "StockAgentItem";
	private String mCode;
	private String mName;
	private int mStockNum;				// 	成交股数
	private float mStockPrice;			//  成交价格
	private int mTradeType;
	private String mTradeId;
	
	private StockAgentItem(String code, String name,
						   int stockNum, float stockPrice, int tradeType, String tradeId) {
		mCode = code;
		mName = name;
		mStockNum = stockNum;	
		mStockPrice = stockPrice;
		mTradeType = tradeType;
		mTradeId = tradeId;
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
	
	public int getTradeType(){
		return mTradeType;
	}
	
	public String getTradeId(){
		return mTradeId;
	}
	
	public static ArrayList<StockAgentItem> resolveJSONObjectArray(JSONObject response) throws JSONException {
		ArrayList<StockAgentItem> stockAgentItemArray = new ArrayList<StockAgentItem>();
		
		JSONArray data = response.getJSONArray("data");
		int len = data.length();
		
		for(int i = 0; i < len; i++) {
			JSONObject perObject = data.getJSONObject(i);
			String code = perObject.getString("stockId");
			String name = perObject.getString("stockName");
			int stockNum = perObject.getInt("stockNum");
			float stockPrice = (float) perObject.getDouble("stockPrice");
			int tradeType = perObject.getInt("tradeType");
			String tradeId = perObject.getString("tradeId");
			StockAgentItem stockAgent = new StockAgentItem(code, name, stockNum, stockPrice, tradeType, tradeId);
			stockAgentItemArray.add(stockAgent);
		}
		
		return stockAgentItemArray;
	}
}
