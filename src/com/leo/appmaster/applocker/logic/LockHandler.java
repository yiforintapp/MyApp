package com.leo.appmaster.applocker.logic;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.utils.LeoLog;

public class LockHandler extends BroadcastReceiver {

	public static final String ACTION_APP_UNLOCKED = "com.leo.applocker.appunlocked";
	public static final String EXTRA_LOCKED_APP_PKG = "locked_app_pkg";

	private Context mContext;
	private ActivityManager mAm;
	private String mLastRunningPkg;
	private String mLastRuningActivity;

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
		if (pkg == null || activity == null)
			return;

		LeoLog.e("handleAppLaunch", pkg + "/" + activity);

		if (!pkg.equals(mLastRunningPkg)) {
			String myPackage = mContext.getPackageName();
			if (pkg.equals(myPackage)
					|| (mLastRunningPkg.equals(myPackage) && mLastRuningActivity
							.contains("LockScreenActivity"))) {
				mLastRunningPkg = pkg;
				mLastRuningActivity = activity;
				return;
			}
			mLastRunningPkg = pkg;
			mLastRuningActivity = activity;
			List<String> list = AppMasterPreference.getInstance(mContext)
					.getLockedAppList();
			if (list.contains(pkg)) {
				Intent intent = new Intent(mContext, LockScreenActivity.class);
				if (!mLockPolicy.onHandleLock(pkg)) {
					int lockType = AppMasterPreference.getInstance(mContext)
							.getLockType();
					if (lockType == AppMasterPreference.LOCK_TYPE_NONE)
						return;
					if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
						intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
								LockFragment.LOCK_TYPE_PASSWD);
					} else if (lockType == AppMasterPreference.LOCK_TYPE_GESTURE) {
						intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
								LockFragment.LOCK_TYPE_GESTURE);
					}
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
					intent.putExtra(EXTRA_LOCKED_APP_PKG, pkg);
					intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
							LockFragment.FROM_OTHER);
					mContext.startActivity(intent);
				}
			}
		} else {
			mLastRuningActivity = activity;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		LeoLog.e("LockReceiver", "action = " + intent.getAction());
		if (ACTION_APP_UNLOCKED.equals(intent.getAction())) {
			String pkg = intent.getStringExtra(EXTRA_LOCKED_APP_PKG);
			mLockPolicy.onUnlocked(pkg);
		} else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
			if (mLockPolicy instanceof TimeoutRelockPolicy) {
				if (AppMasterPreference.getInstance(context).isAutoLock()) {
					LeoLog.e("LockReceiver", "isAutoLock");
					((TimeoutRelockPolicy) mLockPolicy).clearLockApp();
				}
			}

			 Intent lockIntent = new Intent(context, LockService.class);
			 lockIntent.putExtra("lock_service", false);
			 context.startService(lockIntent);

		} else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
			 Intent lockIntent = new Intent(context, LockService.class);
			 lockIntent.putExtra("lock_service", true);
			 context.startService(lockIntent);
			judgeShowLockPage();
		}
	}

	private void judgeShowLockPage() {
		List<String> list = AppMasterPreference.getInstance(mContext)
				.getLockedAppList();
		LeoLog.e("onReceive", "mLastRunningPkg = " + mLastRunningPkg);
		if (list.contains(mLastRunningPkg)) {
			Intent intent2 = new Intent(mContext, LockScreenActivity.class);
			if (!mLockPolicy.onHandleLock(mLastRunningPkg)) {
				int lockType = AppMasterPreference.getInstance(mContext)
						.getLockType();
				if (lockType == AppMasterPreference.LOCK_TYPE_NONE)
					return;
				if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
					intent2.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
							LockFragment.LOCK_TYPE_PASSWD);
				} else if (lockType == AppMasterPreference.LOCK_TYPE_GESTURE) {
					intent2.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
							LockFragment.LOCK_TYPE_GESTURE);
				}
				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent2.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				intent2.putExtra(EXTRA_LOCKED_APP_PKG, mLastRunningPkg);
				intent2.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
						LockFragment.FROM_OTHER);
				mContext.startActivity(intent2);
			}
		}
	}
}
