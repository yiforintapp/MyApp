
package com.leo.appmaster;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.leo.appmaster.cloud.UploadRequest;
import com.leo.appmaster.phoneSecurity.PhoneSecurityConstants;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.DeviceUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.LeoUrls;
import com.leo.appmaster.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
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
        String url = Utilities.getURL(Constants.ONLINE_THEME_URL);
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
        String language = AppwallHttpUtil.getLanguage();
        String country = AppwallHttpUtil.getCountry();
        if ("zh".equalsIgnoreCase(language)) {
            if ("CN".equalsIgnoreCase(country)) {
                requestLanguage = language;
            } else {
                requestLanguage = language + "_" + country;
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
        String url = Utilities.getURL(Constants.CHECK_NEW_THEME);
        String body = "?update_flag="
                + AppMasterPreference.getInstance(mContext)
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
        String url = Utilities.getURL(AppMasterConfig.CHECK_NEW_BUSINESS_APP);
        String body = "?update_flag="
                + AppMasterPreference.getInstance(mContext)
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
        String url = Utilities.getURL(AppMasterConfig.APP_RECOMMEND_URL) + "?re_position=" + type;
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
        String url = Utilities.getURL(AppMasterConfig.GESTURE_RECOMMEND_URL + "/"
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
        String url = Utilities.getURL(AppMasterConfig.APP_RECOMMEND_URL) + "?re_position=4"
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
        String url = Utilities.getURL(Constants.SPLASH_URL
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
        JsonObjectRequest request = new JsonObjectRequest(method, LeoUrls.URL_FEEDBACK, bodyString, listener,
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
     * 加载游戏推荐
     *
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
        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url, body, listener,
                errorListener) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }

        };
        mRequestQueue.add(request);
    }


    /* 加载ISwip更新提示 */
    public void loadISwipCheckNew(Listener<JSONObject> listener, ErrorListener errorListener) {
        String object = "";
        String iswipeUrl = "/appmaster/iswipeswitch.html";
        String url = Utilities.getURL(iswipeUrl);
        Log.d(TAG, "iSwipe访问连接：" + url);
        JsonObjectRequest request = new JsonObjectRequest(Method.GET, url, object, listener,
                errorListener);
        request.setShouldCache(true);
        mRequestQueue.add(request);
    }

    /* 加载推荐应用 */
    public void getAppLockList(Listener<JSONObject> listener, ErrorListener eListener) {
        String url = Utilities.getURL(Constants.APP_LOCK_LIST_URL);
        JsonObjectRequest request = new JsonObjectRequest(url, null, listener, eListener);
        LeoLog.d("LockRecomment", "访问连接：" + url);
        request.setShouldCache(true);
        mRequestQueue.add(request);
    }

    /* 加载广告展示方式 */
    public void loadADShowType(Listener<JSONObject> listener, ErrorListener errorListener) {
        String object = "";
        // String iswipeUrl = "/appmaster/config?ai=0000a.html";
        String versionCodeString = null;
        try {
            int versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            versionCodeString = String.valueOf(versionCode);
        } catch (NameNotFoundException e) {
        }
        String adtypeurl = "/appmaster/adconfig.html?app_version_code=" + versionCodeString;
        String url = Utilities.getURL(adtypeurl);
        JsonObjectRequest request = new JsonObjectRequest(Method.GET, url, object, listener,
                errorListener);
        LeoLog.d("poha", "adtype，访问连接：" + url);
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
        Context context = AppMasterApplication.getInstance();
        String language = getPostLanguage();
        String country = Utilities.getCountryID(context);
        String versionName = mContext.getString(R.string.version_name);
        String channelCode = mContext.getString(R.string.channel_code);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Utilities.getURL(Constants.MSG_CENTER_URL)).append("/")
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

    /* 加载手机防盗数据 */
    public void loadPhoneSecurity(Listener<JSONObject> listener, ErrorListener errorListener) {
        String object = "";
        String securUrl = PhoneSecurityConstants.PHONE_SECUR_URL;
        String url = Utilities.getURL(securUrl);
        LeoLog.i(TAG, "手机防盗访问连接：" + url);
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

    /**
     * 加载Swifty卡片数据
     *
     * @param listener
     * @param errorListener
     */
    public void loadSwiftySecurity(Listener<JSONObject> listener, ErrorListener errorListener) {
        String object = "";
        Context context = AppMasterApplication.getInstance();
        String language = getPostLanguage();
        String country = Utilities.getCountryID(context);
        String versionCodeString = "";
        try {
            int versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            versionCodeString = String.valueOf(versionCode);
        } catch (NameNotFoundException e) {
        }
        String channelCode = mContext.getString(R.string.channel_code);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Utilities.getURL(Constants.SWIFTY_SECURITY_URL)).append("/")
                .append(country).append("/")
                .append(language).append("/")
                .append(versionCodeString).append("/")
                .append(channelCode)
                .append(".html");
        String url = stringBuilder.toString();
        LeoLog.i("SwiftyFetchJob", "load url: " + url);
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

    /**
     * 加载卡片数据
     *
     * @param listener
     * @param errorListener
     */
    public void loadCardMsg(Listener<JSONObject> listener, ErrorListener errorListener) {
        String object = "";
        Context context = AppMasterApplication.getInstance();
        String language = getPostLanguage();
        String country = Utilities.getCountryID(context);
        String versionCodeString = "";
        try {
            int versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            versionCodeString = String.valueOf(versionCode);
        } catch (NameNotFoundException e) {
        }
        String channelCode = mContext.getString(R.string.channel_code);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Utilities.getURL(Constants.PRIVACY_WIFI_URL)).append("/")
                .append(country).append("/")
                .append(language).append("/")
                .append(versionCodeString).append("/")
                .append(channelCode)
                .append(".html");
        String url = stringBuilder.toString();
        LeoLog.i("CardFetchJob", "load url: " + url);
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

    /**
     * 上传黑名单列表
     *
     * @param listener
     * @param errorListener
     * @param bodyString
     */
    public void commitBlackList(Listener<String> listener,
                                ErrorListener errorListener, final String bodyString) {
        int method = Method.POST;
        StringRequest request = new StringRequest(method, "http://192.168.1.205/report", listener, errorListener) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                byte[] bytes = null;
                try {
                    bytes = bodyString.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return bytes;
            }
        };
        int retryCount = 3;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                retryCount, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        request.setBodyNeedCompress();
        request.setBodyNeedEncrypt();
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    /**
     * 加载黑名单的配置文件
     *
     * @param listener
     * @param errorListener
     */
    public void loadBlackList(Listener<JSONObject> listener, ErrorListener errorListener) {
        String object = "";
//        String url = LeoUrls.URI_BLACK_LIST;
//        url = Utilities.getURL(url);
        String url = "http://192.168.1.205/app/config";
        LeoLog.i(TAG, "黑名单配置数据：" + url);
        JsonObjectRequest request = new JsonObjectRequest(Method.GET, url, object, listener,
                errorListener);
        request.setShouldCache(true);
        int retryCount = 3;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                retryCount, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mRequestQueue.add(request);
    }

    /**
     * 下载黑名单列表文件
     *
     * @param filePath
     * @param listener
     * @param errorListener
     */
    public void downloadBlackList(String filePath, Listener<File> listener, ErrorListener errorListener) {
        String uri = null;
        FileRequest request = new FileRequest(Method.GET, uri, filePath, listener, errorListener);
        request.setShouldCache(true);
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
