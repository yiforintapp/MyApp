package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.NetWorkUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by runlee on 15-12-23.
 */
public class BlackListUploadFetchJob extends FetchScheduleJob {

    public static final String NUMBER = "number";
    public static final String TYPE = "type";
    public static final String COUNTRY = "country";
    public static final String ANDROID_ID = "android_id";


    public static void startImmediately() {
        /*存在网络再去拉取*/
        if (NetWorkUtil.isNetworkAvailable(AppMasterApplication.getInstance())) {
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

    private static void startWork() {
        PhoneSecurityFetchJob job = new PhoneSecurityFetchJob();
        FetchScheduleListener listener = job.newJsonObjListener();
        Context context = AppMasterApplication.getInstance();
        Map<String, String> params = new HashMap<String, String>();
        params.put(NUMBER, null);
        params.put(TYPE, null);
        params.put(COUNTRY, null);
        params.put(ANDROID_ID, null);

        HttpRequestAgent.getInstance(context).commitBlackList(listener, listener, params);
    }

}
