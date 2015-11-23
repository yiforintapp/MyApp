package com.leo.appmaster.home;

import android.view.animation.LinearInterpolator;

import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.ui.ScanningImageView;
import com.leo.appmaster.ui.ScanningTextView;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.ValueAnimator;

/**
 * Created by Jasper on 2015/11/20.
 */
public class HomeScanningController {
    private static final int PER_APP = 30;
    private static final int PER_PIC = 70;
    private static final int PER_VID = 90;
    private static final int PER_PRI = 100;

    private static final int DURATION_ROTATE = 400;

    private static final int UP_LIMIT_APP = 1500;
    private static final int UP_LIMIT_PIC = 3000;
    private static final int UP_LIMIT_VID = 2000;

    private ScanningImageView mAppImg;
    private ScanningTextView mAppText;
    private ScanningImageView mPicImg;
    private ScanningTextView mPicText;
    private ScanningImageView mVidImg;
    private ScanningTextView mVidText;

    private ScanningImageView mPrivacyImg;
    private ScanningTextView mPrivacyText;

    private AnimEntry mAppAnim;
    private AnimEntry mPicAnim;
    private AnimEntry mVidAnim;
    private AnimEntry mPrivacyAnim;

    private HomeScanningFragment mFragment;
    private HomeActivity mActivity;

    private int mDurationPrivacy;

    public HomeScanningController(HomeActivity activity, HomeScanningFragment fragment,
                                  ScanningImageView appImg, ScanningTextView appText,
                                  ScanningImageView picImg, ScanningTextView picText,
                                  ScanningImageView vidImg, ScanningTextView vidText,
                                  ScanningImageView privacyImg, ScanningTextView privacyText) {
        mActivity = activity;
        mFragment = fragment;

        mAppImg = appImg;
        mAppText = appText;
        mPicImg = picImg;
        mPicText = picText;
        mVidImg = vidImg;
        mVidText = vidText;
        mPrivacyImg = privacyImg;
        mPrivacyText = privacyText;
    }

