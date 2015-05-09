
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer.Orientation;
import com.leo.appmaster.utils.LeoLog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;

public class QuickGestureLayout extends ViewGroup {

    private QuickGestureContainer mContainer;

    private int mTotalWidth, mTotalHeight;
    private int mItemSize, mIconSize;
    private int mInnerRadius, mOuterRadius;
    private int mRingCount;
    private float mCurrentRotateDegree;

    private Orientation mOrientation = Orientation.Left;
    private static final int INNER_RING_MAX_COUNT = 4;

    public QuickGestureLayout(Context context) {
        this(context, null);
    }

    public QuickGestureLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    private void init() {
        Resources res = getContext().getResources();
        mItemSize = res.getDimensionPixelSize(R.dimen.qg_item_size);
        mIconSize = res.getDimensionPixelSize(R.dimen.qg_item_icon_size);
        mInnerRadius = res.getDimensionPixelSize(R.dimen.qg_layout_inner_radius);
        mOuterRadius = res.getDimensionPixelSize(R.dimen.qg_layout_outer_radius);
        mRingCount = 1;
        mOrientation = Orientation.Left;
    }

    public QuickGestureLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public int getItemSize() {
        return mItemSize;
    }

    public int getIconSize() {
        return mIconSize;
    }

    public int getOuterRadius() {
        return mOuterRadius;
    }

