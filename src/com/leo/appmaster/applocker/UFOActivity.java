
package com.leo.appmaster.applocker;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.R.id;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.MobvistaEngine.MobvistaListener;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageSize;
import com.mobvista.sdk.m.core.MobvistaAd;
import com.mobvista.sdk.m.core.MobvistaAdNative;
import com.mobvista.sdk.m.core.entity.Campaign;

public class UFOActivity extends BaseActivity implements ImageLoadingListener {

    private boolean mHasPlayed = false;// 是否播放过动画，开始播放后置为true，以后每次WindowFocusChanged后就不播放动画了，
    private boolean mHasGetLoadResult = false;// 是否已经得到拉取广告的结果，用于控制loading的实时结束
    private boolean mIsLoaded = false;// 是否从MobvistaEngine的接口中得到结果
    private AnimationDrawable mUFODrawable;//
    // 广告素材
    private MobvistaEngine mAdEngine;

    private ImageView mClose;
    private RelativeLayout mWholeUFO;
    private RelativeLayout mDialog;
    private ImageView mAlien;
    private ImageView mUFO;
    private ImageView mLongLight;
    private ImageView mCircleLight;
    private ImageView mSplashLight;
    private CountDownTimer mCdt;
    private Button mInstall;
    // 动画参数
    private float mUFOW;
    private float mUFOH;
    private float mWindowW;
    private float mWindowH;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_ufo);
        InitUI();
        loadAD();
    };

    private void loadAD() {

        mAdEngine = MobvistaEngine.getInstance();
        mAdEngine.loadMobvista(this, new MobvistaListener() {

            @Override
            public void onMobvistaFinished(int code, Campaign campaign, String msg) {
                if (code == MobvistaEngine.ERR_OK) {
                    mIsLoaded = true;
                    loadADPic(
                            campaign.getIconUrl(),
                            new ImageSize(DipPixelUtil.dip2px(UFOActivity.this, 48), DipPixelUtil
                                    .dip2px(UFOActivity.this, 48)),
                            (ImageView) mDialog.findViewById(R.id.iv_ufo_ad_icon));
                    loadADPic(
                            campaign.getImageUrl(),
                            new ImageSize(DipPixelUtil.dip2px(UFOActivity.this, 302), DipPixelUtil
                                    .dip2px(UFOActivity.this, 158)),
                            (ImageView) mDialog.findViewById(R.id.iv_appbg_ufo));

                    TextView appname = (TextView) mDialog.findViewById(R.id.tv_appname_ufo);
                    appname.setText(campaign.getAppName());

                    TextView appdesc = (TextView) mDialog.findViewById(R.id.tv_appdesc_ufo);
                    appdesc.setText(campaign.getAppDesc());

                    Button call = (Button) mDialog.findViewById(R.id.btn_ufo_dialog_install);
                    call.setText(campaign.getAdCall());
                    mAdEngine.registerView(call);
                }

            }

            @Override
            public void onMobvistaClick(Campaign campaign) {
                UFOActivity.this.finish();
                SDKWrapper.addEvent(UFOActivity.this, SDKWrapper.P1, "ad_cli", "draw_gp");
                AppMasterPreference.getInstance(UFOActivity.this).setAdEtClickTime(
                        System.currentTimeMillis());
            }
        });
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

    private void InitUI() {

        mClose = (ImageView) findViewById(R.id.iv_close_ufo);
        mClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UFOActivity.this.finish();
            }
        });
        mWholeUFO = (RelativeLayout) findViewById(R.id.rl_ufo_withalien);
        mUFO = (ImageView) mWholeUFO.findViewById(R.id.iv_ufo);
        mAlien = (ImageView) mWholeUFO.findViewById(R.id.iv_alien);
        mDialog = (RelativeLayout) findViewById(R.id.rl_ADdialog);
        mInstall = (Button) findViewById(R.id.btn_ufo_dialog_install);
        // mNativeAd.registerView(mInstall, null);

        mLongLight = (ImageView) findViewById(R.id.iv_longlight);
        mCircleLight = (ImageView) findViewById(R.id.iv_circlelight);
        mSplashLight=(ImageView) findViewById(R.id.iv_splashlight);
        WindowManager wm = this.getWindowManager();
        mWindowW = wm.getDefaultDisplay().getWidth();
        mWindowH = wm.getDefaultDisplay().getHeight();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!mHasPlayed) {
            mHasPlayed = true;
            mUFOH = mWholeUFO.getHeight();
            mUFOW = mWholeUFO.getWidth();
            playUFOFly();
        }
    }

    private void playUFOFly() {
        mWholeUFO.setVisibility(View.VISIBLE);
        float ufoStartX = mWholeUFO.getX();
        float ufoStartY = mWholeUFO.getY();

        float xWhenBorder = mWindowW - mUFOW;// UFO在最右边时的x
        float xWhenMiddle = (mWindowW - mUFOW) / 2;// UFO最终时刻的x，使得UFO在水平方向中间

        float yWhenFinal = mWindowH / 4 - mUFOH / 2;// UFO最终时刻的y，使得UFO在屏幕竖直方向1/4处
        float yWhenTop = 0f;// UFO在最顶上的y

        PropertyValuesHolder ufoX = PropertyValuesHolder.ofFloat("x", 
                ufoStartX,
                (xWhenBorder - ufoStartX) / 4,
                2 * (xWhenBorder - ufoStartX) / 4,
                3 * (xWhenBorder - ufoStartX) / 4,
                4 * (xWhenBorder - ufoStartX) / 4,
                xWhenMiddle + 4 * (xWhenBorder - xWhenMiddle) / 5,
                xWhenMiddle + 3 * (xWhenBorder - xWhenMiddle) / 5,
                xWhenMiddle + 2 * (xWhenBorder - xWhenMiddle) / 5,
                xWhenMiddle + 1 * (xWhenBorder - xWhenMiddle) / 5, xWhenMiddle
                );
        PropertyValuesHolder ufoY = PropertyValuesHolder.ofFloat("y", 
                ufoStartY,
                4 * (yWhenTop + ufoStartY) / 7,
                yWhenTop, 
                4 * (yWhenTop + ufoStartY) / 7, 
                ufoStartY, 
                ufoStartY + 1 *(yWhenFinal - ufoStartY) / 4,
                ufoStartY + 2 * (yWhenFinal - ufoStartY) / 4, 
                ufoStartY + 3 * (yWhenFinal - ufoStartY) / 4, 
                yWhenFinal
                );
        PropertyValuesHolder ufoSX = PropertyValuesHolder.ofFloat("scaleX", 0.1f, 0.2f, 0.3f, 0.4f,
                0.5f, 1f, 1f, 1f, 1f
                , 1f);
        PropertyValuesHolder ufoSY = PropertyValuesHolder.ofFloat("scaleY", 0.1f, 0.2f, 0.3f, 0.4f,
                0.5f, 1f, 1f, 1f, 1f
                , 1f);

        final ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(mWholeUFO, ufoX,
                ufoY, ufoSX, ufoSY);
        animator.setDuration(2000);
        animator.start();

        animator.addListener(new MyEndAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator.clearAllAnimations();
                startLoading();
            }
        });
    }

    private void startLoading() {
        float y1 = mWholeUFO.getY();
        int detay2 = DipPixelUtil.dip2px(UFOActivity.this, 10);
        PropertyValuesHolder ufoY2 = PropertyValuesHolder.ofFloat("y", y1, y1 + detay2, y1,
                y1 - detay2, y1, y1 + detay2, y1, y1 - detay2, y1, y1 - detay2, y1);
        ObjectAnimator animator2 = ObjectAnimator.ofPropertyValuesHolder(mWholeUFO, ufoY2);
        animator2.setDuration(6000);
        animator2.start();

        mUFO.setImageResource(R.anim.ufo_light);
        mUFODrawable = (AnimationDrawable) mUFO.getDrawable();
        mUFODrawable.start();
        // boolean hasGetLoadResult = false;
        mCdt = new CountDownTimer(6000, 1000) {
            public void onTick(long millisUntilFinished) {
                if (mIsLoaded && !mHasGetLoadResult) {
                    if (mCdt != null) {
                        mCdt.onFinish();
                        mCdt.cancel();
                    }
                    mHasGetLoadResult = true;
                }
            }

            public void onFinish() {
                if (!mHasGetLoadResult) {
                    if (!mIsLoaded) {
                        mUFODrawable.stop();
                        mWholeUFO.setVisibility(View.INVISIBLE);
                        mLongLight.setVisibility(View.INVISIBLE);
                        mCircleLight.setVisibility(View.INVISIBLE);
                        // mAlien.setVisibility(View.INVISIBLE);
                        Toast.makeText(UFOActivity.this, getString(R.string.ad_timeout), 1).show();
                        UFOActivity.this.finish();
                    }
                    else {
                        showAD();
                    }// else2 end
                }// if1 end
                mHasGetLoadResult = true;
            }
        }.start();
    }

    protected void showAD() {
        mDialog.setVisibility(View.VISIBLE);
        mDialog.setPivotX(mDialog.getWidth()/2);
        mDialog.setPivotY(0);
        mDialog.setY(mWholeUFO.getY()+mWholeUFO.getHeight()+DipPixelUtil.dip2px(UFOActivity.this, 5));
        float xWhenMiddle = (mWindowW - mDialog.getWidth()) / 2;
        float yWhenMiddle =  (mWindowH - mDialog.getHeight()) / 2;
        //广告对话框的出现动画
        PropertyValuesHolder dialogy = PropertyValuesHolder.ofFloat("y",
                Math.min(mDialog.getY() + DipPixelUtil.dip2px(UFOActivity.this, 5),mWindowH - mDialog.getHeight()),
                Math.min(mDialog.getY() + DipPixelUtil.dip2px(UFOActivity.this, 15),mWindowH - mDialog.getHeight()),
                Math.min(mDialog.getY() + DipPixelUtil.dip2px(UFOActivity.this, 25),mWindowH - mDialog.getHeight()),
                Math.min(mDialog.getY() + DipPixelUtil.dip2px(UFOActivity.this, 55),mWindowH - mDialog.getHeight()),
                Math.min(mDialog.getY() + DipPixelUtil.dip2px(UFOActivity.this, 75),mWindowH - mDialog.getHeight()),
                Math.min(mDialog.getY() + DipPixelUtil.dip2px(UFOActivity.this, 95),mWindowH - mDialog.getHeight()),
                yWhenMiddle - (3/(float)4)* (yWhenMiddle-Math.min(mDialog.getY() + DipPixelUtil.dip2px(UFOActivity.this, 95),mWindowH - mDialog.getHeight())),   
                yWhenMiddle - (2/(float)4)* (yWhenMiddle-Math.min(mDialog.getY() + DipPixelUtil.dip2px(UFOActivity.this, 95),mWindowH - mDialog.getHeight())),                  
                yWhenMiddle - (1/ (float)4)*(yWhenMiddle-Math.min(mDialog.getY() + DipPixelUtil.dip2px(UFOActivity.this, 95),mWindowH - mDialog.getHeight())),
                yWhenMiddle);
        PropertyValuesHolder dialogscalex = PropertyValuesHolder.ofFloat("scaleX",0.1f,0.1f, 0.1f, 0.1f, 0.1f, 0.4f, 0.6f, 0.8f, 0.9f, 1f);
        PropertyValuesHolder dialogscaley = PropertyValuesHolder.ofFloat("scaleY", 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.4f, 0.6f, 0.8f, 0.9f, 1f);
        PropertyValuesHolder dialogalpha = PropertyValuesHolder.ofFloat("Alpha", 0.3f,0.3f, 0.5f, 0.7f, 1f, 1f, 1, 1f, 1f, 1f);
        ObjectAnimator animator2 = ObjectAnimator.ofPropertyValuesHolder(mDialog,dialogscalex, dialogscaley, dialogy, dialogalpha);
        animator2.setDuration(2000);
        animator2.start();
        animator2.addListener(new MyEndAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mUFODrawable.stop();
                LeoLog.e("poha", mDialog.getX() + mDialog.getWidth() / 2 + "DX");
                LeoLog.e("poha", mDialog.getY() + "DY");
                mWholeUFO.setVisibility(View.INVISIBLE);
                mLongLight.setVisibility(View.INVISIBLE);
                mCircleLight.setVisibility(View.INVISIBLE);
                findViewById(R.id.rl_ufo_rootview).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                UFOActivity.this.finish();
                            }
                        });
                }
        });
        //同时出现的UFO灯光动画
        mLongLight.setVisibility(View.VISIBLE);
        mCircleLight.setVisibility(View.VISIBLE);
        mSplashLight.setVisibility(View.VISIBLE);
        mSplashLight.setY(mWholeUFO.getY() + mWholeUFO.getHeight() - (DipPixelUtil.dip2px(UFOActivity.this, 30)));
        mLongLight.setY(mWholeUFO.getY() + mWholeUFO.getHeight() - (DipPixelUtil.dip2px(UFOActivity.this, 30)));
        mCircleLight.setY(mSplashLight.getY() + mSplashLight.getHeight() - (float) (mCircleLight.getHeight() / 2));
        mLongLight.setPivotX(0.5f);
        mLongLight.setPivotY(0);
        mSplashLight.setPivotX(mSplashLight.getWidth()/2);
        
        PropertyValuesHolder splashLightScaleXAnim = PropertyValuesHolder.ofFloat("scaleX",0.2f,0.4f,0.6f,0.8f,1.0f);
        ObjectAnimator animator3 = ObjectAnimator.ofPropertyValuesHolder(mSplashLight,splashLightScaleXAnim); 
        animator3.setDuration(700);
        animator3.start();
//        mCircleLight.setPivotX(mCircleLight.getWidth()/2);
//        mCircleLight.setPivotY(mCircleLight.getHeight()/2);
        PropertyValuesHolder circleLightScaleXAnim = PropertyValuesHolder.ofFloat("scaleX",0.2f,0.4f,0.6f,0.8f,1.0f);
        ObjectAnimator animator4 = ObjectAnimator.ofPropertyValuesHolder(mCircleLight,circleLightScaleXAnim); 
        animator4.setDuration(700);
        animator4.start();
    }

    private abstract class MyEndAnimatorListener implements AnimatorListener
    {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

    }

    @Override
    public void finish() {
        if (mCdt != null) {
            mCdt.cancel();
        }
        super.finish();
        overridePendingTransition(DEFAULT_KEYS_DISABLE, DEFAULT_KEYS_DISABLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LockScreenActivity.interupAinimation = false;
        overridePendingTransition(DEFAULT_KEYS_DISABLE, DEFAULT_KEYS_DISABLE);
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {

    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {

    }
}
