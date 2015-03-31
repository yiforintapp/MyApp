
package com.leo.appmaster.applocker.service;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.manager.TaskChangeHandler;
import com.leo.appmaster.ui.Traffic;
import com.leo.appmaster.ui.TrafficInfoPackage;
import com.leo.appmaster.utils.Utilities;

//import android.app.ActivityManager.AppTask;

@SuppressLint("NewApi")
public class TaskDetectService extends Service {

    public static final String EXTRA_STARTUP_FROM = "start_from";

    private static final String SYSTEMUI_PKG = "com.android.systemui";
    private static final String ES_UNINSTALL_ACTIVITY = ".app.UninstallMonitorActivity";
    private static final String STATE_NORMAL = "normal";
    private static final String STATE_WIFI = "wifi";
    private static final String STATE_NO_NETWORK = "nonet";

    private boolean mServiceStarted;
    public float[] tra = {
            0, 0, 0
    };

    private ScheduledFuture<?> mflowDatectFuture;;
    private TimerTask flowDetecTask;

    private ScheduledExecutorService mScheduledExecutor;
    private ScheduledFuture<?> mDetectFuture;
    private TimerTask mDetectTask;
    private boolean mIsFirstDetect;;
    // private Timer mTimer;

    private TaskChangeHandler mLockHandler;
    private TaskDetectBinder mBinder = new TaskDetectBinder();
    private AppMasterPreference sp_traffic;

    public class TaskDetectBinder extends Binder {
        public TaskDetectService getService() {
            return TaskDetectService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mLockHandler = new TaskChangeHandler(getApplicationContext());
        sp_traffic = AppMasterPreference.getInstance(TaskDetectService.this);
        mIsFirstDetect = true;
        mScheduledExecutor = Executors.newScheduledThreadPool(2);
        flowDetecTask = new FlowTask();
        mflowDatectFuture = mScheduledExecutor.scheduleWithFixedDelay(flowDetecTask, 0, 10000,
                TimeUnit.MILLISECONDS);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mServiceStarted) {
            startDetect();
        }
        return START_STICKY;
    }

    public String getLastRunningPackage() {
        return mLockHandler.getLastRunningPackage();
    }

    public String getLastRunningActivity() {
        return mLockHandler.getLastRunningActivity();
    }

    public void startDetect() {
        if (!mServiceStarted) {
            startDetectTask();
            mServiceStarted = true;
        }
    }

    public void stopDetect() {
        stopDetectTask();
        mServiceStarted = false;
    }

    private void stopDetectTask() {
        if (mDetectFuture != null) {
            mDetectFuture.cancel(false);
            mDetectFuture = null;
            mDetectTask = null;
        }
    }

    private void stopFlowTask() {
        if (mflowDatectFuture != null) {
            mflowDatectFuture.cancel(false);
            mflowDatectFuture = null;
            flowDetecTask = null;
        }
    }

