
package com.leo.appmaster.quickgestures.view;

import java.util.ArrayList;
import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
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
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.MessageBean;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.quickgestures.model.QuickGestureContactTipInfo;

public class AppleWatchLayout extends ViewGroup {

    public static final String TAG = "AppleWatchLayout";

    private AppleWatchContainer mContainer;
    private AnimatorSet mReorderAnimator;
    private boolean mRecodering;
    private boolean mAnimCanceled;
    private int mTotalWidth, mTotalHeight;
    private int mCenterPointX, mCenterPointY;
    private int mItemSize, mIconSize;
    private int mInnerRadius, mOuterRadius;
    private float mInnerScale, mOuterScale, mThirdScale;
    private float mCurrentRotateDegree;
    private Context mContext;
    private GestureItemView[][] mHoriChildren = new GestureItemView[3][];

    private enum Direction {
        Right, Left, None;
    }

    private Direction mLastTranslateDeirction = Direction.None;
    private GestureItemView mExtraItemTop, mExtraItemMid, mExtraItemBottom;

    public AppleWatchLayout(Context context) {
        this(context, null);
    }

    public AppleWatchLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppleWatchLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
        addThreeExtraItem();
    }

    private void addThreeExtraItem() {
        LayoutParams lp = null;
        mExtraItemTop = new GestureItemView(getContext());
        lp = new LayoutParams(mItemSize, mItemSize);
        lp.position = -1;
        lp.scale = 0.0f;
        mExtraItemTop.setLayoutParams(lp);

        mExtraItemMid = new GestureItemView(getContext());
        lp = new LayoutParams(mItemSize, mItemSize);
        lp.position = -2;
        lp.scale = 0.0f;
        mExtraItemMid.setLayoutParams(lp);

        mExtraItemBottom = new GestureItemView(getContext());
        lp = new LayoutParams(mItemSize, mItemSize);
        lp.position = -3;
        lp.scale = 0.0f;
        mExtraItemBottom.setLayoutParams(lp);

        addView(mExtraItemTop);
        addView(mExtraItemMid);
        addView(mExtraItemBottom);
    }

    private void init() {
        Resources res = getContext().getResources();
        mItemSize = res.getDimensionPixelSize(R.dimen.apple_watch_item_size);
        mIconSize = res.getDimensionPixelSize(R.dimen.apple_watch_item_icon_size);
        mInnerRadius = res.getDimensionPixelSize(R.dimen.apple_watch_layout_inner_radius);
        mOuterRadius = res.getDimensionPixelSize(R.dimen.apple_watch_layout_outer_radius);
        mInnerScale = 0.77f;
        mOuterScale = 0.66f;
        mThirdScale = 0.4f;
    }

    public GestureItemView getChildAtPosition(int position) {
        GestureItemView child = null;
        for (int i = 0; i < getChildCount(); i++) {
            child = (GestureItemView) getChildAt(i);
            if (position == ((LayoutParams) child.getLayoutParams()).position) {
                return child;
            }
        }
        return null;
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
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        LayoutParams lp;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            lp = (LayoutParams) child.getLayoutParams();
            lp.scale = getItemScale(lp.position);
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize,
                    MeasureSpec.EXACTLY);
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize,
                    MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            child.setScaleX(lp.scale);
            child.setScaleY(lp.scale);
        }

        measureThreeExtraItem();

        mTotalWidth = getMeasuredWidth();
        mTotalHeight = getMeasuredHeight();
        mCenterPointX = mTotalWidth / 2;
        mCenterPointY = mTotalHeight / 2;
    }

    private void measureThreeExtraItem() {
        // TODO Auto-generated method stub
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize,
                MeasureSpec.EXACTLY);
        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize,
                MeasureSpec.EXACTLY);

        mExtraItemTop.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        mExtraItemMid.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        mExtraItemBottom.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        // Laying out the child views
        final int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }

        float innertAngleInterval;
        innertAngleInterval = 60;
        float innerStartAngle;
        innerStartAngle = 0;
        int halfItemSize;
        double outerItemAngle = 30;
        int left = 0, top = 0;

        // layout all position >= 0
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            LayoutParams params = (LayoutParams) child.getLayoutParams();
            halfItemSize = child.getWidth() / 2;
            if (params.position < 0)
                continue;
            if (params.position == 0) {
                left = mCenterPointX - halfItemSize;
                top = mCenterPointY - halfItemSize;
            } else if (params.position <= 6) { // match first ring
                left = (int) (mCenterPointX - mInnerRadius
                        * Math.cos(Math.toRadians(innerStartAngle + (params.position - 1)
                                * innertAngleInterval)) - halfItemSize);

                top = (int) (mCenterPointY - mInnerRadius
                        * Math.sin(Math.toRadians(innerStartAngle + (params.position - 1)
                                * innertAngleInterval)) - halfItemSize);

            } else if (params.position <= 12) { // match second ring

                if (params.position == 7) {
                    outerItemAngle = 330;
                } else if (params.position == 8) {
                    outerItemAngle = 0;
                } else if (params.position == 9) {
                    outerItemAngle = 30;
                } else if (params.position == 10) {
                    outerItemAngle = 150;
                } else if (params.position == 11) {
                    outerItemAngle = 180;
                } else if (params.position == 12) {
                    outerItemAngle = 210;
                }

                if (params.position == 8 || params.position == 11) {
                    left = (int) (mCenterPointX - mOuterRadius * 1.1f
                            * Math.cos(Math.toRadians(outerItemAngle)) - halfItemSize);

                    top = (int) (mCenterPointY
                            - mOuterRadius * 1.1f * Math.sin(Math.toRadians(outerItemAngle)) - halfItemSize);
                } else {
                    left = (int) (mCenterPointX - mOuterRadius
                            * Math.cos(Math.toRadians(outerItemAngle)) - halfItemSize);

                    top = (int) (mCenterPointY
                            - mOuterRadius
                            * Math.sin(Math.toRadians(outerItemAngle)) - halfItemSize);
                }
            }

            child.layout(left, top, left + child.getMeasuredWidth(),
                    top + child.getMeasuredHeight());
        }

        /*
         * now set pivot
         */
        setPivotX(mTotalWidth / 2);
        setPivotY(mTotalHeight * 3);

        mContainer = (AppleWatchContainer) getParent();
    }

    private void layoutThreeExtraItem(Direction direction) {
        LeoLog.e(TAG, "layoutThreeExtraItem");
        if (mExtraItemTop == null) {
            addThreeExtraItem();
            measureThreeExtraItem();
        }
        GestureItemView temp;
        LayoutParams lp;
        if (direction == Direction.Left) {
            for (int i = 0; i < getChildCount(); i++) {
                temp = (GestureItemView) getChildAt(i);
                lp = (LayoutParams) temp.getLayoutParams();
                if (lp.position == 7) {
                    mExtraItemBottom.layout(0, temp.getTop(), mItemSize, temp.getBottom());
                    mExtraItemBottom.setPivotX(0);
                    mExtraItemBottom.setPivotY(mExtraItemBottom.getMeasuredHeight() / 2);
                } else if (lp.position == 8) {
                    mExtraItemMid.layout(0, temp.getTop(), mItemSize, temp.getBottom());
                    mExtraItemMid.setPivotX(0);
                    mExtraItemMid.setPivotY(mExtraItemMid.getMeasuredHeight() / 2);
                } else if (lp.position == 9) {
                    mExtraItemTop.layout(0, temp.getTop(), mItemSize, temp.getBottom());
                    mExtraItemTop.setPivotX(0);
                    mExtraItemTop.setPivotY(mExtraItemTop.getMeasuredHeight() / 2);
                } else if (lp.position == 10) {
                    inflateItem(mExtraItemTop, (BaseInfo) temp.getTag());
                } else if (lp.position == 11) {
                    inflateItem(mExtraItemMid, (BaseInfo) temp.getTag());
                } else if (lp.position == 12) {
                    inflateItem(mExtraItemBottom, (BaseInfo) temp.getTag());
                    break;
                }
            }
        } else if (direction == Direction.Right) {
            for (int i = 0; i < getChildCount(); i++) {
                temp = (GestureItemView) getChildAt(i);
                lp = (LayoutParams) temp.getLayoutParams();
                if (lp.position == 7) {
                    inflateItem(mExtraItemBottom, (BaseInfo) temp.getTag());
                } else if (lp.position == 8) {
                    inflateItem(mExtraItemMid, (BaseInfo) temp.getTag());
                } else if (lp.position == 9) {
                    inflateItem(mExtraItemTop, (BaseInfo) temp.getTag());
                } else if (lp.position == 10) {
                    mExtraItemTop.layout(mTotalWidth - mItemSize, temp.getTop(), mTotalWidth,
                            temp.getBottom());
                    mExtraItemTop.setPivotX(mExtraItemTop.getMeasuredWidth());
                    mExtraItemTop.setPivotY(mExtraItemTop.getMeasuredHeight() / 2);
                } else if (lp.position == 11) {
                    mExtraItemMid.layout(mTotalWidth - mItemSize, temp.getTop(), mTotalWidth,
                            temp.getBottom());
                    mExtraItemMid.setPivotX(mExtraItemMid.getMeasuredWidth());
                    mExtraItemMid.setPivotY(mExtraItemMid.getMeasuredHeight() / 2);
                } else if (lp.position == 12) {
                    mExtraItemBottom.layout(mTotalWidth - mItemSize, temp.getTop(), mTotalWidth,
                            temp.getBottom());
                    mExtraItemBottom.setPivotX(mExtraItemBottom.getMeasuredWidth());
                    mExtraItemBottom.setPivotY(mExtraItemBottom.getMeasuredHeight() / 2);
                    break;
                }
            }

        }
    }

    private void inflateItem(GestureItemView item, BaseInfo info) {
        if (item == null || info == null)
            return;
        item.setGravity(Gravity.CENTER_HORIZONTAL);
        item.setText(info.label);
        item.setTextSize(12);
        item.setCompoundDrawables(null, info.icon, null, null);
        if (info.eventNumber > 0) {
            item.setDecorateAction(new EventAction(getContext(), info.eventNumber));
        }
        item.setTag(info);
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
        saveReorderPosition();
    }

    @Override
    public void removeAllViews() {
        // TODO Auto-generated method stub
        super.removeAllViews();
        addThreeExtraItem();
    }

    /**
     * 在初始化添加view的時候，必須使用次方法
     */
    @Override
    public void addView(View child) {
        super.addView(child);
    }

    /**
     * 在快捷手势界面点击添加按钮时，必须通过此方法添加
     */
    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        int addPosition = ((LayoutParams) params).position;
        LayoutParams lp = ((LayoutParams) params);
        for (int i = 0; i < getChildCount(); i++) {
            lp = (LayoutParams) getChildAt(i).getLayoutParams();
            if (lp.position >= addPosition) {
                lp.position++;
            }
        }
        saveReorderPosition();
        super.addView(child, params);
    }

    public float getItemScale(int position) {
        if (position < 0) {
            return 0.0f;
        } else if (position == 0) {
            return 1f;
        } else if (position <= 6) {
            return mInnerScale;
        } else if (position <= 12) {
            if (position == 8 || position == 11) {
                return mThirdScale;
            } else {
                return mOuterScale;
            }
        } else {
            LeoLog.e(TAG, "position must be >=0 and <= 12");
            return 0f;
        }
    }

    private void animateItem(final View view) {

        AnimatorSet as = new AnimatorSet();
        as.setDuration(300);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f * view.getScaleX(),
                0.8f * view.getScaleX(), 1f * view.getScaleX());
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f * view.getScaleY(),
                0.8f * view.getScaleY(), 1f * view.getScaleY());
        as.playTogether(scaleX, scaleY);
        as.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mContainer.isEditing()) {
                    onItemClick(view);
                }
            }
        });
        as.start();
    }

    private void onItemClick(final View view) {
        GestureItemView item = (GestureItemView) view;
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
            // 蓝牙
            if (sInfo.iDentiName.equals(QuickSwitchManager.BLUETOOTH)) {
                QuickSwitchManager.getInstance(getContext())
                        .toggleBluetooth(sInfo);
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.FLASHLIGHT)) {
                QuickSwitchManager.getInstance(getContext())
                        .toggleFlashLight(sInfo);
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.WLAN)) {
                QuickSwitchManager.getInstance(getContext()).toggleWlan(sInfo);
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.CRAME)) {
                QuickSwitchManager.getInstance(getContext()).openCrame();
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.SOUND)) {
                QuickSwitchManager.getInstance(getContext()).toggleSound(sInfo);
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.LIGHT)) {
                QuickSwitchManager.getInstance(getContext()).toggleLight(sInfo);
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.SPEEDUP)) {
                QuickSwitchManager.getInstance(getContext()).speedUp();
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.CHANGEMODE)) {
                QuickSwitchManager.getInstance(getContext()).toggleMode();
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.SWITCHSET)) {
                QuickSwitchManager.getInstance(getContext()).switchSet();
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.SETTING)) {
                QuickSwitchManager.getInstance(getContext()).goSetting();
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.GPS)) {
                QuickSwitchManager.getInstance(getContext()).toggleGPS();
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.FLYMODE)) {
                QuickSwitchManager.getInstance(getContext()).toggleFlyMode();
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.ROTATION)) {
                QuickSwitchManager.getInstance(getContext()).toggleRotation(sInfo);
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.MOBILEDATA)) {
                QuickSwitchManager.getInstance(getContext()).toggleMobileData(sInfo);
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.HOME)) {
                QuickSwitchManager.getInstance(getContext()).goHome();
            }
        } else if (info instanceof MessageBean) {
            // 短信提醒
            item.cancelShowReadTip();
            MessageBean bean = (MessageBean) info;
            Uri smsToUri = Uri.parse("smsto://" +
                    bean.getPhoneNumber());
            Intent mIntent = new
                    Intent(android.content.Intent.ACTION_SENDTO,
                            smsToUri);
            try {
                mContext.startActivity(mIntent);
                if (QuickGestureManager.getInstance(mContext).mMessages != null
                        && QuickGestureManager.getInstance(mContext).mMessages.size() > 0) {
                    QuickGestureManager.getInstance(getContext()).checkEventItemRemoved(bean);
                }
            } catch (Exception e) {
            }
        } else if (info instanceof ContactCallLog) {
            // 电话提醒
            item.cancelShowReadTip();
            ContactCallLog callLog = (ContactCallLog) info;
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_CALL_BUTTON);
            mContext.startActivity(intent);
            if (QuickGestureManager.getInstance(mContext).mCallLogs != null
                    && QuickGestureManager.getInstance(mContext).mCallLogs.size() > 0) {
                QuickGestureManager.getInstance(getContext()).checkEventItemRemoved(callLog);
            }
        } else if (info instanceof QuickGestureContactTipInfo) {
            // 隐私联系人提示
            item.cancelShowReadTip();
            QuickGestureContactTipInfo privacyInfo = (QuickGestureContactTipInfo) info;
            Intent intent = new Intent();
            intent.setClass(mContext, PrivacyContactActivity.class);
            try {
                mContext.startActivity(intent);
                if (LockManager.getInstatnce().isShowPrivacyCallLog) {
                    LockManager.getInstatnce().isShowPrivacyCallLog = false;
                }
                if (LockManager.getInstatnce().isShowPrivacyMsm) {
                    LockManager.getInstatnce().isShowPrivacyMsm = false;
                }
            } catch (Exception e) {
            }

        }
    }

    public void checkItemClick(float x, float y) {
        // TODO
        View hitView = null, tempView = null;
        for (int i = 0; i < getChildCount(); i++) {
            tempView = getChildAt(i);
            if (x > tempView.getLeft() && x < tempView.getRight()
                    && y > tempView.getTop() && y < tempView.getBottom()) {
                hitView = tempView;
                break;
            }
        }

        LeoLog.d("checkItemClick", "hitView = " + hitView);
        if (hitView != null) {
            GestureItemView giv = (GestureItemView) hitView;
            if (mContainer.isEditing()) {
                if (giv.hasAddFlag()) {
                    // TODO show add item dialog
                    GType type = mContainer.getCurrentGestureType();
                    if (type == GType.MostUsedLayout || type == GType.SwitcherLayout) {
                        showAddNewDiglog(type);
                    }
                } else {
                    Rect rect = giv.getCrossRect();
                    int offsetX = (int) (x - hitView.getLeft());
                    int onnsetY = (int) (y - hitView.getTop());
                    if (rect.contains(offsetX, onnsetY)) {
                        removeView(hitView);
                        onItemRemoved(hitView);
                    }
                }

            } else {
                animateItem(hitView);
            }
        }
    }

    private void showAddNewDiglog(GType type) {
        // TODO Auto-generated method stub
        if (type == GType.MostUsedLayout) {
            Toast.makeText(getContext(), "添加最近使用item", 0).show();
        } else if (type == GType.SwitcherLayout) {
            Toast.makeText(getContext(), "添加快捷开关item", 0).show();
        }
    }

    private void onItemRemoved(View hitView) {
        GType type = mContainer.getCurrentGestureType();
        if (type == GType.DymicLayout) {
            QuickGestureManager.getInstance(getContext()).checkEventItemRemoved(
                    (BaseInfo) hitView.getTag());
        } else if (type == GType.SwitcherLayout) {

            int childCount = getChildCount();
            GestureItemView view = (GestureItemView) getChildAt(childCount - 1);
            if (childCount < 9 && !view.hasAddFlag()) {
                showAddIcon(childCount);
            }

        } else if (type == GType.MostUsedLayout) {
            int childCount = getChildCount();
            GestureItemView view = (GestureItemView) getChildAt(childCount - 1);
            if (childCount < 9 && !view.hasAddFlag()) {
                showAddIcon(childCount);
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
            // 不足9个icon，显示虚框 TODO
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
                break;
            }
        }
        if (hitView != null) {
            if (mContainer.isEditing()) {
                GestureItemView giv = (GestureItemView) hitView;
                Rect rect = giv.getCrossRect();
                int offsetX = (int) (x - hitView.getLeft());
                int onnsetY = (int) (y - hitView.getTop());
                if (!rect.contains(offsetX, onnsetY) && !giv.hasAddFlag()) {
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

    public void replaceItems(GestureItemView fromView, GestureItemView toView) {
        AppleWatchLayout.LayoutParams fromLP = (LayoutParams) fromView.getLayoutParams();
        AppleWatchLayout.LayoutParams toLP = (LayoutParams) toView.getLayoutParams();

        int from = fromLP.position;
        int to = toLP.position;
        toLP.position = from;
        fromLP.position = to;

    }

    public boolean isReordering() {
        return mRecodering;
    }

    public void squeezeItems(GestureItemView fromView, GestureItemView toView) {

        // dont need squeeze dynamic layout
        GType type = mContainer.getCurrentGestureType();
        if (type == GType.DymicLayout) {
            return;
        }

        if (mReorderAnimator != null && mReorderAnimator.isRunning()) {
            mReorderAnimator.cancel();
        }

        int from = ((AppleWatchLayout.LayoutParams) fromView.getLayoutParams()).position;
        int to = ((AppleWatchLayout.LayoutParams) toView.getLayoutParams()).position;

        boolean isForward = to > from;
        final GestureItemView[] hitViews = new GestureItemView[Math.abs(to - from) + 1];
        hitViews[0] = fromView;
        GestureItemView hitView;
        AppleWatchLayout.LayoutParams hitLP;
        for (int i = 0; i < getChildCount(); i++) {
            hitView = (GestureItemView) getChildAt(i);
            hitView.setLeft((int) (hitView.getLeft() +
                    hitView.getTranslationX()));
            hitView.setTop((int) (hitView.getTop() +
                    hitView.getTranslationY()));
            hitView.setTranslationX(0);
            hitView.setTranslationY(0);

            hitLP = (LayoutParams) hitView.getLayoutParams();
            if (isForward) {
                if (hitLP.position > from && hitLP.position <= to) {
                    hitViews[hitLP.position - from] = hitView;
                }
            } else {
                if (hitLP.position >= to && hitLP.position < from) {
                    hitViews[from - hitLP.position] = hitView;
                }
            }
        }

        ArrayList<Animator> animators = new ArrayList<Animator>();
        for (int i = 1; i < hitViews.length; i++) {
            animators.add(createTranslationAnimations(hitViews[i], hitViews[i - 1]));
        }
        mReorderAnimator = new AnimatorSet();
        mReorderAnimator.playTogether(animators);
        mReorderAnimator.setDuration(200);
        mReorderAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mReorderAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimCanceled = false;
                mRecodering = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mAnimCanceled = true;
                mRecodering = false;
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mRecodering = false;
                int lastPosition = ((AppleWatchLayout.LayoutParams) hitViews[hitViews.length - 1]
                        .getLayoutParams()).position;
                int nextPosition;
                for (int i = hitViews.length - 1; i >= 0; i--) {
                    if (i == 0) {
                        ((AppleWatchLayout.LayoutParams) hitViews[i].getLayoutParams()).position = lastPosition;
                    } else {
                        nextPosition = ((AppleWatchLayout.LayoutParams) hitViews[i - 1]
                                .getLayoutParams()).position;
                        ((AppleWatchLayout.LayoutParams) hitViews[i].getLayoutParams()).position = nextPosition;
                    }
                }

                saveReorderPosition();

                if (mAnimCanceled) {
                    for (GestureItemView gestureItemView : hitViews) {
                        gestureItemView.setLeft((int) (gestureItemView.getLeft() + gestureItemView
                                .getTranslationX()));
                        gestureItemView.setTop((int) (gestureItemView.getTop() + gestureItemView
                                .getTranslationY()));
                        gestureItemView.setTranslationX(0);
                        gestureItemView.setTranslationY(0);
                    }
                } else {
                    for (GestureItemView gestureItemView : hitViews) {
                        gestureItemView.setTranslationX(0);
                        gestureItemView.setTranslationY(0);
                    }

                    requestLayout();
                }
            }

        });
        mReorderAnimator.start();
    }

    private void saveReorderPosition() {
        if (mContainer != null) {
            GType gType = mContainer.getCurrentGestureType();
            if (gType == GType.DymicLayout) {
                // TODO update dynamic list

            } else if (gType == GType.MostUsedLayout) {
                // TODO update most used list

            } else if (gType == GType.SwitcherLayout) {
                int mNum = getChildCount();
                LayoutParams params = null;
                List<QuickSwitcherInfo> mSwitchList = new ArrayList<QuickSwitcherInfo>();
                LeoLog.d("QuickGestureLayout", "总孩子数：" + mNum);
                for (int i = 0; i < mNum; i++) {
                    params = (LayoutParams) getChildAt(i).getLayoutParams();
                    int position = params.position;
                    QuickSwitcherInfo sInfo = (QuickSwitcherInfo) getChildAt(i).getTag();
                    if (sInfo != null) {
                        sInfo.position = position;
                        mSwitchList.add(sInfo);
                        LeoLog.d("QuickGestureLayout", "名字：" + sInfo.label + "位置：" + position);
                    }

                }
                QuickGestureManager.getInstance(getContext()).updateSwitcherData(mSwitchList);
            }
        }
    }

    private AnimatorSet createTranslationAnimations(GestureItemView fromView, GestureItemView toView) {
        ObjectAnimator tranX = ObjectAnimator.ofFloat(fromView, "translationX",
                fromView.getTranslationX(), toView.getLeft() - fromView.getLeft());
        ObjectAnimator tranY = ObjectAnimator.ofFloat(fromView, "translationY",
                fromView.getTranslationY(), toView.getTop() - fromView.getTop());

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(fromView, "scaleX",
                fromView.getScaleX(), toView.getScaleX());
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(fromView, "scaleY",
                fromView.getScaleY(), toView.getScaleY());

        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(tranX, tranY, scaleX, scaleY);
        return animSetXY;
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        public int position = -1;
        public float scale = 1.0f;

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }

    public void onLeaveEditMode() {
        if (mContainer.getCurrentGestureType() != GType.DymicLayout) {
            GestureItemView lastChild = (GestureItemView) getChildAt(getChildCount() - 1);
            if (lastChild.hasAddFlag()) {
                removeView(lastChild);
            }
        }
        for (int i = 0; i < getChildCount(); i++) {
            GestureItemView item = (GestureItemView) getChildAt(i);
            item.leaveEditMode();
        }
    }

    public void onEnterEditMode() {
        if (mContainer.getCurrentGestureType() != GType.DymicLayout) {
            int childCount = getChildCount();
            GestureItemView view = (GestureItemView) getChildAt(childCount - 1);
            if (childCount < 13 && !view.hasAddFlag()) {
                showAddIcon(childCount);
            }
        }
    }

    private void showAddIcon(int position) {
        if (position >= 0 && position <= 12) {
            LayoutParams params = new LayoutParams(mItemSize, mItemSize);
            params.position = position;
            GestureItemView addItem = new GestureItemView(mContext);
            addItem.setLayoutParams(params);
            addItem.setBackgroundResource(R.drawable.switch_add);
            addItem.setAddFlag(true);
            addView(addItem);
        }
    }

    public void checkFull() {

    }

    public void translateItem(float moveX) {
        LeoLog.e(TAG, "moveX = " + moveX);
        if (moveX > 0) {
            if (mLastTranslateDeirction != Direction.Left) {
                mLastTranslateDeirction = Direction.Left;
                LeoLog.e(TAG, "translateItem");
                layoutThreeExtraItem(Direction.Left);
                computeHoriChildren(Direction.Left);
            }
            computeTranslateScale(Direction.Left, moveX);
        } else if (moveX < 0) {
            if (mLastTranslateDeirction != Direction.Right) {
                mLastTranslateDeirction = Direction.Right;
                layoutThreeExtraItem(Direction.Right);
                computeHoriChildren(Direction.Right);
            }
            computeTranslateScale(Direction.Right, moveX);
        }
    }

    private void computeHoriChildren(Direction direction) {
        GestureItemView item = null;
        int position = 0;
        if (direction == Direction.Left) {
            mHoriChildren[0] = new GestureItemView[5];
            mHoriChildren[1] = new GestureItemView[6];
            mHoriChildren[2] = new GestureItemView[5];
            for (int i = 0; i < getChildCount(); i++) {
                item = (GestureItemView) getChildAt(i);
                position = ((LayoutParams) item.getLayoutParams()).position;
                if (position == -1) {
                    mHoriChildren[0][0] = item;
                } else if (position == 9) {
                    mHoriChildren[0][1] = item;
                } else if (position == 2) {
                    mHoriChildren[0][2] = item;
                } else if (position == 3) {
                    mHoriChildren[0][3] = item;
                } else if (position == 10) {
                    mHoriChildren[0][4] = item;
                } else if (position == -2) {
                    mHoriChildren[1][0] = item;
                } else if (position == 8) {
                    mHoriChildren[1][1] = item;
                } else if (position == 1) {
                    mHoriChildren[1][2] = item;
                } else if (position == 0) {
                    mHoriChildren[1][3] = item;
                } else if (position == 4) {
                    mHoriChildren[1][4] = item;
                } else if (position == 11) {
                    mHoriChildren[1][5] = item;
                } else if (position == -3) {
                    mHoriChildren[2][0] = item;
                } else if (position == 7) {
                    mHoriChildren[2][1] = item;
                } else if (position == 6) {
                    mHoriChildren[2][2] = item;
                } else if (position == 5) {
                    mHoriChildren[2][3] = item;
                } else if (position == 12) {
                    mHoriChildren[2][4] = item;
                }
            }
        } else if (direction == Direction.Right) {
            mHoriChildren[0] = new GestureItemView[5];
            mHoriChildren[1] = new GestureItemView[6];
            mHoriChildren[2] = new GestureItemView[5];
            for (int i = 0; i < getChildCount(); i++) {
                item = (GestureItemView) getChildAt(i);
                position = ((LayoutParams) item.getLayoutParams()).position;
                if (position == -1) {
                    mHoriChildren[0][4] = item;
                } else if (position == 9) {
                    mHoriChildren[0][0] = item;
                } else if (position == 2) {
                    mHoriChildren[0][1] = item;
                } else if (position == 3) {
                    mHoriChildren[0][2] = item;
                } else if (position == 10) {
                    mHoriChildren[0][3] = item;
                } else if (position == -2) {
                    mHoriChildren[1][5] = item;
                } else if (position == 8) {
                    mHoriChildren[1][0] = item;
                } else if (position == 1) {
                    mHoriChildren[1][1] = item;
                } else if (position == 0) {
                    mHoriChildren[1][2] = item;
                } else if (position == 4) {
                    mHoriChildren[1][3] = item;
                } else if (position == 11) {
                    mHoriChildren[1][4] = item;
                } else if (position == -3) {
                    mHoriChildren[2][4] = item;
                } else if (position == 7) {
                    mHoriChildren[2][0] = item;
                } else if (position == 6) {
                    mHoriChildren[2][1] = item;
                } else if (position == 5) {
                    mHoriChildren[2][2] = item;
                } else if (position == 12) {
                    mHoriChildren[2][3] = item;
                }
            }
        }
    }

    private void computeTranslateScale(Direction direction, float moveX) {
        int i;
        float offset, moveY;
        float rawScale1, rawScale2, targetScale;
        if (direction == Direction.Left) {
            for (i = 0; i < mHoriChildren[0].length; i++) {
                rawScale1 = ((LayoutParams) mHoriChildren[0][i].getLayoutParams()).scale;
                if (i == mHoriChildren[0].length - 1) {
                    offset = mTotalWidth - mHoriChildren[0][i].getRight();
                    targetScale = rawScale1 - moveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[0][i + 1].getLayoutParams()).scale;
                    offset = mHoriChildren[0][i + 1].getLeft() - mHoriChildren[0][i].getLeft();
                    targetScale = rawScale1 + moveX / offset * (rawScale2 - rawScale1);
                    moveY = computeTranslateY(mHoriChildren[0][i], mHoriChildren[0][i + 1], moveX);
                }
                mHoriChildren[0][i].setScaleX(targetScale);
                mHoriChildren[0][i].setScaleY(targetScale);
                mHoriChildren[0][i].setTranslationX(moveX);
                mHoriChildren[0][i].setTranslationY(moveY);
            }
            for (i = 0; i < mHoriChildren[1].length; i++) {
                rawScale1 = ((LayoutParams) mHoriChildren[1][i].getLayoutParams()).scale;
                if (i == mHoriChildren[1].length - 1) {
                    offset = mTotalWidth
                            - (mHoriChildren[1][i].getLeft() + mHoriChildren[1][i].getWidth() / 2
                                    * (1 + rawScale1));
                    targetScale = rawScale1 - moveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[1][i + 1].getLayoutParams()).scale;
                    if (i == 0) {
                        offset = mHoriChildren[1][i + 1].getMeasuredWidth() / 2;
                        targetScale = rawScale1 + moveX / offset * (rawScale2 - rawScale1);
                        moveY = 0f;
                        LeoLog.e("====", "rawScale1 = " + rawScale1 + "      rawScale2 = "
                                + rawScale2
                                + "       targetScale = " + targetScale);
                    } else {
                        offset = mHoriChildren[1][i + 1].getLeft() - mHoriChildren[1][i].getLeft();
                        targetScale = rawScale1 + moveX / offset * (rawScale2 - rawScale1);
                        moveY = computeTranslateY(mHoriChildren[1][i], mHoriChildren[1][i + 1],
                                moveX);
                    }
                }

                mHoriChildren[1][i].setScaleX(targetScale);
                mHoriChildren[1][i].setScaleY(targetScale);
                mHoriChildren[1][i].setTranslationX(moveX);
                mHoriChildren[1][i].setTranslationY(moveY);
            }
            for (i = 0; i < mHoriChildren[2].length; i++) {
                rawScale1 = ((LayoutParams) mHoriChildren[2][i].getLayoutParams()).scale;
                if (i == mHoriChildren[2].length - 1) {
                    offset = mTotalWidth - mHoriChildren[2][i].getRight();
                    targetScale = rawScale1 - moveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[1][i + 1].getLayoutParams()).scale;
                    offset = mHoriChildren[2][i + 1].getLeft() - mHoriChildren[2][i].getLeft();
                    targetScale = rawScale1 + moveX / offset * (rawScale2 - rawScale1);
                    moveY = computeTranslateY(mHoriChildren[2][i], mHoriChildren[2][i + 1], moveX);
                }
                mHoriChildren[2][i].setScaleX(targetScale);
                mHoriChildren[2][i].setScaleY(targetScale);
                mHoriChildren[2][i].setTranslationX(moveX);
                mHoriChildren[2][i].setTranslationY(moveY);
            }
        } else if (direction == Direction.Right) {
            for (i = mHoriChildren[0].length - 1; i >= 0; i--) {
                rawScale1 = ((LayoutParams) mHoriChildren[0][i].getLayoutParams()).scale;
                if (i == 0) {
                    offset = 0 - mHoriChildren[0][i].getLeft();
                    targetScale = rawScale1 - moveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[0][i - 1].getLayoutParams()).scale;
                    offset = mHoriChildren[0][i - 1].getLeft() - mHoriChildren[0][i].getLeft();
                    targetScale = rawScale1 + moveX / offset * (rawScale2 - rawScale1);
                    moveY = computeTranslateY(mHoriChildren[0][i], mHoriChildren[0][i - 1], moveX);
                }
                mHoriChildren[0][i].setScaleX(targetScale);
                mHoriChildren[0][i].setScaleY(targetScale);
                mHoriChildren[0][i].setTranslationX(moveX);
                mHoriChildren[0][i].setTranslationY(moveY);
            }
            for (i = mHoriChildren[1].length - 1; i >= 0; i--) {
                rawScale1 = ((LayoutParams) mHoriChildren[1][i].getLayoutParams()).scale;
                if (i == 0) {
                    offset = mHoriChildren[1][i].getRight() - mHoriChildren[1][i].getWidth() / 2
                            * (1 + rawScale1);
                    targetScale = rawScale1 + moveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[1][i - 1].getLayoutParams()).scale;
                    offset = mHoriChildren[1][i - 1].getLeft() - mHoriChildren[1][i].getLeft();
                    targetScale = rawScale1 + moveX / offset * (rawScale2 - rawScale1);
                    if (i == 5) {
                        offset = mHoriChildren[1][i - 1].getLeft()
                                + mHoriChildren[1][i - 1].getMeasuredWidth() / 2 - mTotalWidth;
                        targetScale = rawScale1 + moveX / offset * (rawScale2 - rawScale1);
                        moveY = 0f;
                        LeoLog.e("====", "rawScale1 = " + rawScale1 + "      rawScale2 = "
                                + rawScale2
                                + "       targetScale = " + targetScale);
                    } else {
                        offset = mHoriChildren[1][i - 1].getLeft() - mHoriChildren[1][i].getLeft();
                        targetScale = rawScale1 + moveX
                                / offset * (rawScale2 - rawScale1);
                        moveY = computeTranslateY(mHoriChildren[1][i], mHoriChildren[1][i - 1],
                                moveX);
                    }
                }
                mHoriChildren[1][i].setScaleX(targetScale);
                mHoriChildren[1][i].setScaleY(targetScale);
                mHoriChildren[1][i].setTranslationX(moveX);
                mHoriChildren[1][i].setTranslationY(moveY);
            }
            for (i = mHoriChildren[2].length - 1; i >= 0; i--) {
                rawScale1 = ((LayoutParams) mHoriChildren[2][i].getLayoutParams()).scale;
                if (i == 0) {
                    offset = 0 - mHoriChildren[2][i].getLeft();
                    targetScale = rawScale1 - moveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[2][i - 1].getLayoutParams()).scale;
                    offset = mHoriChildren[2][i - 1].getLeft() - mHoriChildren[2][i].getLeft();
                    targetScale = rawScale1 + moveX
                            / offset * (rawScale2 - rawScale1);
                    moveY = computeTranslateY(mHoriChildren[2][i], mHoriChildren[2][i - 1], moveX);
                }
                mHoriChildren[2][i].setScaleX(targetScale);
                mHoriChildren[2][i].setScaleY(targetScale);
                mHoriChildren[2][i].setTranslationX(moveX);
                mHoriChildren[2][i].setTranslationY(moveY);
            }
        }
    }

    private float computeTranslateY(GestureItemView from, GestureItemView to, float tranX) {
        float dx = to.getLeft() - from.getLeft();
        float dy = to.getTop() - from.getTop();
        float resault;
        resault = dy / dx * tranX;
        return resault;
    }
}
