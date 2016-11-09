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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2016/11/2.
 */
public class WordZhiBoFragment extends BaseFragment {


    private WordZhiboFragmentAdapter mAdapter;
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
            Constants.WORD_SERVLET + Constants.WORD_ZHIBO_MARK;
    public static final String LOAD_DATA = BASE_URL;
    private int mNowPage = 1;

    private void initViews(){

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
        mAdapter = new WordZhiboFragmentAdapter(mActivity);
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

    private void refreshLisrByButton() {
        mProgressBar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        requestData(LOAD_DATA_TYPE);
    }

    private void initData(){
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
        url = LOAD_DATA + mNowPage + Constants.WORD_TYPE + "1";
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
                            for (int i = 0; i < array.length(); i++) {
                                WordChatItem item = new WordChatItem();

                                JSONObject itemObject = array.getJSONObject(i);
                                item.setCName(itemObject.getString("c_name"));
                                item.setTName(itemObject.getString("t_name"));
                                item.setMsg(itemObject.getString("msg"));
                                item.setAnswer(itemObject.getString("answer"));
                                String askTime = itemObject.getString("ask_time");
                                long a = Long.valueOf(askTime) * 1000;
                                item.setAskTime(String.valueOf(a));
                                String answerTime = itemObject.getString("answer_time");
                                long b = Long.valueOf(answerTime) * 1000;
                                item.setAnswerTime(String.valueOf(b));
                                items.add(item);
                            }


                            if (null != items) {
                                int len = items.size();

                                if (type == LOAD_DATA_TYPE) {
                                    if (len > 0) {
                                        mDataList.clear();
                                        mListView.setVisibility(View.VISIBLE);
                                        mDataList.addAll(items);
//                                        Collections.sort(mDataList, COMPARATOR);
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
//                                        Collections.sort(mDataList, COMPARATOR);
                                        mAdapter.setList(mDataList);
                                        mAdapter.notifyDataSetChanged();
                                        mListView.setSelection(mDataList.size() - len);
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
            boolean isLhsAskEmpty = TextUtils.isEmpty(lhs.getAskTime());
            boolean isLhsAnswerEmpty = TextUtils.isEmpty(lhs.getAnswerTime());
            boolean isRhsAskEmpty = TextUtils.isEmpty(rhs.getAskTime());
            boolean isRhsAnswerEmpty = TextUtils.isEmpty(rhs.getAnswerTime());

            return rhs.getAnswerTime().compareTo(lhs.getAnswerTime());
        }
    };
}
