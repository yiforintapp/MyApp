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
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.DisplayMetrics;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.leo.appmaster.applocker.receiver.LockReceiver;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.appmanage.AppListActivity;
import com.leo.appmaster.appmanage.business.AppBusinessManager;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.sdk.push.UserActManager;
import com.leo.appmaster.sdk.push.ui.PushUIHelper;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NotificationUtil;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.ImageLoaderConfiguration;
import com.leo.imageloader.cache.Md5FileNameGenerator;
import com.leo.imageloader.core.QueueProcessingType;

import com.leoers.leoanalytics.RequestFinishedReporter;

public class AppMasterApplication extends Application {

	private AppLoadEngine mAppsEngine;
	private AppBackupRestoreManager mBackupManager;

	private static AppMasterApplication mInstance;
	private static List<Activity> mActivityList;
	
	/* Separate user phone feed activity from Leo Analytics SDK to App */
	private UserActManager mActManager;

	public Handler mHandler;

	public static SharedPreferences sharedPreferences;
	public static String usedThemePackage;
	// public static String number;

	public static int SDK_VERSION;
	public static float density;
	public static int densityDpi;
	public static String densityString;
	public static int MAX_OUTER_BLUR_RADIUS;

	static {
		System.loadLibrary("leo_service");
	}

	private native void restartApplocker(int sdk);

