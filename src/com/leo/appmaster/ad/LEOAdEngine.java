package com.leo.appmaster.ad;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.utils.LeoLog;
import com.leo.leoadlib.LeoAdFactory;
import com.leo.leoadlib.LeoAdNative;

import java.util.HashMap;
import java.util.Map;

//import com.mobvista.sdk.m.core.MobvistaAd;
//import com.mobvista.sdk.m.core.MobvistaAdWall;

/**
 * 广告相关引擎
 *  广告加载、回调
 *  注册广告点击
 *  创建appwall类型广告接口
 * @author Jasper
 *
 */
public class LEOAdEngine {
    private static final String TAG = "LEOAdEngine";
    
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
    private static final int AD_TIMEOUT = 60 * 60 * 1000; // QA need to modify for test
    
    /**
     * 一分钟内，不要重复拉取广告
     * */
    private static final int MILLIS_IN_MINUTE = 60 * 1000;

    private static LEOAdEngine sInstance;
    
    private Map<String, LeoCompositeData> mLEOLoadedNatives;
    private Map<String, LeoListener> mLeoListeners;
    private Map<String, LEONativeAd> mLeoLoadingNatives;

    private Map<String, String> mUnitIdToPlacementIdMap;

	public static interface LeoListener {
        /**
         * 广告请求回调
         * @param code 返回码，如ERR_PARAMS_NULL
         * @param campaign 请求成功的广告结构体，失败为null
         * @param msg 请求失败sdk返回的描述，成功为null
         */
        public void onLeoAdLoadFinished(int code, LEONativeAdData campaign, String msg);
        /**
         * 广告点击回调
         * @param campaign
         */
        public void onLeoAdClick(LEONativeAdData campaign);
    }
    
    public static synchronized LEOAdEngine getInstance(Context ctx) {
        if (sInstance == null) {
            sInstance = new LEOAdEngine(ctx);
        }
        
        return sInstance;
    }
    
    private LEOAdEngine(Context ctx) {
        mAppContext = ctx;
        mPref = AppMasterPreference.getInstance(ctx);
        mLEOLoadedNatives = new HashMap<String, LeoCompositeData>();
        mLeoListeners = new HashMap<String, LeoListener>();
        mLeoLoadingNatives = new HashMap<String, LEONativeAd>();

        mUnitIdToPlacementIdMap = new HashMap<String, String>();
        mUnitIdToPlacementIdMap.put(LEOAdManager.UNIT_ID_LOCK, LEOAdManager.PLACEMENTID_LOCK);
        LeoLog.i(TAG, "LEOAdEngine() called done");
    }
    

    private LEONativeAd createLEONative(String unitId) {
        String placementId = mUnitIdToPlacementIdMap.get(unitId);
        FBNativeAd fb = new FBNativeAd(mAppContext, placementId);
		/*從max sdk得到的廣告對象*/
		LeoAdNative leoAdNative = LeoAdFactory.newNativeController(
				AppMasterApplication.getInstance(), unitId);
		
        return new LEONativeAd(fb, leoAdNative);
    }
    
    private void loadSingleMobAd(String unitId, LEONativeAd nativeAd){
        // 对应的ad正在loading，不重复load
        if(mLeoLoadingNatives.get(unitId) != null){
            return;
        }
        String placementId = mUnitIdToPlacementIdMap.get(unitId);
        
        // check placement first
        if (TextUtils.isEmpty(placementId)) {
            LeoLog.i(TAG, "cannot find place mentid of this unitid.");
            LeoListener listener = mLeoListeners.remove(unitId);
            if(listener != null){
                listener.onLeoAdLoadFinished(ERR_NOT_FOUND_PLACEMENTID, null, "Mobvista execute throwable.");
            }
            return;
        }

        if (nativeAd == null) {
            nativeAd = createLEONative(unitId);
        }
        try {
            // 这个地方执行导致crash，直接catch住
            nativeAd.loadNativeAd(new AdListenerImpl(unitId));
            LeoLog.i(TAG, "loadSingleMobAd -> ad["+unitId+"]");
            mLeoLoadingNatives.put(unitId, nativeAd);
        } catch (Throwable thr) {
            LeoListener listener = mLeoListeners.get(unitId);
            if(listener != null){
                listener.onLeoAdLoadFinished(ERR_MOBVISTA_FAIL, null, "Mobvista execute throwable.");
            }
            doReleaseInner(unitId);
            return;
        }
    }
    
    
    /**
     * 注册广告点击事件
     * @param view
     */
    public void registerView(String unitId, View view) {
        LeoCompositeData adObject = mLEOLoadedNatives.get(unitId);
        if(adObject == null){
            return;
        }
        LEONativeAd adNative = adObject.nativeAd;
        if (adNative == null) {
            LeoLog.i(TAG, "havnt register activity before.");
            return;
        }
        LeoLog.i(TAG, "registerView");
        adNative.bindAdWithView(view);
    }
    
    /**
     * 释放广告资源
     */
    public void release(String unitId) {
        LeoLog.i(TAG, "release ["+unitId+"]");
        doReleaseInner(unitId);

        // 重新拉取广告
        if(shouldReloadAd(unitId)) {
            LeoLog.d(TAG, "reload ad[" +unitId+ "] when release");
            LEONativeAd nativeAd = removeMobAdData(unitId);
			loadSingleMobAd(unitId, nativeAd);
        }
    }
    
