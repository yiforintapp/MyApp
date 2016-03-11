/**
 * tag_20151016-17:35:
 * 修改广告拉取策略：当进入页面后，优先拉取新广告。
 * 仅当拉取失败之后，再判断是否有缓存数据，有的话显示缓存好的广告。
 * **/

package com.leo.appmaster.ad;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;


public class LEONativeAd {

    /*
     * Major Advertise, always load it first.
     * */
    private BaseNativeAd mMajorAd;
    /*
     * Minor Advertise, load it when failed to load major one.
     * */
    private BaseNativeAd mMinorAd;
    /*
     * Advertise ready to show.
     * */
    private BaseNativeAd mAvailableAd;

    private LEONativeAdListener mLeoListener;

    private boolean isLoading = false;

    private final static String TAG = "STONE_AD_DEBUG";

    private final static int MSG_NATIVE_AD_LOAD_OK = 1;
    private final static int MSG_NATIVE_AD_LOAD_FAIL = 2;
    private final static int MSG_NATIVE_AD_CLICK = 3;

    public interface LEONativeAdListener {
        public void onAdLoaded(LEONativeAdData adData);
        public void onAdLoadFailed();
        public void onAdClicked();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NATIVE_AD_LOAD_OK:
                    if(mLeoListener != null){
                        mLeoListener.onAdLoaded(getAdData());
                    }
                    break;
                case MSG_NATIVE_AD_LOAD_FAIL:
                    if(mLeoListener != null){
                        mLeoListener.onAdLoadFailed();
                    }
                    break;
                case MSG_NATIVE_AD_CLICK:
                    if(mLeoListener != null){
                        mLeoListener.onAdClicked();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public LEONativeAd(BaseNativeAd majorAd, BaseNativeAd minorAd){

        mMinorAd = minorAd;
        mMajorAd = majorAd;

        mMajorAd.setAdListener(new BaseNativeAd.LEOInnerNativeAdListener() {

            @Override
            public void onLoadFailed() {
                loadMinorAd();
            }

            @Override
            public void onLoadDone(LEONativeAdData adData) {
                isLoading = false;
                mAvailableAd = mMajorAd;
                mHandler.sendEmptyMessage(MSG_NATIVE_AD_LOAD_OK);
            }

            @Override
            public void onClicked() {
                mHandler.sendEmptyMessage(MSG_NATIVE_AD_CLICK);
            }
        });
    }

    private void loadMinorAd(){
        SDKWrapper.addEvent(AppMasterApplication.getInstance(), SDKWrapper.P1, "max_request", "lock");
        mMinorAd.setAdListener(new BaseNativeAd.LEOInnerNativeAdListener() {

            @Override
            public void onLoadFailed() {
                mHandler.sendEmptyMessage(MSG_NATIVE_AD_LOAD_FAIL);
            }

            @Override
            public void onLoadDone(LEONativeAdData adData) {
                isLoading = false;
                mAvailableAd = mMinorAd;
                mHandler.sendEmptyMessage(MSG_NATIVE_AD_LOAD_OK);
            }

            @Override
            public void onClicked() {
                mHandler.sendEmptyMessage(MSG_NATIVE_AD_CLICK);
            }
        });

        mMinorAd.loadAd();
    }

    /**
     * Start to load native advertise:
     * load major first, then load minor when failed to load major.
     */
    public void loadNativeAd(LEONativeAdListener listener){
        LeoLog.d(TAG, "loadNativeAd: load major ad - facebook");
        mLeoListener = listener;
        if(isLoading){
            LeoLog.i(TAG, "loadNativeAd: still loading, ignore this load request");
            return;
        }
        isLoading = true;
        mMajorAd.loadAd();
    }

    /**
     * Check if this native advertise ready to show.
     * @return true if ready to show, false otherwise
     * */
    public boolean isAdReady(){
        if(mAvailableAd == null){
            return false;
        }else{
            return mAvailableAd.readyToShow();
        }
    }

    protected final LEONativeAdData getAdData(){
        if(isAdReady()){
            return mAvailableAd.getData();
        }
        return null;
    }

    /**
     * Bind a View with the advertise.
     * Application should call this after showing all elements.
     * Advertising SDK will make this view interactive.
     * */
    public void bindAdWithView(View view){
        if(isAdReady()){
            mAvailableAd.bindToView(view);
        }
    }

    /**
     * Call this in Activity's onDestroy() call.
     */
    public void detachWithView(){
        if(mAvailableAd != null){
            mAvailableAd.unbindView();
        }
    }

    /**
     * Call this in Activity's onDestroy() call.
     */
    public void release() {
        if(mAvailableAd != null){
            mAvailableAd.release();
        }
    }

}
