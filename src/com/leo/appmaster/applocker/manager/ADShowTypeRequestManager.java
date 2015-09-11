
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

import android.util.Log;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;

import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.http.HttpRequestAgent.RequestListener;
import com.leo.appmaster.schedule.FetchScheduleJob.FetchScheduleListener;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.mobvista.sdk.m.core.entity.Campaign;
import com.tendcloud.tenddata.TCAgent;

public class ADShowTypeRequestManager {
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

    private static ADShowTypeRequestManager mInstance;
    private Context mContext;
    private SimpleDateFormat mDateFormate;

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

        LeoLog.e("poha", "满足发起请求的条件，正在发起请求");
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
            LeoLog.e("poha", "请求成功：" + response.toString());
            if (mListener != null) {
                mListener.onResponse(response, noMidify);
            }
            if (response != null)
            {
                AppMasterPreference sp = AppMasterPreference.getInstance(mContext);

                try {
                    int adtype = response.getInt(AD_SHOW_TYPE);
                    LeoLog.e("poha", "请求成功，广告展示形式是：" + adtype);
                    if (adtype != AppMasterPreference.getInstance(mContext).getADShowType())
                    {
                        if (adtype == 1)
                        {
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_banner");
                        }
                        if (adtype == 2)
                        {
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_bannerpop");
                        }
                        if (adtype == 3)
                        {
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_draw");
                        }
                        if (adtype == 4)
                        {
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_none");
                        }
                    }
                    AppMasterPreference.getInstance(mContext).setADShowType(adtype);
                    AppMasterPreference.getInstance(mContext).setUFOAnimType(
                            (response.getInt(UFO_ANIM_TYPE)));
                    LeoLog.e("poha", "请求成功，UFO动画形式是：" + response.getInt(UFO_ANIM_TYPE));
                    AppMasterPreference.getInstance(mContext).setThemeChanceAfterUFO(
                            (response.getInt(THEME_CHANCE_AFTER_UFO)));
                    LeoLog.e("poha",
                            "请求成功，UFO动画roll出主题概率：" + response.getInt(THEME_CHANCE_AFTER_UFO));
                    AppMasterPreference.getInstance(mContext).setADChanceAfterAccelerating(
                            (response.getInt(AD_AFTER_ACCELERATING)));
                    LeoLog.e("poha", "请求成功，加速后出现广告概率：" + response.getInt(AD_AFTER_ACCELERATING));
                    AppMasterPreference.getInstance(mContext).setIsADAfterPrivacyProtectionOpen(
                            (response.getInt(AD_AFTER_PRIVACY_PROTECTION)));
                    LeoLog.e("poha",
                            "请求成功，隐私保护后出现广告的开关：" + response.getInt(AD_AFTER_PRIVACY_PROTECTION));
                    AppMasterPreference.getInstance(mContext).setIsADAtAppLockFragmentOpen(
                            (response.getInt(AD_AT_APPLOCK_FRAGMENT)));
                    LeoLog.e("poha", "请求成功，应用锁界面出现广告的开关：" + response.getInt(AD_AT_APPLOCK_FRAGMENT));
                    AppMasterPreference.getInstance(mContext).setIsADAtLockThemeOpen(
                            (response.getInt(AD_AT_THEME)));
                    LeoLog.e("poha", "请求成功，主题界面出现广告：" + response.getInt(AD_AT_THEME));
                    AppMasterPreference.getInstance(mContext).setIsGiftBoxNeedUpdate(
                            (response.getInt(GIFTBOX_UPDATE)));
                    LeoLog.e("poha", "请求成功，礼物盒是否需要更新：" + response.getInt(GIFTBOX_UPDATE));
                    AppMasterPreference.getInstance(mContext).setVersionUpdateTipsAfterUnlockOpen(
                            (response.getInt(VERSION_UPDATE_AFTER_UNLOCK)));
                    LeoLog.e("poha",
                            "请求成功，解锁后提示更新版本的开关：" + response.getInt(VERSION_UPDATE_AFTER_UNLOCK));
                    AppMasterPreference.getInstance(mContext).setIsAppStatisticsOpen(
                            (response.getInt(APP_STATISTICS)));
                    LeoLog.e("poha", "请求成功，应用统计的开关：" + response.getInt(APP_STATISTICS));
                    AppMasterPreference.getInstance(mContext).setIsWifiStatistics(
                            (response.getInt(WIFI_STATISTICAL)));
                    LeoLog.e("poha", "请求成功，wifi统计的开关：" + response.getInt(WIFI_STATISTICAL));

                    // 应用统计
                    addAppInfoEvent();

                } catch (JSONException e) {

                    LeoLog.e("poha", "请求成功，JSON解析出错");

                    e.printStackTrace();
                }
            }
            else
            {
                LeoLog.e("poha", "请求成功，JSON是null");
            }

        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (mListener != null) {
                mListener.onErrorResponse(error);
            }
            LeoLog.e("poha", "请求失败。。。");

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
        LeoLog.d("testAppEvent", "addAppInfoEvent()");
        int lastTimeStatus = AppMasterPreference.getInstance(mContext)
                .getIsStatisticsLasttime();
        LeoLog.d("testAppEvent", "lastTimeStatus is : " + lastTimeStatus);
        int nowStatus = AppMasterPreference.getInstance(mContext).getIsAppStatisticsOpen();
        LeoLog.d("testAppEvent", "nowStatus is : " + nowStatus);
        if (nowStatus == 1 && lastTimeStatus == 0) {

            LeoLog.d("testAppEvent", "条件符合，addEvent");
            PackageManager packageManager = mContext.getPackageManager();
            List<PackageInfo> list = packageManager
                    .getInstalledPackages(PackageManager.GET_PERMISSIONS);
            for (PackageInfo packageInfo : list) {
                String packNameString = packageInfo.packageName;
                if (!isSystemApp(packageInfo)) {
                    LeoLog.d("testAppEvent", packNameString);
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "install_check",
                            packNameString);
                }
            }

        } else {
            LeoLog.d("testAppEvent", "条件不符合，不addEvent");
        }
        AppMasterPreference.getInstance(mContext).setIsStatisticsLasttime(nowStatus);
    }

    public boolean isSystemApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

}
