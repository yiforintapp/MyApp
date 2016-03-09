package com.leo.appmaster.applocker.manager;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.utils.LeoLog;
import com.mobvista.sdk.m.core.AdListener;
import com.mobvista.sdk.m.core.AdTrackingListener;
import com.mobvista.sdk.m.core.MobvistaAd;
import com.mobvista.sdk.m.core.MobvistaAdNative;
import com.mobvista.sdk.m.core.MobvistaAdWall;
import com.mobvista.sdk.m.core.entity.Campaign;

import java.util.HashMap;
import java.util.Map;

/**
 * 广告相关引擎
 * 广告加载、回调
 * 注册广告点击
 * 创建appwall类型广告接口
 *
 * @author Jasper
 */
public class MobvistaEngine {
    private static final String TAG = "MobvistaEngine [AD_DEBUG]";

    private Context mAppContext;
    private AppMasterPreference mPref;

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
     * unid id 为空
     */
    public static final int ERR_UNITID_NULL = -1003;
    /**
     * 找不到对应的placement id
     */
    public static final int ERR_NOT_FOUND_PLACEMENTID = -1004;
    /**
     * 请求成功
     */
    public static final int ERR_OK = 0;

    /**
     * 广告过期时间, 1小时
     */
    private static final int AD_TIMEOUT = 60 * 60 * 1000;

    /**
     * 一分钟内，不要重复拉取广告
     */
    private static final int MILLIS_IN_MINUTE = 60 * 1000;

    private static MobvistaEngine sInstance;

    private Map<String, MobvistaAdData> mMobVistaCacheMap;
    private Map<String, MobvistaListener> mMobVistaListeners;
    private Map<String, MobVistaLoadingNative> mMobVistaLoadingNative;

    private Map<String, String> mUnitIdToPlacementIdMap;

//    static {
//        Context context = AppMasterApplication.getInstance();
//        try {
//            MobvistaAd.init(context, Constants.MOBVISTA_APPID, Constants.MOBVISTA_APPKEY);
//            MobvistaEngine.getInstance(context).preloadMobvistaAds();
//        } catch (Exception e) {
//            LeoLog.e(TAG, "static block exception: " + e.getMessage());
//            e.printStackTrace();
//        }
//        LeoLog.i(TAG, "static block run done");
//    }

    public static interface MobvistaListener {
        /**
         * 广告请求回调
         *
         * @param code     返回码，如ERR_PARAMS_NULL
         * @param campaign 请求成功的广告结构体，失败为null
         * @param msg      请求失败sdk返回的描述，成功为null
         */
        public void onMobvistaFinished(int code, Campaign campaign, String msg);

        /**
         * 广告点击回调
         *
         * @param campaign
		 * @param unitID
         */
        public void onMobvistaClick(Campaign campaign, String unitID);
    }

    public static synchronized MobvistaEngine getInstance(Context ctx) {
        if (sInstance == null) {
            LeoLog.d(TAG, "MobvistaEngine first touch, init Mobvista");
            initMobvista();
            sInstance = new MobvistaEngine(ctx);
            // Do not preload all advertise when bootup.
            // Load them only if necessary
            /*
            if (!(AppMasterConfig.IS_FOR_MAINLAND_CHINA
                    && sInstance.mPref.getADMainlandSwticher()==0)) {
                sInstance.preloadMobvistaAds();
            }
            */
        }

        return sInstance;
    }

    private static void initMobvista() {
        Context context = AppMasterApplication.getInstance();
        try {
            long start = SystemClock.elapsedRealtime();
            MobvistaAd.init(context, Constants.MOBVISTA_APPID, Constants.MOBVISTA_APPKEY);
            LeoLog.i(TAG, "initMobvista module done, cost time="
                    + (SystemClock.elapsedRealtime() - start));
        } catch (Exception e) {
            LeoLog.e(TAG, "static block exception: " + e.getMessage());
            e.printStackTrace();
        }
        LeoLog.i(TAG, "static block run done");
    }

