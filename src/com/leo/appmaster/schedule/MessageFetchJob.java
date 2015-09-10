package com.leo.appmaster.schedule;

import com.android.volley.VolleyError;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.utils.LeoLog;

import org.json.JSONObject;

/**
 * 消息中心抓取任务
 * Created by Jasper on 2015/9/8.
 */
public class MessageFetchJob extends FetchScheduleJob {

    public static void startJob() {
        new MessageFetchJob().start();
    }

    @Override
    protected void work() {
        LeoLog.i(getJobKey(), "do work.....");

        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onFetchSuccess(null, false);
//                onFetchFail(null);
            }
        }, 5 * 1000);
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
    }

    @Override
    protected void onFetchSuccess(JSONObject response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
    }
}
