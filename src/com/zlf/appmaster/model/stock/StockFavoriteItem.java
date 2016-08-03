package com.zlf.appmaster.model.stock;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class StockFavoriteItem implements Serializable,Comparable<StockFavoriteItem> {
    public static final int TYPE_STOCK = StockItem.CODE_TYPE_STOCK;
    public static final int TYPE_INDEX = StockItem.CODE_TYPE_INDEX;
    private String mKey;            // 键标识,用于区分股票和其它类型
	private String mStockCode;		// 股票代码
	private String mStockName;		// 股票名称
    private int mType;              // 类型（股票0或指数1）// 必须初始化时构造
    private boolean isStockSuspended;       // 是否停牌
    private int mRiseInfo;          // 涨跌信息
    private String mCurPriceFormat; // 当前价
    private String mCurPercentFormat;   // 当前涨幅
    private String mIndustryID;     //  行业ID
    private String mIndustryName;   // 行业名称
    private int mSortCode;          // 排序码


	private long mAddTime;			// 添加时间
    private long mUpdateTime;       // 更新时间

    public StockFavoriteItem(String stockCode, int type){
        init(stockCode, type);
    }
    public StockFavoriteItem(String stockCode){
        init(stockCode, TYPE_STOCK);
    }

    private void init(String stockCode, int type){
        mStockCode = stockCode;
        mType = type;

        if (type == TYPE_INDEX){
            mKey = "index_" + stockCode;
        }
        else {
            mKey = stockCode;
        }
    }

    public String getStockCode() {
		return mStockCode;
	}
	public void setStockCode(String mStockCode) {
		this.mStockCode = mStockCode;
	}
	public long getAddTime() {
		return mAddTime;
	}
	public void setAddTime(long mAddTime) {
		this.mAddTime = mAddTime;
	}
    public String getStockName() {
        return mStockName;
    }
    public void setStockName(String mStockName) {
        this.mStockName = mStockName;
    }

    public int getType() {
        return mType;
    }

//    public void setType(int type) {
//        this.mType = type;
//    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.mUpdateTime = updateTime;
    }


    public int getRiseInfo() {
        return mRiseInfo;
    }

    public void setRiseInfo(int riseInfo) {
        this.mRiseInfo = riseInfo;
    }

    public String getCurPriceFormat() {
        return mCurPriceFormat;
    }

    public void setCurPriceFormat(String curPriceFormat) {
        this.mCurPriceFormat = curPriceFormat;
    }

    public String getCurPercentFormat() {
        return mCurPercentFormat;
    }

    public void setCurPercentFormat(String curPercentFormat) {
        this.mCurPercentFormat = curPercentFormat;
    }

    public boolean isStockSuspended() {
        return isStockSuspended;
    }

    public void setStockSuspended(boolean isStockSuspended) {
        this.isStockSuspended = isStockSuspended;
    }


    public String getIndustryID() {
        return mIndustryID;
    }

    public void setIndustryID(String industryID) {
        this.mIndustryID = industryID;
    }

    public String getIndustryName() {
        return mIndustryName;
    }

    public void setIndustryName(String industryName) {
        this.mIndustryName = industryName;
    }

    public String getKey(){
        return mKey;
    }
    public void copyValue(StockFavoriteItem item){
        if (null == item) return;

        this.mStockName = item.mStockName;
        this.mRiseInfo = item.mRiseInfo;
        this.mType = item.mType;
        this.mCurPercentFormat = item.mCurPercentFormat;
        this.mCurPriceFormat = item.mCurPriceFormat;
        this.isStockSuspended = item.isStockSuspended;
        this.mSortCode = item.mSortCode;

    }

    public void copyValue(StockTradeInfo item){
        if (null == item) return;

        this.setStockName(item.getName());
        this.setRiseInfo(item.getRiseInfo());
        this.setCurPercentFormat(item.getCurPercentFormat());
        this.setCurPriceFormat(item.getCurPriceFormat());
        this.setStockSuspended(item.isStockSuspended());
    }

    public void copyValue(StockIndex item){
        if (null == item) return;

        this.setStockName(item.getName());
        this.setRiseInfo(item.getRiseInfo());
        this.setCurPercentFormat(item.getCurPercentPrompt());
        this.setCurPriceFormat(item.getCurPriceFormat());
    }



    public static HashMap<String, StockFavoriteItem> resolveArrayFromJson(JSONObject response) throws JSONException {
        HashMap<String, StockFavoriteItem> itemArray = new HashMap<String, StockFavoriteItem>();

        JSONObject data = response.getJSONObject("data");
        try{
            JSONObject stockJson = data.getJSONObject("stock");
            Iterator stockKey = stockJson.keys();
            while (stockKey.hasNext()){
                String stockCode = (String)stockKey.next();
                StockTradeInfo stockTradeInfo = StockTradeInfo.resolveSummaryJsonObject(stockJson.getJSONObject(stockCode));

                StockFavoriteItem item = new StockFavoriteItem(stockTradeInfo.getCode());
                item.copyValue(stockTradeInfo);
                itemArray.put(item.getKey(), item);
            }

        }catch (JSONException e){

        }

        try {
            JSONObject indexJson = data.getJSONObject("index");
            Iterator indexKey = indexJson.keys();
            while (indexKey.hasNext()) {
                String indexCode = (String) indexKey.next();
                StockIndex stockIndex = StockIndex.resolveIndexJsonSimpleObject(indexJson.getJSONObject(indexCode));

                StockFavoriteItem item = new StockFavoriteItem(stockIndex.getCode(), TYPE_INDEX);
                item.copyValue(stockIndex);
                itemArray.put(item.getKey(), item);
            }
        }catch (JSONException e){

        }

		return itemArray;
	}

    /**
     * 解析他人自选股
     * @return
     */
    public static ArrayList<StockFavoriteItem> resolveHisSelfStock(JSONObject response){
        ArrayList<StockFavoriteItem> items = new ArrayList<StockFavoriteItem>();

        //:{"data":[{"StockCode":"002228"}],"msg":"","time":1429581715664,"code":0}
        try {
            JSONArray stockArray = response.optJSONArray("data");
            if (stockArray != null){
                for (int i=0;i<stockArray.length();i++){
                    JSONObject stock = stockArray.getJSONObject(i);
                    StockFavoriteItem item;
                    String stockCode = stock.optString("StockCode");

                    if (!TextUtils.isEmpty(stockCode)){
                        item = new StockFavoriteItem(stockCode,TYPE_STOCK);
                    }else {//TODO indexCode
                        String indexCode = stock.optString("IndexCode");
                        item = new StockFavoriteItem(indexCode,TYPE_INDEX);
                    }

                    items.add(item);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return items;
    }

    public int getSortCode() {
        return mSortCode;
    }

    public void setSortCode(int sortCode) {
        this.mSortCode = sortCode;
    }

    @Override
    public int compareTo(StockFavoriteItem stockFavoriteItem) {
        return mSortCode - stockFavoriteItem.mSortCode;
    }
}
