package com.leo.appmaster.schedule;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
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
            String usageGuide = parseContent(KEY, response);
            if (usageGuide != null) {
                LeoSettings.setString(PrefConst.KEY_APP_USAGE_STATE_GUIDE_STRING, usageGuide);
            }

            String notifyAppTitle = parseContent(PrefConst.KEY_NOTIFY_APP_TITLE, response);
            if (notifyAppTitle != null) {
                LeoSettings.setString(PrefConst.KEY_NOTIFY_APP_TITLE, notifyAppTitle);
            }

            String notifyAppContent = parseContent(PrefConst.KEY_NOTIFY_APP_CONTENT, response);
            if (notifyAppContent != null) {
                LeoSettings.setString(PrefConst.KEY_NOTIFY_APP_CONTENT, notifyAppContent);
            }

            String notifyImgTitle = parseContent(PrefConst.KEY_NOTIFY_IMG_TITLE, response);
            if (notifyImgTitle != null) {
                LeoSettings.setString(PrefConst.KEY_NOTIFY_IMG_TITLE, notifyImgTitle);
            }

            String notifyImgContent = parseContent(PrefConst.KEY_NOTIFY_IMG_TITLE, response);
            if (notifyImgContent != null) {
                LeoSettings.setString(PrefConst.KEY_NOTIFY_IMG_CONTENT, notifyImgContent);
            }

            String notifyVidTitle = parseContent(PrefConst.KEY_NOTIFY_VID_TITLE, response);
            if (notifyVidTitle != null) {
                LeoSettings.setString(PrefConst.KEY_NOTIFY_VID_TITLE, notifyVidTitle);
            }

            String notifyVidContent = parseContent(PrefConst.KEY_NOTIFY_VID_CONTENT, response);
            if (notifyVidContent != null) {
                LeoSettings.setString(PrefConst.KEY_NOTIFY_VID_CONTENT, notifyVidContent);
            }
        }
    }
    
    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
        LeoLog.i(TAG, "onFetchFail, error: " + (error == null ? null : error.toString()));
    }

    private String parseContent(String key, Object response) {
        String result = null;
        try {
            JSONObject object = (JSONObject) response;
            boolean isNull = object.isNull(key);
            if (!isNull) {
                JSONObject tip = object.getJSONObject(key);
                if(tip != null) {
                    result = tip.getString(CONTENT_KEY);
                }
            }
        } catch (Exception e) {
        }

        return result;
    }
}
