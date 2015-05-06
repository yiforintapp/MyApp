
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;
import com.leo.appmaster.model.BaseInfo;
//import com.leo.appmaster.quickgestures.view.QuickGestureLayout.LayoutParams;
import com.leo.appmaster.utils.LeoLog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
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

    private QuickGestureLayout mDymicLayout, mMostUsedLayout, mSwitcherLayout;
    private CornerTabs mCornerTabs;
    private GType mCurrentGestureType = GType.DymicLayout;
    private Orientation mOrientation = Orientation.Left;
    private GestureDetector mGesDetector;

    private float mSelfWidth, mSelfHeight;
    private float mTouchDownX, mTouchDownY;
    private float mRotateDegree;

    private boolean mSnaping;
    private int mFullRotateDuration = 500;

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
                        return super.onSingleTapUp(e);
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                            float velocityY) {
                        LeoLog.d(TAG, "onFling: velocityX = " + velocityX + "  velocityY = "
                                + velocityY);

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

                        return super.onFling(e1, e2, velocityX, velocityY);
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        LeoLog.d(TAG, "onLongPress");
                        super.onLongPress(e);
                    }
                });

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
                mTouchDownX = event.getX();
                mTouchDownY = event.getY();
                onTouchDown();
                break;

            case MotionEvent.ACTION_MOVE:
                moveX = event.getX();
                moveY = event.getY();
                computeRotateDegree(mTouchDownX, mTouchDownY, moveX, moveY);
                onTouchMove();
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
        LeoLog.d(TAG, "onTouchDown mRotateDegree = " + mRotateDegree);
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

    public void fillGestureItem(GType type, BaseInfo[] infos) {
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
            TextView tv = null;
            QuickGestureLayout.LayoutParams lp = null;
            BaseInfo info = null;
            for (int i = 0; i < infos.length; i++) {
                if (i >= 9) {
                    break;
                }
                tv = new TextView(getContext());
                lp = new QuickGestureLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.position = i;
                tv.setLayoutParams(lp);
                info = infos[i];
                tv.setText(info.label);
                tv.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null, null);
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

}
