package com.leo.appmaster.battery;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.animation.ThreeDimensionalRotationAnimation;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.ui.CircleArroundView;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.ValueAnimator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jasper on 2016/3/1.
 */
public class BatteryBoostController extends RelativeLayout {
    private static final String TAG = "BatteryBoostContainer";

    private static final int BOOST_SIZE = 8;
    private static final int BOOST_ITEM_DURATION = 1500;
    private static final int MIN_BOOST_NUM = 5;
    private ImageView mShieldIv;
    private CircleArroundView mShieldCircle;
    private BatteryBoostAnimView mBoostAnimView;
    private View mShieldRootView;

    public interface OnBoostFinishListener {
        public void onBoostFinish();
    }

    private ImageView mAnimIv1;
    private ImageView mAnimIv2;
    private ImageView mAnimIv3;
    private ImageView mAnimIv4;

    private TextView mBoostToastTv;

    private View mBoostRl;

    private OnBoostFinishListener mListener;
    private int mCleanedNum;

    public BatteryBoostController(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAnimIv1 = (ImageView) findViewById(R.id.boost_anim_iv1);
        mAnimIv2 = (ImageView) findViewById(R.id.boost_anim_iv2);
        mAnimIv3 = (ImageView) findViewById(R.id.boost_anim_iv3);
        mAnimIv4 = (ImageView) findViewById(R.id.boost_anim_iv4);
        mBoostRl = findViewById(R.id.boost_anim_rl);
        if (Build.VERSION.SDK_INT < 21) {
            mBoostRl.setBackgroundResource(R.drawable.battery_boost_shape_below21);
        } else {
            mBoostRl.setBackgroundResource(R.drawable.battery_boost_gradient_shape);
        }

        mShieldIv = (ImageView) findViewById(R.id.iv_shield);
        mShieldCircle = (CircleArroundView) findViewById(R.id.cav_batterymain);

        mBoostAnimView = (BatteryBoostAnimView) findViewById(R.id.boost_anim_containor);
        mShieldRootView = findViewById(R.id.rl_shield);

        mBoostToastTv = (TextView) findViewById(R.id.boost_toast_tv);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startBoost();
            }
        }, 500);
    }

    public void startBoost() {
        Drawable drawable = getContext().getResources().getDrawable(R.drawable.ic_launcher);

        List<AppItemInfo> list = AppLoadEngine.getInstance(getContext()).getAllPkgInfo();
        List<AppItemInfo> appItemInfos = new ArrayList<AppItemInfo>(BOOST_SIZE);
        if (list.size() > BOOST_SIZE) {
            // 随机取
            float seed = (float) Math.random();
            int start = (int) ((float)(list.size() - 1) * seed);
            for (int i = start; i < list.size() + start; i++) {
                int index = i;
                if (index >= list.size()) {
                    index -= list.size();
                }
                appItemInfos.add(list.get(index));
                if (appItemInfos.size() >= BOOST_SIZE) {
                    break;
                }
            }
        } else {
            appItemInfos.addAll(list);
        }

        startTranslate(mAnimIv1, appItemInfos.iterator());

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                BatteryManager batteryManager = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);
                List<BatteryComsuption> beforeBoost = batteryManager.getBatteryDrainApps();

                ProcessCleaner cleaner = ProcessCleaner.getInstance(getContext());
                cleaner.tryClean(getContext());
                List<BatteryComsuption> afterBoost = batteryManager.getBatteryDrainApps();

                mCleanedNum = afterBoost.size() - beforeBoost.size();
            }
        });
    }

    public void setBoostFinishListener(OnBoostFinishListener listener) {
        mListener = listener;
    }

    private void startTranslate(final ImageView target, final Iterator<AppItemInfo> iterator) {
        if (!iterator.hasNext()) {
            return;
        }
        AppItemInfo itemInfo = iterator.next();
        iterator.remove();

        final boolean hasNext = iterator.hasNext();
        float translation = getTranslation();
        LeoLog.d(TAG, "startTranslate, translation: " + translation);

        float start = -translation;
        float end = translation;

        target.setImageDrawable(AppUtil.getAppIcon(itemInfo.packageName));
        ObjectAnimator iv1Anim = ObjectAnimator.ofFloat(target, "translationY", start, 0f, end);
        iv1Anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                LeoLog.d(TAG, "onAnimationEnd, add target: " + target);
                target.setVisibility(View.INVISIBLE);
                if (!hasNext) {
                    onBoostFinish();
                    return;
                }
                startTranslate(getTargetNextGroup(target), iterator);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                target.setVisibility(View.VISIBLE);
            }
        });
        iv1Anim.setInterpolator(new LinearInterpolator());
        iv1Anim.setDuration(BOOST_ITEM_DURATION);

        AnimatorSet animatorSet = getTotalAnimator(iv1Anim, target);
        animatorSet.start();
        startTranslatePaired(target, iterator);
    }

    private void startTranslatePaired(ImageView target, final Iterator<AppItemInfo> iterator) {
        if (!iterator.hasNext()) {
            return;
        }

        AppItemInfo itemInfo = iterator.next();
        iterator.remove();

        final boolean hasNext = iterator.hasNext();
        float translation = getTranslation();
        LeoLog.d(TAG, "startTranslatePaired, translation: " + translation);

        float start = -translation;
        float end = translation;
        final ImageView nextTarget = getTargetCurrentGroup(target);
        nextTarget.setImageDrawable(AppUtil.getAppIcon(itemInfo.packageName));
        ObjectAnimator iv2Anim = ObjectAnimator.ofFloat(nextTarget, "translationY", start, end);
        iv2Anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                nextTarget.setVisibility(View.INVISIBLE);
                if (!hasNext) {
                    onBoostFinish();
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                nextTarget.setVisibility(View.VISIBLE);
            }
        });
        iv2Anim.setInterpolator(new LinearInterpolator());
        iv2Anim.setDuration(BOOST_ITEM_DURATION);

        AnimatorSet animatorSet = getTotalAnimator(iv2Anim, nextTarget);
        animatorSet.setStartDelay(BOOST_ITEM_DURATION / 2);
        animatorSet.start();
    }

    private AnimatorSet getTotalAnimator(ObjectAnimator animator, ImageView target) {
        float alpha = 0.5f;
        ObjectAnimator alpha1 = ObjectAnimator.ofFloat(target, "alpha", alpha, 1f);
        alpha1.setInterpolator(new LinearInterpolator());
        alpha1.setDuration(BOOST_ITEM_DURATION / 2);

        ObjectAnimator alpha2 = ObjectAnimator.ofFloat(target, "alpha", 1f, alpha);
        alpha2.setInterpolator(new LinearInterpolator());
        alpha2.setDuration(BOOST_ITEM_DURATION / 2);
        AnimatorSet alphaSet = new AnimatorSet();
        alphaSet.playSequentially(alpha1, alpha2);

        float scale = 0.5f;
        ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(target, "scaleX", scale, 1f);
        scaleX1.setInterpolator(new LinearInterpolator());
        scaleX1.setDuration(BOOST_ITEM_DURATION / 2);
        ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(target, "scaleY", scale, 1f);
        scaleY1.setInterpolator(new LinearInterpolator());
        scaleY1.setDuration(BOOST_ITEM_DURATION / 2);
        AnimatorSet scale1Set = new AnimatorSet();
        scale1Set.playTogether(scaleX1, scaleY1);

        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(target, "scaleX", 1f, scale);
        scaleX2.setInterpolator(new LinearInterpolator());
        scaleX2.setDuration(BOOST_ITEM_DURATION / 2);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(target, "scaleY", 1f, scale);
        scaleY2.setInterpolator(new LinearInterpolator());
        scaleY2.setDuration(BOOST_ITEM_DURATION / 2);
        AnimatorSet scale2Set = new AnimatorSet();
        scale2Set.playTogether(scaleX2, scaleY2);

        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.playSequentially(scale1Set, scale2Set);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator, alphaSet, scaleSet);

        return animatorSet;
    }

    private ImageView getTargetCurrentGroup(ImageView target) {
        return target == mAnimIv1 ? mAnimIv2 : mAnimIv4;
    }

    private ImageView getTargetNextGroup(ImageView target) {
        return target == mAnimIv1 ? mAnimIv3 : mAnimIv1;
    }

    private void onBoostFinish() {
        mBoostRl.setVisibility(View.INVISIBLE);
        mBoostAnimView.setVisibility(View.INVISIBLE);
        mShieldRootView.setVisibility(View.VISIBLE);
        startShieldFlip(mShieldIv, mShieldCircle);
    }

    private void startShieldFlip(final ImageView ivShield, final CircleArroundView cavCircle) {
        final float centerX = ivShield.getWidth() / 2.0f;
        final float centerY = ivShield.getHeight() / 2.0f;
        if (centerX != 0f && centerY != 0f) {
            final ThreeDimensionalRotationAnimation rotation = new ThreeDimensionalRotationAnimation(-90, 0,
                    centerX, centerY, 0.0f, true);
            rotation.setDuration(680);
            rotation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    cavCircle.startAnim(0f, -360f, 680l, new CircleArroundView.OnArroundFinishListener() {
                        @Override
                        public void onArroundFinish() {
                            ivShield.setVisibility(View.VISIBLE);
                            int number = mCleanedNum <= 0 ? MIN_BOOST_NUM : mCleanedNum;
                            double seed = Math.random();
                            int minites = (int) (number + ((float)number * seed));
                            String boostResult = getContext().getString(R.string.battery_boost_result, minites, number);
                            mBoostToastTv.setText(boostResult);
                            BatteryBoostController.this.onArroundFinish();
                        }
                    });
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ivShield.setVisibility(View.VISIBLE);
                }
            });
            rotation.setFillAfter(false);
            ivShield.startAnimation(rotation);
        }
    }

    private void onArroundFinish() {
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onBoostFinish();
                }
            }
        }, 2000);
    }

    private int getTranslation() {
        int harfH = mBoostRl.getHeight() / 2;
        int padding = getPaddingTop();
        return harfH - padding;
    }
}
