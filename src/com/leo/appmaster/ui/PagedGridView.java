package com.leo.appmaster.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.AppListActivity.DepthPageTransformer;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.ui.LeoGridView.AnimEndListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PagedGridView extends LinearLayout implements AnimEndListener {

	private int mCellX, mCellY;
	private LeoAppViewPager mViewPager;
	private CirclePageIndicator mIndicator;
	private LayoutInflater mInflater;

	private PagerAdapter mAdapter;
	private int mPageItemCount;
	private ArrayList<LeoGridView> mGridViewList;
	private ArrayList<List<BaseInfo>> mPageDatas;

	private OnItemClickListener mListener;
	private int mPageCount;

	public PagedGridView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mInflater = LayoutInflater.from(context);
	}

	public void setDatas(List<BaseInfo> data, int cellX, int cellY) {
		mCellX = cellX;
		mCellY = cellY;
		mPageItemCount = mCellX * mCellY;
		updateUI(data);
	}

	private void updateUI(List<BaseInfo> data) {
		mPageCount = (int) Math
				.ceil(((double) data.size()) / (mCellX * mCellY));
		int itemCounts[] = new int[mPageCount];
		int i;
		for (i = 0; i < itemCounts.length; i++) {
			if (i == itemCounts.length - 1) {
				itemCounts[i] = data.size() / mPageItemCount;
			} else {
				itemCounts[i] = mPageItemCount;
			}
		}

		mGridViewList = new ArrayList<LeoGridView>();
		mPageDatas = new ArrayList<List<BaseInfo>>();

		for (i = 0; i < mPageCount; i++) {
			GridviewAdapter adapter = null;
			List<BaseInfo> pageData = null;
			LeoGridView gridView = (LeoGridView) mInflater.inflate(
					R.layout.grid_page_item, mViewPager, false);
			if (i == mPageCount - 1) {
				pageData = copyFrom(data.subList(i * mPageItemCount,
						data.size()));
				adapter = new GridviewAdapter(pageData, i);
				gridView.setAdapter(adapter);

			} else {
				pageData = copyFrom(data.subList(i * mPageItemCount, (i + 1)
						* mPageItemCount));
				adapter = new GridviewAdapter(pageData, i);
				gridView.setAdapter(adapter);
			}
			if (mListener != null) {
				gridView.setOnItemClickListener(mListener);
			}
			gridView.setOnAnimEndListener(this);
			mGridViewList.add(gridView);
			mPageDatas.add(pageData);
		}

		mAdapter = new DataPagerAdapter();
		mViewPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mViewPager);
	}

	public void notifyChange(List<BaseInfo> list) {
		updateUI(list);
	}

	private List<BaseInfo> copyFrom(List<BaseInfo> source) {
		ArrayList<BaseInfo> list = null;
		if (source != null) {
			list = new ArrayList<BaseInfo>();
			BaseInfo item;
			for (BaseInfo info : source) {
				item = new BaseInfo();
				item.setPkg(info.getPkg());
				item.setAppIcon(info.getAppIcon());
				item.setAppLabel(info.getAppLabel());
				item.setLocked(info.isLocked());
				list.add(item);
			}
		}
		return list;
	}

	public void setGridviewItemClickListener(OnItemClickListener listener) {
		mListener = listener;
		if (mGridViewList != null) {
			for (LeoGridView gridView : mGridViewList) {
				gridView.setOnItemClickListener(mListener);
			}
		}
	}

	@Override
	protected void onFinishInflate() {
		mInflater.inflate(R.layout.paged_gridview, this, true);
		mViewPager = (LeoAppViewPager) findViewById(R.id.pager);
		// mViewPager.setPageTransformer(true, new DepthPageTransformer());
		mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		super.onFinishInflate();
	}

	private class DataPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mGridViewList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if (position < mGridViewList.size()) {
				container.removeView(mGridViewList.get(position));
			}
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mGridViewList.get(position), 0);
			return mGridViewList.get(position);
		}
	}

	private class GridviewAdapter extends BaseAdapter implements
			LeoGridBaseAdapter {
		private int mHidePosition = -1;
		List<BaseInfo> mList;

		private int mPageIndex;

		public GridviewAdapter(List<BaseInfo> list, int page) {
			super();
			mPageIndex = page;
			initData(list);
		}

		private void initData(List<BaseInfo> list) {
			mList = list;
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
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

			BaseInfo info = mList.get(position);
			imageView.setImageDrawable(info.getAppIcon());
			textView.setText(info.getAppLabel());
			convertView.setTag(info);

			if (position == mHidePosition) {
				convertView.setVisibility(View.INVISIBLE);
			}

			return convertView;
		}

		@Override
		public void reorderItems(int oldPosition, int newPosition) {
			BaseInfo temp = mList.get(oldPosition);
			if (oldPosition < newPosition) {
				for (int i = oldPosition; i < newPosition; i++) {
					Collections.swap(mList, i, i + 1);
				}
			} else if (oldPosition > newPosition) {
				for (int i = oldPosition; i > newPosition; i--) {
					Collections.swap(mList, i, i - 1);
				}
			}

			mList.set(newPosition, temp);
		}

		@Override
		public void setHideItem(int hidePosition) {
			this.mHidePosition = hidePosition;
			notifyDataSetChanged();
		}

		@Override
		public void removeItem(int position) {
			mList.remove(position);

			BaseInfo temp = null;
			if (mPageIndex < mPageCount - 1) {
				int page = mPageIndex;
				for (; page != mPageCount - 1; page++) {
					List<BaseInfo> nextPage = mPageDatas.get(page + 1);
					temp = nextPage.remove(0);
					mPageDatas.get(page).add(temp);
				}
			}
			// for (DragGridView gridView : mGridViewList) {
			// ((GridviewAdapter) gridView.getAdapter())
			// .notifyDataSetChanged();
			// }

			((GridviewAdapter) mGridViewList.get(mPageIndex).getAdapter())
					.notifyDataSetChanged();

		}
	}

	@Override
	public void onAnimEnd() {
		List<BaseInfo> list = mPageDatas.get(mPageCount - 1);
		if (list.size() == 0) {
			mGridViewList.remove(mPageCount - 1);
			mPageDatas.remove(mPageCount - 1);
			mPageCount--;

			int targetIndex = mViewPager.getCurrentItem();
			if (targetIndex == mPageCount) {
				targetIndex--;
			}
			mAdapter.notifyDataSetChanged();
			mViewPager.setCurrentItem(targetIndex);
			mIndicator.invalidate();
		}

		for (LeoGridView gridView : mGridViewList) {
			((GridviewAdapter) gridView.getAdapter()).notifyDataSetChanged();
		}
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
}