    private void startDetectTask() {
        stopDetectTask();
        // for android 5.0, set period to 200, AM-1255
        int period = Build.VERSION.SDK_INT > 19 ? 200 : 100;
        mDetectTask = new DetectTask();
        mDetectFuture = mScheduledExecutor.scheduleWithFixedDelay(mDetectTask, 0, period,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDestroy() {
        stopDetect();
        stopFlowTask();
        sendBroadcast(new Intent("com.leo.appmaster.restart"));
        super.onDestroy();
    }

    private class FlowTask extends TimerTask {
        @Override
        public void run() {

            int mVersion = PhoneInfo.getAndroidVersion();
            String network_state = whatState();

            Traffic traffic = Traffic.getInstance(getApplicationContext());
            tra[0] = traffic.getAllgprs(mVersion, network_state)[2];
            new TrafficInfoPackage(getApplicationContext()).getRunningProcess();

            if (network_state.equals(STATE_NORMAL)) {
                long TotalTraffic = sp_traffic.getTotalTraffic() * 1024 * 1024;
                // 设置了流量套餐才去检测
                if (TotalTraffic > 0) {
                    // 流量预警
                    TrafficNote(TotalTraffic);
                }
            }
        }
    }

    public void TrafficNote(long totalTraffic) {
        boolean isSwtich = sp_traffic.getFlowSetting();
        boolean haveNotice = sp_traffic.getAlotNotice();
        long MonthUsed = sp_traffic.getMonthGprsAll();
        long MonthItSelfTraffic = sp_traffic.getItselfMonthTraffic();
        // long ToTalUsedTraffi = MonthUsed + MonthItSelfTraffic;
        // int bili = (int) (ToTalUsedTraffi * 100 / totalTraffic);
        int bili = 0;
        if (MonthItSelfTraffic > 0) {
            bili = (int) (MonthItSelfTraffic * 100 / totalTraffic);
        } else {
            bili = (int) (MonthUsed * 100 / totalTraffic);
        }

        int TrafficSeekBar = sp_traffic.getFlowSettingBar();

        if (isSwtich && !haveNotice) {
            if (bili > TrafficSeekBar) {
                // LeoLog.d("testnetwork", "服务---超过设定流量！");
                Intent shortcut = new Intent();
                shortcut.setAction("com.leo.appmaster.traffic.alot");
                sendBroadcast(shortcut);
                sp_traffic.setAlotNotice(true);
            }
        }

        boolean mFinishNotice = sp_traffic.getFinishNotice();
        if (isSwtich && !mFinishNotice) {
            if (MonthItSelfTraffic > 0) {
                if (totalTraffic < MonthItSelfTraffic) {
                    // 流量用光了
                    Intent longcut = new Intent();
                    longcut.setAction("com.leo.appmaster.traffic.finish");
                    sendBroadcast(longcut);
                    sp_traffic.setFinishNotice(true);
                }
            } else {
                if (totalTraffic < MonthUsed) {
                    // 流量用光了
                    Intent longcut = new Intent();
                    longcut.setAction("com.leo.appmaster.traffic.finish");
                    sendBroadcast(longcut);
                    sp_traffic.setFinishNotice(true);
                }
            }
        }
    }

    public String whatState() {
        State wifiState = null;
        State mobileState = null;
        ConnectivityManager cm = (ConnectivityManager) TaskDetectService.this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        String mWhatState = "";
        if (wifiState != null && mobileState != null
                && State.CONNECTED != wifiState
                && State.CONNECTED == mobileState) {
            // 手机网络连接成功
            mWhatState = STATE_NORMAL;
        } else if (wifiState != null && mobileState != null
                && State.CONNECTED != wifiState
                && State.CONNECTED != mobileState) {
            // 手机没有任何的网络
            mWhatState = STATE_NO_NETWORK;
        } else if (wifiState != null && State.CONNECTED == wifiState) {
            // wifi
            mWhatState = STATE_WIFI;
        }
        return mWhatState;
    }

    private class DetectTask extends TimerTask {
        ActivityManager mActivityManager;

        public DetectTask() {
            mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        }

        @Override
        public void run() {
            String pkgName = null;
            String activityName = null;
            if (Build.VERSION.SDK_INT > 19) { // Android L and above
                List<RunningAppProcessInfo> list = mActivityManager.getRunningAppProcesses();
                for (RunningAppProcessInfo pi : list) {
                    if ((pi.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND || pi.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE)
                            /*
                             * Foreground or Visible
                             */
                            && pi.importanceReasonCode == RunningAppProcessInfo.REASON_UNKNOWN
                            /*
                             * Filter provider and service
                             */
                            && (0x4 & pi.flags) > 0
                            && pi.processState == ActivityManager.PROCESS_STATE_TOP) {
                        /*
                         * Must have activities and one activity is on the top
                         */
                        String pkgList[] = pi.pkgList;
                        if (pkgList != null && pkgList.length > 0) {
                            pkgName = pkgList[0];
                            if (SYSTEMUI_PKG.equals(pkgName)) {
                                continue;
                            }
                            activityName = pkgList[0];
                            if (pkgName.equals(getPackageName())) {
                                activityName = TaskChangeHandler.LOCKSCREENNAME;
                                try {
                                    List<RunningTaskInfo> tasks = mActivityManager
                                            .getRunningTasks(1);
                                    if (tasks != null && tasks.size() > 0) {
                                        RunningTaskInfo topTaskInfo = tasks.get(0);
                                        if (topTaskInfo.topActivity != null) {
                                            activityName = topTaskInfo.topActivity
                                                    .getShortClassName();
                                        }
                                    }
                                } catch (Exception e) {

                                }
                            } else {
                                activityName = pi.processName;
                            }
                            break;
                        }
                    }
                }
            } else {
                List<RunningTaskInfo> tasks = mActivityManager.getRunningTasks(1);
                if (tasks != null && tasks.size() > 0) {
                    RunningTaskInfo topTaskInfo = tasks.get(0);
                    if (topTaskInfo.topActivity == null) {
                        return;
                    }
                    pkgName = topTaskInfo.topActivity.getPackageName();
                    activityName = topTaskInfo.topActivity.getShortClassName();
                    // For aliyun os (may be others), the component of
                    // topActivity is hidden, make a backup here.
                    if (Utilities.isEmpty(pkgName)) {
                        List<RunningAppProcessInfo> list = mActivityManager
                                .getRunningAppProcesses();
                        for (RunningAppProcessInfo pi : list) {
                            if ((pi.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND || pi.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE)
                                    /*
                                     * Foreground or Visible
                                     */
                                    && pi.importanceReasonCode == RunningAppProcessInfo.REASON_UNKNOWN
                                    /*
                                     * Filter provider and service
                                     */
                                    && (0x4 & pi.flags) > 0) {
                                /*
                                 * Must have activities and one activity is on
                                 * the top
                                 */
                                String pkgList[] = pi.pkgList;
                                if (pkgList != null && pkgList.length > 0) {
                                    pkgName = pkgList[0];
                                    if (SYSTEMUI_PKG.equals(pkgName)) {
                                        continue;
                                    }
                                    if (Utilities.isEmpty(activityName)) {
                                        activityName = pkgName;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // AM-940, filter
            // com.estrongs.android.pop.app.UninstallMonitorActivity
            if (ES_UNINSTALL_ACTIVITY.equals(activityName)) {
                return;
            }

            if (mLockHandler != null && pkgName != null && activityName != null) {
                mLockHandler.handleAppLaunch(pkgName, activityName);
            }
        }
    }
}
