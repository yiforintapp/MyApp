/*
 * 
 * 　                      App ID  Slot ID      placement id    
Iswipe-主页-原生大图          1195    1437    892331967487513_899598356760874 
Iswipe-设置-原生大图          1195    1438    892331967487513_899607563426620 
Iswipe-消息-原生banner       1195    1436    892331967487513_899569520097091
 * 
 * */

package com.leo.appmaster.ad;

import android.content.Context;

import com.leo.appmaster.Constants;
import com.leo.appmaster.utils.LeoLog;


public class LEOAdManager {
	private final static String TAG = "LEOAdManager";

	public enum LeoNatvieAdPosition {
        AD_FOR_LOCKSCREEN;
	}

	public interface LeoAdListener {

		public void onAdLoaded(LEONativeAdData adData);

		public void onAdLoadFailed();
		public void onAdClickded();

	}

	private static LEOAdManager sInstance;
	private Context mContext;

	private LEONativeAd adLock;
    /*LOCK pic 1*/
	public final static String UNIT_ID_LOCK = Constants.UNIT_ID_59;
	/* LOCK pic 2 */
	public final static String UNIT_ID_LOCK_1 = Constants.UNIT_ID_178;
	/* LOCK PIC 3*/
	public final static String UNIT_ID_LOCK_2 = Constants.UNIT_ID_179;
	

	public final static String PLACEMENTID_LOCK = Constants.PLACEMENT_ID_59;
	public final static String PLACEMENTID_LOCK_1 = Constants.PLACEMENT_ID_178;
	public final static String PLACEMENTID_LOCK_2 = Constants.PLACEMENT_ID_179;
	//Leo Max 对应广告位id
	public static final String UNIT_ID_001 = "201512230001";

	public static final String LEOMAX_APPID = "201409010001";

    private LEOAdManager(Context ctx) {
        mContext = ctx;
    }

    public static LEOAdManager getInstance(Context ctx) {
        if (sInstance == null) {
            sInstance = new LEOAdManager(ctx);
        }
        return sInstance;
    }

    /**
     * 根据广告位获取原生广告实例
     * 
     * @param position - 枚举类型的广告位
     * @return LEONativeAd类型的原生广告
     **/
    public LEONativeAd requestNativeAD(LeoNatvieAdPosition position) {
        LeoLog.d(TAG, "requestNativeAD called");
        return getNatvieAdByPosition(position);
    }

    private LEONativeAd getNatvieAdByPosition(LeoNatvieAdPosition position) {
        LEONativeAd nativeAD = null;
        switch (position) {
            case AD_FOR_LOCKSCREEN:
                nativeAD = adLock;
                break;
        }
        return nativeAD;
    }

}
