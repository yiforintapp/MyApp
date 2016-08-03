/*
 * Copyright zh.weir.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zlf.appmaster.model.stock;

import com.zlf.appmaster.utils.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

;


/**
 * 股票信息
 * 
 */
public class StockTradeInfo implements Serializable,Comparable<StockTradeInfo> {
	public static final int STATUS_NORMAL = 0;			//	正常
	public static final int STATUS_STOP = 1;			//  停牌
	public static final int STATUS_OPEN_PREPARE = 2;	//  开盘前系统信息清理
	public static final int STATUS_LIMIT_UP = 3;		//  涨停
	public static final int STATUS_LIMIT_DOWN = 4;		//  跌停
	public static final int STATUS_TRADING_HALT = 5;    //  trading halt 交易中临时停牌
	
	public static final int MARKET_STATUS_NORMAL = 0;							// 正常交易时段
	public static final int MARKET_STATUS_CLOSE = 1;							// 休市
	public static final int MARKET_STATUS_NOT_SERVICE_TIME = 2;					// 当天非交易时段
	
	public static final String DEFAULT_PERCENT_FORMAT = "--";
	public static final String DEFAULT_DATA_FORMAT = "--";
	
	// 股票代码
	protected String mCode;
	// 股票名字
    protected String mName;
    // 是否为沪港通
    protected boolean mIsAH;
	//今日开盘价
    protected float mTodayPrice;
	//昨日收盘价
    protected float mYesterdayPrice;
	//当前价
    protected float mNowPrice;
	//今日最高价
    protected float mHighestPrice;
	//今日最低价
    protected float mLowestPrice;

	//成交股票数，单位“股”。100股为1手。
    protected long mTradeCount;
	//成交额，单位“元”。一般需要转换成“万元”。
    protected long mTradeMoney;
	

	//买单情况
    protected BuyOrSellInfo[] mBuy;
	
	//卖单情况
    protected BuyOrSellInfo[] mSell;
	
	// 更新时间
    protected long mDataTime;
	
	// 股票状态
    protected int mStockStatus;
	// 市场状态
    protected int mMarketStatus;

    // 个股资金信息   // 保证不为空
    private StockAsset mStockAsset = new StockAsset();
	
	//---- 针对个人 ----//
	// 现有资金
	private float mAccountMoney;
	// 现有持仓
	private long mHoldNum;
	// 最多可买/卖
	private long mCanHoldNum;
	// 平均成本价
	private float mCostPrice;
	
	//----- score------//
	private String mScoreFormat;  // 分值描述




    public StockTradeInfo(){

    }

    public StockTradeInfo(String code, String name, float todayPrice, float yestodayPrice, float nowPrice, int stockStatus){
		mCode = code;
		mName = name;
		mTodayPrice = todayPrice;
		mYesterdayPrice = yestodayPrice;
		
		mNowPrice = nowPrice;
		mHighestPrice = 0;
		mLowestPrice = 0;
		
		
		mTradeCount = 0;
		mTradeMoney = 0;
		
		mBuy = null;
		mSell = null;
		
		mDataTime = 0;
		mStockStatus = stockStatus;
	}
	
	public StockTradeInfo(String code, String name, boolean isAH, float todayPrice, float yestodayPrice,
						  float nowPrice, float highestPrice, float lowestPrice,
						  long tradeCount, long tradeMoney,
						  BuyOrSellInfo[] buy, BuyOrSellInfo[] sell, int stockStatus, int marketStatus, long time) {
		mCode = code;
		mName = name;
        mIsAH = isAH;
		mTodayPrice = todayPrice;
		mYesterdayPrice = yestodayPrice;
		
		mNowPrice = nowPrice;
		mHighestPrice = highestPrice;
		mLowestPrice = lowestPrice;
		
		
		mTradeCount = tradeCount;
		mTradeMoney = tradeMoney;
		
		mBuy = buy;
		mSell = sell;
		
		mDataTime = time;
		mStockStatus = stockStatus;
		mMarketStatus = marketStatus;
	}
	
	/**
	 * 拷贝构造函数
	 */
	public StockTradeInfo(StockTradeInfo stockTradeInfo){
        copy(stockTradeInfo);
	}

