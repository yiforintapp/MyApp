package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by forint on 15-12-7.
 */
public class CardFetchJob extends FetchScheduleJob {
    private static final String TAG = "CardFetchJob";

    @Override
    protected void work() {
        LeoLog.i(getJobKey(), "do work.....");
        Context ctx = AppMasterApplication.getInstance();

        FetchScheduleListener listener = newJsonArrayListener();
        HttpRequestAgent.getInstance(ctx).loadCardMsg(listener, listener);
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

            setPriWifiMasterEmpty(leoPreference);
            setPriGradeEmpty(leoPreference);
            setPriFbEmpty(leoPreference);
            setWifiSwiftyEmpty(leoPreference);
            setWifiWifiMasterEmpty(leoPreference);
            setWifiGradeEmpty(leoPreference);
            setWifiFbEmpty(leoPreference);
            setChargeSwiftyEmpty(leoPreference);
            setIntruderSwiftyEmpty(leoPreference);
            setChargeExtraEmpty(leoPreference);
            setCleanSwiftyEmpty(leoPreference);
            setAppGradeEmpty(leoPreference);
            setPictureGradeEmpty(leoPreference);
            setVideoGradeEmpty(leoPreference);
            setGradeFbAnotherEmpty(leoPreference);

            return;
        }

        JSONObject object = (JSONObject) response;

