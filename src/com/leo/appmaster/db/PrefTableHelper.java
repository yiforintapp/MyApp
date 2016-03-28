package com.leo.appmaster.db;

import android.content.Context;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.applocker.manager.ADShowTypeRequestManager;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.ProcessUtils;

/**
 * 常用字段获取帮助接口
 * Created by Jasper on 2016/2/26.
 */
public class PrefTableHelper {
    private static final String TAG = "PrefTableHelper";

    /**
     * 应用内是否展示充电屏保
     * 默认不显示
     * @return
     */
    public static boolean showInsideApp() {
        return PreferenceTable.getInstance().getBoolean(PrefConst.KEY_SHOW_INSIDE_APP, false);
    }

    /**
     * 屏保广告是否显示忽略按钮
     * 默认不显示
     * @return
     */
    public static boolean showIgnoreBtn() {
        return PreferenceTable.getInstance().getBoolean(PrefConst.KEY_SHOW_IGNORE_COC, false);
    }

    /**
     * 获取屏保广告忽略后再次显示的时长，默认24小时，单位小时
     * @return
     */
    public static int getIgnoreTs() {
        return PreferenceTable.getInstance().getInt(PrefConst.KEY_SHOW_IGNORE_COC_TS, 24);
    }

    /**
     * 屏保省电动画显示的时间间隔, 默认3小时，单位小时
     * @return
     */
    public static int getBoostTs() {
        return PreferenceTable.getInstance().getInt(PrefConst.KEY_SHOW_BOOST_TS, 3);
    }

    /**
     * 屏保省电动画显示的内存阀值，默认0.7
     * @return
     */
    public static double getBoostMem() {
        return PreferenceTable.getInstance().getDouble(PrefConst.KEY_SHOW_BOOST_MEM, 0.7);
    }

    private static final boolean DBG = false;
    public static boolean shouldBatteryBoost() {
        if (DBG) {
            return true;
        }
        long lastBoostTs = PreferenceTable.getInstance().getLong(PrefConst.KEY_LAST_BOOST_TS, 0);
        if (lastBoostTs == 0) {
            return true;
        }

        Context ctx = AppMasterApplication.getInstance();
        long totalMem = ProcessUtils.getTotalMem();
        long usedMem = ProcessUtils.getUsedMem(ctx);
        double ratio = (double)usedMem / (double)totalMem;
        LeoLog.d(TAG, "shouldBatteryBoost, totalMem: " + totalMem + " | usedMem: " + usedMem +
                " | ratio: " + ratio);
        long currentTs = System.currentTimeMillis();

        long interval = getBoostTs();
        long intervalMs = interval * 60L * 60L * 1000L;

        double defRatio = PrefTableHelper.getBoostMem();
        if ((currentTs - lastBoostTs >= intervalMs || currentTs < lastBoostTs) && ratio >= defRatio) {
            return true;
        }

        return false;
    }

    /**
     * 获取广告展示的概率 0 ~ 10 对应概率为不显示 ~ 100%
     * @return
     */
    public static int getAdProbability() {
        return PreferenceTable.getInstance().getInt(
                ADShowTypeRequestManager.AD_PROBABILITY, ADShowTypeRequestManager.AD_PROBABILITY_MAX);
    }
}
