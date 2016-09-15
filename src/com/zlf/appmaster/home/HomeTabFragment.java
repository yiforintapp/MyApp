package com.zlf.appmaster.home;

import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.QStringRequest;
import com.zlf.appmaster.client.StockQuotationsClient;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.WinTopItem;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.stockIndex.StockIndexDetailActivity;
import com.zlf.appmaster.ui.BounceBackViewPager;
import com.zlf.appmaster.ui.HorizontalListView;
import com.zlf.appmaster.ui.stock.StockTextView;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.PrefConst;
import com.zlf.appmaster.utils.Utilities;
import com.zlf.appmaster.utils.VolleyTool;
import com.zlf.banner.Banner;
import com.zlf.banner.BannerConfig;
import com.zlf.banner.listener.OnBannerClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/9/8.
 */
public class HomeTabFragment extends BaseFragment {
    private final static String TAG = "HomeTabFragment";
    public final static String WINTOP = "APP_WIN";

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
    private BounceBackViewPager mViewPager;
    private ScrollPageAdapter mPageAdapter;
    private ScrollPageChangeListener mPageChangeLister;
    private List<View> mViews;

    private HorizontalListView mHlistview;
    private WinTopAdapter mWinAdapter;

    private int mLastPosition = 0;

    public static final String[][] ITEM_SHOW = new String[][]{
            {Constants.JIN_GUI_INFO_PRONAME, Constants.JIN_GUI_INFO_MINUTE_PRONAME, Constants.JIN_GUI_INFO_KLINE_PRONAME},
            {Constants.QI_LU_INFO_PRONAME, Constants.QI_LU_INFO_MINUTE_PRONAME, Constants.QI_LU_INFO_KLINE_PRONAME},
            {Constants.WAI_HUI_INFO_PRONAME, Constants.WAI_HUI_INFO_MINUTE_PRONAME, Constants.WAI_HUI_INFO_KLINE_PRONAME},
            {Constants.ZHI_GOLD_INFO_PRONAME, Constants.ZHI_GOLD_INFO_MINUTE_PRONAME, Constants.ZHI_GOLD_INFO_KLINE_PRONAME},
            {Constants.SHANG_HAI_FUTURES_INFO_PRONAME, Constants.SHF_INFO_MINUTE_PRONAME, Constants.SHF_INFO_KLINE_PRONAME},
            {Constants.LME_INFO_PRONAME, Constants.LME_INFO_MINUTE_PRONAME, Constants.LME_INFO_KLINE_PRONAME},
            {Constants.NYMEX_INFO_PRONAME, Constants.NYMEX_INFO_MINUTE_PRONAME, Constants.NYMEX_INFO_KLINE_PRONAME},
            {Constants.COMEX_INFO_PRONAME, Constants.COMEX_INFO_MINUTE_PRONAME, Constants.COMEX_INFO_KLINE_PRONAME}};

    private int mCurrentItem = 0;
    private int mTotalCount = ITEM_SHOW.length;


    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void onInitUI() {
        mStockClient = StockQuotationsClient.getInstance(mActivity);
        mViewPager = (BounceBackViewPager) findViewById(R.id.my_viewpager);
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

        mHlistview = (HorizontalListView) findViewById(R.id.h_listview);
        mWinAdapter = new WinTopAdapter(mActivity);
        mHlistview.setAdapter(mWinAdapter);
        setWinTopData();
        loadWinTopData();


    }

