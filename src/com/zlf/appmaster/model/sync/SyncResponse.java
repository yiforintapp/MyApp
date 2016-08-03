package com.zlf.appmaster.model.sync;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SyncResponse {

    /**
     * 服务器回应的删除标记  1为新增 2为删除
     */
    public final static int DEL_FlAG_NEW = 1;
    public final static int DEL_FlAG_DELETE = 2;


    private final static String TAG = SyncResponse.class.getSimpleName();
	private SyncHeader mSyncHeader;
	
	private Context mContext;

    private int mCommands;

    /**
     * 同步请求应答
     * @param context
     * @param commands  解析一个或多个同步的模块，不填则解析所有模块
     */
	public SyncResponse(Context context, int commands){
		mContext = context;

        if (commands == 0){
            commands = SyncBaseBean.ALL_BASE_COMMANDS;
        }

        mCommands = commands;
	}
	
	
	public void resolveJSONData(JSONObject jsonObject) throws JSONException {
        // 错误码
        int retCode = jsonObject.getInt("code");
        if (retCode == 0){      // 返回成功
            JSONObject data = jsonObject.getJSONObject("data");
            int selector = data.getInt("selector");
            JSONArray newAlert = data.getJSONArray("new_alert");
            int newAlertLen = newAlert.length();
            Map<Integer, Long> targetArray= new HashMap<Integer, Long>();
            for (int i = 0; i < newAlertLen; i++) {
               JSONObject jsonNewAlert = newAlert.getJSONObject(i);
                targetArray.put(jsonNewAlert.getInt("target"),jsonNewAlert.getLong("ncp"));
            }

            for (int index = 0 ; index < 32; index ++) {
                int command = 1 << index;
                if ((selector & command) > 0){      // 查找添加的command
                    try{
                        // 各模块分别进行解析,分别try catch，保证一个模块未同步成功的情况下不会影响其它模块
                        SyncFactory.newBean(mContext,command).setJSONValue(data, targetArray);
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

        }
        else{
            String msg = jsonObject.getString("msg");// 错误描述
            Log.e(TAG, "同步出错："+ msg);

        }
	}
	
}
