package com.leo.appmaster.applocker.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.utils.LeoLog;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.Campaign;
import com.mobvista.msdk.out.Frame;
import com.mobvista.msdk.out.MobVistaSDKFactory;
import com.mobvista.msdk.out.MvNativeHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	
	private static MobVistaSDK sSdk;

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


    private Map<String, String> mUnitIdToPlacementIdMap;
	

	public interface MobvistaListener {
        /**
         * 广告请求回调
         *
         * @param code     返回码，如ERR_PARAMS_NULL
         * @param campaigns 请求成功的广告结构体集合，失败为null
         * @param msg      请求失败sdk返回的描述，成功为null
         */
        public void onMobvistaFinished(int code, List<Campaign> campaigns, String msg);

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
            sInstance = new MobvistaEngine(ctx.getApplicationContext());
        }

        return sInstance;
    }

    private static void initMobvista() {
		Context context = AppMasterApplication.getInstance();
		try {
			long start = SystemClock.elapsedRealtime();
			
			sSdk = MobVistaSDKFactory.getMobVistaSDK();
			Map<String, String> initInfo = sSdk.getMVConfigurationMap(Constants.MOBVISTA_APPID, Constants.MOBVISTA_APPKEY);
			sSdk.init(initInfo, context);
			
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
	 * 获取广告内容
	 *
	 * @param unitId
	 * @param listener
	 */
	public void loadMobvista(String unitId, MvNativeHandler mvNativeHandler, MobvistaListener listener) {
		LeoLog.i(TAG, "Attach to Native Ad");
		if (listener == null) return;

		if (TextUtils.isEmpty(unitId)) {
			LeoLog.i(TAG, "unit id is null.");
			listener.onMobvistaFinished(ERR_UNITID_NULL, null, null);
			return;
		}


		loadSingleMobAd(unitId, listener, mvNativeHandler);
	}
	

    private void loadSingleMobAd(String unitId, MobvistaListener listener, MvNativeHandler mvNativeHandler) {
		
		try {
			long start = SystemClock.elapsedRealtime();


			mvNativeHandler.addTemplate(new MvNativeHandler.Template(MobVistaConstans.TEMPLATE_BIG_IMG, 1));
			AdListenerImpl adListener = new AdListenerImpl(unitId, listener);
			mvNativeHandler.setAdListener(adListener);

			LeoLog.i(TAG, "loadSingleMobAd -> ad[" + unitId + "], cost time = " + (SystemClock.elapsedRealtime() - start));
			mvNativeHandler.setMustBrowser(true);
			LeoLog.i(TAG, "load ad | type = native | unitid = ["+ unitId +"]");
			mvNativeHandler.load();
		} catch (Throwable thr) {
			return;
		}

    }
	

	/**
	 * 程序启动，获取所有广告位的数据作为缓存
	 */
	public void preloadMobvistaAds(String unitId) {
		LeoLog.i(TAG, " preload  native ad | unitid = ["+ unitId +"]");
		Map<String, Object> preloadMap = new HashMap<String, Object>();
		preloadMap.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);
		preloadMap.put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, mUnitIdToPlacementIdMap.get(unitId));
		preloadMap.put(MobVistaConstans.PROPERTIES_UNIT_ID, unitId);

		preloadMap.put(MobVistaConstans.PROPERTIES_AD_NUM, 1);
		List<MvNativeHandler.Template> list = new ArrayList<MvNativeHandler.Template>();
		list.add(new MvNativeHandler.Template(MobVistaConstans.TEMPLATE_BIG_IMG, 3));
		preloadMap.put(MobVistaConstans.NATIVE_INFO, MvNativeHandler.getTemplateString(list));
		sSdk.preload(preloadMap);
	}
	
	/**
	 * 请求多模板的广告
	 * @param unitId
	 */
	public void loadMobvistaTemplate(final String unitId, final MobvistaListener listener) {
		if (unitId != null && listener != null) {
			//通过unitId 申请广告
			Map<String, Object> properties = MvNativeHandler.getNativeProperties(unitId);
			//注入facebook的placementId
			properties.put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, mUnitIdToPlacementIdMap.get(unitId));
			properties.put(MobVistaConstans.PROPERTIES_AD_NUM, 1);
			//通过这个配置产生一个广告调用对象
			final MvNativeHandler templateNativeHandler = new MvNativeHandler(properties, mAppContext);
			
			//支持大图模板，请求一条大图广告（只支持1条）
			/*templateNativeHandler.addTemplate(new MvNativeHandler.Template(
					MobVistaConstans.TEMPLATE_BIG_IMG, 10));*/
			//支持多图模板，请求3条多图。（支持1-10条）
			templateNativeHandler.addTemplate(new MvNativeHandler.Template(
					MobVistaConstans.TEMPLATE_MULTIPLE_IMG, 3));
			//设置在没有应用市场情况下，必须浏览器来承接响应
			templateNativeHandler.setMustBrowser(true);
			//设置多模板广告请求的监听

			AdTemplateListener adTemplateListener = new AdTemplateListener(unitId, listener, templateNativeHandler);
			templateNativeHandler.setAdListener(adTemplateListener);

			try {
				LeoLog.i(TAG, "load ad | type = template | unitid = ["+ unitId +"]");
				templateNativeHandler.load();
			} catch (Exception e) {
				e.printStackTrace();

				// 并且告诉广告调用者， 请求广告的时候发生了问题。 
				if (listener != null) {
					listener.onMobvistaFinished(ERR_MOBVISTA_FAIL, null,  "a error was occured on load template ad");
				}
				templateNativeHandler.release();
			}
		}
	}
	
	public void preloadTemplateNative(final String unitId) {
		LeoLog.i(TAG, " preload  Template ad | unitid = ["+ unitId +"]");
		Map<String, Object> proloadMap = new HashMap<String, Object>();
		 //设置layout类型 
		proloadMap.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);
		 //设置facebook 的placement id 
		proloadMap.put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, mUnitIdToPlacementIdMap.get(unitId));
		// 设置mobvista 的unit id
		proloadMap.put(MobVistaConstans.PROPERTIES_UNIT_ID, unitId);

		proloadMap.put(MobVistaConstans.PROPERTIES_AD_NUM, 1);
		
		List<MvNativeHandler.Template> list = new ArrayList<MvNativeHandler.Template>();
		// 设置一张大图 
		/*list.add(new MvNativeHandler.Template(MobVistaConstans.TEMPLATE_BIG_IMG, 1));*/
		// 设置多模板广告请求的监听 
		list.add(new MvNativeHandler.Template(MobVistaConstans.TEMPLATE_MULTIPLE_IMG, 12));
		
		proloadMap.put(MobVistaConstans.NATIVE_INFO, MvNativeHandler.getTemplateString(list));
		
		sSdk.preload(proloadMap);
	}
	
	public MvNativeHandler getTemplateHandler(String unitId) {
		//通过unitId 申请广告
		Map<String, Object> properties = MvNativeHandler.getNativeProperties(unitId);
		//注入facebook的placementId
		properties.put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, mUnitIdToPlacementIdMap.get(unitId));
		properties.put(MobVistaConstans.PROPERTIES_AD_NUM, 1);
		//通过这个配置产生一个广告调用对象
		return new MvNativeHandler(properties, mAppContext);
	}

	
	public MvNativeHandler getMvNativeHandler(String unitId) {
		Map<String, Object> properties = new HashMap<String, Object>();

		properties.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);//广告样式
		properties.put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, mUnitIdToPlacementIdMap.get(unitId));//Facebook id
		properties.put(MobVistaConstans.PROPERTIES_UNIT_ID, unitId); //unit id
		properties.put(MobVistaConstans.PROPERTIES_AD_NUM, 1);// 请求广告条数，不设默认为1
		
		return new MvNativeHandler(properties, mAppContext);
	}
    

	public void createAdWallController(Activity activity, String unitId) {
		try {
			Class<?> clazz = Class.forName("com.mobvista.msdk.shell.MVActivity");
			Intent intent = new Intent(activity.getApplicationContext(), clazz);
			
			intent.putExtra(MobVistaConstans.PROPERTIES_UNIT_ID, unitId);
			activity.startActivity(intent);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
	

	public void registerTemplateView(View view, Campaign campaign, String unitId) {
		if (view != null && campaign != null) {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);//广告样式
			properties.put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, mUnitIdToPlacementIdMap.get(unitId));//Facebook id
			properties.put(MobVistaConstans.PROPERTIES_UNIT_ID, unitId); //unit id

			MvNativeHandler handler = new MvNativeHandler(properties, mAppContext);
			handler.registerView(view, campaign);
			
		}
	}
	
    /**
     * 注册广告点击事件
     *
     * @param unitId
     * @param view
     */
    public void registerView(String unitId, View view, Campaign campaign) {
		if (!TextUtils.isEmpty(unitId) && view != null && campaign != null) {
			registerView(unitId, view, null, campaign);
		}
    }

    /**
     * 注册广告点击事件
     *
     * @param unitId
     * @param view
     * @param listener 用新listener替换旧的
     */
    public void registerView(String unitId, View view, MobvistaListener listener, Campaign campaign) {


        LeoLog.i(TAG, "registerView");
		
		Map<String, Object> properties = new HashMap<String, Object>();

		properties.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);//广告样式
		properties.put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, mUnitIdToPlacementIdMap.get(unitId));//Facebook id
		properties.put(MobVistaConstans.PROPERTIES_UNIT_ID, unitId); //unit id
		
		MvNativeHandler handler = new MvNativeHandler(properties, mAppContext);

		try {
			handler.registerView(view, campaign);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LeoLog.e("llb", unitId + "<----registerView on MobvistaEngine");
    }

    /**
     * 释放广告资源
     */
    public void release(String unitId, View view, Campaign campaign, MvNativeHandler handler) {
        LeoLog.i(TAG, "release [" + unitId + "]");
        try {
            
			if (handler != null && campaign != null) {
				
				handler.setAdListener(null);
				if (view != null) {
					handler.unregisterView(view, campaign);
				}
				handler.release();
				handler = null;
				
				
			}
        } catch (Exception e) {
            e.printStackTrace();
            LeoLog.e("TAG", "can not release ad");
        }

    }


	private class AdListenerImpl implements MvNativeHandler.NativeAdListener {
		private String mUnitId;
		private MobvistaListener l;
		public AdListenerImpl(String unitId, MobvistaListener listener) {
			this.mUnitId = unitId;
			this.l = listener;
		}


		@Override
		public void onAdLoaded(List<com.mobvista.msdk.out.Campaign> campaigns, int i) {
			
			if (l != null) {
				l.onMobvistaFinished((campaigns != null && campaigns.size() > 0 && campaigns.get(0) != null) 
						? ERR_OK: ERR_MOBVISTA_RESULT_NULL, campaigns, null);
				LeoLog.i(TAG, " onAdLoaded  | type = native | unitid = ["+ mUnitId +"]");
			}
			preloadMobvistaAds(mUnitId);
		}

		@Override
		public void onAdLoadError(String s) {
			LeoLog.i(TAG, "onAdLoadError[" + mUnitId + "], msg: " + s);
			if (l != null) {
				l.onMobvistaFinished(ERR_MOBVISTA_FAIL, null,  s);
			}
		}


		@Override
		public void onAdFramesLoaded(List<Frame> frames) {

		}

		@Override
		public void onAdClick(Campaign campaign) {
			if (l != null) {
				l.onMobvistaClick(campaign, mUnitId);
			}
		}


	}

	private class AdTemplateListener implements MvNativeHandler.NativeAdListener {
		private String unitId;
		private MobvistaListener l;
		private MvNativeHandler h;
		public AdTemplateListener(String unitId, MobvistaListener listener, MvNativeHandler handler) {
			this.unitId = unitId;
			this.l = listener;
			this.h = handler;
		}


		@Override
		public void onAdLoaded(List<Campaign> campaigns, int template) {
			if (campaigns != null && campaigns.size() > 0) {
				LeoLog.i(TAG, " onAdLoaded  | type = template | unitid = ["+ unitId +"]");
				// 将load成功的 MobvistaAdNative 对象移动到 MobvistaAdData 中

				if (l != null) {
					l.onMobvistaFinished((campaigns != null || campaigns.size() > 2)
							? ERR_OK : ERR_MOBVISTA_RESULT_NULL, campaigns,   null);
				}
			}
			preloadTemplateNative(unitId);
		}

		@Override
		public void onAdLoadError(String s) {
			LeoLog.i(TAG, "onAdLoadError[" + unitId + "], msg: " + s);

			if (l != null) {
				l.onMobvistaFinished(ERR_MOBVISTA_FAIL, null,  s);
			}

		}

		@Override
		public void onAdClick(Campaign campaign) {
			if (l != null) {
				l.onMobvistaClick(campaign, unitId);
			}
			// 点击之后，重新load此位置的广告
			LeoLog.i(TAG, "reload the clicked template");
		}

		@Override
		public void onAdFramesLoaded(List<Frame> frames) {

		}


	}

	@Deprecated
	public boolean isADCacheEmpty() {
		return false;
	}

	public void preloadAppWall(String unitId) {
		Map<String,Object> preloadMap = new HashMap<String,Object>();
		preloadMap.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_APPWALL);
		preloadMap.put(MobVistaConstans.PROPERTIES_UNIT_ID, unitId);
		sSdk.preload(preloadMap);
	}
}
