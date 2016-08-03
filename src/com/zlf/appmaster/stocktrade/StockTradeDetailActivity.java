package com.zlf.appmaster.stocktrade;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.xlistview.XListView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.client.SyncClient;
import com.zlf.appmaster.db.stock.StockFavoriteTable;
import com.zlf.appmaster.db.stock.StockTradeTable;
import com.zlf.appmaster.home.BaseActivity;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.model.sync.SyncBaseBean;
import com.zlf.appmaster.model.sync.SyncOperator;
import com.zlf.appmaster.model.sync.SyncRequest;
import com.zlf.appmaster.stocksearch.StockSearchActivity;
import com.zlf.appmaster.ui.stock.StockBaseInfoView;
import com.zlf.appmaster.utils.DipPixelUtil;
import com.zlf.appmaster.utils.QConstants;
import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.utils.UrlConstants;
import com.zlf.appmaster.utils.Utils;


public class StockTradeDetailActivity extends BaseActivity {
    public static final String INTENT_FLAG_STOCKNAME = "intent_flag_stockname";
    public static final String INTENT_FLAG_STOCKCODE = "intent_flag_stockcode";
    public static final String INTENT_FLAG_SEARCH_INDEX = "intent_flag_search_index";   // 从搜索列表进来的索引
    public static final String INTENT_FLAG_IS_FAVORITE = "intent_flag_is_favorite";

    //private final int KLINE_TYPE_HANDICAP = 99; //盘口
    private final static int REQUEST_CODE_SEARCH = 11;

    private static final String TAG = "StockTradeDetailActivity";

    private Context mContext;

    /**
     * 标题区域
     */
    private View mActionBarView;
    private View mActivityTitleView;
    private TextView mActivityTitleTV;          // 界面标题
    private TextView mActivityTitleCommentTV;   // 界面标题注释
    private View mAHTitleView;              // 沪港通
    private String mTitleComment1;          // 注释1为静止时的注释
    private String mTitleComment2;          // 注释2为滚动时的注释


    private String mStockCode;
    private String mStockName;
    private StockClient mStockClient;
    //  private ProgressBar mProgressBar;

    /**
     * 标题动画效果相关,该值由UI直接给出
     */
    private int TITLE_TOP;
    private int TITLE_BOTTOM;



    private StockTradeInfo mStockTradeInfo;


    // 自选股相关 
    private boolean mIsFavoriteStock;
    private boolean mInitFavoriteStockFlag;	// 初始的自选股标记
    private StockFavoriteTable mStockFavoriteTable;
    private View mAddStockFavorite;
    private ImageView mFavoriteImageView;
    private TextView mFavoriteTextView;

    // 加入组合
    private View mAddCombination;

    // 缓存
    private StockBaseInfoView mCacheStockBaseInfoView;


    private StockTradeDetailAdapter mStockTradeDetailAdapter;
    // private PullToRefreshListView mPullRefreshView;
    private XListView mListView;

    private static final int MSG_LOAD_INIT = 1;

    public Handler mHandler = new Handler(){

        @Override
        public  void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case MSG_LOAD_INIT:
                    requestBaseData(false);
                    break;
            }
            super.handleMessage(msg);
        }

    };
    private Runnable mUpdateThread = new Runnable() {
        public void run () {
            requestBaseData(true);
            mHandler.postDelayed(this, UrlConstants.getRefreshCycle());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);setContentView(R.layout.activity_stocktrade_detail);


        mContext = this;

        mStockClient = new StockClient(this);
        mStockFavoriteTable = new StockFavoriteTable(this);

        initViews();

        loadCache();

        mHandler.sendEmptyMessageDelayed(MSG_LOAD_INIT, 50);

//        MobclickAgent.onEvent(this, UmengCustom.FUNNEL_TRADE_STOCK);
    }

    private void initViews(){

        TITLE_TOP = DipPixelUtil.dip2px(StockTradeDetailActivity.this, 12);
        TITLE_BOTTOM = TITLE_TOP + DipPixelUtil.sp2px(StockTradeDetailActivity.this,17);

        Intent intent =getIntent();
        mStockCode = intent.getStringExtra(INTENT_FLAG_STOCKCODE);
        mStockName = intent.getStringExtra(INTENT_FLAG_STOCKNAME);
        if (mStockName == null)
            mStockName = "";

        mActionBarView = findViewById(R.id.title_bar);

        //mProgressBar = (ProgressBar)findViewById(R.id.content_loading);
        mActivityTitleView = findViewById(R.id.stocktrade_title_view);
        mActivityTitleTV 		= (TextView)findViewById(R.id.stocktrade_detail_title);
        mActivityTitleCommentTV = (TextView)findViewById(R.id.stocktrade_detail_title_comment);
        mActivityTitleTV.setText(mStockName + " " + mStockCode);

        mAHTitleView = findViewById(R.id.stocktrade_detail_title_ah);


        // 添加自选股的按钮
        mAddStockFavorite = findViewById(R.id.btn_stock_favorite_add);

        mFavoriteImageView = (ImageView)findViewById(R.id.icon_stock_favorite);
        mFavoriteTextView = (TextView)findViewById(R.id.txt_stock_favorite);

        mInitFavoriteStockFlag = mStockFavoriteTable.isFavorite(mStockCode);
        mIsFavoriteStock = mInitFavoriteStockFlag;

        mAddStockFavorite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //QLog.i(TAG,"mAddBtn");

                mIsFavoriteStock = !mIsFavoriteStock;
                onStockFavoriteHandle(v);
            }
        });
        updateStockFavoriteBtn();

        mAddCombination = findViewById(R.id.btn_combination_add);
        mAddCombination.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

