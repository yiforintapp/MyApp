package com.leo.appmaster.battery;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
public class BatteryBoostAnimView extends View {
    private static final String TAG = "BatteryBoostAnimView";
    private static final int ROTATE_INTERVAL = 4;

    private Drawable mBgDrawable;
    private Drawable mCircleDrawable;

    private int mRotateAngel;

    public BatteryBoostAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Resources res = getContext().getResources();
        mBgDrawable = res.getDrawable(R.drawable.bg_circular_clear);
        if (mBgDrawable != null) {
            mBgDrawable.setBounds(0, 0, getWidth(), getHeight());
        }
        mCircleDrawable = res.getDrawable(R.drawable.bg_circular_clear_circle);
        if (mCircleDrawable != null) {
            mCircleDrawable.setBounds(0, 0, getWidth(), getHeight());
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mBgDrawable.draw(canvas);
        canvas.save();
        canvas.rotate(mRotateAngel, getWidth() / 2, getHeight() / 2);
        mCircleDrawable.draw(canvas);
        canvas.restore();

        mRotateAngel += ROTATE_INTERVAL;
        if (mRotateAngel > 360) {
            mRotateAngel = 0;
        }

        invalidate();
    }


}
