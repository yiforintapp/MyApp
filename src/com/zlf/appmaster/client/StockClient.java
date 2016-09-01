package com.zlf.appmaster.client;

import android.content.Context;
import android.text.TextUtils;

import com.zlf.appmaster.bean.LivePrepareStockInfo;
import com.zlf.appmaster.bean.StockFinance;
import com.zlf.appmaster.bean.StockHottestGroup;
import com.zlf.appmaster.bean.StockMoney;
import com.zlf.appmaster.bean.StockStrategyItem;
import com.zlf.appmaster.bean.StockSummary;
import com.zlf.appmaster.bean.StrategyStockItem;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.chartview.bean.StockKLine;
import com.zlf.appmaster.chartview.bean.StockMinutes;
import com.zlf.appmaster.db.stock.StockTradeTable;
import com.zlf.appmaster.model.industry.IndustryInfo;
import com.zlf.appmaster.model.stock.StockFavoriteItem;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.utils.UrlConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 股票  -- 客户端层
 *
 * @author Deping Huang
 */
public class StockClient {

    private final static String TAG = "StockClient";
    private Context mContext;

    public StockClient(Context context) {
        mContext = context;
    }

    //k线类型
    public static final int KLINE_TYPE_DAILY = 1;
    public static final int KLINE_TYPE_WEEKLY = 2;
    public static final int KLINE_TYPE_MONTHLY = 3;

    //股票or指数
    public static final int TYPE_STOCK = 0;
    public static final int TYPE_INDEX = 1;

    // 获取股票简明信息
    private static final String PATH_STOCK_SIMPLE_INFO = UrlConstants.RequestHQURLString + "/QiNiuApp/core/stock.getBase.do?";
    private static final String PATH_STOCK_HOLDERS_INFO = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSQuotations.ready2Trading.do?";
    // 获取三大指数的具体信息
    private static final String PATH_STOCK_INDEX_INFO = UrlConstants.RequestHQURLString + "/QiNiuApp/core/index.getIndexDetail.do?";
    // 获取所有股票list
    private static final String PATH_ALL_STOCK_INFO = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSStock.getAllStock.do?";

    // 获取领涨领跌行业股票列表
    private static final String PATH_INDUSTRY_POPULAR_STOCK = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSQuotations.getIndustryPopularStock.do?";
    // 获取更多领涨领跌行业
    private static final String PATH_POPULAR_INDUSTRY = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSQuotations.getIndustryList.do?";
    // 获取更多股票信息 type 涨/跌
    private static final String PATH_POPULAR_STOCK = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSQuotations.getAllStockRanking.do?";
    // 获取沪港通股票信息
    private static final String PATH_AH_STOCK = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSQuotations.getAllHGTRanking.do?";
    // 获取最热个股页面
    private static final String PATH_HOT_STOCK = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSRanking.hotStocks.do?";
    // 上传"大家都在搜"
    private static final String PATH_UPLOAD_HOT_SEARCH = UrlConstants.RequestHQURLString + "/QiNiuApp/core/statistic.hotSearchStock.do?";
    // 获取股票组
    private static final String PATH_STOCK_GROUP_INFO = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSQuotations.getSimpleMsgByStockIds.do?";
    // 获取行情信息
    private static final String PATH_STOCK_QUOTATIONS_INFO = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsCenter.getHQ8Ids.do?";

    // 获取某个交易所的涨幅榜
    private static final String PATH_INDEX_POPULAR_STOCK = UrlConstants.RequestHQURLString + "/QiNiuApp/core/index.getIndexStockRanking.do?";


    // 个股概况Path
    private static final String PATH_STOCK_SUMMARY = UrlConstants.RequestHQURLString + "/QiNiuApp/core/companyInfo.getStockBasicInfo8Id.do?";
    // 个股资金
    private static final String PATH_STOCK_MONEY = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSNews.getFlows8sIdODate.do?";

