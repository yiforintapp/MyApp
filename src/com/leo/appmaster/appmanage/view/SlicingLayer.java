package com.leo.appmaster.appmanage.view;

import com.leo.appmaster.R;
import com.leo.appmaster.animation.AnimationListenerAdapter;
import com.leo.appmaster.utils.LeoLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

public class SlicingLayer {

	public static final int SLICING_FROM_APPLIST = 0;
	public static final int SLICING_FROM_FOLDER = 1;

	public static String TAG = "FolderLayer";
	private static int ANIMALTION_TIME = 300;

	private Context mContext;
	private LayoutInflater mInflater;
	private SlicingLayerContainer container;
	private View mBackgroundView;
	private ViewGroup mBackgroundViewParent;
	private View mAnchorView;
	private View mContentView;
	private View mTopView;
	private View mBottomView;

	private int[] mAnchorLocation = new int[2];
	private int mSrceenwidth;
	private int mSrceenheight;
	private int mContentheight;

	private boolean mIsAnimating;
	private boolean mIsOpened = false;
	private boolean mBottomViewStop = false;

	private int mContentMarginTop;
	private int mContentMarginBottom;
	private int mSlicingLine;
	private int mTopOfffsetY;
	private int mBottomOffsetY;

	private OnClosedListener mOnClosedListener;

	public interface OnClosedListener {

		public void onClosed();

	}

	public SlicingLayer(Context context) {
		mContext = context;
		container = new SlicingLayerContainer(mContext);
		mInflater = LayoutInflater.from(context);
	}

	public boolean isSlicinged() {
		return mIsOpened;
	}

	
	public boolean isAnimating() {
		return mIsAnimating;
	}
	
	public void setOnClosedListener(OnClosedListener onFolderClosedListener) {
		this.mOnClosedListener = onFolderClosedListener;
	}

	private AnimationListenerAdapter mOpenAnimationListener = new AnimationListenerAdapter() {
		@Override
		public void onAnimationStart(Animation animation) {
			mBackgroundView.setVisibility(View.INVISIBLE);
			mIsAnimating = true;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mIsOpened = true;
			mIsAnimating = false;
		}
	};

	/**
	 * the close Animation Listener
	 */
	private AnimationListenerAdapter mClosedAnimationListener = new AnimationListenerAdapter() {
		@Override
		public void onAnimationStart(Animation animation) {
			mIsAnimating = true;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mIsAnimating = false;
			mBackgroundView.setVisibility(View.VISIBLE);
			mBackgroundView.setDrawingCacheEnabled(false);
			mAnchorView.setDrawingCacheEnabled(false);
			AlphaAnimation aa = new AlphaAnimation(1f, 0f);
			aa.setDuration(ANIMALTION_TIME);
			aa.setAnimationListener(new AnimationListenerAdapter() {
				@Override
				public void onAnimationEnd(Animation animation) {
					container.removeAllViews();
					mBackgroundViewParent.removeView(container);
				}
			});
			container.startAnimation(aa);
			mIsOpened = false;
			if (mOnClosedListener != null) {
				mOnClosedListener.onClosed();
			}
		}
	};

	/**
	 * 
	 * @param anchor
	 * @param backgroundView
	 * @param contentView
	 * @param derection
	 */
	public void startSlicing(View anchor, View backgroundView, View contentView, int contentHeight) {
		
		mContentheight = contentHeight;
		
		mBackgroundView = backgroundView;
		mBackgroundViewParent = (ViewGroup) mBackgroundView.getParent();

		mAnchorView = anchor;
		mContentView = contentView;
		mSrceenwidth = mBackgroundViewParent.getWidth();
		mSrceenheight = mBackgroundViewParent.getHeight();
		mContentMarginTop = mContentMarginBottom = (mSrceenheight - mContentheight) / 2;

		int[] parentLoaction = new int[2];
		mBackgroundViewParent.getLocationInWindow(parentLoaction);
		mAnchorView.getLocationInWindow(mAnchorLocation);
		mAnchorLocation[1] -= parentLoaction[1];
		mSlicingLine = mAnchorLocation[1] + anchor.getHeight();
		int bottomHeight = mSrceenheight - mSlicingLine;
		mBottomViewStop = false;
		if (bottomHeight < mContentMarginTop) {
			mBottomViewStop = true;
			mContentMarginBottom = bottomHeight;
			mContentMarginTop = mSrceenheight - mContentMarginBottom
					- mContentheight;
			mTopOfffsetY = -mContentheight;
			mBottomOffsetY = 0;
		} else {
			mTopOfffsetY = mContentMarginTop - mSlicingLine;
			mBottomOffsetY = mSrceenheight - mContentMarginBottom
					- mSlicingLine;
		}
		fillUI();
		startSlicingAnimation();
	}

	private void fillUI() {
		container.removeAllViews();

		// add content view
		RelativeLayout.LayoutParams fp = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, mContentheight + 5);
		fp.setMargins(0, mContentMarginTop, 0, 0);
		container.addView(mContentView, fp);

