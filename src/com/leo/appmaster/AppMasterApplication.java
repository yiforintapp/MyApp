package com.leo.appmaster;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

import com.flurry.android.FlurryAgent;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.update.UIHelper;
import com.leoers.leoanalytics.LeoStat;

public class AppMasterApplication extends Application {

	private AppLoadEngine mAppsEngine;

	private static AppMasterApplication mInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		mAppsEngine = AppLoadEngine.getInstance(this);
		mAppsEngine.preloadAllBaseInfo();
		// Register intent receivers
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(mAppsEngine, filter);

		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
		registerReceiver(mAppsEngine, filter);

		filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
		registerReceiver(mAppsEngine, filter);

		iniLeoSdk();
		iniFlurry();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		unregisterReceiver(mAppsEngine);
		FlurryAgent.onEndSession(getApplicationContext());
	}

	private void iniLeoSdk() {
		LeoStat.init(getApplicationContext(), "1", "appmaster");
		LeoStat.setSilentMode();
		LeoStat.initUpdateEngine(UIHelper.getInstance(getApplicationContext()),
				true);
	}

	private void iniFlurry() {
		FlurryAgent.onStartSession(getApplicationContext(),
				"F6PHG92TXG5QZ48H4YC8");
	}

	public static AppMasterApplication getInstance() {
		return mInstance;
	}

}
