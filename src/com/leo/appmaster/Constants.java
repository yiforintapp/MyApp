
package com.leo.appmaster;

import android.provider.MediaStore.MediaColumns;

public class Constants {

    /**
     * Image Loader
     */
    public static final int MAX_MEMORY_CACHE_SIZE = 5 * (1 << 20);// 5M
    public static final int MAX_DISK_CACHE_SIZE = 50 * (1 << 20);// 20 Mb
    public static final int MAX_THREAD_POOL_SIZE = 3;

    /*
     * Server URL
     */
    public static final String APP_LOCK_LIST_DEBUG = "http://test.leostat.com/appmaster/applockerrecommend";

    public static final String APP_LOCK_LIST_DEBUG2 = "http://192.168.1.142:8080/appmaster/appmaster/applockerrecommend";
    public static final String GP_PACKAGE = "com.android.vending";// GP package
                                                                  // name
    public static final int LOCK_TIP_INTERVAL_OF_DATE = 3;
    public static final int LOCK_TIP_INTERVAL_OF_MS = 3 * 24 * 60 * 60 * 1000;
    // public static final int LOCK_TIP_INTERVAL_OF_MS = 1 * 60 * 1000;
    /*
     * AppWall RequestTime
     */
    public static final int REQUEST_TIMEOUT = 5 * 1000;
    public static final int SO_TIMEOUT = 5 * 1000;
    /*
     * LockerTheme
     */
    public static final String ACTION_NEW_THEME = "com.leo.appmaster.newtheme";
    public static final String DEFAULT_THEME = "com.leo.theme.default";// default
                                                                       // theme
    /**
     * theme type
     */
    public static final int THEME_TYPE_DEFAULT = 0;
    public static final int THEME_TYPE_LOCAL = 1;
    public static final int THEME_TYPE_ONLINE = 2;

    /**
     * theme tag
     */
    public static final int THEME_TAG_NONE = 0;
    public static final int THEME_TAG_NEW = 1;
    public static final int THEME_TAG_HOT = 2;

    /**
     * online theme url
     */
    public static final String ONLINE_THEME_URL = "http://api.leostat.com/appmaster/themes";
    public static final String CHECK_NEW_THEME = "http://api.leostat.com/appmaster/themesupdatecheck";

    /**
     * for compat first version theme preview url
     */
    public static final String THEME_MOONNIGHT_URL = "http://files.leostat.com/theme/img/night.jpg";
    public static final String THEME_CHRISTMAS_URL = "http://files.leostat.com/theme/img/christmas.jpg";
    public static final String THEME_FRUIT_URL = "http://files.leostat.com/theme/img/fruit.jpg";
    public static final String THEME_SPATIAL_URL = "http://files.leostat.com/theme/img/spatial.jpg";

    /**
     * compat theme package
     */
    public static final String THEME_PACKAGE_NIGHT = "com.leo.theme.moonnight";
    public static final String THEME_PACKAGE_CHRITMAS = "com.leo.theme.christmas";
    public static final String THEME_PACKAGE_FRUIT = "com.leo.theme.orange";
    public static final String THEME_PACKAGE_SPATIAL = "com.leo.theme.contradict";
    /**
     * HideVideo
     */
    public static final int CANCLE_HIDE_MODE = 0;
    public static final int SELECT_HIDE_MODE = 1;
    public static final String VIDEO_FORMAT = MediaColumns.DATA + " LIKE '%.mp4'" + " or "
            + MediaColumns.DATA
            + " LIKE '%.avi'" + " or " + MediaColumns.DATA + " LIKE '%.mpe'" + " or "
            + MediaColumns.DATA + " LIKE '%.rm'" + " or " + MediaColumns.DATA + " LIKE '%.wmv'"
            + " or " + MediaColumns.DATA + " LIKE '%.vob'"
            + " or " + MediaColumns.DATA + " LIKE '%.mov'" + " or " + MediaColumns.DATA
            + " LIKE '%.mpg'" + " or " + MediaColumns.DATA + " LIKE '%.mpeg'" + " or "
            + MediaColumns.DATA + " LIKE '%.flv'" + " or " + MediaColumns.DATA
            + " LIKE '%.f4v'" + " or " + MediaColumns.DATA + " LIKE '%.3gp'" + " or "
            + MediaColumns.DATA + " LIKE '%.asf'"
            + " or " + MediaColumns.DATA + " LIKE '%.m4v'" + " or " + MediaColumns.DATA
            + " LIKE '%.rmvb'" + " or " + MediaColumns.DATA + " LIKE '%.mkv'";
    public static final String VIDEO_PLUS_GP_URL = "https://play.google.com/store/apps/details?id=com.leomaster.videomaster&referrer=utm_source%3Dad_amtuiguang_01market://details?id= com.leomaster.videomaster&referrer=utm_source%3Dad_amtuiguang_01";
}
