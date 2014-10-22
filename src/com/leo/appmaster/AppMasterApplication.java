package com.leo.appmaster;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.leo.appmaster.applocker.AppLockerPreference;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.update.UIHelper;
import com.leo.appmaster.utils.AndroidConfig;
import com.leoers.leoanalytics.LeoStat;

public class AppMasterApplication extends Application {

	private AppLoadEngine mAppsEngine;

	private static AppMasterApplication mInstance;

	static {
		System.loadLibrary("leo_service");
	}

	private native void restartApplocker(int sdk);

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		mAppsEngine = AppLoadEngine.getInstance(this);
		mAppsEngine.preloadAllBaseInfo();
		// Register intent receivers
		//
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		// 成功删除某个APK之后发出的广播
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(mAppsEngine, filter);

		filter = new IntentFilter();
		// 移动App完成之后发生的广播
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
		// 正在移动App是发出的广播
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
		registerReceiver(mAppsEngine, filter);
		// 设备当前区域设置已更改是发出的广播
		filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
		registerReceiver(mAppsEngine, filter);
		iniLeoSdk();
		iniFlurry();
		judgeLockService();

		// start app destory listener
		
		
		restartApplocker(AndroidConfig.getAndroidVersion());

	}

	private void judgeLockService() {
		if (AppLockerPreference.getInstance(this).getLockType() != AppLockerPreference.LOCK_TYPE_NONE) {
			Intent serviceIntent = new Intent(this, LockService.class);
			serviceIntent.putExtra(LockService.EXTRA_STARTUP_FROM,
					"main activity");

			startService(serviceIntent);
		}
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		unregisterReceiver(mAppsEngine);
		FlurryAgent.onEndSession(getApplicationContext());
	}

	private void iniLeoSdk() {
		LeoStat.init(getApplicationContext(), getString(R.string.channel_code),
				"appmaster");
		LeoStat.setDebugLevel(Log.ERROR);
		LeoStat.initUpdateEngine(UIHelper.getInstance(getApplicationContext()),
				true);
	}

	private void iniFlurry() {
		FlurryAgent.setVersionName(getString(R.string.version_name) + "_"
				+ getString(R.string.channel_code));
		FlurryAgent.onStartSession(getApplicationContext(),
				"F6PHG92TXG5QZ48H4YC8");
	}

	public static AppMasterApplication getInstance() {
		return mInstance;
	}

}
