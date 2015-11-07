package com.leo.appmaster.mgr;

import com.android.volley.toolbox.StringRequest;

/**
 * WIFI安全
 * Created by Jasper on 2015/9/28.
 */
public abstract class WifiSecurityManager extends Manager {
    public final static String WIFITAG = "wifi_change";
    public final static int NO_WIFI = 1;
    public final static int NOT_SAFE = 2;
    public final static int SAFE_WIFI = 3;

    @Override
    public String description() {
        return MgrContext.MGR_WIFI_SECURITY;
    }

    public abstract boolean isWifiOpen();

    /**
     *
     */
    public abstract String getWifiName();

    /**
     *
     */
    public abstract int getLinkSpeed();

    /**
     *
     */
    public abstract int getWifiSignal();

    /**
     *
     */
    public abstract int getWifiSafety();

    /**
     *
     */
    public abstract boolean getIsWifi();

    public abstract void destoryPing();
    public abstract boolean pingOneHost(String host);

    /**
     * 上次扫描结果
     */
    public abstract boolean getLastScanState();

    /**
     * 获取Wifi状态
     */
    public abstract int getWifiState();

    public void setWifiScanState(boolean isScanAlready) {

    }
}
