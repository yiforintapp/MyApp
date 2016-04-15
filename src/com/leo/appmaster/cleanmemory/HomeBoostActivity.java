
package com.leo.appmaster.cleanmemory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.ad.ADEngineWrapper;
import com.leo.appmaster.ad.WrappedCampaign;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.TextFormater;
import com.leo.appmaster.utils.Utilities;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageSize;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.mobvista.msdk.out.Campaign;
import com.mobvista.msdk.out.MvNativeHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class HomeBoostActivity extends Activity {
	
	private static final String TAG = "HomeBoostActivity_[DEBUG]";
    private ImageView mIvRocket, mIvCloud;
    private View mStatusBar;
    //private MobvistaEngine mAdEngine;
	private ADEngineWrapper mAdEngine;
    private boolean isClean = false;
    private ProcessCleaner mCleaner;
    private long mLastUsedMem;
    private long mCleanMem;
    private boolean isCleanFinish = false;
    private int mScreenH;
    private int mCountDownNum = 5;
    private boolean mIsADLoaded = false;
    private FrameLayout mRlResultWithAD;
    private CountDownTimer mCdt;

    private View mParent;
    private ImageView mFire;
    private ImageView mLine;
    private ImageView mSpeedAdLight;
    private AnimatorSet mRocketAnim;
    private AnimatorSet mAdAnim;
    private Random mRandom = new Random();
	
	private List<Campaign> mCampaignList;
	private MvNativeHandler mvNativeHandler;
	
	private List<View> mAdViews = new ArrayList<View>();

	private static int mAdSource = ADEngineWrapper.SOURCE_MOB; // 默认值
	

	DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_boost_activity);
        
        if(getIntent().getBooleanExtra("from_quickhelper", false)){
            SDKWrapper.addEvent(AppMasterApplication.getInstance(), SDKWrapper.P1,
                    "assistant", "accelerate_cnts");
        }
        LeoLog.e("HomeBoostActivity",
                ""+ Utilities.getScreenSize(this)[0] +":" + Utilities.getScreenSize(this)[1]);

        initUI();
        handleIntent();
        overridePendingTransition(0, 0);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "boost", "launcher");
        
        AppMasterPreference amp = AppMasterPreference.getInstance(this);       
        if (NetWorkUtil.isNetworkAvailable(getApplicationContext()) && amp.getADChanceAfterAccelerating() == 1) {
            LeoLog.e("poha", "to load");
			mAdSource = AppMasterPreference.getInstance(this).getAccelerationAdConfig();
            loadAD();
        }
    }

    private void loadAD() {
		
		mAdEngine = ADEngineWrapper.getInstance(this);
		if (mAdSource == ADEngineWrapper.SOURCE_MOB) {
			mvNativeHandler = mAdEngine.getMvNativeHandler(Constants.UNIT_ID_62, ADEngineWrapper.AD_TYPE_TEMPLATE);	
		}
		 
		mAdEngine.loadAd(mAdSource, Constants.UNIT_ID_62, ADEngineWrapper.AD_TYPE_TEMPLATE, mvNativeHandler, new ADEngineWrapper.WrappedAdListener() {

			/**
			 * 广告请求回调
			 *
			 * @param code     返回码，如ERR_PARAMS_NULL
			 * @param campaign 请求成功的广告结构体，失败为null
			 * @param msg      请求失败sdk返回的描述，成功为null
			 * @param obj
			 */
			@Override
			public void onWrappedAdLoadFinished(int code, WrappedCampaign campaign,  String msg, Object obj) {

			}

			@Override
			public void onWrappedAdLoadFinished(int code, List<WrappedCampaign> campaignList,  String msg, Object obj, Object... flag) {
				if (code == MobvistaEngine.ERR_OK  && campaignList != null) {
					sAdImageListener = new AdPreviewLoaderListener(HomeBoostActivity.this, campaignList);

					if (obj != null && obj instanceof List) {
						mCampaignList = (List)obj;
					}
					
					//判断是否一个图片还算多模板
					String imageUrl = null;
					boolean aPic = campaignList != null && campaignList.size() == 1;

					int w = (aPic) ? 262 : 48;
					int h = (aPic) ? 130 : 48;
					int index = 0;

					if (aPic) {

						RelativeLayout adZone = (RelativeLayout) findViewById(R.id.rl_ad_bg);
						ViewGroup.LayoutParams params = adZone.getLayoutParams();

						params.width = (int) HomeBoostActivity.this.getResources().getDimension(R.dimen.ad_content_width);
						params.height = (int) HomeBoostActivity.this.getResources().getDimension(R.dimen.ad_content_height);

						adZone.setLayoutParams(params);

						findViewById(R.id.template_zone).setVisibility(View.GONE);

						findViewById(R.id.ad_info_zone).setVisibility(View.VISIBLE);
						TextView appname = (TextView) findViewById(R.id.tv_ad_appname);
						if(appname != null) {
							appname.setText(campaignList.get(0).getAppName());
						}

						TextView appdesc = (TextView) findViewById(R.id.tv_ad_appdesc);
						if(appdesc != null) {
							appdesc.setText(campaignList.get(0).getDescription());
						}

						Button call = (Button) findViewById(R.id.btn_ad_appcall);
						if(call != null) {
							call.setText(campaignList.get(0).getAdCall());
						}
						imageUrl = campaignList.get(0).getImageUrl();
						if (imageUrl != null && !"".equals(imageUrl)) {
							ImageLoader.getInstance().loadImage(imageUrl,
									new ImageSize(DipPixelUtil.dip2px(HomeBoostActivity.this, w),
											DipPixelUtil.dip2px(HomeBoostActivity.this, h)), options, sAdImageListener);
						}
					} else {

						TextView textView1, textView2, textView3;
						Button btn1, btn2, btn3;


						textView1 = (TextView) findViewById(R.id.ad_title1);
						textView2 = (TextView) findViewById(R.id.ad_title2);
						textView3 = (TextView) findViewById(R.id.ad_title3);

						btn1 = (Button) findViewById(R.id.button1);
						btn2 = (Button) findViewById(R.id.button2);
						btn3 = (Button) findViewById(R.id.button3);

						btn1.setClickable(false);
						btn2.setClickable(false);
						btn3.setClickable(false);

						for (WrappedCampaign campaign : campaignList) {

							switch (index) {
								case 0:
									textView1.setText(campaign.getAppName());
									btn1.setText(campaign.getAdCall());

									break;
								case 1:
									textView2.setText(campaign.getAppName());
									btn2.setText(campaign.getAdCall());

									break;
								case 2:
									textView3.setText(campaign.getAppName());
									btn3.setText(campaign.getAdCall());

									break;
							}
							index++;

							imageUrl = campaign.getIconUrl();
							if (imageUrl != null && !"".equals(imageUrl)) {
								ImageLoader.getInstance().loadImage(imageUrl,
										new ImageSize(DipPixelUtil.dip2px(HomeBoostActivity.this, w),
												DipPixelUtil.dip2px(HomeBoostActivity.this, h)), options, sAdImageListener);
							}
						}
						

					}

					
					

				}
			}
			/**
			 * 广告点击回调
			 *
			 * @param campaign
			 * @param unitID
			 */
			@Override
			public void onWrappedAdClick(WrappedCampaign campaign, String unitID) {
				HomeBoostActivity.this.finish();
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("unitId", unitID);
				SDKWrapper.addEvent(HomeBoostActivity.this.getApplicationContext(), "max_ad", SDKWrapper.P1, "ad_click", "click", mAdSource, map);
				SDKWrapper.addEvent(HomeBoostActivity.this, SDKWrapper.P1, "ad_cli",
						"adv_cnts_bst");
			}
		});
    }

    /**
     * 新需求：当广告大图加载完成之后再展示广告
     */
    public static class AdPreviewLoaderListener implements ImageLoadingListener {
        WeakReference<HomeBoostActivity> mActivity;
		List<WrappedCampaign> mCampaign;
		
		HashMap<String, String> map  = new HashMap<String, String>();

        public AdPreviewLoaderListener (HomeBoostActivity activity, final List<WrappedCampaign> campaigns) {
            mActivity = new WeakReference<HomeBoostActivity>(activity);
            mCampaign = campaigns;
			
			map.put("unitId", Constants.UNIT_ID_62);
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
			SDKWrapper.addEvent(AppMasterApplication.getInstance().getApplicationContext(), "max_ad", SDKWrapper.P1, "ad_load_image", "ready_to", mAdSource, map);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			SDKWrapper.addEvent(AppMasterApplication.getInstance().getApplicationContext(), "max_ad", SDKWrapper.P1, "ad_load_image", "fail", mAdSource, map);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            HomeBoostActivity activity = mActivity.get();
            if (loadedImage != null && activity != null) {
                LeoLog.d(TAG, "[HomeBoostActivity] onLoadingComplete -> " + imageUri);
				SDKWrapper.addEvent(AppMasterApplication.getInstance().getApplicationContext(), "max_ad", SDKWrapper.P1, "ad_load_image", "got:" + loadedImage.getByteCount(), mAdSource, map);
				WrappedCampaign campaign = null;
				String imgUrl = null;
				//是否单图
				boolean aPic = mCampaign != null && mCampaign.size() == 1;
				for (WrappedCampaign c : mCampaign) {
					//单个图用大图url来比较， 多模板用icon 的url来比较
					imgUrl = (aPic && c != null) ? c.getImageUrl() : c.getIconUrl();
					
					if (imgUrl != null && imgUrl.equals(imageUri)) {
						campaign = c;
						break;
					}
					
				}
                activity.notifyAdLoadFinish(campaign, loadedImage, mCampaign.indexOf(campaign), aPic);
                SDKWrapper.addEvent(activity, SDKWrapper.P1, "ad_act", "adv_shws_bst");
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }
    private static AdPreviewLoaderListener sAdImageListener;

    private void notifyAdLoadFinish(WrappedCampaign campaign, Bitmap previewBitmap, int index, boolean aPic){
        if (campaign == null && previewBitmap == null || previewBitmap.isRecycled()) {
            return;
        }

        mIsADLoaded = true;
        LeoLog.e("poha", "loaded!");
        long currentTime = System.currentTimeMillis();
        AppMasterPreference.getInstance(HomeBoostActivity.this).setLastBoostWithADTime(currentTime);
		if (aPic) {

			findViewById(R.id.template_zone).setVisibility(View.GONE);
			loadADPic(campaign.getIconUrl(),
					new ImageSize(DipPixelUtil.dip2px(HomeBoostActivity.this, 48),
							DipPixelUtil.dip2px(HomeBoostActivity.this, 48)),
					(ImageView) findViewById(R.id.iv_ad_icon));

			ImageView previewImageView = (ImageView) findViewById(R.id.iv_ad_bg);
			previewImageView.setVisibility(View.VISIBLE);
			findViewById(R.id.ad_info_zone).setVisibility(View.VISIBLE);

			if(previewImageView != null) {
				previewImageView.setImageBitmap(previewBitmap);
			}

			mAdViews.add(mRlResultWithAD);
			mAdEngine.registerView(mAdSource, mRlResultWithAD, Constants.UNIT_ID_62, mCampaignList.get(0));
		} else {

			ImageView imageView1, imageView2, imageView3;

			imageView1 = (ImageView) findViewById(R.id.imageView1);
			imageView2 = (ImageView) findViewById(R.id.imageView2);
			imageView3 = (ImageView) findViewById(R.id.imageView3);

			switch (index) {
				case 0:
					imageView1.setImageBitmap(previewBitmap);
					mAdViews.add((View)imageView1.getParent());
					mAdEngine.registerTemplateView((View)imageView1.getParent(), mCampaignList.get(index), Constants.UNIT_ID_62);
					break;
				case 1:
					imageView2.setImageBitmap(previewBitmap);
					mAdViews.add((View)imageView2.getParent());
					mAdEngine.registerTemplateView((View)imageView2.getParent(), mCampaignList.get(index), Constants.UNIT_ID_62);
					break;
				case 2:
					imageView3.setImageBitmap(previewBitmap);
					mAdViews.add((View)imageView3.getParent());
					mAdEngine.registerTemplateView((View)imageView3.getParent(), mCampaignList.get(index), Constants.UNIT_ID_62);
					break;
			}

		}

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("unitId", Constants.UNIT_ID_62 );
		SDKWrapper.addEvent(AppMasterApplication.getInstance(), "max_ad", SDKWrapper.P1, "ad_show", "show", mAdSource, map);
		
		
		
    }
	private void loadADPic(String url, ImageSize size, final ImageView v) {
		ImageLoader.getInstance().loadImage(
				url, size, options, new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {
					}

					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					}

					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						if (loadedImage != null) {
							v.setImageBitmap(loadedImage);
						}
					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {
					}
				});
	}

    private void handleIntent() {
        Intent intent = getIntent();
        String abc = intent.getStringExtra("for_sdk");
        if (abc != null) {
            SDKWrapper.addEvent(this, SDKWrapper.P1,
                    "boost", "statusbar");
        }
    }

    @Override
    protected void onResume() {
        mIvRocket.post(new Runnable() {
            @Override
            public void run() {
                startClean();
            }
        });
        super.onResume();
    }

    @Override
    public void finish() {
        if(mCdt!=null){
            mCdt.cancel();
        }
//        overridePendingTransition(0, 0);
//        LockManager lockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
//        lockManager.filterAll(500);
        super.finish();
        overridePendingTransition(DEFAULT_KEYS_DISABLE, R.anim.anim_activity_dismiss_alpha);
    }

    
    private void initUI() {
        Display mDisplay = getWindowManager().getDefaultDisplay();
        mScreenH = mDisplay.getHeight();

        mStatusBar = findViewById(R.id.bg_statusbar);
        mIvRocket = (ImageView) findViewById(R.id.iv_rocket);
        mIvCloud = (ImageView) findViewById(R.id.iv_cloud);
        mRlResultWithAD = (FrameLayout) findViewById(R.id.resuilt_with_ad);
        mSpeedAdLight = (ImageView) findViewById(R.id.speed_ad_light);

        mParent = (View) findViewById(R.id.parent);
        mFire = (ImageView) findViewById(R.id.iv_rocket_fire);
        mLine = (ImageView) findViewById(R.id.iv_line);

        tryTransStatusbar();
    }

    private void startClean() {
        ThreadManager.executeOnAsyncThread(new Runnable() {

            @Override
            public void run() {
                cleanMemory();
            }
        });

        mRocketAnim = new AnimatorSet();
        mRocketAnim.playTogether(getCloudAnim(),
                getRocketShowAnim(), getFireScaleAnim(), getRocketHideAnim(), getRocketUpAnim(),
                getLineMoveInAnim(), getLineAlphaHideAnim(), getFireUpAnim());

        mRocketAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showCleanResault();
            }
        });
        mRocketAnim.start();

    }

    /** 背景线条消失动画 */
    private ObjectAnimator getLineAlphaHideAnim() {
        ObjectAnimator lineAlphaHide = ObjectAnimator.ofFloat(mLine, "alpha", 1f, 0f);
        lineAlphaHide.setDuration(200);
        lineAlphaHide.setStartDelay(1440);

        return lineAlphaHide;
    }

    /** 背景线条进入动画 */
    private ObjectAnimator getLineMoveInAnim() {
        ObjectAnimator lineMoveIn = ObjectAnimator.ofFloat(mLine,
                "translationY", -mScreenH, mLine.getTranslationY());
        lineMoveIn.setDuration(720);
        lineMoveIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mLine.setVisibility(View.VISIBLE);
            }
        });
        lineMoveIn.setStartDelay(920);

        return lineMoveIn;
    }

    /** 火箭缩小动画 */
    private AnimatorSet getRocketHideAnim() {
        ObjectAnimator rocketScaleXAnimator = ObjectAnimator.ofFloat(mIvRocket,
                "scaleX", 1f, 0.3f);
        ObjectAnimator rocketScaleYAnimator = ObjectAnimator.ofFloat(mIvRocket,
                "scaleY", 1f, 0.3f);

        AnimatorSet rocketScaleAnim = new AnimatorSet();
        rocketScaleAnim.playTogether(rocketScaleXAnimator, rocketScaleYAnimator);
        rocketScaleAnim.setDuration(540);
        rocketScaleAnim.setStartDelay(1100);

        return rocketScaleAnim;
    }

    /** 火箭上升动画 */
    private ObjectAnimator getRocketUpAnim() {
        ObjectAnimator rocketUpAnimator = ObjectAnimator.ofFloat(mIvRocket,
                "translationY",
                mIvRocket.getTranslationY(), -mScreenH);
        rocketUpAnimator.setInterpolator(new DecelerateInterpolator());
        rocketUpAnimator.setDuration(720);
        rocketUpAnimator.setStartDelay(920);

        return rocketUpAnimator;
    }

    /** 尾部火伸缩动画 */
    private ObjectAnimator getFireScaleAnim() {
        mFire.setPivotX(mFire.getWidth()/2);
        mFire.setPivotY(0);
        ObjectAnimator fireScale = ObjectAnimator.ofFloat(mFire, "scaleY", 0f, 0.8f);
        fireScale.setDuration(320);
        fireScale.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mFire.setVisibility(View.VISIBLE);
            }
        });
        fireScale.setStartDelay(820);

        return  fireScale;
    }

    /** 尾部火上升动画 */
    private ObjectAnimator getFireUpAnim() {
        ObjectAnimator fireMoveOut = ObjectAnimator.ofFloat(mFire,
                "translationY", mFire.getTranslationY(), -mScreenH - DipPixelUtil.dip2px(this, 140));
        fireMoveOut.setInterpolator(new DecelerateInterpolator());
        fireMoveOut.setDuration(900);
        fireMoveOut.setStartDelay(920);

        return  fireMoveOut;
    }

    /** 火箭上升 */
    private AnimatorSet getRocketShowAnim() {
        ObjectAnimator bgAlphaAnimator = ObjectAnimator.ofFloat(mParent, "alpha", 0f, 0.5f);
        bgAlphaAnimator.setDuration(400);

        ObjectAnimator rocketAnimator1 = ObjectAnimator.ofFloat(mIvRocket, "translationY",
                mScreenH , mIvRocket.getTranslationY());
        rocketAnimator1.setDuration(400);

        AnimatorSet rocketShowAnimator = new AnimatorSet();
        rocketShowAnimator.playTogether(bgAlphaAnimator, rocketAnimator1);

        return  rocketShowAnimator;
    }

    /** 烟雾动画 */
    private AnimatorSet getCloudAnim() {
        ObjectAnimator cloudAlphaShow = ObjectAnimator.ofFloat(mIvCloud, "alpha", 0f, 1f);
        cloudAlphaShow.setDuration(120);

        cloudAlphaShow.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        mIvCloud.setPivotX(mIvCloud.getWidth()/2);
        mIvCloud.setPivotY(mIvCloud.getHeight());
        ObjectAnimator cloudScaleToThirty = ObjectAnimator.ofFloat(mIvCloud, "scaleX", 0f, 0.3f);
        cloudScaleToThirty.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mIvCloud.setVisibility(View.VISIBLE);
            }
        });
        ObjectAnimator cloudScaleYToFifthy = ObjectAnimator.ofFloat(mIvCloud, "scaleY", 0f, 0.3f);

        ObjectAnimator cloudScaleYToHundred = ObjectAnimator.ofFloat(mIvCloud, "scaleY", 0.3f, 1f);
        ObjectAnimator cloudScaleToHundred = ObjectAnimator.ofFloat(mIvCloud, "scaleX", 0.3f, 1f);

        ObjectAnimator cloudAlphaHide = ObjectAnimator.ofFloat(mIvCloud, "alpha", 1f, 0f);
        ObjectAnimator cloudScaleToLarge = ObjectAnimator.ofFloat(mIvCloud, "scaleX", 1f, 1.1f);

        AnimatorSet cloudStartAnimator = new AnimatorSet();
        cloudStartAnimator.playTogether(cloudAlphaShow, cloudScaleToThirty,cloudScaleYToFifthy);
        cloudStartAnimator.setDuration(120);

        AnimatorSet cloudToHundredAnimator = new AnimatorSet();
        cloudToHundredAnimator.playTogether(cloudScaleYToHundred, cloudScaleToHundred);
        cloudToHundredAnimator.setDuration(200);

        AnimatorSet cloudFinishAnimator = new AnimatorSet();
        cloudFinishAnimator.playTogether(cloudAlphaHide, cloudScaleToLarge);
        cloudFinishAnimator.setDuration(200);

        AnimatorSet cloudAnimator =  new AnimatorSet();
        cloudAnimator.playSequentially(cloudStartAnimator,
                cloudToHundredAnimator, cloudFinishAnimator);
        cloudAnimator.setStartDelay(600);

        return  cloudAnimator;
    }

    private void cleanMemory() {
        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        long currentTime = System.currentTimeMillis();
        long lastBoostTime = amp.getLastBoostTime();
        if ((currentTime - lastBoostTime) < 10 * 1000) {
            isClean = false;
        } else {
            isClean = true;
            isCleanFinish = false;
            mCleaner = ProcessCleaner.getInstance(this);
            mLastUsedMem = mCleaner.getUsedMem();
            mCleaner.tryClean(this);
            long curUsedMem = mCleaner.getUsedMem();
            mCleanMem = Math.abs(mLastUsedMem - curUsedMem);
            isCleanFinish = true;
            amp.setLastBoostTime(currentTime);
        }
    }

    public void showCleanResault() {

        String mToast;

        if (isClean) {           
            if (!isCleanFinish || mCleanMem < ProcessCleaner.MIN_CLEAN_SIZE) {
                mToast = getString(R.string.home_app_manager_mem_clean,
                        TextFormater.dataSizeFormat((mRandom.nextInt(150) + 10) * 1024 * 1024L));
            } else {
                mToast = getString(R.string.home_app_manager_mem_clean,
                        TextFormater.dataSizeFormat(mCleanMem));
            }
        } else {
            mToast = getString(R.string.the_best_status_toast);
        }
        if (mIsADLoaded) {
            SDKWrapper.addEvent(HomeBoostActivity.this, SDKWrapper.P1, "ad_act",
                    "adv_shws_bst");
            adShowAnimation();
            TextView resultText = (TextView) findViewById(R.id.tv_accelerat_result);
            resultText.setText(Html.fromHtml(mToast));
            isClean = true;
            final TextView counter = (TextView) findViewById(R.id.tv_counter);
            mCdt = new CountDownTimer(5000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    mCountDownNum--;
                    counter.setText(mCountDownNum+"");
                }

                @Override
                public void onFinish() {
                    counter.setText(0+"");
                    HomeBoostActivity.this.finish();

                }
            };
            mCdt.start();
        }  else {
            LayoutInflater inflater = LayoutInflater.from(this);
            // View view = inflater.inflate(R.layout.toast_self_make, null);
            View view = inflater.inflate(R.layout.view_after_accelerate_new, null);
            TextView tv_clean_rocket = (TextView) view.findViewById(R.id.tv_accelerat_result);
            tv_clean_rocket.setText(Html.fromHtml(mToast));
            Toast toast = new Toast(this);
            toast.setGravity(Gravity.CENTER, 0, -100);

            toast.setView(view);
            toast.setDuration(Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, marginTop);
            toast.show();
            HomeBoostActivity.this.finish();
            isClean = true;
        }

    }
    

    /** 广告界面动画 */
    private void adShowAnimation() {
        ObjectAnimator adScaleXLarge = ObjectAnimator.ofFloat(mRlResultWithAD, "scaleX", 0f , 1.1f);
        ObjectAnimator adScaleYLarge = ObjectAnimator.ofFloat(mRlResultWithAD, "scaleY", 0f , 1.1f);
        adScaleXLarge.setDuration(320);
        adScaleYLarge.setDuration(320);
        adScaleXLarge.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mRlResultWithAD.setVisibility(View.VISIBLE);
            }
        });

        AnimatorSet adScaleLarge = new AnimatorSet();
        adScaleLarge.playTogether(adScaleXLarge, adScaleYLarge);

        ObjectAnimator adAlpha = ObjectAnimator.ofFloat(mRlResultWithAD, "alpha", 0f , 1f);
        adAlpha.setDuration(320);
        ObjectAnimator adScaleXNormal = ObjectAnimator.ofFloat(mRlResultWithAD, "scaleX", 1.1f, 1f);
        ObjectAnimator adScaleYNormal = ObjectAnimator.ofFloat(mRlResultWithAD, "scaleY", 1.1f, 1f);
        adScaleXNormal.setDuration(200);
        adScaleYNormal.setDuration(200);

        AnimatorSet adScaleNormal = new AnimatorSet();
        adScaleNormal.playTogether(adScaleXNormal, adScaleYNormal);

        AnimatorSet adAnimator = new AnimatorSet();
        adAnimator.play(adScaleLarge).with(adAlpha);
        adAnimator.play(adScaleNormal).after(adScaleLarge);
        adAnimator.setStartDelay(280);

        AnimatorSet adLightAnimator = adLightAnimation();

        mAdAnim = new AnimatorSet();
        mAdAnim.play(adLightAnimator).after(200).after(adAnimator);
        mAdAnim.start();


    }

    /** 光圈动画 */
    private AnimatorSet adLightAnimation() {
        ObjectAnimator adLightAlphaShow = ObjectAnimator.ofFloat(mSpeedAdLight, "alpha", 0f, 1f);
        adLightAlphaShow.setDuration(400);
        adLightAlphaShow.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mSpeedAdLight.setVisibility(View.VISIBLE);
            }
        });
        ObjectAnimator adLightAlphaHide = ObjectAnimator.ofFloat(mSpeedAdLight, "alpha", 1f, 0f);
        adLightAlphaHide.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mSpeedAdLight.setVisibility(View.GONE);
            }
        });
        adLightAlphaHide.setDuration(800);
        ObjectAnimator adLightTranslate = ObjectAnimator.ofFloat(mSpeedAdLight, "rotation", 0f, 45f);
        adLightTranslate.setDuration(800);

        AnimatorSet adLightAnimator = new AnimatorSet();
        adLightAnimator.play(adLightAlphaHide).with(adLightTranslate).after(adLightAlphaShow);

        return  adLightAnimator;
    }

    private void tryTransStatusbar() {
        if (VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            mStatusBar.setVisibility(View.GONE);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(DEFAULT_KEYS_DISABLE, DEFAULT_KEYS_DISABLE);
        if(mAdEngine != null && mCampaignList != null) {
			for (int i = 0; (mCampaignList.size() > 0 && i < mCampaignList.size()); i++) {
				mAdEngine.releaseAd(mAdSource, Constants.UNIT_ID_62, mAdViews.get(i),mCampaignList.get(i), mvNativeHandler);
			}
			
        }
        if (mAdAnim != null) {
            mAdAnim.cancel();
            mAdAnim = null;
        }
        if (mRocketAnim != null) {
            mRocketAnim.cancel();
            mRocketAnim = null;
        }
    }

}
