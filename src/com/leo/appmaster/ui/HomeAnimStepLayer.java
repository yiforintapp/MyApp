package com.leo.appmaster.ui;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/24.
 */
public class HomeAnimStepLayer extends AnimLayer {
    private static final String TAG ="HomeAnimStepLayer";
    private static final boolean DBG = true;
    private static int sBigColor;
    private static int sSmallColor;
    private static int sCurrStepColor;
    private int mTotalStepCount;
    private int mCurrentStep;

    private static int sBigRadius;
    private static int sMiddleRadius;
    private static int sSmallRadius;

    private int mLineWidth;
    private int mBigLineHeight;
    private int mSmallLineHeight;

    private List<Dot> mDotList;
    private HomeAnimView mHomeView;

    private Paint mPaint;

    private float mLineRatio;
    private float mCircleRatio;

    private Drawable mConfirm;

    private boolean mMemoryLess = false;

    HomeAnimStepLayer(int stepCount, View view) {
        super(view);

        mTotalStepCount = stepCount;
        mDotList = new ArrayList<Dot>();

        sBigColor = view.getResources().getColor(R.color.home_step_big_circle);
        sSmallColor = view.getResources().getColor(R.color.home_step_small_circle);
        sCurrStepColor = view.getResources().getColor(R.color.white);
        mHomeView = (HomeAnimView) view;

        mBigLineHeight = view.getResources().getDimensionPixelSize(R.dimen.step_line_big_height);
        mSmallLineHeight = view.getResources().getDimensionPixelSize(R.dimen.step_line_small_height);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(sBigColor);

        mConfirm = view.getResources().getDrawable(R.drawable.ic_confirm);
    }

    @Override
    protected void onSizeChanged() {
        super.onSizeChanged();

        mDotList.clear();
        Resources res = mParent.getResources();
        sBigRadius = getHeight() / 2;
        sMiddleRadius = res.getDimensionPixelSize(R.dimen.home_step_middle);
        sSmallRadius = res.getDimensionPixelSize(R.dimen.home_step_small);

        mLineWidth = res.getDimensionPixelSize(R.dimen.home_step_line);

        int dotW = getHeight();
        int dotH = getHeight();
        int totalW = dotW * mTotalStepCount + mLineWidth * (mTotalStepCount - 1);

        int left = getLeft() + (getWidth() - totalW) / 2;
        int top = getTop();
        for (int i = 0; i < mTotalStepCount; i++) {
            if (i != 0) {
                left += mLineWidth + (sBigRadius * 2);
            }

            Dot dot = new Dot(left, top, left + dotW, top + dotH);
            mDotList.add(dot);
            if (i == mTotalStepCount - 1) {
                int centerX = dot.bounds.centerX();
                int centerY = dot.bounds.centerY();
                mConfirm.setBounds(centerX - sSmallRadius, centerY - sSmallRadius,
                        centerX + sSmallRadius, centerY + sSmallRadius);
            }
        }
    }

    @Override
    protected void draw(Canvas canvas) {
        if (mTotalStepCount <= 0) return;

        for (int i = 0; i < mTotalStepCount; i++) {
            Dot dot = mDotList.get(i);
            dot.drawBigCicle(canvas);
        }

        int toolbarColor = mHomeView.getToolbarColor();
        if (mTotalStepCount > 1) {
            Dot start = mDotList.get(0);
            Dot end = mDotList.get(mTotalStepCount - 1);

            Rect rect = getLineRect(start, end, mBigLineHeight);
            mPaint.setColor(toolbarColor);
            canvas.drawRect(rect.left - sBigRadius, rect.top, rect.right + sBigRadius, rect.bottom, mPaint);
        }

        for (int i = 0; i < mTotalStepCount; i++) {
            Dot dot = mDotList.get(i);
            dot.drawMiddleCircle(canvas, toolbarColor);
            if (i > 0) {
                Dot preDot = mDotList.get(i - 1);
                Rect rect = getLineRect(preDot, dot, mSmallLineHeight);
                mPaint.setColor(sSmallColor);
                int diff = sBigRadius - sSmallRadius;
                canvas.drawRect(rect.left - diff, rect.top, rect.right + diff, rect.bottom, mPaint);
            }
        }

        for (int i = 0; i < mTotalStepCount; i++) {
            Dot dot = mDotList.get(i);
            dot.drawSmallCircle(canvas);

            int preDotIdx = i - 1;
            if (preDotIdx >= 0) {
                mPaint.setColor(sCurrStepColor);
                Dot preDot = mDotList.get(preDotIdx);
                Rect rect = getSmallRect(preDot, dot, mSmallLineHeight);
                if (i < mCurrentStep) {
                    canvas.drawRect(rect, mPaint);
                } else if (i == mCurrentStep) {
                    int r = (int) (rect.left + (rect.width() * mLineRatio));
                    canvas.drawRect(rect.left, rect.top, r, rect.bottom, mPaint);
                }
            }
            if (i == 0 || i < mCurrentStep || (mLineRatio >= 1f && i == mCurrentStep)) {
                dot.drawHighlightStep(canvas, mCircleRatio, i == mCurrentStep, i == mTotalStepCount - 1);
            }
        }
    }

