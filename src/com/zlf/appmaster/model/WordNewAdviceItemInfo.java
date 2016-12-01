package com.zlf.appmaster.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/1.
 */
public class WordNewAdviceItemInfo implements Serializable {

    private String mDeal;
    private String mRealName;
    private String mTime;
    private String mEnterPoint;
    private String mProfit;
    private String mLose;
    private String mRemark;

    public String getDeal() {
        return mDeal;
    }

    public void setDeal(String mDeal) {
        this.mDeal = mDeal;
    }

    public String getRealName() {
        return mRealName;
    }

    public void setRealName(String mRealName) {
        this.mRealName = mRealName;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }

    public String getEnterPoint() {
        return mEnterPoint;
    }

    public void setEnterPoint(String mEnterPoint) {
        this.mEnterPoint = mEnterPoint;
    }

    public String getProfit() {
        return mProfit;
    }

    public void setProfit(String mProfit) {
        this.mProfit = mProfit;
    }

    public String getLose() {
        return mLose;
    }

    public void setLose(String mLose) {
        this.mLose = mLose;
    }

    public String getRemark() {
        return mRemark;
    }

    public void setRemark(String mRemark) {
        this.mRemark = mRemark;
    }
}
