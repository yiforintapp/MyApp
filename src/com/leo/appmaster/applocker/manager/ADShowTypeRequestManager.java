
package com.leo.appmaster.applocker.manager;

import java.text.SimpleDateFormat;
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
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.fragment.HomePravicyFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.http.HttpRequestAgent.RequestListener;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.schedule.FetchScheduleJob.FetchScheduleListener;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.mobvista.sdk.m.core.entity.Campaign;

public class ADShowTypeRequestManager {
    private static final String TAG = "ADShowTypeRequestManager";
    /* 是否升级 */

    private static final String AD_SHOW_TYPE = "a";// 广告的展示方式，如半屏广告，banner广告等等
    private static final String UFO_ANIM_TYPE = "b";
    private static final String THEME_CHANCE_AFTER_UFO = "c";
    private static final String AD_AFTER_ACCELERATING = "d";
    private static final String AD_AFTER_PRIVACY_PROTECTION = "e";
    private static final String AD_AT_APPLOCK_FRAGMENT = "f";
    private static final String AD_AT_THEME = "g";
    private static final String GIFTBOX_UPDATE = "h";
    private static final String VERSION_UPDATE_AFTER_UNLOCK = "i";
    private static final String APP_STATISTICS = "j";
    private static final String WIFI_STATISTICAL = "l";
    private static final String AD_LOCK_WALL = "m";
    private static final String PACKAGEDIR = "/system/";

    private static ADShowTypeRequestManager mInstance;
    private Context mContext;
    private SimpleDateFormat mDateFormate;
    // public boolean IsFromPush = false;
    public boolean mIsPushRequestADShowType = false;

    public static Campaign mCampaign;
    private FetchScheduleListener mListener;

