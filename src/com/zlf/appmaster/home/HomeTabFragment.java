package com.zlf.appmaster.home;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockQuotationsClient;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.ui.stock.StockTextView;
import com.zlf.banner.Banner;
import com.zlf.banner.BannerConfig;
import com.zlf.banner.listener.OnBannerClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/8.
 */
public class HomeTabFragment extends BaseFragment {
    private int mIndicatorMargin = BannerConfig.PADDING_SIZE;
    private int mIndicatorWidth = BannerConfig.INDICATOR_SIZE;
    private int mIndicatorHeight = BannerConfig.INDICATOR_SIZE;
    private int mIndicatorSelectedResId = R.drawable.selected_radius;
    private int mIndicatorUnselectedResId = R.drawable.gray_radius;
    private int mCount = 3;

    private List<ImageView> mIndicatorImages;
    private LinearLayout mIndicator;
    private Banner mBanner;
    private List<StockIndex> mData;
    private StockQuotationsClient mStockClient;
    private CircularProgressView mProgressBar;
    private ViewPager mViewPager;
    private List<View> mViews;

    private int lastPosition = 0;


    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void onInitUI() {
        mStockClient = StockQuotationsClient.getInstance(mActivity);
        mViewPager = (ViewPager) findViewById(R.id.my_viewpager);
        mViews = new ArrayList<View>();
        mIndicator = (LinearLayout) findViewById(R.id.my_indicator);
        mProgressBar = (CircularProgressView) findViewById(R.id.content_loading);
        mBanner = (Banner) findViewById(R.id.banner);
        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        String[] images = getResources().getStringArray(R.array.banner_url);
        mIndicatorImages = new ArrayList<ImageView>();
        mData = new ArrayList<StockIndex>();
        mBanner.setImages(images);//可以选择设置图片网址，或者资源文件，默认用Glide加载
        mBanner.setOnBannerClickListener(new OnBannerClickListener() {//设置点击事件
            @Override
            public void OnBannerClick(int position) {
                Toast.makeText(mActivity, "你点击了：" + position, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        requestData();
    }

    /**
     * 请求数据
     */
    private void requestData(){
        mProgressBar.setVisibility(View.VISIBLE);
        mStockClient.requestNewIndexAll(new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {
                Object[] objectArray = (Object[])object;
                mData.clear();
                mData.addAll((List<StockIndex>) objectArray[0]);
                mData.add(0,null);

                mProgressBar.setVisibility(View.GONE);
                if (mData != null && mData.size() > 1) {
                    mData.remove(0);
                    if(mData.size() % 3 == 0) {
                       mCount = mData.size() / 3;
                    } else {
                        mCount = mData.size() / 3 + 1;
                    }
                    createIndicator();
                    mViews.clear();
                    addViews(mData);
                    mViewPager.setAdapter(new ScrollPageAdapter(mViews));
                    mViewPager.setOnPageChangeListener(new ScrollPageChangeListener());
                    mIndicator.setVisibility(View.VISIBLE);
                    mViewPager.setVisibility(View.VISIBLE);
                } else {
                    mViewPager.setVisibility(View.GONE);
                    mIndicator.setVisibility(View.GONE);
                }

            }

            @Override
            public void onError(int errorCode, String errorString) {

                mProgressBar.setVisibility(View.GONE);
                mViewPager.setVisibility(View.GONE);
//                mEmptyView.setVisibility(View.VISIBLE);
            }
        }, Constants.MY_DATA_URL.concat(Constants.JIN_GUI_INFO_PRONAME));

    }

    private void addViews(List<StockIndex> lists) {
        int flag = 3;
        int count = mData.size() / flag;
        List<StockIndex> mNewList;
        List<StockIndex> mCopyList;

        if (mData.size() % flag == 0) {
            for (int i = 0; i < count; i++) {
                mCopyList = lists;
                mNewList = mCopyList.subList(i * flag, i * flag + 3);
                setData(mNewList);
            }
        } else {
            for (int i = 0; i < count; i++) {
                mCopyList = lists;
                mNewList = mCopyList.subList(i * flag, i * flag + 3);
                setData(mNewList);
            }
            int extra = mData.size() % flag;
            mCopyList = lists;
            mNewList = mCopyList.subList(mData.size() - extra, mData.size());
            setData(mNewList);
        }
    }

    private void setData(List<StockIndex> list) {
        TextView name_one;
        StockTextView price_one;
        StockTextView percent_one;
        TextView name_two;
        StockTextView price_two;
        StockTextView percent_two;
        TextView name_three;
        StockTextView price_three;
        StockTextView percent_three;
        ViewGroup page = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.horizontal_item, null);
        for (int i = 0; i < list.size(); i++) {
            StockIndex index = list.get(i);
            if (i == 0) {
                name_one = (TextView) page.findViewById(R.id.name_one);
                price_one = (StockTextView) page.findViewById(R.id.price_one);
                percent_one = (StockTextView) page.findViewById(R.id.percent_one);
                setItemData(index, name_one, price_one, percent_one);
            } else if (i == 1) {
                name_two = (TextView) page.findViewById(R.id.name_two);
                price_two = (StockTextView) page.findViewById(R.id.price_two);
                percent_two = (StockTextView) page.findViewById(R.id.percent_two);
                setItemData(index, name_two, price_two, percent_two);
            } else {
                name_three = (TextView) page.findViewById(R.id.name_three);
                price_three = (StockTextView) page.findViewById(R.id.price_three);
                percent_three = (StockTextView) page.findViewById(R.id.percent_three);
                setItemData(index, name_three, price_three, percent_three);
            }
        }
        mViews.add(page);
    }

    private void setItemData(StockIndex index, TextView name, StockTextView price, StockTextView percent) {
        if (null != index) {
            int riseInfo = index.getRiseInfo();
            name.setText(index.getName());
            price.setRiseInfo(riseInfo);
            price.setText(index.getCurPriceFormat());
            percent.setRiseInfo(riseInfo);
            percent.setText(index.getCurPercentFormat());
        }
    }

    class ScrollPageAdapter extends PagerAdapter {
        List<View> pageViews;

        public ScrollPageAdapter(List<View> pageViews) {
            this.pageViews = pageViews;
        }

        @Override
        public int getCount() {
            return pageViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(pageViews.get(arg1));
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(pageViews.get(arg1));
            return pageViews.get(arg1);
        }
    }

    class ScrollPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int arg0) {
            mIndicatorImages.get(lastPosition).setImageResource(mIndicatorUnselectedResId);
            mIndicatorImages.get(arg0).setImageResource(mIndicatorSelectedResId);
            lastPosition = arg0;
        }

    }

    private void createIndicator() {
        mIndicatorImages.clear();
        mIndicator.removeAllViews();
        for (int i = 0; i < mCount; i++) {
            ImageView imageView = new ImageView(mActivity);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight);
            params.leftMargin = mIndicatorMargin;
            params.rightMargin = mIndicatorMargin;
            if (i == 0) {
                imageView.setImageResource(mIndicatorSelectedResId);
            } else {
                imageView.setImageResource(mIndicatorUnselectedResId);
            }
            mIndicatorImages.add(imageView);
            mIndicator.addView(imageView, params);

        }
    }

}
