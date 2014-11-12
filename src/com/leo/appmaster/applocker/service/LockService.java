package com.leo.appmaster.applocker.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;

import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.applocker.logic.TimeoutRelockPolicy;

public class LockService extends Service {

	public static final String EXTRA_STARTUP_FROM = "start_from";

	private boolean mServiceStarted;

	private Timer mTimer;
	private TimerTask mDetectTask;
	private LockHandler mLockHandler;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mLockHandler = new LockHandler(getApplicationContext());
		mLockHandler.setLockPolicy(new TimeoutRelockPolicy(
				getApplicationContext()));

		IntentFilter filter = new IntentFilter(LockHandler.ACTION_APP_UNLOCKED);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		this.getApplicationContext().registerReceiver(mLockHandler, filter);
		super.onCreate();
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		if (!mServiceStarted) {
			startLockService();
		}
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			if (intent.getBooleanExtra("lock_service", true)) {
				if (!mServiceStarted) {
					startLockService();
				}
			} else {
				stopLockService();
			}
		}
		return START_STICKY;
	}

	private void startLockService() {
		startDetectTask();
		mServiceStarted = true;
	}

	private void stopLockService() {
		stopDetectTsk();
		mServiceStarted = false;
	}

	private void stopDetectTsk() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
			mDetectTask = null;
		}
	}

	private void startDetectTask() {
		stopDetectTsk();
		mTimer = new Timer();
		mDetectTask = new DetectTask();
		mTimer.schedule(mDetectTask, 0, 100);
	}

	@Override
	public void onDestroy() {
		stopLockService();
		this.getApplicationContext().unregisterReceiver(mLockHandler);
		sendBroadcast(new Intent("com.leo.appmaster.restart"));
		super.onDestroy();
	}

	private class DetectTask extends TimerTask {
		ActivityManager mActivityManager;

		public DetectTask() {
			mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		}

		@Override
		public void run() {
			RunningTaskInfo topTaskInfo = mActivityManager.getRunningTasks(1)
					.get(0);

			if (topTaskInfo.topActivity == null)
				return;
			String topActivityPackageName = topTaskInfo.topActivity
					.getPackageName();
			PackageManager pm = getPackageManager();
			PackageInfo topPackageInfo = null;

			String topActivityName = topTaskInfo.topActivity
					.getShortClassName();

			try {
				topPackageInfo = pm.getPackageInfo(topActivityPackageName, 0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

			if (mLockHandler != null) {
				mLockHandler.handleAppLaunch(topPackageInfo.packageName,
						topActivityName);
			}
		}
	}

}
