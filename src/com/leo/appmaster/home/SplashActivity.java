package com.leo.appmaster.home;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;

public class SplashActivity extends Activity {

	public static final int MSG_LAUNCH_HOME_ACTIVITY = 1000;

	private Handler mEventHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		mEventHandler = new EventHandler(this);
		mEventHandler.sendEmptyMessageDelayed(MSG_LAUNCH_HOME_ACTIVITY, 1000);
		startInitTask();
	}

	private static class EventHandler extends Handler {
		SplashActivity sa;

		public EventHandler(SplashActivity sa) {
			super();
			this.sa = sa;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LAUNCH_HOME_ACTIVITY:
				Intent intent = new Intent(sa, HomeActivity.class);
				sa.startActivity(intent);
				sa.finish();
				break;

			default:
				break;
			}
		}
	}

	private void startInitTask() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// get recommend app lock list
				final AppMasterPreference pref = AppMasterPreference
						.getInstance(getApplicationContext());
				long lastPull = pref.getLastLocklistPullTime();
				long interval = pref.getPullInterval();
				if (interval < (System.currentTimeMillis() - lastPull)
						&& NetWorkUtil.isNetworkAvailable(SplashActivity.this)) {
					HttpRequestAgent.getInstance(getApplicationContext())
							.getAppLockList(new Listener<JSONObject>() {
								@Override
								public void onResponse(JSONObject response) {
									JSONArray list;
									ArrayList<String> lockList = new ArrayList<String>();
									long next_pull;
									JSONObject data;
									try {
										data = response.getJSONObject("data");
										list = data.getJSONArray("list");
										for (int i = 0; i < list.length(); i++) {
											lockList.add(list.getString(i));
										}
										next_pull = data.getLong("next_pull");
										LeoLog.d("next_pull = " + next_pull
												+ " lockList = ",
												lockList.toString());

										pref.setPullInterval(next_pull * 24
												* 60 * 60 * 1000);
										pref.setLastLocklistPullTime(System
												.currentTimeMillis());
										Intent intent = new Intent(
												AppLoadEngine.ACTION_RECOMMEND_LIST_CHANGE);
										intent.putStringArrayListExtra(
												Intent.EXTRA_PACKAGES, lockList);
										SplashActivity.this
												.sendBroadcast(intent);
									} catch (JSONException e) {
										e.printStackTrace();
										return;
									}
								}
							}, new ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {
									LeoLog.d("Pull Lock list",
											error.getMessage());
								}
							});
				}
			}
		}).start();
	}
}
