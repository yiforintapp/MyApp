package com.leo.appmaster.appmanage.view;

import java.util.List;

import com.leo.appmaster.animation.AnimationListenerAdapter;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.utils.LeoLog;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView.OnItemClickListener;

public class FolderLayer {

	public static String TAG = "FolderLayer";
	private static int ANIMALTION_TIME = 500;

	private FolderView mFolderView;
	private View mBrotherView;
	private View mAnchorView;
	private OnItemClickListener mFolderItemClickListener;
	private boolean mIsAnimating;
	private boolean mIsOpened = false;
	private OnFolderListener mOnFolderClosedListener;
	private Context mContext;
	private Handler handler;
	private int[] mAnchorLocation;

	public interface OnFolderListener {
		public void onOpened();

		public void onClosed();
	}

	public FolderLayer(Context context, FolderView folderView) {
		mContext = context;
		mFolderView = folderView;
		mFolderView.setFolderLayer(this);
		handler = new Handler();
	}

	public void setAnchorView(View anchor) {
		mAnchorView = anchor;
	}

	public boolean isFolderOpened() {
		return mIsOpened;
	}

	public boolean isAnimating() {
		return mIsAnimating;
	}

	public void setOnClosedListener(OnFolderListener onFolderClosedListener) {
		this.mOnFolderClosedListener = onFolderClosedListener;
	}

	/**
	 * 
	 * @param type
	 * @param anchorView
	 */
	public void openFolderView(int type, View anchorView, View brotherView) {
		mBrotherView = brotherView;
		startOpenAnimation(anchorView, type);
	}

	private void startOpenAnimation(View anchorView, final int type) {
		final AnimationSet as1 = new AnimationSet(true);
		as1.setDuration(ANIMALTION_TIME);
		as1.setInterpolator(new AccelerateDecelerateInterpolator());
		AlphaAnimation aa1 = new AlphaAnimation(0f, 1f);
		as1.addAnimation(aa1);
		mAnchorLocation = new int[2];
		anchorView.getLocationInWindow(mAnchorLocation);

		int[] parentLocation = new int[2];
		View parent = (View) mFolderView.getParent();
		parent.getLocationInWindow(parentLocation);

		mAnchorLocation[0] += anchorView.getWidth() / 2;
		mAnchorLocation[1] += anchorView.getHeight() / 2 - parentLocation[1];

		ScaleAnimation sa1 = new ScaleAnimation(0f, 1f, 0f, 1f,
				mAnchorLocation[0], mAnchorLocation[1]);
		sa1.setDuration(ANIMALTION_TIME);
		as1.setAnimationListener(new AnimationListenerAdapter() {
			@Override
			public void onAnimationStart(Animation animation) {
				mFolderView.setCurrentPosition(type);
				super.onAnimationStart(animation);
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				mIsOpened = true;
				mIsAnimating = false;
				if (mOnFolderClosedListener != null) {
					mOnFolderClosedListener.onOpened();
				}
			}
		});
		as1.addAnimation(sa1);

		AnimationSet as2 = new AnimationSet(true);
		as2.setDuration(ANIMALTION_TIME);
		as2.setInterpolator(new AccelerateDecelerateInterpolator());
		as2.setFillEnabled(true);
		as2.setFillAfter(true);
		AlphaAnimation aa2 = new AlphaAnimation(1f, 0.0f);
		as2.addAnimation(aa2);
		ScaleAnimation sa2 = new ScaleAnimation(1f, 0.95f, 1f, 0.95f,
				mBrotherView.getWidth() / 2, mBrotherView.getHeight() / 2);
		as2.addAnimation(sa2);

		as2.setAnimationListener(new AnimationListenerAdapter() {
			@Override
			public void onAnimationStart(Animation animation) {
				mIsAnimating = true;
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mBrotherView.setVisibility(View.GONE);
			}
		});
		as2.addAnimation(sa2);
		mBrotherView.startAnimation(as2);

		mFolderView.setVisibility(View.VISIBLE);
		mFolderView.startAnimation(as1);

	}

	public void closeFloder() {
		if (!mIsOpened) {
			return;
		}
		mIsOpened = false;
		AnimationSet as1 = new AnimationSet(true);
		as1.setDuration(ANIMALTION_TIME);
		as1.setInterpolator(new AccelerateDecelerateInterpolator());
		AlphaAnimation aa1 = new AlphaAnimation(1f, 0f);
		as1.addAnimation(aa1);
		ScaleAnimation sa = new ScaleAnimation(1f, 0f, 1f, 0f,
				mAnchorLocation[0], mAnchorLocation[1]);
		as1.addAnimation(sa);
		as1.setAnimationListener(new AnimationListenerAdapter() {
			@Override
			public void onAnimationStart(Animation animation) {
				mBrotherView.setVisibility(View.VISIBLE);
				mIsAnimating = true;
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				super.onAnimationEnd(animation);
				mFolderView.setVisibility(View.GONE);
				mIsOpened = false;
				mIsAnimating = false;
				if (mOnFolderClosedListener != null) {
					mOnFolderClosedListener.onClosed();
				}
			}
		});

		AnimationSet as2 = new AnimationSet(true);
		as2.setDuration(ANIMALTION_TIME);
		as2.setInterpolator(new AccelerateDecelerateInterpolator());
		AlphaAnimation aa2 = new AlphaAnimation(0f, 1f);
		as2.addAnimation(aa2);
		ScaleAnimation sa2 = new ScaleAnimation(0.9f, 1f, 0.9f, 1f,
				mBrotherView.getWidth() / 2, mBrotherView.getHeight() / 2);
		as2.addAnimation(sa2);

		mBrotherView.startAnimation(as2);
		mFolderView.startAnimation(as1);
	}

	public void updateFolderData(int folderType, List<AppItemInfo> contentData,
			List<BusinessItemInfo> reccommendData) {
		mFolderView.updateData(folderType, contentData, reccommendData);
	}

}
