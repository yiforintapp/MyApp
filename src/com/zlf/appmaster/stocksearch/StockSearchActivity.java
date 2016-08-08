package com.zlf.appmaster.stocksearch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.client.SyncClient;
import com.zlf.appmaster.db.stock.StockFavoriteTable;
import com.zlf.appmaster.model.search.StockSearchItem;
import com.zlf.appmaster.model.stock.StockItem;
import com.zlf.appmaster.model.sync.SyncBaseBean;
import com.zlf.appmaster.model.sync.SyncOperator;
import com.zlf.appmaster.model.sync.SyncRequest;
import com.zlf.appmaster.stockIndex.StockIndexDetailActivity;
import com.zlf.appmaster.stocksearch.StockSearchAdapter.OnAddListener;
import com.zlf.appmaster.stocktrade.StockTradeDetailActivity;
import com.zlf.appmaster.utils.QLog;

public class StockSearchActivity extends BaseStockSearchActivity {

    private final static String TAG = "StockSearchActivity";

    public final static int INTENT_FLAG_JUMP_TO_STOCK_DETAIL = 1;       // 股票明细
    public final static int INTENT_FLAG_JUMP_TO_STOCK_DEAL = 2;         // 股票交易

    public final static String INTENT_FLAG_JUMP = "intent_flag_jump";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        setOnListDataChangeListener(new OnListDataChangeListener() {
            @Override
            public void onChange() {
                setStockAddListener(new OnStockAddListener());
                setSearchAdapterType(StockSearchAdapter.ADAPTER_TYPE_STOCK_FAVORITE);
            }
        });

        super.onCreate(savedInstanceState);

        // 同步股票信息
        SyncRequest syncRequest = new SyncRequest(this, SyncBaseBean.SYNC_KEY_STOCK_BASE_DATA);
        QLog.e("StockTable", "syncRequestStr===========>.:" + syncRequest);
        SyncClient.getInstance(this).requestSyncA(syncRequest, null);
    }


    @Override
    protected void initViews() {
        // TODO Auto-generated method stub
        super.initViews();


        setTitle(getResources().getString(R.string.add_stock_favorite));

        setOnStockItemClickListener(new OnSearchListItemClick());
    }


    private class OnStockAddListener implements OnAddListener {

        @Override
        public void onAdd(int position) {
            // TODO Auto-generated method stub
            final StockFavoriteTable stockFavoriteTable = new StockFavoriteTable(getApplicationContext());

            //if (!UserUtils.checkLogin(StockSearchActivity.this))
            //    return;

            StockSearchItem item = (StockSearchItem) mlistAdapter.getItem(position);
            if (null != item) {
                // 更新股票列表数据库中的标识
                stockFavoriteTable.addByLocal(item.getStockCode(), item.getType());    // 无时间返回，待修改

                onAddItemClick(position);
            }

        }
    }


    @Override
    public void onBack(View view) {
        // TODO Auto-generated method stub
        super.onBack(view);
    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
    }


//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		// TODO Auto-generated method stub
//		if(resultCode == QConstants.RESULTCODE_UPDATE_STOCKFAVORITE){
////			if(null != mCursor){
////				mCursor.requery();
////			}
//            QLog.i(TAG, " 自选股改变" );
//		}
//		super.onActivityResult(requestCode, resultCode, data);
//	}

    /**
     * 点击股票事件回传相应值
     *
     * @author Deping Huang
     */
    private class OnSearchListItemClick implements OnStockItemClickListener {

        @Override
        public void onClick(int position, String stockCode, String stockName, int codeType) {
            // TODO Auto-generated method stub
            // 添加大家都在搜
            if (codeType == StockItem.CODE_TYPE_STOCK) {     // 股票类型才需要添加
                new StockClient(StockSearchActivity.this).uploadHotSearchStock(stockCode, null);

                int intent_flag = getIntent().getIntExtra(INTENT_FLAG_JUMP, INTENT_FLAG_JUMP_TO_STOCK_DETAIL);
                if (intent_flag == INTENT_FLAG_JUMP_TO_STOCK_DEAL) {
                    Intent intent = new Intent(StockSearchActivity.this, StockDealActivity.class);
                    intent.putExtra(StockDealActivity.INTENT_FLAG_STOCKCODE, stockCode);
                    startActivity(intent);
                } else {
//                    // 跳转
                    Intent intent = new Intent(StockSearchActivity.this, StockTradeDetailActivity.class);
                    intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKNAME, stockName);
                    intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKCODE, stockCode);
                    intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_SEARCH_INDEX, position);
                    startActivityForResult(intent, 0);
                }
            } else if (codeType == StockItem.CODE_TYPE_INDEX) {
                Intent intent = new Intent(StockSearchActivity.this, StockIndexDetailActivity.class);
                intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_INDEXCODE, stockCode);
                intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_INDEXNAME, stockName);
                startActivity(intent);
            }


        }

    }

    @Override
    protected void onResume() {
        super.onResume();
//		MobclickAgent.onPageStart(TAG);
//		MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
//		MobclickAgent.onPageEnd(TAG); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
//		MobclickAgent.onPause(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        QLog.i(TAG, "StockSearch 同步自选股信息");
        //  同步自选股信息(只有增加的情况)
        SyncRequest syncRequest = new SyncRequest(this, SyncBaseBean.SYNC_KEY_FAVORITES);
        syncRequest.addOperator(SyncOperator.ID_FAVORITES)
                .addOperator(SyncOperator.ID_NEWS_SUBSCRIBE_ADD)    // 新闻订阅信息也一次更新（处理未登录用户的自选股订阅--添加）
                .commit();
        SyncClient.getInstance(this).requestSyncA(syncRequest, new OnRequestListener() {
            @Override
            public void onDataFinish(Object object) {
                // 有自选股相关信息更新,通知相关界面更新（主界面有自选股新闻）
//				StockSearchActivity.this.sendBroadcast(new Intent(MessageFragment.ACTION_RECENT_MSG_CHANGED));// 通知消息界面更新
            }

            @Override
            public void onError(int errorCode, String errorString) {

            }
        });
    }
}