        try {
            boolean isPriWifiMasterNull = object.isNull(
                                          PrefConst.KEY_PRI_WIFIMASTER); // 判断key是否存在

            if (!isPriWifiMasterNull) {  // 隐私页wifimaster
                JSONObject priWifiMaster = object.getJSONObject(PrefConst.KEY_PRI_WIFIMASTER);
                setValue(priWifiMaster, "content",
                        PrefConst.KEY_PRI_WIFIMASTER_CONTENT, leoPreference);
                setValue(priWifiMaster, "gp_url",
                        PrefConst.KEY_PRI_WIFIMASTER_GP_URL, leoPreference);
                setValue(priWifiMaster, "img_url",
                        PrefConst.KEY_PRI_WIFIMASTER_IMG_URL, leoPreference);
                setValue(priWifiMaster, "type",
                        PrefConst.KEY_PRI_WIFIMASTER_TYPE, leoPreference);
                setValue(priWifiMaster, "url",
                        PrefConst.KEY_PRI_WIFIMASTER_URL, leoPreference);
                setValue(priWifiMaster, "title",
                        PrefConst.KEY_PRI_WIFIMASTER_TITLE, leoPreference);

            } else {
                setPriWifiMasterEmpty(leoPreference);
            }

            boolean isPriGradeNull = object.isNull(PrefConst.KEY_PRI_GRADE); // 判断key是否存在
            if (!isPriGradeNull) { // 隐私页评分
                JSONObject priGrade = object.getJSONObject(PrefConst.KEY_PRI_GRADE);
                setValue(priGrade, "content",
                        PrefConst.KEY_PRI_GRADE_CONTENT, leoPreference);
                setValue(priGrade, "img_url",
                        PrefConst.KEY_PRI_GRADE_IMG_URL, leoPreference);
                setValue(priGrade, "url",
                        PrefConst.KEY_PRI_GRADE_URL, leoPreference);
                setValue(priGrade, "title",
                        PrefConst.KEY_PRI_GRADE_TITLE, leoPreference);

            } else {
                setPriGradeEmpty(leoPreference);
            }

            boolean isPriFbNull = object.isNull(PrefConst.KEY_PRI_FB); // 判断key是否存在
            if (!isPriFbNull) { // 隐私页分享fb
                JSONObject priFb = object.getJSONObject(PrefConst.KEY_PRI_FB);
                setValue(priFb, "content",
                        PrefConst.KEY_PRI_FB_CONTENT, leoPreference);
                setValue(priFb, "img_url",
                        PrefConst.KEY_PRI_FB_IMG_URL, leoPreference);
                setValue(priFb, "url",
                        PrefConst.KEY_PRI_FB_URL, leoPreference);
                setValue(priFb, "title",
                        PrefConst.KEY_PRI_FB_TITLE, leoPreference);

            } else {
                setPriFbEmpty(leoPreference);
            }

            boolean isWifiSwiftyNull = object.isNull(
                    PrefConst.KEY_WIFI_SWIFTY); // 判断key是否存在
            if (!isWifiSwiftyNull) {  // wifi页Swifty
                JSONObject wifiSwifty = object.getJSONObject(PrefConst.KEY_WIFI_SWIFTY);
                setValue(wifiSwifty, "content",
                        PrefConst.KEY_WIFI_SWIFTY_CONTENT, leoPreference);
                setValue(wifiSwifty, "gp_url",
                        PrefConst.KEY_WIFI_SWIFTY_GP_URL, leoPreference);
                setValue(wifiSwifty, "img_url",
                        PrefConst.KEY_WIFI_SWIFTY_IMG_URL, leoPreference);
                setValue(wifiSwifty, "type",
                        PrefConst.KEY_WIFI_SWIFTY_TYPE, leoPreference);
                setValue(wifiSwifty, "url",
                        PrefConst.KEY_WIFI_SWIFTY_URL, leoPreference);
                setValue(wifiSwifty, "title",
                        PrefConst.KEY_WIFI_SWIFTY_TITLE, leoPreference);

            } else {
                setWifiSwiftyEmpty(leoPreference);
            }

            boolean isWifiWifiMasterNull = object.isNull(
                                            PrefConst.KEY_WIFI_WIFIMASTER); // 判断key是否存在
            if (!isWifiWifiMasterNull) {  // wifi页WifiMaster
                JSONObject wifiWifiMaster = object.getJSONObject(PrefConst.KEY_WIFI_WIFIMASTER);
                setValue(wifiWifiMaster, "content",
                        PrefConst.KEY_WIFI_WIFIMASTER_CONTENT, leoPreference);
                setValue(wifiWifiMaster, "gp_url",
                        PrefConst.KEY_WIFI_WIFIMASTER_GP_URL, leoPreference);
                setValue(wifiWifiMaster, "img_url",
                        PrefConst.KEY_WIFI_WIFIMASTER_IMG_URL, leoPreference);
                setValue(wifiWifiMaster, "type",
                        PrefConst.KEY_WIFI_WIFIMASTER_TYPE, leoPreference);
                setValue(wifiWifiMaster, "url",
                        PrefConst.KEY_WIFI_WIFIMASTER_URL, leoPreference);
                setValue(wifiWifiMaster, "title",
                        PrefConst.KEY_WIFI_WIFIMASTER_TITLE, leoPreference);

            } else {
                setWifiWifiMasterEmpty(leoPreference);
            }

            boolean isWifiGradeNull = object.isNull(PrefConst.KEY_WIFI_GRADE); // 判断key是否存在
            if (!isWifiGradeNull) { // wifi页评分
                JSONObject wifiGrade = object.getJSONObject(PrefConst.KEY_WIFI_GRADE);
                setValue(wifiGrade, "content",
                        PrefConst.KEY_WIFI_GRADE_CONTENT, leoPreference);
                setValue(wifiGrade, "img_url",
                        PrefConst.KEY_WIFI_GRADE_IMG_URL, leoPreference);
                setValue(wifiGrade, "url",
                        PrefConst.KEY_WIFI_GRADE_URL, leoPreference);
                setValue(wifiGrade, "title",
                        PrefConst.KEY_WIFI_GRADE_TITLE, leoPreference);

            } else {
                setWifiGradeEmpty(leoPreference);
            }

            boolean isWifiFbNull = object.isNull(PrefConst.KEY_WIFI_FB); // 判断key是否存在
            if (!isWifiFbNull) { //wifi页分享fb
                JSONObject wifiFb = object.getJSONObject(PrefConst.KEY_WIFI_FB);
                setValue(wifiFb, "content",
                        PrefConst.KEY_WIFI_FB_CONTENT, leoPreference);
                setValue(wifiFb, "img_url",
                        PrefConst.KEY_WIFI_FB_IMG_URL, leoPreference);
                setValue(wifiFb, "url",
                        PrefConst.KEY_WIFI_FB_URL, leoPreference);
                setValue(wifiFb, "title",
                        PrefConst.KEY_WIFI_FB_TITLE, leoPreference);

            } else {
                setWifiFbEmpty(leoPreference);
            }

            boolean isChargeSwiftyNull = object.isNull(
                    PrefConst.KEY_CHARGE_SWIFTY); // 判断key是否存在
            if (!isChargeSwiftyNull) {  // 充电屏保页swifty
                JSONObject chargeSwifty = object.getJSONObject(PrefConst.KEY_CHARGE_SWIFTY);
                setValue(chargeSwifty, "content",
                        PrefConst.KEY_CHARGE_SWIFTY_CONTENT, leoPreference);
                setValue(chargeSwifty, "gp_url",
                        PrefConst.KEY_CHARGE_SWIFTY_GP_URL, leoPreference);
                setValue(chargeSwifty, "img_url",
                        PrefConst.KEY_CHARGE_SWIFTY_IMG_URL, leoPreference);
                setValue(chargeSwifty, "type",
                        PrefConst.KEY_CHARGE_SWIFTY_TYPE, leoPreference);
                setValue(chargeSwifty, "url",
                        PrefConst.KEY_CHARGE_SWIFTY_URL, leoPreference);
                setValue(chargeSwifty, "title",
                        PrefConst.KEY_CHARGE_SWIFTY_TITLE, leoPreference);

            } else {
                setChargeSwiftyEmpty(leoPreference);
            }

            boolean isIntruderSwiftyNull = object.isNull(
                    PrefConst.KEY_INTRUDER_SWIFTY); // 判断key是否存在
            if (!isIntruderSwiftyNull) {  // 入侵者防护页swifty
                JSONObject intruderSwifty = object.getJSONObject(PrefConst.KEY_INTRUDER_SWIFTY);
                setValue(intruderSwifty, "content",
                        PrefConst.KEY_INTRUDER_SWIFTY_CONTENT, leoPreference);
                setValue(intruderSwifty, "gp_url",
                        PrefConst.KEY_INTRUDER_SWIFTY_GP_URL, leoPreference);
                setValue(intruderSwifty, "img_url",
                        PrefConst.KEY_INTRUDER_SWIFTY_IMG_URL, leoPreference);
                setValue(intruderSwifty, "type",
                        PrefConst.KEY_INTRUDER_SWIFTY_TYPE, leoPreference);
                setValue(intruderSwifty, "url",
                        PrefConst.KEY_INTRUDER_SWIFTY_URL, leoPreference);
                setValue(intruderSwifty, "title",
                        PrefConst.KEY_INTRUDER_SWIFTY_TITLE, leoPreference);

            } else {
                setIntruderSwiftyEmpty(leoPreference);
            }

            boolean isCleanSwiftyNull = object.isNull(
                    PrefConst.KEY_CLEAN_SWIFTY); // 判断key是否存在
            if (!isCleanSwiftyNull) {  // 耗电应用页swifty
                JSONObject cleanSwifty = object.getJSONObject(PrefConst.KEY_CLEAN_SWIFTY);
                setValue(cleanSwifty, "content",
                        PrefConst.KEY_CLEAN_SWIFTY_CONTENT, leoPreference);
                setValue(cleanSwifty, "gp_url",
                        PrefConst.KEY_CLEAN_SWIFTY_GP_URL, leoPreference);
                setValue(cleanSwifty, "img_url",
                        PrefConst.KEY_CLEAN_SWIFTY_IMG_URL, leoPreference);
                setValue(cleanSwifty, "type",
                        PrefConst.KEY_CLEAN_SWIFTY_TYPE, leoPreference);
                setValue(cleanSwifty, "url",
                        PrefConst.KEY_CLEAN_SWIFTY_URL, leoPreference);
                setValue(cleanSwifty, "title",
                        PrefConst.KEY_CLEAN_SWIFTY_TITLE, leoPreference);

            } else {
                setCleanSwiftyEmpty(leoPreference);
            }

            boolean isChargeExtraNull = object.isNull(
                    PrefConst.KEY_CHARGE_EXTRA); // 判断key是否存在
            if (!isChargeExtraNull) {  // 屏保页预留
                JSONObject chargeExtra = object.getJSONObject(PrefConst.KEY_CHARGE_EXTRA);
                setValue(chargeExtra, "content",
                        PrefConst.KEY_CHARGE_EXTRA_CONTENT, leoPreference);
                setValue(chargeExtra, "gp_url",
                        PrefConst.KEY_CHARGE_EXTRA_GP_URL, leoPreference);
                setValue(chargeExtra, "img_url",
                        PrefConst.KEY_CHARGE_EXTRA_IMG_URL, leoPreference);
                setValue(chargeExtra, "type",
                        PrefConst.KEY_CHARGE_EXTRA_TYPE, leoPreference);
                setValue(chargeExtra, "url",
                        PrefConst.KEY_CHARGE_EXTRA_URL, leoPreference);
                setValue(chargeExtra, "title",
                        PrefConst.KEY_CHARGE_EXTRA_TITLE, leoPreference);

            } else {
                setChargeExtraEmpty(leoPreference);
            }

            boolean isAppGradeNull = object.isNull(PrefConst.KEY_APP_GRADE);
            if (!isAppGradeNull) {
                JSONObject appGrade = object.getJSONObject(PrefConst.KEY_APP_GRADE);
                setValue(appGrade, "content", PrefConst.KEY_APP_GRADE_CONTENT, leoPreference);
                setValue(appGrade, "gp_url", PrefConst.KEY_APP_GRADE_URL, leoPreference);
            } else {
                setAppGradeEmpty(leoPreference);
            }

            boolean isPictureGradeNull = object.isNull(PrefConst.KEY_PICTURE_GRADE);
            if (!isPictureGradeNull) {
                JSONObject appGrade = object.getJSONObject(PrefConst.KEY_PICTURE_GRADE);
                setValue(appGrade, "content", PrefConst.KEY_PICTURE_GRADE_CONTENT, leoPreference);
                setValue(appGrade, "gp_url", PrefConst.KEY_PICTURE_GRADE_URL, leoPreference);
            } else {
                setPictureGradeEmpty(leoPreference);
            }

            boolean isVideoGradeNull = object.isNull(PrefConst.KEY_VIDEO_GRADE);
            if (!isVideoGradeNull) {
                JSONObject appGrade = object.getJSONObject(PrefConst.KEY_VIDEO_GRADE);
                setValue(appGrade, "content", PrefConst.KEY_VIDEO_GRADE_CONTENT, leoPreference);
                setValue(appGrade, "gp_url", PrefConst.KEY_VIDEO_GRADE_URL, leoPreference);
            } else {
                setVideoGradeEmpty(leoPreference);
            }

            boolean isGradeFbAnotherNull = object.isNull(PrefConst.KEY_GRADE_FB_ANOTHER);
            if (!isGradeFbAnotherNull) {
                leoPreference.putBoolean(PrefConst.KEY_GRADE_FB_ANOTHER, true);
            } else {
                setGradeFbAnotherEmpty(leoPreference);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /** 隐私页wifimaster数据置空 */
    private void setPriWifiMasterEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_PRI_WIFIMASTER_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_PRI_WIFIMASTER_GP_URL, "");
        leoPreference.putString(PrefConst.KEY_PRI_WIFIMASTER_IMG_URL, "");
        leoPreference.putString(PrefConst.KEY_PRI_WIFIMASTER_TYPE, "");
        leoPreference.putString(PrefConst.KEY_PRI_WIFIMASTER_URL, "");
        leoPreference.putString(PrefConst.KEY_PRI_WIFIMASTER_TITLE, "");
    }

