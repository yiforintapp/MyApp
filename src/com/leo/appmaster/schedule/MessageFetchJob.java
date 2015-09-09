package com.leo.appmaster.schedule;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * 消息中心抓取任务
 * Created by Jasper on 2015/9/8.
 */
public class MessageFetchJob extends FetchScheduleJob {
    @Override
    protected void work() {

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
