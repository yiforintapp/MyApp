package com.zlf.appmaster.bean;

import java.text.DecimalFormat;


/**
 * 粉丝关注 类
 * @author Yushian
 */
public class FansFocus {

	public static final int FOCUS_TYPE_NOT = 0; 	//未关注
	public static final int FOCUS_TYPE_FOCUSED = 1;	//已关注
	public static final int FOCUS_TYPE_FANS = 2;	//粉丝
	public static final int FOCUS_TYPE_ALL = 3;		//互相关注

	public static final int GAIN_COLOR_ZERO = 0;
	public static final int GAIN_COLOR_RISE = 1;
	public static final int GAIN_COLOR_SLUMPED = 2;
	//姓名
	private String name;
	//收益率
	private String gain;
	//头像
//	private String headImgKey;
	private int colorType;
	//是否关注 
	private int focusType;
	private String uin;
	private String sex;
	
	public String getSexText(){
        if (sex.equals("1"))
            return "男";
        return "女";
    }
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getUin() {
		return uin;
	}
	
	public Long getLongUin() {
		return Long.parseLong(uin);
	}
	
	public void setUin(String uin) {
		this.uin = uin;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGain() {
		return gain;
	}
	public void setGain(String gain) {
		this.gain = changeGainString(gain);
		
		//判断涨跌
		colorType = getColorFromGain(gain);
	}
	
	public int getColorType() {
		return colorType;
	}
	
//	public String getHeadImgKey() {
//		return headImgKey;
//	}
//	public void setHeadImgKey(String key) {
//		this.headImgKey = key;
//	}
	
	public int getFocusType() {
		return focusType;
	}
	public void setFocusType(int focusType) {
		this.focusType = focusType;
	}
	
	//转换收益
	public static String changeGainString(String rate){
		double rateFloat = Double.parseDouble(rate);
        DecimalFormat df = new DecimalFormat("0.00%");
        String r = df.format(rateFloat);
		return r;
	}
	
	//根据收益获取颜色
	public static int getColorFromGain(String rate) {
		//只取到小数点后两位
		double rateFloat = Double.parseDouble(rate);
		DecimalFormat df = new DecimalFormat("0.00");
        String r = df.format(rateFloat*100);
        rateFloat = Double.parseDouble(r);
		
		if(rateFloat>0){
			return GAIN_COLOR_RISE;
		}
		else if(rateFloat<0){
			return GAIN_COLOR_SLUMPED;
		}
		else {
			return GAIN_COLOR_ZERO;
		}
	}
	
}
