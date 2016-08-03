package com.zlf.appmaster.hometab;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockQuotationsClient;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.topic.TopicInfo;
import com.zlf.appmaster.utils.LiveRecordingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huang on 2015/6/12.
 */
public class StockTopicFragment extends BaseFragment {
    private static String TAG = StockTopicFragment.class.getSimpleName();

    private Context mContext;
    private View mLayout;
    private TopicListAdapter mTopicInfoListAdapter;
    private List<TopicInfo> mData;
    private StockQuotationsClient mStockClient;

    private XListView mListView;
    private ProgressBar mProgressBar;
    private int mCurEndIndex = 0;
    private static final int LOAD_ITEM_NUM = 10;
    private LiveRecordingUtil mLiveRecordingUtil;

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // TODO Auto-generated method stub
//        if (mLayout != null){//初始化过了 就不需要再创建
//            ViewGroup parent = (ViewGroup) mLayout.getParent();//清除自己 再返回 否则会重复设置了父控件
//            if (parent != null) {
//                parent.removeView(mLayout);
//            }
//            initData();
//            return mLayout;
//        }
//        mLayout= inflater.inflate(R.layout.fragment_quotations_content, null);
//        mContext = getActivity();
//
//        mStockClient = StockQuotationsClient.getInstance(mContext);
//
//        mLiveRecordingUtil = LiveRecordingUtil.getInstance();
//
//
//        initViews(mLayout);
//
//        loadCache();
//
//        initData();
//
//        return mLayout;
//    }

    private boolean loadCache() {
        boolean ret = false;
        JSONObject jsonCache = StockJsonCache.loadFromFile(mActivity, StockJsonCache.CACHEID_QUOTATIONS_TOPIC);

        if (jsonCache != null) {

            try {

                List<TopicInfo> loadData = TopicInfo.resolveJSONObjectArray(jsonCache);

                mData.clear();
                mData.addAll(loadData);

                ret = true;

                mTopicInfoListAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    private void initViews(View view){

        mListView = (XListView) findViewById(R.id.quotations_content_list);
        mProgressBar = (ProgressBar) findViewById(R.id.content_loading);

        mData = new ArrayList<TopicInfo>();
        mTopicInfoListAdapter = new TopicListAdapter(mActivity, mData);
        mListView.setAdapter(mTopicInfoListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position > 0) {
//                    TopicInfo topicInfo = (TopicInfo) mTopicInfoListAdapter.getItem(position - 1);
//                    if (null != topicInfo) {
//                        if(!mLiveRecordingUtil.isLiveRecording()) {
//                            Intent intent = new Intent(mContext, TopicDetailActivity.class);
//                            intent.putExtra(TopicDetailActivity.INTENT_FLAG_TOPIC_ID, topicInfo.getTopicID());
//                            intent.putExtra(TopicDetailActivity.INTENT_FLAG_TOPIC_NAME, topicInfo.getName());
//                            mContext.startActivity(intent);
//                        }else{
//                            Intent intent = new Intent(mContext, TopicDetailActivity.class);
//                            intent.putExtra(TopicDetailActivity.INTENT_FLAG_TOPIC_ID, topicInfo.getTopicID());
//                            intent.putExtra(TopicDetailActivity.INTENT_FLAG_TOPIC_NAME, topicInfo.getName());
//                            mContext.startActivity(intent);
//                        }
//                    }
                }
            }
        });

        mListView.setPullLoadEnable(false); // 启用下拉加载
        mListView.setPullRefreshEnable(true);//上拉
        //mListView.setVisibility(View.GONE);
        mListView.setXListViewListener(new XListView.IXListViewListener() {

            @Override
            public void onRefresh() {
                if (mProgressBar.getVisibility() == View.VISIBLE){  // 主进程正在更新
                    onLoaded();
                }
                else{
                    refreshList();
                }
            }

            @Override
            public void onLoadMore() {
                loadMoreList();
            }
        });

    }

    private void initData(){
        mProgressBar.setVisibility(View.VISIBLE);
        refreshList();

    }

    //刷新
    private void refreshList() {
        mCurEndIndex = 0;
        //   mListView.setPullLoadEnable(true);

        loadMoreList();
    }

    /**
     * 请求题材行情信息
     */
    private void loadMoreList(){

        mStockClient.requestTopicInfo(mCurEndIndex, LOAD_ITEM_NUM, new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {
                onLoaded();
                mProgressBar.setVisibility(View.GONE);

                if (null != object) {
                    //mDataItems.clear();
                    if (mCurEndIndex == 0) {//刷新的 要清除
                        mData.clear();
                    }

                    mData.addAll((List<TopicInfo>) object);
                    //mListView.setVisibility(View.VISIBLE);
                }

                int num = mData.size();

                if (num - mCurEndIndex >= LOAD_ITEM_NUM) {
                    mCurEndIndex = num;
                    mListView.setPullLoadEnable(true);
                } else {
                    //没有更多了
                    mListView.setPullLoadEnable(false);
                }

                mTopicInfoListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onError(int errorCode, String errorString) {
                onLoaded();
                mProgressBar.setVisibility(View.GONE);
                mListView.setPullLoadEnable(false);
            }
        });
    }

    private void onLoaded() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
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

        mStockClient = StockQuotationsClient.getInstance(mContext);
        mLiveRecordingUtil = LiveRecordingUtil.getInstance();
        initViews(mLayout);
        loadCache();
        initData();
    }
}