    private MobvistaEngine(Context ctx) {
        mAppContext = ctx.getApplicationContext();
        mPref = AppMasterPreference.getInstance(mAppContext);
        mMobVistaCacheMap = new HashMap<String, MobvistaAdData>();
        mMobVistaListeners = new HashMap<String, MobvistaListener>();
        mMobVistaLoadingNative = new HashMap<String, MobVistaLoadingNative>();

        mUnitIdToPlacementIdMap = new HashMap<String, String>();
        mUnitIdToPlacementIdMap.put(Constants.UNIT_ID_58, Constants.PLACEMENT_ID_58);
        mUnitIdToPlacementIdMap.put(Constants.UNIT_ID_59, Constants.PLACEMENT_ID_59);
        mUnitIdToPlacementIdMap.put(Constants.UNIT_ID_178, Constants.PLACEMENT_ID_178);
        mUnitIdToPlacementIdMap.put(Constants.UNIT_ID_179, Constants.PLACEMENT_ID_179);
        mUnitIdToPlacementIdMap.put(Constants.UNIT_ID_60, Constants.PLACEMENT_ID_60);
        mUnitIdToPlacementIdMap.put(Constants.UNIT_ID_61, Constants.PLACEMENT_ID_61);
        mUnitIdToPlacementIdMap.put(Constants.UNIT_ID_62, Constants.PLACEMENT_ID_62);
        mUnitIdToPlacementIdMap.put(Constants.UNIT_ID_63, Constants.PLACEMENT_ID_63);
        mUnitIdToPlacementIdMap.put(Constants.UNIT_ID_67, Constants.PLACEMENT_ID_67);
        mUnitIdToPlacementIdMap.put(Constants.UNIT_ID_243, Constants.PLACEMENT_ID_243);
        mUnitIdToPlacementIdMap.put(Constants.UNIT_ID_244, Constants.PLACEMENT_ID_244);
        mUnitIdToPlacementIdMap.put(Constants.UNIT_ID_CHARGING, Constants.PLACEMENT_ID_CHARGING);

        LeoLog.i(TAG, "MobvistaEngine() called done");
    }

    /**
     * 程序启动，获取所有广告位的数据作为缓存
     */
    public void preloadMobvistaAds() {
        LeoLog.i(TAG, "loadMobvistaAds() called done");
        for (String unitId : mUnitIdToPlacementIdMap.keySet()) {
            loadSingleMobAd(unitId);
        }
    }

    private void loadSingleMobAd(String unitId) {
        // 对应的ad正在loading，不重复load
        MobVistaLoadingNative loadingNative = mMobVistaLoadingNative.get(unitId);
        if (loadingNative != null &&
                (SystemClock.elapsedRealtime()-loadingNative.requestTimeMs < 60*1000)) {
            LeoLog.d(TAG, "["+unitId+"]previous loading process ongoing, ignore");
            return;
        }

        long start = SystemClock.elapsedRealtime();
        String placementId = mUnitIdToPlacementIdMap.get(unitId);

        // check placement first
        if (TextUtils.isEmpty(placementId)) {
            LeoLog.i(TAG, "cannot find place mentid of this unitid.");
            MobvistaListener listener = mMobVistaListeners.remove(unitId);
            if (listener != null) {
                listener.onMobvistaFinished(ERR_NOT_FOUND_PLACEMENTID, null, "Mobvista execute throwable.");
            }
            return;
        }

        MobvistaAdNative nativeAd = MobvistaAd.newNativeController(mAppContext, unitId, placementId);
        LeoLog.d(TAG, "create new MobvistaAdNative and start to load");
        try {
            // 这个地方执行导致crash，直接catch住
            nativeAd.loadAd(new AdListenerImpl(unitId));
            LeoLog.i(TAG, "loadSingleMobAd -> ad[" + unitId + "], cost time = "
                    + (SystemClock.elapsedRealtime() - start));
            mMobVistaLoadingNative.put(unitId, new MobVistaLoadingNative(nativeAd, SystemClock.elapsedRealtime()));
        } catch (Throwable thr) {
            MobvistaListener listener = mMobVistaListeners.get(unitId);
            if (listener != null) {
                listener.onMobvistaFinished(ERR_MOBVISTA_FAIL, null, "Mobvista execute throwable.");
            }
            doReleaseInner(unitId);
            return;
        }
    }

