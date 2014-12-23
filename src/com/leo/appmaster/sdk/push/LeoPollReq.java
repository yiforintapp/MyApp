
package com.leo.appmaster.sdk.push;

import java.net.URLDecoder;
import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.push.PushManager;

public class LeoPollReq extends BasePushHttpReq {

    private final static String TAG = "PollReq";
    
    private ArrayList<BasicNameValuePair> mData;

    private OnPushListener mListener;

    public LeoPollReq(ArrayList<BasicNameValuePair> data) {
        mData = data;
//        mURL = "http://apia.leomaster.com/appactivity/activity/pull";
         mURL = "puuuulllll"; // TODO: a test URL
    }

    public void setOnPushListener(OnPushListener l) {
        mListener = l;
    }

    @Override
    public void onReqSuccessed(String encodedResponse) {
        String response = URLDecoder.decode(encodedResponse);
        try {
            JSONObject jsonObj = new JSONObject(response);
            int code = jsonObj.getInt("error_code");
            if (code != 0) {
                LeoLog.e(TAG, "error code=" + code + "; erro msg=" + jsonObj.getString("error_msg"));
                return;
            }
            String title = getJSStrValue(jsonObj, "msg_title", "");
            String content = getJSStrValue(jsonObj, "msg_content", "");
            String id = getJSStrValue(jsonObj, "msg_id", "");
            int showType = getJSIntValue(jsonObj, "msg_show_type", PushManager.SHOW_DIALOG_FIRST);
            LeoLog.d(TAG, "id=" + id + "title=" + title + "; content=" + content);
            if (mListener != null) {
                mListener.onPush(id, title, content, showType);
            }
        } catch (JSONException e) {
            LeoLog.e(TAG, "JSONException in PushReq: " + e.getMessage());
        }
    }

    @Override
    public void onReqFailed(int code, String response) {
        LeoLog.d(TAG, "onReqFailed in PushReq");
        // TODO: debug here
        LeoLog.e(TAG,
                "Warning! this debug section should not activate in release version!");
        /* activity 1 */
        if (mListener != null) {
            mListener.onPush("act00001", "祝三哥新年快乐",
                    "请写祝福语给你朋友送10卢比话费～～～～～",
                    PushManager.SHOW_DIALOG_FIRST);
        }
//        try {
//            Thread.sleep(15*1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        /* activity 2 */
//        if (mListener != null) {
//            mListener.onPush("msg10000", "恭喜您5000000美元",
//                    "现在带上AK47前往中国银行领取",
//                    PushManager.SHOW_STATUSBAR_ONLY);
//        }
    }

    @Override
    public ArrayList<BasicNameValuePair> getKVData() {
        return mData;
    }
}
