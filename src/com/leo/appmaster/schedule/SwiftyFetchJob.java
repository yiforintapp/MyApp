package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

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
        LeoPreference leoPreference = LeoPreference.getInstance();
        if (response == null) {
            LeoLog.i(TAG, "response: " + response);
            leoPreference.putString(PrefConst.KEY_SWIFTY_CONTENT, "");
            leoPreference.putString(PrefConst.KEY_SWIFTY_GP_URL, "");
            leoPreference.putString(PrefConst.KEY_SWIFTY_IMG_URL, "");
            leoPreference.putString(PrefConst.KEY_SWIFTY_TITLE, "");
            leoPreference.putString(PrefConst.KEY_SWIFTY_TYPE, "");
            leoPreference.putString(PrefConst.KEY_SWIFTY_URL, "");

            return;
        }

        Context ctx = AppMasterApplication.getInstance();

        JSONObject object = (JSONObject) response;

        setValue(object, "content", PrefConst.KEY_SWIFTY_CONTENT, leoPreference);
        setValue(object, "gp_url", PrefConst.KEY_SWIFTY_GP_URL, leoPreference);
        setValue(object, "img_url", PrefConst.KEY_SWIFTY_IMG_URL, leoPreference);
        setValue(object, "title", PrefConst.KEY_SWIFTY_TITLE, leoPreference);
        setValue(object, "type", PrefConst.KEY_SWIFTY_TYPE, leoPreference);
        setValue(object, "url", PrefConst.KEY_SWIFTY_URL, leoPreference);

    }



}
