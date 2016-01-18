package com.leo.appmaster.model;

import com.leo.appmaster.model.extra.AppPermissionInfo;
import com.leo.appmaster.model.extra.CacheInfo;
import com.leo.appmaster.model.extra.TrafficInfo;

public class AppItemInfo extends AppInfo {
	/*
	 * app cache info
	 */
	public CacheInfo cacheInfo;
	/*
	 * all user permission of app
	 */
	public AppPermissionInfo permissionInfo;
	/*
	 * app traffic info
	 */
	public TrafficInfo trafficInfo;
	/*
	 * app power comsumption info
	 */
	public double powerComsuPercent;

	public boolean detailLoaded = false;
	
	public String sourceDir;
	
	public long lastLaunchTime;

	public String className;

	public AppItemInfo() {
		cacheInfo = new CacheInfo();
		permissionInfo = new AppPermissionInfo();
		trafficInfo = new TrafficInfo();
	}

	@Override
	public String toString() {
		return "local app: label = " + label + "    package = " + packageName;
	}

}
