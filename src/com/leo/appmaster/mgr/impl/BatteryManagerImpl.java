package com.leo.appmaster.mgr.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.applocker.model.ProcessAdj;
import com.leo.appmaster.battery.BatteryNotifyHelper;
import com.leo.appmaster.battery.RemainTimeHelper;
import com.leo.appmaster.battery.BatteryShowViewActivity;
import com.leo.appmaster.callfilter.CallFilterInfo;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.engine.BatteryInfoProvider;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BatteryViewEvent;
import com.leo.appmaster.eventbus.event.LockThemeChangeEvent;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by stone on 16/1/13.
 */
public class BatteryManagerImpl extends BatteryManager {
    private PreferenceTable mPt = PreferenceTable.getInstance();
    private static final String TAG = MgrContext.MGR_BATTERY;
    public static final String SEND_BUNDLE = "battery_bundle";
    public static final String PROTECT_VIEW_TYPE = "protect_view_type";
    public static final String REMAIN_TIME = "remain_time";

    public static final String SHOW_TYPE_IN = "type_1";
    public static final String SHOW_TYPE_OUT = "type_2";
    public static final String UPDATE_UP = "type_3";
    public static final String UPDATE_DONW = "type_4";


    private AppMasterPreference mSp;
    private boolean mPageOnForeground = false;
    private long mLastKillTime = 0;

    private RemainTimeHelper mRemainTimeHelper;
    private BatteryNotifyHelper mNotifyHelper;

    private LockManager mLockManager;
    private WeakReference<BatteryStateListener> mListenerRef;

    private BatteryState mPreviousState = new BatteryState();

