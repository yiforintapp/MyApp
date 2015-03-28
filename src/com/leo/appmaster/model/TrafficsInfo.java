
package com.leo.appmaster.model;


import com.leo.appmaster.utils.ManagerFlowUtils;

import android.graphics.drawable.Drawable;

public class TrafficsInfo{
    private String packName; // 包名
    private Drawable icon; // 应用图标
    private String appName; // 应用名
    private String tx; // 上传流量
    private String rx; // 下载流量
    private String app_all_traffic; // 应用总流量
    private int app_all_traffic_for_show;

    public Integer getApp_all_traffic_for_show() {
        return app_all_traffic_for_show;
    }

    public void setApp_all_traffic_for_show(Integer app_all_traffic_for_show) {
        this.app_all_traffic_for_show = app_all_traffic_for_show;
    }

    public String getApp_all_traffic() {
        return app_all_traffic;
    }

    public void setApp_all_traffic(long app_all_traffic) {
        this.app_all_traffic = ManagerFlowUtils.refreshTraffic_home_app(app_all_traffic);
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTx() {
        
        return tx;
    }

    public void setTx(long tx) {
        this.tx = ManagerFlowUtils.refreshTraffic_home_app(tx);
    }

    public String getRx() {
        return rx;
    }

    public void setRx(long rx) {
        this.rx = ManagerFlowUtils.refreshTraffic_home_app(rx);
    }


}