    public void copy(StockTradeInfo stockTradeInfo){
        mCode = stockTradeInfo.mCode;
        mName = stockTradeInfo.mName;
        mIsAH = stockTradeInfo.mIsAH;
        mTodayPrice = stockTradeInfo.mTodayPrice;
        mYesterdayPrice = stockTradeInfo.mYesterdayPrice;

        mNowPrice = stockTradeInfo.mNowPrice;
        mHighestPrice = stockTradeInfo.mHighestPrice;
        mLowestPrice = stockTradeInfo.mLowestPrice;

        mTradeCount = stockTradeInfo.mTradeCount;
        mTradeMoney = stockTradeInfo.mTradeMoney;

        mBuy = stockTradeInfo.mBuy;
        mSell = stockTradeInfo.mSell;

        mDataTime = stockTradeInfo.mDataTime;
        mStockStatus = stockTradeInfo.mStockStatus;
        mMarketStatus = stockTradeInfo.mMarketStatus;

        if (null != mStockAsset)
            mStockAsset.copy(stockTradeInfo.getStockAsset());
    }

    public void setCode(String code){
        mCode = code;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setNowPrice(float nowPrice){
        mNowPrice = nowPrice;
    }

    public void setYesterdayPrice(float yesterdayPrice){
        mYesterdayPrice = yesterdayPrice;
    }

	// 获取股票代码
	public String getCode() {
		return mCode;
	}
	
	/**
	 * 获取股票名称
	 * @return 股票名称
	 */
	public String getName() {
		return mName;
	}
	
	/**
	 * 获取当前价
	 */
	public String getCurPriceFormat() {
		if(mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0){
			return DEFAULT_PERCENT_FORMAT;
		}
		
		return new DecimalFormat("0.00").format(mNowPrice);
	}
	public float getCurPrice() {
		return mNowPrice;
	}

    public String getCurPointFormat(){
        String result = DEFAULT_PERCENT_FORMAT;
        if(mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0){
            return result;
        }

        if(mYesterdayPrice != 0){
            String symbol="";//正负号
            double rise = mNowPrice - mYesterdayPrice;
            if(rise > 0){
                symbol = "+";
            }

            DecimalFormat df = new DecimalFormat("0.00");
            result = symbol + df.format(rise);
        }

        return result;
    }

	public String getCurPercentFormat(){
		String result = DEFAULT_PERCENT_FORMAT;
		if(mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0){
			return result;
		}
		
		if(mYesterdayPrice != 0){
			String symbol="";//正负号
			double rise = mNowPrice - mYesterdayPrice;
			double percent = rise/ mYesterdayPrice *100;
			if(rise > 0){
				symbol = "+";
			}
			result = symbol + new DecimalFormat("0.00").format(percent) +"%";
		}

		return result;
	}
	
	public double getCurPercent() {
		if(mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0){
			return 0;
		}
		if(mYesterdayPrice == 0){
			return 0;
		}
		
		double rise = mNowPrice - mYesterdayPrice;
		double percent = rise/ mYesterdayPrice *100;
		return percent;
	}
	
	/**
	 * 获取当前价注释
	 */
	public String getCurPriceComment() {
		String result = "----  ----";
		if(mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0){
			return result;
		}
		
		if(mYesterdayPrice != 0){
			String symbol="";//正负号
			double rise = mNowPrice - mYesterdayPrice;
			double percent = rise/ mYesterdayPrice *100;
			if(rise > 0){
				symbol = "+";
			}
			
			DecimalFormat df = new DecimalFormat("0.00");
			result = symbol + df.format(rise)+"  ";
			result += symbol + df.format(percent) +"%";
		}
			
		return result;
	}
	
	/**
	 * 得到股票涨跌情况
	 * @return   1 涨 、0 平、 -1 跌
	 */
	public int getRiseInfo(){ 
		int ret = 0;
		if(mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0){
			return ret;
		}
		
		float riseInfo = mNowPrice - mYesterdayPrice;
		if(riseInfo	> 0)
			ret = 1;
		else if(riseInfo < 0)
			ret = -1;
		
		return ret;
	}
	/**
	 * 获取今日开盘价
	 * @return 今日股票开盘价
	 */
	public String getTodayPriceFormat() {
		if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
				|| isStockSuspended()){
			return DEFAULT_PERCENT_FORMAT;
		}
		
		return new DecimalFormat("0.00").format(mTodayPrice);
	}
	public float getTodayPrice(){
		return mTodayPrice;
	}
	
