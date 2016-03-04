package com.leo.appmaster.mgr.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.applocker.model.ProcessAdj;
import com.leo.appmaster.applocker.model.ProcessDetectorUsageStats;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.battery.BatteryNotifyHelper;
import com.leo.appmaster.battery.BatteryShowViewActivity;
import com.leo.appmaster.battery.RemainTimeHelper;
import com.leo.appmaster.battery.RemainingTimeEstimator;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.db.PrefTableHelper;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.engine.BatteryInfoProvider;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BatteryViewEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.AppUtil;

/**
 * Created by stone on 16/1/13.
 */
public class BatteryManagerImpl extends BatteryManager {
    private PreferenceTable mPt = PreferenceTable.getInstance();
    private static final String TAG = MgrContext.MGR_BATTERY;


    private static final int START_ACTIVITY = 1;

    // 两分钟内不能连续两次清理应用
    private static final int KILL_INTERVAL = 2 * 60 * 1000;

    /* 3.3.1 */
    private static final int MAX_BUBBLE_SHOW_TIME = 3;

    private AppMasterPreference mSp;
    private boolean mPageOnForeground = false;
    private long mLastKillTime = 0;

    private RemainTimeHelper mRemainTimeHelper;
    private BatteryNotifyHelper mNotifyHelper;

    /* 3.3.2 剩余可用时间计算 */
    private RemainingTimeEstimator mTimeEstimator;

    private LockManager mLockManager;
    private WeakReference<BatteryStateListener> mListenerRef;

    private BatteryState mPreviousState = new BatteryState();


