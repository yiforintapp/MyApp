package com.zlf.appmaster.client;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 *	重写超时时间为2500ms 
 *	@author Yushian
 */
public class QJsonObjectRequest extends JsonObjectRequest {
	
	public QJsonObjectRequest(int method, String url, JSONObject jsonRequest,
							  Listener<JSONObject> listener, ErrorListener errorListener,
							  boolean isSetTimeOut, int mTimeOut) {
		super(method, url, jsonRequest, listener, errorListener);
		
		if (isSetTimeOut) {//只发一次请求
			this.setRetryPolicy(new DefaultRetryPolicy(mTimeOut,
					0, 
					0));
		}else {
			/**
			 * 默认超时 会发两次
			 * 第一次 时间为  DEFAULT_TIMEOUT_MS
			 * 第二次 时间为  DEFAULT_TIMEOUT_MS*(DEFAULT_BACKOFF_MULT+1)
			 */
			this.setRetryPolicy(new DefaultRetryPolicy(
					5000,//DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, //超时时间
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES, //重试次数
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));//下次超时间的倍数
		}
		
	}
}
