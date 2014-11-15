package com.leo.appmaster.model;

import android.graphics.drawable.Drawable;

public class AppLockerThemeBean {
		private String themeName;
		private Drawable themeImage;
		private String[] url;
		private String packageName;
		private String flagName;
		private String isVisibility;
		public AppLockerThemeBean(String themeName, Drawable themeImage,
				String[] url, String packageName, String flagName,String isVisibility) {
			super();
			this.themeName = themeName;
			this.themeImage = themeImage;
			this.url = url;
			this.packageName = packageName;
			this.flagName = flagName;
			this.isVisibility=isVisibility;
		}
		public AppLockerThemeBean() {
			super();
		}
		public String getThemeName() {
			return themeName;
		}
		public void setThemeName(String themeName) {
			this.themeName = themeName;
		}
		public Drawable getThemeImage() {
			return themeImage;
		}
		public void setThemeImage(Drawable themeImage) {
			this.themeImage = themeImage;
		}
		public String[] getUrl() {
			return url;
		}
		public void setUrl(String[] url) {
			this.url = url;
		}
		public String getPackageName() {
			return packageName;
		}
		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}
		public String getFlagName() {
			return flagName;
		}
		public void setFlagName(String flagName) {
			this.flagName = flagName;
		}
		public String getIsVisibility() {
			return isVisibility;
		}
		public void setIsVisibility(String isVisibility) {
			this.isVisibility = isVisibility;
		}
		
		
}
