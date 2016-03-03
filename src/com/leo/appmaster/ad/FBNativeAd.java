package com.leo.appmaster.ad;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.leo.appmaster.utils.LeoLog;
import com.leo.leoadlib.MaxSdk;

import java.lang.ref.WeakReference;

public class FBNativeAd extends BaseNativeAd implements AdListener {
    
    private Context mContext;
    private String mPlacementID;
    private NativeAd mNativeAd;
    
    private final static int CHECK_RESULT_OK = 1;
    private final static int CHECK_RESULT_FAILED = 2;
    
    private final static String TAG = "FACEBOOK_AD_DEBUG";
    private final static String FACEBOOK_URL = "http://www.facebook.com";

    static class MainHandler extends Handler {
        WeakReference<FBNativeAd> mFBNativeAdRef;

        public MainHandler(FBNativeAd ad) {
            mFBNativeAdRef = new WeakReference<FBNativeAd>(ad);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mFBNativeAdRef == null) {
                return;
            }
            FBNativeAd ad = mFBNativeAdRef.get();
            if (ad == null) {
                return;
            }
            switch (msg.what) {
                case CHECK_RESULT_OK:
                    LeoLog.d(TAG, "can link to facebook, start to load now");
                    NativeAd fbad = new NativeAd(ad.mContext, ad.mPlacementID);
                    fbad.setAdListener(ad);
                    fbad.loadAd();
                    break;
                case CHECK_RESULT_FAILED:
                    LeoLog.i(TAG, "shit happened, can not link to facebook");
                    if(ad.mListener != null){
                        ad.mListener.onLoadFailed();
                    }
                    break;
                default:
                    break;
            }
        }
    }
    
    private Handler mPushToMain = null;
    
    public FBNativeAd(Context ctx, String id){
        mContext = ctx;
        mPlacementID = id;
        mNativeAd = null;
        mPushToMain = new MainHandler(this);
    }

    @Override
    public void loadAd() {
        /* facebook's NativeAd can not load more than once */
        LeoLog.d(TAG, "FBNativeAd: loadAd() called");
        
        URLChecker.checkURL(FACEBOOK_URL, new URLChecker.URLCheckCallback() {
            
            @Override
            public void onURLUnavailable() {
                mPushToMain.sendEmptyMessage(CHECK_RESULT_FAILED);
            }
            
            @Override
            public void onURLAvailable() {
                mPushToMain.sendEmptyMessage(CHECK_RESULT_OK);
            }
        });

    }

    @Override
    public void bindToView(View view) {
        if(view != null && mNativeAd != null){
            mNativeAd.registerViewForInteraction(view);
        }
    }
    
    @Override
    public void unbindView(){
        if(mNativeAd != null){
            mNativeAd.unregisterView();
        }
    }
    
    private void destroyPrevious(){
        if(mNativeAd != null){
            LeoLog.v(TAG, "FBNativeAd: new Ad arrived, destory old one");
            mNativeAd.unregisterView();
            mNativeAd.setAdListener(null);
            mNativeAd.destroy();
            mNativeAd = null;
        }
    }
    
    /*
     *  AdListener - facebook listener
     */
    @Override
    public void onAdClicked(Ad arg0) {
        if(mListener != null){
            mListener.onClicked();
        }
    }

    @Override
    public void onAdLoaded(Ad arg0) {
        NativeAd ad = (NativeAd)arg0;
        LeoLog.d(TAG, "FBNativeAd[" + ad.getId() + "]: onAdLoaded() called");
        destroyPrevious();
        mNativeAd = ad;
        if(mListener != null){
            LEONativeAdData data = new LEONativeAdData();
            data.mTitle = mNativeAd.getAdTitle();
            data.mTitleForButton = mNativeAd.getAdCallToAction();
            data.mDescription = mNativeAd.getAdSubtitle();
            data.mIconURL = mNativeAd.getAdIcon().getUrl();
            data.mPreviewURL = mNativeAd.getAdCoverImage().getUrl();
            mListener.onLoadDone(data);
        }
    }

    @Override
    public void onError(Ad arg0, AdError arg1) {
        
        NativeAd ad =  (NativeAd)arg0;
        if(mNativeAd!=null && mNativeAd.getId()!= null
                && ad!=null && ad.getId()!=null
                && (mNativeAd.getId().equalsIgnoreCase(ad.getId()))){
            LeoLog.i(TAG, "stupid facebook call onError on an valid Ad, ignore!");
            return;
        }
        
        LeoLog.i(TAG, "FBNativeAd["+ ad.getId() +"]: onError() ,code: " + arg1.getErrorCode() + "; msg: " + arg1.getErrorMessage());
        if(mListener != null) {
            mListener.onLoadFailed();
        }
        
        // 一次loadAd，facebook偶尔会返回多次onError，所以要干掉
        ad.setAdListener(null);
        ad.destroy();
    }

    @Override
    public LEONativeAdData getData() {
        if(readyToShow()){
            LEONativeAdData data = new LEONativeAdData();
            data.mTitle = mNativeAd.getAdTitle();
            data.mTitleForButton = mNativeAd.getAdCallToAction();
            data.mDescription = mNativeAd.getAdSubtitle();
            data.mIconURL = mNativeAd.getAdIcon().getUrl();
            data.mPreviewURL = mNativeAd.getAdCoverImage().getUrl();
            return data;
        }else{
            return null;
        }
    }

    @Override
    public boolean readyToShow() {
        return (mNativeAd==null)?false:mNativeAd.isAdLoaded();
    }

}
