package com.zlf.appmaster.utils;

/**
 * 常量
 * @author Yushian
 *
 */
public class QConstants {
	//startActivityForResult ResultCode
	public static final int RESULTCODE_BACK = 0;//返回  默认
	public static final int RESULTCODE_EXIT = 1;//退出
	
	public static final int RESULTCODE_LOGIN_FAIL = 3;//登录失败
	public static final int RESULTCODE_LOGIN_SUCCESS = 4;//登录成功
	
	public static final int RESULTCODE_UPDATE_STOCKFAVORITE = 10;	// 更新自选股
	public static final int RESULTCODE_UPDATE_MYFAVORITES = 11;		// 更新我的收藏
	public static final int RESULTCODE_UPDATE_TOPICFAVORITE = 12;	// 更新 自选主题

	public static final int RESULTCODE_CHAT_ROOM_CREATED = 1;	// 讨论组创建完成


	//request
	public static final int REQUESTCODE_LOGIN = 1;//调用登录Activity


	/**
	 * 登录角色
	 */
	public static final int ACCOUNT_ROLE_DEFAULT = 0;
	public static final int ACCOUNT_ROLE_NORMAL = 0x01;
	public static final int ACCOUNT_ROLE_ANCHOR = 0x02;



}