    public BatteryManagerImpl() {
        mTimeEstimator = new RemainingTimeEstimator(mContext);
        mNotifyHelper = BatteryNotifyHelper.getInstance(mContext, this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        mContext.registerReceiver(mReceiver, filter);

        mSp = AppMasterPreference.getInstance(mContext);
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
    }

    @Override
    public List<BatteryComsuption> getBatteryDrainApps() {
        BatteryInfoProvider ip = new BatteryInfoProvider(mContext);
        ip.setMinPercentOfTotal(0.01);
        List<BatteryComsuption> list = ip.getBatteryStats();
        for (BatteryComsuption bc : list) {
            LeoLog.d(TAG, "BatteryComsuption -> " + bc.getDefaultPackageName());
        }

        List<ProcessAdj> processAdjList = ip.getProcessWithPS();
        ArrayList<String> pkgs = new ArrayList<String>();
        for (ProcessAdj processAdj : processAdjList) {
            pkgs.add(processAdj.pkg);
            LeoLog.d(TAG, "processAdj -> " + processAdj.pkg);
        }

        Iterator<BatteryComsuption> batteryComsuptionIterator = list.iterator();
        while (batteryComsuptionIterator.hasNext()) {
            BatteryComsuption bc = batteryComsuptionIterator.next();
            if (!pkgs.contains(bc.getDefaultPackageName())) {
                batteryComsuptionIterator.remove();
            }
        }
        return list;
    }

    @Override
    public void killBatteryDrainApps() {
        ProcessCleaner.getInstance(mContext).cleanAllProcess(mContext);
        mLastKillTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void setAppThreshold(int threshold) {
        mSp.setPowerConsumeAppThreshold(threshold);
    }

    @Override
    public int getAppThreshold() {
        return mSp.getPowerConsumeAppThreshold();
    }

    @Override
    public void updateBatteryPageState(boolean isForeground) {
        mPageOnForeground = isForeground;
    }

    @Override
    public void onDestory() {
        super.onDestory();
        mContext.unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        boolean mFirstTouch = true;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPreviousState == null) {
                // AM-3789
                mPreviousState = new BatteryState(intent);
            }

            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                BatteryState bs = new BatteryState(intent);
                if (mFirstTouch) {
                    mFirstTouch = false;
                    if (bs.plugged != UNPLUGGED) {
                        showSaverNotification(bs.level);
                    }
                }
                handleBatteryChange(bs);
                LeoLog.d(TAG, "newState: " + bs.toString());
            } else {
                LeoLog.d(TAG, "action: " + action +
                        "; mPreviousState: " + mPreviousState.toString());

                /* AM-3891: 使用static变量的方式控制是否弹出屏保，当activity在后台的时候判断会出错。
                 * 调整策略：当手机灭屏时通知已在的屏保finish，亮屏时再拉起屏保。 */
                if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                    LeoEventBus.getDefaultBus().post(new BatteryViewEvent("finish_activity"));
                }

                if (action.equals(Intent.ACTION_SCREEN_ON) &&
                        mPreviousState.plugged != UNPLUGGED) {
                    LeoLog.d(TAG, "show screen saver on ACTION_SCREEN_ON");
                    handlePluginEvent(mPreviousState, false);
                }

                /*if (AppUtil.hasOtherScreenSaverInstalled(mContext)) {
                    // 如果安装了其他竞品的屏保，我们在亮屏的时候再拉起屏保
                    if (action.equals(Intent.ACTION_SCREEN_ON) &&
                            mPreviousState.plugged != UNPLUGGED) {
                        LeoLog.d(TAG, "show screen saver on ACTION_SCREEN_ON");
                        handlePluginEvent(mPreviousState, false);
                    }
                } else {
                    // 如果没有安装其他竞品的屏保，我们在灭屏的时候就可以拉起屏保
                    if (action.equals(Intent.ACTION_SCREEN_OFF) &&
                            mPreviousState.plugged != UNPLUGGED) {
                        LeoLog.d(TAG, "show screen saver on ACTION_SCREEN_OFF");
                        handlePluginEvent(mPreviousState, true);
                    }
                }*/

            }
        }
    };

    /* 充电时间计算相关 */
    private void handleBatteryChange(BatteryState newState) {
        if (newState.plugged != UNPLUGGED && mPreviousState.plugged == UNPLUGGED) {
            /* 连接充电器事件 */
            if (isInApp()) {
                Toast.makeText(mContext, "in APP", Toast.LENGTH_SHORT).show();
                if (PrefTableHelper.showInsideApp()) {
                    handlePluginEvent(newState, false);
                } else {

                    if (AppUtil.isScreenLocked(mContext)) {
                        LeoLog.d("testforwhile", "done done done");
                    } else {
                        LeoLog.d("testforwhile", "fuck fuck fuck");
                    }

                    if ((BatteryShowViewActivity.isActivityAlive && mListenerRef != null)) {
                        int remainTime = getRemainTimeHelper(newState).getEstimatedTime(DEFAULT_LEVEL,
                                newState.level, 0);
                        LeoLog.d("testforwhile", "remainTime remainTime");
                        int[] remainTimeArr = getTimeArr(newState);
                        BatteryStateListener listener = mListenerRef.get();
                        if (listener != null) {
                            listener.onStateChange(EventType.SHOW_TYPE_IN, newState, remainTime, remainTimeArr);
                        }
                    } else if (AppUtil.isScreenLocked(mContext)) {
                        handlePluginEvent(newState, false);
                    }
                }
            } else {
                Toast.makeText(mContext, "in LAUNCHER", Toast.LENGTH_SHORT).show();
                handlePluginEvent(newState, false);
            }
            /* 如果屏保开关是打开的，则通知栏一定会出现 */
            showSaverNotification(newState.level);
        } else if (mPreviousState.plugged != UNPLUGGED && newState.plugged == UNPLUGGED) {
            handleUnplugEvent(newState);
            mNotifyHelper.dismissScreenSaverNotification();
        } else if (newState.plugged != UNPLUGGED && newState.level != mPreviousState.level) {
            handleChargingEvent(newState);
            mNotifyHelper.updateNotificationLevel(newState.level);
        } else if (newState.level != mPreviousState.level) {
            handleConsumingState(newState);
        }

        mPreviousState = newState;
    }

    /***
     * 用户插上充电器事件
     *
     * @param newState
     */
    private void handlePluginEvent(BatteryState newState, boolean whenScreenOff) {
        boolean isSwitchOpen = getScreenViewStatus();
        if (!isSwitchOpen) {
            return;
        }

//        Toast.makeText(mContext, "用户插上充电器事件" + newState.toString(), Toast.LENGTH_LONG).show();
        int remainTime = getRemainTimeHelper(newState).getEstimatedTime(DEFAULT_LEVEL,
                newState.level, 0);
        int[] remainTimeArr = getTimeArr(newState);

        if (!BatteryShowViewActivity.isActivityAlive) {
            final Intent intent = new Intent(mContext, BatteryShowViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(PROTECT_VIEW_TYPE, SHOW_TYPE_IN);
            intent.putExtra(REMAIN_TIME, remainTime);
            intent.putExtra(SHOW_WHEN_SCREEN_OFF_FLAG, whenScreenOff);
            intent.putExtra(ARR_REMAIN_TIME, remainTimeArr);
            Bundle bundle = new Bundle();
            bundle.putSerializable(SEND_BUNDLE, newState);
            intent.putExtras(bundle);
            Message msg = Message.obtain();
            msg.obj = intent;

            if (!AppUtil.hasOtherScreenSaverInstalled(mContext)) {
                mLockManager.filterPackage(mContext.getPackageName(), 2000);
                mContext.startActivity(intent);
            } else {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        mLockManager.filterPackage(mContext.getPackageName(), 2000);
                        mContext.startActivity(intent);
                    }
                }, 200);
            }
            TaskDetectService tds = TaskDetectService.getService();
            if (tds != null) {
                tds.ignoreBatteryPage(true);
            }

        } else {
            if (mListenerRef != null) {
                BatteryStateListener listener = mListenerRef.get();
                if (listener != null) {
                    listener.onStateChange(EventType.SHOW_TYPE_IN, newState, remainTime, remainTimeArr);
                }

            }
        }
    }

    public int[] getTimeArr(BatteryState newState) {
        int callTime = mTimeEstimator.getRemainingTime(RemainingTimeEstimator.SCENE_CALL,
                newState.level, newState.scale);
        int internetTime = mTimeEstimator.getRemainingTime(RemainingTimeEstimator.SCENE_INTERNET,
                newState.level, newState.scale);
        int videoTime = mTimeEstimator.getRemainingTime(RemainingTimeEstimator.SCENE_VIDEO,
                newState.level, newState.scale);
        return new int[]{callTime, internetTime, videoTime};
    }


    /***
     * 用户拔下充电器事件
     *
     * @param newState
     */
    private void handleUnplugEvent(BatteryState newState) {
        boolean isSwitchOpen = getScreenViewStatus();
        if (!isSwitchOpen) {
            return;
        }
//        Toast.makeText(mContext, "用户拔下充电器事件" + newState.toString(), Toast.LENGTH_LONG).show();
//        broadcastBatteryLevel(newState);
        int[] remainTimeArr = getTimeArr(newState);
        if (BatteryShowViewActivity.isActivityAlive) {
            if (mListenerRef != null) {
                BatteryStateListener listener = mListenerRef.get();
                if (listener != null) {
                    listener.onStateChange(EventType.SHOW_TYPE_OUT, newState, 0, remainTimeArr);
                }
            }
        }


    }

    /***
     * 正在充电的电量变化事件
     *
     * @param newState
     */
    private void handleChargingEvent(BatteryState newState) {
        boolean isSwitchOpen = getScreenViewStatus();
        if (!isSwitchOpen) {
            return;
        }
//        Toast.makeText(mContext, "正在充电的电量变化事件" + newState.toString(), Toast.LENGTH_LONG).show();
//        broadcastBatteryLevel(newState);

        int remainTime = getRemainTimeHelper(newState)
                .getEstimatedTime(mPreviousState.level, newState.level,
                        (newState.timestamp - mPreviousState.timestamp));

        int[] remainTimeArr = getTimeArr(newState);
        if (BatteryShowViewActivity.isActivityAlive) {
            if (mListenerRef != null) {
                BatteryStateListener listener = mListenerRef.get();
                if (listener != null) {
                    listener.onStateChange(EventType.BAT_EVENT_CHARGING, newState, remainTime, remainTimeArr);
                }
            }
        }

    }

    /***
     * 正在耗电的电量变化事件
     *
     * @param newState
     */
    private void handleConsumingState(BatteryState newState) {
        boolean isSwitchOpen = getScreenViewStatus();
        if (!isSwitchOpen) {
            return;
        }
//        Toast.makeText(mContext, "正在耗电的电量变化事件" + newState.toString(), Toast.LENGTH_LONG).show();
//        broadcastBatteryLevel(newState);
        int[] remainTimeArr = getTimeArr(newState);
        if (BatteryShowViewActivity.isActivityAlive) {
            if (mListenerRef != null) {
                BatteryStateListener listener = mListenerRef.get();
                if (listener != null) {
                    listener.onStateChange(EventType.BAT_EVENT_CONSUMING, newState, 0, remainTimeArr);
                }
            }
        }
    }

    @Override
    public boolean getScreenViewStatus() {
        return mPt.getBoolean(PrefConst.KEY_BATTERY_SCREEN_VIEW_STATUS, true);
    }

    @Override
    public void setScreenViewStatus(boolean value) {
        mPt.putBoolean(PrefConst.KEY_BATTERY_SCREEN_VIEW_STATUS, value);
        if (value && mPreviousState.plugged != UNPLUGGED) {
            showSaverNotification(mPreviousState.level);
        } else {
            dismissSaverNotification();
        }
    }

    @Override
    public boolean getBatteryNotiStatus() {
        return mPt.getBoolean(PrefConst.KEY_BATTERY_NOTIFICATION_STATUS, true);
    }

    @Override
    public void setBatteryNotiStatus(boolean value) {
        mPt.putBoolean(PrefConst.KEY_BATTERY_NOTIFICATION_STATUS, value);
    }

    @Override
    public boolean getBatteryPowSavStatus() {
        return mPt.getBoolean(PrefConst.KEY_BATTERY_POWER_SAVING_STATUS, true);
    }

    @Override
    public void setBatteryPowSavStatus(boolean value) {
        mPt.putBoolean(PrefConst.KEY_BATTERY_POWER_SAVING_STATUS, value);
    }

    @Override
    public void setBatteryStateListener(BatteryStateListener listener) {
        mListenerRef = new WeakReference<BatteryStateListener>(listener);
    }

    @Override
    public void clearBatteryStateListener() {
        mListenerRef = null;
    }

    @Override
    public boolean shouldNotify() {
        LeoLog.v(TAG, "mPageOnForeground: " + mPageOnForeground
                        + " mPreviousState.plugged: " + mPreviousState.plugged
                        + " getBatteryNotiStatus(): " + getBatteryNotiStatus()
        );
        return (!mPageOnForeground
                && mPreviousState.plugged == UNPLUGGED
                && getBatteryNotiStatus());
    }

    @Override
    public boolean shouldEnableCleanFunction() {
        return (SystemClock.elapsedRealtime() - mLastKillTime) > KILL_INTERVAL;
    }

    @Override
    public int getBatteryLevel() {
        return mPreviousState.level;
    }

    @Override
    public Boolean getIsCharing() {
        return (mPreviousState.plugged != UNPLUGGED);
    }

    @Override
    public boolean shouldShowBubble() {
        return (mSp.getScreenSaveBubbleCount() < MAX_BUBBLE_SHOW_TIME);
    }

    @Override
    public void markShowBubble() {
        int count = mSp.getScreenSaveBubbleCount();
        mSp.setScreenSaveBubbleCount(count + 1);
    }

    @Override
    public void markSettingClick() {
        mSp.setScreenSaveBubbleCount(MAX_BUBBLE_SHOW_TIME);
    }

    @Override
    public void onSaverNotifiClick() {
        handlePluginEvent(mPreviousState, false);
    }

    @Override
    public void showSaverNotification() {
        this.showSaverNotification(mPreviousState.level);
    }

    @Override
    public void showSaverNotification(int level) {
        if (getScreenViewStatus()) {
            LeoLog.d("stone_saver", "call showNotificationForScreenSaver");
            mNotifyHelper.showNotificationForScreenSaver(level);
        }
    }

    @Override
    public void dismissSaverNotification() {
        mNotifyHelper.dismissScreenSaverNotification();
    }

    @Override
    public boolean isInApp() {
        return !isHome();
    }

    public boolean isHome() {
        try {
            List<String> homes = getHomes();

            if (Build.VERSION.SDK_INT >= 21 && TaskDetectService.sDetectSpecial && !BuildProperties.isLenoveModel()) {
                ProcessDetectorUsageStats state = new ProcessDetectorUsageStats();
                if (state.checkAvailable()) {
                    TaskDetectService service = TaskDetectService.getService();
                    return homes.contains(service.getLastRunningPackage());
                } else {
                    //always inHome
                    return true;
                }
            } else {
                TaskDetectService service = TaskDetectService.getService();
                return homes.contains(service.getLastRunningPackage());
            }


//            ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
//            List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
//            return homes.contains(rti.get(0).topActivity.getPackageName());

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<String> getHomes() {
        List<String> packages = new ArrayList<String>();
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : resolveInfo) {
            packages.add(info.activityInfo.packageName);
            System.out.println(info.activityInfo.packageName);
        }
        return packages;
    }
    /* 外发电量通知 */
//    private void broadcastBatteryLevel(BatteryState state) {
//        LeoLog.d(TAG, "broadcast battery event for audience");
//        int callTime = mTimeEstimator.getRemainingTime(RemainingTimeEstimator.SCENE_CALL,
//                state.level, state.scale);
//        int internetTime = mTimeEstimator.getRemainingTime(RemainingTimeEstimator.SCENE_INTERNET,
//                state.level, state.scale);
//        int videoTime = mTimeEstimator.getRemainingTime(RemainingTimeEstimator.SCENE_VIDEO,
//                state.level, state.scale);
//
//        LeoLog.d(TAG, "callTime = " + callTime + "; internetTime = " + internetTime
//                + "; videoTime = " + videoTime);
//
////        LeoEventBus.getDefaultBus().postSticky(
////                new BatteryViewEvent(EventId.EVENT_BATTERY_CHANGE_ID, state,
////                        new int[]{callTime, internetTime, videoTime}));
////        LeoEventBus.getDefaultBus().post(new BatteryViewEvent("holy shit"));
//
//        if (mListenerRef != null) {
//            BatteryStateListener listener = mListenerRef.get();
//            if (listener != null) {
//                listener.onTimeChange(state, new int[]{callTime, internetTime, videoTime});
//            }
//        }
//    }

    /* 剩余充电时间计算相关 */
    private RemainTimeHelper getRemainTimeHelper(BatteryState batteryState) {
        if (mRemainTimeHelper == null) {
            mRemainTimeHelper = new RemainTimeHelper(batteryState.scale);
        }
        return mRemainTimeHelper;
    }
}
