package com.leo.appmaster.mgr.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.engine.BatteryInfoProvider;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;

import java.util.List;

/**
 * Created by stone on 16/1/13.
 */
public class BatteryManagerImpl extends BatteryManager {

    private static final String TAG = MgrContext.MGR_BATTERY;

    private AppMasterPreference mSp;

    @Override
    public List<BatteryComsuption> getBatteryDrainApps() {
        BatteryInfoProvider ip = new BatteryInfoProvider(mContext);
        ip.setMinPercentOfTotal(0.01);
        List<BatteryComsuption> list = ip.getBatteryStats();
        // TODO - filter out system apps or background music apps

        return ip.getBatteryStats();
    }

    @Override
    public void killBatteryDrainApps() {
        List<BatteryComsuption> appsToKill = getBatteryDrainApps();
        for (BatteryComsuption batteryComsuption: appsToKill) {
            ProcessCleaner.getInstance(mContext).cleanProcess(batteryComsuption.getDefaultPackageName());
        }
    }

    @Override
    public void setAppThreshold(int threshold) {
        mSp.setPowerConsumeAppThreshold(threshold);
    }

    @Override
    public int getAppThreshold() {
        return mSp.getPowerConsumeAppThreshold();
    }

    public BatteryManagerImpl() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
        mSp = AppMasterPreference.getInstance(mContext);
    }

    @Override
    public void onDestory() {
        super.onDestory();
        mContext.unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO post event through LeoEventBus here

        }
    };
}
