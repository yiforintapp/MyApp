package com.zlf.appmaster.model.news;

import android.content.Context;
import android.text.TextUtils;

import com.zlf.appmaster.R;
import com.zlf.appmaster.db.stock.NewsFlashTable;
import com.zlf.appmaster.utils.QLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Created by think on 2014/11/20.
 */
public class NewsFlashItem implements Serializable {
    private static final String TAG = NewsFlashItem.class.getSimpleName();

    /**
     * 自选股新闻
     */
    public static final int NEWS_TYPE_SELF_STOCK = 0;
//
//
    /**
     * 个股重要公告：10001
     */
    public static final int NEWS_TYPE_ANNOUNCEMENT = 1;
    /**
     * 个股重大新闻：10002 20002
     */
    public static final int NEWS_TYPE_STOCK = 2;
    /**
     * 宏观经济动态：10003
     */
    public static final int NEWS_TYPE_ECONOMY = 3;
    /**
     * 盘前必读：10004
     */
    public static final int NEWS_TYPE_BEFORE = 4;
    /**
     * 盘中直播：10005
     */
    public static final int NEWS_TYPE_MIDDLE = 5;
    /**
     * 盘后解读：10006
     */
    public static final int NEWS_TYPE_AFTER = 6;
    /**
     * 新股：10007
     */
    public static final int NEWS_TYPE_IPO = 7;

    /**
     * 盘面直播（包含盘前 盘中 和 盘后）
     */
    public static final int NEWS_TYPE_MARKET_LIVE = 8;

    /**
     * 是否是股票相关的新闻
     *
     * @param newsType
     * @return
     */
    public static boolean isStockNewsType(int newsType) {
        switch (newsType) {
            case NEWS_TYPE_ANNOUNCEMENT:
            case NEWS_TYPE_STOCK:
            case NEWS_TYPE_SELF_STOCK:
            case NEWS_TYPE_IPO:
                return true;
            default:
                return false;
        }
    }


    public static String getDefaultTitle(int newsType) {
        switch (newsType) {
            case NEWS_TYPE_ANNOUNCEMENT:
                return "个股公告";

            case NEWS_TYPE_STOCK:
                return "个股新闻";

            case NEWS_TYPE_ECONOMY:
                return "宏观动态";

            case NEWS_TYPE_BEFORE:
                return "盘前必读";

            case NEWS_TYPE_MIDDLE:
                return "盘中直播";

            case NEWS_TYPE_AFTER:
                return "盘后解读";

            case NEWS_TYPE_SELF_STOCK:
                return "自选股新闻";

            case NEWS_TYPE_IPO:
                return "新股快讯";

            case NEWS_TYPE_MARKET_LIVE:
                return "盘面直播";
        }
        return "";
    }


    private long time;          //时间点

    public static int getDefaultImg(int newsType) {
        int drawableID = 0;
        switch (newsType) {
            case NewsFlashItem.NEWS_TYPE_ANNOUNCEMENT:
                drawableID = R.drawable.news_icon_announcemnt;
                break;
            case NewsFlashItem.NEWS_TYPE_STOCK:
                drawableID = R.drawable.news_icon_stock;
                break;
            case NewsFlashItem.NEWS_TYPE_ECONOMY:
                drawableID = R.drawable.news_icon_economy;
                break;
            case NewsFlashItem.NEWS_TYPE_BEFORE:
                drawableID = R.drawable.news_icon_before;
                break;
            case NewsFlashItem.NEWS_TYPE_MIDDLE:
                drawableID = R.drawable.news_icon_middle;
                break;
            case NewsFlashItem.NEWS_TYPE_AFTER:
                drawableID = R.drawable.news_icon_after;
                break;
            case NewsFlashItem.NEWS_TYPE_SELF_STOCK:
                drawableID = R.drawable.news_icon_self_stock;
                break;
            case NewsFlashItem.NEWS_TYPE_IPO:
                drawableID = R.drawable.news_icon_ipo;
                break;
            case NewsFlashItem.NEWS_TYPE_MARKET_LIVE:
                drawableID = R.drawable.news_icon_middle;
                break;
            default:
                drawableID = R.drawable.news_icon_stock;
        }

        return drawableID;
    }

    private int classify;       //分类
    private boolean isChanged;  // 是否更改
    private String newsKey;       // 新闻类型
    private long id;            //新闻ID
    private String title;
    private ArrayList<String> stockList, stockNameList;
    private String stockJsonArray;
    private String summary;
    private String media;
    private boolean isFavorite;//是否自选股新闻

