package com.zlf.appmaster.bean;

import com.zlf.appmaster.model.stock.StockTradeInfo;

import java.util.ArrayList;
import java.util.List;

public class StockGroup {
	private String mGroupName;
	private String mGroupID;
	private List<StockTradeInfo> mStockTradeInfoArray = new ArrayList<StockTradeInfo>();
	
	public String getGroupName() {
		return mGroupName;
	}
	public void setGroupName(String mGroupName) {
		this.mGroupName = mGroupName;
	}
	public String getGroupID() {
		return mGroupID;
	}
	public void setGroupID(String mGroupID) {
		this.mGroupID = mGroupID;
	}
	
	public void setStockTraceInfoArray(List<StockTradeInfo> stockTradeInfoArray){
		mStockTradeInfoArray = stockTradeInfoArray;
	}
	
	/**
	 * 填充list时用
	 * @return
	 */
	public int getItemCount() {
		return mStockTradeInfoArray.size() + 1;
	}
	
	public Object getItem(int position) {
		if (position == 0) { // 位置0相当于根结点
			return this;
		} else {
			return mStockTradeInfoArray.get(position - 1);
		}
	}
}
