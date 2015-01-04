package com.leo.appmaster.model;

public class FolderItemInfo extends BaseInfo {
	
	/**
	 * business app folder
	 */
	public static final int FOLDER_BUSINESS_APP = 0;
	/**
	 * system backup folder
	 */
	public static final int FOLDER_BACKUP_RESTORE = 1;
	/**
	 * system app folder
	 */
	public static final int FOLDER_FLOW_SORT = 2;
	/**
	 * running app folder
	 */
	public static final int FOLDER_CAPACITY_SORT = 3 ;


	/**
	 * one of four type above
	 */
	public int folderType;

	/**
	 * item count in this folder
	 */
	public int itemCount;

	/**
	 * folder tag
	 */
	public Object tag;

}
