package com.leo.appmaster.appmanage.view;

import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.animation.AnimationListenerAdapter;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BusinessItemInfo;

import android.content.Context;
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
	private int[] mAnchorLocation;

	public interface OnFolderListener {
		public void onOpened();

		public void onClosed();
	}

	public FolderLayer(Context context, FolderView folderView) {
		mContext = context;
		mFolderView = folderView;
		mFolderView.setFolderLayer(this);
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
	public void openFolderView(int type, View anchorView) {
		mFolderView.setCurrentPosition(type);
		startOpenAnimation(anchorView);
	}

	private void startOpenAnimation(View anchorView) {
		mFolderView.setVisibility(View.VISIBLE);

		AnimationSet as = new AnimationSet(true);
		as.setDuration(ANIMALTION_TIME);
		AlphaAnimation aa = new AlphaAnimation(0f, 1f);
		as.addAnimation(aa);
		as.setInterpolator(new AccelerateDecelerateInterpolator());

		mAnchorLocation = new int[2];
		anchorView.getLocationInWindow(mAnchorLocation);
		ScaleAnimation sa = new ScaleAnimation(0f, 1f, 0f, 1f,
				mAnchorLocation[0], mAnchorLocation[1]);
		sa.setDuration(ANIMALTION_TIME);
		as.setAnimationListener(new AnimationListenerAdapter() {
			@Override
			public void onAnimationStart(Animation animation) {
				mIsAnimating = true;
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

		as.addAnimation(sa);
		mFolderView.startAnimation(as);
	}

	public void closeFloder() {
		if (!mIsOpened) {
			return;
		}
		mIsOpened = false;
		ScaleAnimation sa = new ScaleAnimation(1f, 0f, 1f, 0f,
				mAnchorLocation[0], mAnchorLocation[1]);
		sa.setAnimationListener(new AnimationListenerAdapter() {
			@Override
			public void onAnimationStart(Animation animation) {
				mIsAnimating = true;
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				super.onAnimationEnd(animation);
				mFolderView.setVisibility(View.INVISIBLE);
				mIsOpened = false;
				mIsAnimating = false;
				if (mOnFolderClosedListener != null) {
					mOnFolderClosedListener.onClosed();
				}
			}
		});
		sa.setDuration(ANIMALTION_TIME);
		mFolderView.startAnimation(sa);
	}

	public void updateFolderData(int folderFlowSort,
			List<AppItemInfo> contentData, List<BusinessItemInfo> reccommendData) {
		// TODO Auto-generated method stub
		mFolderView.updateData(folderFlowSort, contentData, reccommendData);
	}

	public void setFolderItemClickListener(
			OnItemClickListener folderItemClickListener) {
		mFolderView.setFolderItemClickListener(folderItemClickListener);
	}

}
