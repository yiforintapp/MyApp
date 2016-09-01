package com.zlf.appmaster.client;

import android.content.Context;

import com.zlf.appmaster.bean.StockHottestGroup;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.model.industry.IndustryInfo;
import com.zlf.appmaster.model.stock.StockAHTradeInfo;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.model.stock.StockRZRQItem;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.model.topic.TopicInfo;
import com.zlf.appmaster.utils.UrlConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Huang on 2015/3/6.
 */
public class StockQuotationsClient {

    private Context mContext;

    // 获取行情相关信息
    private static final String PATH_STOCK_INDEX_ALL_INFO = UrlConstants.RequestHQURLString + "/QiNiuApp/core/index.indexList.do?";
    private static final String PATH_STOCK_HOT_INFO       = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsCenter.hotStocks.do?";
    private static final String PATH_STOCK_RISE_INFO      = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsCenter.stockSort.do?";
    private static final String PATH_STOCK_INDUSTRY_INFO  = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsData.getAllIndustry.do?";
    private static final String PATH_STOCK_PLATE_INFO     = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsCenter.plate.do?";
    private static final String PATH_STOCK_AH_INFO        = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsCenter.AH.do?";
    private static final String PATH_STOCK_RZRQ_INFO      = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsCenter.rzRq.do?";
    private static final String PATH_STOCK_RZRQ_INFO_BY_TYPE      = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsCenter.rzRq4Page.do?";
    private static final String PATH_STOCK_TOPIC_INFO = UrlConstants.RequestHQURLString + "/QiNiuApp/core/ranking.topics.do?";



