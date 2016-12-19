package com.zlf.appmaster.tradetab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockQuotationsClient;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.stockIndex.StockIndexDetailActivity;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/15.
 */
public class StockLMEFragment extends BaseFragment {
    private boolean isInUrFace = false;
    private boolean isFlash = false;
    private static final String TAG = StockIndexFragment.class.getSimpleName();
    private Context mContext;
    private View mLayout;
    private StockQuotationsIndexAdapter mStockQuotationsIndexAdapter;
    private List<StockIndex> mIndexData;
    private List<StockIndex> mForeignIndexData;
    private StockQuotationsClient mStockClient;

    private XListView mListView;
    private CircularProgressView mProgressBar;
    private View mEmptyView;
    private RippleView mRefreshView;

    public android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mStockQuotationsIndexAdapter.setIsFlash(isFlash);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void initViews(View view){

        mListView = (XListView) findViewById(R.id.quotations_content_list);
        mProgressBar = (CircularProgressView) findViewById(R.id.content_loading);
        mEmptyView = findViewById(R.id.empty_view);
        mRefreshView = (RippleView) findViewById(R.id.refresh_button);
        mRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLisrByButton();
            }
        });

        mIndexData = new ArrayList<StockIndex>();
        mForeignIndexData = new ArrayList<StockIndex>();
        mStockQuotationsIndexAdapter = new StockQuotationsIndexAdapter(mActivity, mIndexData, mForeignIndexData);
        mListView.setAdapter(mStockQuotationsIndexAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                StockIndex item = (StockIndex)mStockQuotationsIndexAdapter.getItem(position - 1);
                LeoLog.d("testClick","itemClick : " + position);
                if(position == 1){
                    return;
                }

                if (null != item) {

                    Class targetClass;

                    targetClass = StockIndexDetailActivity.class;
                    Intent intent = new Intent(mActivity, targetClass);

                    intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_INDEXCODE, item.getCode());
                    intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_INDEXNAME, item.getName());
                    intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_OPEN_INDEX, item.getTodayIndex());
                    intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_YESTERDAY_INDEX, item.getYesterdayIndex());
                    intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_NOW_INDEX, item.getNowIndex());
                    intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_HIGH_INDEX, item.getHighestIndex());
                    intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_LOW_INDEX, item.getLowestIndex());
                    intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_GUO_XIN, true);
                    intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_TAB_MINITE_WHAT, Constants.LME_INFO_MINUTE_PRONAME);
                    intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_TAB_KLINE_WHAT, Constants.LME_INFO_KLINE_PRONAME);
                    mActivity.startActivity(intent);

                }
            }
        });

        mListView.setPullLoadEnable(false);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                if (mProgressBar.getVisibility() == View.VISIBLE){  // 主进程正在更新
                    onLoaded();
                }
                else{
                    requestData();
                }
            }

            @Override
            public void onLoadMore() {

            }
        });


    }

    private void refreshLisrByButton() {
        mProgressBar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        requestData();
    }

    private void initData(){
        mProgressBar.setVisibility(View.VISIBLE);

        requestData();
    }

    private void onLoaded() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        isFlash = false;
    }

    /**
     * 请求数据
     */
    private void requestData(){

        mStockClient.requestNewIndexAll(new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {
                Object[] objectArray = (Object[])object;
                mIndexData.clear();
                mIndexData.addAll((List<StockIndex>) objectArray[0]);
                mIndexData.add(0,null);
                mStockQuotationsIndexAdapter.setIsFlash(isFlash);
                mStockQuotationsIndexAdapter.notifyDataSetChanged();
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(1, 300);
                }

                mProgressBar.setVisibility(View.GONE);
                if (mIndexData != null && mIndexData.size() > 0) {
                    mListView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                } else {
                    mListView.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                }

                onLoaded();
            }

            @Override
            public void onError(int errorCode, String errorString) {

                mProgressBar.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                onLoaded();
            }
        }, Constants.MY_DATA_URL.concat(Constants.LME_INFO_PRONAME));


    }

    @Override
    public void onResume() {
        super.onResume();
        isInUrFace = true;
    }

    @Override
    public void onPause() {
        super.onPause();
//        MobclickAgent.onPageEnd( TAG );
    }

    @Override
    public void onStop() {
        super.onStop();
        isInUrFace = false;
    }

    public void toRequestDate() {
        if (isInUrFace) {
            LeoLog.d("TimeService", "LME Fragment request");
            isFlash = true;
            requestData();
        }
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_quotations_content;
    }

    @Override
    protected void onInitUI() {
        mStockClient = StockQuotationsClient.getInstance(mActivity);
        Bundle bundle = getArguments();
        initViews(mLayout);

//        loadCache();
        initData();
    }
}

