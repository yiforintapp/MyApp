package com.zlf.appmaster.userTab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.client.SyncClient;
import com.zlf.appmaster.db.stock.StockFavoriteTable;
import com.zlf.appmaster.db.stock.StockTable;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.model.industry.IndustryItem;
import com.zlf.appmaster.model.stock.StockFavoriteItem;
import com.zlf.appmaster.model.sync.SyncBaseBean;
import com.zlf.appmaster.model.sync.SyncOperator;
import com.zlf.appmaster.model.sync.SyncRequest;
import com.zlf.appmaster.model.topic.TopicItem;
import com.zlf.appmaster.stocksearch.StockSearchActivity;
import com.zlf.appmaster.stocktrade.StockTradeDetailActivity;
import com.zlf.appmaster.utils.QConstants;
import com.zlf.appmaster.utils.UrlConstants;
import com.zlf.appmaster.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

//import com.iqiniu.qiniu.ui.MessageFragment;
//import com.iqiniu.qiniu.ui.search.StockSearchActivity;
//import com.iqiniu.qiniu.utils.UmengCustom;

/**
 * 查看本人的自选股，自选股信息是从数据中读取
 * Created by Deping Huang on 2014/12/1.
 */
public class StockFavoriteFragment extends BaseFragment {

    private String TAG = StockFavoriteFragment.class.getSimpleName();
    private StockClient mStockClient;
    private LayoutInflater mLayoutInflater;
    private XListView mList;
    private List<StockFavoriteItem> mStockFavoriteItemArray;
    private List<IndustryItem> mIndustryItemArray;
    private StockFavoriteListAdapter mStockFavoriteListAdapter;
    private ProgressBar mProgressBar;
    private Button mBtnAddFavorite;
    //private StockTradeTable mStockTradeTable;
    private StockFavoriteTable mStockFavoriteTable;
    private StockTable mStockTable;

    private boolean mInitStart;	//首次启动

    private List<TopicItem> mTopicItemList;

    private View mLayout;

    private static final int MSG_REFREASH_VIEW = 2;

