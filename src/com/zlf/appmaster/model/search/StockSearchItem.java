package com.zlf.appmaster.model.search;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huang on 2015/1/8.
 */
public class StockSearchItem {
    private String mStockCode;
    private String mStockName;
    private int mType;                  // 类型 股票/指数
    private int mSearchCount;          // 搜索次数
    private long mUpdateTime;
    private boolean isFavorite;         // 是否为自选股


    public String getStockCode() {
        return mStockCode;
    }

    public void setStockCode(String stockCode) {
        this.mStockCode = stockCode;
    }

    public String getStockName() {
        return mStockName;
    }

    public void setStockName(String stockName) {
        this.mStockName = stockName;
    }

    public int getSearchCount() {
        return mSearchCount;
    }

    public void setSearchCount(int searchCount) {
        this.mSearchCount = searchCount;
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.mUpdateTime = updateTime;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    /**
     * 从最热个股信息中解析股票项
     * @param response
     * @return
     */
    public static List<StockSearchItem> resolveByStockHottest(JSONObject response) throws JSONException {
        List<StockSearchItem> stockSearchItems = new ArrayList<StockSearchItem>();
        JSONObject data = response.getJSONObject("data");
        JSONArray stockIds = data.getJSONArray("stcokIds");
        JSONObject quotations = data.getJSONObject("quotations");
        int len = stockIds.length();
        if (len > 3){
            len = 3;    // 最多只取3个
        }
        for (int i = 0; i<len; i++){
            StockSearchItem item = new StockSearchItem();

            String stockID = stockIds.getString(i);
            JSONObject perJSON = quotations.getJSONObject(stockID);

            item.setStockCode(stockID);
            item.setStockName(perJSON.getString("HQZQJC"));

            stockSearchItems.add(item);
        }

        return stockSearchItems;
    }
}
