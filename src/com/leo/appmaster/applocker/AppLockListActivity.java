package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.DragGridView;
import com.leo.appmaster.ui.PagedGridView;
import com.leo.appmaster.ui.DragGridView.AnimEndListener;
import com.leoers.leoanalytics.LeoStat;

public class AppLockListActivity extends Activity implements AppChangeListener,
		OnItemClickListener, OnClickListener, AnimEndListener {

	private CommonTitleBar mTtileBar;

	private ImageView mIvAnimator;
	private TextView mTvUnlock, mTvLocked;
	private int mLockedLocationX, mLockedLocationY;
	private int mUnlockLocationX, mUnlockLocationY;
	private List<BaseInfo> mLockedList;
	private List<BaseInfo> mUnlockList;

	private PagedGridView mPagerUnlock, mPagerLock;

	public LayoutInflater mInflater;

	// private View mLastSelectItem;

	private boolean mCaculated;
	private float mScale = 0.5f;

	private BaseInfo mLastSelectApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_app_list);

		AppLoadEngine.getInstance(this).registerAppChangeListener(this);

		initUI();
		loadData();
	}

	@Override
	protected void onDestroy() {
		AppLoadEngine.getInstance(this).unregisterAppChangeListener(this);
		super.onDestroy();
	}

	@SuppressLint("NewApi")
	private void initUI() {
		mInflater = LayoutInflater.from(this);
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_lock);
		mTtileBar.openBackView();
		mTtileBar.setOptionVisibility(View.VISIBLE);
		mTtileBar.setOptionListener(this);
		mTtileBar.setOptionVisibility(View.VISIBLE);
		mTtileBar.setOptionListener(this);

		mIvAnimator = (ImageView) findViewById(R.id.iv_animator);

		mTvUnlock = (TextView) findViewById(R.id.tv_app_unlock);
		mTvLocked = (TextView) findViewById(R.id.tv_app_locked);
		mTvUnlock.setOnClickListener(this);
		mTvLocked.setOnClickListener(this);

		mLockedList = new ArrayList<BaseInfo>();
		mUnlockList = new ArrayList<BaseInfo>();

		mPagerUnlock = (PagedGridView) findViewById(R.id.pager_unlock);
		mPagerLock = (PagedGridView) findViewById(R.id.pager_lock);

		mPagerUnlock.setAnimListener(this);
		mPagerLock.setAnimListener(this);

		mPagerUnlock.setGridviewItemClickListener(this);
		mPagerLock.setGridviewItemClickListener(this);

	}

	private void calculateLoc() {
		if (!mCaculated) {
			mLockedLocationX = mTvLocked.getLeft()
					+ (mTvLocked.getRight() - mTvLocked.getLeft()) / 2;
			mLockedLocationY = mTvLocked.getTop()
					+ (mTvLocked.getBottom() - mTvLocked.getTop()) / 2;
			mUnlockLocationX = mTvUnlock.getLeft()
					+ (mTvUnlock.getRight() - mTvUnlock.getLeft()) / 2;
			mUnlockLocationY = mTvUnlock.getTop()
					+ (mTvUnlock.getBottom() - mTvUnlock.getTop()) / 2;
			mCaculated = true;
		}
	}

	public void onTabClick(View v) {
		if (v == mTvLocked) {
			mPagerUnlock.setVisibility(View.INVISIBLE);
			mPagerLock.setVisibility(View.VISIBLE);

			mTvUnlock.setBackgroundResource(R.color.tab_unselect);
			mTvLocked.setBackgroundResource(R.color.tab_select);
		} else if (v == mTvUnlock) {
			mPagerUnlock.setVisibility(View.VISIBLE);
			mPagerLock.setVisibility(View.INVISIBLE);

			mTvUnlock.setBackgroundResource(R.color.tab_select);
			mTvLocked.setBackgroundResource(R.color.tab_unselect);
		}
	}

	private void loadData() {

		ArrayList<AppDetailInfo> list = AppLoadEngine.getInstance(this)
				.getAllPkgInfo();
		List<String> lockList = AppLockerPreference.getInstance(this)
				.getLockedAppList();
		for (AppDetailInfo appDetailInfo : list) {
			if (lockList.contains(appDetailInfo.getPkg())) {
				appDetailInfo.setLocked(true);
				mLockedList.add(appDetailInfo);
			} else {
				appDetailInfo.setLocked(false);
				mUnlockList.add(appDetailInfo);
			}
		}

		mPagerUnlock.setDatas(mUnlockList, 4, 5);
		mPagerLock.setDatas(mLockedList, 4, 5);

		mTvUnlock.setText("未加锁(" + mUnlockList.size() + ")");
		mTvLocked.setText(" 已加锁(" + mLockedList.size() + ")");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		calculateLoc();
		mLastSelectApp = (BaseInfo) view.getTag();
		BaseInfo info = null;
		if (mLastSelectApp.isLocked()) {
			mLastSelectApp.setLocked(false);
			for (BaseInfo baseInfo : mLockedList) {
				if (baseInfo.getPkg().equals(mLastSelectApp.getPkg())) {
					info = baseInfo;
					break;
				}
			}
			mUnlockList.add(info);
			mLockedList.remove(info);
			moveItemToUnlock(view, mLastSelectApp.getAppIcon());

			LeoStat.addEvent(LeoStat.P2, "unlock app", mLastSelectApp.getPkg());
		} else {
			mLastSelectApp.setLocked(true);
			for (BaseInfo baseInfo : mUnlockList) {
				if (baseInfo.getPkg().equals(mLastSelectApp.getPkg())) {
					info = baseInfo;
					break;
				}
			}
			mLockedList.add(info);
			mUnlockList.remove(info);
			moveItemToLock(view, mLastSelectApp.getAppIcon());
			
			LeoStat.addEvent(LeoStat.P2, "lock app", mLastSelectApp.getPkg());
		}
		((DragGridView) parent).removeItemAnimation(position, mLastSelectApp);

	}

	private void moveItemToLock(View view, Drawable drawable) {

		int orgX = mPagerUnlock.getLeft()
				+ view.getLeft()
				+ (view.getRight() - view.getLeft())
				/ 2
				- (mIvAnimator.getLeft() + (mIvAnimator.getRight() - mIvAnimator
						.getLeft()) / 2);
		int orgY = mPagerUnlock.getTop()
				+ view.getTop()
				+ (view.getBottom() - view.getTop())
				/ 2
				- (mIvAnimator.getTop() + (mIvAnimator.getBottom() - mIvAnimator
						.getTop()) / 2);

		float targetX = (float) (mLockedLocationX - mIvAnimator.getLeft() - (mIvAnimator
				.getRight() - mIvAnimator.getLeft()) * (0.5 - mScale / 2));
		float targetY = (float) (mLockedLocationY - mIvAnimator.getTop() - (mIvAnimator
				.getBottom() - mIvAnimator.getTop()) * (0.5 - mScale / 2));

		Log.e("xxxx", "orgX = " + orgX + ",   orgY = " + orgY
				+ ",   targetX = " + targetX + ",  targetY = " + targetY);

		Animation animation = createFlyAnimation(orgX, orgY, targetX, targetY);
		animation.setAnimationListener(new FlyAnimaListener());

		mIvAnimator.setVisibility(View.VISIBLE);
		mIvAnimator.setImageDrawable(drawable);
		mIvAnimator.startAnimation(animation);

	}

	private void moveItemToUnlock(View view, Drawable drawable) {
		int orgX = mPagerUnlock.getLeft()
				+ view.getLeft()
				+ (view.getRight() - view.getLeft())
				/ 2
				- (mIvAnimator.getLeft() + (mIvAnimator.getRight() - mIvAnimator
						.getLeft()) / 2);
		int orgY = mPagerUnlock.getTop()
				+ view.getTop()
				+ (view.getBottom() - view.getTop())
				/ 2
				- (mIvAnimator.getTop() + (mIvAnimator.getBottom() - mIvAnimator
						.getTop()) / 2);

		float targetX = (float) (mUnlockLocationX - mIvAnimator.getLeft() - (mIvAnimator
				.getRight() - mIvAnimator.getLeft()) * (0.5 - mScale / 2));
		float targetY = (float) (mUnlockLocationY - mIvAnimator.getTop() - (mIvAnimator
				.getBottom() - mIvAnimator.getTop()) * (0.5 - mScale / 2));

		Animation animation = createFlyAnimation(orgX, orgY, targetX, targetY);
		animation.setAnimationListener(new FlyAnimaListener());

		mIvAnimator.setVisibility(View.VISIBLE);
		mIvAnimator.setImageDrawable(drawable);
		mIvAnimator.startAnimation(animation);

	}

	private Animation createFlyAnimation(float orgX, float orgY, float targetX,
			float tragetY) {
		AnimationSet set = new AnimationSet(true);
		set.setInterpolator(new AccelerateDecelerateInterpolator());
		set.setDuration(500);

		Animation ta = new TranslateAnimation(orgX, targetX, orgY, tragetY);
		ScaleAnimation sa = new ScaleAnimation(1.0f, mScale, 1.0f, mScale);

		set.addAnimation(sa);
		set.addAnimation(ta);

		return set;
	}

	private class FlyAnimaListener implements AnimationListener {
		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mIvAnimator.setVisibility(View.INVISIBLE);
			mTvUnlock.setText("未加锁(" + mUnlockList.size() + ")");
			mTvLocked.setText(" 已加锁(" + mLockedList.size() + ")");

			// mLastSelectItem.setVisibility(View.VISIBLE);
			new Thread(new PushLockedListTask()).start();
		}
	}

	private class PushLockedListTask implements Runnable {
		@Override
		public void run() {
			List<String> list = new ArrayList<String>();
			for (BaseInfo info : AppLockListActivity.this.mLockedList) {
				list.add(info.getPkg());
			}
			AppLockerPreference.getInstance(AppLockListActivity.this)
					.setLockedAppList(list);
		}

	}

	@Override
	public void onAppChanged(ArrayList<AppDetailInfo> changes, int type) {

	}

	@Override
	public void onClick(View v) {
		Log.e("xxxx", "id = " + v.getId());
		switch (v.getId()) {
		case R.id.tv_option:
			Intent intent = new Intent(this, LockOptionActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_app_unlock:
			onTabClick(v);
			break;
		case R.id.tv_app_locked:
			onTabClick(v);
			break;
		}
	}

	@Override
	public void onAnimEnd() {
		// mPagerUnlock.notifyChange(mUnlockList);
		// mPagerLock.notifyChange(mLockedList);
	}
}
