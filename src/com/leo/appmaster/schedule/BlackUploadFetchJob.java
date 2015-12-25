package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.callfilter.BlackListInfo;
import com.leo.appmaster.callfilter.CallFilterUtils;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.utils.DeviceUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by runlee on 15-12-23.
 */
public class BlackUploadFetchJob extends FetchScheduleJob {

    public static final String TAG = "BlackUploadFetchJob";

    public static final String NUMBER = "number";
    public static final String TYPE = "type";
    public static final String COUNTRY = "country";
    public static final String ANDROID_ID = "android_id";
    public static final String BODY = "body";
    /**
     * 拉取间隔，24小时
     */
    private static final int FETCH_PERIOD = 24 * 60 * 60 * 1000;

    public static void startImmediately() {
        /*存在wifi网络再去拉取*/
        if (NetWorkUtil.isWifiConnected(AppMasterApplication.getInstance())) {
            startWork();
        }
    }

    @Override
    protected void work() {
        startWork();
    }

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
    }

    public static void startWork() {
        BlackUploadFetchJob job = new BlackUploadFetchJob();
        FetchScheduleListener listener = job.newJsonObjListener();
        Context context = AppMasterApplication.getInstance();

        CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        int i = 1;
        while (true) {
            List<BlackListInfo> infos = pm.getNoUpBlackListLimit(i);
            if (infos == null || infos.size() <= 0) {
                break;
            }
            i = i + 1;
            String bodyString = getJsonString(infos);
            HttpRequestAgent.getInstance(context).commitBlackList(listener, listener, bodyString);
        }

    }

    public static String getJsonString(List<BlackListInfo> infos) {
        if (infos == null || infos.size() <= 0) {
            return null;
        }

        Context context = AppMasterApplication.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 1; i <= infos.size(); i++) {
            sb.append("{");
            sb.append("\"" + NUMBER + "\":");
            String number = infos.get(i - 1).getNumber();
            sb.append("\"" + number + "\",");
            sb.append("\"" + COUNTRY + "\":");
            sb.append("\"" + Utilities.getCountryID(context) + "\",");
            sb.append("\"" + TYPE + "\":");
            int type = infos.get(i - 1).getLocHandlerType();
            sb.append(type + ",");
            sb.append("\"" + ANDROID_ID + "\":");
            sb.append("\"" + DeviceUtil.getAndroidId(context) + "\"");
            sb.append("}");
            if (i != infos.size()) {
                sb.append(",");
            }
        }
        sb.append("]");
        LeoLog.i(TAG, "" + sb.toString());
        return sb.toString();
    }

    @Override
    protected int getPeriod() {
        return FETCH_PERIOD;
    }
}
