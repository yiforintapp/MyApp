
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
import com.leo.appmaster.utils.LeoLog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class AppleWatchTabs extends ViewGroup implements OnClickListener {
    private static final String TAG = "CornerTabs";

    private AppleWatchContainer mContainer;

    private ImageView mIvDynamic, mIvMostUsed, mIvSwitcher;
    private int mTotalWidth, mTotalHeight;
    private float mCoverAngle;

    private float mDymicTargetAngle = 58;
    private float mMostUsedTargetAngle = 30;
    private float mSwitcherTargetAngle = 0;

    private int mSnapDuration = 100;

    private int mItemSize;

    public AppleWatchTabs(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        Resources res = getResources();
        mCoverAngle = -mDymicTargetAngle;
        mItemSize = res.getDimensionPixelSize(R.dimen.apple_watch_tab_item_size);
        mIvDynamic = new ImageView(getContext());
        mIvDynamic.setImageResource(R.drawable.contacts_icon);
        mIvDynamic.setOnClickListener(this);
        mIvMostUsed = new ImageView(getContext());
        mIvMostUsed.setImageResource(R.drawable.call_records_icon);
        mIvMostUsed.setOnClickListener(this);
        mIvSwitcher = new ImageView(getContext());
        mIvSwitcher.setImageResource(R.drawable.call_info_sms_icon);
        mIvSwitcher.setOnClickListener(this);

        addView(mIvDynamic);
        addView(mIvMostUsed);
        addView(mIvSwitcher);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int count = getChildCount();
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize,
                    MeasureSpec.EXACTLY);
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize,
                    MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        mContainer = (AppleWatchContainer) getParent();
        mTotalWidth = getMeasuredWidth();
        mTotalHeight = getMeasuredHeight();

        GType type = mContainer.getCurrentGestureType();
        if (type == GType.DymicLayout) {
            mIvDynamic.layout((mTotalWidth - mItemSize) / 2, 0, (mTotalWidth + mItemSize) / 2,
                    mItemSize);
            mIvDynamic.setScaleX(1);
            mIvDynamic.setScaleY(1);
            mIvMostUsed.layout(0, mTotalHeight - mItemSize, mItemSize, mTotalHeight);
            mIvSwitcher.layout(mTotalWidth - mItemSize, mTotalHeight - mItemSize, mTotalWidth,
                    mTotalHeight);
            mIvMostUsed.setScaleX(0.8f);
            mIvMostUsed.setScaleY(0.8f);
            mIvSwitcher.setScaleX(0.8f);
            mIvSwitcher.setScaleY(0.8f);
        } else if (type == GType.MostUsedLayout) {
            mIvMostUsed.layout((mTotalWidth - mItemSize) / 2, 0, (mTotalWidth + mItemSize) / 2,
                    mItemSize);
            mIvMostUsed.setScaleX(1);
            mIvMostUsed.setScaleY(1);
            mIvSwitcher.layout(0, mTotalHeight - mItemSize, mItemSize, mTotalHeight);
            mIvDynamic.layout(mTotalWidth - mItemSize, mTotalHeight - mItemSize, mTotalWidth,
                    mTotalHeight);
            mIvSwitcher.setScaleX(0.8f);
            mIvSwitcher.setScaleY(0.8f);
            mIvDynamic.setScaleX(0.8f);
            mIvDynamic.setScaleY(0.8f);
        } else if (type == GType.SwitcherLayout) {
            mIvSwitcher.layout((mTotalWidth - mItemSize) / 2, 0, (mTotalWidth + mItemSize) / 2,
                    mItemSize);
            mIvSwitcher.setScaleX(1);
            mIvSwitcher.setScaleY(1);
            mIvDynamic.layout(0, mTotalHeight - mItemSize, mItemSize, mTotalHeight);
            mIvMostUsed.layout(mTotalWidth - mItemSize, mTotalHeight - mItemSize, mTotalWidth,
                    mTotalHeight);
            mIvDynamic.setScaleX(0.8f);
            mIvDynamic.setScaleY(0.8f);
            mIvMostUsed.setScaleX(0.8f);
            mIvMostUsed.setScaleY(0.8f);
        }

    }

    public void updateCoverDegree(float degree) {
        LeoLog.e(TAG, "degree = " + degree);
        GType type = mContainer.getCurrentGestureType();
        if (type == GType.DymicLayout) {
            mCoverAngle = mDymicTargetAngle + degree;
        } else if (type == GType.MostUsedLayout) {
            mCoverAngle = mMostUsedTargetAngle + degree;
        } else {
            mCoverAngle = mSwitcherTargetAngle + degree;
        }

        LeoLog.e(TAG, "mCoverAngle = " + mCoverAngle);

        invalidate();
    }

    public void snapDynamic2Switcher() {
        ValueAnimator va;
        va = ValueAnimator.ofFloat(-30, mSwitcherTargetAngle);
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
        va = ValueAnimator.ofFloat(90, mDymicTargetAngle);
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

    @Override
    public void onClick(View v) {
        if (v == mIvDynamic) {

        } else if (v == mIvMostUsed) {

        } else if (v == mIvSwitcher) {

        }
    }

}
