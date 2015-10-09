
package com.leo.appmaster.sdk.push;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.leo.appmaster.utils.LeoLog;

public abstract class BasePushHttpReq implements Runnable {

    protected String mURL = "default";

    public String getURL() {
        return mURL;
    }
    
    public String getJSStrValue(JSONObject object, String name, String def) {
        try {
            return object.getString(name);
        } catch (Exception e) {
            return def;
        }
    }
    
    public int getJSIntValue(JSONObject object, String name, int def) {
        try {
            return object.getInt(name);
        } catch (Exception e) {
            return def;
        }
    }

    public abstract ArrayList<BasicNameValuePair> getKVData();

    public abstract void onReqSuccessed(String response);

    public abstract void onReqFailed(int code, String response);

    @Override
    public void run() {
        LeoLog.d("_stone_", "run called, ready to send request");
        PushNetwork.sendHttpReq(this);
    }
}
