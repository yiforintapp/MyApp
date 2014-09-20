package com.leo.applocker.logic;

import java.util.HashMap;

import com.leo.applocker.AppLockerPreference;

import android.content.Context;

public class TimeoutRelockPolicy implements ILockPolicy {

	Context mContext;

	private int mRelockTimeout;
	private HashMap<String, Long> mLockapp = new HashMap<String, Long>();

	public TimeoutRelockPolicy(Context mContext) {
		super();
		this.mContext = mContext;
		mRelockTimeout = getRelockTime();
	}

	public int getRelockTime() {
		return AppLockerPreference.getInstance(mContext).getRelockTimeout();
	}

	public void setRelockTime(int timeout) {
		AppLockerPreference.getInstance(mContext).setRelockTimeout(timeout);
	}

	@Override
	public boolean onHandleLock(String pkg) {
		if (mLockapp.containsKey(pkg)) {
			long lastLockTime = mLockapp.get(pkg);
			if ((System.currentTimeMillis() - lastLockTime) > mRelockTimeout)
				return true;
		} else {
			mLockapp.put(pkg, System.currentTimeMillis());
		}
		return false;
	}

	@Override
	public void onUnlocked(String pkg) {
		mLockapp.put(pkg, System.currentTimeMillis());
	}

}
