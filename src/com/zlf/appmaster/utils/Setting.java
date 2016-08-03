package com.zlf.appmaster.utils;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * 配置文件处理
 * @author Yushian
 *
 */
public class Setting {
	//sharePerference
//	private static final String TAG = "Setting";

	
    //保存手机号
    private static final String SETTINGFILE="setting";
    private static final String PHONENUM="phonenum";
    private static final String QQNICKNAME="QQNickname";
    private static final String WXNICKNAME="WXNickname";
    private static final String WEIBONICKNAME="WeiBoNickname";
    private static final String IGNORE_VERSION="ignore_version";//忽略的版本
    private static final String FORCE_UPDATE = "force_update";//强制升级
    private static final String UPDATE_SUMMARY = "update_summary";
    //
    
    
    //自动登录 sid
    private static final String SESSION_ID = "session_id";
    private static final String USER_IN = "user_in";
    private static final String LOGIN_MODE = "login_mode";
    private static final String USER_ROLE = "user_role";
    private static final String ROOM_ID = "room_id";
    private static final String PLAYBACK_STREAM_NAME = "playback_stream_name";
    
    //好友请求数量
    private static final String FRIEND_ADD_COUNT = "friend_add_count";
    
    
    //qq 登录信息
	/**
	 * access_token=C76FF884F63EB8B16BE7A4652F64EE91&
	 * oauth_consumer_key=101118299&
	 * openid=854B8590D60AE05FBFF88BD566DA12BD
	 */
    public static final String QQ_OPENID="param.openid";
    public static final String QQ_ACCESS_TOKEN="param.access_token";
    
    public static final String QQ_OAUTH_KEY="param.oauth_consumer_key";
    public static final String QQ_OAUTH_KEY_NUM = "101118299";
    
    //sina 登录信息
    public static final String SINA_UID="param.uid";
    public static final String SINA_ACCESS_TOKEN="param.access_token";
    
    //下载状态
    public static final int DOWNLOAD_STATUS_NULL = 0;    //无
    public static final int DOWNLOAD_STATUS_PART = 1;    //下载中
    public static final int DOWNLOAD_STATUS_FINISH = 2;    //已下载好
    
    //已经下载在本地的包 版本号、下载状态
    private static final String UPDATE_DOWNLOAD_VERSION = "download_version";
    private static final String UPDATE_DOWNLOAD_STATUS = "download_status";
    private static final String UPDATE_DOWNLOAD_PATH = "download_path";//本地已经下载好的路径
    private static final String UPDATE_DOWNLOAD_URL = "download_url";//网络上的下载地址
    private static final String UPDATE_LAST_CHECK_VERSION = "download_last_check";//上次检查的本地版本

    private static final String BIND_PHONE_FLAG = "bind_phone";
    private static final String BIND_WEIBO_FLAG = "bind_weibo";
    private static final String BIND_QQ_FLAG = "bind_qq";
    private static final String BIND_WX_FLAG = "bind_wx";

    //是否点击了蒙层
    private static final String MASK_STOCK_CLICK = "mask_stock"; //首页
    private static final String MASK_STRATEGY_CLICK = "mask_strategy";//策略

    // 游客临时ID
    private static final String GUEST_UIN = "guest_uin";

    // 最近一条阅读过的新闻ID
    private static final String RECENT_NOTICE_NEWS_ID = "recent_notice_news_id";

    private SharedPreferences settings;
    private String versionFirstString;//本版本是否第一次进入
    private String combinationHelpBtnFirstPress = "combination_btn_first_press";//是否第一次点击迷你基金帮助按钮


    /**
     * 资讯是否阅读标记
     */
    private static final String NEWS_READ_FLAG = "news_read_flag";
    
    public Setting(Context mContext) {
    	versionFirstString = Utils.getSoftVersion(mContext);
    	settings = mContext.getSharedPreferences(SETTINGFILE, 0);
	}


