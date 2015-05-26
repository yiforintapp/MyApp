
package com.leo.appmaster.quickgestures.view;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.leo.appmaster.quickgestures.ui.QuickGesturePopupActivity;
import com.leo.appmaster.quickgestures.view.AppleWatchLayout.Direction;
import com.leo.appmaster.utils.LeoLog;

@SuppressLint("InflateParams")
public class AppleWatchContainer extends FrameLayout {

    public static final String TAG = "AppleWatchQuickGestureContainer";
    private static final int mGetIcon = -1;
    private List<QuickSwitcherInfo> mSwitchList;

    public static enum Orientation {
        Left, Right;
    }

    public static enum GType {
        DymicLayout, MostUsedLayout, SwitcherLayout;
    }

    private AppleWatchLayout mDymicLayout, mMostUsedLayout, mSwitcherLayout;
    private AppleWatchTabs mCornerTabs;
    private TextView mTvCurName;
    private QuickGesturePopupActivity mPopupActivity;
    // private ImageView iv_rocket;
    private GType mCurrentGestureType = GType.DymicLayout;
    private Orientation mOrientation = Orientation.Left;
    private GestureDetector mGesDetector;

    private float mSelfWidth, mSelfHeight;
    private float mTouchDownX, mTouchDownY;
    private float mRotateDegree;

    private volatile boolean mEditing;
    private boolean mSnaping;
    private int mFullRotateDuration = 300;
    private int mStartAngle = 40;

    public AppleWatchContainer(Context context) {
        super(context);
    }

