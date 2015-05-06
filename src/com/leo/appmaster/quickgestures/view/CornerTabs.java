
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer.GType;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer.Orientation;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class CornerTabs extends View {
    private static final String TAG = "CornerTabs";

    private int mOffset;
    private GestureDetector mGestureDetector;
    private QuickGestureContainer mContainer;

    private Orientation mOrientation = Orientation.Left;
    private int mTotalWidth, mTotalHeight;
    private String mDynamic, mMostUsed, mQuickSwitcher;
    private float mTextSize;
    private Path mDynPath, mMostPath, mQuickPath;
    private Paint mTabP;
    private TextPaint mTextPaint;
    private Drawable mBackground, mCorner, mCover;
    private int mCornerWidth, mCornerHeight;
    private float mCoverAngle;

    private float mDymicTargetAngle = 58;
    private float mMostUsedTargetAngle = 30;
    private float mSwitcherTargetAngle = -2;

    public CornerTabs(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        // mContainer.onCenterTabClick(e);
                        return super.onSingleTapUp(e);
                    }

                });
        mTabP = new Paint();
        mTabP.setAntiAlias(true);
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Style.FILL);

        Resources res = getResources();
        mDynamic = res.getString(R.string.dynamic);
        mMostUsed = res.getString(R.string.most_used);
        mQuickSwitcher = res.getString(R.string.quick_switcher);

        mTextSize = res.getDimensionPixelSize(R.dimen.qg_conner_tab_text_size);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(Color.WHITE);

        if (mOrientation == Orientation.Left) {
            mCoverAngle = -mDymicTargetAngle;
        } else {
            mCoverAngle = mDymicTargetAngle;
        }

        mBackground = res.getDrawable(R.drawable.tab_bg);
        mCorner = res.getDrawable(R.drawable.corner);
        mCover = res.getDrawable(R.drawable.corver);

        mCornerWidth = mCorner.getIntrinsicWidth();
        mCornerHeight = mCorner.getIntrinsicHeight();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        // mContainer = (QuickLauncherLayoutContainer) getParent();
        super.onLayout(changed, left, top, right, bottom);

        mTotalWidth = getMeasuredWidth();
        mTotalHeight = getMeasuredHeight();

        makePath();
        mContainer = (QuickGestureContainer) getParent();
    }

    public void updateCoverDegree(float degree) {
        LeoLog.e(TAG, "degree = " + degree);
        GType type = mContainer.getCurrentGestureType();
        if (type == GType.DymicLayout) {
            if (mOrientation == Orientation.Left) {
                mCoverAngle = -mDymicTargetAngle + degree;
            } else {
                mCoverAngle = mDymicTargetAngle + degree;
            }
        } else if (type == GType.MostUsedLayout) {
            if (mOrientation == Orientation.Left) {
                mCoverAngle = -mMostUsedTargetAngle + degree;
            } else {
                mCoverAngle = mMostUsedTargetAngle + degree;
            }
        } else {
            if (mOrientation == Orientation.Left) {
                mCoverAngle = -mSwitcherTargetAngle + degree;
            } else {
                mCoverAngle = mSwitcherTargetAngle + degree;
            }
        }
        
        LeoLog.e(TAG, "mCoverAngle = " + mCoverAngle);

        invalidate();
    }

    private void makePath() {
        if (mOrientation == Orientation.Right) {
            // dynPath
            mDynPath = new Path();
            mDynPath.moveTo(
                    (float) (mTotalWidth - mTotalHeight * Math.tan(15)), 0f);
            mDynPath.lineTo(mTotalWidth, mTotalHeight);

            // mostPath
            mMostPath = new Path();
            mMostPath.moveTo(0f, 0f);
            mMostPath.lineTo(mTotalWidth, mTotalHeight);

            // quickPath
            mQuickPath = new Path();
            mQuickPath.moveTo(0f,
                    (float) (mTotalHeight - mTotalWidth * Math.tan(15)));
            mQuickPath.lineTo(mTotalWidth, mTotalHeight);
        } else {
            // dynPath
            mDynPath = new Path();
            mDynPath.moveTo(0f, mTotalHeight);
            mDynPath.lineTo((float) (mTotalHeight / Math.tan(Math.toRadians(75))), 0f);
            LeoLog.e("xxxx", (float) (mTotalHeight / Math.tan(Math.toRadians(75))) + "");

            // mostPath
            mMostPath = new Path();
            mMostPath.moveTo(0f, mTotalHeight);
            mMostPath.lineTo(mTotalWidth, 0f);

            // quickPath
            mQuickPath = new Path();
            mQuickPath.moveTo(0f, mTotalHeight);
            mQuickPath.lineTo(mTotalWidth, (float) (mTotalHeight - mTotalWidth
                    * Math.tan(Math.toRadians(15))));
            LeoLog.e("xxxx", (float) (mTotalHeight - mTotalWidth
                    * Math.tan(Math.toRadians(15))) + "");
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // first, draw bg
        mBackground.setBounds(0, 0, mTotalWidth, mTotalHeight);
        mBackground.draw(canvas);
        if (mOrientation == Orientation.Left) {
            mCorner.setBounds(0, mTotalHeight - mCornerHeight, mCornerWidth, mTotalHeight);
        } else {
            mCorner.setBounds(mTotalWidth - mCornerWidth, mTotalHeight - mCornerHeight,
                    mTotalWidth, mTotalHeight);
        }
        mCorner.draw(canvas);

        // second, draw Cover
        canvas.save();
        canvas.rotate(mCoverAngle, 0, mTotalHeight);
        mCover.setBounds(0, mTotalHeight - mCover.getIntrinsicHeight(), mCover.getIntrinsicWidth(),
                mTotalHeight);
        mCover.draw(canvas);
        canvas.restore();

        // third, draw text
        canvas.drawTextOnPath(mDynamic, mDynPath, mCornerWidth + 80, mTextSize / 2, mTextPaint);
        canvas.drawTextOnPath(mMostUsed, mMostPath, mCornerWidth + 80, mTextSize / 2, mTextPaint);
        canvas.drawTextOnPath(mQuickSwitcher, mQuickPath, mCornerWidth + 80, mTextSize / 2,
                mTextPaint);

        super.onDraw(canvas);
    }

    public void setAlpha(int mAlpha) {
    }

    public void setStartAngleFromTab(int tab) {
    }

}
