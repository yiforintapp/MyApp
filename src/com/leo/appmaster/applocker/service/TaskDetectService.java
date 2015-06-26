
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
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.TaskChangeHandler;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.ui.QuickGestureActivity;
import com.leo.appmaster.ui.Traffic;
import com.leo.appmaster.ui.TrafficInfoPackage;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.Utilities;

//import android.app.ActivityManager.AppTask;

@SuppressLint("NewApi")
public class TaskDetectService extends Service {

    public static final String EXTRA_STARTUP_FROM = "start_from";

    public static final String SYSTEMUI_PKG = "com.android.systemui";
    private static final String ES_UNINSTALL_ACTIVITY = ".app.UninstallMonitorActivity";
    private static final String STATE_NORMAL = "normal";
    private static final String STATE_WIFI = "wifi";
    private static final String STATE_NO_NETWORK = "nonet";

    private boolean mServiceStarted;
    public float[] tra = {
            0, 0, 0
    };

    private ScheduledFuture<?> mflowDatectFuture;
    private ScheduledFuture<?> mFloatWindowFuture;
    private TimerTask flowDetecTask;

    private ScheduledExecutorService mScheduledExecutor;
    private ScheduledFuture<?> mDetectFuture;
    private TimerTask mDetectTask;

    private TaskChangeHandler mLockHandler;
    // private TaskDetectBinder mBinder = new TaskDetectBinder();
    private AppMasterPreference sp_traffic;
    private FloatWindowTask mFloatWindowTask;
    private Handler mHandler;

    private static TaskDetectService sService;
    private static Notification sNotification;

    public class TaskDetectBinder extends Binder {
        public TaskDetectService getService() {
            return TaskDetectService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mLockHandler = new TaskChangeHandler(getApplicationContext());

        sp_traffic = AppMasterPreference.getInstance(TaskDetectService.this);
        mScheduledExecutor = Executors.newScheduledThreadPool(2);
        flowDetecTask = new FlowTask();
        mflowDatectFuture = mScheduledExecutor.scheduleWithFixedDelay(flowDetecTask, 0, 120000,
                TimeUnit.MILLISECONDS);
        mHandler = new Handler();
        sService = this;
        startForeground(1, getNotification(getApplicationContext()));
        startPhantomService();
        checkFloatWindow();
        mScheduledExecutor.scheduleWithFixedDelay(flowDetecTask, 0, 120000,
                TimeUnit.MILLISECONDS);
        sendQuickPermissionOpenNotification(getApplicationContext());
        super.onCreate();
    }

    private void startPhantomService() {
        startService(new Intent(this, PhantomService.class));
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

    public void checkFloatWindow() {

        if (AppMasterPreference.getInstance(this).getSwitchOpenQuickGesture()) {
            startFloatWindowTask();
            initFloatWindowData();
        } else {
            stopFloatWindowTask();
        }
    }

    private void initFloatWindowData() {
        // catch perference value
        AppMasterPreference pre = AppMasterPreference.getInstance(getApplicationContext());
        // just home
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isJustHome = pre
                .getSlideTimeJustHome();
        // home and apps
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isAppsAndHome = pre
                .getSlideTimeAllAppAndHome();
        // left bottom
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftBottom = pre
                .getDialogRadioLeftBottom();
        // right bottom
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightBottom = pre
                .getDialogRadioRightBottom();
        // left center
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftCenter = pre
                .getDialogRadioLeftCenter();
        // right center
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightCenter = pre
                .getDialogRadioRightCenter();
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).resetSlidAreaSize();
        //初始化未读短信是否已经红点提示过
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isMessageRead = AppMasterPreference
                .getInstance(getApplicationContext()).getMessageIsRedTip();
    }

    private void startFloatWindowTask() {
        stopFloatWindowTask();
        mFloatWindowTask = new FloatWindowTask();
        mFloatWindowFuture = mScheduledExecutor.scheduleWithFixedDelay(mFloatWindowTask, 0, 1500,
                TimeUnit.MILLISECONDS);
    }

