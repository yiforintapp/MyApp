package com.leo.appmaster.ad;

import android.content.Context;
import android.view.View;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.utils.LeoLog;
import com.mobvista.msdk.out.Campaign;

import java.util.List;

/**
 * Created by stone on 16/2/26.
 */
public class ADEngineWrapper {

    private static final String TAG = "ADEngineWrapper [AD_DEBUG]";

    /* 两个可选的广告来源 */
    public static final int SOURCE_MOB = AppMasterPreference.AD_SDK_SOURCE_USE_3TH;
    public static final int SOURCE_MAX = AppMasterPreference.AD_SDK_SOURCE_USE_MAX;

    private LEOAdEngine mMaxEngine;
    private MobvistaEngine mMobEngine;

    public static interface WrappedAdListener {
        /**
         * 广告请求回调
         * @param code 返回码，如ERR_PARAMS_NULL
         * @param campaigns 请求成功的广告结构体，失败为null
         * @param msg 请求失败sdk返回的描述，成功为null
         */
        public void onWrappedAdLoadFinished(int code, List<WrappedCampaign> campaigns, String msg);
        /**
         * 广告点击回调
         * @param campaign
         */
        public void onWrappedAdClick(WrappedCampaign campaign, String unitID);
    }

    private static ADEngineWrapper sInstance;
    private ADEngineWrapper (Context context) {
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
    public void loadAd (final int source, final String unitId, final WrappedAdListener listener) {
		LeoLog.e(TAG, "AD TYPE :" + source + " AD ID: " + unitId);
        if (source == SOURCE_MAX) {
            mMaxEngine.loadMobvista(unitId, new LEOAdEngine.LeoListener() {
                @Override
                public void onLeoAdLoadFinished(int code, LEONativeAdData campaign, String msg) {
                    LeoLog.d(TAG, "[" + unitId + "] source = " + source + "; code = " + code);
                    List<WrappedCampaign> wrappedCampaignList = null;
                    if (code == LEOAdEngine.ERR_OK) {
						wrappedCampaignList = WrappedCampaign.fromMaxSDK(campaign);
                    }
                    listener.onWrappedAdLoadFinished(code, wrappedCampaignList, msg);
                }

                @Override
                public void onLeoAdClick(LEONativeAdData campaign, String unitID) {
                    listener.onWrappedAdClick(WrappedCampaign.converCampaignFromMax(campaign), unitID);
                }
            });
        } else {
			
			mMobEngine.loadMobvista(unitId, new MobvistaEngine.MobvistaListener() {
				@Override
				public void onMobvistaFinished(int code, List<Campaign> campaigns, String msg) {
					LeoLog.d(TAG, "[" + unitId + "] source = " + source + "; code = " + code);
					List<WrappedCampaign> wrappedCampaignList = null;
					if (code == MobvistaEngine.ERR_OK) {
						wrappedCampaignList = WrappedCampaign.fromMabVistaSDK(campaigns);
					}
					listener.onWrappedAdLoadFinished(code, wrappedCampaignList, msg);
				}

				@Override
				public void onMobvistaClick(com.mobvista.msdk.out.Campaign campaign, String unitID) {
					
					listener.onWrappedAdClick(WrappedCampaign.converCampaignFromMob(campaign), unitID);
				}
			});
			
            /*mMobEngine.loadMobvista(unitId, new MobvistaEngine.MobvistaListener() {
                @Override
                public void onMobvistaFinished(int code, Campaign campaign, String msg) {
                    LeoLog.d(TAG, "[" + unitId + "] source = " + source + "; code = " + code);
                    WrappedCampaign wrappedCampaign = null;
                    if (code == MobvistaEngine.ERR_OK) {
                        wrappedCampaign = WrappedCampaign.fromMabVistaSDK(campaign);
                    }
                    listener.onWrappedAdLoadFinished(code, wrappedCampaign, msg);
                }

                @Override
                public void onMobvistaClick(Campaign campaign, String unitID) {
                    listener.onWrappedAdClick(WrappedCampaign.fromMabVistaSDK(campaign), unitID);
                }
				
				
            });*/
        }
    }

    public void registerView (int source, View view, String unitId) {
        LeoLog.d(TAG, "registerView called");
        if (source == SOURCE_MAX) {
            mMaxEngine.registerView(unitId, view);
        } else {
            mMobEngine.registerView(unitId, view);
        }
    }

    public void releaseAd (int source, String unitId, View view) {
        if (source == SOURCE_MAX) {
            mMaxEngine.release(unitId);
        } else {
            mMobEngine.release(unitId, view);
        }
    }
	
	public void removeMobAdData(int source, String unitId, View view) {
		if (source == SOURCE_MAX) {
			mMaxEngine.removeMobAdData(unitId);
		} else {
			mMobEngine.removeMobAdData(unitId, view);
		}
	}
	
	public boolean isADCacheEmpty(int source) {
		if (source == SOURCE_MAX) {
			return mMaxEngine.isADCacheEmpty();
		} else {
			return mMobEngine.isADCacheEmpty();
		}
	}
	
}
