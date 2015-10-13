
package com.leo.appmaster.fragment;

import java.util.List;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.gesture.LockPatternView;
import com.leo.appmaster.applocker.gesture.LockPatternView.Cell;
import com.leo.appmaster.applocker.gesture.LockPatternView.OnPatternListener;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.MobvistaEngine.MobvistaListener;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.eventbus.event.SubmaineAnimEvent;
import com.leo.appmaster.lockertheme.ResourceName;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.theme.LeoResources;
import com.leo.appmaster.theme.ThemeUtils;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.LockPatternUtils;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageSize;
import com.mobvista.sdk.m.core.entity.Campaign;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GestureLockFragment extends LockFragment implements
        OnPatternListener, OnClickListener {
    private LockPatternView mLockPatternView;
    private int mCurrentRegisterView = 0;// 1.普通banner的install 2半屏广告的install
    private MobvistaEngine mAdEngine;
    private static String TAG = "GestureLockFragment";
    private boolean DBG = false;
    /* 用于测试时，指定显示的广告形式 */
    private static final int TEST_AD_NUMBER = 5;
    // 普通Banner广告
    private RelativeLayout mNormalBannerAD, mSupermanBannerAD, mSupermanBanner;
    private AlertDialog mHalfScreenDialog;
    // GP包
    public static final String GPPACKAGE = "com.android.vending";

    // 半屏广告

    private RelativeLayout mToShowHalfScreenBanner;
    private ImageView mBannerAnimImage;
    // ----------------------

    private TextView mGestureTip;
    private RelativeLayout mIconLayout;
    private ImageView mAppIcon;
    private ImageView mAppIconTop;
    private ImageView mAppIconBottom;
    private int mBottomIconRes = 0;
    private int mTopIconRes = 0;
    private boolean mAlphaExcuteAnim;
    private boolean mBannerAdExcuteAnim;
    // private ImageView mAdPic;

    private Animation mShake;
    /* 超人banner广告动画 */
    private ObjectAnimator mSupermanAnim;
    private boolean mIsShowAnim/* 是否显示动画 */, mIsLoadAdSuccess/* 是否显示动画 */,
            mIsCamouflageLockSuccess/* 伪装是否解锁成功 */;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_lock_gesture;
    }

    @Override
    protected void onInitUI() {

        InitADUI();

        mLockPatternView = (LockPatternView) findViewById(R.id.gesture_lockview);
        mLockPatternView.setOnPatternListener(this);
        mLockPatternView.setLockMode(mLockMode);
        mLockPatternView.setIsFromLockScreenActivity(true);
        mGestureTip = (TextView) findViewById(R.id.tv_gesture_tip);
        mIconLayout = (RelativeLayout) findViewById(R.id.iv_app_icon_layout);

        if (mLockMode == LockManager.LOCK_MODE_FULL) {
            mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
            mAppIcon.setVisibility(View.VISIBLE);
            LockScreenActivity lsa = (LockScreenActivity) mActivity;
            if (lsa.mQuickLockMode) {
                LockManager lm = LockManager.getInstatnce();
                List<LockMode> modes = lm.getLockMode();
                LockMode targetMode = null;
                for (LockMode lockMode : modes) {
                    if (lockMode.modeId == lsa.mQuiclModeId) {
                        targetMode = lockMode;
                        break;
                    }
                }
                if (targetMode != null) {
                    mAppIcon.setImageDrawable(targetMode.getModeDrawable());
                    // mAppIcon.setImageDrawable(new
                    // BitmapDrawable(getResources(),
                    // targetMode.modeIcon));
                } else {
                    mAppIcon.setImageDrawable(AppUtil.getDrawable(
                            mActivity.getPackageManager(), mActivity.getPackageName()));
                }
            } else if (mPackageName != null) {
                mAppIcon.setImageDrawable(AppUtil.getDrawable(
                        mActivity.getPackageManager(), mPackageName));
            }
            if (needChangeTheme()) {
                mAppIconTop = (ImageView) findViewById(R.id.iv_app_icon_top);
                mAppIconBottom = (ImageView) findViewById(R.id.iv_app_icon_bottom);
                mAppIconTop.setVisibility(View.VISIBLE);
                mAppIconBottom.setVisibility(View.VISIBLE);
                checkApplyTheme();
            }
        }
        loadMobvistaAd();
        LeoEventBus.getDefaultBus().register(this);
    }

    public void onEventMainThread(SubmaineAnimEvent event) {
        String eventMessage = event.eventMsg;
        /* 没有使用了伪装 */
        if ("no_camouflage_lock".equals(eventMessage)) {
            mIsShowAnim = true;
        } else if ("camouflage_lock_success".equals(eventMessage)) {
            mIsCamouflageLockSuccess = true;
            if (mIsLoadAdSuccess) {
                adLoadSucessShowAnim();
            }
        }
    }

    private void InitADUI() {

        mToShowHalfScreenBanner = (RelativeLayout) findViewById(R.id.rl_halfSreenBannerAD);
        mNormalBannerAD = (RelativeLayout) findViewById(R.id.rl_nomalBannerAD);
        mSupermanBannerAD = (RelativeLayout) findViewById(R.id.rl_superman_bannerAD);
        mSupermanBanner = (RelativeLayout) findViewById(R.id.superman_banner);
        mBannerAnimImage = (ImageView) findViewById(R.id.banner_ad_anim_image);
    }

    @Override
    public void onResume() {
        // initShowAd();
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    private void loadMobvistaAd() {
        AppMasterPreference amp = AppMasterPreference.getInstance(mActivity);
        String unitId;
        WindowManager wm = mActivity.getWindowManager();
        int windowH = wm.getDefaultDisplay().getHeight();
        if (!NetWorkUtil.isNetworkAvailable(mActivity) || windowH <= 320) {
            return;
        }
        mAdEngine = MobvistaEngine.getInstance();
        if (DBG) {
            LeoLog.i(TAG, "当前广告形式：" + amp.getADShowType());
            amp.setADShowType(TEST_AD_NUMBER);
        }
        if (amp.getADShowType() == 1) {
            unitId = Constants.UNIT_ID_59;
        } else if (amp.getADShowType() == 2) {
            unitId = Constants.UNIT_ID_60;
        } else if (amp.getADShowType() == 5) {
            unitId = Constants.UNIT_ID_86;
        } else {
            return;
        }

        mAdEngine.loadMobvista(mActivity, unitId, new MobvistaListener() {

            @Override
            public void onMobvistaFinished(int code, final Campaign campaign, String msg) {
                if (code == MobvistaEngine.ERR_OK) {

                    int showType = AppMasterPreference.getInstance(mActivity).getADShowType();
                    // int showType = 5;
                    switch (showType) {
                        case 1:
                            // app图标
                            ImageView icon1 = (ImageView) mNormalBannerAD
                                    .findViewById(R.id.iv_adicon);
                            loadADPic(
                                    campaign.getIconUrl(),
                                    new ImageSize(DipPixelUtil.dip2px(mActivity, 44), DipPixelUtil
                                            .dip2px(mActivity, 44)),
                                    icon1);
                            // app名字
                            TextView appname1 = (TextView) mNormalBannerAD
                                    .findViewById(R.id.tv_appname);
                            appname1.setText(campaign.getAppName());
                            // app描述
                            TextView appdesc1 = (TextView) mNormalBannerAD
                                    .findViewById(R.id.tv_appdesc);
                            appdesc1.setText(campaign.getAppDesc());
                            // appcall
                            Button call1 = (Button) mNormalBannerAD
                                    .findViewById(R.id.iv_ad_app_download);
                            call1.setText(campaign.getAdCall());
                            mAdEngine.registerView(getActivity(), mNormalBannerAD);
                            mCurrentRegisterView = 1;
                            ImageView close1 = (ImageView) mNormalBannerAD
                                    .findViewById(R.id.iv_adclose);
                            close1.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    mNormalBannerAD.setVisibility(View.GONE);
                                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "ad_cli",
                                            "ad_banner_off");
                                }
                            });
                            mNormalBannerAD.setVisibility(View.VISIBLE);
                            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "ad_act", "ad_banner");
                            break;
                        case 2:
                            // icon
                            ImageView icon2 = (ImageView) mToShowHalfScreenBanner
                                    .findViewById(R.id.iv_adicon2);
                            loadADPic(
                                    campaign.getIconUrl(),
                                    new ImageSize(DipPixelUtil.dip2px(mActivity, 44), DipPixelUtil
                                            .dip2px(mActivity, 44)),
                                    icon2);
                            // name
                            TextView appname2 = (TextView) mToShowHalfScreenBanner
                                    .findViewById(R.id.tv_appname2);
                            appname2.setText(campaign.getAppName());
                            // app描述
                            TextView appdesc2 = (TextView) mToShowHalfScreenBanner
                                    .findViewById(R.id.tv_appdesc2);
                            appdesc2.setText(campaign.getAppDesc());
                            // appcall
                            ImageView show2 = (ImageView) mToShowHalfScreenBanner
                                    .findViewById(R.id.iv_adopen);

                            show2.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {

                                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "ad_cli",
                                            "bannerpop");
                                    if (mHalfScreenDialog == null) {
                                        mHalfScreenDialog = new AlertDialog.Builder(mActivity)
                                                .create();
                                    }

                                    mHalfScreenDialog.setCanceledOnTouchOutside(false);
                                    mHalfScreenDialog.show();
                                    mHalfScreenDialog.getWindow().setGravity(Gravity.BOTTOM);

                                    mHalfScreenDialog.getWindow().setLayout(
                                            android.view.WindowManager.LayoutParams.FILL_PARENT,
                                            android.view.WindowManager.LayoutParams.WRAP_CONTENT);
                                    View view = mActivity.getLayoutInflater().inflate(
                                            R.layout.dialog_ad_halfscreen,
                                            null);
                                    ImageView appIcon3 = (ImageView) view
                                            .findViewById(R.id.iv_adicon3);
                                    loadADPic(campaign.getIconUrl(),
                                            new ImageSize(DipPixelUtil.dip2px(mActivity, 48),
                                                    DipPixelUtil.dip2px(mActivity, 48)),
                                            appIcon3);
                                    ImageView bg = (ImageView) view.findViewById(R.id.iv_ADapp_bg);

                                    loadADPic(campaign.getImageUrl(), new ImageSize(bg.getWidth(),
                                            DipPixelUtil.dip2px(mActivity, 178)), bg);
                                    //
                                    TextView appname = (TextView) view
                                            .findViewById(R.id.tv_appname3);
                                    appname.setText(campaign.getAppName());

                                    TextView appdesc = (TextView) view
                                            .findViewById(R.id.tv_appdesc3);
                                    appdesc.setText(campaign.getAppDesc());

                                    View after = view.findViewById(R.id.bt_after);
                                    after.setOnClickListener(new OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            mHalfScreenDialog.dismiss();
                                            mToShowHalfScreenBanner.setVisibility(View.GONE);
                                        }
                                    });
                                    Button install = (Button) view.findViewById(R.id.bt_installapp);
                                    install.setText(campaign.getAdCall());
                                    mAdEngine.registerView(getActivity(), install);
                                    mCurrentRegisterView = 2;
                                    View close = view.findViewById(R.id.iv_adclose);
                                    close.setOnClickListener(new OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            mHalfScreenDialog.dismiss();
                                        }
                                    });
                                    mHalfScreenDialog.getWindow().setContentView(view);

                                }
                            });
                            mToShowHalfScreenBanner.setVisibility(View.VISIBLE);
                            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "ad_act", "ad_bannerpop");
                            break;
                        case 5:
                            mIsLoadAdSuccess = true;
                            // app图标
                            ImageView icon4 = (ImageView) mSupermanBannerAD
                                    .findViewById(R.id.iv_superman_adicon);
                            loadADPic(
                                    campaign.getIconUrl(),
                                    new ImageSize(DipPixelUtil.dip2px(mActivity, 44), DipPixelUtil
                                            .dip2px(mActivity, 44)),
                                    icon4);
                            // app名字
                            TextView appname4 = (TextView) mSupermanBannerAD
                                    .findViewById(R.id.tv_superman_appname);
                            appname4.setText(campaign.getAppName());
                            // app描述
                            TextView appdesc4 = (TextView) mSupermanBannerAD
                                    .findViewById(R.id.tv_superman_ppdesc);
                            appdesc4.setText(campaign.getAppDesc());
                            // appcall
                            Button call4 = (Button) mSupermanBannerAD
                                    .findViewById(R.id.iv_superman_ad_app_download);
                            call4.setText(campaign.getAdCall());
                            mAdEngine.registerView(getActivity(), mSupermanBannerAD);
                            mCurrentRegisterView = 1;
                            RelativeLayout close4 = (RelativeLayout) mSupermanBannerAD
                                    .findViewById(R.id.iv_gesture_superman_adclose_RL);
                            close4.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    mSupermanBannerAD.setVisibility(View.GONE);
                                }
                            });
                            if (mIsShowAnim || mIsCamouflageLockSuccess) {
                                adLoadSucessShowAnim();
                            }
                            break;
                        default:
                            break;

                    }

                }
            }

            @Override
            public void onMobvistaClick(Campaign campaign) {
                mSupermanBannerAD.setVisibility(View.GONE);
                mToShowHalfScreenBanner.setVisibility(View.GONE);
                mNormalBannerAD.setVisibility(View.GONE);

                if (mCurrentRegisterView == 1) {
                    AppMasterPreference.getInstance(mActivity).setAdBannerClickTime(
                            System.currentTimeMillis());
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "ad_cli", "banner");
                }
                if (mCurrentRegisterView == 2) {
                    if (mHalfScreenDialog != null) {
                        if (mHalfScreenDialog.isShowing()) {
                            mHalfScreenDialog.dismiss();
                        }
                    }
                    AppMasterPreference.getInstance(mActivity).setHalfScreenBannerClickTime(
                            System.currentTimeMillis());
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "ad_cli", "pop_gp");
                }
            }
        });
    }

    /* 广告加载成功显示动画 */
    private void adLoadSucessShowAnim() {
        mSupermanBanner.setVisibility(View.VISIBLE);
        AnimationDrawable anim = (AnimationDrawable) mBannerAnimImage
                .getDrawable();
        if (anim != null) {
            anim.stop();
            mBannerAnimImage.setImageDrawable(null);
            // mBannerAnimImage.setImageResource(R.drawable.lock_banner_ad_anim);
            mBannerAnimImage.setImageDrawable(anim);
            anim.start();
            Handler handler = ThreadManager.getUiThreadHandler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    adSuccessSupermanAnim(mBannerAnimImage, mSupermanBannerAD);
                }
            }, 1280);
        }
    }

    @Override
    public void onDestroy() {
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroy();
        MobvistaEngine.getInstance().release(getActivity());
        if (mSupermanAnim != null) {
            mSupermanAnim = null;
        }
    }

    @Override
    public void onNewIntent() {
        LockScreenActivity lsa = (LockScreenActivity) mActivity;
        if (lsa.mQuickLockMode) {
            LockManager lm = LockManager.getInstatnce();
            List<LockMode> modes = lm.getLockMode();
            LockMode targetMode = null;
            for (LockMode lockMode : modes) {
                if (lockMode.modeId == lsa.mQuiclModeId) {
                    targetMode = lockMode;
                    break;
                }
            }
            if (targetMode != null) {
                // mAppIcon.setImageDrawable(new BitmapDrawable(getResources(),
                // targetMode.modeIcon));
                mAppIcon.setImageDrawable(targetMode.getModeDrawable());
            } else {
                mAppIcon.setImageDrawable(AppUtil.getDrawable(
                        mActivity.getPackageManager(), mActivity.getPackageName()));
            }
        } else {
            if (!TextUtils.isEmpty(mPackageName)) {
                mAppIcon.setImageDrawable(AppUtil.getDrawable(
                        mActivity.getPackageManager(), mPackageName));
            } else {
                mAppIcon.setImageDrawable(AppUtil.getDrawable(
                        mActivity.getPackageManager(), mActivity.getPackageName()));
            }
        }
    }

    private void checkApplyTheme() {
        String pkgName = AppMasterApplication.getSelectedTheme();
        Context themeContext = LeoResources.getThemeContext(getActivity(), pkgName);// com.leo.appmaster:drawable/multi_theme_lock_bg
        Resources themeRes = themeContext.getResources();
        int layoutBgRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                ResourceName.THEME_GESTRUE_BG);
        if (layoutBgRes <= 0) {
            layoutBgRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                    ResourceName.THEME_GENERAL_BG);
        }

        if (themeRes != null) {
            if (layoutBgRes > 0) {
                RelativeLayout layout = (RelativeLayout) getActivity().findViewById(
                        R.id.activity_lock_layout);
                Drawable bgDrawable = themeRes.getDrawable(layoutBgRes);
                layout.setBackgroundDrawable(bgDrawable);
            }
        }

        mTopIconRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                ResourceName.THEME_TOP_ICON);
        mBottomIconRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                ResourceName.THEME_BOTTOM_ICON);
        if (themeRes != null) {
            if (mTopIconRes > 0) {
                mAppIconTop.setBackgroundDrawable(themeRes.getDrawable(mTopIconRes));
            }
            if (mBottomIconRes > 0) {
                mAppIconBottom.setBackgroundDrawable(themeRes.getDrawable(mBottomIconRes));
            }
        }
    }

    @Override
    public void onDestroyView() {
        mLockPatternView.cleangifResource();
        super.onDestroyView();
    }

    @Override
    public void onPatternStart() {

    }

    @Override
    public void onPatternCleared() {

    }

    @Override
    public void onPatternCellAdded(List<Cell> pattern) {

    }

    @Override
    public void onPatternDetected(List<Cell> pattern) {
        final String gesture = LockPatternUtils.patternToString(pattern);
        mLockPatternView.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkGesture(gesture);
            }
        }, 200);

    }

    private void checkGesture(String gesture) {
        mInputCount++;
        AppMasterPreference pref = AppMasterPreference.getInstance(mActivity);
        String savedGesture = pref.getGesture();
        if (savedGesture != null && savedGesture.equals(gesture)) {
            ((LockScreenActivity) mActivity).onUnlockSucceed();
        } else {
            if (mInputCount >= mMaxInput) {
                ((LockScreenActivity) mActivity).onUnlockOutcount();
                mGestureTip.setText(R.string.please_input_gesture);
                mInputCount = 0;
            } else {
                mGestureTip.setText(String.format(
                        mActivity.getString(R.string.input_error_tip),
                        mInputCount + "", (mMaxInput - mInputCount) + ""));
            }
            mLockPatternView.clearPattern();
            shakeIcon();
        }
    }

    private boolean needChangeTheme() {
        return ThemeUtils.checkThemeNeed(getActivity())
                && (mLockMode == LockManager.LOCK_MODE_FULL);
    }

    private void shakeIcon() {
        if (mShake == null) {
            mShake = AnimationUtils.loadAnimation(mActivity,
                    R.anim.left_right_shake);
        }
        mIconLayout.startAnimation(mShake);
    }

    @Override
    public void onLockPackageChanged(String lockedPackage) {
        if (!TextUtils.equals(lockedPackage, mPackageName) && !TextUtils.isEmpty(lockedPackage)) {
            mPackageName = lockedPackage;
            mInputCount = 0;
            mGestureTip.setText(R.string.please_input_gesture);
            if (mAppIcon == null) {
                mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
            }
            mAppIcon.setImageDrawable(AppUtil.getDrawable(
                    mActivity.getPackageManager(), mPackageName));
        }

        mAppIcon.setVisibility(View.VISIBLE);
    }

    public void reInvalideGestureView() {
        mLockPatternView.resetIfHideLine();
    }

    /* banner广告拉去成功超人动画 */
    private void adSuccessSupermanAnim(final ImageView imageView, final View view) {
        if (mSupermanAnim != null) {
            mSupermanAnim.cancel();
        }
        imageView.setImageResource(R.drawable.superman_success);
        if (mSupermanAnim == null) {
            mSupermanAnim = ObjectAnimator.ofFloat(imageView, "translationY", 60, -1148);
        }
        mSupermanAnim.setDuration(1120);
        mSupermanAnim.setRepeatCount(0);
        mSupermanAnim.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                long currentTime = arg0.getCurrentPlayTime();
                if (currentTime >= 60 && !mBannerAdExcuteAnim) {
                    bannerAdInAnim(view);
                    mBannerAdExcuteAnim = true;
                }
                if (currentTime >= 920 && !mAlphaExcuteAnim) {
                    ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(imageView, "alpha", 1.0f, 0f);
                    alphaAnim.setDuration(200);
                    alphaAnim.start();
                    mAlphaExcuteAnim = true;
                }
            }
        });
        mSupermanAnim.start();
    }

    /* banner广告进入动画 */
    private void bannerAdInAnim(View view) {
        ObjectAnimator bannerAnim = new ObjectAnimator();
        bannerAnim.setPropertyName("translationY");
        bannerAnim.setTarget(view);
        bannerAnim.setFloatValues(400, 0);
        bannerAnim.setDuration(1280);
        bannerAnim.setRepeatCount(0);
        bannerAnim.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                mSupermanBannerAD.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {

            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
        bannerAnim.start();
    }

    @Override
    public void onClick(View arg0) {
        // TODO
    }
}
