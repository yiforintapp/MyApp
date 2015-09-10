package com.leo.appmaster.applocker.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.baidu.mobstat.ar;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.applocker.MobvistaProxyActivity;
import com.leo.appmaster.bootstrap.Bootstrap;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.mobvista.sdk.m.core.AdListener;
import com.mobvista.sdk.m.core.AdTrackingListener;
import com.mobvista.sdk.m.core.MobvistaAd;
import com.mobvista.sdk.m.core.MobvistaAdNative;
import com.mobvista.sdk.m.core.MobvistaAdWall;
import com.mobvista.sdk.m.core.entity.Campaign;

/**
 * 广告相关引擎
 *  广告加载、回调
 *  注册广告点击
 *  创建appwall类型广告接口
 * @author Jasper
 *
 */
public class MobvistaEngine {
    private static final String TAG = "MobvistaEngine";
    
    /**
     * 请求参数为null
     */
    public static final int ERR_PARAMS_NULL = -1000;
    /**
     * mobvista请求失败，详细失败原因见返回的msg
     */
    public static final int ERR_MOBVISTA_FAIL = -1001;
    /**
     * 请求成功，但返回的结构体为null
     */
    public static final int ERR_MOBVISTA_RESULT_NULL = -1002;
    /**
     * 请求成功
     */
    public static final int ERR_OK = 0;
    
    /**
     * 广告过期时间, 1小时
     */
    private static final int AD_TIMEOUT = 60 * 60 * 1000;

    private static MobvistaEngine sInstance;
    
    private Map<Activity, Mobvista> mMobvistaMap;
    private Map<Activity, MobvistaListener> mMobvistaListeners;
    private Map<Activity, MobvistaAdNative> mMobvistaNative;
    
    static {
        Context context = AppMasterApplication.getInstance();
        MobvistaAd.init(context, Constants.MOBVISTA_APPID,
                Constants.MOBVISTA_APPKEY);
         
        
//        sMobvistaListeners = new ArrayList<MobvistaEngine.MobvistaListener>();
    }
    
    public static interface MobvistaListener {
        /**
         * 广告请求回调
         * @param code 返回码，如ERR_PARAMS_NULL
         * @param campaign 请求成功的广告结构体，失败为null
         * @param msg 请求失败sdk返回的描述，成功为null
         */
        public void onMobvistaFinished(int code, Campaign campaign, String msg);
        /**
         * 广告点击回调
         * @param campaign
         */
        public void onMobvistaClick(Campaign campaign);
    }
    
    public static synchronized MobvistaEngine getInstance() {
        if (sInstance == null) {
            sInstance = new MobvistaEngine();
        }
        
        return sInstance;
    }
    
    private MobvistaEngine() {
        mMobvistaMap = new HashMap<Activity, Mobvista>();
        mMobvistaListeners = new HashMap<Activity, MobvistaListener>();
        mMobvistaNative = new HashMap<Activity, MobvistaAdNative>();
    }
    
    /**
     * 获取广告内容
     * @param activity
     * @param listener
     */
    public void loadMobvista(Activity activity, MobvistaListener listener) {
        LeoLog.i(TAG, "start load mobvista.");
        if (listener == null) return;
        
        if (activity == null) {
            listener.onMobvistaFinished(ERR_PARAMS_NULL, null, null);
            LeoLog.i(TAG, "activity is null.");
            return;
        }

        if (!(activity instanceof MobvistaProxyActivity)) {
            mMobvistaListeners.put(activity, listener);
        }

        Mobvista mobvista = mMobvistaMap.get(activity);
        if (!isOutOfDate(mobvista)) {
            listener.onMobvistaFinished(ERR_OK, mobvista.campaign, null);
            LeoLog.i(TAG, "data has not outofdate, return old data.");
            return;
        }

        boolean started = mMobvistaNative.get(activity) != null;
        if (started){
            LeoLog.i(TAG, "engine has startd.");
            return;
        }
        
//        Activity requestActivity = mActivity == null ? activity : mActivity;
        MobvistaAdNative mobvistaAd = MobvistaAd.newNativeController(activity,
                Constants.MOBVISTA_UNITID,
                Constants.MOBVISTA_FACEBOOK_ID);
        try {
            // 这个地方执行导致crash，直接catch住
            mobvistaAd.loadAd(new AdListenerImpl(activity));
            mMobvistaNative.put(activity, mobvistaAd);
        } catch (Throwable thr) {
            listener.onMobvistaFinished(ERR_MOBVISTA_FAIL, null, "Mobvista execute throwable.");
            doReleaseInner(activity);
            return;
        }
//        sReleasedByUser = false;
        LeoLog.i(TAG, "real to start load mobvista.");
    }
    
