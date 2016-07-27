package com.zlf.appmaster.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Debug.MemoryInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class ProcessUtils {

	public static boolean isAppRunning(ActivityManager am, String name) {
		if (name == null)
			return false;
		List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
		for (RunningAppProcessInfo runningAppProcessInfo : list) {
			String[] pkgs = runningAppProcessInfo.pkgList;
			for (String pkg : pkgs) {
				if (pkg.equals(name))
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
		long mTotal = 0;
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
	        // beginIndex
	        int begin = content.indexOf(':');
	        // endIndex
	        int end = content.indexOf('k');
	        // 截取字符串信息
	        content = content.substring(begin + 1, end).trim();
	        mTotal = Integer.parseInt(content) * 1024L;
		} catch (Exception e) {
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
				}
			}
		}
		return mTotal;
	}
}
