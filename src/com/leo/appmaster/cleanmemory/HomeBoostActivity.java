
package com.leo.appmaster.cleanmemory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.lang.ref.WeakReference;

public class HomeBoostActivity extends Activity {
    private ImageView mIvRocket, mIvCloud;
    private View mStatusBar;
    private int mRocketHeight;
    private MobvistaEngine mAdEngine;
    private boolean isClean = false;
    private ProcessCleaner mCleaner;
    private long mLastUsedMem;
    private long mCleanMem;
    private boolean isCleanFinish = false;
    private int mScreenH;
    private int mCountDownNum = 5;
    private boolean mIsADLoaded = false;
    private RelativeLayout mRlResultWithAD;
    private CountDownTimer mCdt;

    private View mParent;
    private ImageView mFire;
    private ImageView mLine;

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
        long currentTime = System.currentTimeMillis();
        long lastBoostWithADTime = amp.getLastBoostWithADTime();
        LeoLog.e("poha", "currentTime - lastBoostWithADTime="+(currentTime - lastBoostWithADTime)+"=====24小时=8640000=====开关值="+amp.getADChanceAfterAccelerating());
        
        if (/*(currentTime - lastBoostWithADTime) > 1000 * 60 * 60 * 24
                && */amp.getADChanceAfterAccelerating()==1)
        {
            LeoLog.e("poha", "to load");
            loadAD();
        }
    }

    private void loadAD() {

        mAdEngine = MobvistaEngine.getInstance(this);
        mAdEngine.loadMobvista(Constants.UNIT_ID_62,new MobvistaListener() {

            @Override
            public void onMobvistaFinished(int code, Campaign campaign, String msg) {
                if (code == MobvistaEngine.ERR_OK && mRlResultWithAD != null && campaign != null) {
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
                (ImageView) mRlResultWithAD.findViewById(R.id.iv_ad_icon));

        ImageView previewImageView = (ImageView) mRlResultWithAD.findViewById(R.id.iv_ad_bg);
        previewImageView.setImageBitmap(previewBitmap);

        TextView appname = (TextView) mRlResultWithAD.findViewById(R.id.tv_ad_appname);
        if(appname != null) {
            appname.setText(campaign.getAppName());
        }

        TextView appdesc = (TextView) mRlResultWithAD.findViewById(R.id.tv_ad_appdesc);
        if(appdesc != null) {
            appdesc.setText(campaign.getAppDesc());
        }

        Button call = (Button) mRlResultWithAD.findViewById(R.id.btn_ad_appcall);
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
        mRlResultWithAD = (RelativeLayout) findViewById(R.id.rl_withAD);
        mStatusBar = findViewById(R.id.bg_statusbar);
        mIvRocket = (ImageView) findViewById(R.id.iv_rocket);
        mIvCloud = (ImageView) findViewById(R.id.iv_cloud);

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

        ObjectAnimator BgAlphaAnimator = ObjectAnimator.ofFloat(mParent, "alpha", 0f, 0.5f);
        BgAlphaAnimator.setDuration(400);

        mRocketHeight = mIvRocket.getMeasuredHeight();
        ObjectAnimator rocketAnimator1 = ObjectAnimator.ofFloat(mIvRocket, "translationY",
                mRocketHeight, mRocketHeight * 0.10f, mRocketHeight * 0.18f, mRocketHeight * 0.22f);
        rocketAnimator1.setDuration(400);

        AnimatorSet rocketShowAnimator = new AnimatorSet();
        rocketShowAnimator.playTogether(BgAlphaAnimator, rocketAnimator1);

        ObjectAnimator rocketAnimator2 = ObjectAnimator.ofFloat(mIvRocket,
                "translationY",
                mRocketHeight * 0.22f, -mScreenH);
        rocketAnimator2.setDuration(720);


        /** 烟雾动画 */
        ObjectAnimator cloudAlphaShow = ObjectAnimator.ofFloat(mIvCloud, "alpha", 0f, 1f);
        cloudAlphaShow.setDuration(120);
        ObjectAnimator cloudScaleToThirty = ObjectAnimator.ofFloat(mIvCloud, "scaleX", 0f, 0.3f);
        cloudScaleToThirty.setDuration(120);
        cloudAlphaShow.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mIvCloud.setVisibility(View.VISIBLE);
            }
        });
        ObjectAnimator cloudScaleToHundred = ObjectAnimator.ofFloat(mIvCloud, "scaleX", 0.3f, 1f);
        cloudScaleToHundred.setDuration(480);
        ObjectAnimator cloudAlphaHide = ObjectAnimator.ofFloat(mIvCloud, "alpha", 1f, 0f);
        cloudAlphaHide.setDuration(200);
        ObjectAnimator cloudScaleToLarge = ObjectAnimator.ofFloat(mIvCloud, "scaleX", 1f, 1.1f);
        cloudScaleToLarge.setDuration(200);

        AnimatorSet cloudStartAnimator = new AnimatorSet();
        cloudStartAnimator.playTogether(cloudAlphaShow, cloudScaleToThirty);

        AnimatorSet cloudFinishAnimator = new AnimatorSet();
        cloudFinishAnimator.playTogether(cloudAlphaHide, cloudScaleToLarge);

        AnimatorSet cloudAnimator =  new AnimatorSet();
        cloudAnimator.playSequentially(cloudStartAnimator, cloudScaleToHundred, cloudFinishAnimator);

        /** 尾部火动画 */
        ObjectAnimator fireAnimator = ObjectAnimator.ofFloat(mFire, "scaleY", 0f, 1f);


        /** 背景线条动画 */
        ObjectAnimator lineMoveIn = ObjectAnimator.ofFloat(mLine,
                "translationY", -mScreenH, mLine.getMeasuredHeight());


        AnimatorSet as = new AnimatorSet();
        as.play(cloudAnimator).after(200).after(rocketShowAnimator);
        as.start();
        as.setInterpolator(new LinearInterpolator());
        as.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showCleanResault();
            }
        });

