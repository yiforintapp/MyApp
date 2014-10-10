package com.leo.appmaster.fragment;

import android.content.Intent;

public abstract class LockFragment extends BaseFragment {
	
	protected final int mMaxInput = 5;
	
	public static final int FROM_SELF = 0;
	public static final int FROM_OTHER = 1;
	protected int mFrom = FROM_SELF;
	
	public static final int LOCK_TYPE_PASSWD = 0;
	public static final int LOCK_TYPE_GESTURE = 1;
	protected int mLockType = LOCK_TYPE_PASSWD;
	
	protected int mInputCount = 0;

	protected String mPackage;

	public void setPackage(String pkg) {
		mPackage = pkg;
	}
	
	public void setLockType(int type) {
		mLockType = type;
	}
	
	public void setFrom(int from){
		mFrom = from;
	}
	
	public abstract void onNewIntent(Intent intent);
}
