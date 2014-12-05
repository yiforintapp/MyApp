package com.leo.appmaster.model;

public class BusinessItemInfo extends BaseInfo {
	/**
	 * app list
	 */
	public static final int CONTAIN_APPLIST = 0;
	/**
	 * system folder
	 */
	public static final int CONTAIN_SYSTEM_FOLDER = 1;
	/**
	 * running folder
	 */
	public static final int CONTAIN_RUNNING_FOLDER = 2;
	/**
	 * business folder
	 */
	public static final int CONTAIN_BUSINESS_FOLDER = 3;

	/**
	 * contain type of four above
	 */
	public int containType;

	/**
	 * app package name
	 */
	public String packageName;

	/**
	 * icon url
	 */
	public String iconUrl;

	/**
	 * app icon loaded
	 */
	public boolean iconLoaded;

	/**
	 * app download url
	 */
	public String appDownloadUrl;

	/**
	 * app package size
	 */
	public long appSize;
	
	/**
	 * the download priority
	 */
	public int gpPriority;
	
	/**
	 * gp url
	 */
	public String gpUrl;

	/**
	 * app type
	 */
	public int appType;

	/**
	 * Tag
	 */
	public Object tag;

}
