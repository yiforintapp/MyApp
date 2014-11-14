package com.leo.appmaster.model;

public class AppLockerThemeBean {
		private String themeName;
		private int[] themeImage;
		private String[] url;
		public AppLockerThemeBean(String themeName, int[] themeImage,
				String[] url) {
			super();
			this.themeName = themeName;
			this.themeImage = themeImage;
			this.url = url;
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
		public int[] getThemeImage() {
			return themeImage;
		}
		public void setThemeImage(int[] themeImage) {
			this.themeImage = themeImage;
		}
		public String[] getUrl() {
			return url;
		}
		public void setUrl(String[] url) {
			this.url = url;
		}
		
}
