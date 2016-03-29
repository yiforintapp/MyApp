package com.leo.appmaster.schedule;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

public class LockPermissionTipStringFetchJob extends FetchScheduleJob {
    private static final String TAG = "LockPermissionTipStringFetchJob";
    private static final String KEY = "guideCopy";
    private static final String CONTENT_KEY = "content";
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
                    LeoPreference table = LeoPreference.getInstance();
                    JSONObject tip = object.getJSONObject(KEY);
                    LeoLog.i(TAG, "guideCopy :" + tip);
                    if(tip != null) {
                        String tip2 = tip.getString(CONTENT_KEY);
                        if (!TextUtils.isEmpty(tip2)) {
                            table.putString(PrefConst.KEY_APP_USAGE_STATE_GUIDE_STRING, tip2);
                            LeoLog.i(TAG, "put!" + tip2);
                        }
                    }
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
