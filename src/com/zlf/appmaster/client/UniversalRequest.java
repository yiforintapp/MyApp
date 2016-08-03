package com.zlf.appmaster.client;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.zlf.appmaster.R;
import com.zlf.appmaster.utils.Encrypt;
import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.utils.QToast;
import com.zlf.appmaster.utils.UrlConstants;
import com.zlf.appmaster.utils.UserUtils;
import com.zlf.appmaster.utils.Utils;
import com.zlf.appmaster.utils.VolleyTool;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

//import com.iqiniu.qiniu.ui.EntryActivity;

public class UniversalRequest {
	private static final String TAG = "UniversalRequest";
	public static final String REQUEST_URL_BASE_APP = UrlConstants.RequsetBaseURLString+"/QiNiuApp/";
	public static final String REQUEST_URL_BASE = UrlConstants.RequsetBaseURLString+"/QiNiuApp/core";
	public static final String REQUEST_PARAM_FORM_TYPE = "param.clientType";
	public static final String REQUEST_FORM_TYPE = REQUEST_PARAM_FORM_TYPE+"=android";

	//最大的超时时间
	private static final int MAX_TIME_OUT = 30000;//30s


	
	/**
	 * 添加头部及加密 不含sid 
	 * @param context
	 * @param param
	 */
	public static void addHeaderWithoutSessionId(Context context, Map<String, String> param, boolean isAddClientId) {
		param.put("header.version", String.valueOf(Utils.getVersion(context)));
		param.put("header.os", Utils.getDevName());
		param.put("header.imei", Utils.getIMEI(context));
		long uin = Utils.getAccountLongUin(context);
		if (uin == 0) {
			uin = Utils.getGuestUin(context);
		}
		param.put("header.uin", String.valueOf(uin));
		//用于识别每一条请求
		if (isAddClientId) {
			param.put("param.ClientId", String.valueOf(Utils.getClientID()));
		}
		addSecurity(param);
	}

	public static void addHeaderWithoutSessionId(Context context, Map<String, String> param) {
		addHeaderWithoutSessionId(context, param, true);
	}



	public static void addHeader(Context context, Map<String, String> param, boolean isAddClientId){
		//..
		String sessionString =  Utils.getSessionID(context);
		if (!TextUtils.isEmpty(sessionString)) {
			param.put("header.sid", sessionString);
		}
		addHeaderWithoutSessionId(context,param,isAddClientId);
	}

	
	// 含 sid
	public static void addHeader(Context context, Map<String, String> param) {
		addHeader(context,param,true);
	}
	
	private static void addSecurity(Map<String, String> param){

		String sortString = sortMap(param);
		
		//再加上sid
		if (param.containsKey("header.sid")) {
			sortString += param.get("header.sid");	
		}
		
		//获得 md5加密后的字串
		String security = Encrypt.md5(sortString);
		param.put("securityCode", security);
		
//		QLog.i(TAG, "sort:"+sortString+",security:"+security);
	}
	
	
	/**
	 * 添加设备类型
	 */
	public static void addClientType(Map<String, String> param) {
		param.put("param.clientType", "android");
	}
	
	
	/**
	 * 通用请求，不需要加头信息 不带SessionID 超时时间为2500ms
	 * @param context
	 * @param url
	 * @param requestFinished
	 */
	public static void requestUrlWithoutSessionId(final Context context,
												  String url, Map<String, String> param, final OnRequestListener requestFinished) {
		requestUrlWithoutSessionId(context, url, param, requestFinished,false);
	}
	
	/**
	 * 通用请求，不需要加头信息 不带SessionID 
	 * @param context
	 * @param url
	 * @param param
	 * @param requestFinished
	 * @param isBackgroud 是否后台请求
	 */
	public static void requestUrlWithoutSessionId(final Context context,
												  String url, Map<String, String> param, final OnRequestListener requestFinished, boolean isBackgroud) {
		if (param == null || param.size()==0) {
			param = new HashMap<String, String>();
		}
		
		addHeaderWithoutSessionId(context, param);
		//拼接到 url后
		url += getMapString(param);
		
		requestUrlWithTimeOut(UniversalRequest.class.getSimpleName(),context,url, requestFinished,
					true,MAX_TIME_OUT,isBackgroud);//MAX_TIME_OUT 才超时，且不重发
	}
	
	/**
	 * 通用后台请求
	 * @param context
	 * @param url
	 * @param param
	 * @param requestFinished
	 */
	public static void requestBackground(final Context context,
										 String url, Map<String, String> param, final OnRequestListener requestFinished) {
		
		if (param == null || param.size()==0) {
			param = new HashMap<String, String>();
		}
		
		addHeader(context, param);
		//拼接到 url后
		url += getMapString(param);
		
		requestUrlWithTimeOut(UniversalRequest.class.getSimpleName(),context,url, requestFinished,
				false,0,true);
	}
	
