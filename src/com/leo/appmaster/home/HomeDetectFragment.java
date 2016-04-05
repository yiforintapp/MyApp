package com.leo.appmaster.home;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.imagehide.NewHideImageActivity;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.LostSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.phoneSecurity.PhoneSecurityActivity;
import com.leo.appmaster.privacy.Privacy;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;
import com.leo.appmaster.videohide.NewHideVidActivity;
import com.leo.appmaster.videohide.VideoHideMainActivity;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;

import java.util.Locale;

public class HomeDetectFragment extends Fragment implements View.OnClickListener {
    private static final int SAFT_LEVEL = 0;
    private static final int DANGER_LEVEL = 1;
    private static final long FIR_IN_ANIM_TIME = 700;

    // 是否显示防盗
    private static boolean sShowLost = false;

    private Activity mContext;
    private RelativeLayout mSfatResultAppLt;
    private RelativeLayout mSfatResultImgLt;
    private RelativeLayout mSfatResultVideoLt;
    private RelativeLayout mDangerResultAppLt;
    private RelativeLayout mDangerResultImgLt;
    private RelativeLayout mDangerResultVideoLt;

    // 中间banner
    private RelativeLayout mCenterTipRt;
    private TextView mBannerTv;

    private ImageView mShieldTopIv;
    private ImageView mShieldRightIv;
    private ImageView mShieldLeftIv;
    private ImageView mShieldCenterIv;
    private AnimatorSet mFirstInAnim;
    //应用检测结果view
    private TextView mDetSaftAppNumTv;
    private TextView mDetSaftAppTv;
    private TextView mDetDagAppNumTv;
    private TextView mDetDagAppTv;
    //图片检测结果view
    private TextView mDetSaftImgNumTv;
    private TextView mDetSaftImgTv;
    private TextView mDetDagImgNumTv;
    private TextView mDetDagImgTv;
    //视频检测结果view
    private TextView mDetSaftVideoNumTv;
    private TextView mDetSaftVideoTv;
    private TextView mDetDagVideoNumTv;
    private TextView mDetDagVideoTv;
    private HomeDetectPresenter mDetectPresenter;

