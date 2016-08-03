package com.zlf.appmaster.model.industry;

import com.zlf.appmaster.model.stock.StockTradeInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class IndustryInfo {
	private String mIndustryID;
	private String mName;
	private StockTradeInfo mLedStock; 		// 本行业领头（领涨/领跌）的股票ID
	
	private double mPercent = 0.0;			// 涨跌幅
    private boolean bIsLedUp;               //领涨/领跌

    public boolean isIsLedUp() {
        return bIsLedUp;
    }

    public void setIsLedUp(boolean isLedUp) {
        this.bIsLedUp = isLedUp;
    }

    public String getIndustryID() {
		return mIndustryID;
	}

	public void setIndustryID(String mIndustryID) {
		this.mIndustryID = mIndustryID;
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

	public double getPercent() {
		return mPercent;
	}

	public void setPercent(double mPercent) {
		this.mPercent = mPercent;
	}
	
	public String getPercentFormat() {
		// 开盘前数据清理阶段
		if(mLedStock.getStockStatus() == StockTradeInfo.STATUS_OPEN_PREPARE && mLedStock.getNowPrice() == 0){
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
	
	
	public static ArrayList<IndustryInfo> resolveJSONObjectArray(JSONObject response) throws JSONException {
		ArrayList<IndustryInfo> industryLedArray = new ArrayList<IndustryInfo>();
		
		JSONObject data = response.getJSONObject("data");
		JSONObject jsonQuotationInfo = data.getJSONObject("quotations");
		JSONArray jsonArray = data.getJSONArray("industryList");
		int length = jsonArray.length();
		for(int i = 0; i<length; i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			IndustryInfo industryInfo = new IndustryInfo();
			industryInfo.setName(jsonObject.getString("IndustryName"));
			industryInfo.setPercent(jsonObject.getDouble("IndustryDown"));
			industryInfo.setIndustryID(jsonObject.getString("IndustryId"));
			
			String stockID = jsonObject.getString("stockId");
			JSONObject jsonStockInfo = jsonQuotationInfo.getJSONObject(stockID);
			industryInfo.setLedStock(StockTradeInfo.resolveNameJsonObject(jsonStockInfo));
			
			industryLedArray.add(industryInfo);
		}
		return industryLedArray;
	}
	
}
