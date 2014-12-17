package com.leo.appmaster.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Debug.MemoryInfo;

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

	// get app used memory
	public static long getAppUsedMem(Context ctx, String pkg) {
		if (pkg == null || pkg.equals("")) {
			return 0;
		} else {
			ActivityManager am = (ActivityManager) ctx
					.getSystemService(Context.ACTIVITY_SERVICE);
			try {
				List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
				for (RunningAppProcessInfo runningAppProcessInfo : list) {

					if (runningAppProcessInfo.processName.equals(pkg)) {
						MemoryInfo[] memInfos = am
								.getProcessMemoryInfo(new int[] { runningAppProcessInfo.pid });
						return memInfos[0].dalvikPrivateDirty;
					}
				}
				return 0;
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}

		}

	}

	// 获取已用内存大小
	public static long getUsedMem(Context mContext) {
		return getTotalMem() - getAvailableMem(mContext);
	}

	// 获得可用的内存,以B为单位
	public static long getAvailableMem(Context mContext) {
		long MEM_UNUSED;
		// 得到ActivityManager
		ActivityManager am = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		// 创建ActivityManager.MemoryInfo对象

		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);

		// 取得剩余的内存空间

		MEM_UNUSED = mi.availMem;
		return MEM_UNUSED;
	}

	// 获得总内存, 以B为单位
	public static long getTotalMem() {
		long mTotal;
		// /proc/meminfo读出的内核信息进行解释
		String path = "/proc/meminfo";
		String content = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path), 8);
			String line;
			if ((line = br.readLine()) != null) {
				content = line;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// beginIndex
		int begin = content.indexOf(':');
		// endIndex
		int end = content.indexOf('k');
		// 截取字符串信息

		content = content.substring(begin + 1, end).trim();

		mTotal = Integer.parseInt(content) * 1024L;
		return mTotal;
	}
}
