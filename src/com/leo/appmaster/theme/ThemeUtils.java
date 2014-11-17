package com.leo.appmaster.theme;

import com.leo.appmaster.AppMasterApplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;

/**
 * Theme utility for operate caller 
 * @author Tony Lee
 *
 */
public class ThemeUtils {

	/**
	 * Name of target package name for the theme 
	 */
	public static final String THEME_PREFERENCE = "theme_package_name_apply"; 
	
	/**
	 * Name of share preferences 
	 */
	public static final String THEME = "theme";
	
	/**
	 * Check theme is setup or not
	 * @param context
	 * @return true is had setup, otherwise nothing to be done.
	 */
    public static boolean checkThemeNeed(Context context) {
        boolean need = false;

        if (AppMasterApplication.getSelectedTheme().equals(
                AppMasterApplication.getInstance().getPackageName())) {
            need = false;
        } else {
            need = true;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    AppMasterApplication.getSelectedTheme(), 0);
            if (packageInfo == null) {
                need = false;
            } else {
                need = true;
            }
        } catch (Exception e) {
            need = false;
        }

        return need;

    }

	/**
	 * Fetch the package name of theme, that was be setup for lock fragment. 
	 * @param context
	 * @return Name of package.
	 */
	public static String getMultiThemePkgName(Context context) {
		if (context != null) {
			SharedPreferences preferences = context.getSharedPreferences(THEME, Context.MODE_PRIVATE);
			String applyPkgName = preferences.getString(THEME_PREFERENCE, "");
			return applyPkgName;
		}
		return null;
	}
	
	/**
	 * A tools for get the theme was had setup 
	 * @param context
	 * @param pkgName package name
	 */
	public static void setThemeTarget(Context context, String pkgName) {
		if (context !=null) {
			SharedPreferences preferences = context.getSharedPreferences(THEME, Context.MODE_PRIVATE);
			
			/* just check target package name can not be null , but it can be empty like "" */
			/* if target package name is empty, so in order to reset the theme for lock fragment */
			if (pkgName != null) {
				
				Editor editor = preferences.edit();
				editor.putString(THEME_PREFERENCE, pkgName);
				
				editor.commit();
			}
		}
	}
	
	/**
	 * Get the resource like color, drawable, layout ...etc value <br/> 
	 *   eg: public static final int activity_bg = 0x11111111  <br/>
	 * return this 0x11111111 <br/>
	 * 
	 *  why load the resourece use the theme context? because of the value of the resource name no the same of own apk <br/>
	 *  own apk : public static final int activity_bg = 0x11111111<br/>
	 *  theme apk: may be public static final int activity_bg = 0x11111112 
	 * @param context themeContext use context = createPackageContext(themePkgName, Context.CONTEXT_IGNORE_SECURITY)
	 * @param resType rawType  drawable | color | layout | menu | ...
	 * @param resName the resourece name eg:activity_bg
	 * @return  real value of resource on the theme apk 
	 */
	public static int getValueByResourceName(Context context, String resType,  String resName) {
		int reValue = 0;
		if (context != null) {
			
			if (resName != null && !"".equals(resName)
					&& resType != null && !"".equals(resType)) {
				
//				String realName = resName.substring(resName.lastIndexOf("/") + 1, resName.length());
				
				reValue = context.getResources().getIdentifier(resName, resType, context.getPackageName());
			}
			
		}
		return reValue;
		
	}
	
	
}