    private void stopFloatWindowTask() {
        if (mFloatWindowFuture != null) {
            mFloatWindowFuture.cancel(false);
            mFloatWindowFuture = null;
            mFloatWindowTask = null;
        }
    }

    public void stopFloatWindow() {
        stopFloatWindowTask();
    }

    @Override
    public void onDestroy() {
        stopDetect();
        stopFlowTask();
        stopFloatWindow();
        sendBroadcast(new Intent("com.leo.appmaster.restart"));
        sService = null;
        super.onDestroy();
    }

    private class FlowTask extends TimerTask {
        @Override
        public void run() {

            int mVersion = PhoneInfo.getAndroidVersion();
            String network_state = whatState();

            if (!network_state.equals(STATE_NO_NETWORK)) {
                Traffic traffic = Traffic.getInstance(getApplicationContext());
                tra[0] = traffic.getAllgprs(mVersion, network_state)[2];
                new TrafficInfoPackage(getApplicationContext()).getRunningProcess(false);
            }

            if (network_state.equals(STATE_NORMAL)) {
                long TotalTraffic = sp_traffic.getTotalTraffic() * 1024;
                // 设置了流量套餐才去检测
                if (TotalTraffic > 0) {
                    TrafficNote(TotalTraffic);
                }
            }
        }
    }

    public void TrafficNote(long totalTraffic) {
        boolean isSwtich = sp_traffic.getFlowSetting();
        boolean haveNotice = sp_traffic.getAlotNotice();
        long MonthUsed = sp_traffic.getMonthGprsAll() / 1024;
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

    /*
     * FloatWindowTask
     */
    private class FloatWindowTask implements Runnable {
        ActivityManager mActivityManager;
        private Runnable mRunnable;

        public FloatWindowTask() {
            mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    int screenStatus = Utilities.isScreenType(getApplicationContext());
                    if (screenStatus != -1) {
                        // int value =
                        // AppMasterPreference.getInstance(getApplicationContext())
                        // .getQuickGestureDialogSeekBarValue();
                        int value = QuickGestureManager.getInstance(getApplicationContext()).mSlidAreaSize;
                        if (!FloatWindowHelper.mGestureShowing
                                && AppMasterPreference.getInstance(getApplicationContext())
                                        .getFristSlidingTip()) {
                            // set background color
                            if (FloatWindowHelper.mEditQuickAreaFlag) {
                                FloatWindowHelper
                                        .updateFloatWindowBackgroudColor(FloatWindowHelper.mEditQuickAreaFlag);
                            }
                            boolean isJustHome = QuickGestureManager
                                    .getInstance(AppMasterApplication.getInstance()).isJustHome;
                            boolean isAppsAndHome = QuickGestureManager
                                    .getInstance(AppMasterApplication.getInstance()).isAppsAndHome;
                            if (isAppsAndHome) {
                                if (!checkForegroundRuningFilterApp(mActivityManager)
                                        || FloatWindowHelper.mEditQuickAreaFlag) {
                                    FloatWindowHelper.createFloatWindow(getApplicationContext(),
                                            value);
                                } else {
                                    FloatWindowHelper.removeAllFloatWindow(getApplicationContext());
                                }
                            } else if (isJustHome) {
                                boolean isHomeFlag = Utilities.isHome(getApplicationContext());
                                if (isHomeFlag || FloatWindowHelper.mEditQuickAreaFlag) {
                                    FloatWindowHelper.createFloatWindow(getApplicationContext(),
                                            value);
                                } else {
                                    FloatWindowHelper.removeAllFloatWindow(getApplicationContext());
                                }
                            }
                        } else {
                            FloatWindowHelper.removeAllFloatWindow(getApplicationContext());
                        }
                    } else {
                        FloatWindowHelper.removeAllFloatWindow(getApplicationContext());
                    }
                }
            };
        }

        @Override
        public void run() {
            mHandler.post(mRunnable);
        }
    }

