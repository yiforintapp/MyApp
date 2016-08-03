package com.zlf.appmaster.client;

import android.content.Context;

import com.zlf.appmaster.model.stock.StockAgentHistoryItem;
import com.zlf.appmaster.model.stock.StockAgentItem;
import com.zlf.appmaster.model.stock.StockMyPosition;
import com.zlf.appmaster.utils.UrlConstants;
import com.zlf.appmaster.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StockMarketClient {
//	private final static String TAG = "StockFavoriteClient";
	private Context mContext;
	private StockMarketClient(Context context){
		mContext = context;
	}
	private static StockMarketClient mInstance = null;
	public static StockMarketClient getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new StockMarketClient(context);
		}
		return mInstance;
	}
	
	//持仓
	private static final String PATH_MY_POSITION  = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSHoldOrder.getHoldOrders.do?";
	//获取委托
	private static final String PATH_MY_AGENT  = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSTradeOrders.getOrders.do?";
	//获取历史委托
	private static final String PATH_MY_HISTORY_AGENT = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSTradeOrders.getHisOrders.do?";
	// 获取大家都在搜
	private static final String PATH_HOTTEST_SEARCH = UrlConstants.RequestHQURLString + "/QiNiuApp/core/statistic.getHotSearchStock.do?";
	//买入/卖出
	private static final String PATH_TRADE_ORDER  = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSTradeOrders.addOrders.do?";
	//撤销委托
	private static final String PATH_AGENT_CANCEL  = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSTradeOrders.cancelOrders.do?";
	//获取股票盈亏与市值
	private static final String PATH_TRADE_PROFIT  = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSHoldOrder.getTradeProfit.do?";

	public void requestMyPositionList( final OnRequestListener requestFinished){
		requestPositionListWithUin(Utils.getAccountUin(mContext),requestFinished);
	}
	
	public void requestPositionListWithUin(String uinString, final OnRequestListener requestFinished){
		Map<String, String> param = new HashMap<String, String>();
		param.put("param.fuin", uinString);
		UniversalRequest.requestUrl(mContext, PATH_MY_POSITION, param, new OnRequestListener() {
			
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
					//Log.i(TAG, "=======================>>requestMyPositionList:" + response.toString());
					ArrayList<StockMyPosition> myPrositionArray = new ArrayList<StockMyPosition>();
					JSONArray jsonArray = response.getJSONArray("data");
					int len = jsonArray.length();
					for(int i = 0; i < len; i++) {
						myPrositionArray.add(StockMyPosition.resolveJSONObject(jsonArray.getJSONObject(i)));
					}
					if(requestFinished != null)
						requestFinished.onDataFinish(myPrositionArray);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
	}
	
	
	
	public void requestMyAgent( final OnRequestListener requestFinished){
		Map<String, String> param = new HashMap<String, String>();
		UniversalRequest.requestUrl(mContext, PATH_MY_AGENT, param, new OnRequestListener() {
			
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
					ArrayList<StockAgentItem> stockAgentItemArray =  StockAgentItem.resolveJSONObjectArray(response);
					if(requestFinished != null)
						requestFinished.onDataFinish(stockAgentItemArray);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
	}
	
	
	public void requestMyHistoryAgent(int start, int size, final OnRequestListener requestFinished){
		Map<String, String> param = new HashMap<String, String>();
		param.put("param.currentPage", String.valueOf(start));
		param.put("param.pageSize", String.valueOf(size));
		UniversalRequest.requestUrl(mContext, PATH_MY_HISTORY_AGENT, param, new OnRequestListener() {
			
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
					ArrayList<StockAgentHistoryItem> stockAgentHistoryItemArray =
							StockAgentHistoryItem.resolveJSONObjectArray(response);
					if(requestFinished != null)
						requestFinished.onDataFinish(stockAgentHistoryItemArray);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});	 
	}

	/**
	 * 请求最热个股列表（更换无错误提示的接口 Date:20160308）
	 * @param startIndex
	 * @param endIndex
	 * @param requestFinished
	 */
	public void requestHottestSearch(int startIndex, int endIndex, final OnRequestListener requestFinished){
		
		Map<String, String> param = new HashMap<String, String>();
		param.put("param.startIndex", String.valueOf(startIndex));
		param.put("param.endIndex", String.valueOf(endIndex));
		param.put("param.sort", "1");
		
		UniversalRequest.requestBackground(mContext, PATH_HOTTEST_SEARCH, param, requestFinished);
	}
	
	/**
	 * 买入/卖出委托
	 * @param thradeType	1买		-1卖
	 * @param orderType		1 限价 	2市价
	 * @param stockId		股票ID
	 * @param stockNum		委托数量
	 * @param stockPrice	委托价格
	 * @param reason		交易理由
	 * @param requestFinished
	 */
	public void requestTradeOrder(int thradeType, int orderType, String stockId, int stockNum,
								  float stockPrice, String reason, final OnRequestListener requestFinished){
		Map<String, String> param = new HashMap<String, String>();
		param.put("param.treadType", String.valueOf(thradeType));
		param.put("param.orderType", String.valueOf(orderType));
		param.put("param.stockId", stockId);
		param.put("param.stockNum", String.valueOf(stockNum));
		param.put("param.stockPrice", String.valueOf(stockPrice));
		param.put("param.reason", reason);
		UniversalRequest.requestUrl(mContext, PATH_TRADE_ORDER, param, requestFinished);	 
		
//		MobclickAgent.onEvent(mContext, UmengCustom.FUNNEL_TRADE_FINISH);
		
	}
	
	
	// 撤销委托
	public void requestAgentCancel(String agentId, final OnRequestListener requestFinished){
		
		Map<String, String> param = new HashMap<String, String>();
		param.put("param.treadId", agentId);

		UniversalRequest.requestUrl(mContext, PATH_AGENT_CANCEL, param, requestFinished);	 
		
//		MobclickAgent.onEvent(mContext, UmengCustom.FUNNEL_TRADE_FINISH);
	}
	
	//获取股票盈亏及市值
	public void requestTradeProfit(final OnRequestListener requestFinished) {
		UniversalRequest.requestUrl(mContext, PATH_TRADE_PROFIT, null, requestFinished);	 
	}
	
}
