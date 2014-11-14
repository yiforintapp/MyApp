package com.leo.appmaster.model;

public class AppLockerThemeBean {
		private String themeName;
		private int themeImage;
		private String[] url;
		private String packageName;
		private String flagName;
		public AppLockerThemeBean(String themeName, int themeImage,
				String[] url, String packageName, String flagName) {
			super();
			this.themeName = themeName;
			this.themeImage = themeImage;
			this.url = url;
			this.packageName = packageName;
			this.flagName = flagName;
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
		public int getThemeImage() {
			return themeImage;
		}
		public void setThemeImage(int themeImage) {
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
		
		
}
