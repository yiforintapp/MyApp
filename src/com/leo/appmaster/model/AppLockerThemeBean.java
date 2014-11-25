package com.leo.appmaster.model;

import com.leo.appmaster.Constants;

import android.graphics.drawable.Drawable;

public class AppLockerThemeBean {
	public String themeName;
	public String packageName;
	public String label;
	public int themeType = Constants.THEME_TYPE_DEFAULT;
	public Drawable themeImage;
	public String downloadUrl;
	public String previewUrl;
	public boolean curUsedTheme;
	public int size;
	public int tag;

}
