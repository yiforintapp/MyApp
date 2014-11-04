package com.leo.appmaster;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.receiver.LockReceiver;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.update.UIHelper;
import com.leo.appmaster.utils.LeoLog;
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
		// 设备当前区域设置已更改是发出的广播
		filter.addAction(Intent.ACTION_LOCALE_CHANGED);
		// recommend list change
		filter.addAction(AppLoadEngine.ACTION_RECOMMEND_LIST_CHANGE);

		registerReceiver(mAppsEngine, filter);
		SDKWrapper.iniSDK(this);
		judgeLockService();
		judgeLockAlert();
		// start app destory listener

		restartApplocker(PhoneInfo.getAndroidVersion());

	}

	private void judgeLockAlert() {
		AppMasterPreference pref = AppMasterPreference.getInstance(this);
		if (pref.isReminded()) {
			return;
		}
		Calendar calendar;
		Intent intent;
		AlarmManager am = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		if (!pref.getLastVersion().equals(PhoneInfo.getVersionCode(this))) { // is
																				// new
																				// version
			pref.setLastVersion(PhoneInfo.getVersionCode(this));
			intent = new Intent(this, LockReceiver.class);
			intent.setAction(LockReceiver.ALARM_LOCK_ACTION);

			calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			pref.setLastAlarmSetTime(calendar.getTimeInMillis());
			calendar.add(Calendar.DATE, 5);
			PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
		} else { // not new install
			calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			long detal = calendar.getTimeInMillis() - pref.getInstallTime();
			intent = new Intent(this, LockReceiver.class);
			intent.setAction(LockReceiver.ALARM_LOCK_ACTION);
			if (detal < 3 * 24 * 60 * 60 * 1000) {
				PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, detal, pi);
				pref.setLastAlarmSetTime(calendar.getTimeInMillis());
			} else {
				sendBroadcast(intent);
			}
		}
	}

	private void judgeLockService() {
		if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
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
		SDKWrapper.endSession(this);
	}

	public static AppMasterApplication getInstance() {
		return mInstance;
	}

}