    /**
     * 获取广告内容
     *
     * @param unitId
     * @param listener
     */
    public void loadMobvista(String unitId, MobvistaListener listener) {
        LeoLog.i(TAG, "Attach to Native Ad");
        if (listener == null) return;

        if (TextUtils.isEmpty(unitId)) {
            LeoLog.i(TAG, "unit id is null.");
            listener.onMobvistaFinished(ERR_UNITID_NULL, null, null);
            return;
        }

        mMobVistaListeners.put(unitId, listener);

        // 广告过时则需要重新拉取
        MobvistaAdData mobvista = mMobVistaCacheMap.get(unitId);
        if (isOutOfDate(mobvista)) {
            loadSingleMobAd(unitId);
            LeoLog.i(TAG, "data out ofdate: reload new one.");
            return;
        }

        boolean loading = mMobVistaLoadingNative.get(unitId) != null;
        if (loading) {
            LeoLog.i(TAG, "MobvistaNative is loading");
            return;
        }

        MobvistaAdData adData = mMobVistaCacheMap.get(unitId);
        if (adData != null && adData.campaign != null && adData.nativeAd != null) {
            listener.onMobvistaFinished(ERR_OK, adData.campaign, null);
        }
    }

    /**
     * @param activity
     * @return
     * @deprecated 创建appwall广告接口
     */
    public MobvistaAdWall createAdWallController(Activity activity) {
        return createAdWallController(activity, null);
    }

    public MobvistaAdWall createAdWallController(Activity activity, String unitId) {
        if (TextUtils.isEmpty(unitId)) {
            LeoLog.i(TAG, "unit id is null.");
            return null;
        }
        String placementId = mUnitIdToPlacementIdMap.get(unitId);
        if (TextUtils.isEmpty(placementId)) {
            LeoLog.i(TAG, "cannot find place mentid of this unitid.");
            return null;
        }
        return MobvistaAd.newAdWallController(activity, unitId, placementId);
    }

    /**
     * 注册广告点击事件
     *
     * @param unitId
     * @param view
     */
    public void registerView(String unitId, View view) {
        registerView(unitId, view, null);
    }

    /**
     * 注册广告点击事件
     *
     * @param unitId
     * @param view
     * @param listener 用新listener替换旧的
     */
    public void registerView(String unitId, View view, MobvistaListener listener) {
        MobvistaAdData adObject = mMobVistaCacheMap.get(unitId);
        if (adObject == null) {
            return;
        }
        MobvistaAdNative adNative = adObject.nativeAd;
        if (adNative == null) {
            LeoLog.i(TAG, "havnt register activity before.");
            return;
        }

        // replace the listener
        if (listener != null) {
            mMobVistaListeners.put(unitId, listener);
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

            @Override
            public void onDismissLoading(Campaign arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean onInterceptDefaultLoadingDialog() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onShowLoading(Campaign arg0) {
                // TODO Auto-generated method stub

            }
        });
    }

