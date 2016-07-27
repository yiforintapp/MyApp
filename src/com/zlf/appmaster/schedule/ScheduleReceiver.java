package com.zlf.appmaster.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.utils.LeoLog;

public class ScheduleReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.leo.appmaster.action.SCHEDULE";

    private static final String TAG = "ScheduleReceiverJob";

    public ScheduleReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        LeoLog.i(TAG, "action: " + action);

        if (ACTION.equals(action)) {
            String clazz = intent.getStringExtra(com.zlf.appmaster.schedule.FetchScheduleJob.KEY_JOB);
            execWork(clazz);
        }
    }

    private void execWork(String clazzStr) {
        if (TextUtils.isEmpty(clazzStr)) return;

        try {
            Class<?> clazz = Class.forName(clazzStr);
            Object object = clazz.newInstance();

            if (object instanceof com.zlf.appmaster.schedule.ScheduleJob) {
                final com.zlf.appmaster.schedule.ScheduleJob job = (com.zlf.appmaster.schedule.ScheduleJob) object;
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        job.work();
                    }
                });
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
