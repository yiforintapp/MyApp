package com.zlf.appmaster.utils;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.zlf.appmaster.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Url相关的常量定义
 * @author Deping Huang
 *
 */
public class UrlConstants {
	/**
	 * 网络环境控制
	 * 
	 * 内网正式0、
	 * 外网正式（1）1、
     * 外网正式（2）8、
	 * 测试找不到主机5、
	 * 测试超时6、
	 * 外网正式Temp7、指定具体外网机器
	 * 老谢、默认
	 */
	public static final int DEBUG_FLAG = 1;//

	/**
	 * 系统错误
	 */
	public static final int  CODE_SYSTEM 		= -9999;
	/**
	 * 与服务器通信错误码定义
	 */
	public static final int  CODE_CORRECT 		= 0;//0			// 请求返回正常
	/**
	 * 参数错误.
	 */
	public static final int  CODE_PARAM 		= -1;
	/**
	 * 未登录,无权限
	 */
	public static final int  CODE_NOLOGIN 		= -2;
	/**
	 * 登录超时
	 */
	public static final int  CODE_LOGINOUTTIME 	= -3;

	/**
	 * 在进行唯一操作时，标示已存在错误
	 */
	public static final int  CODE_EXIST 		= -8;
	/**
	 * 在进行唯一性删除时，表示不存在错误
	 */
	public static final int  CODE_NOT_EXIST 	= -9;

	/**
	 * 通用错误码：进行集合操作时，部分成功
	 */
	public static int ERROR_PART_OK = -12;

	/**
	 * 通用错误码：拒绝访问
	 */
	public static int ERROR_DENIDE_ACCESS = -13;

	/**
	 * 通用错误码：不能频繁操作(如：手机找回密码一天不能超过5次)
	 */
	public static int ERROR_FREQ_LIMITED = -14;

	/**
	 * 通用错误码：非测试号码，不允许操作
	 */
	public static int ERROR_ALPHA_OPERATOR = -15;
	/**
	 * 通用错误码：不存在
	 */
	public static int ERROR_NOT_EXIST = -16;

	/**
	 * 通用错误码：通知用户进行重试
	 */
	public static int ERROR_CMD_REPEAT = -18;

	/**
	 * 以下部分未验证是否有效
	 * **************************************************************************
	 */
	public static final int  CODE_STOCK_NO_MORE_DATA = 302;			// 没有更多数据
	public static final int  CODE_200 = 200;
	public static final int  CODE_404 = 404;
	public static final int  CODE_LOGINNOAUTH 	= 502;			// 已经登录,无权限
	public static final int  CODE_PHONEREGISTER	= 520;			//"当前手机号已被注册"
	public static final int  CODE_STOCK_LIMIT_UP = 120;				// 涨停
	public static final int  CODE_STOCK_NO_MONEY = 1600;			// 金额不足
	public static final int  CODE_STOCK_OVERFLOW = 1601;			// 超过最大可卖股数
	public static final int  CODE_STOCK_CLOSE = 1602;				// 休市
		/**
		 * 组合逻辑处理错误：股票停牌
		 */
		public static int ERROR_TRADING_HALT = -300;

		/**
		 * 组合逻辑处理错误：股票临时停牌
		 */
		public static int ERROR_TARDING_TEMPORARY_HALT = -301;

		/**
		 * 组合逻辑处理错误：股票涨停
		 */
		public static int ERROR_LIMIT_UP = -301;

		/**
		 * 组合逻辑处理错误：股票跌停
		 */
		public static int ERROR_LIMIT_DOWN = -302;

		/**
		 * 组合逻辑处理错误：非交易时间段
		 */
		public static int ERROR_NOT_TRADING_TIME = -302;
	/**
	 * **************************************************************************
	 */


