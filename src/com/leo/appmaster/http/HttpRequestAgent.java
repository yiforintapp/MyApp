
package com.leo.appmaster.http;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.filterfw.core.FinalPort;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.FileRequest;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.feedback.FeedbackHelper;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

/**
 * Http request Proxy, use volley framework <br>
 * NOTE: all those request is async, when you start a request ,you should
 * consider whether open cache
 * 
 * @author zhangwenyang
 */
public class HttpRequestAgent {

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
        String body = "?update_flag="
                + AppMasterPreference.getInstance(mContext)
                        .getLocalThemeSerialNumber() + "&market_id="
                + mContext.getString(R.string.channel_code) + "&language="
                + AppwallHttpUtil.getLanguage() + "&app_ver="
                + mContext.getString(R.string.version_name) + "&app_id="
                + mContext.getPackageName();
        url += body;
        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url,
                "", listener, eListener);
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    public void checkNewBusinessData(Listener<JSONObject> listener,
            ErrorListener eListener) {
        String url = Utilities.getURL(AppMasterConfig.CHECK_NEW_BUSINESS_APP);
        String body = "?update_flag="
                + AppMasterPreference.getInstance(mContext)
                        .getLocalBusinessSerialNumber() + "&market_id="
                + mContext.getString(R.string.channel_code) + "&language="
                + AppwallHttpUtil.getLanguage() + "&app_ver="
                + mContext.getString(R.string.version_name) + "&app_id="
                + mContext.getPackageName();
        url += body;
        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url,
                "", listener, eListener);
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
     * get gesture recommend apps
     * 
     * @param listener
     * @param eListener
     */
    public void loadGestureRecomApp(int type, Listener<JSONObject> listener,
            ErrorListener eListener) {

        String url = Utilities.getURL(AppMasterConfig.GESTURE_RECOMMEND_URL + "/"
                + mContext.getString(R.string.version_name) + "/"
                + Utilities.getCountryID(mContext) + "/" + AppwallHttpUtil.getLanguage() + "/"
                + mContext.getString(R.string.channel_code) + ".html");

        LeoLog.d("loadGestureRecomApp", "url = " + url);
        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url,
                "", listener, eListener);
        request.setShouldCache(false);
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
     * 加载闪屏
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
        JsonObjectRequest request = new JsonObjectRequest(Method.GET, url,
                object, listener, errorListener);
        request.setShouldCache(true);
        mRequestQueue.add(request);
    }

    /**
     * 加载闪屏图
     * 
     * @param url
     * @param listener
     * @param eListener
     */
    public void loadSplashImage(final String url, String dir,
            Listener<File> listener, ErrorListener eListener) {
        FileRequest request = new FileRequest(url, dir, listener, eListener);
        request.setShouldCache(true);
        mRequestQueue.add(request);
    }
    
    /**
     * 提交用户反馈
     * @param listener
     * @param errorListener
     * @param params
     * @param device
     */
    public void commitFeedback(Listener<JSONObject> listener,
            ErrorListener errorListener, final Map<String, String> params, final String device) {
        String bodyString = null;
        String url = Utilities.getURL(FeedbackHelper.FEEDBACK_URL);
        int method = Method.POST;
        JsonObjectRequest request = new JsonObjectRequest(method, url, bodyString, listener, errorListener) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("device", device);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        // 最多重试3次
        int retryCount = 3;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                retryCount, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mRequestQueue.add(request);
    }
    
    /**
     * 加载游戏推荐
     * @param listener
     * @param errorListener
     */
    public void loadGameData(Listener<JSONObject> listener, ErrorListener errorListener) {
        String url = Utilities.getURL(Constants.PATH_GAME_DATA); 
        String language = AppwallHttpUtil.getLanguage();
        String code = AppMasterApplication.getInstance().getString(R.string.channel_code);
        final Map<String, String> map = new HashMap<String, String>();
        map.put("language_type", language);
        map.put("market_id", code);
        
        String body = null;
        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url, body, listener, errorListener) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
            
        };
        mRequestQueue.add(request);
    }

}
