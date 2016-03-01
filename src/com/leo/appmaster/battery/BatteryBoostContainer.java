package com.leo.appmaster.battery;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.ObjectAnimator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jasper on 2016/3/1.
 */
public class BatteryBoostContainer extends RelativeLayout {
    private static final String TAG = "BatteryBoostContainer";

    private static final int STATE_BEGIN = 0;
    private static final int STATE_END = 1;
    private static final int STATE_NROMAL = 2;

    private static final int BOOST_SIZE = 10;
    private static final int BOOST_ITEM_DURATION = 1000;

    public interface OnBoostFinishListener {
        public void onBoostFinish();
    }

    private ImageView mAnimIv1;
    private ImageView mAnimIv2;
    private ImageView mAnimIv3;
    private ImageView mAnimIv4;

    private View mBoostRl;

    private boolean mStarted = true;

    private LinkedBlockingQueue<ImageView> mIdleViews;

    private int mCount = 10;
    private List<AppItemInfo> mItemsInfo;

    private OnBoostFinishListener mListener;

    public BatteryBoostContainer(Context context, AttributeSet attrs) {
        super(context, attrs);

        mIdleViews = new LinkedBlockingQueue<ImageView>(3);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAnimIv1 = (ImageView) findViewById(R.id.boost_anim_iv1);
        mAnimIv2 = (ImageView) findViewById(R.id.boost_anim_iv2);
        mAnimIv3 = (ImageView) findViewById(R.id.boost_anim_iv3);
        mAnimIv4 = (ImageView) findViewById(R.id.boost_anim_iv4);
        mBoostRl = findViewById(R.id.boost_anim_rl);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        startBoost();
    }

    public void startBoost() {
        mStarted = true;
        Drawable drawable = getContext().getResources().getDrawable(R.drawable.ic_launcher);

        List<AppItemInfo> list = AppLoadEngine.getInstance(getContext()).getAllPkgInfo();
        List<AppItemInfo> appItemInfos = new ArrayList<AppItemInfo>(BOOST_SIZE);
        if (list.size() > BOOST_SIZE) {
            appItemInfos.addAll(list.subList(0, BOOST_SIZE));
        } else {
            appItemInfos.addAll(list);
        }

        mItemsInfo = appItemInfos;
        startTranslate(STATE_NROMAL, drawable, mAnimIv1, appItemInfos.iterator());
    }

    public void setBoostFinishListener(OnBoostFinishListener listener) {
        mListener = listener;
    }

    private void startTranslate(final int state, final Drawable drawable, final ImageView target, final Iterator<AppItemInfo> iterator) {
        if (!iterator.hasNext()) {
            return;
        }
        AppItemInfo itemInfo = iterator.next();
        iterator.remove();

        final boolean hasNext = iterator.hasNext();
        float translation = mBoostRl.getHeight() / 2;
        LeoLog.d(TAG, "startTranslate, translation: " + translation + " | state: " + state);

        float start = -translation;
        float end = translation;

        target.setVisibility(View.VISIBLE);
        target.setImageDrawable(AppUtil.getAppIcon(itemInfo.packageName));
        ObjectAnimator iv1Anim = ObjectAnimator.ofFloat(target, "translationY", start, end);
        iv1Anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                LeoLog.d(TAG, "onAnimationEnd, add target: " + target);
                target.setVisibility(View.INVISIBLE);
                if (!hasNext) {
                    if (mListener != null) {
                        mListener.onBoostFinish();
                    }
                    return;
                }
                startTranslate(STATE_NROMAL, drawable, getTargetNextGroup(target), iterator);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                target.setVisibility(View.VISIBLE);
            }
        });
        iv1Anim.setInterpolator(new LinearInterpolator());
        iv1Anim.setDuration(BOOST_ITEM_DURATION);
        iv1Anim.start();

        startTranslatePaired(state, target, iterator);
    }

    private void startTranslatePaired(final int state, ImageView target, final Iterator<AppItemInfo> iterator) {
        if (!iterator.hasNext()) {
            return;
        }

        AppItemInfo itemInfo = iterator.next();
        iterator.remove();

        final boolean hasNext = iterator.hasNext();
        float translation = mBoostRl.getHeight() / 2;
        LeoLog.d(TAG, "startTranslate, translation: " + translation + " | state: " + state);

        float start = -translation;
        float end = translation;
        final ImageView nextTarget = getTargetCurrentGroup(target);
        nextTarget.setImageDrawable(AppUtil.getAppIcon(itemInfo.packageName));
        nextTarget.setVisibility(View.VISIBLE);
        ObjectAnimator iv2Anim = ObjectAnimator.ofFloat(nextTarget, "translationY", start, end);
        iv2Anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                nextTarget.setVisibility(View.INVISIBLE);
                if (!hasNext) {
                    if (mListener != null) {
                        mListener.onBoostFinish();
                    }
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
        iv2Anim.setStartDelay(BOOST_ITEM_DURATION / 2);
        iv2Anim.start();
    }

    private ImageView getTargetCurrentGroup(ImageView target) {
        return target == mAnimIv1 ? mAnimIv2 : mAnimIv4;
    }

    private ImageView getTargetNextGroup(ImageView target) {
        return target == mAnimIv1 ? mAnimIv3 : mAnimIv1;
    }
}
