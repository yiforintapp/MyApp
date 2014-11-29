package com.leo.appmaster;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.leo.appmaster.applocker.receiver.LockReceiver;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.LeoStat;
import com.leoers.leoanalytics.RequestFinishedReporter;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class AppMasterApplication extends Application implements
		RequestFinishedReporter {

	private AppLoadEngine mAppsEngine;

	private static AppMasterApplication mInstance;

	private static List<Activity> mActivityList;

	public static SharedPreferences sharedPreferences;
	public static String usedThemePackage;
	public static String number;
	static {
		System.loadLibrary("leo_service");
	}

	private native void restartApplocker(int sdk);

	@Override
	public void onCreate() {
		super.onCreate();
		mActivityList = new ArrayList<Activity>();
		mInstance = this;
		mAppsEngine = AppLoadEngine.getInstance(this);
		mAppsEngine.preloadAllBaseInfo();
		initImageLoader(getApplicationContext());
		sharedPreferences = getSharedPreferences("lockerTheme",
				Context.MODE_WORLD_WRITEABLE);
		usedThemePackage = sharedPreferences.getString("packageName",
				Constants.DEFAULT_THEME);
		number = sharedPreferences.getString("firstNumber", "0");
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
		LeoStat.registerRequestFailedReporter(this);

		startInitTask(this);
		restartApplocker(PhoneInfo.getAndroidVersion());
	}

	private void startInitTask(final Context ctx) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				judgeLockService();
				judgeLockAlert();
				judgeStatictiUnlockCount();
				initImageLoader();
				checkNewTheme();
			}
		}).start();
	}

	private void judgeStatictiUnlockCount() {
		AppMasterPreference pref = AppMasterPreference.getInstance(this);
		if (!pref.getLastVersion().equals(PhoneInfo.getVersionCode(this))) {
			pref.setUnlockCount(0);
		}
	}

	private void initImageLoader() {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.threadPoolSize(Constants.MAX_THREAD_POOL_SIZE)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSizePercentage(12)
				.diskCacheSize(Constants.MAX_DISK_CACHE_SIZE) // 50 Mb
				.denyCacheImageMultipleSizesInMemory().build();
		ImageLoader.getInstance().init(config);
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
			pref.setHaveEverAppLoaded(false);
			pref.setLastVersion(PhoneInfo.getVersionCode(this));
			intent = new Intent(this, LockReceiver.class);
			intent.setAction(LockReceiver.ALARM_LOCK_ACTION);

			calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			pref.setLastAlarmSetTime(calendar.getTimeInMillis());
			calendar.add(Calendar.DATE, Constants.LOCK_TIP_INTERVAL_OF_DATE);
			PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
		} else { // not new install
			calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			long detal = calendar.getTimeInMillis() - pref.getInstallTime();
			intent = new Intent(this, LockReceiver.class);
			intent.setAction(LockReceiver.ALARM_LOCK_ACTION);
			if (detal < Constants.LOCK_TIP_INTERVAL_OF_MS) {
				PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
						+ Constants.LOCK_TIP_INTERVAL_OF_MS - detal, pi);
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

	private void showNewThemeTip() {
		Notification notif = new Notification();
		Intent intent = new Intent(this, LockerTheme.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.putExtra("from", "new_theme_tip");
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);

		notif.icon = R.drawable.ic_launcher;
		notif.tickerText = this.getString(R.string.find_new_theme);
		notif.flags = Notification.FLAG_ONGOING_EVENT
				| Notification.FLAG_AUTO_CANCEL;

		notif.setLatestEventInfo(this, this.getString(R.string.find_new_theme),
				this.getString(R.string.find_new_theme_content), contentIntent);

		notif.when = System.currentTimeMillis();
		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(0, notif);
	}

	public void checkNewTheme() {
		final AppMasterPreference pref = AppMasterPreference.getInstance(this);
		long curTime = System.currentTimeMillis();

		long lastCheckTime = pref.getLastCheckThemeTime();
		if (lastCheckTime == 0
				|| (curTime - pref.getLastCheckThemeTime()) > /*12 * 60 * 60*/ 2 * 1000) {

			if (pref.getLocalSerialNumber() != pref.getOnlineSerialNumber()) {
				showNewThemeTip();
				return;
			}

			HttpRequestAgent.getInstance(this).checkNewTheme(
					new Listener<JSONObject>() {

						@Override
						public void onResponse(JSONObject response) {
							if (response != null) {
								try {
									JSONObject jsonObject = response.getJSONObject("data");
									LeoLog.e("checkNewTheme",
											response.toString());
									if (jsonObject != null) {
										boolean hasNewTheme = jsonObject
												.getBoolean("need_update");
										String serialNumber = jsonObject
												.getString("update_flag");
										pref.setOnlineSerialNumber(serialNumber);

										if (hasNewTheme) {
											showNewThemeTip();
										}
										pref.setLastCheckTheme(System
												.currentTimeMillis());
									}

								} catch (JSONException e) {
									e.printStackTrace();
									LeoLog.e("checkNewTheme", e.getMessage());
								}
							}
						}

					}, new ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError error) {
							LeoLog.e("checkNewTheme", error.getMessage());
							TimerTask recheckTask = new TimerTask() {
								@Override
								public void run() {
									checkNewTheme();
								}
							};
							Timer timer = new Timer();
							timer.schedule(recheckTask, 2/* * 60 * 60*/ * 1000);
						}
					});
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

	// init ImageLoader
	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY)
				.memoryCacheSizePercentage(10)
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024)
				.tasksProcessingOrder(QueueProcessingType.FIFO)
				.writeDebugLogs().build();
		ImageLoader.getInstance().init(config);
	}

	// for force update strategy to exit application completely
	public void addActivity(Activity activity) {
		mActivityList.add(activity);
	}

	public void removeActivity(Activity activity) {
		mActivityList.remove(activity);
	}

	public void exitApplication() {
		for (Activity activity : mActivityList) {
			activity.finish();
		}
	}

	@Override
	public void reportRequestFinished(String description) {
		SDKWrapper.addEvent(getInstance(), LeoStat.P1, "leosdk", description);
	}

	public static void setSharedPreferencesValue(String lockerTheme) {
		Editor editor = sharedPreferences.edit();
		editor.putString("packageName", lockerTheme);
		editor.commit();
		usedThemePackage = lockerTheme;
	}

	public static void setSharedPreferencesNumber(String lockerThemeNumber) {
		Editor editor = sharedPreferences.edit();
		editor.putString("firstNumber", lockerThemeNumber);
		editor.commit();
		number = lockerThemeNumber;
	}

	public static String getSelectedTheme() {
		return usedThemePackage;
	}
}
