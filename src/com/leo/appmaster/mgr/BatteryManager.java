package com.leo.appmaster.mgr;

import android.content.Intent;
import android.os.SystemClock;

import com.leo.appmaster.engine.BatteryComsuption;

import java.io.Serializable;
import java.util.List;

/**
 * Created by stone on 16/1/13.
 */
public abstract class BatteryManager extends Manager {

    protected static final int UNPLUGGED = 0;
    protected static final int DEFAULT_LEVEL = -1;
    protected static final int DEFAULT_SCALE = 100;
    protected static final int DEFAULT_TEMP = 0;
    protected static final int DEFAULT_VOLTAGE = 0;
    protected static final boolean DEFAULT_PRESENT = false;

    public static final String SEND_BUNDLE = "battery_bundle";
    public static final String PROTECT_VIEW_TYPE = "protect_view_type";
    public static final String REMAIN_TIME = "remain_time";
    public static final String SHOW_WHEN_SCREEN_OFF_FLAG = "show_when_screen_off";
    public static final String ARR_REMAIN_TIME = "arr_remain_time";

    public static final String SHOW_TYPE_IN = "type_1";
    public static final String SHOW_TYPE_OUT = "type_2";
    public static final String UPDATE_UP = "type_3";
    public static final String UPDATE_DONW = "type_4";

    public enum EventType {
        SHOW_TYPE_IN, SHOW_TYPE_OUT, BAT_EVENT_CHARGING, BAT_EVENT_CONSUMING
    }

    public interface BatteryStateListener {
        public void onStateChange(EventType type, BatteryState newState, int remainTime,int[] remainTimeArr);
    }

    public static class BatteryState implements Serializable {
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
            return "status: " + status + "; level: " + level +
                    "; plugged: " + plugged + "; scale: " + scale;
        }
    }

    public static final String APP_THRESHOLD_KEY = "consum_app_num";

    /***
     * 获取当前正在耗电的后台应用列表
     *
     * @return 当前正在耗电的后台应用列表
     */
    public abstract List<BatteryComsuption> getBatteryDrainApps();

    /***
     * 清理正在耗电的后台应用
     */
    public abstract void killBatteryDrainApps();

    /***
     * 设置清理耗电app的阈值
     *
     * @param threshold 新阈值
     */
    public abstract void setAppThreshold(int threshold);

    /***
     * 获取清理耗电app的阈值
     *
     * @return 阈值
     */
    public abstract int getAppThreshold();

    /***
     * 更新电量管理主页的状态，请在对应activity的onResume()/onPause()中调用
     *
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

    /**
     * 获取电量屏保的开关
     *
     * @return
     */
    public abstract boolean getScreenViewStatus();

    /**
     * 设置电量屏保的开关
     *
     * @return
     */
    public abstract void setScreenViewStatus(boolean value);

    /**
     * 获取耗电应用通知的开关
     *
     * @return
     */
    public abstract boolean getBatteryNotiStatus();

    /**
     * 设置耗电应用通知的开关
     *
     * @return
     */
    public abstract void setBatteryNotiStatus(boolean value);

    /**
     * 获取自动清理耗电应用的开关
     *
     * @return
     */
    public abstract boolean getBatteryPowSavStatus();

    /**
     * 设置自动清理耗电应用的开关
     *
     * @return
     */
    public abstract void setBatteryPowSavStatus(boolean value);

    /***
     * 设置监听电池状态listener
     */
    public abstract void setBatteryStateListener(BatteryStateListener listener);

    /**
     * 清除监听电池状态listener
     */
    public abstract void clearBatteryStateListener();

    /**
     * 是否满足弹耗电通知条件
     */
    public abstract boolean shouldNotify();

    /***
     * “电源管理页”是否需要开启“一键省电”功能
     *
     * @return
     */
    public abstract boolean shouldEnableCleanFunction();

    public abstract int getBatteryLevel();

    public abstract Boolean getIsCharing();

    /* 3.3.1 充电屏保气泡通知相关 */

    /**
     * 查询是否需要显示气泡提示
     * @return true需要显示，false不显示
     */
    public abstract boolean shouldShowBubble();

    /***
     * 显示气泡的时候调用此接口
     */
    public abstract void markShowBubble();

    /***
     * 用户点击了屏保上的设置按钮时调用此接口
     */
    public abstract void markSettingClick();
}
