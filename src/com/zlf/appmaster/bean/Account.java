package com.zlf.appmaster.bean;

import android.content.Context;
import android.text.TextUtils;

import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.db.stock.UserTable;
import com.zlf.appmaster.utils.Setting;

import org.json.JSONObject;


/**
 * 登陆账号的拥有者
 * @author Deping Huang
 */
public class Account extends User{

    private static final String TAG = Account.class.getSimpleName();


	/**
	 * 0、游客
	 1、普通用户
	 2、主播
     参看QConstants中的定义
	 */
	private int mRole;//角色

	private String mSessionID="";
	private int mParticipateNum;//求败参与次数
	private String province;
	private String city;

	public int getParticipateNum() {
		return mParticipateNum;
	}
	
	public void setParticipateNum(int num) {
		mParticipateNum = num;
	}
	
	public String getSessionID() {
		return mSessionID;
	}

	public void setSessionID(String mSessionID) {
		this.mSessionID = mSessionID;
	}

	public int getRole() {
		return mRole;
	}

	public void setRole(int mRole) {
		this.mRole = mRole;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public static Account resolveJsonObject(JSONObject response){
		Account account = new Account();
		try {
            QLog.i(TAG, "response:" + response);
			JSONObject jsonInfo = response.getJSONObject("data");

			account.setName(jsonInfo.optString("nick_name"));
			//account.setSessionID(jsonInfo.optString("Session"));
			account.setUin(jsonInfo.optString("uin"));
			account.setSex(jsonInfo.optInt("sex"));
			account.setRole(jsonInfo.optInt("role"));

			account.setProvince(jsonInfo.optString("province"));
			account.setCity(jsonInfo.optString("city"));
			account.setSign(jsonInfo.optString("signature"));
			account.setStockAge(jsonInfo.optString("stock_age"));
			account.setOHeadImg(jsonInfo.optString("original"));//ohead_img

            //绑定的信息
            String qqBindInfo = jsonInfo.optString("bind_qq");
            if (!TextUtils.isEmpty(qqBindInfo)){
                account.setIsBindQQ(true);
                account.setNickNameQQ(jsonInfo.optString("nick_name_qq"));
            }else {
                account.setIsBindQQ(false);
            }

            String weChatBindInfo = jsonInfo.optString("bind_wx");
            if (!TextUtils.isEmpty(weChatBindInfo)){
                account.setIsBindWeChat(true);
                account.setNickNameWeChat(jsonInfo.optString("nick_name_qq"));
            }else {
                account.setIsBindWeChat(false);
            }

            String sinaBindInfo = jsonInfo.optString("bind_sina");
            if (!TextUtils.isEmpty(sinaBindInfo)){
                account.setIsBindSina(true);
                account.setNickNameSina(jsonInfo.optString("nick_name_sina"));
            }else {
                account.setIsBindSina(false);
            }

            //
            String phoneBindInfo = jsonInfo.optString("bind_mobile");
            if (!TextUtils.isEmpty(phoneBindInfo)){
                account.setIsBindPhone(true);
                account.setPhoneNum(phoneBindInfo);
            }else {
                account.setIsBindPhone(false);
            }

            //主播资料
            if(account.getRole() == 2) {
                JSONObject jboJason = jsonInfo.getJSONObject("job");
                account.setCompanyID(jboJason.getInt("company_id"));
                account.setCompanyName(jboJason.getString("company_name"));
                account.setAddress(jboJason.getString("address"));
                account.setJobID(jboJason.getInt("job_id"));
                account.setJobName(jboJason.getString("job_name"));
                account.setJobCard(jboJason.getString("job_card"));
                account.setResume(jboJason.getString("resume"));
            }

       /*     //免打扰
            JSONObject jboJason = jsonInfo.getJSONObject("msg_filters");
            if(jboJason != null) {
                boolean isSetting = jsonInfo.optBoolean("is_setting");
                if (isSetting){
                    account.setDisturb(1);
                }else{
                    account.setDisturb(0);
                }
                account.setBeginHour(jboJason.getInt("begin_hour"));
                account.setBeginHour(jboJason.getInt("begin_min"));
                account.setStopHour(jboJason.getInt("stop_hour"));
                account.setStopMinute(jboJason.getInt("stop_min"));
            }*/
		}catch (Exception ex){
			ex.printStackTrace();
		}

		return account;
	}

    public static Account syncResolveJsonObject(JSONObject response, Context context){
        Account account = new Account();
        try {
            QLog.i(TAG, "sync response:" + response);
            JSONObject jsonInfo = response.getJSONObject("1");
            account.setName(jsonInfo.optString("nick_name"));
            //account.setSessionID(jsonInfo.optString("Session"));
            account.setUin(jsonInfo.optString("uin"));
            account.setSex(jsonInfo.optInt("sex"));
            account.setRole(jsonInfo.optInt("role"));

            account.setProvince(jsonInfo.optString("province"));
            account.setCity(jsonInfo.optString("city"));
            account.setSign(jsonInfo.optString("signature"));
            account.setStockAge(jsonInfo.optString("stock_age"));
            account.setOHeadImg(jsonInfo.optString("original"));

            //绑定的信息
            String qqBindInfo = jsonInfo.optString("bind_qq");
            if (!TextUtils.isEmpty(qqBindInfo)){
                account.setIsBindQQ(true);
                account.setNickNameQQ(jsonInfo.optString("nick_name_qq"));
            }else {
                account.setIsBindQQ(false);
            }

            String weChatBindInfo = jsonInfo.optString("bind_wx");
            if (!TextUtils.isEmpty(weChatBindInfo)){
                account.setIsBindWeChat(true);
                account.setNickNameWeChat(jsonInfo.optString("nick_name_qq"));
            }else {
                account.setIsBindWeChat(false);
            }

            String sinaBindInfo = jsonInfo.optString("bind_sina");
            if (!TextUtils.isEmpty(sinaBindInfo)){
                account.setIsBindSina(true);
                account.setNickNameSina(jsonInfo.optString("nick_name_sina"));
            }else {
                account.setIsBindSina(false);
            }

            //
            String phoneBindInfo = jsonInfo.optString("bind_mobile");
            if (!TextUtils.isEmpty(phoneBindInfo)){
                account.setIsBindPhone(true);
                account.setPhoneNum(phoneBindInfo);
            }else {
                account.setIsBindPhone(false);
            }

            //主播资料
            if(account.getRole() == 2) {
                JSONObject jboJason = jsonInfo.getJSONObject("job");
                account.setCompanyID(jboJason.getInt("company_id"));
                account.setCompanyName(jboJason.getString("company_name"));
                account.setAddress(jboJason.getString("address"));
                account.setJobID(jboJason.getInt("job_id"));
                account.setJobName(jboJason.getString("job_name"));
                account.setJobCard(jboJason.getString("job_card"));
                account.setResume(jboJason.getString("resume"));
            }
            //保存setting和USER_TABLE
            //免打扰
            JSONObject jboJason = jsonInfo.getJSONObject("msg_filters");
            if(jboJason != null) {
                boolean isSetting = jsonInfo.optBoolean("is_setting");
                if (isSetting){
                    account.setDisturb(1);
                }else{
                    account.setDisturb(0);
                }
                account.setBeginHour(jboJason.getInt("begin_hour"));
                account.setBeginHour(jboJason.getInt("begin_min"));
                account.setStopHour(jboJason.getInt("stop_hour"));
                account.setStopMinute(jboJason.getInt("stop_min"));
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return account;
    }

    private boolean isBindQQ;
    private boolean isBindWeChat;
    private boolean isBindSina;
    private boolean isBindPhone;

    private String nickNameQQ;
    private String nickNameWeChat;
    private String nickNameSina;
    private String phoneNum;

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public boolean isBindPhone() {
        return isBindPhone;
    }

    public void setIsBindPhone(boolean isBindPhone) {
        this.isBindPhone = isBindPhone;
    }

    public boolean isBindQQ() {
        return isBindQQ;
    }

    public void setIsBindQQ(boolean isBindQQ) {
        this.isBindQQ = isBindQQ;
    }

    public boolean isBindWeChat() {
        return isBindWeChat;
    }

    public void setIsBindWeChat(boolean isBindWeChat) {
        this.isBindWeChat = isBindWeChat;
    }

    public boolean isBindSina() {
        return isBindSina;
    }

    public void setIsBindSina(boolean isBindSina) {
        this.isBindSina = isBindSina;
    }

    public String getNickNameQQ() {
        return nickNameQQ;
    }

    public void setNickNameQQ(String nickNameQQ) {
        this.nickNameQQ = nickNameQQ;
    }

    public String getNickNameWeChat() {
        return nickNameWeChat;
    }

    public void setNickNameWeChat(String nickNameWeChat) {
        this.nickNameWeChat = nickNameWeChat;
    }

    public String getNickNameSina() {
        return nickNameSina;
    }

    public void setNickNameSina(String nickNameSina) {
        this.nickNameSina = nickNameSina;
    }

    /**
     * 获取绑定数量
     * @return
     */
    public int getBindNum(){
        int bindNum = 0;
        if (isBindPhone())
            bindNum++;

        if (isBindQQ())
            bindNum++;

        if (isBindSina())
            bindNum++;

        if (isBindWeChat())
            bindNum++;

        return bindNum;
    }

    /**
	 * 保存数据到本地
	 * @param context
	 */
	public void saveAccountToSetting(Context context, String loginMode){
		//保存下sessionId
		Setting setting = new Setting(context);
		setting.setSessionId(getSessionID());
        setting.setUin(getUin());
        setting.setUserRole(getRole());

        setting.setLoginMode(loginMode);

        setting.saveIsBindPhone(this.isBindPhone());
        setting.saveIsBindQQ(this.isBindQQ());
        setting.saveIsBindWeiBo(this.isBindSina());
        setting.saveIsBindWX(this.isBindWeChat());

        setting.savePhoneNum(this.getPhoneNum());
        setting.saveQQNickname(this.getNickNameQQ());
        setting.saveWeiBoNickname(this.getNickNameSina());
        setting.saveWXNickname(this.getNickNameWeChat());

        UserTable table = new UserTable(context);
		table.saveUserInfo(this);
	}

    /**
     * 保存数据到本地
     * @param context
     */
    public void saveAccountToSetting(Context context){
        //保存下sessionId
        Setting setting = new Setting(context);
        setting.setSessionId(getSessionID());
        setting.setUin(getUin());
        setting.setUserRole(getRole());

        setting.saveIsBindPhone(this.isBindPhone());
        setting.saveIsBindQQ(this.isBindQQ());
        setting.saveIsBindWeiBo(this.isBindSina());
        setting.saveIsBindWX(this.isBindWeChat());

        setting.savePhoneNum(this.getPhoneNum());
        setting.saveQQNickname(this.getNickNameQQ());
        setting.saveWeiBoNickname(this.getNickNameSina());
        setting.saveWXNickname(this.getNickNameWeChat());

        UserTable table = new UserTable(context);
        table.saveUserInfo(this);
    }
}
