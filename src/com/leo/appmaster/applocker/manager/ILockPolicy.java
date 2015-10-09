package com.leo.appmaster.applocker.manager;

public interface ILockPolicy {
	boolean onHandleLock(String pkg);

	void onUnlocked(String pkg);
}
