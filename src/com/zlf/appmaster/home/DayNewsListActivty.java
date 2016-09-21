package com.zlf.appmaster.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.NewsClient;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.QJsonObjectRequest;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.model.DayNewsItem;
import com.zlf.appmaster.model.news.NewsFlashItem;
import com.zlf.appmaster.stocknews.NewsDetailActivity;
import com.zlf.appmaster.stocknews.NewsFlashAdapter;
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.ui.stock.StockBaseFragment;
import com.zlf.appmaster.utils.LeoLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DayNewsListActivty extends Activity {
    public static final int HANDLER_MSG_REFRESH = 2;
    public static final int ERROR_TYPE = -1;
    public static final int NORMAL_TYPE = 1;

    public static final String DAYNEWSLIST = "newslist";


    private List<DayNewsItem> mDataList = new ArrayList<DayNewsItem>();
    private Context mContext;
    private XListView mListView;
    private CircularProgressView mLoadingView;
    private DayNewsListAdapter mDayNewsListAdapter;
    private View mEmptyView;
    private RippleView mRefreshView;
    private CommonToolbar mToolBar;

    /**
     * 数据加载时用
     */
    private long mNowPage = 1;
    private static final int SHOW_NUM_PER_TIME = 20;

    private Handler mHandler;

    static class MyHandler extends Handler {
        WeakReference<DayNewsListActivty> mReference;

        public MyHandler(DayNewsListActivty owner) {
            super();
            mReference = new WeakReference<DayNewsListActivty>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            DayNewsListActivty owner = mReference.get();
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
        setContentView(R.layout.daynewslist_activity);
        mHandler = new MyHandler(this);
        initViews();
        initData();
    }

    private void initViews() {
        mContext = this;

        mToolBar = (CommonToolbar) findViewById(R.id.fb_toolbar);
        mToolBar.setToolbarTitle(getResources().getString(R.string.zlf_day_news_title));

        mEmptyView = findViewById(R.id.empty_view);
        mLoadingView = (CircularProgressView) findViewById(R.id.content_loading);
        mLoadingView.setVisibility(View.VISIBLE);
        mRefreshView = (RippleView) findViewById(R.id.refresh_button);
        mRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLisrByButton();
            }
        });

        mListView = (XListView) findViewById(R.id.news_flash_list_view);
        mListView.setPullLoadEnable(false);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }

            @Override
            public void onLoadMore() {
                mNowPage = mNowPage + 1;
                loadMoreData();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    DayNewsItem info = mDataList.get(position - 1);
                    Intent intent = new Intent(DayNewsListActivty.this, DayNewsDetailActivity.class);
                    intent.putExtra(Constants.DAYNEWS_DETAILS_TITLE, info.getTitle());
                    intent.putExtra(Constants.DAYNEWS_DETAILS_ID, info.getId() + "");
                    intent.putExtra(Constants.DAYNEWS_DETAILS_TIME, info.getTime());
                    DayNewsListActivty.this.startActivity(intent);

                }
            }
        });

    }

    private void refreshLisrByButton() {
        mLoadingView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        refreshList();
    }

    private void initData() {
        mDayNewsListAdapter = new DayNewsListAdapter(this);
        mListView.setAdapter(mDayNewsListAdapter);

        refreshList();
    }

    private void onLoaded(int type) {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mLoadingView.setVisibility(View.GONE);
        if (type == ERROR_TYPE) {
            mListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }


    private void refreshList() {
        mNowPage = 1;
        loadMoreData();
    }


    private void loadMoreData() {

        String url = Constants.ADDRESS + Constants.APPSERVLET + Constants.DATA + DAYNEWSLIST + "&pp=" + mNowPage;
        LeoLog.d("testnewsJson", "new url is  : " + url);


        UniversalRequest.requestNewUrlWithTimeOut("Tag", mContext, url,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        onLoaded(ERROR_TYPE);
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
    private void showData(Object obj) {
        mLoadingView.setVisibility(View.GONE);

        List<DayNewsItem> items = new ArrayList<DayNewsItem>();
        JSONArray array = (JSONArray) obj;

        try {
            for (int i = 0; i < array.length(); i++) {
                DayNewsItem item = new DayNewsItem();

                JSONObject itemObject = array.getJSONObject(i);
                item.setId(itemObject.getInt("id"));
                item.setDesc(itemObject.getString("desc"));
                item.setTitle(itemObject.getString("title"));
                item.setTime(itemObject.getString("newstime"));
                items.add(item);
            }

            Collections.sort(items, COMPARATOR);

            if (null != items) {
                int len = items.size();
                if (len < SHOW_NUM_PER_TIME || mNowPage >=3) {
                    //没有数据了
                    mListView.setPullLoadEnable(false);
                } else {
                    mListView.setPullLoadEnable(true);
                }

                if (len > 0) {
                    mListView.setVisibility(View.VISIBLE);
                    if (mNowPage == 1) {// 刷新
                        mDataList.clear();
                    }

                    mDataList.addAll(items);
                    mDayNewsListAdapter.setList(mDataList);
                    mDayNewsListAdapter.notifyDataSetChanged();
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
                onLoaded(NORMAL_TYPE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            onLoaded(ERROR_TYPE);
        }
    }

    private static final Comparator<DayNewsItem> COMPARATOR = new Comparator<DayNewsItem>() {
        @Override
        public int compare(DayNewsItem lhs, DayNewsItem rhs) {
            return rhs.getTime().compareTo(lhs.getTime());
        }
    };


}