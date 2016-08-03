package com.zlf.appmaster.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class StockMoney {
	private double mMainIn;			//	主力流入
	private double mMainOut;		// 	主力流出
	private double mMinorIn;		// 	散户流入
	private double mMinorOut;		//	散户流出
	private long mDate;				// 	日期
	
	public double getMainIn() {
		return mMainIn;
	}
	public void setMainIn(double mMainIn) {
		this.mMainIn = mMainIn;
	}
	public double getMainOut() {
		return mMainOut;
	}
	public void setMainOut(double mMainOut) {
		this.mMainOut = mMainOut;
	}
	public double getMinorIn() {
		return mMinorIn;
	}
	public void setMinorIn(double mMinorIn) {
		this.mMinorIn = mMinorIn;
	}
	public double getMinorOut() {
		return mMinorOut;
	}
	public void setMinorOut(double mMinorOut) {
		this.mMinorOut = mMinorOut;
	}
	public long getDate() {
		return mDate;
	}
	public void setDate(long mDate) {
		this.mDate = mDate;
	}
	
	/**
	 * 获取主力增减量(单位：万元)
	 * @return
	 */
	public double getMainRegulation(){
		return (mMainIn - mMainOut)/10000;
	}
	
	
	public static ArrayList<StockMoney> resolveJSONObjectArray(JSONObject response) throws JSONException {
		ArrayList<StockMoney> stcokMoneyArray = new ArrayList<StockMoney>();
		JSONArray arrayList = response.getJSONArray("data");
		int len = arrayList.length();
		for(int i = 0; i < len; i++){
			StockMoney stockMoney = new StockMoney();
			JSONObject perStock = arrayList.getJSONObject(i);
			
			stockMoney.setMainIn(perStock.getDouble("mainForceInflow"));
			stockMoney.setMainOut(perStock.getDouble("mainForceOutflow"));
			stockMoney.setMinorIn(perStock.getDouble("nonMainInflow"));
			stockMoney.setMinorOut(perStock.getDouble("nonMainOutflow"));
			
			stockMoney.setDate(perStock.getLong("marketDay"));
			stcokMoneyArray.add(stockMoney);
		}
		return stcokMoneyArray;
	}
	
}