    private String timeString;
    private String idString;


    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public String getStockJsonArray() {
        return stockJsonArray;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


    public int getClassify() {
        return classify;
    }

    public void setClassify(int type) {
        this.classify = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null)
            return;
/*        //最大20个字符
        if ( title.length() > 20){
            this.title = title.substring(0,21);
            this.title += "...";
        }else {
            this.title = title;
        }*/
        this.title = title;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getNewsKey() {
        return newsKey;
    }

    public void setNewsKey(String newsKey) {
        this.newsKey = newsKey;
    }

    public NewsFlashItem() {
    }

    /**
     * 设置股票ID
     */
    public void setStockList(JSONArray array) {
        if (array == null) {
            return;
        }

        stockList = new ArrayList<String>();
        stockNameList = new ArrayList<String>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject stockObject = array.getJSONObject(i);
                stockList.add(stockObject.optString("StockId"));
                stockNameList.add(stockObject.optString("StockName"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        stockJsonArray = array.toString();
    }

    public ArrayList<String> getStockList() {
        return stockList;
    }

    public ArrayList<String> getStockNameList() {
        return stockNameList;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setIsChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getTimeString(){
        return timeString;
    }

    public void setTimeString(String mTime){
        timeString = mTime;
    }

    public String getidString(){
        return idString;
    }

    public void setidString(String mId){
        idString = mId;
    }


    /**
     * 判断是否有自选股
     *
     * @param stocks
     * @return
     */
    public static boolean hasFavorite(ArrayList<String> stocks, HashSet<String> mFavoriteStocks) {
        if (mFavoriteStocks.isEmpty() || stocks.isEmpty()) {
            return false;
        }

        for (String stock : stocks) {
            if (mFavoriteStocks.contains(stock)) {
                QLog.i(TAG, "hasFavorite:" + stock + "," + stocks.toString() + "," + mFavoriteStocks.toString());
                return true;
            }
        }

        return false;
    }


    /**
     * 解析新闻组
     *
     * @param newsKey
     * @param response
     * @return
     * @throws JSONException
     */
    public static List<NewsFlashItem> resolveNewsArray(String newsKey, JSONObject response) throws JSONException {
        List<NewsFlashItem> items = new ArrayList<NewsFlashItem>();

        JSONArray array = response.getJSONArray("data");

        for (int i = 0; i < array.length(); i++) {
            NewsFlashItem item = new NewsFlashItem();
            items.add(item);

            JSONObject itemObject = array.getJSONObject(i);
            item.setNewsKey(newsKey);
            item.setId(itemObject.getLong("NewsId"));
            item.setClassify(itemObject.getInt("Classify"));
            item.setIsChanged(itemObject.getBoolean("IsChange"));
            item.setTime(itemObject.getLong("Ctime"));
            item.setTitle(itemObject.getString("Title"));
            item.setStockList(itemObject.getJSONArray("AboutStock"));
            item.setSummary(itemObject.optString("Summary"));
            item.setMedia(itemObject.optString("Media"));

        }
        return items;
    }

    /**
     * 解析个股的新闻组
     *
     * @param stockCode
     * @param response
     * @return
     * @throws JSONException
     */
    public static List<NewsFlashItem> resolveStockNewsArray(String stockCode, JSONObject response) throws JSONException {
        return resolveNewsArray("10-" + stockCode, response);
    }

    /**
     * 解析新闻列表保存至数据库
     *
     * @param object
     * @return
     */
    public static List<NewsFlashItem> resolveNewsArrayAndSave(String newsKey, Object object, Context context) {

        List<NewsFlashItem> items = new ArrayList<NewsFlashItem>();
        try {
            items = resolveNewsArray(newsKey, (JSONObject) object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //保存到本地
        NewsFlashTable table = new NewsFlashTable(context);
        table.saveNewsFlashItemArray(items);
        table.close();

        return items;
    }


    public static void deleteNoTitles(ArrayList<NewsFlashItem> items) {
        for (int i = 0; i < items.size(); i++) {
            NewsFlashItem item = items.get(i);
            if (TextUtils.isEmpty(item.getTitle())) {
                items.remove(i);
                i -= 1;
            }
        }
    }


    @Override
    public String toString() {
        return "NewsFlashItem{" +
                "time=" + time +
                ", classify=" + classify +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", stockList=" + stockList +
                '}';
    }


}
