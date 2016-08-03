package com.zlf.appmaster.bean;

import android.location.Location;
import android.text.TextUtils;

import com.zlf.appmaster.utils.QLog;

import java.util.Locale;

/**
 * 用户类
 * @author Deping Huang
 *
 */
public class User {

	public static int SEX_TYPE_MAN = 1;		//男
	public static int SEX_TYPE_WOMAN = 0;	//女

	private String mUin;
	private String mName;
	private String mLevel;		// 等级
	private Boolean isUploadLocation = false;//是否已上传位置信息
	
	//位置信息
	private Location mLocation;
	private int mSex;
	
	private String mBirthday;
	private String mStockAge;
	private String mJob;
	private String mSign;
	private long mMoney;
	private int mDisturb;
	private int beginHour,beginMinute,stopHour,stopMinute;

	private int companyID,jobID;
	private String companyName,jobName,address,jobCard,resume;

	//上传标记
	private Boolean isUploadContacts=false;
	private Boolean isUploadWeibo=false;

	private Boolean isPostUserTextInfo = true ;

	//头像
	private String OHeadImg,MHeadImg,BHeadImg,SHeadImg;

	public String getMHeadImg() {
		return MHeadImg;
	}

	public void setMHeadImg(String MHeadImg) {
		this.MHeadImg = MHeadImg;
	}

	public String getBHeadImg() {
		return BHeadImg;
	}

	public void setBHeadImg(String BHeadImg) {
		this.BHeadImg = BHeadImg;
	}

	public String getSHeadImg() {
		return SHeadImg;
	}

	public void setSHeadImg(String SHeadImg) {
		this.SHeadImg = SHeadImg;
	}

	public Boolean getIsPostUserTextInfo() {
		return isPostUserTextInfo;
	}

	public void setIsPostUserTextInfo(Boolean isPostUserTextInfo) {
		this.isPostUserTextInfo = isPostUserTextInfo;
	}


	public String getOHeadImg() {
		return OHeadImg;
	}

	public void setOHeadImg(String OHeadImg) {
		this.OHeadImg = OHeadImg;
	}

	public Boolean getIsUploadContacts() {
		return isUploadContacts;
	}
	public void setIsUploadContacts(Boolean isUploadContacts) {
		this.isUploadContacts = isUploadContacts;
	}
	public Boolean getIsUploadWeibo() {
		return isUploadWeibo;
	}
	public void setIsUploadWeibo(Boolean isUploadWeibo) {
		this.isUploadWeibo = isUploadWeibo;
	}
	public long getMoney() {
		return mMoney;
	}
	public void setMoney(long money) {
		this.mMoney = money;
	}
	
	public String getBirthday() {
		return mBirthday;
	}
	public void setBirthday(String mBirthday) {
		this.mBirthday = mBirthday;
	}
	public String getStockAge() {
		return mStockAge;
	}

	public String getStockAgeFormat(){
		if (TextUtils.isDigitsOnly(mStockAge) && Integer.valueOf(mStockAge) == 10)
			return String.format("股龄10年以上");
		return String.format("股龄%s年",mStockAge);
	}

	public void setStockAge(String mStockAge) {
		this.mStockAge = mStockAge;
	}
	public String getJob() {
		return mJob;
	}
	public void setJob(String mJob) {
		this.mJob = mJob;
	}
	public String getSign() {
		return mSign;
	}
	public void setSign(String mSign) {
		this.mSign = mSign;
	}
	public int getSex() {
		return mSex;
	}
	public void setSex(int sex) {
		this.mSex = sex;
	}


	public void setIsUploadLocation(Boolean isUpload) {
		isUploadLocation = isUpload;
	}
	
	public Boolean getIsUploadLocation() {
		return isUploadLocation;
	}
	
	public void setLocation(Location location) {
		QLog.q("setLocation : latitude:"+ location.getLatitude()+",longitude:"+location.getLongitude());
		this.mLocation = location;
	}
	
	public Location getLocation() {
		return mLocation;
	}
	
	public String getUin() {
		return mUin;
	}
	public void setUin(String mUin) {
		this.mUin = mUin;
	}
	public String getName() {
		return mName;
	}
	public void setName(String mName) {
		this.mName = mName;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "uin:"+mUin+"	name:"+mName;
	}
	public String getLevel() {
		return mLevel;
	}
	public void setLevel(String level) {
		this.mLevel = level;
	}

	public int getCompanyID() {
		return companyID;
	}

	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}

	public int getJobID() {
		return jobID;
	}

	public void setJobID(int jobID) {
		this.jobID = jobID;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getJobCard() {
		return jobCard;
	}

	public void setJobCard(String jobCard) {
		this.jobCard = jobCard;
	}

	public String getResume() {
		return resume;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}

	public int getDisturb() {
		return mDisturb;
	}

	public void setDisturb(int disturb) {
		this.mDisturb = disturb;
	}

	public int getBeginHour() {
		return beginHour;
	}

	public void setBeginHour(int beginHour) {
		this.beginHour = beginHour;
	}

	public int getBeginMinute() {
		return beginMinute;
	}

	public void setBeginMinute(int beginMinute) {
		this.beginMinute = beginMinute;
	}

	public int getStopHour() {
		return stopHour;
	}

	public void setStopHour(int stopHour) {
		this.stopHour = stopHour;
	}

	public int getStopMinute() {
		return stopMinute;
	}

	public void setStopMinute(int stopMinute) {
		this.stopMinute = stopMinute;
	}

	//获取用户级别与资产：散户红段（100万）
	public String getLevelAndMoney() {
		if (mLevel == null) {
			return null;
		}
		
		return String.format(Locale.getDefault(),"%s(%d万)", mLevel,mMoney/10000);
	}
	
	//获得用户名与级别：高手张（小牛B咖）
	public String getNameAndLevel() {
		if (TextUtils.isEmpty(mLevel)) {
			if (TextUtils.isEmpty(mName)) {
				return "";
			}
			return mName;
		}
		return String.format(Locale.getDefault(),"%s(%s)", mName,mLevel);
	}
}
