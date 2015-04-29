
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;
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

    private RectF mOval;
    private int mOffset;
    private GestureDetector mGestureDetector;
    private int mCircleCenterX;
    private int mCircleCenterY;
    private float mOvalStartAngle;
    // private QuickLauncherLayoutContainer mContainer;

    /*
     * 0: left; 1: right
     */
    private int mType = 0;
    private int mTotalWidth, mTotalHeight;
    private String mDynamic, mMostUsed, mQuickSwitcher;
    private float mTextSize;
    private Path mDynPath, mMostPath, mQuickPath;
    private Paint mTabP;
    private TextPaint mTextPaint;
    private Drawable mBackground, mCorner, mCover;
    private int mCornerWidth, mCornerHeight;

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

        mOval = new RectF();
        mOvalStartAngle = 210;
        // mOffset = context.getResources().getDimensionPixelSize(
        // R.dimen.center_tab_offset);

        mBackground = res.getDrawable(R.drawable.tab_bg);
        mCorner = res.getDrawable(R.drawable.corner);
        mCover = res.getDrawable(R.drawable.corver);

        mCornerWidth = mCorner.getIntrinsicWidth();
        mCornerHeight = mCorner.getIntrinsicHeight();
        LeoLog.e("xxxx", "mCornerWidth = " + mCornerWidth + "    mCornerHeight = " + mCornerHeight);
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
        mCircleCenterX = (right - left) / 2;
        mCircleCenterY = (bottom - top) / 2;
        mOval.set(mOffset, mOffset, right - left - mOffset, bottom - top
                - mOffset);
        // mContainer = (QuickLauncherLayoutContainer) getParent();
        super.onLayout(changed, left, top, right, bottom);

        mTotalWidth = getMeasuredWidth();
        mTotalHeight = getMeasuredHeight();

        LeoLog.e("xxxx", "mTotalWidth = " + mTotalWidth + "    mTotalHeight = " + mTotalHeight);
        makePath();

    }

    private void makePath() {
        if (mType == 1) {
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
        if (mType == 0) {
            mCorner.setBounds(0, mTotalHeight - mCornerHeight, mCornerWidth, mTotalHeight);
        } else {
            mCorner.setBounds(mTotalWidth - mCornerWidth, mTotalHeight - mCornerHeight,
                    mTotalWidth, mTotalHeight);
        }
        mCorner.draw(canvas);

        // second, draw Oval
        mCover.setBounds(0, mTotalHeight - mCover.getIntrinsicHeight(), mCover.getIntrinsicWidth(),
                mTotalHeight);
        mCover.draw(canvas);

        // third, draw text
        canvas.drawTextOnPath(mDynamic, mDynPath, mCornerWidth + 80, mTextSize / 2, mTextPaint);
        canvas.drawTextOnPath(mMostUsed, mMostPath, mCornerWidth + 80, mTextSize / 2, mTextPaint);
        canvas.drawTextOnPath(mQuickSwitcher, mQuickPath, mCornerWidth + 80, mTextSize / 2,
                mTextPaint);

        // mTabP.setARGB(mAlpha, 0x0f, 0x1f, 0x6c);
        if (mOvalStartAngle > 360) {
            mOvalStartAngle -= 360;
        } else if (mOvalStartAngle < 0) {
            mOvalStartAngle += 360;
        }
        // if (mContainer.isFlinging()) {
        // invalidate(0, 0, mCircleCenterX * 2, mCircleCenterY * 2);
        // }

        // path = new Path();

        // canvas.drawTextOnPath(mDynamic, path, 0, 0, mTextP);

        super.onDraw(canvas);
    }

    public float getOvalStartAngle() {
        return mOvalStartAngle;
    }

    public void setOvalStartAngle(float mOvalStartAngle) {
        this.mOvalStartAngle = mOvalStartAngle;
    }

    public void setAlpha(int mAlpha) {
        // this.mAlpha =(int) (255 / 5 + mAlpha * 4 / 5);
    }

    public void setStartAngleFromTab(int tab) {
        // int angle = 210;
        // switch (tab) {
        // // case QuickLauncherMananger.TAB_MOST_USED:
        // angle = 90;
        // break;
        // // case QuickLauncherMananger.TAB_RECENTLY_LAUNCHED:
        // angle = 210;
        // break;
        // // case QuickLauncherMananger.TAB_RECENTLY_INSTALLED:
        // angle = 330;
        // break;
        // default:
        // break;
        // }
        // mOvalStartAngle = angle;
    }

}
