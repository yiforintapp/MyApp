
package com.leo.appmater.globalbroadcast;

import java.util.List;

import com.leo.analytics.LeoAgent;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.ADShowTypeRequestManager;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.wifichecker.wifi.APInfo;
import com.leo.wifichecker.wifi.WifiInfoFetcher;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ScreenOnOffListener extends BroadcastListener {

    public static final String TAG = "SCREEN ON OFF";
    public static final long mTwoDay = 48 * 60 * 60 * 1000;

    public final void onEvent(String action) {
        if (Intent.ACTION_SCREEN_OFF.equals(action)
                || Intent.ACTION_SCREEN_ON.equals(action)
                || Intent.ACTION_USER_PRESENT.equals(action)) {
            onScreenChanged(mIntent);
        }
    }

    @Override
    protected final IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        return filter;
    }

    /**
     * added, changed, removed
     */
    public void onScreenChanged(Intent intent) {
        /* 解锁手机加载iSwipe更新数据 */
        loadISwipeUpdateForOnScreen(intent);

        loadWifiData(intent);

    }

    private void loadWifiData(Intent intent)
    {
        final Context mContext = AppMasterApplication.getInstance();
        if (!AppUtil.isScreenLocked(mContext)
                && Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {

        } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            LeoLog.d("testOpenScreen", "ACTION_SCREEN_ON");

            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    // 开关
                    int isWifiSwitch = AppMasterPreference.getInstance(mContext)
                            .getIsWifiStatistics();
                    // 是否统计过
                    long isUploadData = AppMasterPreference.getInstance(mContext)
                            .getIsWifiStatisticsIsLoad();
                    LeoLog.d("testOpenScreen", "wifi统计开关 : " + isWifiSwitch);
                    LeoLog.d("testOpenScreen", "wifi是否统计过全量 : " + isUploadData);

                    WifiInfoFetcher.WifiFetcherListener listener = new
                            WifiInfoFetcher.WifiFetcherListener() {
                                @Override
                                public void onWifiChanged(List<APInfo> results) {

                                }
                            };

                    WifiInfoFetcher mWifiFetcher = WifiInfoFetcher.getInstance();
                    mWifiFetcher.init(mContext, listener);
                    mWifiFetcher.enableDebug(false);
                    mWifiFetcher.setMinDistanceUpdateInterval(1000 * 10);
                    mWifiFetcher.start();
                    List<APInfo> results = mWifiFetcher.getApInfoList();
                    // 第一次统计
                    if (results.size() > 0 && isWifiSwitch == 1 && isUploadData == 0) {
                        LeoLog.d("testOpenScreen", "第一次统计，全量上报");
                        addEvent(mContext, results);
                    }

                    long nowTime = System.currentTimeMillis();
                    // 后续统计，增量上报
                    if (results.size() > 0 && isWifiSwitch == 1 && isUploadData > 0
                            && (nowTime - isUploadData > mTwoDay)) {
                        List<APInfo> newresults = mWifiFetcher.prepareUploadData();

                        if (newresults.size() > 0) {
                            LeoLog.d("testOpenScreen", "后续统计，增量上报！");
                            addEvent(mContext, newresults);
                            mWifiFetcher.afterUpload();
                        } else {
                            LeoLog.d("testOpenScreen", "暂无新增！");
                        }
                    }
                }
            });
        }
    }

    private void addEvent(final Context mContext, List<APInfo> results) {
        try {
            String abc = "";
            for (int i = 0; i < results.size(); i++) {
                abc = abc + results.get(i).toString() + ";";
                if (i == results.size() - 1) {
                    break;
                }
            }
            LeoLog.d("testOpenScreen", abc);
            LeoAgent.addEvent("wifi_upload", abc);
            AppMasterPreference.getInstance(mContext).setIsWifiStatisticsIsLoad(
                    System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadISwipeUpdateForOnScreen(Intent intent) {
        Context mContext = AppMasterApplication.getInstance();
        boolean isLoadData = ISwipUpdateRequestManager.getInstance(mContext).isLoadIswipeData();
        if (!AppUtil.isScreenLocked(mContext)
                && Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            // Log.d(Constants.RUN_TAG, "开屏");
            if (isLoadData) {
                if (ISwipUpdateRequestManager.getInstance(mContext).isUseIswipUser()) {
                    ISwipUpdateRequestManager.getInstance(mContext)
                            .showIswipeAlarmNotificationHandler();
                }
                ISwipUpdateRequestManager.getInstance(mContext).loadIswipCheckNew();
            }
        } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            // Log.d(Constants.RUN_TAG, "解锁");
            if (isLoadData) {
                boolean isUseIswip = ISwipUpdateRequestManager.getInstance(mContext)
                        .isUseIswipUser();
                int iSwipeUpdateFlag = AppMasterPreference.getInstance(mContext)
                        .getIswipUpdateFlag();
                boolean isIswipUpdate = (iSwipeUpdateFlag == 1);
                if (isUseIswip && isIswipUpdate) {
                    ISwipUpdateRequestManager.getInstance(mContext)
                            .showIswipeAlarmNotificationHandler();
                }
                ISwipUpdateRequestManager.getInstance(mContext).loadIswipCheckNew();
            }
        }
    }
}
