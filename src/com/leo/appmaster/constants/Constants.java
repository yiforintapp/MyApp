package com.leo.appmaster.constants;

public class Constants {

	/*
	 * Server URL
	 */
	public static final String APP_LOCK_LIST_DEBUG = "http://test.leostat.com/appmaster/applockerrecommend";

	public static final String APP_LOCK_LIST_DEBUG2 = "http://192.168.1.142:8080/appmaster/appmaster/applockerrecommend";

	public static final int LOCK_TIP_INTERVAL_OF_DATE = 3;
	public static final int LOCK_TIP_INTERVAL_OF_MS = 3 * 24 * 60 * 60 * 1000;
//	 public static final int LOCK_TIP_INTERVAL_OF_MS = 1 * 60 * 1000;
	/*
	 * AppWall RequestTime
	 */
	public static final int REQUEST_TIMEOUT = 5 * 1000;// 设置请求超时5秒钟
	public static final int SO_TIMEOUT = 5 * 1000; // 设置等待数据超时时间5秒钟

}
