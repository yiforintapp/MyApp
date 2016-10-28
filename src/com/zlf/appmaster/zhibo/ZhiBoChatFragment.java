package com.zlf.appmaster.zhibo;

import android.view.View;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockQuotationsClient;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.ChatItem;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.utils.LeoLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2016/8/15.
 */
public class ZhiBoChatFragment extends BaseFragment implements View.OnClickListener {
    private static final int SHOW_NUM_PER_TIME = 20;
    public static final int ERROR_TYPE = -1;
    public static final int NORMAL_TYPE = 1;
    public static final int LOAD_DATA_TYPE = 1;
    public static final int LOAD_MORE_TYPE = 2;
    public static final String BASE_URL = Constants.CHAT_DOMAIN +
            Constants.CHAT_SERVLET + Constants.CHAT_MARK;
    public static final String LOAD_DATA = BASE_URL + "chat_new";
    public static final String LOAD_MORE = BASE_URL + "chat_more&page=";
    private int mNowPage = 1;
    private ChatFragmentAdapter chatAdapter;
    private List<StockIndex> mIndexData;
    private List<StockIndex> mForeignIndexData;
    private StockQuotationsClient mStockClient;

    private XListView mListView;
    private CircularProgressView mProgressBar;
    private View mEmptyView;
    private RippleView mRefreshView;
    private TextView mSendButton;
    private List<ChatItem> mDataList;

    @Override
    protected int layoutResourceId() {
        return R.layout.zhibo_chat_fragment;
    }

    @Override
    protected void onInitUI() {
        mStockClient = StockQuotationsClient.getInstance(mActivity);
        initViews();
        initData();
    }

    private void initViews() {

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

        mSendButton = (TextView) findViewById(R.id.tv_submit);
        mSendButton.setOnClickListener(this);


        mIndexData = new ArrayList<StockIndex>();
        mForeignIndexData = new ArrayList<StockIndex>();
        chatAdapter = new ChatFragmentAdapter(mActivity);
        mListView.setAdapter(chatAdapter);

        mDataList = new ArrayList<ChatItem>();

        mListView.setPullLoadEnable(false);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                requestData(LOAD_MORE_TYPE);
            }

            @Override
            public void onLoadMore() {
                requestData(LOAD_DATA_TYPE);
            }
        });

    }

    private void refreshLisrByButton() {
        mProgressBar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        requestData(LOAD_DATA_TYPE);
    }

    private void initData() {
        mProgressBar.setVisibility(View.VISIBLE);
        requestData(LOAD_DATA_TYPE);
    }

//    private void onLoaded() {
//        mListView.setSelection(chatAdapter.getCount()-1);
//        mListView.stopRefresh();
//        mListView.stopLoadMore();
//    }

    private void onLoaded(int type) {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mProgressBar.setVisibility(View.GONE);
        if (type == ERROR_TYPE) {
            mListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 请求数据
     */
    private void requestData(final int type) {
        LeoLog.d("CHAT", "now page : " + mNowPage);
        String url;
        if (type == LOAD_DATA_TYPE) {
            mNowPage = 1;
            url = LOAD_DATA;
        } else {
            mNowPage += 1;
            url = LOAD_MORE + mNowPage;
        }
        LeoLog.d("CHAT", "request page : " + mNowPage);
        LeoLog.d("CHAT", "url : " + url);

        UniversalRequest.requestNewUrlWithTimeOut("Tag", mActivity, url,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        onLoaded(ERROR_TYPE);
                    }

                    @Override
                    public void onDataFinish(Object object) {

                        List<ChatItem> items = new ArrayList<ChatItem>();
                        JSONArray array = (JSONArray) object;

                        try {
                            for (int i = 0; i < array.length(); i++) {
                                ChatItem item = new ChatItem();

                                JSONObject itemObject = array.getJSONObject(i);
                                item.setDate(itemObject.getString("date"));
                                item.setText(itemObject.getString("content"));
                                item.setName(itemObject.getString("name"));
                                items.add(item);
                            }


                            if (null != items) {
                                int len = items.size();

                                if (type == LOAD_DATA_TYPE) {
                                    if (len > 0) {
                                        mDataList.clear();
                                        mListView.setVisibility(View.VISIBLE);
                                        mDataList.addAll(items);
                                        Collections.sort(mDataList, COMPARATOR);
                                        chatAdapter.setList(mDataList);
                                        chatAdapter.notifyDataSetChanged();
                                        mListView.setSelection(mDataList.size() - 1);
                                        onLoaded(NORMAL_TYPE);
                                        mListView.setPullRefreshEnable(true);
                                    } else {
                                        onLoaded(ERROR_TYPE);
                                    }

                                } else {
                                    if (len > 0) {

                                        int addBefore = mDataList.size();
                                        LeoLog.d("CHAT", "addBefore : " + addBefore);

                                        mListView.setVisibility(View.VISIBLE);
                                        mDataList.addAll(items);
                                        Collections.sort(mDataList, COMPARATOR);
                                        chatAdapter.setList(mDataList);
                                        chatAdapter.notifyDataSetChanged();
                                        mListView.setSelection(len);
                                        if (len < SHOW_NUM_PER_TIME || mNowPage >= 5) {
                                            mListView.setPullRefreshEnable(false);
                                        } else {
                                            mListView.setPullRefreshEnable(true);
                                        }
                                    } else {
                                        mListView.setPullRefreshEnable(false);
                                    }
                                }
                                onLoaded(NORMAL_TYPE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            onLoaded(ERROR_TYPE);
                        }
                    }
                }, false, 0, false);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {

    }

    private static final Comparator<ChatItem> COMPARATOR = new Comparator<ChatItem>() {
        @Override
        public int compare(ChatItem lhs, ChatItem rhs) {
            return lhs.getDate().compareTo(rhs.getDate());
        }
    };
}