    // 个股财务
    private static final String PATH_STOCK_FINANCE = UrlConstants.RequestHQURLString + "/QiNiuApp/core/companyInfo.getStockFinance.do?";
    // 获取个股评论
    // QiNiuApp/core/IOSComment.getStockComment.do?header.version=1.1.0.0&header.imei=606789CC-27C8-408E-A007-3BF6C1BE2625&header.sessionId=8bdSCZY0EGSBhRkFh4iSIA==&param.uin=927033096317046784&param.stockId=002636&param.currentPage=1&param.pageSize=10

    // 获取个股动态
    // /QiNiuApp/core/IOSStockDy.getStockDynamic.do?header.version=1.1.0.0&header.imei=606789CC-27C8-408E-A007-3BF6C1BE2625&header.sessionId=8bdSCZY0EGSBhRkFh4iSIA==&param.uin=927033096317046784&param.stockId=002636&param.checkPoint=0&param.backNum=10

    // 获取推荐股票策略列表
    private static final String PATH_STOCK_STRATEGY_LIST = UrlConstants.RequestHQURLString + "/QiNiuApp/core/ceLue.getStockList.do?";
    // 该策略最优股
    private static final String PATH_STRATEGY_STOCK_BEST_ONE = UrlConstants.RequestHQURLString + "/QiNiuApp/core/ceLue.getNo1Stock.do?";
    // 某个策略分类下的股票列表
    private static final String PATH_STRATEGY_STOCK_LIST = UrlConstants.RequestHQURLString + "/QiNiuApp/core/ceLue.getRanking8Key.do?";


    //分时信息
    private static final String PATH_STOCK_MINUTE = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsData.minute.do?";
    private static final String PATH_STOCK_MINUTE_LASTTIME = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsData.minute8LastTime.do?";
    //k线
    private static final String PATH_STOCK_KLINE = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsData.kLine.do?";
    //个股详情
    private static final String PATH_INDEX_DETAIL = UrlConstants.RequestHQURLString + "/QiNiuApp/core/IOSStockIndex.getIndexDetail.do?";
    //盘口信息
    private static final String PATH_STOCK_HANDICAP = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsCenter.getHQ8StockId.do?";

    //个股维度信息
    private static final String PATH_STOCK_DIMENSION = UrlConstants.RequestHQURLString + "/QiNiuApp/core/weidu.getAll.do?";
    //个股单一五维图的信息
    private static final String PATH_STOCK_DIMENSION_WITH_KEY = UrlConstants.RequestHQURLString + "/QiNiuApp/core/weidu.getWeiDuChartData.do?";
    //获取该股票可比的所有股票列表
    private static final String PATH_DIMENSION_COMPARE_LIST = UrlConstants.RequestHQURLString + "/QiNiuApp/core/weidu.getCompareStockIds.do?";
    //获取比较的股票曲线数据
    private static final String PATH_DIMENSION_LINE_DATA = UrlConstants.RequestHQURLString + "/QiNiuApp/core/weidu.compareWeiDuData.do?";
    //批量获取比较的曲线数据
    private static final String PATH_BATCH_DIMENSION_LINE_DATA = UrlConstants.RequestHQURLString + "/QiNiuApp/core/weidu.getBatchWeiDu.do?";

    //他人自选股
    private static final String PATH_GET_HIS_SELF_STOCK = UrlConstants.RequestHQURLString + "/QiNiuApp/core/goods.getOthers.do?";
    //直播间股票详细信息
    private static final String PATH_GET_STOCK_INFO_DETAIL = UrlConstants.RequestHQURLString + "/QiNiuApp/core/quotationsCenter.getHQDetail8StockId.do?";
    //直播间股指详细信息
    private static final String PATH_GET_INDEX_INFO_DETAIL = UrlConstants.RequestHQURLString + "/QiNiuApp/core/index.getIndexDetail8IndexId.do?";


    // 获取K线图
    public static String getStockKLine(Context context, String code, String type) {
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", code);
        param.put("param.type", type);
        UniversalRequest.addHeader(context, param);

        return UrlConstants.RequsetBaseURLString +
                "/QiNiuApp/core/stockImg.getStockLine.do?" + UniversalRequest.getMapString(param);
    }

