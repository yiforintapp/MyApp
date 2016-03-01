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

/**
 * Created by Jasper on 2016/2/29.
 */
public class BatteryBoostAnimView extends RelativeLayout {
    private static final int ROTATE_INTERVAL = 4;

    private Drawable mBgDrawable;
    private Drawable mGradientDrawable;

    private int mRotateAngel;

    private ImageView mAnimIv1;
    private ImageView mAnimIv2;

    private boolean mStarted = false;

    public BatteryBoostAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAnimIv1 = (ImageView) findViewById(R.id.boost_anim_iv1);
        mAnimIv1 = (ImageView) findViewById(R.id.boost_anim_iv2);
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
    }
}
