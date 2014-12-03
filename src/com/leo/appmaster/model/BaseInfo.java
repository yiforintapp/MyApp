package com.leo.appmaster.model;

import android.graphics.drawable.Drawable;

public class BaseInfo {

	/**
	 * item type
	 */
	public static final int ITEM_TYPE_APP = 0;
	public static final int ITEM_TYPE_FOLDER = 1;
	public static final int ITEM_TYPE_BUSINESS_APP = 2;

	/**
	 * label
	 */
	public String label;

	/**
	 * icon
	 */
	public Drawable icon;

	/**
	 * top index
	 */
	public int topPos = -1;

	/**
	 * item type
	 */
	public int type = ITEM_TYPE_APP;
}
