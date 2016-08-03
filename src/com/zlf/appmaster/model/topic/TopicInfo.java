package com.zlf.appmaster.model.topic;

import com.zlf.appmaster.model.stock.StockTradeInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicInfo {
	private String mTopicID;
	private String mName;
	private StockTradeInfo mLedStock; 		// 本行业领头（领涨/领跌）的股票ID

	private double mPercent = 0.0;			// 涨跌幅
    private boolean bIsLedUp;               //领涨/领跌

	private MarketStatus mMarketStatus;		// 股票行情状态

    public String getTopicID() {
		return mTopicID;
	}

	public void setTopicID(String mIndustryID) {
		this.mTopicID = mIndustryID;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public StockTradeInfo getLedStock() {
		return mLedStock;
	}

	public void setLedStock(StockTradeInfo ledStock) {
		this.mLedStock = ledStock;
	}

	public MarketStatus getMarketStatus() {
		return mMarketStatus;
	}

	public void setMarketStatus(MarketStatus marketStatus) {
		this.mMarketStatus = marketStatus;
	}

	public double getPercent() {
		return mPercent;
	}

	public void setPercent(double mPercent) {
		this.mPercent = mPercent;
	}
	
	public String getPercentFormat() {
		// 开盘前数据清理阶段
		if(mMarketStatus.getOpenStatus() == MarketStatus.STATUS_OPEN_PREPARE){
			return	StockTradeInfo.DEFAULT_PERCENT_FORMAT;
		}
		String symbol="";//正负号
		DecimalFormat df = new DecimalFormat("0.00");
		if(mPercent > 0){
			symbol = "+";
		}
		return symbol+df.format(mPercent*100) + "%";
	}
	

	/**
	 * 得到股票涨跌情况
	 * @return   1 涨 、0 平、 -1 跌
	 */
	public int getRiseInfo(){ 
		int ret = 0;
		if(mPercent	> 0)
			ret = 1;
		else if(mPercent < 0)
			ret = -1;
		
		return ret;
	}
	
	
	public static List<TopicInfo> resolveJSONObjectArray(JSONObject response) throws JSONException {
		List<TopicInfo> topicInfos = new ArrayList<TopicInfo>();
		
		JSONObject data = response.getJSONObject("data");
		JSONArray jsonArray = data.getJSONArray("Topics");
		MarketStatus marketStatus = MarketStatus.resolveJson(data.getJSONObject("MarketStatus"));
		int length = jsonArray.length();
		for(int i = 0; i<length; i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			TopicInfo topicInfo = new TopicInfo();
			topicInfo.setName(jsonObject.getString("Name"));
			topicInfo.setPercent(jsonObject.getDouble("Range"));
			topicInfo.setTopicID(jsonObject.getString("Id"));

			StockTradeInfo stockTradeInfo = new StockTradeInfo();
			stockTradeInfo.setCode(jsonObject.getString("StockCode"));
			stockTradeInfo.setName(jsonObject.optString("StockName"));
			topicInfo.setLedStock(stockTradeInfo);

			topicInfo.setMarketStatus(marketStatus);
			topicInfos.add(topicInfo);
		}

		return topicInfos;
	}


	public static Map<String, TopicInfo> resolveJSONObjectArrayGetMap(JSONObject response) throws JSONException {
		Map<String, TopicInfo> topicInfoMap = new HashMap<String, TopicInfo>();

		JSONObject data = response.getJSONObject("data");
		JSONArray jsonArray = data.getJSONArray("Topics");
		MarketStatus marketStatus = MarketStatus.resolveJson(data.getJSONObject("MarketStatus"));
		int length = jsonArray.length();
		for(int i = 0; i<length; i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			TopicInfo topicInfo = new TopicInfo();
			topicInfo.setName(jsonObject.getString("Name"));
			topicInfo.setPercent(jsonObject.getDouble("Range"));
			topicInfo.setTopicID(jsonObject.getString("Id"));

			StockTradeInfo stockTradeInfo = new StockTradeInfo();
			stockTradeInfo.setCode(jsonObject.getString("StockCode"));
			stockTradeInfo.setName(jsonObject.optString("StockName"));
			topicInfo.setLedStock(stockTradeInfo);

			topicInfo.setMarketStatus(marketStatus);
			topicInfoMap.put(topicInfo.getTopicID(), topicInfo);
		}

		return topicInfoMap;
	}

}
