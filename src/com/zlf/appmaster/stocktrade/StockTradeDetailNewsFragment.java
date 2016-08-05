package com.zlf.appmaster.stocktrade;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;

import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.client.NewsClient;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.model.news.NewsFlashItem;
import com.zlf.appmaster.stocknews.AnnouncementDetailActivity;
import com.zlf.appmaster.stocknews.NewsDetailActivity;
import com.zlf.appmaster.stocknews.NewsListAdapter;
import com.zlf.appmaster.stocknews.StockReportListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 个股新闻
 * Created by Deping Huang on 2014/12/17.
 */
public class StockTradeDetailNewsFragment extends Fragment {

    private final static String TAG = StockTradeDetailNewsFragment.class.getSimpleName();

    private XListView mListView;
    private ProgressBar mProgressBar;
    private NewsClient mNewsClient;
    private String mStockCode;
    private BaseAdapter mListAdapter;

    private List<NewsFlashItem> mDataItems;
    private static final int SHOW_NUM_PER_TIME = 10;

    private View mLayout;
    private Context mContext;

    private long mMaxID = 0, mMinFilterID = 0;
    private int mLabel;     // 请求的类型 from arguments

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout != null){//初始化过了 就不需要再创建
            ViewGroup parent = (ViewGroup) mLayout.getParent();//清除自己 再返回 否则会重复设置了父控件
            if (parent != null) {
                parent.removeView(mLayout);
            }
            return mLayout;
        }

        mLayout= inflater.inflate(R.layout.activity_stocktrade_detail_news, null);
        mContext = getActivity();

        mLabel = getArguments().getInt(StockTradeDetailNewsActivity.INTENT_FLAG_LABEL);

        initViews(mLayout);

        initData();

        return  mLayout;
    }


    private void initViews(View view){
        mProgressBar = (ProgressBar)view.findViewById(R.id.content_loading);
        mListView = (XListView)view.findViewById(R.id.news_list);
        mListView.setPullLoadEnable(false); // 启用下拉加载
        mListView.setPullRefreshEnable(true);//上拉
        mListView.setVisibility(View.GONE);
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
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {



                NewsFlashItem newsHomeSub = (NewsFlashItem) mListAdapter
                        .getItem(position - 1);
                if (null != newsHomeSub) {
                    int classify = newsHomeSub.getClassify();
                    if (classify == NewsDetailActivity.NEWS_TYPE_ANNOUCEMENT){
                        Intent intent = new Intent(mContext, AnnouncementDetailActivity.class);
                        intent.putExtra(AnnouncementDetailActivity.INTENT_FLAG_DATA, newsHomeSub);
                        mContext.startActivity(intent);
                    }
                    else{
                        String titleStr;
                        switch (mLabel){
                            case NewsClient.STOCK_NEWS_LABEL_REPORT:
                                titleStr = "研报详情";
                                break;
                            default:
                                titleStr = "新闻详情";
                                break;
                        }

                        Intent intent = new Intent(mContext,
                                NewsDetailActivity.class);
                        intent.putExtra(
                                NewsDetailActivity.KEY_INTENT_NEWS_CATALOGUE,
                                titleStr);
                        intent.putExtra(NewsDetailActivity.KEY_INTENT_NEWS_ID,
                                newsHomeSub.getId());
                        intent.putExtra(NewsDetailActivity.KEY_INTENT_NEWS_TYPE, newsHomeSub.getClassify());
                        startActivity(intent);
                    }
                }

            }
        });


    }

    private void initData(){
        mStockCode = getActivity().getIntent().getStringExtra(StockTradeDetailNewsActivity.INTENT_FLAG_STOCK_CODE);
        mNewsClient = NewsClient.getInstance(mContext);


        mDataItems = loadStockNewsFromCache();
        if(null != mDataItems && mDataItems.size() > 0){
            mListView.setVisibility(View.VISIBLE);
        }

        switch (mLabel){
            case NewsClient.STOCK_NEWS_LABEL_REPORT:
                mListAdapter = new StockReportListAdapter(mContext, mDataItems);
                break;
            case NewsClient.STOCK_NEWS_LABEL_ANNOUNCEMENT:
                mListAdapter = new StockReportListAdapter(mContext, mDataItems);
                break;
            default:
                mListAdapter = new NewsListAdapter(mContext, mDataItems);
                break;
        }
        mListView.setAdapter(mListAdapter);

        mProgressBar.setVisibility(View.VISIBLE);
        refreshList();
    }

    /**
     * 加载缓存
     */
    private List<NewsFlashItem> loadStockNewsFromCache(){
        List<NewsFlashItem> items = null;
        JSONObject response = StockJsonCache.loadFromFile(mStockCode, mContext, StockJsonCache.CACHEID_EXTRA_INFO_NEWS);
        if(null != response){
            try {
                items = NewsFlashItem.resolveStockNewsArray(mStockCode, response);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (null == items){
            items = new ArrayList<NewsFlashItem>();
        }
        return items;
    }



    //刷新
    private void refreshList() {
        mMaxID = 0;
        mMinFilterID = 0;
        loadMoreList();
    }

    private void loadMoreList(){
        mNewsClient.requestStockNewsList(mStockCode, mLabel, mMaxID, mMinFilterID, 0, SHOW_NUM_PER_TIME, new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {
                List<NewsFlashItem> items = (List<NewsFlashItem>) object;

                    if (null != items) {
                        int len = items.size();
                        if (len < SHOW_NUM_PER_TIME) {
                            //没有数据了
                            mListView.setPullLoadEnable(false);
                        } else {
                            mListView.setPullLoadEnable(true);
                        }

                        if (len > 0) {
                            if (mMaxID == 0) {   // 刷新
                                mDataItems.clear();
                            }

                            // 更新请求的索引
                            NewsFlashItem itemLast = items.get(len - 1);
                            mMaxID = itemLast.getId();
                            mMinFilterID = itemLast.getId();    //  （这个应该从数据库中读取最小的，现版本未缓存全部数据，暂时每次都取最新的）

                            mDataItems.addAll(items);
                            mListAdapter.notifyDataSetChanged();
                        }
                    }

                    onLoaded();
            }

            @Override
            public void onError(int errorCode, String errorString) {
                onLoaded();
            }
        });
    }

    private void onLoaded() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mProgressBar.setVisibility(View.GONE);
    }



    @Override
    public void onResume() {
        super.onResume();
//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(mContext);
    }

    @Override
    public void onPause() {
        super.onPause();
//        MobclickAgent.onPageEnd(TAG); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
//        MobclickAgent.onPause(mContext);
    }
}
