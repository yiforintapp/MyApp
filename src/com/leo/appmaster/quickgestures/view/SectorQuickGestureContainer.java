
package com.leo.appmaster.quickgestures.view;

import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.MessageBean;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickGestureContactTipInfo;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;

//import com.leo.appmaster.quickgestures.view.QuickGestureLayout.LayoutParams;

public class SectorQuickGestureContainer extends FrameLayout {

    public static final String TAG = "QuickGestureContainer";
    private List<QuickSwitcherInfo> mSwitchList;

    public static enum Orientation {
        Left, Right;
    }

    public static enum GType {
        DymicLayout, MostUsedLayout, SwitcherLayout;
    }

    private SectorLayout mDymicLayout, mMostUsedLayout, mSwitcherLayout;
    private SectorTabs mCornerTabs;
    private GType mCurrentGestureType = GType.DymicLayout;
    private Orientation mOrientation = Orientation.Left;
    private GestureDetector mGesDetector;

    private float mSelfWidth, mSelfHeight;
    private float mTouchDownX, mTouchDownY;
    private float mRotateDegree;

    private volatile boolean mEditing;
    private boolean mSnaping;
    private int mFullRotateDuration = 300;

    public SectorQuickGestureContainer(Context context) {
        super(context);
    }

    public SectorQuickGestureContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GestureDirection);

        int derictor = typedArray.getInt(R.styleable.GestureDirection_Direction, 0);
        if (derictor == 0) {
            mOrientation = Orientation.Left;
        } else {
            mOrientation = Orientation.Right;
        }
        typedArray.recycle();
        init();
    }

    private void init() {
        mGesDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        LeoLog.d(TAG, "onSingleTapUp");
                        double offset2CfC;
                        if (mOrientation == Orientation.Left) {
                            offset2CfC = (double) Math.sqrt(Math.pow((e.getX() - 0), 2)
                                    + Math.pow((e.getY() - mSelfHeight), 2));
                        } else {
                            offset2CfC = (double) Math.sqrt(Math.pow((mSelfWidth - e.getX()), 2)
                                    + Math.pow((e.getY() - mSelfHeight), 2));
                        }

                        if (offset2CfC > (mDymicLayout.getOuterRadius() + DipPixelUtil.dip2px(
                                getContext(), 30))) {
                            if (mEditing) {
                                // TODO leave edit mode
                                leaveEditMode();
                            } else {
                                // TODO close quick gesture
                                Activity activity = (Activity) SectorQuickGestureContainer.this
                                        .getContext();
                                activity.onBackPressed();
                                showCloseAnimation();
                            }

                        } else {
                            SectorLayout gestureLayout = null;
                            if (mCurrentGestureType == GType.DymicLayout) {
                                gestureLayout = mDymicLayout;
                            } else if (mCurrentGestureType == GType.MostUsedLayout) {
                                gestureLayout = mMostUsedLayout;
                            } else {
                                gestureLayout = mSwitcherLayout;
                            }
                            gestureLayout.checkItemClick(e.getX(), e.getY());
                        }
                        return super.onSingleTapUp(e);
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                            float velocityY) {
                        LeoLog.d(TAG, "onFling: velocityX = " + velocityX + "  velocityY = "
                                + velocityY);
                        if (!mEditing) {
                            if (mOrientation == Orientation.Left) {
                                if (velocityX > 0 && velocityY > 0
                                        && (velocityX > 300 || velocityY > 300)) {
                                    snapToPrevious();
                                    return true;
                                }

                                if (velocityX < 0 && velocityY < 0
                                        && (velocityX < -300 || velocityY < -300)) {
                                    snapToNext();
                                    return true;
                                }
                            } else {
                                // TODO
                                if (velocityX > 0 && velocityY < 0
                                        && (velocityX > 300 || velocityY < -300)) {
                                    snapToNext();
                                    return true;
                                }

                                if (velocityX < 0 && velocityY > 0
                                        && (velocityX < -300 || velocityY > 300)) {
                                    snapToPrevious();
                                    return true;
                                }
                            }
                        }

                        return super.onFling(e1, e2, velocityX, velocityY);
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        LeoLog.d(TAG, "onLongPress");
                        if (!mEditing) {
                            SectorLayout gestureLayout = null;
                            if (mCurrentGestureType == GType.DymicLayout) {
                                gestureLayout = mDymicLayout;
                            } else if (mCurrentGestureType == GType.MostUsedLayout) {
                                gestureLayout = mMostUsedLayout;
                            } else {
                                gestureLayout = mSwitcherLayout;
                            }
                            gestureLayout.checkItemLongClick(e.getX(), e.getY());
                            super.onLongPress(e);
                        }
                    }

                });

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mOrientation == Orientation.Left) {
            setPivotX(0f);
            setPivotY(getMeasuredHeight());
        } else {
            setPivotX(getMeasuredWidth());
            setPivotY(getMeasuredHeight());
        }
    }

    public void leaveEditMode() {
        mEditing = false;
        mDymicLayout.onLeaveEditMode();
        mMostUsedLayout.onLeaveEditMode();
        mSwitcherLayout.onLeaveEditMode();
    }

    public boolean isEditing() {
        return mEditing;
    }

    public void setEditing(boolean editing) {
        mEditing = editing;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mSelfWidth = getMeasuredWidth();
        mSelfHeight = getMeasuredHeight();
    }

    @Override
    protected void onFinishInflate() {
        mCornerTabs = (SectorTabs) findViewById(R.id.cornerTabs);
        mDymicLayout = (SectorLayout) findViewById(R.id.qg_dymic_layout);
        mMostUsedLayout = (SectorLayout) findViewById(R.id.qg_mostused_layout);
        mSwitcherLayout = (SectorLayout) findViewById(R.id.qg_switcher_layout);

        super.onFinishInflate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LeoLog.e(TAG, "onTouchEvent");
        if (mSnaping)
            return false;
        mGesDetector.onTouchEvent(event);
        float moveX, moveY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mEditing) {
                    LeoLog.d(TAG, "ACTION_DOWN in editing ");
                    SectorLayout gestureLayout = null;
                    if (mCurrentGestureType == GType.DymicLayout) {
                        gestureLayout = mDymicLayout;
                    } else if (mCurrentGestureType == GType.MostUsedLayout) {
                        gestureLayout = mMostUsedLayout;
                    } else {
                        gestureLayout = mSwitcherLayout;
                    }
                    gestureLayout.checkActionDownInEditing(event.getX(), event.getY());
                } else {
                    mTouchDownX = event.getX();
                    mTouchDownY = event.getY();
                    onTouchDown();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mEditing) {
                    LeoLog.d(TAG, "ACTION_MOVE in editing ");

                } else {
                    moveX = event.getX();
                    moveY = event.getY();
                    computeRotateDegree(mTouchDownX, mTouchDownY, moveX, moveY);
                    onTouchMove();
                }
                break;
            case MotionEvent.ACTION_UP:
                onTouchUp();
                break;
            default:
                break;
        }

        return true;
        // return super.onTouchEvent(event);
    }

    private void onTouchUp() {
        LeoLog.d(TAG, "onTouchUp mRotateDegree = " + mRotateDegree);
        // TODO Auto-generated method stub
        if (mOrientation == Orientation.Left) {
            if (mRotateDegree < 0) {
                if (mRotateDegree < -15) {
                    snapToPrevious();
                } else {
                    snapToCurrent();
                }
            } else if (mRotateDegree > 0) {
                if (mRotateDegree > 15) {
                    snapToNext();
                } else {
                    snapToCurrent();
                }
            }
        } else {
            // TODO
            if (mRotateDegree < 0) {
                if (mRotateDegree < -15) {
                    snapToPrevious();
                } else {
                    snapToCurrent();
                }
            } else if (mRotateDegree > 0) {
                if (mRotateDegree > 15) {
                    snapToNext();
                } else {
                    snapToCurrent();
                }
            }
        }
    }

    private void onTouchDown() {
        if (mCurrentGestureType == GType.DymicLayout) {
            if (mOrientation == Orientation.Left) {
                mMostUsedLayout.setCurrentRotateDegree(90);
                mSwitcherLayout.setCurrentRotateDegree(-90);
            } else {
                mMostUsedLayout.setCurrentRotateDegree(-90);
                mSwitcherLayout.setCurrentRotateDegree(90);
            }
            mDymicLayout.setCurrentRotateDegree(0);
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            if (mOrientation == Orientation.Left) {
                mSwitcherLayout.setCurrentRotateDegree(90);
                mDymicLayout.setCurrentRotateDegree(-90);
            } else {
                mSwitcherLayout.setCurrentRotateDegree(-90);
                mDymicLayout.setCurrentRotateDegree(90);
            }
            mMostUsedLayout.setCurrentRotateDegree(0);
        } else if (mCurrentGestureType == GType.SwitcherLayout) {
            if (mOrientation == Orientation.Left) {
                mDymicLayout.setCurrentRotateDegree(90);
                mMostUsedLayout.setCurrentRotateDegree(-90);
            } else {
                mDymicLayout.setCurrentRotateDegree(-90);
                mMostUsedLayout.setCurrentRotateDegree(90);
            }
            mSwitcherLayout.setCurrentRotateDegree(0);
        }
        mDymicLayout.setVisibility(View.VISIBLE);
        mMostUsedLayout.setVisibility(View.VISIBLE);
        mSwitcherLayout.setVisibility(View.VISIBLE);
    }

    private void onTouchMove() {
        rotateLayout();
    }

    private void rotateLayout() {
        if (mRotateDegree == 0)
            return;

        if (mCurrentGestureType == GType.DymicLayout) {
            if (mOrientation == Orientation.Left) {
                if (mRotateDegree > 0) {
                    mMostUsedLayout.setCurrentRotateDegree(90 - mRotateDegree);
                } else if (mRotateDegree < 0) {
                    mSwitcherLayout.setCurrentRotateDegree(-90 - mRotateDegree);
                }
            } else {
                if (mRotateDegree > 0) {
                    mMostUsedLayout.setCurrentRotateDegree(-90 + mRotateDegree);
                } else if (mRotateDegree < 0) {
                    mSwitcherLayout.setCurrentRotateDegree(90 + mRotateDegree);
                }
            }
            if (mOrientation == Orientation.Left) {
                mDymicLayout.setCurrentRotateDegree(-mRotateDegree);
            } else {
                mDymicLayout.setCurrentRotateDegree(mRotateDegree);
            }
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            if (mOrientation == Orientation.Left) {
                if (mRotateDegree > 0) {
                    mSwitcherLayout.setCurrentRotateDegree(90 - mRotateDegree);
                } else if (mRotateDegree < 0) {
                    mDymicLayout.setCurrentRotateDegree(-90 - mRotateDegree);
                }
            } else {
                if (mRotateDegree > 0) {
                    mSwitcherLayout.setCurrentRotateDegree(-90 + mRotateDegree);
                } else if (mRotateDegree < 0) {
                    mDymicLayout.setCurrentRotateDegree(90 + mRotateDegree);
                }
            }
            if (mOrientation == Orientation.Left) {
                mMostUsedLayout.setCurrentRotateDegree(-mRotateDegree);
            } else {
                mMostUsedLayout.setCurrentRotateDegree(mRotateDegree);
            }
        } else if (mCurrentGestureType == GType.SwitcherLayout) {
            if (mOrientation == Orientation.Left) {
                if (mRotateDegree > 0) {
                    mDymicLayout.setCurrentRotateDegree(90 - mRotateDegree);
                } else if (mRotateDegree < 0) {
                    mMostUsedLayout.setCurrentRotateDegree(-90 - mRotateDegree);
                }
            } else {
                if (mRotateDegree > 0) {
                    mDymicLayout.setCurrentRotateDegree(-90 + mRotateDegree);
                } else if (mRotateDegree < 0) {
                    mMostUsedLayout.setCurrentRotateDegree(90 + mRotateDegree);
                }
            }
            if (mOrientation == Orientation.Left) {
                mSwitcherLayout.setCurrentRotateDegree(-mRotateDegree);
            } else {
                mSwitcherLayout.setCurrentRotateDegree(mRotateDegree);
            }
        }

        if (mOrientation == Orientation.Left) {
            mCornerTabs.updateCoverDegree(mRotateDegree / 3);
        } else {
            mCornerTabs.updateCoverDegree(-mRotateDegree / 3);
        }

    }

    private void computeRotateDegree(float firstX, float firstY, float secondX,
            float secondY) {

        float firstOffsetX, firstOffsetY, secondOffsetX, secondOffsetY;
        if (mOrientation == Orientation.Left) {
            firstOffsetX = firstX;
            firstOffsetY = mSelfHeight - firstY;
            secondOffsetX = secondX;
            secondOffsetY = mSelfHeight - secondY;
        } else {
            firstOffsetX = mSelfWidth - firstX;
            firstOffsetY = mSelfHeight - firstY;
            secondOffsetX = mSelfWidth - secondX;
            secondOffsetY = mSelfHeight - secondY;
        }

        float firstDegree = (float) (Math.atan(firstOffsetY / firstOffsetX) * 180f / Math.PI);
        float secondDegree = (float) (Math.atan(secondOffsetY / secondOffsetX) * 180f / Math.PI);

        mRotateDegree = secondDegree - firstDegree;
        LeoLog.d(TAG, "mRotateDegree = " + mRotateDegree + "        firstDegree = " + firstDegree
                + "        secondDegree = " + secondDegree);
    }

    public void snapToCurrent() {
        if (mSnaping)
            return;
        float duration = Math.abs(90 - mRotateDegree) / 90 * mFullRotateDuration + 5f;
        ValueAnimator va = ValueAnimator.ofFloat(mRotateDegree, 0);
        va.setDuration((long) duration);
        va.setInterpolator(new DecelerateInterpolator());
        va.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                mSnaping = true;
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mSnaping = false;
                if (mCurrentGestureType == GType.DymicLayout) {
                    mDymicLayout.setVisibility(View.VISIBLE);
                    mSwitcherLayout.setVisibility(View.GONE);
                    mMostUsedLayout.setVisibility(View.GONE);
                } else if (mCurrentGestureType == GType.MostUsedLayout) {
                    mMostUsedLayout.setVisibility(View.VISIBLE);
                    mSwitcherLayout.setVisibility(View.GONE);
                    mDymicLayout.setVisibility(View.GONE);
                } else if (mCurrentGestureType == GType.SwitcherLayout) {
                    mSwitcherLayout.setVisibility(View.VISIBLE);
                    mDymicLayout.setVisibility(View.GONE);
                    mMostUsedLayout.setVisibility(View.GONE);
                }
                mRotateDegree = 0;
            }
        });
        va.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRotateDegree = (Float) animation.getAnimatedValue();
                rotateLayout();
            }
        });
        va.start();
    }

    public void snapToPrevious() {

        if (mSnaping)
            return;
        float duration = Math.abs(90 - mRotateDegree) / 90 * mFullRotateDuration;
        ValueAnimator va = null;
        va = ValueAnimator.ofFloat(mRotateDegree, -90);
        va.setDuration((long) duration);
        va.setInterpolator(new DecelerateInterpolator());
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mSnaping = true;
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mSnaping = false;
                if (mCurrentGestureType == GType.DymicLayout) {
                    mCurrentGestureType = GType.SwitcherLayout;
                    mSwitcherLayout.setVisibility(View.VISIBLE);
                    mDymicLayout.setVisibility(View.GONE);
                    mMostUsedLayout.setVisibility(View.GONE);
                    mCornerTabs.snapDynamic2Switcher();
                } else if (mCurrentGestureType == GType.MostUsedLayout) {
                    mCurrentGestureType = GType.DymicLayout;
                    mDymicLayout.setVisibility(View.VISIBLE);
                    mSwitcherLayout.setVisibility(View.GONE);
                    mMostUsedLayout.setVisibility(View.GONE);
                } else if (mCurrentGestureType == GType.SwitcherLayout) {
                    mCurrentGestureType = GType.MostUsedLayout;
                    mMostUsedLayout.setVisibility(View.VISIBLE);
                    mDymicLayout.setVisibility(View.GONE);
                    mSwitcherLayout.setVisibility(View.GONE);
                }
                mRotateDegree = 0;
            }
        });
        va.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRotateDegree = (Float) animation.getAnimatedValue();
                rotateLayout();
            }
        });
        va.start();
    }

    public void snapToNext() {
        if (mSnaping)
            return;
        float duration = Math.abs(90 - mRotateDegree) / 90 * mFullRotateDuration;
        ValueAnimator va = null;
        // if (mOrientation == Orientation.Left) {
        // va = ValueAnimator.ofFloat(mRotateDegree, -90);
        // } else {
        va = ValueAnimator.ofFloat(mRotateDegree, 90);
        // }
        va.setDuration((long) duration);
        va.setInterpolator(new DecelerateInterpolator());
        va.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                mSnaping = true;
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mSnaping = false;
                if (mCurrentGestureType == GType.DymicLayout) {
                    if (mOrientation == Orientation.Left) {
                        mCurrentGestureType = GType.MostUsedLayout;
                        mMostUsedLayout.setVisibility(View.VISIBLE);
                        mDymicLayout.setVisibility(View.GONE);
                        mSwitcherLayout.setVisibility(View.GONE);
                    } else {
                        mCurrentGestureType = GType.MostUsedLayout;
                        mMostUsedLayout.setVisibility(View.VISIBLE);
                        mDymicLayout.setVisibility(View.GONE);
                        mSwitcherLayout.setVisibility(View.GONE);
                    }

                } else if (mCurrentGestureType == GType.MostUsedLayout) {
                    if (mOrientation == Orientation.Left) {
                        mCurrentGestureType = GType.SwitcherLayout;
                        mSwitcherLayout.setVisibility(View.VISIBLE);
                        mDymicLayout.setVisibility(View.GONE);
                        mMostUsedLayout.setVisibility(View.GONE);
                    } else {
                        mCurrentGestureType = GType.SwitcherLayout;
                        mSwitcherLayout.setVisibility(View.VISIBLE);
                        mDymicLayout.setVisibility(View.GONE);
                        mMostUsedLayout.setVisibility(View.GONE);
                    }

                } else if (mCurrentGestureType == GType.SwitcherLayout) {
                    if (mOrientation == Orientation.Left) {
                        mCurrentGestureType = GType.DymicLayout;
                        mDymicLayout.setVisibility(View.VISIBLE);
                        mMostUsedLayout.setVisibility(View.GONE);
                        mSwitcherLayout.setVisibility(View.GONE);
                        mCornerTabs.snapSwitcher2Dynamic();
                    } else {
                        mCurrentGestureType = GType.DymicLayout;
                        mDymicLayout.setVisibility(View.VISIBLE);
                        mMostUsedLayout.setVisibility(View.GONE);
                        mSwitcherLayout.setVisibility(View.GONE);
                        mCornerTabs.snapSwitcher2Dynamic();
                    }

                }
                mRotateDegree = 0;
            }
        });
        va.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRotateDegree = (Float) animation.getAnimatedValue();
                rotateLayout();
            }
        });
        va.start();
    }

    public void fillGestureItem(GType type, List<? extends BaseInfo> infos) {
        if (infos == null) {
            LeoLog.e(TAG, "fillGestureItem, infos is null");
            return;
        }
        SectorLayout targetLayout = null;
        if (type == GType.DymicLayout) {
            targetLayout = mDymicLayout;
            fillDynamicItem(targetLayout, infos, 0);
        } else if (type == GType.MostUsedLayout) {
            targetLayout = mMostUsedLayout;
            fillItem(targetLayout, infos);
        } else if (type == GType.SwitcherLayout) {
            targetLayout = mSwitcherLayout;
            setSwitchList((List<QuickSwitcherInfo>) infos);
            fillSwitchItem(targetLayout, infos);
        }
    }

    public void fillDynamicItem(SectorLayout targetLayout,
            List<? extends BaseInfo> itemInfos, int businessIndes) {
        if (targetLayout != null) {
            targetLayout.removeAllViews();
            GestureItemView tv = null;
            AppleWatchLayout.LayoutParams lp = null;
            BaseInfo info = null;
            List<BaseInfo> infos = (List<BaseInfo>) itemInfos;
            // 快捷手势未读短信提醒
            boolean isShowMsmTip = AppMasterPreference.getInstance(getContext())
                    .getSwitchOpenNoReadMessageTip();
            boolean isShowCallLogTip = AppMasterPreference.getInstance(getContext())
                    .getSwitchOpenRecentlyContact();
            boolean isShowPrivacyContactTip = AppMasterPreference.getInstance(getContext())
                    .getSwitchOpenPrivacyContactMessageTip();
            if (isShowMsmTip) {
                if (QuickGestureManager.getInstance(mContext).getQuiQuickNoReadMessage()  != null
                        && QuickGestureManager.getInstance(mContext).getQuiQuickNoReadMessage() .size() > 0) {
                    for (MessageBean message : QuickGestureManager.getInstance(mContext).getQuiQuickNoReadMessage() ) {
                        message.icon = getContext().getResources().getDrawable(
                                R.drawable.gesture_message);
                        if (message.getMessageName() != null
                                && !"".equals(message.getMessageName())) {
                            message.label = message.getMessageName();
                        } else {
                            message.label = message.getPhoneNumber();
                        }
                        message.isShowReadTip = true;
                        infos.add(0, message);
                    }
                }
            }
            // 快捷手势未读通话提醒
            if (isShowCallLogTip) {
                if (QuickGestureManager.getInstance(mContext).getQuickNoReadCall() != null
                        && QuickGestureManager.getInstance(mContext).getQuickNoReadCall().size() > 0) {
                    for (ContactCallLog baseInfo : QuickGestureManager.getInstance(mContext).getQuickNoReadCall()) {
                        baseInfo.icon = getContext().getResources().getDrawable(
                                R.drawable.gesture_call);
                        if (baseInfo.getCallLogName() != null
                                && !"".equals(baseInfo.getCallLogName())) {
                            baseInfo.label = baseInfo.getCallLogName();
                        } else {
                            baseInfo.label = baseInfo.getCallLogNumber();
                        }
                        baseInfo.isShowReadTip = true;
                        infos.add(0, baseInfo);
                    }
                }
            }
            // 快捷手势未读隐私短信提示
            if (isShowPrivacyContactTip) {
                if (QuickGestureManager.getInstance(mContext).isShowPrivacyCallLog
                        || QuickGestureManager.getInstance(mContext).isShowPrivacyMsm) {
                    QuickGestureContactTipInfo item = new QuickGestureContactTipInfo();
                    item.icon = getContext().getResources().getDrawable(
                            R.drawable.gesture_system);
                    item.label = mContext.getResources().getString(
                            R.string.pg_appmanager_quick_gesture_privacy_contact_tip_lable);
                    item.isShowReadTip = true;
                    infos.add(businessIndes, item);
                }
            }
            if (infos.size() > 9) {
                infos = infos.subList(0, 9);
            }

            for (int i = 0; i < infos.size(); i++) {
                tv = new GestureItemView(getContext());
                lp = new AppleWatchLayout.LayoutParams(
                        targetLayout.getItemSize(), targetLayout.getItemSize());
                lp.position = i;
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setLayoutParams(lp);
                info = infos.get(i);
                if (info instanceof QuickGestureContactTipInfo) {
                    if (((QuickGestureContactTipInfo) info).isShowReadTip) {
                        tv.showReadTip();
                    } else {
                        tv.cancelShowReadTip();
                    }
                } else if (info instanceof MessageBean) {
                    if (((MessageBean) info).isShowReadTip) {
                        tv.showReadTip();
                    } else {
                        tv.cancelShowReadTip();
                    }
                } else if (info instanceof ContactCallLog) {
                    if (((ContactCallLog) info).isShowReadTip) {
                        tv.showReadTip();
                    } else {
                        tv.cancelShowReadTip();
                    }
                }
                tv.setItemName(info.label);
                tv.setItemIcon(info.icon, true);
                tv.setDecorateAction(new EventAction(getContext(), info.eventNumber));
                tv.setTag(info);
                targetLayout.addView(tv);
            }
        }
    }

    public void fillSwitchItem(SectorLayout targetLayout, List<? extends BaseInfo> infos) {
        if (targetLayout != null) {
            targetLayout.removeAllViews();
            GestureItemView tv = null;
            AppleWatchLayout.LayoutParams lp = null;
            QuickSwitcherInfo sInfo = null;
            int iconSize = targetLayout.getIconSize();
            for (int i = 0; i < infos.size(); i++) {
                if (i >= 9) {
                    break;
                }
                sInfo = (QuickSwitcherInfo) infos.get(i);
                tv = new GestureItemView(getContext());
                lp = new AppleWatchLayout.LayoutParams(
                        targetLayout.getItemSize(), targetLayout.getItemSize());
                lp.position = sInfo.gesturePosition;
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setLayoutParams(lp);
                tv.setItemName(sInfo.label);
                // tv.setTextSize(12);
                if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext).getLabelFromName(
                        QuickSwitchManager.BLUETOOTH))) {
                    // check 蓝牙状态
                    checkBlueToothStatus(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.FLASHLIGHT))) {
                    // 手电筒状态
                    checkFlashLightStatus(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.WLAN))) {
                    // Wifi状态
                    checkWlanStatus(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.CRAME))) {
                    // Crame状态
                    checkCrameStatus(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.SOUND))) {
                    // Sound状态
                    checkSoundStatus(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.LIGHT))) {
                    // 亮度状态
                    checkLightStatus(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.SPEEDUP))) {
                    // 加速
                    checkSpeedUpStatus(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.CHANGEMODE))) {
                    // 情景模式切换
                    checkChangeMode(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.SWITCHSET))) {
                    // 手势设置
                    checkSwitchSet(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.SETTING))) {
                    // 系统设置
                    checkSetting(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.GPS))) {
                    // GPS
                    checkGPS(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.FLYMODE))) {
                    // 飞行模式
                    checkFlyMode(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.ROTATION))) {
                    // 飞行模式
                    checkRotation(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.MOBILEDATA))) {
                    // 移动数据
                    checkMobileData(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.HOME))) {
                    // 桌面
                    checkHome(sInfo, iconSize, tv);
                } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                        .getLabelFromName(
                                QuickSwitchManager.XUKUANG))) {
                    // 虚框
                    checkXuKuang(sInfo, iconSize, tv);
                }
                if (sInfo.eventNumber > 0) {
                    tv.setDecorateAction(new EventAction(getContext(), sInfo.eventNumber));
                }
                tv.setTag(sInfo);
                targetLayout.addView(tv);
            }
        }
    }

    private void checkXuKuang(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
        tv.setItemIcon(sInfo.switchIcon[0], false);
    }

    private void checkLockScreen(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
        tv.setItemIcon(sInfo.switchIcon[0], false);
    }

    private void checkHome(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
        tv.setItemIcon(sInfo.switchIcon[0], false);
    }

    private void checkMobileData(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (QuickSwitchManager.checkMoblieData()) {
            sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[0], false);
        }
    }

    private void checkRotation(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (QuickSwitchManager.checkRotation()) {
            sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.switchIcon[1].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[1], false);
        }
    }

    private void checkFlyMode(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (QuickSwitchManager.checkFlyMode()) {
            sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.switchIcon[1].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[1], false);
        }
    }

    private void checkGPS(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (QuickSwitchManager.checkGps()) {
            sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.switchIcon[1].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[1], false);
        }
    }

    private void checkSetting(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
        tv.setItemIcon(sInfo.switchIcon[0], false);
    }

    private void checkSwitchSet(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
        tv.setItemIcon(sInfo.switchIcon[0], false);
    }

    private void checkChangeMode(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
        tv.setItemIcon(sInfo.switchIcon[0], false);
    }

    private void checkSpeedUpStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
        tv.setItemIcon(sInfo.switchIcon[0], false);
    }

    private void checkLightStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (QuickSwitchManager.checkLight() == QuickSwitchManager.LIGHT_AUTO) {
            sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else if (QuickSwitchManager.checkLight() == QuickSwitchManager.LIGHT_NORMAL) {
            sInfo.switchIcon[1].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[1], false);
        } else if (QuickSwitchManager.checkLight() == QuickSwitchManager.LIGHT_50_PERCENT) {
            sInfo.switchIcon[2].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[2], false);
        } else if (QuickSwitchManager.checkLight() == QuickSwitchManager.LIGHT_100_PERCENT) {
            sInfo.switchIcon[3].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[3], false);
        } else {
            // err
            sInfo.switchIcon[1].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[1], false);
        }
    }

    private void checkSoundStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (QuickSwitchManager.checkSound() == QuickSwitchManager.mSound) {
            sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else if (QuickSwitchManager.checkSound() == QuickSwitchManager.mQuite) {
            sInfo.switchIcon[1].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.switchIcon[2].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[0], false);
        }
    }

    private void checkCrameStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
        tv.setItemIcon(sInfo.switchIcon[0], false);
    }

    private void checkWlanStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (QuickSwitchManager.checkWlan()) {
            sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.switchIcon[1].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[1], false);
        }
    }

    private void checkFlashLightStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (QuickSwitchManager.checkFlashLight()) {
            sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.switchIcon[1].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[1], false);
        }
    }

    private void checkBlueToothStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (QuickSwitchManager.checkBlueTooth()) {
            sInfo.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.switchIcon[1].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(sInfo.switchIcon[1], false);
        }
    }

    public void fillItem(SectorLayout targetLayout, List<? extends BaseInfo> infos) {
        if (targetLayout != null) {
            targetLayout.removeAllViews();
            GestureItemView tv = null;
            AppleWatchLayout.LayoutParams lp = null;
            BaseInfo info = null;
            for (int i = 0; i < infos.size(); i++) {
                if (i >= 11) {
                    break;
                }
                tv = new GestureItemView(getContext());
                lp = new AppleWatchLayout.LayoutParams(
                        targetLayout.getItemSize(), targetLayout.getItemSize());
                lp.position = i;
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setLayoutParams(lp);
                info = infos.get(i);
                tv.setItemName(info.label);
                tv.setItemIcon(info.icon, false);
                if (info.eventNumber > 0) {
                    tv.setDecorateAction(new EventAction(getContext(), info.eventNumber));
                }
                tv.setTag(info);
                targetLayout.addView(tv);
            }

            targetLayout.checkFull();
        }
    }

    public GType getCurrentGestureType() {
        return mCurrentGestureType;
    }

    public float getRotateDegree() {
        return mRotateDegree;
    }

    public void showOpenAnimation() {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(400);
        Animator animationx = ObjectAnimator.ofFloat(this, "scaleX", 0.0f, 1.1f,
                1.0f);
        Animator animationy = ObjectAnimator.ofFloat(this, "scaleY", 0.0f, 1.1f,
                1.0f);
        set.playTogether(animationx, animationy);
        set.start();
    }

    public void showCloseAnimation() {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(400);
        Animator animationx = ObjectAnimator.ofFloat(this, "scaleX", 1.0f,
                1.1f, 0.0f);
        Animator animationy = ObjectAnimator.ofFloat(this, "scaleY", 1.0f,
                1.1f, 0.0f);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Activity activity = (Activity) SectorQuickGestureContainer.this.getContext();
                FloatWindowHelper.mGestureShowing = false;
                activity.finish();
                super.onAnimationEnd(animation);
            }
        });
        set.playTogether(animationx, animationy);
        set.start();
    }

    public void hideGestureLayout(GType type) {
        if (type == GType.DymicLayout) {
            mDymicLayout.setVisibility(View.GONE);
        } else if (type == GType.MostUsedLayout) {
            mMostUsedLayout.setVisibility(View.GONE);
        } else if (type == GType.SwitcherLayout) {
            mSwitcherLayout.setVisibility(View.GONE);
        }
    }

    public void showGestureLayout(GType type) {
        if (type == GType.DymicLayout) {
            mDymicLayout.setVisibility(View.VISIBLE);
        } else if (type == GType.MostUsedLayout) {
            mMostUsedLayout.setVisibility(View.VISIBLE);
        } else if (type == GType.SwitcherLayout) {
            mSwitcherLayout.setVisibility(View.VISIBLE);
        }
    }

    public void snapToSwitcher() {
        if (mCurrentGestureType == GType.DymicLayout) {
            onTouchDown();
            snapToPrevious();
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            onTouchDown();
            snapToNext();
        }
    }

    public void snapToMostUsed() {
        if (mCurrentGestureType == GType.DymicLayout) {
            onTouchDown();
            snapToNext();
        } else if (mCurrentGestureType == GType.SwitcherLayout) {
            onTouchDown();
            snapToPrevious();
        }

    }

    public void setSwitchList(List<QuickSwitcherInfo> list) {
        mSwitchList = list;
    }

    public List<QuickSwitcherInfo> getSwitchList() {
        return mSwitchList;
    }

    public void snapToDynamic() {
        if (mCurrentGestureType == GType.MostUsedLayout) {
            onTouchDown();
            snapToPrevious();
        } else if (mCurrentGestureType == GType.SwitcherLayout) {
            onTouchDown();
            snapToNext();
        }
    }

}