    public void saveIsClickStockMask(Boolean isClick) {
        settings.edit().putBoolean(MASK_STOCK_CLICK, isClick).commit();
    }

    public Boolean getIsClickStockMask() {
        return true;
//        return settings.getBoolean(MASK_STOCK_CLICK, false);
    }

    public void setGuestUin(long guestUin) {
        settings.edit().putLong(GUEST_UIN, guestUin).commit();
    }

    public long getGuestUin() {
        return settings.getLong(GUEST_UIN, 0);
    }

    public void setRecentNoticeNewsId(String newsID) {
        settings.edit().putString(RECENT_NOTICE_NEWS_ID, newsID).commit();
    }

    public String getRecentNoticeNewsId() {
        return settings.getString(RECENT_NOTICE_NEWS_ID, "0");
    }

    public void saveIsBindWX(Boolean isBind) {
        settings.edit().putBoolean(BIND_WX_FLAG, isBind).commit();
    }

    public Boolean getIsBindWX() {
        return settings.getBoolean(BIND_WX_FLAG, false);
    }

    public void saveIsBindQQ(Boolean isBind) {
    	settings.edit().putBoolean(BIND_QQ_FLAG, isBind).commit();
	}
    
    public Boolean getIsBindQQ() {
    	return settings.getBoolean(BIND_QQ_FLAG, false);
	}
    
    public void saveIsBindWeiBo(Boolean isBind) {
    	settings.edit().putBoolean(BIND_WEIBO_FLAG, isBind).commit();
	}
    
    public Boolean getIsBindWeiBo() {
    	return settings.getBoolean(BIND_WEIBO_FLAG, false);
	}
    
    public void saveIsBindPhone(Boolean isBind) {
    	settings.edit().putBoolean(BIND_PHONE_FLAG, isBind).commit();
	}
    
    public Boolean getIsBindPhone() {
    	return settings.getBoolean(BIND_PHONE_FLAG, false);
	}
    
    public Boolean getFirstFlag() {
		return settings.getBoolean(versionFirstString, true);
	}
   
    public void saveFirstFlag() {
    	settings.edit().putBoolean(versionFirstString, false).commit();
	}


    public Boolean getCombinationHelpBtnFlag() {
        return settings.getBoolean(combinationHelpBtnFirstPress, false);
    }

    public void saveCombinationHelpBtnFlag() {
        settings.edit().putBoolean(combinationHelpBtnFirstPress, true).commit();
    }

    public void saveQQNickname(String qqNicknameString) {
        if (qqNicknameString == null ) {
            return;
        }

        settings.edit().putString(QQNICKNAME, qqNicknameString).commit();
    }

    public String getQQNickname() {

        return settings.getString(QQNICKNAME, "");
    }

    public void saveWXNickname(String wxNicknameString) {
        if (wxNicknameString == null ) {
            return;
        }

        settings.edit().putString(WXNICKNAME, wxNicknameString).commit();
    }

    public String getWXNickname() {

        return settings.getString(WXNICKNAME, "");
    }

    public void saveWeiBoNickname(String weiboNicknameString) {
        if (weiboNicknameString == null ) {
            return;
        }

        settings.edit().putString(WEIBONICKNAME, weiboNicknameString).commit();
    }

    public String getWeiBoNickname() {

        return settings.getString(WEIBONICKNAME, "");
    }

    public void savePhoneNum(String phoneString) {
    	if (phoneString == null || phoneString.length() != 11) {
			return;
		}
    	
    	settings.edit().putString(PHONENUM, phoneString).commit();
	}
    
    public String getPhoneNum() {

    	return settings.getString(PHONENUM, "");
	}
    
    public int getUpdateDownloadStatus() {
    	return settings.getInt(UPDATE_DOWNLOAD_STATUS, DOWNLOAD_STATUS_NULL);
	}

