package com.leo.appmaster.mgr;

import com.leo.appmaster.engine.BatteryComsuption;

import java.util.List;

/**
 * Created by stone on 16/1/13.
 */
public abstract class BatteryManager extends Manager {

    /***
     * 获取当前正在耗电的后台应用列表
     * @return 当前正在耗电的后台应用列表
     */
    public abstract List<BatteryComsuption> getBatteryDrainApps();

    /***
     * 清理正在耗电的后台应用
     */
    public abstract void killBatteryDrainApps();

    @Override
    public void onDestory() {

    }

    @Override
    public String description() {
        return MgrContext.MGR_BATTERY;
    }
}
