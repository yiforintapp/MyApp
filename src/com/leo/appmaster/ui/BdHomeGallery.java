/**
 * Filename:    BdHomeGallery.java
 * Description:
 * Copyright:   Baidu MIC Copyright(c)2013
 * @author:     Rambow
 * @version:    1.0
 * Create at:   Jul 2, 2013 2:10:18 PM
 * 
 * Modification History:
 * Date         Author      Version     Description
 * ------------------------------------------------------------------
 * Jul 2, 2013    Rambow      1.0         1.0 Version
 */
package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

/**
 *首页gallery
 */
public class BdHomeGallery extends ViewGroup {
	/** touch状态集 */
	private static enum TouchState { REST, SCROLLING	};
	/** 弹性比，基于单屏宽度 */
	private static final float ELASTIC_RATIO = 0.4f;
	/** 速度阈值 */
	private int mSnapVelocity = 300; //SUPPRESS CHECKSTYLE
	/** 当前所在屏幕index */
	private int mCurScreen;
	/** touch slop */
	private int mTouchSlop;
	/** last x */
	private float mLastMotionX;
	/** down y */
	private float mDownMotionY;
	/** scroller */
	private Scroller mScroller;
	/** v tracker */
	private VelocityTracker mVelocityTracker;
	/** current touch state */
	private TouchState mTouchState = TouchState.REST;
	/** listener */
	private IHomeGalleryListener mListener;
	/** x轴是否被锁定 */
	private boolean isXLocked = false;
	/** 总的宽度 */
	private int mTotalWidth;
	/** 单片宽度 */
	private int mSingleWidth;
	/** 左边距，因有弹性 */
	private int mLeftEdge;
	/** 右边距，因有弹性 */
	private int mRightRdge;
	
    /** 交叉线颜色 */
    public static final int COLOR_CROSS_LINE = 0x12000000;
    /** 交叉线阴影颜色 */
    public static final int COLOR_CROSS_SHADOW_LINE = 0x46ffffff;
    /** 夜间交叉线颜色 */
    public static final int COLOR_CROSS_LINE_NIGHT = 0x26000000;
    /** 夜间交叉线阴影颜色 */
    public static final int COLOR_CROSS_SHADOW_LINE_NIGHT = 0x26515258;
    /** 宫格画笔宽度 */
    public static final int DIVIDER_STROKE_WIDTH = 1;
    /** 分割线画笔 */
    private Paint mDividerLinePaint;
    /** 分割线阴影画笔 */
    private Paint mDividerShadowLinePaint;
    /** 画笔宽度 */
    private int mDividerStrokeWidth;
	private int mType;
	
    public static final int TYPE_HOME = 0;
    public static final int TYPE_MENU= 1;
	public static final int TYPE_MAINPAGE_FOLDER = 2;
	public static final int TYPE_DOWNLOAD_VIEW = 3;
    
	/**
	 * Constructor
	 * @param aContext context
	 */
	public BdHomeGallery(Context aContext, int type) {
		super(aContext);
		init(aContext, type);
	}

	/**
	 * 初始化
	 * @param aCtx context
	 */
	private void init(Context aCtx, int type) {
		
		mType = type;
		if(mType == TYPE_MENU){
			mScroller = new Scroller(aCtx);
		}else
		{
			mScroller = new Scroller(aCtx,new LinearInterpolator(){

				@Override
				public float getInterpolation(float input) {
					
					input= (float) (input*2);
					if(input<1.0f)
					{
						return input;
					}else
					{
						return 1.0f;
					}
				}
				
			});
		}
	
		mTouchSlop = ViewConfiguration.get(aCtx).getScaledTouchSlop();
		mVelocityTracker = VelocityTracker.obtain();
		this.setWillNotDraw(true);
		
		this.reset();
		
		if (android.os.Build.MODEL.toLowerCase().startsWith("mi-one")) {
			mSnapVelocity = 200; //SUPPRESS CHECKSTYLE
		}
		
		// 初始主题
		initDivider();
	}
	
	/**
	 * 设置默认gallery屏幕
	 * @param aDefScreen 默认gallery屏幕index
	 */
	public void setDefaultScreen(int aDefScreen) {
		mCurScreen = aDefScreen;
	}
	
	/**
	 *锁定x轴方向移动
	 */
	public void lockX() {
		isXLocked = true;
	}
	
	/**
	 * 解锁x轴方向移动
	 */
	public void unlockX() {
		isXLocked = false;
	}
	
	/**
	 * @return 当前屏幕的index
	 */
	public int getCurScreen() {
		return mCurScreen;
	}
	