    /**
     * 释放广告资源
     */
    public void release(String unitId) {
        LeoLog.i(TAG, "release [" + unitId + "]");
        try {
            doReleaseInner(unitId);

            // 重新拉取广告
            if (shouldReloadAd(unitId)) {
                LeoLog.d(TAG, "reload ad[" + unitId + "] when release");
                removeMobAdData(unitId);
                loadSingleMobAd(unitId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LeoLog.e("TAG", "can not release ad");
        }

    }

    private boolean shouldReloadAd(String unitId) {
        MobvistaAdData adData = mMobVistaCacheMap.get(unitId);

        if (adData == null) return true;
        long lastRequestTime = adData.requestTimeMs;
        long now = System.currentTimeMillis();
        LeoLog.d(TAG, "["+unitId+"]lastRequest:" + lastRequestTime + "; now:" + now
                +"; period:" + (now-lastRequestTime));
        if (now-lastRequestTime > mPref.getADFetchInterval()*MILLIS_IN_MINUTE) {
            return true;
        }

        return false;
    }

    private void doReleaseInner(String unitId) {
        // 因为目前loading与UI已无关系，release的时候无需清除loading的广告
//        MobvistaAdNative adNative = mMobVistaLoadingNative.remove(unitId);
//        if (adNative != null) {
//            try {
//                adNative.release();
//            } catch (Throwable e) {
//            }
//        }

        mMobVistaListeners.remove(unitId);
//        removeMobAdData(unitId);
    }

    public void removeMobAdData(String unitId) {
        MobvistaAdData adData = mMobVistaCacheMap.remove(unitId);
        if (adData != null) {
            MobvistaAdNative nativeAd = adData.nativeAd;
            if (nativeAd != null) {
                nativeAd.release();
            }
        }
    }

    private static boolean isOutOfDate(MobvistaAdData mobvista) {
        if (mobvista == null) return true;

        long current = System.currentTimeMillis();
        return current - mobvista.requestTimeMs > AD_TIMEOUT;
    }

    private class AdListenerImpl implements AdListener {
        private String mUnitId;

        public AdListenerImpl(String unitId) {
            this.mUnitId = unitId;
        }

        @Override
        public void onAdLoaded(Campaign campaign) {
            LeoLog.i(TAG, "onAdLoaded [" + mUnitId + "]: " + campaign.getAppName() + "; imageURL=" + campaign.getImageUrl());

            MobVistaLoadingNative loadingNative = mMobVistaLoadingNative.remove(mUnitId);

            if (loadingNative == null) {
                /* AM-4016: 这是一次超时的load操作，直接抛弃 */
                return;
            }

            // 将load成功的 MobvistaAdNative 对象移动到 MobvistaAdData 中
            MobvistaAdData mobvista = new MobvistaAdData();
            mobvista.nativeAd = loadingNative.nativeAd;
            mobvista.campaign = campaign;
            mobvista.requestTimeMs = System.currentTimeMillis();
            mMobVistaCacheMap.put(mUnitId, mobvista);

            MobvistaListener listener = mMobVistaListeners.get(mUnitId);

            if (listener != null) {
                listener.onMobvistaFinished((campaign == null) ? ERR_MOBVISTA_RESULT_NULL : ERR_OK, campaign, null);
            }
        }

        @Override
        public void onAdLoadError(String s) {
            LeoLog.i(TAG, "onAdLoadError[" + mUnitId + "], msg: " + s);
            MobvistaListener listener = mMobVistaListeners.get(mUnitId);

            if (listener != null) {
                listener.onMobvistaFinished(ERR_MOBVISTA_FAIL, null, s);
            }

            mMobVistaLoadingNative.remove(mUnitId);
        }

        @Override
        public void onAdClick(Campaign campaign) {
            Campaign data = null;
            MobvistaAdData m = mMobVistaCacheMap.remove(mUnitId);
            if (m != null) {
                data = m.campaign;
            }

            MobvistaListener listener = mMobVistaListeners.get(mUnitId);
            if (listener != null) {
                listener.onMobvistaClick(campaign == null ? data : campaign, mUnitId);
            }
            // 点击之后，重新load此位置的广告
            LeoLog.i(TAG, "reload the clicked Ad"); 	
            if(m != null && m.nativeAd != null) {
                try {
                    MobvistaAd.release();
                    mMobVistaCacheMap.clear();
                } catch (Exception e) {
                }
            }
            loadSingleMobAd(mUnitId);
            if(!Constants.UNIT_ID_59.equals(mUnitId)) {
                loadSingleMobAd(Constants.UNIT_ID_59);
            }
        }
		
		
    }

    private static class MobvistaAdData {
        public Campaign campaign;
        public MobvistaAdNative nativeAd;
        public long requestTimeMs;
    }

    private static class MobVistaLoadingNative {
        public long requestTimeMs;
        public MobvistaAdNative nativeAd;
        public MobVistaLoadingNative (MobvistaAdNative nativeAd, long timestamp) {
            this.requestTimeMs = timestamp;
            this.nativeAd = nativeAd;
        }
    }

	public boolean isADCacheEmpty() {
		return mMobVistaCacheMap == null ? true : mMobVistaCacheMap.isEmpty();
	}

}
