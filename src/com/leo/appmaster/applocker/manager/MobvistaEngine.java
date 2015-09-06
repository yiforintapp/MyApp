package com.leo.appmaster.applocker.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.baidu.mobstat.ar;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.applocker.MobvistaProxyActivity;
import com.leo.appmaster.utils.LeoLog;
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
public class MobvistaEngine implements AdListener {
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
    
    private static Mobvista sMobvista;
    private static MobvistaAdNative sNativeAd;
    
    private static List<MobvistaListener> sMobvistaListeners;
    
    private static boolean sIsStarted;
    private static boolean sReleasedByUser;
    
    private static MobvistaEngine sInstance;
    
    private Activity mActivity;
    
    static {
        Context context = AppMasterApplication.getInstance();
        MobvistaAd.init(context, Constants.MOBVISTA_APPID,
                Constants.MOBVISTA_APPKEY);
        
        sMobvistaListeners = new ArrayList<MobvistaEngine.MobvistaListener>();
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
        
        if (!sMobvistaListeners.contains(listener) && !(activity instanceof MobvistaProxyActivity)) {
            sMobvistaListeners.add(listener);
        }
        
//        AppMasterApplication context = AppMasterApplication.getInstance();
//        AppMasterPreference preference = AppMasterPreference.getInstance(context);
//        if (mActivity == null && preference.isMobvistaClicked()) {
//            // 广告已经点击过，使用MobvistaProxyActivity被长期持有
//            if (activity instanceof MobvistaProxyActivity) {
//                mActivity = activity;
//            }
//            if (mActivity == null) {
//                Intent intent = new Intent(context, MobvistaProxyActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//                LeoLog.i(TAG, "start mob proxy activity.");
//                return;
//            } 
//        }
//        
        if (!isOutOfDate(sMobvista)) {
            listener.onMobvistaFinished(ERR_OK, sMobvista.campaign, null);
            LeoLog.i(TAG, "data has not outofdate, return old data.");
            return;
        }
        
        if (sIsStarted){
            LeoLog.i(TAG, "engine has startd.");
            return;
        }
        
        Activity requestActivity = mActivity == null ? activity : mActivity;
        sNativeAd = MobvistaAd.newNativeController(requestActivity,
                Constants.MOBVISTA_UNITID,
                Constants.MOBVISTA_FACEBOOK_ID);
        try {
            // 这个地方执行导致crash，直接catch住
            sNativeAd.loadAd(this);
        } catch (Throwable thr) {
            listener.onMobvistaFinished(ERR_MOBVISTA_FAIL, null, "Mobvista execute throwable.");
            doReleaseInner();
            return;
        }
        sIsStarted = true;
        sReleasedByUser = false;
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
    public void registerView(View view) {
        if (sNativeAd != null) {
            LeoLog.i(TAG, "registerView");
            sNativeAd.registerView(view, new AdTrackingListener() {
                
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
    }
    
    /**
     * 释放广告资源
     */
    public void release() {
        sReleasedByUser = true;
        sIsStarted = false;
        doReleaseInner();
    }
    
    @Override
    public void onAdClick(Campaign arg0) {
        LeoLog.i(TAG, "onAdClick, arg0: " + arg0);
        for (MobvistaListener mobvistaListener : sMobvistaListeners) {
            Campaign data = sMobvista == null ? null : sMobvista.campaign;
            mobvistaListener.onMobvistaClick(arg0 == null ? data : arg0);
        }
        sMobvista = null;
    }

    @Override
    public void onAdLoadError(String arg0) {
        LeoLog.i(TAG, "onAdLoadError, string: " + arg0);
        sIsStarted = false;
        if (sReleasedByUser) {
            doReleaseInner();
            return;
        }

        notifyFail(ERR_MOBVISTA_FAIL, arg0);
    }
    
    @Override
    public void onAdLoaded(Campaign arg0) {
        LeoLog.i(TAG, "onAdLoaded, arg0: " + arg0);
        sIsStarted = false;
        if (sReleasedByUser) {
            doReleaseInner();
            return;
        }
        if (arg0 == null) {
            notifyFail(ERR_MOBVISTA_RESULT_NULL, null);
        } else {
            sMobvista = new Mobvista();
            sMobvista.campaign = arg0;
            sMobvista.requestTimeMs = System.currentTimeMillis();

            notifySuccess(arg0);
        }
    }
    
    private void notifySuccess(Campaign arg0) {
        Iterator<MobvistaListener> iterator = sMobvistaListeners.iterator();
        while (iterator.hasNext()) {
            MobvistaListener listener = iterator.next();
            listener.onMobvistaFinished(ERR_OK, arg0, null);
        }
    }
    
    private void notifyFail(int code, String msg) {
        Iterator<MobvistaListener> iterator = sMobvistaListeners.iterator();
        while (iterator.hasNext()) {
            MobvistaListener listener = iterator.next();
            listener.onMobvistaFinished(code, null, msg);
        } 
    }
    
    private void doReleaseInner() {
        try {
            if (mActivity == null && sNativeAd != null) {
                // mActivity为空，说明使用的不是MobvistaProxyActivity，所以需要释放
                sNativeAd.release();
                sNativeAd = null;
                
                sMobvista = null;
            }
            sMobvistaListeners.clear();
            sIsStarted = false;
        } catch (Exception e) {
        }
    }
    
    private static boolean isOutOfDate(Mobvista mobvista) { 
        if (mobvista == null) return true;
        
        long current = System.currentTimeMillis();
        return current - mobvista.requestTimeMs > AD_TIMEOUT; 
    }
    
    private static class Mobvista {
        public Campaign campaign;
        public long requestTimeMs;
    }

}
