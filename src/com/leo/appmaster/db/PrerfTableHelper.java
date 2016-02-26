package com.leo.appmaster.db;

import com.leo.appmaster.utils.PrefConst;

/**
 * 常用字段获取帮助接口
 * Created by Jasper on 2016/2/26.
 */
public class PrerfTableHelper {

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
}
