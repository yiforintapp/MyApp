
package com.leo.appmaster.applocker.service;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.RemoteViews;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.TaskChangeHandler;
import com.leo.appmaster.cleanmemory.HomeBoostActivity;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.ui.Traffic;
import com.leo.appmaster.ui.TrafficInfoPackage;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

//import android.app.ActivityManager.AppTask;

@SuppressLint("NewApi")
public class TaskDetectService extends Service {
    private static final String TAG = "TaskDetectService";
    protected static final boolean DBG = false;
    public static final String EXTRA_STARTUP_FROM = "start_from";

    public static final String PRETEND_PACKAGE = "pretent_pkg";

    public static final String SYSTEMUI_PKG = "com.android.systemui";
    private static final String ES_UNINSTALL_ACTIVITY = ".app.UninstallMonitorActivity";

    private static final String HTC_USAGE = "com.htc.usage";
    private static final String STATE_NORMAL = "normal";
    private static final String STATE_WIFI = "wifi";
    private static final String STATE_NO_NETWORK = "nonet";
    public static final int SHOW_NOTI_PRE_DAY = 24 * 60 * 60 * 1000;
    // public static final int SHOW_NOTI_PRE_DAY = 20000;
    public static final int MAX_MEMORY = 65;
    // public static final int MAX_MEMORY = 20;
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
    // sony 5.1.1及Android M以上系统使用
    private DetectThreadCompat mDetectThreadCompat;

    private TaskChangeHandler mLockHandler;
    // private TaskDetectBinder mBinder = new TaskDetectBinder();
    private AppMasterPreference sp_traffic;
    private FloatWindowTask mFloatWindowTask;
    private Handler mHandler;
    private ProcessCleaner mCleaner;

    private static TaskDetectService sService;
    private static Notification sNotification;
    private String language = "zh";
    
    public static boolean sDetectSpecial = false;

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

        mCleaner = ProcessCleaner.getInstance(getApplicationContext());

