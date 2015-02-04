
package com.leo.appmaster.sdk.push;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import com.leo.appmaster.sdk.SDKWrapper;

public class LeoPollAckReq extends BasePushHttpReq {

    private ArrayList<BasicNameValuePair> mData;

    public LeoPollAckReq(ArrayList<BasicNameValuePair> data) {
        mData = data;
        mURL = "http://" + SDKWrapper.getBestServerDomain() + "/appactivity/activity/pullack";
    }

    public void onReqSuccessed(String response) {
        /* this need not further process */
    }

    public void onReqFailed(int code, String response) {
        /* this need not further process */
    }

    @Override
    public ArrayList<BasicNameValuePair> getKVData() {
        return mData;
    }

}
