package com.zlf.appmaster.client;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.utils.UrlConstants;
import com.zlf.appmaster.utils.VolleyTool;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * 通用的Post请求，可传参数
 * @author think
 *
 */
public class StringPostRequest {
	private static final String TAG = "StringPostRequest";
	
	
	/**
	 * 
	 * @param url
	 * @param mContext
	 * @param requestListener
	 * @param params 
	 */
	public static void request(String url,
							   final Context mContext,
							   final OnRequestListener requestListener,
							   final Map<String,String> params, final boolean isReturnJson) {
		
		UniversalRequest.addHeader(mContext, params);	
		
		QLog.i(TAG,"url:"+url);
//		QLog.i(TAG, "params:"+UniversalRequest.getMapString(params));

		StringRequest sRequest = new StringRequest(Request.Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String response, boolean noMidify) {
						if (isReturnJson) {
							try {
								JSONObject jsonObject = new JSONObject(response);
								UniversalRequest.handleResponse(mContext, jsonObject, requestListener,false);
							} catch (JSONException e) {
								e.printStackTrace();
								requestListener.onError(UrlConstants.CODE_JSON_ANALYSIS_ERROR, e.toString());
							}
						}else {
							byte [] reslut = response.getBytes();
							for (int i = 0; i < 20; i++) {
								QLog.i(TAG, ""+reslut[i]);
							}
							requestListener.onDataFinish(response);
						}
					}
				},  new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				error.printStackTrace();
				UniversalRequest.handleErrorResponse(mContext, error, requestListener,false);
			}
		}){

			@Override
			protected Map<String, String> getParams()
					throws AuthFailureError {
				return params;
			}

		};

		sRequest.setTag(TAG);// 设置tag
		// callAll的时候使用
		VolleyTool.getInstance(mContext).getRequestQueue()
		.add(sRequest);	
	}
	
	/**
	 * 
	 * @param url
	 * @param mContext
	 * @param requestListener
	 * @param params 
	 */
	public static void request(String url,
			final Context mContext,
			final OnRequestListener requestListener,
			final Map<String,String> params) {
		request(url, mContext, requestListener, params,true);
	}
}
