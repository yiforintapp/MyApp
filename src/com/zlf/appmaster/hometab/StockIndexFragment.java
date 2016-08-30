package com.zlf.appmaster.hometab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockQuotationsClient;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.stockIndex.StockIndexDetailActivity;
import com.zlf.appmaster.utils.LiveRecordingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huang on 2015/3/5.
 */
public class StockIndexFragment extends BaseFragment {
    private static final String TAG = StockIndexFragment.class.getSimpleName();
    private Context mContext;
    private View mLayout;
    private StockQuotationsIndexAdapter mStockQuotationsIndexAdapter;
    private List<StockIndex> mIndexData;
    private List<StockIndex> mForeignIndexData;
    private StockQuotationsClient mStockClient;

    private XListView mListView;
    private CircularProgressView mProgressBar;
    private LiveRecordingUtil mLiveRecordingUtil;

//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // TODO Auto-generated method stub
//        if (mLayout != null){//初始化过了 就不需要再创建
//            ViewGroup parent = (ViewGroup) mLayout.getParent();//清除自己 再返回 否则会重复设置了父控件
//            if (parent != null) {
//                parent.removeView(mLayout);
//            }
//            requestData();
//            return mLayout;
//        }
//        mLayout= inflater.inflate(R.layout.fragment_quotations_content, null);
//        mContext = getActivity();
//        Bundle bundle = getArguments();
//        mLiveRecordingUtil = LiveRecordingUtil.getInstance();
//        mStockClient = StockQuotationsClient.getInstance(mContext);
//
//        initViews(mLayout);
//
//        loadCache();
//        initData();
//
//        return mLayout;
//    }

    private boolean loadCache(){
        boolean ret = false;
        JSONObject jsonCache = StockJsonCache.loadFromFile(mActivity, StockJsonCache.CACHEID_QUOTATIONS_INDEX);

        if (jsonCache != null){

            try {

                JSONObject data = jsonCache.getJSONObject("data");

                List<StockIndex> stockIndexes = StockIndex
                        .resolveAllIndexJsonObject(data.getJSONArray("indexs"));
                mIndexData.clear();
                mIndexData.addAll(stockIndexes);

                List<StockIndex> foreignDelayIndexes = StockIndex
                        .resolveAllIndexJsonObject(data.getJSONArray("slowIndexs"));        // 国外的延迟指数
                mForeignIndexData.clear();
                mForeignIndexData.addAll(foreignDelayIndexes);

                ret = true;

                mStockQuotationsIndexAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }
    private void initViews(View view){

        mListView = (XListView) findViewById(R.id.quotations_content_list);
        mProgressBar = (CircularProgressView) findViewById(R.id.content_loading);

        mIndexData = new ArrayList<StockIndex>();
        mForeignIndexData = new ArrayList<StockIndex>();
        mStockQuotationsIndexAdapter = new StockQuotationsIndexAdapter(mActivity, mIndexData, mForeignIndexData);
        mListView.setAdapter(mStockQuotationsIndexAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                StockIndex item = (StockIndex)mStockQuotationsIndexAdapter.getItem(position - 1);
                if (null != item) {
                    Class targetClass;

//                    if(mLiveRecordingUtil.isLiveRecording()){
//                        targetClass = LiveAnchorLectureActivity.class;
//                    }else{
                        targetClass = StockIndexDetailActivity.class;
//                    }
                    Intent intent = new Intent(mActivity, targetClass);

//                    if(targetClass == LiveAnchorLectureActivity.class){
//                        mLiveRecordingUtil.setStockName(item.getName());
//                        mLiveRecordingUtil.setStockCode(item.getCode());
//                        mLiveRecordingUtil.setStock(false);
//                        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    }else{
                        intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_INDEXCODE, item.getCode());
                        intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_INDEXNAME, item.getName());
//                    }
                    mActivity.startActivity(intent);

                }
            }
        });

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
        mStockClient.requestStockIndexAll(new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {
                Object[] objectArray = (Object[])object;

                mIndexData.clear();
                mIndexData.addAll((List<StockIndex>) objectArray[0]);

                mForeignIndexData.clear();
                mForeignIndexData.addAll((List<StockIndex>) objectArray[1]);

                mStockQuotationsIndexAdapter.notifyDataSetChanged();

                mProgressBar.setVisibility(View.GONE);
                onLoaded();
            }

            @Override
            public void onError(int errorCode, String errorString) {

                mProgressBar.setVisibility(View.GONE);
                onLoaded();
            }
        });
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
        mStockClient = StockQuotationsClient.getInstance(mActivity);
        Bundle bundle = getArguments();
        mLiveRecordingUtil = LiveRecordingUtil.getInstance();
        initViews(mLayout);

//      loadCache();
        initData();
    }
}
