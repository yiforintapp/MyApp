
package com.leo.appmaster.model;

public class AppInfo extends BaseInfo {
    /*
     * app package name
     */
    public String packageName;
    /*
     * activity name
     */
    public String activityName;
    /*
     * is system app
     */
    public boolean systemApp;
    /*
     * install in external
     */
    public boolean inSdcard;
    /*
     * app uid
     */
    public int uid;
    /*
     * is locked
     */
    public boolean isLocked;
    /*
     * is locked
     */
    public boolean isRecomment = false;
    /*
     * app install time
     */
    public long installTime = -1l;
    /**
     * app version code
     */
    public int versionCode;
    /**
     * app version name
     */
    public String versionName;

    public boolean isChecked;

    public boolean isBackuped;

    public long backupTime;

    public String titleName;

}
