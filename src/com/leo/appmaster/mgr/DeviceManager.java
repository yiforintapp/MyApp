package com.leo.appmaster.mgr;


import android.content.Intent;

import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.model.TrafficsInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备管理
 * Created by Jasper on 2015/9/28.
 */
public abstract class DeviceManager extends Manager {
    @Override
    public String description() {
        return MgrContext.MGR_DEVICE;
    }

    public abstract void init();

    public abstract void wifiChangeReceiver(Intent intent);

    /**
     * 是否加锁
     */
    public abstract boolean getIsLockNow(String name);

    /**
     * 加锁人数
     */
    public abstract boolean getLockNum(String name);

    /**
     * 设置开关
     */
    public abstract void setSwitch(String name, boolean lock);

    /**
     * 获取今日流量(单位B)
     */
    public abstract float getTodayUsed();

    /**
     * 获取本月已用(单位B)
     */
    public abstract long getMonthUsed();

    /**
     * 设置本月已用(单位MB)
     *
     * @param mMonthUsed
     */
    public abstract void setMonthUsed(int mMonthUsed);

    /**
     * 设置每月套餐(单位MB)
     *
     * @param mTotalTraffic
     */
    public abstract void setMonthTotalTraffic(int mTotalTraffic);

    /**
     * 获取每月套餐(单位MB)
     */
    public abstract int getMonthTotalTraffic();

    /**
     * 本月每日流量(KB)
     */
    public abstract ArrayList<Integer> getEveryDayTraffic();

    /**
     * 获取超额提醒开关
     */
    public abstract boolean getOverDataSwitch();

    /**
     * 开/关超额提醒功能
     *
     * @param mFlag
     */
    public abstract void setOverDataSwitch(boolean mFlag);

    /**
     * 获取超额提醒百分比
     */
    public abstract int getOverDataInvokePercent();

    /**
     * 超额提醒百分比
     *
     * @param mPercent
     */
    public abstract void setOverDataInvokePercent(int mPercent);


    /**
     * 设置每月结算日
     *
     * @param mDay
     */
    public abstract void setDataCutDay(int mDay);

    /**
     * 获取每月结算日
     */
    public abstract int getDataCutDay();

    /**
     * 获取应用流量排行
     */
    public abstract List<TrafficsInfo> getAppRange();

    /**
     * 获取耗电量排行
     */
    public abstract List<BatteryComsuption> getBatteryRange();

    /**
     * 停止应用
     */
    public abstract void setStopApp(String mPackageName);
}