    /**
     *  标示自选信息是否改变
     */
    private boolean mbFavoriteDataChanged = false;
    /**
     * 长按菜单会在两个fragment中的list都响应，用标记表示是不是当前界面
     */
    private boolean mIsCurrentContextMenu = false;

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {

                case MSG_REFREASH_VIEW:
                    updateViews();
                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }

    };
    private Runnable mUpdateThread = new Runnable() {
        public void run () {
            if (!mInitStart){    // 首次启动不进行刷新，由其它地方主动刷新
             //   Log.i(TAG,"非首次启动");

                requestData(true);
            }
            else {
                Log.i(TAG,"首次启动不进行刷新，由其它地方主动刷新");
            }
            mHandler.postDelayed(this, UrlConstants.getRefreshCycle());

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"mInitStart = true");
        mInitStart = true;  // 首次启动
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_stockfavorite;
    }

    @Override
    protected void onInitUI() {
        mStockFavoriteTable = new StockFavoriteTable(mActivity);
        mStockTable = new StockTable(mActivity);
        initViews(mLayout);
        initData(mActivity); // 初始化缓存数据

        mStockClient = new StockClient(mActivity);

        mProgressBar.setVisibility(View.VISIBLE);
        requestData(true);

//        MobclickAgent.onEvent(mActivity, UmengCustom.FUNNEL_TRADE_SELF);
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        if (mLayout != null) {//初始化过了 就不需要再创建
//            ViewGroup parent = (ViewGroup) mLayout.getParent();//清除自己 再返回 否则会重复设置了父控件
//            if (parent != null) {
//                parent.removeView(mLayout);
//            }
//            return mLayout;
//        }
//        mLayoutInflater = inflater;
//        mLayout = inflater.inflate(R.layout.fragment_stockfavorite, null);
//        mMainActivity = getActivity();
//        mActivity = mMainActivity;
//
//        // /mStockTradeTable = new StockTradeTable(mActivity);
//        mStockFavoriteTable = new StockFavoriteTable(mActivity);
//        mStockTable = new StockTable(mActivity);
//        initViews(mLayout);
//        initData(mActivity); // 初始化缓存数据
//
//        mStockClient = new StockClient(mActivity);
//
//        mProgressBar.setVisibility(View.VISIBLE);
//        requestData(true);
//
//        MobclickAgent.onEvent(mActivity, UmengCustom.FUNNEL_TRADE_SELF);
//
//        return mLayout;
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //  有改变的情况下同步自选股信息
        if (mbFavoriteDataChanged){
            SyncRequest syncRequest = new SyncRequest(mActivity, SyncBaseBean.SYNC_KEY_FAVORITES);
            syncRequest.addOperator(SyncOperator.ID_FAVORITES).commit();

            SyncClient.getInstance(mActivity).requestSyncA(syncRequest, new OnRequestListener() {
                @Override
                public void onDataFinish(Object object) {
                    // 有自选股相关信息更新,通知相关界面更新（主界面有自选股新闻）
//                    mActivity.sendBroadcast(new Intent(MessageFragment.ACTION_RECENT_MSG_CHANGED));// 通知消息界面更新
                }

                @Override
                public void onError(int errorCode, String errorString) {

                }
            });
            mbFavoriteDataChanged = false;
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mStockFavoriteTable != null)
            mStockFavoriteTable.close();
    }

    private void initData(Context context){
        JSONObject cacheData =  StockJsonCache.loadFromFile(context, StockJsonCache.CACHEID_FAVORITE_STOCK_LIST);
        if (null != cacheData){
            try {
                HashMap<String, StockFavoriteItem> itemsCache =
                        StockFavoriteItem.resolveArrayFromJson(cacheData);
                // 刷新数据
                for (StockFavoriteItem item:mStockFavoriteItemArray){
                    item.copyValue(itemsCache.get(item.getKey()));
                }

                mStockFavoriteListAdapter.notifyDataSetChanged();

            }catch (JSONException e){

            }
        }
    }


    private void initViews(View v){
        mProgressBar = (ProgressBar)findViewById(R.id.content_loading);

        initListView();
    }
    private void initListView() {
        mList = (XListView) findViewById(R.id.stock_favorite_listview);
        mStockFavoriteItemArray = mStockFavoriteTable.getFavoriteItems();
        mIndustryItemArray = mStockFavoriteTable.getStockFavoriteIndustryInfo();
        mTopicItemList = getTopicItems(mStockFavoriteItemArray);

        mStockFavoriteListAdapter = new StockFavoriteListAdapter(mActivity, mStockFavoriteItemArray, mIndustryItemArray, mTopicItemList,false);

        mBtnAddFavorite = (Button) findViewById(R.id.btn_stock_favorite_add);
        mBtnAddFavorite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(mActivity, StockSearchActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        mList.setAdapter(mStockFavoriteListAdapter);
        mList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

                //添加菜单项
                contextMenu.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.delete);
                contextMenu.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, R.string.favorite_top);
            }
        });
        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                StockFavoriteItem item = (StockFavoriteItem) mStockFavoriteListAdapter.getItem(position - 1);
                if (null != item) {     // 有数据则执行上下文菜单
                    mIsCurrentContextMenu = true;
                    return false;
                }
                return true;
            }
        });


        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {        // 股票搜索界面, 第0项为xlistviewheader
                    Intent intent = new Intent(mActivity, StockSearchActivity.class);
                    startActivityForResult(intent, 0);
                    return ;
                }

                StockFavoriteItem stockFavoriteItem = (StockFavoriteItem) mStockFavoriteListAdapter.getItem(position - 1);
                if (null != stockFavoriteItem) {
                    if (stockFavoriteItem.getType() == StockFavoriteItem.TYPE_STOCK) {
                        Intent intent = new Intent(mActivity, StockTradeDetailActivity.class);
                        intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKNAME, stockFavoriteItem.getStockName());
                        intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKCODE, stockFavoriteItem.getStockCode());
                        startActivityForResult(intent, 0);
                    } else {
//                        Intent intent = new Intent(mActivity, StockIndexDetailActivity.class);
//                        intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_INDEXCODE, stockFavoriteItem.getStockCode());
//                        intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_INDEXNAME, stockFavoriteItem.getStockName());
//                        startActivity(intent);
                    }
                }
            }
        });

        mList.setEmptyView( findViewById(R.id.empty_view));

        mList.setPullLoadEnable(false);
        mList.setPullRefreshEnable(true);
        mList.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                if (mProgressBar.getVisibility() == View.VISIBLE){  // 主进程正在更新
                    onLoaded();
                }
                else{
                    requestData(false);
                }
            }

            @Override
            public void onLoadMore() {

            }
        });

    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mIsCurrentContextMenu){
            int listPosition = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
            switch (item.getItemId()){
                case Menu.FIRST:        // 删除
                    requestDeleteStockFavorite(listPosition);
                    break;
                case Menu.FIRST + 1:    // 置顶
                    requestTopStockFavorite(listPosition);
                    break;
            }
            mIsCurrentContextMenu = false;
        }


        return super.onContextItemSelected(item);
    }

    private synchronized void requestData(final boolean autoRefresh ) {

        if (mInitStart && !Utils.GetNetWorkStatus(mActivity)) {	// 首次启动又无网络时提示此信息，第二次自动刷新时不提示
            Toast.makeText(mActivity, getResources().getString(R.string.network_unconnected),
                    Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
            mInitStart = false;
            return;
        }

        mInitStart = false;

        loadStockList(autoRefresh);
    }

    private synchronized void loadStockList(final boolean autoRefresh) {
        String stockIDs = "";
        String indexIDs = "";
        final List<StockFavoriteItem> stockFavoriteItems = mStockFavoriteTable.getFavoriteItems();
        for(StockFavoriteItem stockFavorite: stockFavoriteItems){
            if (stockFavorite.getType() == StockFavoriteItem.TYPE_STOCK){
                stockIDs += stockFavorite.getStockCode()+",";
            }
            else if(stockFavorite.getType() == StockFavoriteItem.TYPE_INDEX) {
                indexIDs += stockFavorite.getStockCode()+",";
            }

        }
        final List<IndustryItem> industryItems = mStockFavoriteTable.getStockFavoriteIndustryInfo();

        if(TextUtils.isEmpty(stockIDs) && TextUtils.isEmpty(indexIDs)){
            onLoaded();
            mProgressBar.setVisibility(View.GONE);
            mStockFavoriteItemArray.clear();
            mStockFavoriteListAdapter.notifyDataSetChanged();
            return;
        }

        mStockClient.requestQuotationsByIds(stockIDs, indexIDs, StockJsonCache.CACHEID_FAVORITE_STOCK_LIST,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
//				if(!autoRefresh){
//					UrlConstants.showUrlErrorCode(mMainActivity, errorCode);
//				}
                        mProgressBar.setVisibility(View.GONE);
                        onLoaded();
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub
                        HashMap<String, StockFavoriteItem> itemsData = (HashMap<String, StockFavoriteItem>) object;

                        // 刷新数据
                        for (StockFavoriteItem item : stockFavoriteItems) {
                            item.copyValue(itemsData.get(item.getKey()));
                        }

                        mStockFavoriteItemArray.clear();
                        mStockFavoriteItemArray.addAll(stockFavoriteItems);

                        mTopicItemList.clear();
                        mTopicItemList.addAll(getTopicItems(mStockFavoriteItemArray));

                        mIndustryItemArray.clear();
                        mIndustryItemArray.addAll(industryItems);

                        //mHandler.sendEmptyMessage(MSG_REFREASH_VIEW);
                        mStockFavoriteListAdapter.notifyDataSetChanged();

                        mProgressBar.setVisibility(View.GONE);
                        onLoaded();
                    }
                });


    }
    private void onLoaded() {
        mList.stopRefresh();
        mList.stopLoadMore();
    }

    private void updateViews(){
        mStockFavoriteListAdapter.notifyDataSetChanged();
    }

    // 删除自选
    private void requestDeleteStockFavorite(final int position) {

        mProgressBar.setVisibility(View.GONE);

        StockFavoriteItem item = (StockFavoriteItem)mStockFavoriteListAdapter.getItem(position - 1);

        if (null != item) {    // 更新数据库中的状态
            mStockFavoriteTable.deleteByLocal(item.getStockCode(), item.getType());


            mStockFavoriteItemArray.remove(position - 3);

            // 刷新题材图
            mTopicItemList.clear();
            mTopicItemList.addAll(getTopicItems(mStockFavoriteItemArray));
            // 刷新行业图
            mIndustryItemArray.clear();
            mIndustryItemArray.addAll(mStockFavoriteTable.getStockFavoriteIndustryInfo());

            mStockFavoriteListAdapter.notifyDataSetChanged();

            mbFavoriteDataChanged =  true;
        }
    }

    // 置顶
    private void requestTopStockFavorite(final int position){
        StockFavoriteItem item = (StockFavoriteItem)mStockFavoriteListAdapter.getItem(position - 1);
        if (null != item) {    // 更新数据库中的状态
            int sortCode = mStockFavoriteTable.addByLocal(item.getStockCode(), item.getType());
            item.setSortCode(sortCode);
            Collections.sort((LinkedList) mStockFavoriteItemArray);
            mStockFavoriteListAdapter.notifyDataSetChanged();

            mbFavoriteDataChanged =  true;
        }
    }

    private void startAutoRefresh(){
        // 定时刷新
        //mProgressBar.setVisibility(View.VISIBLE); // 暂时屏蔽
        mHandler.removeCallbacks(mUpdateThread);
        mHandler.postDelayed(mUpdateThread, 50);
    }


    private void stopAutoRefresh() {
        mHandler.removeCallbacks(mUpdateThread);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        Log.i(TAG, "requestCode:" + requestCode);
        if(resultCode == QConstants.RESULTCODE_UPDATE_STOCKFAVORITE){
            requestData(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onResume() {
    super.onResume();
    startAutoRefresh();
//    MobclickAgent.onPageStart(TAG);
//    MobclickAgent.onResume(mActivity);
}

    @Override
    public void onPause() {
        super.onPause();
        stopAutoRefresh();
//        MobclickAgent.onPageEnd(TAG); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
//        MobclickAgent.onPause(mActivity);
    }

    /**
     * 组装Topic对应的股票
     * @param stockItems
     * @return
     */
    public List<TopicItem> getTopicItems(List<StockFavoriteItem> stockItems){
        HashMap<String, TopicItem> mapTopic = new HashMap<String, TopicItem>();  // 用topicID 作为键，

        int len = stockItems.size();
        for (int i = 0; i < len; i ++) {
            StockFavoriteItem stockItem = stockItems.get(i);
            if (stockItem.getType() != StockFavoriteItem.TYPE_STOCK)
                continue;
            List<TopicItem> items = mStockTable.getStockTopic(stockItem.getStockCode());// 查找每个股票所属的题材
            if (null != items){
                for (int j = 0; j < items.size(); j ++){
                    TopicItem perItem = items.get(j);
                    if (mapTopic.containsKey(perItem.getID())){        // 已有该key，去map中的topicItem
                        List<StockFavoriteItem> stockFavoriteItems = mapTopic.get(perItem.getID()).getSubStockIDs();
                        stockFavoriteItems.add(stockItem);

                    }
                    else{
                        mapTopic.put(perItem.getID(), perItem);

                        List<StockFavoriteItem> stockFavoriteItems = new ArrayList<StockFavoriteItem>();
                        stockFavoriteItems.add(stockItem);
                        perItem.setSubStockIDs(stockFavoriteItems);
                    }
                }
            }
        }

        List<TopicItem> topicItemsList = new LinkedList<TopicItem>(mapTopic.values());
        Collections.sort(topicItemsList);
        return topicItemsList;
    }


}