	public void setCurScreen(int a) {
		mCurScreen = a;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		if (!mScroller.isFinished()) {
			mScroller.forceFinished(true);
		}
		
		int width = MeasureSpec.getSize(widthMeasureSpec);

		if (((MeasureSpec.getMode(widthMeasureSpec) & (~MeasureSpec.EXACTLY)) != 0) 
				|| ((MeasureSpec.getMode(heightMeasureSpec) & (~MeasureSpec.EXACTLY)) != 0)) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		mTotalWidth = count * width;
		mSingleWidth = width;
		mLeftEdge = (int) (-width * ELASTIC_RATIO);
		mRightRdge = (int) ((count - 1 + ELASTIC_RATIO) * width);
		
		this.setMeasuredDimension(mTotalWidth, MeasureSpec.getSize(heightMeasureSpec));
		
		scrollTo(mCurScreen * width, 0);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;
		final int childCount = getChildCount();

		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			if (childView.getVisibility() == View.GONE) {
				continue;
			}
			
			final int childWidth = childView.getMeasuredWidth();
			childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
			childLeft += childWidth;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 绘制分割线
		
		if (mTouchState != TouchState.REST) {
			
			drawDividerLine(canvas);
		}
	}
	
	/**
	 * 初始化分割线变量
	 */
	public void initDivider() {
		// 初始化分割线
		mDividerLinePaint = new Paint();
    	mDividerLinePaint.setStyle(Style.STROKE);
    	mDividerLinePaint.setStrokeWidth(1);

        // 初始化分割线阴影
    	mDividerShadowLinePaint = new Paint();
    	mDividerShadowLinePaint.setStyle(Style.STROKE);
    	mDividerShadowLinePaint.setStrokeWidth(1);
	}
	
	/**
	 * 初始化分割线
	 */
	public void drawDividerLine(Canvas aCanvas) {
		// 初始化画笔宽度
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        mDividerStrokeWidth = (int) (DIVIDER_STROKE_WIDTH * dm.density);

		// 计算分割线
        drawDividerLine(aCanvas, mDividerLinePaint, 0, COLOR_CROSS_LINE, mDividerStrokeWidth);
        
        // 计算分割线阴影
        final float coef = 1.5f;
        drawDividerLine(aCanvas, mDividerShadowLinePaint, 
                (int) (mDividerStrokeWidth), COLOR_CROSS_SHADOW_LINE, (int) Math.ceil(mDividerStrokeWidth * coef));
	}
	
