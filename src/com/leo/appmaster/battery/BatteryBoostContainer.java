package com.leo.appmaster.battery;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.ObjectAnimator;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jasper on 2016/3/1.
 */
public class BatteryBoostContainer extends RelativeLayout {
    private static final String TAG = "BatteryBoostContainer";

    private static final int STATE_BEGIN = 0;
    private static final int STATE_END = 1;
    private static final int STATE_NROMAL = 2;

    private ImageView mAnimIv1;
    private ImageView mAnimIv2;
    private ImageView mAnimIv3;
    private ImageView mAnimIv4;

    private View mBoostRl;

    private boolean mStarted = true;

    private LinkedBlockingQueue<ImageView> mIdleViews;

    private int mCount = 10;

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
//        mAnimIv3 = (ImageView) findViewById(R.id.boost_anim_iv3);

//        mIdleViews.add(mAnimIv1);
//        mIdleViews.add(mAnimIv2);
//        mIdleViews.add(mAnimIv3);
//        startBoost();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        startBoost();
    }

    public void startBoost() {
        mStarted = true;
        invalidate();

        Drawable drawable = getContext().getResources().getDrawable(R.drawable.ic_launcher);

//        ImageView imageView = mIdleViews.poll();
        startTranslate(STATE_NROMAL, drawable, mAnimIv1);
    }

    private void startTranslate(final int state, final Drawable drawable, final ImageView target) {
        float translation = mBoostRl.getHeight() / 2;
        LeoLog.d(TAG, "startTranslate, translation: " + translation + " | state: " + state);

        float start = -translation;
        float end = translation;
        int duration = 1000;
//        float startNextValue = 0;
//        if (state == STATE_BEGIN) {
//            start = 0;
//            end = translation;
//            duration /= 2;
//        } else if (state == STATE_END) {
//            start = -translation;
//            end = 0;
//            duration = 500;
//        }

        target.setVisibility(View.VISIBLE);
        target.setImageDrawable(drawable);
        ObjectAnimator iv1Anim = ObjectAnimator.ofFloat(target, "translationY", start, end);
        iv1Anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                LeoLog.d(TAG, "onAnimationEnd, add target: " + target);
                target.setVisibility(View.INVISIBLE);
                startTranslate(STATE_NROMAL, drawable, getTargetNextGroup(target));
            }
        });
        iv1Anim.setInterpolator(new LinearInterpolator());
        iv1Anim.setDuration(duration);
        iv1Anim.start();

        final ImageView nextTarget = getTargetCurrentGroup(target);
        nextTarget.setVisibility(View.VISIBLE);
        ObjectAnimator iv2Anim = ObjectAnimator.ofFloat(nextTarget, "translationY", start, end);
        iv2Anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                nextTarget.setVisibility(View.INVISIBLE);
            }
        });
        iv2Anim.setInterpolator(new LinearInterpolator());
        iv2Anim.setDuration(duration);
        iv2Anim.setStartDelay(duration / 2);
        iv2Anim.start();

//        if (state == STATE_BEGIN) {
//            ImageView nextTarget = null;
//            nextTarget = getNext(target);
//
//            final int nextState = mCount == 0 ? STATE_END : STATE_NROMAL;
//            final Drawable nextDrawable = getContext().getResources().getDrawable(R.drawable.ic_launcher);
//            startTranslate(nextState, nextDrawable, nextTarget);
//        }
    }

    private ImageView getTargetCurrentGroup(ImageView target) {
        return target == mAnimIv1 ? mAnimIv2 : mAnimIv4;
    }

    private ImageView getTargetNextGroup(ImageView target) {
        return target == mAnimIv1 ? mAnimIv3 : mAnimIv1;
    }
}