    public String getMinLine(String code) {
        // 获取新浪K线图
        /*
        String codeFormat;
		if(code.startsWith("60")){
			codeFormat = "sh" + code;
		}
		else{
			codeFormat = "sz" + code;
		}
		return String.format("http://image.sinajs.cn/newchart/min/n/%s.gif", codeFormat);
		return String.format("http://image.sinajs.cn/newchart/daily/n/%s.gif", codeFormat);
		return String.format("http://image.sinajs.cn/newchart/weekly/n/%s.gif", codeFormat);
		return String.format("http://image.sinajs.cn/newchart/monthly/n/%s.gif", codeFormat);
		*/
        return getStockKLine(mContext, code, "minuteLine");

    }

    public String getDailyLine(String code) {
        return getStockKLine(mContext, code, "dayKLine");
    }

    public String getWeeklyLine(String code) {
        return getStockKLine(mContext, code, "weekKLine");
    }

    public String getMonthlyLine(String code) {
        return getStockKLine(mContext, code, "monthKLine");
    }

    // 获取指数K线图
    public static String getIndexKLine(Context context, String code, String type) {
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("param.indexId", code);
        param.put("param.type", type);
        UniversalRequest.addHeader(context, param);

        return UrlConstants.RequsetBaseURLString +
                "/QiNiuApp/core/indexImg.getIndexLine.do?" + UniversalRequest.getMapString(param);
    }

    public String getIndexMinLine(String code) {
        return getIndexKLine(mContext, code, "minuteLine");
    }

    public String getIndexDailyLine(String code) {
        return getIndexKLine(mContext, code, "dayKLine");
    }

    public String getIndexWeeklyLine(String code) {
        return getIndexKLine(mContext, code, "weekKLine");
    }

    public String getIndexMonthlyLine(String code) {
        return getIndexKLine(mContext, code, "monthKLine");
    }


    // 得到股票ICONPATH
    public static String getStockIconPath(Context context, String stockCode) {

        HashMap<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);
        UniversalRequest.addHeaderWithoutSessionId(context, param);

        String url = UrlConstants.RequsetBaseURLString +
                "/QiNiuApp/core/stock.getImg8StockId.do?" + UniversalRequest.getMapString(param);

