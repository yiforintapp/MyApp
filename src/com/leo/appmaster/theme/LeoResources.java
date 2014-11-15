package com.leo.appmaster.theme;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;



/**
 * In order to replace the resources for multi-theme, this tools through find  multi-theme's resources  
 * from other apk or not, like set a bridge between main apk on multi-theme apk. 
 * @author Tony.Lee
 *
 */
public class LeoResources {


	/**
	 * In inder to find the target resources from target package name 
	 * @param srcCtx original context 
	 * @param targetPkgName target package name
	 * @return a resource from target package name 
	 */
	public static Resources getThemeResources(Context srcCtx, String targetPkgName) {
		Resources targetResources = null;
		if (srcCtx != null && targetPkgName != null 
				&& !"".equals(targetPkgName)) {
			
			try {
				Context targetCtx = srcCtx.createPackageContext(targetPkgName, Context.CONTEXT_IGNORE_SECURITY);
				targetResources = targetCtx.getResources();
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return targetResources;
	}
	
	public static Context getThemeContext(Context srcCtx, String targetPkgName) {
		Context targetCtx = null;
		if (srcCtx != null && targetPkgName != null 
				&& !"".equals(targetPkgName)) {
			try {
				targetCtx = srcCtx.createPackageContext(targetPkgName, Context.CONTEXT_IGNORE_SECURITY);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return targetCtx;
	}

}