//        rocketAnimator2.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                mIvCloud.setVisibility(View.VISIBLE);
//                ObjectAnimator cloudAlphaAnimator1 = ObjectAnimator.ofFloat(mIvCloud,
//                        "alpha", 0f,
//                        1f);
//                cloudAlphaAnimator1.setDuration(300);
//                ObjectAnimator cloudScaleXAnimator = ObjectAnimator.ofFloat(mIvCloud,
//                        "scaleX", 1f,
//                        1.2f);
//                ObjectAnimator cloudAlphaAnimator2 = ObjectAnimator.ofFloat(mIvCloud,
//                        "alpha", 1f,
//                        0f);
//                cloudScaleXAnimator.setDuration(1200);
//                cloudAlphaAnimator2.setDuration(1200);
//                cloudAlphaAnimator2.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        if(!mIsADLoaded){
//                            HomeBoostActivity.this.finish();
//                        }
//                    }
//                });
//                AnimatorSet cloudAnimatorSet = new AnimatorSet();
//                cloudAnimatorSet.play(cloudScaleXAnimator).with(cloudAlphaAnimator2)
//                        .after(cloudAlphaAnimator1);
//                cloudAnimatorSet.start();
//            }
//
//        });
//        AnimatorSet as = new AnimatorSet();
//        as.play(rocketAnimator2).after(300).after(rocketShowAnimator);
//        as.start();
//        as.setInterpolator(new LinearInterpolator());
//        as.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                showCleanResault();
//            }
//        });
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
            mRlResultWithAD.setVisibility(View.VISIBLE);
            TextView resultText = (TextView) mRlResultWithAD.findViewById(R.id.tv_accelerat_result);
            resultText.setText(mToast);
            isClean = true;
            final TextView counter = (TextView) mRlResultWithAD.findViewById(R.id.tv_counter);
            mCdt = new CountDownTimer(4000, 1000) {
               
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
        }
        else {
            LayoutInflater inflater = LayoutInflater.from(this);
            // View view = inflater.inflate(R.layout.toast_self_make, null);
            View view = inflater.inflate(R.layout.view_after_accelerate_new, null);
            TextView tv_clean_rocket = (TextView) view.findViewById(R.id.tv_accelerat_result);
            tv_clean_rocket.setText(mToast);
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
            isClean = true;
        }

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
    }

}
