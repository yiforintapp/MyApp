package com.zlf.appmaster.client;

import android.content.Context;

import com.zlf.appmaster.model.sync.SyncBaseBean;
import com.zlf.appmaster.model.sync.SyncRequest;
import com.zlf.appmaster.model.sync.SyncResponse;
import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.utils.StringUtil;
import com.zlf.appmaster.utils.UrlConstants;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncClient {
	
	private static final String TAG = "SyncClient";
		
	private Context mContext;
	private static SyncClient mInstance = null;

    private SyncClient(Context context) {
        mContext = context;
    }
	public static SyncClient getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SyncClient(context);
		}
		return mInstance;
	}


	// 同步行情相关基本数据接口
    private static final String PATH_HQ_SYNC = UrlConstants.RequestHQURLString + "/QiNiuApp/core/resource.sync.do?";

    // 同步个人相关的信息接口
    private static final String PATH_PERSONAL_SYNC = UrlConstants.RequsetBaseURLString + "/QiNiuApp/core/personal.sync.do?";

    // 请求他人的同步信息
    private static final String PATH_OTHERS_SYNC_INFO = UrlConstants.RequsetBaseURLString + "/QiNiuApp/core/resource.others.do?";

    /**
     * “同步”数据（请求为异步方式）
     * @param url
     * @param request
     * @param requestFinished
     */
    private void requestBaseSyncA(String url, final SyncRequest request, final OnRequestListener requestFinished){
        QLog.i(TAG,"Sync Request Str:"+request);

        Map<String, String> params = new HashMap<String, String>();
        params.put("param.data", request.toString());


        UniversalRequest.addHeader(mContext, params);

        final RequestParams requestParams = new RequestParams(params);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, requestParams, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int arg0, Header[] header, byte[] data) {
                // TODO Auto-generated method stub

                byte[] upZip = StringUtil.unGZip(data);
                if (upZip != null) {
                    try {
                        String jsonStr = new String(upZip, "utf-8");
                        QLog.i(TAG, "uzip data:" + jsonStr);
                        SyncResponse responseFactory = new SyncResponse(mContext, request.getCommands());
                        responseFactory.resolveJSONData(new JSONObject(jsonStr));

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (requestFinished != null) {
                    requestFinished.onDataFinish(data);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                arg3.printStackTrace();

                if (requestFinished != null) {
                    requestFinished.onError(arg0, "onFailure");
                }
            }
        });
    }
    public void requestSyncA(final SyncRequest request,  final OnRequestListener requestFinished){
        int commands = request.getCommands();
        String url = PATH_PERSONAL_SYNC;
        boolean isSyncBase = false;
        if ((commands & SyncBaseBean.ALL_BASE_COMMANDS) > 0) {      // 基本数据相关接口
            url = PATH_HQ_SYNC;
            isSyncBase = true;
        }

        if (isSyncBase && (commands & SyncBaseBean.ALL_PERSONAL_COMMANDS) > 0) {
            // 两种命令字不能同时同步
            QLog.e(TAG, "两种不同类型的数据不能单次同步");
            return;
        }


        requestBaseSyncA(url, request, requestFinished);
    }

    /**
     * 请求他人的同步数据信息
     * @param keyType
     * @param othersUin
     */
    public void requestOthersSyncData(int keyType, long othersUin, final OnRequestListener requestFinished){
        Map<String, String> param = new HashMap<String, String>();
        param.put("param.key", String.valueOf(keyType));
        param.put("param.fuin", String.valueOf(othersUin));

        UniversalRequest.requestUrl(mContext, PATH_OTHERS_SYNC_INFO, param, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                // TODO Auto-generated method stub
                if(null != requestFinished)
                    requestFinished.onError(errorCode, errorString);
            }

            @Override
            public void onDataFinish(Object object) {
                // TODO Auto-generated method stub

                List<String> localUrlArray = new ArrayList<String>();
                try {
                    JSONObject json = (JSONObject) object;
                    JSONArray data = json.getJSONArray("data");
                    int len = data.length();
                    for (int i = 0; i<len; i++){
                        localUrlArray.add(data.getString(i));;
                    }

                }catch (JSONException e){

                }



                //QLog.i(TAG,"data===========================:"+data);

                if(null != requestFinished)
                    requestFinished.onDataFinish(localUrlArray);

            }
        });
    }

	
}
