package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.MsgCenterTable;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.msgcenter.Message;
import com.leo.appmaster.utils.LeoLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息中心抓取任务
 * Created by Jasper on 2015/9/8.
 */
public class MessageFetchJob extends FetchScheduleJob {
    private static final boolean DBG = true;

    public static void startByPush() {
        MessageFetchJob job = new MessageFetchJob();
        FetchScheduleListener listener = job.newJsonArrayListener();
        Context ctx = AppMasterApplication.getInstance();
        HttpRequestAgent.getInstance(ctx).loadMessageCenterList(listener, listener);
    }

    @Override
    protected void work() {
        LeoLog.i(getJobKey(), "do work.....");

        Context ctx = AppMasterApplication.getInstance();

        FetchScheduleListener listener = newJsonArrayListener();
        HttpRequestAgent.getInstance(ctx).loadMessageCenterList(listener, listener);
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
    }

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
        if (response == null || !(response instanceof JSONArray)) return;

        List<Message> list = new ArrayList<Message>();
        try {
            JSONArray array = (JSONArray) response;
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = (JSONObject) array.get(i);

                Message message = new Message();
                message.time = obj.getString("activity_time");
                message.name = obj.getString("category_name");
                message.description = obj.getString("description");
                message.imageUrl = obj.getString("image_url");
                message.jumpUrl = obj.getString("link");
                message.offlineTime = obj.getString("offline_time");
                message.title = obj.getString("title");
                message.typeId =obj.getString("type_id");
                message.resUrl =obj.getString("resource");
                message.id = obj.getInt("id");

                list.add(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MsgCenterTable table = new MsgCenterTable();
        table.insertMsgList(list);
    }

    @Override
    protected int getPeriod() {
        return 10 * 1000;
    }

    @Override
    protected int getFailPeriod() {
        return 10 * 1000;
    }

}
