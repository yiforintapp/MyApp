package com.zlf.appmaster.zhibo;

import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.WordChatItem;
import com.zlf.appmaster.model.WordNewAdviceInfo;
import com.zlf.appmaster.model.WordNewAdviceItemInfo;
import com.zlf.appmaster.ui.PinnedHeaderExpandableListView;
import com.zlf.appmaster.ui.RippleView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2016/11/29.
 */
public class WordNewAdviceFragment extends BaseFragment implements View.OnClickListener {

    private CircularProgressView mProgressBar;
    private View mEmptyView;
    private RippleView mRefreshView;

    private TextView mCurrentBtn;
    private TextView mWaitingBtn;
    private int mIndex;

    public static final int ERROR_TYPE = -1;
    public static final int NORMAL_TYPE = 1;
    public static final String BASE_URL = Constants.WORD_DOMAIN +
            Constants.WORD_SERVLET + Constants.WORD_ZHIBO_NEW_ADVICE_MARK;
    public static final String LOAD_DATA = BASE_URL;
    private String mType;
    private int mCurrentIndex; //当前计划


    private PinnedHeaderExpandableListView mXListView;
    private List<WordNewAdviceInfo> mInGroupData;
    private List<WordNewAdviceInfo> mOutGroupData;
    private List<List<WordNewAdviceItemInfo>> mInChildrenData;
    private List<List<WordNewAdviceItemInfo>> mOutChildrenData;
    private int expandFlag = -1;//控制列表的展开
    private PinnedHeaderExpandableAdapter mAdapter;

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

