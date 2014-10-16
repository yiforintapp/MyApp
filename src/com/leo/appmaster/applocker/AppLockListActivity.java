package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import com.flurry.android.FlurryAgent;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoGridView;
import com.leo.appmaster.ui.PagedGridView;
import com.leoers.leoanalytics.LeoStat;

public class AppLockListActivity extends Activity implements AppChangeListener,
		OnItemClickListener, OnClickListener {

	private CommonTitleBar mTtileBar;

	private ImageView mIvAnimator;
	private View mTabContainer;
	private TextView mTvUnlock, mTvLocked;
	private int mLockedLocationX, mLockedLocationY;
	private int mUnlockLocationX, mUnlockLocationY;
	private List<BaseInfo> mLockedList;
	private List<BaseInfo> mUnlockList;

	private PagedGridView mPagerUnlock, mPagerLock;
	public LayoutInflater mInflater;
	private boolean mCaculated;
	private float mScale = 0.5f;
	private BaseInfo mLastSelectApp;
	private Object mLock = new Object();

	private boolean isFlying;

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

	private void initUI() {
		mInflater = LayoutInflater.from(this);
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_lock);
		mTtileBar.openBackView();
		mTtileBar.setOptionText(getString(R.string.setting));
		mTtileBar.setOptionTextVisibility(View.VISIBLE);
		mTtileBar.setOptionListener(this);

		mIvAnimator = (ImageView) findViewById(R.id.iv_animator);

		mTabContainer = findViewById(R.id.tab_container);
		mTvUnlock = (TextView) findViewById(R.id.tv_app_unlock);
		mTvLocked = (TextView) findViewById(R.id.tv_app_locked);
		mTvUnlock.setOnClickListener(this);
		mTvLocked.setOnClickListener(this);

		mLockedList = new ArrayList<BaseInfo>();
		mUnlockList = new ArrayList<BaseInfo>();

		mPagerUnlock = (PagedGridView) findViewById(R.id.pager_unlock);
		mPagerLock = (PagedGridView) findViewById(R.id.pager_lock);

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
			mPagerLock.notifyChange(mLockedList);
			mPagerUnlock.setVisibility(View.INVISIBLE);
			mPagerLock.setVisibility(View.VISIBLE);

			mTvUnlock.setTextColor(getResources().getColor(R.color.white));
			mTvLocked.setTextColor(getResources().getColor(
					R.color.tab_select_text));
			mTabContainer.setBackgroundResource(R.drawable.stacked_tabs_r);
		} else if (v == mTvUnlock) {
			mPagerUnlock.notifyChange(mUnlockList);
			mPagerUnlock.setVisibility(View.VISIBLE);
			mPagerLock.setVisibility(View.INVISIBLE);

			mTvUnlock.setTextColor(getResources().getColor(
					R.color.tab_select_text));
			mTvLocked.setTextColor(getResources().getColor(R.color.white));
			mTabContainer.setBackgroundResource(R.drawable.stacked_tabs_l);
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

		Collections.sort(mLockedList, new LockedAppComparator(lockList));

		int rowCount = getResources().getInteger(R.integer.gridview_row_count);
		mPagerUnlock.setDatas(mUnlockList, 4, rowCount);
		mPagerLock.setDatas(mLockedList, 4, rowCount);

		updateLockText();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (isFlying) {
			return;
		}

		isFlying = true;

		calculateLoc();
		mLastSelectApp = (BaseInfo) view.getTag();
		BaseInfo info = null;
		if (mLastSelectApp.isLocked()) {
			mLastSelectApp.setLocked(false);
			for (BaseInfo baseInfo : mLockedList) {
				if (baseInfo.getPkg().equals(mLastSelectApp.getPkg())) {
					info = baseInfo;
					info.setLocked(false);
					break;
				}
			}
			mUnlockList.add(info);
			mLockedList.remove(info);
			moveItemToUnlock(view, mLastSelectApp.getAppIcon());

			LeoStat.addEvent(LeoStat.P2, "unlock app", mLastSelectApp.getPkg());
			FlurryAgent.logEvent(mLastSelectApp.getPkg() + ": unlock app");
		} else {
			mLastSelectApp.setLocked(true);
			for (BaseInfo baseInfo : mUnlockList) {
				if (baseInfo.getPkg().equals(mLastSelectApp.getPkg())) {
					info = baseInfo;
					info.setLocked(true);
					break;
				}
			}
			mLockedList.add(0, info);
			mUnlockList.remove(info);
			moveItemToLock(view, mLastSelectApp.getAppIcon());

			LeoStat.addEvent(LeoStat.P2, "lock app", mLastSelectApp.getPkg());
			FlurryAgent.logEvent(mLastSelectApp.getPkg() + ": lock app");
		}
		((LeoGridView) parent).removeItemAnimation(position, mLastSelectApp);
		saveLockList();
	}

	private void saveLockList() {
		new Thread(new PushLockedListTask()).start();
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

	private void updateLockText() {
		int unlockSize = mUnlockList.size();
		int lockSize = mLockedList.size();
		if (unlockSize == 0) {
			mTvUnlock.setText(getString(R.string.unlock_count));
		} else {
			mTvUnlock.setText(getString(R.string.unlock_count) + "("
					+ unlockSize + ")");
		}

		if (lockSize == 0) {
			mTvLocked.setText(getString(R.string.lock_count));
		} else {
			mTvLocked.setText(getString(R.string.lock_count) + "(" + lockSize
					+ ")");
		}
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

		public FlyAnimaListener() {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mIvAnimator.setVisibility(View.INVISIBLE);
			updateLockText();
			isFlying = false;
		}
	}

	private class PushLockedListTask implements Runnable {
		@Override
		public void run() {
			synchronized (mLock) {
				List<String> list = new ArrayList<String>();
				for (BaseInfo info : AppLockListActivity.this.mLockedList) {
					list.add(info.getPkg());
				}
				AppLockerPreference.getInstance(AppLockListActivity.this)
						.setLockedAppList(list);
			}
		}
	}

	@Override
	public void onAppChanged(ArrayList<AppDetailInfo> changes, int type) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				loadData();
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_option_text:
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

	private class LockedAppComparator implements Comparator<BaseInfo> {
		List<String> sortBase;

		public LockedAppComparator(List<String> sortBase) {
			super();
			this.sortBase = sortBase;
		}

		@Override
		public int compare(BaseInfo lhs, BaseInfo rhs) {
			if (sortBase.indexOf(lhs.getPkg()) > sortBase.indexOf(rhs.getPkg())) {
				return 1;
			} else {
				return -1;
			}
		}
	}
}