//                if (!UserUtils.checkLogin(view,StockTradeDetailActivity.this,StockTradeDetailActivity.this))
//                    return;
//
//                ContactsTableOld table = new ContactsTableOld(mContext);
//                if (table.hasCreateCombination()) {
//                    Intent intent = new Intent(StockTradeDetailActivity.this, CombinationAddInActivity.class);
//                    intent.putExtra(CombinationAddInActivity.INTENT_EXTRA_STOCK_CODE, mStockCode);
//                    intent.putExtra(CombinationAddInActivity.INTENT_EXTRA_STOCK_NAME, mStockName);
//                    startActivity(intent);
//                }else {
//                    Intent intent = new Intent(StockTradeDetailActivity.this, CombinationCreateActivity.class);
//                    startActivity(intent);
//                }
            }
        });

        mListView = (XListView)findViewById(R.id.stock_detail_list);

        mListView.setPullRefreshEnable(true);
        mListView.setPullLoadEnable(false);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                requestBaseData(false);
                requestKLineData();
            }

            @Override
            public void onLoadMore() {

            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                position = position -1;//   用的是xlistview

            }
        });

        // 滑动监听，做标题效果
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // QLog.i(TAG, "view:"+view)
                if (firstVisibleItem ==  1){     // 只处理第一行
                    int y = -view.getChildAt(0).getTop();

                    onScrollY(y, TITLE_TOP, TITLE_BOTTOM);
                }
                else if(firstVisibleItem == 0){         // 强制滑动
                    onScrollY(TITLE_TOP, TITLE_TOP, TITLE_BOTTOM);
                }
                else {
                    onScrollY(TITLE_BOTTOM, TITLE_TOP, TITLE_BOTTOM);
                }
                /*// 滑动标题
                if (firstVisibleItem < .size() && visibleItemCount > 0) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) titleText
                            .getLayoutParams();
                    View itemView = view.getChildAt(1);
                    int top = 0;
                   // if (list.get(firstVisibleItem + 1).isTitle()) {
                        top = itemView.getTop() - itemView.getHeight();
                  //  }

                    params.setMargins(0, top, 0, 0);
                    titleText.setLayoutParams(params);
                }*/


            }
        });


        // 先设置空view
        View emptyView = findViewById(R.id.empty_view);
        mCacheStockBaseInfoView = (StockBaseInfoView)emptyView.findViewById(R.id.cache_stock_base_info_view);
        mListView.setEmptyView(emptyView);

    }

    private void loadCache(){
        StockTradeTable stockTradeTable;
        stockTradeTable = new StockTradeTable(this);
        mStockTradeInfo = stockTradeTable.getItem(mStockCode);
        if (mStockTradeInfo != null){
            mCacheStockBaseInfoView.updateViews(mStockTradeInfo);
        }
        else {
            mStockTradeInfo = new StockTradeInfo();
            mStockTradeInfo.setCode(mStockCode);
            mStockTradeInfo.setName(mStockName);
        }


        mStockTradeDetailAdapter = new StockTradeDetailAdapter(mContext, mStockClient, mStockTradeInfo);
        mListView.setAdapter(mStockTradeDetailAdapter);
    }



    private void updateTitle(StockTradeInfo stockTradeInfo) {

        // --- 更新页action bar内容  ----- //

        if(stockTradeInfo.isStockSuspended()) {	// 停牌
            mTitleComment2 = stockTradeInfo.getCurPriceFormat() + "  " +getString(R.string.stock_trade_suspended);
        }
        else {

            mTitleComment2 = stockTradeInfo.getCurPriceFormat() + "  " +stockTradeInfo.getCurPriceComment();
        }

//        if (stockTradeInfo.isIsAH()){         // 沪港通不显示了 comment by DepingHuang Date:20150505
//            mAHTitleView.setVisibility(View.VISIBLE);
//        }
        // 根据当前的涨跌情况，改变标题的背景色
//        if (mStockTradeInfo.getRiseInfo() == -1){
//            mActionBarView.setBackgroundColor(getResources().getColor(R.color.stock_slumped));
//            mActivityTitleView.setBackgroundColor(getResources().getColor(R.color.stock_slumped));
//        }else {
//            mActionBarView.setBackgroundColor(getResources().getColor(R.color.stock_rise));
//            mActivityTitleView.setBackgroundColor(getResources().getColor(R.color.stock_rise));
//        }


        mTitleComment1 = "交易中" + stockTradeInfo.getDataTimeFormat();
        int marketStatus = stockTradeInfo.getMarketStatus();
        if(marketStatus != StockTradeInfo.MARKET_STATUS_NORMAL){
            mTitleComment1 = "休市中 " + stockTradeInfo.getDataTimeFormat();

            // 休市则终止自动刷新
            stopAutoRefresh();
        }
        mActivityTitleCommentTV.setText(mTitleComment1);

    }


    private void requestBaseData(final boolean autoRefresh){

        if (Utils.GetNetWorkStatus(this)) {
            //	mProgressBar.setVisibility(View.VISIBLE);
            mStockClient.requestStockInfo(mStockCode, new OnRequestListener() {

                @Override
                public void onError(int errorCode, String errorString) {
                    // TODO Auto-generated method stub
                    //mProgressBar.setVisibility(View.GONE);
                    QLog.i(TAG, "requestData  onError");
                    if (!autoRefresh) {
                        UrlConstants.showUrlErrorCode(StockTradeDetailActivity.this, errorCode);
                        stopPullRefresh();
                    }
                }

                @Override
                public void onDataFinish(Object object) {
                    // TODO Auto-generated method stub
                    //mProgressBar.setVisibility(View.GONE);

                    if (mStockTradeInfo != null) {
                        mStockTradeInfo.copy((StockTradeInfo) object);
                        mStockTradeDetailAdapter.notifyDataSetChanged();
                        updateTitle(mStockTradeInfo);
                    }

                    stopPullRefresh();
                }
            });

        }
        else{
            if (!autoRefresh) {
                Toast.makeText(this, getResources().getString(R.string.network_unconnected),
                        Toast.LENGTH_SHORT).show();
                stopPullRefresh();
            }
        }

    }

    private void requestKLineData(){
        mStockTradeDetailAdapter.refreshKLineDataView();
    }



    /**
     * 回传值
     */
    private void goBackResult(){
        if(mInitFavoriteStockFlag != mIsFavoriteStock){// 有改变则回传该信息
            QLog.i(TAG, "mInitFavoriteStockFlag != mIsFavoriteStock");
            Intent intent = new Intent();
            intent.putExtra(INTENT_FLAG_SEARCH_INDEX, getIntent().getIntExtra(INTENT_FLAG_SEARCH_INDEX, -1));
            intent.putExtra(INTENT_FLAG_IS_FAVORITE, mIsFavoriteStock);
            setResult(QConstants.RESULTCODE_UPDATE_STOCKFAVORITE, intent);

            //  同步自选股信息
            QLog.i(TAG, "StockDetail 同步自选股信息");
            SyncRequest syncRequest = new SyncRequest(this, SyncBaseBean.SYNC_KEY_FAVORITES);
            syncRequest.addOperator(SyncOperator.ID_FAVORITES)
                    .commit();
            SyncClient.getInstance(this).requestSyncA(syncRequest, new OnRequestListener() {
                @Override
                public void onDataFinish(Object object) {
                    // 有自选股相关信息更新,通知相关界面更新（主界面有自选股新闻）
//                    mContext.sendBroadcast(new Intent(MessageFragment.ACTION_RECENT_MSG_CHANGED));// 通知消息界面更新
                }

                @Override
                public void onError(int errorCode, String errorString) {

                }
            });
        }

    }


    public void onStockAgent(View view) {
//        Intent intent = new Intent(this, StockDealActivity.class);
//        intent.putExtra(StockDealActivity.INTENT_FLAG_STOCKCODE, mStockCode);
//        startActivity(intent);
//        MobclickAgent.onEvent(this, UmengCustom.FUNNEL_TRADE_DEAL);
    }

    public void onStockFavoriteHandle(View view) {

        if (mIsFavoriteStock){
            // 数据库中增加
            mStockFavoriteTable.addByLocal(mStockCode);
        }
        else {
            // 数据库中删除
            mStockFavoriteTable.deleteByLocal(mStockCode);
        }

        // 更新按钮状态
        updateStockFavoriteBtn();


//        MobclickAgent.onEvent(mContext, UmengCustom.CLICK_STOCK_ADD);
    }

    // 加入/删除自选按钮更新提示
    private void updateStockFavoriteBtn(){
        if(mIsFavoriteStock) {
            //Log.i(TAG, "该股票为自选股:"+mStockCode);
            mFavoriteImageView.setImageResource(R.drawable.icon_stock_favorite_delete);
            mFavoriteTextView.setText(R.string.stock_favorite_delete);

        }
        else {
            //Log.i(TAG, "该股票非自选股:"+mStockCode);
            mFavoriteImageView.setImageResource(R.drawable.icon_stock_favorite_add);
            mFavoriteTextView.setText(R.string.stock_favorite_add);
        }
    }


    public void onBack(View view) {
        goBackResult();
        finish();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        //startAutoRefresh();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        //stopAutoRefresh();
    }


    private void startAutoRefresh(){
        // 定时刷新
        mHandler.removeCallbacks(mUpdateThread);
        mHandler.postDelayed(mUpdateThread, UrlConstants.getRefreshCycle());
    }


    private void stopAutoRefresh() {
        mHandler.removeCallbacks(mUpdateThread);
    }




    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        goBackResult();
        super.onBackPressed();
    }



    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        // 清理该股票的缓存
        //StockJsonCache.deleteStockDir(this, mStockCode);

        if(mStockFavoriteTable != null)
            mStockFavoriteTable.close();

        super.onDestroy();
    }

    public void onSearch(View v){
        Intent intent = new Intent(mContext, StockSearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, REQUEST_CODE_SEARCH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SEARCH){
            QLog.i(TAG, "从搜索界面返回");
            boolean isFavoriteStock = mStockFavoriteTable.isFavorite(mStockCode);
            if (mIsFavoriteStock != isFavoriteStock){
                mIsFavoriteStock = isFavoriteStock;
                updateStockFavoriteBtn();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        MobclickAgent.onPageEnd(TAG); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
//        MobclickAgent.onPause(this);
    }

    private void stopPullRefresh(){
        //mPullRefreshView.onRefreshComplete();
        mListView.stopRefresh();
        mListView.stopLoadMore();
    }

    boolean bScrollUpChageTitle = false;
    boolean bScrollDownChageTitle = false;
    public void onScrollY(int scrollY, int top, int bottom) {
        // TODO Auto-generated method stub
        //	int parentTop = Math.max(scrollY, mExtraListTitle.getTop());
        //	mExtraListTitleFixed.layout(mExtraListTitle.getLeft(), parentTop, mExtraListTitle.getLeft()+mExtraListTitle.getWidth(), parentTop + mExtraListTitle.getHeight());

        if(scrollY >= bottom){
            if(!bScrollUpChageTitle){
                //Log.i(TAG, "指数");
                mActivityTitleCommentTV.setText(mTitleComment2);
                //mActivityTitleCommentTV.setTextColor(mStockPercentTV.getTextColors());
                mActivityTitleCommentTV.setAnimation(AnimationUtils.loadAnimation(this, R.anim.stock_title_text_down_in));
                bScrollUpChageTitle = true;
                bScrollDownChageTitle = false;
            }

        }
        else if(scrollY <= top){
            if(!bScrollDownChageTitle){
                //Log.i(TAG, "时间提示");
                mActivityTitleCommentTV.setText(mTitleComment1);
                //mActivityTitleCommentTV.setTextColor(getResources().getColor(R.color.stock_tradeinfo_title_comment));
                mActivityTitleCommentTV.setAnimation(AnimationUtils.loadAnimation(this, R.anim.stock_title_text_up_in));
                bScrollUpChageTitle = false;
                bScrollDownChageTitle = true;
            }
        }
    }

}

