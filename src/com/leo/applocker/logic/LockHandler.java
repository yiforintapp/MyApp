package com.leo.applocker.logic;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.leo.applocker.AppLockerPreference;
import com.leo.applocker.LockScreenActivity;

public class LockHandler extends BroadcastReceiver{

	private static final String TAG = "LockHandler";

	public static final String ACTION_APP_UNLOCKED = "com.leo.applocker.appunlocked";
	public static final String EXTRA_UNLOCKED_APP_PKG = "unlocked_app_pkg";

	private Context mContext;
	private ActivityManager mAm;
	private String mLastRunningPkg;

	private ILockPolicy mLockPolicy;

	public LockHandler(Context mContext) {
		this.mContext = mContext;
		mAm = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		mLastRunningPkg = getRunningPkg();
	}
	public void setLockPolicy(ILockPolicy policy) {
		mLockPolicy = policy;
		if(mLockPolicy instanceof TimeoutRelockPolicy) {
			
		}
	}

	public ILockPolicy getLockPoliy() {
		return mLockPolicy;
	}

	private String getRunningPkg() {
		String pkg = null;
		List<RunningTaskInfo> list = mAm.getRunningTasks(1);
		if (list != null && !list.isEmpty()) {
			pkg = list.get(0).topActivity.getPackageName();
		}
		return pkg;
	}

	public void handleAppLaunch(String pkg, String activity) {
		Log.d(TAG, "handleLock: " + pkg + "/" + activity);
		if (pkg == null)
			return;
		if (!pkg.equals(mLastRunningPkg)) {
			
			mLastRunningPkg = pkg;
			List<String> list = AppLockerPreference.getInstance(mContext)
					.getLockedAppList();
			if (list.contains(pkg)) {
				if (!mLockPolicy.onHandleLock(pkg)) {
					Intent intent = new Intent(mContext,
							LockScreenActivity.class);
					intent.putExtra(LockScreenActivity.EXTRA_LOCK_PKG, pkg);
					mContext.startActivity(intent);
				}
			}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ACTION_APP_UNLOCKED.equals(intent.getAction())) {
			String pkg = intent.getStringExtra(EXTRA_UNLOCKED_APP_PKG);
			mLockPolicy.onUnlocked(pkg);
		}
	}

}
