package com.zlf.appmaster.hometab;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.industry.IndustryInfo;
import com.zlf.appmaster.stockindustry.IndustryStockListActivity;
import com.zlf.appmaster.utils.LiveRecordingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huang on 2015/3/5.
 */
public class StockPlateFragment extends BaseFragment {
    private static final String TAG = StockPlateFragment.class.getSimpleName();
    private Context mContext;
    private View mLayout;
    //private StockPlateAdapter mStockPlateAdapter;
    private IndustryListAdapter mStockPlateAdapter;
    private List<IndustryInfo> mIndustryLedUpArray;

    //private StockQuotationsClient mStockClient;
    private StockClient mStockClient;

    private XListView mListView;
    private ProgressBar mProgressBar;
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
//        mLiveRecordingUtil = LiveRecordingUtil.getInstance();
//        mStockClient = new StockClient(mContext);
//
//        initViews(mLayout);
//        mProgressBar.setVisibility(View.VISIBLE);
//        loadCache();
//        initData();
//
//        return mLayout;
//    }
    private boolean loadCache(){
        boolean ret = false;
        JSONObject jsonCache = StockJsonCache.loadFromFile(mContext, StockJsonCache.CACHEID_QUOTATIONS_INDUSTRY);

        if (jsonCache != null){

            try {

                ArrayList<IndustryInfo> loadData = IndustryInfo.resolveJSONObjectArray(jsonCache);

                mIndustryLedUpArray.clear();
                mIndustryLedUpArray.addAll(loadData);

                ret = true;

                mStockPlateAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }
    private void initViews(View view){

        mListView = (XListView) findViewById(R.id.quotations_content_list);
        mProgressBar = (ProgressBar) findViewById(R.id.content_loading);

        mIndustryLedUpArray = new ArrayList<IndustryInfo>();

        mStockPlateAdapter = new IndustryListAdapter(mContext, mIndustryLedUpArray);
        mListView.setAdapter(mStockPlateAdapter);

        //mListView.setVisibility(View.GONE);
        mListView.setPullLoadEnable(false);
        mListView.setPullRefreshEnable(true);
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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                IndustryInfo industryInfo = (IndustryInfo)mStockPlateAdapter.getItem(position - 1);

                if (null != industryInfo){
                    if(!mLiveRecordingUtil.isLiveRecording()) {
                        Intent intent = new Intent(mActivity, IndustryStockListActivity.class);
                        intent.putExtra(IndustryStockListActivity.INTENT_FLAG_INDUSTRYID, industryInfo.getIndustryID());
                        intent.putExtra(IndustryStockListActivity.INTENT_FLAG_INDUSTRYNAME, industryInfo.getName());
                        intent.putExtra(IndustryStockListActivity.INTENT_FLAG_DISPLAY_TYPE, 1);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(mActivity, IndustryStockListActivity.class);
                        intent.putExtra(IndustryStockListActivity.INTENT_FLAG_INDUSTRYID, industryInfo.getIndustryID());
                        intent.putExtra(IndustryStockListActivity.INTENT_FLAG_INDUSTRYNAME, industryInfo.getName());
                        intent.putExtra(IndustryStockListActivity.INTENT_FLAG_DISPLAY_TYPE, 1);
                        startActivity(intent);
                    }
                }

            }
        });

    }

    private void initData(){
        refreshList();
    }


    private void onLoaded() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
    }


    //刷新
    private void refreshList() {
        loadMoreList();
    }

    /**
     * 请求板块信息
     */
    private void loadMoreList(){
      /*  mStockClient.requestStockPlateInfo(new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {

                if (null != object) {
                    Object objectArray[] = (Object[]) object;
                    mIndustryLedUpArray.clear();
                    mIndustryLedUpArray.addAll((ArrayList<IndustryInfo>) objectArray[0]);
                    mIndustryLedDownArray.clear();
                    mIndustryLedDownArray.addAll((ArrayList<IndustryInfo>) objectArray[1]);

                }
                mStockPlateAdapter.notifyDataSetChanged();

                mProgressBar.setVisibility(View.GONE);
                onLoaded();
            }

            @Override
            public void onError(int errorCode, String errorString) {
                mProgressBar.setVisibility(View.GONE);

                onLoaded();
            }
        });*/

        mStockClient.requestIndustryListStock(1, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                // TODO Auto-generated method stub
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        onLoaded();
                    }
                });
            }

            @Override
            public void onDataFinish(Object object) {

                mIndustryLedUpArray.clear();

                mIndustryLedUpArray.addAll((List<IndustryInfo>) object);


                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStockPlateAdapter.notifyDataSetChanged();
                        onLoaded();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
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
        mContext = getActivity();
        mLiveRecordingUtil = LiveRecordingUtil.getInstance();
        mStockClient = new StockClient(mActivity);

        initViews(mLayout);
        mProgressBar.setVisibility(View.VISIBLE);
        loadCache();
        initData();
    }
}
