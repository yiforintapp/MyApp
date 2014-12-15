package com.leo.appmaster;

import android.net.Uri;

public class Constants {

	public static final String DATABASE_NAME = "appmaster.db";
	public static final int DATABASE_VERSION = 2;

	public static final String AUTHORITY = "com.leo.appmaster.provider";
	public static final String ID = "_id";

	public static final String TABLE_DOWNLOAD = "download";
	public static final String TABLE_FEEDBACK = "feedback";
	public static final String TABLE_APPLIST_BUSINESS = "applist_business";

	public static final Uri DOWNLOAD_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_DOWNLOAD);

	public static final Uri FEEDBACK_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_FEEDBACK);

	public static final Uri APPLIST_BUSINESS_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_APPLIST_BUSINESS);

	// download table
	public static final String COLUMN_DOWNLOAD_FILE_NAME = "file_name";
	public static final String COLUMN_DOWNLOAD_DESTINATION = "dest";
	public static final String COLUMN_DOWNLOAD_URL = "url";
	public static final String COLUMN_DOWNLOAD_MIME_TYPE = "mime_type";
	public static final String COLUMN_DOWNLOAD_TOTAL_SIZE = "total_size";
	public static final String COLUMN_DOWNLOAD_CURRENT_SIZE = "current_size";
	public static final String COLUMN_DOWNLOAD_STATUS = "status";
	public static final String COLUMN_DOWNLOAD_DATE = "download_date";
	public static final String COLUMN_DOWNLOAD_TITLE = "title";
	public static final String COLUMN_DOWNLOAD_DESCRIPTION = "description";
	public static final String COLUMN_DOWNLOAD_WIFIONLY = "wifionly";

	// download status
	public static final int RESULT_SUCCESS = 0;
	public static final int RESULT_FAILED = 1;
	public static final int RESULT_CANCELLED = 2;
	public static final int RESULT_FAILED_SDCARD = 3;
	public static final int RESULT_FAILED_NO_NETWORK = 4;
	public static final int RESULT_FAILED_SDCARD_INSUFFICIENT = 5;

	// download parameter
	public static final String PARAMETER_NOTIFY = "notify";

	// download type
	public static final String MIME_TYPE_THEME_ICON = "ICON";
	// public static final String MIME_TYPE_WALLPAPER = "wallpaper";
	public static final String MIME_TYPE_PUSH = "apk";

	// download action
	public static final String ACTION_DOWNLOAD_ADD = "com.leo.appmaster.download.add";
	public static final String ACTION_DOWNLOAD_STOP = "com.leo.appmaster.download.stop";
	public static final String ACTION_DOWNLOAD_PAUSE = "com.leo.appmaster.download.pause";
	public static final String ACTION_DOWNLOAD_START = "com.leo.appmaster.download_start";
	public static final String ACTION_DOWNLOAD_PROGRESS = "com.leo.appmaster.download_progress";
	public static final String ACTION_DOWNLOAD_COMPOLETED = "com.leo.appmaster.download_completed";

	public static final String TYPE = "type";
	public static final String EXTRA_TIME = "extra_time";
	public static final int PROGRESS_INTERVAL = 1000;

	// mime type
	public static final String MIME_TYPE_BUSINESS_APK = "application";

	// download notify
	public static final String EXTRA_ID = "extra_id";
	public static final String EXTRA_TOTAL = "extra_total";
	public static final String EXTRA_CURRENT = "extra_current";
	public static final String EXTRA_TITLE = "extra_title";
	public static final String EXTRA_PROGRESS = "extra_progress";
	public static final String EXTRA_RESULT = "extra_result";
	public static final String EXTRA_NOTIFY_TYPE = "extra_notify_type";
	public static final String EXTRA_DEST_PATH = "extra_dest_path";
	public static final String EXTRA_URL = "extra_url";
	public static final String EXTRA_MIMETYPE = "extra_mimetype";

	// Message
	public static final int MESSAGE_SHORTCUT_INSTALLED = 100;
	public static final int MESSAGE_SHORTCUT_NOSPACE = 101;
	public static final int MESSAGE_SHORTCUT_UNINSTALLED = 102;
	public static final int MESSAGE_DOWNLOAD_FAILED = 103;

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

	/*
	 * RECOMMEND URL
	 */
	public static final String APP_RECOMMEND_URL = "http://192.168.1.201:8080/leo/appmaster/apprecommend/list";

}
