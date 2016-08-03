package com.zlf.appmaster.bean;

/**
 * 排行榜
 * @author Yushian
 *
 */
public class RankingItem {
	
	//类型
	public static final int RANKTYPE_NATION = 0;
	public static final int RANKTYPE_CITY = 1;
	public static final int RANKTYPE_WEEK = 2;
	public static final int RANKTYPE_MONTH = 3;
	
	
//	public static final int GAIN_COLOR_ZERO = 0;
//	public static final int GAIN_COLOR_RISE = 1;
//	public static final int GAIN_COLOR_SLUMPED = 2;
//	
//	//排名
//	private String noString;
//	//头像
//	private String headurl;
//	//名称
//	private String nameString;
//	//收益
//	private String gainString;
//	private int colorType;
//	//粉丝
//	private String fansNumString;
//	//关注
//	private String focusNumString;
//	private String uinString;
//	private String sex;
//	
//	
//	public String getSex() {
//		return sex;
//	}
//	public void setSex(String sex) {
//		this.sex = sex;
//	}
//	public String getUinString() {
//		return uinString;
//	}
//	public void setUinString(String uinString) {
//		this.uinString = uinString;
//	}
//	public String getNoString() {
//		return noString;
//	}
//	public void setNoString(String noString) {
//		
//		this.noString = checkIfEmpty(noString);
//	}
//	public String getNameString() {
//		return nameString;
//	}
//	public void setNameString(String nameString) {
//		this.nameString = nameString;
//	}
//	public String getGainString() {
//		return gainString;
//	}
//	public void setGainString(String gainString) {
//		this.gainString = changeGainString(gainString);
//		//判断涨跌
//		colorType = RankingItem.getColorFromGain(gainString);
//	}
//	
//	public int getColorType() {
//		return colorType;
//	}
//	
//	public String getFansNumString() {
//		return fansNumString;
//	}
//	public void setFansNumString(String fansNumString) {
//		this.fansNumString = checkIfEmpty(fansNumString);
//	}
//	public String getFocusNumString() {
//		return focusNumString;
//	}
//	public void setFocusNumString(String focusNumString) {
//		this.focusNumString = checkIfEmpty(focusNumString);
//	}
//	
//	//判断是否为空 为空则返回0
//	private String checkIfEmpty(String src) {
//		if (src.equals("") || src.equals(" ")) {
//			return "0";
//		}
//		return src;
//	}
//	public String getHeadurl() {
//		return headurl;
//	}
//	public void setHeadurl(String headurl) {
//		this.headurl = UrlConstants.getImgUrlFromKey(headurl);
//	}
//	
//	//设置view
//	public static void setViewWithItem(View convertView,RankingItem mData) {
//		TextView noTextView = (TextView)convertView.findViewById(R.id.tv_ranking_no);
//		noTextView.setText(mData.getNoString());
//		
//		//头像
//		NetworkImageView headView = (NetworkImageView) convertView.findViewById(R.id.iv_ranking_head);
//		UniversalRequest.setHeadImg(headView,convertView.getContext(),
//				mData.getSex(),mData.getHeadurl());
//		
//		//名字
//		TextView nameTextView = (TextView)convertView.findViewById(R.id.tv_ranking_name);
//		nameTextView.setText(mData.getNameString());
//		
//		//收益率
//		TextView gainTextView = (TextView)convertView.findViewById(R.id.tv_ranking_gain);
//		gainTextView.setText(mData.getGainString());
//		//涨跌
//		if (mData.getColorType() == GAIN_COLOR_RISE) {
//			//红
//			gainTextView.setTextColor(
//					convertView.getResources().getColor(R.color.stock_rise));
//		}
//		else if (mData.getColorType() == GAIN_COLOR_SLUMPED){
//			//绿
//			gainTextView.setTextColor(
//					convertView.getResources().getColor(R.color.stock_slumped));
//		}
//		else {
//			gainTextView.setTextColor(
//					convertView.getResources().getColor(R.color.black));
//		}
//		
//		//粉丝
//		TextView fansTextView = (TextView)convertView.findViewById(R.id.tv_ranking_fans);
//		fansTextView.setText(mData.getFansNumString());
//		
//		//关注
//		TextView fcousTextView = (TextView)convertView.findViewById(R.id.tv_ranking_focus);
//		fcousTextView.setText(mData.getFocusNumString());
//	}
	
	
	
}
