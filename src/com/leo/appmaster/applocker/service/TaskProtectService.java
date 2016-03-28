package com.leo.appmaster.applocker.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.leo.appmaster.utils.LeoLog;

/**
 * Created by stone on 16/3/17.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class TaskProtectService extends JobService {

    private static final int JOB_ID = 20160317;
    private static final String TAG = TaskProtectService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        LeoLog.d(TAG, "onCreate "  + this.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LeoLog.d(TAG, "onDestroy "  + this.toString());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        LeoLog.d(TAG, "onStartCommand " + this.toString());
        return Service.START_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        LeoLog.d(TAG, "onStartJob " + this.toString());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public static void scheduleService(Context context) {
        context.startService(new Intent(context.getApplicationContext(), TaskProtectService.class));

        JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(context.getPackageName(), TaskProtectService.class.getName()));
        builder.setPersisted(true);     //设置开机启动
        builder.setPeriodic(3 * 1000);     //设置1分钟执行一次
        js.schedule(builder.build());
    }
}
