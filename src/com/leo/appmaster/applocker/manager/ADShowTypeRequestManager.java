
package com.leo.appmaster.applocker.manager;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.HttpRequestAgent.RequestListener;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.schedule.FetchScheduleJob.FetchScheduleListener;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.mobvista.sdk.m.core.entity.Campaign;

public class ADShowTypeRequestManager {
    private static final String TAG = "ADShowTypeRequestManager";

//    private static final String AD_SHOW_TYPE = "a";// 广告的展示方式，如半屏广告，banner广告等等
    private static final String UFO_ANIM_TYPE = "b";
    private static final String THEME_CHANCE_AFTER_UFO = "c";
//    private static final String AD_AFTER_ACCELERATING = "d";
    private static final String AD_AFTER_PRIVACY_PROTECTION = "e";
    private static final String AD_AT_APPLOCK_FRAGMENT = "f";
    private static final String AD_AT_THEME = "g";
    private static final String GIFTBOX_UPDATE = "h";
    private static final String VERSION_UPDATE_AFTER_UNLOCK = "i";
    private static final String APP_STATISTICS = "j";
    private static final String WIFI_STATISTICAL = "l";
    private static final String AD_LOCK_WALL = "m";
    /* 2.12以后版本后台广告形式 */
    private static final String AD_NEW_SHOW_TYPE = "n";
    /* 2.14以后版本后加速广告位 */
    private static final String AD_NEW_ACCELERATING = "p";
    private static final String PACKAGEDIR = "/system/";
    /* 广告展示的形式 */
    private static final String[] LOCAL_AD_SHOW_TYPE = {
            "1", "2", "3", "5", "6"
    };
    /*3.0  锁屏大图广告 */
    private static final String AD_LARGE_BANNER_PROBABILITY = "q";
    /* 3.0广告开关 */
    private static final String AD_WIFI_SCAN = "r";
    /* 3.1 国内渠道广告总开关 */
    private static final String AD_MAIN_SWITCHER = "s";
    /* 3.1 拉取广告时间间隔 */
    private static final String AD_FETCH_INTERVAL = "t";
    /* 3.2 intruder */
    private static final String AD_INTRUDER = "v";
    /* 3.2 after privacy scan */
    private static final String AD_AFTER_SCAN = "u";
    /* 3.3 充电屏保广告位 */
    private static final String AD_ON_SCREEN_SAVER = "w";

    /* 如果后台配置本地没有的广告形式时，默认广告类型 */
    public static final int DEFAULT_AD_SHOW_TYPE = 3;
    /* 关闭Lock页所有广告指令 */
    public static final int CLOSE_LOCK_AD_SHOW = 4;
    /* 潜水艇广告指令号码 */
    public static final int SUBMARIN_AD_TYPE = 6;
    private static ADShowTypeRequestManager mInstance;
    private Context mContext;
    private AppMasterPreference mSp;
    private SimpleDateFormat mDateFormate;
    // public boolean IsFromPush = false;
    public boolean mIsPushRequestADShowType = false;

    public static Campaign mCampaign;
    private FetchScheduleListener mListener;

    private ADShowTypeRequestManager(Context context) {
        mContext = context;
        mSp = AppMasterPreference.getInstance(mContext);
        mDateFormate = new SimpleDateFormat("yyyy-MM-dd");
    }

