
package com.leo.appmaster.quickgestures.view;

import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.utils.BitmapUtils;
//import com.leo.appmaster.quickgestures.view.QuickGestureLayout.LayoutParams;
import com.leo.appmaster.utils.LeoLog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

public class QuickGestureContainer extends FrameLayout {

    public static final String TAG = "QuickGestureContainer";

    public static enum Orientation {
        Left, Right;
    }

    public static enum GType {
        DymicLayout, MostUsedLayout, SwitcherLayout;
    }

    private RightGesturePopupWindow mPopWindow;
    private QuickGestureLayout mDymicLayout, mMostUsedLayout, mSwitcherLayout;
    private CornerTabs mCornerTabs;
    private GType mCurrentGestureType = GType.DymicLayout;
    private Orientation mOrientation = Orientation.Left;
    private GestureDetector mGesDetector;

    private float mSelfWidth, mSelfHeight;
    private float mTouchDownX, mTouchDownY;
    private float mRotateDegree;

    private volatile boolean mEditing;
    private boolean mSnaping;
    private int mFullRotateDuration = 300;

    public QuickGestureContainer(Context context) {
        super(context);
    }

    public QuickGestureContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mGesDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        LeoLog.d(TAG, "onSingleTapUp");

                        double offset2CfC = (double) Math.sqrt(Math.pow((e.getX() - 0), 2)
                                + Math.pow((e.getY() - mSelfHeight), 2));

