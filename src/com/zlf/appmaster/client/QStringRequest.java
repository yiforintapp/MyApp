package com.zlf.appmaster.client;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;

/**
 * Created by Administrator on 2016/8/15.
 */
public class QStringRequest extends StringRequest {
//    public QStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
//        super(url, listener, errorListener);
//
//
//
//        this.setRetryPolicy(new DefaultRetryPolicy(
//                5000,//DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, //超时时间
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, //重试次数
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//    }

    public QStringRequest(int method, String url, Object o, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);

        this.setRetryPolicy(new DefaultRetryPolicy(
                5000,//DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, //超时时间
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, //重试次数
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

//    public QStringRequest(int method, String url, JsonArrayRequest jsonRequest,
//                          Response.Listener<JSONArray> listener, Response.ErrorListener errorListener,
//                          boolean isSetTimeOut, int mTimeOut) {
//        super(url, listener, errorListener);
//
//        if (isSetTimeOut) {//只发一次请求
//            this.setRetryPolicy(new DefaultRetryPolicy(mTimeOut,
//                    0,
//                    0));
//        }else {
//            /**
//             * 默认超时 会发两次
//             * 第一次 时间为  DEFAULT_TIMEOUT_MS
//             * 第二次 时间为  DEFAULT_TIMEOUT_MS*(DEFAULT_BACKOFF_MULT+1)
//             */
//            this.setRetryPolicy(new DefaultRetryPolicy(
//                    5000,//DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, //超时时间
//                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES, //重试次数
//                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));//下次超时间的倍数
//        }
//
//    }
}
