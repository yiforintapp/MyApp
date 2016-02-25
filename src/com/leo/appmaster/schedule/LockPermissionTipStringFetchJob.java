package com.leo.appmaster.schedule;

import org.json.JSONObject;

import android.content.Context;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.schedule.FetchScheduleJob.FetchScheduleListener;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

public class LockPermissionTipStringFetchJob extends FetchScheduleJob {
    private static final String TAG = "LockPermissionTipStringFetchJob";
    private static final String KEY = "guideCopy";
    @Override
    protected void work() {
        LeoLog.i(TAG, "do work.....");
        Context ctx = AppMasterApplication.getInstance();
        FetchScheduleListener listener = newJsonArrayListener();
        HttpRequestAgent.getInstance(ctx).requestAppUsageStateGuideString(listener, listener);
    }

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
        LeoLog.i(TAG, "success!");
        if (response != null) {
            try {
                JSONObject object = (JSONObject) response;
                boolean isNull = object.isNull(KEY);
                if (!isNull) {
                    PreferenceTable table = PreferenceTable.getInstance();
                    table.putString(PrefConst.KEY_APP_USAGE_STATE_GUIDE_STRING, object.getString(KEY));
                    LeoLog.i(TAG, "put!" + object.getString(KEY));
                } else {
                }
            } catch (Exception e) {
            }
        }
    }
    
    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
        LeoLog.i(TAG, "onFetchFail, error: " + (error == null ? null : error.toString()));
    }
}