    /**
     * 初始化分割线数据
     * 
     * @param aPath
     *          路径
     * @param aPaint
     *          画笔
     * @param aLineOffset
     *          偏移
     * @param aLineColor
     *          线颜色
     * @param aLineWidth
     *          线宽度
     */
    public void drawDividerLine(Canvas aCanvas, Paint aPaint, int aLineOffset, int aLineColor, int aLineWidth) {
        // 变量
		int offset = aLineOffset;
		final int childCount = getChildCount();

		// 循环绘制分割路径
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			if (childView.getVisibility() == View.GONE) {
				continue;
			}
			
			// 递增
			final int childWidth = childView.getMeasuredWidth();
			offset += childWidth;
			
			// 根据线宽度绘制
            for (int j = 0; j < aLineWidth; j++) {
            	aCanvas.drawLine(offset + j, 0, offset + j, getMeasuredHeight(), aPaint);
            }
		}
    }

	/**
	 * 切换到指定屏
	 * @param aWhichScreen 要切换的屏幕index
	 * @param aCycle  是否循环切换
	 */
	public void snapToScreen(int aWhichScreen, boolean aCycle) {
		
		if(mType == TYPE_MENU){
			snapToScreenWithDuration(aWhichScreen, 200,aCycle);
		}else
		{
			snapToScreenWithDuration(aWhichScreen, 300,aCycle);
		}
	}

	/**
	 * 切换到指定屏
	 * @param aWhichScreen 要切换的屏幕index
	 * @param aDuration 切换动作的时长
	 */
	public void snapToScreenWithDuration(int aWhichScreen, int aDuration, boolean aCycle) {
		aWhichScreen = aWhichScreen < 0 ? 0 
				: aWhichScreen > (getChildCount() - 1) ? (getChildCount() - 1) : aWhichScreen;
			
		if (getScrollX() == (aWhichScreen * mSingleWidth))	{
			return;
		}
		int delta = aWhichScreen * mSingleWidth - getScrollX();
		final int during = aDuration == -1 ? Math.abs(delta) :  aDuration;
		
		if(aCycle){
			if(Math.abs(aWhichScreen - mCurScreen) == 1 ) {
				delta = aWhichScreen * mSingleWidth - getScrollX();
				
				mScroller.startScroll(getScrollX(), 0, delta, 0, during);
	
			} else {
				//快速切到中屏之后开始动画
				int middlescreen = 1; 
				delta  = mSingleWidth * aWhichScreen - mSingleWidth;
				int startxnew = mSingleWidth * middlescreen;
				mScroller.startScroll(startxnew, 0, delta, 0, during);
				
			}
		} else {
			delta = aWhichScreen * mSingleWidth - getScrollX();
			
			mScroller.startScroll(getScrollX(), 0, delta, 0, during);
		}
	
		mCurScreen = aWhichScreen;
		
//		BdHome.getInstance().getView().getIndicator().setCurIndex(aWhichScreen);
		
		invalidate(); // Redraw the layout
		
		if (mListener != null) {
			mListener.onGalleryScreenChanged(getChildAt(mCurScreen), mCurScreen);
		}
		
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (mListener != null) {
					mListener.onGalleryScreenChangeComplete(getChildAt(mCurScreen), mCurScreen);
				}
			}
		}, during + 150); //SUPPRESS CHECKSTYLE
	}

	@Override
	public void computeScroll() {
		if (!mScroller.computeScrollOffset()) {
			return;
		}
		
		scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
		this.invalidate();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mVelocityTracker.addMovement(ev);
		if (ev.getAction() == MotionEvent.ACTION_CANCEL 
				|| ev.getAction() == MotionEvent.ACTION_UP) {
			if (isXLocked) {
				this.unlockX();
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TouchState.REST)
				&& !isXLocked) {
			return true;
		}

		final float x = event.getX();

		switch (action) {

			case MotionEvent.ACTION_MOVE:
				final int xDiff = (int) Math.abs(mLastMotionX - x);
				if (xDiff > mTouchSlop) {
					mTouchState = TouchState.SCROLLING;
					mLastMotionX = x;
					if (xDiff < Math.abs(event.getY() - mDownMotionY)) {
						this.lockX();
					}
				}
				break;
			case MotionEvent.ACTION_DOWN:
				mLastMotionX = x;
				mDownMotionY = event.getY();
				mTouchState = mScroller.isFinished() ? TouchState.REST : TouchState.SCROLLING;
				break;

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mTouchState = TouchState.REST;
				isXLocked = false;
				break;
			default:
		}
		
		if (isXLocked) {
			return false;
		}

		return mTouchState != TouchState.REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float x = event.getX();

		switch (action) {
			case MotionEvent.ACTION_DOWN:

				if (!mScroller.isFinished()) {
					mScroller.forceFinished(true);
				}
				mLastMotionX = x;
				break;

			case MotionEvent.ACTION_MOVE:
				if (this.isXLocked) {
					break;
				}
					
				int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;
				
				if (mListener != null && deltaX != 0) {
					mListener.onXChange(deltaX);
				}

				final int dst = getScrollX() + deltaX;
				deltaX = dst < mLeftEdge ? 0 : dst > mRightRdge ? 0 : deltaX;
				scrollBy(deltaX, 0);
				break;

			case MotionEvent.ACTION_UP:
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000); //SUPPRESS CHECKSTYLE
				float velocityX = velocityTracker.getXVelocity();
				if (velocityX > mSnapVelocity && mCurScreen > 0) { // left
					snapToScreen(mCurScreen - 1,false);
				} else if (velocityX < -mSnapVelocity && mCurScreen < getChildCount() - 1) {
					snapToScreen(mCurScreen + 1,false);
				} else {
					snapAccordCurrX();
				}

				reset();
				break;

			case MotionEvent.ACTION_CANCEL:
				snapAccordCurrX();
				
				reset();
				break;
			default:
		}

		return true;
	}
	
	/**
	 * reset所有touch操作
	 */
	private void reset() {
		this.unlockX();
		mVelocityTracker.clear();
		mTouchState = TouchState.REST;
	}
	
	/**
	 * 根据x差量切换屏幕
	 */
	private void snapAccordCurrX() {
		final int screenWidth = this.mSingleWidth;
		if (screenWidth == 0) {
			return;
		}
		
		final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		snapToScreen(destScreen,false);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (mListener != null) {
			mListener.onScrollXChanged(this.getScrollX());
		}
	}
	

	/**
	 * 设置listener
	 * @param aListener listener
	 */
	public void setHomeGalleryListener(IHomeGalleryListener aListener) {
		mListener = aListener;
	}

	/**
	 *home gallery listener
	 */
	public interface IHomeGalleryListener {
		/**
		 * 切屏的回调
		 * @param aView 切屏后显示的View
		 * @param aScreen 所切的屏幕index
		 */
		void onGalleryScreenChanged(View aView, int aScreen);
		
		/**
		 * 切屏完成的回调
		 * @param aView 切屏后显示的View
		 * @param aScreen 所切的屏幕index
		 */
		void onGalleryScreenChangeComplete(View aView, int aScreen);
		
		/**
		 * x轴改变的回调
		 * @param aDelta x轴的改变量
		 */
		void onXChange(int aDelta);
		
		/**
		 * scroll x
		 * @param aScrollX scroll x
		 */
		void onScrollXChanged(int aScrollX);
	}
}