    private StockQuotationsClient(Context context){
        mContext = context;
    }
    private static StockQuotationsClient mInstance = null;
    public static StockQuotationsClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StockQuotationsClient(context);
        }
        return mInstance;
    }


    /**
     * 获取最热个股
     */
    public void requestStockHot(final OnRequestListener requestFinished){

        UniversalRequest.requestUrl(mContext, PATH_STOCK_HOT_INFO, null,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        requestFinished.onError(errorCode, errorString);
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub
                        JSONObject response = (JSONObject) object;
                        try {

                            List<StockHottestGroup> stockHottestGroupArray = StockHottestGroup.resolveHottestGroupArray(response);

                            if(requestFinished != null){
                                // save to cache
                                StockJsonCache.saveToFile(mContext,
                                        StockJsonCache.CACHEID_QUOTATIONS_HOT_STOCK, response);
                                requestFinished.onDataFinish(stockHottestGroupArray);
                            }


                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 获取指数详情
     */
    public void requestStockIndexAll(final OnRequestListener requestFinished){

        UniversalRequest.requestUrl(mContext, PATH_STOCK_INDEX_ALL_INFO, null,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        requestFinished.onError(errorCode, errorString);
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub
                        JSONObject response = (JSONObject) object;
                        Object objectRet[] = new Object[2];     // 固定2个回调者要知道顺序

                        try {
                            JSONObject data = response.getJSONObject("data");

                            List<StockIndex> stockIndexes = StockIndex
                                    .resolveAllIndexJsonObject(data.getJSONArray("indexs"));
                            objectRet[0] = stockIndexes;

                            List<StockIndex> foreignDelayIndexes = StockIndex
                                    .resolveAllIndexJsonObject(data.getJSONArray("slowIndexs"));        // 国外的延迟指数
                            objectRet[1] = foreignDelayIndexes;

                            if (requestFinished != null) {
                                // save to cache
                                StockJsonCache.saveToFile(mContext,
                                        StockJsonCache.CACHEID_QUOTATIONS_INDEX, response);
                                // call back
                                requestFinished.onDataFinish(objectRet);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void requestNewIndexAll(final OnRequestListener requestFinished, String url){

        UniversalRequest.requestNewUrlWithTimeOut("Tag", mContext, url,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        requestFinished.onError(errorCode, errorString);
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub
                        Object objectRet[] = new Object[1];     // 固定2个回调者要知道顺序
                        try {
                            JSONArray response = (JSONArray) object;
                            List<StockIndex> stockIndexes = new ArrayList<StockIndex>();
                            for (int i = 0; i < response.length(); i ++) {
                                JSONObject dataIndexJSON = response.getJSONObject(i);

                                StockIndex item = new StockIndex();

                                item.setCode(dataIndexJSON.getString("ZSZSDM"));
                                item.setName(dataIndexJSON.getString("ZSZSQC"));

                                item.setYesterdayIndex(dataIndexJSON.getDouble("ZSSSZS"));
                                item.setNowIndex(dataIndexJSON.getDouble("ZSZJZS"));

                                item.setTodayIndex(dataIndexJSON.getDouble("open"));
                                item.setHighestIndex(dataIndexJSON.getDouble("high"));
                                item.setLowestIndex(dataIndexJSON.getDouble("low"));

                                stockIndexes.add(item);
                            }
                            objectRet[0] = stockIndexes;

                            if (requestFinished != null) {
                                // save to cache
//                                StockJsonCache.saveToFile(mContext,
//                                        StockJsonCache.CACHEID_QUOTATIONS_INDEX, response);
                                // call back
                                requestFinished.onDataFinish(objectRet);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }, false, 0, false);

    }


    public void requestNewIndexItem(final OnRequestListener requestFinished, String url){

        UniversalRequest.requestNewUrlWithTimeOut("Tag", mContext, url,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        requestFinished.onError(errorCode, errorString);
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub
                        Object objectRet[] = new Object[1];     // 固定2个回调者要知道顺序
                        try {
                            JSONArray response = (JSONArray) object;
                            List<StockIndex> stockIndexes = new ArrayList<StockIndex>();
                            for (int i = 0; i < response.length(); i ++) {
                                JSONObject dataIndexJSON = response.getJSONObject(i);

                                StockIndex item = new StockIndex();

                                item.setCode(dataIndexJSON.getString("code"));
                                item.setName(dataIndexJSON.getString("name"));

                                item.setTodayIndex(dataIndexJSON.optDouble("open"));
                                item.setYesterdayIndex(dataIndexJSON.optDouble("lastclose"));
                                item.setNowIndex(dataIndexJSON.optDouble("sell"));
                                item.setHighestIndex(dataIndexJSON.optDouble("high"));
                                item.setLowestIndex(dataIndexJSON.optDouble("low"));

                                item.setTradeCount(99999);
                                item.setTradePrice(99999999);

                                item.setDataTime(dataIndexJSON.optLong("quoteTime"));

                                item.setUpCount(999);
                                item.setDeuceCount(999);
                                item.setDownCount(999);
                                item.setIsOPen(dataIndexJSON.getString("status"));

                                item.setCurrentTime(System.currentTimeMillis());

                                stockIndexes.add(item);

                            }
                            objectRet[0] = stockIndexes;

                            if (requestFinished != null) {
                                // save to cache
//                                StockJsonCache.saveToFile(mContext,
//                                        StockJsonCache.CACHEID_QUOTATIONS_INDEX, response);
                                // call back
                                requestFinished.onDataFinish(objectRet);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }, false, 0, false);

    }


    /**
     * 获取涨跌幅板块
     */
    public void requestStockRiseInfo(final OnRequestListener requestFinished){

        UniversalRequest.requestUrl(mContext, PATH_STOCK_RISE_INFO, null,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        requestFinished.onError(errorCode, errorString);
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub
                        JSONObject response = (JSONObject) object;
                        // 解析涨幅跌幅榜
                        Object dataArray[] = new Object[2];
                        ArrayList<StockTradeInfo> stockLedUpArray = new ArrayList<StockTradeInfo>();
                        ArrayList<StockTradeInfo> stockLedDownArray = new ArrayList<StockTradeInfo>();
                        try {
                            JSONObject data = response.getJSONObject("data");
                            JSONObject jsonQuotationInfo = data.getJSONObject("quotations");
                            JSONArray jsonArray = data.getJSONArray("zhangfu");
                            int length = jsonArray.length();
                            for (int i = 0; i < length; i++) {
                                String stockID = jsonArray.getString(i);
                                JSONObject jsonStockInfo = jsonQuotationInfo.getJSONObject(stockID);
                                StockTradeInfo stockTradeInfo = StockTradeInfo.resolveSummaryJsonObject(jsonStockInfo);
                                stockLedUpArray.add(stockTradeInfo);
                            }

                            jsonArray = data.getJSONArray("diefu");
                            length = jsonArray.length();
                            for (int i = 0; i < length; i++) {
                                String stockID = jsonArray.getString(i);
                                JSONObject jsonStockInfo = jsonQuotationInfo.getJSONObject(stockID);
                                StockTradeInfo stockTradeInfo = StockTradeInfo.resolveSummaryJsonObject(jsonStockInfo);
                                stockLedDownArray.add(stockTradeInfo);
                            }

                            dataArray[0] = stockLedUpArray;
                            dataArray[1] = stockLedDownArray;

                            if (requestFinished != null) {
                                // save to cache
                                StockJsonCache.saveToFile(mContext,
                                        StockJsonCache.CACHEID_QUOTATIONS_RISE_INFO, response);

                                // call back
                                requestFinished.onDataFinish(dataArray);
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                });
    }


    /**
     * 获取板块详情
     * @param requestFinished
     */
    public void requestStockPlateInfo(final OnRequestListener requestFinished){

        UniversalRequest.requestUrl(mContext, PATH_STOCK_PLATE_INFO, null,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        requestFinished.onError(errorCode, errorString);
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub
                        JSONObject response = (JSONObject) object;
                        // 解析涨幅跌幅榜
                        Object dataArray[] = new Object[2];
                        // 解析领涨领跌行业
                        List<IndustryInfo> industryLedUpArray = new ArrayList<IndustryInfo>();
                        List<IndustryInfo> industryLedDownArray = new ArrayList<IndustryInfo>();
                        try {
                            JSONObject data = response.getJSONObject("data");
                            JSONObject jsonQuotationInfo = data.getJSONObject("quotations");
                            JSONArray jsonArray = data.getJSONArray("lingzhang");
                            int length = jsonArray.length();
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

                            dataArray[0] = industryLedUpArray;
                            dataArray[1] = industryLedDownArray;

                            if (requestFinished != null) {
                                // call back
                                requestFinished.onDataFinish(dataArray);
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                });
    }


    /**
     * 获取A/H详情
     */
    public void requestStockAHInfo(int startIndex, int endIndex, final OnRequestListener requestFinished){
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.start", String.valueOf(startIndex));
        param.put("param.end", String.valueOf(endIndex));
        param.put("param.isAsc", String.valueOf(1));
        UniversalRequest.requestUrl(mContext, PATH_STOCK_AH_INFO, param,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        requestFinished.onError(errorCode, errorString);
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub

                        JSONObject response = (JSONObject) object;
                        try {

                            List<StockAHTradeInfo> stockAHTradeInfoArrays =StockAHTradeInfo.resolveJsonArray(response);

                            if (requestFinished != null) {
                                // save to cache
                                StockJsonCache.saveToFile(mContext,
                                        StockJsonCache.CACHEID_QUOTATIONS_AH, response);
                                // call back
                                requestFinished.onDataFinish(stockAHTradeInfoArrays);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }



                    }
                });
    }


    /**
     * 获取融资融券信息
     */
    public void requestStockRZRQ(final OnRequestListener requestFinished){

        UniversalRequest.requestUrl(mContext, PATH_STOCK_RZRQ_INFO, null,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        requestFinished.onError(errorCode, errorString);
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub
                        JSONObject response = (JSONObject) object;
                        try {
                            Object objectRZRQArray[] = StockRZRQItem.resolveRZRQJSONObject(response);
                            if (requestFinished != null) {
                                // save to cache
                                StockJsonCache.saveToFile(mContext,
                                        StockJsonCache.CACHEID_QUOTATIONS_RZRQ, response);
                                // call back
                                requestFinished.onDataFinish(objectRZRQArray);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            if (requestFinished != null) {
                                requestFinished.onError(UrlConstants.CODE_JSON_ANALYSIS_ERROR, "");
                            }
                        }
                    }
                });
    }


    /**
     *
     * @param startIndex
     * @param endIndex
     * @param type param.type=rz|rq  rz:代表融资|rq代表融券
     * @param requestFinished
     */
    public void requestStockRZRQByType(int startIndex, int endIndex, final String type, final OnRequestListener requestFinished){
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.start", String.valueOf(startIndex));
        param.put("param.end", String.valueOf(endIndex));
        param.put("param.type", type);
        UniversalRequest.requestUrl(mContext, PATH_STOCK_RZRQ_INFO_BY_TYPE, param,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        requestFinished.onError(errorCode, errorString);
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub
                       JSONObject response = (JSONObject) object;
                        try {
                            List<StockRZRQItem> items = new ArrayList<StockRZRQItem>();

                            JSONObject data = response.getJSONObject("data");
                            JSONObject quotations = data.getJSONObject("quotations");
                            JSONArray rzrqJson = data.getJSONArray(type);
                            int len =rzrqJson.length();
                            for (int i = 0; i < len ; i++){
                                JSONObject perItem = rzrqJson.getJSONObject(i);
                                items.add(StockRZRQItem.resolveJSONObject(perItem, quotations));
                            }


                            if (requestFinished != null) {
                                // call back
                                 requestFinished.onDataFinish(items);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            if (requestFinished != null) {
                                requestFinished.onError(UrlConstants.CODE_JSON_ANALYSIS_ERROR, "");
                            }
                        }
                    }
                });
    }

    /**
     * 获取题材列表
     * @param startIndex
     * @param pageSize
     * @param requestFinished
     */
    public void requestTopicInfo(int startIndex, int pageSize, final OnRequestListener requestFinished){
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.start_index", String.valueOf(startIndex));
        param.put("param.page_size", String.valueOf(pageSize));
        UniversalRequest.requestUrl(mContext, PATH_STOCK_TOPIC_INFO, param,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        requestFinished.onError(errorCode, errorString);
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub

                        JSONObject response = (JSONObject) object;

                        try {
                            List<TopicInfo> items = TopicInfo.resolveJSONObjectArray(response);
                            if (requestFinished != null) {
                                // save to cache
                                StockJsonCache.saveToFile(mContext,
                                        StockJsonCache.CACHEID_QUOTATIONS_TOPIC, response);
                                // call back
                                requestFinished.onDataFinish(items);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            if (requestFinished != null) {
                                requestFinished.onError(UrlConstants.CODE_JSON_ANALYSIS_ERROR, "");
                            }
                        }

                    }
                });
    }

}
