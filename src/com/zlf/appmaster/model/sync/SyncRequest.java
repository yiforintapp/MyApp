package com.zlf.appmaster.model.sync;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 组装请求命令
 * @author Deping Huang
 *
 */
public class SyncRequest {


    private SyncHeader mHeader;
	private List<SyncBaseBean> mBodyBeanArray = null;
    private HashMap<Integer, Object> mOperatorIDs = new HashMap<Integer, Object>();
	private JSONObject mJSONRequest;
    private int mCommands;

    /**
     * 同步请求
     * @param context
     * @param commands 需要同步的模块，不填则同步所有模块
     */
	public SyncRequest(Context context, int commands){
		
		// 设置头信息
		setHeader(new SyncHeader(context));

        if (commands == 0){
            commands = SyncBaseBean.ALL_BASE_COMMANDS;
        }
        mCommands = commands;

        for (int index = 0 ; index < 32; index ++) {
            int command = 1 << index;
            if ((commands & command) > 0){      // 查找添加的command
                addBean(SyncFactory.newBean(context, command));
            }
        }

        init();
	}

	private SyncHeader getHeader() {
		return mHeader;
	}

	private void setHeader(SyncHeader header) {
		this.mHeader = header;
	}

    private void addBean(SyncBaseBean baseBody){
		if(mBodyBeanArray == null){
			mBodyBeanArray = new ArrayList<SyncBaseBean>();
		}
		mBodyBeanArray.add(baseBody);
	}
	
	private JSONObject init(){
		mJSONRequest = new JSONObject();
		try {
            /*  -- 屏蔽不需要设置消息
			// 设置header
			mJSONRequest.accumulate("Header", mHeader.getJSONObject());
			
			// 设置body
			JSONObject bodyJson = new JSONObject();
            bodyJson.accumulate("Selector", mCommands);

            JSONArray alertArray = new JSONArray();
			for(SyncBaseBean bean: mBodyBeanArray){
                alertArray.put(bean.getJSONValue());
			}
            bodyJson.accumulate("Alert", alertArray);

            bodyJson.accumulate("OperatorCount", 0);    // 占位

            bodyJson.accumulate("Operator", new JSONObject());
			
			mJSONRequest.accumulate("Body", bodyJson);*/


            mJSONRequest.accumulate("selector", mCommands);

            JSONArray alertArray = new JSONArray();
            for(SyncBaseBean bean: mBodyBeanArray){
                alertArray.put(bean.getJSONValue());
            }
            mJSONRequest.accumulate("alert", alertArray);

            mJSONRequest.accumulate("operator_count", 0);    // 占位

            mJSONRequest.accumulate("operator", new JSONObject());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mJSONRequest;
	}

    @Override
    public String toString() {
        if(null != mJSONRequest){
            return mJSONRequest.toString();
        }
        return "";
    }

    public int getCommands(){
        return mCommands;
    }

    /**
     * 设置操作的类型
     * @param operatorIDs
     * @return
     */
    public SyncRequest addOperator(HashMap<Integer, Object> operatorIDs){
        if (null != operatorIDs){
            mOperatorIDs.putAll(operatorIDs);

            // 更改JSON中的数量
            if (null == mJSONRequest) {
                try {
                    int operatorCount = mJSONRequest.getInt("operator_count");
                    mJSONRequest.put("operator_count", operatorCount + operatorIDs.size());

                } catch (JSONException e) {

                }
            }
        }

        return  this;
    }


    /**
     *
     * @param operatorID 操作ID，见SyncOperator中的定义
     * @param params 该操作对应的自定义参数，该参数回调传回相应的工厂类，可为空
     * @return
     */
    public SyncRequest addOperator(int operatorID, Object params) {
        mOperatorIDs.put(operatorID, params);

        // 更改JSON中的数量
        if (null != mJSONRequest) {
            try {
                int operatorCount = mJSONRequest.getInt("operator_count");
                mJSONRequest.put("operator_count", operatorCount + 1);

            } catch (JSONException e) {

            }

        }

        return this;
    }
    public SyncRequest addOperator(int operatorID){

        return addOperator(operatorID, null);
    }


    /**
     * 确认提交完成请求
     */
    public SyncRequest commit(){
        if (null != mJSONRequest) {
            try {
                JSONObject operatorJSON = mJSONRequest.getJSONObject("operator");
                for (SyncBaseBean bean : mBodyBeanArray) {
                    bean.accumulateOperator(operatorJSON, mOperatorIDs);
                }
            }catch (JSONException e){

            }
        }
        return  this;
    }




}
