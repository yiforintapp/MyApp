/**
 * 
 * 
 * tag_20151016-17:35: 
 * 修改广告拉取策略：当进入页面后，优先拉取新广告。
 * 仅当拉取失败之后，再判断是否有缓存数据，有的话显示缓存好的广告。
 * **/

package com.leo.appmaster.ad;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.leo.appmaster.utils.LeoLog;
import com.leo.leoadlib.AdListener;
import com.leo.leoadlib.LeoAdNative;
import com.leo.leoadlib.model.Campaign;

import java.util.HashMap;


public class LEONativeAd {
    
    /**
     * Major Advertise, always load it first.
     * */
    private BaseNativeAd mMajorAd;
    /**
     * Minor Advertise, load it when failed to load major one.
     */
    private BaseNativeAd mMinorAd;
	/**
	 * Minor Advertise, load it when failed to load major one.
	 */
	private LeoAdNative mMinorAd2;
    /**
     * Advertise ready to show.
     **/
    private BaseNativeAd mAvailableAd;
    
    private LEONativeAdListener mLeoListener;
    
    private boolean isBindToView = false;
    private boolean isLoading = false;
	/*由max sdk返回的campaign數據*/
	private Campaign mCampaign;
    
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
    
	public LEONativeAd(BaseNativeAd majorAd, LeoAdNative minorAd) {
		mMajorAd = majorAd;
		mMinorAd2 = minorAd;
		
		mMajorAd.setAdListener(new BaseNativeAd.LEOInnerNativeAdListener() {

			@Override
			public void onLoadFailed() {
				loadMinorAd();
			}

			@Override
			public void onLoadDone(LEONativeAdData adData) {
				LeoLog.e(TAG, "Facebook ad request success");
				
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
		if (mMinorAd != null) {

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
		} else if (mMinorAd2 != null) {
			HashMap<String, String> map = new HashMap<String, String>();
			mMinorAd2.loadAd(map, new AdListener() {
				@Override
				public void onAdLoaded(Campaign campaign) {
					isLoading = false;
//					mAvailableAd = mMinorAd2;
					mCampaign = campaign;
					mHandler.sendEmptyMessage(MSG_NATIVE_AD_LOAD_OK);
				}

				@Override
				public void onAdLoadError(int code, String paramString) {
					mHandler.sendEmptyMessage(MSG_NATIVE_AD_LOAD_FAIL);
				}

				@Override
				public void onAdClick(Campaign campaign) {
					mHandler.sendEmptyMessage(MSG_NATIVE_AD_CLICK);
				}
			});
		}
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
        if(mAvailableAd == null && mMinorAd2 == null){
            return false;
        } else if (mAvailableAd != null /*&& mMinorAd2 == null*/) {
			return mAvailableAd.readyToShow();
		} else if (mAvailableAd == null && mMinorAd2 != null) {
            return !isLoading;
        } 
		return false;
    }

    protected final LEONativeAdData getAdData(){
        if(isAdReady()){
			if (mAvailableAd != null) {
				return mAvailableAd.getData();
			} else if (mMinorAd2 != null) {
				return getLEONativeAdDataByAdapter(mCampaign);
			} else {
				return null;
			}
        }
        return null;
    }
    
    /**
     * Bind a View with the advertise.
     * Application should call this after showing all elements.
     * Advertising SDK will make this view interactive.
     * */
    public void bindAdWithView(View view){
        if(isAdReady()) {
			if (mAvailableAd != null) {
				mAvailableAd.bindToView(view);
			} else if (mMinorAd2 != null) {
				mMinorAd2.registerView(view);
			}
            isBindToView = true;
        }
    }
    
    /**
     * Call this in Activity's onDestroy() call.
     */
    public void detachWithView(){
        isBindToView = false;
        if(mAvailableAd != null){
            mAvailableAd.unbindView();
        }
    }

    /**
     * Call this in Activity's onDestroy() call.
     */
    public void release() {

    }
	
	private LEONativeAdData getLEONativeAdDataByAdapter(Campaign campaign) {
		LEONativeAdData data = null;
		
		if (campaign != null) {
			data = new LEONativeAdData();

			data.mTitle = campaign.getTitle();// mNativeAd.getAdTitle();
			data.mTitleForButton = campaign.getCta();//mNativeAd.getAdCallToAction();
			data.mDescription = campaign.getDescription();//mNativeAd.getAdSubtitle();
			data.mIconURL = campaign.getIconUrl();//mNativeAd.getAdIcon().getUrl();
			data.mPreviewURL = campaign.getPreviewUrl();//mNativeAd.getAdCoverImage().getUrl();

			return data;
		}
		
		
		return data;
	}
    
}
