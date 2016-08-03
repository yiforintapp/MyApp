package com.zlf.appmaster.bean;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 策略项
 * @author Deping Huang
 *
 */
public class StrategyItem implements Parcelable {
	private long mID;
    private long mVersionCode;
    private long mGroupID;
	private String mName;
	private String mAlias;
    private String mInstruction;
    private long mCreateTime;
    private long mUpdateTime;
    private String mMainType;
    private String mSubType;
    private int mSockCounts;        // 该策略下的股票数

    public int getDefaultOpen() {
        return mDefaultOpen;
    }

    public void setDefaultOpen(int defaultOpen) {
        this.mDefaultOpen = defaultOpen;
    }

    private int mDefaultOpen;
    private int mBGColor;

    public StrategyItem(){

    }

    public StrategyItem(long id){
        mID = id;
    }

    public StrategyItem(String id){
        try{
            mID = Long.valueOf(id);
        }
        catch (NumberFormatException e){

        }
    }
	
	public String getName() {
		return mName;
	}
	public void setName(String mName) {
		this.mName = mName;
	}
	public String getAlias() {
		return mAlias;
	}
	public void setAlias(String mAlias) {
		this.mAlias = mAlias;

	}
	public long getID() {
		return mID;
	}
	public void setID(long mID) {
		this.mID = mID;
	}

    public String getInstruction() {
        return mInstruction;
    }

    public void setInstruction(String instruction) {
        this.mInstruction = instruction;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(long createTime) {
        this.mCreateTime = createTime;
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.mUpdateTime = updateTime;
    }

    public int getBGColor() {
        return mBGColor;
    }

    public void setBGColor(int BGColor) {
        this.mBGColor = BGColor;
    }

    public long getGroupID() {
        return mGroupID;
    }

    public void setGroupID(long groupID) {
        this.mGroupID = groupID;
    }

    public long getVersionCode() {
        return mVersionCode;
    }

    public void setVersionCode(long versionCode) {
        this.mVersionCode = versionCode;
    }

    public static StrategyItem resolveJSONObject(JSONObject jsonObject) throws JSONException {

        StrategyItem item = new StrategyItem();
        item.setID(jsonObject.getLong("sid"));
        item.setGroupID(jsonObject.getLong("tid"));
        item.setBGColor(jsonObject.getInt("bcolor"));
        item.setName(jsonObject.getString("name"));
        item.setAlias(jsonObject.getString("sname"));
        item.setInstruction(jsonObject.getString("instruction"));
        item.setVersionCode(jsonObject.getInt("cp"));
        item.setUpdateTime(jsonObject.getLong("utime"));
        item.setCreateTime(jsonObject.getLong("ctime"));

        return item;
    }

    /**
     * 获取主类型
     * @param alias
     * @return
     */
    public static String getMainType(String alias){
        //分类
        if (!TextUtils.isEmpty(alias)){
            return alias.substring(0,1);
        }
        return "";
    }
    /**
     * 获取子类型
     * @param alias
     * @return
     */
    public static String getSubType(String alias){
        //分类
        if (!TextUtils.isEmpty(alias)){
            return alias.substring(1);
        }
        return "";
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeLong(mID);
        dest.writeLong(mVersionCode);
        dest.writeLong(mGroupID);
        dest.writeString(mName);
        dest.writeString(mAlias);
        dest.writeString(mInstruction);
        dest.writeLong(mCreateTime);
        dest.writeLong(mUpdateTime);
        dest.writeInt(mBGColor);
    }

    //实例化静态内部对象CREATOR实现接口Parcelable.Creator
    public static final Parcelable.Creator<StrategyItem> CREATOR = new Creator<StrategyItem>() {

        @Override
        public StrategyItem[] newArray(int size) {
            return new StrategyItem[size];
        }

        //将Parcel对象反序列化为ParcelableDate
        @Override
        public StrategyItem createFromParcel(Parcel source) {

            StrategyItem item = new StrategyItem();
            item.setID(source.readLong());
            item.setVersionCode(source.readLong());
            item.setGroupID(source.readLong());
            item.setName(source.readString());
            item.setAlias(source.readString());
            item.setInstruction(source.readString());
            item.setCreateTime(source.readLong());
            item.setUpdateTime(source.readLong());
            item.setBGColor(source.readInt());
            return item;
        }
    };

    public int getSockCounts() {
        return mSockCounts;
    }

    public void setSockCounts(int sockCounts) {
        this.mSockCounts = sockCounts;
    }
}
