package com.leo.appmaster.applocker.logic;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.leo.appmaster.applocker.AppLockerPreference;
import com.leo.appmaster.applocker.LockScreenActivity;

public class LockHandler extends BroadcastReceiver {

	private static final String TAG = "LockHandler";

	public static final String ACTION_APP_UNLOCKED = "com.leo.applocker.appunlocked";
	public static final String EXTRA_LOCKED_APP_PKG = "locked_app_pkg";

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
		if (mLockPolicy instanceof TimeoutRelockPolicy) {

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
			String myPackage = mContext.getPackageName();
			if (pkg.equals(myPackage) || mLastRunningPkg.equals(myPackage)) {
				mLastRunningPkg = pkg;
				return;
			}
			mLastRunningPkg = pkg;
			List<String> list = AppLockerPreference.getInstance(mContext)
					.getLockedAppList();
			if (list.contains(pkg)) {
				if (!mLockPolicy.onHandleLock(pkg)) {
					Intent intent = new Intent(mContext,
							LockScreenActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra(EXTRA_LOCKED_APP_PKG, pkg);
					mContext.startActivity(intent);
				}
			}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ACTION_APP_UNLOCKED.equals(intent.getAction())) {
			String pkg = intent.getStringExtra(EXTRA_LOCKED_APP_PKG);
			mLockPolicy.onUnlocked(pkg);
		}
	}

}
