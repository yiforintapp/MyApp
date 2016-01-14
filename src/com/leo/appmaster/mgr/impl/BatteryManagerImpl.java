package com.leo.appmaster.mgr.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.applocker.model.ProcessAdj;
import com.leo.appmaster.battery.BatterProtectView;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.engine.BatteryInfoProvider;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.imageloader.utils.L;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by stone on 16/1/13.
 */
public class BatteryManagerImpl extends BatteryManager {
    private PreferenceTable mPt = PreferenceTable.getInstance();
    private static final String TAG = MgrContext.MGR_BATTERY;

    private AppMasterPreference mSp;
    private boolean mPageOnForeground = false;
    private long mLastKillTime = 0;

    private static final int UNPLUGGED = 0;
    private static final int DEFAULT_LEVEL = 0;
    private static final int DEFAULT_SCALE = 100;
    private static final int DEFAULT_TEMP = 0;
    private static final int DEFAULT_VOLTAGE = 0;
    private static final boolean DEFAULT_PRESENT = false;

    private static class BatteryState {
        public int level = DEFAULT_LEVEL;
        public int plugged = android.os.BatteryManager.BATTERY_PLUGGED_USB;
        public boolean present = DEFAULT_PRESENT;
        public int scale = DEFAULT_SCALE;
        public int status = android.os.BatteryManager.BATTERY_STATUS_UNKNOWN;
        public int temperature = DEFAULT_TEMP;
        public int voltage = DEFAULT_VOLTAGE;
        public long timestamp = 0;

        public BatteryState() {
        }

        public BatteryState(Intent intent) {
            level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL,
                    DEFAULT_LEVEL);
            plugged = intent.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED,
                    UNPLUGGED);
            present = intent.getBooleanExtra(android.os.BatteryManager.EXTRA_PRESENT,
                    DEFAULT_PRESENT);
            scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE,
                    DEFAULT_SCALE);
            status = intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS,
                    android.os.BatteryManager.BATTERY_STATUS_UNKNOWN);
            temperature = intent.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE,
                    DEFAULT_TEMP);
            voltage = intent.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE,
                    DEFAULT_VOLTAGE);
            timestamp = SystemClock.elapsedRealtime();
        }

        public String toString() {
            return "status: " + status + "; level: " + level + "; plugged: " + plugged;
        }
    }

    private BatteryState mPreviousState = new BatteryState();

    public BatteryManagerImpl() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiver(mReceiver, filter);
        mSp = AppMasterPreference.getInstance(mContext);
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
        List<BatteryComsuption> appsToKill = getBatteryDrainApps();
        for (BatteryComsuption batteryComsuption : appsToKill) {
            ProcessCleaner.getInstance(mContext).cleanProcess(batteryComsuption.getDefaultPackageName());
        }
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
        BatterProtectView mProtectView = BatterProtectView.makeText(mContext);
        mProtectView.setBatteryStatus(true);
        mProtectView.setBatteryLevel(newState.level);
        mProtectView.notifyViewUi();
        mProtectView.show();
    }

    /***
     * 用户拔下充电器事件
     *
     * @param newState
     */
    private void handleUnplugEvent(BatteryState newState) {
        Toast.makeText(mContext, "用户拔下充电器事件" + newState.toString(), Toast.LENGTH_LONG).show();
        BatterProtectView mProtectView = BatterProtectView.makeText(mContext);
        mProtectView.setBatteryStatus(false);
        mProtectView.setBatteryLevel(newState.level);
        mProtectView.notifyViewUi();
    }

    /***
     * 正在充电的电量变化事件
     *
     * @param newState
     */
    private void handleChargingEvent(BatteryState newState) {
        Toast.makeText(mContext, "正在充电的电量变化事件" + newState.toString(), Toast.LENGTH_LONG).show();
        BatterProtectView mProtectView = BatterProtectView.makeText(mContext);
        mProtectView.setBatteryStatus(true);
        mProtectView.setBatteryLevel(newState.level);
        mProtectView.notifyViewUi();
    }

    /***
     * 正在耗电的电量变化事件
     *
     * @param newState
     */
    private void handleConsumingState(BatteryState newState) {
        Toast.makeText(mContext, "正在耗电的电量变化事件" + newState.toString(), Toast.LENGTH_LONG).show();
        BatterProtectView mProtectView = BatterProtectView.makeText(mContext);
        mProtectView.setBatteryStatus(true);
        mProtectView.setBatteryLevel(newState.level);
        mProtectView.notifyViewUi();

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
}