	/**
	 * 通用请求，不需要加头信息
	 * @param context
	 * @param url
	 * @param requestFinished
	 */
	public static void requestUrl(final Context context,
								  String url, Map<String, String> param, final OnRequestListener requestFinished) {
		
		if (param == null || param.size()==0) {
			param = new HashMap<String, String>();
		}
		
		addHeader(context, param);
		//拼接到 url后
		url += getMapString(param);
		requestUrl(UniversalRequest.class.getSimpleName(),
				context,url,requestFinished);
	}
	
	/**
	 * 将map参数转成String参数 并转一次utf8
	 * @param param
	 * @return
	 */
	public static String getMapString(Map<String, String> param) {
		//拼接到 url后
		StringBuffer stringBuffer = new StringBuffer();
		Set<String> keys = param.keySet();
		int i=0;
		
		for (String key:keys) {
			if (i > 0) {
				stringBuffer.append("&");
			}
			//进行一次utf8
			String value = chineseEncode(param.get(key));
			stringBuffer.append(key+"="+value);
			i++;
		}
		return stringBuffer.toString();
	}
	
	
	private static class StringCompartor implements Comparator<String> {

		@Override
		public int compare(String lhs, String rhs) {
			return lhs.toLowerCase(Locale.getDefault()).compareTo(rhs.toLowerCase(Locale.getDefault()));
		}
		
	}
	
	private static String sortMap(Map<String, String> map){
		Set<String> keys = map.keySet();
		List<String> list = new ArrayList<String>(keys);
		Collections.sort(list, new StringCompartor());
		
		//排序之后的key
		TreeSet<String> sortKeys = new TreeSet<String>(list);
		StringBuffer sb = new StringBuffer();
		for(String key :sortKeys ){
			  sb.append(key);
			  sb.append(map.get(key));
		}
        return sb.toString();
	}
	

	/**
	 * 通用处理错误响应
	 * @param mContext
	 * @param error
	 * @param requestFinished
	 * @param isBackground 是否后台请求
	 */
	public static void handleErrorResponse(Context mContext, VolleyError error, OnRequestListener requestFinished, boolean isBackground) {
		error.printStackTrace();
		if (requestFinished != null) {//网络错误
			requestFinished
					.onError(UrlConstants.CODE_CONNECT_ERROR,error.toString());
		}
		
		if (isBackground) {
			return;
		}
		
		if (error instanceof TimeoutError) {
			QToast.show(mContext, R.string.network_timeout, Toast.LENGTH_SHORT);
		}
		else {
			QToast.show(mContext, R.string.network_server_busy, Toast.LENGTH_SHORT);
		}
	}
	
	/**
	 * 通用处理响应
	 * @param mContext
	 * @param response
	 * @param requestFinished
	 * @param isBackground 是否后台请求
	 */
	public static void handleResponse(Context mContext, JSONObject response, OnRequestListener requestFinished, boolean isBackground) {
		try {
			int errorCode = response.getInt("code");
			if (errorCode == UrlConstants.CODE_CORRECT) {// Check错误码
				QLog.i(TAG, "Success");
				if (requestFinished != null)
					requestFinished.onDataFinish(response);
			}else if(errorCode == UrlConstants.CODE_STOCK_NO_MORE_DATA){
				if (requestFinished != null)
					requestFinished.onError(UrlConstants.CODE_STOCK_NO_MORE_DATA,response.toString());
			}else if (isBackground) {//后台请求 不处理错误
				if (requestFinished != null) {//服务器返回错误
					requestFinished.onError(UrlConstants.CODE_SERVER_ERROR,response.toString());
				}
			}else if (errorCode == UrlConstants.CODE_NOT_EXIST ||
					errorCode == UrlConstants.CODE_EXIST) {
				if (requestFinished != null) {//不存在 交给具体的类去处理
					requestFinished
							.onError(errorCode,response.toString());
				}
			} else if (errorCode == UrlConstants.CODE_NOLOGIN
					|| errorCode == UrlConstants.CODE_LOGINNOAUTH
					|| errorCode == UrlConstants.CODE_LOGINOUTTIME) {
                QToast.showShortTime(mContext, "登录超时，请重新登录");
                QLog.e(TAG, "登录超时，请重新登录");
                //清除本地登录信息
                UserUtils.clearUserLoginInfo(mContext);
                //进入登陆界面
//                Intent intent = new Intent(mContext, EntryActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                mContext.startActivity(intent);
//重启
//                Utils.restartApplication(mContext,StartUpActivity.class.getName());
			} else {
					// 错误提示
					UrlConstants.showUrlErrorCode(mContext,
							errorCode,response.getString("msg"));
					
					QLog.e(TAG, "Error:" + response);

					if (requestFinished != null) {//服务器返回错误
						requestFinished
								.onError(errorCode,response.toString());
					}
			}
				
		} catch (JSONException e) {
			e.printStackTrace();
			
			if (requestFinished != null) {//解析错误
				requestFinished
						.onError(UrlConstants.CODE_JSON_ANALYSIS_ERROR,e.toString());
			}
		}
	}
	
