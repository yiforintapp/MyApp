package com.leo.appmaster.mgr.impl;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.WifiSecurityEvent;
import com.leo.appmaster.mgr.WifiSecurityManager;
import com.leo.appmaster.ui.SelfDurationToast;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;
import com.leo.appmaster.utils.WifiAdmin;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;
import com.leo.appmaster.wifiSecurity.WifiSettingActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class WifiSecurityManagerImpl extends WifiSecurityManager {
    private static final int TOAST_SHOW_TIME = 5000;
    private static final int DONT_SHOW_PG_START = 33;
    private static final String ACTIVITYNAME = "WifiSecurityActivity";
    private static final String SCAN_WIFI_NAME = "scan_wifi_name";
    private WifiAdmin wifiManager;
    private boolean mLastScanState = false;
    private long lastTimeIn;
    private long wifiToastLastIn;
//    private boolean isStartPG = false;


//    private android.os.Handler mHandler = new android.os.Handler() {
//        public void handleMessage(android.os.Message msg) {
//            switch (msg.what) {
//                case DONT_SHOW_PG_START:
//                    isStartPG = false;
//                    break;
//            }
//        }
//    };

    public WifiSecurityManagerImpl() {
        wifiManager = WifiAdmin.getInstance(mContext);

        //wifi change
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        LeoLog.d("testwifiBor", "onCreate");
//        isStartPG = true;
        mContext.registerReceiver(new ConnectionChangeReceiver(), filter);

        lastTimeIn = System.currentTimeMillis();
    }

    @Override
    public void onDestory() {

    }

    @Override
    public boolean isWifiOpen() {
        boolean isOpen = wifiManager.getWifiState();
        if (isOpen) {
            LeoLog.d("testWifiImp", "wifi is open");
        } else {
            LeoLog.d("testWifiImp", "wifi is close");
        }
        return isOpen;
    }

    @Override
    public String getWifiName() {
        String name = wifiManager.getSSID();
        LeoLog.d("testWifiImp", "name is : " + name);
        return name;
    }

    @Override
    public int getLinkSpeed() {
        return 0;
    }

    @Override
    public int getWifiSignal() {
        int k;
        int signal = Math.abs(wifiManager.getRssi());
        if (signal > 0 && signal <= 50) {
            //信号很强
            k = 1;
        } else if (signal > 50 && signal <= 70) {
            //信号强
            k = 2;
        } else if (signal > 70 && signal <= 80) {
            //信号一般
            k = 3;
        } else if (signal > 80 && signal <= 100) {
            //信号差
            k = 4;
        } else {
            //无信号
            k = 5;
        }
        LeoLog.d("testWifiImp", "Signal is : " + k);
        return k;
    }

    /*
    SECURITY_NONE = 0;
    SECURITY_WEP = 1;
    SECURITY_PSK = 2;
    SECURITY_EAP = 3;
     */
    @Override
    public int getWifiSafety() {
        int mType = wifiManager.getSecurity();
        LeoLog.d("testWifiImp", "当前网络安全性：" + mType);
        return mType;
    }

    @Override
    public boolean getIsWifi() {
        Context context = mContext.getApplicationContext();
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info;
        if (connectivity != null) {
            info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {
                        LeoLog.d("testWifiImp", "is wifi now");
                        return true;
                    }
                }
            }
        }
        LeoLog.d("testWifiImp", "no wifi connect");
        return false;
    }

    @Override
    public void destoryPing() {
        try {
            if (ps != null && ps.size() > 0) {
                LeoLog.d("testPing", "destoryPing");
                for (int i = 0; i < ps.size(); i++) {
                    Process p = ps.get(i);
                    p.destroy();
                }
                ps.clear();
            }
        } catch (Exception e) {
            LeoLog.d("testPing", "catch destoryPing");
            e.printStackTrace();
        }

    }


    private List<Process> ps = new ArrayList<Process>();

    public boolean pingOneHost(String host) {
        LeoLog.d("testPing", "ping host : " + host);
        long start = System.currentTimeMillis();
        int status = 99;
        Process p;
        boolean isConnect = false;
        //ping 2 times
        int pingNum = 2;
        //time out 6s
        int timeout = 6;
        try {
            p = Runtime.getRuntime().exec("/system/bin/ping -c " + pingNum + " " + host);
//            p = Runtime.getRuntime().exec("/system/bin/ping -c " + pingNum + " -w " + timeout + " " + host);
            ps.add(p);
            status = p.waitFor();
            if (status == 0) {
                isConnect = true;
            } else {// status != 0

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        LeoLog.d("testPing", host + " time cost : " + (end - start) + " , status is : " + status);
        return isConnect;
    }

    @Override
    public boolean getLastScanState() {
        return mLastScanState;
    }

    @Override
    public int getWifiState() {
        //wifi open?
        boolean isWifiOpen = isWifiOpen();
        //connect wifi?
        boolean isSelectWifi = getIsWifi();
        if (!isWifiOpen || !isSelectWifi) {
            //无连接WiFi
            return NO_WIFI;
        }

        int passWordType = getWifiSafety();
        if (passWordType == 0) {
            //WiFi有风险
            return NOT_SAFE;
        } else {
            //WiFi安全
            return SAFE_WIFI;
        }

    }

    public void setWifiScanState(boolean isScan) {
        this.mLastScanState = isScan;
    }

    public class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LeoLog.d("testwifiBor", "onReceive");
            mLastScanState = false;
            try {
                boolean needToHandle = false;
                String action = intent.getAction();
                if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    NetworkInfo.State curState = info.getState();
                    LeoLog.d("testWifiPart", "[NETWORK_STATE_CHANGED_ACTION] broadcast, state=" + curState);
                    if (curState == NetworkInfo.State.DISCONNECTED) {
                        needToHandle = true;
                    }
                }

                if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    LeoLog.d("testWifiPart", "[CONNECTIVITY_ACTION] -> ?????????????");
                    if (intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false)) {
                        LeoLog.d("testWifiPart", "[CONNECTIVITY_ACTION] -> EXTRA_IS_FAILOVER");
                    } else {
                        if (getIsWifi()) {
                            LeoLog.d("testWifiPart", "[CONNECTIVITY_ACTION] -> Wifi Connected!");
                            needToHandle = true;
                        }
                    }
                }

                //first time start PG do not go this
