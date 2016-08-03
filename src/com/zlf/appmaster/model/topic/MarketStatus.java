package com.zlf.appmaster.model.topic;

import org.json.JSONObject;

/**
 * Created by Huang on 2015/6/15.
 */
public class MarketStatus {
    public static final int STATUS_NORMAL = 0;			//	正常
    public static final int STATUS_STOP = 1;			//  停牌
    public static final int STATUS_OPEN_PREPARE = 2;	//  开盘前系统信息清理
    public static final int STATUS_LIMIT_UP = 3;		//  涨停
    public static final int STATUS_LIMIT_DOWN = 4;		//  跌停
    public static final int STATUS_TRADING_HALT = 5;    //  trading halt 交易中临时停牌

    public static final int MARKET_STATUS_NORMAL = 0;							// 正常交易时段
    public static final int MARKET_STATUS_CLOSE = 1;							// 休市
    public static final int MARKET_STATUS_NOT_SERVICE_TIME = 2;					// 当天非交易时段

    private boolean mIsHoliday;     // 是否是休息日
    private int mStatus;            // 市场状态
    private int mOpenStatus;        //


    public boolean isHoliday() {
        return mIsHoliday;
    }

    public void setIsHoliday(boolean isHoliday) {
        this.mIsHoliday = isHoliday;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public int getOpenStatus() {
        return mOpenStatus;
    }

    public void setOpenStatus(int openStatus) {
        this.mOpenStatus = openStatus;
    }


    public static MarketStatus resolveJson(JSONObject jsonMarketStatus){
        MarketStatus item = new MarketStatus();
        item.setIsHoliday(jsonMarketStatus.optBoolean("Holidays"));
        item.setOpenStatus(jsonMarketStatus.optInt("OpenStatus"));
        item.setStatus(jsonMarketStatus.optInt("MarketStatus"));

        return item;
    }
}
