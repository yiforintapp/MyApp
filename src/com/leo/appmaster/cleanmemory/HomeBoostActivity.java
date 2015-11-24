
package com.leo.appmaster.cleanmemory;

import java.lang.ref.WeakReference;

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
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.MobvistaEngine.MobvistaListener;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.TextFormater;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageSize;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.mobvista.sdk.m.core.entity.Campaign;

public class HomeBoostActivity extends Activity {
    private ImageView mIvRocket, mIvCloud;
    private View mStatusBar;
    private MobvistaEngine mAdEngine;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_boost_activity);
        
        if(getIntent().getBooleanExtra("from_quickhelper", false)){
            SDKWrapper.addEvent(AppMasterApplication.getInstance(), SDKWrapper.P1,
                    "assistant", "accelerate_cnts");
        }
        
        initUI();
        handleIntent();
        overridePendingTransition(0, 0);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "boost", "launcher");
        
        AppMasterPreference amp = AppMasterPreference.getInstance(this);       
        if (NetWorkUtil.isNetworkAvailable(getApplicationContext()) && amp.getADChanceAfterAccelerating() == 1) {
            LeoLog.e("poha", "to load");
            loadAD();
        }
    }

    private void loadAD() {

        mAdEngine = MobvistaEngine.getInstance(this);
        mAdEngine.loadMobvista(Constants.UNIT_ID_62,new MobvistaListener() {

            @Override
            public void onMobvistaFinished(int code, Campaign campaign, String msg) {
                if (code == MobvistaEngine.ERR_OK  && campaign != null) {
                    sAdImageListener = new AdPreviewLoaderListener(HomeBoostActivity.this, campaign);
                    ImageLoader.getInstance().loadImage(campaign.getImageUrl(),
                            new ImageSize(DipPixelUtil.dip2px(HomeBoostActivity.this, 262),
                                    DipPixelUtil.dip2px(HomeBoostActivity.this, 130)),
                            sAdImageListener);
                }
            }

            @Override
            public void onMobvistaClick(Campaign campaign) {
                HomeBoostActivity.this.finish();
             
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
        Campaign mCampaign;

        public AdPreviewLoaderListener (HomeBoostActivity activity, final Campaign campaign) {
            mActivity = new WeakReference<HomeBoostActivity>(activity);
            mCampaign = campaign;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            HomeBoostActivity activity = mActivity.get();
            if (loadedImage != null && activity != null) {
                LeoLog.d("MobvistaEngine", "[HomeBoostActivity]onLoadingComplete -> " + imageUri);
                activity.notifyAdLoadFinish(mCampaign, loadedImage);
                SDKWrapper.addEvent(activity, SDKWrapper.P1, "ad_act",
                        "adv_shws_bst");
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }
    private static AdPreviewLoaderListener sAdImageListener;

    private void notifyAdLoadFinish(Campaign campaign, Bitmap previewBitmap){
        mIsADLoaded = true;
        LeoLog.e("poha", "loaded!");
        long currentTime = System.currentTimeMillis();
        AppMasterPreference.getInstance(HomeBoostActivity.this).setLastBoostWithADTime(currentTime);
        loadADPic(campaign.getIconUrl(),
                new ImageSize(DipPixelUtil.dip2px(HomeBoostActivity.this, 48),
                        DipPixelUtil
                                .dip2px(HomeBoostActivity.this, 48)),
                (ImageView) findViewById(R.id.iv_ad_icon));

        ImageView previewImageView = (ImageView) findViewById(R.id.iv_ad_bg);
        if(previewImageView != null) {
            previewImageView.setImageBitmap(previewBitmap);
        }
        TextView appname = (TextView) findViewById(R.id.tv_ad_appname);
        if(appname != null) {
            appname.setText(campaign.getAppName());
        }

        TextView appdesc = (TextView) findViewById(R.id.tv_ad_appdesc);
        if(appdesc != null) {
            appdesc.setText(campaign.getAppDesc());
        }

        Button call = (Button) findViewById(R.id.btn_ad_appcall);
        if(call != null) {
            call.setText(campaign.getAdCall());
            mAdEngine.registerView(Constants.UNIT_ID_62, call);
        }
    }

    private void loadADPic(String url, ImageSize size, final ImageView v) {
        ImageLoader.getInstance().loadImage(
                url, size, new ImageLoadingListener() {

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
        overridePendingTransition(0, 0);
        LockManager lockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        lockManager.filterAll(500);
        super.finish();
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
        mRocketAnim.playTogether(getCloudAnim(), getRocketShowAnim(), getFireAnim(),
                getRocketUpAnim(), getLineMoveInAnim(), getLineAlphaHideAnim());
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
        lineAlphaHide.setStartDelay(1220);

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
        lineMoveIn.setStartDelay(720);

        return lineMoveIn;
    }

    /** 火箭上升动画 */
    private ObjectAnimator getRocketUpAnim() {
        ObjectAnimator rocketUpAnimator = ObjectAnimator.ofFloat(mIvRocket,
                "translationY",
                mIvRocket.getTranslationY(), -mScreenH);
        rocketUpAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rocketUpAnimator.setDuration(820);
        rocketUpAnimator.setStartDelay(720);

        return rocketUpAnimator;
    }

    /** 尾部火动画 */
    private AnimatorSet getFireAnim() {
        mFire.setPivotX(mFire.getWidth()/2);
        mFire.setPivotY(mFire.getTop());
        ObjectAnimator fireScale = ObjectAnimator.ofFloat(mFire, "scaleY", 0f, 1f);
        fireScale.setDuration(320);
        fireScale.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mFire.setVisibility(View.VISIBLE);
            }
        });

        ObjectAnimator fireMoveOut = ObjectAnimator.ofFloat(mFire,
                "translationY", mFire.getTranslationY(),
                -mScreenH - DipPixelUtil.dip2px(HomeBoostActivity.this, 130));

        fireMoveOut.setDuration(880);

        AnimatorSet fireAnimator = new AnimatorSet();
        fireAnimator.playTogether(fireScale, fireMoveOut);
        fireAnimator.setStartDelay(720);

        return  fireAnimator;
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
        mIvCloud.setPivotY(mIvCloud.getBottom());
        ObjectAnimator cloudScaleToThirty = ObjectAnimator.ofFloat(mIvCloud, "scaleX", 0f, 0.3f);
        cloudScaleToThirty.setDuration(120);
        cloudAlphaShow.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mIvCloud.setVisibility(View.VISIBLE);

            }
        });
        ObjectAnimator cloudScaleYToFifthy = ObjectAnimator.ofFloat(mIvCloud, "scaleY", 0.95f, 0.95f);
        cloudScaleYToFifthy.setDuration(120);

        ObjectAnimator cloudScaleYToHundred = ObjectAnimator.ofFloat(mIvCloud, "scaleY", 0.95f, 1f);
        cloudScaleYToHundred.setDuration(380);

        ObjectAnimator cloudScaleToHundred = ObjectAnimator.ofFloat(mIvCloud, "scaleX", 0.3f, 1f);
        cloudScaleToHundred.setDuration(380);
        ObjectAnimator cloudAlphaHide = ObjectAnimator.ofFloat(mIvCloud, "alpha", 1f, 0f);
        cloudAlphaHide.setDuration(200);
        ObjectAnimator cloudScaleToLarge = ObjectAnimator.ofFloat(mIvCloud, "scaleX", 1f, 1.1f);
        cloudScaleToLarge.setDuration(200);

        AnimatorSet cloudStartAnimator = new AnimatorSet();
        cloudStartAnimator.playTogether(cloudAlphaShow, cloudScaleToThirty,cloudScaleYToFifthy);

        AnimatorSet cloudToHundredAnimator = new AnimatorSet();
        cloudToHundredAnimator.playTogether(cloudScaleYToHundred, cloudScaleToHundred);

        AnimatorSet cloudFinishAnimator = new AnimatorSet();
        cloudFinishAnimator.playTogether(cloudAlphaHide, cloudScaleToLarge);

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
            if (isCleanFinish) {
                if (mCleanMem <= 0) {
                    LeoLog.d("testspeed", "CleanMem <= 0");
                    mToast = getString(R.string.home_app_manager_mem_clean_one);
                } else {
                    LeoLog.d("testspeed", "CleanMem > 0");
                    mToast = getString(R.string.home_app_manager_mem_clean,
                            TextFormater.dataSizeFormat(mCleanMem));
                }
            } else {
                mToast = getString(R.string.home_app_manager_mem_clean,
                        TextFormater.dataSizeFormat(230));
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
            toast.setDuration(0);
            int marginTop = 0;
            if (mScreenH >= 1920) {
                marginTop = 150;
            } else if (mScreenH >= 1280) {
                marginTop = 120;
            } else if (mScreenH >= 800) {
                marginTop = 80;
            } else {
                marginTop = 30;
            }
//            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, marginTop);
            toast.show();
            mCdt = new CountDownTimer(2000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    HomeBoostActivity.this.finish();

                }
            };
            mCdt.start();
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
        if(mAdEngine!=null){
            mAdEngine.release(Constants.UNIT_ID_62);
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
