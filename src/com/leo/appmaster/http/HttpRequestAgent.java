package com.leo.appmaster.http;

import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.leo.appmaster.constants.Constants;
import com.android.volley.Response.Listener;
import com.android.volley.Response.ErrorListener;

import android.content.Context;

/**
 * Http request Proxy
 * 
 * @author zhangwenyang
 * 
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
				Constants.APP_LOCK_LIST_DEBUG, null, listener, eListener);
		request.setShouldCache(true);
		mRequestQueue.add(request);
	}
}