	@Override
	public void onCreate() {
		super.onCreate();
		initDensity(this);
		mActivityList = new ArrayList<Activity>();
		mInstance = this;
		mHandler = new Handler();
		mAppsEngine = AppLoadEngine.getInstance(this);

		mBackupManager = new AppBackupRestoreManager(this);
		initImageLoader(getApplicationContext());
		sharedPreferences = getSharedPreferences("lockerTheme",
				Context.MODE_WORLD_WRITEABLE);
		usedThemePackage = sharedPreferences.getString("packageName",
				Constants.DEFAULT_THEME);
		// number = sharedPreferences.getString("firstNumber", "0");
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
//		LeoStat.registerRequestFailedReporter(this);

		startInitTask(this);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				checkNewTheme();
				checkNewAppBusiness();
			}
		}, 10000);
		restartApplocker(PhoneInfo.getAndroidVersion());
		
		/* init user activity manager here */
		mActManager = UserActManager.getInstance(getApplicationContext(), 
		        PushUIHelper.getInstance(getApplicationContext()));
	}

	private void startInitTask(final Context ctx) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mAppsEngine.preloadAllBaseInfo();
				AppBusinessManager.getInstance(mInstance).init();
				mBackupManager.getBackupList();
				judgeLockService();
				judgeLockAlert();
				judgeStatictiUnlockCount();
				initImageLoader();
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
				.diskCacheSize(Constants.MAX_DISK_CACHE_SIZE) // 100 Mb
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

	public AppBackupRestoreManager getBuckupManager() {
		return mBackupManager;
	}

	private void showNewThemeTip() {
		// send new theme broadcast
		Intent intent = new Intent(Constants.ACTION_NEW_THEME);
		sendBroadcast(intent);

		// show new theme status tip
		Notification notif = new Notification();
		intent = new Intent(this, LockerTheme.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.putExtra("from", "new_theme_tip");
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notif.icon = R.drawable.ic_launcher_notification;
		notif.tickerText = this.getString(R.string.find_new_theme);
		notif.flags = Notification.FLAG_AUTO_CANCEL;
		notif.setLatestEventInfo(this, this.getString(R.string.find_new_theme),
				this.getString(R.string.find_new_theme_content), contentIntent);
	    NotificationUtil.setBigIcon(notif, R.drawable.ic_launcher_notification_big);
		notif.when = System.currentTimeMillis();
		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(0, notif);
	}

	private void showNewBusinessTip(String mainTitle, String content) {
		Intent intent = null;
		Notification notif = new Notification();
		intent = new Intent(this, AppListActivity.class);
		intent.putExtra("from_statubar", true);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notif.icon = R.drawable.ic_launcher_notification;
		notif.tickerText = mainTitle;
		notif.flags = Notification.FLAG_AUTO_CANCEL;
		notif.setLatestEventInfo(this, mainTitle, content, contentIntent);
	    NotificationUtil.setBigIcon(notif, R.drawable.ic_launcher_notification_big);
		notif.when = System.currentTimeMillis();
		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(1, notif);
	}

	protected void checkNewAppBusiness() {
		final AppMasterPreference pref = AppMasterPreference.getInstance(this);
		long curTime = System.currentTimeMillis();

		long lastCheckTime = pref.getLastCheckBusinessTime();
		if (lastCheckTime == 0
				|| (curTime - lastCheckTime) > AppMasterConfig.TIME_12_HOUR
		/* 2 * 60 * 1000 */) {
			HttpRequestAgent.getInstance(this).checkNewBusinessData(
					new Listener<JSONObject>() {

						@Override
						public void onResponse(JSONObject response,
								boolean noMidify) {
							if (response != null) {
								try {
									JSONObject jsonObject = response.getJSONObject("data");
									if (jsonObject != null) {
										boolean hasNewBusinessData = jsonObject
												.getBoolean("need_update");
										String serialNumber = jsonObject
												.getString("update_flag");

										// String mainTitle = jsonObject
										// .getString("main_title");
										// String content = jsonObject
										// .getString("content");
										String mainTitle = AppMasterApplication.this
												.getString(R.string.new_app_tip_title);
										String content = AppMasterApplication.this
												.getString(R.string.new_app_tip_content);

										if (!hasNewBusinessData) {
											pref.setLocalBusinessSerialNumber(serialNumber);
										}
										pref.setOnlineBusinessSerialNumber(serialNumber);

										if (hasNewBusinessData) {
											showNewBusinessTip(mainTitle,
													content);
											AppMasterPreference pref = AppMasterPreference
													.getInstance(AppMasterApplication.this);
											pref.setHomeBusinessTipClick(false);
										}
										pref.setLastCheckBusinessTime(System
												.currentTimeMillis());
									}

									TimerTask recheckTask = new TimerTask() {
										@Override
										public void run() {
											checkNewAppBusiness();
										}
									};
									Timer timer = new Timer();
									timer.schedule(recheckTask,
											AppMasterConfig.TIME_12_HOUR/*
																		 * 2 *
																		 * 60 *
																		 * 1000
																		 */);

								} catch (JSONException e) {
									e.printStackTrace();
									LeoLog.e("checkNewAppBusiness",
											e.getMessage());
								}
							}
						}

					}, new ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError error) {
							TimerTask recheckTask = new TimerTask() {
								@Override
								public void run() {
									checkNewAppBusiness();
								}
							};
							Timer timer = new Timer();
							timer.schedule(recheckTask,
									AppMasterConfig.TIME_2_HOUR
							/* 2 * 60 * 1000 */);
						}
					});
		} else {
			TimerTask recheckTask = new TimerTask() {
				@Override
				public void run() {
					checkNewAppBusiness();
				}
			};
			Timer timer = new Timer();
			long delay = AppMasterConfig.TIME_12_HOUR
					- (curTime - lastCheckTime);
			timer.schedule(recheckTask, delay);
		}

	}

	public void checkNewTheme() {
		final AppMasterPreference pref = AppMasterPreference.getInstance(this);
		long curTime = System.currentTimeMillis();

		long lastCheckTime = pref.getLastCheckThemeTime();
		if (lastCheckTime == 0
				|| (curTime - pref.getLastCheckThemeTime()) > AppMasterConfig.TIME_12_HOUR) {
			HttpRequestAgent.getInstance(this).checkNewTheme(
					new Listener<JSONObject>() {

						@Override
						public void onResponse(JSONObject response,
								boolean noMidify) {
							if (response != null) {
								try {
									JSONObject jsonObject = response.getJSONObject("data");
									if (jsonObject != null) {
										boolean hasNewTheme = jsonObject
												.getBoolean("need_update");
										String serialNumber = jsonObject
												.getString("update_flag");

										if (!hasNewTheme) {
											pref.setLocalThemeSerialNumber(serialNumber);
										}
										pref.setOnlineThemeSerialNumber(serialNumber);

										if (hasNewTheme) {
											showNewThemeTip();
										}
										pref.setLastCheckThemeTime(System
												.currentTimeMillis());
									}

									TimerTask recheckTask = new TimerTask() {
										@Override
										public void run() {
											checkNewTheme();
										}
									};
									Timer timer = new Timer();
									timer.schedule(recheckTask,
											AppMasterConfig.TIME_12_HOUR);

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
							timer.schedule(recheckTask,
									AppMasterConfig.TIME_2_HOUR);
						}
					});
		} else {
			TimerTask recheckTask = new TimerTask() {
				@Override
				public void run() {
					checkNewTheme();
				}
			};
			Timer timer = new Timer();
			long delay = AppMasterConfig.TIME_12_HOUR
					- (curTime - lastCheckTime);
			timer.schedule(recheckTask, delay);
		}
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		mBackupManager.onDestory(this);
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
				.memoryCacheSizePercentage(3)
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(100 * 1024 * 1024)
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

	public static void setSharedPreferencesValue(String lockerTheme) {
		Editor editor = sharedPreferences.edit();
		editor.putString("packageName", lockerTheme);
		editor.commit();
		usedThemePackage = lockerTheme;
	}

	// public static void setSharedPreferencesNumber(String lockerThemeNumber) {
	// Editor editor = sharedPreferences.edit();
	// editor.putString("firstNumber", lockerThemeNumber);
	// editor.commit();
	// number = lockerThemeNumber;
	// }

	public static String getSelectedTheme() {
		return usedThemePackage;
	}

	private static void initDensity(Context context) {
		SDK_VERSION = android.os.Build.VERSION.SDK_INT;
		Resources res = context.getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		density = dm.density;
		densityDpi = dm.densityDpi;
		switch (densityDpi) {
		case DisplayMetrics.DENSITY_XHIGH:
			densityString = "xhdpi";
			break;
		}
		MAX_OUTER_BLUR_RADIUS = (int) (density * 12.0f);
	}

	/**
	 * Whether the sdk level is higher than 14 (android 4.0)
	 * 
	 * @return true if sdk level is higher than android 4.0, false otherwise
	 */
	public static boolean isAboveICS() {
		return AppMasterApplication.SDK_VERSION >= 14;
	}
}
