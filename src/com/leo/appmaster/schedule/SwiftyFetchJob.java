package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by forint on 15-11-18.
 */
public class SwiftyFetchJob extends FetchScheduleJob {
    private static final String TAG = "SwiftyFetchJob";

    @Override
    protected void work() {
        LeoLog.i(getJobKey(), "do work.....");
        Context ctx = AppMasterApplication.getInstance();

        FetchScheduleListener listener = newJsonArrayListener();
        HttpRequestAgent.getInstance(ctx).loadSwiftySecurity(listener, listener);
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
        LeoLog.i(getJobKey(), "onFetchFail, error: " + (error == null ? null : error.toString()));
    }

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
        PreferenceTable preferenceTable = PreferenceTable.getInstance();
        if (response == null) {
            LeoLog.i(TAG, "response: " + response);
            preferenceTable.putString(PrefConst.KEY_SWIFTY_CONTENT, "");
            preferenceTable.putString(PrefConst.KEY_SWIFTY_GP_URL, "");
            preferenceTable.putString(PrefConst.KEY_SWIFTY_IMG_URL, "");
            preferenceTable.putString(PrefConst.KEY_SWIFTY_TITLE, "");
            preferenceTable.putString(PrefConst.KEY_SWIFTY_TYPE, "");
            preferenceTable.putString(PrefConst.KEY_SWIFTY_URL, "");

            return;
        }

        Context ctx = AppMasterApplication.getInstance();

        JSONObject object = (JSONObject) response;

        try {
            if (object.getString("content") != null && object.getString("content").length() > 0) {
                String content = object.getString("content");
                preferenceTable.putString(PrefConst.KEY_SWIFTY_CONTENT, content);
            } else {
                preferenceTable.putString(PrefConst.KEY_SWIFTY_CONTENT, "");
            }

            if (object.getString("gp_url") != null && object.getString("gp_url").length() > 0) {
                String gpUrl = object.getString("gp_url");
                preferenceTable.putString(PrefConst.KEY_SWIFTY_GP_URL, gpUrl);
            } else {
                preferenceTable.putString(PrefConst.KEY_SWIFTY_GP_URL, "");
            }

            if (object.getString("img_url") != null && object.getString("img_url").length() > 0) {
                String imgUrl = object.getString("img_url");
                preferenceTable.putString(PrefConst.KEY_SWIFTY_IMG_URL, imgUrl);
            } else {
                preferenceTable.putString(PrefConst.KEY_SWIFTY_IMG_URL, "");
            }

            if (object.getString("title") != null && object.getString("title").length() > 0) {
                String title = object.getString("title");
                preferenceTable.putString(PrefConst.KEY_SWIFTY_TITLE, title);
            } else {
                preferenceTable.putString(PrefConst.KEY_SWIFTY_TITLE, "");
            }

            if (object.getString("type") != null && object.getString("type").length() > 0) {
                String type = object.getString("type");
                preferenceTable.putString(PrefConst.KEY_SWIFTY_TYPE, type);
            } else {
                preferenceTable.putString(PrefConst.KEY_SWIFTY_TYPE, "");
            }

            if (object.getString("url") != null && object.getString("url").length() > 0) {
                String url = object.getString("url");
                preferenceTable.putString(PrefConst.KEY_SWIFTY_URL, url);
            } else {
                preferenceTable.putString(PrefConst.KEY_SWIFTY_URL, "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
