package com.leo.appmaster.ad;

public class LEONativeAdData {
	
	/*服務器返回的狀態代碼*/
	protected int code;
	/*服務器返回的狀態msg*/
	protected String msg; 
	
    /* 广告标题 */
    protected String mTitle;
    /* 按钮文字 */
    protected String mTitleForButton;
    /* 广告描述 */
    protected String mDescription;

	/* 图标 */
    protected String mIconURL;
    /* 预览图，大图 */
    protected String mPreviewURL;

    public String getAppName() {
        return mTitle;
    }

    public String getAdCall() {
        return mTitleForButton;
    }

    public String getAppDesc() {
        return mDescription;
    }

    public String getIconUrl() {
        return mIconURL;
    }

    public String getImageUrl() {
        return mPreviewURL;
    }

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}
	
}