//                if (needToHandle && !isStartPG) {
                if (needToHandle) {
                    handleWifiChange();
                }
//                else {
//                    mHandler.sendEmptyMessageDelayed(DONT_SHOW_PG_START, 1500);
//                }
            } catch (Exception e) {
                e.printStackTrace();
//                isStartPG = false;
            }
        }
    }

    /**
     * do something when WIFI/connectivity change
     */
    private void handleWifiChange() {
        if (Utilities.isAppOnTop(mContext)) {
            LeoLog.d("testWifiPart", "App on Top");
            if (!Utilities.isActivityOnTop(mContext, WifiSecurityActivity.class.getName())) {
                boolean isShouldShow = PreferenceTable.getInstance().
                        getBoolean(WifiSettingActivity.IS_SHOW_WIFI_SAFE, true);
                if (isShouldShow) {
                    LeoLog.d("testWifiPart", "ready to show Toast");
                    ShowWifiSateToast();
                }
            }
        } else {
            LeoLog.d("testWifiPart", "App Not on Top");
            boolean isShouldShow = PreferenceTable.getInstance().
                    getBoolean(WifiSettingActivity.IS_SHOW_WIFI_SAFE, true);
            if (isShouldShow) {
                LeoLog.d("testWifiPart", "ready to show Toast");
                ShowWifiSateToast();
            }
        }
        LeoEventBus.getDefaultBus().postSticky(
                new WifiSecurityEvent(EventId.EVENT_WIFISECURITY, WIFITAG));
    }

    private void ShowWifiSateToast() {
        long nowIn = System.currentTimeMillis();
        int wifiState = getWifiState();
        if (wifiState != NO_WIFI) {
            LeoLog.d("testWifiPart", "we have WiFi!!!!!!");
            String wifiName = getWifiName();
            if (wifiName.startsWith("\"")) {
                //去除双引号
                wifiName = wifiName.substring(wifiName.indexOf("\"") + 1,
                        wifiName.lastIndexOf("\""));
            }

            boolean isWifiScaned = isScanAlready(wifiName);

            if (nowIn - wifiToastLastIn > 5000 && !isWifiScaned) {
                if (wifiState == NOT_SAFE) {
                    LeoLog.d("testWifiPart", "wifiState is :" + wifiState + " , show NOT_SAFE");
                    SelfDurationToast.makeText(mContext, wifiName, TOAST_SHOW_TIME, wifiState).show();
                    saveWifiName(wifiName);
                } else {
                    LeoLog.d("testWifiPart", "wifiState is :" + wifiState + " , show SAFE");
                    SelfDurationToast.makeText(mContext, wifiName, TOAST_SHOW_TIME, wifiState).show();
                    saveWifiName(wifiName);
                }
                wifiToastLastIn = nowIn;
            }
        } else {
            LeoLog.d("testWifiPart", "NO WiFi!!!!!!");
        }
    }

    private boolean isScanAlready(String wifiName) {
        boolean isScan = false;
        String wifiNames = PreferenceTable.getInstance().getString(SCAN_WIFI_NAME);
        LeoLog.d("testScanWifi", "wifiNames : " + wifiNames);
        if (wifiNames != null) {
            String[] nameStrings = wifiNames.split(";");
            for (int i = 0; i < nameStrings.length; i++) {
                String name = nameStrings[i];
                if (name != null && wifiName.equals(name)) {
                    isScan = true;
                }
            }
            return isScan;
        } else {
            return isScan;
        }
    }

    private void saveWifiName(String wifiName) {
        if (wifiName != null) {
            String wifiNames = PreferenceTable.getInstance().getString(SCAN_WIFI_NAME);
            LeoLog.d("testScanWifi", "wifiNames : " + wifiNames);
            if (wifiNames != null) {
                wifiNames = wifiNames + ";" + wifiName;
            } else {
                wifiNames = wifiName;
            }
            PreferenceTable.getInstance().putString(SCAN_WIFI_NAME, wifiNames);
            LeoLog.d("testScanWifi", "save wifiNames : " + wifiNames);
        }
    }

}
