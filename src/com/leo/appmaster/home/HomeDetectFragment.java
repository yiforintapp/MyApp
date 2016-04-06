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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.imagehide.NewHideImageActivity;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.LostSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.phoneSecurity.PhoneSecurityActivity;
import com.leo.appmaster.phoneSecurity.PhoneSecurityGuideActivity;
import com.leo.appmaster.privacy.Privacy;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;
import com.leo.appmaster.videohide.NewHideVidActivity;
import com.leo.appmaster.videohide.VideoHideMainActivity;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;

public class HomeDetectFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "HomeDetectFragment";
    private static final int SAFT_LEVEL = 0;
    private static final int DANGER_LEVEL = 1;
    private static final long FIR_IN_ANIM_TIME = 700;

    private static final int TYPE_LOST = 0;
    private static final int TYPE_LOCK = 1;
    private static final int TYPE_NONE = -1;

    // 是否显示防盗
    private static boolean sShowLost = false;

    private int mCurrentType = TYPE_NONE;

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
    private ImageView mShieldDangerLeftIv;
    private ImageView mShieldDangerRightIv;
    private ImageView mShieldDangerTopIv;
    private ImageView mShieldDangerCenterIv;
    private TextView mDangerResultAppDetailTv;
    private TextView mDangerResultPicDetailTv;
    private TextView mDangerResultVideoDetailTv;
    private TextView mDangerDetTip;
    private int mPrivacyConut;

    private RelativeLayout mAppSafeContent;
    private RelativeLayout mAppDangerContent;
    private RelativeLayout mPicSafeContent;
    private RelativeLayout mPicDangerContent;
    private RelativeLayout mVidSafeContent;
    private RelativeLayout mVidDangerContent;
    private int mLastPrivacyConut;
    private boolean mPrivacyApp;
    private boolean mPrivacyPic;
    private boolean mPrivacyVideo;
    private boolean mLastPrivacyPic;
    private boolean mLastPrivacyApp;
    private boolean mLastPrivacyVideo;

    private boolean mFromEnter;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        LeoLog.d(TAG, "onResume...");

        // 刷新状态
        refreshDetectStatus();
        // 初始化中间的banner
        initBannerTip();

        if (mFromEnter) {
            initAnim();
        }
    }

    private void refreshDetectStatus() {
//        初始化标志状态
        mPrivacyConut = 0;
        mPrivacyApp = false;
        mPrivacyPic = false;
        mPrivacyVideo = false;

        reloadAppStatus();
        reloadImageStatus();
        reloadVideoStatus();

        if (!mPrivacyApp && !mPrivacyPic && !mPrivacyVideo) {
            setSfateShieldView(mFromEnter);
        } else {
            setDangerShieldView(true, mFromEnter);
        }
        if (mPrivacyConut > 0) {
            mDangerDetTip.setVisibility(View.VISIBLE);
            String str = this.getResources().getString(R.string.hd_privacy_danger);
            String format = String.format(str, mPrivacyConut);
            mDangerDetTip.setText(format);
        } else {
            mDangerDetTip.setVisibility(View.INVISIBLE);
        }

//        LeoLog.d("refreshDetectStatus","mLastPrivacyConut:"+mLastPrivacyConut);
//        LeoLog.d("refreshDetectStatus","mLastPrivacyApp:"+mLastPrivacyApp);
//        LeoLog.d("refreshDetectStatus","mLastPrivacyPic:"+mLastPrivacyPic);
//        LeoLog.d("refreshDetectStatus","mLastPrivacyVideo:"+mLastPrivacyVideo);
        if (mLastPrivacyApp && !mPrivacyApp) {
            LeoLog.d("refreshDetectStatus", "privacy app conver anim!");
            detectResultConversionAnim(mAppSafeContent, mAppDangerContent,
                    mSfatResultAppLt, mDangerResultAppLt, mDetectShieldConverAnimListener);
        }
        if (mLastPrivacyPic && !mPrivacyPic) {
            LeoLog.d("refreshDetectStatus", "privacy app conver anim!");
            detectResultConversionAnim(mPicSafeContent, mPicDangerContent,
                    mSfatResultImgLt, mDangerResultImgLt, mDetectShieldConverAnimListener);
        }
        if (mLastPrivacyVideo && !mPrivacyVideo) {
            LeoLog.d("refreshDetectStatus", "privacy app conver anim!");
            detectResultConversionAnim(mVidSafeContent, mVidDangerContent,
                    mSfatResultVideoLt, mDangerResultVideoLt, mDetectShieldConverAnimListener);
        }
    }

    Animator.AnimatorListener mDetectShieldConverAnimListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mLastPrivacyConut > 0 && mPrivacyConut<=0) {
                LeoLog.d("refreshDetectStatus","privacy app conver anim!");
                dangerShieldConverAnim();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };


    @Override
    public void onPause() {
        super.onPause();
        mLastPrivacyConut = 0;
        mLastPrivacyApp = false;
        mLastPrivacyPic = false;
        mLastPrivacyVideo = false;

        mLastPrivacyConut = mPrivacyConut;
        mLastPrivacyApp = mPrivacyApp;
        mLastPrivacyPic = mPrivacyPic;
        mLastPrivacyVideo = mPrivacyVideo;
    }

    private void initBannerTip() {
        LostSecurityManager lsm = (LostSecurityManager) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        boolean lostDisabled = !lsm.isUsePhoneSecurity();
        boolean usageDisabled = !lm.isUsageStateEnable();

        int lastType = mCurrentType;
        if ((lastType == TYPE_LOCK && !usageDisabled) || (lastType == TYPE_LOST && !lostDisabled)) {
            // 之前显示的，返回后已开启，则不显示
            mCenterTipRt.setVisibility(View.INVISIBLE);
            mCurrentType = TYPE_NONE;
            return;
        }
        Privacy appPrivacy = PrivacyHelper.getAppPrivacy();
        Privacy imagePrivacy = PrivacyHelper.getImagePrivacy();
        Privacy videoPrivacy = PrivacyHelper.getVideoPrivacy();
        if (appPrivacy.isDangerous() || imagePrivacy.isDangerous() || videoPrivacy.isDangerous()) {
            // 不显示中间banner
            mCenterTipRt.setVisibility(View.INVISIBLE);
            mCurrentType = TYPE_NONE;
        } else {
            if (lostDisabled && usageDisabled) {
                if (mFromEnter) {
                    mCenterTipRt.setVisibility(View.INVISIBLE);
                } else {
                    mCenterTipRt.setVisibility(View.VISIBLE);
                }
                if (sShowLost) {
                    mBannerTv.setText(R.string.hd_lost_permisson_tip);
                    mBannerIntent = new Intent(mContext, PhoneSecurityActivity.class);
                    mCurrentType = TYPE_LOST;
                } else {
                    mBannerTv.setText(R.string.hd_lock_permisson_tip);
                    mBannerIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    mBannerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mCurrentType = TYPE_LOCK;
                }
            } else if (lostDisabled) {
                mBannerIntent = new Intent(mContext, PhoneSecurityGuideActivity.class);
                if (mFromEnter) {
                    mCenterTipRt.setVisibility(View.INVISIBLE);
                } else {
                    mCenterTipRt.setVisibility(View.VISIBLE);
                }
                mBannerTv.setText(R.string.hd_lost_permisson_tip);

                mCurrentType = TYPE_LOST;
            } else if (usageDisabled) {
                if (mFromEnter) {
                    mCenterTipRt.setVisibility(View.INVISIBLE);
                } else {
                    mCenterTipRt.setVisibility(View.VISIBLE);
                }
                mBannerTv.setText(R.string.hd_lock_permisson_tip);
                mBannerIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                mBannerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                mCurrentType = TYPE_LOCK;
            } else {
                mCenterTipRt.setVisibility(View.INVISIBLE);
                mBannerIntent = null;

                mCurrentType = TYPE_NONE;
            }
        }
    }

    private void reloadAppStatus() {
        Privacy privacy = PrivacyHelper.getAppPrivacy();
        if (privacy.isDangerous()) {
            mSfatResultAppLt.setVisibility(View.INVISIBLE);

            if (mFromEnter) {
                mDangerResultAppLt.setVisibility(View.INVISIBLE);
            } else {
                mDangerResultAppLt.setVisibility(View.VISIBLE);
            }
            mDangerResultAppLt.setBackgroundResource(R.drawable.strip_home_nook1);

            mDetDagAppTv.setText(privacy.getPrivacyTitleId());
            mDetDagAppNumTv.setText(privacy.getPrivacyCountText());
            mDangerResultAppDetailTv.setText(privacy.getDangerTipId());
            mPrivacyConut = mPrivacyConut + 1;
            mPrivacyApp = true;
        } else {
            if (mFromEnter || (mLastPrivacyApp && !mPrivacyApp)) {
                mSfatResultAppLt.setVisibility(View.INVISIBLE);
            } else {
                mSfatResultAppLt.setVisibility(View.VISIBLE);
            }
            mSfatResultAppLt.setBackgroundResource(R.drawable.strip_home_ok1);
            if (mLastPrivacyApp && !mPrivacyApp) {
                mDangerResultAppLt.setVisibility(View.VISIBLE);
            } else {
                mDangerResultAppLt.setVisibility(View.INVISIBLE);
            }

            mDetSaftAppTv.setText(privacy.getPrivacyTitleId());
            boolean numVisible = privacy.showPrivacyCount();
            mDetSaftAppNumTv.setVisibility(numVisible ? View.VISIBLE : View.GONE);
            if (numVisible) {
                mDetSaftAppNumTv.setText(privacy.getPrivacyCountText());
            }
        }
        mAppDangerContent.setTranslationY(0);

    }

    private void reloadImageStatus() {
        Privacy privacy = PrivacyHelper.getImagePrivacy();
        if (privacy.isDangerous()) {
            mSfatResultImgLt.setVisibility(View.INVISIBLE);
            if (mFromEnter) {
                mDangerResultImgLt.setVisibility(View.INVISIBLE);
            } else {
                mDangerResultImgLt.setVisibility(View.VISIBLE);
            }

            mDetDagImgNumTv.setText(privacy.getPrivacyCountText());
            mDetDagImgTv.setText(privacy.getPrivacyTitleId());
            mDangerResultPicDetailTv.setText(privacy.getDangerTipId());
            mPrivacyConut = mPrivacyConut + 1;
            mPrivacyPic = true;
        } else {
            if (mFromEnter || (mLastPrivacyPic && !mPrivacyPic)) {
                mSfatResultImgLt.setVisibility(View.INVISIBLE);
            } else {
                mSfatResultImgLt.setVisibility(View.VISIBLE);
            }
            if (mLastPrivacyPic && !mPrivacyPic) {
                mDangerResultImgLt.setVisibility(View.VISIBLE);
            } else {
                mDangerResultImgLt.setVisibility(View.INVISIBLE);
            }

            mDetSaftImgTv.setText(privacy.getPrivacyTitleId());
            boolean numVisible = privacy.showPrivacyCount();
            mDetSaftImgNumTv.setVisibility(numVisible ? View.VISIBLE : View.GONE);
            if (numVisible) {
                mDetSaftImgNumTv.setText(privacy.getPrivacyCountText());
            }
        }
        mPicDangerContent.setTranslationY(0);
    }

    private void reloadVideoStatus() {
        Privacy privacy = PrivacyHelper.getVideoPrivacy();
        if (privacy.isDangerous()) {
            mSfatResultVideoLt.setVisibility(View.INVISIBLE);
            if (mFromEnter) {
                mDangerResultVideoLt.setVisibility(View.INVISIBLE);
            } else {
                mDangerResultVideoLt.setVisibility(View.VISIBLE);
            }
            mDetDagVideoNumTv.setText(privacy.getPrivacyCountText());
            mDetDagVideoTv.setText(privacy.getPrivacyTitleId());
            mDangerResultVideoDetailTv.setText(privacy.getDangerTipId());
            mPrivacyConut = mPrivacyConut + 1;
            mPrivacyVideo = true;
        } else {
            if (mFromEnter || (mLastPrivacyVideo && !mPrivacyVideo)) {
                mSfatResultVideoLt.setVisibility(View.INVISIBLE);
            } else {
                mSfatResultVideoLt.setVisibility(View.VISIBLE);
            }
            if (mLastPrivacyVideo && !mPrivacyVideo) {
                mDangerResultVideoLt.setVisibility(View.VISIBLE);
            } else {
                mDangerResultVideoLt.setVisibility(View.INVISIBLE);
            }

            mDetSaftVideoTv.setText(privacy.getPrivacyTitleId());
            boolean numVisible = privacy.showPrivacyCount();
            mDetSaftVideoNumTv.setVisibility(numVisible ? View.VISIBLE : View.GONE);
            if (numVisible) {
                mDetSaftVideoNumTv.setText(privacy.getPrivacyCountText());
            }
        }
        mVidDangerContent.setTranslationY(0);
    }

    private void initUI(View view) {
        FrameLayout resultRootView = (FrameLayout) view.findViewById(R.id.det_result_ly);

        mSfatResultAppLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_saft_result_app);
        mSfatResultImgLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_saft_result_img);
        mSfatResultVideoLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_saft_result_video);

        mDangerResultAppLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_danger_result_app);
        mDangerResultImgLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_danger_result_img);
        mDangerResultVideoLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_danger_result_video);

        mDangerResultAppDetailTv = (TextView) resultRootView.findViewById(R.id.det_danger_app_detal_tv);
        mDangerResultPicDetailTv = (TextView) resultRootView.findViewById(R.id.det_danger_pic_detal_tv);
        mDangerResultVideoDetailTv = (TextView) resultRootView.findViewById(R.id.det_danger_video_detal_tv);

        mDangerDetTip = (TextView) view.findViewById(R.id.lt_home_danger_det_tip);

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

        mShieldDangerLeftIv = (ImageView) view.findViewById(R.id.shield_danger_left_iv);
        mShieldDangerRightIv = (ImageView) view.findViewById(R.id.shield_danger_right_iv);
        mShieldDangerTopIv = (ImageView) view.findViewById(R.id.shield_danger_top_iv);
        mShieldDangerCenterIv = (ImageView) view.findViewById(R.id.shield_danger_center_iv);
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

        mAppDangerContent = (RelativeLayout) resultRootView.findViewById(R.id.app_danger_content);
        mAppSafeContent = (RelativeLayout) resultRootView.findViewById(R.id.app_safe_content);
        mPicDangerContent = (RelativeLayout) resultRootView.findViewById(R.id.pic_danger_content);
        mPicSafeContent = (RelativeLayout) resultRootView.findViewById(R.id.pic_safe_content);
        mVidDangerContent = (RelativeLayout) resultRootView.findViewById(R.id.vid_danger_content);
        mVidSafeContent = (RelativeLayout) resultRootView.findViewById(R.id.vid_safe_content);

        mFromEnter = true;

    }


    private void initAnim() {
        mFromEnter = false;
        if (!mPrivacyApp && !mPrivacyPic && !mPrivacyVideo) {
            initHomeTopShieldAnim(mShieldTopIv, mShieldLeftIv, mShieldRightIv);
        } else {
            initHomeTopShieldAnim(mShieldDangerTopIv, mShieldDangerLeftIv, mShieldDangerRightIv);
        }
        //测试
        startFirstAnim();
        if (!mPrivacyApp && !mPrivacyPic && !mPrivacyVideo) {
            startHomeCenterShieldAnim(mShieldCenterIv);
        } else {
            startHomeCenterShieldAnim(mShieldDangerCenterIv);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDetectPresenter.detachView();

        sShowLost = !sShowLost;
        mFromEnter = false;
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
                if (privacy.getNewCount() > 0 && privacy.getNewCount() != privacy.getTotalCount()) {
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
                if (privacy.getNewCount() > 0 && privacy.getNewCount() != privacy.getTotalCount()) {
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
                if (privacy.getNewCount() > 0 && privacy.getNewCount() != privacy.getTotalCount()) {
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
    private void shieldPositionInit() {
        Context context = getActivity();
        mShieldTopIv.setTranslationY(-DipPixelUtil.dip2px(
                context, getResources().getInteger(R.integer.shield_top_offset)));
        mShieldLeftIv.setTranslationX(-DipPixelUtil.dip2px(
                context, getResources().getInteger(R.integer.shield_left_offset)));
        mShieldRightIv.setTranslationX(DipPixelUtil.dip2px(
                context, getResources().getInteger(R.integer.shield_right_offset)));
        mShieldCenterIv.setTranslationX(DipPixelUtil.dip2px(
                context, getResources().getInteger(R.integer.shield_center_blue_offset)));

        mShieldDangerTopIv.setTranslationY(-DipPixelUtil.dip2px(
                context, getResources().getInteger(R.integer.shield_top_offset)));
        mShieldDangerLeftIv.setTranslationX(-DipPixelUtil.dip2px(
                context, getResources().getInteger(R.integer.shield_left_offset)));
        mShieldDangerRightIv.setTranslationX(DipPixelUtil.dip2px(
                context, getResources().getInteger(R.integer.shield_right_offset)));
        mShieldDangerCenterIv.setTranslationX(DipPixelUtil.dip2px(
                context, getResources().getInteger(R.integer.shield_center_red_offset)));

    }

    //危险盾牌设置
    public void setDangerShieldView(boolean isShow, boolean firstEnter) {
        shieldPositionInit();
        if (isShow) {
            if (firstEnter) {
                mShieldDangerTopIv.setVisibility(View.INVISIBLE);
                mShieldDangerLeftIv.setVisibility(View.INVISIBLE);
                mShieldDangerRightIv.setVisibility(View.INVISIBLE);
                mShieldDangerCenterIv.setVisibility(View.INVISIBLE);
            } else {
                LeoLog.e("setDangerShieldView", "setDangerShieldView");
                mShieldDangerTopIv.setVisibility(View.VISIBLE);
                mShieldDangerLeftIv.setVisibility(View.VISIBLE);
                mShieldDangerRightIv.setVisibility(View.VISIBLE);
                mShieldDangerCenterIv.setVisibility(View.VISIBLE);
            }
            mShieldTopIv.setVisibility(View.INVISIBLE);
            mShieldLeftIv.setVisibility(View.INVISIBLE);
            mShieldRightIv.setVisibility(View.INVISIBLE);
            mShieldCenterIv.setVisibility(View.INVISIBLE);
        } else {
            mShieldDangerTopIv.setVisibility(View.INVISIBLE);
            mShieldDangerLeftIv.setVisibility(View.INVISIBLE);
            mShieldDangerRightIv.setVisibility(View.INVISIBLE);
            mShieldDangerCenterIv.setVisibility(View.INVISIBLE);
        }
    }

    //安全盾牌设置
    public void setSfateShieldView(boolean firstEnter) {
        shieldPositionInit();
        boolean last = mLastPrivacyApp || mLastPrivacyPic || mLastPrivacyVideo;
        boolean current = mPrivacyApp && mPrivacyPic && mPrivacyVideo;
        if (firstEnter || (last && !current)) {
            mShieldTopIv.setVisibility(View.INVISIBLE);
            mShieldLeftIv.setVisibility(View.INVISIBLE);
            mShieldRightIv.setVisibility(View.INVISIBLE);
            mShieldCenterIv.setVisibility(View.INVISIBLE);
        } else {
            mShieldTopIv.setVisibility(View.VISIBLE);
            mShieldLeftIv.setVisibility(View.VISIBLE);
            mShieldRightIv.setVisibility(View.VISIBLE);
            mShieldCenterIv.setVisibility(View.VISIBLE);
        }
        if (last && !current) {
            mShieldDangerTopIv.setVisibility(View.VISIBLE);
            mShieldDangerLeftIv.setVisibility(View.VISIBLE);
            mShieldDangerRightIv.setVisibility(View.VISIBLE);
            mShieldDangerCenterIv.setVisibility(View.VISIBLE);
        } else {
            mShieldDangerTopIv.setVisibility(View.INVISIBLE);
            mShieldDangerLeftIv.setVisibility(View.INVISIBLE);
            mShieldDangerRightIv.setVisibility(View.INVISIBLE);
            mShieldDangerCenterIv.setVisibility(View.INVISIBLE);
        }
    }

    //首次进入主页盾牌动画
    public void initHomeTopShieldAnim(final View top, final View left, final View right) {
        int value2 = -DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_top_offset));
        int value1 = -DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_top_y_trans)) + value2;
        PropertyValuesHolder topTransY = PropertyValuesHolder.ofFloat("translationY", value1, value2);
        PropertyValuesHolder topAlpha = PropertyValuesHolder.ofFloat("alpha", 0, 1);

        int valueLeft2 = -DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_left_offset));
        int valueLeft1 = -DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_left_x_trans)) + value2;
        PropertyValuesHolder leftTransY = PropertyValuesHolder.ofFloat("translationX", valueLeft1, valueLeft2);
        PropertyValuesHolder leftAlpha = PropertyValuesHolder.ofFloat("alpha", 0, 1);


        int valueRight2 = DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_left_offset));
        int valueRight1 = DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_right_x_trans)) + value2;

        PropertyValuesHolder rightTransY = PropertyValuesHolder.ofFloat("translationX", valueRight1, valueRight2);
        PropertyValuesHolder rightAlpha = PropertyValuesHolder.ofFloat("alpha", 0, 1);
        ObjectAnimator topAnim = ObjectAnimator.ofPropertyValuesHolder(top, topTransY, topAlpha);
        topAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                top.setVisibility(View.VISIBLE);
                left.setVisibility(View.VISIBLE);
                right.setVisibility(View.VISIBLE);
            }
        });
        ObjectAnimator leftAnim = ObjectAnimator.ofPropertyValuesHolder(left, leftTransY, leftAlpha);
        ObjectAnimator rightAnim = ObjectAnimator.ofPropertyValuesHolder(right, rightTransY, rightAlpha);


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
    public void startHomeCenterShieldAnim(final View view) {
        ObjectAnimator scaleXFirstAnim = ObjectAnimator.ofFloat(view, "scaleX", 0.6f, 1.06f);
        ObjectAnimator scaleYFirstAnim = ObjectAnimator.ofFloat(view, "scaleY", 0.6f, 1.06f);
        AnimatorSet scaleFirstAnimatorSet = new AnimatorSet();
        scaleFirstAnimatorSet.playTogether(scaleXFirstAnim, scaleYFirstAnim);
        scaleFirstAnimatorSet.setDuration(200);

        ObjectAnimator scaleXEndAnim = ObjectAnimator.ofFloat(view, "scaleX", 1.06f, 1f);
        ObjectAnimator scaleYEndAnim = ObjectAnimator.ofFloat(view, "scaleY", 1.06f, 1f);
        AnimatorSet scaleEndAnimatorSet = new AnimatorSet();
        scaleEndAnimatorSet.playTogether(scaleXEndAnim, scaleYEndAnim);
        scaleEndAnimatorSet.setDuration(80);

        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        alphaAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setVisibility(View.VISIBLE);
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

        boolean totalDangerous = false;
        if (PrivacyHelper.getAppPrivacy().isDangerous()) {
            appAnimatorSet = getTranslateAnim(mDangerResultAppLt);
            totalDangerous = true;
        } else {
            appAnimatorSet = getTranslateAnim(mSfatResultAppLt);
        }
        if (PrivacyHelper.getImagePrivacy().isDangerous()) {
            picAnimatorSet = getTranslateAnim(mDangerResultImgLt);
            totalDangerous = true;
        } else {
            picAnimatorSet = getTranslateAnim(mSfatResultImgLt);
        }
        if (PrivacyHelper.getVideoPrivacy().isDangerous()) {
            vidAnimatorSet = getTranslateAnim(mDangerResultVideoLt);
            totalDangerous = true;
        } else {
            vidAnimatorSet = getTranslateAnim(mSfatResultVideoLt);
        }
        ObjectAnimator tipsAnim = ObjectAnimator.ofFloat(mCenterTipRt, "x",
                            -mScreenWidth, mCenterTipRt.getTranslationX());
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
        if (!totalDangerous) {
            animatorSet.play(tipsAnim).after(1000).after(vidAnimatorSet);
        }

        animatorSet.start();

    }

    /**
     * 扫描结果位移动画
     */
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

    /**
     * 扫描结果处理后切换动画
     */
    public void detectResultConversionAnim(final View current,final View top, final View showView,
                                           final View missView, Animator.AnimatorListener listener) {

        ObjectAnimator currentDown = ObjectAnimator.ofFloat(current, "translationY", -50, 0);
        currentDown.setDuration(300);
        ObjectAnimator topDown = ObjectAnimator.ofFloat(top, "translationY", 0, 40);
        topDown.setDuration(300);

        currentDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                showView.setVisibility(View.VISIBLE);
                showView.setBackgroundColor(getResources().getColor(R.color.transparent));
            }
        });
        topDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(showView, "alpha", 0f, 1f);
        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(mAppSafeContent == current){
                    showView.setBackgroundDrawable(
                            getResources().getDrawable(R.drawable.strip_home_ok1));
                }else if(mPicSafeContent == current){
                    showView.setBackgroundDrawable(
                            getResources().getDrawable(R.drawable.strip_home_ok2));
                }else if(mVidSafeContent == current){
                    showView.setBackgroundDrawable(
                            getResources().getDrawable(R.drawable.strip_home_ok3));
                }else{
                    showView.setBackgroundDrawable(
                            getResources().getDrawable(R.drawable.strip_home_ok1));
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                missView.setVisibility(View.INVISIBLE);
                top.setTranslationY(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        alphaAnim.setDuration(150);

        final AnimatorSet totalAnimatorSet = new AnimatorSet();
        totalAnimatorSet.playTogether(currentDown, topDown, alphaAnim);
        if (listener != null) {
            totalAnimatorSet.addListener(listener);
        }
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                totalAnimatorSet.start();

            }
        }, 500);
    }

    //红蓝盾牌的替换动画

    public void dangerShieldConverAnim() {
        setDangerShieldView(true, false);
        //危险状态
        ObjectAnimator dangerLeftScalX = ObjectAnimator.ofFloat(mShieldDangerLeftIv, "scaleX", 1.0f, 1.1f);
        ObjectAnimator dangerLeftScalY = ObjectAnimator.ofFloat(mShieldDangerLeftIv, "scaleY", 1.0f, 1.1f);
        ObjectAnimator dangerLeftApl = ObjectAnimator.ofFloat(mShieldDangerLeftIv, "alpha", 1.0f, 0.0f);
        AnimatorSet dangerLeftAnim = new AnimatorSet();
        dangerLeftAnim.playTogether(dangerLeftScalX, dangerLeftScalY, dangerLeftApl);
        dangerLeftAnim.setDuration(480);
        dangerLeftAnim.start();

        ObjectAnimator dangerRightScalX = ObjectAnimator.ofFloat(mShieldDangerRightIv, "scaleX", 1.0f, 1.1f);
        ObjectAnimator dangerRightScalY = ObjectAnimator.ofFloat(mShieldDangerRightIv, "scaleY", 1.0f, 1.1f);
        ObjectAnimator dangerRightApl = ObjectAnimator.ofFloat(mShieldDangerRightIv, "alpha", 1.0f, 0.0f);
        AnimatorSet dangerRightAnim = new AnimatorSet();
        dangerRightAnim.playTogether(dangerRightScalX, dangerRightScalY, dangerRightApl);
        dangerRightAnim.setDuration(480);
        dangerRightAnim.start();

        ObjectAnimator dangerTopScalX = ObjectAnimator.ofFloat(mShieldDangerTopIv, "scaleX", 1.0f, 1.1f);
        ObjectAnimator dangerTopScalY = ObjectAnimator.ofFloat(mShieldDangerTopIv, "scaleY", 1.0f, 1.1f);
        ObjectAnimator dangerTopApl = ObjectAnimator.ofFloat(mShieldDangerTopIv, "alpha", 1.0f, 0.0f);
        AnimatorSet dangerTopAnim = new AnimatorSet();
        dangerTopAnim.playTogether(dangerTopScalX, dangerTopScalY, dangerTopApl);
        dangerTopAnim.setDuration(480);
        dangerTopAnim.start();

        ObjectAnimator dangerCenterScalX = ObjectAnimator.ofFloat(mShieldDangerCenterIv, "scaleX", 1.0f, 1.1f);
        ObjectAnimator dangerCenterScalY = ObjectAnimator.ofFloat(mShieldDangerCenterIv, "scaleY", 1.0f, 1.1f);
        ObjectAnimator dangerCenterApl = ObjectAnimator.ofFloat(mShieldDangerCenterIv, "alpha", 1.0f, 0.0f);
        AnimatorSet dangerCenterAnim = new AnimatorSet();
        dangerCenterAnim.playTogether(dangerCenterScalX, dangerCenterScalY, dangerCenterApl);
        dangerCenterAnim.setDuration(480);
        dangerCenterAnim.start();

        //蓝色盾牌
        ObjectAnimator saftLeftScalX = ObjectAnimator.ofFloat(mShieldLeftIv, "scaleX", 0.8f, 1.0f);
        ObjectAnimator saftLeftScalY = ObjectAnimator.ofFloat(mShieldLeftIv, "scaleY", 0.8f, 1.0f);
        ObjectAnimator saftLeftApl = ObjectAnimator.ofFloat(mShieldLeftIv, "alpha", 0.8f, 1.0f);
        AnimatorSet saftLeftAnim = new AnimatorSet();
        saftLeftAnim.playTogether(saftLeftScalX, saftLeftScalY, saftLeftApl);
        saftLeftAnim.setDuration(600);
        saftLeftAnim.start();

        ObjectAnimator saftRightScalX = ObjectAnimator.ofFloat(mShieldRightIv, "scaleX", 0.8f, 1.0f);
        ObjectAnimator saftRightScalY = ObjectAnimator.ofFloat(mShieldRightIv, "scaleY", 0.8f, 1.0f);
        ObjectAnimator saftRightApl = ObjectAnimator.ofFloat(mShieldRightIv, "alpha", 0.8f, 1.0f);
        AnimatorSet saftRightAnim = new AnimatorSet();
        saftRightAnim.playTogether(saftRightScalX, saftRightScalY, saftRightApl);
        saftRightAnim.setDuration(600);
        saftRightAnim.start();

        ObjectAnimator saftTopScalX = ObjectAnimator.ofFloat(mShieldTopIv, "scaleX", 0.8f, 1.0f);
        ObjectAnimator saftTopScalY = ObjectAnimator.ofFloat(mShieldTopIv, "scaleY", 0.8f, 1.0f);
        ObjectAnimator saftTopApl = ObjectAnimator.ofFloat(mShieldTopIv, "alpha", 0.8f, 1.0f);
        AnimatorSet saftTopAnim = new AnimatorSet();
        saftTopAnim.playTogether(saftTopScalX, saftTopScalY, saftTopApl);
        saftTopAnim.setDuration(600);
        saftTopAnim.start();

        ObjectAnimator saftCenterScalX = ObjectAnimator.ofFloat(mShieldCenterIv, "scaleX", 0.8f, 1.0f);
        ObjectAnimator saftCenterScalY = ObjectAnimator.ofFloat(mShieldCenterIv, "scaleY", 0.8f, 1.0f);
        ObjectAnimator saftCenterApl = ObjectAnimator.ofFloat(mShieldCenterIv, "alpha", 0.0f, 1.0f);
        AnimatorSet saftCenterAnim = new AnimatorSet();
        saftCenterAnim.playTogether(saftCenterScalX, saftCenterScalY, saftCenterApl);
        saftCenterAnim.setDuration(600);
        saftCenterApl.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                mShieldLeftIv.setVisibility(View.VISIBLE);
                mShieldRightIv.setVisibility(View.VISIBLE);
                mShieldTopIv.setVisibility(View.VISIBLE);
                mShieldCenterIv.setVisibility(View.VISIBLE);
            }
        });
        saftTopApl.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setDangerShieldView(false, false);
                getAlphaAnim(mShieldDangerTopIv);
                getAlphaAnim(mShieldDangerCenterIv);
                getAlphaAnim(mShieldDangerLeftIv);
                getAlphaAnim(mShieldDangerRightIv);
            }
        });
        saftCenterAnim.start();

    }

    private void getAlphaAnim(View view) {
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f);
        alphaAnim.setDuration(50);

        alphaAnim.start();
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
