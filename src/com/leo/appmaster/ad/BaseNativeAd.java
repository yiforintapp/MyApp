package com.leo.appmaster.ad;

import android.view.View;

public abstract class BaseNativeAd {

    protected LEOInnerNativeAdListener mListener;

    protected interface LEOInnerNativeAdListener {
        public void onLoadDone(final LEONativeAdData adData);
        public void onLoadFailed();
        public void onClicked();
    }
    
    public abstract void loadAd();
    
    protected void setAdListener(LEOInnerNativeAdListener listener){
        mListener = listener;
    }

    protected abstract void bindToView(View view);

    protected abstract void unbindView();
    
    protected abstract void release();

    protected abstract LEONativeAdData getData();

    protected abstract boolean readyToShow();
}
