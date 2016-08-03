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
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.utils.LiveRecordingUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huang on 2015/3/5.
 */
public class StockRiseInfoFragment extends BaseFragment {
    private static final String TAG = StockRiseInfoFragment.class.getSimpleName();
    private Context mContext;
    private View mLayout;
    private StockRiseInfoAdapter mStockRiseInfoAdapter;

    // 领涨/领跌榜
    private List<StockTradeInfo> mStockLedUpArray;
    private List<StockTradeInfo> mStockLedDownArray;

    private StockQuotationsClient mStockClient;

    private XListView mListView;
    private ProgressBar mProgressBar;
    private String mRoomName;
    private int mLiveStatus,mOnlineCnt;
    private boolean isLiveIntent;
    private LiveRecordingUtil mLiveRecordingUtil;
//    private LiverInfo mLiverInfo;

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // TODO Auto-generated method stub
//        if (mLayout != null) {//初始化过了 就不需要再创建
//            ViewGroup parent = (ViewGroup) mLayout.getParent();//清除自己 再返回 否则会重复设置了父控件
//            if (parent != null) {
//                parent.removeView(mLayout);
//            }
//            requestData();
//
//            return mLayout;
//        }
//        mLayout = inflater.inflate(R.layout.fragment_quotations_content, null);
//        mContext = getActivity();
//
//        mStockClient = StockQuotationsClient.getInstance(mContext);
//        mLiveRecordingUtil = LiveRecordingUtil.getInstance();
////        if (mLiveRecordingUtil.isLiveRecording() && mLiverInfo.getLiverRoomInfo()!= null) {
////            mRoomName = mLiverInfo.getLiverRoomInfo().getRoom_name();
////            mLiveStatus = mLiverInfo.getLiverRoomInfo().getLiving_status();
////            mOnlineCnt = mLiverInfo.getLiverRoomInfo().getOnlines_cnt();
////        }
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
        JSONObject jsonCache = StockJsonCache.loadFromFile(mActivity, StockJsonCache.CACHEID_QUOTATIONS_RISE_INFO);

        if (jsonCache != null){

            try {

                ArrayList<StockTradeInfo> stockLedUpArray = new ArrayList<StockTradeInfo>();
                ArrayList<StockTradeInfo> stockLedDownArray = new ArrayList<StockTradeInfo>();
                    JSONObject data = jsonCache.getJSONObject("data");
                    JSONObject jsonQuotationInfo = data.getJSONObject("quotations");
                    JSONArray jsonArray = data.getJSONArray("zhangfu");
                    int length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        String stockID = jsonArray.getString(i);
                        JSONObject jsonStockInfo = jsonQuotationInfo.getJSONObject(stockID);
                        StockTradeInfo stockTradeInfo = StockTradeInfo.resolveSummaryJsonObject(jsonStockInfo);
                        stockLedUpArray.add(stockTradeInfo);
                    }

                    jsonArray = data.getJSONArray("diefu");
                    length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        String stockID = jsonArray.getString(i);
                        JSONObject jsonStockInfo = jsonQuotationInfo.getJSONObject(stockID);
                        StockTradeInfo stockTradeInfo = StockTradeInfo.resolveSummaryJsonObject(jsonStockInfo);
                        stockLedDownArray.add(stockTradeInfo);
                    }

                    mStockLedUpArray.clear();
                    mStockLedUpArray.addAll(stockLedDownArray);

                    mStockLedDownArray.clear();
                    mStockLedDownArray.addAll(stockLedDownArray);

                    ret = true;

                    mStockRiseInfoAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }
    private void initViews(View view) {

        mListView = (XListView) findViewById(R.id.quotations_content_list);
        mProgressBar = (ProgressBar) findViewById(R.id.content_loading);

        mStockLedUpArray = new ArrayList<StockTradeInfo>();
        mStockLedDownArray = new ArrayList<StockTradeInfo>();

        mStockRiseInfoAdapter = new StockRiseInfoAdapter(mActivity, mStockLedUpArray, mStockLedDownArray);
        mListView.setAdapter(mStockRiseInfoAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                StockTradeInfo item = (StockTradeInfo) mStockRiseInfoAdapter.getItem(position - 1);
//                if (null != item) {
//                    Class targetClass;
//                    if(isLiveIntent == true){
//                        targetClass = LiveAnchorLectureActivity.class;
//                    }else{
//                        targetClass = StockTradeDetailActivity.class;
//                    }
//                    Intent intent = new Intent(mContext, targetClass);
//
//                    if(targetClass == LiveAnchorLectureActivity.class){
//                        mLiveRecordingUtil.setStockName(item.getName());
//                        mLiveRecordingUtil.setStockCode(item.getCode());
//                        mLiveRecordingUtil.setStock(true);
//                        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    }else{
//                        intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKNAME, item.getName());
//                        intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKCODE, item.getCode());
//                    }
//
//                    startActivityForResult(intent, 0);
//                }
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

    private void initData() {
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
    private void requestData() {
        mStockClient.requestStockRiseInfo(new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {

                if (null != object) {
                    Object objectArray[] = (Object[]) object;
                    mStockLedUpArray.clear();
                    mStockLedUpArray.addAll((ArrayList<StockTradeInfo>) objectArray[0]);
                    mStockLedDownArray.clear();
                    mStockLedDownArray.addAll((ArrayList<StockTradeInfo>) objectArray[1]);

                }
                mStockRiseInfoAdapter.notifyDataSetChanged();

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
        mLiveRecordingUtil = LiveRecordingUtil.getInstance();
//        if (mLiveRecordingUtil.isLiveRecording() && mLiverInfo.getLiverRoomInfo()!= null) {
//            mRoomName = mLiverInfo.getLiverRoomInfo().getRoom_name();
//            mLiveStatus = mLiverInfo.getLiverRoomInfo().getLiving_status();
//            mOnlineCnt = mLiverInfo.getLiverRoomInfo().getOnlines_cnt();
//        }

        initViews(mLayout);

        loadCache();
        initData();
    }
}