    public static synchronized ADShowTypeRequestManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ADShowTypeRequestManager(context);
        }
        return mInstance;
    }

    public void loadADCheckShowType(FetchScheduleListener listener) {
        mListener = listener;
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                requestADShowType();
            }
        });
    }

    public void requestADShowType() {
        LeoLog.i(TAG, "start requestADShowType....");
        UpdateADShowTypeRequestListener listenerr = new UpdateADShowTypeRequestListener(
                AppMasterApplication.getInstance());
        HttpRequestAgent.getInstance(AppMasterApplication.getInstance()).loadADShowType(
                listenerr,
                listenerr);

    }

    private int getJSIntValue(JSONObject object, String name, int def) {
        try {
            return object.getInt(name);
        } catch (Exception e) {
            return def;
        }
    }
    
    private boolean updateADMainConfig(JSONObject response) {
        // 中国大陆总开关
        boolean forceClose = false;
        if (AppMasterConfig.IS_FOR_MAINLAND_CHINA) {
            mSp.setADMainlandSwticher(getJSIntValue(response, AD_MAIN_SWITCHER, 0));
            forceClose = (mSp.getADMainlandSwticher()==0);
            LeoLog.d(TAG, "Switcher for Mainland, forceClose = " + forceClose);
        } else {
            LeoLog.i(TAG, "Global user, use normal switchers");
        }
        mSp.setADFetchInterval(getJSIntValue(response, AD_FETCH_INTERVAL,
                AppMasterPreference.DEFAULT_FETCH_INTERAL));
        return forceClose;
    }
    
    private void updateADInLockScreenConfig(JSONObject response ,boolean forceClose) {
        try {
            int value = forceClose ? CLOSE_LOCK_AD_SHOW : (response.getInt(AD_NEW_SHOW_TYPE));
            LeoLog.d("poha", "请求成功，广告展示形式是：" + response.getInt(AD_NEW_SHOW_TYPE));
            if (value != mSp.getADShowType()) {
                if (value == 1) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_banner");
                }
                if (value == 2) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_bannerpop");
                }
                if (value == 3) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_draw");
                }
                if (value == 4) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_none");
                }
                if (value == 5) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_superman");
                }
                if (value == 6) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_submarine");
                }
                List<String> list = Arrays.asList(LOCAL_AD_SHOW_TYPE);
                String adTypeString = String.valueOf(value);
                if (!(list.contains(adTypeString)) && value != CLOSE_LOCK_AD_SHOW) {
                    /* 满足两个条件：1,不再本地广告形式内，2，不为关闭广告指令 */
                    value = DEFAULT_AD_SHOW_TYPE;
                }
                mSp.setADShowType(value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        int currentType = mSp.getADShowType();
//        int adtype = currentType;
//        try {
//            adtype = response.getInt(AD_NEW_SHOW_TYPE);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return;
//        }
//        LeoLog.d("poha", "请求成功，广告展示形式是：" + adtype);
//        if (adtype != currentType) {
//            if (adtype == 1) {
//                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_banner");
//            }
//            if (adtype == 2) {
//                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_bannerpop");
//            }
//            if (adtype == 3) {
//                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_draw");
//            }
//            if (adtype == 4) {
//                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_none");
//            }
//            if (adtype == 5) {
//                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_superman");
//            }
//            if (adtype == 6) {
//                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_submarine");
//            }
//        }
//        /* 2.12版本加入，如果后台拉取到的广告形式本地没有，默认使用方式3 */
//        List<String> list = Arrays.asList(LOCAL_AD_SHOW_TYPE);
//        String adTypeString = String.valueOf(adtype);
//        if (!(list.contains(adTypeString)) && adtype != CLOSE_LOCK_AD_SHOW) {
//            /* 满足两个条件：1,不再本地广告形式内，2，不为关闭广告指令 */
//            adtype = DEFAULT_AD_SHOW_TYPE;
//        }
//        mSp.setADShowType(forceClose ? CLOSE_LOCK_AD_SHOW : adtype);
    }

    private void updateLargeADInLockScreenConfig(JSONObject response, boolean forceClose) {
        try {
            LeoLog.d("poha", "请求成功，解锁界面大图banner 直接显示的概率：" + response.getInt(AD_LARGE_BANNER_PROBABILITY));
            int value = forceClose ? 0 : (response.getInt(AD_LARGE_BANNER_PROBABILITY));
            if (value != mSp.getLockBannerADShowProbability()) {
                mSp.setLockBannerADShowProbability(value);
                if (value == 0) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "adv_picad_off");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        int largeBannerAdSwitch = mSp.getLockBannerADShowProbability();
//        try {
//            largeBannerAdSwitch = response.getInt(AD_LARGE_BANNER_PROBABILITY);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return;
//        }
//        mSp.setLockBannerADShowProbability(forceClose?0:largeBannerAdSwitch);
//        if (largeBannerAdSwitch == 0) {
//            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "adv_picad_off");
//        }
//        LeoLog.d("poha", "请求成功，解锁界面大图banner 直接显示的概率：" + mSp.getLockBannerADShowProbability());
    }

    private void updateUFOADConfig(JSONObject response, boolean forceClose) {
    /* UFO 广告位已被去掉
        try {
            LeoLog.d("poha", "请求成功，UFO动画形式是：" + response.getInt(UFO_ANIM_TYPE));
            LeoLog.d("poha","请求成功，UFO动画roll出主题概率：" + response.getInt(THEME_CHANCE_AFTER_UFO));
            int value = forceClose ? 0 : (response.getInt(UFO_ANIM_TYPE));
            int value2 = forceClose ? Integer.MAX_VALUE : (response.getInt(THEME_CHANCE_AFTER_UFO));

            if (value != mSp.getUFOAnimType()) {
                mSp.setLockBannerADShowProbability(value);
            }
            if (value2 != mSp.getThemeChanceAfterUFO()) {
                mSp.setLockBannerADShowProbability(value2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    */
    }
    
    private void updateADAfterAcceleratingConfig(JSONObject response, boolean forceClose) {
        try {
            LeoLog.d("poha", "请求成功，加速后出现广告：" + response.getInt(AD_NEW_ACCELERATING));
            int value = forceClose ? 0 : (response.getInt(AD_NEW_ACCELERATING));
            if (value != mSp.getADChanceAfterAccelerating()) {
                mSp.setADChanceAfterAccelerating(value);
                if (value == 1) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_toast_on");
                } else {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_toast_off");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
//        int currentType = mSp.getADChanceAfterAccelerating();
//        int adtype = currentType;
//        try {
//            adtype = response.getInt(AD_NEW_ACCELERATING);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return;
//        }
//        if (forceClose) {
//            mSp.setADChanceAfterAccelerating(0);
//        } else {
//            if (adtype != currentType) {
//                mSp.setADChanceAfterAccelerating(adtype);
//                if (mSp.getADChanceAfterAccelerating() == 1) {
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_toast_on");
//                } else {
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_toast_off");
//                }
//            }
//        }
//        LeoLog.d("poha", "请求成功，加速后出现广告：" + adtype);
    }
    
    private void updateADAtWifiScanConfig(JSONObject response, boolean forceClose) {
        try {
            LeoLog.d("poha", "请求成功，wifi扫描开关:" + response.getInt(AD_WIFI_SCAN));
            int value = forceClose ? 0 : (response.getInt(AD_WIFI_SCAN));
            if (value != mSp.getADWifiScan()) {
                mSp.setADWifiScan(value);
                if (value == 1) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "adv_wifi_on");
                } else {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "adv_wifi_off");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        if (forceClose) {
//            mSp.setADChanceAfterAccelerating(0);
//        } else {
//            try {
//                if (response.getInt(AD_WIFI_SCAN) != mSp.getADWifiScan()) {
//                    mSp.setADWifiScan(forceClose ? 0 : (response.getInt(AD_WIFI_SCAN)));
//                    if (mSp.getADWifiScan() == 1) {
//                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "adv_wifi_on");
//                    } else {
//                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "adv_wifi_off");
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//                return;
//            }
//        }
    }
    
    private void updateADAtPrivacyProtectionConfig(JSONObject response, boolean forceClose) {
        try {
            LeoLog.d("poha", "请求成功，隐私保护后出现广告的开关：" + response.getInt(AD_AFTER_PRIVACY_PROTECTION));
            int value = forceClose ? 0 : (response.getInt(AD_AFTER_PRIVACY_PROTECTION));
            if (value != mSp.getIsADAfterPrivacyProtectionOpen()) {
                mSp.setIsADAfterPrivacyProtectionOpen(value);
                if (value == 1) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "adv_scanRST_on");
                } else {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "adv_scanRST_off");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        
//        int currentType = mSp.getIsADAfterPrivacyProtectionOpen();
//        int adtype = currentType;
//        try {
//            adtype = response.getInt(AD_AFTER_PRIVACY_PROTECTION);
//            if (currentType != adtype) {
//                mSp.setIsADAfterPrivacyProtectionOpen(forceClose ? 0 : adtype);
//                if (adtype == 1) {
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "adv_scanRST_on");
//                } else {
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "adv_scanRST_off");
//                }
//            }
//            LeoLog.d("poha", "请求成功，隐私保护后出现广告的开关：" + adtype);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return;
//        }
    }
    
    private void updateADEntryAtHomeActivityConfig(JSONObject response, boolean forceClose) {
        try {
            LeoLog.d("poha", "请求成功，应用锁界面出现广告的开关：" + response.getInt(AD_AT_APPLOCK_FRAGMENT));
            int value = forceClose ? 0 : (response.getInt(AD_AT_APPLOCK_FRAGMENT));
            if (value != mSp.getIsADAtAppLockFragmentOpen()) {
                mSp.setIsADAtAppLockFragmentOpen(value);
//                if (value == 1) {
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "adv_scanRST_on");
//                } else {
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "adv_scanRST_off");
//                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        try {
//            mSp.setIsADAtAppLockFragmentOpen(forceClose ? 0 : (response.getInt(AD_AT_APPLOCK_FRAGMENT)));
//            HomeActivity.mHomeAdSwitchOpen = response.getInt(AD_AT_APPLOCK_FRAGMENT);
//            LeoLog.d("poha","请求成功，应用锁界面出现广告的开关：" + response.getInt(AD_AT_APPLOCK_FRAGMENT));
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return;
//        }
    }
    
    private void updateGiftBoxNeedJump(JSONObject response, boolean forceClose) {
        try {
            mSp.setIsGiftBoxNeedUpdate(forceClose?0:(response.getInt(GIFTBOX_UPDATE)));
            if (mIsPushRequestADShowType && response.getInt(GIFTBOX_UPDATE) == 1) {
                AppMasterPreference.getInstance(mContext).setIsADAppwallNeedUpdate(true);
                mIsPushRequestADShowType = false;
            }
            LeoLog.d("poha", "请求成功，礼物盒是否需要更新：" + response.getInt(GIFTBOX_UPDATE));
            mSp.setIsLockAppWallOpen(forceClose ? 0 : (response.getInt(AD_LOCK_WALL)));
            LeoLog.d("poha", "请求成功，解锁应用墙的开关：" + response.getInt(AD_LOCK_WALL));
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    private void updateIntruderAdConfig(JSONObject response, boolean forceClose) {
        try {
            int value = forceClose ? 0 : (response.getInt(AD_INTRUDER));
            LeoLog.d("poha", "请求成功，入侵者防护开关:" + value);
            if (value != mSp.getADIntruder()) {
                mSp.setADIntruder(value);
                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull",
                        (value == 1) ? "adv_capture_on" : "adv_capture_off");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateAfterScanAdConfig(JSONObject response, boolean forceClose) {
        try {
            LeoLog.d("poha", "请求成功，扫描结果页开关:" + response.getInt(AD_AFTER_SCAN));
            int value = forceClose?0:(response.getInt(AD_AFTER_SCAN));
            if (value != mSp.getADAfterScan()) {
                mSp.setADAfterScan(value);
                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull",
                        (value == 1) ? "adv_scan_on" : "adv_scan_off");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateScreenSaverAdConfig(JSONObject response, boolean forceClose) {
        try {
            LeoLog.d("poha", "请求成功，屏保广告位开关:" + response.getInt(AD_ON_SCREEN_SAVER));
            int value = forceClose?0:(response.getInt(AD_ON_SCREEN_SAVER));
            if (value != mSp.getADOnScreenSaver()) {
                mSp.setADOnScreenSaver(value);
                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull",
                        (value == 1) ? "adv_screen_on" : "adv_screen_off");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void updateADAtThemeListConfig(JSONObject response, boolean forceClose) {
        try {
            int value = forceClose ? 0 : (response.getInt(AD_AT_THEME));
            if (value != mSp.getIsADAtLockThemeOpen()) {
                mSp.setIsADAtLockThemeOpen(value);
                if (value == 1) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_on");
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_local");
                } else if (value == 2) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_on");
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_online");
                } else {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_off");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
//        int currentType = mSp.getIsADAtLockThemeOpen();
//        int adtype = currentType;
//        try {
//            adtype = response.getInt(AD_AT_THEME);
//            if (adtype != currentType) {
//                if (adtype == 1) {
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_on");
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_local");
//                } else if (adtype == 2) {
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_on");
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_online");
//                } else {
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_off");
//                }
//            }
//            mSp.setIsADAtLockThemeOpen(forceClose ? 0 : adtype);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return;
//        }
    }
    
    private class UpdateADShowTypeRequestListener extends RequestListener<AppMasterApplication> {
        public UpdateADShowTypeRequestListener(AppMasterApplication outerContext) {
            super(outerContext);
        }

        @Override
        public void onResponse(JSONObject response, boolean noMidify) {
            long start = SystemClock.elapsedRealtime();
            LeoLog.d("poha", "请求成功：" + response.toString());
            if (mListener != null) {
                mListener.onResponse(response, noMidify);
            }
            if (response != null) {
                boolean forceClose = false;
                try {
                    //TODO
                    // 中国大陆总开关
                    forceClose = updateADMainConfig(response);
                    // 旧的锁屏界面N选一的广告展示形式
                    updateADInLockScreenConfig(response, forceClose);  
                    // 锁屏界面的大图广告配置
                    updateLargeADInLockScreenConfig(response, forceClose);
                    // UFO动画的特有配置
                    updateUFOADConfig(response, forceClose);
                    // 桌面加速中的广告配置
                    updateADAfterAcceleratingConfig(response, forceClose);
                    //WIFI扫描中的广告开关
                    updateADAtWifiScanConfig(response, forceClose);
                    //隐私防护中的广告开关
                    updateADAtPrivacyProtectionConfig(response, forceClose);
                    //原主页中躲在一边的钱钱
                    updateADEntryAtHomeActivityConfig(response, forceClose);
                    //主题界面的广告配置
                    updateADAtThemeListConfig(response, forceClose);
                    //锁屏界面的礼物盒是否作更新显示的配置
                    updateGiftBoxNeedJump(response, forceClose);
                    // 3.2 入侵者检测广告配置
                    updateIntruderAdConfig(response, forceClose);
                    // 3.2 扫描结果页广告配置
                    updateAfterScanAdConfig(response, forceClose);
                    // 3.3 屏保界面广告位
                    updateScreenSaverAdConfig(response, forceClose);
                    // 注意：下述3个非广告开关
                    mSp.setVersionUpdateTipsAfterUnlockOpen((response.getInt(VERSION_UPDATE_AFTER_UNLOCK)));
                    LeoLog.d("poha", "请求成功，解锁后提示更新版本的开关：" + response.getInt(VERSION_UPDATE_AFTER_UNLOCK));
                    mSp.setIsAppStatisticsOpen((response.getInt(APP_STATISTICS)));
                    LeoLog.d("poha", "请求成功，应用统计的开关：" + response.getInt(APP_STATISTICS));
                    mSp.setIsWifiStatistics((response.getInt(WIFI_STATISTICAL)));
                    LeoLog.d("poha", "请求成功，wifi统计的开关：" + response.getInt(WIFI_STATISTICAL));
                    addAppInfoEvent();
                    LeoLog.i(TAG, "cost, UpdateADShowTypeRequestListener.onResponse: " + (SystemClock.elapsedRealtime() - start));
                } catch (JSONException e) {
                    LeoLog.d("poha", "请求成功，JSON解析出错");
                    e.printStackTrace();
                }
            } 
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (mListener != null) {
                mListener.onErrorResponse(error);
            }

            LeoLog.d("poha", "请求失败。。。");

            AppMasterPreference sp = AppMasterPreference.getInstance(mContext);
            long currentTime = System.currentTimeMillis();
            String currentDate = mDateFormate.format(new Date(System.currentTimeMillis()));
            String LastRequestDate = mDateFormate.format(new Date(sp.getADRequestShowTypeLastTime()));

            sp.setADRequestShowTypeLastTime(currentTime);

            if (currentDate.equals(LastRequestDate)) {
                sp.setADRequestShowtypeFailTimesCurrentDay(sp.getADRequestShowtypeFailTimesCurrentDay() + 1);
                if (sp.getADRequestShowtypeFailTimesCurrentDay() == 3) {
                    sp.setADRequestShowTypeNextTimeSpacing(1000 * 60 * 60 * 12);
                } else {
                    sp.setADRequestShowTypeNextTimeSpacing(1000 * 60 * 60 * 2);
                }
            } else {
                sp.setADRequestShowtypeFailTimesCurrentDay(1);
                sp.setADRequestShowTypeNextTimeSpacing(1000 * 60 * 60 * 2);
            }

        }

    }

    public void addAppInfoEvent() {
        LeoLog.d("poha", "addAppInfoEvent()");
        int lastTimeStatus = AppMasterPreference.getInstance(mContext)
                .getIsStatisticsLasttime();
        LeoLog.d("poha", "lastTimeStatus is : " + lastTimeStatus);
        int nowStatus = AppMasterPreference.getInstance(mContext).getIsAppStatisticsOpen();
        // nowStatus = 1;
        LeoLog.d("poha", "nowStatus is : " + nowStatus);
        if (nowStatus == 1 && lastTimeStatus == 0) {

            LeoLog.d("poha", "条件符合，addEvent");
            PackageManager packageManager = mContext.getPackageManager();
            List<PackageInfo> list = packageManager
                    .getInstalledPackages(PackageManager.GET_PERMISSIONS);
            for (PackageInfo packageInfo : list) {
                String packNameString = packageInfo.packageName;
                String mDir = packageInfo.applicationInfo.sourceDir;
                // LeoLog.d("testDir", "pck : " + packNameString);
                // LeoLog.d("testDir", "dir : " + mDir);
                // LeoLog.d("testDir",
                // "---------------------------------------");
                if (!isSystemApp(packageInfo) && !isSysDir(mDir)) {
                    LeoLog.d("poha", packNameString);
                    // LeoLog.d("poha", mDir);
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "install_check",
                            packNameString);
                }
            }

        } else {
            LeoLog.d("poha", "条件不符合，不addEvent");
        }
        AppMasterPreference.getInstance(mContext).setIsStatisticsLasttime(nowStatus);
    }

    private boolean isSysDir(String mDir) {
        return mDir.startsWith(PACKAGEDIR) ? true : false;
    }

    public boolean isSystemApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

}
