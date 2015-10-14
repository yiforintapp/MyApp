
package com.leo.appmater.globalbroadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.utils.AppUtil;

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

//        loadWifiData(intent);

    }

//    private void loadWifiData(Intent intent)
//    {
//        final Context mContext = AppMasterApplication.getInstance();
//        if (!AppUtil.isScreenLocked(mContext)
//                && Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
//
//        } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
//            LeoLog.d("testOpenScreen", "ACTION_SCREEN_ON");
//
//            ThreadManager.executeOnAsyncThread(new Runnable() {
//                @Override
//                public void run() {
//                    // 开关
//                    int isWifiSwitch = AppMasterPreference.getInstance(mContext)
//                            .getIsWifiStatistics();
//                    // 是否统计过
//                    long isUploadData = AppMasterPreference.getInstance(mContext)
//                            .getIsWifiStatisticsIsLoad();
//                    LeoLog.d("testOpenScreen", "wifi统计开关 : " + isWifiSwitch);
//                    LeoLog.d("testOpenScreen", "wifi是否统计过全量 : " + isUploadData);
//                    // isWifiSwitch = 1;
//                    WifiInfoFetcher.WifiFetcherListener listener = new
//                            WifiInfoFetcher.WifiFetcherListener() {
//                                @Override
//                                public void onWifiChanged(List<APInfo> results) {
//
//                                }
//                            };
//
//                    WifiInfoFetcher mWifiFetcher = WifiInfoFetcher.getInstance();
//                    mWifiFetcher.init(mContext, listener);
//                    mWifiFetcher.enableDebug(false);
//                    mWifiFetcher.setMinDistanceUpdateInterval(1000 * 10);
//                    mWifiFetcher.start();
//
//                    if (isWifiSwitch == 1) {
//                        List<APInfo> newresults = mWifiFetcher.prepareUploadData();
//                        long nowTime = System.currentTimeMillis();
//                        if (newresults.size() > 0
//                                && nowTime - isUploadData > mTwoDay) {
//                            if (isUploadData == 0) {
//                                LeoLog.d("testOpenScreen", "第一次统计，全量上报");
//                            } else {
//                                LeoLog.d("testOpenScreen", "后续统计，增量上报！");
//                            }
//                            addEvent(mContext, newresults);
//                        } else if (newresults.size() == 0) {
//                            if (isUploadData == 0) {
//                                LeoLog.d("testOpenScreen", "未能获取到wifi信息！");
//                            } else {
//                                LeoLog.d("testOpenScreen", "暂无新增！");
//                            }
//                        }
//                        mWifiFetcher.afterUpload(true);
//                    }
//                }
//            });
//        }
//    }

//    private void addEvent(final Context mContext, List<APInfo> results) {
//        try {
//            String abc = "";
//            for (int i = 0; i < results.size(); i++) {
//                abc = abc + results.get(i).toString() + ";";
//                if (i == results.size() - 1) {
//                    break;
//                }
//            }
//
//            boolean isRoot = new ExecTerminal().checkSu();
//            if (isRoot) {
//                SDKWrapper
//                        .addEvent(mContext, SDKWrapper.P1, "wifi_info", "wifi_info_pw");
//                LeoLog.d("testOpenScreen", "已root手机");
//            } else {
//                SDKWrapper
//                        .addEvent(mContext, SDKWrapper.P1, "wifi_info", "wifi_info_usual");
//                LeoLog.d("testOpenScreen", "非root手机");
//            }
//
//            LeoLog.d("testOpenScreen", abc);
//            LeoAgent.addEvent("wifi_upload", abc);
//            AppMasterPreference.getInstance(mContext).setIsWifiStatisticsIsLoad(
//                    System.currentTimeMillis());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

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
