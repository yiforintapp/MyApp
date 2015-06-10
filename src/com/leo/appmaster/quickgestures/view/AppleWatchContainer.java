
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
import android.net.wifi.WifiManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.MessageBean;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickGestureContactTipInfo;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.AppleWatchLayout.Direction;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.TextFormater;

@SuppressLint("InflateParams")
public class AppleWatchContainer extends FrameLayout {

    public static final String TAG = "AppleWatchQuickGestureContainer";
    private static final int mGetIcon = -1;
    public static final int mLastTimeDymic = 1;
    public static final int mLastTimeMost = 2;
    public static final int mLastTimeSwitch = 3;
    private AppMasterPreference mPref;
    private int mCurrentLayout = 3;
    private List<QuickSwitcherInfo> mSwitchList;
    private WifiManager mWifimanager;

    public static enum Orientation {
        Left, Right;
    }

    public static enum GType {
        DymicLayout, MostUsedLayout, SwitcherLayout;
    }

    private AppleWatchLayout mDymicLayout, mMostUsedLayout, mSwitcherLayout;
    private AppleWatchTabs mCornerTabs;
    private TextView mTvCurName;
    private GType mCurrentGestureType = GType.SwitcherLayout;
    private Orientation mOrientation = Orientation.Left;
    private GestureDetector mGesDetector;
    private Orientation mShowOrientation = Orientation.Left;
    private ImageView mRockey, mPIngtai, mYun;

    private float mSelfHeight;
    private float mTouchDownX, mTouchDownY;
    private float mRotateDegree;
    private volatile boolean mEditing;
    private boolean mSnaping;
    private boolean isClean = false;
    private int mFullRotateDuration = 300;
    private int mStartAngle = 40;
    private ProcessCleaner mCleaner;
    private long mLastUsedMem;
    private long mCleanMem;
    private boolean isAnimating;
    private boolean mHasRelayout;
    private boolean mMoving;
    protected long mStartShowingTime;

    public AppleWatchContainer(Context context) {
        super(context);
    }

