package com.leo.appmaster.home;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.SecurityScoreEvent;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.ui.BurstDecor;
import com.leo.appmaster.ui.HomeAnimLoadingLayer;
import com.leo.appmaster.ui.HomeAnimShieldLayer;
import com.leo.appmaster.ui.HomeAnimView;
import com.leo.appmaster.ui.ShieldFlipDecor;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PropertyInfoUtil;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.ValueAnimator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 隐私等级扫描
 *
 * @author lishuai
 */
public class HomePrivacyFragment extends Fragment {
    private static final String TAG = "HomePrivacyFragment";
    private static final int ID_FAST_ARROW = 1000;
    // 每一分耗时50ms
    private static final int TPS = 50;
    private static final int MIN_TIME = 1000;

    public static int sScreenWidth = 0;
    public static int sScreenHeight = 0;
    private HomeAnimView mHomeAnimView;
    private AnimatorSet mAnimatorSet;
    private int mWidth;

    private FastHandler mHandler;

    private ObjectAnimator mFastArrowAnim;

    private boolean mStopped;
    private boolean mFistAnimFinished;
    private int mCurrentScore;

    private ObjectAnimator mScanningAnimator;

    private HomeActivity mActivity;
    private AnimatorSet mFinalAnim;
    private boolean mProgressAnimating = true;
    private Runnable mScoreChangeRunnable;
    private ObjectAnimator mCircleRotateAnim;

    private int mCurrentPercent;
    private ObjectAnimator mShieldOffsetYAnim;
    private ObjectAnimator mShieldOffsetXAnim;
    private boolean mInterceptRaise;

    private static boolean mMemoryLess = false;

    public static boolean mAnimatorPlaying;

    private AnimatorSet mMoveLeftTopAnim;

    private AnimatorSet mFullScoreMove;
    private ObjectAnimator mFullScoreStart;

    private boolean mIsReset; //是否在扫描时点击取消按钮或回退 控制满分动画中断

    public HomePrivacyFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new FastHandler(this);
        mHomeAnimView = (HomeAnimView) view.findViewById(R.id.home_wave_tv);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mWidth = metrics.widthPixels;

        sScreenWidth = mWidth;
        sScreenHeight = metrics.heightPixels;

