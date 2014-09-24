package com.leo.appmaster.cleanmemory;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;

public class ProcessCleaner {

	private ActivityManager mAm;

	public ProcessCleaner(ActivityManager mAm) {
		this.mAm = mAm;
	}

	/**
	 * clean one process
	 * 
	 * @param pkg
	 */
	private void cleanProcess(String pkg) {
		mAm.killBackgroundProcesses(pkg);
	}

	/**
	 * clean multi processes
	 * 
	 * @param pkg
	 */
	private void cleanProcess(List<String> pkgs) {
		if (pkgs != null) {
			for (String pkg : pkgs) {
				mAm.killBackgroundProcesses(pkg);
			}
		}
	}

	/**
	 * clean all running process
	 */
	private void cleanAllProcess() {
		List<RunningAppProcessInfo> list = mAm.getRunningAppProcesses();
		for (RunningAppProcessInfo runningAppProcessInfo : list) {
			mAm.killBackgroundProcesses(runningAppProcessInfo.processName);
		}
	}
}
