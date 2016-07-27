package com.zlf.appmaster.applocker.model;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.utils.LeoLog;

import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 使用UsageStatsManager获取最近历史
 * Created by Jasper on 2015/9/23.
 */
@SuppressLint("NewApi")
public class ProcessDetectorUsageStats extends com.zlf.appmaster.applocker.model.ProcessDetector {
    private static final String TAG = "PDectorUsageStats";

    private static final boolean DBG = false;

    private static final int MAX_RETYR = 5;

    private static final int WAIT_TIMEOUT = 200;

    private Object mStatsManager;

    private int mRetryCount;

    public ProcessDetectorUsageStats() {
        AppMasterApplication context = AppMasterApplication.getInstance();
        try {
            mStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkAvailable() {
        if (mStatsManager == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -1);
        long startTime = calendar.getTimeInMillis();

        UsageStatsManager statsManager = (UsageStatsManager) mStatsManager;
        List<UsageStats> list = statsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                startTime, endTime);
        return list != null && list.size() > 0;
    }

    @Override
    public com.zlf.appmaster.applocker.model.ProcessAdj getForegroundProcess() {
        if (mStatsManager == null) {
            return null;
        }
        UsageStatsManager statsManager = (UsageStatsManager) mStatsManager;

        Calendar calendar = Calendar.getInstance();
        long endTs = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -1);
        long startTs = calendar.getTimeInMillis();
        startTs = startTs < 0 ? 0 : startTs;
        List<UsageStats> stats = statsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTs, endTs);

        com.zlf.appmaster.applocker.model.ProcessAdj processAdj = null;
        if (stats != null) {
            SortedMap<Long, UsageStats> runningTask = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : stats) {
                if (stats != null) {
                    runningTask.put(usageStats.getLastTimeUsed(), usageStats);
                }
            }
            logTasks(runningTask);
            if (runningTask.isEmpty()) {
                if (DBG) {
                    Log.d(TAG, "there is no app found.");
                }
                LeoLog.i(TAG, "there is no app found.");
            } else {
                String pkg = runningTask.get(runningTask.lastKey()).getPackageName();

                Context ctx = AppMasterApplication.getInstance();
                if (mRetryCount < MAX_RETYR && !AppUtil.isInstallPkgName(ctx, pkg) ||
                        (!hasLaunchIntent(pkg) && !isHome(pkg))) {
                    LeoLog.d(TAG, "the pkg is not installed or has no launch intent, so retry again.");
                    mRetryCount++;
                    return getForegroundProcess();
                }
                mRetryCount = 0;
                if (DBG) {
                    Log.i(TAG, "pkg: " + pkg);
                }
                LeoLog.i(TAG, "pkg: " + pkg);
                processAdj = new com.zlf.appmaster.applocker.model.ProcessAdj();
                processAdj.pkg = pkg;
            }
        }

        return processAdj;
    }

    @Override
    public int getTimeoutMs(com.zlf.appmaster.applocker.model.ProcessAdj proAdj) {
        return WAIT_TIMEOUT;
    }

    private void logTasks(SortedMap<Long, UsageStats> runningTask) {
        if (DBG) {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            for (Long aLong : runningTask.keySet()) {
                builder.append("[")
                        .append(aLong)
                        .append("->")
                        .append(runningTask.get(aLong))
                        .append("]")
                        .append(",");
            }
            builder.append("}");
            Log.d(TAG, builder.toString());
        }
    }

    private boolean hasLaunchIntent(String pkgName) {
        Context ctx = AppMasterApplication.getInstance();
        Intent intent = null;
        try {
            intent = ctx.getPackageManager().getLaunchIntentForPackage(pkgName);
        } catch (Exception e) {
        }

        return intent != null;
    }

    private boolean isHome(String pkgName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        Context ctx = AppMasterApplication.getInstance();
        List<ResolveInfo> homeList = ctx.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : homeList) {
            if (resolveInfo == null || resolveInfo.activityInfo == null || resolveInfo.activityInfo.packageName == null) {
                continue;
            }

            if (resolveInfo.activityInfo.packageName.equals(pkgName)) {
                return true;
            }
        }

        return false;
    }
}
