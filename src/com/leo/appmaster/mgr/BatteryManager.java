package com.leo.appmaster.mgr;

import com.leo.appmaster.engine.BatteryComsuption;

import java.util.List;

/**
 * Created by stone on 16/1/13.
 */
public abstract class BatteryManager extends Manager {

    public static final String APP_THRESHOLD_KEY = "consum_app_num";

    /***
     * 获取当前正在耗电的后台应用列表
     * @return 当前正在耗电的后台应用列表
     */
    public abstract List<BatteryComsuption> getBatteryDrainApps();

    /***
     * 清理正在耗电的后台应用
     */
    public abstract void killBatteryDrainApps();

    /***
     * 设置清理耗电app的阈值
     * @param threshold 新阈值
     */
    public abstract void setAppThreshold(int threshold);

    /***
     * 获取清理耗电app的阈值
     * @return 阈值
     */
    public abstract int getAppThreshold();

    /***
     * 更新电量管理主页的状态，请在对应activity的onResume()/onPause()中调用
     * @param isForeground 电量管理主页是否在前台
     */
    public abstract void updateBatteryPageState(boolean isForeground);

    @Override
    public void onDestory() {

    }

    @Override
    public String description() {
        return MgrContext.MGR_BATTERY;
    }
}
