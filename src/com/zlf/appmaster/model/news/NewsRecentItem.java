package com.zlf.appmaster.model.news;

import com.zlf.appmaster.db.stock.SyncBaseTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huang on 2015/6/3.
 */
public class NewsRecentItem {
    /**
     * 自选股新闻
     */
    public static String KEY_GOODS_STOCK_NEWS = "10-0000000";

    /**
     * 个股重要新闻
     */
    public static String KEY_IMPORTANT_STOCK_NEWS = "11-000000";

    /**
     * 个股重要公告
     */
    public static String KEY_IMPORTANT_STOCK_NOTICE = "12-000000";

    /**
     * 盘前必读
     */
    public static String KEY_BEFORE_OPEN_MARKET_NEWS = "13-000000";

    /**
     * 盘中必读
     */
    public static String KEY_OPEN_MARKET_NEWS = "14-000000";

    /**
     * 盘后必读
     */
    public static String KEY_AFTER_OPEN_MARKET_NEWS = "15-000000";

    /**
     * 宏观经济动态
     */
    public static String KEY_MACRO_ECONOMY_NEWS = "16-000000";

    /**
     * 新股快讯
     */
    public static String KEY_NEW_STOCK_NEWS = "17-000000";

    /**
     * 盘面直播
     */
    public static String KEY_MARKET_NEWS_LIVE= "18-000000";



    private String mKey;
    private boolean mHasNews;
    private String mTitle;
    private long mCreateTime;
    private int mNewsFlashType = -1;
    private boolean mIsSubscribe = false;       // 是否已订阅





    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        this.mKey = key;
    }

    public boolean isHasNews() {
        return mHasNews;
    }

    public void setHasNews(boolean hasNews) {
        this.mHasNews = hasNews;
    }

    public void setHasNews(int i) {
        if (i == 0){
            mHasNews = false;
        }
        else {
            mHasNews = true;
        }
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(long createTime) {
        this.mCreateTime = createTime;
    }


    /**
     * 转换为NewsFlashItem中的定义
     * @param key
     * @return
     */
    public int getNewsFlashTypeByKey(String key){
        if (mNewsFlashType != -1){
            return mNewsFlashType;
        }
        mNewsFlashType = convertNewsFlashType(key);

        return  mNewsFlashType;
    }

    public static int convertNewsFlashType(String key){
        int newsFlashType = -1;
        if (key.equals(KEY_GOODS_STOCK_NEWS)){
            newsFlashType = NewsFlashItem.NEWS_TYPE_SELF_STOCK;
        }
        else if (key.equals(KEY_IMPORTANT_STOCK_NEWS)){
            newsFlashType = NewsFlashItem.NEWS_TYPE_STOCK;
        }
        else if (key.equals(KEY_IMPORTANT_STOCK_NOTICE)){
            newsFlashType = NewsFlashItem.NEWS_TYPE_ANNOUNCEMENT;
        }
        else if (key.equals(KEY_BEFORE_OPEN_MARKET_NEWS)){
            newsFlashType = NewsFlashItem.NEWS_TYPE_BEFORE;
        }
        else if (key.equals(KEY_OPEN_MARKET_NEWS)){
            newsFlashType = NewsFlashItem.NEWS_TYPE_MIDDLE;
        }
        else if (key.equals(KEY_AFTER_OPEN_MARKET_NEWS)){
            newsFlashType = NewsFlashItem.NEWS_TYPE_AFTER;
        }
        else if (key.equals(KEY_MACRO_ECONOMY_NEWS)){
            newsFlashType = NewsFlashItem.NEWS_TYPE_ECONOMY;
        }
        else if (key.equals(KEY_NEW_STOCK_NEWS)){
            newsFlashType = NewsFlashItem.NEWS_TYPE_IPO;
        }
        else if (key.equals(KEY_MARKET_NEWS_LIVE)){
            newsFlashType = NewsFlashItem.NEWS_TYPE_MARKET_LIVE;
        }
        return  newsFlashType;
    }

    public static List<NewsRecentItem> resolveArray(JSONObject response) throws JSONException {
        List<NewsRecentItem> items = new ArrayList<NewsRecentItem>();

        JSONArray array = response.getJSONArray("data");

        for (int i = 0; i < array.length(); i++) {
            NewsRecentItem item = new NewsRecentItem();
            items.add(item);

            JSONObject itemObject = array.getJSONObject(i);
            item.setKey(itemObject.getString("Key"));
            item.setHasNews(itemObject.getBoolean("HasNews"));
            item.setCreateTime(itemObject.getLong("Ctime"));
            item.setTitle(itemObject.getString("Title"));

        }
        return items;
    }

    public boolean isSubscribe() {
        return mIsSubscribe;
    }

    public void setIsSubscribe(boolean isSubscribe) {
            mIsSubscribe = isSubscribe;
    }

    public void setIsSubscribe(int isSubscribe) {
        if (isSubscribe == SyncBaseTable.DELETE_FLAG_NORMAL){
            mIsSubscribe = true;
        }
        else {
            mIsSubscribe = false;
        }
    }
}
