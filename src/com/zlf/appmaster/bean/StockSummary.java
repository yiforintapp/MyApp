package com.zlf.appmaster.bean;

import android.text.TextUtils;

import com.zlf.appmaster.utils.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;


public class StockSummary {
	private final static String DEFAULT_DATA = "--";
	
	private String mCompanyName;			// 公司名称
	private long mMarketStartDate;			// 上市日期
	private double mMarketStartPrice;		// 上市价格
	private double mMarketStartNum;			// 发行数量(万股)
	private String mArea;					// 所属地区
	private String mIndustry;				// 所属行业
	private String mMainBussiness;			// 主营业务
	
	private StockShares mStockShares = null;
	
	public String getCompanyName() {
		return mCompanyName;
	}
	public void setCompanyName(String mCompanyName) {
		this.mCompanyName = mCompanyName;
	}
	public String getMarketStartDateFormat() {
		return TimeUtil.getYearAndDay(mMarketStartDate);
	}
	public void setMarketStartDate(long mMarketStartDate) {
		this.mMarketStartDate = mMarketStartDate;
	}
	public String getMarketStartPriceFormat() {
		return FormatSummaryStr((float)mMarketStartPrice);
	}
	public void setMarketStartPrice(double mMarketStartPrice) {
		this.mMarketStartPrice = mMarketStartPrice;
	}
	public String getMarketStartNumFormat() {
		return FormatStockNumStr((float)mMarketStartNum);
	}
	public void setMarketStartNum(double mMarketStartNum) {
		this.mMarketStartNum = mMarketStartNum;
	}
	public String getArea() {
		return FormatSummaryStr(mArea);
	}
	public void setArea(String mArea) {
		this.mArea = mArea;
	}
	public String getIndustry() {
		return FormatSummaryStr(mIndustry);
	}
	public void setIndustry(String mIndustry) {
		this.mIndustry = mIndustry;
	}
	public String getMainBussiness() {
		return FormatSummaryStr(mMainBussiness);
	}
	public void setMainBussiness(String mMainBussiness) {
		this.mMainBussiness = mMainBussiness;
	}
	public StockShares getStockShares() {
		return mStockShares;
	}
	public void setStockShares(StockShares mStockShares) {
		this.mStockShares = mStockShares;
	}
	/**
	 * 解析Json To StockSummary
	 * @throws JSONException
	 */
	public static StockSummary resolveJSONObject(JSONObject response) throws JSONException {
		StockSummary stockSummary = new StockSummary();
		JSONObject data = response.getJSONObject("data");
		

		// 公司简况
		JSONObject stockBaseMessageObj = data.getJSONObject("stockBaseMessage");
		try {
			stockSummary.setCompanyName(stockBaseMessageObj.getString("chiName"));
			stockSummary.setArea(stockBaseMessageObj.getString("province"));
			stockSummary.setIndustry(stockBaseMessageObj.getString("industryName"));
			stockSummary.setMainBussiness(stockBaseMessageObj.getString("businessMajor"));
			
			stockSummary.setMarketStartPrice(stockBaseMessageObj.getDouble("issuePrice"));
			stockSummary.setMarketStartNum(stockBaseMessageObj.getDouble("issueShares"));
			
			stockSummary.setMarketStartDate(stockBaseMessageObj.getLong("listedDate"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 股东信息
		stockSummary.setStockShares(StockShares.resolveJSONObject(data));
		
		return stockSummary;
	}
	
	public static String FormatStockNumStr(float data){
		float absFinance = Math.abs(data);
		
		if(absFinance == 0)
			return DEFAULT_DATA;
		
		return new DecimalFormat("0.00").format(data)+"万股";
	}

	
	public static String FormatSummaryStr(float data){
		
		float absFinance = Math.abs(data);
		
		if(absFinance == 0)
			return DEFAULT_DATA;
		
		return new DecimalFormat("0.00").format(data);
	}
	
	public static String FormatSummaryStr(String data){
		
		if(TextUtils.isEmpty(data))
			return DEFAULT_DATA;
		
		return data;
	}
}
