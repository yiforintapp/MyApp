package com.zlf.appmaster.client;

import android.content.Context;

import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.model.combination.CombinationInfo;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.model.topic.TopicInfo;
import com.zlf.appmaster.utils.UrlConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Huang on 2015/6/12.
 */
public class TopicClient {
    private Context mContext;

    // 题材下股票排行
    private static final String PATH_GET_TOPIC = UrlConstants.RequestHQURLString + "/QiNiuApp/core/ranking.stock_in_topic.do?";
    // 题材下的基金
    private static final String PATH_GET_COMBINATION = UrlConstants.RequestHQURLString + "/QiNiuApp/core/zuHe.page8topic.do?";
    // 批量获取题材的排名
    private static final String PATH_GET_TOPIC_RANK = UrlConstants.RequestHQURLString + "/QiNiuApp/core/ranking.batch_topic.do?";
    // 获取该题材的收益
    private static final String PATH_GET_TOPIC_PROFIT = UrlConstants.RequestHQURLString + "/QiNiuApp/core/topic.getProfit.do?";

    private TopicClient(Context context){
        mContext = context;
    }
    private static TopicClient mInstance = null;
    public static TopicClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TopicClient(context);
        }
        return mInstance;
    }

    public void requestStockList(String topicID, int startIndex, int pageSize, final OnRequestListener requestFinished){
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.topic_id", topicID);
        param.put("param.start_index", String.valueOf(startIndex));
        param.put("param.page_size", String.valueOf(pageSize));
        param.put("param.sort", String.valueOf(1));
        UniversalRequest.requestUrl(mContext, PATH_GET_TOPIC, param,
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
                            ArrayList<StockTradeInfo> mStockArray = new ArrayList<StockTradeInfo>();
                            JSONObject data = response.getJSONObject("data");
                            JSONArray stockIDs = data.getJSONArray("stockIds");
                            JSONObject quotations = data.getJSONObject("AQuotations");
                            double recentProfit = data.optDouble("recentprofit", 0);

                            int len = stockIDs.length();
                            for(int i = 0; i < len; i++) {
                                JSONObject perStock = quotations.getJSONObject(stockIDs.getString(i));
                                mStockArray.add(StockTradeInfo.resolveSummaryJsonObject(perStock));
                            }
                            if(requestFinished != null) {
                                // call back
                                requestFinished.onDataFinish(mStockArray);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            if(requestFinished != null) {
                                requestFinished.onError(UrlConstants.CODE_JSON_ANALYSIS_ERROR, "");
                            }
                        }

                    }
                });
    }

    public void requestCombinationList(String topicID, long maxID, int pageSize, final OnRequestListener requestFinished){
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.topic", topicID);
        param.put("param.max_id", String.valueOf(maxID));
        param.put("param.page_size", String.valueOf(pageSize));
        UniversalRequest.requestUrl(mContext, PATH_GET_COMBINATION, param,
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

                        ArrayList<CombinationInfo> items = CombinationInfo.resolveList(mContext, response);
                        if (requestFinished != null) {
                            // call back
                            requestFinished.onDataFinish(items);
                        }


                    }
                });
    }

    public void requestTopicRankList(String topicIDs, final OnRequestListener requestFinished){
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.topics", topicIDs);
        UniversalRequest.requestUrl(mContext, PATH_GET_TOPIC_RANK, param,
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
                            Map<String,TopicInfo> items = TopicInfo.resolveJSONObjectArrayGetMap(response);
                            if (requestFinished != null) {
                                // save to cache
                                StockJsonCache.saveToFile(mContext,
                                        StockJsonCache.CACHEID_TOPIC_FAVORITE, response);
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

    public void requestTopicProfit(String topicID, int days, final OnRequestListener requestFinished){
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.id", topicID);
        param.put("param.preday", String.valueOf(days));
        UniversalRequest.requestUrl(mContext, PATH_GET_TOPIC_PROFIT, param, requestFinished);
    }


}
