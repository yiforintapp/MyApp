package com.leo.appmaster;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.leo.appmaster.applocker.receiver.LockReceiver;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.constants.Constants;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leoers.leoanalytics.LeoStat;
import com.leoers.leoanalytics.RequestFinishedReporter;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class AppMasterApplication extends Application implements RequestFinishedReporter{

	private AppLoadEngine mAppsEngine;

	private static AppMasterApplication mInstance;

    private static List<Activity> mActivityList;

    private static final int MAX_MEMORY_CACHE_SIZE = 5 * (1 << 20);// 5M
    private static final int MAX_DISK_CACHE_SIZE = 50 * (1 << 20);// 20 Mb
    private static final int MAX_THREAD_POOL_SIZE = 3;
	
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
		judgeLockService();
		judgeLockAlert();
		// start app destory listener

		restartApplocker(PhoneInfo.getAndroidVersion());
		initImageLoader();
	}

	private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .threadPoolSize(MAX_THREAD_POOL_SIZE)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSizePercentage(12)
                 .diskCacheSize(MAX_DISK_CACHE_SIZE) // 50 Mb
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
			pref.setLastVersion(PhoneInfo.getVersionCode(this));
			intent = new Intent(this, LockReceiver.class);
			intent.setAction(LockReceiver.ALARM_LOCK_ACTION);

			calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			pref.setLastAlarmSetTime(calendar.getTimeInMillis());
			calendar.add(Calendar.SECOND, Constants.LOCK_TIP_INTERVAL_OF_DATE);
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
	
	//初始化ImageLoader
		public static void initImageLoader(Context context) {
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
					.threadPriority(Thread.NORM_PRIORITY - 2)
					.denyCacheImageMultipleSizesInMemory()
					.diskCacheFileNameGenerator(new Md5FileNameGenerator())
					.diskCacheSize(50 * 1024 * 1024) 
					.tasksProcessingOrder(QueueProcessingType.LIFO)
					.writeDebugLogs() 
					.build();
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
	        android.os.Process.killProcess(android.os.Process.myPid());  
	        System.exit(0);
	    }

        @Override
        public void reportRequestFinished(String description) {
            SDKWrapper.addEvent(getInstance(), LeoStat.P1, "leosdk", description);
        }

}