    private Rect getLineRect(Dot start, Dot end, int lineHeight) {
        int l = start.bounds.right;
        int t = (sBigRadius * 2 - lineHeight) / 2 + getTop();
        int r = end.bounds.left;
        int b = t + lineHeight;

        Rect rect = new Rect(l, t, r, b);

        return rect;
    }

    private Rect getSmallRect(Dot start, Dot end, int lineHeight) {
        int diff = sBigRadius - sSmallRadius;
        int l = start.bounds.right - diff;
        int t = (sBigRadius * 2 - lineHeight) / 2 + getTop();
        int r = end.bounds.left + diff;
        int b = t + lineHeight;

        return new Rect(l, t, r, b);
    }

    public void setLineRatio(float lineRatio) {
        mLineRatio = lineRatio;
        if (DBG) {
            LeoLog.i(TAG, "setLineRatio, lineRatio: " + lineRatio);
        }
        if (mMemoryLess) {
            mParent.invalidate();
        }
    }

    public void setCircleRatio(float circleRatio) {
        mCircleRatio = circleRatio;
        if (DBG) {
            LeoLog.i(TAG, "setCircleRatio, circleRatio: " + circleRatio);
        }
        if (mMemoryLess) {
            mParent.invalidate();
        }
    }

    public void setTotalStepCount(int totalStepCount, boolean isMemoryLess) {
        mTotalStepCount = totalStepCount;
        mMemoryLess = isMemoryLess;
        mCurrentStep = 0;
        onSizeChanged();
    }

    public int getTotalStepCount() {
        return mTotalStepCount;
    }

    public void increaseCurrentStep() {
        mCurrentStep++;
        mLineRatio = 0f;
        if (mMemoryLess) {
            mParent.invalidate();
        }
    }

    private class Dot {
        private Paint mPaint;
        Rect bounds;

        public Dot(int l, int t, int r, int b) {
            bounds = new Rect(l, t, r, b);

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(sBigColor);
        }

        public void drawBigCicle(Canvas canvas) {
            mPaint.setColor(sBigColor);
            canvas.drawCircle(bounds.centerX(), bounds.centerY(), bounds.width() / 2, mPaint);
        }

        public void drawMiddleCircle(Canvas canvas, int toolbarColor) {
            mPaint.setColor(toolbarColor);
            canvas.drawCircle(bounds.centerX(), bounds.centerY(), sMiddleRadius, mPaint);
        }

        public void drawSmallCircle(Canvas canvas) {
            mPaint.setColor(sSmallColor);
            canvas.drawCircle(bounds.centerX(), bounds.centerY(), sSmallRadius, mPaint);
        }

        public void drawHighlightStep(Canvas canvas, float circleRatio, boolean isCurrent, boolean isLast) {
            mPaint.setColor(sCurrStepColor);
            canvas.save();

            int centerX = bounds.centerX();
            int centerY = bounds.centerY();
            if (circleRatio > 0 && isCurrent) {
                canvas.scale(circleRatio, circleRatio, centerX, centerY);
            }
            if (isLast) {
                mConfirm.draw(canvas);
            } else {
                canvas.drawCircle(bounds.centerX(), bounds.centerY(), sSmallRadius, mPaint);
            }
            if (circleRatio > 0) {
                canvas.restore();
            }
        }
    }
}
