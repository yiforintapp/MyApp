package com.leo.appmaster.appmanage;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.PageTransformer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.leo.appmaster.ui.LeoGridBaseAdapter;
import com.leo.appmaster.ui.LeoAppViewPager;
import com.leo.appmaster.ui.PageIndicator;

@SuppressLint("InflateParams")
public class AppListActivity extends Activity implements AppChangeListener,
		OnClickListener, OnItemClickListener {

	View mLoadingView;

	private CommonTitleBar mTtileBar;
	private PageIndicator mPageIndicator;
	private LeoAppViewPager mViewPager;
	private View mPagerContain;

	List<AppDetailInfo> mAppDetails;
	LayoutInflater mInflater;

	private int pageItemCount = 20;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);

		AppLoadEngine.getInstance(this).registerAppChangeListener(this);

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
		// mViewPager.setPageTransformer(true, new DepthPageTransformer());

		fillData();
	}

	public void fillData() {
		mAppDetails = AppLoadEngine.getInstance(this).getAllPkgInfo();
		mLoadingView.setVisibility(View.INVISIBLE);
		int pageCount = Math.round(((long) mAppDetails.size()) / pageItemCount);
		int itemCounts[] = new int[pageCount];
		int i;
		for (i = 0; i < itemCounts.length; i++) {
			if (i == itemCounts.length - 1) {
				itemCounts[i] = mAppDetails.size() / pageItemCount;
			} else {
				itemCounts[i] = pageItemCount;
			}
		}
		ArrayList<View> viewList = new ArrayList<View>();

		for (i = 0; i < pageCount; i++) {
		    GridView gridView = (GridView) mInflater.inflate(
					R.layout.appmanage_gridview, mViewPager, false);
			if (i == pageCount) {
				gridView.setAdapter(new DataAdapter(mAppDetails, i
						* pageItemCount, mAppDetails.size() - 1));
			} else {
				gridView.setAdapter(new DataAdapter(mAppDetails, i
						* pageItemCount, i * pageItemCount + pageItemCount - 1));
			}
			gridView.setOnItemClickListener(this);
			viewList.add(gridView);
		}
		mViewPager.setAdapter(new DataPagerAdapter(viewList));
		mPageIndicator.setViewPager(mViewPager);
		mPagerContain.setVisibility(View.VISIBLE);
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

		public DataAdapter(List<AppDetailInfo> appDetails, int start, int end) {
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

			AppDetailInfo info = mAppDetails.get(startLoc + position);
			imageView.setImageDrawable(info.getAppIcon());
			textView.setText(info.getAppLabel());
			convertView.setTag(info.getPkg());
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

	public class DepthPageTransformer implements PageTransformer {
		private static final float MIN_SCALE = 0.75f;

		@SuppressLint("NewApi")
		@Override
		public void transformPage(View view, float position) {
			int pageWidth = view.getWidth();
			if (position < -1) { // [-Infinity,-1)
									// This page is way off-screen to the left.
				view.setAlpha(0);
			} else if (position <= 0) { // [-1,0]
										// Use the default slide transition when
										// moving to the left page
				view.setAlpha(1);
				view.setTranslationX(0);
				view.setScaleX(1);
				view.setScaleY(1);
			} else if (position <= 1) { // (0,1]
										// Fade the page out.
				view.setAlpha(1 - position);
				// Counteract the default slide transition
				view.setTranslationX(pageWidth * -position);
				// Scale the page down (between MIN_SCALE and 1)
				float scaleFactor = MIN_SCALE + (1 - MIN_SCALE)
						* (1 - Math.abs(position));
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);
			} else { // (1,+Infinity]
						// This page is way off-screen to the right.
				view.setAlpha(0);

			}
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String pkg = (String) view.getTag();
		Intent intent = new Intent(this, AppDetailActivity.class);
		intent.putExtra(AppDetailActivity.EXTRA_LOAD_PKG, pkg);
		this.startActivity(intent);
	}

	@Override
	public void onAppChanged(ArrayList<AppDetailInfo> changes, int type) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				fillData();
			}
		});
	}

}
