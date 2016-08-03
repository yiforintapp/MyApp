package com.zlf.appmaster.model.stock;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class StockMyPosition {
	private String mCode;
	private int mHoldNum;	// 持仓股数
	private double mCostPrice;	// 价格
	
	private StockTradeInfo mStockTradeInfo;	// 当前的股票交易信息
	
	private StockMyPosition(String code, int holdNum, double costPrice){
		mCode		= code;
		mHoldNum	= holdNum;
		mCostPrice	= costPrice;
	}
	public String getCode(){
		return mCode;
	}
	public String getCostPrice(){
		return new DecimalFormat("0.00").format(mCostPrice);
	}
	public String getHoldNum() {
		return String.valueOf(mHoldNum);
	}
	//浮动盈亏 = (现价-平均成本价)/平均成本价
	public String getProfitFormat(){
		
		String ret = StockTradeInfo.DEFAULT_DATA_FORMAT;
		if(mStockTradeInfo != null){
			ret = new DecimalFormat("0.00").format(getProfit()) + "%";
		}
		return ret;
	}
	
	public float getProfit(){
		
		// 开盘前或停牌的价格用昨日收盘价来计算
		if(((mStockTradeInfo.getStockStatus() == StockTradeInfo.STATUS_OPEN_PREPARE || mStockTradeInfo.isStockSuspended())
				&& mStockTradeInfo.getNowPrice() == 0)){
			return (float) ((mStockTradeInfo.getYestodayPrice() - mCostPrice)/mCostPrice*100);
		}
		
		return (float) ((mStockTradeInfo.getNowPrice() - mCostPrice)/mCostPrice*100);
	}
	

	/**
	 * 得到股票涨跌情况
	 * @return   1 涨 、0 平、 -1 跌
	 */
	public int getRiseInfo(){ 
		int ret = 0;
		float riseInfo = getProfit();
		if(riseInfo	> 0)
			ret = 1;
		else if(riseInfo < 0)
			ret = -1;
		
		return ret;
	}
	
	public void setStockTradeInfo(StockTradeInfo stockTradeInfo){
		mStockTradeInfo = stockTradeInfo;
	}
	public StockTradeInfo getStockTradeInfo(){
		return mStockTradeInfo;
	}
	
	
	public static StockMyPosition resolveJSONObject(JSONObject obj) throws JSONException {
		String code = obj.getString("stockId");
		int holdNum = obj.getInt("holdNum");
		double costPrice = obj.getDouble("costPrice");
		return  new StockMyPosition(code, holdNum, costPrice);
	}
	
	
	
}