    /** 隐私页评分数据置空 */
    private void setPriGradeEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_PRI_GRADE_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_PRI_GRADE_IMG_URL, "");
        leoPreference.putString(PrefConst.KEY_PRI_GRADE_URL, "");
        leoPreference.putString(PrefConst.KEY_PRI_GRADE_TITLE, "");
    }

    /** 隐私页分享fb数据置空 */
    private void setPriFbEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_PRI_FB_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_PRI_FB_IMG_URL, "");
        leoPreference.putString(PrefConst.KEY_PRI_FB_URL, "");
        leoPreference.putString(PrefConst.KEY_PRI_FB_TITLE, "");
    }

    /** wifi页wifimaster数据置空 */
    private void setWifiWifiMasterEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_WIFI_WIFIMASTER_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_WIFI_WIFIMASTER_GP_URL, "");
        leoPreference.putString(PrefConst.KEY_WIFI_WIFIMASTER_IMG_URL, "");
        leoPreference.putString(PrefConst.KEY_WIFI_WIFIMASTER_TYPE, "");
        leoPreference.putString(PrefConst.KEY_WIFI_WIFIMASTER_URL, "");
        leoPreference.putString(PrefConst.KEY_WIFI_WIFIMASTER_TITLE, "");
    }

    /** wifi页swifty数据置空 */
    private void setWifiSwiftyEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_WIFI_SWIFTY_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_WIFI_SWIFTY_GP_URL, "");
        leoPreference.putString(PrefConst.KEY_WIFI_SWIFTY_IMG_URL, "");
        leoPreference.putString(PrefConst.KEY_WIFI_SWIFTY_TYPE, "");
        leoPreference.putString(PrefConst.KEY_WIFI_SWIFTY_URL, "");
        leoPreference.putString(PrefConst.KEY_WIFI_SWIFTY_TITLE, "");
    }

    /** wifi页评分数据置空 */
    private void setWifiGradeEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_WIFI_GRADE_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_WIFI_GRADE_IMG_URL, "");
        leoPreference.putString(PrefConst.KEY_WIFI_GRADE_URL, "");
        leoPreference.putString(PrefConst.KEY_WIFI_GRADE_TITLE, "");
    }

    /** wifi页分享fb数据置空 */
    private void setWifiFbEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_WIFI_FB_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_WIFI_FB_IMG_URL, "");
        leoPreference.putString(PrefConst.KEY_WIFI_FB_URL, "");
        leoPreference.putString(PrefConst.KEY_WIFI_FB_TITLE, "");
    }

    /** 充电屏保swifty数据置空 */
    private void setChargeSwiftyEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_CHARGE_SWIFTY_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_CHARGE_SWIFTY_GP_URL, "");
        leoPreference.putString(PrefConst.KEY_CHARGE_SWIFTY_IMG_URL, "");
        leoPreference.putString(PrefConst.KEY_CHARGE_SWIFTY_TYPE, "");
        leoPreference.putString(PrefConst.KEY_CHARGE_SWIFTY_URL, "");
        leoPreference.putString(PrefConst.KEY_CHARGE_SWIFTY_TITLE, "");
    }

    /** 入侵者防护swifty数据置空 */
    private void setIntruderSwiftyEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_INTRUDER_SWIFTY_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_INTRUDER_SWIFTY_GP_URL, "");
        leoPreference.putString(PrefConst.KEY_INTRUDER_SWIFTY_IMG_URL, "");
        leoPreference.putString(PrefConst.KEY_INTRUDER_SWIFTY_TYPE, "");
        leoPreference.putString(PrefConst.KEY_INTRUDER_SWIFTY_URL, "");
        leoPreference.putString(PrefConst.KEY_INTRUDER_SWIFTY_TITLE, "");
    }

    /** 应用清理swifty数据置空 */
    private void setCleanSwiftyEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_CLEAN_SWIFTY_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_CLEAN_SWIFTY_GP_URL, "");
        leoPreference.putString(PrefConst.KEY_CLEAN_SWIFTY_IMG_URL, "");
        leoPreference.putString(PrefConst.KEY_CLEAN_SWIFTY_TYPE, "");
        leoPreference.putString(PrefConst.KEY_CLEAN_SWIFTY_URL, "");
        leoPreference.putString(PrefConst.KEY_CLEAN_SWIFTY_TITLE, "");
    }

    /** 充电屏保预留位数据置空 */
    private void setChargeExtraEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_CHARGE_EXTRA_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_CHARGE_EXTRA_GP_URL, "");
        leoPreference.putString(PrefConst.KEY_CHARGE_EXTRA_IMG_URL, "");
        leoPreference.putString(PrefConst.KEY_CHARGE_EXTRA_TYPE, "");
        leoPreference.putString(PrefConst.KEY_CHARGE_EXTRA_URL, "");
        leoPreference.putString(PrefConst.KEY_CHARGE_EXTRA_TITLE, "");
    }

    /** 应用锁评分弹窗文案置空 */
    private void setAppGradeEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_APP_GRADE_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_APP_GRADE_URL, "");
    }

    /** 图片评分弹窗文案置空 */
    private void setPictureGradeEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_PICTURE_GRADE_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_PICTURE_GRADE_URL, "");
    }

    /** 视频评分弹窗文案置空 */
    private void setVideoGradeEmpty(LeoPreference leoPreference) {
        leoPreference.putString(PrefConst.KEY_VIDEO_GRADE_CONTENT, "");
        leoPreference.putString(PrefConst.KEY_VIDEO_GRADE_URL, "");
    }

    /** 好评弹框反馈是否使用其他文案置空 */
    private void setGradeFbAnotherEmpty(LeoPreference leoPreference) {
        leoPreference.putBoolean(PrefConst.KEY_GRADE_FB_ANOTHER, false);
    }

}
