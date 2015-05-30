
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
import com.leo.appmaster.utils.DipPixelUtil;
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

    private ImageView mIvDynamic, mIvMostUsed, mIvSwitcher, mIvSelected;
    private int mTotalWidth, mTotalHeight;

    private float mDymicTargetAngle = 58;
    private float mSwitcherTargetAngle = 0;

    private int mSnapDuration = 100;

    private int mItemSize;
    private int mSelectBarWidth, mSelectBarHeight;

    public AppleWatchTabs(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        Resources res = getResources();
        mItemSize = res.getDimensionPixelSize(R.dimen.apple_watch_tab_item_size);
        mSelectBarWidth = res.getDimensionPixelSize(R.dimen.apple_watch_select_bar_width);
        mSelectBarHeight = res.getDimensionPixelSize(R.dimen.apple_watch_select_bar_height);
        mIvDynamic = new ImageView(getContext());
        mIvDynamic.setImageResource(R.drawable.gesture_recent);
        mIvDynamic.setOnClickListener(this);
        mIvMostUsed = new ImageView(getContext());
        mIvMostUsed.setImageResource(R.drawable.gesture_apps);
        mIvMostUsed.setOnClickListener(this);
        mIvSwitcher = new ImageView(getContext());
        mIvSwitcher.setImageResource(R.drawable.gesture_switch);
        mIvSwitcher.setOnClickListener(this);
        addView(mIvDynamic);
        addView(mIvMostUsed);
        addView(mIvSwitcher);

        mIvSelected = new ImageView(getContext());
        mIvSelected.setImageResource(R.drawable.gesture_select_bar);
        addView(mIvSelected);
    }

    private void resetItemScale() {
        GType type = mContainer.getCurrentGestureType();
        if (type == GType.DymicLayout) {
            mIvDynamic.setTag(1f);
            mIvMostUsed.setTag(0.68f);
            mIvSwitcher.setTag(0.68f);
        } else if (type == GType.MostUsedLayout) {
            mIvDynamic.setTag(0.68f);
            mIvMostUsed.setTag(1f);
            mIvSwitcher.setTag(0.68f);
        } else {
            mIvDynamic.setTag(0.68f);
            mIvMostUsed.setTag(0.68f);
            mIvSwitcher.setTag(1f);
        }
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
        /*
         * measure three gesture icon
         */
        for (int i = 0; i < count - 1; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize,
                    MeasureSpec.EXACTLY);
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize,
                    MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            child.setPivotX(child.getMeasuredWidth() / 2);
            child.setPivotY(child.getMeasuredHeight());
        }
        /*
         * measure gesture select bar
         */
        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mSelectBarWidth,
                MeasureSpec.EXACTLY);
        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mSelectBarHeight,
                MeasureSpec.EXACTLY);
        mIvSelected.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        if (mContainer == null) {
            mContainer = (AppleWatchContainer) getParent();
            resetItemScale();
        }
        mTotalWidth = getMeasuredWidth();
        mTotalHeight = getMeasuredHeight();

        /*
         * layout three gesture icon
         */
        GType type = mContainer.getCurrentGestureType();
        if (type == GType.DymicLayout) {
            mIvDynamic.layout((mTotalWidth - mItemSize) / 2, 0, (mTotalWidth + mItemSize) / 2,
                    mItemSize);
            mIvDynamic.setScaleX(1);
            mIvDynamic.setScaleY(1);
            mIvSwitcher.layout(0, mTotalHeight - mItemSize, mItemSize, mTotalHeight);
            mIvMostUsed.layout(mTotalWidth - mItemSize, mTotalHeight - mItemSize, mTotalWidth,
                    mTotalHeight);
            mIvMostUsed.setScaleX(0.68f);
            mIvMostUsed.setScaleY(0.68f);
            mIvSwitcher.setScaleX(0.68f);
            mIvSwitcher.setScaleY(0.68f);
        } else if (type == GType.MostUsedLayout) {
            mIvMostUsed.layout((mTotalWidth - mItemSize) / 2, 0, (mTotalWidth + mItemSize) / 2,
                    mItemSize);
            mIvMostUsed.setScaleX(1);
            mIvMostUsed.setScaleY(1);
            mIvDynamic.layout(0, mTotalHeight - mItemSize, mItemSize, mTotalHeight);
            mIvSwitcher.layout(mTotalWidth - mItemSize, mTotalHeight - mItemSize, mTotalWidth,
                    mTotalHeight);
            mIvSwitcher.setScaleX(0.68f);
            mIvSwitcher.setScaleY(0.68f);
            mIvDynamic.setScaleX(0.68f);
            mIvDynamic.setScaleY(0.68f);
        } else if (type == GType.SwitcherLayout) {
            mIvSwitcher.layout((mTotalWidth - mItemSize) / 2, 0, (mTotalWidth + mItemSize) / 2,
                    mItemSize);
            mIvSwitcher.setScaleX(1);
            mIvSwitcher.setScaleY(1);
            mIvMostUsed.layout(0, mTotalHeight - mItemSize, mItemSize, mTotalHeight);
            mIvDynamic.layout(mTotalWidth - mItemSize, mTotalHeight - mItemSize, mTotalWidth,
                    mTotalHeight);
            mIvDynamic.setScaleX(0.68f);
            mIvDynamic.setScaleY(0.68f);
            mIvMostUsed.setScaleX(0.68f);
            mIvMostUsed.setScaleY(0.68f);
        }

        /*
         * layout gesture select bar
         */
        int offsetY = DipPixelUtil.dip2px(getContext(), 30);
        mIvSelected.layout((mTotalWidth - mSelectBarWidth) / 2, offsetY,
                (mTotalWidth + mSelectBarWidth) / 2, offsetY + mSelectBarHeight);
    }

    /**
     * @param degree if degree > 0, rotate to left; if degree < 0, rotate to
     *            right
     */
    public void updateCoverDegree(float degree) {
        float allAngle = 40;
        float percent = degree / allAngle;
        translateDynamic(percent);
        translateSwitcher(percent);
        translateMostUsed(percent);
    }

    private void translateMostUsed(float percent) {
        float transX;
        float transY;
        float scale;
        GType type = mContainer.getCurrentGestureType();
        if (type == GType.DymicLayout) {
            if (percent > 0) {
                transX = (mIvDynamic.getLeft() - mIvMostUsed.getLeft()) * percent;
                transY = (mIvDynamic.getTop() - mIvMostUsed.getTop()) * percent;
                scale = (Float) mIvMostUsed.getTag()
                        + ((Float) mIvDynamic.getTag() - (Float) mIvMostUsed.getTag())
                        * percent;
            } else {
                percent = -percent;
                transX = (mIvSwitcher.getLeft() - mIvMostUsed.getLeft()) * percent;
                transY = (mIvSwitcher.getTop() - mIvMostUsed.getTop()) * percent;
                scale = (Float) mIvMostUsed.getTag()
                        + ((Float) mIvSwitcher.getTag() - (Float) mIvMostUsed.getTag())
                        * percent;
            }
        } else if (type == GType.MostUsedLayout) {
            if (percent > 0) {
                transX = (mIvDynamic.getLeft() - mIvMostUsed.getLeft()) * percent;
                transY = (mIvDynamic.getTop() - mIvMostUsed.getTop()) * percent;
                scale = (Float) mIvMostUsed.getTag()
                        + ((Float) mIvDynamic.getTag() - (Float) mIvMostUsed.getTag())
                        * percent;
            } else {
                percent = -percent;
                transX = (mIvSwitcher.getLeft() - mIvMostUsed.getLeft()) * percent;
                transY = (mIvSwitcher.getTop() - mIvMostUsed.getTop()) * percent;
                scale = (Float) mIvMostUsed.getTag()
                        + ((Float) mIvSwitcher.getTag() - (Float) mIvMostUsed.getTag())
                        * percent;
            }
        } else {
            if (percent > 0) {
                transX = (mIvDynamic.getLeft() - mIvMostUsed.getLeft()) * percent;
                transY = (mIvDynamic.getTop() - mIvMostUsed.getTop()) * percent;
                scale = (Float) mIvMostUsed.getTag()
                        + ((Float) mIvDynamic.getTag() - (Float) mIvMostUsed.getTag())
                        * percent;
            } else {
                percent = -percent;
                transX = (mIvSwitcher.getLeft() - mIvMostUsed.getLeft()) * percent;
                transY = (mIvSwitcher.getTop() - mIvMostUsed.getTop()) * percent;
                scale = (Float) mIvMostUsed.getTag()
                        + ((Float) mIvSwitcher.getTag() - (Float) mIvMostUsed.getTag())
                        * percent;
            }
        }
        mIvMostUsed.setTranslationX(transX);
        mIvMostUsed.setTranslationY(transY);
        mIvMostUsed.setScaleX(scale);
        mIvMostUsed.setScaleY(scale);
    }

    private void translateSwitcher(float percent) {
        float transX;
        float transY;
        float scale;
        GType type = mContainer.getCurrentGestureType();
        if (type == GType.DymicLayout) {
            if (percent > 0) {
                transX = (mIvMostUsed.getLeft() - mIvSwitcher.getLeft()) * percent;
                transY = (mIvMostUsed.getTop() - mIvSwitcher.getTop()) * percent;
                scale = (Float) mIvSwitcher.getTag()
                        + ((Float) mIvMostUsed.getTag() - (Float) mIvSwitcher.getTag())
                        * percent;
            } else {
                percent = -percent;
                transX = (mIvDynamic.getLeft() - mIvSwitcher.getLeft()) * percent;
                transY = (mIvDynamic.getTop() - mIvSwitcher.getTop()) * percent;
                scale = (Float) mIvSwitcher.getTag()
                        + ((Float) mIvDynamic.getTag() - (Float) mIvSwitcher.getTag())
                        * percent;
            }
        } else if (type == GType.MostUsedLayout) {
            if (percent > 0) {
                transX = (mIvMostUsed.getLeft() - mIvSwitcher.getLeft()) * percent;
                transY = (mIvMostUsed.getTop() - mIvSwitcher.getTop()) * percent;
                scale = (Float) mIvSwitcher.getTag()
                        + ((Float) mIvMostUsed.getTag() - (Float) mIvSwitcher.getTag())
                        * percent;
            } else {
                percent = -percent;
                transX = (mIvDynamic.getLeft() - mIvSwitcher.getLeft()) * percent;
                transY = (mIvDynamic.getTop() - mIvSwitcher.getTop()) * percent;
                scale = (Float) mIvSwitcher.getTag()
                        + ((Float) mIvDynamic.getTag() - (Float) mIvSwitcher.getTag())
                        * percent;
            }
        } else {
            if (percent > 0) {
                transX = (mIvMostUsed.getLeft() - mIvSwitcher.getLeft()) * percent;
                transY = (mIvMostUsed.getTop() - mIvSwitcher.getTop()) * percent;
                scale = (Float) mIvSwitcher.getTag()
                        + ((Float) mIvMostUsed.getTag() - (Float) mIvSwitcher.getTag())
                        * percent;
            } else {
                percent = -percent;
                transX = (mIvDynamic.getLeft() - mIvSwitcher.getLeft()) * percent;
                transY = (mIvDynamic.getTop() - mIvSwitcher.getTop()) * percent;
                scale = (Float) mIvSwitcher.getTag()
                        + ((Float) mIvDynamic.getTag() - (Float) mIvSwitcher.getTag())
                        * percent;
            }
        }
        mIvSwitcher.setTranslationX(transX);
        mIvSwitcher.setTranslationY(transY);
        mIvSwitcher.setScaleX(scale);
        mIvSwitcher.setScaleY(scale);

    }

    private void translateDynamic(float percent) {
        float transX;
        float transY;
        float scale;
        GType type = mContainer.getCurrentGestureType();
        if (type == GType.DymicLayout) {
            if (percent > 0) {
                transX = (mIvSwitcher.getLeft() - mIvDynamic.getLeft()) * percent;
                transY = (mIvSwitcher.getTop() - mIvDynamic.getTop()) * percent;
                scale = (Float) mIvDynamic.getTag()
                        + ((Float) mIvSwitcher.getTag() - (Float) mIvDynamic.getTag())
                        * percent;
            } else {
                percent = -percent;
                transX = (mIvMostUsed.getLeft() - mIvDynamic.getLeft()) * percent;
                transY = (mIvMostUsed.getTop() - mIvDynamic.getTop()) * percent;
                scale = (Float) mIvDynamic.getTag()
                        + ((Float) mIvMostUsed.getTag() - (Float) mIvDynamic.getTag())
                        * percent;
            }
        } else if (type == GType.MostUsedLayout) {
            if (percent > 0) {
                transX = (mIvSwitcher.getLeft() - mIvDynamic.getLeft()) * percent;
                transY = (mIvSwitcher.getTop() - mIvDynamic.getTop()) * percent;
                scale = (Float) mIvDynamic.getTag()
                        + ((Float) mIvSwitcher.getTag() - (Float) mIvDynamic.getTag())
                        * percent;
            } else {
                percent = -percent;
                transX = (mIvMostUsed.getLeft() - mIvDynamic.getLeft()) * percent;
                transY = (mIvMostUsed.getTop() - mIvDynamic.getTop()) * percent;
                scale = (Float) mIvDynamic.getTag()
                        + ((Float) mIvMostUsed.getTag() - (Float) mIvDynamic.getTag())
                        * percent;
            }
        } else {
            if (percent > 0) {
                transX = (mIvSwitcher.getLeft() - mIvDynamic.getLeft()) * percent;
                transY = (mIvSwitcher.getTop() - mIvDynamic.getTop()) * percent;
                scale = (Float) mIvDynamic.getTag()
                        + ((Float) mIvSwitcher.getTag() - (Float) mIvDynamic.getTag())
                        * percent;
            } else {
                percent = -percent;
                transX = (mIvMostUsed.getLeft() - mIvDynamic.getLeft()) * percent;
                transY = (mIvMostUsed.getTop() - mIvDynamic.getTop()) * percent;
                scale = (Float) mIvDynamic.getTag()
                        + ((Float) mIvMostUsed.getTag() - (Float) mIvDynamic.getTag())
                        * percent;
            }
        }
        mIvDynamic.setTranslationX(transX);
        mIvDynamic.setTranslationY(transY);
        mIvDynamic.setScaleX(scale);
        mIvDynamic.setScaleY(scale);
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
                // mCoverAngle = (Float) animation.getAnimatedValue();
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
                // mCoverAngle = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        va.start();
    }

    @Override
    public void onClick(View v) {
        if (mContainer.isEditing()) {
            return;
        }
        LeoLog.d(TAG, "onClick");
        GType type = mContainer.getCurrentGestureType();
        if (v == mIvDynamic) {
            if (type != GType.DymicLayout) {
                mContainer.snapToDynamic();
            }
        } else if (v == mIvMostUsed) {
            if (type != GType.MostUsedLayout) {
                mContainer.snapToMostUsed();
            }
        } else if (v == mIvSwitcher) {
            if (type != GType.SwitcherLayout) {
                mContainer.snapToSwitcher();
            }
        }
    }

    public void resetLayout() {
        mIvDynamic.setTranslationX(0);
        mIvDynamic.setTranslationY(0);
        mIvMostUsed.setTranslationX(0);
        mIvMostUsed.setTranslationY(0);
        mIvSwitcher.setTranslationX(0);
        mIvSwitcher.setTranslationY(0);
        resetItemScale();
        requestLayout();
    }

}
