
package com.leo.appmaster.http;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.Utilities;

/**
 * Http request Proxy, use volley framework <br>
 * NOTE: all those request is async, when you start a request ,you should
 * consider whether open cache
 * 
 * @author zhangwenyang
 */
public class HttpRequestAgent {

    private static final String Tag = "HttpRequestAgent";
    private Context mContext;
    private RequestQueue mRequestQueue;
    private static HttpRequestAgent mInstance;

    private HttpRequestAgent(Context ctx) {
        mContext = ctx.getApplicationContext();
        mRequestQueue = Volley.newRequestQueue(mContext);
        mRequestQueue.start();
    }

    public static synchronized HttpRequestAgent getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new HttpRequestAgent(ctx);
        }
        return mInstance;
    }

    public void getAppLockList(Listener<JSONObject> listener,
            ErrorListener eListener) {
        JsonObjectRequest request = new JsonObjectRequest(
                Utilities.getURL(Constants.APP_LOCK_LIST_URL), null, listener, eListener);
        request.setShouldCache(true);
        mRequestQueue.add(request);
    }

    public void loadOnlineTheme(List<String> loadedTheme,
            Listener<JSONObject> listener, ErrorListener eListener) {
        String url = Utilities.getURL(Constants.ONLINE_THEME_URL);
        String combined = "";
        for (String string : loadedTheme) {
            combined = combined + string + ";";
        }
        String body = null;
        body = "language=" + AppwallHttpUtil.getLanguage() + "&market_id="
                + mContext.getString(R.string.channel_code) + "&app_ver="
                + mContext.getString(R.string.version_name) + "&loaded_theme="
                + combined + "&pgsize=" + "6";

        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url,
                body, listener, eListener);
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    public void checkNewTheme(Listener<JSONObject> listener,
            ErrorListener eListener) {
        String url = Utilities.getURL(Constants.CHECK_NEW_THEME);
        // List<String> hideThemes = AppMasterPreference.getInstance(mContext)
        // .getHideThemeList();
        // String combined = "";
        // for (String string : hideThemes) {
        // combined = combined + string + ";";
        // }
        String body = "update_flag="
                + AppMasterPreference.getInstance(mContext)
                        .getLocalThemeSerialNumber() + "&market_id="
                + mContext.getString(R.string.channel_code) + "&language="
                + AppwallHttpUtil.getLanguage() + "&app_ver="
                + mContext.getString(R.string.version_name) + "&app_id="
                + mContext.getPackageName();
        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url,
                body, listener, eListener);
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    public void checkNewBusinessData(Listener<JSONObject> listener,
            ErrorListener eListener) {
        String url = Utilities.getURL(AppMasterConfig.CHECK_NEW_BUSINESS_APP);
        // String url =
        // "http://192.168.1.201:8800/appmaster/apprecommend/checkappupdate";
        String body = "update_flag="
                + AppMasterPreference.getInstance(mContext)
                        .getLocalThemeSerialNumber() + "&market_id="
                + mContext.getString(R.string.channel_code) + "&language="
                + AppwallHttpUtil.getLanguage() + "&app_ver="
                + mContext.getString(R.string.version_name) + "&app_id="
                + mContext.getPackageName();
        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url,
                body, listener, eListener);
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    /**
     * get running page recommend apps
     * 
     * @param listener
     * @param eListener
     */
    public void loadRecomApp(int type, Listener<JSONObject> listener,
            ErrorListener eListener) {
        String url = Utilities.getURL(AppMasterConfig.APP_RECOMMEND_URL) + "?re_position=" + type;
        String body = "&market_id=" + mContext.getString(R.string.channel_code)
                + "&language=" + AppwallHttpUtil.getLanguage();
        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url,
                body, listener, eListener);
        request.setShouldCache(true);
        mRequestQueue.add(request);
    }

    /**
     * get business page recommend apps
     * 
     * @param listener
     * @param eListener
     */
    public void loadBusinessRecomApp(int page, int number, Listener<JSONObject> listener,
            ErrorListener eListener) {
        String url = Utilities.getURL(AppMasterConfig.APP_RECOMMEND_URL) + "?re_position=4"
                + "&pgcurrent=" + page;
        // String url =
        // "http://192.168.1.201:8080/leo/appmaster/apprecommend/list?re_position=4&pgcurrent="+page;
        String body = "&market_id=" + mContext.getString(R.string.channel_code)
                + "&language=" + AppwallHttpUtil.getLanguage() + "&pgsize="
                + number;
        // String body = "&market_id=" +
        // mContext.getString(R.string.channel_code);

        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url,
                body, listener, eListener);
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    /**
     * get business page recommend apps
     * 
     * @param listener
     * @param eListener
     */
    public void loadBusinessAppIcon(final String url,
            Listener<Bitmap> listener, ErrorListener eListener) {
        ImageRequest request = new ImageRequest(url, listener, 200, 200,
                Config.ARGB_8888, eListener);
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    /**
     * load splash from server
     * 
     * @param listener
     * @param eListener
     */
    public void loadSplashDate(Listener<JSONObject> listener,
            ErrorListener errorListener) {
        String object = "";
        String url = Utilities.getURL(Constants.SPLASH_URL
                + mContext.getString(R.string.version_name) + "/"
                + Utilities.getCountryID(mContext) + "/"
                + mContext.getString(R.string.channel_code) + ".html");
        Log.e("xxxxxxx", "访问闪屏URL：" + url);
        JsonObjectRequest request = new JsonObjectRequest(Method.GET, url,
                object, listener, errorListener);
        request.setShouldCache(true);
        mRequestQueue.add(request);
        Log.e("xxxxxxx", "正在拉取闪屏数据。。。。");
    }

    public void loadSplashImage(final String url,
            Listener<Bitmap> listener, ErrorListener eListener) {
        ImageRequest request = new ImageRequest(url, listener, 200, 200,
                Config.ARGB_8888, eListener);
        request.setShouldCache(true);
        mRequestQueue.add(request);
    }

}
