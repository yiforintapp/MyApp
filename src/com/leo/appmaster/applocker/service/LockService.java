package com.leo.appmaster.applocker.service;

import java.util.Timer;
import java.util.TimerTask;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.applocker.logic.TimeoutRelockPolicy;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.util.Log;

public class LockService extends Service {

	private static final String TAG = "LockService";
	public static final String EXTRA_STARTUP_FROM = "start_from";
	private final int NOTIFY_ID = 1000;

	private boolean mServiceStarted;

	private Timer mTimer;
	private TimerTask mDetectTask;
	private LockHandler mLockHandler;
	private NotificationManager mNM;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
//		Notification notification = new Notification(R.drawable.ic_launcher,
//				"leo applocker", System.currentTimeMillis());
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//				new Intent(this, LockScreenActivity.class), 0);
//
//		notification.setLatestEventInfo(this, "leo applocker",
//				"leo applocker is servicing", contentIntent);
//
//		mNM = (NotificationManager) getApplicationContext().getSystemService(
//				Context.NOTIFICATION_SERVICE);
//		mNM.notify(NOTIFY_ID, notification);
//		startForeground(NOTIFY_ID, notification);

		mLockHandler = new LockHandler(getApplicationContext());
		mLockHandler.setLockPolicy(new TimeoutRelockPolicy(
				getApplicationContext()));

		IntentFilter filter = new IntentFilter(LockHandler.ACTION_APP_UNLOCKED);
		this.getApplicationContext().registerReceiver(mLockHandler, filter);
		super.onCreate();
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		if (!mServiceStarted) {
			startLockService(intent);
		}
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!mServiceStarted) {
			startLockService(intent);
		}
		return START_STICKY;
	}

	private void startLockService(Intent intent) {
		Log.d(TAG, "start lock service");
		startDetectTask();
		mServiceStarted = true;
	}

	private void stopLockService() {
		Log.d(TAG, "stop lock service");
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
		mTimer.schedule(mDetectTask, 0, 200);
	}

	@Override
	public void onDestroy() {
		stopLockService();
		stopForeground(true);
		mNM.cancel(NOTIFY_ID);
		this.getApplicationContext().unregisterReceiver(mLockHandler);
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
			String topActivityPackageName = topTaskInfo.topActivity
					.getPackageName();

			PackageManager pm = getPackageManager();
			PackageInfo topPackageInfo = null;

			String topActivityName = topTaskInfo.topActivity
					.getShortClassName().toString();
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