    private void loadWinTopData() {
        String url = Constants.ADDRESS + Constants.APPSERVLET + Constants.DATA + WINTOP;
        LeoLog.d(TAG, "check update url is : " + url);

        QStringRequest stringRequest = new QStringRequest(
                Request.Method.GET, url, null, new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                LeoLog.d(TAG, "check update requestFinished version is : " + s);

                if (!Utilities.isEmpty(s) && !s.equals("-1")) {
                    saveWinTop(s);
                    setWinTopData();
                }
            }


        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                LeoLog.d(TAG, "check update err");
            }

        });

        // callAll的时候使用
        VolleyTool.getInstance(mActivity).getRequestQueue()
                .add(stringRequest);
    }

    private void setWinTopData() {

        String s = LeoSettings.getString(PrefConst.WIN_TOP_STRING, Constants.DEFAULT_WIN_TOP);
        List<WinTopItem> list = new ArrayList<WinTopItem>();

        String[] groups = s.split(";");
        for (int i = 0; i < groups.length; i++) {
            String group = groups[i];
            if (!Utilities.isEmpty(group)) {
                WinTopItem item = new WinTopItem();
                String name = group.split("_")[0];
                String price = group.split("_")[1];

                item.setWinName(name);
                item.setWinPrice(Double.valueOf(price));
                list.add(item);
            }
        }

        if (list.size() > 0) {
            mWinAdapter.setList(list);
            mWinAdapter.notifyDataSetChanged();
        }

    }

    private void saveWinTop(String s) {
        LeoSettings.setString(PrefConst.WIN_TOP_STRING, s);
    }

    @Override
    public void onResume() {
        super.onResume();
        requestData();
    }

    /**
     * 请求数据
     */
    private void requestData() {
        mProgressBar.setVisibility(View.VISIBLE);
        mStockClient.requestNewIndexAll(new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {
                Object[] objectArray = (Object[]) object;
                mData.clear();
                mData.addAll((List<StockIndex>) objectArray[0]);
                mData.add(0, null);

                mProgressBar.setVisibility(View.GONE);
                if (mData != null && mData.size() > 1) {
                    mData.remove(0);
                    mCount = mData.size() / 3 + 1;
                    mLastPosition = 0;
                    createIndicator();
                    mViews.clear();
                    addViews(mData);
                    mPageAdapter = new ScrollPageAdapter();
                    mViewPager.setAdapter(null);
                    mViewPager.setAdapter(mPageAdapter);
                    mPageChangeLister = new ScrollPageChangeListener();
                    mViewPager.setOnPageChangeListener(mPageChangeLister);
                    mPageAdapter.notifyDataSetChanged();
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
        }, Constants.MY_DATA_URL.concat(ITEM_SHOW[mCurrentItem][0]));

    }

    private void addViews(List<StockIndex> lists) {
        int flag = 3;
        int extra;
        int count = mData.size() / flag;
        List<StockIndex> mNewList;
        List<StockIndex> mCopyList;

        if (mData.size() % flag == 0) {
            for (int i = 0; i < count; i++) {
                mCopyList = lists;
                extra = 0;
                mNewList = mCopyList.subList(i * flag, i * flag + 3);
                setData(mNewList, extra);
            }
            extra = 3;
            mCopyList = null;
            setData(mCopyList, extra);
        } else {
            for (int i = 0; i < count; i++) {
                mCopyList = lists;
                extra = 0;
                mNewList = mCopyList.subList(i * flag, i * flag + 3);
                setData(mNewList, extra);
            }
            extra = mData.size() % flag;
            mCopyList = lists;
            mNewList = mCopyList.subList(mData.size() - extra, mData.size());
            setData(mNewList, extra);
        }
    }

    private void setData(List<StockIndex> list, int extra) {
        ViewGroup page = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.horizontal_item, null);
        if (extra == 3) {
            StockIndex index = null;
            View viewTwo = (View) page.findViewById(R.id.view_two);
            viewTwo.setVisibility(View.INVISIBLE);
            View viewThree = (View) page.findViewById(R.id.view_three);
            viewThree.setVisibility(View.INVISIBLE);
            TextView textView = (TextView) page.findViewById(R.id.change_one);
            textView.setVisibility(View.VISIBLE);
            textView.setOnClickListener(new ItemClickListener(index));

            mViews.add(page);

            return;
        }
        RelativeLayout parentOne;
        TextView name_one;
        StockTextView price_one;
        StockTextView percent_one;
        RelativeLayout parentTwo;
        TextView name_two;
        StockTextView price_two;
        StockTextView percent_two;
        RelativeLayout parentThree;
        TextView name_three;
        StockTextView price_three;
        StockTextView percent_three;
        for (int i = 0; i < list.size(); i++) {
            StockIndex index = list.get(i);
            if (i == 0) {
                name_one = (TextView) page.findViewById(R.id.name_one);
                price_one = (StockTextView) page.findViewById(R.id.price_one);
                percent_one = (StockTextView) page.findViewById(R.id.percent_one);
                setItemData(index, name_one, price_one, percent_one);
                parentOne = (RelativeLayout) page.findViewById(R.id.parent_one);
                parentOne.setOnClickListener(new ItemClickListener(index));
            } else if (i == 1) {
                name_two = (TextView) page.findViewById(R.id.name_two);
                price_two = (StockTextView) page.findViewById(R.id.price_two);
                percent_two = (StockTextView) page.findViewById(R.id.percent_two);
                setItemData(index, name_two, price_two, percent_two);
                parentTwo = (RelativeLayout) page.findViewById(R.id.parent_two);
                parentTwo.setOnClickListener(new ItemClickListener(index));
            } else {
                name_three = (TextView) page.findViewById(R.id.name_three);
                price_three = (StockTextView) page.findViewById(R.id.price_three);
                percent_three = (StockTextView) page.findViewById(R.id.percent_three);
                setItemData(index, name_three, price_three, percent_three);
                parentThree = (RelativeLayout) page.findViewById(R.id.parent_three);
                parentThree.setOnClickListener(new ItemClickListener(index));
            }
        }
        TextView textView;
        if (extra == 1) {
            StockIndex index = null;
            textView = (TextView) page.findViewById(R.id.change_two);
            textView.setVisibility(View.VISIBLE);
            textView.setOnClickListener(new ItemClickListener(index));
            View view = (View) page.findViewById(R.id.view_three);
            view.setVisibility(View.INVISIBLE);
        } else if (extra == 2) {
            StockIndex index = null;
            textView = (TextView) page.findViewById(R.id.change_three);
            textView.setVisibility(View.VISIBLE);
            textView.setOnClickListener(new ItemClickListener(index));
        }
        mViews.add(page);
    }

    private class ItemClickListener implements View.OnClickListener {
        private StockIndex mItem;

        public ItemClickListener(StockIndex item) {
            this.mItem = item;
        }

        @Override
        public void onClick(View v) {
            if (null != mItem) {
                Intent intent = new Intent(mActivity, StockIndexDetailActivity.class);
                intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_INDEXCODE, mItem.getCode());
                intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_INDEXNAME, mItem.getName());
                intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_OPEN_INDEX, mItem.getTodayIndex());
                intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_YESTERDAY_INDEX, mItem.getYesterdayIndex());
                intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_NOW_INDEX, mItem.getNowIndex());
                intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_HIGH_INDEX, mItem.getHighestIndex());
                intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_LOW_INDEX, mItem.getLowestIndex());
                intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_GUO_XIN, true);
                intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_TAB_MINITE_WHAT, ITEM_SHOW[mCurrentItem][1]);
                intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_TAB_KLINE_WHAT, ITEM_SHOW[mCurrentItem][2]);
                mActivity.startActivity(intent);
            } else {
                int newItem = 0;
                while (mCurrentItem == newItem) {
                    Random random = new Random();
                    newItem = random.nextInt(mTotalCount);
                }
                mCurrentItem = newItem;
                requestData();
            }
        }

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

        @Override
        public int getCount() {
            return mViews.size();
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
            if (arg1 < mViews.size()) {
                ((ViewPager) arg0).removeView(mViews.get(arg1));
            }
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mViews.get(arg1));
            return mViews.get(arg1);
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
            mIndicatorImages.get(mLastPosition).setImageResource(mIndicatorUnselectedResId);
            mIndicatorImages.get(arg0).setImageResource(mIndicatorSelectedResId);
            mLastPosition = arg0;
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
