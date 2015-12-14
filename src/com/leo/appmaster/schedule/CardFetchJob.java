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
        PreferenceTable preferenceTable = PreferenceTable.getInstance();
        if (response == null) {
            LeoLog.i(TAG, "response: " + response);

            setPriWifiMasterEmpty(preferenceTable);
            setPriGradeEmpty(preferenceTable);
            setPriFbEmpty(preferenceTable);
            setWifiSwiftyEmpty(preferenceTable);
            setWifiWifiMasterEmpty(preferenceTable);
            setWifiGradeEmpty(preferenceTable);
            setWifiFbEmpty(preferenceTable);

            return;
        }

        JSONObject object = (JSONObject) response;

        try {
            boolean isPriWifiMasterNull = object.isNull(
                                          PrefConst.KEY_PRI_WIFIMASTER); // 判断key是否存在

            if (!isPriWifiMasterNull) {  // 隐私页wifimaster
                JSONObject priWifiMaster = object.getJSONObject(PrefConst.KEY_PRI_WIFIMASTER);
                setValue(priWifiMaster, "content",
                        PrefConst.KEY_PRI_WIFIMASTER_CONTENT, preferenceTable);
                setValue(priWifiMaster, "gp_url",
                        PrefConst.KEY_PRI_WIFIMASTER_GP_URL, preferenceTable);
                setValue(priWifiMaster, "img_url",
                        PrefConst.KEY_PRI_WIFIMASTER_IMG_URL, preferenceTable);
                setValue(priWifiMaster, "type",
                        PrefConst.KEY_PRI_WIFIMASTER_TYPE, preferenceTable);
                setValue(priWifiMaster, "url",
                        PrefConst.KEY_PRI_WIFIMASTER_URL, preferenceTable);
                setValue(priWifiMaster, "title",
                        PrefConst.KEY_PRI_WIFIMASTER_TITLE, preferenceTable);

            } else {
                setPriWifiMasterEmpty(preferenceTable);
            }

            boolean isPriGradeNull = object.isNull(PrefConst.KEY_PRI_GRADE); // 判断key是否存在
            if (!isPriGradeNull) { // 隐私页评分
                JSONObject priGrade = object.getJSONObject(PrefConst.KEY_PRI_GRADE);
                setValue(priGrade, "content",
                        PrefConst.KEY_PRI_GRADE_CONTENT, preferenceTable);
                setValue(priGrade, "img_url",
                        PrefConst.KEY_PRI_GRADE_IMG_URL, preferenceTable);
                setValue(priGrade, "url",
                        PrefConst.KEY_PRI_GRADE_URL, preferenceTable);
                setValue(priGrade, "title",
                        PrefConst.KEY_PRI_GRADE_TITLE, preferenceTable);

            } else {
                setPriGradeEmpty(preferenceTable);
            }

            boolean isPriFbNull = object.isNull(PrefConst.KEY_PRI_FB); // 判断key是否存在
            if (!isPriFbNull) { // 隐私页分享fb
                JSONObject priFb = object.getJSONObject(PrefConst.KEY_PRI_FB);
                setValue(priFb, "content",
                        PrefConst.KEY_PRI_FB_CONTENT, preferenceTable);
                setValue(priFb, "img_url",
                        PrefConst.KEY_PRI_FB_IMG_URL, preferenceTable);
                setValue(priFb, "url",
                        PrefConst.KEY_PRI_FB_URL, preferenceTable);
                setValue(priFb, "title",
                        PrefConst.KEY_PRI_FB_TITLE, preferenceTable);

            } else {
                setPriFbEmpty(preferenceTable);
            }

            boolean isWifiSwiftyNull = object.isNull(
                    PrefConst.KEY_WIFI_SWIFTY); // 判断key是否存在
            if (!isWifiSwiftyNull) {  // wifi页Swifty
                JSONObject wifiSwifty = object.getJSONObject(PrefConst.KEY_WIFI_SWIFTY);
                setValue(wifiSwifty, "content",
                        PrefConst.KEY_WIFI_SWIFTY_CONTENT, preferenceTable);
                setValue(wifiSwifty, "gp_url",
                        PrefConst.KEY_WIFI_SWIFTY_GP_URL, preferenceTable);
                setValue(wifiSwifty, "img_url",
                        PrefConst.KEY_WIFI_SWIFTY_IMG_URL, preferenceTable);
                setValue(wifiSwifty, "type",
                        PrefConst.KEY_WIFI_SWIFTY_TYPE, preferenceTable);
                setValue(wifiSwifty, "url",
                        PrefConst.KEY_WIFI_SWIFTY_URL, preferenceTable);
                setValue(wifiSwifty, "title",
                        PrefConst.KEY_WIFI_SWIFTY_TITLE, preferenceTable);

            } else {
                setWifiSwiftyEmpty(preferenceTable);
            }

            boolean isWifiWifiMasterNull = object.isNull(
                                            PrefConst.KEY_WIFI_WIFIMASTER); // 判断key是否存在
            if (!isWifiWifiMasterNull) {  // wifi页WifiMaster
                JSONObject wifiWifiMaster = object.getJSONObject(PrefConst.KEY_WIFI_WIFIMASTER);
                setValue(wifiWifiMaster, "content",
                        PrefConst.KEY_WIFI_WIFIMASTER_CONTENT, preferenceTable);
                setValue(wifiWifiMaster, "gp_url",
                        PrefConst.KEY_WIFI_WIFIMASTER_GP_URL, preferenceTable);
                setValue(wifiWifiMaster, "img_url",
                        PrefConst.KEY_WIFI_WIFIMASTER_IMG_URL, preferenceTable);
                setValue(wifiWifiMaster, "type",
                        PrefConst.KEY_WIFI_WIFIMASTER_TYPE, preferenceTable);
                setValue(wifiWifiMaster, "url",
                        PrefConst.KEY_WIFI_WIFIMASTER_URL, preferenceTable);
                setValue(wifiWifiMaster, "title",
                        PrefConst.KEY_WIFI_WIFIMASTER_TITLE, preferenceTable);

            } else {
                setWifiWifiMasterEmpty(preferenceTable);
            }

            boolean isWifiGradeNull = object.isNull(PrefConst.KEY_WIFI_GRADE); // 判断key是否存在
            if (!isWifiGradeNull) { // wifi页评分
                JSONObject wifiGrade = object.getJSONObject(PrefConst.KEY_WIFI_GRADE);
                setValue(wifiGrade, "content",
                        PrefConst.KEY_WIFI_GRADE_CONTENT, preferenceTable);
                setValue(wifiGrade, "img_url",
                        PrefConst.KEY_WIFI_GRADE_IMG_URL, preferenceTable);
                setValue(wifiGrade, "url",
                        PrefConst.KEY_WIFI_GRADE_URL, preferenceTable);
                setValue(wifiGrade, "title",
                        PrefConst.KEY_WIFI_GRADE_TITLE, preferenceTable);

            } else {
                setWifiGradeEmpty(preferenceTable);
            }

            boolean isWifiFbNull = object.isNull(PrefConst.KEY_WIFI_FB); // 判断key是否存在
            if (!isWifiFbNull) { //wifi页分享fb
                JSONObject wifiFb = object.getJSONObject(PrefConst.KEY_WIFI_FB);
                setValue(wifiFb, "content",
                        PrefConst.KEY_WIFI_FB_CONTENT, preferenceTable);
                setValue(wifiFb, "img_url",
                        PrefConst.KEY_WIFI_FB_IMG_URL, preferenceTable);
                setValue(wifiFb, "url",
                        PrefConst.KEY_WIFI_FB_URL, preferenceTable);
                setValue(wifiFb, "title",
                        PrefConst.KEY_WIFI_FB_TITLE, preferenceTable);

            } else {
                setWifiFbEmpty(preferenceTable);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /** 隐私页wifimaster数据置空 */
    private void setPriWifiMasterEmpty(PreferenceTable preferenceTable) {
        preferenceTable.putString(PrefConst.KEY_PRI_WIFIMASTER_CONTENT, "");
        preferenceTable.putString(PrefConst.KEY_PRI_WIFIMASTER_GP_URL, "");
        preferenceTable.putString(PrefConst.KEY_PRI_WIFIMASTER_IMG_URL, "");
        preferenceTable.putString(PrefConst.KEY_PRI_WIFIMASTER_TYPE, "");
        preferenceTable.putString(PrefConst.KEY_PRI_WIFIMASTER_URL, "");
        preferenceTable.putString(PrefConst.KEY_PRI_WIFIMASTER_TITLE, "");
    }

    /** 隐私页评分数据置空 */
    private void setPriGradeEmpty(PreferenceTable preferenceTable) {
        preferenceTable.putString(PrefConst.KEY_PRI_GRADE_CONTENT, "");
        preferenceTable.putString(PrefConst.KEY_PRI_GRADE_IMG_URL, "");
        preferenceTable.putString(PrefConst.KEY_PRI_GRADE_URL, "");
        preferenceTable.putString(PrefConst.KEY_PRI_GRADE_TITLE, "");
    }

    /** 隐私页分享fb数据置空 */
    private void setPriFbEmpty(PreferenceTable preferenceTable) {
        preferenceTable.putString(PrefConst.KEY_PRI_FB_CONTENT, "");
        preferenceTable.putString(PrefConst.KEY_PRI_FB_IMG_URL, "");
        preferenceTable.putString(PrefConst.KEY_PRI_FB_URL, "");
        preferenceTable.putString(PrefConst.KEY_PRI_FB_TITLE, "");
    }

    /** wifi页wifimaster数据置空 */
    private void setWifiWifiMasterEmpty(PreferenceTable preferenceTable) {
        preferenceTable.putString(PrefConst.KEY_WIFI_WIFIMASTER_CONTENT, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_WIFIMASTER_GP_URL, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_WIFIMASTER_IMG_URL, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_WIFIMASTER_TYPE, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_WIFIMASTER_URL, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_WIFIMASTER_TITLE, "");
    }

    /** wifi页swifty数据置空 */
    private void setWifiSwiftyEmpty(PreferenceTable preferenceTable) {
        preferenceTable.putString(PrefConst.KEY_WIFI_SWIFTY_CONTENT, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_SWIFTY_GP_URL, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_SWIFTY_IMG_URL, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_SWIFTY_TYPE, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_SWIFTY_URL, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_SWIFTY_TITLE, "");
    }

    /** wifi页评分数据置空 */
    private void setWifiGradeEmpty(PreferenceTable preferenceTable) {
        preferenceTable.putString(PrefConst.KEY_WIFI_GRADE_CONTENT, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_GRADE_IMG_URL, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_GRADE_URL, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_GRADE_TITLE, "");
    }

    /** wifi页分享fb数据置空 */
    private void setWifiFbEmpty(PreferenceTable preferenceTable) {
        preferenceTable.putString(PrefConst.KEY_WIFI_FB_CONTENT, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_FB_IMG_URL, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_FB_URL, "");
        preferenceTable.putString(PrefConst.KEY_WIFI_FB_TITLE, "");
    }

}