    public int getInnerRadius() {
        return mInnerRadius;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int count = getChildCount();
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize, MeasureSpec.EXACTLY);
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }

        mTotalWidth = getMeasuredWidth();
        mTotalHeight = getMeasuredHeight();

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        // Laying out the child views
        final int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }

        /*
         * first ring max count is 4
         */
        if (childCount > INNER_RING_MAX_COUNT) {
            mRingCount = 2;
        }

        int innerRingCount, outerRingCount;
        float innertAngleInterval, outerAngleInterval;

        if (mRingCount == 1) {
            innerRingCount = childCount;
            outerRingCount = 0;
            innertAngleInterval = 90f / innerRingCount;
            outerAngleInterval = 0;
        } else {
            innerRingCount = INNER_RING_MAX_COUNT;
            outerRingCount = childCount - innerRingCount;
            innertAngleInterval = 90f / innerRingCount;
            outerAngleInterval = 90f / outerRingCount;
        }

        int left = 0, top = 0;

        float innerStartAngle, outerStartAngle;
        innerStartAngle = 90 - innertAngleInterval / 2;
        outerStartAngle = 90 - outerAngleInterval / 2;

        float halfItemSize = mItemSize / 2.0f;

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            LayoutParams params = (LayoutParams) child.getLayoutParams();

            if (params.position < 0)
                continue;

            if (params.position < innerRingCount) { // match first ring
                if (mOrientation == Orientation.Left) {
                    left = (int) (mInnerRadius
                            * Math.cos(Math.toRadians(innerStartAngle - params.position
                                    * innertAngleInterval)) - halfItemSize);
                } else {
                    // TODO right
                    left = mTotalWidth - (int) (mInnerRadius
                            * Math.cos(Math.toRadians(innerStartAngle - params.position
                                    * innertAngleInterval)) - halfItemSize);
                }

                top = (int) (mTotalHeight - mInnerRadius
                        * Math.sin(Math.toRadians(innerStartAngle - params.position
                                * innertAngleInterval)) - halfItemSize);
            } else { // match second ring
                if (mOrientation == Orientation.Left) {
                    left = (int) (mOuterRadius
                            * Math.cos(Math.toRadians(outerStartAngle
                                    - (params.position - innerRingCount)
                                    * outerAngleInterval)) - halfItemSize);
                } else {
                    // TODO right
                    left = mTotalWidth - (int) (mOuterRadius
                            * Math.cos(Math.toRadians(outerStartAngle
                                    - (params.position - innerRingCount)
                                    * outerAngleInterval)) - halfItemSize);
                }

                top = (int) (mTotalHeight
                        - mOuterRadius
                        * Math.sin(Math.toRadians(outerStartAngle
                                - (params.position - innerRingCount)
                                * outerAngleInterval)) - halfItemSize);
            }

            child.layout(left, top, left + mItemSize, top + mItemSize);
        }

        /*
         * now set pivot
         */
        if (mOrientation == Orientation.Left) {
            setPivotX(0f);
            setPivotY(mTotalHeight);
        } else {
            setPivotX(mTotalWidth);
            setPivotY(mTotalHeight);
        }

        mContainer = (QuickGestureContainer) getParent();
    }

    @Override
    public void removeView(View view) {
        LayoutParams params = null;
        int removePosition = ((LayoutParams) view.getLayoutParams()).position;
        for (int i = 0; i < getChildCount(); i++) {
            params = (LayoutParams) getChildAt(i).getLayoutParams();
            if (params.position > removePosition) {
                params.position--;
            }
        }
        super.removeView(view);
    }

    @Override
    public void addView(View child) {
        int addPosition = ((LayoutParams) child.getLayoutParams()).position;
        LayoutParams params = null;
        for (int i = 0; i < getChildCount(); i++) {
            params = (LayoutParams) getChildAt(i).getLayoutParams();
            if (params.position >= addPosition) {
                params.position++;
            }
        }
        super.addView(child);
    }

    private void animateItem(final View view) {

        AnimatorSet as = new AnimatorSet();
        as.setDuration(300);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f,
                0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f,
                0.8f, 1f);
        as.playTogether(scaleX, scaleY);
        as.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mContainer.isEditing()) {
                    BaseInfo info = (BaseInfo) view.getTag();
                    if (info instanceof AppItemInfo) {
                        AppItemInfo appInfo = (AppItemInfo) info;
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setComponent(new ComponentName(appInfo.packageName,
                                appInfo.activityName));
                        getContext().startActivity(intent);
                    } else if (info instanceof QuickSwitcherInfo) {// 快捷开关
                        QuickSwitcherInfo sInfo = (QuickSwitcherInfo) info;
                        //蓝牙
                        if(sInfo.iDentiName.equals(QuickSwitchManager.BLUETOOTH)){
                            QuickSwitchManager.getInstance(getContext()).toggleBluetooth(mContainer,mContainer.getSwitchList(),QuickGestureLayout.this);
                        }
                    }
                }
            }
        });
        as.start();
    }

    public void checkItemClick(float x, float y) {
        // TODO
        View hitView = null, tempView = null;
        for (int i = 0; i < getChildCount(); i++) {
            tempView = getChildAt(i);
            if (x > tempView.getLeft() && x < tempView.getRight()
                    && y > tempView.getTop() && y < tempView.getBottom()) {
                hitView = tempView;
                LeoLog.d("checkItemClick", "hitView");
                break;
            }
        }

        if (hitView != null) {
            if (mContainer.isEditing()) {
                GestureItemView giv = (GestureItemView) hitView;
                Rect rect = giv.getCrossRect();
                int offsetX = (int) (x - hitView.getLeft());
                int onnsetY = (int) (y - hitView.getTop());
                if (rect.contains(offsetX, onnsetY)) {
                    removeView(hitView);
                }
            } else {
                animateItem(hitView);
            }
        }
    }

    public void checkItemLongClick(float x, float y) {
        View hitView = null, tempView = null;
        for (int i = 0; i < getChildCount(); i++) {
            tempView = getChildAt(i);
            if (x > tempView.getLeft() && x < tempView.getRight()
                    && y > tempView.getTop() && y < tempView.getBottom()) {
                hitView = tempView;
                LeoLog.d("checkItemLongClick", "hitView");
                break;
            }
        }

        if (hitView != null) {
            // animateItem(hitView);
            hitView.startDrag(null, new GestureDragShadowBuilder(hitView, 2.0f), hitView, 0);
            hitView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
    }

    public void checkActionDownInEditing(float x, float y) {
        // TODO
        View hitView = null, tempView = null;
        for (int i = 0; i < getChildCount(); i++) {
            tempView = getChildAt(i);
            if (x > tempView.getLeft() && x < tempView.getRight()
                    && y > tempView.getTop() && y < tempView.getBottom()) {
                hitView = tempView;
                LeoLog.d("checkItemClick", "hitView");
                break;
            }
        }
        if (hitView != null) {
            if (mContainer.isEditing()) {
                GestureItemView giv = (GestureItemView) hitView;
                Rect rect = giv.getCrossRect();
                int offsetX = (int) (x - hitView.getLeft());
                int onnsetY = (int) (y - hitView.getTop());
                if (!rect.contains(offsetX, onnsetY)) {
                    hitView.startDrag(null, new GestureDragShadowBuilder(hitView, 2.0f), hitView, 0);
                    hitView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
            }
        }
    }

    public void setCurrentRotateDegree(float degree) {
        mCurrentRotateDegree = degree;
        setRotation(mCurrentRotateDegree);
    }

    public float getCurrentRotateDegree() {
        return mCurrentRotateDegree;
    }

    public int pointToPosition(float x, float y) {
        for (int i = 0; i < getChildCount(); i++) {
            View item = (View) getChildAt(i);
            if (item.getLeft() < x && item.getRight() > x & item.getTop() < y
                    && item.getBottom() > y) {
                return i;
            }
        }
        return -1;
    }

    public void leaveEditMode() {
        for (int i = 0; i < getChildCount(); i++) {
            GestureItemView item = (GestureItemView) getChildAt(i);
            item.leaveEditMode();
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        public int position = -1;

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }
}
