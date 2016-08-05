package com.zlf.appmaster.stocktopic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.StockKLine;
import com.zlf.appmaster.chartview.gain.GainGraphView;
import com.zlf.appmaster.chartview.tool.OnGetDailyKLinesListener;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.client.TopicClient;
import com.zlf.appmaster.db.stock.IndexKLineTable;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.combination.CombinationGain;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.model.topic.TopicDetail;
import com.zlf.appmaster.stocktrade.StockTradeDetailActivity;
import com.zlf.appmaster.utils.TimeUtil;
import com.zlf.appmaster.utils.Utils;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by Huang on 2015/6/15.
 */
public class TopicDetailStockFragment extends BaseFragment {
    private static final int UI_HANDLE_NOTIFY = 1;

    private TopicDetailActivity mTopicDetailActivity;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private View mLayout;

    private List<StockTradeInfo> mListData;
    private StockTradeInfoListAdapter mListAdapter;
    private XListView mListView;

    private ProgressBar mProgressBar;
    private TopicClient mClient;

    private int mCurEndIndex = 0;
    private static final int LIST_LOAD_SIZE = 20;

    private String mTopicID;
    private int mTopicDays;

    private TopicDetail mTopicDetailData;
    //hs300
    private static final String mStockIndexID = "399300";
    private ArrayList<StockKLine> mDailyKLines;
    private StockClient mStockClient;
    private UIHandler mUIHandler;


    //lineView
    private GainGraphView mGainLineView;
    private View mGainLayoutView;
    private View mGainNoDataView;
    private TextView mGainTodayProfitView;
    private TextView mTopicDaysView ;
    private static final int LOAD_ITEM_NUM = 10;
//    private LiveRecordingUtil mLiveRecordingUtil;

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // TODO Auto-generated method stub
//        if (mLayout != null) {//初始化过了 就不需要再创建
//            ViewGroup parent = (ViewGroup) mLayout.getParent();//清除自己 再返回 否则会重复设置了父控件
//            if (parent != null) {
//                parent.removeView(mLayout);
//            }
//            initData();
//
//            return mLayout;
//        }
//        mLayout = inflater.inflate(R.layout.fragment_quotations_content, null);
//        mTopicDetailActivity = (TopicDetailActivity)getActivity();  // 宿主
//        mContext = mTopicDetailActivity;
//
//        mClient = TopicClient.getInstance(mContext);
//        mLayoutInflater = getLayoutInflater(savedInstanceState);
//
//        Bundle argumentBundle = getArguments();
//        if (null != argumentBundle){
//            mTopicID = argumentBundle.getString(TopicDetailActivity.INTENT_FLAG_TOPIC_ID);
//            mTopicDays = argumentBundle.getInt(TopicDetailActivity.INTENT_FLAG_TOPIC_DAYS, TopicDetailActivity.INTENT_TODAY);
//        }
//        mLiveRecordingUtil = LiveRecordingUtil.getInstance();
//
//        initViews(mLayout);
//
//        initData();
//
//        return mLayout;
//    }

