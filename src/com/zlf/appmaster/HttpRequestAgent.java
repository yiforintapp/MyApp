
package com.zlf.appmaster;

import android.content.Context;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zlf.appmaster.R;
import com.zlf.appmaster.utils.DeviceUtil;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

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
    public static final String TAG = "HttpRequestAgent";

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

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }


    // public void loadOnlineTheme(List<String> loadedTheme,
    // Listener<JSONObject> listener, ErrorListener eListener) {
    // String url = Utilities.getURL(Constants.ONLINE_THEME_URL);
    // String combined = "";
    // for (String string : loadedTheme) {
    // combined = combined + string + ";";
    // }
    // String body = null;
    // String requestLanguage = getPostLanguage();
    // body = "language=" + requestLanguage + "&market_id="
    // + mContext.getString(R.string.channel_code) + "&app_ver="
    // + mContext.getString(R.string.version_name) + "&loaded_theme="
    // + combined + "&pgsize=" + "6";
    //
    // JsonObjectRequest request = new JsonObjectRequest(Method.POST, url,
    // body, listener, eListener);
    // request.setShouldCache(false);
    // mRequestQueue.add(request);
    // }

    public void loadOnlineTheme(List<String> loadedTheme, RequestListener listener) {
        String url = Utilities.getURL(com.zlf.appmaster.Constants.ONLINE_THEME_URL);
        String combined = "";
        for (String string : loadedTheme) {
            combined = combined + string + ";";
        }
        String body = null;
        String requestLanguage = getPostLanguage();
        body = "language=" + requestLanguage + "&market_id="
                + mContext.getString(R.string.channel_code) + "&app_ver="
                + mContext.getString(R.string.version_name) + "&loaded_theme="
                + combined + "&pgsize=" + "6";

        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url,
                body, listener, listener);
        if (loadedTheme == null || loadedTheme.isEmpty()) {
            // 本地未安装任何列表启用缓存
            request.setShouldCache(true);
        } else {
            request.setShouldCache(false);
        }
        mRequestQueue.add(request);
    }

    /*
     * 对系统语言上传到服务器作出理（主要对中文简体和繁体中文）"zh":中文简体，”zh_(地区)“：繁体中文
     */
    private String getPostLanguage() {
        String requestLanguage;
        String language = "kjljkl";
        String country = "jk;l";
        if ("zh".equalsIgnoreCase(language)) {
            if ("CN".equalsIgnoreCase(country)) {
                requestLanguage = language;
            } else {
                requestLanguage = language + "-" + country;
            }
        } else {
            requestLanguage = language;
        }
        // Log.d(Constants.RUN_TAG, "sys_language:" +requestLanguage);
        return requestLanguage;
    }

    public void checkNewTheme(Listener<JSONObject> listener,
                              ErrorListener eListener) {
        String requestLanguage = getPostLanguage();
        String url = Utilities.getURL(com.zlf.appmaster.Constants.CHECK_NEW_THEME);
        String body = "?update_flag="
                + com.zlf.appmaster.AppMasterPreference.getInstance(mContext)
                .getLocalThemeSerialNumber() + "&market_id="
                + mContext.getString(R.string.channel_code) + "&language="
                + requestLanguage + "&app_ver="
                + mContext.getString(R.string.version_name) + "&app_id="
                + mContext.getPackageName();
//      String body = "?update_flag="
//      + "abcdefg" + "&market_id="
//      + mContext.getString(R.string.channel_code) + "&language="
//      + requestLanguage + "&app_ver="
//      + mContext.getString(R.string.version_name) + "&app_id="
//      + mContext.getPackageName();
        url += body;
        LeoLog.d("httpurl", "New Theme Http is :" + url);
        JsonObjectRequest request = new JsonObjectRequest(Method.GET, url,
                "", listener, eListener);
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    public void checkNewBusinessData(Listener<JSONObject> listener,
                                     ErrorListener eListener) {
        String requestLanguage = getPostLanguage();
        String url = Utilities.getURL(com.zlf.appmaster.AppMasterConfig.CHECK_NEW_BUSINESS_APP);
        String body = "?update_flag="
                + com.zlf.appmaster.AppMasterPreference.getInstance(mContext)
                .getLocalBusinessSerialNumber() + "&market_id="
                + mContext.getString(R.string.channel_code) + "&language="
                + requestLanguage + "&app_ver="
                + mContext.getString(R.string.version_name) + "&app_id="
                + mContext.getPackageName();
//        String body = "?update_flag="
//                + "zyxrdx" + "&market_id="
//                + mContext.getString(R.string.channel_code) + "&language="
//                + requestLanguage + "&app_ver="
//                + mContext.getString(R.string.version_name) + "&app_id="
//                + mContext.getPackageName();
        url += body;
        LeoLog.d("httpurl", "New Business Http is :" + url);
        JsonObjectRequest request = new JsonObjectRequest(Method.GET, url,
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
        String requestLanguage = getPostLanguage();
        String url = Utilities.getURL(com.zlf.appmaster.AppMasterConfig.APP_RECOMMEND_URL) + "?re_position=" + type;
        String body = "&market_id=" + mContext.getString(R.string.channel_code)
                + "&language=" + requestLanguage;
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
        String requestLanguage = getPostLanguage();
        String url = Utilities.getURL(com.zlf.appmaster.AppMasterConfig.GESTURE_RECOMMEND_URL + "/"
                + mContext.getString(R.string.version_name) + "/"
                + Utilities.getCountryID(mContext) + "/" + requestLanguage + "/"
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
        String requestLanguage = getPostLanguage();
        String url = Utilities.getURL(com.zlf.appmaster.AppMasterConfig.APP_RECOMMEND_URL) + "?re_position=4"
                + "&pgcurrent=" + page;
        // String url =
        // "http://192.168.1.201:8080/leo/appmaster/apprecommend/list?re_position=4&pgcurrent="+page;
        String body = "&market_id=" + mContext.getString(R.string.channel_code)
                + "&language=" + requestLanguage + "&pgsize="
                + number;
        // String body = "&market_id=" +
        // mContext.getString(R.string.channel_code);

        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url,
                body, listener, eListener);
        if (page == 1) {
            request.setShouldCache(true);
        } else {
            request.setShouldCache(false);
        }
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
     */
    public void loadSplashDate(Listener<JSONObject> listener,
                               ErrorListener errorListener) {
        String requestLanguage = getPostLanguage();
        String object = "";
        String url = Utilities.getURL(com.zlf.appmaster.Constants.SPLASH_URL
                + Utilities.getCountryID(mContext) + "/"
                + requestLanguage + "/" +
                mContext.getString(R.string.version_name) + "/"
                + mContext.getString(R.string.channel_code) + ".html");
        LeoLog.i(TAG, "闪屏请求链接：" + url);
        JsonObjectRequest request = new JsonObjectRequest(Method.GET, url,
                object, listener, errorListener);
        request.setShouldCache(false);
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
     *
     * @param listener
     * @param errorListener
     * @param params
     * @param device
     */
    public void commitFeedback(Listener<JSONObject> listener,
                               ErrorListener errorListener, final Map<String, String> params, final String device) {
        String bodyString = null;
        int method = Method.POST;
        JsonObjectRequest request = new JsonObjectRequest(method, "ghkghkg", bodyString, listener,
                errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        Map<String, String> data = DeviceUtil.getFeedbackData(mContext);
        request.addEncryptHeaders(data);
        // 最多重试3次
        int retryCount = 3;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                retryCount, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }


    /**
     * 加载消息中心列表
     *
     * @param listener
     * @param errorListener
     */
    public void loadMessageCenterList(Listener<JSONArray> listener, ErrorListener errorListener) {
        Context context = com.zlf.appmaster.AppMasterApplication.getInstance();
        String language = getPostLanguage();
        String country = Utilities.getCountryID(context);
        String versionName = mContext.getString(R.string.version_name);
        String channelCode = mContext.getString(R.string.channel_code);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Utilities.getURL(com.zlf.appmaster.Constants.MSG_CENTER_URL)).append("/")
                .append(country).append("/")
                .append(language).append("/")
                .append(versionName).append("/")
                .append(channelCode)
                .append(".html");
        String url = stringBuilder.toString();
        LeoLog.i("MsgCenterFetchJob", "load url: " + url);
        JsonArrayRequest request = new JsonArrayRequest(url, listener, errorListener);
        request.setShouldCache(true);
        // 最多重试3次
        int retryCount = 3;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                retryCount, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mRequestQueue.add(request);
    }


    /**
     * 加载自分享数据
     *
     * @param listener
     * @param errorListener
     */
    public void loadShareMsg(Listener<JSONObject> listener, ErrorListener errorListener) {
        String object = "";
        Context context = com.zlf.appmaster.AppMasterApplication.getInstance();
        String language = getPostLanguage();
        String country = Utilities.getCountryID(context);
        final String productId = "appmaster";
        final String unitName = "selfshare";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Utilities.getURL("")).append("/")
                .append(productId).append("/")
                .append(unitName).append("/")
                .append(country).append("/")
                .append(language)
                .append(".html");
        String url = stringBuilder.toString();
        LeoLog.i("ShareFetchJob", "load url: " + url);
        JsonObjectRequest request = new JsonObjectRequest(Method.GET, url, object, listener,
                errorListener);
        request.setShouldCache(true);
        // 最多重试3次
        int retryCount = 3;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                retryCount, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mRequestQueue.add(request);
    }

    public abstract static class RequestListener<T> implements Listener<JSONObject>, ErrorListener {
        private WeakReference<T> outerContextRef;

        public RequestListener(T outerContext) {
            outerContextRef = new WeakReference<T>(outerContext);
        }

        protected T getOuterContext() {
            return outerContextRef.get();
        }

    }

}
