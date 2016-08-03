package com.zlf.appmaster.model.news;


import com.zlf.appmaster.utils.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NewsDetail {
	private String mContentID;
	private String mContent;
	private String mTitle;
	private String mTime;
	private String mMedia="";
	private String mAuthor;
	private boolean mIsFavorite;		// 是否为收藏
	private String mImgUrl;
	private String mBreviary;//摘要
	private int mClassify;	// 分类（内部标示）
	
	
	public String getImgUrl() {
		return mImgUrl;
	}
	public void setImgUrl(String mImgUrl) {
		this.mImgUrl = mImgUrl;
	}
	public String getBreviary() {
		return mBreviary;
	}
	public void setBreviary(String mBreviary) {
		this.mBreviary = mBreviary;
	}
	public String getContentID() {
		return mContentID;
	}
	public void setContentID(String mContentID) {
		this.mContentID = mContentID;
	}
	public String getContent() {
		return mContent;
	}
	public void setContent(String mContent) {
		this.mContent = mContent;
	}
	public String getTitle() {
		return mTitle;
	}
	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}
	public String getTime() {
		return mTime;
	}
	public void setTime(String mTime) {
		this.mTime = mTime;
	}
	public String getMedia() {
		return mMedia;
	}
	public void setMedia(String mMedia) {
		this.mMedia = mMedia;
	}
	public String getAuthor() {
		return mAuthor;
	}
	public void setAuthor(String mAuthor) {
		this.mAuthor = mAuthor;
	}
	public boolean isFavorite() {
		return mIsFavorite;
	}
	public void setIsFavorite(boolean mIsFavorite) {
		this.mIsFavorite = mIsFavorite;
	}
	
	public static NewsDetail resolveJsonObject(JSONObject response) throws JSONException {
		NewsDetail newsDetail = new NewsDetail();
		JSONObject newsContentObject = response.optJSONObject("data");
		
		//newsDetail.setContentID(newsContentObject.optString("id"));
		newsDetail.setContent(newsContentObject.optString("Content"));
		long timeLong = newsContentObject.optLong("Ctime");
		newsDetail.setTime(TimeUtil.getTime(timeLong));
		newsDetail.setTitle(newsContentObject.optString("Title"));
		newsDetail.setMedia(newsContentObject.optString("Media"));
		newsDetail.setBreviary(newsContentObject.optString("Summary"));
		newsDetail.setImgUrl(newsContentObject.optString("img"));
		newsDetail.setClassify(newsContentObject.optInt("Classify"));
		
		return newsDetail;
	}

	public static NewsDetail resolveNoticeJsonObject(JSONObject response){
		NewsDetail newsDetail = new NewsDetail();
		JSONArray newsArray= response.optJSONArray("data");
		if (null != newsArray ){
			int len = newsArray.length();
			if (len > 0){
				JSONObject jsonObject = newsArray.optJSONObject(0);
				if (null != jsonObject) {
					newsDetail.setContentID(jsonObject.optString("NewsId"));
					newsDetail.setTitle(jsonObject.optString("NewsTitle"));
				}
			}
		}

		return newsDetail;
	}
	


	public int getClassify() {
		return mClassify;
	}

	public void setClassify(int classify) {
		this.mClassify = classify;
	}
}