    private void initViews(View view){
        mContext = getActivity();
        mStockClient = new StockClient(mContext);
        mUIHandler = new UIHandler(this);

        mProgressBar = (ProgressBar) findViewById(R.id.content_loading);

        //set list
        mListView = (XListView) findViewById(R.id.quotations_content_list);
        mListView.setPullLoadEnable(false);
        mListView.setPullRefreshEnable(true);
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
                if (position > 1 ) {
                    StockTradeInfo stockTradeInfo = (StockTradeInfo) mListAdapter.getItem(position - 2);
                    if (null != stockTradeInfo) {
//                        if(!mLiveRecordingUtil.isLiveRecording()) {
                            Intent intent = new Intent(mContext, StockTradeDetailActivity.class);
                            intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKNAME, stockTradeInfo.getName());
                            intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKCODE, stockTradeInfo.getCode());
                            startActivity(intent);
//                        } else {
//                            Intent intent = new Intent(mContext, LiveAnchorLectureActivity.class);
//                            mLiveRecordingUtil.setStock(true);
//                            mLiveRecordingUtil.setStockCode(stockTradeInfo.getCode());
//                            mLiveRecordingUtil.setStockName(stockTradeInfo.getName());
//                            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            mContext.startActivity(intent);
//                        }
                    }
                }
            }
        });
        mListView.setVisibility(View.GONE);
        mListData = new ArrayList<StockTradeInfo>();
        mListAdapter = new StockTradeInfoListAdapter(mContext, mListData);
        mListView.setAdapter(mListAdapter);

        initListHead();
    }


    private void initListHead() {
        View viewGain = mActivity.getLayoutInflater().inflate(R.layout.list_item_topic_detail_gain,null);
        mGainLineView = (GainGraphView)viewGain.findViewById(R.id.view_gain_line);
        mGainLayoutView = viewGain.findViewById(R.id.layout_line);
        mGainNoDataView = viewGain.findViewById(R.id.tv_no_data);
        mGainTodayProfitView = (TextView)viewGain.findViewById(R.id.tv_today_gain);
        mTopicDaysView = (TextView)viewGain.findViewById(R.id.tv_days);
        switch (mTopicDays){
            case TopicDetailActivity.INTENT_5TH_DAYS:
                mTopicDaysView.setText(R.string.cur_topic_5days_percent);
                break ;
            case TopicDetailActivity.INTENT_20TH_DAYS:
                mTopicDaysView.setText(R.string.cur_topic_20days_percent);
                break ;
            case TopicDetailActivity.INTENT_60TH_DAYS:
                mTopicDaysView.setText(R.string.cur_topic_60days_percent);
                break ;
            case TopicDetailActivity.INTENT_TODAY:
                mTopicDaysView.setText(R.string.cur_topic_today_percent);
                break ;
            default:
                mTopicDaysView.setText(R.string.cur_topic_today_percent);
                break ;
        }
        mListView.addHeaderView(viewGain);
    }

    private void initData(){

        mTopicDetailData = new TopicDetail();

        mProgressBar.setVisibility(View.VISIBLE);
        mClient = TopicClient.getInstance(mContext);

        refreshList();
    }

    //刷新
    private void refreshList() {
        mCurEndIndex = 0;
        //mListView.setPullLoadEnable(true);

        loadMoreList();
    }

    private void loadMoreList() {

        mClient.requestStockList(mTopicID, mCurEndIndex, LIST_LOAD_SIZE, new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {
                onLoaded();
                mProgressBar.setVisibility(View.GONE);

                int loadSize = 0;
                if (null != object) {
                    //mDataItems.clear();
                    if (mCurEndIndex == 0) {//刷新的 要清除
                        mListData.clear();
                    }
                    List<StockTradeInfo> loadData = ((List<StockTradeInfo>) object);
                    Collections.sort(loadData);
                    loadSize = loadData.size();
                    mListData.addAll(loadData);
                    mListView.setVisibility(View.VISIBLE);
                }


                if (loadSize < LIST_LOAD_SIZE) {
                    //没有更多了
                    mListView.setPullLoadEnable(false);
                } else {
                    mListView.setPullLoadEnable(true);
                }
                mCurEndIndex += loadSize;

                mListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(int errorCode, String errorString) {
                mListView.setPullLoadEnable(false);
                onLoaded();
            }
        });

    }


    private void onLoaded(){
        mListView.stopLoadMore();
        mListView.stopRefresh();
        mProgressBar.setVisibility(View.GONE);
    }


    @Override
    public void onResume() {
        super.onResume();
//        MobclickAgent.onPageStart(TAG);

        mProgressBar.setVisibility(View.VISIBLE);
        mClient.requestTopicProfit(mTopicID, mTopicDays, new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {
                mTopicDetailData.resolveJSONObject((JSONObject) object,mTopicDays);


                // mAdapter.notifyDataSetChanged();

                //get hs300 两个点以上才有线
                if (mTopicDetailData.getGainLine() != null && mTopicDetailData.getGainLine().size() > 1)
                    getHS300Data();

                //resetLayout();
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(int errorCode, String errorString) {
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
//        MobclickAgent.onPageEnd(TAG);
    }




    /**
     * 获取日K数据
     * @param listener
     */
    public void getStockDailyKLines(final OnGetDailyKLinesListener listener) {

        //获取下起始时间点 大于1则说明有数据，可直接显示
        long startTime = 1;

        //从数据库中获取
        IndexKLineTable mKLineTable = new IndexKLineTable(mContext);
        byte[] b = mKLineTable.getKLineData(mStockIndexID);
        if (b != null && b.length > 0) {
            mDailyKLines = StockKLine.resloveKLineFromSql(b, mContext);
            startTime = mDailyKLines.get(mDailyKLines.size()-1).getDataTime();//最后一条时间取起

            if (listener != null) {
                listener.onDataFinish(mDailyKLines);
            }
        }

        //获取新的数据
        //获取下起始时间点 大于1则说明有数据，可直接显示
        long endTime = 32500886400000L;//2999/11/30 0:0:0

        int type = StockClient.TYPE_INDEX;
        //获取新的数据
        mStockClient.getKlineInfo(mStockIndexID, StockClient.KLINE_TYPE_DAILY, type, startTime, endTime, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                listener.onError();
            }

            @Override
            public void onDataFinish(Object object) {
                ArrayList<StockKLine> dataLines = StockKLine.resolveKLineZip(object, mContext);
                if (dataLines == null || dataLines.size() == 0) {
                    //没有获取到K线数据
                    listener.onDataFinish(mDailyKLines);
                    return;
                }
                if (mDailyKLines != null) {
                    mDailyKLines = StockKLine.addKLine(mDailyKLines, dataLines);
                } else {
                    mDailyKLines = dataLines;
                }

                listener.onDataFinish(mDailyKLines);

                //要把之前的加进去

                byte[] data = StockKLine.getKLineBytes(mDailyKLines);

                //保存起来
                IndexKLineTable mKLineTable = new IndexKLineTable(mContext);
                mKLineTable.saveKLineData(mStockIndexID, data, mDailyKLines.get(mDailyKLines.size() - 1).getDataTime());
            }
        });

    }


    private void getHS300Data() {
        //get data from db
        getStockDailyKLines(new OnGetDailyKLinesListener() {
            @Override
            public void onDataFinish(ArrayList<StockKLine> data) {
                resolveHS300Data();
            }

            @Override
            public void onError() {
                resolveHS300Data();
            }
        });
    }


    private void resolveHS300Data() {

        if (mDailyKLines != null) {
            //change data
            ArrayList<Float> hs300DescClose = new ArrayList<Float>();
            ArrayList<Float> hs300Gains = new ArrayList<Float>();

            int lastIndex = mDailyKLines.size()-1;
            float close = 0f;

            //从后往前找 除了第一个点
            for (int i= mTopicDetailData.getGainLine().size()-1;i>=1;i--){
                long gainTime = mTopicDetailData.getGainLine().get(i).getTime();
                for (int j=lastIndex;j>=0;j--){
                    long hsTime = mDailyKLines.get(j).getDataTime();
                    int ret = TimeUtil.isSameDay(gainTime, hsTime);

                    if (ret == 0){//同一天
                        close =mDailyKLines.get(j).getClose();
                        hs300DescClose.add(close);
                        lastIndex = j-1;
                        break;
                    }else if (hsTime < gainTime){
                        //没有此点数据，如果是第一个点，则跳过;如果是中间点缺少则用上一天的
                        if (hs300DescClose.size() > 0)
                            hs300DescClose.add(close);
                        break;
                    }

                }
            }
            //添加起始点
            hs300Gains.add(0f);

            float baseClose = close;
            if (baseClose != 0){
                for (int i = hs300DescClose.size() - 1; i >= 0; i--) {
                    hs300Gains.add((hs300DescClose.get(i)-baseClose)/baseClose);
                }
            }else{
                for (int i=hs300DescClose.size() - 1; i >= 0; i--) {
                    hs300Gains.add(0f);
                }
            }



            //set line
            mTopicDetailData.setHs300Line(hs300Gains);
        }
        mUIHandler.sendEmptyMessage(UI_HANDLE_NOTIFY);
    }



    class UIHandler extends Handler {
        WeakReference<TopicDetailStockFragment> mReference;
        UIHandler(TopicDetailStockFragment fragment) {
            mReference = new WeakReference<TopicDetailStockFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mReference == null){
                return;
            }
            TopicDetailStockFragment fragment = mReference.get();
            if (fragment == null)
                return;

            if (msg.what == UI_HANDLE_NOTIFY) {
                //fragment.mCombinationItemAdapter.notifyDataSetChanged();
                int gainColor = Utils.getColorByFloat(mContext, mTopicDetailData.getTodayProfit());
                mGainTodayProfitView.setTextColor(gainColor);
                mGainTodayProfitView.setText(mTopicDetailData.getTodayProfitFormat());
                updateGainLine(mTopicDetailData.getGainLine());
                fragment.mListAdapter.notifyDataSetChanged();
            }
        }
    }


    private void updateGainLine(ArrayList<CombinationGain> data){
        if (data == null || data.size() < 2){//两个点以上才有线
            mGainNoDataView.setVisibility(View.VISIBLE);
            mGainLayoutView.setVisibility(View.GONE);
            return;
        }
        ArrayList<Float> curCombinationGains = new ArrayList<Float>();
        ArrayList<Long> times = new ArrayList<Long>();

        int color1 = 0xffd70a23;
        int color2 = 0xff008ad3;

        for(CombinationGain gain:data){
            curCombinationGains.add(gain.getGain());
            times.add(gain.getTime());
        }

        mGainLayoutView.setVisibility(View.VISIBLE);
        mGainNoDataView.setVisibility(View.GONE);
        mGainLineView.setLineData(curCombinationGains, color1,
                mTopicDetailData.getHs300Line(), color2,
                times);

    }

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_quotations_content;
    }

    @Override
    protected void onInitUI() {
        mTopicDetailActivity = (TopicDetailActivity)getActivity();  // 宿主
        mContext = mTopicDetailActivity;

        mClient = TopicClient.getInstance(mContext);
//        mLayoutInflater = getLayoutInflater(savedInstanceState);

        Bundle argumentBundle = getArguments();
        if (null != argumentBundle){
            mTopicID = argumentBundle.getString(TopicDetailActivity.INTENT_FLAG_TOPIC_ID);
            mTopicDays = argumentBundle.getInt(TopicDetailActivity.INTENT_FLAG_TOPIC_DAYS, TopicDetailActivity.INTENT_TODAY);
        }
//        mLiveRecordingUtil = LiveRecordingUtil.getInstance();

        initViews(mLayout);

        initData();

    }
}
