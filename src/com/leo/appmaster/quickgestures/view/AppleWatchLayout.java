
package com.leo.appmaster.quickgestures.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.R.animator;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.MessageBean;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.QuickGestureManager.AppLauncherRecorder;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.GestureEmptyItemInfo;
import com.leo.appmaster.quickgestures.model.QuickGestureContactTipInfo;
import com.leo.appmaster.quickgestures.model.QuickGsturebAppInfo;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;

public class AppleWatchLayout extends ViewGroup {

    public static final String TAG = "AppleWatchLayout";
    public static final int NORMALINFO = 0;
    public static final int APPITEMINFO = 1;

    private AppleWatchContainer mContainer;
    private AnimatorSet mReorderAnimator;
    private AppMasterPreference mPref;
    private boolean mRecodering;
    private boolean mAnimCanceled;
    private int mTotalWidth, mTotalHeight;
    private int mCenterPointX, mCenterPointY;
    private int mItemSize, mIconSize;
    private int mInnerRadius, mOuterRadius;
    private float mInnerScale, mOuterScale;
    private float mCurrentRotateDegree;
    private Context mContext;
    private GestureItemView[][] mHoriChildren = new GestureItemView[3][];
    private float mLastMovex;
    private float mLastAdjustX;
    private float mMinuOffset = 0f;
    private int mAdjustCount = 0;
    private boolean mSnapping;
    public GType mMyType;
    public boolean mHasFillExtraItems;
    private boolean isSqueez = false;

    public static enum Direction {
        Right, Left, None;
    }

    private boolean mNeedRelayoutExtraItem;

    private List<BaseInfo> mRecordMostList;

    public AppleWatchLayout(Context context) {
        this(context, null);
    }

