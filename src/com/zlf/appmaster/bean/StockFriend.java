package com.zlf.appmaster.bean;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.zlf.appmaster.stockinterface.ContactItemInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
//import com.ngohung.widget.PingYinUtil;

/**
 * 股友信息
 * @author Yushian
 *
 */
public class StockFriend extends FansFocus implements ContactItemInterface,Parcelable {
	
	/**
	 * 新朋友状态：潜在朋友，还未添加好友关系
	 */
	public static final String RELATION_STATUS_NEW = "1";//不是好友 可添加
	/**
	 * 新朋友状态：潜在朋友，已发送好友邀请，等待对方验证
	 */
	public static final String RELATION_STATUS_WAITTING = "2";//验证中
	/**
	 * 新朋友状态：潜在好友，对方发送了好友邀请，等待自己接受
	 */
	public static final String RELATION_STATUS_ACCEPT = "3";//接受
	
	/**
	 * 新朋友状态：陌生人
	 */
	public static final String RELATION_STATUS_STRANGER = "4";// 没有注册
	
	/**
	 * 新朋友状态：已经添加了好友关系
	 */
	public static final String RELATION_STATUS_FRIEND = "5";//好友
	
	
	
	private String level;
	private long virtualMoney;
	private String relation = RELATION_STATUS_NEW;//好友关系
	private String signature;
	private String phone;
	private String index;//索引
	//排名
	private String noString;
	//位置
	private String locationString;
	//来源名称（手机联系人、微博里面的名称）
	private String tokenName;
	//求败胜率
	private String defeatWinRate;
	
	//是否选中了
	private boolean isSelected;
	
	
	
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public String getDefeatWinRate() {
		return "胜率 "+defeatWinRate;
	}
	
	public String getDefeatWinRateEx() {
		return defeatWinRate;
	}
	
	public void setDefeatWinRate(double defeatWinRate) {
		this.defeatWinRate = String.format("%.2f", defeatWinRate*100);
		this.defeatWinRate += "%";
	}
	
	public String getTokenName() {
		return tokenName;
	}
	public void setTokenName(String tokenName) {
		this.tokenName = tokenName;
	}
	public String getLocationString() {
		return locationString;
	}
	public void setLocationString(String locationString) {
		this.locationString = locationString;
	}
	public String getNoString() {
		return noString;
	}
	public void setNoString(String noString) {
		
		this.noString = checkIfEmpty(noString);
	}
	
	//判断是否为空 为空则返回0
	private String checkIfEmpty(String src) {
		if (TextUtils.isEmpty(src)) {
			return "0";
		}
		return src;
	}
	
