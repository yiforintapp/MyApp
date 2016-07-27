
package com.zlf.appmaster.utils;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

public class WifiAdmin {
    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;

    private static WifiAdmin sInstance;
    private static byte[] sLock = new byte[1];
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mWifiList;
    private List<WifiConfiguration> mWifiConfigurations;
    private WifiLock mWifiLock;

    public static WifiAdmin getInstance(Context mContext) {
        if (sInstance == null) {
            synchronized (sLock) {
                if (sInstance == null) {
                    sInstance = new WifiAdmin(mContext);
                }
            }
        }

        return sInstance;
    }

    private WifiAdmin(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        mWifiInfo = mWifiManager.getConnectionInfo();
//        startScan();
    }


    /**
     * 打开wifi
     */
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭wifi
     */
    public void closeWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 检查当前wifi状态
     */
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    /**
     * 锁定wifiLock
     */
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    /**
     * 解锁wifiLock
     */
    public void releaseWifiLock() {
        // 判断是否锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    /**
     * 创建一个wifiLock
     */
    public void createWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("test");
    }

    /**
     * 得到配置好的网络
     */
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfigurations;
    }

    /**
     * 指定配置好的网络进行连接
     */
    public void connetionConfiguration(int index) {
        if (index > mWifiConfigurations.size()) {
            return;
        }
        /** 连接配置好指定ID的网络 */
        mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId, true);
    }

    /**
     * 开始扫描
     */
    public void startScan() {
        mWifiManager.startScan();
        /** 得到扫描结果 */
        mWifiList = mWifiManager.getScanResults();
        /** 得到配置好的网络连接 */
        mWifiConfigurations = mWifiManager.getConfiguredNetworks();
    }

    // 得到网络列表
    public List<ScanResult> getWifiList() {
        return mWifiList;
    }

    /**
     * 查看扫描结果
     */
    public StringBuffer lookUpScan() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mWifiList.size(); i++) {
            sb.append("Index_" + Integer.valueOf(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            sb.append((mWifiList.get(i)).toString()).append("\n");
        }
        return sb;
    }

    /**
     * 获取Mac地址
     */
    public String getMacAddress() {
        if (mWifiManager != null) {
            mWifiInfo = mWifiManager.getConnectionInfo();
            return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
        } else {
            return "NULL";
        }
    }

    /**
     * 获取BSSID
     */
    public String getBSSID() {
        if (mWifiManager != null) {
            mWifiInfo = mWifiManager.getConnectionInfo();
            return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
        } else {
            return "NULL";
        }
    }

    public String getSSID() {
        if (mWifiManager != null) {
            mWifiInfo = mWifiManager.getConnectionInfo();
            return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
        } else {
            return "NULL";
        }
    }

    public int getLinkSpeed() {
        if (mWifiManager != null) {
            mWifiInfo = mWifiManager.getConnectionInfo();
            return (mWifiInfo == null) ? 0 : mWifiInfo.getLinkSpeed();
        } else {
            return 0;
        }
    }

    public int getRssi() {
        if (mWifiManager != null) {
            mWifiInfo = mWifiManager.getConnectionInfo();
            return (mWifiInfo == null) ? 0 : mWifiInfo.getRssi();
        } else {
            return 0;
        }
    }

    public boolean getWifiState() {
        if (mWifiManager != null) {
            mWifiInfo = mWifiManager.getConnectionInfo();
            if (mWifiManager != null) {
                if (mWifiManager.isWifiEnabled()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public int getSecurity() {
        List<WifiConfiguration> wifiConfigList;
        int mType = 0;
        try {
            if (mWifiManager != null) {
                wifiConfigList = mWifiManager.getConfiguredNetworks();
                mWifiInfo = mWifiManager.getConnectionInfo();

                for (WifiConfiguration wifiConfiguration : wifiConfigList) {
                    //配置过的SSID
                    String configSSid = wifiConfiguration.SSID;
                    configSSid = configSSid.replace("\"", "");

                    //当前连接SSID
                    String currentSSid = mWifiInfo.getSSID();
                    currentSSid = currentSSid.replace("\"", "");

                    //比较networkId，防止配置网络保存相同的SSID
                    if (currentSSid.equals(configSSid) && mWifiInfo.getNetworkId() == wifiConfiguration.networkId) {
                        mType = getSecurity(wifiConfiguration);
                    }
                }
            }
        } catch (Exception e) {
            return mType;
        }
        return mType;
    }

    static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    /**
     * 获取IP
     */
    public int getIpAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    /**
     * 得到连接的ID
     */
    public int getNetWordId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    /**
     * 得到wifiInfo的所有信息
     */
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    /** */
    // 添加一个网络并连接
    public void addNetWork(WifiConfiguration configuration) {
        int wcgId = mWifiManager.addNetwork(configuration);
        mWifiManager.enableNetwork(wcgId, true);
    }

    /**
     * 断开指定ID的网络
     */
    public void disConnectionWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }
}
