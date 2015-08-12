package com.leo.appmaster.appmanage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;

public class BusinessAppInstallTracker {

	private HashMap<String, Long> mInstallMap;
	private long mTimeout;

	public BusinessAppInstallTracker() {
		super();
		mInstallMap = new HashMap<String, Long>();
		mTimeout = 2 * 60 * 60 * 1000;
	}

	public void onAppInstalled(String packageName) {
		LeoLog.d("BusinessAppInstallTracker", "onAppInstalled: " + packageName);
		if (mInstallMap.containsKey(packageName)) {
			long trackTime = mInstallMap.get(packageName);
			long curTime = System.currentTimeMillis();
			if ((curTime - trackTime) <= mTimeout) {
				// TODO
				LeoLog.d("BusinessAppInstallTracker", "app_act: " + packageName);
				SDKWrapper.addEvent(AppMasterApplication.getInstance(),
						SDKWrapper.P1, "app_act", packageName);
			}
			unTrack(packageName);
		}
	}

	public void track(String pkg) {
		LeoLog.d("BusinessAppInstallTracker", "track: " + pkg);
		if (pkg == null || "".equals(pkg))
			return;

		long curTime = System.currentTimeMillis();
		// filter all timeout app
		List<String> removeList = new ArrayList<String>();
		Set<String> keySet = mInstallMap.keySet();
		for (String key : keySet) {
			long trackTime = mInstallMap.get(key);
			if ((curTime - trackTime) > mTimeout) {
				removeList.add(key);
			}
		}
		if (!removeList.isEmpty()) {
			for (String removePkg : removeList) {
				mInstallMap.remove(removePkg);
			}
		}

		// add to track
		mInstallMap.put(pkg, curTime);
	}

	public void unTrack(String pkg) {
		mInstallMap.remove(pkg);
	}

	public void clearAllTrackedApp() {
		mInstallMap.clear();
	}

}
