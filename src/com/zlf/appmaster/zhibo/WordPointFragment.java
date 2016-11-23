package com.zlf.appmaster.zhibo;

import android.text.TextUtils;
import android.view.View;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.WordChatItem;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2016/11/15.
 */
public class WordPointFragment extends BaseFragment {

    private WordPointFragmentAdapter mAdapter;
    private List<WordChatItem> mDataList;

    private XListView mListView;
    private CircularProgressView mProgressBar;
    private View mEmptyView;
    private RippleView mRefreshView;

    private static final int SHOW_NUM_PER_TIME = 20;
    public static final int ERROR_TYPE = -1;
    public static final int NORMAL_TYPE = 1;
    public static final int LOAD_DATA_TYPE = 1;
    public static final int LOAD_MORE_TYPE = 2;
    public static final String BASE_URL = Constants.WORD_DOMAIN +
            Constants.WORD_SERVLET + Constants.WORD_POINT_MARK;
    public static final String LOAD_DATA = BASE_URL;
    private int mNowPage = 1;
    private String mType;

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

        mDataList = new ArrayList<WordChatItem>();
        mAdapter = new WordPointFragmentAdapter(mActivity);
        mListView.setAdapter(mAdapter);


        mListView.setPullLoadEnable(true);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                requestData(LOAD_DATA_TYPE);
            }

            @Override
            public void onLoadMore() {
                requestData(LOAD_MORE_TYPE);
            }
        });


    }

    public void setType (String type) {
        this.mType = type;
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
        } else {
            mNowPage += 1;
        }
        url = LOAD_DATA + mNowPage + Constants.WORD_TYPE + mType;
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

                        List<WordChatItem> items = new ArrayList<WordChatItem>();
                        JSONArray array = (JSONArray) object;

                        try {
                            if (array != null && array.length() > 0) {
                                for (int i = 0; i < array.length(); i++) {
                                    WordChatItem item = new WordChatItem();

                                    JSONObject itemObject = array.getJSONObject(i);
                                    item.setTName(itemObject.getString("t_name"));
                                    item.setAnswer(itemObject.getString("answer"));
                                    if (!TextUtils.isEmpty(itemObject.getString("image_url"))) {
                                        item.setAnswerImg(itemObject.getString("image_url"));
                                    }
                                    String answerTime = itemObject.getString("answer_time");
                                    long a;
                                    if (!Utilities.isEmpty(answerTime)) {
                                        a = Long.valueOf(answerTime) * 1000;
                                    } else {
                                        answerTime = "1478502755";
                                        a = Long.valueOf(answerTime) * 1000;
                                    }
                                    item.setAnswerTime(String.valueOf(a));

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
                                            mAdapter.setList(mDataList);
                                            mAdapter.notifyDataSetChanged();
                                            mListView.setSelection(0);
                                            onLoaded(NORMAL_TYPE);
                                            if (len < SHOW_NUM_PER_TIME || mNowPage >= 5) {
                                                mListView.setPullLoadEnable(false);
                                            } else {
                                                mListView.setPullLoadEnable(true);
                                            }
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
                                            mAdapter.setList(mDataList);
                                            mAdapter.notifyDataSetChanged();
                                            mListView.setSelection(mDataList.size() - items.size());
                                            if (len < SHOW_NUM_PER_TIME || mNowPage >= 5) {
                                                mListView.setPullLoadEnable(false);
                                            } else {
                                                mListView.setPullLoadEnable(true);
                                            }
                                        } else {
                                            mListView.setPullLoadEnable(false);
                                        }
                                    }
                                    onLoaded(NORMAL_TYPE);
                                }
                            } else {
                                mListView.setPullLoadEnable(false);
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
    protected int layoutResourceId() {
        return R.layout.fragment_quotations_content;
    }

    @Override
    protected void onInitUI() {
        initViews();
        initData();
    }

    private static final Comparator<WordChatItem> COMPARATOR = new Comparator<WordChatItem>() {
        @Override
        public int compare(WordChatItem lhs, WordChatItem rhs) {

            return rhs.getAnswerTime().compareTo(lhs.getAnswerTime());
        }
    };


}
