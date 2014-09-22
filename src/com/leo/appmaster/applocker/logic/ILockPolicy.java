package com.leo.appmaster.applocker.logic;

public interface ILockPolicy {
	boolean onHandleLock(String pkg);

	void onUnlocked(String pkg);
}
