package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.leo.appmaster.R;

/**
 * Created by Jasper on 2015/10/20.
 */
public class ScanningImageView extends ImageView {
    public static final float MAX_ROTATE = 360f;

    public static final float INNER_SCALE = 0.68f;

    private Drawable mRotateDrawable;
    private float mScaleRatio;
    private float mRotateDegree;

    private float mInnerDrawableScale = INNER_SCALE;

    private float mCenterX;
    private float mCenterY;

    private Drawable mCircleDrawable;
    private float mMaxRotate;

    public ScanningImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mRotateDrawable = context.getResources().getDrawable(R.drawable.ic_scan_rotate);

        mCircleDrawable = context.getResources().getDrawable(R.drawable.ic_scanning_circle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int gap = getResources().getDimensionPixelSize(R.dimen.scan_scanning_gap);
        int width = mRotateDrawable.getIntrinsicWidth();
        int height = mRotateDrawable.getIntrinsicHeight();

        int left = getLeft() + gap;
        int top = getTop() + gap;
        mRotateDrawable.setBounds(left, top, left + width, top + height);

        mCircleDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());

        Rect rect = mRotateDrawable.getBounds();
        mCenterX = (rect.left + rect.right) / 2;
        mCenterY = (rect.top + rect.bottom) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        float scale = mScaleRatio;

//        if (scale < 1) {
//            canvas.scale(scale, scale, mCenterX, mCenterY);
//        }
        float innerScale = scale * mInnerDrawableScale;
        if (innerScale < 1f) {
            canvas.scale(innerScale, innerScale, mCenterX, mCenterY);
        }
        super.onDraw(canvas);

        float maxRotate = mMaxRotate != 0f ? mMaxRotate : MAX_ROTATE;
        float rotate = mRotateDegree;
        canvas.restore();

        canvas.save();
        if (rotate > 0 && rotate < maxRotate) {
            canvas.rotate(rotate, mCenterX, mCenterY);
            canvas.scale(scale, scale, mCenterX, mCenterY);
            mRotateDrawable.draw(canvas);
            mCircleDrawable.draw(canvas);
        }
//        if (mInnerDrawableScale < 1f && rotate > 0) {
//            mCircleDrawable.draw(canvas);
//        }
        canvas.restore();
    }

    public void setMaxRotate(float maxRotate) {
        mMaxRotate = maxRotate;
    }

    public void setRotateDegree(float rotateDegree) {
        mRotateDegree = rotateDegree;
        invalidate();
    }

    public void setScaleRatio(float scaleRatio) {
        mScaleRatio = scaleRatio;
        invalidate();
    }

    public void setInnerDrawableScale(float innerDrawableScale) {
        mInnerDrawableScale = innerDrawableScale;
        invalidate();
    }
}
