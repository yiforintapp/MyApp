package com.zlf.appmaster.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * 个股股东相关信息
 * @author Deping Huang
 *
 */
public class StockShares {
	private final static String DEFAULT_DATA = "--";
	private int mTotalNum;		// 总股本
	private int mCircleNum;		// 流通股
	
	private String mTopHolderResource;			// 十大股东信息来源
	private String mFundHolderResource;			// 基金股东信息来源
	
	private int mHoldersNum;	// 股东人数
	private int mPerHoldersNum;	// 平均持股
	
	
	// 大股东
	private ArrayList<StockShareHolder> mTopHolderArray;
	
	// 基金持股
	private ArrayList<StockShareHolder> mFundHolderArray;
	
	public static class StockShareHolder {
		private String mName;			// 名字
		private float mChangeRange;		// 变化幅度
		private int mChange;			// 变化
		private float mProportion;			// 占比
		
		private String mDataResource;		// 数据来源
		
		public String getName() {
			return mName;
		}
		public void setName(String mName) {
			this.mName = mName;
		}

		public void setChangeRange(float range) {
			this.mChangeRange = range;
		}
		public String getChangeFormat() {
			String ret = "未变";
			if(mChange > 0){	
				if(mChangeRange == 0){		// 数量大于0，但是幅度未变化则为新增数据
					ret = "新进";
				}
				else {
					ret = "增持";
				}
			}
			else if(mChange < 0){
				ret = "减持";
			}
			
			return ret;
		}
		public void setChange(int mChange) {
			this.mChange = mChange;
		}
		public String getDataResource() {
			return mDataResource;
		}
		public void setDataResource(String mDataResource) {
			this.mDataResource = mDataResource;
		}
		
		public static ArrayList<StockShareHolder> resolveJSONObjectArray(JSONArray arrayList) throws JSONException {
			ArrayList<StockShareHolder> topHolderArray = new ArrayList<StockShareHolder>();

			int len = arrayList.length();
			for(int i = 0; i < len; i++){
				StockShareHolder stockShareHolder = new StockShares.StockShareHolder();
				JSONObject perStockGD = arrayList.getJSONObject(i);
				
				stockShareHolder.setName(perStockGD.getString("sHList"));
				stockShareHolder.setChangeRange((float)perStockGD.getDouble("holdSumChangeRate"));
				stockShareHolder.setChange(perStockGD.getInt("holdSumChange"));
				stockShareHolder.setProportion((float)perStockGD.getDouble("pCTOfTotalShares"));
				stockShareHolder.setDataResource(perStockGD.getString("infoSource"));
				
				topHolderArray.add(stockShareHolder);
			}
			return topHolderArray;
			
		}
		public String getProportionFormat() {
			if(mProportion < 0)
				return new DecimalFormat("0.000").format(mProportion) +"%";
			else
				return new DecimalFormat("0.00").format(mProportion) +"%";
		}
		public void setProportion(float mProportion) {
			this.mProportion = mProportion;
		}
	}

	
	public static StockShares resolveJSONObject(JSONObject data) throws JSONException {
		StockShares stockShares = new StockShares();
		
		// 股东信息
		JSONObject stockGB = data.getJSONObject("stockGB");
		stockShares.setTotalNum(stockGB.getInt("totalShares"));
		stockShares.setCircleNum(stockGB.getInt("nonRestrictedShares"));
		
		// 大股东列表
		JSONObject stockGD = data.getJSONObject("stockGD");
		stockShares.setTopHolderDataSource(stockGD.getString("infoSource"));
		stockShares.setTopHolderArray(StockShareHolder.resolveJSONObjectArray(stockGD.getJSONArray("gdList")));
	
		
		return stockShares;
	}

	public String getTotalNumFormat() {
		return FormatStockNumStr(mTotalNum/10000);
	}

	public void setTotalNum(int mTotalNum) {
		this.mTotalNum = mTotalNum;
	}

	public String getCircleNumFormat() {
		return FormatStockNumStr(mCircleNum/10000);
	}

	public void setCircleNum(int mCircleNum) {
		this.mCircleNum = mCircleNum;
	}
	
	public String getTopHolderDataSource(){
		return mTopHolderResource;
	}
	public void setTopHolderDataSource(String dataSrouce){
		mTopHolderResource = dataSrouce;
	}
	
	
	
	public String getFundHolderDataSource(){
		return mFundHolderResource;
	}
	public void setFundHolderDataSource(String dataSrouce){
		mFundHolderResource = dataSrouce;
	}

	public ArrayList<StockShareHolder> getTopHolderArray() {
		return mTopHolderArray;
	}

	public void setTopHolderArray(ArrayList<StockShareHolder> mTopHolderArray) {
		this.mTopHolderArray = mTopHolderArray;
	}

	public ArrayList<StockShareHolder> getFundHolderArray() {
		return mFundHolderArray;
	}

	public void setFundHolderArray(ArrayList<StockShareHolder> mFundHolderArray) {
		this.mFundHolderArray = mFundHolderArray;
	}
	
	public static String FormatStockNumStr(float data){
		float absFinance = Math.abs(data);
		
		if(absFinance == 0)
			return DEFAULT_DATA;
		
		return new DecimalFormat("0.00").format(data)+"万股";
	}
}