    private ADShowTypeRequestManager(Context context) {
        mContext = context;
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
        AppMasterPreference sp = AppMasterPreference.getInstance(mContext);
        // 当前时间（ms）
        // long currentTime = System.currentTimeMillis();
        // long lastRequestTime = sp.getADRequestShowTypeLastTime();
        // String currentDate = mDateFormate.format(new Date(currentTime));
        // String lastRequestDate = mDateFormate.format(new
        // Date(lastRequestTime));
        // LeoLog.e("poha", "当前时间：" + currentTime + "，上次请求时间：" + lastRequestTime
        // + ",间隔是（s）："
        // + (currentTime - lastRequestTime) / 1000);
        // LeoLog.e("poha", "当前日期：" + currentDate + "，上次请求日期：" +
        // lastRequestDate);
        // // LeoLog.e("poha", "上次记录的到下次请求的间隔时间（12h/2h）：" +
        // sp.getADRequestShowTypeNextTimeSpacing() / 1000
        // / 60 / 60 + "，当天的请求失败次数："
        // + sp.getADRequestShowtypeFailTimesCurrentDay());
        // if (((currentTime - lastRequestTime) >
        // sp.getADRequestShowTypeNextTimeSpacing()
        // && sp.getADRequestShowtypeFailTimesCurrentDay() < 3) ||
        // mIsPushRequestADShowType)
        // {
        // mIsPushRequestADShowType = false;
        // LeoLog.e("poha", "满足发起请求的条件，正在发起请求");
        UpdateADShowTypeRequestListener listenerr = new UpdateADShowTypeRequestListener(
                AppMasterApplication.getInstance());
        HttpRequestAgent.getInstance(AppMasterApplication.getInstance()).loadADShowType(
                listenerr,
                listenerr);
        // }

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
                AppMasterPreference sp = AppMasterPreference.getInstance(mContext);

                try {
                    int adtype = response.getInt(AD_SHOW_TYPE);
                    LeoLog.d("poha", "请求成功，广告展示形式是：" + adtype);
                    if (adtype != sp.getADShowType()) {
                        if (adtype == 1) {
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_banner");
                        }
                        if (adtype == 2) {
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_bannerpop");
                        }
                        if (adtype == 3) {
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_draw");
                        }
                        if (adtype == 4) {
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_none");
                        }
                    }
                    sp.setADShowType(adtype);
                    sp.setUFOAnimType((response.getInt(UFO_ANIM_TYPE)));
                    LeoLog.d("poha", "请求成功，UFO动画形式是：" + response.getInt(UFO_ANIM_TYPE));
                    sp.setThemeChanceAfterUFO((response.getInt(THEME_CHANCE_AFTER_UFO)));
                    LeoLog.d("poha",
                            "请求成功，UFO动画roll出主题概率：" + response.getInt(THEME_CHANCE_AFTER_UFO));
                    if (response.getInt(AD_AFTER_ACCELERATING) != sp
                            .getADChanceAfterAccelerating()) {
                        sp.setADChanceAfterAccelerating((response.getInt(AD_AFTER_ACCELERATING)));
                        if (sp.getADChanceAfterAccelerating() == 1) {
                            SDKWrapper
                                    .addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_toast_on");
                        } else {
                            SDKWrapper
                                    .addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_toast_off");
                        }
                    }

                    LeoLog.d("poha", "请求成功，加速后出现广告：" + response.getInt(AD_AFTER_ACCELERATING));
                    // 隐私防护
                    int lastPrivacyType = sp.getIsADAfterPrivacyProtectionOpen();
                    int nowPrivacyType = response.getInt(AD_AFTER_PRIVACY_PROTECTION);
                    if (lastPrivacyType != nowPrivacyType) {
                        if (nowPrivacyType == 1) {
                            SDKWrapper
                                    .addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_privacy_on");
                        } else {
                            SDKWrapper
                                    .addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_privacy_off");
                        }
                    }
                    sp.setIsADAfterPrivacyProtectionOpen(nowPrivacyType);
                    HomePravicyFragment.mPrivicyAdSwitchOpen = nowPrivacyType;
                    LeoLog.d("poha",
                            "请求成功，隐私保护后出现广告的开关：" + nowPrivacyType);
                    // 主页ad
                    sp.setIsADAtAppLockFragmentOpen((response.getInt(AD_AT_APPLOCK_FRAGMENT)));
                    HomeActivity.mHomeAdSwitchOpen = response.getInt(AD_AT_APPLOCK_FRAGMENT);
                    LeoLog.d("poha", "请求成功，应用锁界面出现广告的开关：" + response.getInt(AD_AT_APPLOCK_FRAGMENT));
                    // 主题
                    int lastThemeType = sp.getIsADAtLockThemeOpen();
                    int nowThemeType = response.getInt(AD_AT_THEME);
                    if (lastThemeType != nowThemeType) {
                        if (lastThemeType == 1) {
                            SDKWrapper
                                    .addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_on");
                            SDKWrapper
                                    .addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_local");
                        } else if (lastThemeType == 2) {
                            SDKWrapper
                                    .addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_on");
                            SDKWrapper
                                    .addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_online");
                        } else {
                            SDKWrapper
                                    .addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_theme_off");
                        }
                    }
                    sp.setIsADAtLockThemeOpen(nowThemeType);
                    LockerTheme.mThemeAdSwitchOpen = nowThemeType;
                    LeoLog.d("poha", "请求成功，主题界面出现广告：" + nowThemeType);

                    sp.setIsGiftBoxNeedUpdate((response.getInt(GIFTBOX_UPDATE)));
                    if (mIsPushRequestADShowType && response.getInt(GIFTBOX_UPDATE) == 1) {
                        AppMasterPreference.getInstance(mContext).setIsADAppwallNeedUpdate(true);
                        mIsPushRequestADShowType = false;
                    }
                    LeoLog.d("poha", "请求成功，礼物盒是否需要更新：" + response.getInt(GIFTBOX_UPDATE));
                    sp.setVersionUpdateTipsAfterUnlockOpen((response
                            .getInt(VERSION_UPDATE_AFTER_UNLOCK)));
                    LeoLog.d("poha",
                            "请求成功，解锁后提示更新版本的开关：" + response.getInt(VERSION_UPDATE_AFTER_UNLOCK));
                    sp.setIsAppStatisticsOpen((response.getInt(APP_STATISTICS)));
                    LeoLog.d("poha", "请求成功，应用统计的开关：" + response.getInt(APP_STATISTICS));
                    sp.setIsWifiStatistics((response.getInt(WIFI_STATISTICAL)));
                    LeoLog.d("poha", "请求成功，wifi统计的开关：" + response.getInt(WIFI_STATISTICAL));
                    
                    sp.setIsLockAppWallOpen((response.getInt(AD_LOCK_WALL)));
                    LeoLog.d("poha", "请求成功，解锁应用墙的开关：" + response.getInt(AD_LOCK_WALL));
                    //
                    // AppMasterPreference.getInstance(mContext).setADChanceAfterAccelerating(
                    // (1));
                    // LeoLog.e("poha", "请求成功，加速后出现广告概率：" +
                    // response.getInt(AD_AFTER_ACCELERATING));
                    // int random = new Random().nextInt(10) + 1;
                    // LeoLog.e("poha",
                    // "random：" + random);
                    // if (random > 5) {
                    // random = 1;
                    // } else {
                    // random = 0;
                    // }
                    // AppMasterPreference.getInstance(mContext).setIsADAfterPrivacyProtectionOpen(
                    // random);
                    // HomePravicyFragment.mPrivicyAdSwitchOpen = random;
                    // LeoLog.e("poha",
                    // "请求成功，隐私保护后出现广告的开关：" + random);
                    // AppMasterPreference.getInstance(mContext).setIsADAfterPrivacyProtectionOpen(
                    // (1));
                    // HomePravicyFragment.mPrivicyAdSwitchOpen = 1;
                    // LeoLog.e("poha",
                    // "请求成功，隐私保护后出现广告的开关：" +
                    // response.getInt(AD_AFTER_PRIVACY_PROTECTION));
                    //
                    // //
                    // AppMasterPreference.getInstance(mContext).setIsADAtAppLockFragmentOpen(
                    // // random);
                    // // HomeActivity.mHomeAdSwitchOpen = random;
                    // // LeoLog.e("poha", "请求成功，应用锁界面出现广告的开关：" + random);
                    // AppMasterPreference.getInstance(mContext).setIsADAtAppLockFragmentOpen(
                    // (1));
                    // HomeActivity.mHomeAdSwitchOpen =1;
                    //
                    // LeoLog.e("poha", "请求成功，应用锁界面出现广告的开关：" +
                    // response.getInt(AD_AT_APPLOCK_FRAGMENT));
                    //
                    //
                    // AppMasterPreference.getInstance(mContext).setIsADAtLockThemeOpen(
                    // (2));
                    // LockerTheme.mThemeAdSwitchOpen = 2;
                    // LeoLog.e("poha", "请求成功，主题界面出现广告：" +
                    // response.getInt(AD_AT_THEME));
                    //
                    // AppMasterPreference.getInstance(mContext).setIsGiftBoxNeedUpdate(
                    // (response.getInt(GIFTBOX_UPDATE)));
                    // if (mIsPushRequestADShowType &&
                    // response.getInt(GIFTBOX_UPDATE) == 1) {
                    // AppMasterPreference.getInstance(mContext).setIsADAppwallNeedUpdate(true);
                    // mIsPushRequestADShowType = false;
                    // }
                    // LeoLog.e("poha", "请求成功，礼物盒是否需要更新：" +
                    // response.getInt(GIFTBOX_UPDATE));
                    // AppMasterPreference.getInstance(mContext).setVersionUpdateTipsAfterUnlockOpen(
                    // (response.getInt(VERSION_UPDATE_AFTER_UNLOCK)));
                    // LeoLog.e("poha",
                    // "请求成功，解锁后提示更新版本的开关：" +
                    // response.getInt(VERSION_UPDATE_AFTER_UNLOCK));
                    // AppMasterPreference.getInstance(mContext).setIsAppStatisticsOpen(
                    // (response.getInt(APP_STATISTICS)));
                    // LeoLog.e("poha", "请求成功，应用统计的开关：" +
                    // response.getInt(APP_STATISTICS));
                    // AppMasterPreference.getInstance(mContext).setIsWifiStatistics(
                    // (response.getInt(WIFI_STATISTICAL)));
                    // LeoLog.e("poha", "请求成功，wifi统计的开关：" +
                    // response.getInt(WIFI_STATISTICAL));
                    //
                    // 应用统计
                    addAppInfoEvent();
                    LeoLog.i(TAG, "cost, UpdateADShowTypeRequestListener.onResponse: " +
                            (SystemClock.elapsedRealtime() - start));
                } catch (JSONException e) {

                    LeoLog.d("poha", "请求成功，JSON解析出错");

                    e.printStackTrace();
                }
            }
            else
            {
                LeoLog.d("poha", "请求成功，JSON是null");
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
            String LastRequestDate = mDateFormate
                    .format(new Date(sp.getADRequestShowTypeLastTime()));

            sp.setADRequestShowTypeLastTime(currentTime);

            if (currentDate.equals(LastRequestDate))
            {
                sp.setADRequestShowtypeFailTimesCurrentDay(sp
                        .getADRequestShowtypeFailTimesCurrentDay() + 1);
                if (sp.getADRequestShowtypeFailTimesCurrentDay() == 3)
                {
                    sp.setADRequestShowTypeNextTimeSpacing(1000 * 60 * 60 * 12);
                }
                else
                {
                    sp.setADRequestShowTypeNextTimeSpacing(1000 * 60 * 60 * 2);
                }
            }
            else
            {
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
