package com.leo.appmaster.model;

import android.content.pm.PermissionInfo;

public class AppDetailInfo extends BaseInfo {
	/*
	 * app cache info
	 */
	private CacheInfo mCacheInfo;
	/*
	 * all user permission of app
	 */
	private AppPermissionInfo mPermissionInfo;
	/*
	 * app traffic info
	 */
	private TrafficInfo mTrafficInfo;
	/*
	 * app power comsumption info
	 */
	private PowerComsuInfo mPowerComInfo;

	public AppDetailInfo() {
		mCacheInfo = new CacheInfo();
		mPermissionInfo = new AppPermissionInfo();
		mTrafficInfo = new TrafficInfo();
		mPowerComInfo = new PowerComsuInfo();
	}

	public CacheInfo getCacheInfo() {
		return mCacheInfo;
	}

	public void setCacheInfo(CacheInfo mCacheInfo) {
		this.mCacheInfo = mCacheInfo;
	}

	public AppPermissionInfo getPermissionInfo() {
		return mPermissionInfo;
	}

	public void setPermissionInfo(AppPermissionInfo permissionInfo) {
		this.mPermissionInfo = permissionInfo;
	}

	public TrafficInfo getTrafficInfo() {
		return mTrafficInfo;
	}

	public void setTrafficInfo(TrafficInfo mTrafficInfo) {
		this.mTrafficInfo = mTrafficInfo;
	}

	public PowerComsuInfo getPowerComInfo() {
		return mPowerComInfo;
	}

	public void setPowerComInfo(PowerComsuInfo mPowerComInfo) {
		this.mPowerComInfo = mPowerComInfo;
	}

}
