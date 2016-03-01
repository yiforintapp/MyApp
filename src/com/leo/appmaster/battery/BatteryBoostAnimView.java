package com.leo.appmaster.battery;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.ValueAnimator;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jasper on 2016/2/29.
 */
public class BatteryBoostAnimView extends RelativeLayout {
    private static final String TAG = "BatteryBoostAnimView";
    private static final int ROTATE_INTERVAL = 4;

    private static final int STATE_BEGIN = 0;
    private static final int STATE_END = 1;
    private static final int STATE_NROMAL = 2;

    private Drawable mBgDrawable;
    private Drawable mGradientDrawable;

    private int mRotateAngel;

    private ImageView mAnimIv1;
    private ImageView mAnimIv2;
    private ImageView mAnimIv3;

    private boolean mStarted = false;

    private LinkedBlockingQueue<ImageView> mIdleViews;

    private int mCount = 10;

    public BatteryBoostAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mIdleViews = new LinkedBlockingQueue<ImageView>(3);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAnimIv1 = (ImageView) findViewById(R.id.boost_anim_iv1);
        mAnimIv2 = (ImageView) findViewById(R.id.boost_anim_iv2);
        mAnimIv3 = (ImageView) findViewById(R.id.boost_anim_iv3);

        mIdleViews.add(mAnimIv1);
        mIdleViews.add(mAnimIv2);
        mIdleViews.add(mAnimIv3);
        startBoost();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Resources res = getContext().getResources();
        mBgDrawable = res.getDrawable(R.drawable.bg_circular_clear);
        if (mBgDrawable != null) {
            mBgDrawable.setBounds(0, 0, getWidth(), getHeight());
        }

        int padding = res.getDimensionPixelSize(R.dimen.battery_boost_padding);
        mGradientDrawable = res.getDrawable(R.drawable.battery_boost_gradient_shape);
        if (mGradientDrawable != null) {
            mGradientDrawable.setBounds(padding, padding, getWidth() - padding, getHeight() - padding);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.rotate(mRotateAngel, getWidth() / 2, getHeight() / 2);
        mBgDrawable.draw(canvas);
        canvas.restore();

        mRotateAngel += ROTATE_INTERVAL;
        if (mRotateAngel > 360) {
            mRotateAngel = 0;
        }

        mGradientDrawable.draw(canvas);

        if (mStarted) {
            invalidate();
        }
    }

    public void startBoost() {
        mStarted = true;
        invalidate();

        Drawable drawable = getContext().getResources().getDrawable(R.drawable.ic_launcher);

        ImageView imageView = mIdleViews.poll();
        startTranslate(STATE_BEGIN, drawable, imageView);
    }

    private void startTranslate(final int state, Drawable drawable, final ImageView target) {
        float translation = getHeight() / 2;
        LeoLog.d(TAG, "startTranslate, translation: " + translation + " | state: " + state);

        float start = -translation;
        float end = translation;
        float startNextValue = 0;
        if (state == STATE_BEGIN) {
            start = 0;
            end = translation;
        } else if (state == STATE_END) {
            start = -translation;
            end = 0;
        }

        target.setImageDrawable(drawable);
        ObjectAnimator iv1Anim = ObjectAnimator.ofFloat(target, "translationY", start, end);
        iv1Anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIdleViews.add(target);
            }
        });
        iv1Anim.setDuration(1000);
        iv1Anim.start();

        final ImageView finalNextTarget = mIdleViews.poll();
        final int nextState = mCount == 0 ? STATE_END : STATE_NROMAL;
        final Drawable nextDrawable = getContext().getResources().getDrawable(R.drawable.ic_launcher);
        if (state == STATE_BEGIN) {
            startTranslate(nextState, nextDrawable, finalNextTarget);
        } else if (state != STATE_END) {
            ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startTranslate(nextState, nextDrawable, finalNextTarget);
                }
            }, 500);
        }
//        iv1Anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                Object obj = animation.getAnimatedValue();
//                if (obj == null) {
//                    return;
//                }
//
//                ImageView nextTarget = null;
//                try {
//                    nextTarget = mIdleViews.take();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Float valueObj = (Float) obj;
//                float value = valueObj.floatValue();
//                if (value == startNext && state != STATE_END) {
//                    mCount--;
//                    int nextState = mCount == 0 ? STATE_END : STATE_NROMAL;
//
//                    LeoLog.d(TAG, "startTranslate, update value: " + value + " | nextTarget: " + nextTarget);
//                    Drawable nextDrawable = getContext().getResources().getDrawable(R.drawable.ic_launcher);
//                    startTranslate(nextState, nextDrawable, nextTarget);
//                }
//            }
//        });
    }

}
