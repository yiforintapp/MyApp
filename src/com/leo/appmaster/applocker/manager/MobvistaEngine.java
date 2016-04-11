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

    private Map<String, MobvistaAdData> mMobVistaCacheMap;
    private Map<String, MobvistaListener> mMobVistaListeners;
    private Map<String, MobVistaLoadingNative> mMobVistaLoadingNative;

    private Map<String, String> mUnitIdToPlacementIdMap;
	
	private static MobVistaSDK sdk;

	public void setContext(Context context) {
		mAppContext = context;
	}


//    static {
//        Context context = AppMasterApplication.getInstance();
//        try {
//            MobvistaAd.init(context, Constants.MOBVISTA_APPID, Constants.MOBVISTA_APPKEY);
//            MobvistaEngine.getInstance(context).preloadMobvistaAds();
//        } catch (Exception e) {
//            LeoLog.e(TAG, "static block exception: " + e.getMessage());
//            e.printStackTrace();
//        }
//        LeoLog.i(TAG, "static block run done");
//    }

    public static interface MobvistaListener {
        /**
         * 广告请求回调
         *
         * @param code     返回码，如ERR_PARAMS_NULL
         * @param campaigns 请求成功的广告结构体集合，失败为null
         * @param msg      请求失败sdk返回的描述，成功为null
         */
        public void onMobvistaFinished(int code, List<com.mobvista.msdk.out.Campaign> campaigns, String msg);

        /**
         * 广告点击回调
         *
         * @param campaign
		 * @param unitID
         */
        public void onMobvistaClick(com.mobvista.msdk.out.Campaign campaign, String unitID);
    }

    public static synchronized MobvistaEngine getInstance(Context ctx) {
        if (sInstance == null) {
            LeoLog.d(TAG, "MobvistaEngine first touch, init Mobvista");
            initMobvista(ctx);
            sInstance = new MobvistaEngine(ctx);
            // Do not preload all advertise when bootup.
            // Load them only if necessary
            /*
            if (!(AppMasterConfig.IS_FOR_MAINLAND_CHINA
                    && sInstance.mPref.getADMainlandSwticher()==0)) {
                sInstance.preloadMobvistaAds();
            }
            */
        }

        return sInstance;
    }

    private static void initMobvista(Context ctx) {
        //Context context = AppMasterApplication.getInstance();
        try {
            long start = SystemClock.elapsedRealtime();
            // Mob 6.x
			//MobvistaAd.init(context, Constants.MOBVISTA_APPID, Constants.MOBVISTA_APPKEY);
			// Mob 7.x 
			
			sdk = MobVistaSDKFactory.getMobVistaSDK();
			Map<String, String> initInfo = sdk.getMVConfigurationMap(Constants.MOBVISTA_APPID, Constants.MOBVISTA_APPKEY);
			sdk.init(initInfo, /*(AppMasterApplication)context*/ctx);
			
            LeoLog.i(TAG, "initMobvista module done, cost time="
                    + (SystemClock.elapsedRealtime() - start));
        } catch (Exception e) {
            LeoLog.e(TAG, "static block exception: " + e.getMessage());
            e.printStackTrace();
        }
        LeoLog.i(TAG, "static block run done");
    }

    private MobvistaEngine(Context ctx) {
        mAppContext = ctx/*.getApplicationContext()*/;
        mPref = AppMasterPreference.getInstance(mAppContext);
        mMobVistaCacheMap = new HashMap<String, MobvistaAdData>();
        mMobVistaListeners = new HashMap<String, MobvistaListener>();
        mMobVistaLoadingNative = new HashMap<String, MobVistaLoadingNative>();

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
     * 程序启动，获取所有广告位的数据作为缓存
     */
    public void preloadMobvistaAds() {
        LeoLog.i(TAG, "loadMobvistaAds() called done");
        for (String unitId : mUnitIdToPlacementIdMap.keySet()) {
            loadSingleMobAd(unitId);
        }
    }

    private void loadSingleMobAd(String unitId) {
        // 对应的ad正在loading，不重复load
        MobVistaLoadingNative loadingNative = mMobVistaLoadingNative.get(unitId);
        if (loadingNative != null &&
                (SystemClock.elapsedRealtime()-loadingNative.requestTimeMs < 60*1000)) {
            LeoLog.d(TAG, "["+unitId+"]previous loading process ongoing, ignore");
            return;
        }

        long start = SystemClock.elapsedRealtime();
        String placementId = mUnitIdToPlacementIdMap.get(unitId);

        // check placement first
        if (TextUtils.isEmpty(placementId)) {
            LeoLog.i(TAG, "cannot find place mentid of this unitid.");
            MobvistaListener listener = mMobVistaListeners.remove(unitId);
            if (listener != null) {
                listener.onMobvistaFinished(ERR_NOT_FOUND_PLACEMENTID, null, "Mobvista execute throwable.");
            }
            return;
        }


		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE,
					MobVistaConstans.LAYOUT_NATIVE);//广告样式
			properties.put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT,
					placementId);//Facebook id
			properties.put(MobVistaConstans.PROPERTIES_UNIT_ID, unitId); //unit id
			properties.put(MobVistaConstans.PROPERTIES_AD_NUM, 1);// 请求广告条数，不设默认为1

			LeoLog.d(TAG, "create new MobvistaAdNative and start to load");
			MvNativeHandler nativeHandler = new MvNativeHandler(properties, mAppContext);
			
			nativeHandler.addTemplate(new MvNativeHandler.Template(
					MobVistaConstans.TEMPLATE_BIG_IMG, 10));
			
			nativeHandler.setAdListener(new AdListenerImpl(unitId));

			LeoLog.i(TAG, "loadSingleMobAd -> ad[" + unitId + "], cost time = "
					+ (SystemClock.elapsedRealtime() - start));
			mMobVistaLoadingNative.put(unitId, new MobVistaLoadingNative(nativeHandler, SystemClock.elapsedRealtime()));
			nativeHandler.setMustBrowser(true);
			nativeHandler.load();
		} catch (Throwable thr) {
			return;
		}

    }

	/**
	 * 请求多模板的广告
	 * @param unitId
	 * @param listener
	 */
	public void loadMobvistaTemplate(final String unitId, final MobvistaListener listener) {
		if (unitId != null && listener != null) {
			LeoLog.d(TAG, "有广告位 在申请广告 广告位 unit id: " + unitId);
			//通过unitId 申请广告
			Map<String, Object> properties = MvNativeHandler.getNativeProperties(unitId);

			//注入facebook的placementId
			properties.put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, mUnitIdToPlacementIdMap.get(unitId));
			
			properties.put(MobVistaConstans.PROPERTIES_AD_NUM, 1);
			//通过这个配置产生一个广告调用对象
			final MvNativeHandler templateNativeHandler = new MvNativeHandler(properties, mAppContext);
			
			//支持大图模板，请求一条大图广告（只支持1条）
			templateNativeHandler.addTemplate(new MvNativeHandler.Template(
					MobVistaConstans.TEMPLATE_BIG_IMG, 10));
			//支持多图模板，请求3条多图。（支持1-10条）
			templateNativeHandler.addTemplate(new MvNativeHandler.Template(
					MobVistaConstans.TEMPLATE_MULTIPLE_IMG, 10));
			//设置在没有应用市场情况下，必须浏览器来承接响应
			templateNativeHandler.setMustBrowser(true);
			//设置多模板广告请求的监听
			templateNativeHandler.setAdListener(new MvNativeHandler.NativeAdListener() {
				@Override
				public void onAdLoaded(List<Campaign> campaigns, int template) {
					if (campaigns != null && campaigns.size() > 0) {
						LeoLog.i(TAG, "on template AdLoaded[" + unitId + "], campaign's size: " + campaigns.size());
						// 将load成功的 MobvistaAdNative 对象移动到 MobvistaAdData 中
						MobvistaAdData mobvista = new MobvistaAdData();
						mobvista.nativeAd = templateNativeHandler;
						mobvista.campaigns = campaigns;
						mobvista.adTYpe = template;
						mobvista.requestTimeMs = System.currentTimeMillis();
						mMobVistaCacheMap.put(unitId, mobvista);

						if (listener != null) {
							listener.onMobvistaFinished((campaigns == null || campaigns.size() < 1) ? ERR_MOBVISTA_RESULT_NULL : ERR_OK, campaigns, null);
						}
					}
				}

				@Override
				public void onAdLoadError(String s) {
					LeoLog.i(TAG, "onAdLoadError[" + unitId + "], msg: " + s);

					if (listener != null) {
						listener.onMobvistaFinished(ERR_MOBVISTA_FAIL, null, s);
					}

				}

				@Override
				public void onAdClick(Campaign campaign) {
					if (listener != null) {
						listener.onMobvistaClick(campaign, unitId);
						mMobVistaCacheMap.remove(unitId);
					}
					// 点击之后，重新load此位置的广告
					LeoLog.i(TAG, "reload the clicked template");
				}

				@Override
				public void onAdFramesLoaded(List<Frame> frames) {

				}
			});

			try {
				templateNativeHandler.load();
			} catch (Exception e) {
				e.printStackTrace();
				/* 发生了问题，释放多模板广告对象 */
				releaseTemplate(unitId);

				/* 并且告诉广告调用者， 请求广告的时候发生了问题。 */
				if (listener != null) {
					listener.onMobvistaFinished(ERR_MOBVISTA_FAIL, null, "a error was occured on load template ad");
				}
			}
		}
	}
	
	public void preloadTemplateNative(final String unitId) {
		Map<String, Object> proloadMap = new HashMap<String, Object>();
		/* 设置layout类型 */
		proloadMap.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);
		/* 设置facebook 的placement id */
		proloadMap.put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, mUnitIdToPlacementIdMap.get(unitId));
		/* 设置mobvista 的unit id*/
		proloadMap.put(MobVistaConstans.PROPERTIES_UNIT_ID, unitId);

		proloadMap.put(MobVistaConstans.PREIMAGE, true);
