package com.zlf.appmaster.model;

/**
 * Created by Administrator on 2016/9/19.
 */
public class HomeBannerInfo extends  AppInfo {
    public String mIvUrl;
    public String mOpenUrl;

    public HomeBannerInfo() {

    }

    //构造函数
    public HomeBannerInfo(String ivUrl, String openUrl) {
        this.mIvUrl = ivUrl;
        this.mOpenUrl = openUrl;
    }
}