    public AppleWatchContainer(Context context, AttributeSet attrs) {
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
                        float tapY = e.getY();
                        float tapX = e.getX();
                        if (tapY < mDymicLayout.getTop()
                                || (tapY >= mDymicLayout.getBottom() && (tapX < mCornerTabs
                                        .getLeft() || tapX > mCornerTabs.getRight()))) {
                            if (mEditing) {
                                leaveEditMode();
                            } else {
                                Activity activity = (Activity) AppleWatchContainer.this
                                        .getContext();
                                activity.onBackPressed();
                            }

                        } else {
                            AppleWatchLayout gestureLayout = null;
                            if (mCurrentGestureType == GType.DymicLayout) {
                                gestureLayout = mDymicLayout;
                            } else if (mCurrentGestureType == GType.MostUsedLayout) {
                                gestureLayout = mMostUsedLayout;
                            } else {
                                gestureLayout = mSwitcherLayout;
                            }
                            gestureLayout.checkItemClick(e.getX(), tapY - mDymicLayout.getTop());
                        }
                        return super.onSingleTapUp(e);
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                            float velocityY) {
                        LeoLog.d(TAG, "onFling: velocityX = " + velocityX + "  velocityY = "
                                + velocityY);
                        if (!mEditing) {
                            if (mTouchDownY > mDymicLayout.getBottom()) {
                                if (velocityX > 300) {
                                    snapToPrevious();
                                    return true;
                                }
                                if (velocityX < -300) {
                                    snapToNext();
                                    return true;
                                }
                            } else if (mTouchDownY > mDymicLayout.getTop()) {
                                AppleWatchLayout gestureLayout = null;
                                if (mCurrentGestureType == GType.DymicLayout) {
                                    gestureLayout = mDymicLayout;
                                } else if (mCurrentGestureType == GType.MostUsedLayout) {
                                    gestureLayout = mMostUsedLayout;
                                } else {
                                    gestureLayout = mSwitcherLayout;
                                }
                                if (velocityX > 300) {
                                    gestureLayout.snapLong(Direction.Left);
                                    return true;
                                }
                                if (velocityX < -300) {
                                    gestureLayout.snapLong(Direction.Right);
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
                            AppleWatchLayout gestureLayout = null;
                            if (mCurrentGestureType == GType.DymicLayout) {
                                gestureLayout = mDymicLayout;
                            } else if (mCurrentGestureType == GType.MostUsedLayout) {
                                gestureLayout = mMostUsedLayout;
                            } else {
                                gestureLayout = mSwitcherLayout;
                            }
                            gestureLayout.checkItemLongClick(e.getX(),
                                    e.getY() - gestureLayout.getTop());
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
        AppleWatchLayout gestureLayout = null;
        if (mCurrentGestureType == GType.DymicLayout) {
            gestureLayout = mDymicLayout;
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            gestureLayout = mMostUsedLayout;
        } else {
            gestureLayout = mSwitcherLayout;
        }
        gestureLayout.onLeaveEditMode();
    }

    public boolean isEditing() {
        return mEditing;
    }

    public void setEditing(boolean editing) {
        mEditing = editing;
    }

    public float getStartAngle() {
        return mRotateDegree;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mSelfWidth = getMeasuredWidth();
        mSelfHeight = getMeasuredHeight();
    }

    @Override
    protected void onFinishInflate() {
        mTvCurName = (TextView) findViewById(R.id.tv_type_name);
        mCornerTabs = (AppleWatchTabs) findViewById(R.id.applewatchtab);
        mDymicLayout = (AppleWatchLayout) findViewById(R.id.qg_dymic_layout);
        mMostUsedLayout = (AppleWatchLayout) findViewById(R.id.qg_mostused_layout);
        mSwitcherLayout = (AppleWatchLayout) findViewById(R.id.qg_switcher_layout);
        mTvCurName.setText(R.string.quick_gesture_dynamic);
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
                    AppleWatchLayout gestureLayout = null;
                    if (mCurrentGestureType == GType.DymicLayout) {
                        gestureLayout = mDymicLayout;
                    } else if (mCurrentGestureType == GType.MostUsedLayout) {
                        gestureLayout = mMostUsedLayout;
                    } else {
                        gestureLayout = mSwitcherLayout;
                    }
                    gestureLayout.checkActionDownInEditing(event.getX(), event.getY()
                            - gestureLayout.getTop());
                } else {
                    mTouchDownX = event.getX();
                    mTouchDownY = event.getY();
                    if (mTouchDownY > mDymicLayout.getBottom()) {
                        onTouchDown();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mEditing) {
                    LeoLog.d(TAG, "ACTION_MOVE in editing ");

                } else {
                    moveX = event.getX();
                    moveY = event.getY();

                    if (mTouchDownY >= mDymicLayout.getTop()
                            && mTouchDownY <= mDymicLayout.getBottom()) {
                        onTouchMoveTranslate(moveX - mTouchDownX, moveY - mTouchDownY);
                    }

                    if (mTouchDownY > mDymicLayout.getBottom()) {
                        onTouchMoveRotate(moveX, moveY);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                onTouchUp();
                break;
            default:
                break;
        }

        return true;
    }

    private void onTouchUp() {
        LeoLog.d(TAG, "onTouchUp mRotateDegree = " + mRotateDegree);
        if (mTouchDownY > mDymicLayout.getBottom()) {
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
            } else if (mOrientation == Orientation.Right) {
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
        } else if (mTouchDownY > mDymicLayout.getTop()) {
            if (mCurrentGestureType == GType.DymicLayout) {
                mDymicLayout.onTouchUp();
            } else if (mCurrentGestureType == GType.MostUsedLayout) {
                mMostUsedLayout.onTouchUp();
            } else if (mCurrentGestureType == GType.SwitcherLayout) {
                mSwitcherLayout.onTouchUp();
            }
        }
    }

    private void onTouchDown() {
        if (mCurrentGestureType == GType.DymicLayout) {
            mMostUsedLayout.setCurrentRotateDegree(mStartAngle);
            mSwitcherLayout.setCurrentRotateDegree(-mStartAngle);
            mDymicLayout.setCurrentRotateDegree(0);
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            mSwitcherLayout.setCurrentRotateDegree(mStartAngle);
            mDymicLayout.setCurrentRotateDegree(-mStartAngle);
            mMostUsedLayout.setCurrentRotateDegree(0);
        } else if (mCurrentGestureType == GType.SwitcherLayout) {
            mDymicLayout.setCurrentRotateDegree(mStartAngle);
            mMostUsedLayout.setCurrentRotateDegree(-mStartAngle);
            mSwitcherLayout.setCurrentRotateDegree(0);
        }
        mDymicLayout.setVisibility(View.VISIBLE);
        mMostUsedLayout.setVisibility(View.VISIBLE);
        mSwitcherLayout.setVisibility(View.VISIBLE);
    }

    private void onTouchMoveRotate(float moveX, float moveY) {
        computeRotateDegree(mTouchDownX, mTouchDownY, moveX, moveY);
        rotateLayout();
    }

    private void onTouchMoveTranslate(float moveX, float moveY) {
        // TODO
        AppleWatchLayout targetLayout;
        if (mCurrentGestureType == GType.DymicLayout) {
            targetLayout = mDymicLayout;
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            targetLayout = mMostUsedLayout;
        } else {
            targetLayout = mSwitcherLayout;
        }
        targetLayout.translateItem(moveX);

    }

    private void rotateLayout() {
        if (mRotateDegree == 0)
            return;
        if (mCurrentGestureType == GType.DymicLayout) {
            if (mOrientation == Orientation.Left) {
                if (mRotateDegree > 0) {
                    mMostUsedLayout.setCurrentRotateDegree(mStartAngle - mRotateDegree);
                } else if (mRotateDegree < 0) {
                    mSwitcherLayout.setCurrentRotateDegree(-mStartAngle - mRotateDegree);
                }
            } else {
                if (mRotateDegree > 0) {
                    mMostUsedLayout.setCurrentRotateDegree(-mStartAngle + mRotateDegree);
                } else if (mRotateDegree < 0) {
                    mSwitcherLayout.setCurrentRotateDegree(mStartAngle + mRotateDegree);
                }
            }
            if (mOrientation == Orientation.Left) {
                mDymicLayout.setCurrentRotateDegree(-mRotateDegree);
            } else {
                mDymicLayout.setCurrentRotateDegree(mRotateDegree);
            }
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            if (mRotateDegree > 0) {
                mSwitcherLayout.setCurrentRotateDegree(mStartAngle - mRotateDegree);
            } else if (mRotateDegree < 0) {
                mDymicLayout.setCurrentRotateDegree(-mStartAngle - mRotateDegree);
            }
            mMostUsedLayout.setCurrentRotateDegree(-mRotateDegree);

        } else if (mCurrentGestureType == GType.SwitcherLayout) {
            if (mRotateDegree > 0) {
                mDymicLayout.setCurrentRotateDegree(mStartAngle - mRotateDegree);
            } else if (mRotateDegree < 0) {
                mMostUsedLayout.setCurrentRotateDegree(-mStartAngle - mRotateDegree);
            }
            mSwitcherLayout.setCurrentRotateDegree(-mRotateDegree);
        }
        mCornerTabs.updateCoverDegree(mRotateDegree);

    }

    private void computeRotateDegree(float firstX, float firstY, float secondX,
            float secondY) {

        float py = mDymicLayout.getPivotY() - mDymicLayout.getMeasuredHeight()
                - mCornerTabs.getMeasuredHeight();

        float firstOffsetX, firstOffsetY, secondOffsetX, secondOffsetY;
        firstOffsetX = firstX;
        firstOffsetY = mSelfHeight - firstY + py;
        secondOffsetX = secondX;
        secondOffsetY = mSelfHeight - secondY + py;

        float firstDegree = (float) (Math.atan(firstOffsetY / firstOffsetX) * 180f / Math.PI);
        float secondDegree = (float) (Math.atan(secondOffsetY / secondOffsetX) * 180f / Math.PI);

        mRotateDegree = secondDegree - firstDegree;
        LeoLog.d(TAG, "mRotateDegree = " + mRotateDegree + "        firstDegree = " + firstDegree
                + "        secondDegree = " + secondDegree);
    }

    public void snapToCurrent() {
        if (mSnaping)
            return;
        float duration = Math.abs(mStartAngle - mRotateDegree) / mStartAngle * mFullRotateDuration;
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
                mCornerTabs.resetLayout();
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
        float duration = Math.abs(mStartAngle - mRotateDegree) / mStartAngle * mFullRotateDuration;
        ValueAnimator va = null;
        va = ValueAnimator.ofFloat(mRotateDegree, -mStartAngle);
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
                    mTvCurName.setText(R.string.quick_gesture_switcher);
                } else if (mCurrentGestureType == GType.MostUsedLayout) {
                    mCurrentGestureType = GType.DymicLayout;
                    mDymicLayout.setVisibility(View.VISIBLE);
                    mSwitcherLayout.setVisibility(View.GONE);
                    mMostUsedLayout.setVisibility(View.GONE);
                    mTvCurName.setText(R.string.quick_gesture_dynamic);
                } else if (mCurrentGestureType == GType.SwitcherLayout) {
                    mCurrentGestureType = GType.MostUsedLayout;
                    mMostUsedLayout.setVisibility(View.VISIBLE);
                    mDymicLayout.setVisibility(View.GONE);
                    mSwitcherLayout.setVisibility(View.GONE);
                    mTvCurName.setText(R.string.quick_gesture_most_used);
                }
                mRotateDegree = 0;
                mCornerTabs.resetLayout();
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
        float duration = Math.abs(mStartAngle - mRotateDegree) / mStartAngle * mFullRotateDuration;
        ValueAnimator va = null;
        va = ValueAnimator.ofFloat(mRotateDegree, mStartAngle);
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
                        mTvCurName.setText(R.string.quick_gesture_most_used);
                    } else {
                        mCurrentGestureType = GType.MostUsedLayout;
                        mMostUsedLayout.setVisibility(View.VISIBLE);
                        mDymicLayout.setVisibility(View.GONE);
                        mSwitcherLayout.setVisibility(View.GONE);
                        mTvCurName.setText(R.string.quick_gesture_most_used);
                    }

                } else if (mCurrentGestureType == GType.MostUsedLayout) {
                    if (mOrientation == Orientation.Left) {
                        mCurrentGestureType = GType.SwitcherLayout;
                        mSwitcherLayout.setVisibility(View.VISIBLE);
                        mDymicLayout.setVisibility(View.GONE);
                        mMostUsedLayout.setVisibility(View.GONE);
                        mTvCurName.setText(R.string.quick_gesture_switcher);
                    } else {
                        mCurrentGestureType = GType.SwitcherLayout;
                        mSwitcherLayout.setVisibility(View.VISIBLE);
                        mDymicLayout.setVisibility(View.GONE);
                        mMostUsedLayout.setVisibility(View.GONE);
                        mTvCurName.setText(R.string.quick_gesture_switcher);
                    }

                } else if (mCurrentGestureType == GType.SwitcherLayout) {
                    if (mOrientation == Orientation.Left) {
                        mCurrentGestureType = GType.DymicLayout;
                        mDymicLayout.setVisibility(View.VISIBLE);
                        mMostUsedLayout.setVisibility(View.GONE);
                        mSwitcherLayout.setVisibility(View.GONE);
                        mCornerTabs.snapSwitcher2Dynamic();
                        mTvCurName.setText(R.string.quick_gesture_dynamic);
                    } else {
                        mCurrentGestureType = GType.DymicLayout;
                        mDymicLayout.setVisibility(View.VISIBLE);
                        mMostUsedLayout.setVisibility(View.GONE);
                        mSwitcherLayout.setVisibility(View.GONE);
                        mCornerTabs.snapSwitcher2Dynamic();
                        mTvCurName.setText(R.string.quick_gesture_dynamic);
                    }
                }
                mRotateDegree = 0;
                mCornerTabs.resetLayout();
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

    public void fillGestureItem(GType type, List<BaseInfo> infos) {
        if (infos == null) {
            LeoLog.e(TAG, "fillGestureItem, infos is null");
            return;
        }
        AppleWatchLayout targetLayout = null;
        if (type == GType.DymicLayout) {
            targetLayout = mDymicLayout;
            // fillDynamicItem(targetLayout, infos, 0);
        } else if (type == GType.MostUsedLayout) {
            targetLayout = mMostUsedLayout;
            // fillItem(targetLayout, infos);
        } else if (type == GType.SwitcherLayout) {
            targetLayout = mSwitcherLayout;
            infos = fixInfoRight(infos);
        }
        targetLayout.fillItems(infos);
    }

    private List<BaseInfo> fixInfoRight(List<BaseInfo> infos) {
        QuickSwitcherInfo sInfo = null;
        List<BaseInfo> mSwitchList = new ArrayList<BaseInfo>();
        GestureItemView tv = makeGestureItem();
        for (int i = 0; i < infos.size(); i++) {
            sInfo = (QuickSwitcherInfo) infos.get(i);
            if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext).getLabelFromName(
                    QuickSwitchManager.BLUETOOTH))) {
                // check 蓝牙状态
                checkBlueToothStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.FLASHLIGHT))) {
                // 手电筒状态
                checkFlashLightStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.WLAN))) {
                // Wifi状态
                checkWlanStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.CRAME))) {
                // Crame状态
                checkCrameStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.SOUND))) {
                // Sound状态
                checkSoundStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.LIGHT))) {
                // 亮度状态
                checkLightStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.SPEEDUP))) {
                // 加速
                checkSpeedUpStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.CHANGEMODE))) {
                // 情景模式切换
                checkChangeMode(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.SWITCHSET))) {
                // 手势设置
                checkSwitchSet(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.SETTING))) {
                // 系统设置
                checkSetting(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.GPS))) {
                // GPS
                checkGPS(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.FLYMODE))) {
                // 飞行模式
                checkFlyMode(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.ROTATION))) {
                // 屏幕旋转
                checkRotation(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.MOBILEDATA))) {
                // 移动数据
                checkMobileData(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.HOME))) {
                // 桌面
                checkHome(sInfo, mGetIcon, tv);
            } else if (sInfo.label.equals(QuickSwitchManager.getInstance(mContext)
                    .getLabelFromName(QuickSwitchManager.XUKUANG))) {
                // 虚框
                // checkXuKuang(sInfo, mGetIcon, tv);
            }
            mSwitchList.add(sInfo);
        }
        return mSwitchList;
    }

    public void fillDynamicItem(AppleWatchLayout targetLayout,
            List<? extends BaseInfo> itemInfos, int businessIndes) {
        if (targetLayout != null) {
            targetLayout.removeAllViews();
            GestureItemView gestureItem = null;
            AppleWatchLayout.LayoutParams lp = null;
            BaseInfo info = null;
            int iconSize = targetLayout.getIconSize();
            List<BaseInfo> infos = (List<BaseInfo>) itemInfos;
            // 快捷手势未读短信提醒
            boolean isShowMsmTip = AppMasterPreference.getInstance(getContext())
                    .getSwitchOpenNoReadMessageTip();
            boolean isShowCallLogTip = AppMasterPreference.getInstance(getContext())
                    .getSwitchOpenRecentlyContact();
            boolean isShowPrivacyContactTip = AppMasterPreference.getInstance(getContext())
                    .getSwitchOpenPrivacyContactMessageTip();
            if (isShowMsmTip) {
                if (QuickGestureManager.getInstance(mContext).mMessages != null
                        && QuickGestureManager.getInstance(mContext).mMessages.size() > 0) {
                    for (MessageBean message : QuickGestureManager.getInstance(mContext).mMessages) {
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
                if (QuickGestureManager.getInstance(mContext).mCallLogs != null
                        && QuickGestureManager.getInstance(mContext).mCallLogs.size() > 0) {
                    for (ContactCallLog baseInfo : QuickGestureManager.getInstance(mContext).mCallLogs) {
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
                if (LockManager.getInstatnce().isShowPrivacyCallLog
                        || LockManager.getInstatnce().isShowPrivacyMsm) {
                    QuickGestureContactTipInfo item = new QuickGestureContactTipInfo();
                    item.icon = getContext().getResources().getDrawable(
                            R.drawable.gesture_system);
                    item.label = mContext.getResources().getString(
                            R.string.pg_appmanager_quick_gesture_privacy_contact_tip_lable);
                    item.isShowReadTip = true;
                    infos.add(businessIndes, item);
                }
            }

            if (infos.size() > 13) {
                infos = infos.subList(0, 13);
            }

            for (int i = 0; i < infos.size(); i++) {
                gestureItem = makeGestureItem();
                lp = new AppleWatchLayout.LayoutParams(
                        targetLayout.getItemSize(), targetLayout.getItemSize());
                lp.position = i;
                gestureItem.setGravity(Gravity.CENTER_HORIZONTAL);
                gestureItem.setLayoutParams(lp);
                info = infos.get(i);
                if (info instanceof QuickGestureContactTipInfo) {
                    if (((QuickGestureContactTipInfo) info).isShowReadTip) {
                        gestureItem.showReadTip();
                    } else {
                        gestureItem.cancelShowReadTip();
                    }
                } else if (info instanceof MessageBean) {
                    if (((MessageBean) info).isShowReadTip) {
                        gestureItem.showReadTip();
                    } else {
                        gestureItem.cancelShowReadTip();
                    }
                } else if (info instanceof ContactCallLog) {
                    if (((ContactCallLog) info).isShowReadTip) {
                        gestureItem.showReadTip();
                    } else {
                        gestureItem.cancelShowReadTip();
                    }
                }

                gestureItem.setItemName(info.label);
                gestureItem.setItemIcon(info.icon);
                gestureItem.setDecorateAction(new EventAction(getContext(), info.eventNumber));
                gestureItem.setTag(info);
                targetLayout.addView(gestureItem);
            }
        }
    }

    // public void fillSwitchItem(AppleWatchLayout targetLayout, List<? extends
    // BaseInfo> infos) {
    // if (targetLayout != null) {
    // targetLayout.removeAllViews();
    // GestureItemView gestureItem = null;
    // AppleWatchLayout.LayoutParams lp = null;
    // QuickSwitcherInfo sInfo = null;
    // int iconSize = targetLayout.getIconSize();
    // for (int i = 0; i < infos.size(); i++) {
    // if (i >= 13) {
    // break;
    // }
    // sInfo = (QuickSwitcherInfo) infos.get(i);
    // gestureItem = makeGestureItem();
    // lp = new AppleWatchLayout.LayoutParams(
    // targetLayout.getItemSize(), targetLayout.getItemSize());
    // lp.position = sInfo.gesturePosition;
    // gestureItem.setGravity(Gravity.CENTER_HORIZONTAL);
    // gestureItem.setLayoutParams(lp);
    // gestureItem.setItemName(sInfo.label);
    // if (sInfo.iDentiName.equals(QuickSwitchManager.BLUETOOTH)) {
    // // check 蓝牙状态
    // checkBlueToothStatus(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.FLASHLIGHT)) {
    // // 手电筒状态
    // checkFlashLightStatus(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.WLAN)) {
    // // Wifi状态
    // checkWlanStatus(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.CRAME)) {
    // // Crame状态
    // checkCrameStatus(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.SOUND)) {
    // // Sound状态
    // checkSoundStatus(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.LIGHT)) {
    // // 亮度状态
    // checkLightStatus(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.SPEEDUP)) {
    // // 加速
    // checkSpeedUpStatus(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.CHANGEMODE)) {
    // // 情景模式切换
    // checkChangeMode(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.SWITCHSET)) {
    // // 手势设置
    // checkSwitchSet(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.SETTING)) {
    // // 系统设置
    // checkSetting(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.GPS)) {
    // // GPS
    // checkGPS(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.FLYMODE)) {
    // // 飞行模式
    // checkFlyMode(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.ROTATION)) {
    // // 屏幕旋转
    // checkRotation(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.MOBILEDATA)) {
    // // 移动数据
    // checkMobileData(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.HOME)) {
    // // 桌面
    // checkHome(sInfo, iconSize, gestureItem);
    // } else if (sInfo.iDentiName.equals(QuickSwitchManager.XUKUANG)) {
    // // 虚框
    // checkXuKuang(sInfo, iconSize, gestureItem);
    // }
    // if (sInfo.eventNumber > 0) {
    // gestureItem.setDecorateAction(new EventAction(getContext(),
    // sInfo.eventNumber));
    // }
    // gestureItem.setTag(sInfo);
    // targetLayout.addView(gestureItem);
    // }
    // }
    // }

    private void checkXuKuang(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0]);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkHome(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0]);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkMobileData(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            if (QuickSwitchManager.checkMoblieData()) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0]);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1]);
            }
        } else {
            if (QuickSwitchManager.checkMoblieData()) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }
    }

    private void checkRotation(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            if (QuickSwitchManager.checkRotation()) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0]);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1]);
            }
        } else {
            if (QuickSwitchManager.checkRotation()) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }
    }

    private void checkFlyMode(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            if (QuickSwitchManager.checkFlyMode()) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0]);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1]);
            }
        } else {
            if (QuickSwitchManager.checkFlyMode()) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }
    }

    private void checkGPS(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            if (QuickSwitchManager.checkGps()) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0]);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1]);
            }
        } else {
            if (QuickSwitchManager.checkGps()) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }
    }

    private void checkSetting(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0]);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkSwitchSet(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0]);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkChangeMode(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0]);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkSpeedUpStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0]);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkLightStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            if (QuickSwitchManager.checkLight() == QuickSwitchManager.LIGHT_AUTO) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0]);
            } else if (QuickSwitchManager.checkLight() == QuickSwitchManager.LIGHT_NORMAL) {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1]);
            } else if (QuickSwitchManager.checkLight() == QuickSwitchManager.LIGHT_50_PERCENT) {
                sInfo.icon = sInfo.switchIcon[2];
                tv.setItemIcon(sInfo.switchIcon[2]);
            } else if (QuickSwitchManager.checkLight() == QuickSwitchManager.LIGHT_100_PERCENT) {
                sInfo.icon = sInfo.switchIcon[3];
                tv.setItemIcon(sInfo.switchIcon[3]);
            } else {
                // err
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1]);
            }
        } else {
            if (QuickSwitchManager.checkLight() == QuickSwitchManager.LIGHT_AUTO) {
                sInfo.icon = sInfo.switchIcon[0];
            } else if (QuickSwitchManager.checkLight() == QuickSwitchManager.LIGHT_NORMAL) {
                sInfo.icon = sInfo.switchIcon[1];
            } else if (QuickSwitchManager.checkLight() == QuickSwitchManager.LIGHT_50_PERCENT) {
                sInfo.icon = sInfo.switchIcon[2];
            } else if (QuickSwitchManager.checkLight() == QuickSwitchManager.LIGHT_100_PERCENT) {
                sInfo.icon = sInfo.switchIcon[3];
            } else {
                // err
                sInfo.icon = sInfo.switchIcon[1];
            }
        }

    }

    private void checkSoundStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            if (QuickSwitchManager.checkSound() == QuickSwitchManager.mSound) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0]);
            } else if (QuickSwitchManager.checkSound() == QuickSwitchManager.mQuite) {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1]);
            } else {
                sInfo.icon = sInfo.switchIcon[2];
                tv.setItemIcon(sInfo.switchIcon[2]);
            }
        } else {
            if (QuickSwitchManager.checkSound() == QuickSwitchManager.mSound) {
                sInfo.icon = sInfo.switchIcon[0];
            } else if (QuickSwitchManager.checkSound() == QuickSwitchManager.mQuite) {
                sInfo.icon = sInfo.switchIcon[1];
            } else {
                sInfo.icon = sInfo.switchIcon[2];
            }
        }
    }

    private void checkCrameStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0]);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkWlanStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            if (QuickSwitchManager.checkWlan()) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0]);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1]);
            }
        } else {
            if (QuickSwitchManager.checkWlan()) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }

    }

    private void checkFlashLightStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            if (QuickSwitchManager.checkFlashLight()) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0]);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1]);
            }
        } else {
            if (QuickSwitchManager.checkFlashLight()) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }

    }

    private void checkBlueToothStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            if (QuickSwitchManager.checkBlueTooth()) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0]);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1]);
            }
        } else {
            if (QuickSwitchManager.checkBlueTooth()) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }

    }

    public void fillItem(AppleWatchLayout targetLayout, List<? extends BaseInfo> infos) {
        if (targetLayout != null) {
            targetLayout.removeAllViews();
            GestureItemView gestureItem = null;
            AppleWatchLayout.LayoutParams lp = null;
            BaseInfo info = null;
            for (int i = 0; i < infos.size(); i++) {
                if (i >= 13) {
                    break;
                }
                gestureItem = makeGestureItem();
                lp = new AppleWatchLayout.LayoutParams(
                        targetLayout.getItemSize(), targetLayout.getItemSize());
                lp.position = i;
                gestureItem.setGravity(Gravity.CENTER_HORIZONTAL);
                gestureItem.setLayoutParams(lp);
                info = infos.get(i);
                gestureItem.setItemName(info.label);
                gestureItem.setItemIcon(info.icon);
                if (info.eventNumber > 0) {
                    gestureItem.setDecorateAction(new EventAction(getContext(), info.eventNumber));
                }
                gestureItem.setTag(info);
                targetLayout.addView(gestureItem);
            }
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
        Animator animationx = ObjectAnimator.ofFloat(this, "scaleX", 0.0f,
                1.1f,
                1.0f);
        Animator animationy = ObjectAnimator.ofFloat(this, "scaleY", 0.0f,
                1.1f,
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
                Activity activity = (Activity) AppleWatchContainer.this.getContext();
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

    public GestureItemView makeGestureItem() {
        LayoutInflater inflate = LayoutInflater.from(getContext());
        GestureItemView item = (GestureItemView) inflate.inflate(R.layout.gesture_item, null);
        return item;
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

    public void checkStatus(QuickSwitcherInfo info) {
        GestureItemView tv = null;
        int iconSize = mSwitcherLayout.getIconSize();
        if (info.label.equals(QuickSwitchManager.getInstance(mContext).getLabelFromName(
                QuickSwitchManager.BLUETOOTH))) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkBlueToothStatus(info, iconSize, tv);
        } else if (info.label.equals(QuickSwitchManager.getInstance(mContext).getLabelFromName(
                QuickSwitchManager.FLASHLIGHT))) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkFlashLightStatus(info, iconSize, tv);
        } else if (info.label.equals(QuickSwitchManager.getInstance(mContext).getLabelFromName(
                QuickSwitchManager.WLAN))) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkWlanStatus(info, iconSize, tv);
        } else if (info.label.equals(QuickSwitchManager.getInstance(mContext).getLabelFromName(
                QuickSwitchManager.SOUND))) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkSoundStatus(info, iconSize, tv);
        } else if (info.label.equals(QuickSwitchManager.getInstance(mContext).getLabelFromName(
                QuickSwitchManager.LIGHT))) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkLightStatus(info, iconSize, tv);
        } else if (info.label.equals(QuickSwitchManager.getInstance(mContext).getLabelFromName(
                QuickSwitchManager.ROTATION))) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkRotation(info, iconSize, tv);
        } else if (info.label.equals(QuickSwitchManager.getInstance(mContext).getLabelFromName(
                QuickSwitchManager.MOBILEDATA))) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkMobileData(info, iconSize, tv);
        } else if (info.label.equals(QuickSwitchManager.getInstance(mContext).getLabelFromName(
                QuickSwitchManager.SPEEDUP))) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            speedUp(info, iconSize, tv);
        }
    }

    private void speedUp(QuickSwitcherInfo info, int iconSize, GestureItemView tv) {
        // first - change to no roket icon
        info.switchIcon[1].setBounds(0, 0, iconSize, iconSize);
        tv.setItemIcon(info.switchIcon[1]);
        // second - show roket int icon place
        int mLayoutTop = mSwitcherLayout.getTop();
        int mLayoutBottom = mSwitcherLayout.getBottom();
        int mRocketWidth = tv.getWidth();
        int mRocketHeight = tv.getHeight();
        int mRocketX = (int) tv.getX() + mRocketWidth / 2;
        int mRocketY = (int) tv.getY() + mRocketHeight / 2 + mLayoutTop;

        LeoLog.d("AppleWatchContainer", " mRocketX : " + mRocketX
                + " ;  mRocketY : " + mRocketY);
        mPopupActivity.rockeyAnimation(tv, mLayoutBottom, mRocketX, mRocketY, info);
    }

    public void setRocket(QuickGesturePopupActivity quickGesturePopupActivity) {
        this.mPopupActivity = quickGesturePopupActivity;
    }

    public void makeNormalIcon(QuickSwitcherInfo info) {
        // show Toast
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.toast_self_make, null);
        Toast toast = new Toast(mContext);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 150);
        toast.show();

        // make Normal Icon
        GestureItemView tv = null;
        int iconSize = mSwitcherLayout.getIconSize();
        if (info.label.equals(QuickSwitchManager.getInstance(mContext).getLabelFromName(
                QuickSwitchManager.SPEEDUP))) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            info.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(info.switchIcon[0]);
        }
    }

}
