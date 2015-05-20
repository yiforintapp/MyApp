
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
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.Orientation;
import com.leo.appmaster.utils.LeoLog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
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

    private static final int MAX_COUNT = 11;
    private static final int INNER_RING_MAX_COUNT = 6;
    private static final int OUTER_RING_MAXCOUNT = 4;

    private AppleWatchContainer mContainer;
    private AnimatorSet mReorderAnimator;
    private boolean mRecodering;
    private boolean mAnimCanceled;
    private int mTotalWidth, mTotalHeight;
    private int mCenterPointX, mCenterPointY;
    private int mItemSize, mIconSize;
    private int mInnerRadius, mOuterRadius;
    private float mInnerScale, mOuterScale;

    private int mRingCount;
    private float mCurrentRotateDegree;
    private Context mContext;

    public AppleWatchLayout(Context context) {
        this(context, null);
    }

    public AppleWatchLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;

        init();
    }

    private void init() {
        Resources res = getContext().getResources();
        mItemSize = res.getDimensionPixelSize(R.dimen.apple_watch_item_size);
        mIconSize = res.getDimensionPixelSize(R.dimen.apple_watch_item_icon_size);
        mInnerRadius = res.getDimensionPixelSize(R.dimen.apple_watch_layout_inner_radius);
        mOuterRadius = res.getDimensionPixelSize(R.dimen.apple_watch_layout_outer_radius);
        mRingCount = 2;
        mInnerScale = 0.8f;
        mOuterScale = 0.6f;
    }

    public AppleWatchLayout(Context context, AttributeSet attrs, int defStyle) {
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

        mTotalWidth = getMeasuredWidth();
        mTotalHeight = getMeasuredHeight();
        mCenterPointX = mTotalWidth / 2;
        mCenterPointY = mTotalHeight / 2;
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
        } else {
            mRingCount = 1;
        }

        int innerRingCount = 6;
        float innertAngleInterval, outerAngleInterval;
        innertAngleInterval = 60;
        outerAngleInterval = 120;
        float innerStartAngle, outerStartAngle;
        innerStartAngle = 0;
        outerStartAngle = 30;

        int halfItemSize;

        int left = 0, top = 0;
        double outerItemAngle = 30;

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

            } else if (params.position <= 10) { // match second ring

                if (params.position == 7) {
                    outerItemAngle = 30;
                } else if (params.position == 8) {
                    outerItemAngle = 150;
                } else if (params.position == 9) {
                    outerItemAngle = 210;
                } else if (params.position == 10) {
                    outerItemAngle = 330;
                }

                left = (int) (mCenterPointX - mOuterRadius
                        * Math.cos(Math.toRadians(outerItemAngle)) - halfItemSize);

                top = (int) (mCenterPointY
                        - mOuterRadius
                        * Math.sin(Math.toRadians(outerItemAngle)) - halfItemSize);

            }

            child.layout(left, top, left + child.getMeasuredWidth(),
                    top + child.getMeasuredHeight());
        }

        /*
         * now set pivot
         */
        setPivotX(mTotalWidth / 2);
        setPivotY(mTotalHeight);

        mContainer = (AppleWatchContainer) getParent();
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
        if (position <= 0) {
            return 1f;
        } else if (position <= 6) {
            return mInnerScale;
        } else if (position <= 10) {
            return mOuterScale;
        } else {
            LeoLog.e(TAG, "position must be >=0 and <= 10");
            return 0f;
        }
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
                        .toggleBluetooth(mContainer, mContainer.getSwitchList(),
                                AppleWatchLayout.this);
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.FLASHLIGHT)) {
                QuickSwitchManager.getInstance(getContext())
                        .toggleFlashLight(mContainer, mContainer.getSwitchList(),
                                AppleWatchLayout.this);
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.WLAN)) {
                QuickSwitchManager.getInstance(getContext()).toggleWlan(mContainer,
                        mContainer.getSwitchList(), AppleWatchLayout.this);
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.CRAME)) {
                QuickSwitchManager.getInstance(getContext()).openCrame();
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.SOUND)) {
                QuickSwitchManager.getInstance(getContext()).toggleSound(mContainer,
                        mContainer.getSwitchList(), AppleWatchLayout.this);
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.LIGHT)) {
                QuickSwitchManager.getInstance(getContext()).toggleLight(mContainer,
                        mContainer.getSwitchList(), AppleWatchLayout.this);
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
                QuickSwitchManager.getInstance(getContext()).toggleRotation(mContainer,
                        mContainer.getSwitchList(), AppleWatchLayout.this);
            } else if (sInfo.iDentiName.equals(QuickSwitchManager.MOBILEDATA)) {
                QuickSwitchManager.getInstance(getContext())
                        .toggleMobileData(mContainer, mContainer.getSwitchList(),
                                AppleWatchLayout.this);
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
            // TODO show add new item icon
            // 方案一，失败
            // String switchListString =
            // QuickSwitchManager.getInstance(mContext).getListStringFromSp();
            // List<QuickSwitcherInfo> mNowList =
            // QuickSwitchManager.getInstance(mContext).StringToList(switchListString);
            // QuickSwitcherInfo mXuKuang =
            // QuickSwitchManager.getInstance(mContext).getXuKuangInfo();
            // mXuKuang.position = mNowList.size();
            // mNowList.add(mXuKuang);
            // mContainer.fillSwitchItem(QuickGestureLayout.this,mNowList);
            // 方案二，直接加个view
            // GestureItemView mIvXuKuang =
            // QuickSwitchManager.getInstance(mContext).getXuKuang(
            // hitView);
            // addView(mIvXuKuang);

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
        ObjectAnimator animX = ObjectAnimator.ofFloat(fromView, "translationX",
                fromView.getTranslationX(), toView.getLeft() - fromView.getLeft());
        ObjectAnimator animY = ObjectAnimator.ofFloat(fromView, "translationY",
                fromView.getTranslationY(), toView.getTop() - fromView.getTop());
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
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
            if (childCount < 9 && !view.hasAddFlag()) {
                showAddIcon(childCount);
            }
        }
    }

    private void showAddIcon(int position) {
        if (position >= 0 && position <= 10) {
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
}
