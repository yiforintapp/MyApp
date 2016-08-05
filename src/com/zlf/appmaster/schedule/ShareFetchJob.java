package com.zlf.appmaster.schedule;

import android.content.Context;

import com.zlfandroid.zlfvolley.VolleyError;
import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.HttpRequestAgent;
import com.zlf.appmaster.db.LeoPreference;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.PrefConst;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by forint on 16-1-14.
 */
public class ShareFetchJob extends com.zlf.appmaster.schedule.FetchScheduleJob {
    private static final String TAG = "ShareFetchJob";

    @Override
    protected void work() {
        LeoLog.i(getJobKey(), "do work.....");
        Context ctx = AppMasterApplication.getInstance();

        FetchScheduleListener listener = newJsonArrayListener();
        HttpRequestAgent.getInstance(ctx).loadShareMsg(listener, listener);
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
        LeoLog.e("onFetchSuccess", response + "");
        if (response == null) {
            LeoLog.i(TAG, "response: " + response);

            setPhoneShareEmpty(leoPreference);
            setIntruderShareEmpty(leoPreference);
            setCallFilterShareEmpty(leoPreference);

            return;
        }

        JSONObject object = (JSONObject) response;

        try {
            boolean isPhoneShareNull = object.isNull(
                    PrefConst.KEY_PHONE_SHARE); // 判断key是否存在

            if (!isPhoneShareNull) {  // 手机防盗页分享
                JSONObject priWifiMaster = object.getJSONObject(PrefConst.KEY_PHONE_SHARE);
                setValue(priWifiMaster, "content",
                        PrefConst.KEY_PHONE_SHARE_CONTENT, leoPreference);
                setValue(priWifiMaster, "url",
                        PrefConst.KEY_PHONE_SHARE_URL, leoPreference);
            } else {
                setPhoneShareEmpty(leoPreference);
            }

            boolean isIntruderShareNull = object.isNull(PrefConst.KEY_INTRUDER_SHARE); // 判断key是否存在
            if (!isIntruderShareNull) { // 入侵者防护页分享
                JSONObject priGrade = object.getJSONObject(PrefConst.KEY_INTRUDER_SHARE);
                setValue(priGrade, "content",
                        PrefConst.KEY_INTRUDER_SHARE_CONTENT, leoPreference);
                setValue(priGrade, "url",
                        PrefConst.KEY_INTRUDER_SHARE_URL, leoPreference);

            } else {
                setIntruderShareEmpty(leoPreference);
            }

            boolean isCallFilterShareNull = object.isNull(PrefConst.KEY_CALL_FILTER_SHARE); // 判断key是否存在
            if (!isCallFilterShareNull) { // 骚扰拦截分享
                JSONObject priFb = object.getJSONObject(PrefConst.KEY_CALL_FILTER_SHARE);
                setValue(priFb, "content",
                        PrefConst.KEY_CALL_FILTER_SHARE_CONTENT, leoPreference);
                setValue(priFb, "url",
                        PrefConst.KEY_CALL_FILTER_SHARE_URL, leoPreference);

            } else {
                setCallFilterShareEmpty(leoPreference);
            }

            boolean isBlackListShareNull = object.isNull(PrefConst.KEY_ADD_TO_BLACKLIST_SHARE); // 判断key是否存在
            if (!isBlackListShareNull) { // 加入黑名单弹窗分享
                JSONObject blackList = object.getJSONObject(PrefConst.KEY_ADD_TO_BLACKLIST_SHARE);
                setValue(blackList, "content",
                        PrefConst.KEY_ADD_TO_BLACKLIST_SHARE_CONTENT, leoPreference);
                setValue(blackList, "url",
                        PrefConst.KEY_ADD_TO_BLACKLIST_SHARE_URL, leoPreference);
                setValue(blackList, "dialogcontent",
                        PrefConst.KEY_ADD_TO_BLACKLIST_SHARE_DIALOG_CONTENT, leoPreference);

            } else {
                setBlackListShareEmpty(leoPreference);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /** 手机防盗分享数据置空 */
    private void setPhoneShareEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_PHONE_SHARE_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_PHONE_SHARE_URL, "");
    }

    /** 入侵者防护分享数据置空 */
    private void setIntruderShareEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_INTRUDER_SHARE_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_INTRUDER_SHARE_URL, "");
    }

    /** 骚扰拦截分享数据置空 */
    private void setCallFilterShareEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_CALL_FILTER_SHARE_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_CALL_FILTER_SHARE_URL, "");
    }

    /** 加入黑名单弹窗分享 */
    private void setBlackListShareEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_ADD_TO_BLACKLIST_SHARE_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_ADD_TO_BLACKLIST_SHARE_URL, "");
        leoPreference.putString(PrefConst.KEY_ADD_TO_BLACKLIST_SHARE_DIALOG_CONTENT, "");
    }
}
