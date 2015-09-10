package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.msgcenter.Message;
import com.leo.appmaster.utils.LeoLog;

import org.json.JSONException;
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

        Context ctx = AppMasterApplication.getInstance();

        FetchScheduleListener listener = newFetchListener();
        HttpRequestAgent.getInstance(ctx).loadMessageCenterList(listener, listener);

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

        if (response == null) return;

        Message message = new Message();
        try {
            message.time = response.getString("activity_time");
            message.name = response.getString("category_name");
            message.description = response.getString("description");
            message.imageUrl = response.getString("image_url");
            message.jumpUrl = response.getString("link");
            message.offlineTime = response.getString("offline_time");
            message.title = response.getString("title");
            message.typeId = response.getString("type_id");
            message.id = response.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
