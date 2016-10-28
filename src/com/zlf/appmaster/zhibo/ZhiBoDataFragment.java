package com.zlf.appmaster.zhibo;

import android.view.View;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.ChatItem;
import com.zlf.appmaster.tradetab.StockIndexFragment;
import com.zlf.appmaster.ui.RippleView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/15.
 */
public class ZhiBoDataFragment extends BaseFragment {
    private static final String TAG = StockIndexFragment.class.getSimpleName();
    private DataFragmentAdapter mAdapter;
    private List<ChatItem> mDataList;

    private XListView mListView;
    private CircularProgressView mProgressBar;
    private View mEmptyView;
    private RippleView mRefreshView;


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

        mDataList = new ArrayList<ChatItem>();
        mAdapter = new DataFragmentAdapter(mActivity);
        mListView.setAdapter(mAdapter);


        mListView.setPullLoadEnable(false);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                if (mProgressBar.getVisibility() == View.VISIBLE){  // 主进程正在更新
                    onLoaded();
                }
                else{
                    requestData();
                }
            }

            @Override
            public void onLoadMore() {

            }
        });


    }

    private void refreshLisrByButton() {
        mProgressBar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        requestData();
    }

    private void initData(){
        mProgressBar.setVisibility(View.VISIBLE);

        requestData();
    }

    private void onLoaded() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
    }

    /**
     * 请求数据
     */
    private void requestData(){
        for (int i = 0; i < 10 ; i ++) {
            mDataList.add(new ChatItem());
        }
        mAdapter.setList(mDataList);
        mAdapter.notifyDataSetChanged();
        mProgressBar.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        onLoaded();

    }

    @Override
    public void onResume() {
        super.onResume();
//        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
//        MobclickAgent.onPageEnd( TAG );
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
}

