package com.zlf.appmaster.model.stock;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Huang on 2015/4/27.
 */
public class StockAsset implements Serializable {
    private long mAFloatListed;           // 流通股数
    private long mTotalShares;            // 总股数


    private double m52WeeksMaxPrice;        // 52周最高价
    private double m52WeeksMinPrice;        // 52周最低价
    private double mPeRatio;                // 市盈率
    private double mBookValue;              // 市净率
    private double mEPS;                    // 每股收益率Earnings Per Share
    private double mShareRepurchase;        // 每股净资产

    public StockAsset(){

    }

    public StockAsset(StockAsset stockAsset){
        copy(stockAsset);
    }


    public void copy(StockAsset stockAsset){
        mAFloatListed = stockAsset.mAFloatListed;           // 总股数
        mTotalShares = stockAsset.mTotalShares;            // 流通股数


        m52WeeksMaxPrice = stockAsset.m52WeeksMaxPrice;        // 52周最高价
        m52WeeksMinPrice = stockAsset.m52WeeksMinPrice;        // 52周最低价
        mPeRatio = stockAsset.mPeRatio;                // 市盈率
        mBookValue = stockAsset.mBookValue;              // 市净率
        mEPS = stockAsset.mEPS;                    // 每股收益率Earnings Per Share
        mShareRepurchase = stockAsset.mShareRepurchase;        // 每股净资产
    }

    public long getAFloatListed() {
        return mAFloatListed;
    }

    public void setAFloatListed(long AFloatListed) {
        this.mAFloatListed = AFloatListed;
    }

    public long getTotalShares() {
        return mTotalShares;
    }

    public void setTotalShares(long totalShares) {
        this.mTotalShares = totalShares;
    }

    public double get52WeeksMaxPrice() {
        return m52WeeksMaxPrice;
    }

    public void set52WeeksMaxPrice(double m52WeeksMaxPrice) {
        this.m52WeeksMaxPrice = m52WeeksMaxPrice;
    }

    public double get52WeeksMinPrice() {
        return m52WeeksMinPrice;
    }

    public void set52WeeksMinPrice(double m52WeeksMinPrice) {
        this.m52WeeksMinPrice = m52WeeksMinPrice;
    }

    public double getPeRatio() {
        return mPeRatio;
    }

    public void setPeRatio(double peRatio) {
        this.mPeRatio = peRatio;
    }

    public double getBookValue() {
        return mBookValue;
    }

    public void setBookValue(double bookValue) {
        this.mBookValue = bookValue;
    }

    public double getEPS() {
        return mEPS;
    }

    public void setEPS(double EPS) {
        this.mEPS = EPS;
    }

    public double getShareRepurchase() {
        return mShareRepurchase;
    }

    public void setShareRepurchase(double shareRepurchase) {
        this.mShareRepurchase = shareRepurchase;
    }


    public static StockAsset resolveJSONObject(JSONObject stockAssetJSON) throws JSONException {
        StockAsset item = new StockAsset();
        item.setAFloatListed(stockAssetJSON.optLong("AFloatListed"));
        item.setTotalShares(stockAssetJSON.optLong("totalShares"));

        JSONObject dataJSON = stockAssetJSON.getJSONObject("data");
        item.set52WeeksMaxPrice(dataJSON.optDouble("zgj",0));
        item.set52WeeksMinPrice(dataJSON.optDouble("zdj",0));
        item.setPeRatio(dataJSON.optDouble("syl",0));          // 市盈率
        item.setBookValue(dataJSON.optDouble("sjl",0));        // 市净率
        item.setEPS(dataJSON.optDouble("mgsy",0));
        item.setShareRepurchase(dataJSON.optDouble("mgjzc",0));

        return item;
    }
}
