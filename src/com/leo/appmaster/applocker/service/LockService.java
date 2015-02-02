package com.leo.appmaster.applocker.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.applocker.logic.TimeoutRelockPolicy;
//import android.app.ActivityManager.AppTask;

@SuppressLint("NewApi")
public class LockService extends Service {

	public static final String EXTRA_STARTUP_FROM = "start_from";
	
	private static final String SYSTEMUI_PKG = "com.android.systemui";
	private static final String ES_UNINSTALL_ACTIVITY = ".app.UninstallMonitorActivity";

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
		} else {
			if (!mServiceStarted) {
				startLockService();
			}
		}
		return START_STICKY;
	}

	private void startLockService() {
		AppMasterPreference pref = AppMasterPreference.getInstance(this);
		List<String> lockList = pref.getLockedAppList();
		
		if(lockList != null && !lockList.isEmpty()) {
			startDetectTask();
			mServiceStarted = true;
		}
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
		// AM-663
		mTimer.schedule(mDetectTask, 100, 100);
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
            String pkgName = null;
            String activityName = null;
            if (Build.VERSION.SDK_INT > 19) { // Android L and above
                List<RunningAppProcessInfo> list = mActivityManager.getRunningAppProcesses();
                for (RunningAppProcessInfo pi : list) {
                    if ((pi.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND  || pi.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE)  // Foreground or Visible
                            && pi.importanceReasonCode == RunningAppProcessInfo.REASON_UNKNOWN // Filter provider and service
                            && (0x4 & pi.flags) > 0 &&  pi.processState == ActivityManager.PROCESS_STATE_TOP) { // Must have activities and one activity is on the top
                        String pkgList[] = pi.pkgList;
                        if(pkgList != null && pkgList.length > 0) {
                            pkgName = pkgList[0];
                            if(SYSTEMUI_PKG.equals(pkgName)) {
                                continue;
                            }
                            activityName = pkgList[0];
                            if(pkgName.equals(getApplication().getPackageName())) {
                                activityName = "LockScreenActivity";
                                try {
                                    List<AppTask> tasks = mActivityManager.getAppTasks();
                                    if(tasks != null && tasks.size() > 0) {
                                        RecentTaskInfo rti =  tasks.get(0).getTaskInfo();
                                        if(rti != null) {
                                            Intent intent = rti.baseIntent;
                                            ComponentName cn = intent.getComponent();
                                            if(cn != null) {
                                                activityName = cn.getShortClassName();
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    
                                }
                            } else {
                                activityName = pi.processName;
                            }
                            break;
                        }
                    }
                }
            } else {
                List<RunningTaskInfo> tasks = mActivityManager.getRunningTasks(1);
                if(tasks != null && tasks.size() > 0) {
                    RunningTaskInfo topTaskInfo = tasks.get(0);
                    if (topTaskInfo.topActivity == null) {
                        return;
                    }
                    pkgName = topTaskInfo.topActivity.getPackageName();
                    activityName = topTaskInfo.topActivity.getShortClassName();
                }
            }
            // AM-940, filter com.estrongs.android.pop.app.UninstallMonitorActivity
            if(ES_UNINSTALL_ACTIVITY.equals(activityName)) {
                return;
            }
            if (mLockHandler != null && pkgName != null && activityName != null) {
                mLockHandler.handleAppLaunch(pkgName, activityName);
            }           
        }
	}

}
