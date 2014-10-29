package com.leo.appmaster.model;

import android.graphics.drawable.Drawable;
// app class
public class BaseInfo {
	/*
	 * app package name
	 */
	private String mPkg;
	/*
	 * app name
	 */
	private String mAppLabel;

	/*
	 * app icon, read from pm
	 */
	private Drawable mAppIcon;

	/*
	 * is system app
	 */
	private boolean mSystemApp;
	/*
	 * install in external
	 */
	private boolean mInSdcard;
	
	/*
	 * app uid
	 */
	private int mUid;

	/*
	 * is locked
	 */
	private boolean mLocked;
	
	/*
	 * top index
	 */
	public int topPos = -1;
	
	/*
	 * app install time
	 */
	public long installTime = -1l;
	
	private int versionCode;
    private String versionName;
	
	public String getPkg() {
		return mPkg;
	}

	public void setPkg(String mPkg) {
		this.mPkg = mPkg;
	}

	public String getAppLabel() {
		return mAppLabel;
	}

	public void setAppLabel(String mAppLabel) {
		this.mAppLabel = mAppLabel;
	}

	public Drawable getAppIcon() {
		return mAppIcon;
	}

	public void setAppIcon(Drawable mAppIcon) {
		this.mAppIcon = mAppIcon;
	}

	public boolean isSystemApp() {
		return mSystemApp;
	}

	public void setSystemApp(boolean mIsSystem) {
		this.mSystemApp = mIsSystem;
	}

	public boolean isInSdcard() {
		return mInSdcard;
	}

	public void setInSdcard(boolean mIsInSdcard) {
		this.mInSdcard = mIsInSdcard;
	}

	public int getUid() {
		return mUid;
	}

	public void setUid(int mUid) {
		this.mUid = mUid;
	}

	public boolean isLocked() {
		return mLocked;
	}

	public void setLocked(boolean mLocked) {
		this.mLocked = mLocked;
	}
	
	public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

	
}
