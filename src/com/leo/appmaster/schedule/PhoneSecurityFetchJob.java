package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.PrefConst;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by runlee on 15-10-23.
 */
public class PhoneSecurityFetchJob extends FetchScheduleJob {
    private static String TAG = "PhoneSecurityFetchJob";

    public static void startImmediately() {
        /*存在网络再去拉取*/
        if (NetWorkUtil.isNetworkAvailable(AppMasterApplication.getInstance())) {
            PhoneSecurityFetchJob job = new PhoneSecurityFetchJob();
            FetchScheduleListener listener = job.newJsonObjListener();
            Context context = AppMasterApplication.getInstance();
            HttpRequestAgent.getInstance(context).loadPhoneSecurity(listener, listener);
        }
    }

    @Override
    protected void work() {
        Context context = AppMasterApplication.getInstance();
        FetchScheduleListener listener = newJsonObjListener();
        HttpRequestAgent.getInstance(context).loadPhoneSecurity(listener, listener);
    }

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
        JSONObject resp = (JSONObject) response;
        try {
                /*开启手机防盗人数*/
            int securNumber = resp.getInt("data");
            if (securNumber > 0) {
                LeoLog.i(TAG, "开启手机防盗人数:" + securNumber);
                LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                lostMgr.setUsePhoneSecurityConut(securNumber);
            } else {
                LeoLog.i(TAG, "手机防盗开启人数小于0");
            }

            /* 3.3 耗电app阈值 */
            if (!resp.isNull(BatteryManager.APP_THRESHOLD_KEY)) {
                int appNumber = resp.getInt(BatteryManager.APP_THRESHOLD_KEY);
                BatteryManager bm = (BatteryManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                bm.setAppThreshold(appNumber);
            }

            boolean isCallFilterShareTimesNull = resp.isNull("harass_intercept_num"); // 判断key是否存在
            PreferenceTable preferenceTable = PreferenceTable.getInstance();
            if (!isCallFilterShareTimesNull) {
                int callFilterTimes = resp.getInt("harass_intercept_num");
                preferenceTable.putInt(PrefConst.KEY_CALL_FILTER_SHARE_TIMES, callFilterTimes);
            } else {
                preferenceTable.putInt(PrefConst.KEY_CALL_FILTER_SHARE_TIMES, 10); // 获取不到默认10次
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
