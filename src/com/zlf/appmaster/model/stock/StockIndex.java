package com.zlf.appmaster.model.stock;

import com.zlf.appmaster.model.industry.IndustryInfo;
import com.zlf.appmaster.utils.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 股票指数
 * @author Deping Huang
 *
 */
public class StockIndex /*implements Parcelable*/{

	public static final String INDEX_CODE_SZ = "000001";	// 上证指数
	public static final String INDEX_CODE_SC = "399001"; 	// 深成指数
	public static final String INDEX_CODE_CYB = "399006";	// 创业板

	private String DEFAULT_COMMENT = "--";
	
	private String mCode = INDEX_CODE_SZ;
	private String mName;
	
	private double mYesterdayIndex;				// 昨日收盘指数
	private double mTodayIndex;					// 今日开市指数
	private double mNowIndex;					// 当前指数
	private double mHighestIndex;				// 今日最高指数
	private double mLowestIndex;				// 今日最低指数
	
	private double mTradeCount;					// 成交数量
	private double mTradePrice;					// 成交金额
	
	//  涨家数/平家数/跌家数
	private int mUpCount;
	private int mDeuceCount;
	private int mDownCount;
	
	private long mDataTime;					// 数据更新时间
	private long mCurrentTime;				// 服务器当前时间

	private String mIsOpen;  //是否开市

    public StockIndex(){

    }

    public StockIndex(StockIndex stockIndex){
        copy(stockIndex);
    }

    public void copy(StockIndex stockIndex){
        mCode = stockIndex.mCode;
        mName = stockIndex.mName;
        mYesterdayIndex = stockIndex.mYesterdayIndex;
        mTodayIndex = stockIndex.mTodayIndex;
        mNowIndex = stockIndex.mNowIndex;
        mHighestIndex = stockIndex.mHighestIndex;
        mLowestIndex = stockIndex.mLowestIndex;

        mTradeCount = stockIndex.mTradeCount;
        mTradePrice = stockIndex.mTradePrice;

        mUpCount = stockIndex.mUpCount;
        mDeuceCount = stockIndex.mDeuceCount;
        mDownCount = stockIndex.mDownCount;

        mDataTime = stockIndex.mDataTime;
        mCurrentTime = stockIndex.mCurrentTime;

		mIsOpen = stockIndex.mIsOpen;
    }


	// 市场状态
	private int mMarketStatus = StockTradeInfo.MARKET_STATUS_NOT_SERVICE_TIME;
	
	
	public String getCode() {
		return mCode;
	}
	public void setCode(String code) {
		mCode = code;
	}
	
	public void setTodayIndex(double todayIndex) {
		mTodayIndex = todayIndex;
	}
	
	public String getNowIndexFormat() {
		// 格式化输出小数点后2位
		DecimalFormat df = new DecimalFormat("#.00");
		return df.format(mNowIndex);
	}
	
	public void setNowIndex(double nowIndex) {
		mNowIndex = nowIndex;
	}
	public double getNowIndex() {
		return mNowIndex ;
	}
	

	/**
	 * 得到股票涨跌情况
	 * @return   1 涨 、0 平、 -1 跌
	 */
	public int getRiseInfo(){ 
		int ret = 0;
		float riseInfo = (float)(mNowIndex - mYesterdayIndex);
		if(riseInfo	> 0){
			ret = 1;
		}
		else if(riseInfo < 0){
			ret = -1;
		}	
		
		return ret;
	}

    //得到行情的变化值
    public String getChangeValue(){
        String result;
        String symbol="";//正负号
        double rise = mNowIndex - mYesterdayIndex;
        if(rise > 0){
            symbol = "+";
        }

        DecimalFormat df = new DecimalFormat("0.00");
        result = symbol + df.format(rise)+"  ";
        return result;
    }


