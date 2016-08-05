package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.db.stock.StockTable;
import com.zlf.appmaster.model.stock.StockTradeInfo;

/**
 * Created by Deping Huang on 2014/12/27.
 */
public class StockBaseInfoView extends LinearLayout {

    private Context mContext;


    private StockTextView mStockPriceTV;
    private StockTextView mStockPercentTV;
    private StockTextView mStockPointTV;
    private TextView mStockTodayPriceTV;
    private TextView mStockYesterdayPriceTV;
    private TextView mStockSuspendedPrompt;     // 停牌标识
    private TextView mStockVolumeTV;			// 成交量

    private TextView mStockMaxPriceTV;
    private TextView mStockMinPriceTV;
    private TextView mStockAllPriceTV;
    private TextView mStockPreRatioTV;            // 市盈率
    private TextView mStockBookValueTV;           // 市净率
    private TextView mStockMaxPrice52WeeksTV;
    private TextView mStockMinPrice52WeeksTV;
    private TextView mStockEPSTV;               // 每股收益
    private TextView mShareRepurchaseTV;        // 每股净资产

    // 题材标签
    private StockDetailTopicTagLayout mTopicTagLayout;

    // 控制股票信息的收缩/展开
    private boolean isStockInfoViewExpand = false;
    private ImageView mStockInfoExpandView;
    private View mStockMoreInfoLayout;
    private View mStockBaseInfoLayout;  // 点击这个区域也要控制收缩/展开

    private StockTable mStockTable;


