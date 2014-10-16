package com.leo.appmaster.cleanmemory;

import java.util.List;

import com.leo.appmaster.utils.ProcessUtils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

public class ProcessCleaner {

	private Context mContext;
	private ActivityManager mAm;
	private static ProcessCleaner mInstance;

	private long mLastCleanTime;
	private long mLastMemUsed;
	private long mMemTotal;
	private long mLastCleanMem;

	private static int CLEAN_INTERVAL = 30 * 1000;

	public static synchronized ProcessCleaner getInstance(Context ctx) {
		if (mInstance == null) {
			mInstance = new ProcessCleaner(
					(ActivityManager) ctx
							.getSystemService(Context.ACTIVITY_SERVICE),
					ctx);
		}

		return mInstance;
	}

	public long getTotalMem() {
		if (mMemTotal == 0) {
			mMemTotal = ProcessUtils.getTotalMem();
		}
		return mMemTotal;
	}

	public long getLastCleanMem() {
		return mLastCleanMem;
	}
	
	public long getUsedMem() {
		long curTime = System.currentTimeMillis();
		if ((curTime - mLastCleanTime) > CLEAN_INTERVAL) {
			mLastMemUsed = ProcessUtils.getUsedMem(mContext);
		}

		return mLastMemUsed;
	}

	public boolean allowClean() {
		long curTime = System.currentTimeMillis();
		return (curTime - mLastCleanTime) > CLEAN_INTERVAL;
	}

	public ProcessCleaner(ActivityManager mAm, Context ctx) {
		this.mAm = mAm;
		this.mContext = ctx.getApplicationContext();
	}

	public long tryClean() {
		long resault = -1;
		long curTime = System.currentTimeMillis();
		if ((curTime - mLastCleanTime) > CLEAN_INTERVAL) {
			mLastCleanTime = curTime;
			cleanAllProcess();
			cleanAllProcess();
			long curUsedMem = ProcessUtils.getUsedMem(mContext);
			resault = mLastCleanMem = Math.abs(curUsedMem - mLastMemUsed);
			mLastMemUsed = curUsedMem;
		}
		return resault;
	}

	/**
	 * clean one process
	 * 
	 * @param pkg
	 */
	public void cleanProcess(String pkg) {
		mAm.killBackgroundProcesses(pkg);
	}

	/**
	 * clean multi processes
	 * 
	 * @param pkg
	 */
	public void cleanProcess(List<String> pkgs) {
		if (pkgs != null) {
			for (String pkg : pkgs) {
				mAm.killBackgroundProcesses(pkg);
			}
		}
	}

	/**
	 * clean all running process
	 */
	public void cleanAllProcess() {
		List<RunningAppProcessInfo> list = mAm.getRunningAppProcesses();
		for (RunningAppProcessInfo runningAppProcessInfo : list) {
			mAm.killBackgroundProcesses(runningAppProcessInfo.processName);
		}
	}
}