    public AppleWatchLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppleWatchLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mPref = AppMasterPreference.getInstance(context);
    }

    public boolean isSnapping() {
        return mSnapping;
    }

    public void fillItems(List<BaseInfo> infos, boolean loadExtra) {
        BaseInfo info = null;
        if (infos.size() > 11) {
            infos = infos.subList(0, 11);
        } else if (infos.size() < 11) {
            int[] hit = new int[11];
            for (int i = 0; i < infos.size(); i++) {
                info = (BaseInfo) infos.get(i);
                if (info.gesturePosition < 0 || info.gesturePosition > 11) {
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
                if (info.gesturePosition < 0 || info.gesturePosition > 11) {
                    info.gesturePosition = i;
                }
            }
        }

        removeAllViews();
        GestureItemView gestureItem = null;
        AppleWatchLayout.LayoutParams lp = null;
        if (null == mContainer) {
            mContainer = (AppleWatchContainer) getParent();
        }
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

            // TODO
            if (info instanceof QuickGestureContactTipInfo) {
                if (((QuickGestureContactTipInfo) info).isShowReadTip) {
                    gestureItem.showReadTip();
                } else {
                    gestureItem.cancelShowReadTip();
                }
                gestureItem.setDecorateAction(new EventAction(getContext(), 0));
            } else if (info instanceof MessageBean) {
                if (((MessageBean) info).isShowReadTip) {
                    gestureItem.showReadTip();
                } else {
                    gestureItem.cancelShowReadTip();
                }
                gestureItem.setDecorateAction(new EventAction(getContext(), 0));
            } else if (info instanceof ContactCallLog) {
                if (((ContactCallLog) info).isShowReadTip) {
                    gestureItem.showReadTip();
                } else {
                    gestureItem.cancelShowReadTip();
                }
                gestureItem.setDecorateAction(new EventAction(getContext(), 0));
            }

            if (info instanceof GestureEmptyItemInfo) {
                info.icon = QuickGestureManager.getInstance(getContext()).applyEmptyIcon();
            }
            if (info instanceof GestureEmptyItemInfo || info instanceof QuickSwitcherInfo) {
                gestureItem.setItemIcon(info.icon, false);
            } else {
                gestureItem.setItemIcon(info.icon, true);
            }
            if (info.eventNumber > 0) {
                gestureItem.setDecorateAction(new EventAction(getContext(), info.eventNumber));
            }

            if (isCurrentLayout()) {
                gestureItem.setVisibility(View.INVISIBLE);
            }
            gestureItem.setTag(info);
            if (isCurrentLayout()) {
                gestureItem.setVisibility(View.INVISIBLE);
            }
            addView(gestureItem);
            computeCenterItem(gestureItem, lp.position, false);
        }
        requestLayout();

        if (loadExtra) {
            // postDelayed(new Runnable() {
            // @Override
            // public void run() {
            fillExtraChildren();
            // }
            // }, 800);
        }

    }

    public boolean isCurrentLayout() {
        return mMyType == mContainer.getCurrentGestureType();
    }

    public void setType(GType type) {
        mMyType = type;
    }

    public void fillExtraChildren() {
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

        for (int i = 0; i < 6; i++) {
            if (i < 3) {
                tempView = mHoriChildren[1][i + 3];
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
                mHoriChildren[1][i + 3] = addView;
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

        mHasFillExtraItems = true;
    }

    private void init() {
        Resources res = getContext().getResources();
        mItemSize = res.getDimensionPixelSize(R.dimen.apple_watch_item_size);
        mIconSize = res.getDimensionPixelSize(R.dimen.apple_watch_item_icon_size);
        mInnerRadius = res.getDimensionPixelSize(R.dimen.apple_watch_layout_inner_radius);
        mOuterRadius = res.getDimensionPixelSize(R.dimen.apple_watch_layout_outer_radius);
        mInnerScale = 0.69f;
        mOuterScale = 0.49f;
        mHoriChildren[0] = new GestureItemView[12];
        mHoriChildren[1] = new GestureItemView[9];
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
        LeoLog.e(TAG, "onLayout");
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
            halfItemSize = child.getMeasuredWidth() / 2;
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
                    outerItemAngle = 330;
                } else if (params.position == 8) {
                    outerItemAngle = 30;
                } else if (params.position == 9) {
                    outerItemAngle = 150;
                } else if (params.position == 10) {
                    outerItemAngle = 210;
                }

                left = (int) (mCenterPointX - mOuterRadius
                        * Math.cos(Math.toRadians(outerItemAngle)) - halfItemSize);

                top = (int) (mCenterPointY
                        - mOuterRadius
                        * Math.sin(Math.toRadians(outerItemAngle)) - halfItemSize);
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
        // if (mContainer == null) {
        setPivotX(mTotalWidth / 2);
        setPivotY(mTotalHeight * 3);
        // mContainer = (AppleWatchContainer) getParent();
        // }
    }

    private void inflateItem(GestureItemView item, BaseInfo info) {
        if (item == null || info == null)
            return;
        item.setGravity(Gravity.CENTER_HORIZONTAL);
        item.setItemName(info.label);
        if (info instanceof GestureEmptyItemInfo || info instanceof QuickSwitcherInfo) {
            item.setItemIcon(info.icon, false);
        } else {
            item.setItemIcon(info.icon, true);
        }
        if (info.eventNumber > 0) {
            item.setDecorateAction(new EventAction(getContext(), info.eventNumber));
        }
        if (info instanceof QuickGestureContactTipInfo) {
            if (((QuickGestureContactTipInfo) info).isShowReadTip) {
                item.showReadTip();
            } else {
                item.cancelShowReadTip();
            }
            item.setDecorateAction(new EventAction(getContext(), 0));
        } else if (info instanceof MessageBean) {
            if (((MessageBean) info).isShowReadTip) {
                item.showReadTip();
            } else {
                item.cancelShowReadTip();
            }
            item.setDecorateAction(new EventAction(getContext(), 0));
        } else if (info instanceof ContactCallLog) {
            if (((ContactCallLog) info).isShowReadTip) {
                item.showReadTip();
            } else {
                item.cancelShowReadTip();
            }
            item.setDecorateAction(new EventAction(getContext(), 0));
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
    public void addView(View child, ViewGroup.LayoutParams params) {
        int addPosition = ((LayoutParams) params).position;
        LayoutParams lp = ((LayoutParams) params);
        for (int i = 0; i < getChildCount(); i++) {
            lp = (LayoutParams) getChildAt(i).getLayoutParams();
            if (lp.position >= addPosition) {
                lp.position++;
            }
        }
        saveReorderPosition();
        computeCenterItem((GestureItemView) child, addPosition, true);
        super.addView(child, params);
    }

    public float getItemScale(int position) {
        if (position < 0) {
            return 0.0f;
        } else if (position == 0) {
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
        LeoLog.d("TestLayout", "info.label" + info.label);
        if (info instanceof BusinessItemInfo) {// 运营app
            LeoLog.d("TestLayout", "BusinessItemInfo");
            BusinessItemInfo bif = (BusinessItemInfo) info;
            if (bif.gpPriority == 1) {
                if (AppUtil.appInstalled(getContext(),
                        Constants.GP_PACKAGE)) {
                    try {
                        AppUtil.downloadFromGp(getContext(),
                                bif.packageName);
                    } catch (Exception e) {
                        AppUtil.downloadFromBrowser(getContext(),
                                bif.appDownloadUrl);
                    }
                } else {
                    AppUtil.downloadFromBrowser(getContext(),
                            bif.appDownloadUrl);
                }
            } else {
                AppUtil.downloadFromBrowser(getContext(),
                        bif.appDownloadUrl);
            }
            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "dynamic_cli");
        } else if (info instanceof QuickSwitcherInfo) {// 快捷开关
            LeoLog.d("TestLayout", "QuickSwitcherInfo");
            QuickSwitcherInfo sInfo = (QuickSwitcherInfo) info;
            if (sInfo.swtichIdentiName.equals(QuickSwitchManager.BLUETOOTH)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext())
                        .toggleBluetooth(sInfo);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.FLASHLIGHT)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext())
                        .toggleFlashLight(sInfo);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.WLAN)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).toggleWlan(sInfo);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.CRAME)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).openCrame();
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.SOUND)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).toggleSound(sInfo);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.LIGHT)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).toggleLight(sInfo, item);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.SPEEDUP)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).speedUp(sInfo);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.CHANGEMODE)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).toggleMode();
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.SWITCHSET)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).switchSet();
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.SETTING)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).goSetting();
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.GPS)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).toggleGPS();
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.FLYMODE)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).toggleFlyMode();
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.ROTATION)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).toggleRotation(sInfo);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.MOBILEDATA)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).toggleMobileData(sInfo);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.HOME)) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_switch", "cli_"
                        + sInfo.swtichIdentiName);
                QuickSwitchManager.getInstance(getContext()).goHome();
            }
            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "switch_cli");
        } else if (info instanceof AppItemInfo) {
            LeoLog.d("TestLayout", "AppItemInfo");
            AppItemInfo appInfo = (AppItemInfo) info;
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName(appInfo.packageName,
                    appInfo.activityName));
            getContext().startActivity(intent);

            if (mMyType == GType.DymicLayout) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "dynamic_cli");
            } else if (mMyType == GType.MostUsedLayout) {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "common_cli");
            } else {
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "switch_cli");
            }
        } else if (info instanceof MessageBean) {
            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "dynamic_cli");
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
            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "dynamic_cli");
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
            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "dynamic_cli");
        } else if (info instanceof QuickGestureContactTipInfo) {
            // 隐私联系人提示
            item.cancelShowReadTip();
            Intent intent = new Intent();
            intent.setClass(mContext, PrivacyContactActivity.class);
            try {
                mContext.startActivity(intent);
                if (QuickGestureManager.getInstance(mContext).isShowPrivacyCallLog) {
                    QuickGestureManager.getInstance(mContext).isShowPrivacyCallLog = false;
                }
                if (QuickGestureManager.getInstance(mContext).isShowPrivacyMsm) {
                    QuickGestureManager.getInstance(mContext).isShowPrivacyMsm = false;
                }
            } catch (Exception e) {
            }
            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "dynamic_cli");
        } else if (info instanceof QuickGsturebAppInfo) {
            LeoLog.d("TestLayout", "QuickGsturebAppInfo");
            QuickGsturebAppInfo appInfo = (QuickGsturebAppInfo) info;
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName(appInfo.packageName,
                    appInfo.activityName));
            getContext().startActivity(intent);
            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "common_cli");
        }
    }

    public void checkItemClick(float x, float y) {
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
                        if (type == GType.MostUsedLayout) {
                            SDKWrapper
                                    .addEvent(getContext(), SDKWrapper.P1, "qs_tab", "common_add");
                        } else {
                            SDKWrapper
                                    .addEvent(getContext(), SDKWrapper.P1, "qs_tab", "switch_add");
                        }
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
        QuickGestureManager qgm = QuickGestureManager.getInstance(getContext());
        if (type == GType.MostUsedLayout) {
            qgm.showCommonAppDialog(getContext());
            ((Activity) getContext()).finish();
            mPref.setLastTimeLayout(2);
        } else if (type == GType.SwitcherLayout) {
            // get list from sp
            qgm.showQuickSwitchDialog(getContext());
            ((Activity) getContext()).finish();
            mPref.setLastTimeLayout(3);
        }
    }

    private void replaceEmptyIcon(GestureItemView hitView) {
        GType type = mContainer.getCurrentGestureType();
        BaseInfo baseInfo = (BaseInfo) hitView.getTag();
        QuickGestureManager qgm = QuickGestureManager.getInstance(getContext());
        if (type == GType.DymicLayout) {
            QuickGestureManager.getInstance(getContext()).checkEventItemRemoved(
                    (BaseInfo) hitView.getTag());

            if (baseInfo instanceof BusinessItemInfo) {
                qgm.deleteBusinessItem((BusinessItemInfo) baseInfo);
            }
            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "dynamic_delete");
        } else if (type == GType.SwitcherLayout) {
            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "switch_delete");
        } else if (type == GType.MostUsedLayout) {
            // TODO
            if (!(hitView.getTag() instanceof GestureEmptyItemInfo)) {
                AppInfo info = (AppInfo) hitView.getTag();
                boolean isRecorderFlag = AppMasterPreference.getInstance(mContext)
                        .getQuickGestureCommonAppDialogCheckboxValue();
                if (isRecorderFlag) {

                    List<BaseInfo> mComList = qgm.loadCommonAppInfo();
                    for (int i = 0; i < mComList.size(); i++) {
                        AppInfo mInfo = (AppInfo) mComList.get(i);
                        if (mInfo.packageName.equals(info.packageName)) {
                            AppMasterPreference.getInstance(mContext)
                                    .setCommonAppPackageNameRemove(
                                            info.packageName + ":" + info.gesturePosition);
                            break;
                        }
                    }

                    ArrayList<AppLauncherRecorder> mRecorderApp = LockManager.getInstatnce().mAppLaunchRecorders;
                    Iterator<AppLauncherRecorder> recorder = mRecorderApp.iterator();
                    while (recorder.hasNext()) {
                        AppLauncherRecorder recorderAppInfo = recorder.next();
                        if (info != null) {
                            if (info.packageName.equals(recorderAppInfo.pkg)) {
                                recorderAppInfo.launchCount = 0;
                                LockManager.getInstatnce().saveAppLaunchRecoder();
                            }
                        }
                    }
                } else {
                    AppMasterPreference.getInstance(mContext).setCommonAppPackageNameRemove(
                            info.packageName + ":" + info.gesturePosition);
                }
            }

            SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "qs_tab", "common_delete");
        }

        GestureEmptyItemInfo info = new GestureEmptyItemInfo();
        info.gesturePosition = baseInfo.gesturePosition;
        info.icon = qgm.applyEmptyIcon();
        hitView.setItemIcon(info.icon, false);
        hitView.setItemName(info.label);
        hitView.setDecorateAction(null);
        hitView.setTag(info);
        hitView.enterEditMode();
        saveReorderPosition();
        refillExtraItems();
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

    public boolean checkActionDownInEditing(float x, float y) {
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
                if (!rect.contains(offsetX, onnsetY) && !giv.isEmptyIcon()) {
                    hitView.startDrag(null, new GestureDragShadowBuilder(hitView, 2.0f),
                            hitView, 0);
                    hitView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    return true;
                }
            }
        }
        return false;
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
        } else if (type == GType.MostUsedLayout) {
            // boolean isCheck = AppMasterPreference.getInstance(mContext)
            // .getQuickGestureCommonAppDialogCheckboxValue();
            // int mNum = getChildCount();
            // LeoLog.d("testSp", "AllChildNum : " + mNum);
            // mRecordMostList = new ArrayList<BaseInfo>();
            //
            // if (isCheck) {
            // mRecordMostList =
            // QuickGestureManager.getInstance(mContext).loadRecorderAppInfo();
            // for (int i = 0; i < mRecordMostList.size(); i++) {
            // // AppInfo sInfo = (AppInfo) getChildAt(i).getTag();
            // AppInfo sInfo = (AppInfo) mRecordMostList.get(i);
            // LeoLog.d("testSp", "sInfo.childName : " + sInfo.packageName
            // + " - sInfo.childPosition : "
            // + sInfo.gesturePosition);
            // }
            // isSqueez = true;
            // }

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
                        computeCenterItem(hitViews[i], lastPosition, false);
                    } else {
                        nextPosition = ((AppleWatchLayout.LayoutParams) hitViews[i - 1]
                                .getLayoutParams()).position;
                        ((AppleWatchLayout.LayoutParams) hitViews[i].getLayoutParams()).position = nextPosition;
                        computeCenterItem(hitViews[i], nextPosition, false);
                    }
                }
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

                saveReorderPosition();
                refillExtraItems();
            }

        });
        mReorderAnimator.start();
    }

    private void refillExtraItems() {
        GestureItemView tempView;
        for (int i = 0; i < 8; i++) {
            if (i < 4) {
                tempView = mHoriChildren[0][i];
            } else {
                tempView = mHoriChildren[0][i + 4];
            }
            removeView(tempView);
        }
        for (int i = 0; i < 6; i++) {
            if (i < 3) {
                tempView = mHoriChildren[1][i];
            } else {
                tempView = mHoriChildren[1][i + 3];
            }
            removeView(tempView);
        }
        for (int i = 0; i < 8; i++) {
            if (i < 4) {
                tempView = mHoriChildren[2][i];
            } else {
                tempView = mHoriChildren[2][i + 4];
            }
            removeView(tempView);
        }
        fillExtraChildren();
    }

    private void saveReorderPosition() {
        if (mContainer != null) {
            GType gType = mContainer.getCurrentGestureType();
            if (gType == GType.DymicLayout) {

            } else if (gType == GType.MostUsedLayout) {
                // boolean isCheck = AppMasterPreference.getInstance(mContext)
                // .getQuickGestureCommonAppDialogCheckboxValue();
                int mNum = getChildCount();
                LeoLog.d("testSp", "ChildNum : " + mNum);
                LayoutParams params = null;
                List<BaseInfo> mostUseApp = new ArrayList<BaseInfo>();

                // if (isCheck) {
                // for (int i = 0; i < mNum; i++) {
                // LayoutParams mParams = null;
                // mParams = (LayoutParams) getChildAt(i).getLayoutParams();
                // int position = mParams.position;
                // if (position > -1) {
                // if (getChildAt(i).getTag() instanceof AppInfo) {
                // AppInfo sInfo = (AppInfo) getChildAt(i).getTag();
                // LeoLog.d("testSp", "childName : " + sInfo.packageName
                // + " - childPosition : "
                // + position);
                // if (sInfo != null && !sInfo.label.isEmpty()) {
                // sInfo.gesturePosition = position;
                // mostUseApp.add(sInfo);
                // }
                // }
                // }
                // }
                //
                // HashMap<String, Integer> packagePosition = new
                // HashMap<String, Integer>();
                // ArrayList<AppLauncherRecorder> records =
                // LockManager.getInstatnce().mAppLaunchRecorders;
                // Iterator<AppLauncherRecorder> iterator =
                // records.iterator();
                // AppLauncherRecorder record;
                // int i = 0;
                // while (iterator.hasNext()) {
                // if (i > 13)
                // break;
                // record = iterator.next();
                // packagePosition.put(record.pkg, record.launchCount);
                // i++;
                // }
                //
                // if(isSqueez){
                // // 计算原位的position:launchercount
                // if (mRecordMostList.size() > 0) {
                // for (int j = 0; j < mRecordMostList.size(); j++) {
                // AppInfo sInfo = (AppInfo) mRecordMostList.get(j);
                // int position = sInfo.gesturePosition;
                // int launcherCount =
                // packagePosition.get(sInfo.packageName);
                // LeoLog.d("testSp", "position : " + position
                // + " - launcherCount : "
                // + launcherCount);
                // }
                //
                // mRecordMostList.clear();
                // mRecordMostList = null;
                // }
                // isSqueez = false;
                // }
                // for (int i = 0; i < mNum; i++) {
                // params = (LayoutParams) getChildAt(i).getLayoutParams();
                // int position = params.position;
                // if (position > -1) {
                // LeoLog.d("testSp", "child[" + i + "] position is : " +
                // position);
                // if (getChildAt(i).getTag() instanceof AppInfo) {
                // AppInfo sInfo = (AppInfo) getChildAt(i).getTag();
                // if (sInfo != null && !sInfo.label.isEmpty()) {
                // sInfo.gesturePosition = position;
                // mostUseApp.add(sInfo);
                // }
                // }
                // }
                // }
                // String NeedSave =
                // QuickSwitchManager.getInstance(getContext())
                // .listToPackString(mostUseApp, mostUseApp.size(), NORMALINFO);
                // LeoLog.d("testSp", "NeedSave : " + NeedSave);
                // mPref.setCommonAppPackageName(NeedSave);

                // } else {
                for (int i = 0; i < mNum; i++) {
                    params = (LayoutParams) getChildAt(i).getLayoutParams();
                    int position = params.position;
                    if (position > -1) {
                        LeoLog.d("testSp", "child[" + i + "] position is : " + position);
                        if (getChildAt(i).getTag() instanceof AppInfo) {
                            AppInfo sInfo = (AppInfo) getChildAt(i).getTag();
                            if (sInfo != null && !sInfo.label.isEmpty()) {
                                sInfo.gesturePosition = position;
                                mostUseApp.add(sInfo);
                            }
                        }
                    }
                }
                String NeedSave = QuickSwitchManager.getInstance(getContext())
                        .listToPackString(mostUseApp, mostUseApp.size(), NORMALINFO);
                LeoLog.d("testSp", "NeedSave : " + NeedSave);
                mPref.setCommonAppPackageName(NeedSave);
                // }
            } else if (gType == GType.SwitcherLayout) {
                int mNum = getChildCount();
                LayoutParams params = null;
                List<BaseInfo> mSwitchList = new ArrayList<BaseInfo>();
                for (int i = 0; i < mNum; i++) {
                    params = (LayoutParams) getChildAt(i).getLayoutParams();
                    int position = params.position;
                    if (position > -1) {
                        BaseInfo sInfo = (BaseInfo) getChildAt(i).getTag();
                        if (sInfo != null && !sInfo.label.isEmpty()) {
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
            if (childCount < 11 && !view.isEmptyIcon()) {
                showAddIcon(childCount);
            }
        }
    }

    private void showAddIcon(int position) {
        if (position >= 0 && position <= 10) {
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

    private void computeCenterItem(GestureItemView view, int position, boolean newAdd) {
        if (position == 8) {
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
        } else if (position == 9) {
            mHoriChildren[0][7] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[0][3], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[0][11], (BaseInfo) view.getTag());
            }
        } else if (position == 1) {
            mHoriChildren[1][3] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[1][0], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[1][10], (BaseInfo) view.getTag());
            }
        } else if (position == 0) {
            mHoriChildren[1][4] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[1][1], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[1][11], (BaseInfo) view.getTag());
            }
        } else if (position == 4) {
            mHoriChildren[1][5] = view;
            if (newAdd) {
                inflateItem(mHoriChildren[1][2], (BaseInfo) view.getTag());
                inflateItem(mHoriChildren[1][12], (BaseInfo) view.getTag());
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
        } else if (position == 10) {
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
        LeoLog.e("xxxx", "adjustIconPosition   direction = " + direction);
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

    public void relayoutExtraChildren() {
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

        for (int i = 0; i < 6; i++) {
            if (i < 3) {
                targetView = mHoriChildren[1][i];
                tempView = mHoriChildren[1][i + 3];
                targetLp = (LayoutParams) targetView.getLayoutParams();
                tempLp = (LayoutParams) tempView.getLayoutParams();
                targetView.layout(tempView.getLeft() - mTotalWidth, tempView.getTop(),
                        tempView.getRight() - mTotalWidth, tempView.getBottom());
                targetLp.scale = tempLp.scale;
                targetView.setScaleX(targetLp.scale);
                targetView.setScaleY(targetLp.scale);
            } else {
                tempView = mHoriChildren[1][i];
                targetView = mHoriChildren[1][i + 3];
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
        float adjustMoveX;
        float offset, moveY;
        float rawScale1, rawScale2, targetScale;
        mLastMovex = moveX;

        if (mMinuOffset == 0) {
            if (moveX >= 0) {
                mMinuOffset = mHoriChildren[0][5].getLeft()
                        - mHoriChildren[0][4].getLeft();
            } else {
                mMinuOffset = mHoriChildren[0][4].getRight()
                        - mHoriChildren[0][5].getRight();
            }
            mLastAdjustX = 0;
        }

        if ((moveX - mLastAdjustX) >= 0) {
            direction = Direction.Left;
            mMinuOffset = mHoriChildren[0][5].getLeft()
                    - mHoriChildren[0][4].getLeft();
            if ((moveX - mLastAdjustX) >= Math.abs(mMinuOffset)) {
                adjustIconPosition(Direction.Left);
                mAdjustCount++;
                mLastAdjustX += Math.abs(mMinuOffset);
            }
        } else {
            direction = Direction.Right;
            mMinuOffset = mHoriChildren[0][4].getRight()
                    - mHoriChildren[0][5].getRight();
            if ((moveX - mLastAdjustX) <= -Math.abs(mMinuOffset)) {
                adjustIconPosition(Direction.Right);
                mAdjustCount--;
                mLastAdjustX -= Math.abs(mMinuOffset);
            }
        }

        if (direction == Direction.Left) {
            for (i = 0; i < mHoriChildren[0].length - 1; i++) {
                rawScale1 = ((LayoutParams) mHoriChildren[0][i]
                        .getLayoutParams()).scale;
                if (i == mHoriChildren[0].length - 1) {
                    offset = moveX;
                    adjustMoveX = offset / mMinuOffset
                            * (moveX - mLastAdjustX);
                    targetScale = rawScale1 - adjustMoveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[0][i + 1]
                            .getLayoutParams()).scale;
                    offset = getOffset(mHoriChildren[0][i], mHoriChildren[0][i + 1],
                            Direction.Left);
                    adjustMoveX = offset / mMinuOffset
                            * (moveX - mLastAdjustX);

                    if (i == 3 || i == 7) {
                        if (adjustMoveX <= offset / 2) {
                            targetScale = (offset / 2 - adjustMoveX) /
                                    (offset / 2) * rawScale1;
                            // targetScale = rawScale1 - adjustMoveX *
                            // (rawScale1 / offset);
                        } else {
                            targetScale = (adjustMoveX - offset / 2) /
                                    (offset / 2) * rawScale1;
                            // targetScale = rawScale1 - (offset - adjustMoveX)
                            // * (rawScale1 / offset);

                        }
                    } else {
                        targetScale = rawScale1 + adjustMoveX / offset * (rawScale2 - rawScale1);
                    }

                    moveY = computetranslationY(mHoriChildren[0][i],
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
                    adjustMoveX = offset / mMinuOffset
                            * (moveX - mLastAdjustX);
                    targetScale = rawScale1 - adjustMoveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[1][i + 1]
                            .getLayoutParams()).scale;
                    offset = getOffset(mHoriChildren[1][i], mHoriChildren[1][i
                            + 1],
                            Direction.Left);
                    adjustMoveX = offset / mMinuOffset
                            * (moveX - mLastAdjustX);

                    if (i == 2 || i == 5) {
                        if (adjustMoveX <= offset / 2) {
                            targetScale = (offset / 2 - adjustMoveX) /
                                    (offset / 2) * rawScale1;
                            // targetScale = rawScale1 - adjustMoveX *
                            // (rawScale1 / offset);
                        } else {
                            targetScale = (adjustMoveX - offset / 2) /
                                    (offset / 2) * rawScale1;
                            // targetScale = rawScale1 - (offset - adjustMoveX)
                            // * (rawScale1 / offset);
                        }
                    } else {
                        targetScale = rawScale1 + adjustMoveX / offset * (rawScale2 - rawScale1);
                    }

                    moveY = computetranslationY(mHoriChildren[1][i],
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
                    adjustMoveX = offset / mMinuOffset
                            * (moveX - mLastAdjustX);
                    targetScale = rawScale1 - adjustMoveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[2][i + 1].getLayoutParams()).scale;
                    offset = getOffset(mHoriChildren[2][i], mHoriChildren[2][i + 1],
                            Direction.Left);
                    adjustMoveX = offset / mMinuOffset
                            * (moveX - mLastAdjustX);
                    if (i == 3 || i == 7) {
                        if (adjustMoveX <= offset / 2) {
                            targetScale = (offset / 2 - adjustMoveX) / (offset / 2) * rawScale1;
                        } else {
                            targetScale = (adjustMoveX - offset / 2) / (offset / 2) * rawScale1;
                        }
                    } else {
                        targetScale = rawScale1 + adjustMoveX / offset * (rawScale2 - rawScale1);
                    }
                    moveY = computetranslationY(mHoriChildren[2][i], mHoriChildren[2][i + 1],
                            adjustMoveX);
                }
                mHoriChildren[2][i].setScaleX(targetScale);
                mHoriChildren[2][i].setScaleY(targetScale);
                mHoriChildren[2][i].setTranslationX(adjustMoveX);
                mHoriChildren[2][i].setTranslationY(moveY);
            }
        } else if (direction == Direction.Right) {
            for (i = mHoriChildren[0].length - 1; i >= 0; i--) {
                rawScale1 = ((LayoutParams) mHoriChildren[0][i]
                        .getLayoutParams()).scale;
                if (i == 0) {
                    offset = moveX;
                    adjustMoveX = offset / mMinuOffset
                            * (moveX - mLastAdjustX);
                    targetScale = rawScale1 - adjustMoveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[0][i - 1]
                            .getLayoutParams()).scale;
                    offset = getOffset(mHoriChildren[0][i], mHoriChildren[0][i
                            - 1],
                            Direction.Right);
                    adjustMoveX = offset / mMinuOffset
                            * (moveX - mLastAdjustX);

                    if (i == 4 || i == 8) {
                        if (adjustMoveX <= offset / 2) {
                            targetScale = -(offset / 2 - adjustMoveX) / (offset / 2) * rawScale1;
                        } else {
                            targetScale = -(adjustMoveX - offset / 2) / (offset / 2) * rawScale1;
                        }
                    } else {
                        targetScale = rawScale1 + adjustMoveX / offset * (rawScale2 - rawScale1);
                    }
                    moveY = computetranslationY(mHoriChildren[0][i],
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
                    adjustMoveX = offset / mMinuOffset
                            * (moveX - mLastAdjustX);
                    targetScale = rawScale1 - adjustMoveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[1][i - 1]
                            .getLayoutParams()).scale;
                    offset = getOffset(mHoriChildren[1][i], mHoriChildren[1][i
                            - 1],
                            Direction.Right);
                    adjustMoveX = offset / mMinuOffset
                            * (moveX - mLastAdjustX);
                    if (i == 3 || i == 6) {
                        if (adjustMoveX <= offset / 2) {
                            targetScale = -(offset / 2 - adjustMoveX) / (offset / 2) * rawScale1;
                        } else {
                            targetScale = -(adjustMoveX - offset / 2) / (offset / 2) * rawScale1;
                        }
                    } else {
                        targetScale = rawScale1 + adjustMoveX / offset * (rawScale2 - rawScale1);
                    }
                    moveY = computetranslationY(mHoriChildren[1][i],
                            mHoriChildren[1][i - 1],
                            adjustMoveX - mAdjustCount * mMinuOffset);
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
                    adjustMoveX = offset / mMinuOffset
                            * (moveX - mLastAdjustX);
                    targetScale = rawScale1 - adjustMoveX / offset * rawScale1;
                    moveY = 0f;
                } else {
                    rawScale2 = ((LayoutParams) mHoriChildren[2][i - 1]
                            .getLayoutParams()).scale;
                    offset = getOffset(mHoriChildren[2][i], mHoriChildren[2][i
                            - 1],
                            Direction.Right);
                    adjustMoveX = offset / mMinuOffset
                            * (moveX - mLastAdjustX);
                    if (i == 4 || i == 8) {
                        if (adjustMoveX <= offset / 2) {
                            targetScale = -(offset / 2 - adjustMoveX) / (offset / 2) * rawScale1;
                        } else {
                            targetScale = -(adjustMoveX - offset / 2) / (offset / 2) * rawScale1;
                        }
                    } else {
                        targetScale = rawScale1 + adjustMoveX / offset * (rawScale2 - rawScale1);
                    }
                    moveY = computetranslationY(mHoriChildren[2][i],
                            mHoriChildren[2][i - 1], adjustMoveX);
                }
                mHoriChildren[2][i].setScaleX(targetScale);
                mHoriChildren[2][i].setScaleY(targetScale);
                mHoriChildren[2][i].setTranslationX(adjustMoveX);
                mHoriChildren[2][i].setTranslationY(moveY);
            }
        }
    }

    private float computetranslationY(GestureItemView from, GestureItemView to, float tranX) {
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
        for (int i = 0; i < 9; i++) {
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
        float target;
        int temp;
        if (!mSnapping) {
            if (direction == Direction.Left) {
                temp = (int) (mLastMovex / mMinuOffset);
                target = (temp + 2) * mMinuOffset;
                ValueAnimator transAnima = ValueAnimator.ofFloat(mLastMovex, target);
                transAnima.setInterpolator(new DecelerateInterpolator());
                transAnima.setDuration(500);
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
                        mLastAdjustX = 0;
                        mMinuOffset = 0;
                        saveReorderPosition();
                    }
                });
                transAnima.start();
                mSnapping = true;
            } else if (direction == Direction.Right) {
                temp = (int) (mLastMovex / mMinuOffset);
                target = (temp + 2) * mMinuOffset;
                ValueAnimator transAnima = ValueAnimator.ofFloat(mLastMovex, target);
                transAnima.setDuration(500);
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
                        mLastAdjustX = 0;
                        mMinuOffset = 0;
                        saveReorderPosition();
                    }
                });
                transAnima.start();
                mSnapping = true;
            }
        }
    }

    private void snapShort(Direction direction) {
        float distance, offset;
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
                transAnima.setDuration(200);
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
                        mLastAdjustX = 0;
                        mMinuOffset = 0;
                        saveReorderPosition();
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
                transAnima.setDuration(200);
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
                        mLastAdjustX = 0;
                        mMinuOffset = 0;
                        saveReorderPosition();
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

    private Animator iconAppearAnimator(final View targetView) {
        if (targetView == null) {
            return null;
        }
        float scale = targetView.getScaleX();
        float maxScale = 1.2f * scale;
        PropertyValuesHolder pvAlpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1.0f);
        PropertyValuesHolder pvScaleX = PropertyValuesHolder.ofFloat("scaleX", 0.5f, maxScale,
                scale);
        PropertyValuesHolder pvScaleY = PropertyValuesHolder.ofFloat("scaleY", 0.5f, maxScale,
                scale);

        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(targetView, pvAlpha, pvScaleX,
                pvScaleY);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                targetView.setVisibility(View.VISIBLE);
            }
        });
        return anim;
    }

    public AnimatorSet makeIconShowAnimator(int direction) {
        Animator[] iconAnimators = new Animator[11];
        AnimatorSet set = new AnimatorSet();
        AnimatorSet partOneSet = new AnimatorSet();
        AnimatorSet partTwoSet = new AnimatorSet();
        AnimatorSet partThreeSet = new AnimatorSet();
        Animator firstAnim = null, lastAnim = null;

        GestureItemView targetItem;
        for (int i = 0; i < 11; i++) {
            if (i < 4) {
                targetItem = mHoriChildren[0][i + 4];
            } else if (i < 7) {
                targetItem = mHoriChildren[1][i - 1];
            } else {
                targetItem = mHoriChildren[2][i - 3];
            }
            if (iconAppearAnimator(targetItem) != null) {
                iconAnimators[i] = iconAppearAnimator(targetItem);
            }
        }

        if (direction == 0) {// show from left-center
            partOneSet.playTogether(iconAnimators[0], iconAnimators[4], iconAnimators[8]);
            partTwoSet.playTogether(iconAnimators[1], iconAnimators[5], iconAnimators[9]);
            partThreeSet.playTogether(iconAnimators[2], iconAnimators[6], iconAnimators[10]);
            firstAnim = iconAnimators[7];
            lastAnim = iconAnimators[3];
        } else if (direction == 1) {// show from left-bottom
            partOneSet.playTogether(iconAnimators[0], iconAnimators[4], iconAnimators[8]);
            partTwoSet.playTogether(iconAnimators[1], iconAnimators[5], iconAnimators[9]);
            partThreeSet.playTogether(iconAnimators[2], iconAnimators[6], iconAnimators[10]);
            firstAnim = iconAnimators[7];
            lastAnim = iconAnimators[3];
        } else if (direction == 2) {// show from right-center
            partOneSet.playTogether(iconAnimators[3], iconAnimators[6], iconAnimators[9]);
            partTwoSet.playTogether(iconAnimators[2], iconAnimators[5], iconAnimators[8]);
            partThreeSet.playTogether(iconAnimators[1], iconAnimators[4], iconAnimators[7]);
            firstAnim = iconAnimators[10];
            lastAnim = iconAnimators[0];
        } else if (direction == 3) {// show from right-bottom
            partOneSet.playTogether(iconAnimators[3], iconAnimators[6], iconAnimators[9]);
            partTwoSet.playTogether(iconAnimators[2], iconAnimators[5], iconAnimators[8]);
            partThreeSet.playTogether(iconAnimators[1], iconAnimators[4], iconAnimators[7]);
            firstAnim = iconAnimators[10];
            lastAnim = iconAnimators[0];
        }
        firstAnim.setDuration(320);
        partOneSet.setDuration(320).setStartDelay(80);
        partTwoSet.setDuration(320).setStartDelay(160);
        partThreeSet.setDuration(320).setStartDelay(240);
        lastAnim.setDuration(320).setStartDelay(320);
        set.playTogether(firstAnim, partOneSet, partTwoSet, partThreeSet, lastAnim);
        return set;
    }

    private Animator iconDisappearAnimator(final View targetView) {
        float scale = targetView.getScaleX();
        float maxScale = 1.2f * scale;
        PropertyValuesHolder pvAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0f);
        PropertyValuesHolder pvScaleX = PropertyValuesHolder.ofFloat("scaleX", scale, maxScale, 0f);
        PropertyValuesHolder pvScaleY = PropertyValuesHolder.ofFloat("scaleY", scale, maxScale, 0f);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(targetView, pvAlpha, pvScaleX,
                pvScaleY);
        return anim;
    }

    public AnimatorSet makeIconCloseAnimator(int direction) {
        Animator[] iconAnimators = new Animator[11];
        AnimatorSet set = new AnimatorSet();
        AnimatorSet partOneSet = new AnimatorSet();
        AnimatorSet partTwoSet = new AnimatorSet();
        AnimatorSet partThreeSet = new AnimatorSet();
        Animator firstAnim = null, lastAnim = null;

        GestureItemView targetItem;
        for (int i = 0; i < 11; i++) {
            if (i < 4) {
                targetItem = mHoriChildren[0][i + 4];
            } else if (i < 7) {
                targetItem = mHoriChildren[1][i - 1];
            } else {
                targetItem = mHoriChildren[2][i - 3];
            }
            iconAnimators[i] = iconDisappearAnimator(targetItem);
        }

        if (direction == 0) {// show from left-center
            partOneSet.playTogether(iconAnimators[2], iconAnimators[6], iconAnimators[10]);
            partThreeSet.playTogether(iconAnimators[0], iconAnimators[4], iconAnimators[8]);
            partTwoSet.playTogether(iconAnimators[1], iconAnimators[5], iconAnimators[9]);
            firstAnim = iconAnimators[3];
            lastAnim = iconAnimators[7];
        } else if (direction == 1) {// show from left-bottom
            partOneSet.playTogether(iconAnimators[2], iconAnimators[6], iconAnimators[10]);
            partThreeSet.playTogether(iconAnimators[0], iconAnimators[4], iconAnimators[8]);
            partTwoSet.playTogether(iconAnimators[1], iconAnimators[5], iconAnimators[9]);
            firstAnim = iconAnimators[3];
            lastAnim = iconAnimators[7];
        } else if (direction == 2) {// show from right-center
            partOneSet.playTogether(iconAnimators[1], iconAnimators[4], iconAnimators[7]);
            partTwoSet.playTogether(iconAnimators[2], iconAnimators[5], iconAnimators[8]);
            partThreeSet.playTogether(iconAnimators[3], iconAnimators[6], iconAnimators[9]);
            firstAnim = iconAnimators[0];
            lastAnim = iconAnimators[10];
        } else if (direction == 3) {// show from right-bottom
            partOneSet.playTogether(iconAnimators[1], iconAnimators[4], iconAnimators[7]);
            partTwoSet.playTogether(iconAnimators[2], iconAnimators[5], iconAnimators[8]);
            partThreeSet.playTogether(iconAnimators[3], iconAnimators[6], iconAnimators[9]);
            firstAnim = iconAnimators[0];
            lastAnim = iconAnimators[10];
        }
        firstAnim.setDuration(200);
        partOneSet.setDuration(200).setStartDelay(80);
        partTwoSet.setDuration(200).setStartDelay(160);
        partThreeSet.setDuration(200).setStartDelay(240);
        lastAnim.setDuration(200).setStartDelay(320);
        set.playTogether(firstAnim, partOneSet, partTwoSet, partThreeSet, lastAnim);
        return set;
    }
}
