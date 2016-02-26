package com.leo.appmaster.ad;

import android.content.Context;
import android.view.View;

import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.utils.LeoLog;
import com.mobvista.sdk.m.core.entity.Campaign;

/**
 * Created by stone on 16/2/26.
 */
public class ADEngineWrapper {

    private static final String TAG = "ADEngineWrapper";

    /* 两个可选的广告来源 */
    public static final int SOURCE_MOB = 1;
    public static final int SOURCE_MAX = 2;

    private Context mContext;

    private LEOAdEngine mMaxEngine;
    private MobvistaEngine mMobEngine;

    public static interface WrappedAdListener {
        /**
         * 广告请求回调
         * @param code 返回码，如ERR_PARAMS_NULL
         * @param campaign 请求成功的广告结构体，失败为null
         * @param msg 请求失败sdk返回的描述，成功为null
         */
        public void onWrappedAdLoadFinished(int code, WrappedCampaign campaign, String msg);
        /**
         * 广告点击回调
         * @param campaign
         */
        public void onWrappedAdClick(WrappedCampaign campaign);
    }

    private static ADEngineWrapper sInstance;
    private ADEngineWrapper (Context context) {
        mContext =  context;
        mMaxEngine = LEOAdEngine.getInstance(context);
        mMobEngine = MobvistaEngine.getInstance(context);
    }

    public static ADEngineWrapper getInstance (Context context) {
        if (sInstance == null) {
            sInstance = new ADEngineWrapper(context);
        }
        return sInstance;
    }

    /***
     * 请求广告数据
     */
    public void loadAd (final int source, String unitId, final WrappedAdListener listener) {
        if (source == SOURCE_MAX) {
            mMaxEngine.loadMobvista(unitId, new LEOAdEngine.LeoListener() {
                @Override
                public void onLeoAdLoadFinished(int code, LEONativeAdData campaign, String msg) {
                    LeoLog.d(TAG, "source = " + source + "; code = " + code);
                    WrappedCampaign wrappedCampaign = null;
                    if (code == LEOAdEngine.ERR_OK) {
                        wrappedCampaign = WrappedCampaign.fromMaxSDK(campaign);
                    }
                    listener.onWrappedAdLoadFinished(code, wrappedCampaign, msg);
                }

                @Override
                public void onLeoAdClick(LEONativeAdData campaign, String unitID) {
                    listener.onWrappedAdClick(WrappedCampaign.fromMaxSDK(campaign));
                }
            });
        } else {
            mMobEngine.loadMobvista(unitId, new MobvistaEngine.MobvistaListener() {
                @Override
                public void onMobvistaFinished(int code, Campaign campaign, String msg) {
                    LeoLog.d(TAG, "source = " + source + "; code = " + code);
                    WrappedCampaign wrappedCampaign = null;
                    if (code == MobvistaEngine.ERR_OK) {
                        wrappedCampaign = WrappedCampaign.fromMabVistaSDK(campaign);
                    }
                    listener.onWrappedAdLoadFinished(code, wrappedCampaign, msg);
                }

                @Override
                public void onMobvistaClick(Campaign campaign, String unitID) {
                    listener.onWrappedAdClick(WrappedCampaign.fromMabVistaSDK(campaign));
                }
            });
        }
    }

    public void registerView (int source, View view, String unitId) {
        if (source == SOURCE_MAX) {
            mMaxEngine.registerView(unitId, view);
        } else {
            mMobEngine.registerView(unitId, view);
        }
    }

    public void releaseAd (int source, String unitId) {
        if (source == SOURCE_MAX) {
            mMaxEngine.release(unitId);
        } else {
            mMobEngine.release(unitId);
        }
    }

}
