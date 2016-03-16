package com.leo.appmaster.battery;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.animation.AnimationListenerAdapter;
import com.leo.appmaster.animation.ThreeDimensionalRotationAnimation;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.ui.CircleArroundView;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.DipPixelUtil;
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

    private static final int BOOST_SIZE = 3;
    private static final int BOOST_ITEM_DURATION = 300;
    private static final int MIN_BOOST_NUM = 3;
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

    private ImageView mAnimCenterImg;
    private ImageView mAnimImg1;
    private ImageView mAnimImg2;
    private ImageView mAnimImg3;
    private ImageView mAnimImg4;
    private ImageView mAnimImg5;

    private static final float DEGREE_0 = 1.8f;
    private static final float DEGREE_1 = -2.0f;
    private static final float DEGREE_2 = 2.0f;
    private static final float DEGREE_3 = -1.5f;
    private static final float DEGREE_4 = 1.5f;
    private static final int SHAKE_DURATION = 30; //振动频率
    private int mCount = 0;
    private AnimatorSet imageAnim1;
    private AnimatorSet imageAnim2;
    private AnimatorSet imageAnim3;
    private AnimatorSet imageAnim4;
    private AnimatorSet imageAnim5;



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

        mAnimCenterImg = (ImageView) findViewById(R.id.boost_center_img);
        mAnimImg1 = (ImageView) findViewById(R.id.boost_anim_img1);
        mAnimImg2 = (ImageView) findViewById(R.id.boost_anim_img2);
        mAnimImg3 = (ImageView) findViewById(R.id.boost_anim_img3);
        mAnimImg4 = (ImageView) findViewById(R.id.boost_anim_img4);
        mAnimImg5 = (ImageView) findViewById(R.id.boost_anim_img5);

    }

    private void setShakeAnimation(final View v) {
        float rotate = 0;
        int c = mCount++ % 5;
        if (c == 0) {
            rotate = DEGREE_0;
        } else if (c == 1) {
            rotate = DEGREE_1;
        } else if (c == 2) {
            rotate = DEGREE_2;
        } else if (c == 3) {
            rotate = DEGREE_3;
        } else {
            rotate = DEGREE_4;
        }
        int centerPoint =  DipPixelUtil.dip2px(AppMasterApplication.getInstance(), 40);
        final RotateAnimation mra = new RotateAnimation(
                rotate, -rotate,  centerPoint / 2, centerPoint / 2);
        final RotateAnimation mrb = new RotateAnimation(
                -rotate, rotate, centerPoint / 2, centerPoint / 2);

        mra.setDuration(SHAKE_DURATION);
        mrb.setDuration(SHAKE_DURATION);

        mra.setAnimationListener(new AnimationListenerAdapter() {

            @Override
            public void onAnimationEnd(Animation animation) {
                mra.reset();
                v.startAnimation(mrb);
            }
        });

        mrb.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mrb.reset();
                v.startAnimation(mra);
            }


        });
        v.startAnimation(mra);
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

    private void startBoostAnim(List<AppItemInfo> list) {

        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(mAnimCenterImg, "rotation", 0f, 360f);
        rotateAnim.setRepeatCount(10);
        rotateAnim.setDuration(208);
        rotateAnim.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(mAnimCenterImg, "scaleX", 1f, 0f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(mAnimCenterImg, "scaleY", 1f, 0f);
        AnimatorSet scaleAnimatorSet = new AnimatorSet();
        scaleAnimatorSet.playTogether(scaleXAnim, scaleYAnim);
        scaleAnimatorSet.setDuration(520);
        scaleAnimatorSet.setStartDelay(1540);
        scaleAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                clearShakeAnimation(mAnimImg1);
                clearShakeAnimation(mAnimImg2);
                clearShakeAnimation(mAnimImg3);
                clearShakeAnimation(mAnimImg4);
                clearShakeAnimation(mAnimImg5);
                onBoostFinish();
            }
        });

        AppItemInfo appInfo;
        for (int i = 0; i < list.size() ; i++) {
            appInfo = list.get(i);
            if (i == 0) {
                imageAnim1 = getIconAnim(mAnimImg1);
                mAnimImg1.setImageDrawable(AppUtil.getAppIcon(appInfo.packageName));
                mAnimImg1.setVisibility(View.VISIBLE);
            } else if (i == 1) {
                imageAnim2 = getIconAnim(mAnimImg2);
                mAnimImg2.setImageDrawable(AppUtil.getAppIcon(appInfo.packageName));
                mAnimImg2.setVisibility(View.VISIBLE);
            } else if (i == 2) {
                imageAnim3 = getIconAnim(mAnimImg3);
                mAnimImg3.setImageDrawable(AppUtil.getAppIcon(appInfo.packageName));
                mAnimImg3.setVisibility(View.VISIBLE);
            } else if (i == 3) {
                imageAnim4 = getIconAnim(mAnimImg4);
                mAnimImg4.setImageDrawable(AppUtil.getAppIcon(appInfo.packageName));
                mAnimImg4.setVisibility(View.VISIBLE);
            } else if (i == 4) {
                imageAnim5 = getIconAnim(mAnimImg5);
                mAnimImg5.setImageDrawable(AppUtil.getAppIcon(appInfo.packageName));
                mAnimImg5.setVisibility(View.VISIBLE);
            }
        }

        AnimatorSet boostAnimatorSet = new AnimatorSet();
        boostAnimatorSet.play(rotateAnim).with(scaleAnimatorSet);
        if (imageAnim1 != null) {
            boostAnimatorSet.play(scaleAnimatorSet).with(imageAnim1);
        }
        if (imageAnim2 != null) {
            boostAnimatorSet.play(imageAnim1).with(imageAnim2);
        }
        if (imageAnim3 != null) {
            boostAnimatorSet.play(imageAnim2).with(imageAnim3);
        }
        if (imageAnim4 != null) {
            boostAnimatorSet.play(imageAnim3).with(imageAnim4);
        }
        if (imageAnim5 != null) {
            boostAnimatorSet.play(imageAnim4).with(imageAnim5);
        }
        boostAnimatorSet.start();

    }

    private void clearShakeAnimation(ImageView view) {
        if (view != null) {
            view.clearAnimation();
        }
    }

    private AnimatorSet getIconAnim(ImageView view) {
        float centerX = mAnimCenterImg.getLeft() + mAnimCenterImg.getWidth() / 2
                        - (view.getLeft() + view.getWidth() / 2);
        float centerY = mAnimCenterImg.getTop() + mAnimCenterImg.getHeight() / 2
                        - (view.getTop() + view.getHeight() / 2);
        ObjectAnimator moveXAnim =ObjectAnimator.ofFloat(view, "translationX", centerX);
        ObjectAnimator moveYAnim =ObjectAnimator.ofFloat(view, "translationY", centerY);
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.6f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.6f);
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveXAnim, moveYAnim, scaleXAnim, scaleYAnim, alphaAnim);
        animatorSet.setDuration(520);
        animatorSet.setStartDelay(520);
        setShakeAnimation(view);

        return  animatorSet;
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


        startBoostAnim(appItemInfos);


//        ImageView target = getNext(null);
//        target.setImageDrawable(AppUtil.getAppIcon(itemInfo.packageName));
//        Animator animatorSet = getTotalAnimator(iterator, target, iterator.hasNext());
//        animatorSet.start();

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

    private ImageView getNextImage(ImageView target) {
        if (mAnimImg1 == target) {
            return mAnimImg2;
        } else if (mAnimImg2 == target) {
            return mAnimImg3;
        } else if (mAnimImg3 == target) {
            return mAnimImg4;
        } else if (mAnimImg4 == target) {
            return mAnimImg5;
        } else if (mAnimImg5 == target) {
            return  mAnimImg1;
        }
        return mAnimImg1;
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
