package com.zlf.appmaster.stockIndex;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.client.StockQuotationsClient;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.stocksearch.StockSearchActivity;
import com.zlf.appmaster.stocktrade.StockTradeDetailActivity;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class StockIndexDetailActivity extends Activity {
	public static final String INTENT_FLAG_INDEXCODE = "intent_flag_index_code";
    public static final String INTENT_FLAG_INDEXNAME = "intent_flag_index_name";
    public static final String INTENT_FLAG_GUO_XIN = "intent_flag_index_guo_xin";
    public static final String INTENT_FLAG_OPEN_INDEX = "intent_flag_open_index";
    public static final String INTENT_FLAG_YESTERDAY_INDEX = "intent_flag_yesterday_index";
    public static final String INTENT_FLAG_NOW_INDEX = "intent_flag_now_index";
    public static final String INTENT_FLAG_HIGH_INDEX = "intent_flag_high_index";
    public static final String INTENT_FLAG_LOW_INDEX = "intent_flag_low_index";
    public static final String INTENT_FLAG_TAB_MINITE_WHAT = "intent_flag_tab_minute_what"; //  哪一个tab分时tag
    public static final String INTENT_FLAG_TAB_KLINE_WHAT = "intent_flag_tab_kline_what"; //  哪一个tab

	private static final String TAG = StockIndexDetailActivity.class.getSimpleName();
    private static final int MSG_UPDATE_INDEX = 1;
    private static final int MSG_UPDATE_LED_UP_LIST = 2;

    private Context mContext;
    private StockClient mStockClient;
    private StockIndex mStockIndex;
    private List<StockTradeInfo> mData;
    private int mDataType = 0;

    private String mStockIndexID,mStockIndexName;

    private TextView mActivityTitleTV;
    private TextView mActivityTitleCommentTV;
    private String mTitleComment1;
    private String mTitleComment2;

    private StockIndexDetailListAdapter mStockIndexDetailAdapter;
    private XListView mListView;

    private ProgressBar mProgressBar;
    private int mCurEndIndex = 0;
    private static final int LOAD_ITEM_NUM = 9;

    private double mOpenIndex;
    public static double mYesterdayIndex;
    private double mNowIndex;
    private double mHighIndex;
    private double mLowIndex;


    private StockQuotationsClient mStockQuotationsClient;
    private boolean mFromGuoXin;
    private String mTabMinuteTag;  // 哪一个tab分时的标识
    private String mTabKLineTag;


    public Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case MSG_UPDATE_INDEX:
                    updateViews(mStockIndex);
                    break;
                case MSG_UPDATE_LED_UP_LIST:
                  //  updateLedUpInfoList();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stockindex_detail);
		
		initViews();

        initData();
	}

	public void initViews(){
        mContext = this;

        mStockClient = new StockClient(this);
        mStockQuotationsClient = StockQuotationsClient.getInstance(this);
        mStockIndexID = getIntent().getStringExtra(INTENT_FLAG_INDEXCODE);
        mStockIndexName = getIntent().getStringExtra(INTENT_FLAG_INDEXNAME);
        mFromGuoXin = getIntent().getBooleanExtra(INTENT_FLAG_GUO_XIN, false);

        mActivityTitleTV 		= (TextView)findViewById(R.id.stocktrade_detail_title);
        mActivityTitleCommentTV = (TextView)findViewById(R.id.stocktrade_detail_title_comment);
        mActivityTitleTV.setText(mStockIndexName + " " + mStockIndexID);


        mListView = (XListView)findViewById(R.id.stock_index_detail_list);
        mListView.setPullRefreshEnable(true);
        mListView.setPullLoadEnable(false);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }

            @Override
            public void onLoadMore() {
                loadMoreList();
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                position = position -1;//   用的是xlistview
                StockTradeInfo stockTradeInfo = (StockTradeInfo)mStockIndexDetailAdapter.getItem(position);
                if (null != stockTradeInfo){
                    Intent intent = new Intent(mContext, StockTradeDetailActivity.class);
                    intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKNAME, stockTradeInfo.getName());
                    intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKCODE, stockTradeInfo.getCode());
                    startActivity(intent);
                }
            }
        });
	}

    private void initData(){
        Intent intent = getIntent();
        mOpenIndex = intent.getDoubleExtra(INTENT_FLAG_OPEN_INDEX, 0);
        mYesterdayIndex = intent.getDoubleExtra(INTENT_FLAG_YESTERDAY_INDEX, 0);
        mNowIndex = intent.getDoubleExtra(INTENT_FLAG_NOW_INDEX, 0);
        mHighIndex = intent.getDoubleExtra(INTENT_FLAG_HIGH_INDEX, 0);
        mLowIndex = intent.getDoubleExtra(INTENT_FLAG_LOW_INDEX, 0);
        mTabMinuteTag = intent.getStringExtra(INTENT_FLAG_TAB_MINITE_WHAT);
        mTabKLineTag = intent.getStringExtra(INTENT_FLAG_TAB_KLINE_WHAT);


        // 从缓存中加载
        mStockIndex = loadCache(mContext, mStockIndexID, mStockIndexName);

        // 初始化数据

        mData = new ArrayList<StockTradeInfo>();
        mStockIndexDetailAdapter = new StockIndexDetailListAdapter(mContext, mStockClient, mStockIndex, mData, mFromGuoXin, mTabMinuteTag, mTabKLineTag);

        mStockIndexDetailAdapter.setOnTabChange(new StockIndexDetailListAdapter.OnTabChange() {
            @Override
            public void onChange(int type) {
                mStockIndexDetailAdapter.setAdapterNotify(true);
                mDataType = type;
                // 刷新涨跌幅榜
                mCurEndIndex = 0;
                mData.clear();
                loadMoreList();
            }
        });

        mListView.setAdapter(mStockIndexDetailAdapter);

        requestData(false);
    }

    private StockIndex loadCache(Context context, String code, String name){
        StockIndex stockIndex = null;

        try{
            stockIndex = StockIndex.resolveIndexJsonObject(StockJsonCache.loadFromFile(code, context, StockJsonCache.CACHEID_EXTRA_INFO_INDEX_DETAIL));
        }catch (JSONException e){

        }

        if (stockIndex == null){
            stockIndex = new StockIndex();      // 不能为空
            stockIndex.setCode(code);
            stockIndex.setName(name);
        }

        return stockIndex;
    }


    public void updateViews(StockIndex stockIndex){

        if(null == stockIndex) return;



        mTitleComment1 = " 交易中" + stockIndex.getDataTimeFormat();
        if (mFromGuoXin) {
            if (!"open".equals(mStockIndex.getIsOPen())) {
                mTitleComment1 = " 休市中 " + stockIndex.getDataTimeFormat();
            }
        } else {
            int marketStatus = stockIndex.getMarketStatus();
            if (marketStatus != StockTradeInfo.MARKET_STATUS_NORMAL) {
                mTitleComment1 = " 休市中 " + stockIndex.getDataTimeFormat();

                // 休市则终止自动刷新
                //stopAutoRefresh();
            }
        }
        mActivityTitleCommentTV.setText(mTitleComment1);
        mTitleComment2 = stockIndex.getCurPriceFormat() + "  " +stockIndex.getCurPriceComment();


    }



    private void requestData(final boolean autoRefresh){
        if (mFromGuoXin) {
            mStockIndex.setCode(mStockIndexID);
            mStockIndex.setName(mStockIndexName);

            mStockIndex.setTodayIndex(mOpenIndex);
            mStockIndex.setYesterdayIndex(mYesterdayIndex);
            mStockIndex.setNowIndex(mNowIndex);
            mStockIndex.setHighestIndex(mHighIndex);
            mStockIndex.setLowestIndex(mLowIndex);

            mStockIndex.setTradeCount(99999);
            mStockIndex.setTradePrice(99999999);

            mStockIndex.setDataTime(/*dataIndexJSON.optLong("quoteTime")*/System.currentTimeMillis());

            mStockIndex.setUpCount(999);
            mStockIndex.setDeuceCount(999);
            mStockIndex.setDownCount(999);
            mStockIndex.setIsOPen("open");
            mStockIndex.setCurrentTime(System.currentTimeMillis());
            updateViews(mStockIndex);
            mStockIndexDetailAdapter.notifyDataSetChanged();
            mStockIndexDetailAdapter.setAdapterNotify(false);
            onLoaded();
//            loadMoreList();
//            mStockQuotationsClient.requestNewIndexItem(new OnRequestListener() {
//                @Override
//                public void onDataFinish(Object object) {
//                    Object[] objectArray = (Object[])object;
//                    mStockIndex.copy(((List<StockIndex>)objectArray[0]).get(0));
//                    updateViews(mStockIndex);
//                    mStockIndexDetailAdapter.notifyDataSetChanged();
//                    mStockIndexDetailAdapter.setAdapterNotify(false);
//                    loadMoreList();
//                }
//
//                @Override
//                public void onError(int errorCode, String errorString) {
//
//                }
//            }, Constants.JIN_GUI_INFO_ITEM.concat(mStockIndexID));
        } else {
            mStockClient.requestStockIndexDetail(mStockIndexID, new OnRequestListener() {

                @Override
                public void onError(int errorCode, String errorString) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onDataFinish(Object object) {
                    // TODO Auto-generated method stub
                    mStockIndex.copy((StockIndex) object);
                    updateViews(mStockIndex);
                    //mStockIndexDetailAdapter.notifyDataSetChanged();

                    // mHandler.sendEmptyMessage(MSG_UPDATE_INDEX);

                    loadMoreList();
                }
            });
        }
    }

    //刷新
    private void refreshList() {
        mCurEndIndex = 0;
        //mListView.setPullLoadEnable(true);

        requestData(false);

        mStockIndexDetailAdapter.refreshKLineView();
    }


    private void loadMoreList(){

        mStockClient.requestIndexPopularStock(mStockIndexID, mCurEndIndex, mCurEndIndex + LOAD_ITEM_NUM, mDataType, new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {
                onLoaded();
//                mProgressBar.setVisibility(View.GONE);

                if (null != object) {
                    //mDataItems.clear();
                    if (mCurEndIndex == 0) {//刷新的 要清除
                        mData.clear();
                    }

                    mData.addAll((List<StockTradeInfo>) object);
                    mListView.setVisibility(View.VISIBLE);
                }

                int num = mData.size();

                if (num - mCurEndIndex > LOAD_ITEM_NUM / 2) {//大于要求加载的一半算是有更多的，否则没有
                    mCurEndIndex = num;
                    mListView.setPullLoadEnable(true);
                } else {
                    //没有更多了
                    mListView.setPullLoadEnable(false);
                }

                //mHandler.sendEmptyMessage(MSG_UPDATE_LED_UP_LIST);
                mStockIndexDetailAdapter.notifyDataSetChanged();
                mStockIndexDetailAdapter.setAdapterNotify(false);
            }

            @Override
            public void onError(int errorCode, String errorString) {
                onLoaded();
         //       mProgressBar.setVisibility(View.GONE);
                mListView.setPullLoadEnable(false);
                mStockIndexDetailAdapter.setAdapterNotify(false);
            }
        });
    }

    private void onLoaded() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
    }
	
	public void onBack(View v){
		finish();
	}

    public void onSearch(View v){
        Intent intent = new Intent(mContext, StockSearchActivity.class);
        startActivityForResult(intent, 0);
    }

	@Override
	protected void onResume() {
		super.onResume();
//		MobclickAgent.onPageStart(TAG);
//		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
//        MobclickAgent.onPageEnd(TAG); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
//        MobclickAgent.onPause(this);
	}

}
