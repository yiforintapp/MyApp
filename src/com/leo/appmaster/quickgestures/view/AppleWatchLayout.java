
package com.leo.appmaster.quickgestures.view;

import java.util.ArrayList;
import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.GestureEmptyItemInfo;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
import com.leo.appmaster.utils.LeoLog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
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
    private float mLastMovex;
    private float mMinuOffset;
    private int mAdjustCount = 0;
    private int mSnapDuration = 800;
    private boolean mSnapping;

    public static enum Direction {
        Right, Left, None;
    }

    private boolean mNeedRelayoutExtraItem;

    public AppleWatchLayout(Context context) {
        this(context, null);
    }

    public AppleWatchLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppleWatchLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public void fillItems(List<BaseInfo> infos) {
        BaseInfo info = null;
        if (infos.size() > 13) {
            infos = infos.subList(0, 13);
        } else if (infos.size() < 13) {
            int[] hit = new int[13];
            for (int i = 0; i < infos.size(); i++) {
                info = (BaseInfo) infos.get(i);
                if (info.gesturePosition < 0 || info.gesturePosition > 13) {
                    info.gesturePosition = i;
                }
                hit[info.gesturePosition] = 1;
            }

            for (int i = 0; i < hit.length; i++) {
                if (hit[i] != 1) {
                    info = new GestureEmptyItemInfo();
                    info.gesturePosition = i;
                    infos.add(info);
                }
            }
        } else {
            for (int i = 0; i < infos.size(); i++) {
                info = (BaseInfo) infos.get(i);
                if (info.gesturePosition < 0 || info.gesturePosition > 13) {
                    info.gesturePosition = i;
                }
            }
        }

        removeAllViews();
        GestureItemView gestureItem = null;
        AppleWatchLayout.LayoutParams lp = null;
        for (int i = 0; i < infos.size(); i++) {
            info = (BaseInfo) infos.get(i);
            gestureItem = makeGestureItem();
            lp = new AppleWatchLayout.LayoutParams(
                    mItemSize, mItemSize);
            if (info.gesturePosition == -1000) {
                lp.position = i;
            } else {
                lp.position = info.gesturePosition;
            }
            gestureItem.setGravity(Gravity.CENTER);
            gestureItem.setLayoutParams(lp);
            gestureItem.setItemName(info.label);
            if (info instanceof GestureEmptyItemInfo) {
                info.icon = QuickGestureManager.getInstance(getContext()).applyEmptyIcon();
            }
            gestureItem.setItemIcon(info.icon);
            if (info.eventNumber > 0) {
                gestureItem.setDecorateAction(new EventAction(getContext(), info.eventNumber));
            }
            gestureItem.setTag(info);
            addView(gestureItem);
            computeCenterChildren(gestureItem, lp.position, false);
        }
        requestLayout();
        postDelayed(new Runnable() {

            @Override
            public void run() {
                fillExtraChildren();
            }
        }, 500);

    }

    private void fillExtraChildren() {
        GestureItemView addView, tempView;
        BaseInfo info = null;
        LayoutParams lp, temp;
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize,
                MeasureSpec.EXACTLY);
        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemSize,
                MeasureSpec.EXACTLY);
        for (int i = 0; i < 8; i++) {
            if (i < 4) {
                tempView = mHoriChildren[0][i + 4];
                info = (BaseInfo) tempView.getTag();
                addView = makeGestureItem();
                temp = (LayoutParams) tempView.getLayoutParams();
                lp = new LayoutParams(temp.width, temp.height);
                lp.position = -1;
                lp.scale = temp.scale;
                addView.setLayoutParams(lp);
                inflateItem(addView, info);
                mHoriChildren[0][i] = addView;
                addView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                addView(addView);
                addView.layout(tempView.getLeft() - mTotalWidth, tempView.getTop(),
                        tempView.getRight() - mTotalWidth, tempView.getBottom());
                addView.setScaleX(lp.scale);
                addView.setScaleY(lp.scale);
            } else {
                tempView = mHoriChildren[0][i];
                info = (BaseInfo) tempView.getTag();
                addView = makeGestureItem();
                temp = (LayoutParams) tempView.getLayoutParams();
                lp = new LayoutParams(temp.width, temp.height);
                lp.position = -1;
                lp.scale = temp.scale;
                inflateItem(addView, info);
                addView.setLayoutParams(lp);
                mHoriChildren[0][i + 4] = addView;
                addView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                addView(addView);
                addView.layout(tempView.getLeft() + mTotalWidth, tempView.getTop(),
                        tempView.getRight() + mTotalWidth, tempView.getBottom());
                addView.setScaleX(lp.scale);
                addView.setScaleY(lp.scale);
            }

        }

        for (int i = 0; i < 10; i++) {
            if (i < 5) {
                tempView = mHoriChildren[1][i + 5];
                info = (BaseInfo) tempView.getTag();
                addView = makeGestureItem();
                temp = (LayoutParams) tempView.getLayoutParams();
                lp = new LayoutParams(temp.width, temp.height);
                lp.position = -1;
                lp.scale = temp.scale;
                addView.setLayoutParams(lp);
                inflateItem(addView, info);
                mHoriChildren[1][i] = addView;
                addView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                addView(addView);
                addView.layout(tempView.getLeft() - mTotalWidth, tempView.getTop(),
                        tempView.getRight() - mTotalWidth, tempView.getBottom());
                addView.setScaleX(lp.scale);
                addView.setScaleY(lp.scale);
            } else {
                tempView = mHoriChildren[1][i];
                info = (BaseInfo) mHoriChildren[1][i].getTag();
                addView = makeGestureItem();
                temp = (LayoutParams) tempView.getLayoutParams();
                lp = new LayoutParams(temp.width, temp.height);
                lp.position = -1;
                lp.scale = temp.scale;
                addView.setLayoutParams(lp);
                inflateItem(addView, info);
                mHoriChildren[1][i + 5] = addView;
                addView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                addView(addView);
                addView.layout(tempView.getLeft() + mTotalWidth, tempView.getTop(),
                        tempView.getRight() + mTotalWidth, tempView.getBottom());
                addView.setScaleX(lp.scale);
                addView.setScaleY(lp.scale);
            }
        }
        for (int i = 0; i < 8; i++) {
            if (i < 4) {
                tempView = mHoriChildren[2][i + 4];
                info = (BaseInfo) tempView.getTag();
                addView = makeGestureItem();
                temp = (LayoutParams) tempView.getLayoutParams();
                lp = new LayoutParams(temp.width, temp.height);
                lp.position = -1;
                lp.scale = temp.scale;
                addView.setLayoutParams(lp);
                inflateItem(addView, info);
                mHoriChildren[2][i] = addView;
                addView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                addView(addView);
                addView.layout(tempView.getLeft() - mTotalWidth, tempView.getTop(),
                        tempView.getRight() - mTotalWidth, tempView.getBottom());
                addView.setScaleX(lp.scale);
                addView.setScaleY(lp.scale);
            } else {
                tempView = mHoriChildren[2][i];
                info = (BaseInfo) tempView.getTag();
                addView = makeGestureItem();
                temp = (LayoutParams) tempView.getLayoutParams();
                lp = new LayoutParams(temp.width, temp.height);
                lp.position = -1;
                lp.scale = temp.scale;
                addView.setLayoutParams(lp);
                inflateItem(addView, info);
                mHoriChildren[2][i + 4] = addView;
                addView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                addView(addView);
                addView.layout(tempView.getLeft() + mTotalWidth, tempView.getTop(),
                        tempView.getRight() + mTotalWidth, tempView.getBottom());
                addView.setScaleX(lp.scale);
                addView.setScaleY(lp.scale);
            }
        }
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
        mHoriChildren[0] = new GestureItemView[12];
        mHoriChildren[1] = new GestureItemView[15];
        mHoriChildren[2] = new GestureItemView[12];
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

    public AppleWatchContainer getContainer() {
        return mContainer;
    }

    @Override
    protected void onFinishInflate() {
        init();
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
            if (lp.position < 0)
                continue;
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
            params.scale = getItemScale(params.position);
            child.setScaleX(params.scale);
            child.setScaleY(params.scale);
        }

        if (mNeedRelayoutExtraItem) {
            relayoutExtraChildren();
            mNeedRelayoutExtraItem = false;
        }

        /*
         * now set pivot
         */
        if (mContainer == null) {
            setPivotX(mTotalWidth / 2);
            setPivotY(mTotalHeight * 3);
            mContainer = (AppleWatchContainer) getParent();
        }
    }

    private void inflateItem(GestureItemView item, BaseInfo info) {
        if (item == null || info == null)
            return;
        item.setGravity(Gravity.CENTER_HORIZONTAL);
        item.setItemName(info.label);
        item.setItemIcon(info.icon);
        if (info.eventNumber > 0) {
            item.setDecorateAction(new EventAction(getContext(), info.eventNumber));
        }
        item.setTag(info);
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
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
        computeCenterChildren((GestureItemView) child, addPosition, true);
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
        int position = ((LayoutParams) item.getLayoutParams()).position;
        BaseInfo info = (BaseInfo) view.getTag();
        info.gesturePosition = position;
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
                QuickSwitchManager.getInstance(getContext()).speedUp(sInfo);
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
                if (giv.isEmptyIcon()) {
                    GType type = mContainer.getCurrentGestureType();
                    if (type == GType.MostUsedLayout || type == GType.SwitcherLayout) {
                        showAddNewDiglog(type);
                    }
                } else {
                    Rect rect = giv.getCrossRect();
                    int offsetX = (int) (x - hitView.getLeft());
                    int onnsetY = (int) (y - hitView.getTop());
                    if (rect.contains(offsetX, onnsetY)) {
                        replaceEmptyIcon(giv);
                    }
                }
            } else {
                animateItem(hitView);
            }
        }
    }

    private void showAddNewDiglog(GType type) {
        // TODO Auto-generated method stub
        QuickGestureManager qgm = QuickGestureManager.getInstance(getContext());
        if (type == GType.MostUsedLayout) {
            Toast.makeText(getContext(), "添加最近使用item", 0).show();
            qgm.showCommontAppDialog(getContext());
        } else if (type == GType.SwitcherLayout) {
            Toast.makeText(getContext(), "添加快捷开关item", 0).show();
            qgm.showQuickSwitchDialog(getContext());
        }
    }

    private void replaceEmptyIcon(GestureItemView hitView) {
        GType type = mContainer.getCurrentGestureType();
        if (type == GType.DymicLayout) {
            QuickGestureManager.getInstance(getContext()).checkEventItemRemoved(
                    (BaseInfo) hitView.getTag());
        } else if (type == GType.SwitcherLayout) {
        } else if (type == GType.MostUsedLayout) {
        }

        BaseInfo baseInfo = (BaseInfo) hitView.getTag();
        GestureEmptyItemInfo info = new GestureEmptyItemInfo();
        info.gesturePosition = baseInfo.gesturePosition;
        info.icon = QuickGestureManager.getInstance(getContext()).applyEmptyIcon();
        hitView.setItemIcon(info.icon);
        hitView.setItemName(info.label);
        hitView.setDecorateAction(null);
        hitView.setTag(info);
        hitView.enterEditMode();
        saveReorderPosition();
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
                if (!rect.contains(offsetX, onnsetY)) {
                    GType type = mContainer.getCurrentGestureType();
                    if (type == GType.DymicLayout) {
                        if (!giv.isEmptyIcon()) {
                            hitView.startDrag(null, new GestureDragShadowBuilder(hitView, 2.0f),
                                    hitView, 0);
                            hitView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        }
                    } else {
                        hitView.startDrag(null, new GestureDragShadowBuilder(hitView, 2.0f),
                                hitView, 0);
                        hitView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    }
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
                List<BaseInfo> mSwitchList = new ArrayList<BaseInfo>();
                for (int i = 0; i < mNum; i++) {
                    params = (LayoutParams) getChildAt(i).getLayoutParams();
                    int position = params.position;
                    if (position > -1) {
                        BaseInfo sInfo = (BaseInfo) getChildAt(i).getTag();
                        if (sInfo != null) {
                            sInfo.gesturePosition = position;
                            mSwitchList.add(sInfo);
                        }
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
        for (int i = 0; i < getChildCount(); i++) {
            GestureItemView item = (GestureItemView) getChildAt(i);
            item.leaveEditMode();
        }
    }

    public void onEnterEditMode() {
        if (mContainer.getCurrentGestureType() != GType.DymicLayout) {
            int childCount = getChildCount();
            GestureItemView view = (GestureItemView) getChildAt(childCount - 1);
            if (childCount < 13 && !view.isEmptyIcon()) {
                showAddIcon(childCount);
            }
        }
    }

    private void showAddIcon(int position) {
        if (position >= 0 && position <= 12) {
            LayoutParams params = new LayoutParams(mItemSize, mItemSize);
            params.position = position;
            GestureItemView addItem = makeGestureItem();
            addItem.setLayoutParams(params);
            addItem.setBackgroundResource(R.drawable.switch_add);
            GestureEmptyItemInfo info = new GestureEmptyItemInfo();
            info.icon = QuickGestureManager.getInstance(getContext()).applyEmptyIcon();
            addItem.setTag(info);
            addView(addItem);
        }
    }

    public void translateItem(float moveX) {
        if (moveX > 0) {
            computeTranslateScale(Direction.Left, moveX);
        } else if (moveX < 0) {
            computeTranslateScale(Direction.Right, moveX);
        }
    }

    private void computeCenterChildren(GestureItemView view, int position, boolean newAdd) {
        if (position == 9) {
            mHoriChildren[0][4] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[0][0], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[0][8], (BaseInfo) view.getTag());
            }
        } else if (position == 2) {
            mHoriChildren[0][5] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[0][1], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[0][9], (BaseInfo) view.getTag());
            }
        } else if (position == 3) {
            mHoriChildren[0][6] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[0][2], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[0][10], (BaseInfo) view.getTag());
            }
        } else if (position == 10) {
            mHoriChildren[0][7] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[0][3], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[0][11], (BaseInfo) view.getTag());
            }
        } else if (position == 8) {
            mHoriChildren[1][5] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[1][0], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[1][10], (BaseInfo) view.getTag());
            }
        } else if (position == 1) {
            mHoriChildren[1][6] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[1][1], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[1][11], (BaseInfo) view.getTag());
            }
        } else if (position == 0) {
            mHoriChildren[1][7] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[1][2], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[1][12], (BaseInfo) view.getTag());
            }
        } else if (position == 4) {
            mHoriChildren[1][8] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[1][3], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[1][13], (BaseInfo) view.getTag());
            }
        } else if (position == 11) {
            mHoriChildren[1][9] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[1][4], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[1][14], (BaseInfo) view.getTag());
            }
        } else if (position == 7) {
            mHoriChildren[2][4] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[2][0], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[2][8], (BaseInfo) view.getTag());
            }
        } else if (position == 6) {
            mHoriChildren[2][5] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[2][1], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[2][9], (BaseInfo) view.getTag());
            }
        } else if (position == 5) {
            mHoriChildren[2][6] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[2][2], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[2][10], (BaseInfo) view.getTag());
            }
        } else if (position == 12) {
            mHoriChildren[2][7] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[2][3], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[2][11], (BaseInfo) view.getTag());
            }
        }
    }

    private float getOffset(GestureItemView from, GestureItemView to, Direction direction) {
        float offset = 0.0f;
        if (direction == Direction.Left) {
            if (from != null && to != null) {
                offset = to.getLeft() - from.getLeft();
            } else if (from != null) {
                offset = mTotalWidth - from.getLeft() - from.getMeasuredWidth() / 2;
            } else if (to != null) {
                offset = to.getRight() - to.getMeasuredWidth() / 2;
            }
        } else if (direction == Direction.Right) {
            if (from != null && to != null) {
                offset = to.getLeft() - from.getLeft();
            } else if (from != null) {
                offset = from.getMeasuredWidth() / 2 - from.getRight();
            } else if (to != null) {
                offset = mTotalWidth - to.getLeft() - to.getMeasuredWidth() / 2;
            }
        }

        return offset;
    }

    private void adjustIconPosition(Direction direction) {
        int i, firstPosition, lastPosition;
        GestureItemView tempView;
        if (direction == Direction.Left) {
            firstPosition = ((LayoutParams) mHoriChildren[0][0].getLayoutParams()).position;
            for (i = 0; i < mHoriChildren[0].length; i++) {
                if (i == mHoriChildren[0].length - 1) {
                    ((LayoutParams) mHoriChildren[0][i].getLayoutParams()).position = firstPosition;
                } else {
                    ((LayoutParams) mHoriChildren[0][i].getLayoutParams()).position = ((LayoutParams) mHoriChildren[0][i + 1]
                            .getLayoutParams()).position;
                }
                mHoriChildren[0][i].setTranslationX(0f);
                mHoriChildren[0][i].setTranslationY(0f);
            }
            tempView = mHoriChildren[0][mHoriChildren[0].length - 1];
            for (i = mHoriChildren[0].length - 1; i >= 0; i--) {
                if (i == 0) {
                    mHoriChildren[0][i] = tempView;
                } else {
                    mHoriChildren[0][i] = mHoriChildren[0][i - 1];
                }
            }

            firstPosition = ((LayoutParams) mHoriChildren[1][0].getLayoutParams()).position;
            for (i = 0; i < mHoriChildren[1].length; i++) {
                if (i == mHoriChildren[1].length - 1) {
                    ((LayoutParams) mHoriChildren[1][i].getLayoutParams()).position = firstPosition;
                } else {
                    ((LayoutParams) mHoriChildren[1][i].getLayoutParams()).position = ((LayoutParams) mHoriChildren[1][i + 1]
                            .getLayoutParams()).position;
                }
                mHoriChildren[1][i].setTranslationX(0f);
                mHoriChildren[1][i].setTranslationY(0f);
            }
            tempView = mHoriChildren[1][mHoriChildren[1].length - 1];
            for (i = mHoriChildren[1].length - 1; i >= 0; i--) {
                if (i == 0) {
                    mHoriChildren[1][i] = tempView;
                } else {
                    mHoriChildren[1][i] = mHoriChildren[1][i - 1];
                }
            }

            firstPosition = ((LayoutParams) mHoriChildren[2][0].getLayoutParams()).position;
            for (i = 0; i < mHoriChildren[2].length; i++) {
                if (i == mHoriChildren[2].length - 1) {
                    ((LayoutParams) mHoriChildren[2][i].getLayoutParams()).position = firstPosition;
                } else {
                    ((LayoutParams) mHoriChildren[2][i].getLayoutParams()).position = ((LayoutParams) mHoriChildren[2][i + 1]
                            .getLayoutParams()).position;
                }
                mHoriChildren[2][i].setTranslationX(0f);
                mHoriChildren[2][i].setTranslationY(0f);
            }
            tempView = mHoriChildren[2][mHoriChildren[2].length - 1];
            for (i = mHoriChildren[2].length - 1; i >= 0; i--) {
                if (i == 0) {
                    mHoriChildren[2][i] = tempView;
                } else {
                    mHoriChildren[2][i] = mHoriChildren[2][i - 1];
                }
            }

        } else if (direction == Direction.Right) {
            lastPosition = ((LayoutParams) mHoriChildren[0][mHoriChildren[0].length - 1]
                    .getLayoutParams()).position;
            for (i = mHoriChildren[0].length - 1; i >= 0; i--) {
                if (i == 0) {
                    ((LayoutParams) mHoriChildren[0][i].getLayoutParams()).position = lastPosition;
                } else {
                    ((LayoutParams) mHoriChildren[0][i].getLayoutParams()).position = ((LayoutParams) mHoriChildren[0][i - 1]
                            .getLayoutParams()).position;
                }
                mHoriChildren[0][i].setTranslationX(0f);
                mHoriChildren[0][i].setTranslationY(0f);
            }
            tempView = mHoriChildren[0][0];
            for (i = 0; i < mHoriChildren[0].length; i++) {
                if (i == mHoriChildren[0].length - 1) {
                    mHoriChildren[0][i] = tempView;
                } else {
                    mHoriChildren[0][i] = mHoriChildren[0][i + 1];
                }
            }

            lastPosition = ((LayoutParams) mHoriChildren[1][mHoriChildren[1].length - 1]
                    .getLayoutParams()).position;
            for (i = mHoriChildren[1].length - 1; i >= 0; i--) {
                if (i == 0) {
                    ((LayoutParams) mHoriChildren[1][i].getLayoutParams()).position = lastPosition;
                } else {
                    ((LayoutParams) mHoriChildren[1][i].getLayoutParams()).position = ((LayoutParams) mHoriChildren[1][i - 1]
                            .getLayoutParams()).position;
                }
                mHoriChildren[1][i].setTranslationX(0f);
                mHoriChildren[1][i].setTranslationY(0f);
            }
            tempView = mHoriChildren[1][0];
            for (i = 0; i < mHoriChildren[1].length; i++) {
                if (i == mHoriChildren[1].length - 1) {
                    mHoriChildren[1][i] = tempView;
                } else {
                    mHoriChildren[1][i] = mHoriChildren[1][i + 1];
                }
            }

            lastPosition = ((LayoutParams) mHoriChildren[2][mHoriChildren[2].length - 1]
                    .getLayoutParams()).position;
            for (i = mHoriChildren[2].length - 1; i >= 0; i--) {
                if (i == 0) {
                    ((LayoutParams) mHoriChildren[2][i].getLayoutParams()).position = lastPosition;
                } else {
                    ((LayoutParams) mHoriChildren[2][i].getLayoutParams()).position = ((LayoutParams) mHoriChildren[2][i - 1]
                            .getLayoutParams()).position;
                }
                mHoriChildren[2][i].setTranslationX(0f);
                mHoriChildren[2][i].setTranslationY(0f);
            }
            tempView = mHoriChildren[2][0];
            for (i = 0; i < mHoriChildren[2].length; i++) {
                if (i == mHoriChildren[2].length - 1) {
                    mHoriChildren[2][i] = tempView;
                } else {
                    mHoriChildren[2][i] = mHoriChildren[2][i + 1];
                }
            }
        }

        requestLayout();
        mNeedRelayoutExtraItem = true;
    }

    private void relayoutExtraChildren() {
        GestureItemView tempView, targetView;
        LayoutParams tempLp, targetLp;
        for (int i = 0; i < 8; i++) {
            if (i < 4) {
                targetView = mHoriChildren[0][i];
                tempView = mHoriChildren[0][i + 4];
                targetLp = (LayoutParams) targetView.getLayoutParams();
                tempLp = (LayoutParams) tempView.getLayoutParams();
                targetView.layout(tempView.getLeft() - mTotalWidth, tempView.getTop(),
                        tempView.getRight() - mTotalWidth, tempView.getBottom());
                targetLp.scale = tempLp.scale;
                targetView.setScaleX(targetLp.scale);
                targetView.setScaleY(targetLp.scale);
            } else {
                tempView = mHoriChildren[0][i];
                targetView = mHoriChildren[0][i + 4];
                targetLp = (LayoutParams) targetView.getLayoutParams();
                tempLp = (LayoutParams) tempView.getLayoutParams();
                targetView.layout(tempView.getLeft() + mTotalWidth, tempView.getTop(),
                        tempView.getRight() + mTotalWidth, tempView.getBottom());
                targetLp.scale = tempLp.scale;
                targetView.setScaleX(targetLp.scale);
                targetView.setScaleY(targetLp.scale);
            }

        }

        for (int i = 0; i < 10; i++) {
            if (i < 5) {
                targetView = mHoriChildren[1][i];
                tempView = mHoriChildren[1][i + 5];
                targetLp = (LayoutParams) targetView.getLayoutParams();
                tempLp = (LayoutParams) tempView.getLayoutParams();
                targetView.layout(tempView.getLeft() - mTotalWidth, tempView.getTop(),
                        tempView.getRight() - mTotalWidth, tempView.getBottom());
                targetLp.scale = tempLp.scale;
                targetView.setScaleX(targetLp.scale);
                targetView.setScaleY(targetLp.scale);
            } else {
                tempView = mHoriChildren[1][i];
                targetView = mHoriChildren[1][i + 5];
                targetLp = (LayoutParams) targetView.getLayoutParams();
                tempLp = (LayoutParams) tempView.getLayoutParams();
                targetView.layout(tempView.getLeft() + mTotalWidth, tempView.getTop(),
                        tempView.getRight() + mTotalWidth, tempView.getBottom());
                targetLp.scale = tempLp.scale;
                targetView.setScaleX(targetLp.scale);
                targetView.setScaleY(targetLp.scale);
            }
        }
        for (int i = 0; i < 8; i++) {
            if (i < 4) {
                targetView = mHoriChildren[2][i];
                tempView = mHoriChildren[2][i + 4];
                targetLp = (LayoutParams) targetView.getLayoutParams();
                tempLp = (LayoutParams) tempView.getLayoutParams();
                targetView.layout(tempView.getLeft() - mTotalWidth, tempView.getTop(),
                        tempView.getRight() - mTotalWidth, tempView.getBottom());
                targetLp.scale = tempLp.scale;
                targetView.setScaleX(targetLp.scale);
                targetView.setScaleY(targetLp.scale);
            } else {
                tempView = mHoriChildren[2][i];
                targetView = mHoriChildren[2][i + 4];
                targetLp = (LayoutParams) targetView.getLayoutParams();
                tempLp = (LayoutParams) tempView.getLayoutParams();
                targetView.layout(tempView.getLeft() + mTotalWidth, tempView.getTop(),
                        tempView.getRight() + mTotalWidth, tempView.getBottom());
                targetLp.scale = tempLp.scale;
                targetView.setScaleX(targetLp.scale);
                targetView.setScaleY(targetLp.scale);
            }
        }
    }

    /**
     * @param direction is new show location, not be scolle direction
     * @param moveX
     */
    private void computeTranslateScale(Direction direction, float moveX) {
        int i;
        float minuOffset, adjustMoveX;
        float offset, moveY;
        float rawScale1, rawScale2, targetScale;
        mLastMovex = moveX;
        if (direction == Direction.Left) {
            minuOffset = mHoriChildren[1][10].getLeft()
                    - mHoriChildren[1][9].getLeft();
            mMinuOffset = minuOffset;
            int shouldAdjustCount = (int) (moveX / minuOffset);
            if (mAdjustCount < shouldAdjustCount) {
                adjustIconPosition(Direction.Left);
                mAdjustCount++;
            } else if (mAdjustCount > shouldAdjustCount) {
                adjustIconPosition(Direction.Right);
                mAdjustCount--;
            }
            for (i = 0; i < mHoriChildren[0].length - 1; i++) {
                rawScale1 = ((LayoutParams) mHoriChildren[0][i]
                        .getLayoutParams()).scale;
                if (i == mHoriChildren[0].length - 1) {
                    // offset = getOffset(mHoriChildren[0][i], null,
                    // Direction.Left);
                    offset = moveX;
                    adjustMoveX = offset / minuOffset * (moveX - mAdjustCount * minuOffset);
                    targetScale = rawScale1 - adjustMoveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[0][i + 1]
                            .getLayoutParams()).scale;
                    offset = getOffset(mHoriChildren[0][i], mHoriChildren[0][i + 1],
                            Direction.Left);
                    adjustMoveX = offset / minuOffset * (moveX - mAdjustCount * minuOffset);
                    targetScale = rawScale1 + adjustMoveX / offset * (rawScale2 - rawScale1);
                    moveY = computeTranslateY(mHoriChildren[0][i],
                            mHoriChildren[0][i + 1],
                            adjustMoveX);
                }
                mHoriChildren[0][i].setScaleX(targetScale);
                mHoriChildren[0][i].setScaleY(targetScale);
                mHoriChildren[0][i].setTranslationX(adjustMoveX);
                mHoriChildren[0][i].setTranslationY(moveY);
            }
            for (i = 0; i < mHoriChildren[1].length - 1; i++) {
                rawScale1 = ((LayoutParams) mHoriChildren[1][i]
                        .getLayoutParams()).scale;
                if (i == mHoriChildren[1].length - 1) {
                    offset = moveX;
                    // offset = getOffset(mHoriChildren[1][i], null,
                    // Direction.Left);
                    adjustMoveX = offset / minuOffset * (moveX - mAdjustCount * minuOffset);
                    targetScale = rawScale1 - adjustMoveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[1][i + 1]
                            .getLayoutParams()).scale;
                    offset = getOffset(mHoriChildren[1][i], mHoriChildren[1][i
                            + 1],
                            Direction.Left);
                    adjustMoveX = offset / minuOffset * (moveX - mAdjustCount * minuOffset);
                    targetScale = rawScale1 + adjustMoveX / offset * (rawScale2 - rawScale1);
                    moveY = computeTranslateY(mHoriChildren[1][i],
                            mHoriChildren[1][i + 1],
                            adjustMoveX);
                }

                mHoriChildren[1][i].setScaleX(targetScale);
                mHoriChildren[1][i].setScaleY(targetScale);
                mHoriChildren[1][i].setTranslationX(adjustMoveX);
                mHoriChildren[1][i].setTranslationY(moveY);
            }
            for (i = 0; i < mHoriChildren[2].length - 1; i++) {
                rawScale1 = ((LayoutParams) mHoriChildren[2][i].getLayoutParams()).scale;
                if (i == mHoriChildren[1].length - 1) {
                    offset = moveX;
                    // offset = getOffset(mHoriChildren[2][i], null,
                    // Direction.Left);
                    adjustMoveX = offset / minuOffset * (moveX - mAdjustCount * minuOffset);
                    targetScale = rawScale1 - adjustMoveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[2][i + 1].getLayoutParams()).scale;
                    offset = getOffset(mHoriChildren[2][i], mHoriChildren[2][i + 1],
                            Direction.Left);
                    adjustMoveX = offset / minuOffset * (moveX - mAdjustCount * minuOffset);
                    targetScale = rawScale1 + adjustMoveX / offset * (rawScale2 - rawScale1);
                    moveY = computeTranslateY(mHoriChildren[2][i], mHoriChildren[2][i + 1],
                            adjustMoveX);
                }
                mHoriChildren[2][i].setScaleX(targetScale);
                mHoriChildren[2][i].setScaleY(targetScale);
                mHoriChildren[2][i].setTranslationX(adjustMoveX);
                mHoriChildren[2][i].setTranslationY(moveY);
            }
        } else if (direction == Direction.Right) {
            minuOffset = mHoriChildren[1][4].getRight()
                    - mHoriChildren[1][5].getRight();
            mMinuOffset = minuOffset;
            int shouldAdjustCount = (int) (moveX / minuOffset);
            if (mAdjustCount < shouldAdjustCount) {
                adjustIconPosition(Direction.Right);
                mAdjustCount++;
            } else if (mAdjustCount > shouldAdjustCount) {
                adjustIconPosition(Direction.Left);
                mAdjustCount--;
            }
            for (i = mHoriChildren[0].length - 1; i >= 0; i--) {
                rawScale1 = ((LayoutParams) mHoriChildren[0][i]
                        .getLayoutParams()).scale;
                if (i == 0) {
                    offset = moveX;
                    // offset = getOffset(mHoriChildren[0][i], null,
                    // Direction.Right);
                    adjustMoveX = offset / minuOffset * (moveX - mAdjustCount * minuOffset);
                    targetScale = rawScale1 - adjustMoveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[0][i - 1]
                            .getLayoutParams()).scale;
                    offset = getOffset(mHoriChildren[0][i], mHoriChildren[0][i
                            - 1],
                            Direction.Right);
                    adjustMoveX = offset / minuOffset * (moveX - mAdjustCount * minuOffset);
                    targetScale = rawScale1 + adjustMoveX / offset * (rawScale2 - rawScale1);
                    moveY = computeTranslateY(mHoriChildren[0][i],
                            mHoriChildren[0][i - 1],
                            adjustMoveX);
                }
                mHoriChildren[0][i].setScaleX(targetScale);
                mHoriChildren[0][i].setScaleY(targetScale);
                mHoriChildren[0][i].setTranslationX(adjustMoveX);
                mHoriChildren[0][i].setTranslationY(moveY);
            }
            for (i = mHoriChildren[1].length - 1; i >= 0; i--) {
                rawScale1 = ((LayoutParams) mHoriChildren[1][i]
                        .getLayoutParams()).scale;
                if (i == 0) {
                    offset = moveX;
                    // offset = getOffset(mHoriChildren[1][i], null,
                    // Direction.Right);
                    adjustMoveX = offset / minuOffset * (moveX - mAdjustCount * minuOffset);
                    targetScale = rawScale1 - adjustMoveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[1][i - 1]
                            .getLayoutParams()).scale;
                    offset = getOffset(mHoriChildren[1][i], mHoriChildren[1][i
                            - 1],
                            Direction.Right);
                    adjustMoveX = offset / minuOffset * (moveX - mAdjustCount * minuOffset);
                    targetScale = rawScale1 + adjustMoveX / offset * (rawScale2 - rawScale1);
                    moveY = computeTranslateY(mHoriChildren[1][i],
                            mHoriChildren[1][i - 1],
                            adjustMoveX - mAdjustCount * minuOffset);
                }
                mHoriChildren[1][i].setScaleX(targetScale);
                mHoriChildren[1][i].setScaleY(targetScale);
                mHoriChildren[1][i].setTranslationX(adjustMoveX);
                mHoriChildren[1][i].setTranslationY(moveY);
            }
            for (i = mHoriChildren[2].length - 1; i >= 0; i--) {
                rawScale1 = ((LayoutParams) mHoriChildren[2][i]
                        .getLayoutParams()).scale;
                if (i == 0) {
                    offset = moveX;
                    // offset = getOffset(mHoriChildren[2][i], null,
                    // Direction.Right);
                    adjustMoveX = offset / minuOffset * (moveX - mAdjustCount * minuOffset);
                    targetScale = rawScale1 - adjustMoveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[2][i - 1]
                            .getLayoutParams()).scale;
                    offset = getOffset(mHoriChildren[2][i], mHoriChildren[2][i
                            - 1],
                            Direction.Right);
                    adjustMoveX = offset / minuOffset * (moveX - mAdjustCount * minuOffset);
                    targetScale = rawScale1 + adjustMoveX
                            / offset * (rawScale2 - rawScale1);
                    moveY = computeTranslateY(mHoriChildren[2][i],
                            mHoriChildren[2][i - 1], adjustMoveX);
                }
                mHoriChildren[2][i].setScaleX(targetScale);
                mHoriChildren[2][i].setScaleY(targetScale);
                mHoriChildren[2][i].setTranslationX(adjustMoveX);
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

    private void printChildren() {
        GestureItemView targetView;
        LayoutParams targetLp;
        String resault = "";
        for (int i = 0; i < 12; i++) {
            targetView = mHoriChildren[0][i];
            targetLp = (LayoutParams) targetView.getLayoutParams();
            resault += targetView.getItemName().toString() + targetLp.position + "    "
                    + targetView.getTranslationX() + "; ";
        }
        LeoLog.d("children", resault);
        resault = "";
        for (int i = 0; i < 15; i++) {
            targetView = mHoriChildren[1][i];
            targetLp = (LayoutParams) targetView.getLayoutParams();
            resault += targetView.getItemName().toString() + targetLp.position + "    "
                    + targetView.getTranslationX() + "; ";
        }
        LeoLog.d("children", resault);
        resault = "";
        for (int i = 0; i < 12; i++) {
            targetView = mHoriChildren[2][i];
            targetLp = (LayoutParams) targetView.getLayoutParams();
            resault += targetView.getItemName().toString() + targetLp.position + "    "
                    + targetView.getTranslationX() + "; ";
        }
        LeoLog.d("children", resault);
    }

    public void snapLong(Direction direction) {
        float distance;
        int duration;
        if (!mSnapping) {
            if (direction == Direction.Left) {
                distance = mMinuOffset * 3 - mLastMovex;
                ValueAnimator transAnima = ValueAnimator.ofFloat(mLastMovex, mMinuOffset * 3);
                duration = (int) ((distance / mMinuOffset * 3) * mSnapDuration);
                transAnima.setInterpolator(new DecelerateInterpolator());
                transAnima.addUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mLastMovex = (Float) animation.getAnimatedValue();
                        computeTranslateScale(Direction.Left, mLastMovex);
                    }
                });
                transAnima.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mSnapping = false;
                        mAdjustCount = 0;
                        mLastMovex = 0;
                    }
                });
                transAnima.start();
                mSnapping = true;
            } else if (direction == Direction.Right) {
                distance = mMinuOffset * 3 - mLastMovex;
                ValueAnimator transAnima = ValueAnimator.ofFloat(mLastMovex, mMinuOffset * 3);
                duration = (int) ((distance / mMinuOffset * 3) * mSnapDuration);
                transAnima.setInterpolator(new DecelerateInterpolator());
                transAnima.addUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mLastMovex = (Float) animation.getAnimatedValue();
                        computeTranslateScale(Direction.Right, mLastMovex);
                    }
                });
                transAnima.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mSnapping = false;
                        mAdjustCount = 0;
                        mLastMovex = 0;
                    }
                });
                transAnima.start();
                mSnapping = true;
            }
        }
    }

    private void snapShort(Direction direction) {
        float distance, offset;
        int duration;
        int temp;
        if (!mSnapping) {
            if (direction == Direction.Left) {
                temp = (int) (mLastMovex / mMinuOffset);
                offset = mLastMovex - temp * mMinuOffset;
                ValueAnimator transAnima;
                if (offset > mMinuOffset / 2) {
                    distance = (temp + 1) * mMinuOffset;
                } else {
                    distance = temp * mMinuOffset;
                }
                transAnima = ValueAnimator.ofFloat(mLastMovex, distance);
                duration = (int) (((distance - mLastMovex) / mMinuOffset * 3) * mSnapDuration);
                transAnima.setInterpolator(new DecelerateInterpolator());
                transAnima.addUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mLastMovex = (Float) animation.getAnimatedValue();
                        computeTranslateScale(Direction.Left, mLastMovex);
                    }
                });
                transAnima.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mSnapping = false;
                        mAdjustCount = 0;
                        mLastMovex = 0;
                    }
                });
                transAnima.start();
                mSnapping = true;
            } else if (direction == Direction.Right) {
                temp = (int) (mLastMovex / mMinuOffset);
                offset = mLastMovex - temp * mMinuOffset;
                ValueAnimator transAnima;
                if (offset < mMinuOffset / 2) {
                    distance = (temp + 1) * mMinuOffset;
                } else {
                    distance = temp * mMinuOffset;
                }
                transAnima = ValueAnimator.ofFloat(mLastMovex, distance);
                duration = (int) (((distance - mLastMovex) / mMinuOffset * 3) * mSnapDuration);
                transAnima.addUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mLastMovex = (Float) animation.getAnimatedValue();
                        computeTranslateScale(Direction.Right, mLastMovex);
                    }
                });
                transAnima.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mSnapping = false;
                        mAdjustCount = 0;
                        mLastMovex = 0;
                    }
                });
                transAnima.start();
                mSnapping = true;
            }
        }
    }

    public void onTouchUp() {
        if (mLastMovex > 0) { // Left
            snapShort(Direction.Left);
        } else if (mLastMovex < 0) { // Right
            snapShort(Direction.Right);
        }
    }

    public GestureItemView makeGestureItem() {
        LayoutInflater inflate = LayoutInflater.from(getContext());
        GestureItemView item = (GestureItemView) inflate.inflate(R.layout.gesture_item, null);
        return item;
    }
}