                        if (offset2CfC > (mDymicLayout.getOuterRadius() + 200)) {
                            if (mEditing) {
                                // TODO leave edit mode
                                leaveEditMode();
                            } else {
                                // TODO close quick gesture
                                if (mPopWindow != null) {
                                    mPopWindow.dismiss();
                                }
                            }

                        } else {
                            QuickGestureLayout gestureLayout = null;
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
                            }
                        }

                        return super.onFling(e1, e2, velocityX, velocityY);
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        LeoLog.d(TAG, "onLongPress");
                        if (!mEditing) {
                            QuickGestureLayout gestureLayout = null;
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

    private void leaveEditMode() {
        mEditing = false;
        mDymicLayout.leaveEditMode();
        mMostUsedLayout.leaveEditMode();
        mSwitcherLayout.leaveEditMode();
    }

    public boolean isEditing() {
        return mEditing;
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        ClipData data = event.getClipData();
        mEditing = true;
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED: {
                LeoLog.i(TAG, "ACTION_DRAG_STARTED");
                break;
            }
            case DragEvent.ACTION_DRAG_ENDED: {
                LeoLog.i(TAG, "ACTION_DRAG_ENDED");
                break;
            }
            case DragEvent.ACTION_DRAG_LOCATION: {
                LeoLog.i(TAG, "ACTION_DRAG_LOCATION: x = " + event.getX() + "  y = " + event.getY());
                break;
            }
            case DragEvent.ACTION_DROP: {
                Log.i(TAG, "ACTION_DROP");
                break;
            }
            case DragEvent.ACTION_DRAG_ENTERED: {
                LeoLog.i(TAG, "ACTION_DRAG_ENTERED ");
                break;
            }

            case DragEvent.ACTION_DRAG_EXITED: {
                LeoLog.i(TAG, "ACTION_DRAG_EXITED ");
                break;
            }
            default:
                Log.i(TAG, "other drag event: " + event);
                break;
        }
        return true;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mSelfWidth = getMeasuredWidth();
        mSelfHeight = getMeasuredHeight();
    }

    @Override
    protected void onFinishInflate() {
        mCornerTabs = (CornerTabs) findViewById(R.id.cornerTabs);
        mDymicLayout = (QuickGestureLayout) findViewById(R.id.qg_dymic_layout);
        mMostUsedLayout = (QuickGestureLayout) findViewById(R.id.qg_mostused_layout);
        mSwitcherLayout = (QuickGestureLayout) findViewById(R.id.qg_switcher_layout);

        super.onFinishInflate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mSnaping)
            return false;
        mGesDetector.onTouchEvent(event);
        float moveX, moveY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mEditing) {
                    LeoLog.d(TAG, "ACTION_DOWN in editing ");
                    QuickGestureLayout gestureLayout = null;
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
                    snapToNext();
                } else {
                    snapToCurrent();
                }
            } else if (mRotateDegree > 0) {
                if (mRotateDegree > 15) {
                    snapToPrevious();
                } else {
                    snapToCurrent();
                }
            }
        } else {
            // TODO
        }
    }

    private void onTouchDown() {
        if (mCurrentGestureType == GType.DymicLayout) {
            mMostUsedLayout.setCurrentRotateDegree(90);
            mSwitcherLayout.setCurrentRotateDegree(-90);
            mDymicLayout.setCurrentRotateDegree(0);
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            mSwitcherLayout.setCurrentRotateDegree(90);
            mDymicLayout.setCurrentRotateDegree(-90);
            mMostUsedLayout.setCurrentRotateDegree(0);
        } else if (mCurrentGestureType == GType.SwitcherLayout) {
            mDymicLayout.setCurrentRotateDegree(90);
            mMostUsedLayout.setCurrentRotateDegree(-90);
            mSwitcherLayout.setCurrentRotateDegree(0);
        }
        mDymicLayout.setVisibility(View.VISIBLE);
        mMostUsedLayout.setVisibility(View.VISIBLE);
        mSwitcherLayout.setVisibility(View.VISIBLE);
    }

    private void onTouchMove() {
        LeoLog.d(TAG, "mRotateDegree = " + mRotateDegree);
        rotateLayout();
    }

    private void rotateLayout() {
        if (mRotateDegree == 0)
            return;

        if (mCurrentGestureType == GType.DymicLayout) {
            if (mRotateDegree > 0) {
                mSwitcherLayout.setCurrentRotateDegree(-90 + mRotateDegree);
            } else if (mRotateDegree < 0) {
                mMostUsedLayout.setCurrentRotateDegree(90 + mRotateDegree);
            }
            mDymicLayout.setCurrentRotateDegree(mRotateDegree);
        } else if (mCurrentGestureType == GType.MostUsedLayout) {
            if (mRotateDegree > 0) {
                mDymicLayout.setCurrentRotateDegree(-90 + mRotateDegree);
            } else if (mRotateDegree < 0) {
                mSwitcherLayout.setCurrentRotateDegree(90 + mRotateDegree);
            }
            mMostUsedLayout.setCurrentRotateDegree(mRotateDegree);
        } else if (mCurrentGestureType == GType.SwitcherLayout) {
            if (mRotateDegree > 0) {
                mMostUsedLayout.setCurrentRotateDegree(-90 + mRotateDegree);
            } else if (mRotateDegree < 0) {
                mDymicLayout.setCurrentRotateDegree(90 + mRotateDegree);
            }

            mSwitcherLayout.setCurrentRotateDegree(mRotateDegree);
        }
        mCornerTabs.updateCoverDegree(-mRotateDegree / 3);
    }

    private void computeRotateDegree(float firstX, float firstY, float secondX,
            float secondY) {
        float firstOffsetX = firstX;
        float firstOffsetY = mSelfHeight - firstY;
        float secondOffsetX = secondX;
        float secondOffsetY = mSelfHeight - secondY;

        float firstDegree = (float) (Math.atan(firstOffsetY / firstOffsetX) * 180f / Math.PI);
        float secondDegree = (float) (Math.atan(secondOffsetY / secondOffsetX) * 180f / Math.PI);

        mRotateDegree = firstDegree - secondDegree;
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
        ValueAnimator va = ValueAnimator.ofFloat(mRotateDegree, mRotateDegree >= 0 ? 90 : -90);
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
        ValueAnimator va = ValueAnimator.ofFloat(mRotateDegree, mRotateDegree > 0 ? 90 : -90);
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
                    mCurrentGestureType = GType.MostUsedLayout;
                    mMostUsedLayout.setVisibility(View.VISIBLE);
                    mDymicLayout.setVisibility(View.GONE);
                    mSwitcherLayout.setVisibility(View.GONE);
                } else if (mCurrentGestureType == GType.MostUsedLayout) {
                    mCurrentGestureType = GType.SwitcherLayout;
                    mSwitcherLayout.setVisibility(View.VISIBLE);
                    mDymicLayout.setVisibility(View.GONE);
                    mMostUsedLayout.setVisibility(View.GONE);
                } else if (mCurrentGestureType == GType.SwitcherLayout) {
                    mCurrentGestureType = GType.DymicLayout;
                    mDymicLayout.setVisibility(View.VISIBLE);
                    mMostUsedLayout.setVisibility(View.GONE);
                    mSwitcherLayout.setVisibility(View.GONE);
                    mCornerTabs.snapSwitcher2Dynamic();
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
        QuickGestureLayout targetLayout = null;
        if (type == GType.DymicLayout) {
            targetLayout = mDymicLayout;
        } else if (type == GType.MostUsedLayout) {
            targetLayout = mMostUsedLayout;
        } else if (type == GType.SwitcherLayout) {
            targetLayout = mSwitcherLayout;
        }

        if (targetLayout != null) {
            targetLayout.removeAllViews();
            GestureItemView tv = null;
            QuickGestureLayout.LayoutParams lp = null;
            BaseInfo info = null;
            int iconSize = targetLayout.getIconSize();
            for (int i = 0; i < infos.size(); i++) {
                if (i >= 9) {
                    break;
                }
                tv = new GestureItemView(getContext());
                lp = new QuickGestureLayout.LayoutParams(
                        targetLayout.getItemSize(), targetLayout.getItemSize());
                lp.position = i;
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setLayoutParams(lp);

                info = infos.get(i);
                tv.setText(info.label);
                tv.setTextSize(12);
                info.icon.setBounds(0, 0, iconSize, iconSize);
                tv.setCompoundDrawables(null, info.icon, null, null);
                if (info.eventNumber > 0) {
                    tv.setDecorateAction(new EventAction(getContext(), info.eventNumber));
                }
                tv.setTag(info);
                targetLayout.addView(tv);
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
        setPivotX(0);
        setPivotY(mSelfHeight);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(600);
        Animator animationx = ObjectAnimator.ofFloat(this, "scaleX", 0.0f, 1.05f, 1.0f);
        Animator animationy = ObjectAnimator.ofFloat(this, "scaleY", 0.0f, 1.05f, 1.0f);
        set.playTogether(animationx, animationy);
    }

    public void showCloseAnimation() {
        setPivotX(0);
        setPivotY(mSelfHeight);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(600);
        Animator animationx = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 1.05f, 0.0f);
        Animator animationy = ObjectAnimator.ofFloat(this, "scaleY", 1.0f, 1.05f, 0.0f);
        set.playTogether(animationx, animationy);
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

    public void snapToDynamic() {
        if (mCurrentGestureType == GType.MostUsedLayout) {
            onTouchDown();
            snapToPrevious();
        } else if (mCurrentGestureType == GType.SwitcherLayout) {
            onTouchDown();
            snapToNext();
        }
    }

    public void setPopWindow(RightGesturePopupWindow rightGesturePopupWindow) {
        mPopWindow = rightGesturePopupWindow;
    }

}
