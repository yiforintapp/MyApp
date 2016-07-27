
package com.zlf.appmaster.model.extra;

import com.zlf.appmaster.model.extra.AppWallUrlBean;

import java.util.List;

//APP墙每个应用的信息实体
public class AppWallBean {
    private String image;
    private String appName;
    private String appDesc;
    private List<AppWallUrlBean> download;
    private String appPackageName;
    private long appsize;
    private float rating;

    public AppWallBean(String image, String appName, String appDesc, List<AppWallUrlBean> download,
            String appPackageName,long appsize,float rating) {
        super();
        this.image = image;
        this.appName = appName;
        this.appDesc = appDesc;
        this.download = download;
        this.appPackageName = appPackageName;
        this.appsize = appsize;
        this.rating = rating;
    }

    public AppWallBean() {
        super();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getAppsize() {
        return appsize;
    }

    public void setAppsize(long appsize) {
        this.appsize = appsize;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppDesc() {
        return appDesc;
    }

    public void setAppDesc(String appDesc) {
        this.appDesc = appDesc;
    }

    public List<AppWallUrlBean> getDownload() {
        return download;
    }

    public void setDownload(List<AppWallUrlBean> download) {
        this.download = download;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

}
