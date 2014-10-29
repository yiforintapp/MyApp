package com.leo.appmaster.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;

public class AppUtil {
	public static boolean isSystemApp(ApplicationInfo info) {
		// 有些系统应用是可以更新的，如果用户自己下载了一个系统的应用来更新了原来的，
		// 它就不是系统应用，这个就是判断这种情况的
		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			return true;
		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0)// 判断是不是系统应用
		{
			return true;
		}
		return false;
	}

	public static ApplicationInfo getApplicationInfo(String pkg, Context ctx) {
		ApplicationInfo info = null;
		try {
			ctx.getPackageManager().getApplicationInfo(pkg,
					PackageManager.GET_UNINSTALLED_PACKAGES);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return info;
	}

	public static String getAppLabel(String pkg, Context ctx) {
		try {
			return ctx
					.getPackageManager()
					.getApplicationLabel(
							ctx.getPackageManager().getApplicationInfo(pkg, 0))
					.toString();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isInstalledInSDcard(ApplicationInfo info) {
		if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
			return true;
		}
		return false;
	}

	public long getMobileTraffic() {
		return TrafficStats.getMobileRxBytes()
				+ TrafficStats.getMobileTxBytes();
	}

	public long getWifiTraffic() {
		long totalTraffic = TrafficStats.getTotalRxBytes()
				+ TrafficStats.getTotalTxBytes();
		return totalTraffic - getMobileTraffic();
	}

	public static Drawable getDrawable(PackageManager pm, String pkg) {
		Drawable d = null;
		try {
			d = pm.getApplicationIcon(pkg);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return d;
	}

	public static long getTotalTriffic() {
		return TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
	}
}