    /**
     * 创建appwall广告接口
     * @param activity
     * @return
     */
    public MobvistaAdWall createAdWallController(Activity activity) {
        return MobvistaAd.newAdWallController(activity, Constants.MOBVISTA_UNITID,
                Constants.MOBVISTA_FACEBOOK_ID); 
    }
    
    /**
     * 注册广告点击事件
     * @param view
     */
    public void registerView(Activity activity, View view) {
        MobvistaAdNative adNative = mMobvistaNative.get(activity);
        if (adNative == null) {
            LeoLog.i(TAG, "havnt register activity before.");
            return;
        }
        LeoLog.i(TAG, "registerView");
        adNative.registerView(view, new AdTrackingListener() {

            @Override
            public void onStartRedirection(Campaign arg0, String arg1) {
                LeoLog.i(TAG, "-->onStartRedirection arg0: " + arg0 + " | string: " + arg1);
            }

            @Override
            public void onRedirectionFailed(Campaign arg0, String arg1) {
                LeoLog.i(TAG, "-->onRedirectionFailed arg0: " + arg0 + " | string: " + arg1);

            }

            @Override
            public void onFinishRedirection(Campaign arg0, String arg1) {
                LeoLog.i(TAG, "-->onFinishRedirection arg0: " + arg0 + " | string: " + arg1);
                AppMasterApplication context = AppMasterApplication.getInstance();
                AppMasterPreference preference = AppMasterPreference.getInstance(context);

                // 记录广告已经被点击过
                preference.setMobvistaClicked();
            }

            @Override
            public void onDownloadStart(Campaign arg0) {
                LeoLog.i(TAG, "-->onDownloadStart arg0: " + arg0);
            }

            @Override
            public void onDownloadProgress(Campaign arg0, int arg1) {
                LeoLog.i(TAG, "-->onDownloadProgress arg0: " + arg0 + " | progress: " + arg1);
            }

            @Override
            public void onDownloadFinish(Campaign arg0) {
                LeoLog.i(TAG, "-->onDownloadFinish arg0: " + arg0);
            }

            @Override
            public void onDownloadError(String arg0) {
                LeoLog.i(TAG, "-->onDownloadError arg0: " + arg0);
            }
        });
    }
    
    /**
     * 释放广告资源
     */
    public void release(Activity activity) {
        doReleaseInner(activity);
    }

    private void doReleaseInner(Activity activity) {
        MobvistaAdNative adNative = null;
        if (mMobvistaNative.containsKey(activity)) {
            adNative = mMobvistaNative.remove(activity);
        }
        if (adNative != null) {
            try {
                adNative.release();
            } catch (Throwable e) {
            }
        }
        if (mMobvistaListeners.containsKey(activity)) {
            mMobvistaListeners.remove(activity);
        }
        if (mMobvistaMap.containsKey(activity)) {
            mMobvistaMap.remove(activity);
        }
    }

    private static boolean isOutOfDate(Mobvista mobvista) {
        if (mobvista == null) return true;
        
        long current = System.currentTimeMillis();
        return current - mobvista.requestTimeMs > AD_TIMEOUT; 
    }

    private class AdListenerImpl implements AdListener {
        private Activity activity;
        public AdListenerImpl(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onAdLoaded(Campaign campaign) {
            LeoLog.i(TAG, "onAdLoaded...");
            MobvistaListener listener = null;
            if (mMobvistaListeners.containsKey(activity)) {
                listener = mMobvistaListeners.get(activity);
            }

            if (listener != null) {
                if (campaign == null) {
                    listener.onMobvistaFinished(ERR_MOBVISTA_RESULT_NULL, campaign, null);

                    Mobvista mobvista = new Mobvista();
                    mobvista.campaign = campaign;
                    mobvista.requestTimeMs = System.currentTimeMillis();
                    mMobvistaMap.put(activity, mobvista);
                } else {
                    listener.onMobvistaFinished(ERR_OK, campaign, null);
                }
            }
        }

        @Override
        public void onAdLoadError(String s) {
            LeoLog.i(TAG, "onAdLoadError...s: " + s);
            MobvistaListener listener = null;
            if (mMobvistaListeners.containsKey(activity)) {
                listener = mMobvistaListeners.get(activity);
            }

            if (listener != null) {
                listener.onMobvistaFinished(ERR_MOBVISTA_FAIL, null, s);
            }
        }

        @Override
        public void onAdClick(Campaign campaign) {
            Campaign data = null;
            if (mMobvistaMap.containsKey(activity)) {
                Mobvista m = mMobvistaMap.get(activity);
                if (m != null) {
                    data = m.campaign;
                }
            }

            MobvistaListener listener = null;
            if (mMobvistaListeners.containsKey(activity)) {
                listener = mMobvistaListeners.get(activity);
            }

            if (listener != null) {
                listener.onMobvistaClick(campaign == null ? data : campaign);
            }
        }
    }
    
    private static class Mobvista {
        public Campaign campaign;
        public long requestTimeMs;
    }

}