    public BatteryManagerImpl() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiver(mReceiver, filter);
        mSp = AppMasterPreference.getInstance(mContext);
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        mNotifyHelper = new BatteryNotifyHelper(mContext, this);
    }

    @Override
    public List<BatteryComsuption> getBatteryDrainApps() {
        BatteryInfoProvider ip = new BatteryInfoProvider(mContext);
        ip.setMinPercentOfTotal(0.01);
        List<BatteryComsuption> list = ip.getBatteryStats();
        for (BatteryComsuption bc : list) {
            LeoLog.d("stone_test", "BatteryComsuption -> " + bc.getDefaultPackageName());
        }

        // TODO - filter out system apps or background music apps

        List<ProcessAdj> processAdjList = ip.getProcessWithPS();
        ArrayList<String> pkgs = new ArrayList<String>();
        for (ProcessAdj processAdj : processAdjList) {
            pkgs.add(processAdj.pkg);
            LeoLog.d("stone_test", "processAdj -> " + processAdj.pkg);
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
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // TODO post event through LeoEventBus here
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                BatteryState bs = new BatteryState(intent);
                handleBatteryChange(bs);
                LeoLog.d(TAG, "newState: " + bs.toString());
            } else {
                LeoLog.d(TAG, "action: " + action +
                        "; mPreviousState: " + mPreviousState.toString());
                if (action.equals(Intent.ACTION_SCREEN_ON) &&
                        mPreviousState.plugged != UNPLUGGED) {
                    LeoLog.d(TAG, "need to show charging screen");
                    handlePluginEvent(mPreviousState);
                }
            }
        }
    };

    /* 充电时间计算相关 */
    private void handleBatteryChange(BatteryState newState) {
        if (newState.plugged != UNPLUGGED && mPreviousState.plugged == UNPLUGGED) {
            handlePluginEvent(newState);
        } else if (mPreviousState.plugged != UNPLUGGED && newState.plugged == UNPLUGGED) {
            handleUnplugEvent(newState);
        } else if (newState.plugged != UNPLUGGED && newState.level != mPreviousState.level) {
            handleChargingEvent(newState);
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
    private void handlePluginEvent(BatteryState newState) {
        Toast.makeText(mContext, "用户插上充电器事件" + newState.toString(), Toast.LENGTH_LONG).show();

        int remainTime = getRemainTimeHelper(newState).getEstimatedTime(DEFAULT_LEVEL,
                newState.level, 0);

        Intent intent = new Intent(mContext, BatteryShowViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent.putExtra(PROTECT_VIEW_TYPE, SHOW_TYPE_IN);
        intent.putExtra(REMAIN_TIME, remainTime);

        Bundle bundle = new Bundle();
        bundle.putSerializable(SEND_BUNDLE, newState);
        intent.putExtras(bundle);

        mLockManager.filterSelfOneMinites();
        mLockManager.filterPackage(mContext.getPackageName(), 1000);

        mContext.startActivity(intent);

//        BatteryProtectView mProtectView = BatteryProtectView.makeText(mContext);
//        mProtectView.setBatteryStatus(true);
//        mProtectView.setBatteryLevel(newState.level);
//        mProtectView.notifyViewUi();
//        mProtectView.show();
    }

    /***
     * 用户拔下充电器事件
     *
     * @param newState
     */
    private void handleUnplugEvent(BatteryState newState) {
        Toast.makeText(mContext, "用户拔下充电器事件" + newState.toString(), Toast.LENGTH_LONG).show();

        BatteryViewEvent event = new BatteryViewEvent(SHOW_TYPE_OUT);
        event.state = newState;
        LeoEventBus.getDefaultBus().post(event);

//        BatteryProtectView mProtectView = BatteryProtectView.makeText(mContext);
//        mProtectView.setBatteryStatus(false);
//        mProtectView.setBatteryLevel(newState.level);
//        mProtectView.notifyViewUi();
    }

    /***
     * 正在充电的电量变化事件
     *
     * @param newState
     */
    private void handleChargingEvent(BatteryState newState) {
        Toast.makeText(mContext, "正在充电的电量变化事件" + newState.toString(), Toast.LENGTH_LONG).show();
        int remainTime = getRemainTimeHelper(newState)
                .getEstimatedTime(mPreviousState.level, newState.level,
                        (newState.timestamp - mPreviousState.timestamp));

        if (mListenerRef != null) {
            BatteryStateListener listener = mListenerRef.get();
            if (listener != null) {
                listener.onStateChange(EventType.BAT_EVENT_CHARGING, newState, remainTime);
            }
        }

        BatteryViewEvent event = new BatteryViewEvent(UPDATE_UP);
        event.state = newState;
        event.remainTime = remainTime;
        LeoEventBus.getDefaultBus().post(event);

//        BatteryProtectView mProtectView = BatteryProtectView.makeText(mContext);
//        mProtectView.setBatteryStatus(true);
//        mProtectView.setBatteryLevel(newState.level);
//        mProtectView.notifyViewUi();
    }

    /***
     * 正在耗电的电量变化事件
     *
     * @param newState
     */
    private void handleConsumingState(BatteryState newState) {
        Toast.makeText(mContext, "正在耗电的电量变化事件" + newState.toString(), Toast.LENGTH_LONG).show();

        if (mListenerRef != null) {
            BatteryStateListener listener = mListenerRef.get();
            if (listener != null) {
                listener.onStateChange(EventType.BAT_EVENT_CONSUMING, newState, 0);
            }
        }

        BatteryViewEvent event = new BatteryViewEvent(UPDATE_DONW);
        event.state = newState;
        LeoEventBus.getDefaultBus().post(event);


//        BatteryProtectView mProtectView = BatteryProtectView.makeText(mContext);
//        mProtectView.setBatteryStatus(true);
//        mProtectView.setBatteryLevel(newState.level);
//        mProtectView.notifyViewUi();

    }

    @Override
    public boolean getScreenViewStatus() {
        return mPt.getBoolean(PrefConst.KEY_BATTERY_SCREEN_VIEW_STATUS, true);
    }

    @Override
    public void setScreenViewStatus(boolean value) {
        mPt.putBoolean(PrefConst.KEY_BATTERY_SCREEN_VIEW_STATUS, value);
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
    public void setBatteryStateListener(BatteryStateListener listener) {
        mListenerRef = new WeakReference<BatteryStateListener>(listener);
    }

    @Override
    public void clearBatteryStateListener() {
        mListenerRef = null;
    }

    /* 剩余充电时间计算相关 */
    private RemainTimeHelper getRemainTimeHelper(BatteryState batteryState) {
        if (mRemainTimeHelper == null) {
            mRemainTimeHelper = new RemainTimeHelper(batteryState.scale);
        }
        return mRemainTimeHelper;
    }
}
