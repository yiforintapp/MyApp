package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.db.MsgCenterTable;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by forint on 15-11-18.
 */
public class SwiftyFetchJob extends FetchScheduleJob {


    @Override
    protected void work() {

        Context ctx = AppMasterApplication.getInstance();

        FetchScheduleListener listener = newJsonArrayListener();
        HttpRequestAgent.getInstance(ctx).loadSwiftySecurity(listener, listener);
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
        LeoLog.i("loadSwiftySecurity", "onFetchFail");
    }

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
        LeoLog.i("loadSwiftySecurity", "response: ");
        if (response == null) {

            return;
        }

        Context ctx = AppMasterApplication.getInstance();

        JSONObject object = (JSONObject) response;

        PreferenceTable preferenceTable = PreferenceTable.getInstance();
        try {
            if (object.getString("content") != null && object.getString("content").length() > 0) {
                String content = object.getString("content");
                preferenceTable.putString(PrefConst.KEY_SWIFTY_CONTENT, content);
            }
            LeoLog.i("loadSwiftySecurity", "content: " + object.getString("content"));
            if (object.getString("gp_url") != null && object.getString("gp_url").length() > 0) {
                String gpUrl = object.getString("gp_url");
                preferenceTable.putString(PrefConst.KEY_SWIFTY_GP_URL, gpUrl);
            }
            LeoLog.i("loadSwiftySecurity", "gp_url: " + object.getString("gp_url"));
            if (object.getString("img_url") != null && object.getString("img_url").length() > 0) {
                String imgUrl = object.getString("img_url");
                preferenceTable.putString(PrefConst.KEY_SWIFTY_IMG_URL, imgUrl);
            }

            if (object.getString("title") != null && object.getString("title").length() > 0) {
                String title = object.getString("title");
                preferenceTable.putString(PrefConst.KEY_SWIFTY_TITLE, title);
            }

            if (object.getString("type") != null && object.getString("type").length() > 0) {
                String type = object.getString("type");
                preferenceTable.putString(PrefConst.KEY_SWIFTY_TYPE, type);
            }

            if (object.getString("url") != null && object.getString("url").length() > 0) {
                String url = object.getString("url");
                preferenceTable.putString(PrefConst.KEY_SWIFTY_URL, url);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
