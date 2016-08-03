package com.zlf.appmaster.bean;

import com.zlf.appmaster.model.stock.StockTradeInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 *
 * Created by Deping Huang on 2014/11/29.
 */
public class StrategyStockItem {
    private String mStockCode;
    private StockTradeInfo mStockTradeInfo;
    private String mIntroduction;

    public StrategyStockItem(String stockCode){
        mStockCode = stockCode;
    }

    public String getStockCode(){
        return mStockCode;
    }

    public StockTradeInfo getStockTradeInfo() {
        return mStockTradeInfo;
    }

    public void setStockTradeInfo(StockTradeInfo stockTradeInfo) {
        this.mStockTradeInfo = stockTradeInfo;
    }

    public String getIntroduction() {
        return mIntroduction;
    }

    public void setIntroduction(String introduction) {
        this.mIntroduction = introduction;
    }


    public static ArrayList<StrategyStockItem> resolveJSONObject(JSONObject jsonObject) throws JSONException {
        ArrayList<StrategyStockItem> items = new ArrayList<StrategyStockItem>();
        JSONObject data = jsonObject.getJSONObject("data");

        // 股票列表
        JSONArray jsonStockIDs = data.getJSONArray("stockIds");
        JSONObject jsonStockQuotations = data.getJSONObject("stockQuotations");

        JSONObject jsonStockIntroduction = null;
        try {
            jsonStockIntroduction  = data.getJSONObject("stockCelue");
        }
        catch (JSONException e){

        }



        int stockLen = jsonStockIDs.length();
        for (int i = 0; i < stockLen; i++){
            String stockCode = jsonStockIDs.getString(i);
            StrategyStockItem item = new StrategyStockItem(stockCode);

            // 解析该股票的行情
            StockTradeInfo stockTradeInfo = StockTradeInfo.resolveSummaryJsonObject(jsonStockQuotations.getJSONObject(stockCode));
            item.setStockTradeInfo(stockTradeInfo);
            // 解析该股票的说明
            if (null != jsonStockIntroduction){
                String introduction = jsonStockIntroduction.getString(stockCode);
                item.setIntroduction(introduction);
            }

            items.add(item);
        }

        return items;
    }
}
