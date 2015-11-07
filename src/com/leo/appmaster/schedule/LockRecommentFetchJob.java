package com.leo.appmaster.schedule;

import android.content.Context;
import android.content.Intent;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.utils.LeoLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by qili on 15-10-16.
 */
public class LockRecommentFetchJob extends FetchScheduleJob {

    public final static String EXTRA_NUM = "lock_recomment_num";

    @Override
    protected void work() {
        LeoLog.d("LockRecomment", "LockRecommentFetchJob do work...");
        FetchScheduleListener listener = newJsonObjListener();
        HttpRequestAgent.getInstance(AppMasterApplication.getInstance()).
                getAppLockList(listener, listener);
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
        LeoLog.d("LockRecomment", "onErrorResponse!");
    }

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        LeoLog.d("LockRecomment", "onResponse!");
        super.onFetchSuccess(response, noMidify);
        Context ctx = AppMasterApplication.getInstance();
        JSONArray listName;
        JSONArray listNum;
        ArrayList<String> lockRecList = new ArrayList<String>();
        ArrayList<String> lockNumList = new ArrayList<String>();
        JSONObject data = (JSONObject) response;

        try {
            data = (JSONObject) data.getJSONObject("data");
            listName = data.getJSONArray("list");
            listNum = data.getJSONArray("list_num");
            if (listName != null && listNum != null) {
                for (int i = 0; i < listName.length(); i++) {
                    lockRecList.add(listName.getString(i));
                    lockNumList.add(listNum.getString(i));
                }
            } else {
                return;
            }

            LeoLog.d("LockRecomment", "listName = [" + lockRecList.toString() + "]");
            LeoLog.d("LockRecomment", "listName = [" + lockNumList.toString() + "]");
            Intent intent = new Intent(AppLoadEngine.ACTION_RECOMMEND_LIST_CHANGE);
            intent.putStringArrayListExtra(Intent.EXTRA_PACKAGES, lockRecList);
            intent.putStringArrayListExtra(EXTRA_NUM, lockNumList);
            ctx.sendBroadcast(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
