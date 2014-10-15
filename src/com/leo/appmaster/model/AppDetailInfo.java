
package com.leo.appmaster.model;


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
    private double mPowerComsuPercent;

    private String mSourceDir;
    
    public boolean isChecked;
    
    public boolean isBackuped;
    
    public int topPos = -1;

    public AppDetailInfo() {
        mCacheInfo = new CacheInfo();
        mPermissionInfo = new AppPermissionInfo();
        mTrafficInfo = new TrafficInfo();
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

    public double getPowerComsuPercent() {
        return mPowerComsuPercent;
    }

    public void setPowerComsuPercent(double percentOfTotal) {
        this.mPowerComsuPercent = percentOfTotal;
    }

    public String getSourceDir() {
        return mSourceDir;
    }

    public void setSourceDir(String dir) {
        this.mSourceDir = dir;
    }

}