//		proloadMap.put(MobVistaConstans.PROPERTIES_AD_NUM, 10);
		
		List<MvNativeHandler.Template> list = new ArrayList<MvNativeHandler.Template>();
		/* 设置一张大图 */
		list.add(new MvNativeHandler.Template(MobVistaConstans.TEMPLATE_BIG_IMG, 1));
		/* 设置多模板广告请求的监听 */
		list.add(new MvNativeHandler.Template(MobVistaConstans.TEMPLATE_MULTIPLE_IMG, 3));
		
		proloadMap.put(MobVistaConstans.NATIVE_INFO, MvNativeHandler.getTemplateString(list));
		
		sdk.preload(proloadMap);
	}

    /**
     * 获取广告内容
     *
     * @param unitId
     * @param listener
	 * @param adType
     */
    public void loadMobvista(String unitId, int adType, MobvistaListener listener) {
        LeoLog.i(TAG, "Attach to Native Ad");
        if (listener == null) return;

        if (TextUtils.isEmpty(unitId)) {
            LeoLog.i(TAG, "unit id is null.");
            listener.onMobvistaFinished(ERR_UNITID_NULL, null, null);
            return;
        }

        mMobVistaListeners.put(unitId, listener);

        // 广告过时则需要重新拉取
        MobvistaAdData mobvista = mMobVistaCacheMap.get(unitId);
        if (isOutOfDate(mobvista)) {
            loadSingleMobAd(unitId);
            LeoLog.i(TAG, "data out ofdate: reload new one.");
            return;
        }

        boolean loading = mMobVistaLoadingNative.get(unitId) != null;
        if (loading) {
            LeoLog.i(TAG, "MobvistaNative is loading");
            return;
        }

        MobvistaAdData adData = mMobVistaCacheMap.get(unitId);
        if (adData != null && adData.campaigns != null && adData.nativeAd != null) {
            listener.onMobvistaFinished(ERR_OK, adData.campaigns, null);
        }
    }

    /*
     * @param activity
     * @return
     * @deprecated 创建appwall广告接口
     */
    /*public MobvistaAdWall createAdWallController(Activity activity) {
        return createAdWallController(activity, null);
    }*/
	
	public void createAdWallController1(Activity activity, String unitId) {
		try {
			Class<?> clazz = Class.forName("com.mobvista.msdk.shell.MVActivity");
			Intent intent = new Intent(activity.getApplicationContext(), clazz);
			
			intent.putExtra(MobVistaConstans.PROPERTIES_UNIT_ID, unitId);
			activity.startActivity(intent);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
	
    /*public MobvistaAdWall createAdWallController(Activity activity, String unitId) {
        if (TextUtils.isEmpty(unitId)) {
            LeoLog.i(TAG, "unit id is null.");
            return null;
        }
        String placementId = mUnitIdToPlacementIdMap.get(unitId);
        if (TextUtils.isEmpty(placementId)) {
            LeoLog.i(TAG, "cannot find place mentid of this unitid.");
            return null;
        }
		
        return MobvistaAd.newAdWallController(activity, unitId, placementId);
    }*/

	public void registerTemplateView(View view, int index, String unitId) {
		if (view != null) {
			MobvistaAdData data = mMobVistaCacheMap.get(unitId);
			if (data != null && data.campaigns != null) {

				Campaign campaign = data.campaigns.get(index);

				MvNativeHandler nativeHander = data.nativeAd;
				if (nativeHander != null) {
					nativeHander.registerView(view, campaign);
				}
			}
			
			
		}
	}
	
    /**
     * 注册广告点击事件
     *
     * @param unitId
     * @param view
     */
    public void registerView(String unitId, View view) {
        registerView(unitId, view, null);
    }

    /**
     * 注册广告点击事件
     *
     * @param unitId
     * @param view
     * @param listener 用新listener替换旧的
     */
    public void registerView(String unitId, View view, MobvistaListener listener) {
        MobvistaAdData adObject = mMobVistaCacheMap.get(unitId);
        if (adObject == null) {
            return;
        }
        MvNativeHandler nativeHandler = adObject.nativeAd;
        if (nativeHandler == null) {
            LeoLog.i(TAG, "havnt register activity before.");
            return;
        }

        // replace the listener
        if (listener != null) {
            mMobVistaListeners.put(unitId, listener);
        }

        LeoLog.i(TAG, "registerView");
		
		nativeHandler.registerView(view, adObject.campaigns.get(0));
		nativeHandler.setTrackingListener(new MvNativeHandler.NativeTrackingListener() {

			@Override
			public void onDownloadProgress(int i) {
				
			}

			@Override
			public boolean onInterceptDefaultLoadingDialog() {
				return false;
			}

			@Override
			public void onShowLoading(Campaign campaign) {

			}

			@Override
			public void onDismissLoading(Campaign campaign) {

			}

			@Override
			public void onStartRedirection(com.mobvista.msdk.out.Campaign campaign, String s) {
				LeoLog.i(TAG, "-->onStartRedirection arg0: " + campaign + " | string: " + s);
			}

			@Override
			public void onFinishRedirection(com.mobvista.msdk.out.Campaign campaign, String s) {
				LeoLog.i(TAG, "-->onFinishRedirection arg0: " + campaign + " | string: " + s);
				AppMasterApplication context = AppMasterApplication.getInstance();
				AppMasterPreference preference = AppMasterPreference.getInstance(context);

				// 记录广告已经被点击过
				preference.setMobvistaClicked();
			}

			@Override
			public void onRedirectionFailed(com.mobvista.msdk.out.Campaign campaign, String s) {
				LeoLog.i(TAG, "-->onRedirectionFailed arg0: " + campaign + " | string: " + s);
			}

			@Override
			public void onDownloadStart(com.mobvista.msdk.out.Campaign campaign) {
				LeoLog.i(TAG, "-->onDownloadStart arg0: " + campaign);
			}

			@Override
			public void onDownloadFinish(com.mobvista.msdk.out.Campaign campaign) {
				LeoLog.i(TAG, "-->onDownloadFinish arg0: " + campaign);
			}
		});
		/*nativeHandler.registerView(view, new AdTrackingListener() {

            @Override
            public void onStartRedirection(Campaign arg0, String arg1) {
                LeoLog.i(TAG, "-->onStartRedirection arg0: " + arg0 + " | string: " + arg1);
            }

            @Override
            public void onRedirectionFailed(Campaign arg0, String arg1) {
                LeoLog.i(TAG, "-->onRedirectionFailed arg0: " + arg0 + " | string: " + arg1);

            }

            @Override
            public void onFinishRedirection(Campaign arg0, String arg1) {
                LeoLog.i(TAG, "-->onFinishRedirection arg0: " + arg0 + " | string: " + arg1);
                AppMasterApplication context = AppMasterApplication.getInstance();
                AppMasterPreference preference = AppMasterPreference.getInstance(context);

                // 记录广告已经被点击过
                preference.setMobvistaClicked();
            }

            @Override
            public void onDownloadStart(Campaign arg0) {
                LeoLog.i(TAG, "-->onDownloadStart arg0: " + arg0);
            }

            @Override
            public void onDownloadProgress(Campaign arg0, int arg1) {
                LeoLog.i(TAG, "-->onDownloadProgress arg0: " + arg0 + " | progress: " + arg1);
            }

            @Override
            public void onDownloadFinish(Campaign arg0) {
                LeoLog.i(TAG, "-->onDownloadFinish arg0: " + arg0);
            }

            @Override
            public void onDownloadError(String arg0) {
                LeoLog.i(TAG, "-->onDownloadError arg0: " + arg0);
            }

            @Override
            public void onDismissLoading(Campaign arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean onInterceptDefaultLoadingDialog() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onShowLoading(Campaign arg0) {
                // TODO Auto-generated method stub

            }
        });*/
    }

    /**
     * 释放广告资源
     */
    public void release(String unitId, View view) {
        LeoLog.i(TAG, "release [" + unitId + "]");
        try {
            doReleaseInner(unitId);

			removeMobAdData(unitId, view);
			// 重新拉取广告
			if (shouldReloadAd(unitId)) {
				LeoLog.d(TAG, "reload ad[" + unitId + "] when release");
                loadSingleMobAd(unitId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LeoLog.e("TAG", "can not release ad");
        }

    }

    private boolean shouldReloadAd(String unitId) {
        MobvistaAdData adData = mMobVistaCacheMap.get(unitId);

        if (adData == null) return true;
        long lastRequestTime = adData.requestTimeMs;
        long now = System.currentTimeMillis();
        LeoLog.d(TAG, "["+unitId+"]lastRequest:" + lastRequestTime + "; now:" + now
                +"; period:" + (now-lastRequestTime));
        if (now-lastRequestTime > mPref.getADFetchInterval()*MILLIS_IN_MINUTE) {
            return true;
        }

        return false;
    }

    private void doReleaseInner(String unitId) {
        // 因为目前loading与UI已无关系，release的时候无需清除loading的广告
//        MobvistaAdNative adNative = mMobVistaLoadingNative.remove(unitId);
//        if (adNative != null) {
//            try {
//                adNative.release();
//            } catch (Throwable e) {
//            }
//        }

        mMobVistaListeners.remove(unitId);
//        removeMobAdData(unitId);
    }

    public void removeMobAdData(String unitId, View view) {
        MobvistaAdData adData = mMobVistaCacheMap.remove(unitId);
        if (adData != null) {
            MvNativeHandler nativeHandler = adData.nativeAd;
            if (nativeHandler != null) {
				nativeHandler.release();
				nativeHandler.unregisterView(view, adData.campaigns.get(0));
            }
        }
    }

    private static boolean isOutOfDate(MobvistaAdData mobvista) {
        if (mobvista == null) return true;

        long current = System.currentTimeMillis();
        return current - mobvista.requestTimeMs > AD_TIMEOUT;
    }
	
	private class AdListenerImpl implements MvNativeHandler.NativeAdListener {

		private String mUnitId;

		public AdListenerImpl(String unitId) {
			this.mUnitId = unitId;
		}



		@Override
		public void onAdFramesLoaded(List<Frame> frames) {

		}

		@Override
		public void onAdLoaded(List<com.mobvista.msdk.out.Campaign> campaigns, int template) {
			LeoLog.i(TAG, "onAdLoaded [" + mUnitId + "]: campaigns size : " + campaigns.size());

			MobVistaLoadingNative loadingNative = mMobVistaLoadingNative.remove(mUnitId);

			if (loadingNative == null) {
                /* AM-4016: 这是一次超时的load操作，直接抛弃 */
				return;
			}

			// 将load成功的 MobvistaAdNative 对象移动到 MobvistaAdData 中
			MobvistaAdData mobvista = new MobvistaAdData();
			mobvista.nativeAd = loadingNative.nativeAd;
			mobvista.campaigns = campaigns;
			mobvista.requestTimeMs = System.currentTimeMillis();
			mMobVistaCacheMap.put(mUnitId, mobvista);

			MobvistaListener listener = mMobVistaListeners.get(mUnitId);

			if (listener != null) {
				listener.onMobvistaFinished((campaigns == null || campaigns.size()  < 1) ? ERR_MOBVISTA_RESULT_NULL : ERR_OK, campaigns, null);
			}
		}

		@Override
		public void onAdLoadError(String s) {
			LeoLog.i(TAG, "onAdLoadError[" + mUnitId + "], msg: " + s);
			MobvistaListener listener = mMobVistaListeners.get(mUnitId);

			if (listener != null) {
				listener.onMobvistaFinished(ERR_MOBVISTA_FAIL, null, s);
			}

			mMobVistaLoadingNative.remove(mUnitId);
		}

		@Override
		public void onAdClick(com.mobvista.msdk.out.Campaign campaign) {
			com.mobvista.msdk.out.Campaign data = null;
			MobvistaAdData m = mMobVistaCacheMap.remove(mUnitId);
			if (m != null && m.campaigns != null && m.campaigns.size() > 0) {
				if (m.campaigns.indexOf(campaign) >= m.campaigns.size() 
						|| m.campaigns.indexOf(campaign) == -1) {
					return;
				}
				
				data = m.campaigns.get(m.campaigns.indexOf(campaign));
			}

			MobvistaListener listener = mMobVistaListeners.get(mUnitId);
			if (listener != null) {
				listener.onMobvistaClick(campaign == null ? data : campaign, mUnitId);
			}
			// 点击之后，重新load此位置的广告
			LeoLog.i(TAG, "reload the clicked Ad");
			//Mobvista 7.x sdk 已经修复持有这个activity 的内存泄漏的问题，所以无需要再每次点击后进行对广告对象的释放操作。
			if(m != null && m.nativeAd != null) {
				try {
					//MobvistaAd.release();
					//MobVistaSDK.
					mMobVistaCacheMap.clear();
					
				} catch (Exception e) {
				}
			}
			loadSingleMobAd(mUnitId);
			if(!Constants.UNIT_ID_59.equals(mUnitId)) {
				loadSingleMobAd(Constants.UNIT_ID_59);
			}
		}
	}


    private static class MobvistaAdData {
		public int adTYpe;
        public List<com.mobvista.msdk.out.Campaign> campaigns;
        public MvNativeHandler nativeAd;
        public long requestTimeMs;
    }

    private static class MobVistaLoadingNative {
        public long requestTimeMs;
        public MvNativeHandler nativeAd;
        public MobVistaLoadingNative (MvNativeHandler nativeAd, long timestamp) {
            this.requestTimeMs = timestamp;
            this.nativeAd = nativeAd;
        }
    }

	public boolean isADCacheEmpty() {
		return mMobVistaCacheMap == null ? true : mMobVistaCacheMap.isEmpty();
	}


	public void proloadTemplate(String unitId) {
		/* 使用sdk提供的预加载的功能，管理多模板广告的前期加载 */
		if (!TextUtils.isEmpty(unitId)) {
			preloadTemplateNative(unitId);
		}
	}
	
	public void releaseTemplate(String unitId) {
		MobvistaAdData data = mMobVistaCacheMap.get(unitId);
		if (data != null) {
			MvNativeHandler nativeHandler = data.nativeAd;
			
			if (nativeHandler != null) {
				nativeHandler.release();
			}
			mMobVistaCacheMap.remove(unitId);
		}
	}


}
