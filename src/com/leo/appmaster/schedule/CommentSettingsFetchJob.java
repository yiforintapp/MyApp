package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.PrefConst;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by forint on 16-4-1.
 */
public class CommentSettingsFetchJob extends FetchScheduleJob {
    private static String TAG = "CommentSettingsFetchJob";

    @Override
    protected void work() {
        Context context = AppMasterApplication.getInstance();
        FetchScheduleJob.FetchScheduleListener listener = newJsonObjListener();
        HttpRequestAgent.getInstance(context).loadCommentSettings(listener, listener);
    }

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
        JSONObject resp = (JSONObject) response;
        try {
            /* 3.3 耗电app阈值 */
            if (!resp.isNull(BatteryManager.APP_THRESHOLD_KEY)) {
                int appNumber = resp.getInt(BatteryManager.APP_THRESHOLD_KEY);
                LeoLog.i(TAG, "耗电app阈值:" + appNumber);
                BatteryManager bm = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);
                bm.setAppThreshold(appNumber);
            }

            boolean isCallFilterShareTimesNull = resp.isNull("harass_intercept_num"); // 判断key是否存在
            LeoPreference leoPreference = LeoPreference.getInstance();
            if (!isCallFilterShareTimesNull) {
                int callFilterTimes = resp.getInt("harass_intercept_num");
                LeoLog.i(TAG, "骚扰拦截限制次数:" + callFilterTimes);
                leoPreference.putInt(PrefConst.KEY_CALL_FILTER_SHARE_TIMES, callFilterTimes);
            } else {
                leoPreference.putInt(PrefConst.KEY_CALL_FILTER_SHARE_TIMES, 10); // 获取不到默认10次
            }

            // 应用内是否展示充电屏保，默认不显示
            boolean hasData = !resp.isNull(PrefConst.KEY_SHOW_INSIDE_APP);
            if (hasData) {
                LeoLog.i(TAG, "应用内展示充电屏保");
                int data = resp.getInt(PrefConst.KEY_SHOW_INSIDE_APP);
                boolean showInsideApp = data == 1;
                leoPreference.putBoolean(PrefConst.KEY_SHOW_INSIDE_APP, showInsideApp);
            } else {
                LeoLog.i(TAG, "应用内不展示充电屏保");
            }

            // 是否显示屏保的忽略按钮
            hasData = !resp.isNull(PrefConst.KEY_SHOW_IGNORE_COC);
            if (hasData) {
                LeoLog.i(TAG, "显示屏保的忽略按钮");
                int data = resp.getInt(PrefConst.KEY_SHOW_IGNORE_COC);
                boolean showIgnore = data == 1;
                leoPreference.putBoolean(PrefConst.KEY_SHOW_IGNORE_COC, showIgnore);
            } else {
                LeoLog.i(TAG, "不显示屏保的忽略按钮");
            }

            // 忽略屏保的忽略按钮后再次显示的时间间隔
            hasData = !resp.isNull(PrefConst.KEY_SHOW_IGNORE_COC_TS);
            if (hasData) {
                int data = 24; // 默认24小时
                data = resp.getInt(PrefConst.KEY_SHOW_IGNORE_COC_TS);
                LeoLog.i(TAG, "忽略屏保的忽略按钮后再次显示的时间间隔(hour) : " + data);
                leoPreference.putInt(PrefConst.KEY_SHOW_IGNORE_COC_TS, data);
            } else {
                LeoLog.i(TAG, "没有-忽略屏保的忽略按钮后再次显示的时间间隔");
            }

            // 屏保省电动画的时间间隔
            hasData = !resp.isNull(PrefConst.KEY_SHOW_BOOST_TS);
            if (hasData) {
                int bootData = resp.getInt(PrefConst.KEY_SHOW_BOOST_TS);
                LeoLog.i(TAG, "屏保省电动画的时间间隔 : " + bootData);
                leoPreference.putInt(PrefConst.KEY_SHOW_BOOST_TS, bootData);
            } else {
                LeoLog.i(TAG, "没有-屏保省电动画的时间间隔");
            }

            // 屏保省电动画的内存阀值
            hasData = !resp.isNull(PrefConst.KEY_SHOW_BOOST_MEM);
            if (hasData) {
                double bootData = resp.getDouble(PrefConst.KEY_SHOW_BOOST_MEM);
                LeoLog.i(TAG, "屏保省电动画的内存阀值 : " + bootData);
                leoPreference.putDouble(PrefConst.KEY_SHOW_BOOST_MEM, bootData);
            } else {
                LeoLog.i(TAG, "没有-屏保省电动画的内存阀值");
            }

            boolean isGradeTimesNull = resp.isNull(PrefConst.KEY_GRADE_TIME); // 判断key是否存在
            if (!isGradeTimesNull) {
                int gradeTimes = resp.getInt("grade_time");
                LeoLog.i(TAG, "评分弹窗间隔小时:" + gradeTimes);
                leoPreference.putInt(PrefConst.KEY_GRADE_TIME, gradeTimes);
            } else {
                leoPreference.putInt(PrefConst.KEY_GRADE_TIME, 72); // 获取不到默认72小时
            }

            // 应用隐私通知个数
            boolean isAppNotifyCount = resp.isNull(PrefConst.KEY_NOTIFY_APP_COUNT);
            if (!isAppNotifyCount) {
                int count = resp.getInt(PrefConst.KEY_NOTIFY_APP_COUNT);
                if (count > 0) {
                    LeoSettings.setInteger(PrefConst.KEY_NOTIFY_APP_COUNT, count);
                }
            }

            // 应用隐私通知个数
            boolean isImgNotifyCount = resp.isNull(PrefConst.KEY_NOTIFY_IMG_COUNT);
            if (!isImgNotifyCount) {
                int count = resp.getInt(PrefConst.KEY_NOTIFY_IMG_COUNT);
                if (count > 0) {
                    LeoSettings.setInteger(PrefConst.KEY_NOTIFY_IMG_COUNT, count);
                }
            }

            // 应用隐私通知个数
            boolean isVidNotifyCount = resp.isNull(PrefConst.KEY_NOTIFY_VID_COUNT);
            if (!isVidNotifyCount) {
                int count = resp.getInt(PrefConst.KEY_NOTIFY_VID_COUNT);
                if (count > 0) {
                    LeoSettings.setInteger(PrefConst.KEY_NOTIFY_VID_COUNT, count);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
    }
}