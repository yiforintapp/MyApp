package com.zlf.appmaster.model.sync;

import android.content.Context;
import android.text.TextUtils;

import com.zlf.appmaster.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class SyncHeader {
    private String mSessionID;      // 会话ID
    private long mUin;
	private String mDevID;			// imei
    private String mDevType;        // 设备类型
	private int mVersion;		// 版本号
	private JSONObject mJSONObject = null;	// 本类对应的JSON对象
		
	public SyncHeader(){
        mSessionID = "";
		mDevID = "";
        mDevType = "";
		mVersion = 0;
        mUin = 0;
	}
	
	public SyncHeader(Context context){
        mSessionID = Utils.getSessionID(context);
        String uin = Utils.getAccountUin(context);
        if (!TextUtils.isEmpty(uin)){
            mUin = Long.valueOf(uin);
        }
		mDevID = Utils.getIMEI(context);
		mDevType =  Utils.getDevName();
        mVersion = Utils.getVersion(context);
	}

	public String getDev() {
		return mDevID;
	}
	public void setDev(String mDev) {
		this.mDevID = mDev;
	}
	public int getVersion() {
		return mVersion;
	}
	public void setVersion(int mVersion) {
		this.mVersion = mVersion;
	}
	
	
	public JSONObject getJSONObject(){
		if(mJSONObject == null){
			mJSONObject = toJSONObject();
		}
		return mJSONObject;
	}
	
	private JSONObject toJSONObject(){
		mJSONObject = new JSONObject();
		try {
            mJSONObject.accumulate("Session", mSessionID);
            mJSONObject.accumulate("Uin", mUin);
            mJSONObject.accumulate("Version", 0);//先写死0 version
			mJSONObject.accumulate("DeviceId", mDevID);
            mJSONObject.accumulate("Device",  mDevType);
			mJSONObject.accumulate("Scene", 0); // 场景（预留）

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mJSONObject;
	}
}
