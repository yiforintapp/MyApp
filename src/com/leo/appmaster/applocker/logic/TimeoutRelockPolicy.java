package com.leo.appmaster.applocker.logic;

import java.util.HashMap;

import com.leo.appmaster.applocker.AppLockerPreference;

import android.content.Context;
import android.util.Log;

public class TimeoutRelockPolicy implements ILockPolicy {

	private static final String TAG = "TimeoutRelockPolicy";

	Context mContext;

	private HashMap<String, Long> mLockapp = new HashMap<String, Long>();

	public TimeoutRelockPolicy(Context mContext) {
		super();
		this.mContext = mContext;

	}

	public int getRelockTime() {
		return AppLockerPreference.getInstance(mContext).getRelockTimeout();
	}

	@Override
	public boolean onHandleLock(String pkg) {
		long curTime = System.currentTimeMillis();
		if (mLockapp.containsKey(pkg)) {
			long lastLockTime = mLockapp.get(pkg);
			Log.d(TAG, " curTime -  lastLockTime = " + (curTime - lastLockTime)
					+ "       mRelockTimeout =  " + getRelockTime());
			if ((curTime - lastLockTime) < getRelockTime())
				return true;
		} else {
			mLockapp.put(pkg, curTime);
		}
		return false;
	}

	public void clearLockApp() {
		mLockapp.clear();
	}

	@Override
	public void onUnlocked(String pkg) {
		long curTime = System.currentTimeMillis();
		Log.e("xxxx", "onUnlocked time: " + curTime);
		mLockapp.put(pkg, curTime);
	}

}
