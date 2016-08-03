package com.zlf.appmaster.bean;

import com.zlf.appmaster.model.stock.StockTradeInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 股票策略项
 * @author Deping Huang
 *
 */
public class StockStrategyItem {
	private String mStockCode;
	private StockTradeInfo mStockTradeInfo;
	private List<StrategyItem> mStrategyItems;			// 该股票拥有的策略项
	
	public StockStrategyItem(String stockCode){
		mStockCode = stockCode;
	}
	
	public String getStockCode() {
		return mStockCode;
	}

	public StockTradeInfo getStockTradeInfo() {
		return mStockTradeInfo;
	}
	public void setStockTradeInfo(StockTradeInfo mStockTradeInfo) {
		this.mStockTradeInfo = mStockTradeInfo;
	}
	public List<StrategyItem> getStrategyItems() {
		return mStrategyItems;
	}
	public void setStrategyItems(List<StrategyItem> mStrategyItems) {
		this.mStrategyItems = mStrategyItems;
	}
	
	
	public static ArrayList<StockStrategyItem> resolveJSONObject(JSONObject jsonObject) throws JSONException {
		ArrayList<StockStrategyItem> items = new ArrayList<StockStrategyItem>();

        JSONObject data = null;
        try{
             data = jsonObject.getJSONObject("data");
        }
        catch (JSONException e){

        }

        if(null == data){
            return items;
        }


        // 股票列表
        JSONArray jsonStockIDs = data.getJSONArray("stockIds");
        JSONObject jsonStockQuotations = data.getJSONObject("stockQuotations");
        JSONObject jsonStockStrategy = data.getJSONObject("stockCelue");
        int stockLen = jsonStockIDs.length();
        for (int i = 0; i < stockLen; i++){
            String stockCode = jsonStockIDs.getString(i);
            StockStrategyItem item = new StockStrategyItem(stockCode);

            // 解析该股票的行情
            StockTradeInfo stockTradeInfo = StockTradeInfo.resolveSummaryJsonObject(jsonStockQuotations.getJSONObject(stockCode));
            item.setStockTradeInfo(stockTradeInfo);
            // 解析该股票的策略
            JSONArray strategyArray = jsonStockStrategy.getJSONArray(stockCode);
            int strategyLen = strategyArray.length();
            List<StrategyItem> strategyItems = new ArrayList<StrategyItem>();
            for (int j = 0; j < strategyLen; j++){
                StrategyItem strategyItem = new StrategyItem(strategyArray.getString(j));

                strategyItems.add(strategyItem);
            }
            item.setStrategyItems(strategyItems);


            items.add(item);
        }

		return items;
	}
	
	
	
}
