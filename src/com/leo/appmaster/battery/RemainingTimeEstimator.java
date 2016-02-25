package com.leo.appmaster.battery;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.leo.appmaster.utils.LeoLog;

/**
 * Created by stone on 16/2/25.
 */
public class RemainingTimeEstimator {

    /* 通话 */
    public static final int SCENE_CALL = 0;
    /* 上网 */
    public static final int SCENE_INTERNET = 1;
    /* 播放视频 */
    public static final int SCENE_VIDEO = 2;
    public static final int SCENE_COUNT = SCENE_VIDEO + 1;

    private int capacity; // mAh - 没有统一的API获取电量，参考意义不大
    private int processorNumber;
    private double screenSize; // in inches

    /* 参考设备：华为P7 */
    private static final int REFERENT_CAPACITY = 2460;
    private static final int REFERENT_PROCESSOR_NUMBER = 4;
    private static final double REFERENT_SCREEN_SIZE = 4.33f;

    public RemainingTimeEstimator(Context context) {
        capacity = (int) getDeviceBatteryCapacity();
        processorNumber = getDeviceProcessorNumber();
        screenSize = getDeviceScreenSize(context);
    }

    /***
     * 估算某场景下电池可用时间
     * @param scene 场景，现支持 通话，上网，播放视频
     * @param level 当前电池电量百分比
     * @return 以秒为单位返回电池可用时间
     */
    public int getRemainingTime (int scene, int level) {
        if (scene < 0 || scene >= SCENE_COUNT) {
            scene = SCENE_INTERNET;
        }

        return 0;
    }

    /* private methods */
    private double getDeviceBatteryCapacity() {
        Object mPowerProfile_ = null;

        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mPowerProfile_ == null) {
            return REFERENT_CAPACITY;
        }

        try {
            double batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
            LeoLog.d("stone_battery", "batteryCapacity=" + batteryCapacity);
            return batteryCapacity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return REFERENT_CAPACITY;
    }

    private int getDeviceProcessorNumber () {
        return Runtime.getRuntime().availableProcessors();
    }

    public double getDeviceScreenSize (Context context) {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
            int width=dm.widthPixels;
            int height=dm.heightPixels;
            int dens=dm.densityDpi;
            double wi=(double)width/(double)dens;
            double hi=(double)height/(double)dens;
            double x = Math.pow(wi, 2);
            double y = Math.pow(hi, 2);
            double screenInches = Math.sqrt(x + y);
            LeoLog.d("stone_battery", "screenInches=" + screenInches + "; width=" + width + "; height=" + height);
            return screenInches;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return REFERENT_SCREEN_SIZE;
    }
}