    public AppleWatchContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GestureDirection);

        mPref = AppMasterPreference.getInstance(context);
        mCurrentLayout = mPref.getLastTimeLayout();
        LeoLog.d("AppleWatchContainer", "刚来！show 出的是：" + mCurrentLayout);
        makeNowLayout();

        // 清理内存
        mCleaner = ProcessCleaner.getInstance(context);
        mLastUsedMem = mCleaner.getUsedMem();
        // clean
        mCleaner.tryClean(mContext);
        long curUsedMem = mCleaner.getUsedMem();
        mCleanMem = Math.abs(mLastUsedMem - curUsedMem);
        System.gc();

        int derictor = typedArray.getInt(R.styleable.GestureDirection_Direction, 0);
        if (derictor == 0) {
            mOrientation = Orientation.Left;
        } else {
            mOrientation = Orientation.Right;
        }
        typedArray.recycle();
        init();
    }

    private void makeNowLayout() {
        if (mCurrentLayout == mLastTimeDymic) {
            mCurrentGestureType = GType.DymicLayout;
        } else if (mCurrentLayout == mLastTimeMost) {
            mCurrentGestureType = GType.MostUsedLayout;
        } else {
            mCurrentGestureType = GType.SwitcherLayout;
        }
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
                        if (mTouchDownY > mDymicLayout.getBottom()) {
                            if (!mEditing) {
                                leaveEditMode();
                            }
                            if (velocityX > 300) {
                                snapToPrevious();
                                return true;
                            }
                            if (velocityX < -300) {
                                snapToNext();
                                return true;
                            }
                        } else if (mTouchDownY > mDymicLayout.getTop()) {
                            if (!mEditing) {
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
                        if (!mEditing && !mMoving) {
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

    public void setShowOrientation(Orientation o) {
        mShowOrientation = o;
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
        mSwitcherLayout.onLeaveEditMode();
        mMostUsedLayout.onLeaveEditMode();
        mDymicLayout.onLeaveEditMode();
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
        mSelfHeight = getMeasuredHeight();
    }

    @Override
    protected void onFinishInflate() {
        mTvCurName = (TextView) findViewById(R.id.tv_type_name);
        mCornerTabs = (AppleWatchTabs) findViewById(R.id.applewatchtab);
        mDymicLayout = (AppleWatchLayout) findViewById(R.id.qg_dymic_layout);
        mDymicLayout.mMyType = GType.DymicLayout;
        mMostUsedLayout = (AppleWatchLayout) findViewById(R.id.qg_mostused_layout);
        mMostUsedLayout.mMyType = GType.MostUsedLayout;
        mSwitcherLayout = (AppleWatchLayout) findViewById(R.id.qg_switcher_layout);
        mSwitcherLayout.mMyType = GType.SwitcherLayout;

        mRockey = (ImageView) findViewById(R.id.iv_rocket);
        mPIngtai = (ImageView) findViewById(R.id.iv_pingtai);
        mYun = (ImageView) findViewById(R.id.iv_yun);

        if (mCurrentGestureType == GType.DymicLayout) {
            mTvCurName.setText(R.string.quick_gesture_dynamic);
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            mTvCurName.setText(R.string.quick_gesture_most_used);
        } else {
            mTvCurName.setText(R.string.quick_gesture_switcher);
        }

        showGestureLayout(mCurrentGestureType);
        super.onFinishInflate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSnaping)
            return false;
        AppleWatchLayout gestureLayout = null;
        if (mCurrentGestureType == GType.DymicLayout) {
            gestureLayout = mDymicLayout;
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            gestureLayout = mMostUsedLayout;
        } else {
            gestureLayout = mSwitcherLayout;
        }
        if (isAnimating || gestureLayout.isSnapping())
            return false;

        long curTime = System.currentTimeMillis();
        if ((curTime - mStartShowingTime) < 500) {
            return false;
        }

        mGesDetector.onTouchEvent(event);
        float moveX, moveY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = event.getX();
                mTouchDownY = event.getY();
                if (mEditing) {
                    if (!gestureLayout.checkActionDownInEditing(event.getX(), event.getY()
                            - gestureLayout.getTop())) {
                        if (mTouchDownY > mDymicLayout.getBottom()) {
                            onTouchDown();
                        }
                    }
                } else {
                    if (mTouchDownY > mDymicLayout.getBottom()) {
                        onTouchDown();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mEditing) {
                    if (mTouchDownY < mDymicLayout.getBottom()) {
                        break;
                    }
                }
                moveX = event.getX();
                moveY = event.getY();

                // 下拉通知栏，finish
                if (mTouchDownY >= 0 && mTouchDownY < 70) {
                    if (moveY - mTouchDownY > 70) {
                        Activity activity = (Activity) AppleWatchContainer.this.getContext();
                        activity.finish();
                    }
                }

                if (Math.abs(moveX - mTouchDownX) > DipPixelUtil.dip2px(getContext(), 10)) {
                    mMoving = true;
                    if (mTouchDownY >= mDymicLayout.getTop()
                            && mTouchDownY <= mDymicLayout.getBottom()) {
                        onTouchMoveTranslate(moveX - mTouchDownX, moveY - mTouchDownY);
                    }

                    if (mTouchDownY > mDymicLayout.getBottom()) {
                        if (mDymicLayout.mHasFillExtraItems
                                && mMostUsedLayout.mHasFillExtraItems
                                && mSwitcherLayout.mHasFillExtraItems) {
                            onTouchMoveRotate(moveX, moveY);
                        }
                    }
                }
                // }
                break;
            case MotionEvent.ACTION_UP:
                mMoving = false;
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
        if (mEditing) {
            mEditing = false;
            leaveEditMode();
        }
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
        if (mEditing) {
            mEditing = false;
            leaveEditMode();
        }
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

    public void fillGestureItem(GType type, List<BaseInfo> infos, boolean loadExtra) {
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
        targetLayout.fillItems(infos, loadExtra);
    }

    private List<BaseInfo> fixInfoRight(List<BaseInfo> infos) {
        QuickSwitcherInfo sInfo = null;
        List<BaseInfo> mSwitchList = new ArrayList<BaseInfo>();
        GestureItemView tv = makeGestureItem();
        for (int i = 0; i < infos.size(); i++) {
            sInfo = (QuickSwitcherInfo) infos.get(i);
            if (sInfo.swtichIdentiName.equals(QuickSwitchManager.BLUETOOTH)) {
                // check 蓝牙状态
                checkBlueToothStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.FLASHLIGHT)) {
                // 手电筒状态
                checkFlashLightStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.WLAN)) {
                // Wifi状态
                checkWlanStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.CRAME)) {
                // Crame状态
                checkCrameStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.SOUND)) {
                // Sound状态
                checkSoundStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.LIGHT)) {
                // 亮度状态
                checkLightStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.SPEEDUP)) {
                // 加速
                checkSpeedUpStatus(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.SWITCHSET)) {
                // 手势设置
                checkSwitchSet(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.SETTING)) {
                // 系统设置
                checkSetting(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.GPS)) {
                // GPS
                checkGPS(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.FLYMODE)) {
                // 飞行模式
                checkFlyMode(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.ROTATION)) {
                // 屏幕旋转
                checkRotation(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.MOBILEDATA)) {
                // 移动数据
                checkMobileData(sInfo, mGetIcon, tv);
            } else if (sInfo.swtichIdentiName.equals(QuickSwitchManager.HOME)) {
                // 桌面
                checkHome(sInfo, mGetIcon, tv);
            }
            mSwitchList.add(sInfo);
        }
        return mSwitchList;
    }

    public void fillDynamicItem(AppleWatchLayout targetLayout,
            List<BaseInfo> itemInfos, int businessIndes) {
        if (targetLayout != null) {
            targetLayout.removeAllViews();
            GestureItemView gestureItem = null;
            AppleWatchLayout.LayoutParams lp = null;
            BaseInfo info = null;
            List<BaseInfo> infos = itemInfos;
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

            if (infos.size() > 11) {
                infos = infos.subList(0, 11);
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
                gestureItem.setItemIcon(info.icon, true);
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

    // private void checkXuKuang(QuickSwitcherInfo sInfo, int iconSize,
    // GestureItemView tv) {
    // if (iconSize != mGetIcon) {
    // sInfo.icon = sInfo.switchIcon[0];
    // tv.setItemIcon(sInfo.switchIcon[0]);
    // } else {
    // sInfo.icon = sInfo.switchIcon[0];
    // }
    // }

    private void checkHome(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkMobileData(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        boolean DataIsOpen = QuickSwitchManager.checkMoblieData();
        if (iconSize != mGetIcon) {
            if (DataIsOpen) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0], false);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1], false);
            }
        } else {
            if (DataIsOpen) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }
    }

    private void checkRotation(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        boolean isOpen = QuickSwitchManager.checkRotation();
        if (iconSize != mGetIcon) {
            if (isOpen) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0], false);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1], false);
            }
        } else {
            if (isOpen) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }
    }

    private void checkFlyMode(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        boolean isOpen = QuickSwitchManager.checkFlyMode();
        if (iconSize != mGetIcon) {
            if (isOpen) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0], false);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1], false);
            }
        } else {
            if (isOpen) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }
    }

    private void checkGPS(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        boolean isOpen = QuickSwitchManager.checkGps();
        if (iconSize != mGetIcon) {
            if (isOpen) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0], false);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1], false);
            }
        } else {
            if (isOpen) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }
    }

    private void checkSetting(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkSwitchSet(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkChangeMode(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkSpeedUpStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkLightStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        int mLightStatus = QuickSwitchManager.checkLight();
        if (iconSize != mGetIcon) {
            if (mLightStatus == QuickSwitchManager.LIGHT_AUTO) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0], false);
            } else if (mLightStatus == QuickSwitchManager.LIGHT_NORMAL) {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1], false);
            } else if (mLightStatus == QuickSwitchManager.LIGHT_50_PERCENT) {
                sInfo.icon = sInfo.switchIcon[2];
                tv.setItemIcon(sInfo.switchIcon[2], false);
            } else if (mLightStatus == QuickSwitchManager.LIGHT_100_PERCENT) {
                sInfo.icon = sInfo.switchIcon[3];
                tv.setItemIcon(sInfo.switchIcon[3], false);
            } else {
                // err
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1], false);
            }
        } else {
            if (mLightStatus == QuickSwitchManager.LIGHT_AUTO) {
                sInfo.icon = sInfo.switchIcon[0];
            } else if (mLightStatus == QuickSwitchManager.LIGHT_NORMAL) {
                sInfo.icon = sInfo.switchIcon[1];
            } else if (mLightStatus == QuickSwitchManager.LIGHT_50_PERCENT) {
                sInfo.icon = sInfo.switchIcon[2];
            } else if (mLightStatus == QuickSwitchManager.LIGHT_100_PERCENT) {
                sInfo.icon = sInfo.switchIcon[3];
            } else {
                // err
                sInfo.icon = sInfo.switchIcon[1];
            }
        }

    }

    private void checkSoundStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        int mSoundStatus = QuickSwitchManager.checkSound();
        if (iconSize != mGetIcon) {
            if (mSoundStatus == QuickSwitchManager.mSound) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0], false);
            } else if (mSoundStatus == QuickSwitchManager.mQuite) {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1], false);
            } else {
                sInfo.icon = sInfo.switchIcon[2];
                tv.setItemIcon(sInfo.switchIcon[2], false);
            }
        } else {
            if (mSoundStatus == QuickSwitchManager.mSound) {
                sInfo.icon = sInfo.switchIcon[0];
            } else if (mSoundStatus == QuickSwitchManager.mQuite) {
                sInfo.icon = sInfo.switchIcon[1];
            } else {
                sInfo.icon = sInfo.switchIcon[2];
            }
        }
    }

    private void checkCrameStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        if (iconSize != mGetIcon) {
            sInfo.icon = sInfo.switchIcon[0];
            tv.setItemIcon(sInfo.switchIcon[0], false);
        } else {
            sInfo.icon = sInfo.switchIcon[0];
        }
    }

    private void checkWlanStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        boolean isOpen = QuickSwitchManager.checkWlan();
        if (iconSize != mGetIcon) {
            if (isOpen) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0], false);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1], false);
            }
        } else {
            if (isOpen) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }

    }

    private void checkFlashLightStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        boolean isOpen = QuickSwitchManager.checkFlashLight();
        if (iconSize != mGetIcon) {
            if (isOpen) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0], false);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1], false);
            }
        } else {
            if (isOpen) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }

    }

    private void checkBlueToothStatus(QuickSwitcherInfo sInfo, int iconSize, GestureItemView tv) {
        boolean isOpen = QuickSwitchManager.checkBlueTooth();
        if (iconSize != mGetIcon) {
            if (isOpen) {
                sInfo.icon = sInfo.switchIcon[0];
                tv.setItemIcon(sInfo.switchIcon[0], false);
            } else {
                sInfo.icon = sInfo.switchIcon[1];
                tv.setItemIcon(sInfo.switchIcon[1], false);
            }
        } else {
            if (isOpen) {
                sInfo.icon = sInfo.switchIcon[0];
            } else {
                sInfo.icon = sInfo.switchIcon[1];
            }
        }

    }

    public GType getCurrentGestureType() {
        return mCurrentGestureType;
    }

    public float getRotateDegree() {
        return mRotateDegree;
    }

    public void showOpenAnimation(final Runnable run) {
        final long a = System.currentTimeMillis();

        int direction = mShowOrientation == Orientation.Left ? 0 : 2;
        final AppleWatchLayout targetLayout;
        if (mCurrentGestureType == GType.DymicLayout) {
            targetLayout = mDymicLayout;
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            targetLayout = mMostUsedLayout;
        } else {
            targetLayout = mSwitcherLayout;
        }

        ObjectAnimator tabAnimator = ObjectAnimator.ofFloat(mCornerTabs, "translationY",
                mCornerTabs.getHeight(), 0);
        tabAnimator.setDuration(300);
        tabAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                mCornerTabs.setVisibility(View.VISIBLE);
            };
        });
        ObjectAnimator titleAnimator = ObjectAnimator.ofFloat(mTvCurName, "alpha", 0, 1)
                .setDuration(640);
        AnimatorSet iconAnimatorSet = targetLayout.makeIconShowAnimator(direction);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(tabAnimator, titleAnimator, iconAnimatorSet);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mStartShowingTime = System.currentTimeMillis();
                isAnimating = true;
                // targetLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.i("time", System.currentTimeMillis() - a + " ");
                isAnimating = false;
                targetLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        targetLayout.fillExtraChildren();
                    }
                });
                run.run();
            }
        });
        set.start();
    }

    public void showCloseAnimation() {
        final long a = System.currentTimeMillis();

        int direction = mShowOrientation == Orientation.Left ? 0 : 2;
        AppleWatchLayout targetLayout;
        if (mCurrentGestureType == GType.DymicLayout) {
            targetLayout = mDymicLayout;
            LeoLog.d("AppleWatchContainer", "关闭是 : mDymicLayout");
            mPref.setLastTimeLayout(mLastTimeDymic);
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            targetLayout = mMostUsedLayout;
            LeoLog.d("AppleWatchContainer", "关闭是 : mLastTimeMost");
            mPref.setLastTimeLayout(mLastTimeMost);
        } else {
            targetLayout = mSwitcherLayout;
            LeoLog.d("AppleWatchContainer", "关闭是 : mLastTimeSwitch");
            mPref.setLastTimeLayout(mLastTimeSwitch);
        }

        ObjectAnimator tabAnimator = ObjectAnimator.ofFloat(mCornerTabs, "translationY",
                0, mCornerTabs.getHeight());
        tabAnimator.setDuration(250);
        ObjectAnimator titleAnimator = ObjectAnimator.ofFloat(mTvCurName, "alpha", 1, 0)
                .setDuration(540);
        AnimatorSet iconAnimatorSet = targetLayout.makeIconCloseAnimator(direction);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(tabAnimator, titleAnimator, iconAnimatorSet);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Activity activity = (Activity) AppleWatchContainer.this.getContext();
                FloatWindowHelper.mGestureShowing = false;
                activity.finish();
                isAnimating = false;
                super.onAnimationEnd(animation);
                Log.i("close time", System.currentTimeMillis() - a + " ");
            }
        });
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
            mSwitcherLayout.setVisibility(View.INVISIBLE);
            mMostUsedLayout.setVisibility(View.INVISIBLE);
        } else if (type == GType.MostUsedLayout) {
            mDymicLayout.setVisibility(View.INVISIBLE);
            mMostUsedLayout.setVisibility(View.VISIBLE);
            mSwitcherLayout.setVisibility(View.INVISIBLE);
        } else if (type == GType.SwitcherLayout) {
            mSwitcherLayout.setVisibility(View.VISIBLE);
            mDymicLayout.setVisibility(View.INVISIBLE);
            mMostUsedLayout.setVisibility(View.INVISIBLE);
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
        if (info.swtichIdentiName.equals(QuickSwitchManager.BLUETOOTH)) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkBlueToothStatus(info, iconSize, tv);
        } else if (info.swtichIdentiName.equals(QuickSwitchManager.FLASHLIGHT)) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkFlashLightStatus(info, iconSize, tv);
        } else if (info.swtichIdentiName.equals(QuickSwitchManager.WLAN)) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkWlanStatus(info, iconSize, tv);
        } else if (info.swtichIdentiName.equals(QuickSwitchManager.SOUND)) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkSoundStatus(info, iconSize, tv);
        } else if (info.swtichIdentiName.equals(QuickSwitchManager.LIGHT)) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkLightStatus(info, iconSize, tv);
        } else if (info.swtichIdentiName.equals(QuickSwitchManager.ROTATION)) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkRotation(info, iconSize, tv);
        } else if (info.swtichIdentiName.equals(QuickSwitchManager.MOBILEDATA)) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            checkMobileData(info, iconSize, tv);
        } else if (info.swtichIdentiName.equals(QuickSwitchManager.SPEEDUP)) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            speedUp(info, iconSize, tv);
        }
    }

    private void speedUp(QuickSwitcherInfo info, int iconSize, GestureItemView tv) {
        isAnimating = true;
        // first - change to no roket icon
        info.switchIcon[1].setBounds(0, 0, iconSize, iconSize);
        tv.setItemIcon(info.switchIcon[1], false);
        // second - show roket in icon place
        int mLayoutTop = mSwitcherLayout.getTop();
        int mLayoutBottom = mSwitcherLayout.getBottom();
        int mRocketWidth = tv.getWidth();
        int mRocketHeight = tv.getHeight();
        int mRocketX = (int) tv.getX() + mRocketWidth / 2;
        int mRocketY = (int) tv.getY() + mRocketHeight / 2 + mLayoutTop;
        rockeyAnimation(tv, mLayoutBottom, mRocketX, mRocketY, info);
    }

    public void rockeyAnimation(GestureItemView tv, final int mLayoutBottom, int mRocketX,
            int mRocketY, final QuickSwitcherInfo info) {
        int smallRockeyX = mRocketX - mRockey.getWidth() / 2;
        int smallRockeyY = mRocketY - mRockey.getHeight() / 2;

        float iv_roketScaleX = mRockey.getScaleX();
        float iv_width = mRockey.getWidth() * iv_roketScaleX;
        float iv_height = mRockey.getHeight() * iv_roketScaleX;

        MarginLayoutParams margin = new MarginLayoutParams(mRockey.getLayoutParams());
        margin.width = (int) iv_width;
        margin.height = (int) iv_height;
        margin.setMargins(smallRockeyX, smallRockeyY, smallRockeyX + mRockey.getWidth(),
                smallRockeyY + mRockey.getHeight());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
        mRockey.setLayoutParams(layoutParams);
        mRockey.setVisibility(View.VISIBLE);

        ObjectAnimator turnBig = ObjectAnimator.ofFloat(mRockey, "scaleX", 1f, 1.3f);
        ObjectAnimator turnBig2 = ObjectAnimator.ofFloat(mRockey, "scaleY", 1f, 1.3f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(turnBig).with(turnBig2);
        animSet.setDuration(400);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPIngtai.setVisibility(View.VISIBLE);
                final int mScreenHeight = AppleWatchContainer.this.getHeight();
                int mScreenWidth = AppleWatchContainer.this.getWidth();
                int transY = (int) mPIngtai.getTranslationY();
                int mRockeyMoveX = mScreenWidth / 2
                        - (mRockey.getLeft() + mRockey.getWidth() / 2);
                int mRockeyMoveY = mLayoutBottom - mRockey.getHeight() - mRockey.getTop();
                ObjectAnimator moveToX = ObjectAnimator.ofFloat(mRockey, "translationX",
                        mRockey.getTranslationX(), mRockeyMoveX);
                ObjectAnimator moveToY = ObjectAnimator.ofFloat(mRockey, "translationY",
                        mRockey.getTranslationY(), mRockeyMoveY);
                ObjectAnimator pingtaiMoveToY = ObjectAnimator
                        .ofFloat(mPIngtai, "translationY", mScreenHeight, transY);
                AnimatorSet animMoveSet = new AnimatorSet();
                animMoveSet.play(moveToX).with(moveToY).with(pingtaiMoveToY);
                animMoveSet.setDuration(800);
                animMoveSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mYun.setVisibility(View.VISIBLE);
                        ObjectAnimator mRocketmoveToY = ObjectAnimator.ofFloat(mRockey,
                                "translationY",
                                mRockey.getTranslationY(), -mScreenHeight);
                        ObjectAnimator pingtaiMoveDownToY = ObjectAnimator
                                .ofFloat(mPIngtai, "translationY", mPIngtai.getTranslationY(),
                                        mScreenHeight);
                        ObjectAnimator yunComeOut = ObjectAnimator.ofFloat(mYun, "alpha", 0.0f,
                                1f);
                        ObjectAnimator yunLeave = ObjectAnimator.ofFloat(mYun, "alpha", 1.0f,
                                0.0f);
                        AnimatorSet animMoveGoSet = new AnimatorSet();
                        animMoveGoSet.play(mRocketmoveToY).with(pingtaiMoveDownToY)
                                .with(yunComeOut).before(yunLeave);
                        animMoveGoSet.setDuration(800);
                        animMoveGoSet.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // all return
                                mRockey.setVisibility(View.INVISIBLE);
                                mPIngtai.setVisibility(View.INVISIBLE);
                                ObjectAnimator turnSmall = ObjectAnimator.ofFloat(mRockey,
                                        "scaleX", 1.3f, 1.0f);
                                ObjectAnimator turnSmall2 = ObjectAnimator.ofFloat(mRockey,
                                        "scaleY", 1.3f, 1.0f);
                                ObjectAnimator returnX = ObjectAnimator.ofFloat(mRockey,
                                        "translationX", mRockey.getTranslationX(), 0);
                                ObjectAnimator returnY = ObjectAnimator.ofFloat(mRockey,
                                        "translationY", mRockey.getTranslationY(), 0);
                                ObjectAnimator pingtai_returnY = ObjectAnimator.ofFloat(mPIngtai,
                                        "translationY", mPIngtai.getTranslationY(), 0);
                                AnimatorSet returnAnimation = new AnimatorSet();
                                returnAnimation.play(turnSmall).with(turnSmall2).with(returnX)
                                        .with(returnY).with(pingtai_returnY);
                                returnAnimation.setDuration(200);
                                returnAnimation.start();
                                // make normal iCon
                                makeNormalIcon(info);
                            }
                        });
                        animMoveGoSet.start();
                    }
                });
                animMoveSet.start();
            }
        });
        animSet.start();
    }

    public void makeNormalIcon(QuickSwitcherInfo info) {
        // show Toast
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.toast_self_make, null);
        TextView tv_clean_rocket = (TextView) view.findViewById(R.id.tv_clean_rocket);
        String mToast;
        if (!isClean) {
            if (mCleanMem == 0) {
                mToast = mContext.getString(R.string.home_app_manager_mem_clean_one);
            } else {
                mToast = mContext.getString(R.string.home_app_manager_mem_clean,
                        TextFormater.dataSizeFormat(mCleanMem));
            }
        } else {
            mToast = mContext.getString(R.string.the_best_status_toast);
        }
        tv_clean_rocket.setText(mToast);
        Toast toast = new Toast(mContext);
        toast.setView(view);
        toast.setDuration(0);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 150);
        toast.show();
        isClean = true;

        // make Normal Icon
        GestureItemView tv = null;
        int iconSize = mSwitcherLayout.getIconSize();
        if (info.swtichIdentiName.equals(QuickSwitchManager.SPEEDUP)) {
            tv = (GestureItemView) mSwitcherLayout.getChildAtPosition(info.gesturePosition);
            // info.switchIcon[0].setBounds(0, 0, iconSize, iconSize);
            tv.setItemIcon(info.switchIcon[0], false);
        }
        isAnimating = false;
    }

    public AppleWatchLayout getCurrentLayout() {
        if (mCurrentGestureType == GType.DymicLayout) {
            return mDymicLayout;
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            return mMostUsedLayout;
        } else {
            return mSwitcherLayout;
        }
    }

    public int getNowLayout() {
        return mCurrentLayout;
    }

}