	// 得到行情涨跌情况
	public String getComment(){
		String result;
		String symbol="";//正负号
		double rise = mNowIndex - mYesterdayIndex;
		double percent = rise/mYesterdayIndex*100;
		if(rise > 0){
			symbol = "+";
		}
		
		DecimalFormat df = new DecimalFormat("0.00");
		result = symbol + df.format(rise)+"  ";
		result += symbol + df.format(percent) +"%";
		
		return result;
	}
	
	
	/*
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(mCode);
		dest.writeDouble(mStartIndex);
		dest.writeDouble(mLastIndex);
	}
	
	
	// 反序列化
	;
		}

		@Override


		} 
		 
	 };
	*/

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return mCode;
	}
	
	
	public double getYesterdayIndex() {
		return mYesterdayIndex;
	}
	public void setYesterdayIndex(double mYesterdayIndex) {
		this.mYesterdayIndex = mYesterdayIndex;
	}
	public double getHighestIndex() {
		return mHighestIndex;
	}
	public void setHighestIndex(double mHighestIndex) {
		this.mHighestIndex = mHighestIndex;
	}
	public double getLowestIndex() {
		return mLowestIndex;
	}
	public void setLowestIndex(double mLowestIndex) {
		this.mLowestIndex = mLowestIndex;
	}
	public void setTradeCount(double mTradeCount) {
		this.mTradeCount = mTradeCount;
	}
	public void setTradePrice(double mTradePrice) {
		this.mTradePrice = mTradePrice;
	}
	public void setUpCount(int mUpCount) {
		this.mUpCount = mUpCount;
	}
	public void setDeuceCount(int mDeuceCount) {
		this.mDeuceCount = mDeuceCount;
	}
	public void setDownCount(int mDownCount) {
		this.mDownCount = mDownCount;
	}
	public void setDataTime(long mDataTime) {
		this.mDataTime = mDataTime;
	}
	public long getDataTime() {
		return mDataTime;
	}
	public void setCurrentTime(long mCurrentTime) {
		this.mCurrentTime = mCurrentTime;
	}
	public String getName() {
		return mName;
	}
	public void setName(String mName) {
		this.mName = mName;
	}
	public int getMarketStatus() {
		return mMarketStatus;
	}
	public void setMarketStatus(int mMarketStatus) {
		this.mMarketStatus = mMarketStatus;
	}
	public String getIsOPen() {
		return mIsOpen;
	}
	public void setIsOPen(String isOpen) {
		this.mIsOpen = isOpen;
	}
	
	public static String getNameById(String indexId){
		String ret = "";
		if(indexId.equals(INDEX_CODE_SZ)){
			ret = "上证指数";
		}
		else if(indexId.equals(INDEX_CODE_SC)){
			ret = "深证成指";
		}
		else if(indexId.equals(INDEX_CODE_CYB)){
			ret = "创业板指";
		}
		return ret;
	}
	
	/**
	 * 获取数据刷新信息的时间。例如周末，或者其他休市期间获取的数据将不是实时的。
	 */
	public String getDataTimeFormat() {
		return TimeUtil.getSimpleTime(mDataTime);
	}
	
	/**
	 * 获取当前价
	 */
	public String getCurPriceFormat() {

        if (mNowIndex == 0) return DEFAULT_COMMENT;

		return new DecimalFormat("0.00").format(mNowIndex);
	}

	public String getCurPercentFormat(){

        if (mYesterdayIndex == 0){
            return DEFAULT_COMMENT;
        }

		String result;
		String symbol="";//正负号
		double rise = mNowIndex - mYesterdayIndex;
		double percent = rise/mYesterdayIndex*100;
		if(rise > 0){
			symbol = "+";
		}
		result = symbol + new DecimalFormat("0.00").format(percent) +"%";
		return result;
	}
    public String getCurPointFormat(){
        if (mNowIndex == 0) return DEFAULT_COMMENT;

        String result;
        String symbol="";//正负号
        double rise = mNowIndex - mYesterdayIndex;
        if(rise > 0){
            symbol = "+";
        }

        DecimalFormat df = new DecimalFormat("0.00");
        result = symbol + df.format(rise);

        return result;
    }
	/**
	 * 获取当前价注释
	 */
	public String getCurPriceComment() {
        if (mYesterdayIndex == 0) return DEFAULT_COMMENT;

		String result;
		String symbol="";//正负号
		double rise = mNowIndex - mYesterdayIndex;
		double percent = rise/mYesterdayIndex*100;
		if(rise > 0){
			symbol = "+";
		}
		
		DecimalFormat df = new DecimalFormat("0.00");
		result = symbol + df.format(rise)+"  ";
		result += symbol + df.format(percent) +"%";
		
		return result;
	}
    public String getCurPercentPrompt() {
        if (mYesterdayIndex == 0) return DEFAULT_COMMENT;

        String result;
        String symbol="";//正负号
        double rise = mNowIndex - mYesterdayIndex;
        double percent = rise/mYesterdayIndex*100;
        if(rise > 0){
            symbol = "+";
        }

        DecimalFormat df = new DecimalFormat("0.00");
        result = symbol + df.format(percent) +"%";

        return result;
    }



    public String getTodayPriceFormat() {
        if (mTodayIndex == 0) return DEFAULT_COMMENT;

        return new DecimalFormat("0.00").format(mTodayIndex);
	}
	

	public String getYestodayPriceFormat() {
        if (mYesterdayIndex == 0) return DEFAULT_COMMENT;

        return new DecimalFormat("0.00").format(mYesterdayIndex);
	}
	
	public String getNowPriceFormat() {
		return new DecimalFormat("0.00").format(mNowIndex);
	}


	/**
	 * 获取涨家、平家、跌家
	 */
	public String getUpCountFormat() {
        if (mUpCount == 0) return DEFAULT_COMMENT;

        return String.valueOf(mUpCount);
	}
	public String getDeuceCountFormat() {
        if (mDeuceCount == 0) return DEFAULT_COMMENT;

        return String.valueOf(mDeuceCount);
	}
	public String getDownCountFormat() {
        if (mDownCount == 0) return DEFAULT_COMMENT;

        return String.valueOf(mDownCount);
	}
	
	/**
	 * 获取振幅
	 */
	public String getDayPercent(){
		double dayPercent = (mHighestIndex  - mLowestIndex)/mYesterdayIndex *100;
		return new DecimalFormat("0.00").format(dayPercent)+"%";
	}
	
	/**
	 * 获取今日最高价
	 * @return 今日最高价
	 */
	public String getHighestPriceFormat() {
        if (mHighestIndex == 0) return DEFAULT_COMMENT;

        return new DecimalFormat("0.00").format(mHighestIndex);
	}
	
	/**
	 * 获取今日最低价
	 * @return 今日最低价
	 */
	public String getLowestPriceFormat() {
        if (mLowestIndex == 0) return DEFAULT_COMMENT;

        return new DecimalFormat("0.00").format(mLowestIndex);
	}
	
	/**
	 * 获取股票交易量。单位为“股”，100股为1手，请注意转换。
	 * @return 股票交易量
	 */
	public String getTradeCountFormat() {
        if (mTradeCount == 0) return DEFAULT_COMMENT;

        float tradeformat = (float)mTradeCount/(10000*10000);
		return new DecimalFormat("0.00").format(tradeformat) + "亿手";
	}
	
	public String getTradePriceFormat() {
        if (mTradePrice == 0) return DEFAULT_COMMENT;

        float tradeformat = (float)mTradePrice/100000000;
		return new DecimalFormat("0.00").format(tradeformat) + "亿";
	}
	
	/**
	 * 行情界面Json解析
	 * @param response
	 * @return
	 * @throws JSONException
	 */
    public final static int QUOTATIONS_OBJECT_COUNTS = 6;    //行情中心返回的对象数
	public static Object[] resolveQuotationJsonObject(JSONObject response) throws JSONException {
		
		JSONObject data = response.getJSONObject("data");
		JSONObject jsonQuotationInfo = data.getJSONObject("quotations");
		
		// 解析三大指数
		ArrayList<StockIndex> stockIndexArray = new ArrayList<StockIndex>();
		JSONArray jsonArray = data.getJSONArray("indexs");
		int length = jsonArray.length();
		for(int i = 0; i<length; i++) {
			JSONObject indexObject = jsonArray.getJSONObject(i);
			StockIndex stockIndex = new StockIndex();
			stockIndex.setNowIndex(indexObject.getDouble("ZSZJZS"));
			stockIndex.setYesterdayIndex(indexObject.getDouble("ZSSSZS"));
			stockIndex.setCode(indexObject.getString("ZSZSDM"));
			
			stockIndexArray.add(stockIndex);
		}					
		
		// 解析领涨领跌行业
		ArrayList<IndustryInfo> industryLedUpArray = new ArrayList<IndustryInfo>();
		ArrayList<IndustryInfo> industryLedDownArray = new ArrayList<IndustryInfo>();
		jsonArray = data.getJSONArray("lingzhang");
		length = jsonArray.length();
		for(int i = 0; i<length; i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			IndustryInfo industryInfo = new IndustryInfo();
			industryInfo.setName(jsonObject.getString("IndustryName"));
			industryInfo.setPercent(jsonObject.getDouble("IndustryDown"));
			industryInfo.setIndustryID(jsonObject.getString("IndustryId"));
			
			String stockID = jsonObject.getString("stockId");
			JSONObject jsonStockInfo = jsonQuotationInfo.getJSONObject(stockID);
			industryInfo.setLedStock(StockTradeInfo.resolveSummaryJsonObject(jsonStockInfo));
			
			industryLedUpArray.add(industryInfo);
		}
		jsonArray = data.getJSONArray("lingdie");
		length = jsonArray.length();
		for(int i = 0; i<length; i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			IndustryInfo industryInfo = new IndustryInfo();
			industryInfo.setName(jsonObject.getString("IndustryName"));
			industryInfo.setPercent(jsonObject.getDouble("IndustryDown"));
			industryInfo.setIndustryID(jsonObject.getString("IndustryId"));
			
			String stockID = jsonObject.getString("stockId");
			JSONObject jsonStockInfo = jsonQuotationInfo.getJSONObject(stockID);
			industryInfo.setLedStock(StockTradeInfo.resolveSummaryJsonObject(jsonStockInfo));
			industryLedDownArray.add(industryInfo);
		}
		
		// 解析涨幅跌幅榜
		ArrayList<StockTradeInfo> stockLedUpArray = new ArrayList<StockTradeInfo>();
		ArrayList<StockTradeInfo> stockLedDownArray = new ArrayList<StockTradeInfo>();
		
		jsonArray = data.getJSONArray("zhangfu");
		length = jsonArray.length();
		for(int i = 0; i<length; i++) {
			String stockID = jsonArray.getString(i);
			JSONObject jsonStockInfo = jsonQuotationInfo.getJSONObject(stockID);
			StockTradeInfo stockTradeInfo = StockTradeInfo.resolveSummaryJsonObject(jsonStockInfo);
			stockLedUpArray.add(stockTradeInfo);
		}
		
		jsonArray = data.getJSONArray("diefu");
		length = jsonArray.length();
		for(int i = 0; i<length; i++) {
			String stockID = jsonArray.getString(i);
			JSONObject jsonStockInfo = jsonQuotationInfo.getJSONObject(stockID);
			StockTradeInfo stockTradeInfo = StockTradeInfo.resolveSummaryJsonObject(jsonStockInfo);
			stockLedDownArray.add(stockTradeInfo);
		}

        // 解析沪港通
        ArrayList<StockTradeInfo> stockAHArray = new ArrayList<StockTradeInfo>();
        jsonArray = data.getJSONArray("HGTZhangFu");
        length = jsonArray.length();
        for(int i = 0; i<length; i++) {
            String stockID = jsonArray.getString(i);
            JSONObject jsonStockInfo = jsonQuotationInfo.getJSONObject(stockID);
            StockTradeInfo stockTradeInfo = StockTradeInfo.resolveSummaryJsonObject(jsonStockInfo);
            stockAHArray.add(stockTradeInfo);
        }

		// 回传解析好的数据
		Object[] callbackObjectArray = new Object[QUOTATIONS_OBJECT_COUNTS];
		callbackObjectArray[0] = stockIndexArray;
		callbackObjectArray[1] = industryLedUpArray;
		callbackObjectArray[2] = industryLedDownArray;
		callbackObjectArray[3] = stockLedUpArray;
		callbackObjectArray[4] = stockLedDownArray;
        callbackObjectArray[5] = stockAHArray;
		
		return callbackObjectArray;
	}
	
	/**
	 * 解析单个指数的详细信息
	 * @param response
	 * @throws JSONException
	 */
	public static StockIndex resolveIndexJsonObject(JSONObject response) throws JSONException {

        if (response == null) return  null;

		StockIndex stockIndex = new StockIndex();
		JSONObject data = response.getJSONObject("data");
        JSONObject indexes = data.getJSONObject("indexs");
        JSONObject marketStatus = data.getJSONObject("marketStatus");


		stockIndex.setCode(indexes.getString("ZSZSDM"));
		stockIndex.setName(indexes.getString("ZSZSQC"));
		
		stockIndex.setTodayIndex(indexes.optDouble("ZSKSZS"));
		stockIndex.setYesterdayIndex(indexes.optDouble("ZSSSZS"));
		
		stockIndex.setNowIndex(indexes.optDouble("ZSZJZS"));
		stockIndex.setHighestIndex(indexes.optDouble("ZSZGZS"));
		stockIndex.setLowestIndex(indexes.optDouble("ZSZDZS"));
		
		stockIndex.setTradeCount(indexes.optDouble("ZSCJSL"));
		stockIndex.setTradePrice(indexes.optDouble("ZSCJJE"));
        stockIndex.setDataTime(indexes.optLong("time"));

        // 涨家数 跌家数
        JSONObject stockCount = data.optJSONObject("stockCount");
        if (null != stockCount){
            stockIndex.setUpCount(stockCount.optInt("ZSZJS"));
            stockIndex.setDeuceCount(stockCount.optInt("ZSPJS"));
            stockIndex.setDownCount(stockCount.optInt("ZSDJS"));
        }


        stockIndex.setCurrentTime(marketStatus.optLong("time"));

        stockIndex.setMarketStatus(marketStatus.optInt("marketStatus", StockTradeInfo.MARKET_STATUS_NOT_SERVICE_TIME));


		return stockIndex;
	}

    public static StockIndex resolveIndexJsonSimpleObject(JSONObject data) throws JSONException {
        StockIndex stockIndex = new StockIndex();
        stockIndex.setCode(data.getString("ZSZSDM"));
        stockIndex.setName(data.getString("ZSZSQC"));

        stockIndex.setTodayIndex(data.getDouble("ZSKSZS"));
        stockIndex.setYesterdayIndex(data.getDouble("ZSSSZS"));

        stockIndex.setNowIndex(data.getDouble("ZSZJZS"));
        stockIndex.setHighestIndex(data.getDouble("ZSZGZS"));
        stockIndex.setLowestIndex(data.getDouble("ZSZDZS"));

        stockIndex.setTradeCount(data.getDouble("ZSCJSL"));
        stockIndex.setTradePrice(data.getDouble("ZSCJJE"));

        stockIndex.setDataTime(data.getLong("time"));

        stockIndex.setMarketStatus(data.optInt("marketStatus", StockTradeInfo.MARKET_STATUS_NOT_SERVICE_TIME));

        return stockIndex;
    }

    /**
     * 解析一组指数信息
     * @param indexJsonArray
     * @return
     * @throws JSONException
     */
    public static List<StockIndex> resolveAllIndexJsonObject(JSONArray indexJsonArray) throws JSONException {
        List<StockIndex> stockIndexes = new ArrayList<StockIndex>();

        for (int i = 0; i < indexJsonArray.length() ; i ++){
            JSONObject dataIndexJSON = indexJsonArray.getJSONObject(i);

            StockIndex item = new StockIndex();

            item.setCode(dataIndexJSON.getString("ZSZSDM"));
            item.setName(dataIndexJSON.getString("ZSZSQC"));

            item.setYesterdayIndex(dataIndexJSON.getDouble("ZSSSZS"));
            item.setNowIndex(dataIndexJSON.getDouble("ZSZJZS"));

            stockIndexes.add(item);
        }

        return stockIndexes;
    }


}
