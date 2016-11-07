package com.zlf.appmaster.zhibo;

import android.view.View;
import android.widget.AdapterView;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.UniversalRequest;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.WordAdviceItem;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.ui.dialog.AdviceDialog;
import com.zlf.appmaster.utils.LeoLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2016/11/4.
 */
public class WordNoticeFragment extends BaseFragment {

    private WordNoticeFragmentAdapter mAdapter;
    private List<WordAdviceItem> mDataList;

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
            Constants.WORD_SERVLET + Constants.WORD_ADVICE_MARK;
    public static final String LOAD_DATA = BASE_URL;
    private int mNowPage = 1;

    private AdviceDialog mDialog;

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

        mDataList = new ArrayList<WordAdviceItem>();
        mAdapter = new WordNoticeFragmentAdapter(mActivity);
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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mDialog == null) {
                    mDialog = new AdviceDialog(mActivity);
                }
                mDialog.setContent(mDataList.get(position).getContent());
                mDialog.show();
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

        UniversalRequest.requestNewUrlWithTimeOut("Tag", mActivity, url,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        onLoaded(ERROR_TYPE);
                    }

                    @Override
                    public void onDataFinish(Object object) {

                        List<WordAdviceItem> items = new ArrayList<WordAdviceItem>();
                        JSONArray array = (JSONArray) object;

                        try {
                            for (int i = 0; i < array.length(); i++) {
                                WordAdviceItem item = new WordAdviceItem();

                                JSONObject itemObject = array.getJSONObject(i);
                                item.setContent(itemObject.getString("content"));
                                String time = itemObject.getString("date");
                                long a = Long.valueOf(time) * 1000;
                                item.setDate(String.valueOf(a));
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

                                        mListView.setVisibility(View.VISIBLE);
                                        mDataList.addAll(items);
                                        Collections.sort(mDataList, COMPARATOR);
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

    private static final Comparator<WordAdviceItem> COMPARATOR = new Comparator<WordAdviceItem>() {
        @Override
        public int compare(WordAdviceItem lhs, WordAdviceItem rhs) {
            return rhs.getDate().compareTo(lhs.getDate());
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