    // checkout current runing filter app
    private boolean checkForegroundRuningFilterApp(ActivityManager activityManager) {
        List<String> filterAppList = QuickGestureManager.getInstance(getApplicationContext())
                .getFreeDisturbAppName();
        if (filterAppList != null && filterAppList.size() > 0) {
            String pkgName = null;
            if (Build.VERSION.SDK_INT > 19) {
                List<RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
                for (RunningAppProcessInfo pi : list) {
                    if ((pi.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND || pi.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE)
                            && pi.importanceReasonCode == RunningAppProcessInfo.REASON_UNKNOWN
                            && (0x4 & pi.flags) > 0
                            && pi.processState == ActivityManager.PROCESS_STATE_TOP) {
                        String pkgList[] = pi.pkgList;
                        if (pkgList != null && pkgList.length > 0) {
                            pkgName = pkgList[0];
                            if (SYSTEMUI_PKG.equals(pkgName)) {
                                continue;
                            }
                            break;
                        }
                    }
                }
            } else {
                List<RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
                if (tasks != null && tasks.size() > 0) {
                    RunningTaskInfo topTaskInfo = tasks.get(0);
                    if (topTaskInfo.topActivity == null) {
                        return false;
                    }
                    pkgName = topTaskInfo.topActivity.getPackageName();
                    if (Utilities.isEmpty(pkgName)) {
                        List<RunningAppProcessInfo> list = activityManager
                                .getRunningAppProcesses();
                        for (RunningAppProcessInfo pi : list) {
                            if ((pi.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND || pi.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE)
                                    && pi.importanceReasonCode == RunningAppProcessInfo.REASON_UNKNOWN
                                    && (0x4 & pi.flags) > 0) {
                                String pkgList[] = pi.pkgList;
                                if (pkgList != null && pkgList.length > 0) {
                                    pkgName = pkgList[0];
                                    if (SYSTEMUI_PKG.equals(pkgName)) {
                                        continue;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (pkgName != null) {
                if (QuickGestureManager.getInstance(getApplicationContext())
                        .getFreeDisturbAppName()
                        .contains(pkgName)) {
                    // Log.d("get current running app", "pkgName:" + pkgName);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public static TaskDetectService getService() {
        return sService;
    }

    @SuppressWarnings("deprecation")
    public static synchronized Notification getNotification(Context context) {
        if (sNotification == null) {
            PendingIntent pi = PendingIntent.getActivity(context, 0,
                    new Intent(context, HomeActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            sNotification = new Notification();
            sNotification.icon = R.drawable.ic_launcher;
            sNotification.flags |= Notification.FLAG_ONGOING_EVENT;
            String title = context.getString(R.string.app_name);
            sNotification.setLatestEventInfo(context, title, title, pi);
        }
        return sNotification;
    }

    private void sendQuickPermissionOpenNotification(final Context context) {
        boolean flag = AppMasterPreference.getInstance(context)
                .getQuickPermissonOpenFirstNotificatioin();
        // if(!flag){
        // Log.e("######", "快捷开关权限是否已经发过通知：" + flag+"进去执行");
        // }else{
        // Log.e("######", "快捷开关权限是否已经发过通知：" + flag+"不去下面执行");
        // }
        if (!flag) {
            // boolean checkHuaWei = BuildProperties.isHuaWeiTipPhone(context);
            boolean checkMiui = BuildProperties.isMIUI();
            boolean checkFloatWindow = BuildProperties.isFloatWindowOpAllowed(context);
            // Log.e("#######", "是否为MIUI：" + checkMiui);
            // Log.e("######", "是否为HUAWEI：" + checkHuaWei);
            // Log.e("####", "悬浮窗权限是否打开：" + checkFloatWindow);
            if (checkMiui && checkFloatWindow) {
                if (AppMasterPreference.getInstance(context).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {

                    QuickGestureManager.getInstance(context)
                            .sendPermissionOpenNotification(context);

                }
            }
        }
    }
}
