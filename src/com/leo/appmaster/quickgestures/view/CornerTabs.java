
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer.GType;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer.Orientation;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
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
import android.view.animation.DecelerateInterpolator;

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
    private float mSwitcherTargetAngle = 0;

    private int mSnapDuration = 100;

    public CornerTabs(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GestureDirection);
        int derictor = typedArray.getInt(R.styleable.GestureDirection_Direction, 0);
        if (derictor == 0) {
            mOrientation = Orientation.Left;
        } else {
            mOrientation = Orientation.Right;
        }
        typedArray.recycle();

        init(context);
    }

    private void init(Context context) {
        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        // mContainer.onCenterTabClick(e);
                        float x = e.getX();
                        float y = e.getY();

                        double offset2CfC;
                        if (mOrientation == Orientation.Left) {
                            offset2CfC = (double) Math.sqrt(Math.pow((x - 0), 2)
                                    + Math.pow((y - mTotalHeight), 2));
                        } else {
                            offset2CfC = (double) Math.sqrt(Math.pow((mTotalWidth - x), 2)
                                    + Math.pow((y - mTotalHeight), 2));
                        }

                        if (offset2CfC <= mCornerWidth) {
                            // TODO
                            LeoLog.e(TAG, "onCorner click");
                            onConerClick();
                        } else if (offset2CfC <= mTotalWidth) {

                            if (mOrientation == Orientation.Left) {
                                double d = Math.atan2(mTotalHeight - y, x) * 180 / Math.PI;
                                LeoLog.e(TAG, "d = " + d);
                                if (d < 30) {
                                    mContainer.snapToSwitcher();
                                } else if (d < 60) {
                                    mContainer.snapToMostUsed();
                                } else {
                                    mContainer.snapToDynamic();
                                }

                            } else {
                                double d = Math.atan2(mTotalHeight - y, mTotalWidth - x) * 180
                                        / Math.PI;
                                LeoLog.e(TAG, "d = " + d);
                                if (d < 30) {
                                    mContainer.snapToSwitcher();
                                } else if (d < 60) {
                                    mContainer.snapToMostUsed();
                                } else {
                                    mContainer.snapToDynamic();
                                }
                            }
                        }
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
            mBackground = res.getDrawable(R.drawable.left_tab_bg);
            mCorner = res.getDrawable(R.drawable.left_corner);
            mCover = res.getDrawable(R.drawable.left_corver);
        } else {
            mCoverAngle = mDymicTargetAngle;
            mBackground = res.getDrawable(R.drawable.right_tab_bg);
            mCorner = res.getDrawable(R.drawable.right_corner);
            mCover = res.getDrawable(R.drawable.right_corver);
        }

        mCornerWidth = mCorner.getIntrinsicWidth();
        mCornerHeight = mCorner.getIntrinsicHeight();
    }

    private void onConerClick() {
        // TODO Auto-generated method stub
        if (mContainer.isEditing()) {
            mContainer.leaveEditMode();
        } else {
            mContainer.showCloseAnimation();
        }
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
                    (float) (mTotalWidth - mTotalHeight * Math.tan(Math.toRadians(15))), 0f);
            mDynPath.lineTo(mTotalWidth, mTotalHeight);

            // mostPath
            mMostPath = new Path();
            mMostPath.moveTo(0f, 0f);
            mMostPath.lineTo(mTotalWidth, mTotalHeight);

            // quickPath
            mQuickPath = new Path();
            mQuickPath.moveTo(0f,
                    (float) (mTotalHeight - mTotalWidth * Math.tan(Math.toRadians(15))));
            mQuickPath.lineTo(mTotalWidth, mTotalHeight);
        } else {
            // dynPath
            mDynPath = new Path();
            mDynPath.moveTo(0f, mTotalHeight);
            mDynPath.lineTo((float) (mTotalHeight / Math.tan(Math.toRadians(75))), 0f);

            // mostPath
            mMostPath = new Path();
            mMostPath.moveTo(0f, mTotalHeight);
            mMostPath.lineTo(mTotalWidth, 0f);

            // quickPath
            mQuickPath = new Path();
            mQuickPath.moveTo(0f, mTotalHeight);
            mQuickPath.lineTo(mTotalWidth, (float) (mTotalHeight - mTotalWidth
                    * Math.tan(Math.toRadians(15))));
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
        if (mOrientation == Orientation.Left) {
            canvas.rotate(mCoverAngle, 0, mTotalHeight);
        } else {
            canvas.rotate(mCoverAngle, mTotalWidth, mTotalHeight);
        }
        mCover.setBounds(0, mTotalHeight - mCover.getIntrinsicHeight(), mCover.getIntrinsicWidth(),
                mTotalHeight);
        mCover.draw(canvas);
        canvas.restore();

        // third, draw text
        if (mOrientation == Orientation.Left) {
            int offset = DipPixelUtil.dip2px(getContext(), 27);
            canvas.drawTextOnPath(mDynamic, mDynPath,
                    mCornerWidth + offset, mTextSize / 2, mTextPaint);
            canvas.drawTextOnPath(mMostUsed, mMostPath, mCornerWidth + offset, mTextSize / 2,
                    mTextPaint);
            canvas.drawTextOnPath(mQuickSwitcher, mQuickPath, mCornerWidth + offset,
                    mTextSize / 2,
                    mTextPaint);
        } else {
            canvas.drawTextOnPath(mDynamic, mDynPath,
                    mCornerWidth + DipPixelUtil.dip2px(getContext(), 20), mTextSize / 2, mTextPaint);
            canvas.drawTextOnPath(mMostUsed, mMostPath,
                    mCornerWidth + DipPixelUtil.dip2px(getContext(), 50), mTextSize / 2,
                    mTextPaint);
            canvas.drawTextOnPath(mQuickSwitcher, mQuickPath,
                    mCornerWidth + DipPixelUtil.dip2px(getContext(), 10), mTextSize / 2,
                    mTextPaint);
        }

        super.onDraw(canvas);
    }

    public void snapDynamic2Switcher() {
        ValueAnimator va;
        if (mOrientation == Orientation.Left) {
            va = ValueAnimator.ofFloat(30, mSwitcherTargetAngle);
        } else {
            va = ValueAnimator.ofFloat(-30, mSwitcherTargetAngle);
        }
        va.setDuration(mSnapDuration);
        va.setInterpolator(new DecelerateInterpolator());
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        va.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCoverAngle = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        va.start();
    }

    public void snapSwitcher2Dynamic() {
        ValueAnimator va;
        if (mOrientation == Orientation.Left) {
            va = ValueAnimator.ofFloat(-90, -mDymicTargetAngle);
        } else {
            va = ValueAnimator.ofFloat(90, mDymicTargetAngle);
        }
        va.setDuration(mSnapDuration);
        va.setInterpolator(new DecelerateInterpolator());
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        va.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCoverAngle = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        va.start();
    }

    public void setAlpha(int mAlpha) {
    }

    public void setStartAngleFromTab(int tab) {
    }

}
