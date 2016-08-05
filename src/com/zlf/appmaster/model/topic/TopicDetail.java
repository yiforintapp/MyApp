package com.zlf.appmaster.model.topic;


import com.zlf.appmaster.model.combination.CombinationGain;
import com.zlf.appmaster.utils.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 题材详情
 * Created by Huang on 2015/6/19.
 */
public class TopicDetail {

    private float todayProfit;

    private int mCombinationCount;  //  该题材下的基金数
    private int mStockCount;        // 该题材下的股票数

    // gain line
    private ArrayList<CombinationGain> gainLine;
    private ArrayList<Float> hs300Line;


    public ArrayList<CombinationGain> getGainLine() {
        return gainLine;
    }

    public void setGainLine(ArrayList<CombinationGain> gainLine) {
        this.gainLine = gainLine;
    }

    public ArrayList<Float> getHs300Line() {
        return hs300Line;
    }

    public float getTodayProfit() {
        return todayProfit;
    }


    public void setHs300Line(ArrayList<Float> hs300Line) {
        this.hs300Line = hs300Line;
    }

    public void setTodayProfit(float todayProfit) {
        this.todayProfit = todayProfit;
    }

    public String getTodayProfitFormat(){
        String strFormat;
        if (todayProfit > 0){
            strFormat = "+%.2f%%";
        }
        else {
            strFormat = "%.2f%%";
        }
        return String.format(strFormat, todayProfit * 100f);
    }

    public int getCombinationCount() {
        return mCombinationCount;
    }

    public void setCombinationCount(int combinationCount) {
        this.mCombinationCount = combinationCount;
    }

    public int getStockCount() {
        return mStockCount;
    }

    public void setStockCount(int stockCount) {
        this.mStockCount = stockCount;
    }


    public void resolveJSONObject(JSONObject jsonObject) {

        try {
            JSONObject dataObject = jsonObject.getJSONObject("data");
            this.setTodayProfit((float)dataObject.optDouble("DProfit"));
            this.setCombinationCount(dataObject.optInt("FundsCnt"));
            this.setStockCount(dataObject.optInt("HoldOrdersCnt"));

            //line
            ArrayList<CombinationGain> gains = new ArrayList<CombinationGain>();
            setGainLine(gains);
            JSONArray gainArray = dataObject.optJSONArray("Profits");
            if (gainArray != null) {
                CombinationGain firstGain = new CombinationGain(0, dataObject.optLong("StartTime"));
                gains.add(firstGain);

                for (int i = 0; i < gainArray.length(); i++) {
                    JSONObject gainItem  = gainArray.getJSONObject(i);

                    CombinationGain gain = new CombinationGain(
                            (float) gainItem.optDouble("Profit"),
                            gainItem.optLong("Date"));
                    gains.add(gain);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        filterGainLine();
    }

    public void resolveJSONObject(JSONObject jsonObject, int topicDays) {

        try {
            JSONObject dataObject = jsonObject.getJSONObject("data");
          /*  switch (topicDays){
                case TopicDetailActivity.INTENT_5TH_DAYS:
                    this.setTodayProfit((float)dataObject.optDouble("Day005"));
                    break;
                case TopicDetailActivity.INTENT_20TH_DAYS:
                    this.setTodayProfit((float)dataObject.optDouble("Day020"));
                    break;
                case TopicDetailActivity.INTENT_60TH_DAYS:
                    this.setTodayProfit((float)dataObject.optDouble("Day060"));
                    break;
                case TopicDetailActivity.INTENT_TODAY:
                    this.setTodayProfit((float)dataObject.optDouble("DProfit"));
                    break;
                default:
                    break;
            }

            this.setCombinationCount(dataObject.optInt("FundsCnt"));
            this.setStockCount(dataObject.optInt("HoldOrdersCnt"));*/
            this.setTodayProfit((float)dataObject.optDouble("recentProfit"));
            //line
            ArrayList<CombinationGain> gains = new ArrayList<CombinationGain>();
            setGainLine(gains);
            JSONArray gainArray = dataObject.optJSONArray("topicProfits");
            if (gainArray != null) {
              /*  CombinationGain firstGain = new CombinationGain(0, dataObject.optLong("StartTime"));
                gains.add(firstGain);*/

                for (int i = 0; i < gainArray.length(); i++) {
                    JSONObject gainItem  = gainArray.getJSONObject(i);

                    CombinationGain gain = new CombinationGain(
                            (float) gainItem.optDouble("Profit"),
                            gainItem.optLong("Date"));
                    gains.add(gain);
                }
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
        filterGainLine();
    }


    /**
     * 过滤收益曲线
     * 时间不重复，顺序 不判断第一个点
     */
    private void filterGainLine(){
        if (gainLine == null || gainLine.size() == 0){
            return;
        }
        Collections.sort(gainLine);

        /**
         * 过滤掉同一天的,保留最新的
         */
        String dateString = TimeUtil.getYearAndDay(gainLine.get(gainLine.size() - 1).getTime());
        for (int i=gainLine.size()-2;i>=1;i--){//不判断第一个点
            String curDateString = TimeUtil.getYearAndDay(gainLine.get(i).getTime());
//            QLog.i(TAG,dateString+",compare:"+curDateString);
            if (dateString.equals(curDateString)){//同一天
                gainLine.remove(i);
            }else{
                dateString = curDateString;
            }
        }

    }


}
