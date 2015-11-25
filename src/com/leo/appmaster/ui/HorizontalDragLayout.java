package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.leo.appmaster.R;


/**
 * @author linxiongzhou
 *    锁屏广告中拖动广告的容器
 */
public class HorizontalDragLayout extends RelativeLayout
{
    private ViewDragHelper mDragger;

    private View mDragView;

    private Point mAutoBackOriginPos = new Point();
    
    private IDrageRelease mListener;
    
    public interface IDrageRelease {
        public void onDrageRelease(View releasedChild, float xvel, float yvel, int dest);
        public void onTouchMoveLeft();
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy);
    }
    
    public HorizontalDragLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mDragger = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback()
        {
            @Override
            public boolean tryCaptureView(View child, int pointerId)
            {
                return child == mDragView ;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx)
            {
//                final int leftBound = getPaddingLeft();
//                final int rightBound = getWidth() - mDragView.getWidth();
//                final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
                return left;
            }

            
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                // TODO Auto-generated method stub
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                if (mListener != null) {
                    mListener.onViewPositionChanged(changedView, left, top, dx, dy);
                }
            }

            //手指释放的时候回调
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                int dest = (getMeasuredWidth() - getResources().getDimensionPixelSize(R.dimen.fragment_lock_large_banner_out_width)) / 2;
                if (releasedChild.getLeft() - mAutoBackOriginPos.x > 0) {
                    dest = ((ViewGroup) releasedChild.getParent()).getWidth()
                            - getResources().getDimensionPixelSize(
                                    R.dimen.fragment_lock_large_banner_image_right);
                } 
                
                if (mListener != null) {
                    mListener.onDrageRelease(releasedChild, xvel, yvel, dest);
                }
                mAutoBackOriginPos.x = dest;
                mDragger.settleCapturedViewAt(dest, releasedChild.getTop());
                invalidate();
                
            }

            @Override
            public int getViewHorizontalDragRange(View child)
            {
                return getMeasuredWidth()-child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(View child)
            {
                return getMeasuredHeight()-child.getMeasuredHeight();
            }
        });
//        mDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT);
    }

    public void setListener(IDrageRelease listener) {
        mListener = listener;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        return mDragger.shouldInterceptTouchEvent(event);
    }

    int mStartDownX = -1;
    int mOldX = 0;
    int mAlignRightPos = 0;
    int mMoveX = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mDragger.processTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartDownX =  (int)event.getX();
                mOldX = (int)event.getX();
                mAlignRightPos = getWidth()- getResources().getDimensionPixelSize(
                                R.dimen.fragment_lock_large_banner_image_right);
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveX = (int)event.getX();
                if (mOldX < mStartDownX && mOldX < mAlignRightPos) {
                    mDragView.offsetLeftAndRight(mMoveX - mOldX);
                }
                mOldX = mMoveX;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mStartDownX > 0 && event.getX() - mStartDownX < 0/* && mDragView.getRight() > getMeasuredWidth()*/ && mOldX < mAlignRightPos) {
                    if (mListener != null) {
                        int dest = (getMeasuredWidth() - mDragView.getWidth()) / 2;
                        smoothSlideTo(dest);
                        mListener.onTouchMoveLeft();
                        mAutoBackOriginPos.x = dest;
                    }
                } else if (mAlignRightPos > 0 && mDragView.getLeft() > mAlignRightPos) {
                    smoothSlideTo(mAlignRightPos);
                    mAutoBackOriginPos.x = mAlignRightPos;
                }
                mStartDownX = -1;
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void computeScroll()
    {
        if(mDragger.continueSettling(true))
        {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        mDragView = getChildAt(0);
        mAutoBackOriginPos.x = mDragView.getLeft();
        mAutoBackOriginPos.y = mDragView.getTop();
    }
    
    public boolean smoothSlideTo(int destX) {
        if (mDragger.smoothSlideViewTo(mDragView, destX, mDragView.getTop())) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }
}
