package com.leo.appmaster.battery;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Jasper on 2016/3/1.
 */
public class BatteryBoostController extends RelativeLayout {
    private static final String TAG = "BatteryBoostController";

    private static final int BOOST_SIZE = 6;
    private static final int BOOST_ITEM_DURATION = 300;
    private static final int MIN_BOOST_NUM = 6;
    private ImageView mShieldIv;
    private CircleArroundView mShieldCircle;
    private BatteryBoostAnimView mBoostAnimView;
    private View mShieldRootView;
    private boolean mHasPlayed = false;

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

    private LinearLayout mBoostResultLayout;
    private TextView mBoostResultText;

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

        mBoostResultLayout = (LinearLayout) findViewById(R.id.charge_clean_result_layout);
        mBoostResultText = (TextView) findViewById(R.id.charge_clean_result_txt);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (!mHasPlayed) {
            mHasPlayed = true;
            ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startBoost();
                }
            }, 500);
        }
    }

    public void startBoost() {
        List<AppItemInfo> list = AppLoadEngine.getInstance(getContext()).getAllPkgInfo();
        List<AppItemInfo> appItemInfos = new ArrayList<AppItemInfo>(BOOST_SIZE);
        if (list.size() > BOOST_SIZE) {
            // 随机取
            float seed = (float) Math.random();
            int start = (int) ((float) (list.size() - 1) * seed);
            for (int i = start; i < list.size() + start; i++) {
                int index = i;
                if (index >= list.size()) {
                    index -= list.size();
                }

                AppItemInfo itemInfo = list.get(index);
                if (itemInfo.packageName == null || itemInfo.packageName.startsWith("com.leo.")) {
                    // 排出掉公司自研产品
                    continue;
                }
                appItemInfos.add(list.get(index));
                if (appItemInfos.size() >= BOOST_SIZE) {
                    break;
                }
            }
        } else {
            appItemInfos.addAll(list);
        }

        Iterator<AppItemInfo> iterator = appItemInfos.iterator();
        final AppItemInfo itemInfo = iterator.next();
        iterator.remove();

        ImageView target = getNext(null);
        target.setImageDrawable(AppUtil.getAppIcon(itemInfo.packageName));
        Animator animatorSet = getTotalAnimator(iterator, target, iterator.hasNext());
        animatorSet.start();

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                BatteryManager batteryManager = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);
                List<BatteryComsuption> beforeBoost = batteryManager.getBatteryDrainApps();

                ProcessCleaner cleaner = ProcessCleaner.getInstance(getContext());
                cleaner.tryClean(getContext());
                List<BatteryComsuption> afterBoost = batteryManager.getBatteryDrainApps();

                mCleanedNum = beforeBoost.size() - afterBoost.size();
            }
        });
    }

    public void setBoostFinishListener(OnBoostFinishListener listener) {
        mListener = listener;
    }

    private void continueTranslateDelay(final ImageView target, final Iterator iterator) {
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animator continueSet = null;

                final boolean hasNext = iterator.hasNext();
                if (hasNext) {
                    ImageView next = getNext(target);
                    AppItemInfo itemInfo = (AppItemInfo) iterator.next();
                    iterator.remove();
                    next.setImageDrawable(AppUtil.getAppIcon(itemInfo.packageName));

                    continueSet = getTotalAnimator(iterator, next, iterator.hasNext());
                }
                Animator nextAnimator = getDismissAnimator(target, hasNext);
                AnimatorSet animatorSet = null;
                if (continueSet != null) {
                    animatorSet = new AnimatorSet();
                    animatorSet.playTogether(continueSet, nextAnimator);
                    animatorSet.start();
                } else {
                    nextAnimator.start();
                }
            }
        }, 100);
    }

    private Animator getTotalAnimator(final Iterator iterator, final ImageView target, boolean hasNext) {
        float translation = getTranslation();
        float transFrom = -translation;
        float transTo = 0;

        ObjectAnimator transAnim = ObjectAnimator.ofFloat(target, "translationY", transFrom, transTo);

        float alpha = 0.5f;
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(target, "alpha", alpha, 1f);

        float scale = 0.5f;
        float scaleFrom = scale;
        float scaleTo = 1.2f;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, "scaleX", scaleFrom, scaleTo);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, "scaleY", scaleFrom, scaleTo);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(transAnim, alphaAnim, scaleX, scaleY);
        animatorSet.setDuration(BOOST_ITEM_DURATION);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                LeoLog.d(TAG, "onAnimationEnd, target: " + target);
                continueTranslateDelay(target, iterator);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                target.setVisibility(View.VISIBLE);
                target.setRotation(0);
                LeoLog.d(TAG, "onAnimationStart, target: " + target);
            }
        });

        return animatorSet;
    }

    private Animator getDismissAnimator(ImageView target, final boolean hasNext) {
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(target, "rotation", 0f, 360f);
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(target, "scaleX", 1f, 0f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(target, "scaleY", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.playTogether(rotateAnim, scaleXAnim, scaleYAnim);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!hasNext) {
                    onBoostFinish();
                }
            }
        });

        return animatorSet;
    }

    private ImageView getNext(ImageView target) {
        if (mAnimIv1 == target) {
            return mAnimIv2;
        } else if (mAnimIv2 == target) {
            return mAnimIv3;
        } else if (mAnimIv3 == target) {
            return mAnimIv4;
        } else if (mAnimIv4 == target) {
            return mAnimIv1;
        }
        return mAnimIv1;
    }

    private void onBoostFinish() {
        mBoostRl.setVisibility(View.INVISIBLE);
        mShieldRootView.setVisibility(View.VISIBLE);
        mShieldCircle.setVisibility(View.INVISIBLE);
        startBoostDismissAnim();
    }

    private void startBoostDismissAnim() {
        ObjectAnimator shieldAlpha = ObjectAnimator.ofFloat(mShieldRootView, "alpha", 0f, 255f);
        ObjectAnimator shieldScaleX = ObjectAnimator.ofFloat(mShieldRootView, "scaleX", 0.8f, 1f);
        ObjectAnimator shieldScaleY = ObjectAnimator.ofFloat(mShieldRootView, "scaleY", 0.8f, 1f);

        ObjectAnimator boostAlpha = ObjectAnimator.ofFloat(mBoostAnimView, "alpha", 255f, 0f);
        ObjectAnimator boostScaleX = ObjectAnimator.ofFloat(mBoostAnimView, "scaleX", 1f, 0.8f);
        ObjectAnimator boostScaleY = ObjectAnimator.ofFloat(mBoostAnimView, "scaleY", 1f, 0.8f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(shieldAlpha, shieldScaleX, shieldScaleY, boostAlpha, boostScaleX, boostScaleY);
        animatorSet.setDuration(400);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startShieldFlip(mShieldIv, mShieldCircle);
                mBoostAnimView.setVisibility(View.INVISIBLE);
            }
        });

        animatorSet.start();
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
                    cavCircle.setVisibility(View.VISIBLE);
                    cavCircle.startAnim(0f, -360f, 680l, new CircleArroundView.OnArroundFinishListener() {
                        @Override
                        public void onArroundFinish() {
                            ivShield.setVisibility(View.VISIBLE);
                            int number = mCleanedNum <= MIN_BOOST_NUM ? MIN_BOOST_NUM : mCleanedNum;
                            double seed = Math.random();
                            int minites = (int) (number + ((float) number * seed));
                            String boostResult = getContext().getString(R.string.battery_boost_result, minites, number);
//                            mBoostToastTv.setText(boostResult);
                            mBoostResultText.setText(Html.fromHtml(boostResult));
                            mBoostToastTv.setVisibility(View.GONE);
                            mBoostResultLayout.setVisibility(View.VISIBLE);
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
                setVisibility(View.INVISIBLE);
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
