package com.leo.appmaster.model;

import com.leo.appmaster.model.extra.AppPermissionInfo;
import com.leo.appmaster.model.extra.CacheInfo;
import com.leo.appmaster.model.extra.TrafficInfo;

public class AppItemInfo extends AppInfo {
	/*
	 * app cache info
	 */
	public CacheInfo mCacheInfo;
	/*
	 * all user permission of app
	 */
	public AppPermissionInfo mPermissionInfo;
	/*
	 * app traffic info
	 */
	public TrafficInfo mTrafficInfo;
	/*
	 * app power comsumption info
	 */
	public double mPowerComsuPercent;

	public String sourceDir;

	public boolean isChecked;

	public boolean isBackuped;

	public AppItemInfo() {
		mCacheInfo = new CacheInfo();
		mPermissionInfo = new AppPermissionInfo();
		mTrafficInfo = new TrafficInfo();
	}

	@Override
	public String toString() {
		return "local app: label = " + label + "    package = " + packageName;
	}

}