    private boolean shouldReloadAd(String unitId){
       LeoCompositeData adData = mLEOLoadedNatives.get(unitId);

        if(adData == null) return true;
        if(System.currentTimeMillis() - adData.requestTimeMs
                > MILLIS_IN_MINUTE * mPref.getAdFetchInterval()) {
            return true;
        }
        
        return false;
    }

    private void doReleaseInner(String unitId) {
        // 因为目前loading与UI已无关系，release的时候无需清除loading的广告
//        MobvistaAdNative adNative = mLeoLoadingNatives.remove(unitId);
//        if (adNative != null) {
//            try {
//                adNative.release();
//            } catch (Throwable e) {
//            }
//        }
        
        mLeoListeners.remove(unitId);
//        removeMobAdData(unitId);
    }
    
    private LEONativeAd removeMobAdData(String unitId){
        LeoCompositeData adData = mLEOLoadedNatives.remove(unitId);
        if(adData != null){
            LEONativeAd nativeAd = adData.nativeAd;
            if(nativeAd != null){
                nativeAd.release();
            }
            return nativeAd;
        } else {
            return null;
        }
    }

    private static boolean isOutOfDate(LeoCompositeData mobvista) {
        if (mobvista == null) return true;
        
        long current = System.currentTimeMillis();
        return current - mobvista.requestTimeMs > AD_TIMEOUT; 
    }

    private class AdListenerImpl implements LEONativeAd.LEONativeAdListener {
        private String mUnitId;
        public AdListenerImpl(String unitId) {
            this.mUnitId = unitId;
        }

        @Override
        public void onAdLoaded(LEONativeAdData adData) {
			if (adData == null) {
				return;
			}
			try {
				LeoLog.i(TAG, "onAdLoaded ["+mUnitId+"] title: " + adData);
				LeoLog.i(TAG, "onAdLoaded ["+mUnitId+"] CTA: " + adData.getAdCall());
				LeoLog.i(TAG, "onAdLoaded ["+mUnitId+"] description: " + adData.getAppDesc());
				LeoLog.i(TAG, "onAdLoaded ["+mUnitId+"] imageURL: " + adData.getImageUrl());
				LeoLog.i(TAG, "onAdLoaded ["+mUnitId+"] iconURL: " + adData.getIconUrl());
			} catch (Exception e) {
			}
			LeoCompositeData mobvista = new LeoCompositeData();
            // 将load成功的 MobvistaAdNative 对象移动到 LeoCompositeData 中
            mobvista.nativeAd = mLeoLoadingNatives.remove(mUnitId);
            mobvista.campaign = adData;
            mobvista.requestTimeMs = System.currentTimeMillis();
            mLEOLoadedNatives.put(mUnitId, mobvista);

            LeoListener listener = mLeoListeners.get(mUnitId);

            if (listener != null) {
                listener.onLeoAdLoadFinished((adData == null) ? ERR_MOBVISTA_RESULT_NULL : ERR_OK, adData, null);
            }
        }

        @Override
        public void onAdLoadFailed() {
            LeoLog.i(TAG, "onAdLoadError[" + mUnitId);
            LeoListener listener = mLeoListeners.get(mUnitId);

            if (listener != null) {
                listener.onLeoAdLoadFinished(ERR_MOBVISTA_FAIL, null, "ad load failed");
            }

            mLeoLoadingNatives.remove(mUnitId);
        }

        @Override
        public void onAdClicked() {
            LEONativeAdData data = null;
            LeoCompositeData m = mLEOLoadedNatives.get(mUnitId);
            if (m != null) {
                data = m.campaign;
            }

            // 响应之后，干掉listener
            LeoListener listener = mLeoListeners.get(mUnitId);
            if (listener != null) {
                listener.onLeoAdClick(data);
            }
            // 点击之后，重新load此位置的广告
            LeoLog.i(TAG, "reload the clicked Ad");
			loadSingleMobAd(mUnitId, m.nativeAd);
        }

    }

	/**
	 * 获取广告内容
	 * @param listener
	 */
	public void loadMobvista(String unitId, LeoListener listener) {
		LeoLog.i(TAG, "Attach to Native Ad");
		if (listener == null) return;

		if (TextUtils.isEmpty(unitId)) {
			LeoLog.i(TAG, "unit id is null.");
			listener.onLeoAdLoadFinished(ERR_UNITID_NULL, null, null);
			return;
		}

		// 记录下listener
		mLeoListeners.put(unitId, listener);

		// 广告过时则需要重新拉取
		LeoCompositeData cData = mLEOLoadedNatives.get(unitId);
		if (isOutOfDate(cData)) {
			if (cData != null) {
				loadSingleMobAd(unitId, cData.nativeAd);
			} else {
				loadSingleMobAd(unitId, null);
			}
			LeoLog.i(TAG, "data out ofdate: reload new one.");
			return;
		}

		boolean loading = mLeoLoadingNatives.get(unitId) != null;
		if (loading){
			LeoLog.i(TAG, "MobvistaNative is loading");
			return;
		}

		LeoCompositeData adData = mLEOLoadedNatives.get(unitId);
		if(adData!= null && adData.campaign!=null && adData.nativeAd!=null){
			listener.onLeoAdLoadFinished(ERR_OK, adData.campaign, null);
		}
	}
	
    private static class LeoCompositeData {
        public LEONativeAdData campaign;
        public LEONativeAd nativeAd;
        public long requestTimeMs;
    }

}
