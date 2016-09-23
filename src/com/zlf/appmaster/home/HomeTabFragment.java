package com.zlf.appmaster.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockQuotationsClient;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.hometab.HomeJsonData;
import com.zlf.appmaster.hometab.HomeTabTopWebActivity;
import com.zlf.appmaster.login.HttpCallBackListener;
import com.zlf.appmaster.login.LoginActivity;
import com.zlf.appmaster.login.LoginHttpUtil;
import com.zlf.appmaster.login.ProductActivity;
import com.zlf.appmaster.model.DayNewsItem;
import com.zlf.appmaster.model.HomeBannerInfo;
import com.zlf.appmaster.model.WinTopItem;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.stockIndex.StockIndexDetailActivity;
import com.zlf.appmaster.stocknews.testWebViewActivity;
import com.zlf.appmaster.ui.BounceBackViewPager;
import com.zlf.appmaster.ui.HorizontalListView;
import com.zlf.appmaster.ui.MyViewPager;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.ui.dialog.StockSelectDialog;
import com.zlf.appmaster.ui.stock.StockTextView;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.utils.PrefConst;
import com.zlf.banner.Banner;
import com.zlf.banner.BannerConfig;
import com.zlf.banner.listener.OnBannerClickListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/8.
 */
public class HomeTabFragment extends BaseFragment implements View.OnClickListener {
    private final static String TAG = "HomeTabFragment";
    public final static String WINTOP = "APP_WIN";

    public final static int DETAILS_TYPE_ONE = 0;
    public final static int DETAILS_TYPE_TWO = 1;
    public final static int DETAILS_TYPE_THR = 2;
    public final static int DETAILS_TYPE_FOR = 3;

    public final static int ERROR_WHAT = -1;
    public final static int BANNER_WHAT = 0;
    public final static int WINTOP_WHAT = 1;
    public final static int DAYNWES_WHAT = 2;

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
    private BounceBackViewPager mViewPager;
    private ScrollPageAdapter mPageAdapter;
    private ScrollPageChangeListener mPageChangeLister;
    private List<View> mViews;

    private HorizontalListView mHlistview;
    private WinTopAdapter mWinAdapter;
    private HomeJsonData mHomeJsonData;


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

    private String[] mDialogString;
    private StockSelectDialog mDialog;

    private int mCurrentItem;
    private int mTotalCount = ITEM_SHOW.length;
    private FrameLayout mStockLayout;

    private List<String> mIvUrls;
    private List<String> mOpenUrls;
    private List<WinTopItem> mWinTopList;
    private List<DayNewsItem> mDayNewsList;

    private RippleView mLiveRipple;
    private RippleView mProduct;
    private RelativeLayout mLoginContent;
    private RippleView mStockRipple;
    private RippleView mDayNewClick;

    private static DataHandler mHandler;

    private View mWinTopLayout;
    private View mDayNewsLayout;
    private TextView mYearOne, mYearTwo, mYearThr, mYearFor;
    private TextView mDateOne, mDateTwo, mDateThr, mDateFor;
    private TextView mTitleOne, mTitleTwo, mTitleThr, mTitleFor;
    private TextView mDescOne, mDescTwo, mDescThr, mDescFor;
    private RippleView mItemOne, mItemTwo, mItemThr, mItemFor;
    private RippleView mLoginView;


    //用于处理消息的Handler
    private static class DataHandler extends Handler {
        WeakReference<HomeTabFragment> mActivityReference;