	/**
	 * 本地错误码定义
	 */
	public static final int CODE_SERVER_ERROR = 5000;			// 服务器返回错误
	public static final int CODE_JSON_ANALYSIS_ERROR = 5001;	// json解析错误
	public static final int CODE_CONNECT_ERROR = 5002;	// 网络访问错误
	
	/**
	 *  im命令字
	 */	
	public final static String CONECT_HEART_BEAN_REQ	 = "00000001";		//心跳请求消息
	public final static String CONECT_HEART_BEAN_ACK	 = "10000001";		//心跳回应消息
	public final static String CONECT_BINDING_REQ		 = "00000010";		//绑定请求消息
	public final static String CONECT_BINDING_ACK		 = "10000010";		//绑定回应消息
	public final static String CONECT_UNBUNDING_REQ 	 = "00000011";		//解除绑定请求消息
	public final static String CONECT_UNBUNDING_ACK 	 = "10000011";		//解除绑定回应消息
	public final static String CONECT_SEND_CHAT_MSG_REQ  = "00000100";		//发送聊天消息请求消息
	public final static String CONECT_SEND_CHAT_MSG_ACK  = "10000100";		//发送聊天消息回应消息
	public final static String CONECT_SEND_OFFLINE_REQ 	 = "00000101";		//发送下线请求消息
	public final static String CONECT_SEND_OFFLINE_ACK   = "10000101";		//发送下线回应消息
	public final static String CONECT_PUSH_CHAT_MSG_REQ  = "00000110";		//Push聊天消息请求
	public final static String CONECT_PUSH_CHAT_MSG_ACK  = "10000110";		//Push聊天消息回应
	public final static String CONECT_FRIENDS_REQUEST_MSG_REQ = "00000111";	//添加好友请求
	public final static String CONECT_NEW_FRIENDS_MSG_REQ = "00001000";		//有新的联系人加入到了系统
	public final static String CONECT_SHARE_NEWS_MSG_REG = "00001010";		//分享 ---- 新闻
	public final static String CONECT_SHARE_QIUBAI_MSG_REQ = "00001001";	//分享 ---- 求败
	
	/**
	 * 用户信息域
	 */
	public final static String USER_UIN = "uin";		//用户ID
	public final static String USER_MOBILE = "mobile";	//手机号
	public final static String USER_EMAIL = "email";	//邮箱
	public final static String USER_NAME = "nickName"; 		//名称
	public final static String USER_SEX = "sex";
	public final static String USER_ROLE = "role";
	public final static String USER_HEADURL = "headUrl";
	public final static String USER_LEVEL = "level";

	public final static String USER_ADDRESS = "address";
	public final static String USER_PROVINCE = "province";
	public final static String USER_CITY = "city";
	
	public final static String USER_BIRTHDAY = "birthday";
	public final static String USER_STOCKAGE = "stock_age";
	public final static String USER_JOB = "job";
	public final static String USER_AUTH = "isAuth";
	public final static String USER_HONOUR = "honour";
	public final static String USER_JOBCODE = "jobCode";
	public final static String USER_PWD = "pwd";
	public final static String USER_SECOND_PWD = "secondPwd";
	public final static String USER_CARD = "card";
	public final static String USER_TOKEN = "token";
	public final static String USER_REGIST_TYPE = "registType";
	public final static String USER_CREATE_TIME = "createTime";
	public final static String USER_LAST_MODIFY_TIME = "lastModifyTime";
	public final static String USER_LAST_TIME = "lastTime";
	public final static String USER_SIGNATURE = "signature";			//用户签名
	public final static String USER_RESUME = "resume";			//个人简介

	public static final String USER_PARNTER_PHONE = "1";  //手机号登陆
	public static final String USER_PARNTER_IMEI = "2";   //IMEI号登陆(服务端使用)
	public final static String USER_PARNTER_QQ = "3";
	public final static String USER_PARNTER_WECHAT = "4";
	public final static String USER_PARNTER_WEIBO = "5";
	
	public final static String SEX_STRING[]={"","男","女"};
	
