package com.leo.appmaster.applocker;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.BaseActivity;
import com.leo.appmaster.R;
import com.leo.appmaster.SDKWrapper;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.ui.LockImageView;
import com.leo.appmaster.ui.PagedGridView;
import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.LeoStat;

public class AppLockListActivity extends BaseActivity implements
		AppChangeListener, OnItemClickListener, OnClickListener {

	public LayoutInflater mInflater;
	private CommonTitleBar mTtileBar;
	private View mMaskLayer;
	private List<BaseInfo> mLockedList;
	private List<BaseInfo> mUnlockList;
	private PagedGridView mAppPager;
	private LeoPopMenu mLeoPopMenu;

	private BaseInfo mLastSelectApp;
	private Object mLock = new Object();
	private boolean mShouldLockOnRestart = true;
	private String[] mSortType;

	public static final int DEFAULT_SORT = 0;
	public static final int NAME_SORT = 1;
	public static final int INSTALL_TIME_SORT = 2;
	private int mCurSortType = DEFAULT_SORT;

	public static final int REQUEST_CODE_LOCK = 9999;
	public static final int REQUEST_CODE_OPTION = 1001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_app_list);
		AppLoadEngine.getInstance(this).registerAppChangeListener(this);
		initUI();
	}

	@Override
	protected void onResume() {
		loadData();
		super.onResume();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		AppLoadEngine.getInstance(this).unregisterAppChangeListener(this);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (mMaskLayer != null && mMaskLayer.getVisibility() == View.VISIBLE) {
			mMaskLayer.setVisibility(View.GONE);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		try {
			super.onRestoreInstanceState(savedInstanceState);
		} catch (Exception e) {

		}
	}

	private void initUI() {
		mSortType = getResources().getStringArray(R.array.sort_type);
		mCurSortType = AppMasterPreference.getInstance(this).getSortType();

		mInflater = LayoutInflater.from(this);

		if (AppMasterPreference.getInstance(this).isFisrtUseLocker()) {
			mMaskLayer = findViewById(R.id.mask_layer);
			mMaskLayer.setOnClickListener(this);
			mMaskLayer.setVisibility(View.VISIBLE);
			AppMasterPreference.getInstance(this).setLockerUsed();
		}

		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_lock);
		mTtileBar.openBackView();
		mTtileBar.setOptionImage(R.drawable.selector_applock_setting);
		mTtileBar.setOptionImageVisibility(View.VISIBLE);
		mTtileBar.setOptionListener(this);
		mTtileBar.setSpinerVibility(View.VISIBLE);
		mTtileBar.setSpinerListener(this);
		mTtileBar.setSpinerText(mSortType[mCurSortType]);

		mLockedList = new ArrayList<BaseInfo>();
		mUnlockList = new ArrayList<BaseInfo>();
		mAppPager = (PagedGridView) findViewById(R.id.pager_unlock);
		mAppPager.setItemClickListener(this);
	}

	private void loadData() {
		if (AppMasterPreference.getInstance(this).isFisrtUseLocker()) {
			mMaskLayer.setVisibility(View.VISIBLE);
			mMaskLayer.setOnClickListener(this);
		}
		mUnlockList.clear();
		mLockedList.clear();
		ArrayList<AppDetailInfo> list = AppLoadEngine.getInstance(this)
				.getAllPkgInfo();
		List<String> lockList = AppMasterPreference.getInstance(this)
				.getLockedAppList();
		for (AppDetailInfo appDetailInfo : list) {
			if (appDetailInfo.getPkg().equals(this.getPackageName()))
				continue;
			if (lockList.contains(appDetailInfo.getPkg())) {
				appDetailInfo.setLocked(true);
				mLockedList.add(appDetailInfo);
			} else {
				appDetailInfo.setLocked(false);
				mUnlockList.add(appDetailInfo);
			}
		}
		Collections.sort(mLockedList, new LockedAppComparator(lockList));
		if (mCurSortType == DEFAULT_SORT) {
			Collections.sort(mUnlockList, new DefalutAppComparator());
		} else if (mCurSortType == NAME_SORT) {
			Collections.sort(mUnlockList, new NameComparator());
		} else if (mCurSortType == INSTALL_TIME_SORT) {
			Collections.sort(mUnlockList, new InstallTimeComparator());
		}

		ArrayList<BaseInfo> resault = new ArrayList<BaseInfo>(mLockedList);
		resault.addAll(mUnlockList);

		int rowCount = getResources().getInteger(R.integer.gridview_row_count);
		mAppPager.setDatas(resault, 4, rowCount);

		updateLockText();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		animateItem(view);
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

			// to set view unlocked
			((LockImageView) view.findViewById(R.id.iv_app_icon))
					.setLocked(false);

			SDKWrapper.addEvent(this, LeoStat.P1, "app", "unlock: "
					+ mLastSelectApp.getPkg());
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
			// to set view lock
			((LockImageView) view.findViewById(R.id.iv_app_icon))
					.setLocked(true);

			SDKWrapper.addEvent(this, LeoStat.P1, "app", " lock: "
					+ mLastSelectApp.getPkg());
		}
		saveLockList();
	}

	private void animateItem(View view) {

		AnimatorSet as = new AnimatorSet();
		as.setDuration(300);
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f,
				0.8f, 1f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f,
				0.8f, 1f);
		as.playTogether(scaleX, scaleY);
		as.start();
	}

	private void saveLockList() {
		new Thread(new PushLockedListTask()).start();
	}

	private void updateLockText() {
		int unlockSize = mUnlockList.size();
		int lockSize = mLockedList.size();
	}

	private class PushLockedListTask implements Runnable {
		@Override
		public void run() {
			synchronized (mLock) {
				List<String> list = new ArrayList<String>();
				for (BaseInfo info : AppLockListActivity.this.mLockedList) {
					list.add(info.getPkg());
				}
				List<String> recommendList = AppLoadEngine.getInstance(
						getApplicationContext()).getRecommendLockList();

				int count = 0;
				for (String string : list) {
					if (recommendList.contains(string))
						count++;
				}

				AppMasterPreference pref = AppMasterPreference
						.getInstance(AppLockListActivity.this);

				pref.setRecommendLockPercent(((float) count)
						/ recommendList.size());

				pref.setLockedAppList(list);
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
		case R.id.tv_option_image:
			Intent intent = new Intent(this, LockOptionActivity.class);
			startActivityForResult(intent, REQUEST_CODE_OPTION);
			break;
		case R.id.layout_right:
			if (mLeoPopMenu == null) {
				mLeoPopMenu = new LeoPopMenu();
				mLeoPopMenu.setAnimation(R.style.CenterEnterAnim);
				mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if (mCurSortType == DEFAULT_SORT) {
							if (position == 0) {
								mCurSortType = NAME_SORT;
							} else if (position == 1) {
								mCurSortType = INSTALL_TIME_SORT;
							}
						} else if (mCurSortType == NAME_SORT) {
							if (position == 0) {
								mCurSortType = DEFAULT_SORT;
							} else if (position == 1) {
								mCurSortType = INSTALL_TIME_SORT;
							}
						} else {
							if (position == 0) {
								mCurSortType = DEFAULT_SORT;
							} else if (position == 1) {
								mCurSortType = NAME_SORT;
							}
						}
						loadData();
						AppMasterPreference.getInstance(
								AppLockListActivity.this).setSortType(
								mCurSortType);
						mTtileBar.setSpinerText(mSortType[mCurSortType]);
						mLeoPopMenu.dismissSnapshotList();
					}
				});
			}
			mLeoPopMenu.setPopMenuItems(getPopMenuItems());
			mLeoPopMenu.showPopMenu(this,
					mTtileBar.findViewById(R.id.layout_right), null, null);
			break;
		case R.id.mask_layer:
			mMaskLayer.setVisibility(View.INVISIBLE);
			AppMasterPreference.getInstance(this).setLockerUsed();
			break;
		}
	}

	private List<String> getPopMenuItems() {
		List<String> listItems = new ArrayList<String>();

		if (mCurSortType == DEFAULT_SORT) {
			listItems.add(mSortType[NAME_SORT]);
			listItems.add(mSortType[INSTALL_TIME_SORT]);
		} else if (mCurSortType == NAME_SORT) {
			listItems.add(mSortType[DEFAULT_SORT]);
			listItems.add(mSortType[INSTALL_TIME_SORT]);
		} else {
			listItems.add(mSortType[DEFAULT_SORT]);
			listItems.add(mSortType[NAME_SORT]);
		}
		return listItems;
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

	public static class NameComparator implements Comparator<BaseInfo> {

		@Override
		public int compare(BaseInfo lhs, BaseInfo rhs) {
			return Collator.getInstance().compare(
					trimString(lhs.getAppLabel()),
					trimString(rhs.getAppLabel()));
		}

		private String trimString(String s) {
			return s.replaceAll("\u00A0", "").trim();
		}

	}
	
	

	@Override
	protected void onNewIntent(Intent intent) {
		LeoLog.e("xxxx", "onNewIntent");
		super.onNewIntent(intent);
	}

	public static class InstallTimeComparator implements Comparator<BaseInfo> {

		@Override
		public int compare(BaseInfo lhs, BaseInfo rhs) {
			if (lhs.installTime > rhs.installTime) {
				return -1;
			} else if (lhs.installTime < rhs.installTime) {
				return 1;
			} else {
				return Collator.getInstance().compare(
						trimString(lhs.getAppLabel()),
						trimString(rhs.getAppLabel()));
			}
		}

		private String trimString(String s) {
			return s.replaceAll("\u00A0", "").trim();
		}
	}

	public static class DefalutAppComparator implements Comparator<BaseInfo> {
		@Override
		public int compare(BaseInfo lhs, BaseInfo rhs) {
			if (lhs.topPos > -1 && rhs.topPos < 0) {
				return -1;
			} else if (lhs.topPos < 0 && rhs.topPos > -1) {
				return 1;
			} else if (lhs.topPos > -1 && rhs.topPos > -1) {
				return lhs.topPos - rhs.topPos;
			}

			if (lhs.isSystemApp() && !rhs.isSystemApp()) {
				return -1;
			} else if (!lhs.isSystemApp() && rhs.isSystemApp()) {
				return 1;
			}

			return Collator.getInstance().compare(
					trimString(lhs.getAppLabel()),
					trimString(rhs.getAppLabel()));
		}

		private String trimString(String s) {
			return s.replaceAll("\u00A0", "").trim();
		}
	}

	@Override
	public void onActivityCreate() {
		LeoLog.e("AppLockListActivity", "onActivityCreate");
		// showLockPage();
	}

	@Override
	public void onActivityRestart() {
		LeoLog.e("AppLockListActivity", "onActivityRestart");
		if (mShouldLockOnRestart) {
			showLockPage();
		} else {
			mShouldLockOnRestart = true;
		}
	}

	private void showLockPage() {
		LeoLog.e("AppLockListActivity", "showLockPage");
		Intent intent = new Intent(this, LockScreenActivity.class);
		int lockType = AppMasterPreference.getInstance(this).getLockType();
		if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_PASSWD);
		} else {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_GESTURE);
		}
		intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
				LockFragment.FROM_SELF);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		startActivityForResult(intent, REQUEST_CODE_LOCK);
	}

	@Override
	public void onActivityResault(int requestCode, int resultCode) {
		LeoLog.e("AppLockListActivity", "onActivityResault: requestCode = "
				+ requestCode + "    resultCode = " + resultCode);
		if (REQUEST_CODE_LOCK == requestCode) {
			mShouldLockOnRestart = false;
		} else if (REQUEST_CODE_OPTION == requestCode) {
			mShouldLockOnRestart = false;
		}
	}

}