        return url;
    }


    /**
     * 请求个股信息
     */
    public void requestStockInfo(String stockCode, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);

        UniversalRequest.requestUrl(mContext, PATH_STOCK_SIMPLE_INFO, param, new OnRequestListener() {

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
                    StockTradeInfo stockTradeInfo = StockTradeInfo.resolveJsonObject(response.getJSONObject("data"));
                    // 缓存到数据库中
                    StockTradeTable stockTradeTable = new StockTradeTable(mContext);
                    stockTradeTable.saveItem(stockTradeInfo);
                    stockTradeTable.close();


                    if (requestFinished != null)
                        requestFinished.onDataFinish(stockTradeInfo);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 请求个股持仓信息
     *
     * @param stockCode
     * @param requestFinished
     */
    public void requestStockHoldersInfo(String stockCode, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);

        UniversalRequest.requestUrl(mContext, PATH_STOCK_HOLDERS_INFO, param, new OnRequestListener() {

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
                    StockTradeInfo stockTradeInfo = StockTradeInfo.resolveJsonPersonStock(response.getJSONObject("data"));
                    // 缓存到数据库中
                    StockTradeTable stockTradeTable = new StockTradeTable(mContext);
                    stockTradeTable.saveItem(stockTradeInfo);
                    stockTradeTable.close();

                    if (requestFinished != null)
                        requestFinished.onDataFinish(stockTradeInfo);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 请求最热个股信息
     */
    public void requestHottestStockInfo(final OnRequestListener requestFinished) {

        UniversalRequest.requestUrl(mContext, PATH_HOT_STOCK, null, new OnRequestListener() {

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
                    ArrayList<StockHottestGroup> stockHottestGroupArray = new ArrayList<StockHottestGroup>();
                    JSONObject data = response.getJSONObject("data");
                    StockHottestGroup group = StockHottestGroup.resolveHottestGroup(data, "buyMax", StockHottestGroup.TYPE_buyMax);
                    if (group != null) stockHottestGroupArray.add(group);

                    group = StockHottestGroup.resolveHottestGroup(data, "upMax", StockHottestGroup.TYPE_upMax);
                    if (group != null) stockHottestGroupArray.add(group);

                    group = StockHottestGroup.resolveHottestGroup(data, "moneyInMax", StockHottestGroup.TYPE_moneyInMax);
                    if (group != null) stockHottestGroupArray.add(group);

                    group = StockHottestGroup.resolveHottestGroup(data, "maxBuyMax", StockHottestGroup.TYPE_maxBuyMax);
                    if (group != null) stockHottestGroupArray.add(group);

                    group = StockHottestGroup.resolveHottestGroup(data, "saleMax", StockHottestGroup.TYPE_saleMax);
                    if (group != null) stockHottestGroupArray.add(group);

                    group = StockHottestGroup.resolveHottestGroup(data, "downMax", StockHottestGroup.TYPE_downMax);
                    if (group != null) stockHottestGroupArray.add(group);

                    group = StockHottestGroup.resolveHottestGroup(data, "moneyOutMax", StockHottestGroup.TYPE_moneyOutMax);
                    if (group != null) stockHottestGroupArray.add(group);

                    group = StockHottestGroup.resolveHottestGroup(data, "maxSaleMax", StockHottestGroup.TYPE_maxSaleMax);
                    if (group != null) stockHottestGroupArray.add(group);

                    group = StockHottestGroup.resolveHottestGroup(data, "maxSearch", StockHottestGroup.TYPE_maxSearch);
                    if (group != null) stockHottestGroupArray.add(group);

                    if (requestFinished != null)
                        requestFinished.onDataFinish(stockHottestGroupArray);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 获取股票组
     * cacheId 参见StockJsonCache,为-1不保存
     */
    public void requestStockGroupInfo(String stockCodes, final int cacheId, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockIds", stockCodes);

        UniversalRequest.requestUrl(mContext, PATH_STOCK_GROUP_INFO, param, new OnRequestListener() {

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

                    HashMap<String, StockTradeInfo> stockTradeInfoArray =
                            StockTradeInfo.resolveJsonObjectArray(response);

                    if (requestFinished != null) {
                        if (cacheId != -1) {
                            // save to cache
                            StockJsonCache.saveToFile(mContext, cacheId, response);
                        }

                        // call back
                        requestFinished.onDataFinish(stockTradeInfoArray);

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

    }


    /**
     * 获取股票/指数行情信息
     */
    public void requestQuotationsByIds(String stockIDs, String indexIDs, final int cacheId, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockIds", stockIDs);
        param.put("param.indexIds", indexIDs);
        UniversalRequest.requestUrl(mContext, PATH_STOCK_QUOTATIONS_INFO, param, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                // TODO Auto-generated method stub
                if (requestFinished != null) {
                    requestFinished.onError(errorCode, errorString);
                }
            }

            @Override
            public void onDataFinish(Object object) {
                // TODO Auto-generated method stub
                JSONObject response = (JSONObject) object;
                try {


                    HashMap<String, StockFavoriteItem> items =
                            StockFavoriteItem.resolveArrayFromJson(response);

                    if (requestFinished != null) {
                        if (cacheId != -1) {
                            // save to cache
                            StockJsonCache.saveToFile(mContext, cacheId, response);
                        }
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
     * 获取股票/指数行情信息
     */
    public void requestPrepareStockQuotationsByIds(String stockIDs, String indexIDs, final int cacheId, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockIds", stockIDs);
        param.put("param.indexIds", indexIDs);
        UniversalRequest.requestUrl(mContext, PATH_STOCK_QUOTATIONS_INFO, param, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                // TODO Auto-generated method stub
                if (requestFinished != null) {
                    requestFinished.onError(errorCode, errorString);
                }
            }

            @Override
            public void onDataFinish(Object object) {
                // TODO Auto-generated method stub
                JSONObject response = (JSONObject) object;
                try {


                    HashMap<String, LivePrepareStockInfo> items =
                            LivePrepareStockInfo.resolveArrayFromJson(response);

                    if (requestFinished != null) {
                        if (cacheId != -1) {
                            // save to cache
                            StockJsonCache.saveToFile(mContext, cacheId, response);
                        }
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
     * 获取某行业的领涨/领跌股票列表
     *
     * @param type 1为涨 -1为跌
     */
    public void requestIndustryPopularStock(String industryId, int type, final OnRequestListener requestFinished) {

        Map<String, String> param = new HashMap<String, String>();
        param.put("param.industryId", industryId);
        param.put("param.type", String.valueOf(type));
        UniversalRequest.requestUrl(mContext, PATH_INDUSTRY_POPULAR_STOCK, param, new OnRequestListener() {

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
                    JSONArray stockIDs = data.getJSONArray("stockId");
                    JSONObject quotations = data.getJSONObject("quotations");

                    int len = stockIDs.length();
                    for (int i = 0; i < len; i++) {
                        JSONObject perStock = quotations.getJSONObject(stockIDs.getString(i));
                        mStockArray.add(StockTradeInfo.resolveSummaryJsonObject(perStock));
                    }
                    if (requestFinished != null) {
                        // call back
                        requestFinished.onDataFinish(mStockArray);
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
     * 获取行业信息列表
     *
     * @param isAsc           1 领涨 0领跌
     * @param requestFinished
     */
    public void requestIndustryListStock(int isAsc,/*, int startIndex, int endIndex, */final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.isAsc", String.valueOf(isAsc));
//         param.put("param.start", String.valueOf(startIndex));
//         param.put("param.end", String.valueOf(endIndex));
        UniversalRequest.requestUrl(mContext, PATH_POPULAR_INDUSTRY, param, new OnRequestListener() {

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
                    //Log.i(TAG, "=======================>>requestIndustryListStock:" + response.toString());
                    ArrayList<IndustryInfo> industryLedArray = IndustryInfo.resolveJSONObjectArray(response);
                    if (requestFinished != null) {
                        // save to cache
                        StockJsonCache.saveToFile(mContext,
                                StockJsonCache.CACHEID_QUOTATIONS_INDUSTRY, response);
                        // call back
                        requestFinished.onDataFinish(industryLedArray);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取股票信息列表
     *
     * @param type            1为涨 -1为跌
     * @param requestFinished
     */
    public void requestPopularStockList(int type, int startIndex, int endIndex, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.type", String.valueOf(type));
        param.put("param.startIndex", String.valueOf(startIndex));
        param.put("param.endIndex", String.valueOf(endIndex));
        UniversalRequest.requestUrl(mContext, PATH_POPULAR_STOCK, param, new OnRequestListener() {

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
                    ArrayList<StockTradeInfo> stockArray = new ArrayList<StockTradeInfo>();
                    JSONObject data = response.getJSONObject("data");
                    JSONObject quotations = data.getJSONObject("quotations");
                    JSONArray stockIdList = data.getJSONArray("stockId");
                    int len = stockIdList.length();
                    for (int i = 0; i < len; i++) {
                        stockArray.add(StockTradeInfo.resolveSummaryJsonObject(quotations.getJSONObject(stockIdList.getString(i))));
                    }
                    if (requestFinished != null) {
                        // call back
                        requestFinished.onDataFinish(stockArray);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取沪港通股票信息列表
     *
     * @param type            1为涨 -1为跌
     * @param requestFinished
     */
    public void requestAHStockList(int type, int startIndex, int endIndex, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.type", String.valueOf(type));
        param.put("param.startIndex", String.valueOf(startIndex));
        param.put("param.endIndex", String.valueOf(endIndex));
        UniversalRequest.requestUrl(mContext, PATH_AH_STOCK, param, new OnRequestListener() {

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
                    ArrayList<StockTradeInfo> stockArray = new ArrayList<StockTradeInfo>();
                    JSONObject data = response.getJSONObject("data");
                    JSONObject quotations = data.getJSONObject("quotations");
                    JSONArray stockIdList = data.getJSONArray("stockId");
                    int len = stockIdList.length();
                    for (int i = 0; i < len; i++) {
                        stockArray.add(StockTradeInfo.resolveSummaryJsonObject(quotations.getJSONObject(stockIdList.getString(i))));
                    }
                    if (requestFinished != null) {
                        // call back
                        requestFinished.onDataFinish(stockArray);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 获取三大指数的具体信息
     */
    public void requestStockIndexDetail(final String stockIndexId, final OnRequestListener requestFinished) {

        Map<String, String> param = new HashMap<String, String>();
        param.put("param.indexId", stockIndexId);

        UniversalRequest.requestUrl(mContext, PATH_STOCK_INDEX_INFO, param,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        if (requestFinished != null) {
                            requestFinished.onError(errorCode, errorString);
                        }
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub
                        JSONObject response = (JSONObject) object;
                        try {
                            StockIndex stockIndex = StockIndex
                                    .resolveIndexJsonObject(response);

                            // save to cache
                            StockJsonCache.saveToFile(stockIndexId, mContext, StockJsonCache.CACHEID_EXTRA_INFO_INDEX_DETAIL, response);

                            if (requestFinished != null) {
                                // call back
                                requestFinished.onDataFinish(stockIndex);
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
     * 获取某个交易所的领涨股票信息
     */
    public void requestIndexPopularStock(String stockIndexId, int startIndex, int endIndex, int isAsc, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.indexId", stockIndexId);
        param.put("param.start", String.valueOf(startIndex));
        param.put("param.end", String.valueOf(endIndex));
        param.put("param.isAsc", String.valueOf(isAsc));

        UniversalRequest.requestUrl(mContext, PATH_INDEX_POPULAR_STOCK, param,
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

                            List<StockTradeInfo> stockArray = StockTradeInfo.resolveStockIDsArray(response);

                            if (requestFinished != null) {
                                // call back
                                requestFinished.onDataFinish(stockArray);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });

    }


    /**
     * 请求个股概况
     *
     * @param stockCode
     * @param requestFinished
     */
    public void requestStockSummaryInfo(final String stockCode, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);
        UniversalRequest.requestBackground(mContext, PATH_STOCK_SUMMARY, param,
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
                            if (requestFinished != null) {
                                // call back
                                requestFinished.onDataFinish(StockSummary.resolveJSONObject(response));
                                StockJsonCache.saveToFile(stockCode, mContext, StockJsonCache.CACHEID_EXTRA_INFO_SUMMARY, response);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 请求个股资金
     *
     * @param stockCode
     * @param requestFinished
     */
    public void requestStockMoneyInfo(final String stockCode, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);
        UniversalRequest.requestUrl(mContext, PATH_STOCK_MONEY, param,
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
                            if (requestFinished != null) {
                                // call back
                                requestFinished.onDataFinish(StockMoney.resolveJSONObjectArray(response));
                                StockJsonCache.saveToFile(stockCode, mContext, StockJsonCache.CACHEID_EXTRA_INFO_MONEY, response);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });

    }

    /**
     * 请求个股财务
     *
     * @param stockCode
     * @param requestFinished
     */
    public void requestStockFinanceInfo(final String stockCode, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);
        UniversalRequest.requestBackground(mContext, PATH_STOCK_FINANCE, param,
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
                            if (requestFinished != null) {
                                // call back
                                requestFinished.onDataFinish(StockFinance.resolveJSONObject(response));
                                // save to cache
                                StockJsonCache.saveToFile(stockCode, mContext, StockJsonCache.CACHEID_EXTRA_INFO_FINANCE, response);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 上传大家都在搜
     *
     * @param stockCode
     * @param requestFinished
     */
    public void uploadHotSearchStock(final String stockCode, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);
        UniversalRequest.requestBackground(mContext, PATH_UPLOAD_HOT_SEARCH, param, requestFinished);
    }

    /**
     * 获取股票分时信息
     * id:股票或者指数id
     * //type: 不传默认为股票
     * //      stock 为股票  0
     * //      index 为指数  1
     * //startTime:开始时间,毫秒
     * //entTime:结束时间,毫秒
     */
    public void getMinuteInfo(String stockCode, int type,
                              long startTime, long endTime,
                              final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.id", stockCode);
        if (type == 1) {
            param.put("param.type", "index");
        } else {
            param.put("param.type", "stock");
        }
        param.put("param.startTime", Long.toString(startTime));
        param.put("param.endTime", Long.toString(endTime));

        UniversalRequest.requestUrl(mContext, PATH_STOCK_MINUTE, param, requestFinished);
    }

    /**
     * 通过上次保存的数据库的时间来获取股票分时
     *
     * @param stockCode
     * @param type
     * @param lastTime
     * @param requestFinished
     */
    public void getMinuteInfoByLastTime(String stockCode, int type,
                                        long lastTime, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.id", stockCode);
        if (type == 1) {
            param.put("param.type", "index");
        } else {
            param.put("param.type", "stock");
        }
        param.put("param.lastTime", Long.toString(lastTime));
        UniversalRequest.requestUrl(mContext, PATH_STOCK_MINUTE_LASTTIME, param, requestFinished);
    }

    /**
     * k线
     *
     * @param stockCode
     * @param kType           1 2 3
     * @param type
     * @param startTime
     * @param endTime
     * @param requestFinished
     */
    public void getKlineInfo(String stockCode, int kType, int type,
                             long startTime, long endTime,
                             final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.id", stockCode);
        if (type == TYPE_INDEX) {
            param.put("param.type", "index");
        } else {
            param.put("param.type", "stock");
        }
        param.put("param.startTime", Long.toString(startTime));
        param.put("param.endTime", Long.toString(endTime));

        switch (kType) {
            case KLINE_TYPE_DAILY:
                param.put("param.kType", "day");
                break;
            case KLINE_TYPE_WEEKLY:
                param.put("param.kType", "week");
                break;
            case KLINE_TYPE_MONTHLY:
                param.put("param.kType", "month");
                break;

            default:
                break;
        }
        UniversalRequest.syncPost(mContext, PATH_STOCK_KLINE, param, requestFinished);
    }

    public void requestNewMinuteData(final OnRequestListener requestFinished, String url) {

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

                        ArrayList<StockMinutes> dataArrayList = StockMinutes.resloveNewMinutesData(object, false);

                        if (requestFinished != null) {
                            requestFinished.onDataFinish(dataArrayList);
                        }
                    }
                }, false, 0, false);

    }

    public void requestNewKLineData(final OnRequestListener requestFinished, String url) {

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

                        ArrayList<StockKLine> dataArrayList = StockKLine.resloveNewKLineData(object);

                        if (requestFinished != null) {
                            requestFinished.onDataFinish(dataArrayList);
                        }
                    }
                }, false, 0, false);

    }


    public void requestStockStrategyList(String strategyArray, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.ceLueKeys", strategyArray);
        UniversalRequest.requestBackground(mContext, PATH_STOCK_STRATEGY_LIST, param, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                // TODO Auto-generated method stub
                if (null != requestFinished)
                    requestFinished.onError(errorCode, errorString);
            }

            @Override
            public void onDataFinish(Object object) {
                // TODO Auto-generated method stub

                ArrayList<StockStrategyItem> items = null;

                try {
                    items = StockStrategyItem.resolveJSONObject((JSONObject) object);
                    StockJsonCache.saveToFile(mContext, StockJsonCache.CACHEID_STOCK_STRATEGY_LIST, (JSONObject) object);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (null != requestFinished)
                    requestFinished.onDataFinish(items);
            }
        });
    }

    public void requestStockStrategyList(long strategyKey, int startIndex, int endIndex, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.ceLueKey", String.valueOf(strategyKey));
        param.put("param.startIndex", String.valueOf(startIndex));
        param.put("param.endIndex", String.valueOf(endIndex));

        UniversalRequest.requestUrl(mContext, PATH_STRATEGY_STOCK_LIST, param, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                // TODO Auto-generated method stub
                if (null != requestFinished)
                    requestFinished.onError(errorCode, errorString);
            }

            @Override
            public void onDataFinish(Object object) {
                // TODO Auto-generated method stub

                ArrayList<StrategyStockItem> items = null;

                try {
                    items = StrategyStockItem.resolveJSONObject((JSONObject) object);
                    StockJsonCache.saveToFile(mContext, StockJsonCache.CACHEID_STOCK_STRATEGY_LIST, (JSONObject) object);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (null != requestFinished)
                    requestFinished.onDataFinish(items);

            }
        });
    }

    /**
     * 获取指数行情
     *
     * @param stockCode
     * @param requestFinished
     */
    public void getIndexDetail(final String stockCode, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.indexId", stockCode);
        UniversalRequest.requestUrl(mContext, PATH_INDEX_DETAIL, param, requestFinished);
    }

    /**
     * 获取个股维度信息
     */
    public void getStockDimension(final String stockCode, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);
        UniversalRequest.requestUrl(mContext, PATH_STOCK_DIMENSION, param, requestFinished);
    }

    /**
     * 获取盘口数据
     */
    public void getStockHandicap(final String stockCode, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);
        UniversalRequest.requestUrl(mContext, PATH_STOCK_HANDICAP, param, requestFinished);
    }

    /**
     * 获取该股票可比的所有股票列表
     */
    public void getCompareStockList(final String stockCode, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);
        UniversalRequest.requestUrl(mContext, PATH_DIMENSION_COMPARE_LIST, param, requestFinished);
    }

    /**
     * 获取某个股票的某一五维图信息
     * weidu.getWeiDuChartDate.do
     */
    public void getStockDimensionWithKey(final String stockCode, String chartId, long time,
                                         final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);
        param.put("param.chartId", chartId);
        param.put("param.time", Long.toString(time));
        UniversalRequest.requestUrl(mContext, PATH_STOCK_DIMENSION_WITH_KEY, param, requestFinished);
    }


    /**
     * 曲线图 获取比较的数据
     * compareWeiDuData
     */
    public void getDimensionLineData(final String stockCode, String weiduKey,
                                     long startTime, long endTime,
                                     final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);
        param.put("param.weiDuKey", weiduKey);
//        param.put("param.startTime", Long.toString(startTime));
//        param.put("param.endTime", Long.toString(endTime));
        UniversalRequest.requestUrl(mContext, PATH_DIMENSION_LINE_DATA, param, requestFinished);
    }

    /**
     * 曲线图 批量获取比较数据
     */
    public void getBatchDimensionLineData(final String stockCode, String weiduKey,
                                          long startTime, long endTime,
                                          final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockIds", stockCode);
        param.put("param.weiDuKey", weiduKey);
//        param.put("param.startTime", Long.toString(startTime));
//        param.put("param.endTime", Long.toString(endTime));
        param.put("param.lastTime", Long.toString(endTime));
        UniversalRequest.requestUrl(mContext, PATH_BATCH_DIMENSION_LINE_DATA, param, requestFinished);
    }


    /**
     * 获取他的自选股
     */
    public void getHisSelfStock(String uin, OnRequestListener requestListener) {
        if (TextUtils.isEmpty(uin)) {
            if (requestListener != null) {
                requestListener.onError(0, "uin is empty");
            }
            return;
        }
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.others", uin);
        UniversalRequest.requestUrl(mContext, PATH_GET_HIS_SELF_STOCK, param, requestListener);
    }

    /**
     * 直播间股票详情
     */
    public void getStockInfoDetail(final String stockCode, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.stockId", stockCode);
        UniversalRequest.requestUrl(mContext, PATH_GET_STOCK_INFO_DETAIL, param, requestFinished);
    }


    /**
     * 直播间股指详情
     */
    public void getIndexInfoDetail(final String stockCode, final OnRequestListener requestFinished) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.indexId", stockCode);
        UniversalRequest.requestUrl(mContext, PATH_GET_INDEX_INFO_DETAIL, param, requestFinished);
    }

}