    private int mScreenWidth;
    private Banner mBanner;
    private Intent mBannerIntent;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mDetectPresenter = new HomeDetectPresenter();
        mScreenWidth = Utilities.getScreenSize(mContext)[0];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_detect, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDetectPresenter.attachView(this);
        initUI(view);
        initAnim();
        //测试
        startFirstAnim();
        startHomeCenterShieldAnim();

    }

    @Override
    public void onResume() {
        super.onResume();

        // 刷新状态
        reloadAppStatus();
        reloadImageStatus();
        reloadVideoStatus();

        // 初始化中间的banner
        initBannerTip();
    }

    @Override
    public void onStop() {
        super.onStop();

        sShowLost = !sShowLost;
    }

    private void initBannerTip() {
        Privacy appPrivacy = PrivacyHelper.getAppPrivacy();
        Privacy imagePrivacy = PrivacyHelper.getImagePrivacy();
        Privacy videoPrivacy = PrivacyHelper.getVideoPrivacy();
        if (appPrivacy.isDangerous() || imagePrivacy.isDangerous() || videoPrivacy.isDangerous()) {
            // 不显示中间banner
            mCenterTipRt.setVisibility(View.INVISIBLE);
        } else {
            LostSecurityManager lsm = (LostSecurityManager) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            boolean lostDisabled = !lsm.isUsePhoneSecurity();

            LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            boolean usageDisabled = !lm.isUsageStateEnable();
            if (lostDisabled && usageDisabled) {
                mCenterTipRt.setVisibility(View.VISIBLE);
                if (sShowLost) {
                    mBannerTv.setText(R.string.hd_lost_permisson_tip);
                    mBannerIntent = new Intent(mContext, PhoneSecurityActivity.class);
                } else {
                    mBannerTv.setText(R.string.hd_lock_permisson_tip);
                    mBannerIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    mBannerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            } else if (lostDisabled) {
                mBannerIntent = new Intent(mContext, PhoneSecurityActivity.class);
                mCenterTipRt.setVisibility(View.VISIBLE);
                mBannerTv.setText(R.string.hd_lost_permisson_tip);
            } else if (usageDisabled) {
                mCenterTipRt.setVisibility(View.VISIBLE);
                mBannerTv.setText(R.string.hd_lock_permisson_tip);
                mBannerIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                mBannerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                mCenterTipRt.setVisibility(View.INVISIBLE);
                mBannerIntent = null;
            }
        }
    }

    private void reloadAppStatus() {
        Privacy privacy = PrivacyHelper.getAppPrivacy();
        if (privacy.isDangerous()) {
            mSfatResultAppLt.setVisibility(View.INVISIBLE);
            mDangerResultAppLt.setVisibility(View.VISIBLE);

            mDetDagAppTv.setText(privacy.getPrivacyTitleId());
            mDetDagAppNumTv.setText(privacy.getPrivacyCountText());
        } else {
            mSfatResultAppLt.setVisibility(View.VISIBLE);
            mDangerResultAppLt.setVisibility(View.INVISIBLE);

            mDetSaftAppTv.setText(privacy.getPrivacyTitleId());
            boolean numVisible = privacy.showPrivacyCount();
            mDetSaftAppNumTv.setVisibility(numVisible ? View.VISIBLE : View.INVISIBLE);
            if (numVisible) {
                mDetSaftAppNumTv.setText(privacy.getPrivacyCountText());
            }
        }
    }

    private void reloadImageStatus() {
        Privacy privacy = PrivacyHelper.getImagePrivacy();
        if (privacy.isDangerous()) {
            mSfatResultImgLt.setVisibility(View.INVISIBLE);
            mDangerResultImgLt.setVisibility(View.VISIBLE);

            mDetDagImgNumTv.setText(privacy.getPrivacyCountText());
            mDetDagImgTv.setText(privacy.getPrivacyTitleId());
        } else {
            mSfatResultImgLt.setVisibility(View.VISIBLE);
            mDangerResultImgLt.setVisibility(View.INVISIBLE);

            mDetSaftImgTv.setText(privacy.getPrivacyTitleId());
            boolean numVisible = privacy.showPrivacyCount();
            mDetSaftImgNumTv.setVisibility(numVisible ? View.VISIBLE : View.INVISIBLE);
            if (numVisible) {
                mDetSaftImgNumTv.setText(privacy.getPrivacyCountText());
            }
        }
    }

    private void reloadVideoStatus() {
        Privacy privacy = PrivacyHelper.getVideoPrivacy();
        if (privacy.isDangerous()) {
            mSfatResultVideoLt.setVisibility(View.INVISIBLE);
            mDangerResultVideoLt.setVisibility(View.VISIBLE);

            mDetDagVideoNumTv.setText(privacy.getPrivacyCountText());
            mDetDagVideoTv.setText(privacy.getPrivacyTitleId());
        } else {
            mSfatResultVideoLt.setVisibility(View.VISIBLE);
            mDangerResultVideoLt.setVisibility(View.INVISIBLE);

            mDetSaftVideoTv.setText(privacy.getPrivacyTitleId());
            boolean numVisible = privacy.showPrivacyCount();
            mDetSaftVideoNumTv.setVisibility(numVisible ? View.VISIBLE : View.INVISIBLE);
            if (numVisible) {
                mDetSaftVideoNumTv.setText(privacy.getPrivacyCountText());
            }
        }
    }

    private void initUI(View view) {
        RelativeLayout resultRootView = (RelativeLayout) view.findViewById(R.id.det_result_ly);

        mSfatResultAppLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_saft_result_app);
        mSfatResultImgLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_saft_result_img);
        mSfatResultVideoLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_saft_result_video);

        mDangerResultAppLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_danger_result_app);
        mDangerResultImgLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_danger_result_img);
        mDangerResultVideoLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_danger_result_video);

       /* mSfatResultAppLt.setVisibility(View.INVISIBLE);
        mSfatResultImgLt.setVisibility(View.INVISIBLE);
        mSfatResultVideoLt.setVisibility(View.INVISIBLE);

        mDangerResultAppLt.setVisibility(View.VISIBLE);
        mDangerResultImgLt.setVisibility(View.VISIBLE);
        mDangerResultVideoLt.setVisibility(View.VISIBLE);*/


        mCenterTipRt = (RelativeLayout) view.findViewById(R.id.lt_home_det_tip);
        mSfatResultAppLt.setOnClickListener(this);
        mSfatResultImgLt.setOnClickListener(this);
        mSfatResultVideoLt.setOnClickListener(this);

        mDangerResultAppLt.setOnClickListener(this);
        mDangerResultImgLt.setOnClickListener(this);
        mDangerResultVideoLt.setOnClickListener(this);

        mCenterTipRt.setOnClickListener(this);
        mShieldLeftIv = (ImageView) view.findViewById(R.id.shield_left_iv);
        mShieldRightIv = (ImageView) view.findViewById(R.id.shield_right_iv);
        mShieldTopIv = (ImageView) view.findViewById(R.id.shield_top_iv);
        mShieldCenterIv = (ImageView) view.findViewById(R.id.shield_center_iv);
        //初始化应用检测结果
        mDetSaftAppNumTv = (TextView) resultRootView.findViewById(R.id.det_saft_app_num_tv);
        mDetSaftAppTv = (TextView) resultRootView.findViewById(R.id.det_saft_app_tv);
        mDetDagAppNumTv = (TextView) resultRootView.findViewById(R.id.det_danger_app_num_tv);
        mDetDagAppTv = (TextView) resultRootView.findViewById(R.id.det_danger_app_tv);
        //初始化图片检测结果
        mDetSaftImgNumTv = (TextView) resultRootView.findViewById(R.id.det_saft_img_num_tv);
        mDetSaftImgTv = (TextView) resultRootView.findViewById(R.id.det_saft_img_tv);
        mDetDagImgNumTv = (TextView) resultRootView.findViewById(R.id.det_danger_img_num_tv);
        mDetDagImgTv = (TextView) resultRootView.findViewById(R.id.det_danger_img_tv);
        //初始化图片检测结果
        mDetSaftVideoNumTv = (TextView) resultRootView.findViewById(R.id.det_saft_video_num_tv);
        mDetSaftVideoTv = (TextView) resultRootView.findViewById(R.id.det_saft_video_tv);
        mDetDagVideoNumTv = (TextView) resultRootView.findViewById(R.id.det_danger_video_num_tv);
        mDetDagVideoTv = (TextView) resultRootView.findViewById(R.id.det_danger_video_tv);

        mBannerTv = (TextView) view.findViewById(R.id.det_tip_txt_tv);

        setSfateShieldView();
    }


    private void initAnim() {
        initHomeTopShieldAnim();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDetectPresenter.detachView();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lt_det_saft_result_app:
                //应用扫描安全结果
                //mDetectPresenter.appSaftHandler();
                //break;
            case R.id.lt_det_danger_result_app:
                //应用扫描危险结果
                //mDetectPresenter.appDangerHandler();
                Intent appIntent = new Intent(mContext, AppLockListActivity.class);
                Privacy privacy = PrivacyHelper.getAppPrivacy();
                if (privacy.getNewCount() > 0) {
                    appIntent.putExtra(Constants.FROM_APP_SCAN_RESULT, true);
                }
                mContext.startActivity(appIntent);
                LeoSettings.setBoolean(PrefConst.KEY_APP_COMSUMED, true);
                break;
            case R.id.lt_det_saft_result_img:
                //图片扫描安全结果
                //mDetectPresenter.imageSaftHandler();
                //break;
            case R.id.lt_det_danger_result_img:
                //图片扫描危险结果
                //mDetectPresenter.imageDangerHandler();
                Intent imageIntent = null;
                privacy = PrivacyHelper.getImagePrivacy();
                if (privacy.getNewCount() > 0) {
                    imageIntent = new Intent(mContext, NewHideImageActivity.class);
                } else {
                    imageIntent = new Intent(mContext, ImageHideMainActivity.class);
                }
                mContext.startActivity(imageIntent);
                LeoSettings.setBoolean(PrefConst.KEY_PIC_COMSUMED, true);
                break;
            case R.id.lt_det_saft_result_video:
                //视频扫描安全结果
                //mDetectPresenter.videoSaftHandler();
                //break;
            case R.id.lt_det_danger_result_video:
                //视频扫描危险结果
                //mDetectPresenter.videoDangerHandler();
                Intent intent = null;
                privacy = PrivacyHelper.getVideoPrivacy();
                if (privacy.getNewCount() > 0) {
                    intent = new Intent(mContext, VideoHideMainActivity.class);
                } else {
                    intent = new Intent(mContext, NewHideVidActivity.class);
                }
                mContext.startActivity(intent);
                LeoSettings.setBoolean(PrefConst.KEY_VID_COMSUMED, true);
                break;
            case R.id.lt_home_det_tip:
                //中间banner
                //mDetectPresenter.centerBannerHandler();
                if (mBannerIntent != null) {
                    mContext.startActivity(mBannerIntent);
                    if (!mContext.getPackageName().equals(mBannerIntent.getPackage())) {
                        LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                        lm.filterPackage(mBannerIntent.getPackage(), false);
                        lm.filterSelfOneMinites();
                    }
                }
                break;
            default:
                break;
        }
    }

    //盾牌位置初始化
    private void shieldPositionInit(int level) {
        Context context = getActivity();
        mShieldTopIv.setTranslationY(-DipPixelUtil.dip2px(context, getResources().getInteger(R.integer.shield_top_offset)));
        mShieldLeftIv.setTranslationX(-DipPixelUtil.dip2px(context, getResources().getInteger(R.integer.shield_left_offset)));
        mShieldRightIv.setTranslationX(DipPixelUtil.dip2px(context, getResources().getInteger(R.integer.shield_right_offset)));
        if (level == SAFT_LEVEL) {
            mShieldCenterIv.setTranslationX(DipPixelUtil.dip2px(context, getResources().getInteger(R.integer.shield_center_blue_offset)));
        } else if (level == DANGER_LEVEL) {
            mShieldCenterIv.setTranslationX(DipPixelUtil.dip2px(context, getResources().getInteger(R.integer.shield_center_red_offset)));
        }

    }

    //危险盾牌设置
    public void setDangerShieldView() {
        shieldPositionInit(DANGER_LEVEL);
        mShieldTopIv.setImageResource(R.drawable.shield_red_top);
        mShieldLeftIv.setImageResource(R.drawable.shield_red_left);
        mShieldRightIv.setImageResource(R.drawable.shield_red_right);
        mShieldCenterIv.setImageResource(R.drawable.shield_bad);
    }

    //安全盾牌设置
    public void setSfateShieldView() {
        shieldPositionInit(SAFT_LEVEL);
        mShieldTopIv.setImageResource(R.drawable.shield_blue_top);
        mShieldLeftIv.setImageResource(R.drawable.shield_blue_left);
        mShieldRightIv.setImageResource(R.drawable.shield_blue_right);
        mShieldCenterIv.setImageResource(R.drawable.shield_good);
    }

    //首次进入主页盾牌动画
    public void initHomeTopShieldAnim() {
        int value2 = -DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_top_offset));
        int value1 = -DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_top_y_trans)) + value2;
        PropertyValuesHolder topTransY = PropertyValuesHolder.ofFloat("translationY", value1, value2);
        PropertyValuesHolder topAlpha = PropertyValuesHolder.ofFloat("alpha", 0, 1);
        ObjectAnimator topAnim = ObjectAnimator.ofPropertyValuesHolder(mShieldTopIv, topTransY, topAlpha);

        int valueLeft2 = -DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_left_offset));
        int valueLeft1 = -DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_left_x_trans)) + value2;
        PropertyValuesHolder leftTransY = PropertyValuesHolder.ofFloat("translationX", valueLeft1, valueLeft2);
        PropertyValuesHolder leftAlpha = PropertyValuesHolder.ofFloat("alpha", 0, 1);
        ObjectAnimator leftAnim = ObjectAnimator.ofPropertyValuesHolder(mShieldLeftIv, leftTransY, leftAlpha);

        int valueRight2 = DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_left_offset));
        int valueRight1 = DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_right_x_trans)) + value2;

        PropertyValuesHolder rightTransY = PropertyValuesHolder.ofFloat("translationX", valueRight1, valueRight2);
        PropertyValuesHolder rightAlpha = PropertyValuesHolder.ofFloat("alpha", 0, 1);
        ObjectAnimator rightAnim = ObjectAnimator.ofPropertyValuesHolder(mShieldRightIv, rightTransY, rightAlpha);

        if (mFirstInAnim == null) {
            mFirstInAnim = new AnimatorSet();
        } else {
            mFirstInAnim.cancel();
        }
        mFirstInAnim.playTogether(topAnim, leftAnim, rightAnim);
        mFirstInAnim.setDuration(FIR_IN_ANIM_TIME);

    }

    //启动首次进入动画
    public void startFirstAnim() {
        if (mFirstInAnim == null) {
            return;
        }
        mFirstInAnim.start();
    }

    //结束首次动画
    public void cancelFirstAnim() {
        if (mFirstInAnim == null) {
            return;
        }
        mFirstInAnim.cancel();
    }

    //首次进入主页Center盾牌动画
    public void startHomeCenterShieldAnim() {
//        PropertyValuesHolder centerScaleX = PropertyValuesHolder.ofFloat("scaleX", (float) 0.6, (float) 1.06);
//        PropertyValuesHolder centerScaleY = PropertyValuesHolder.ofFloat("scaleY", (float) 0.6, (float) 1.06);
//
//        ObjectAnimator centerAnim = ObjectAnimator.ofPropertyValuesHolder(mShieldCenterIv, centerScaleX, centerScaleY);
//        centerAnim.setDuration(FIR_IN_ANIM_TIME);
//        centerAnim.setStartDelay(FIR_IN_ANIM_TIME);
//        centerAnim.start();
        ObjectAnimator scaleXFirstAnim = ObjectAnimator.ofFloat(mShieldCenterIv, "scaleX", 0.6f, 1.06f);
        ObjectAnimator scaleYFirstAnim = ObjectAnimator.ofFloat(mShieldCenterIv, "scaleY", 0.6f, 1.06f);
        AnimatorSet scaleFirstAnimatorSet = new AnimatorSet();
        scaleFirstAnimatorSet.playTogether(scaleXFirstAnim, scaleYFirstAnim);
        scaleFirstAnimatorSet.setDuration(200);

        ObjectAnimator scaleXEndAnim = ObjectAnimator.ofFloat(mShieldCenterIv, "scaleX", 1.06f, 1f);
        ObjectAnimator scaleYEndAnim = ObjectAnimator.ofFloat(mShieldCenterIv, "scaleY", 1.06f, 1f);
        AnimatorSet scaleEndAnimatorSet = new AnimatorSet();
        scaleEndAnimatorSet.playTogether(scaleXEndAnim, scaleYEndAnim);
        scaleEndAnimatorSet.setDuration(80);

        ObjectAnimator alphaAnim =ObjectAnimator.ofFloat(mShieldCenterIv, "alpha", 0f, 1f);
        alphaAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mShieldCenterIv.setVisibility(View.VISIBLE);
            }
        });
        alphaAnim.setDuration(120);

        AnimatorSet scaleAnimatorSet = new AnimatorSet();
        scaleAnimatorSet.playSequentially(scaleFirstAnimatorSet, scaleEndAnimatorSet);

        AnimatorSet centerAnimatorSet = new AnimatorSet();
        centerAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startScanResultAnim();
            }
        });
        centerAnimatorSet.playTogether(scaleAnimatorSet, alphaAnim);
        centerAnimatorSet.setStartDelay(500);
        centerAnimatorSet.start();

    }

    private void startScanResultAnim() {
        AnimatorSet appAnimatorSet;
        AnimatorSet picAnimatorSet;
        AnimatorSet vidAnimatorSet;
        if (PrivacyHelper.getAppPrivacy().isDangerous()) {
            appAnimatorSet = getTranslateAnim(mDangerResultAppLt);
        } else {
            appAnimatorSet = getTranslateAnim(mSfatResultAppLt);
        }
        if (PrivacyHelper.getImagePrivacy().isDangerous()) {
            picAnimatorSet = getTranslateAnim(mDangerResultImgLt);
        } else {
            picAnimatorSet = getTranslateAnim(mSfatResultImgLt);
        }
        if (PrivacyHelper.getVideoPrivacy().isDangerous()) {
            vidAnimatorSet = getTranslateAnim(mDangerResultVideoLt);
        } else {
            vidAnimatorSet = getTranslateAnim(mSfatResultVideoLt);
        }
        ObjectAnimator tipsAnim = ObjectAnimator.ofFloat(mCenterTipRt, "x", -mScreenWidth, mCenterTipRt.getTranslationX());
        tipsAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mCenterTipRt.setVisibility(View.VISIBLE);
            }
        });
        tipsAnim.setDuration(400);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(picAnimatorSet).after(280).after(appAnimatorSet);
        animatorSet.play(vidAnimatorSet).after(280).after(picAnimatorSet);
        animatorSet.play(tipsAnim).after(1000).after(vidAnimatorSet);
        animatorSet.start();

    }

    /** 扫描结果位移动画 */
    private AnimatorSet getTranslateAnim(final View view) {
        ObjectAnimator translateAnim = ObjectAnimator.ofFloat(
                view, "x", -mScreenWidth, view.getTranslationX());
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        alphaAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setVisibility(View.VISIBLE);
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(translateAnim, alphaAnim);
        animatorSet.setDuration(400);

        return animatorSet;
    }

    //扫描结果处理后切换动画
    public void detectResultConversionAnim(View up1, View up2, View down1, View down2, Animator.AnimatorListener listener) {

        ObjectAnimator transUp1 = ObjectAnimator.ofFloat(up1, "translationY", -100, 0);
        ObjectAnimator transUp2 = ObjectAnimator.ofFloat(up2, "translationY", -100, 0);

        ObjectAnimator transDown1 = ObjectAnimator.ofFloat(up1, "translationY", 0, 100);
        ObjectAnimator transDown2 = ObjectAnimator.ofFloat(up2, "translationY", 0, 100);

        AnimatorSet set = new AnimatorSet();
        if (listener != null) {
            set.addListener(listener);
        }
        set.playTogether(transUp1, transUp2, transDown1, transDown2);
        set.setDuration(200);
        set.start();
    }

    private class Banner {
        private Intent intent;
        public Banner(Intent intent) {
            this.intent = intent;
        }

        public void click() {
            mContext.startActivity(intent);
        }
    }
}