        sp_traffic = AppMasterPreference.getInstance(TaskDetectService.this);
        mScheduledExecutor = ThreadManager.getAsyncExecutor();
        flowDetecTask = new FlowTask();
        // mflowDatectFuture =
        // mScheduledExecutor.scheduleWithFixedDelay(flowDetecTask, 0, 120000,
        // TimeUnit.MILLISECONDS);
        mflowDatectFuture = mScheduledExecutor.scheduleWithFixedDelay(flowDetecTask, 0, 60000,
                TimeUnit.MILLISECONDS);
        mHandler = new Handler();
        sService = this;
        startForeground(1, getNotification(getApplicationContext()));
        startPhantomService();
        checkFloatWindow();
        mScheduledExecutor.scheduleWithFixedDelay(flowDetecTask, 0, 120000,
                TimeUnit.MILLISECONDS);
        // sendQuickPermissionOpenNotification(getApplicationContext());
        language = AppwallHttpUtil.getLanguage();
        super.onCreate();
    }

    private void startPhantomService() {
        startService(new Intent(this, PhantomService.class));
    }

    public void callPretendAppLaunch() {
        mLockHandler.handleAppLaunch(PRETEND_PACKAGE, PRETEND_PACKAGE, PRETEND_PACKAGE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mServiceStarted) {
            startDetect();
        }
        // // As native process is not work for android 5.0 and above, use
        // AlarmManager instead.
        // triggerForLollipop();
        return START_STICKY;
    }

    // private void triggerForLollipop() {
    // if(PhoneInfo.getAndroidVersion() > 19) {
    // Context context = getApplicationContext();
    // Intent localIntent = new Intent(context, getClass());
    // localIntent.setPackage(getPackageName());
    // PendingIntent pi = PendingIntent.getService(context, 1, localIntent,
    // PendingIntent.FLAG_ONE_SHOT);
    // ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.ELAPSED_REALTIME,
    // 5000 + SystemClock.elapsedRealtime(), pi);
    // }
    // }

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
        if (mDetectThreadCompat != null) {
            mDetectThreadCompat.interrupt();
            mDetectThreadCompat = null;
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
        if (Build.VERSION.SDK_INT < 21 || isGetRunningProcessAvailable()) {// Android
                                                                            // 5.1.1及以下
            // for android 5.0, set period to 200, AM-1255
            int period = Build.VERSION.SDK_INT > 19 ? 200 : 100;
            mDetectTask = new DetectTask();
            mDetectFuture = mScheduledExecutor.scheduleWithFixedDelay(mDetectTask, 0, period,
                    TimeUnit.MILLISECONDS);
        } else {
            sDetectSpecial = true;
            mDetectThreadCompat = new DetectThreadCompat(mLockHandler);
            mDetectThreadCompat.start();
        }
    }

    /**
     * getRunningAppProcesses是否可用
     * 
     * @return
     */
    private boolean isGetRunningProcessAvailable() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();

        if (infos != null && infos.size() > 5)
            return true;

        return false;
    }

    public void checkFloatWindow() {

        if (AppMasterPreference.getInstance(this).getSwitchOpenQuickGesture()) {
            startFloatWindowTask();
            initFloatWindowData();
        } else {
            LeoLog.d("FloatWindowTask", "没有启动PG内快捷手势停止使用");
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
        FloatWindowHelper.initSlidingArea(pre);
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).resetSlidAreaSize();
        // 初始化未读短信是否已经红点提示过
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isMessageReadRedTip = pre
                .getMessageIsRedTip();
        // 初始化未读通话记录是否已经红点提示过
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isCallLogRead = pre
                .getCallLogIsRedTip();
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

            // 2min check memory is over 80%
            checkMemory();

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

    public void checkMemory() {
        if (mCleaner != null) {
            long mLastUsedMem = mCleaner.getUsedMem();
            long mTotalMem = mCleaner.getTotalMem();
            int mProgress = (int) (mLastUsedMem * 100 / mTotalMem);
            LeoLog.d("testServiceData", mLastUsedMem + "/" + mTotalMem + "--" + mProgress);

            long lastTime = sp_traffic.getLastShowNotifyTime();
            long nowTime = System.currentTimeMillis();
            if (mProgress > MAX_MEMORY && (nowTime - lastTime > SHOW_NOTI_PRE_DAY)) {// 24hours
                shwoNotify(mProgress);
                sp_traffic.setLastShowNotifyTime(nowTime);
            }
        }
    }

    private void shwoNotify(int mProgress) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int notifyId = 101;
        RemoteViews view_custom;
        if (!BuildProperties.checkIsHuaWeiEmotion31()) {
            // 先设定RemoteViews
            view_custom = new RemoteViews(getPackageName(), R.layout.clean_mem_notify);
        } else {
            // 先设定RemoteViews
            view_custom = new RemoteViews(getPackageName(), R.layout.clean_mem_notify_huawei);
        }
        // 设置对应IMAGEVIEW的ID的资源图片
        view_custom.setImageViewResource(R.id.appwallIV, R.drawable.boosticon);
        if ("zh".equalsIgnoreCase(language)) {
            view_custom.setTextViewText(R.id.app_precent, mProgress + "%");
        } else {
            view_custom.setTextViewText(R.id.app_precent, " " + mProgress + "%");
        }
        // view_custom.setTextViewText(R.id.app_precent,
        // getApplicationContext().getString(R.string.clean_mem_notify_big_right));
        view_custom.setTextViewText(R.id.appwallDescTV,
                getApplicationContext().getString(R.string.clean_mem_notify_small));
        view_custom.setTextViewText(R.id.app_precent, " " + mProgress + "%");
        NotificationCompat.Builder mBuilder = new Builder(this);
        mBuilder.setContent(view_custom)
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker(getApplicationContext().getString(R.string.clean_mem_notify_big))
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(false)// 不是正在进行的 true为正在进行 效果和.flag一样
                .setSmallIcon(R.drawable.statusbaricon)
                .setAutoCancel(true);

        // Intent realIntent = new Intent(this, HomeBoostActivity.class);
        // realIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Intent clickIntent = new Intent(this, showTrafficAlof.class);
        // clickIntent.putExtra("realIntent", realIntent);
        // clickIntent.setAction("com.leo.appmaster.boost.notification");
        // PendingIntent pi = PendingIntent.getBroadcast(this, 0, clickIntent,
        // PendingIntent.FLAG_UPDATE_CURRENT);
        // mBuilder.setContentIntent(pi);

        Intent intent = new Intent(this,
                HomeBoostActivity.class);
        intent.putExtra("for_sdk", "for_sdk");
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        // mNotificationManager.notify(notifyId, mBuilder.build());
        Notification notify = mBuilder.build();
        notify.contentView = view_custom;
        mNotificationManager.notify(notifyId, notify);
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
            String baseActivity = null;
            if (Build.VERSION.SDK_INT > 19) { // Android L and above
                List<RunningAppProcessInfo> list = mActivityManager.getRunningAppProcesses();
                boolean foundHuaweiLauncher = false;
                for (RunningAppProcessInfo pi : list) {
                    // Foreground or Visible
                    if ((pi.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND || pi.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE)
                            // Filter provider and service
                            && pi.importanceReasonCode == RunningAppProcessInfo.REASON_UNKNOWN
                            // RunningAppProcessInfo.FLAG_HAS_ACTIVITIES --> 0x4
                            // Must have activities
                            && (0x4 & pi.flags) > 0
                            // one activity is on the top
                            && pi.processState == ActivityManager.PROCESS_STATE_TOP) {
                        if (pi.processName.equals("com.huawei.android.launcher")) {
                            foundHuaweiLauncher = true;
                        }
                        if (foundHuaweiLauncher
                                && BuildProperties.isHuaWeiP8Model()
                                && pi.processName.equals("com.tencent.mm")) {
                            // FIXME: 2015/9/22 AM-2336 华为P8打开最近历史，莫名把微信拉起来，做特殊处理
                            break;
                        }
                        String pkgList[] = pi.pkgList;
                        if (pkgList != null && pkgList.length > 0) {
                            int index = 0;
                            pkgName = pkgList[index];
                            if (SYSTEMUI_PKG.equals(pkgName)) {
                                continue;
                            }
                            if (HTC_USAGE.equals(pkgName)) {
                                // FIXME: 2015/9/15 AM-2330 htc机型，com.htc.usage会加载到com.android.settings进程中
                                if (pkgList.length > 1) {
                                    pkgName = pkgList[++index];
                                } else {
                                    continue;
                                }
                            }
                            activityName = pkgList[index];
                            if (pkgName.equals(getPackageName())) {
                                activityName = TaskChangeHandler.LOCKSCREENNAME;
                                try {
                                    List<RunningTaskInfo> tasks = mActivityManager.getRunningTasks(1);
                                    if (tasks != null && tasks.size() > 0) {
                                        RunningTaskInfo topTaskInfo = tasks.get(0);
                                        String topPkg = topTaskInfo.topActivity.getPackageName();
                                        // 上面获取前台进程有误，获取到非前台App，但获取到的栈是正常的，就会出现，包名和activity名字不属于同一个包得尴尬情况 
                                        if (topPkg != null && !topPkg.equals(pkgName)) return;
                                        
                                        if (topTaskInfo.baseActivity != null) {
                                            baseActivity = topTaskInfo.baseActivity.getShortClassName();
                                        }
                                        if (topTaskInfo.topActivity != null) {
                                            activityName = topTaskInfo.topActivity.getShortClassName();
                                        }
                                    }
                                } catch (Exception e) {
                                    LeoLog.e(TAG, "get top activity and base activity ex.");
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
                    if (topTaskInfo.baseActivity != null) {
                        baseActivity = topTaskInfo.baseActivity.getShortClassName();
                    }
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
                mLockHandler.handleAppLaunch(pkgName, activityName, baseActivity);
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
                    if (DBG) {
                        LeoLog.i(TAG, "快捷手势在运行");
                    }
                    int screenStatus = Utilities.isScreenType(getApplicationContext());
                    if (screenStatus != -1) {
                        // int value =
                        // AppMasterPreference.getInstance(getApplicationContext())
                        // .getQuickGestureDialogSeekBarValue();
                        int value = QuickGestureManager.getInstance(getApplicationContext()).mSlidAreaSize;
                        if (!FloatWindowHelper.mGestureShowing
                                && AppMasterPreference.getInstance(getApplicationContext())
                                        .getFristSlidingTip()) {
                            boolean isHomeFlag = false;
                            AppMasterPreference amp = AppMasterPreference
                                    .getInstance(getApplicationContext());
                            if (amp.getNeedShowWhiteDotSlideTip()) {
                                isHomeFlag = Utilities.isHome(getApplicationContext());
                                if (isHomeFlag) {
                                    FloatWindowHelper
                                            .checkShowWhiteDotLuminescence(TaskDetectService.this
                                                    .getApplicationContext());
                                }
                            }
                            // set background color
                            if (FloatWindowHelper.mEditQuickAreaFlag) {
                                FloatWindowHelper
                                        .updateFloatWindowBackgroudColor(getApplicationContext(),
                                                FloatWindowHelper.mEditQuickAreaFlag);
                            }
                            boolean isJustHome = QuickGestureManager
                                    .getInstance(AppMasterApplication.getInstance()).isJustHome;
                            boolean isAppsAndHome = QuickGestureManager
                                    .getInstance(AppMasterApplication.getInstance()).isAppsAndHome;
                            // when the dialog is showing ,the window view not
                            // create
                            boolean isDialogingShowing = QuickGestureManager
                                    .getInstance(AppMasterApplication.getInstance()).isDialogShowing;

                            // phtc

                            if (isAppsAndHome) {
                                boolean isFilterApp = checkForegroundRuningFilterApp(mActivityManager);

                                if ((!isFilterApp
                                        || FloatWindowHelper.mEditQuickAreaFlag)
                                        && !isDialogingShowing
                                        && sp_traffic.getIsOpenFloatWindows()) {
                                    FloatWindowHelper.createFloatWindow(getApplicationContext(),
                                            value);

                                    // TODO
                                    Log.e("iscreated", "appsandhome");

                                } else {
                                    FloatWindowHelper.removeAllFloatWindow(getApplicationContext());
                                }

                                /** about white float view **/
                                if (sp_traffic.getSwitchOpenStrengthenMode()) {
                                    if (!isFilterApp && !FloatWindowHelper.mEditQuickAreaFlag
                                            && !isDialogingShowing) {
                                        FloatWindowHelper
                                                .showWhiteFloatView(TaskDetectService.this);
                                    } else {
                                        FloatWindowHelper
                                                .hideWhiteFloatView(TaskDetectService.this);
                                    }
                                }
                            } else if (isJustHome) {
                                if (!isHomeFlag)
                                    isHomeFlag = Utilities.isHome(getApplicationContext());
                                if ((isHomeFlag || FloatWindowHelper.mEditQuickAreaFlag)
                                        && !isDialogingShowing
                                        && sp_traffic.getIsOpenFloatWindows()) {
                                    FloatWindowHelper.createFloatWindow(getApplicationContext(),
                                            value);
                                    Log.e("iscreated", "appsandhome");
                                } else {
                                    FloatWindowHelper.removeAllFloatWindow(getApplicationContext());
                                }
                                /** about white float view **/
                                if (sp_traffic.getSwitchOpenStrengthenMode()) {
                                    if (isHomeFlag && !FloatWindowHelper.mEditQuickAreaFlag
                                            && !isDialogingShowing) {
                                        FloatWindowHelper
                                                .showWhiteFloatView(TaskDetectService.this);
                                    } else {
                                        FloatWindowHelper
                                                .hideWhiteFloatView(TaskDetectService.this);
                                    }
                                }
                            }
                        } else {
                            FloatWindowHelper.removeAllFloatWindow(getApplicationContext());
                            FloatWindowHelper.hideWhiteFloatView(TaskDetectService.this);
                        }
                    } else {
                        FloatWindowHelper.removeAllFloatWindow(getApplicationContext());
                        FloatWindowHelper.hideWhiteFloatView(TaskDetectService.this);
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
}