    private AnimEntry createScanningAnim(final ScanningImageView imageView, ScanningTextView textView) {
        AnimEntry entry = new AnimEntry();
        final AnimatorSet appAnimSet = new AnimatorSet();
        // 外环放大
        ObjectAnimator scaleImgAnim = ObjectAnimator.ofFloat(imageView, "scaleRatio", 0f, 1f);
        scaleImgAnim.setInterpolator(new LinearInterpolator());
        scaleImgAnim.setDuration(300);

        ObjectAnimator scaleTextAnim = ObjectAnimator.ofFloat(textView, "scaleRatio", 0f, 1f);
        scaleTextAnim.setInterpolator(new LinearInterpolator());
        scaleTextAnim.setDuration(300);

        AnimatorSet scaleAnim = new AnimatorSet();
        scaleAnim.playTogether(scaleImgAnim, scaleTextAnim);

        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(imageView, "rotateDegree", 1f, 360f);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.setDuration(DURATION_ROTATE);
        rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnim.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationRepeat(Animator animation) {
                onAnimatorRepeat(animation);
            }
        });

        appAnimSet.playSequentially(scaleAnim, rotateAnim);
        entry.totalAnim = appAnimSet;
        entry.rotateAnim = rotateAnim;

        ObjectAnimator innerScaleAnim = ObjectAnimator.ofFloat(imageView, "innerDrawableScale",
                ScanningImageView.INNER_SCALE, 1f);
        innerScaleAnim.setInterpolator(new LinearInterpolator());
        innerScaleAnim.setDuration(300);
        innerScaleAnim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimatorEnd(animation);
            }
        });

        entry.innerScaleAnim = innerScaleAnim;
        return entry;
    }


    private AnimEntry createPrivacyAnim(final ScanningImageView imageView, ScanningTextView textView) {
        AnimEntry entry = new AnimEntry();
        final AnimatorSet appAnimSet = new AnimatorSet();
        // 外环放大
        ObjectAnimator scaleImgAnim = ObjectAnimator.ofFloat(imageView, "scaleRatio", 0f, 1f);
        scaleImgAnim.setInterpolator(new LinearInterpolator());
        scaleImgAnim.setDuration(300);

        ObjectAnimator scaleTextAnim = ObjectAnimator.ofFloat(textView, "scaleRatio", 0f, 1f);
        scaleTextAnim.setInterpolator(new LinearInterpolator());
        scaleTextAnim.setDuration(300);

        AnimatorSet scaleAnim = new AnimatorSet();
        scaleAnim.playTogether(scaleImgAnim, scaleTextAnim);

        int repeatCount = 1;
        int duration = (repeatCount + 1) * DURATION_ROTATE;
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(imageView, "rotateDegree", 1f, 360f);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.setDuration(DURATION_ROTATE);
        rotateAnim.setRepeatCount(repeatCount);

        ObjectAnimator innerScaleAnim = ObjectAnimator.ofFloat(imageView, "innerDrawableScale",
                ScanningImageView.INNER_SCALE, 1f);
        innerScaleAnim.setInterpolator(new LinearInterpolator());
        innerScaleAnim.setDuration(300);
        innerScaleAnim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimatorEnd(animation);
            }
        });

        appAnimSet.playSequentially(scaleAnim, rotateAnim, innerScaleAnim);
        entry.totalAnim = appAnimSet;
        entry.rotateAnim = rotateAnim;
        entry.innerScaleAnim = innerScaleAnim;

        mDurationPrivacy = 300 + duration + 300;
        return entry;
    }

    public void startScanning() {
        mAppAnim = createScanningAnim(mAppImg, mAppText);

        mAppAnim.totalAnim.start();
        mFragment.scanningPercent(UP_LIMIT_APP, 0, PER_APP);
    }

    private void onAnimatorEnd(Animator animation) {
        if (animation == mAppAnim.innerScaleAnim) {
            mFragment.onAnimatorEnd(mAppImg);

            mPicAnim = createScanningAnim(mPicImg, mPicText);
            int currPct = mActivity.getScanningPercent();
            mFragment.scanningPercent(UP_LIMIT_PIC, currPct, PER_PIC);
            mPicAnim.totalAnim.start();
        } else if (animation == mPicAnim.innerScaleAnim) {
            mFragment.onAnimatorEnd(mPicImg);

            mVidAnim = createScanningAnim(mVidImg, mVidText);
            int currPct = mActivity.getScanningPercent();
            mFragment.scanningPercent(UP_LIMIT_VID, currPct, PER_VID);
            mVidAnim.totalAnim.start();
        } else if (animation == mVidAnim.innerScaleAnim) {
            mFragment.onAnimatorEnd(mVidImg);

            mPrivacyAnim = createPrivacyAnim(mPrivacyImg, mPrivacyText);
            int currPct = mActivity.getScanningPercent();
            mFragment.scanningPercent(mDurationPrivacy, currPct, PER_PRI + 1);
            mPrivacyAnim.totalAnim.start();
        } else {
            mFragment.onAnimatorEnd(mPrivacyImg);
        }
    }

    private void onAnimatorRepeat(Animator animator) {
        if (animator == mAppAnim.rotateAnim) {
            if (mFragment.isScanFinish(mAppImg)) {
                cancelAnimatorDelay(animator);
                mAppAnim.innerScaleAnim.start();
            }
        } else if (animator == mPicAnim.rotateAnim) {
            if (mFragment.isScanFinish(mPicImg)) {
                cancelAnimatorDelay(animator);
                mPicAnim.innerScaleAnim.start();
            }
        } else if (animator == mVidAnim.rotateAnim) {
            if (mFragment.isScanFinish(mVidImg)) {
                cancelAnimatorDelay(animator);
                mVidAnim.innerScaleAnim.start();
            }
        }
    }

    private void cancelAnimatorDelay(final Animator animator) {
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animator.end();
            }
        }, 100);
    }

    private class AnimEntry {
        public Animator totalAnim;
        public Animator rotateAnim;
        public Animator innerScaleAnim;
    }
}
