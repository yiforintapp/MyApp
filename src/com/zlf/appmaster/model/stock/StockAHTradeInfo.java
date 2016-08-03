package com.zlf.appmaster.model.stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huang on 2015/3/12.
 */
public class StockAHTradeInfo extends StockTradeInfo {
    private StockTradeInfo mAStockInfo;          // 在A股上市的信息，可为空
    private float mExchangeRate = 1;                // 汇率
    private float mRateDiscount;


    public StockTradeInfo getAStockInfo() {
        return mAStockInfo;
    }

    public void setAStockInfo(StockTradeInfo mAStockInfo) {
        this.mAStockInfo = mAStockInfo;
    }

    /**
     * 获取溢价率
     * 获取溢价率 = （a股当前价－（h股当前价＊h股汇率））／（h股当前价＊h股汇率）＊100
     */
    public float getRateDiscount(){
        float ret = 0;
        if (mAStockInfo != null){
            ret = (mAStockInfo.getNowPrice() /(mNowPrice * mExchangeRate))-1;
        }
        return  ret;
    }
    /*public float getRateDiscount() {
        return mRateDiscount/100;
    }*/

    public void setRateDisCount(float rateDiscount) {
        this.mRateDiscount = rateDiscount;
    }
    public String getRateDiscountFormat(){
        return new DecimalFormat("0.00%").format(getRateDiscount());
    }
    public int getRateDiscountRiseInfo(){
        int ret = 0;

        float riseInfo = getRateDiscount();
        if(riseInfo	> 0)
            ret = 1;
        else if(riseInfo < 0)
            ret = -1;

        return ret;
    }

    public float getExchangeRate() {
        return mExchangeRate;
    }

    public void setExchangeRate(float exchangeRate) {
        this.mExchangeRate = exchangeRate;
    }

    /**
     * 解析港股信息
     * @param jsonAInfo A股信息
     * @param jsonHInfo 港股信息
     * @return
     * @throws JSONException
     */
    public static StockAHTradeInfo resolveAHSummaryJsonObject(JSONObject jsonAInfo, JSONObject jsonHInfo) throws JSONException {
        StockAHTradeInfo stockAHTradeInfo = new StockAHTradeInfo();

        stockAHTradeInfo.setAStockInfo(resolveSummaryJsonObject(jsonAInfo));

        stockAHTradeInfo.setCode(jsonHInfo.optString("HQZQDM"));
        //float HQJRKP = (float)jsonHInfo.optDouble("HQJRKP",0);  // 今日开盘价
        stockAHTradeInfo.setYesterdayPrice((float) jsonHInfo.optDouble("pspjh", 0));  // 昨日收盘价
        stockAHTradeInfo.setNowPrice((float) jsonHInfo.optDouble("spjh", 0));  // 按盘价
        stockAHTradeInfo.setExchangeRate((float)jsonHInfo.optDouble("hl",1));   // 汇率 （计算折价率时用）
        stockAHTradeInfo.setRateDisCount((float)jsonHInfo.optDouble("zjl",0));

        return stockAHTradeInfo;
    }


    public static List<StockAHTradeInfo> resolveJsonArray(JSONObject response) throws JSONException {
        List<StockAHTradeInfo> stockAHTradeInfoArrays = new ArrayList<StockAHTradeInfo>();
        JSONObject data = response.getJSONObject("data");
        JSONObject jsonAQuotationInfo = data.getJSONObject("AQuotations");
        JSONObject jsonHQuotationInfo = data.getJSONObject("HInfo");
        JSONArray jsonArray = data.getJSONArray("stockIds");
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            String stockID = jsonArray.getString(i);
            JSONObject jsonAStockInfo = jsonAQuotationInfo.getJSONObject(stockID);
            JSONObject jsonHStockInfo = jsonHQuotationInfo.getJSONObject(stockID);
            StockAHTradeInfo item = StockAHTradeInfo.resolveAHSummaryJsonObject(jsonAStockInfo, jsonHStockInfo);
            stockAHTradeInfoArrays.add(item);
        }

        return stockAHTradeInfoArrays;
    }



}
