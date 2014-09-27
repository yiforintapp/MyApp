package com.leo.appmaster.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;

public class ProcessUtils {

	public static boolean isAppRunning(ActivityManager am, String name) {
		if (name == null)
			return false;
		List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
		for (RunningAppProcessInfo runningAppProcessInfo : list) {
			String[] pkgs = runningAppProcessInfo.pkgList;
			for (String pkg : pkgs) {
				if (pkgs.equals(name))
					return true;
			}
		}
		return false;
	}

}