		// add top view
		BitmapDrawable bd;
		Bitmap srceen = getScreenBg();
		mAnchorView.setDrawingCacheEnabled(true);
		Bitmap anchorBitmap = mAnchorView.getDrawingCache(true);
		Bitmap top = Bitmap.createBitmap(srceen, 0, 0, mSrceenwidth,
				mSlicingLine);
		top = createMoveBitmap(top, anchorBitmap, mAnchorLocation);
		bd = new BitmapDrawable(mContext.getResources(), top);
		mTopView = mInflater.inflate(R.layout.folder_move_frame, null);
		mTopView.setId(1000);
		mTopView.setBackgroundDrawable(bd);
		RelativeLayout.LayoutParams ft = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, mSlicingLine);
		container.addView(mTopView, ft);

		// add bottom view
		Bitmap bottom = Bitmap.createBitmap(srceen, 0, mSlicingLine,
				mSrceenwidth, mSrceenheight - mSlicingLine);
		bottom = createMoveBitmap(bottom, null, null);
		int bottomHeight = mSrceenheight - mSlicingLine;
		bd = new BitmapDrawable(mContext.getResources(), bottom);
		mBottomView = mInflater.inflate(R.layout.folder_move_frame, null);
		mBottomView.setBackgroundDrawable(bd);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, bottomHeight);

		lp.addRule(RelativeLayout.BELOW, 1000);
		container.addView(mBottomView, lp);
		mBackgroundViewParent.addView(container);
	}

	private Bitmap getScreenBg() {
		mBackgroundView.setDrawingCacheEnabled(true);
		Bitmap srceen = mBackgroundView.getDrawingCache(true);
		Canvas canvas = new Canvas(srceen);
		Bitmap bg = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.folder_move_bg);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		canvas.drawBitmap(bg, 0, 0, paint);
		return srceen;
	}

	private void startSlicingAnimation() {

		// move top view
		TranslateAnimation topTrans = new TranslateAnimation(0, 0, 0,
				mTopOfffsetY);
		// topTrans.setInterpolator(new DecelerateInterpolator());
		topTrans.setDuration(ANIMALTION_TIME);
		topTrans.setFillAfter(true);
		topTrans.setAnimationListener(mOpenAnimationListener);
		mTopView.startAnimation(topTrans);

		if (!mBottomViewStop) {
			// move bottom view
			TranslateAnimation bottomTrans = new TranslateAnimation(0, 0, 0,
					mBottomOffsetY);
			bottomTrans.setDuration(ANIMALTION_TIME);
			bottomTrans.setFillAfter(true);
			mBottomView.startAnimation(bottomTrans);

			ScaleAnimation sa = new ScaleAnimation(1f, 1f, 0f, 1f, 0,
					mSlicingLine - mContentMarginTop);
			sa.setDuration(ANIMALTION_TIME);
			mContentView.startAnimation(sa);

		} else {
			// move folder
			ScaleAnimation sa = new ScaleAnimation(1f, 1f, 0f, 1f, 0,
					mContentheight);
			sa.setDuration(ANIMALTION_TIME);
			mContentView.startAnimation(sa);
		}
	}

	private Bitmap createMoveBitmap(Bitmap source, Bitmap anchor, int[] location) {
		Canvas canvas = new Canvas(source);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
//		canvas.drawARGB(200, 255, 255, 255);
		if (anchor != null) {
			canvas.drawBitmap(anchor, location[0], location[1], paint);
		}
		return source;
	}

	public void closeSlicing() {
		if (!mIsOpened) {
			return;
		}
		mIsOpened = false;
		// move top view
		TranslateAnimation topTrans = new TranslateAnimation(0, 0,
				mTopOfffsetY, 0);
		topTrans.setInterpolator(new DecelerateInterpolator());
		topTrans.setDuration(ANIMALTION_TIME);
		topTrans.setFillAfter(true);
		topTrans.setAnimationListener(mClosedAnimationListener);
		mTopView.startAnimation(topTrans);

		if (!mBottomViewStop) {
			// move bottom view
			TranslateAnimation bottomTrans = new TranslateAnimation(0, 0,
					mBottomOffsetY, 0);
			bottomTrans.setInterpolator(new DecelerateInterpolator());
			bottomTrans.setDuration(ANIMALTION_TIME);
			bottomTrans.setFillAfter(true);
			mBottomView.startAnimation(bottomTrans);

			// move folder view
			ScaleAnimation sa = new ScaleAnimation(1f, 1f, 1f, 0f, 0,
					mSlicingLine - mContentMarginTop);
			sa.setDuration(ANIMALTION_TIME);
			mContentView.startAnimation(sa);
		} else {
			// move folder
			ScaleAnimation sa = new ScaleAnimation(1f, 1f, 1f, 0f, 0,
					mContentheight);
			sa.setDuration(ANIMALTION_TIME);
			mContentView.startAnimation(sa);
		}

	}

	private class SlicingLayerContainer extends RelativeLayout {

		public SlicingLayerContainer(Context context) {
			super(context);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			final int y = (int) event.getY();

			if (mIsAnimating) {
				return true;
			} else {
				if (mIsOpened
						&& event.getAction() == MotionEvent.ACTION_DOWN
						&& (y < mContentMarginTop || y > mSrceenheight
								- mContentMarginBottom)) {
					closeSlicing();
				}
				return true;
			}
		}
	}
}
