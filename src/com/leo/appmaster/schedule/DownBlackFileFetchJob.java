package com.leo.appmaster.schedule;

import android.content.Context;
import android.telecom.Call;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.callfilter.CallFilterConstants;
import com.leo.appmaster.callfilter.CallFilterUtils;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.Utilities;

/**
 * Created by runlee on 15-12-24.
 */
public class DownBlackFileFetchJob extends FetchScheduleJob {
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
        StringBuilder sbName = new StringBuilder();
        String countryId = Utilities.getCountryID(AppMasterApplication.getInstance());
        sbName.append(countryId);
        sbName.append(CallFilterConstants.GZIP);

        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterUtils.getBlackPath());
        sb.append(sbName.toString());
        String filePath = sb.toString();
        CallFilterUtils.parseBlactList(filePath);
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
    }

    public static void startWork() {
        DownBlackFileFetchJob job = new DownBlackFileFetchJob();
        FetchScheduleListener listener = job.newJsonObjListener();
        Context context = AppMasterApplication.getInstance();
        StringBuilder sbName = new StringBuilder();
        String countryId = Utilities.getCountryID(AppMasterApplication.getInstance());
        sbName.append(countryId);
        sbName.append(CallFilterConstants.GZIP);

        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterUtils.getBlackPath());
        sb.append(sbName.toString());
        String filePath = sb.toString();
        HttpRequestAgent.getInstance(context).downloadBlackList(filePath, listener, listener);
    }

    @Override
    protected int getPeriod() {
        return FETCH_PERIOD;
    }
}
