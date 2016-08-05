package com.zlf.appmaster.stocknews;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.NewsClient;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.model.news.NewsFlashItem;
import com.zlf.appmaster.ui.stock.StockBaseFragment;
import com.zlf.appmaster.utils.LiveRecordingUtil;
import com.zlf.appmaster.utils.QLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class NewsFlashFragment extends StockBaseFragment {
    private static final String TAG = NewsFlashFragment.class.getSimpleName();

    private static final int HANDLER_MSG_REFRESH = 2;

    private View mLayout;
    private List<NewsFlashItem> mDataList = new ArrayList<NewsFlashItem>();

    private Context mContext;

    private XListView mListView;
    private View mLoadingView;
    private NewsFlashAdapter mNewsFlashAdapter;
    private NewsClient mNewsClient;

    private String mNewsKey;
    private LiveRecordingUtil mLiveRecordingUtil;
    /**
     * 数据加载时用
     */
    private long mMaxID = 0, mMinFilterID = 0;
    private static final int SHOW_NUM_PER_TIME = 20;

    private Handler mHandler;
    static class MyHandler extends Handler {
        WeakReference<NewsFlashFragment> mReference;
        public MyHandler(NewsFlashFragment owner) {
            super();
            mReference = new WeakReference<NewsFlashFragment>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            NewsFlashFragment owner = mReference.get();
            if (owner == null) {
                return;
            }
            switch (msg.what) {

                case HANDLER_MSG_REFRESH: {
                   owner.refreshData(msg.obj);
                }
                break;

            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new MyHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mLayout != null) {
            ViewGroup parent = (ViewGroup) mLayout.getParent();
            if (parent != null) {
                parent.removeView(mLayout);
            }
            return mLayout;
        }
        mLayout = inflater.inflate(R.layout.fragment_newsflash,
                container, false);

        mLiveRecordingUtil = LiveRecordingUtil.getInstance();

        initViews(mLayout);

        initData();


        return mLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        QLog.i(TAG, "NewsFlashFragment onDestroyView ===========");
    }

    private void initViews(View v){
        mContext = getActivity();
        mNewsKey = "00-0000000";    // 目前其它类型已去掉

        mListView = (XListView)v.findViewById(R.id.news_flash_list_view);
        mListView.setPullLoadEnable(false);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }

            @Override
            public void onLoadMore() {
                loadMoreData();
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    NewsFlashItem item = mNewsFlashAdapter.getItem(position - 1);   // 用xListView需减一

                    if (null != item) {
                        Intent intent = new Intent(mContext, NewsDetailActivity.class);
                        intent.putExtra(NewsDetailActivity.KEY_INTENT_NEWS_CATALOGUE, "详情");
                        intent.putExtra(NewsDetailActivity.KEY_INTENT_NEWS_ID, item.getId());
                        mContext.startActivity(intent);
                    }

                }


            }
        });

        mLoadingView = v.findViewById(R.id.content_loading);
    }

    private void initData(){

        // 加载缓存等
        // ..
        // .

        mNewsFlashAdapter= new NewsFlashAdapter(mContext, R.layout.news_flash_item, mDataList);
        mListView.setAdapter(mNewsFlashAdapter);

        mNewsClient = NewsClient.getInstance(mContext);
        mLoadingView.setVisibility(View.VISIBLE);
        refreshList();

    }

    private void onLoaded() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mLoadingView.setVisibility(View.GONE);
    }


    private void refreshList(){
        mMaxID = 0;
        mMinFilterID = 0;
        loadMoreData();;
    }

    private void loadMoreData(){
        mNewsClient.requestNewsList(mNewsKey, -1, mMaxID, mMinFilterID, 0, SHOW_NUM_PER_TIME, new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {

                Message msg = new Message();
                msg.what = HANDLER_MSG_REFRESH;
                msg.obj = object;
                mHandler.sendMessage(msg);

            }

            @Override
            public void onError(int errorCode, String errorString) {
                onLoaded();
            }
        });
    }

    private void refreshData(Object object){
        List<NewsFlashItem> items;
        try {
            items = NewsFlashItem.resolveNewsArray(mNewsKey, (JSONObject) object);
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
                        mDataList.clear();
                    }

                    // 更新请求的索引
                    NewsFlashItem itemLast = items.get(len - 1);
                    mMaxID = itemLast.getId();
                    mMinFilterID = itemLast.getId();    //  （这个应该从数据库中读取最小的，现版本未缓存全部数据，暂时每次都取最新的）

                    mDataList.addAll(items);
                    mNewsFlashAdapter.notifyDataSetChanged();
                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            onLoaded();
        }
    }

}