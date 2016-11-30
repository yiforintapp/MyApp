package com.zlf.appmaster.zhibo;

import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.WordChatItem;
import com.zlf.appmaster.ui.PinnedHeaderExpandableListView;
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
 * Created by Administrator on 2016/11/29.
 */
public class WordNewAdviceFragment extends BaseFragment implements View.OnClickListener {

    private XListView mListView;
    private CircularProgressView mProgressBar;
    private View mEmptyView;
    private RippleView mRefreshView;

    private TextView mCurrentBtn;
    private TextView mWaitingBtn;
    private int mIndex;

    private WordZhiboFragmentAdapter mAdapter;
    private List<WordChatItem> mDataList;
    private static final int SHOW_NUM_PER_TIME = 20;
    public static final int ERROR_TYPE = -1;
    public static final int NORMAL_TYPE = 1;
    public static final int LOAD_DATA_TYPE = 1;
    public static final int LOAD_MORE_TYPE = 2;
    public static final String BASE_URL = Constants.WORD_DOMAIN +
            Constants.WORD_SERVLET + Constants.WORD_ZHIBO_MARK;
    public static final String LOAD_DATA = BASE_URL;
    private int mNowPage = 1;
    private String mType;


    private PinnedHeaderExpandableListView explistview;
    private String[][] childrenData = new String[3][3];
    private String[] groupData = new String[3];
    private int expandFlag = -1;//控制列表的展开
    private PinnedHeaderExpandableAdapter adapter;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_advice_new_word;
    }

    @Override
    protected void onInitUI() {
        initViews();
        initData();
    }

    private void initViews() {

        explistview = (PinnedHeaderExpandableListView)findViewById(R.id.explistview);
        mCurrentBtn = (TextView) findViewById(R.id.current_btn);
        mWaitingBtn = (TextView) findViewById(R.id.waiting_btn);
        mCurrentBtn.setOnClickListener(this);
        mWaitingBtn.setOnClickListener(this);
//        mListView = (XListView) findViewById(R.id.quotations_content_list);
        mProgressBar = (CircularProgressView) findViewById(R.id.content_loading);
        mProgressBar.setVisibility(View.GONE);
//        mEmptyView = findViewById(R.id.empty_view);
//        mRefreshView = (RippleView) findViewById(R.id.refresh_button);
//        mRefreshView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                refreshLisrByButton();
//            }
//        });
//
//        mDataList = new ArrayList<WordChatItem>();
//        mAdapter = new WordZhiboFragmentAdapter(mActivity);
//        mListView.setAdapter(mAdapter);
//
//
//        mListView.setPullLoadEnable(true);
//        mListView.setPullRefreshEnable(true);
//        mListView.setXListViewListener(new XListView.IXListViewListener() {
//            @Override
//            public void onRefresh() {
//                requestData(LOAD_DATA_TYPE);
//            }
//
//            @Override
//            public void onLoadMore() {
//                requestData(LOAD_MORE_TYPE);
//            }
//        });


    }

    private void refreshLisrByButton() {
        mProgressBar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        requestData(LOAD_DATA_TYPE);
    }

    private void initData() {
//        mProgressBar.setVisibility(View.VISIBLE);
//        requestData(LOAD_DATA_TYPE);
        for(int i=0;i<3;i++){
            groupData[i] = "" + i;
        }

        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                childrenData[i][j] = "好友"+i+"-"+j;
            }
        }
        //设置悬浮头部VIEW
        explistview.setHeaderView(mActivity.getLayoutInflater().inflate(R.layout.group_head,
                explistview, false));
        adapter = new PinnedHeaderExpandableAdapter(childrenData, groupData, mActivity.getApplicationContext(),explistview);
        explistview.setAdapter(adapter);

        //设置单个分组展开
//        explistview.setOnGroupClickListener(new GroupClickListener());
    }

    class GroupClickListener implements OnGroupClickListener{
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v,
                                    int groupPosition, long id) {
            if (expandFlag == -1) {
                // 展开被选的group
                explistview.expandGroup(groupPosition);
                // 设置被选中的group置于顶端
                explistview.setSelectedGroup(groupPosition);
                expandFlag = groupPosition;
            } else if (expandFlag == groupPosition) {
                explistview.collapseGroup(expandFlag);
                expandFlag = -1;
            } else {
                explistview.collapseGroup(expandFlag);
                // 展开被选的group
                explistview.expandGroup(groupPosition);
                // 设置被选中的group置于顶端
                explistview.setSelectedGroup(groupPosition);
                expandFlag = groupPosition;
            }
            return true;
        }
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
                                    item.setCName(itemObject.getString("c_name"));
                                    item.setTName(itemObject.getString("t_name"));
                                    item.setMsg(itemObject.getString("msg"));
                                    item.setAnswer(itemObject.getString("answer"));
                                    item.setAnswerHeadImg(itemObject.getString("portrait"));

                                    String askTime = itemObject.getString("ask_time");
                                    long ask;
                                    if (!Utilities.isEmpty(askTime)) {
                                        ask = Long.valueOf(askTime) * 1000;
                                    } else {
                                        askTime = "1478502755";
                                        ask = Long.valueOf(askTime) * 1000;
                                    }
                                    item.setAskTime(String.valueOf(ask));

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.current_btn:
                if (mIndex == 0) {
                    return;
                }
                changeTabBg(0);
                break;
            case R.id.waiting_btn:
                if (mIndex == 1) {
                    return;
                }
                changeTabBg(1);
                break;
        }
    }

    private void changeTabBg(int position) {
        switch (position) {
            case 0:
                mCurrentBtn.setBackgroundColor(getResources().getColor(R.color.advice_current_click));
                changeUnSelectBg(mIndex);
                mIndex = 0;
                break;
            case 1:
                mWaitingBtn.setBackgroundColor(getResources().getColor(R.color.advice_waiting_click));
                changeUnSelectBg(mIndex);
                mIndex = 1;
                break;
        }
    }

    private void changeUnSelectBg(int position) {
        switch (position) {
            case 0:
                mCurrentBtn.setBackgroundColor(getResources().getColor(R.color.advice_current));
                break;
            case 1:
                mWaitingBtn.setBackgroundColor(getResources().getColor(R.color.advice_waiting));
                break;
        }
    }


    private static final Comparator<WordChatItem> COMPARATOR = new Comparator<WordChatItem>() {
        @Override
        public int compare(WordChatItem lhs, WordChatItem rhs) {

            return rhs.getAnswerTime().compareTo(lhs.getAnswerTime());
        }
    };

}
