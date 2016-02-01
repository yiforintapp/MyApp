package com.leo.appmaster.ad;

import android.content.Context;
import android.view.View;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.utils.LeoLog;
import com.leo.leoadlib.AdListener;
import com.leo.leoadlib.LeoAdFactory;
import com.leo.leoadlib.LeoAdNative;
import com.leo.leoadlib.model.Campaign;

/**
 * Created by stone on 16/2/1.
 */
public class MaxNativeAd extends BaseNativeAd {
    private final static String TAG = "MAX_AD_DEBUG";

    private LeoAdNative maxAd;
    private Campaign mCampaign;
    private Context mContext;

    public MaxNativeAd (Context context, String unitId) {
        mContext = context;
        maxAd = LeoAdFactory.newNativeController(
                AppMasterApplication.getInstance(), unitId);
    }

    @Override
    public void loadAd() {
        LeoLog.d(TAG, "MaxSDK start to load");
        mCampaign = null;
        maxAd.loadAd(null, new AdListener() {
            @Override
            public void onAdLoaded(Campaign campaign) {
                if(mListener != null){
                    LeoLog.d(TAG, "MaxSDK onAdLoaded -> " + campaign.getTitle());
                    mCampaign = campaign;
                    LEONativeAdData data = new LEONativeAdData();
                    data.mTitle = campaign.getTitle();
                    data.mTitleForButton = campaign.getCta();
                    data.mDescription = campaign.getDescription();
                    data.mIconURL = campaign.getIconUrl();
                    data.mPreviewURL = campaign.getPreviewUrl();
                    mListener.onLoadDone(data);
                }
            }

            @Override
            public void onAdLoadError(int i, String s) {
                if (mListener != null) {
                    mListener.onLoadFailed();
                }
            }

            @Override
            public void onAdClick(Campaign campaign) {
                if (mListener != null) {
                    mListener.onClicked();
                }
            }
        });
    }

    @Override
    protected void bindToView(View view) {
        if (readyToShow()) {
            LeoLog.d(TAG, "MaxSDK registerView");
            maxAd.registerView(view);
        }
    }

    @Override
    protected void unbindView() {

    }

    @Override
    protected LEONativeAdData getData() {
        if (readyToShow()) {
            LeoLog.d(TAG, "MaxSDK bring back data");
            LEONativeAdData data = new LEONativeAdData();
            data.mTitle = mCampaign.getTitle();
            data.mTitleForButton = mCampaign.getCta();
            data.mDescription = mCampaign.getDescription();
            data.mIconURL = mCampaign.getIconUrl();
            data.mPreviewURL = mCampaign.getPreviewUrl();
            return data;
        }
        return null;
    }

    @Override
    protected boolean readyToShow() {
        return (mCampaign!=null);
    }
}
