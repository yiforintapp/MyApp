package com.leo.appmaster.appmanage;

import java.util.ArrayList;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.FolderItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoGridBaseAdapter;
import com.leo.appmaster.ui.LeoAppViewPager;
import com.leo.appmaster.ui.PageIndicator;

public class AppListActivity extends BaseActivity implements AppChangeListener,
		OnClickListener, OnItemClickListener {

	private View mLoadingView;
	private CommonTitleBar mTtileBar;
	private PageIndicator mPageIndicator;
	private LeoAppViewPager mViewPager;
	private View mPagerContain;
	private LayoutInflater mInflater;

	private List<BaseInfo> mAllItems;
	private List<BaseInfo> mFolderItems;
	private List<BaseInfo> mBusinessItems;
	private List<AppItemInfo> mAppDetails;
	private int pageItemCount = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);

		AppLoadEngine.getInstance(this).registerAppChangeListener(this);
		// animate=AnimationUtils.loadAnimation(AppListActivity.this,R.anim.locker_scale);
		intiUI();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppLoadEngine.getInstance(this).unregisterAppChangeListener(this);
	}

	private void intiUI() {
		mInflater = getLayoutInflater();
		mLoadingView = findViewById(R.id.rl_loading);

		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_manage);
		mTtileBar.openBackView();
		mTtileBar.setOptionTextVisibility(View.INVISIBLE);

		mPagerContain = findViewById(R.id.layout_pager_container);
		mPageIndicator = (PageIndicator) findViewById(R.id.indicator);
		mViewPager = (LeoAppViewPager) findViewById(R.id.pager);
		fillData();
	}

	public void fillData() {
		mAllItems = new ArrayList<BaseInfo>();

		// first, add four folders
		mFolderItems = new ArrayList<BaseInfo>();
		loadFolderData();
		mAllItems.addAll(mFolderItems);

		// second, add business items
		mBusinessItems = new ArrayList<BaseInfo>();
		loadBusinessData();

		// third, add all local apps
		mAppDetails = AppLoadEngine.getInstance(this).getAllPkgInfo();
		mAllItems.addAll(mAppDetails);

		// data load finished
		mLoadingView.setVisibility(View.INVISIBLE);
		int pageCount = Math.round(((long) mAllItems.size()) / pageItemCount);
		int itemCounts[] = new int[pageCount];
		int i;
		for (i = 0; i < itemCounts.length; i++) {
			if (i == itemCounts.length - 1) {
				itemCounts[i] = mAllItems.size() / pageItemCount;
			} else {
				itemCounts[i] = pageItemCount;
			}
		}
		ArrayList<View> viewList = new ArrayList<View>();

		for (i = 0; i < pageCount; i++) {
			GridView gridView = (GridView) mInflater.inflate(
					R.layout.grid_page_item, mViewPager, false);
			if (i == pageCount) {
				gridView.setAdapter(new DataAdapter(mAllItems, i
						* pageItemCount, mAppDetails.size() - 1));
			} else {
				gridView.setAdapter(new DataAdapter(mAllItems, i
						* pageItemCount, i * pageItemCount + pageItemCount - 1));
			}
			gridView.setOnItemClickListener(this);
			viewList.add(gridView);
		}
		mViewPager.setAdapter(new DataPagerAdapter(viewList));
		mPageIndicator.setViewPager(mViewPager);
		mPagerContain.setVisibility(View.VISIBLE);
	}

	/**
	 * we should judge load sync or not
	 */
	private void loadBusinessData() {

	}

	private void loadFolderData() {
		FolderItemInfo folder = null;
		// add system app folder
		folder = new FolderItemInfo();
		folder.type = BaseInfo.ITEM_TYPE_FOLDER;
		folder.folderType = FolderItemInfo.FOLDER_FLOW_SORT;
		folder.icon = getResources().getDrawable(R.drawable.folder);
		folder.label = getString(R.string.folder_sort_flow);
		mFolderItems.add(folder);
		// add running app folder
		folder = new FolderItemInfo();
		folder.type = BaseInfo.ITEM_TYPE_FOLDER;
		folder.folderType = FolderItemInfo.FOLDER_CAPACITY_SORT;
		folder.icon = getResources().getDrawable(R.drawable.folder);
		folder.label = getString(R.string.folder_sort_capacity);
		mFolderItems.add(folder);
		// add restore folder
		folder = new FolderItemInfo();
		folder.type = BaseInfo.ITEM_TYPE_FOLDER;
		folder.folderType = FolderItemInfo.FOLDER_BACKUP_RESTORE;
		folder.icon = getResources().getDrawable(R.drawable.folder);
		folder.label = getString(R.string.folder_backup_restore);
		mFolderItems.add(folder);
		// add business app folder
		folder = new FolderItemInfo();
		folder.type = BaseInfo.ITEM_TYPE_FOLDER;
		folder.folderType = FolderItemInfo.FOLDER_BUSINESS_APP;
		folder.icon = getResources().getDrawable(R.drawable.folder);
		folder.label = getString(R.string.folder_recommend);
		mFolderItems.add(folder);
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

	private class DataPagerAdapter extends PagerAdapter {
		List<View> pagerList;

		public DataPagerAdapter(ArrayList<View> viewList) {
			pagerList = viewList;
		}

		@Override
		public int getCount() {
			return pagerList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(pagerList.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = pagerList.get(position);
			container.addView(view);
			return view;
		}
	}

	private class DataAdapter extends BaseAdapter implements LeoGridBaseAdapter {

		int startLoc;
		int endLoc;

		public DataAdapter(List<BaseInfo> appDetails, int start, int end) {
			super();
			startLoc = start;
			endLoc = end;
		}

		@Override
		public int getCount() {
			return endLoc - startLoc + 1;
		}

		@Override
		public Object getItem(int position) {
			return mAppDetails.get(startLoc + position);
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

			BaseInfo info = mAllItems.get(startLoc + position);
			imageView.setImageDrawable(info.icon);
			textView.setText(info.label);
			convertView.setTag(info);
			return convertView;
		}

		@Override
		public void reorderItems(int oldPosition, int newPosition) {
		}

		@Override
		public void setHideItem(int hidePosition) {
		}

		@Override
		public void removeItem(int position) {
		}

	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		animateItem(view);
		BaseInfo itemInfo = (BaseInfo) view.getTag();
		handleItemClick(itemInfo);
	}

	private void handleItemClick(BaseInfo itemInfo) {
		Intent intent = null;
		switch (itemInfo.type) {
		case BaseInfo.ITEM_TYPE_NORMAL_APP:
			AppItemInfo appinfo = (AppItemInfo) itemInfo;
			intent = new Intent(this, AppDetailActivity.class);
			intent.putExtra(AppDetailActivity.EXTRA_LOAD_PKG,
					appinfo.packageName);
			this.startActivity(intent);
			break;
		case BaseInfo.ITEM_TYPE_FOLDER:
			intent = new Intent(this, FolderActivity.class);
			FolderItemInfo folderInfo = (FolderItemInfo) itemInfo;
			intent.putExtra("from_type", folderInfo.folderType);
			this.startActivity(intent);
			break;
		case BaseInfo.ITEM_TYPE_BUSINESS_APP:

			break;

		default:
			break;
		}
	}

	@Override
	public void onAppChanged(ArrayList<AppItemInfo> changes, int type) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fillData();
			}
		});

	}

}
