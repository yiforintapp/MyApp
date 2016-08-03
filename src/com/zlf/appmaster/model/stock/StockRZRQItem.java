package com.zlf.appmaster.model.stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huang on 2015/4/23.
 */
public class StockRZRQItem {
    private double mZZLV;       // 折价率
    private double mRZYE;       // 融资余额
    private double mRQYE;       // 融券余额
    private long mTime;         // 数据时间

    private StockTradeInfo mStockTradeInfo;     // 股票交易明细

    public double getZZLV() {
        return mZZLV;
    }
    public String getZZLVFormat(){
        return new DecimalFormat("0.00%").format(mZZLV);
    }

    public void setZZLV(double ZZLV) {
        this.mZZLV = ZZLV;
    }

    public double getRZYE() {
        return mRZYE;
    }

    public String getRZYEFormat(){
        return  new DecimalFormat("0.00").format(mRZYE);
    }

    public void setRZYE(double RZYE) {
        this.mRZYE = RZYE;
    }

    public double getRQYE() {
        return mRQYE;
    }
    public String getRQYEFormat(){
        return  new DecimalFormat("0.0000").format(mRQYE);
    }

    public void setRQYE(double RQYE) {
        this.mRQYE = RQYE;
    }

    public StockTradeInfo getStockTradeInfo() {
        return mStockTradeInfo;
    }

    public void setStockTradeInfo(StockTradeInfo stockTradeInfo) {
        this.mStockTradeInfo = stockTradeInfo;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        this.mTime = time;
    }

    /**
     *
     * @param jsonObject
     * @return Object[0] 为融资  Object[1]为融券
     */
    public static Object[] resolveRZRQJSONObject(JSONObject jsonObject) throws JSONException {
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject quotations = data.getJSONObject("quotations");
        JSONArray rzJson = data.getJSONArray("rz");
        JSONArray rqJson = data.getJSONArray("rq");

        List<StockRZRQItem> itemsRZ = new ArrayList<StockRZRQItem>();
        for (int i = 0; i < rzJson.length(); i++){
            JSONObject perItem = rzJson.getJSONObject(i);
            itemsRZ.add(resolveJSONObject(perItem, quotations));
        }

        List<StockRZRQItem> itemsRQ = new ArrayList<StockRZRQItem>();
        for (int i = 0; i < rqJson.length(); i++){
            JSONObject perItem = rqJson.getJSONObject(i);
            itemsRQ.add(resolveJSONObject(perItem, quotations));
        }

        Object itemsRet[] = new Object[2];
        itemsRet[0] = itemsRZ;
        itemsRet[1] = itemsRQ;

        return  itemsRet;
    }


    public static StockRZRQItem resolveJSONObject(JSONObject perItem, JSONObject quotations) throws JSONException {

        StockRZRQItem item = new StockRZRQItem();
        item.setRQYE(perItem.optDouble("rqye"));
        item.setRZYE(perItem.optDouble("rzye"));
        item.setZZLV(perItem.optDouble("zzlv"));
        item.setTime(perItem.optLong("dt"));
        item.setStockTradeInfo(StockTradeInfo.resolveSummaryJsonObject(quotations.getJSONObject(perItem.getString("stockId"))));

        return item;
    }


}
