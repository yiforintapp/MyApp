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
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.NewsClient;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.model.news.NewsFlashItem;
import com.zlf.appmaster.ui.stock.StockBaseFragment;
import com.zlf.appmaster.utils.LeoLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NewsAnalyFragment extends StockBaseFragment {
    private static final int HANDLER_MSG_REFRESH = 2;

    private View mLayout;
    private List<NewsFlashItem> mDataList = new ArrayList<NewsFlashItem>();
    private Context mContext;
    private XListView mListView;
    private View mLoadingView;
    private NewsFlashAdapter mNewsFlashAdapter;

    /**
     * 数据加载时用
     */
    private long mNowPage = 1;
    private static final int SHOW_NUM_PER_TIME = 20;

    private Handler mHandler;

    static class MyHandler extends Handler {
        WeakReference<NewsAnalyFragment> mReference;
        public MyHandler(NewsAnalyFragment owner) {
            super();
            mReference = new WeakReference<NewsAnalyFragment>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            NewsAnalyFragment owner = mReference.get();
            if (owner == null) {
                return;
            }
            switch (msg.what) {

                case HANDLER_MSG_REFRESH: {
                    owner.showData(msg.obj);
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


        initViews(mLayout);
        initData();


        return mLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initViews(View v){
        mContext = getActivity();

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
                LeoLog.d("testnewsJson","loadMoreData");
                mNowPage = mNowPage + 1;
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
                        intent.putExtra(NewsDetailActivity.KEY_INTENT_NEWS_CATALOGUE, "分析研究");
                        intent.putExtra(NewsDetailActivity.KEY_INTENT_NEWS_ID, item.getidString());
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

        mLoadingView.setVisibility(View.VISIBLE);
        refreshList();

    }

    private void onLoaded() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mLoadingView.setVisibility(View.GONE);
    }


    private void refreshList(){
        mNowPage = 1;
        loadMoreData();
    }


    private void loadMoreData(){

        String url = NewsClient.PATH_FROM_CRM_NEWS + "proname=" +
                Constants.NEWS_TYPE_ANALY + "&pp=" + mNowPage;
        LeoLog.d("testnewsJson","new url is  : " + url);


        UniversalRequest.requestNewUrlWithTimeOut("Tag", mContext, url,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        onLoaded();
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        Message msg = new Message();
                        msg.what = HANDLER_MSG_REFRESH;
                        msg.obj = object;
                        mHandler.sendMessage(msg);
                    }
                }, false, 0, false);

    }


    //记得删除
    private void showData(Object obj){
        List<NewsFlashItem> items = new ArrayList<NewsFlashItem>();
        JSONArray array = (JSONArray) obj;

        try{
            for (int i = 0; i < array.length(); i++) {
                NewsFlashItem item = new NewsFlashItem();

                JSONObject itemObject = array.getJSONObject(i);
                item.setNewsKey("");
                item.setidString(itemObject.getString("id"));
                item.setClassify(70);
                item.setIsChanged(true);
                item.setTimeString(itemObject.getString("created"));
                item.setTitle(itemObject.getString("title"));
                item.setSummary("");
                item.setMedia("");
                items.add(item);
            }

            Collections.sort(items, COMPARATOR);

            if (null != items) {
                int len = items.size();
                if (len < SHOW_NUM_PER_TIME) {
                    //没有数据了
                    mListView.setPullLoadEnable(false);
                } else {
                    mListView.setPullLoadEnable(true);
                }

                if (len > 0) {
                    if (mNowPage == 1) {// 刷新
                        mDataList.clear();
                    }


                    mDataList.addAll(items);
                    mNewsFlashAdapter.notifyDataSetChanged();
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            onLoaded();
        }

    }

    private static final Comparator<NewsFlashItem> COMPARATOR = new Comparator<NewsFlashItem>() {
        @Override
        public int compare(NewsFlashItem lhs, NewsFlashItem rhs) {
            return rhs.getTimeString().compareTo(lhs.getTimeString());
        }
    };



}