	/**
	 * 可设置超时的请求 非后台请求
	 * @param tag
	 * @param mContext
	 * @param url
	 * @param requestFinished
	 * @param isSetTimeOut
	 * @param timeOut
	 */
	public static void requestUrlWithTimeOut(String tag, final Context mContext, String url, final OnRequestListener requestFinished,
											 Boolean isSetTimeOut, int timeOut) {
		requestUrlWithTimeOut(tag, mContext, url, requestFinished, isSetTimeOut, timeOut,false);
	}
	
	/**
	 * 可设置是否为后台请求
	 * @param tag
	 * @param mContext
	 * @param url
	 * @param requestFinished
	 * @param isSetTimeOut
	 * @param timeOut
	 * @param isBackground
	 */
	public static void requestUrlWithTimeOut(String tag, final Context mContext, String url, final OnRequestListener requestFinished,
											 Boolean isSetTimeOut, int timeOut, final boolean isBackground) {
		QLog.i(TAG, url); // for test
		
		if (Utils.GetNetWorkStatus(mContext) == false) {//无网络的处理
			QToast.showShortTime(mContext, R.string.network_unconnected);
			if (requestFinished != null && !isBackground) {
				requestFinished.onError(UrlConstants.CODE_CONNECT_ERROR, 
						mContext.getResources().getString(R.string.network_server_busy));
			}
			return;
		}

		QJsonObjectRequest jsonObjectRequest = new QJsonObjectRequest(
				Request.Method.GET, url, null, new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response, boolean noMidify) {
						handleResponse(mContext,response,requestFinished,isBackground);	
					}

				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						handleErrorResponse(mContext,error,requestFinished,isBackground);
						error.printStackTrace();
					}

				},isSetTimeOut,timeOut);
		jsonObjectRequest.setTag(tag);// 设置tag
																	// callAll的时候使用
		VolleyTool.getInstance(mContext).getRequestQueue()
				.add(jsonObjectRequest);
	}
	
	/**
	 * 通用请求 需要完整的url（包含头信息）使用默认超时时间
	 * @param tag
	 * @param mContext
	 * @param url
	 * @param requestFinished
	 */
	public static void requestUrl(String tag, final Context mContext, String url, final OnRequestListener requestFinished) {
		requestUrlWithTimeOut(tag,mContext,url,requestFinished,false,0);
	}	
	
	
	//中文编码一次utf-8
	public static String chineseEncode(String str) {
		if (TextUtils.isEmpty(str)){
			QLog.e(TAG,"chineseEncode str 为null");
			return "";
		}

		String utf8String=null;
//		String utf8String2=null;
		try {
			utf8String = URLEncoder.encode(str, "utf-8");
//			utf8String2 = URLEncoder.encode(utf8String, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		QLog.i(TAG, "原码：" + str);
//		QLog.i(TAG, "第一次编码："+utf8String);
//		QLog.q("第二次编码："+utf8String2);
		return utf8String;
	}
	
	
	

    /**
     * Converts <code>params</code> into an application/x-www-form-urlencoded encoded string.
     */
    public static String encodeParameters(Map<String, String> params) {
    	 String paramsEncoding = "UTF-8";
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
            	encodedParams.append('&');
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }


    /**
     * 通用AsyncHttpClient 文件post请求
     * @param mContext
     * @param url
     * @param params
     * @param requestFinished
     */
    public static void syncPost(final Context mContext, final String url, final Map<String, String> params,
								final OnRequestListener requestFinished){
        if (Utils.GetNetWorkStatus(mContext) == false) {//无网络的处理
            QToast.showShortTime(mContext, R.string.network_unconnected);
            if (requestFinished != null) {
                requestFinished.onError(UrlConstants.CODE_CONNECT_ERROR,
                        mContext.getResources().getString(R.string.network_server_busy));
            }
            return;
        }

//        String paraString="";
//        for (String key:params.keySet()){
//            paraString += key +":"+params.get(key)+",";
//        }
        QLog.i(TAG,"syncPost:"+url+",param:"+params);


        UniversalRequest.addHeader(mContext, params);
        final RequestParams requestParams = new RequestParams(params);

        AsyncHttpClient client =new AsyncHttpClient();
        client.post(url,requestParams,new FileAsyncHttpResponseHandler(mContext) {
            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                final int errorCode = i;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (requestFinished != null) {
                            requestFinished.onError(errorCode,"onFailure");
                        }
                    }
                }).start();

            }

            @Override
            public void onSuccess(int i, Header[] headers, File file) {
                QLog.i(TAG,"onSuccess");
                final File resultFile = file;
                final int errorCode = i;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (requestFinished != null) {
                            try {
                                FileInputStream fis = new FileInputStream(resultFile);
                                if (fis.available() > 1024)
                                    QLog.i(TAG,"文件大小："+fis.available()/1024+"k");
                                else {
                                    QLog.i(TAG,"文件大小："+fis.available()+"字节");
                                }
                                byte[] b=new byte[fis.available()];//新建一个字节数组
                                fis.read(b);//将文件中的内容读取到字节数组中
                                fis.close();

                                requestFinished.onDataFinish(b);
                                return;
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            requestFinished.onError(errorCode,"onFailure");

                        }
                    }
                }).start();

            }
        });
    }
}
