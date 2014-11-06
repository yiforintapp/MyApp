package com.leo.appmaster.fragment;

import com.leo.appmaster.applocker.logic.LockHandler;

import android.content.Intent;

public abstract class LockFragment extends BaseFragment {

	protected final int mMaxInput = 5;

	public static final int FROM_SELF = 0;
	public static final int FROM_OTHER = 1;
    public static final int FROM_RESTART = 2;
	protected int mFrom = FROM_SELF;

	public static final int LOCK_TYPE_PASSWD = 0;
	public static final int LOCK_TYPE_GESTURE = 1;
	protected int mLockType = LOCK_TYPE_PASSWD;

	protected int mInputCount = 0;

	protected String mPackage;
	protected String mActivityName;

	public void setPackage(String pkg) {
		mPackage = pkg;
	}

	public void setActivity(String activity) {
		mActivityName = activity;
	}

	public void setLockType(int type) {
		mLockType = type;
	}

	public void setFrom(int from) {
		mFrom = from;
	}

	protected void unlockSucceed(String pkg) {
		Intent intent = new Intent(LockHandler.ACTION_APP_UNLOCKED);
		intent.putExtra(LockHandler.EXTRA_LOCKED_APP_PKG, pkg);
		mActivity.sendBroadcast(intent);
	}

	public abstract void onNewIntent(Intent intent);

}