	public final static int REFRESH_CYCLE_DEFAULT = 10000; //定时刷新周期 10s
	
	// 通讯连接地址
	public static final String RequsetBaseURLString;
	public static final String RequestHQURLString;	// 行情链路
	public static final String IMSocketHost;
	public static final int IMSocketPort; 

	public static final String RequestLiveURLString ;
	public static final String RequestLiveAudienceURLString;
	public static final String RequestPlaybackURLString ;
	
	static {
		switch(DEBUG_FLAG){
		case 0:		// 内网正式环境
			RequestLiveURLString ="rtmp://192.168.0.152:1935/live/";
			RequestPlaybackURLString ="rtmp://192.168.0.151:1935/vod/";
			RequestLiveAudienceURLString = RequestLiveURLString;
			RequsetBaseURLString = "http://fengniu.f3322.net:8080";
			RequestHQURLString = RequsetBaseURLString;
			IMSocketHost = "192.168.0.174";
			IMSocketPort = 58081;
			break;
		case 1:		// 外网正式环境1
			RequestLiveURLString ="rtmp://anchors2.fengniuzhibo.com:1935/live/";
			RequestPlaybackURLString ="rtmp://192.168.0.151:1935/vod/";
			RequestLiveAudienceURLString = "rtmp://audience2.fengniuzhibo.com:1935/live/";
			RequsetBaseURLString = "http://short2.fengniuzhibo.com:58080";
			RequestHQURLString = "http://hqshort.fengniuzhibo.com:58080";
			IMSocketHost = "long2.fengniuzhibo.com";
			IMSocketPort = 58081;
			//关闭打印信息
			QLog.setLogFlag(true);
			break;
		case 2:		// 外网预测试环境
			RequestLiveURLString ="rtmp://qiniu.ddns.net:51935/live/";
			RequestPlaybackURLString ="rtmp://192.168.0.151:1935/vod/";
			RequestLiveAudienceURLString = RequestLiveURLString;
			RequsetBaseURLString = "http://qiniu.ddns.net:58080";
			RequestHQURLString = RequsetBaseURLString;
			IMSocketHost = "qiniu.ddns.net";
			IMSocketPort = 58081;
			break;

		case 4://测试找不到主机
			RequestLiveURLString ="rtmp://short.119q1i2n3iu:51935/live/";
			RequestPlaybackURLString ="rtmp://192.168.0.151:1935/vod/";
			RequestLiveAudienceURLString = RequestLiveURLString;
			RequsetBaseURLString = "http://short.119q1i2n3iu.com.cn:48080";
			RequestHQURLString = RequsetBaseURLString;
			IMSocketHost = "http://short.119q1i2n3iu.com.cn";
			IMSocketPort = 58081;
			break;
			
		case 5:	//测试超时
			RequestLiveURLString ="rtmp://192.168.0.255:51935/live/";
			RequestPlaybackURLString ="rtmp://192.168.0.151:1935/vod/";
			RequestLiveAudienceURLString = RequestLiveURLString;
			RequsetBaseURLString = "http://192.168.0.255:8080";
			RequestHQURLString = RequsetBaseURLString;
			IMSocketHost = "192.168.0.255";
			IMSocketPort = 58081;
			break;

		case 10:    // 蒋海波
			RequestLiveURLString ="rtmp://192.168.0.153:51935/live/";
			RequestPlaybackURLString ="rtmp://192.168.0.151:1935/vod/";
			RequestLiveAudienceURLString = RequestLiveURLString;
			RequsetBaseURLString = "http://192.168.0.153:8080";
			RequestHQURLString = RequsetBaseURLString;
			IMSocketHost = "192.168.0.153";
			IMSocketPort = 58081;
			break;
		default:	// 老谢机器
			RequestLiveURLString ="rtmp://192.168.0.152:1935/live/";
			RequestPlaybackURLString ="rtmp://192.168.0.151:1935/vod/";
			RequestLiveAudienceURLString = RequestLiveURLString;
			RequsetBaseURLString = "http://192.168.0.153:8080";
			RequestHQURLString = RequsetBaseURLString;
			IMSocketHost = "192.168.0.174";
			IMSocketPort = 58081;
			//QLog.setLogFlag(false);
			break;
		
		}
	}
	
