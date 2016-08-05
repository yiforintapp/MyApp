package com.zlf.appmaster.userTab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.model.industry.IndustryItem;
import com.zlf.appmaster.model.stock.StockFavoriteItem;
import com.zlf.appmaster.stocktrade.StockTradeDetailActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Huang on 2015/4/30.
 */
public class StockFavoriteIndustryActivity extends Activity {
    public final static String INTENT_FLAG_DATA = "intent_flag_data";
    public final static String INTENT_FLAG_TITLE = "intent_flag_title";
    private Context mContext;
    private StockFavoriteIndustryAdapter mAdapter;

    private List<IndustryItem> mListData;
    private String mStockIds= "";       // 包含的股票组


    private XListView mListView;
    private ProgressBar mProgressBar;

    //private StockFavoriteTable mStockTable;
    private StockClient mStockClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_favorite_industry);
        mContext = this;

        mStockClient = new StockClient(mContext);

        initViews();
        initData();
        requestData(mStockIds);
    }

    private void initViews(){

        ((TextView)findViewById(R.id.title)).setText(getIntent().getStringExtra(INTENT_FLAG_TITLE));

        mListView = (XListView)findViewById(R.id.list_stock_favorite_industry);
        mProgressBar = (ProgressBar)findViewById(R.id.content_loading);

        mListData = (List<IndustryItem>)getIntent().getSerializableExtra(INTENT_FLAG_DATA);

        mAdapter = new StockFavoriteIndustryAdapter(mContext, mListData);
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
                    requestData(mStockIds);
                }
            }

            @Override
            public void onLoadMore() {

            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                StockFavoriteItem item = (StockFavoriteItem)mAdapter.getItem(position - 1);
                if (null != item){
                    Intent intent = new Intent(mContext, StockTradeDetailActivity.class);
                    intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKNAME, item.getStockName());
                    intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKCODE, item.getStockCode());
                    mContext.startActivity(intent);
                }
            }
        });

    }


    private void initData(){
        mProgressBar.setVisibility(View.VISIBLE);
        JSONObject cacheData =  StockJsonCache.loadFromFile(this, StockJsonCache.CACHEID_FAVORITE_STOCK_LIST);
        if (null != cacheData){
            try {
                HashMap<String, StockFavoriteItem> itemsCache =
                        StockFavoriteItem.resolveArrayFromJson(cacheData);
                // 刷新数据
                for (IndustryItem industryItem : mListData){
                    // 刷新数据
                    for (StockFavoriteItem item:industryItem.getSubStockIDs()){
                        item.copyValue(itemsCache.get(item.getKey()));
                    }
                }

                mAdapter.notifyDataSetChanged();

            }catch (JSONException e){

            }

        }

        for(IndustryItem item: mListData){
            List<StockFavoriteItem> stockFavoriteItems = item.getSubStockIDs();

            for (StockFavoriteItem stockFavorite: stockFavoriteItems){
                if (stockFavorite.getType() == StockFavoriteItem.TYPE_STOCK){
                    mStockIds += stockFavorite.getStockCode()+",";
                }

            }

        }

    }


    private void onLoaded() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
    }



    private void requestData(String stockIDs){

        if(TextUtils.isEmpty(stockIDs)){
            mProgressBar.setVisibility(View.GONE);
            return;
        }

        mStockClient.requestQuotationsByIds(stockIDs, "", StockJsonCache.CACHEID_FAVORITE_STOCK_LIST,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub

                        mProgressBar.setVisibility(View.GONE);
                        onLoaded();
                    }

                    @Override
                    public void onDataFinish(Object object) {
                        // TODO Auto-generated method stub
                        HashMap<String, StockFavoriteItem> itemsData = (HashMap<String, StockFavoriteItem>) object;

                        for (IndustryItem industryItem : mListData){
                            // 刷新数据
                            for (StockFavoriteItem item:industryItem.getSubStockIDs()){
                                item.copyValue(itemsData.get(item.getKey()));
                            }
                        }

                        mAdapter.notifyDataSetChanged();

                        mProgressBar.setVisibility(View.GONE);
                        onLoaded();
                    }
                });

    }


    public void onBack(View view){
        finish();
    }
}
