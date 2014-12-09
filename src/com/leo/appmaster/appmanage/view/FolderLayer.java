package com.leo.appmaster.appmanage.view;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class FolderLayer {

	public static String TAG = "FolderLayer";
	private static int ANIMALTION_TIME = 350;

	private Context mContext;
	private LayoutInflater mInflater;
	private FolderLayerContainer container;
	private View mBackgroundView;
	private ViewGroup mBackgroundViewParent;
	private View mContentView;
	private View mTopView;
	private View mBottomView;

	private int[] mAnchorLocation = new int[2];
	private int mSrceenwidth;
	private int mSrceenheight;
	private int mFolderheight;
	private int mFolderUpY;
	private int mFolderDownY;

	private boolean mIsOpened = false;
	private boolean mMoveFolder = false;

	private int mFolderTop;
	private int mCutLine;
	private int mTopOfffsetY;
	private int mBottomOffsetY;

	public interface OnClosedListener {

		public void onClosed();
	}

	private OnClosedListener mOnFolderClosedListener;

	public void setOnClosedListener(OnClosedListener onFolderClosedListener) {
		this.mOnFolderClosedListener = onFolderClosedListener;
	}

	private Animation.AnimationListener mOpenAnimationListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
			mBackgroundView.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mIsOpened = true;
		}
	};

	/**
	 * the folder close Animation Listener
	 */
	private Animation.AnimationListener mClosedAnimationListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mBackgroundView.setVisibility(View.VISIBLE);
			container.removeAllViews();
			mBackgroundViewParent.removeView(container);
			mBackgroundView.setDrawingCacheEnabled(false);
			mIsOpened = false;
			if (mOnFolderClosedListener != null) {
				mOnFolderClosedListener.onClosed();
			}
		}
	};

	public FolderLayer(Context context) {
		mContext = context;
		container = new FolderLayerContainer(mContext);
		// container.setBackgroundResource(R.drawable.app_manage_bg);
		mInflater = LayoutInflater.from(context);
		mFolderheight = mContext.getResources().getDimensionPixelSize(
				R.dimen.folder_content_height);
	}

	/**
	 * 
	 * @param anchor
	 * @param backgroundView
	 * @param folderView
	 * @param derection
	 */
	public void openFolderView(View anchor, View backgroundView, View folderView) {

		mBackgroundView = backgroundView;
		mBackgroundViewParent = (ViewGroup) mBackgroundView.getParent();

		mContentView = folderView;
		mSrceenwidth = mBackgroundViewParent.getWidth();
		mSrceenheight = mBackgroundViewParent.getHeight();
		mFolderTop = (mSrceenheight - mFolderheight) / 2;

		int bottomHeight = mSrceenheight - mCutLine;
		if (bottomHeight < mFolderTop) {
			mMoveFolder = true;
		}
		
		int[] parentLoaction = new int[2];
		mBackgroundViewParent.getLocationInWindow(parentLoaction);
		anchor.getLocationInWindow(mAnchorLocation);
		mAnchorLocation[1] -= parentLoaction[1];

		// anchor is below of folder
		if (mAnchorLocation[1] > (mFolderTop + mFolderheight)) {
			mCutLine = mAnchorLocation[1];
			mTopOfffsetY = mCutLine + mFolderTop;
			mBottomOffsetY = mFolderTop + mFolderheight - mCutLine;
		} else if ((mAnchorLocation[1] + anchor.getHeight()) < mFolderTop) {
			// anchor is above of folder
			mCutLine = mAnchorLocation[1] + anchor.getHeight();
			mTopOfffsetY = mFolderTop - mCutLine;
			mBottomOffsetY = mFolderTop + mFolderheight - mCutLine;
		} else {
			mCutLine = mAnchorLocation[1] + anchor.getHeight();
			mTopOfffsetY = mFolderTop - mCutLine;
			mBottomOffsetY = mFolderTop + mFolderheight - mCutLine;
		}

		fillUI();
		startOpenAnimation();
	}

	private void fillUI() {
		container.removeAllViews();
		
		int bottomHeight = mSrceenheight - mCutLine;
		Bitmap bottom;
		if (bottomHeight < mFolderTop) {
			mMoveFolder = true;
		}
		
		// add content view
		RelativeLayout.LayoutParams fp = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, mFolderheight);
		
		if(mMoveFolder) {
			fp.setMargins(0, mFolderTop, 0, 0);
		} else {
			fp.setMargins(0, mFolderTop, 0, 0);
		}
		
		container.addView(mContentView, fp);

		BitmapDrawable bd;
		mBackgroundView.setDrawingCacheEnabled(true);
		Bitmap srceen = mBackgroundView.getDrawingCache(true);

		// add top view
		Bitmap top = Bitmap.createBitmap(srceen, 0, 0, mSrceenwidth, mCutLine);
		bd = new BitmapDrawable(mContext.getResources(), top);
		mTopView = mInflater.inflate(R.layout.move_frame, null);
		mTopView.setId(1000);
		mTopView.setBackgroundDrawable(bd);
		RelativeLayout.LayoutParams ft = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, mCutLine);
		container.addView(mTopView, ft);

		// add bottom view
		bottom = Bitmap.createBitmap(srceen, 0, mCutLine, mSrceenwidth,
				mSrceenheight - mCutLine);
		LeoLog.e("xxxx", "bottomHeight = " + bottomHeight + "   mFolderTop = "
				+ mFolderTop);
		bd = new BitmapDrawable(mContext.getResources(), bottom);
		mBottomView = mInflater.inflate(R.layout.move_frame, null);
		mBottomView.setBackgroundDrawable(bd);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, bottomHeight);

		lp.addRule(RelativeLayout.BELOW, 1000);
		container.addView(mBottomView, lp);

		mBackgroundViewParent.addView(container);
	}

	private void startOpenAnimation() {

		// move top view
		TranslateAnimation topTrans = new TranslateAnimation(0, 0, 0,
				mTopOfffsetY);
		// topTrans.setInterpolator(new DecelerateInterpolator());
		topTrans.setDuration(ANIMALTION_TIME);
		topTrans.setFillAfter(true);
		topTrans.setAnimationListener(mOpenAnimationListener);
		mTopView.startAnimation(topTrans);

		// move folder
		// ScaleAnimation sa = new ScaleAnimation(fromX, toX, fromY, toY,
		// pivotX, pivotY)

		// move bottom view
		TranslateAnimation bottomTrans = new TranslateAnimation(0, 0, 0,
				mBottomOffsetY);
		// bottomTrans.setInterpolator(new DecelerateInterpolator());
		bottomTrans.setDuration(ANIMALTION_TIME);
		bottomTrans.setFillAfter(true);
		bottomTrans.setAnimationListener(mOpenAnimationListener);
		mBottomView.startAnimation(bottomTrans);
	}

	public boolean isOpened() {
		return mIsOpened;
	}

	private void closeFloder() {
		if (!mIsOpened) {
			return;
		}

		// move top view
		TranslateAnimation topTrans = new TranslateAnimation(0, 0,
				mTopOfffsetY, 0);
		topTrans.setInterpolator(new DecelerateInterpolator());
		topTrans.setDuration(ANIMALTION_TIME);
		topTrans.setFillAfter(true);
		topTrans.setAnimationListener(mClosedAnimationListener);
		mTopView.startAnimation(topTrans);

		// move bottom view
		TranslateAnimation bottomTrans = new TranslateAnimation(0, 0,
				mBottomOffsetY, 0);
		bottomTrans.setInterpolator(new DecelerateInterpolator());
		bottomTrans.setDuration(ANIMALTION_TIME);
		bottomTrans.setFillAfter(true);
		mBottomView.startAnimation(bottomTrans);

		// AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
		// aa.setDuration(ANIMALTION_TIME);
		// container.startAnimation(aa);

	}

	private class FolderLayerContainer extends RelativeLayout {

		long lasttime = 0;
		boolean isvalid = false;

		public FolderLayerContainer(Context context) {
			super(context);
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				if (getKeyDispatcherState() == null) {
					return super.dispatchKeyEvent(event);
				}

				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getRepeatCount() == 0) {
					KeyEvent.DispatcherState state = getKeyDispatcherState();
					if (state != null) {
						state.startTracking(event, this);
					}
					return true;
				} else if (event.getAction() == KeyEvent.ACTION_UP) {
					KeyEvent.DispatcherState state = getKeyDispatcherState();
					if (state != null && state.isTracking(event)
							&& !event.isCanceled()) {
						closeFloder();
						return true;
					}
				}
				return super.dispatchKeyEvent(event);
			} else {
				return super.dispatchKeyEvent(event);
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			final int y = (int) event.getY();

			if (System.currentTimeMillis() - lasttime > ANIMALTION_TIME) {
				isvalid = true;
			} else {
				isvalid = false;
			}
			lasttime = System.currentTimeMillis();

			if ((event.getAction() == MotionEvent.ACTION_DOWN) && isvalid
					&& (y < mFolderUpY || y > mFolderDownY)) {
				closeFloder();
				return true;
			} else {
				return super.onTouchEvent(event);
			}
		}
	}
}
