package com.leo.appmaster.applocker.model;

import android.annotation.SuppressLint;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.Constants;
import com.leo.appmaster.utils.LeoLog;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 使用UsageStatsManager获取最近历史
 * Created by Jasper on 2015/9/23.
 */
@SuppressLint("NewApi")
public class ProcessDetectorUsageStats extends ProcessDetector {
    private static final String TAG = "PDectorUsageStats";

    private static final boolean DBG = false;

    private static final int WAIT_TIMEOUT = 200;

    private UsageStatsManager mStatsManager;

    public ProcessDetectorUsageStats() {
        AppMasterApplication context = AppMasterApplication.getInstance();
        mStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Override
    public boolean checkAvailable() {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -1);
        long startTime = calendar.getTimeInMillis();

        List<UsageStats> list = mStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                startTime, endTime);
        return list != null && list.size() > 0;
    }

    @Override
    public ProcessAdj getForegroundProcess() {
//        Calendar calendar = Calendar.getInstance();
//        long endTime = calendar.getTimeInMillis();
//        calendar.add(Calendar.YEAR, -2);
//        long startTime = calendar.getTimeInMillis();
//
//        List<UsageStats> stats = mStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY,
//                startTime, endTime);
        long currentTs = System.currentTimeMillis();
        long startTs = currentTs - Constants.TIME_ONE_DAY;
        startTs = startTs < 0 ? 0 : startTs;
        List<UsageStats> stats = mStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTs, currentTs);

        ProcessAdj processAdj = null;
        if (stats != null) {
            SortedMap<Long, UsageStats> runningTask = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : stats) {
//                if (usageStats.mLastEvent == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                runningTask.put(usageStats.getLastTimeUsed(), usageStats);
//                }
            }
            logTasks(runningTask);
            if (runningTask.isEmpty()) {
                if (DBG) {
                    Log.d(TAG, "there is no app found.");
                }
                LeoLog.i(TAG, "there is no app found.");
            } else {
                String pkg = runningTask.get(runningTask.lastKey()).getPackageName();
                if (DBG) {
                    Log.i(TAG, "pkg: " + pkg);
                }
                LeoLog.i(TAG, "pkg: " + pkg);
                processAdj = new ProcessAdj();
                processAdj.pkg = pkg;
            }
        }

        return processAdj;
    }

    @Override
    public int getTimeoutMs(ProcessAdj proAdj) {
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
}
