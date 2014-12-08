package com.leo.appmaster.http;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;
import com.android.volley.Request.Method;
import com.android.volley.Response.Listener;
import com.android.volley.Response.ErrorListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

/**
 * Http request Proxy, use volley framework <br>
 * NOTE: all those request is async, when you start a request ,you should
 * consider whether open cache
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

	public void loadOnlineTheme(List<String> loadedTheme,
			Listener<JSONObject> listener, ErrorListener eListener) {
		String url = Constants.ONLINE_THEME_URL;
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
		String url = Constants.CHECK_NEW_THEME;
		List<String> hideThemes = AppMasterPreference.getInstance(mContext)
				.getHideThemeList();
		String combined = "";
		for (String string : hideThemes) {
			combined = combined + string + ";";
		}
		String body = "update_flag="
				+ AppMasterPreference.getInstance(mContext)
						.getLocalSerialNumber() + "&loaded_theme=" + combined;
		JsonObjectRequest request = new JsonObjectRequest(Method.POST, url,
				body, listener, eListener);
		request.setShouldCache(false);
		mRequestQueue.add(request);
	}

	/**
	 * get system preset page recommend apps
	 * 
	 * @param listener
	 * @param eListener
	 */
	public void loadApplistRecomApp(Listener<JSONObject> listener,
			ErrorListener eListener) {

	}

	/**
	 * get system preset page recommend apps
	 * 
	 * @param listener
	 * @param eListener
	 */
	public void loadSysPresetRecomApp(Listener<JSONObject> listener,
			ErrorListener eListener) {

	}

	/**
	 * get running page recommend apps
	 * 
	 * @param listener
	 * @param eListener
	 */
	public void loadRunningRecomApp(Listener<JSONObject> listener,
			ErrorListener eListener) {

	}

	/**
	 * get business page recommend apps
	 * 
	 * @param listener
	 * @param eListener
	 */
	public void loadBusinessRecomApp(Listener<JSONObject> listener,
			ErrorListener eListener) {

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
}
