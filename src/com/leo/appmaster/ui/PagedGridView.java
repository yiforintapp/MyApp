package com.leo.appmaster.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.ui.DragGridView.AnimEndListener;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PagedGridView extends LinearLayout {

	private List<BaseInfo> mData;
	private int mCellX, mCellY;
	private ViewPager mViewPager;
	private CirclePageIndicator mIndicator;
	private LayoutInflater mInflater;

	private PagerAdapter mAdapter;
	private int mPageItemCount;
	private ArrayList<DragGridView> mGridViewList;
	private ArrayList<List<BaseInfo>> mPageDatas;

	private OnItemClickListener mListener;
	private AnimEndListener mAnimListener;
	private int mPageCount;

	public PagedGridView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mInflater = LayoutInflater.from(context);
	}

	public void setDatas(List<BaseInfo> data, int cellX, int cellY) {
		mData = data;
		mCellX = cellX;
		mCellY = cellY;
		mPageItemCount = mCellX * mCellY;
		mPageCount = (int) Math.ceil(((double) mData.size())
				/ (mCellX * mCellY));
		updateUI();
	}

	public void setAnimListener(AnimEndListener listener) {
		mAnimListener = listener;
		if (mGridViewList != null && !mGridViewList.isEmpty()) {
			for (DragGridView gridView : mGridViewList) {
				gridView.setOnAnimEndListener(listener);
			}
		}
	}

	private void updateUI() {
		int itemCounts[] = new int[mPageCount];
		int i;
		for (i = 0; i < itemCounts.length; i++) {
			if (i == itemCounts.length - 1) {
				itemCounts[i] = mData.size() / mPageItemCount;
			} else {
				itemCounts[i] = mPageItemCount;
			}
		}

		mGridViewList = new ArrayList<DragGridView>();
		mPageDatas = new ArrayList<List<BaseInfo>>();

		for (i = 0; i < mPageCount; i++) {
			GridviewAdapter adapter = null;
			List<BaseInfo> pageData = null;
			DragGridView gridView = (DragGridView) mInflater.inflate(
					R.layout.grid_page_item, mViewPager, false);
			if (i == mPageCount - 1) {
				pageData = copyFrom(mData.subList(i * mPageItemCount,
						mData.size()));
				adapter = new GridviewAdapter(pageData, i);
				gridView.setAdapter(adapter);

			} else {
				pageData = copyFrom(mData.subList(i * mPageItemCount, (i + 1)
						* mPageItemCount));
				adapter = new GridviewAdapter(pageData, i);
				gridView.setAdapter(adapter);
			}
			if (mListener != null) {
				gridView.setOnItemClickListener(mListener);
			}
			if (mAnimListener != null) {
				gridView.setOnAnimEndListener(mAnimListener);
			}
			mGridViewList.add(gridView);
			mPageDatas.add(pageData);
		}

		mAdapter = new DataPagerAdapter(mGridViewList);
		mViewPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mViewPager);
	}

	public void notifyChange(List<BaseInfo> list) {
		updateUI();
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
			for (DragGridView gridView : mGridViewList) {
				gridView.setOnItemClickListener(mListener);
			}
		}
	}

	@Override
	protected void onFinishInflate() {
		mInflater.inflate(R.layout.paged_gridview, this, true);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		super.onFinishInflate();
	}

	private class DataPagerAdapter extends PagerAdapter {

		List<DragGridView> pagerList;

		public DataPagerAdapter(ArrayList<DragGridView> viewList) {
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
			container.addView(pagerList.get(position), 0);
			return pagerList.get(position);
		}
	}

	private class GridviewAdapter extends BaseAdapter implements
			DragGridBaseAdapter {
		private int mHidePosition = -1;
		List<BaseInfo> mList;

		private int mCurPage;

		public GridviewAdapter(List<BaseInfo> list, int page) {
			super();
			mCurPage = page;
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
		public void removeItem(BaseInfo removeApp) {
			boolean re = mList.remove(removeApp);
			BaseInfo temp = null;
			if (mCurPage < mPageCount - 1) {
				int page = mCurPage;
				for (; page != mPageCount - 1; page++) {
					List<BaseInfo> nextPage = mPageDatas.get(page + 1);
					temp = nextPage.remove(0);
					mPageDatas.get(page).add(temp);
				}
			}
			for (DragGridView gridView : mGridViewList) {
				((GridviewAdapter) gridView.getAdapter())
						.notifyDataSetChanged();
			}

		}
	}
}
