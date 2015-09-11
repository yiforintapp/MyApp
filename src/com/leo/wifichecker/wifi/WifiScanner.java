package com.leo.wifichecker.wifi;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.leo.wifichecker.utils.LogEx;

/**
 * Created by luqingyuan on 15/9/7.
 */
public class WifiScanner {
    private static final String TAG = "WifiScanner";
    private WifiManager mWifiManager;
    private WifiInfoFetcher.InnerFetcherListener mListener;

    public WifiScanner(Context context, WifiInfoFetcher.InnerFetcherListener lis) {
        mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        mListener = lis;
    }

    /**
     * 开始扫描
     * @param context
     */
    public void startScan(Context context) {
        LogEx.enter();
        final IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(mReceiver, filter);
        mWifiManager.startScan();
        LogEx.leave();
    }

    /**
     * 停止扫描
     * @param context
     */
    public void stopScan(Context context) {
        LogEx.enter();
        try {
            context.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            LogEx.w("wifi stopScan", e.getMessage());
        }
        LogEx.leave();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            LogEx.d(TAG,"receiver action = " +action);
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                notifyScanResult(mWifiManager.getScanResults());
            }
        }
    };

    private void notifyScanResult(List<ScanResult> scanResults) {
        if(scanResults == null) {
            return;
        }
        List<APInfo> infos = new ArrayList<APInfo>();
        for (ScanResult result : scanResults) {
            if(result != null) {
                APInfo info = new APInfo();
                info.mSSID = TextUtils.isEmpty(result.SSID)?null:
                        APInfo.stripLeadingAndTrailingQuotes(result.SSID);
                info.mBSSID = TextUtils.isEmpty(result.BSSID)?null:
                        APInfo.stripLeadingAndTrailingQuotes(result.BSSID);
                info.setCapabilities(result.capabilities);
                info.mFrequency = result.frequency;
                info.mLevel =WifiManager.calculateSignalLevel(result.level,5);
                infos.add(info);
            }
        }
        if(mListener != null) {
            mListener.onWifiChanged(infos, WifiInfoFetcher.InnerFetcherListener.INFO_FROM_WIFI_SCANNER);
        }
    }
}