    public void setUpdateDownloadStatus(int status) {
    	settings.edit().putInt(UPDATE_DOWNLOAD_STATUS, status).commit();
	}
    
    public String getUpdateDownloadVersion() {
    	return settings.getString(UPDATE_DOWNLOAD_VERSION, "");
	}    
    
    public void setUpdateDownloadVersion(String version) {
    	settings.edit().putString(UPDATE_DOWNLOAD_VERSION, version).commit();
	}    
    
    public void setUpdateDownloadPath(String path) {
		settings.edit().putString(UPDATE_DOWNLOAD_PATH, path).commit();
	}
    
    public String getUpdateDownloadPath() {
    	return settings.getString(UPDATE_DOWNLOAD_PATH, "");
	}
    
    //
    public void setIgnoreVersion(String versionString) {
		settings.edit().putString(IGNORE_VERSION, versionString).commit();
	}
    
    public String getIgnoreVersion() {
    	return settings.getString(IGNORE_VERSION, "");
	}
    public void setUpdateSummary(String summary) {
		settings.edit().putString(UPDATE_SUMMARY, summary).commit();
	}
    
    public String getUpdateSummary() {
    	return settings.getString(UPDATE_SUMMARY, "");
	}
    
    
    public void setForceUpdate(boolean isforce) {
		settings.edit().putBoolean(FORCE_UPDATE, isforce).commit();
	}
    
    public boolean getForceUpdate() {
    	return settings.getBoolean(FORCE_UPDATE, false);
	}

    public void setSessionId(String sessionId) {
		settings.edit().putString(SESSION_ID, sessionId).commit();
	}
    
    public String getSessionId() {
    	return settings.getString(SESSION_ID, "");
	}


    public void setUserRole(int userRole) {
        settings.edit().putInt(USER_ROLE, userRole).commit();
    }

    public int getUserRole() {
        return settings.getInt(USER_ROLE, QConstants.ACCOUNT_ROLE_DEFAULT);
    }

    public void setRoomId(int roomId) {
        settings.edit().putInt(ROOM_ID, roomId).commit();
    }

    public int getRoomId() {
        return settings.getInt(ROOM_ID, 0);
    }

    public void setPlaybackStreamName(String streamName) {
        settings.edit().putString(PLAYBACK_STREAM_NAME, streamName).commit();
    }

    public String getPlaybackStreamName() {
        return settings.getString(PLAYBACK_STREAM_NAME,"");
    }

    public void setLoginMode(String loginMode) {
        settings.edit().putString(LOGIN_MODE, loginMode).commit();
    }

    public String getLoginMode() {
        return settings.getString(LOGIN_MODE, UrlConstants.USER_PARNTER_IMEI);
    }

    public void setUin(String uin) {
		settings.edit().putString(USER_IN, uin).commit();
	}
    
    public String getUin() {
    	String uinString = settings.getString(USER_IN, "");
    	return uinString;
	}    
 
    //在之前的基础上加
    public void addFriendAddCount(int num ) {
    	int preNum = getFriendAddCount();
		settings.edit().putInt(FRIEND_ADD_COUNT,  num+preNum).commit();
	}
    
    public void setFriendAddCount(int num) {
    	settings.edit().putInt(FRIEND_ADD_COUNT,  num).commit();
	}
    
    public int getFriendAddCount() {
    	int num = settings.getInt(FRIEND_ADD_COUNT,0);
    	return num;
	}


    public void setDownloadUrl(String url){
        settings.edit().putString(UPDATE_DOWNLOAD_URL, url).commit();
    }

    public String getDownloadUrl(){
        return settings.getString(UPDATE_DOWNLOAD_URL,"");
    }

    public void setLastCheckVersion(String url){
        settings.edit().putString(UPDATE_LAST_CHECK_VERSION, url).commit();
    }

    public String getLastCheckVersion(){
        return settings.getString(UPDATE_LAST_CHECK_VERSION,"");
    }

}