	/**
	 * 检验手机号的合法性 并返回不含86（+86）的11位数字
	 * @return String
	 */
	public static String verifyPhoneNum(String phone) {
		if (TextUtils.isDigitsOnly(phone)) {
			//11位的数字 或 13位 （86打头）
			if (phone.length() == 11 && phone.charAt(0) == '1') {
				return phone;	
			}
			else if (phone.length() == 13 
					&& phone.charAt(0) == '8'
					&& phone.charAt(1) == '6'
					&& phone.charAt(2) == '1'){
				return phone.substring(2);
			}
		}
		else {
			String onlyNumString = phone.replace(" ", "");//去掉空格
			if (onlyNumString.charAt(0) == '+'
				&& onlyNumString.charAt(1) == '8'
				&& onlyNumString.charAt(2) == '6') {//+86
				if (TextUtils.isDigitsOnly(onlyNumString.substring(3))
						&& onlyNumString.charAt(3) == '1'
						&& onlyNumString.length() == 14) {
					return onlyNumString.substring(3);	
				}
			}
		}
		return null;
	}
	
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLevel() {
		return level;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public long getVirtualMoney() {
		return virtualMoney;
	}

	public void setVirtualMoney(long virtualMoney) {
		this.virtualMoney = virtualMoney;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String status) {
		this.relation = status;
	}

	
	public StockFriend(String name, String phone){
		setName(name);
		setPhone(phone);
	}
	
	public void setCommonData(JSONObject jsonObject) {
		try {
			setUin(jsonObject.optString("uin"));
			setName(jsonObject.optString("nickName"));
			setLevel(jsonObject.opt("level").toString());
			setVirtualMoney(jsonObject.optLong("virtualMoney"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			setSex(jsonObject.opt("sex").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 填充数据
	 * @param jsonObject
	 */
	public StockFriend(JSONObject jsonObject) {
		setCommonData(jsonObject);
		try {
			setRelation(jsonObject.get("relation").toString());	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 推荐高手 数据填充
	 * @param jsonObject 
	 */
	public void setDataByAces(JSONObject jsonObject) {
		try {
			setUin(jsonObject.getString("uin"));
			setName(jsonObject.getString("name"));
			setLevel(jsonObject.get("level").toString());
			setLocationString(jsonObject.getString("area"));
			setSex(jsonObject.get("sex").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		try {
//			setRelation(jsonObject.get("relation").toString());	
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}	
	
	
	/**
	 * 通讯录专用
	 * @param jsonObject
	 */
	public void setDataByContact(JSONObject jsonObject) {
		setCommonData(jsonObject);
		setIndexFromName();//转换下 获取index
		
		try {
			setSignature(jsonObject.get("doing").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 榜单数据填充
	 */
	public void setDataByRanking(JSONObject jsonObject, int endIndex, int rankType) {
//		setCommonData(jsonObject);
		try {
			setUin(jsonObject.getString("uin"));
			setName(jsonObject.getString("name"));
			
			setLevel(jsonObject.get("level").toString());
			setVirtualMoney(jsonObject.getLong("totalMoney"));
			
			setSex(jsonObject.get("sex").toString());
			//收益
			setGain(jsonObject.get("totalRate").toString());

			//排名
			int no = jsonObject.getInt("rankNumber")+endIndex;
			setNoString(Integer.toString(no));
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (rankType == RankingItem.RANKTYPE_CITY) {
			// 本市不需要
			return;
		}
		
		try {
			//位置
			String cityString = jsonObject.getString("province");
			if (cityString.equals(jsonObject.getString("city")) == false) {
				cityString += jsonObject.getString("city");
			}
			setLocationString(cityString);
		} catch (Exception e) {
			//部分没有
		}
	}
	
	public StockFriend() {
	}
	

	@Override
	public void setName(String name) {
		setName(name,false);
	}
	
	public void setName(String name, Boolean isSavePinyin) {
		super.setName(name);
		
		if (isSavePinyin ) {
			//转拼音
			setIndexFromName();	
		}
	}
	
	//将名字转换为拼音索引
	public void setIndexFromName(){
		if (TextUtils.isEmpty(index)) {
			//转拼音
//			index = PingYinUtil.getPingYin(getName()).toUpperCase(Locale.getDefault());
		}
	}
	
	//要在设置名字之前设置好
	public void setIndex(String index) {
		this.index = index;
	}

	@Override
	public String getItemForIndex() {
		return index;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getUin());
		dest.writeString(getSex());
		dest.writeString(getName());
		dest.writeString(getLevel());
		dest.writeLong(getVirtualMoney());
		dest.writeString(getRelation());
		
		dest.writeString(getSignature());

//		dest.writeString(getGain());
//		dest.writeString(getPhone());
	}
	
	public StockFriend(Parcel in) {
		setUin(in.readString());
		setSex(in.readString());
		setName(in.readString());
		setLevel(in.readString());
		setVirtualMoney(in.readLong());
		setRelation(in.readString());
		setSignature(in.readString());
	}
	
	public static final Parcelable.Creator<StockFriend> CREATOR = new Parcelable.Creator<StockFriend>() {

		@Override
		public StockFriend createFromParcel(Parcel source) {
			return new StockFriend(source);
		}

		@Override
		public StockFriend[] newArray(int size) {
			return new StockFriend[size];
		}
		
	};
	
	//获得用户名与级别：高手张（小牛B咖）
	public String getNameAndLevel() {
		if (TextUtils.isEmpty(getLevel())) {
			if (TextUtils.isEmpty(getName())) {
				return "";
			}
			return getName();
		}
		return String.format(Locale.getDefault(),"%s(%s)", getName(),getLevel());
	}
	
	//获取级别与资金
	public String getLevelAndMoney() {
		if (TextUtils.isEmpty(getLevel())) {
			return Long.toString(getVirtualMoney()/10000);
		}
		return String.format(Locale.getDefault(),"%s(%d万)", getLevel(),getVirtualMoney()/10000);
	}
	
	public void resloveInviteDefeatJsonObjcet(JSONObject infoJsonObject) {
		try {
			setName(infoJsonObject.getString("nickName"),true);
			setSex(infoJsonObject.getString("sex"));
			setDefeatWinRate(infoJsonObject.getDouble("userRatio"));
			setLevel(infoJsonObject.getString("level"));
			
			double money = infoJsonObject.getDouble("totalMoney");
			long longMoney = Math.round(money);
			setVirtualMoney(longMoney);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 解析求败邀请股友
	 * @param object
	 * "data":{"10071":{"nickName":"凭海临风","sex":"1","userRatio":"0"}}
	 */
	public static ArrayList<StockFriend> resloveInviteDefeatJsonArray(JSONObject object) {
		
		ArrayList<StockFriend> list = new ArrayList<StockFriend>();
		try {
			JSONObject dataJsonObject = object.getJSONObject("data");
			Iterator<String> keys =  dataJsonObject.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				JSONObject infoJsonObject;
				
				infoJsonObject = dataJsonObject.getJSONObject(key);
				
				StockFriend friend = new StockFriend();
				friend.resloveInviteDefeatJsonObjcet(infoJsonObject);
				friend.setUin(key);
				list.add(friend);
			}
			
			return list;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