	//协议页面
	public static final String RequestProtocolURLString = RequsetBaseURLString+"/QiNiuApp/html/protocol.html";
	
	// 骑牛公告
	public static final String RequestQiniuAnnouncementURLString = RequsetBaseURLString+"/QiNiuApp/html/gaoyonghushu.html";
	//组合信息
	public static final String RequestCombinationInfoURLString = RequsetBaseURLString+"/QiNiuApp/html/minijijinreadme.html";

    // 测试UI
    public static final String RequestStrategyIntroduction = RequsetBaseURLString+"/QiNiuApp/clnote/";
	
	public static void showUrlErrorCode(Context context, int errorCode, String errorString){

		String errorPrompt = context.getResources().getString(R.string.network_server_busy);

		if (errorString != null && errorString.length() > 1) {
//			Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show();
//			return;
			//解析服务器返回的错误信息
			errorPrompt = "错误(" + errorCode + ")：" + errorString;
			QLog.e("showUrlErrorCode", errorString);
		}
		
		switch(errorCode){
			case CODE_SERVER_ERROR:
				if(!TextUtils.isEmpty(errorString))
					errorPrompt = errorString;
				break;
			case CODE_JSON_ANALYSIS_ERROR:
				errorPrompt = context.getResources().getString(R.string.network_analysis_error);
				break;
			case CODE_CONNECT_ERROR:
				errorPrompt = context.getResources().getString(R.string.network_error);
				break;
				
			case CODE_NOT_EXIST:
				if (TextUtils.isEmpty(errorString)) {
					errorPrompt = context.getResources().getString(R.string.lr_error_notexist);
				}
				else {
					errorPrompt = errorString;	
				}
				
				break;
				
			case CODE_PHONEREGISTER:
				errorPrompt = context.getResources().getString(R.string.lr_error_phoneregister);
				break;
			case CODE_200:
			case CODE_404:
				errorPrompt = context.getResources().getString(R.string.network_server_busy);
				break;
		}
		if (Looper.myLooper() == null) {
			Looper.prepare();
		}
		Toast.makeText(context, errorPrompt, Toast.LENGTH_SHORT).show();
		if (Looper.myLooper() == null) {
			Looper.loop();
		}
	}
	public static void showUrlErrorCode(Context context, int errorcode){
		showUrlErrorCode(context, errorcode, "");
	}
	/**
	 * 获取服务器错误提示
	 * @return
	 */
	public static String getServerErrorString(String errorStr){
		int errorCode = 0;
		
		// 解析错误码
		try {
			JSONObject errorJson = new JSONObject(errorStr);
			errorCode = errorJson.getInt("code");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// 返回提示
		String errorString = "服务器繁忙"+"("+errorCode+")，"+"请稍后再试";
		switch(errorCode){
		case CODE_SYSTEM:
			errorString = "系统错误";
			break;
		case CODE_PARAM:
			errorString = "参数错误";
			break;
		case CODE_STOCK_LIMIT_UP:
			errorString = "涨停不能买";
			break;
		case CODE_STOCK_NO_MONEY:
			errorString = "金额不足";
			break;
		case CODE_STOCK_OVERFLOW:
			errorString = "超过最大可卖股数";
			break;
		case CODE_STOCK_CLOSE:
			errorString = "休市中";
			break;
		}
		
		
		return errorString;
	}
	
	
	public static int getRefreshCycle(){
		// 以后可能要从用户设置中读取
		return REFRESH_CYCLE_DEFAULT;
	}
}
