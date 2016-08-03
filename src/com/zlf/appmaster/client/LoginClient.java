package com.zlf.appmaster.client;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.zlf.appmaster.utils.Encrypt;
import com.zlf.appmaster.R;
import com.zlf.appmaster.bean.Account;
import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.utils.QToast;
import com.zlf.appmaster.utils.UrlConstants;
import com.zlf.appmaster.utils.UserUtils;
import com.zlf.appmaster.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;


/**
 * 客户端请求 -- 登录
 * 
 * @author Deping Huang
 * 
 */
public class LoginClient {


	/**
	 * 用户逻辑处理错误：用户账号或密码错误.
	 */
	public static final int ERROR_ACCT_PWD = -100;

	/**
	 * 用户逻辑处理错误：用户已注册
	 */
	public static final int ERROR_USER_REG = -101;

	/**
	 * 用户逻辑处理错误：用户未注册
	 */
	public static final int ERROR_USER_UNREG = -102;

	/**
	 *  用户逻辑处理错误：手机验证码错误
	 */
	public static final int ERROR_TICK = -103;

	/**
	 * 用户逻辑处理错误：用户昵称已存在
	 */
	public static final int ERROR_NICK_NAME_EXIST = -104;

	/**
	 * 用户逻辑处理错误：用户昵称包含敏感词汇
	 */
	public static final int ERROR_NICK_NAME_SPAM = -105;

	/**
	 * 用户逻辑处理错误：用户被冻结
	 */
	public static final int ERROR_FREEZE = -106;

	/**
	 * 用户逻辑处理错误：QQ注册时，从腾讯获取QQ信息异常.
	 */
	public static final int ERROR_GETUSERINFO_4QQ = -151;

	/**
	 * 用户逻辑处理错误：新浪微博注册时，从新浪获取用户信息异常
	 */
	public static final int ERROR_GETUSERINFO_4SINA = -152;

	/**
	 * 用户逻辑处理错误：微信注册时，从微信获取用户信息异常
	 */
	public static final int ERROR_GETUSERINFO_4WX = -152;

    /* op_mode */
    public static final int OP_MODE_PHONE = 1;
    public static final int OP_MODE_IMEI = 2;
    public static final int OP_MODE_QQ = 3;
    public static final int OP_MODE_WECHAT = 4;
    public static final int OP_MODE_SINA = 5;
    public static final int OP_MODE_MAIL = 6;

    /* op_code*/
    /**
     * 根据手机号码获取验证码
     */
    public static int OPERATOR_VERIFY = 1;

    /**
     * 检查是否已绑定
     */
    public static int OPERATOR_CHECK = 2;

    /**
     * 根据手机号码修改密码
     */
    public static int OPERATOR_UPDATE_PWD = 3;

    /**
     * 绑定第三方账户
     */
    public static int OPERATOR_BIND = 4;

    /**
     * 解绑第三方账户
     */
    public static int OPERATOR_UN_BIND = 5;

    /**
     * 跟新第三方账户access_token
     */
    public static int OPERATOR_UPDATE_TOKEN = 6;


	private static LoginClient mInstance = null;
	private static final String TAG = "Client.Login";
	private Context mContext;

	private Account mAccount;
	private HashMap<String,String> param;
	
	// 单例模式
	private LoginClient(Context context) {
		mContext = context;
	}