	/**
	 * 获取昨日收盘价
	 * @return 昨日收盘价
	 */
	public float getYestodayPrice() {
		return mYesterdayPrice;
	}
	public String getYestodayPriceFormat() {
		if(mStockStatus == STATUS_OPEN_PREPARE && mYesterdayPrice == 0){
			return DEFAULT_PERCENT_FORMAT;
		}
		
		return new DecimalFormat("0.00").format(mYesterdayPrice);
	}
	
	/**
	 * 获取当前股价
	 * @return 当前股价
	 */
	public String getNowPriceFormat() {
		if(mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0){
			return DEFAULT_PERCENT_FORMAT;
		}
		return new DecimalFormat("0.00").format(mNowPrice);
	}
	public float getNowPrice() {
		return mNowPrice;
	}

	/**
	 * 获取今日最高价
	 * @return 今日最高价
	 */
	public String getHighestPriceFormat() {
		if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
				|| isStockSuspended()){
			return DEFAULT_PERCENT_FORMAT;
		}
		return new DecimalFormat("0.00").format(mHighestPrice);
	}
	public float getHighestPrice(){
		return mHighestPrice;
	}
	
	/**
	 * 获取今日最低价
	 * @return 今日最低价
	 */
	public String getLowestPriceFormat() {
		if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
				|| isStockSuspended()){
			return DEFAULT_PERCENT_FORMAT;
		}
		return new DecimalFormat("0.00").format(mLowestPrice);
	}
	public float getLowestPrice(){
		return mLowestPrice;
	}
	
	/**
	 * 获取股票交易量。单位为“股”，100股为1手，请注意转换。
	 * @return 股票交易量
	 */
	public String getTradeCountFormat() {
		if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
				|| isStockSuspended()){
			return DEFAULT_DATA_FORMAT;
		}
		float tradeformat = (float)mTradeCount/(100*10000);
		return new DecimalFormat("0.00").format(tradeformat) + "万手";
	}
	public long getTradeCount(){
		return mTradeCount;
	}

    /**
     * 获取成交额
     * @return
     */
    public String getTradeFormat(){
        if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
                || isStockSuspended()){
            return DEFAULT_DATA_FORMAT;
        }

        String retString;
        float tradeMoney = (float)mTradeMoney / 100000000;
        if(tradeMoney>1){   // 大于1亿
            retString = String.format("%.2f亿", tradeMoney);
        }
        else {
            retString = String.format("%.0f万", (float)mTradeMoney/10000);
        }

        return retString;
    }
	
	
	/**
	 * 获取股票交易额。单位为“元”，如需显示“万元”，请注意转换。
	 * @return 股票交易额
	 */
	public long getTradeMoney() {
		return mTradeMoney;
	}
	/**
	 * 获取振幅
	 */
	public String getDayPercent(){
		if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
				|| isStockSuspended()){
			return DEFAULT_PERCENT_FORMAT;
		}
		
		if(mYesterdayPrice != 0){
			float dayPercent = (mHighestPrice  - mLowestPrice)/ mYesterdayPrice *100;
			return new DecimalFormat("0.00").format(dayPercent)+"%";
		}
		else{
			return "0.00";
		}
		
	}
	/**
	 * 获取总市值
	 */
	public String getAllTradePriceFormat(){
		if(mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0
                || mStockAsset.getTotalShares() == 0){
			return DEFAULT_PERCENT_FORMAT;
		}
		
		String retString;
		float allTradePrice = mStockAsset.getTotalShares() * mNowPrice / 100000000;
		if(allTradePrice>1){
			retString = String.format("%.0f亿", allTradePrice);
		}
		else {
			retString = String.format("%.2f亿", allTradePrice);
		}
		return retString;
	}

	/**
	 * 获取流通市值
	 */
	public String getMoveTradePriceFormat(){
		if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
				|| mStockAsset.getAFloatListed() == 0){
			return DEFAULT_PERCENT_FORMAT;
		}
		
		String retString;
		float moveTradePrice = mStockAsset.getAFloatListed() * mNowPrice / 100000000;
		if(moveTradePrice>1){
			retString = String.format("%.0f亿", moveTradePrice);
		}
		else {
			retString = String.format("%.2f亿", moveTradePrice);
		}
		return retString;
	}

    /**
     * 获取市盈率
     * @return
     */
    public String getPreRatioFormat(){
        if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
                || mStockAsset.getPeRatio() == 0){
            return DEFAULT_PERCENT_FORMAT;
        }

        return String.format("%.2f", mStockAsset.getPeRatio());
    }

    /**
     * 获取市净率
     * @return
     */
    public String getBookValueFormat(){
        if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
                || mStockAsset.getBookValue() == 0){
            return DEFAULT_PERCENT_FORMAT;
        }

        return String.format("%.2f", mStockAsset.getBookValue());
    }

    /**
     * 获取52周最高/最低价
     * @return
     */
    public String get52WeeksMaxPriceFormat(){
        if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
                || mStockAsset.get52WeeksMaxPrice() == 0){
            return DEFAULT_PERCENT_FORMAT;
        }

        return String.format("%.2f", mStockAsset.get52WeeksMaxPrice());
    }
    public String get52WeeksMinPriceFormat(){
        if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
                || mStockAsset.get52WeeksMinPrice() == 0){
            return DEFAULT_PERCENT_FORMAT;
        }

        return String.format("%.2f", mStockAsset.get52WeeksMinPrice());
    }

    /**
     * 获取每股收益
     * @return
     */
    public String getEPSFormat(){
        if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
                || mStockAsset.getEPS() == 0){
            return DEFAULT_PERCENT_FORMAT;
        }

        return String.format("%.2f", mStockAsset.getEPS());
    }

    /**
     * 获取每股净资产
     * @return
     */
    public String getShareRepurchaseFormat(){
        if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
                || mStockAsset.getShareRepurchase() == 0){
            return DEFAULT_PERCENT_FORMAT;
        }

        return String.format("%.2f", mStockAsset.getShareRepurchase());
    }



    /**
	 * 获取买手信息。
	 * @return 买手信息。数组从前到后依次为：买一、买二、买三、买四、买五。
	 */
	public BuyOrSellInfo[] getBuyInfo() {
		return mBuy;
	}
	
	/**
	 * 获取卖手信息。
	 * @return 卖手信息。数组从前到后依次为：卖一、卖二、卖三、卖四、卖五。
	 */
	public BuyOrSellInfo[] getSellInfo() {
		return mSell;
	}
	