        public DataHandler(HomeTabFragment fragment) {
            super();
            mActivityReference = new WeakReference<HomeTabFragment>(fragment);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final HomeTabFragment fragment = mActivityReference.get();
            if (fragment == null) {
                ((HomeMainActivity) fragment.mActivity).stopRefreshAnim();
                return;
            }
            ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((HomeMainActivity) fragment.mActivity).stopRefreshAnim();
                }
            }, 2000);
            if (fragment.ERROR_WHAT == msg.what) {
                fragment.mBanner.setVisibility(View.GONE);
            } else if (fragment.BANNER_WHAT == msg.what) {
                if (fragment.mIvUrls != null && fragment.mIvUrls.size() > 0) {
                    fragment.mBanner.setImages(fragment.mIvUrls);//可以选择设置图片网址，或者资源文件，默认用Glide加载
                    fragment.mBanner.setVisibility(View.VISIBLE);
                    fragment.mBanner.setOnBannerClickListener(new OnBannerClickListener() {//设置点击事件
                        @Override
                        public void OnBannerClick(int position) {
                            Intent intent = new Intent(fragment.mActivity, HomeTabTopWebActivity.class);
                            if (fragment.mOpenUrls != null && position - 1 < fragment.mOpenUrls.size()) {
                                intent.putExtra(HomeTabTopWebActivity.WEB_URL, fragment.mOpenUrls.get(position - 1));
                                fragment.mActivity.startActivity(intent);
                            }
                        }
                    });
                }
            } else if (fragment.WINTOP_WHAT == msg.what) {
                if (fragment.mWinTopList != null && fragment.mWinTopList.size() > 0) {
                    fragment.mWinAdapter.setList(fragment.mWinTopList);
                    fragment.mWinAdapter.notifyDataSetChanged();
                    fragment.mWinTopLayout.setVisibility(View.VISIBLE);
                }
            } else if (fragment.DAYNWES_WHAT == msg.what) {
                if (fragment.mDayNewsList != null && fragment.mDayNewsList.size() > 0) {

                    for (int i = 0; i < fragment.mDayNewsList.size(); i++) {
                        DayNewsItem info;
                        switch (i) {
                            case 0:
                                info = fragment.mDayNewsList.get(i);
                                fragment.mYearOne.setText(AppUtil.getDateTime(Long.parseLong(info.getTime()), 1));
                                fragment.mDateOne.setText(AppUtil.getDateTime(Long.parseLong(info.getTime()), 2));
                                fragment.mTitleOne.setText(info.getTitle());
                                fragment.mDescOne.setText(info.getDesc());
                                break;
                            case 1:
                                info = fragment.mDayNewsList.get(i);
                                fragment.mYearTwo.setText(AppUtil.getDateTime(Long.parseLong(info.getTime()), 1));
                                fragment.mDateTwo.setText(AppUtil.getDateTime(Long.parseLong(info.getTime()), 2));
                                fragment.mTitleTwo.setText(info.getTitle());
                                fragment.mDescTwo.setText(info.getDesc());
                                break;
                            case 2:
                                info = fragment.mDayNewsList.get(i);
                                fragment.mYearThr.setText(AppUtil.getDateTime(Long.parseLong(info.getTime()), 1));
                                fragment.mDateThr.setText(AppUtil.getDateTime(Long.parseLong(info.getTime()), 2));
                                fragment.mTitleThr.setText(info.getTitle());
                                fragment.mDescThr.setText(info.getDesc());
                                break;
                            case 3:
                                info = fragment.mDayNewsList.get(i);
                                fragment.mYearFor.setText(AppUtil.getDateTime(Long.parseLong(info.getTime()), 1));
                                fragment.mDateFor.setText(AppUtil.getDateTime(Long.parseLong(info.getTime()), 2));
                                fragment.mTitleFor.setText(info.getTitle());
                                fragment.mDescFor.setText(info.getDesc());
                                break;
                        }
                    }
                    fragment.mDayNewsLayout.setVisibility(View.VISIBLE);
                }
            }

        }
    }


    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void onInitUI() {
        mHandler = new DataHandler(this);
        mStockClient = StockQuotationsClient.getInstance(mActivity);
        mViewPager = (BounceBackViewPager) findViewById(R.id.my_viewpager);
        mViews = new ArrayList<View>();
        mIndicator = (LinearLayout) findViewById(R.id.my_indicator);
        mStockLayout = (FrameLayout) findViewById(R.id.stock_layout);
        mBanner = (Banner) findViewById(R.id.banner);
        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        mProduct = (RippleView) findViewById(R.id.product);
        mProduct.setOnClickListener(this);
        mLiveRipple = (RippleView) findViewById(R.id.live);
        mLiveRipple.setOnClickListener(this);
        mStockRipple = (RippleView) findViewById(R.id.stock);
        mStockRipple.setOnClickListener(this);
        mLoginContent = (RelativeLayout) findViewById(R.id.login_content);
//        String[] images = getResources().getStringArray(R.array.banner_url);
        mHomeJsonData = HomeJsonData.getInstance();
        mIndicatorImages = new ArrayList<ImageView>();
        mData = new ArrayList<StockIndex>();
        mIvUrls = new ArrayList<String>();
        mOpenUrls = new ArrayList<String>();
        mCurrentItem = LeoSettings.getInteger(PrefConst.CURRENT_SELECT_STOCK, 0);
        mDialogString = getResources().getStringArray(R.array.stock_dialog_String);

        mLoginView = (RippleView) findViewById(R.id.login);
        mLoginView.setOnClickListener(this);
//        mBanner.setImages(images);//可以选择设置图片网址，或者资源文件，默认用Glide加载
//        mBanner.setOnBannerClickListener(new OnBannerClickListener() {//设置点击事件
//            @Override
//            public void OnBannerClick(int position) {
//                Toast.makeText(mActivity, "你点击了：" + position, Toast.LENGTH_LONG).show();
//                startActivity(new Intent(mActivity, HomeTabTopWebActivity.class));
//            }
//        });

        mWinTopLayout = findViewById(R.id.include_win_top);
        mDayNewsLayout = findViewById(R.id.include_day_news);
        setDayNewsFind();

        mHlistview = (HorizontalListView) findViewById(R.id.h_listview);
        mWinAdapter = new WinTopAdapter(mActivity);
        requestHomeData();

        mHlistview.setAdapter(mWinAdapter);

    }

    private void setDayNewsFind() {
        mDayNewClick = (RippleView) mDayNewsLayout.findViewById(R.id.rv_click_day);
        mDayNewClick.setOnClickListener(this);

        mItemOne = (RippleView) mDayNewsLayout.findViewById(R.id.item_one);
        mItemOne.setOnClickListener(this);
        mYearOne = (TextView) mDayNewsLayout.findViewById(R.id.tv_year_one);
        mDateOne = (TextView) mDayNewsLayout.findViewById(R.id.tv_date_one);
        mTitleOne = (TextView) mDayNewsLayout.findViewById(R.id.tv_title_one);
        mDescOne = (TextView) mDayNewsLayout.findViewById(R.id.tv_desc_one);

        mItemTwo = (RippleView) mDayNewsLayout.findViewById(R.id.item_two);
        mItemTwo.setOnClickListener(this);
        mYearTwo = (TextView) mDayNewsLayout.findViewById(R.id.tv_year_two);
        mDateTwo = (TextView) mDayNewsLayout.findViewById(R.id.tv_date_two);
        mTitleTwo = (TextView) mDayNewsLayout.findViewById(R.id.tv_title_two);
        mDescTwo = (TextView) mDayNewsLayout.findViewById(R.id.tv_desc_two);

        mItemThr = (RippleView) mDayNewsLayout.findViewById(R.id.item_thr);
        mItemThr.setOnClickListener(this);
        mYearThr = (TextView) mDayNewsLayout.findViewById(R.id.tv_year_thr);
        mDateThr = (TextView) mDayNewsLayout.findViewById(R.id.tv_date_thr);
        mTitleThr = (TextView) mDayNewsLayout.findViewById(R.id.tv_title_thr);
        mDescThr = (TextView) mDayNewsLayout.findViewById(R.id.tv_desc_thr);

        mItemFor = (RippleView) mDayNewsLayout.findViewById(R.id.item_for);
        mItemFor.setOnClickListener(this);
        mYearFor = (TextView) mDayNewsLayout.findViewById(R.id.tv_year_for);
        mDateFor = (TextView) mDayNewsLayout.findViewById(R.id.tv_date_for);
        mTitleFor = (TextView) mDayNewsLayout.findViewById(R.id.tv_title_for);
        mDescFor = (TextView) mDayNewsLayout.findViewById(R.id.tv_desc_for);
    }

    public void requestHomeData() {
        // 发送请求
        LoginHttpUtil.sendBannerHttpRequest(mActivity, Constants.HOME_PAGE_DATA, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                if (response != null) {
                    mHomeJsonData.setData(response.toString());
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            setBanner();
                            setWinTop();
                            setDayNews();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                if (mHandler != null) {
                    Message message = mHandler.obtainMessage();
                    message.what = ERROR_WHAT;
                    mHandler.sendMessage(message);
                }
            }
        });
    }

    private void setDayNews() {
        mDayNewsList = mHomeJsonData.getDayNews();
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            message.what = DAYNWES_WHAT;
            mHandler.sendMessage(message);
        }
    }

    private void setWinTop() {

        mWinTopList = mHomeJsonData.getHomeWinTop();
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            message.what = WINTOP_WHAT;
            mHandler.sendMessage(message);
        }

    }

    private void setBanner() {
        List<HomeBannerInfo> list = mHomeJsonData.getHomeBannerData();
        if (list != null && list.size() > 0) {
            mIvUrls.clear();
            mOpenUrls.clear();
            for (HomeBannerInfo info : list) {
                mIvUrls.add(info.mIvUrl);
                mOpenUrls.add(info.mOpenUrl);
            }
            if (mHandler != null) {
                Message message = mHandler.obtainMessage();
                message.what = BANNER_WHAT;
                mHandler.sendMessage(message);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        requestData();
        initLoginContent();
    }


    private void initLoginContent() {
        if (mLoginContent != null) {
            if (AppUtil.isLogin()) {
                mLoginContent.setVisibility(View.GONE);
            } else {
                mLoginContent.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 请求数据
     */
    public void requestData() {
        mStockClient.requestNewIndexAll(new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {
                Object[] objectArray = (Object[]) object;
                mData.clear();
                mData.addAll((List<StockIndex>) objectArray[0]);
                mData.add(0, null);

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
                    mStockLayout.setVisibility(View.VISIBLE);
                } else {
                    mStockLayout.setVisibility(View.GONE);
                }

            }

            @Override
            public void onError(int errorCode, String errorString) {
                mStockLayout.setVisibility(View.GONE);
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
            RippleView rippleView = (RippleView) page.findViewById(R.id.parent_one);
            rippleView.setOnClickListener(new ItemClickListener(index));
            rippleView.setVisibility(View.VISIBLE);
            RippleView rippleViewTwo = (RippleView) page.findViewById(R.id.parent_two);
            rippleViewTwo.setEnabled(false);
            RippleView rippleViewThree = (RippleView) page.findViewById(R.id.parent_three);
            rippleViewThree.setEnabled(false);

            mViews.add(page);

            return;
        }
        RippleView parentOne;
        TextView name_one;
        StockTextView price_one;
        StockTextView percent_one;
        RippleView parentTwo;
        TextView name_two;
        StockTextView price_two;
        StockTextView percent_two;
        RippleView parentThree;
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
                parentOne = (RippleView) page.findViewById(R.id.parent_one);
                parentOne.setOnClickListener(new ItemClickListener(index));
                parentOne.setVisibility(View.VISIBLE);
            } else if (i == 1) {
                name_two = (TextView) page.findViewById(R.id.name_two);
                price_two = (StockTextView) page.findViewById(R.id.price_two);
                percent_two = (StockTextView) page.findViewById(R.id.percent_two);
                setItemData(index, name_two, price_two, percent_two);
                parentTwo = (RippleView) page.findViewById(R.id.parent_two);
                parentTwo.setOnClickListener(new ItemClickListener(index));
                parentTwo.setVisibility(View.VISIBLE);
            } else {
                name_three = (TextView) page.findViewById(R.id.name_three);
                price_three = (StockTextView) page.findViewById(R.id.price_three);
                percent_three = (StockTextView) page.findViewById(R.id.percent_three);
                setItemData(index, name_three, price_three, percent_three);
                parentThree = (RippleView) page.findViewById(R.id.parent_three);
                parentThree.setOnClickListener(new ItemClickListener(index));
                parentThree.setVisibility(View.VISIBLE);
            }
        }
        TextView textView;
        if (extra == 1) {
            StockIndex index = null;
            textView = (TextView) page.findViewById(R.id.change_two);
            textView.setVisibility(View.VISIBLE);
            RippleView rippleView = (RippleView) page.findViewById(R.id.parent_two);
            rippleView.setOnClickListener(new ItemClickListener(index));
            View view = (View) page.findViewById(R.id.view_three);
            view.setVisibility(View.INVISIBLE);
            RippleView rippleViewThree = (RippleView) page.findViewById(R.id.parent_three);
            rippleViewThree.setEnabled(false);

        } else if (extra == 2) {
            StockIndex index = null;
            textView = (TextView) page.findViewById(R.id.change_three);
            textView.setVisibility(View.VISIBLE);
            RippleView rippleView = (RippleView) page.findViewById(R.id.parent_three);
            rippleView.setOnClickListener(new ItemClickListener(index));
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
//                int newItem = 0;
//                while (mCurrentItem == newItem) {
//                    Random random = new Random();
//                    newItem = random.nextInt(mTotalCount);
//                }
//                mCurrentItem = newItem;
//                requestData();
                mDialog = new StockSelectDialog(mActivity);
                mDialog.setData(mDialogString, mCurrentItem);
                mDialog.setBottomBtnListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LeoSettings.setInteger(PrefConst.CURRENT_SELECT_STOCK, mCurrentItem);
                        requestData();
                    }
                });
                mDialog.setCanceledOnTouchOutside(true);
                mDialog.show();
            }
        }

    }

    public void setSelectStock(int index) {
        mCurrentItem = index;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.live:
                Intent intent = null;
                if (AppUtil.isLogin()) {
                    intent = new Intent(mActivity, testWebViewActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(mActivity, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.stock:
                MyViewPager viewPager = ((HomeMainActivity) mActivity).getViewPager();
                if (viewPager != null) {
                    viewPager.setCurrentItem(1);
                }
                break;
            case R.id.item_one:
                gotoDetails(DETAILS_TYPE_ONE);
                break;
            case R.id.item_two:
                gotoDetails(DETAILS_TYPE_TWO);
                break;
            case R.id.item_thr:
                gotoDetails(DETAILS_TYPE_THR);
                break;
            case R.id.item_for:
                gotoDetails(DETAILS_TYPE_FOR);
                break;
            case R.id.rv_click_day:
                gotoNewsList();
                break;
            case R.id.login:
                gotoLogin();
                break;
            case R.id.product:
                gotoProduct();
                break;
            default:
                break;
        }
    }

    private void gotoProduct() {
        Intent intent = new Intent(mActivity, ProductActivity.class);
        mActivity.startActivity(intent);
    }

    private void gotoLogin() {
        Intent intent = new Intent(mActivity,LoginActivity.class);
        mActivity.startActivity(intent);
    }

    private void gotoNewsList() {
        Intent intent = new Intent(mActivity, DayNewsListActivty.class);
        mActivity.startActivity(intent);
    }

    private void gotoDetails(int detailsType) {
        DayNewsItem info = mDayNewsList.get(detailsType);

        Intent intent = new Intent(mActivity, DayNewsDetailActivity.class);
        intent.putExtra(Constants.DAYNEWS_DETAILS_TITLE, info.getTitle());
        intent.putExtra(Constants.DAYNEWS_DETAILS_ID, info.getId() + "");
        intent.putExtra(Constants.DAYNEWS_DETAILS_TIME, info.getTime());
        mActivity.startActivity(intent);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mActivity != null) {
            ((HomeMainActivity) mActivity).stopRefreshAnim();
        }
    }
}
