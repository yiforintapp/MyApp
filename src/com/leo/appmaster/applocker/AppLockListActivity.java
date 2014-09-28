package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.ui.CommonTitleBar;

public class AppLockListActivity extends Activity implements AppChangeListener,
		OnItemClickListener, OnClickListener {
    
	private CommonTitleBar mTtileBar;

	private ImageView mIvAnimator;
	private TextView mTvUnlock, mTvLocked;
	private ViewPager mPager;
	private int mLockedLocationX, mLockedLocationY;
	private int mUnlockLocationX, mUnlockLocationY;
	private GridView mUnlockPage, mLockPage;
	private GridviewAdapter mUnlockAdapter, mLockAdapter;
	private List<AppDetailInfo> mLockedList;
	private List<AppDetailInfo> mUnlockList;
	public LayoutInflater mInflater;

	private View mLastSelectItem;

	private boolean mCaculated;
	private float mScale = 0.5f;

	private AppDetailInfo mLastSelectApp;

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

		mLockedList = new ArrayList<AppDetailInfo>();
		mUnlockList = new ArrayList<AppDetailInfo>();

		mPager = (ViewPager) findViewById(R.id.pager);
		mUnlockPage = (GridView) mInflater.inflate(R.layout.grid_page_item,
				null);
		mLockPage = (GridView) mInflater.inflate(R.layout.grid_page_item, null);

		mUnlockPage.setOnItemClickListener(this);
		mLockPage.setOnItemClickListener(this);

		mPager.setAdapter(new ViewPagerAdapter());

		mPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				setPageSelected(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		setPageSelected(0);
	}

	private void setPageSelected(int page) {
		if (page == 0) {
			mTvUnlock.setBackgroundResource(R.color.page_select);
			mTvLocked.setBackgroundResource(R.color.page_unselect);
		} else {
			mTvUnlock.setBackgroundResource(R.color.page_unselect);
			mTvLocked.setBackgroundResource(R.color.page_select);
		}
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
			mPager.setCurrentItem(1);
		} else if (v == mTvUnlock) {
			mPager.setCurrentItem(0);
		}
	}

	private void loadData() {
	    ArrayList<AppDetailInfo> list = AppLoadEngine.getInstance(this).getAllPkgInfo();
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

		mUnlockAdapter = new GridviewAdapter(mUnlockList);
		mLockAdapter = new GridviewAdapter(mLockedList);

		mUnlockPage.setAdapter(mUnlockAdapter);
		mLockPage.setAdapter(mLockAdapter);

		mTvUnlock.setText("未加锁(" + mUnlockList.size() + "个)");
		mTvLocked.setText(" 已加锁(" + mLockedList.size() + "个)");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mLastSelectApp = (AppDetailInfo) view.getTag();
		calculateLoc();
		if (mLastSelectApp.isLocked()) {
			moveItemToUnlock(view, mLastSelectApp.getAppIcon());
		} else {
			moveItemToLock(view, mLastSelectApp.getAppIcon());
		}
		mLastSelectItem = view;
		mLastSelectItem.setVisibility(View.INVISIBLE);
	}

	private void moveItemToLock(View view, Drawable drawable) {

		int orgX = mPager.getLeft()
				+ view.getLeft()
				+ (view.getRight() - view.getLeft())
				/ 2
				- (mIvAnimator.getLeft() + (mIvAnimator.getRight() - mIvAnimator
						.getLeft()) / 2);
		int orgY = mPager.getTop()
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
		int orgX = mPager.getLeft()
				+ view.getLeft()
				+ (view.getRight() - view.getLeft())
				/ 2
				- (mIvAnimator.getLeft() + (mIvAnimator.getRight() - mIvAnimator
						.getLeft()) / 2);
		int orgY = mPager.getTop()
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
			if (mLastSelectApp.isLocked()) {
				mLastSelectApp.setLocked(false);
				mLockedList.remove(mLastSelectApp);
				mUnlockList.add(mLastSelectApp);
			} else {
				mLastSelectApp.setLocked(true);
				mUnlockList.remove(mLastSelectApp);
				mLockedList.add(mLastSelectApp);
			}
			mTvUnlock.setText("未加锁(" + mUnlockList.size() + "个)");
			mTvLocked.setText(" 已加锁(" + mLockedList.size() + "个)");
			mUnlockAdapter.notifyDataSetChanged();
			mLockAdapter.notifyDataSetChanged();
			mLastSelectItem.setVisibility(View.VISIBLE);
			new Thread(new PushLockedListTask()).start();
		}
	}

	private class PushLockedListTask implements Runnable {
		@Override
		public void run() {
			List<String> list = new ArrayList<String>();
			for (AppDetailInfo info : AppLockListActivity.this.mLockedList) {
				list.add(info.getPkg());
			}
			AppLockerPreference.getInstance(AppLockListActivity.this)
					.setLockedAppList(list);
		}

	}

	private class GridviewAdapter extends BaseAdapter {

		List<AppDetailInfo> list;

		public GridviewAdapter(List<AppDetailInfo> list) {
			super();
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.app_item, null);
			}

			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.iv_app_icon);
			TextView textView = (TextView) convertView
					.findViewById(R.id.tv_app_name);

			AppDetailInfo info = list.get(position);
			imageView.setImageDrawable(info.getAppIcon());
			textView.setText(info.getAppLabel());
			convertView.setTag(info);
			return convertView;
		}

	}

	private class ViewPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = null;
			if (position == 0) {
				view = mUnlockPage;
			} else {
				view = mLockPage;
			}
			container.addView(view, 0);
			return view;
		}

	}

    @Override
    public void onAppChanged(ArrayList<AppDetailInfo> changes, int type) {
        // TODO Auto-generated method stub
        
    }
    
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.tv_option:
			Intent intent = new Intent(this, LockOptionActivity.class);
			startActivity(intent);
			break;
		}
	}
}
