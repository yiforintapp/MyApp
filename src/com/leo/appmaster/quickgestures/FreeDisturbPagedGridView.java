
package com.leo.appmaster.quickgestures;

import java.util.ArrayList;
import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.ui.LeoAppViewPager;
import com.leo.appmaster.ui.LeoApplistCirclePageIndicator;
import com.leo.appmaster.ui.LockImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.PageTransformer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FreeDisturbPagedGridView extends LinearLayout {

    private int mCellX, mCellY;
    private LeoAppViewPager mViewPager;
    private LeoApplistCirclePageIndicator mIndicator;
    private LayoutInflater mInflater;

    private PagerAdapter mAdapter;
    private int mPageItemCount;
    private ArrayList<GridView> mGridViewList;
    private ArrayList<List<AppInfo>> mPageDatas;

    private OnItemClickListener mClickListener;
    private OnTouchListener mTouchListener;
    private int mPageCount;
    private String mFlag;

    public FreeDisturbPagedGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mInflater = LayoutInflater.from(context);
    }

    public void setDatas(List<AppInfo> data, int cellX, int cellY) {
        mCellX = cellX;
        mCellY = cellY;
        mPageItemCount = mCellX * mCellY;
        updateUI(data);
    }

    public void setFlag(String flag) {
        mFlag = flag;
    }

    private void updateUI(List<AppInfo> data) {
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

        mGridViewList = new ArrayList<GridView>();
        mPageDatas = new ArrayList<List<AppInfo>>();

        for (i = 0; i < mPageCount; i++) {
            GridviewAdapter adapter = null;
            List<AppInfo> pageData = null;
            GridView gridView = (GridView) mInflater.inflate(
                    R.layout.dialog_free_disturb_app_grid_page_item, mViewPager, false);
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
            if (mClickListener != null) {
                gridView.setOnItemClickListener(mClickListener);
            }

            gridView.setOnTouchListener(mTouchListener);
            mGridViewList.add(gridView);
            mPageDatas.add(pageData);
        }

        mAdapter = new DataPagerAdapter();
        mViewPager.setAdapter(mAdapter);
        if (mPageCount > 1) {
            mIndicator.setViewPager(mViewPager);
        }

    }

    public void notifyChange(List<AppInfo> list) {
        updateUI(list);
    }

    private List<AppInfo> copyFrom(List<AppInfo> source) {
        ArrayList<AppInfo> list = null;
        if (source != null) {
            list = new ArrayList<AppInfo>();
            AppInfo item;
            for (AppInfo info : source) {
                item = new AppInfo();
                item.packageName = info.packageName;
                item.icon = info.icon;
                item.label = info.label;
                item.isLocked = info.isLocked;
                list.add(item);
            }
        }
        return list;
    }

    public void setItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
        if (mGridViewList != null) {
            for (GridView gridView : mGridViewList) {
                gridView.setOnItemClickListener(mClickListener);
            }
        }
    }

    public void setItemTouchListener(OnTouchListener listener) {
        mTouchListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        mInflater.inflate(R.layout.dialog_free_disturb_app_paged_gridview, this, true);
        mViewPager = (LeoAppViewPager) findViewById(R.id.pager_dialog);
        mIndicator = (LeoApplistCirclePageIndicator) findViewById(R.id.indicator_dialog);
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

    private class GridviewAdapter extends BaseAdapter {
        List<AppInfo> mList;

        private int mPageIndex;

        public GridviewAdapter(List<AppInfo> list, int page) {
            super();
            mPageIndex = page;
            initData(list);
        }

        private void initData(List<AppInfo> list) {
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

        class ViewHolder {
            LockImageView imageView;
            TextView textView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (vh == null) {
                vh = new ViewHolder();
                convertView = mInflater.inflate(
                        R.layout.dialog_free_disturb_app_paged_gridview_app_item, null);
                vh.imageView = (LockImageView) convertView
                        .findViewById(R.id.iv_app_icon_free);
                vh.textView = (TextView) convertView
                        .findViewById(R.id.tv_app_name_free);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            AppInfo info = mList.get(position);
            vh.imageView.setDefaultRecommendApp(info.isLocked);
            vh.imageView.setImageDrawable(info.icon);
            vh.textView.setText(info.label);
            convertView.setTag(info);
            return convertView;
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

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {

        }
    }

}
