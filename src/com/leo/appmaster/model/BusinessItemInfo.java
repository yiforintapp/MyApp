package com.leo.appmaster.model;

public class BusinessItemInfo extends BaseInfo {
	/**
	 * app list
	 */
	public static final int CONTAIN_APPLIST = 1;
	/**
	 * system folder
	 */
	public static final int CONTAIN_SORT_FLOW = 2;
	/**
	 * running folder
	 */
	public static final int CONTAIN_CAPACITY_SORT = 3;
	/**
	 * business folder
	 */
	public static final int CONTAIN_BUSINESS_FOLDER = 4;

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

//	/**
//	 * app type
//	 */
//	public int appType;

	/**
	 * Tag
	 */
	public Object tag;
	
	/**
	 * is local app
	 */
	public boolean installed;

	@Override
	public String toString() {
		return "label = " + label +  "       packageName = "+ packageName +"    iconUrl = " + iconUrl + "  appSize = "
				+ appSize + "    type = " + type + "  appDownloadUrl =  "
				+ appDownloadUrl + "    gpUrl = " + gpUrl + "   icon = " + icon;
	}
}