//	/**
//	 * 获取对应股票信息的日期。例如周末，或者其他休市期间获取的数据将不是实时的。
//	 * @return 获取对应股票信息的日期。
//	 */
//	public String getDate() {
//		return mDate;
//	}
//	
	/**
	 * 获取对应股票信息的时间。例如周末，或者其他休市期间获取的数据将不是实时的。
	 * @return 获取对应股票信息的时间。
	 */
	public String getDataTimeFormat() {
		return TimeUtil.getSimpleTime(mDataTime);
	}
	public long getDataTime(){
		return mDataTime;
	}


	/**
	 * 获取换手率
	 * @return 换手率
	 */
	public String getTurnoverRate(){
		if((mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0)
				|| mStockAsset.getAFloatListed() == 0 || isStockSuspended()){
			return DEFAULT_PERCENT_FORMAT;
		}
		
		float rate = 0;
		if(mStockAsset.getAFloatListed() > 0){
			rate = (float) ((double)mTradeCount/mStockAsset.getAFloatListed() * 100);
		}
				
		return new DecimalFormat("0.00").format(rate)+"%";
	} 
	
	
	// 获取当前的交易状态
	public int getStockStatus() {
		return mStockStatus;
	}
	public int getMarketStatus() {
		return mMarketStatus;
	} 
	
	// 是否停牌
	public boolean isStockSuspended(){
		return (mStockStatus == STATUS_STOP || mStockStatus == STATUS_TRADING_HALT);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append("股票名称： " + getName() + "\n");
		sb.append("今日开盘价： " + getTodayPriceFormat() + "元\n");
		sb.append("昨日收盘价： " + getYestodayPriceFormat() + "元\n");
		sb.append("当前股价： " + getNowPriceFormat() + "元\n");
		sb.append("今日最高价： " + getHighestPriceFormat() + "元\n");
		sb.append("今日最低价： " + getLowestPriceFormat() + "元\n");
		sb.append("今日交易量： " + getTradeCountFormat() + "股\n");
		sb.append("今日成交量： " + getTradeMoney() + "元\n"); 
		
		BuyOrSellInfo[] buy = getBuyInfo();
		sb.append("买一：\n" + buy[0] + "\n");
		sb.append("买二：\n" + buy[1] + "\n");
		sb.append("买三：\n" + buy[2] + "\n");
		sb.append("买四：\n" + buy[3] + "\n");
		sb.append("买五：\n" + buy[4] + "\n");
		
		BuyOrSellInfo[] sell = getSellInfo();
		sb.append("卖一：\n" + sell[0] + "\n");
		sb.append("卖二：\n" + sell[1] + "\n");
		sb.append("卖三：\n" + sell[2] + "\n");
		sb.append("卖四：\n" + sell[3] + "\n");
		sb.append("卖五：\n" + sell[4] + "\n");
		
		sb.append("时间： " + getDataTimeFormat() + "\n");
		
		return sb.toString();
	}

    public boolean isIsAH() {
        return mIsAH;
    }

    public void setIsAH(boolean isAH) {
        this.mIsAH = isAH;
    }

    public StockAsset getStockAsset() {
        return mStockAsset;
    }

    public void setStockAsset(StockAsset mStockAsset) {
        this.mStockAsset = mStockAsset;
    }


    //买单或卖单信息。
	public static class BuyOrSellInfo{
		//数量。单位为“股”。100股为1手。
		long mCount;
		//价格。
		float mPrice;
		
		// 昨日收盘价
		float mYesterdayPrice;
		
		public BuyOrSellInfo(long count, float price, float yesterdayPrice){
			mCount = count;
			mPrice = price;
			mYesterdayPrice = yesterdayPrice;
		}
		
		public String getCountFormat(){
            long showCount = mCount/100;
            if (showCount > 999999){// 超过百万手则显示“万”为单位
                return new DecimalFormat("0.0万").format((float)showCount/10000);
            }
            else {
                return String.valueOf(showCount);
            }
		}
		public String getPriceFormat(){
			return new DecimalFormat("0.00").format(mPrice);
		}
		

		/**
		 * 得到当前买卖手出价的涨跌情况
		 * @return   1 涨 、0 平、 -1 跌
		 */
		public int getRiseInfo(){ 
			int ret = 0;
			float riseInfo = mPrice - mYesterdayPrice;
			if(riseInfo	> 0)
				ret = 1;
			else if(riseInfo < 0)
				ret = -1;
			
			return ret;
		}

		public long getCount(){
			return mCount;
		}
		public float getPrice(){
			return mPrice;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("数量： " + mCount + "股    ");
			sb.append("价格： " + mPrice + "元");
			return sb.toString();
		}
	}
	
	// 得到涨跌情况描述
	public String getSimpleComment(){
		if(mStockStatus == STATUS_OPEN_PREPARE && mNowPrice == 0){
			return DEFAULT_PERCENT_FORMAT;
		}
		
		String result;
		String symbol="";//正负号
		double rise = mNowPrice - mTodayPrice;
		double percent = 0;
		if(mTodayPrice !=0 )
			percent = rise/mTodayPrice*100;
		if(rise > 0){
			symbol = "+";
		}
		
		DecimalFormat df = new DecimalFormat("0.00");
		result = symbol + df.format(rise)+"  ";
		result += symbol + df.format(percent) +"%";
		
		return result;
	}
	
	
	public float getAccountMoney() {
		return mAccountMoney;
	}

	public void setAccountMoney(float mAccountMoney) {
		this.mAccountMoney = mAccountMoney;
	}

	public long getHoldNum() {
		return mHoldNum;
	}

	public void setHoldNum(long mHoldNum) {
		this.mHoldNum = mHoldNum;
	}

	public long getCanHoldNum() {
		return mCanHoldNum;
	}

	public void setCanHoldNum(long mCanHoldNum) {
		this.mCanHoldNum = mCanHoldNum;
	}
	
	public float getCostPrice() {
		return mCostPrice;
	}

	public void setCostPrice(float mCostPrice) {
		this.mCostPrice = mCostPrice;
	}
	
	/**
	 * 是否为上交所
	 * @return
	 */
	public boolean isSHCode(){
		if(mCode.startsWith("60")){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 解析仅有名称和代码的StockInfo
	 */
	public static StockTradeInfo resolveNameJsonObject(JSONObject jsonStockInfo) throws JSONException {
		String name = jsonStockInfo.getString("HQZQJC");
		String code = jsonStockInfo.getString("HQZQDM");

		
		return  new StockTradeInfo(code, name, 0, 0, 0, STATUS_NORMAL);
	}
	
	/**
	 * 解析简明的StockInfo
	 */
	public static StockTradeInfo resolveSummaryJsonObject(JSONObject jsonStockInfo) throws JSONException {
		String name = jsonStockInfo.optString("HQZQJC");
		String code = jsonStockInfo.optString("HQZQDM");
		float HQJRKP = (float)jsonStockInfo.optDouble("HQJRKP",0);  // 今日开盘价
		float HQZRSP = (float)jsonStockInfo.optDouble("HQZRSP",0);  // 昨日收盘价
		float HQZJCJ = (float)jsonStockInfo.optDouble("HQZJCJ",0);  // 按盘价
		int stockStatus = jsonStockInfo.optInt("stockStatus",STATUS_NORMAL);
		
		return  new StockTradeInfo(code, name, HQJRKP, HQZRSP, HQZJCJ,stockStatus);
	}
	/**
	 * 解析完整的StockInfo 
	 * @throws JSONException
	 */
	public static StockTradeInfo resolveJsonObject(JSONObject jsonStockInfo) throws JSONException {


        // 是否为沪港通
        boolean isAH = jsonStockInfo.optBoolean("isHGT",false);

        // 基本行情信息
        JSONObject jsonQuotations = jsonStockInfo.getJSONObject("quotations");
		String name = jsonQuotations.getString("HQZQJC");
		String code = jsonQuotations.getString("HQZQDM");
		float todayPrice = (float)jsonQuotations.getDouble("HQJRKP");  		// 今日开盘价
		float yesterdayPrice = (float)jsonQuotations.getDouble("HQZRSP"); 	// 昨日收盘价
		float nowPrice = (float)jsonQuotations.getDouble("HQZJCJ");  		// 按盘价
		float highestPrice = (float)jsonQuotations.getDouble("HQZGCJ"); 		// 最高成交价
		float lowestPrice = (float)jsonQuotations.getDouble("HQZDCJ");		// 最低成交价
		//float buy1Price = (float)jsonStockInfo.getDouble("HQBJW1"); 		// 买价位一
		//float sell1Price = (float)jsonStockInfo.getDouble("HQSJW1"); 		// 卖价位一
		long tradeCount = jsonQuotations.getLong("HQCJSL");					// 成交数量
		long tradeMoney = jsonQuotations.getLong("HQCJJE");   				// 成交金额
        int stockStatus = jsonQuotations.getInt("stockStatus");
        long time = jsonQuotations.getLong("time");

		BuyOrSellInfo buy1 = new BuyOrSellInfo(jsonQuotations.getLong("HQBSL1"), (float)jsonQuotations.getDouble("HQBJW1"), yesterdayPrice); // 买卖数量和价位
		BuyOrSellInfo buy2 = new BuyOrSellInfo(jsonQuotations.getLong("HQBSL2"), (float)jsonQuotations.getDouble("HQBJW2"), yesterdayPrice);
		BuyOrSellInfo buy3 = new BuyOrSellInfo(jsonQuotations.getLong("HQBSL3"), (float)jsonQuotations.getDouble("HQBJW3"), yesterdayPrice);
		BuyOrSellInfo buy4 = new BuyOrSellInfo(jsonQuotations.getLong("HQBSL4"), (float)jsonQuotations.getDouble("HQBJW4"), yesterdayPrice);
		BuyOrSellInfo buy5 = new BuyOrSellInfo(jsonQuotations.getLong("HQBSL5"), (float)jsonQuotations.getDouble("HQBJW5"), yesterdayPrice);
		
		BuyOrSellInfo sell1 = new BuyOrSellInfo(jsonQuotations.getLong("HQSSL1"), (float)jsonQuotations.getDouble("HQSJW1"), yesterdayPrice);
		BuyOrSellInfo sell2 = new BuyOrSellInfo(jsonQuotations.getLong("HQSSL2"), (float)jsonQuotations.getDouble("HQSJW2"), yesterdayPrice);
		BuyOrSellInfo sell3 = new BuyOrSellInfo(jsonQuotations.getLong("HQSSL3"), (float)jsonQuotations.getDouble("HQSJW3"), yesterdayPrice);
		BuyOrSellInfo sell4 = new BuyOrSellInfo(jsonQuotations.getLong("HQSSL4"), (float)jsonQuotations.getDouble("HQSJW4"), yesterdayPrice);
		BuyOrSellInfo sell5 = new BuyOrSellInfo(jsonQuotations.getLong("HQSSL5"), (float)jsonQuotations.getDouble("HQSJW5"), yesterdayPrice);


        // 股票市场信息
        JSONObject jsonMarketStatus = jsonStockInfo.getJSONObject("marketStatus");
        int marketStatus = jsonMarketStatus.getInt("marketStatus");


		StockTradeInfo stockInfo = new StockTradeInfo(code, name, isAH, todayPrice, yesterdayPrice, nowPrice,
				highestPrice, lowestPrice, 
				tradeCount, tradeMoney,
				new BuyOrSellInfo[]{buy1, buy2, buy3, buy4, buy5}, 
				new BuyOrSellInfo[]{sell1, sell2, sell3, sell4, sell5}, stockStatus, marketStatus,
				 time);

		try{
			stockInfo.setStockAsset(StockAsset.resolveJSONObject(jsonStockInfo.getJSONObject("stockAsset")));
		}
		catch (JSONException e){
			e.printStackTrace();
		}


		return stockInfo;
	}


    public static StockTradeInfo resolveJsonPersonStock(JSONObject jsonStockInfo) throws JSONException {
        StockTradeInfo stockTradeInfo = resolveJsonObject(jsonStockInfo);
        // 与个人相关

		try {
            stockTradeInfo.setAccountMoney((float)jsonStockInfo.getDouble("activeMoney"));
            JSONObject holdOrders = jsonStockInfo.getJSONObject("holdOrders");
            stockTradeInfo.setCanHoldNum(holdOrders.getLong("canTradedNum"));
            stockTradeInfo.setHoldNum(holdOrders.getLong("haveNum"));
            stockTradeInfo.setCostPrice((float)holdOrders.getDouble("costPrice"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return stockTradeInfo;
    }




	public String getScoreFormat() {
		return mScoreFormat;
	}

	public void setScoreFormat(String scoreFormat) {
		mScoreFormat = scoreFormat;
	}

    /**
     * 获取一组股票信息
     * @param response
     * @return 按Hash查找
     * @throws JSONException
     */
    public static HashMap<String, StockTradeInfo> resolveJsonObjectArray(JSONObject response) throws JSONException {
        HashMap<String, StockTradeInfo> stockTradeInfoArray = new HashMap<String, StockTradeInfo>();
        //ArrayList<StockTradeInfo> stockTradeInfoArray = new ArrayList<StockTradeInfo>();
        JSONArray data = response.getJSONArray("data");
        int len = data.length();
        for(int i = 0; i < len; ++i){
            StockTradeInfo stockTradeInfo = StockTradeInfo.resolveSummaryJsonObject(data.getJSONObject(i));
            stockTradeInfoArray.put(stockTradeInfo.getCode(), stockTradeInfo);
        }
        return stockTradeInfoArray;
    }

    public static List<StockTradeInfo> resolveStockIDsArray(JSONObject response) throws JSONException {
        List<StockTradeInfo> items = new ArrayList<StockTradeInfo>();
        JSONObject data = response.getJSONObject("data");
        JSONObject quotations = data.getJSONObject("quotations");
        JSONArray stockIds = data.getJSONArray("stockIds");
        int len = stockIds.length();
        for (int i = 0; i< len ; i++){
            JSONObject perItem = quotations.getJSONObject(stockIds.getString(i));
            items.add(resolveSummaryJsonObject(perItem));
        }

        return items;
    }



    /**
     * 解析盘口数据
     */
    public static StockTradeInfo resloveHandicapData(JSONObject object){
        StockTradeInfo info = new StockTradeInfo();
        try {
            JSONObject jsonQuotations = object.getJSONObject("data").getJSONObject("quotations");
            float yesterdayPrice = (float)jsonQuotations.getDouble("HQZRSP"); 	// 昨日收盘价

            BuyOrSellInfo buy1 = new BuyOrSellInfo(jsonQuotations.getLong("HQBSL1"), (float) jsonQuotations.getDouble("HQBJW1"), yesterdayPrice); // 买卖数量和价位
            BuyOrSellInfo buy2 = new BuyOrSellInfo(jsonQuotations.getLong("HQBSL2"), (float) jsonQuotations.getDouble("HQBJW2"), yesterdayPrice);
            BuyOrSellInfo buy3 = new BuyOrSellInfo(jsonQuotations.getLong("HQBSL3"), (float) jsonQuotations.getDouble("HQBJW3"), yesterdayPrice);
            BuyOrSellInfo buy4 = new BuyOrSellInfo(jsonQuotations.getLong("HQBSL4"), (float) jsonQuotations.getDouble("HQBJW4"), yesterdayPrice);
            BuyOrSellInfo buy5 = new BuyOrSellInfo(jsonQuotations.getLong("HQBSL5"), (float) jsonQuotations.getDouble("HQBJW5"), yesterdayPrice);

            BuyOrSellInfo sell1 = new BuyOrSellInfo(jsonQuotations.getLong("HQSSL1"), (float) jsonQuotations.getDouble("HQSJW1"), yesterdayPrice);
            BuyOrSellInfo sell2 = new BuyOrSellInfo(jsonQuotations.getLong("HQSSL2"), (float) jsonQuotations.getDouble("HQSJW2"), yesterdayPrice);
            BuyOrSellInfo sell3 = new BuyOrSellInfo(jsonQuotations.getLong("HQSSL3"), (float) jsonQuotations.getDouble("HQSJW3"), yesterdayPrice);
            BuyOrSellInfo sell4 = new BuyOrSellInfo(jsonQuotations.getLong("HQSSL4"), (float) jsonQuotations.getDouble("HQSJW4"), yesterdayPrice);
            BuyOrSellInfo sell5 = new BuyOrSellInfo(jsonQuotations.getLong("HQSSL5"), (float) jsonQuotations.getDouble("HQSJW5"), yesterdayPrice);

            info.mBuy = new BuyOrSellInfo[]{buy1, buy2, buy3, buy4, buy5};
            info.mSell = new BuyOrSellInfo[]{sell1, sell2, sell3, sell4, sell5};
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return info;
    }


	@Override
	public int compareTo(StockTradeInfo stockTradeInfo) {
		double riseInfo = (mNowPrice - mYesterdayPrice)/mYesterdayPrice;

		double riseInfoCompare = (stockTradeInfo.mNowPrice - stockTradeInfo.mYesterdayPrice)/stockTradeInfo.mYesterdayPrice;

		int ret = 0;
		double compareInfo = riseInfoCompare - riseInfo;
		if(compareInfo	> 0)
			ret = 1;
		else if(compareInfo < 0)
			ret = -1;

		return ret;
	}
}