        mXListView = (PinnedHeaderExpandableListView)findViewById(R.id.explistview);
        mCurrentBtn = (TextView) findViewById(R.id.current_btn);
        mWaitingBtn = (TextView) findViewById(R.id.waiting_btn);
        mInGroupData = new ArrayList<WordNewAdviceInfo>();
        mOutGroupData = new ArrayList<WordNewAdviceInfo>();
        mInChildrenData = new ArrayList<List<WordNewAdviceItemInfo>>();
        mOutChildrenData = new ArrayList<List<WordNewAdviceItemInfo>>();
        mCurrentBtn.setOnClickListener(this);
        mWaitingBtn.setOnClickListener(this);
        mXListView.setGroupIndicator(null);
        mXListView.setPullLoadEnable(false);// 禁止下拉加载更多
        mXListView.setXListViewListener(new PinnedHeaderExpandableListView.IXListViewListener() {

            @Override
            public void onRefresh() {
                //设置显示刷新的提示
                requestData();     //测试下拉刷新的数据
            }

            @Override
            public void onLoadMore() {
            }
        });
        mAdapter = new PinnedHeaderExpandableAdapter(mInChildrenData, mInGroupData, mActivity.getApplicationContext(), mCurrentIndex);
        mXListView.setAdapter(mAdapter);
        mProgressBar = (CircularProgressView) findViewById(R.id.content_loading);
        mProgressBar.setVisibility(View.GONE);
        mEmptyView = findViewById(R.id.empty_view);
        mRefreshView = (RippleView) findViewById(R.id.refresh_button);
        mRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLisrByButton();
            }
        });

    }

    private void refreshLisrByButton() {
        mProgressBar.setVisibility(View.VISIBLE);
        mXListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        requestData();
    }

    public void setType (String type) {
        this.mType = type;
    }

    private void initData() {
        mProgressBar.setVisibility(View.VISIBLE);
        requestData();
//        for(int i=0;i<3;i++){
//            groupData[i] = "" + i;
//        }
//
//        for(int i=0;i<3;i++){
//            for(int j=0;j<3;j++){
//                childrenData[i][j] = "好友"+i+"-"+j;
//            }
//        }
        //设置单个分组展开
//        mXListView.setOnGroupClickListener(new GroupClickListener());
    }

    class GroupClickListener implements OnGroupClickListener{
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v,
                                    int groupPosition, long id) {
            if (expandFlag == -1) {
                // 展开被选的group
                mXListView.expandGroup(groupPosition);
                // 设置被选中的group置于顶端
                mXListView.setSelectedGroup(groupPosition);
                expandFlag = groupPosition;
            } else if (expandFlag == groupPosition) {
                mXListView.collapseGroup(expandFlag);
                expandFlag = -1;
            } else {
                mXListView.collapseGroup(expandFlag);
                // 展开被选的group
                mXListView.expandGroup(groupPosition);
                // 设置被选中的group置于顶端
                mXListView.setSelectedGroup(groupPosition);
                expandFlag = groupPosition;
            }
            return true;
        }
    }

    private void onLoaded(int type) {
        mXListView.stopRefresh();
        mXListView.stopLoadMore();
        mProgressBar.setVisibility(View.GONE);
        if (type == ERROR_TYPE) {
            mXListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
        if (mCurrentIndex == 0) {
            for (int i = 0; i < mInGroupData.size(); i++) {
                mXListView.collapseGroup(i);
            }
        } else {
            for (int i = 0; i < mOutGroupData.size(); i++) {
                mXListView.collapseGroup(i);
            }
        }

    }

    /**
     * 请求数据
     */
    private void requestData() {
        String url;
        url = LOAD_DATA + Constants.WORD_TYPE + mType;
        UniversalRequest.requestUrlWithTimeOut("Tag", mActivity, url,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        onLoaded(ERROR_TYPE);
                    }

                    @Override
                    public void onDataFinish(Object object) {

                        JSONObject jsonObject = (JSONObject) object;
                        try {
                            if (jsonObject != null) {
                                mInGroupData.clear();
                                mInChildrenData.clear();
                                mOutGroupData.clear();
                                mOutChildrenData.clear();
                                if (!jsonObject.isNull("in")) {
                                    JSONArray jsonArray = jsonObject.getJSONArray("in");
                                    parseJson(jsonArray, "in");
                                }
                                if (!jsonObject.isNull("out")) {
                                    JSONArray jsonArray = jsonObject.getJSONArray("out");
                                    parseJson(jsonArray, "out");
                                }
                                if (mAdapter != null) {
                                    mAdapter.notifyDataSetChanged();
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

    private void parseJson(JSONArray jsonArray, String type) {
        try {
            WordNewAdviceInfo wordNewAdviceInfo;
            WordNewAdviceItemInfo wordNewAdviceItemInfo;
            List<WordNewAdviceItemInfo> childList;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                wordNewAdviceInfo = new WordNewAdviceInfo();
                if (!jsonObject1.isNull("name")) {
                    wordNewAdviceInfo.setName(jsonObject1.getString("name"));
                }
                if (!jsonObject1.isNull("desc")) {
                    wordNewAdviceInfo.setDesc(jsonObject1.getString("desc"));
                }
                if (!jsonObject1.isNull("icon")) {
                    wordNewAdviceInfo.setIcon(jsonObject1.getString("icon"));
                }
                if (!jsonObject1.isNull("item")) {
                    JSONArray jsonArray1 = (JSONArray) jsonObject1.getJSONArray("item");
                    childList = new ArrayList<WordNewAdviceItemInfo>();
                    for (int j = 0; j < jsonArray1.length(); j++) {
                        JSONObject jsonObject2 = (JSONObject) jsonArray1.get(j);
                        wordNewAdviceItemInfo = new WordNewAdviceItemInfo();
                        if (!jsonObject2.isNull("deal")) {
                            wordNewAdviceItemInfo.setDeal(jsonObject2.getString("deal"));
                        }
                        if (!jsonObject2.isNull("realname")) {
                            wordNewAdviceItemInfo.setRealName(jsonObject2.getString("realname"));
                        }
                        if (!jsonObject2.isNull("time")) {
                            long time = Long.parseLong(jsonObject2.getString("time")) * 1000;
                            wordNewAdviceItemInfo.setTime(String.valueOf(time));
                        }
                        if (!jsonObject2.isNull("enterpoint")) {
                            wordNewAdviceItemInfo.setEnterPoint(jsonObject2.getString("enterpoint"));
                        }
                        if (!jsonObject2.isNull("profit")) {
                            wordNewAdviceItemInfo.setProfit(jsonObject2.getString("profit"));
                        }
                        if (!jsonObject2.isNull("lose")) {
                            wordNewAdviceItemInfo.setLose(jsonObject2.getString("lose"));
                        }
                        if (!jsonObject2.isNull("remark")) {
                            wordNewAdviceItemInfo.setRemark(jsonObject2.getString("remark"));
                        }
                        childList.add(wordNewAdviceItemInfo);
                    }
                    if ("in".equals(type)) {
                        mInChildrenData.add(childList);
                    } else if ("out".equals(type)) {
                        mOutChildrenData.add(childList);
                    }
                }
                if ("in".equals(type)) {
                    mInGroupData.add(wordNewAdviceInfo);
                } else if ("out".equals(type)) {
                    mOutGroupData.add(wordNewAdviceInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            onLoaded(ERROR_TYPE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.current_btn:
                if (mIndex == 0) {
                    return;
                }
                mCurrentIndex = 0;
                changeTabBg(0);
                changeList();
                break;
            case R.id.waiting_btn:
                if (mIndex == 1) {
                    return;
                }
                mCurrentIndex = 1;
                changeTabBg(1);
                changeList();
                break;
        }
    }

    private void changeList() {
        if (mCurrentIndex == 0) {
            mAdapter = new PinnedHeaderExpandableAdapter(mInChildrenData,
                    mInGroupData, mActivity.getApplicationContext(), mCurrentIndex);

        } else if (mCurrentIndex == 1) {
            mAdapter = new PinnedHeaderExpandableAdapter(mOutChildrenData,
                    mOutGroupData, mActivity.getApplicationContext(), mCurrentIndex);
        }
        mXListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void changeTabBg(int position) {
        switch (position) {
            case 0:
                mCurrentBtn.setBackgroundColor(getResources().getColor(R.color.advice_current));
                changeUnSelectBg(mIndex);
                mIndex = 0;
                break;
            case 1:
                mWaitingBtn.setBackgroundColor(getResources().getColor(R.color.advice_waiting));
                changeUnSelectBg(mIndex);
                mIndex = 1;
                break;
        }
    }

    private void changeUnSelectBg(int position) {
        switch (position) {
            case 0:
                mCurrentBtn.setBackgroundColor(getResources().getColor(R.color.advice_current_click));
                break;
            case 1:
                mWaitingBtn.setBackgroundColor(getResources().getColor(R.color.advice_waiting_click));
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