	public static LoginClient getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new LoginClient(context);
		}
		return mInstance;
	}
	
	public void clearAccountInfo() {
		mAccount = new Account();
	}

	public Account getAccountInfo() {
		if (mAccount == null) // 登录未成功的情况，待处理。。。
			mAccount = new Account();
		return mAccount;
	}


	// 登录 密码已经是MD5加密过的
	public void requestLoginWithMd5(String phone, String pwdMD5,
									final OnRequestListener requestListener) {
		
		String url = String.format("%s/QiNiuApp/core/user.auth.do?", UrlConstants.RequsetBaseURLString);
		
		param = new HashMap<String, String>();
		param.put("param.auth_mode", UrlConstants.USER_PARNTER_PHONE);
		param.put("param.token", phone);
		param.put("param.pwd", pwdMD5);

		UniversalRequest.addClientType(param);

		
		UniversalRequest.requestUrlWithoutSessionId(mContext, url, param, new OnRequestListener() {
			
			@Override
			public void onError(int errorCode, String errorString) {
				//已处理
				requestListener.onError(errorCode, errorString);
			}
			
			@Override
			public void onDataFinish(Object object) {
				JSONObject response = (JSONObject) object;
				try {
					int errorcode = response.getInt("code");
					if (errorcode == UrlConstants.CODE_CORRECT) {// Check错误码

						mAccount = Account.resolveJsonObject(response);
						mAccount.saveAccountToSetting(mContext,UrlConstants.USER_PARNTER_PHONE);

						if (requestListener != null)
							requestListener.onDataFinish(mAccount);
					} else {
						QLog.e(TAG, "Login Error:" + response);
						UrlConstants.showUrlErrorCode(mContext,
								errorcode,response.getString("msg"));
						
						if (requestListener != null) {
							requestListener
									.onError(UrlConstants.CODE_CONNECT_ERROR,response.toString());
						}
					}

				} catch (JSONException e) {
					e.printStackTrace();
					
					if (requestListener != null) {//解析错误
						requestListener
								.onError(UrlConstants.CODE_JSON_ANALYSIS_ERROR,e.toString());
					}
					QToast.show(mContext, R.string.network_server_busy, Toast.LENGTH_SHORT);
				}
			}
		});
	}

	// 登录
	public void requestLogin(String name, String pwd,
							 final OnRequestListener requestListener) {
		pwd = Encrypt.md5(pwd);
		requestLoginWithMd5(name, pwd, requestListener);
	}

	// 请求获取验证码
	public void requestGetCode(String phone,
			final OnRequestListener requestListener) {
		String url = String
				.format("%s/QiNiuApp/core/user.op.do?",
						UrlConstants.RequsetBaseURLString);
		
		param = new HashMap<String, String>();
		param.put("param.mobile", phone);
		param.put("param.op_code", String.valueOf(OPERATOR_VERIFY));
		param.put("param.op_mode", String.valueOf(OP_MODE_PHONE));
        UniversalRequest.requestUrlWithoutSessionId(mContext, url, param, new OnRequestListener() {
			@Override
			public void onDataFinish(Object object) {
				if (requestListener != null)
					requestListener.onDataFinish(object);
			}

			@Override
			public void onError(int errorCode, String errorString) {

				int retCode = 0;
				try {
					JSONObject response = new JSONObject(errorString);
					retCode = response.optInt("code");
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				if (retCode == 200) {
					QToast.showShortTime(mContext, R.string.network_get_code_too_frequency);
				}

				if (requestListener != null)
					requestListener.onError(errorCode, errorString);
			}
		});
	}

	// 验证随机验证码
	public void requestCheckCode(String name, String randString,
								 final OnRequestListener requestListener) {
		String url = String
				.format("%s/QiNiuApp/core/random.checkRandom.do?",
						UrlConstants.RequsetBaseURLString);
		
		param = new HashMap<String ,String>();
		param.put("param.mobile", name);
		param.put("param.random", randString);
		UniversalRequest.requestUrlWithoutSessionId(mContext, url, param, requestListener);
	}

	// 根据电话号码修改密码
	public void requestChangePwd(String phone, String pwdString, String code,
								 final OnRequestListener requestListener) {
		pwdString = Encrypt.md5(pwdString);
		String url = String
				.format("%s/QiNiuApp/core/user.op.do?",
						UrlConstants.RequsetBaseURLString);
		
		param = new HashMap<String ,String>();
		param.put("param.mobile", phone);
		param.put("param.pwd", pwdString);
		param.put("param.op_code", String.valueOf(OPERATOR_UPDATE_PWD));
		param.put("param.tick", code);
		param.put("param.op_mode", String.valueOf(OP_MODE_PHONE));
		UniversalRequest.requestUrlWithoutSessionId(mContext, url, param, requestListener);
	}
	
	//获取其他用户的用户名与头像路径
	public void requestOtherUserNameAndHeadImg(String uin,
			final OnRequestListener requestListener) {
		String url = String
				.format("%s/QiNiuApp/core/user.getOtherUserFields.do?",UrlConstants.RequsetBaseURLString);
		String fields = String.format("%s,%s,%s,%s", UrlConstants.USER_NAME,UrlConstants.USER_HEADURL,
				UrlConstants.USER_SEX,UrlConstants.USER_LEVEL);
		
		param = new HashMap<String, String>();
		param.put("param.fuin", uin);
		param.put("param.fields", fields);
		UniversalRequest.requestUrl(mContext, url, param, requestListener);
	}
	

	// 请求 获取用户信息的基本项
	public void requestInfo(final OnRequestListener requestListener) {
		String url = String
				.format("%s/QiNiuApp/core/user.getProfile.do?", UrlConstants.RequsetBaseURLString);

		param = new HashMap<String ,String>();
		param.put("param.head_uin", Utils.getAccountUin(mContext));
		UniversalRequest.requestUrl(mContext, url, param, requestListener);
	}

	//修改用户信息
	public void requestEdit(
			String nameString,
			String signString,
			String resumeString,
			final OnRequestListener requestListener) {
		
		//先utf8一次
		nameString = UniversalRequest.chineseEncode(nameString);
		signString = UniversalRequest.chineseEncode(signString);
		resumeString = UniversalRequest.chineseEncode(resumeString);

		String url = String
				.format("%s/QiNiuApp/core/user.update.do?",UrlConstants.RequsetBaseURLString);
		
		param = new HashMap<String ,String>();
		param.put("param.nick_name", nameString);
		param.put("param."+UrlConstants.USER_SIGNATURE, signString);
		param.put("param."+UrlConstants.USER_RESUME, resumeString);

		UniversalRequest.requestUrl(mContext, url, param, requestListener);
	}
	

	
	//注册
	public void requestRegister(
			String name, String sex, String phone, String pwd, String code,
			final OnRequestListener requestListener) {
		name = UniversalRequest.chineseEncode(name);
		pwd = Encrypt.md5(pwd);
		
		String url = String.format("%s/QiNiuApp/core/user.reg.do?",
						UrlConstants.RequsetBaseURLString);
		
		param = new HashMap<String ,String>();
		param.put("param.mobile", phone);
		param.put("param.nick_name", name);
		param.put("param.sex", sex);
		param.put("param.pwd", pwd);
		param.put("param.tick", code);
		param.put("param.reg_mode","1");

		UniversalRequest.addClientType(param);
		UniversalRequest.requestUrlWithoutSessionId(mContext, url, param, requestListener);
	}

	//上传图像
	public void postHeadImg(String imageString, final OnRequestListener requestListener) {
		String url = UrlConstants.RequsetBaseURLString +
				"/QiNiuApp/core/user.update.do?";

		param = new HashMap<String ,String>();
		param.put("param.head_uin",Utils.getAccountUin(mContext));
		param.put("param.head_img", imageString);
		param.put("param.album", String.valueOf(1));//相册参数为1
		param.put(UniversalRequest.REQUEST_PARAM_FORM_TYPE, "android");

		StringPostRequest.request(url, mContext, requestListener, param);
	}

	//上传图像
	public void postHeadImg(final Bitmap bitmap, final OnRequestListener requestListener) {
		String url = UrlConstants.RequsetBaseURLString +
				"/QiNiuApp/core/user.update.do?";

		param = new HashMap<String ,String>();

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
		String imageString = byte2String(bao.toByteArray());
		param.put("param.head_uin",Utils.getAccountUin(mContext));
		param.put("param.head_img", imageString);
		param.put("param.album", String.valueOf(1));//相册参数为1
		param.put(UniversalRequest.REQUEST_PARAM_FORM_TYPE, "android");

		StringPostRequest.request(url, mContext, requestListener, param);
	}
	
	//上传图像
	public void postAlbumImg(final Bitmap bitmap, final OnRequestListener requestListener) {

		String url = UrlConstants.RequsetBaseURLString +
				"/QiNiuApp/core/head.up_user.do?";
		
		param = new HashMap<String ,String>();
		
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
    	String imageString = byte2String(bao.toByteArray());

		param.put("param.head_uin",Utils.getAccountUin(mContext));
    	param.put("param.img", imageString);
		param.put("param.album", String.valueOf(1));//相册参数为1
        param.put(UniversalRequest.REQUEST_PARAM_FORM_TYPE, "android");
        
        StringPostRequest.request(url, mContext, requestListener, param);
	}

	//删除图像
	public void deleteAlbumImg(String imageString, final OnRequestListener requestListener) {


		String url = UrlConstants.RequsetBaseURLString +
				"/QiNiuApp/core/head.del8img.do?";

		param = new HashMap<String ,String>();
		param.put("param.head_uin",Utils.getAccountUin(mContext));
		param.put("param.img", imageString);
		param.put("param.album", String.valueOf(1));//相册参数为1
		param.put(UniversalRequest.REQUEST_PARAM_FORM_TYPE, "android");

		StringPostRequest.request(url, mContext, requestListener, param);
	}
	
	public static String byte2String(byte[] bs) {
		if(bs == null || bs.length == 0){
			return null;
		}
		try {
			return new String(bs,"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			QLog.q("将byte数组["+bs+"]转换成字符串异常..."+e.toString());
			return null;
		}
	}	
	
	public static byte[] string2Byte(String str) {
		if(str==null || str.length() == 0){
			return null;
		}
		try {
			return str.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			QLog.q("将字符串["+str+"]转换成byte数组异常..."+e);
			return null;
		}
	}
	
	
	/**
	 * 根据手机号获取用户名
	 * 可用于判断手机号是否被注册
	 */
	public void requestGetUin(String phoneString, final OnRequestListener requestListener) {
		String url = String.format("%s/QiNiuApp/core/user.op.do?",
				UrlConstants.RequsetBaseURLString);
		
		param = new HashMap<String ,String>();
		param.put("param.mobile", phoneString);
		param.put("param.op_code", String.valueOf(OPERATOR_CHECK));
		param.put("param.op_mode", String.valueOf(OP_MODE_PHONE));
		UniversalRequest.requestUrlWithoutSessionId(mContext,url,param, requestListener);
	}
	
	/**
	 * 校验并登陆用户.
	 */
	public void requestCheckAndLogin(String partnerType, String partnerId,
									 final OnRequestListener requestListener) {
		String url = String.format("%s/QiNiuApp/core/user.auth.do?",UrlConstants.RequsetBaseURLString);

		param = new HashMap<String ,String>();
		param.put("param.auth_mode", partnerType);
		param.put("param.token", partnerId);

		UniversalRequest.addClientType(param);
		UniversalRequest.requestUrlWithoutSessionId(mContext,url,param, requestListener);
	}
	
	/**
	 * 第三方登录 --QQ  注册
	 */
	public void requestSendQQUserInfo(HashMap<String, String> data, final OnRequestListener requestListener) {
		
		String url = String.format("%s/QiNiuApp/core/user.reg.do?",
				UrlConstants.RequsetBaseURLString);
		
		param = new HashMap<String ,String>(data);
		param.put("param.reg_mode","3");

		UniversalRequest.addClientType(param);
		UniversalRequest.requestUrlWithoutSessionId(mContext,url,param, requestListener);
	}
	/**
	 * 第三方登录 --WeChat  注册
	 */
	public void requestSendWeChatUserInfo(HashMap<String, String> data, final OnRequestListener requestListener) {

		String url = String.format("%s/QiNiuApp/core/user.reg.do?",
				UrlConstants.RequsetBaseURLString);

		param = new HashMap<String ,String>(data);
		param.put("param.reg_mode","4");

		UniversalRequest.addClientType(param);
		UniversalRequest.requestUrlWithoutSessionId(mContext, url, param, requestListener);
	}

	/**
	 * 第三方登录--新浪微博 注册
	 */
	public void requestSendWeiBoUserInfo(HashMap<String, String> data, final OnRequestListener requestListener) {
		String url = String.format("%s/QiNiuApp/core/user.reg.do?",
				UrlConstants.RequsetBaseURLString);
		
		param = new HashMap<String ,String>(data);
		param.put("param.reg_mode","5");
		UniversalRequest.addClientType(param);
		UniversalRequest.requestUrlWithoutSessionId(mContext, url, param, requestListener);
	}
	
	/**
	 * 绑定第三方账户 bindPartner
	 * partnerType	第三方类型
	 * partnerId	第三方唯一ID
	 */
	public void requestBindPartner(HashMap<String, String> data,
			final OnRequestListener requestListener) {
		String url = String.format("%s/QiNiuApp/core/user.op.do?",UrlConstants.RequsetBaseURLString);
		
		param = new HashMap<String ,String>(data);
        param.put("param.head_uin",Utils.getAccountUin(mContext));
        param.put("param.op_code", String.valueOf(OPERATOR_BIND));
		UniversalRequest.addClientType(param);
		UniversalRequest.requestUrlWithoutSessionId(mContext, url, param, requestListener);
	}
	
	/**
	 * 解除绑定第三方账户
	 * 
	 */
	public void requestUnBindPartner(HashMap<String, String> data, final OnRequestListener requestListener) {
        String url = String.format("%s/QiNiuApp/core/user.op.do?",UrlConstants.RequsetBaseURLString);

        param = new HashMap<String ,String>(data);
        param.put("param.head_uin",Utils.getAccountUin(mContext));
        param.put("param.op_code", String.valueOf(OPERATOR_UN_BIND));
        UniversalRequest.addClientType(param);
        UniversalRequest.requestUrl(mContext,url,param, requestListener);
	}
	
	
	/**
	 * 退出登录
	 * 下次进入则不能自动登录
	 */
	public void requestLogout() {
		String url = String.format("%s/QiNiuApp/core/user.logout.do?",UrlConstants.RequsetBaseURLString);

		param = new HashMap<String ,String>();
		param.put("param.head_uin", Utils.getAccountUin(mContext));
		param.put("param.token", "");
		//不能先清sid
		UniversalRequest.requestUrl(mContext, url, param, new OnRequestListener() {
			@Override
			public void onError(int errorCode, String errorString) {
				QLog.e(TAG, "退出登录失败！ " + errorString);
			}

			@Override
			public void onDataFinish(Object object) {
				QLog.i(TAG, "成功退出登录！");
			}
		});
        //清除信息
        UserUtils.clearUserLoginInfo(mContext);
	}


	/**
	 * 测试超时
	 */
	public void requestTimeOut(){
		UniversalRequest.requestUrlWithTimeOut(UniversalRequest.class.getSimpleName(), mContext, "http://192.168.1.116:8000/hello/", null,
				false,0,false);
//				true, 5000, false);
	}
}

