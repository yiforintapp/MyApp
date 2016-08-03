package com.zlf.appmaster.bean;

import com.zlf.appmaster.model.stock.StockTradeInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StockHottestGroup extends StockGroup{
	
	public final static int TYPE_buyMax = 1;
	public final static int TYPE_upMax = 2;
	public final static int TYPE_moneyInMax = 3;
	public final static int TYPE_maxBuyMax = 4;
	public final static int TYPE_saleMax = 5;
	public final static int TYPE_downMax = 6;
	public final static int TYPE_moneyOutMax = 7;
	public final static int TYPE_maxSaleMax = 8;
	public final static int TYPE_maxSearch = 9;
	
	
	
	private ArrayList<String> mStockIds = new ArrayList<String>();
	private HashMap<String, String> mStockScoreMap = new HashMap<String, String>();
	
	
	private long mScore;
	
	public ArrayList<String> getStockIds(){
		return mStockIds;
	}
	
	public String getFormatStockIDStr(){
		String format = "";
		for(String s:mStockIds){
			 format += s + ",";
		}
		return format;
	}
	
	public HashMap<String, String> getStockScoreMap(){
		return mStockScoreMap;
	}
	
	// scoreFormat该分类中的提示信息,考虑无此键的情况，在内部捕获异常
	public static StockHottestGroup resolveHottestGroup(JSONObject jobj, String groupKey, int type){
		StockHottestGroup stockHottestGroup = null;

		try {
			JSONArray jsonArray = jobj.getJSONArray(groupKey);
			int len = jsonArray.length();
			if (len > 0) {
				stockHottestGroup = new StockHottestGroup();
				stockHottestGroup.setGroupName(getGroupName(type));
				for (int i = 0; i < len; i++) {

					JSONObject sub = jsonArray.getJSONObject(i);
					String member = sub.getString("member");
					stockHottestGroup.mStockIds.add(member);
					stockHottestGroup.mStockScoreMap.put(member,
							getGroupScoreFormat(type, sub.getLong("score")));

				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stockHottestGroup;
	}

    /**
     * 同上，有行情数据的情况
     * @param jObj
     * @param jsonQuotationInfo
     * @param groupKey
     * @param type
     * @return
     */
    public static StockHottestGroup resolveHottestGroup(JSONObject jObj, JSONObject jsonQuotationInfo, String groupKey, int type){
        StockHottestGroup stockHottestGroup = null;

        try {
            JSONArray jsonArray = jObj.getJSONArray(groupKey);
            int len = jsonArray.length();
            if (len > 0) {
                stockHottestGroup = new StockHottestGroup();
                stockHottestGroup.setGroupName(getGroupName(type));

                List<StockTradeInfo> quotationsArray = new ArrayList<StockTradeInfo>();
                for (int i = 0; i < len; i++) {

                    JSONObject sub = jsonArray.getJSONObject(i);
                    String member = sub.getString("member");
                    stockHottestGroup.mStockIds.add(member);
                    stockHottestGroup.mStockScoreMap.put(member,
                            getGroupScoreFormat(type, sub.getLong("score")));

                    if (null != jsonQuotationInfo){
                        JSONObject jsonStockInfo = jsonQuotationInfo.getJSONObject(member);
                        StockTradeInfo perStockTradeInfo = StockTradeInfo.resolveSummaryJsonObject(jsonStockInfo);
                        perStockTradeInfo.setScoreFormat(stockHottestGroup.getStockScoreMap().get(member));
                        quotationsArray.add(perStockTradeInfo);
                    }
                }

                stockHottestGroup.setStockTraceInfoArray(quotationsArray);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return stockHottestGroup;
    }

    public static List<StockHottestGroup> resolveHottestGroupArray(JSONObject response) throws JSONException {
        ArrayList<StockHottestGroup> stockHottestGroupArray = new ArrayList<StockHottestGroup>();
        JSONObject data = response.getJSONObject("data");
        JSONObject quotations = data.getJSONObject("quotations");
        StockHottestGroup group = StockHottestGroup.resolveHottestGroup(data, quotations, "maxSearch", StockHottestGroup.TYPE_maxSearch);
        if (group != null) stockHottestGroupArray.add(group);

        group = StockHottestGroup.resolveHottestGroup(data, quotations, "buyMax", StockHottestGroup.TYPE_buyMax);
        if (group != null) stockHottestGroupArray.add(group);

        group = StockHottestGroup.resolveHottestGroup(data, quotations, "saleMax", StockHottestGroup.TYPE_saleMax);
        if (group != null) stockHottestGroupArray.add(group);

        return stockHottestGroupArray;
    }

	public static String getGroupName(int type){
		String groupName = "";
		switch(type){
		case TYPE_buyMax:
			groupName = "迷你基金都在买：";
			break;
		case TYPE_upMax:
			groupName = "大家都看涨";
			break;
		case TYPE_moneyInMax:
			groupName = "资金流入最多";
			break;
		case TYPE_maxBuyMax:
			groupName = "最强大单买入";
			break;
		case TYPE_saleMax:
			groupName = "迷你基金都在卖：";
			break;
		case TYPE_downMax:
			groupName = "大家都看跌";
			break;
		case TYPE_moneyOutMax:
			groupName = "资金流出最多";
			break;
		case TYPE_maxSaleMax:
			groupName = "最强大单卖出";
			break;
		case TYPE_maxSearch:
			groupName = "骑牛用户都在搜：";
			break;
		}
		
		return groupName;
	}
	
	public static String getGroupScoreFormat(int type, long score){
		String ScoreFormat = "";
		switch(type){
		case TYPE_buyMax:
			ScoreFormat = String.format("今日买入%d笔", score);
			break;
		case TYPE_upMax:
			ScoreFormat = String.format("今日看涨%d人", score);
			break;
		case TYPE_moneyInMax:
			ScoreFormat = String.format("今日流入%.2f万元", (double)score/10000);
			break;
		case TYPE_maxBuyMax:
			ScoreFormat = String.format("今日大单%d笔", score);
			break;
		case TYPE_saleMax:
			ScoreFormat = String.format("今日卖出%d笔", score);
			break;
		case TYPE_downMax:
			ScoreFormat = String.format("今日看跌%d人", score);
			break;
		case TYPE_moneyOutMax:
			ScoreFormat = String.format("今日流出%.2f万", (double)score/10000);
			break;
		case TYPE_maxSaleMax:
			ScoreFormat = String.format("今日大单%d笔", score);
			break;
		case TYPE_maxSearch:
			ScoreFormat = String.format("%d次", score);
			break;
		}
		
		return ScoreFormat;
	}
	
	public long getScore() {
		return mScore;
	}

	public void setScore(long mScore) {
		this.mScore = mScore;
	}
}