    public StockBaseInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context, "","");
    }

    /**
     * 传入stockCode和stockName 快速初始化一些东西
     * @param context
     * @param stockCode
     * @param stockName
     */
    public StockBaseInfoView(Context context, String stockCode, String stockName) {
        super(context);
        initViews(context, stockCode, stockName);

    }


    private void initViews(Context context, String stockCode, String stockName){
        LayoutInflater inflater = LayoutInflater.from(context);
        mContext = context;
        if (isInEditMode()) {
            return;
        }
        View view = inflater.inflate(R.layout.stocktrade_detail_baseinfo, this,true);


        mStockPriceTV = (StockTextView)view.findViewById(R.id.stock_price);
        mStockPercentTV 	   = (StockTextView)view.findViewById(R.id.stock_percent);
        mStockPointTV = (StockTextView)view.findViewById(R.id.stock_point);
        mStockSuspendedPrompt = (TextView)view.findViewById(R.id.stock_trade_suspended);
        mStockTodayPriceTV 	   = (TextView)view.findViewById(R.id.today_start_price);
        mStockYesterdayPriceTV = (TextView)view.findViewById(R.id.yesterday_start_price);
        mStockVolumeTV = (TextView)view.findViewById(R.id.volume);

        mStockMaxPriceTV 	   = (TextView)view.findViewById(R.id.max_price);
        mStockMinPriceTV 	   = (TextView)view.findViewById(R.id.min_price);
        mStockAllPriceTV 	   = (TextView)view.findViewById(R.id.all_price);
        mStockPreRatioTV 	   = (TextView)view.findViewById(R.id.pre_ratio);
        mStockBookValueTV 	   = (TextView)view.findViewById(R.id.book_value);
        mStockMaxPrice52WeeksTV = (TextView)view.findViewById(R.id.max_price_52_weeks);
        mStockMinPrice52WeeksTV = (TextView)view.findViewById(R.id.min_price_52_weeks);
        mStockEPSTV 	       = (TextView)view.findViewById(R.id.EPS);
        mShareRepurchaseTV 	   = (TextView)view.findViewById(R.id.share_repurchase);

        mTopicTagLayout = (StockDetailTopicTagLayout)view.findViewById(R.id.topic_tag_layout);

        mStockInfoExpandView = (ImageView)view.findViewById(R.id.expanded_stock_info);
        mStockMoreInfoLayout = view.findViewById(R.id.stock_more_info_layout);
        mStockBaseInfoLayout = view.findViewById(R.id.stock_base_info_layout);

        mStockTable = new StockTable(mContext);
    }

    public void updateViews(final StockTradeInfo stockTradeInfo){
        if (null != stockTradeInfo){
            if(!stockTradeInfo.isStockSuspended()) {
                mStockPriceTV.setRiseInfo(stockTradeInfo.getRiseInfo());
                mStockPercentTV.setRiseInfo(stockTradeInfo.getRiseInfo());
                mStockPointTV.setRiseInfo(stockTradeInfo.getRiseInfo());
                mStockSuspendedPrompt.setVisibility(View.GONE);
                mStockPercentTV.setVisibility(View.VISIBLE);
            }
            else {// 停牌
                mStockSuspendedPrompt.setVisibility(View.VISIBLE);
                mStockPercentTV.setVisibility(View.GONE);
                mStockPointTV.setVisibility(View.GONE);
                mStockPriceTV.setVisibility(View.GONE);
            }
            setStockInfoExpandView(stockTradeInfo);
            //mStockIcon.setImage(stockTradeInfo.getCode());
            //mStockNameTV.setText(stockTradeInfo.getName());

            mStockPriceTV.setText(stockTradeInfo.getCurPriceFormat());
            mStockPercentTV.setText(stockTradeInfo.getCurPercentFormat());


            mStockTodayPriceTV.setText(stockTradeInfo.getTodayPriceFormat());
            mStockYesterdayPriceTV.setText(stockTradeInfo.getYestodayPriceFormat());
            mStockPointTV.setText(stockTradeInfo.getCurPointFormat());
            mStockMaxPriceTV.setText(stockTradeInfo.getHighestPriceFormat());
            mStockMinPriceTV.setText(stockTradeInfo.getLowestPriceFormat());
            mStockVolumeTV.setText(stockTradeInfo.getTradeFormat());    // getTradeCountFormat
            mStockAllPriceTV.setText(stockTradeInfo.getAllTradePriceFormat());

            mStockPreRatioTV.setText(stockTradeInfo.getPreRatioFormat());
            mStockBookValueTV.setText(stockTradeInfo.getBookValueFormat());
            mStockMaxPrice52WeeksTV.setText(stockTradeInfo.get52WeeksMaxPriceFormat());
            mStockMinPrice52WeeksTV.setText(stockTradeInfo.get52WeeksMinPriceFormat());
            mStockEPSTV.setText(stockTradeInfo.getEPSFormat());
            mShareRepurchaseTV.setText(stockTradeInfo.getShareRepurchaseFormat());

            mStockInfoExpandView.setOnClickListener(new OnExpandViewClickListener(stockTradeInfo));
            mStockBaseInfoLayout.setOnClickListener(new OnExpandViewClickListener(stockTradeInfo));



            View btnDimension = findViewById(R.id.go_dimensionView);
            btnDimension.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent(mContext,StockDimensionDetailActivity.class);
//                    intent.putExtra(StockDimensionDetailActivity.INTENT_EXTRA_STOCK_NAME,stockTradeInfo.getName());
//                    intent.putExtra(StockDimensionDetailActivity.INTENT_EXTRA_STOCK_CODE,stockTradeInfo.getCode());
//                    mContext.startActivity(intent);
                }
            });

            mTopicTagLayout.updateViews(mStockTable.getStockTopic(stockTradeInfo.getCode()));
        }
    }

    /**
     * 根据涨跌展开与否设置图标
     */
    private void setStockInfoExpandView(StockTradeInfo stockTradeInfo){

        if (stockTradeInfo != null){
            if (isStockInfoViewExpand){
                mStockMoreInfoLayout.setVisibility(View.VISIBLE);

                mStockInfoExpandView.setImageResource(R.drawable.icon_stock_detail_base_info_expand);

            }
            else{
                mStockMoreInfoLayout.setVisibility(View.GONE);

                mStockInfoExpandView.setImageResource(R.drawable.icon_stock_detail_base_info_default);
            }
        }

    }

    public int getTitleTop(){
        return mStockPriceTV.getTop();
    }

    public int getTitleBottom(){
        return mStockPriceTV.getBottom();
    }

    private class OnExpandViewClickListener implements OnClickListener{
        private StockTradeInfo mStockTradeInfo;
        public OnExpandViewClickListener(StockTradeInfo stockTradeInfo){
            mStockTradeInfo = stockTradeInfo;
        }
        @Override
        public void onClick(View view) {
            isStockInfoViewExpand = !isStockInfoViewExpand;
            setStockInfoExpandView(mStockTradeInfo);
        }
    }

}