        initAnim();
        if (mHomeAnimView.isLayouted()) {
            startWaveDelay();
        } else {
            mHomeAnimView.startAfterLayout(new Runnable() {
                @Override
                public void run() {
                    startWaveDelay();
                }
            });
        }
    }

    public void showProcessProgress(int type) {
        if (type == PrivacyHelper.PRIVACY_APP_LOCK) {
            mHomeAnimView.setShowProcessLoading(true, HomeAnimLoadingLayer.LOAD_LOCK_APP);
        } else if (type == PrivacyHelper.PRIVACY_HIDE_PIC) {
            mHomeAnimView.setShowProcessLoading(true, HomeAnimLoadingLayer.LOAD_HIDE_PIC);
        } else if (type == PrivacyHelper.PRIVACY_HIDE_VID) {
            mHomeAnimView.setShowProcessLoading(true, HomeAnimLoadingLayer.LOAD_HIDE_VID);
        } else {
            mHomeAnimView.setShowProcessLoading(false, 0);
        }
    }

    /**
     * 开始上升动画及后续动画
     * 点击处理之后的场景
     *
     * @param increaseScore
     */
    public void startLoadingRiseAnim(final int increaseScore) {
        mInterceptRaise = false;
        mHomeAnimView.getLoadingLayer().setRiseHeight(0);
        int height = AppMasterApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.home_loading_rise);
        final ObjectAnimator riseAnim = ObjectAnimator.ofInt(mHomeAnimView.getLoadingLayer(), "riseHeight", 0, height);
        riseAnim.setDuration(320);
        riseAnim.setInterpolator(new LinearInterpolator());
        riseAnim.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                mHomeAnimView.setShowProcessLoading(false, 0);
                startShieldBeatAnim(increaseScore);
            }
        });

        riseAnim.start();
    }

    private ObjectAnimator getWaveAnimator(String propertyName, long delay, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mHomeAnimView.getShieldLayer(), propertyName,
                0f, 1f);
        if (delay > 0) {
            animator.setStartDelay(delay);
        }
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());

        return animator;
    }

    private void initAnim() {
        PrivacyHelper helper = PrivacyHelper.getInstance(getActivity());
        int currentScore = helper.getSecurityScore();
        int time = (100 - currentScore) * TPS;
        time = time < MIN_TIME ? MIN_TIME : time;
        LeoLog.e(TAG, "currentScore:" + currentScore);
        mHomeAnimView.getBgLayer().setTargetScore(currentScore);
        mHomeAnimView.getBgLayer().setIncrease(false);
        mCurrentScore = currentScore;

        AnimatorSet firstAnimSet = new AnimatorSet();
        // 时间减少
        ObjectAnimator timeAnim = ObjectAnimator.ofInt(mHomeAnimView, "securityScore", 100, currentScore);
        timeAnim.setDuration(time);
        timeAnim.setInterpolator(new LinearInterpolator());

        // 进度条
        ObjectAnimator progressAnim = ObjectAnimator.ofInt(mHomeAnimView, "progress", 0, mWidth);
        progressAnim.setDuration(time);
        progressAnim.setInterpolator(new LinearInterpolator());
        progressAnim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mProgressAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mProgressAnimating = false;
                if (mScoreChangeRunnable != null) {
                    mScoreChangeRunnable.run();
                }
            }
        });
        firstAnimSet.playTogether(timeAnim, progressAnim);

        List<Animator> animators = new ArrayList<Animator>();
        // 外环放大
        ObjectAnimator outCircleScaleAnim = ObjectAnimator.ofFloat(mHomeAnimView, "outCircleScaleRatio",
                HomeAnimShieldLayer.MIN_OUT_CIRCLE_SCALE_RATIO, HomeAnimShieldLayer.MAX_OUT_CIRCLE_SCALE_RATIO);
        outCircleScaleAnim.setInterpolator(new LinearInterpolator());
        outCircleScaleAnim.setDuration(600);
        animators.add(outCircleScaleAnim);

        // 内环放大
        ObjectAnimator inCircleScaleAnim = ObjectAnimator.ofFloat(mHomeAnimView, "inCircleScaleRatio",
                HomeAnimShieldLayer.MIN_IN_CIRCLE_SCALE_RATIO, HomeAnimShieldLayer.MAX_IN_CIRCLE_SCALE_RATIO);
        inCircleScaleAnim.setInterpolator(new LinearInterpolator());
        inCircleScaleAnim.setDuration(600);
        animators.add(inCircleScaleAnim);

        inCircleScaleAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimatorPlaying = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimatorPlaying = false;
            }
        });


        // 内环、外环旋转
        long memorySize = PropertyInfoUtil.getTotalMemory(mActivity);
        mCircleRotateAnim = ObjectAnimator.ofFloat(mHomeAnimView, "circleRotateRatio", 0f, 360f);
        mCircleRotateAnim.setDuration(3500);
        mCircleRotateAnim.setInterpolator(new LinearInterpolator());
        if (memorySize >= Constants.TOTAL_MEMORY_JUDGE_AS_LOW_MEMORY) {
            mCircleRotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        } else {
            mCircleRotateAnim.setRepeatCount(3);
            mHomeAnimView.setLessMemory();
            mMemoryLess = true;
        }
        animators.add(mCircleRotateAnim);

        // 盾牌缩小
        ObjectAnimator shieldScaleAnim = ObjectAnimator.ofFloat(mHomeAnimView, "shieldScaleRatio",
                HomeAnimShieldLayer.MAX_SHIELD_SCALE_RATIO, HomeAnimShieldLayer.MIN_SHIELD_SCALE_RATIO);
        shieldScaleAnim.setInterpolator(new LinearInterpolator());
        shieldScaleAnim.setDuration(600);
        animators.add(shieldScaleAnim);

        // 内环、外环透明度
        ObjectAnimator inCircleAlphaAnim = ObjectAnimator.ofInt(mHomeAnimView.getShieldLayer(), "inCircleAlpha", 0, 255);
        inCircleAlphaAnim.setInterpolator(new LinearInterpolator());
        inCircleAlphaAnim.setDuration(600);
        animators.add(inCircleAlphaAnim);

        ObjectAnimator outCircleAlphaAnim = ObjectAnimator.ofInt(mHomeAnimView.getShieldLayer(), "outCircleAlpha", 0, 255);
        outCircleAlphaAnim.setInterpolator(new LinearInterpolator());
        outCircleAlphaAnim.setDuration(600);
        animators.add(outCircleAlphaAnim);

        AnimatorSet secondAnimSet = new AnimatorSet();
        secondAnimSet.playTogether(animators);

        firstAnimSet.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.sendEmptyMessageDelayed(ID_FAST_ARROW, 2000);
                mFistAnimFinished = true;
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(firstAnimSet, secondAnimSet);
    }

    public void onFromNotifi() {
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet.end();
            LeoLog.e(TAG, "onFromNotifi");
            mAnimatorSet = null;
        }

        showScanningPercent(-1);
        stopFinalAnim();
        mHomeAnimView.setShowStep(false);
        mHomeAnimView.setShowProcessLoading(false, 0);

        HomeAnimShieldLayer shieldLayer = mHomeAnimView.getShieldLayer();
        shieldLayer.setFinalShieldRatio(0);
        shieldLayer.setFinalTextRatio(HomeAnimShieldLayer.MIN_SHIELD_SCALE_RATIO);
        shieldLayer.setInCircleScaleRatio(HomeAnimShieldLayer.MAX_IN_CIRCLE_SCALE_RATIO);
        shieldLayer.setOutCircleScaleRatio(HomeAnimShieldLayer.MAX_OUT_CIRCLE_SCALE_RATIO);
        shieldLayer.setShieldScale(HomeAnimShieldLayer.MIN_SHIELD_SCALE_RATIO);
        shieldLayer.setInCircleAlpha(255);
        shieldLayer.setOutCircleAlpha(255);

        if (mShieldOffsetYAnim != null) {
            mShieldOffsetYAnim.end();
        }
        mHomeAnimView.setShieldOffsetY(0);
        mHomeAnimView.setShieldOffsetX(0);

        PrivacyHelper helper = PrivacyHelper.getInstance(getActivity());
        int currentScore = helper.getSecurityScore();
        LeoLog.e(TAG, "onFromNotifi currentScore:" + currentScore);
        mHomeAnimView.getBgLayer().setTargetScore(currentScore);
        mHomeAnimView.getBgLayer().setIncrease(false);
        mCurrentScore = currentScore;

        mHomeAnimView.setSecurityScore(currentScore);

        mCircleRotateAnim = ObjectAnimator.ofFloat(mHomeAnimView, "circleRotateRatio", 0f, 360f);
        mCircleRotateAnim.setDuration(3500);
        mCircleRotateAnim.setInterpolator(new LinearInterpolator());
        mCircleRotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        mCircleRotateAnim.start();

    }

    public void onScanStart() {
        if (!mMemoryLess) {
            return;
        }
        if (mCircleRotateAnim != null) {
            mCircleRotateAnim.cancel();
            mCircleRotateAnim = null;
        }
        mCircleRotateAnim = ObjectAnimator.ofFloat(mHomeAnimView, "circleRotateRatio", 0f, 360f);
        mCircleRotateAnim.setDuration(3500);
        mCircleRotateAnim.setInterpolator(new LinearInterpolator());
        mCircleRotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        mCircleRotateAnim.start();
    }

    public void onScanCancel() {
        if (mCircleRotateAnim != null && mMemoryLess) {
            mCircleRotateAnim.cancel();
            mCircleRotateAnim = null;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (HomeActivity) activity;
        LeoEventBus.getDefaultBus().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LeoEventBus.getDefaultBus().unregister(this);

        endWaveAnim();
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (mStopped) {
//            if (!mFistAnimFinished) {
//                initAnim();
//            } else {
//                initResumedAnim();
//            }
//        }

    }

    private void startAnim(long duration) {
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startWaveAnim();
            }
        }, duration);
    }

    private void startWaveDelay() {
        if ("HUAWEI P6-T00".equals(android.os.Build.MODEL)) {
            LeoLog.i("dhdh", "in if");
            startAnim(500);
        } else {
            startAnim(400);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
//        endWaveAnim();
//        mStopped = true;
          onScanCancel();
    }

    public void onEventMainThread(SecurityScoreEvent event) {
        final int oldScore = mCurrentScore;
        final int newScore = event.securityScore;
        mCurrentScore = newScore;

        LeoLog.i(TAG, "onEventMainThread, oldScore: " + oldScore + " | newScore: " + newScore +
                " | mProgressAnimating: " + mProgressAnimating);
        if (!mProgressAnimating) {
            doScoreChangeAnimation(oldScore, newScore);
        } else {
            mScoreChangeRunnable = new Runnable() {
                @Override
                public void run() {
                    doScoreChangeAnimation(oldScore, newScore);
                }
            };
        }
    }

    private void doScoreChangeAnimation(int fromScore, int toScore) {
        LeoLog.i(TAG, "doScoreChangeAnimation, fromScore: " + fromScore + " | toScore: " + toScore);
        mHomeAnimView.getBgLayer().setTargetScore(mCurrentScore);
        mHomeAnimView.getBgLayer().setIncrease(fromScore < toScore);

        // 时间减少
        ObjectAnimator timeAnim = ObjectAnimator.ofInt(mHomeAnimView, "securityScore", fromScore, toScore);
        timeAnim.setDuration(600);
        timeAnim.setInterpolator(new LinearInterpolator());
        timeAnim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mScoreChangeRunnable = null;
            }
        });
        timeAnim.start();
    }

    private ObjectAnimator getIncreaseScoreAnim(int score) {
        int oldScore = mCurrentScore;
        int newScore = oldScore + score;
        mCurrentScore = newScore;

        mHomeAnimView.getBgLayer().setTargetScore(mCurrentScore);
        mHomeAnimView.getBgLayer().setIncrease(true);
        ObjectAnimator timeAnim = ObjectAnimator.ofInt(mHomeAnimView, "securityScore", oldScore, newScore);
        timeAnim.setDuration(360);
        timeAnim.setInterpolator(new LinearInterpolator());
        timeAnim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mInterceptRaise) {
                    mActivity.jumpToNextFragment(false);
                    mHomeAnimView.increaseCurrentStep();
                    startStepAnim();
                }
            }
        });
        return timeAnim;
    }

    public void startIncreaseSocreAnim(int score) {
        int oldScore = mCurrentScore;
        int newScore = oldScore + score;
        mCurrentScore = newScore;

        mHomeAnimView.getBgLayer().setIncrease(true);
        ObjectAnimator timeAnim = ObjectAnimator.ofInt(mHomeAnimView, "securityScore", oldScore, newScore);
        timeAnim.setDuration(360);
        timeAnim.setInterpolator(new LinearInterpolator());
        timeAnim.start();
    }

    private void startStepAnim() {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator lineAnim = ObjectAnimator.ofFloat(mHomeAnimView.getStepLayer(), "lineRatio", 0f, 1f);
        lineAnim.setDuration(920);
        lineAnim.setInterpolator(new LinearInterpolator());

        ObjectAnimator circleBigAnim = ObjectAnimator.ofFloat(mHomeAnimView.getStepLayer(), "circleRatio", 1f, 1.3f);
        circleBigAnim.setDuration(160);
        circleBigAnim.setInterpolator(new LinearInterpolator());

        ObjectAnimator circleSmallAnim = ObjectAnimator.ofFloat(mHomeAnimView.getStepLayer(), "circleRatio", 1.3f, 1f);
        circleSmallAnim.setDuration(240);
        circleSmallAnim.setInterpolator(new LinearInterpolator());

        animatorSet.playSequentially(lineAnim, circleBigAnim, circleSmallAnim);
        animatorSet.start();
    }

    public void increaseStepAnim() {
        mHomeAnimView.increaseCurrentStep();
        startStepAnim();
    }

    public void startShieldBeatAnim(final int increaseScore) {
        ObjectAnimator shieldBeatAnim = ObjectAnimator.ofFloat(mHomeAnimView, "shieldScaleRatio",
                HomeAnimShieldLayer.MIN_SHIELD_SCALE_RATIO, 0.84f, HomeAnimShieldLayer.MIN_SHIELD_SCALE_RATIO);
        shieldBeatAnim.setInterpolator(new LinearInterpolator());
        shieldBeatAnim.setDuration(320);

        ObjectAnimator wave1 = getWaveAnimator("firstWaveRatio", 80, 1200);
        ObjectAnimator wave2 = getWaveAnimator("secondWaveRatio", 480, 1200);
        ObjectAnimator wave3 = getWaveAnimator("thirdWaveRatio", 880, 1200);

        ObjectAnimator increaseScoreAnim = getIncreaseScoreAnim(increaseScore);
        increaseScoreAnim.setStartDelay(600);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(shieldBeatAnim, wave1, wave2, wave3, increaseScoreAnim);
        animatorSet.start();
    }

    /**
     * 设置是否显示多彩进度条
     */
    public void setShowColorProgress(boolean show) {
        mHomeAnimView.setShowColorProgress(show);
    }

    /**
     * 显示进度百分比动画
     *
     * @param duration 时常，-1则不显示
     */
    public void showScanningPercent(int duration) {
        if (duration == -1) {
            if (mScanningAnimator != null) {
                mScanningAnimator.cancel();
            }
            mHomeAnimView.setScanningPercent(-1);
        } else {
            mScanningAnimator = ObjectAnimator.ofInt(mHomeAnimView, "scanningPercent", 0, 101);
            mScanningAnimator.setDuration(duration);
            mScanningAnimator.setInterpolator(new LinearInterpolator());
            mScanningAnimator.start();
            mScanningAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer integer = (Integer) animation.getAnimatedValue();
                    if (integer != null) {
                        mCurrentPercent = integer;
                    }
                }
            });
        }
    }

    /**
     * 显示进度百分比动画
     *
     * @param duration 时常，-1则不显示
     */
    public void showScanningPercent(int duration, int from, int to) {
        if (duration == -1) {
            if (mScanningAnimator != null) {
                mScanningAnimator.cancel();
            }
            mHomeAnimView.setScanningPercent(-1);
        } else {
            if (mScanningAnimator != null) {
                mScanningAnimator.end();
            }
            if (mMoveLeftTopAnim == null) {
                return;
            }
            mScanningAnimator = ObjectAnimator.ofInt(mHomeAnimView, "scanningPercent", from, to);
            mScanningAnimator.setDuration(duration);
            mScanningAnimator.setInterpolator(new LinearInterpolator());
            mScanningAnimator.start();
            mScanningAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer integer = (Integer) animation.getAnimatedValue();
                    if (integer != null) {
                        mCurrentPercent = integer;
                    }
                }
            });
        }
    }

    public int getScanningPercent() {
        return mCurrentPercent;
    }

    /**
     * 开始盾牌上移、外环框隐藏动画
     */
    public void startProcessing(int stepCount) {
        if (isRemoving() || isDetached() || getActivity() == null
                || mHomeAnimView == null
                || mHomeAnimView.getShieldLayer() == null) {
            return;
        }

        mHomeAnimView.getShieldLayer().setOutCircleAlpha(0);
        mHomeAnimView.getShieldLayer().setInCircleAlpha(0);
        showScanningPercent(-1);

        mHomeAnimView.setShowStep(true);
        mHomeAnimView.setTotalStepCount(stepCount);
//        int offsetY = mHomeAnimView.getShieldLayer().getMaxOffsetY();
//        mShieldOffsetYAnim = ObjectAnimator.ofInt(mHomeAnimView, "shieldOffsetY", 0, offsetY);
//        int duration = getActivity().getResources().getInteger(android.R.integer.config_mediumAnimTime);
//        mShieldOffsetYAnim.setDuration(duration);
//        mShieldOffsetYAnim.setInterpolator(new LinearInterpolator());
//        mShieldOffsetYAnim.start();
        int offsetX = mHomeAnimView.getShieldLayer().getMaxOffsetX();
        mShieldOffsetYAnim = ObjectAnimator.ofInt(mHomeAnimView, "shieldOffsetX", offsetX, 0);
        int duration = getActivity().getResources().getInteger(android.R.integer.config_mediumAnimTime);
        mShieldOffsetYAnim.setDuration(duration);
        mShieldOffsetYAnim.setInterpolator(new LinearInterpolator());
        mShieldOffsetYAnim.start();

        // 盾牌放大
        ObjectAnimator shieldScaleAnim = ObjectAnimator.ofFloat(mHomeAnimView, "shieldScaleRatio",
                HomeAnimShieldLayer.SHIELD_SCANNING_RATIO, HomeAnimShieldLayer.MIN_SHIELD_SCALE_RATIO);
        shieldScaleAnim.setInterpolator(new LinearInterpolator());
        shieldScaleAnim.setDuration(200);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(mShieldOffsetYAnim, shieldScaleAnim);
        animatorSet.start();
    }

    public int getTotalStepCount() {
        return mHomeAnimView.getStepLayer().getTotalStepCount();
    }

    /**
     * 启动开始扫描动画
     * 1 盾牌、内环，缩小+透明
     * 2 外环缩小至0.5
     * 3 盾牌区域向左、上移动
     */
    public void startScanningAnim() {
        if (isRemoving() || isDetached() || getActivity() == null
                || mHomeAnimView == null
                || mHomeAnimView.getShieldLayer() == null) {
            return;
        }

        mIsReset = false;

        List<Animator> animators = new ArrayList<Animator>();

        // 盾牌区域向左、上移动
        int offsetY = mHomeAnimView.getShieldLayer().getMaxOffsetY();
        LeoLog.e("Home", "offsetY:" + offsetY);
        mShieldOffsetYAnim = ObjectAnimator.ofInt(mHomeAnimView, "shieldOffsetY", 0, offsetY);
        int duration = getActivity().getResources().getInteger(android.R.integer.config_mediumAnimTime);
        mShieldOffsetYAnim.setDuration(duration);
        mShieldOffsetYAnim.setInterpolator(new LinearInterpolator());
        animators.add(mShieldOffsetYAnim);

        int offsetX = mHomeAnimView.getShieldLayer().getMaxOffsetX();
        LeoLog.e("Home", "offsetX:" + offsetX);
        mShieldOffsetXAnim = ObjectAnimator.ofInt(mHomeAnimView, "shieldOffsetX", 0, offsetX);
        duration = getActivity().getResources().getInteger(android.R.integer.config_mediumAnimTime);
        mShieldOffsetXAnim.setDuration(duration);
        mShieldOffsetXAnim.setInterpolator(new LinearInterpolator());
        animators.add(mShieldOffsetXAnim);

        // 盾牌缩小
        ObjectAnimator shieldScaleAnim = ObjectAnimator.ofFloat(mHomeAnimView, "shieldScaleRatio",
                HomeAnimShieldLayer.MIN_SHIELD_SCALE_RATIO, HomeAnimShieldLayer.SHIELD_SCANNING_RATIO);
        shieldScaleAnim.setInterpolator(new LinearInterpolator());
        shieldScaleAnim.setDuration(100);
        animators.add(shieldScaleAnim);
        // 盾牌透明
        ObjectAnimator alphaAnim = ObjectAnimator.ofInt(mHomeAnimView.getShieldLayer(), "shieldAlpha", 255, 0);
        alphaAnim.setInterpolator(new LinearInterpolator());
        alphaAnim.setDuration(100);
        animators.add(alphaAnim);
        // 内环缩小
        ObjectAnimator inScaleAnim = ObjectAnimator.ofFloat(mHomeAnimView.getShieldLayer(), "inCircleScaleRatio",
                HomeAnimShieldLayer.MAX_IN_CIRCLE_SCALE_RATIO, 0.4f);
        inScaleAnim.setInterpolator(new LinearInterpolator());
        inScaleAnim.setDuration(200);
        animators.add(inScaleAnim);
        // 内环透明
        ObjectAnimator inAlphaAnim = ObjectAnimator.ofInt(mHomeAnimView.getShieldLayer(), "inCircleAlpha", 255, 0);
        inAlphaAnim.setInterpolator(new LinearInterpolator());
        inAlphaAnim.setDuration(100);
        animators.add(inAlphaAnim);
        // 外环缩小至0.5
        ObjectAnimator outScaleAnim = ObjectAnimator.ofFloat(mHomeAnimView.getShieldLayer(), "outCircleScaleRatio", 1f, 0.5f);
        outScaleAnim.setInterpolator(new LinearInterpolator());
        outScaleAnim.setDuration(200);
        animators.add(outScaleAnim);
        // 虚线框缩小至0.5
        ObjectAnimator dashScaleAnim = ObjectAnimator.ofFloat(mHomeAnimView.getShieldLayer(), "scanningScale", 1f, 0.64f);
        dashScaleAnim.setInterpolator(new LinearInterpolator());
        dashScaleAnim.setDuration(200);
        animators.add(dashScaleAnim);
        // 虚线框透明度
        mHomeAnimView.getShieldLayer().setDashAlpha(0);
        ObjectAnimator dashAlphaAnim = ObjectAnimator.ofInt(mHomeAnimView.getShieldLayer(), "dashAlpha", 0, 255);
        dashAlphaAnim.setInterpolator(new LinearInterpolator());
        dashAlphaAnim.setStartDelay(350);
        dashAlphaAnim.setDuration(100);
        animators.add(dashAlphaAnim);

        mMoveLeftTopAnim = new AnimatorSet();
        mMoveLeftTopAnim.playTogether(animators);
        mMoveLeftTopAnim.start();
        mMoveLeftTopAnim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
    }

    /**
     * 启动从扫描页面，直接跳转到隐私建议页面的动画
     */
    public void startDirectBurstAnim() {
        LeoLog.i("tesi", "startDirectBurstAnim");
        // 启动翻转、火花动画
        final HomeAnimShieldLayer shieldLayer = mHomeAnimView.getShieldLayer();
        shieldLayer.getFlipDecor().startFlipAnim(1000, new ShieldFlipDecor.OnFlipEndListener() {
            @Override
            public void OnFlipEnd() {
                LeoLog.i("TheTest", "getFlipDecor mIsReset:" + mIsReset);
                if (mIsReset) {
                    return;
                }
                LeoLog.i("TheTest", "Burst");
                shieldLayer.getBurstDecor().startBurstAnim(500, new BurstDecor.OnBurstEndListener() {

                    @Override
                    public void OnBurstEnd() {
                        LeoLog.i("TheTest", "getBurstDecor mIsReset:" + mIsReset);
                        if (!mIsReset) {
                            LeoLog.i("TheTest", "jumpToNextFragment ");
                            mActivity.jumpToNextFragment(true);
                            startDirectTranslation();
                        }
                    }
                });

                mFullScoreStart = ObjectAnimator.ofFloat(mHomeAnimView.getShieldLayer(), "firstWaveRatio", 0f, 3f);
                mFullScoreStart.setDuration(500);
                mFullScoreStart.setInterpolator(new LinearInterpolator());
                mFullScoreStart.start();
            }
        });
    }

    public void startDirectTranslation() {
        int duration = 580;
        List<Animator> animators = new ArrayList<Animator>();

        ObjectAnimator shieldRatio = ObjectAnimator.ofFloat(mHomeAnimView.getShieldLayer(),
                "finalShieldRatio", 0f, 1f);
        shieldRatio.setDuration(duration);
        shieldRatio.setInterpolator(new LinearInterpolator());
        animators.add(shieldRatio);

        // 盾牌向右移动
        HomeAnimShieldLayer shieldLayer = mHomeAnimView.getShieldLayer();
        int maxOffsetX = shieldLayer.getMaxOffsetX();
        ObjectAnimator shieldTran = ObjectAnimator.ofInt(shieldLayer, "shieldOffsetX", maxOffsetX, 0);
        shieldTran.setDuration(duration);
        shieldTran.setInterpolator(new LinearInterpolator());
        animators.add(shieldTran);

        // 盾牌以透明
        ObjectAnimator alphaAnim = ObjectAnimator.ofInt(mHomeAnimView.getShieldLayer(), "shieldAlpha", 255, 0);
        alphaAnim.setInterpolator(new LinearInterpolator());
        alphaAnim.setDuration(duration);
        animators.add(alphaAnim);

        // 分数变大
        ObjectAnimator textFinalAnim = ObjectAnimator.ofFloat(mHomeAnimView.getShieldLayer(),
                "finalTextRatio", 0.76f, 1.6f);
        textFinalAnim.setDuration(duration);
        textFinalAnim.setInterpolator(new LinearInterpolator());
        animators.add(textFinalAnim);

        mFullScoreMove = new AnimatorSet();
        mFullScoreMove.playTogether(animators);
        mFullScoreMove.start();
    }

    /**
     * 开启隐私完成页面的盾牌上移、分数放大动画
     */
    public void startFinalAnim() {
        ObjectAnimator shieldRatio = ObjectAnimator.ofFloat(mHomeAnimView.getShieldLayer(),
                "finalShieldRatio", 0f, 1f);
        shieldRatio.setDuration(480);
        shieldRatio.setInterpolator(new LinearInterpolator());

        ObjectAnimator textRatioBig1 = ObjectAnimator.ofFloat(mHomeAnimView.getShieldLayer(),
                "finalTextRatio", 0.76f, 1.34f);
        textRatioBig1.setDuration(720);
        textRatioBig1.setInterpolator(new LinearInterpolator());

        ObjectAnimator textRatioBig2 = ObjectAnimator.ofFloat(mHomeAnimView.getShieldLayer(),
                "finalTextRatio", 1.34f, 0.76f);
        textRatioBig2.setDuration(320);
        textRatioBig2.setInterpolator(new LinearInterpolator());

        float maxRatio = 1.1f;
        if (mHomeAnimView.getStepLayer().getTotalStepCount() == 1) {
            maxRatio = 1.6f;
        }
        ObjectAnimator textRatioSmall = ObjectAnimator.ofFloat(mHomeAnimView.getShieldLayer(),
                "finalTextRatio", 0.76f, maxRatio);
        textRatioSmall.setDuration(200);
        textRatioSmall.setInterpolator(new LinearInterpolator());

        AnimatorSet textAnim = new AnimatorSet();
        textAnim.playSequentially(textRatioBig1, textRatioBig2, textRatioSmall);

        AnimatorSet finalAnim = new AnimatorSet();
        finalAnim.playTogether(shieldRatio, textAnim);
        finalAnim.start();
        mFinalAnim = finalAnim;
    }

    public void stopFinalAnim() {
        if (mFinalAnim != null) {
            mFinalAnim.cancel();
            mFinalAnim = null;
        }
    }

    private void startWaveAnim() {
        if (!mHandler.hasMessages(ID_FAST_ARROW) && mStopped && mFistAnimFinished) {
            mHandler.sendEmptyMessageDelayed(ID_FAST_ARROW, 3000);
        }
        if (mAnimatorSet != null) {
            mAnimatorSet.start();
        }
        mHomeAnimView.startAfterLayout(null);
    }

    private void endWaveAnim() {
        mHandler.removeMessages(ID_FAST_ARROW);
        if (mAnimatorSet != null) {
            mAnimatorSet.end();
            mAnimatorSet = null;
        }
        endAnim(mCircleRotateAnim);

        endAnim(mMoveLeftTopAnim);

        endAnim(mFullScoreStart);

        endAnim(mFullScoreMove);

    }

    private void postFastArrowAnim() {
        // 快速滚动条
        int width = mHomeAnimView.getFastArrowWidth() + mWidth;
        ObjectAnimator fastArrowAnim = ObjectAnimator.ofInt(mHomeAnimView, "fastProgress", 0, width);
        fastArrowAnim.setDuration(1000);
        fastArrowAnim.setInterpolator(new LinearInterpolator());
        fastArrowAnim.start();
        mHandler.sendEmptyMessageDelayed(ID_FAST_ARROW, 3000);
    }

    public void reset() {


        mIsReset = true;

        LeoLog.i("TheTest", "reset mIsReset:" + mIsReset);

        endAnim(mMoveLeftTopAnim);
        final HomeAnimShieldLayer shieldLayer = mHomeAnimView.getShieldLayer();
        if (mFullScoreStart != null) {
            mFullScoreStart.cancel();
            mFullScoreStart.end();
            mFullScoreStart = null;
        }
        if (mFullScoreMove != null) {
            mFullScoreMove.cancel();
            mFullScoreMove.end();
            mFullScoreMove = null;
        }
        shieldLayer.getFlipDecor().end();
        shieldLayer.getBurstDecor().end();


        showScanningPercent(-1);
        stopFinalAnim();
        mHomeAnimView.setShowStep(false);
        mHomeAnimView.setShowProcessLoading(false, 0);

        shieldLayer.setFinalShieldRatio(0);
        shieldLayer.setFinalTextRatio(HomeAnimShieldLayer.MIN_SHIELD_SCALE_RATIO);
        shieldLayer.setInCircleScaleRatio(HomeAnimShieldLayer.MAX_IN_CIRCLE_SCALE_RATIO);
        shieldLayer.setOutCircleScaleRatio(HomeAnimShieldLayer.MAX_OUT_CIRCLE_SCALE_RATIO);
        shieldLayer.setShieldScale(HomeAnimShieldLayer.MIN_SHIELD_SCALE_RATIO);
        shieldLayer.setInCircleAlpha(255);
        shieldLayer.setOutCircleAlpha(255);
        shieldLayer.setShieldAlpha(255);

        if (mShieldOffsetYAnim != null) {
            mShieldOffsetYAnim.end();
        }
        mHomeAnimView.setShieldOffsetY(0);
        mHomeAnimView.setShieldOffsetX(0);
    }

    private void endAnim(Animator animator) {
        if (animator != null) {
            animator.cancel();
            animator.end();
            animator = null;
        }
    }

    public int getToolbarColor() {
        return mHomeAnimView.getToolbarColor();
    }

    public void setInterceptRaiseAnim() {
        mInterceptRaise = true;
    }

    private static class FastHandler extends Handler {
        WeakReference<HomePrivacyFragment> weakRef;

        FastHandler(HomePrivacyFragment fragment) {
            weakRef = new WeakReference<HomePrivacyFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ID_FAST_ARROW:
                    HomePrivacyFragment fragment = weakRef.get();
                    if (fragment == null) break;

                    fragment.postFastArrowAnim();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

}
