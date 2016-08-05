package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.stock.StockIndex;


/**
 * Created by Deping Huang on 2014/12/27.
 */
public class IndexBaseInfoView extends LinearLayout {

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
    //private TextView mStockTurnVolumeTV;		// 成交额

    private TextView mUpCountTV;			// 涨家数
    private TextView mDeuceCountTV;			// 平家数
    private TextView mDownCountTV;			// 跌家数




    public IndexBaseInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    /**
     * 传入stockCode和stockName 快速初始化一些东西
     * @param context
     */
    public IndexBaseInfoView(Context context) {
        super(context);
        initViews(context);

    }


    private void initViews(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        mContext = context;
        if (isInEditMode()) {
            return;
        }
        View view = inflater.inflate(R.layout.stockindex_trade_detail_baseinfo, this,true);


        mStockPriceTV = (StockTextView)view.findViewById(R.id.stock_price);
        mStockPercentTV 	   = (StockTextView)view.findViewById(R.id.stock_percent);
        mStockPointTV = (StockTextView)view.findViewById(R.id.stock_point);
        mStockSuspendedPrompt = (TextView)view.findViewById(R.id.stock_trade_suspended);
        mStockTodayPriceTV 	   = (TextView)view.findViewById(R.id.today_start_price);
        mStockYesterdayPriceTV = (TextView)view.findViewById(R.id.yesterday_start_price);
        mStockVolumeTV = (TextView)view.findViewById(R.id.volume);

        mStockMaxPriceTV 	   = (TextView)view.findViewById(R.id.max_price);
        mStockMinPriceTV 	   = (TextView)view.findViewById(R.id.min_price);
        mUpCountTV	   = (TextView)findViewById(R.id.up_count);
        mDeuceCountTV  = (TextView)findViewById(R.id.deuce_count);
        mDownCountTV   = (TextView)findViewById(R.id.down_count);


    }

    public void updateViews(final StockIndex stockIndex){
        if(null == stockIndex) return;

        mStockPriceTV.setRiseInfo(stockIndex.getRiseInfo());
        mStockPriceTV.setText(stockIndex.getCurPriceFormat());
        mStockPointTV.setRiseInfo(stockIndex.getRiseInfo());
        mStockPointTV.setText(stockIndex.getCurPointFormat());
        mStockPercentTV.setRiseInfo(stockIndex.getRiseInfo());
        mStockPercentTV.setText(stockIndex.getCurPercentFormat());


        mStockTodayPriceTV.setText(stockIndex.getTodayPriceFormat());
        mStockYesterdayPriceTV.setText(stockIndex.getYestodayPriceFormat());
        mStockMaxPriceTV.setText(stockIndex.getHighestPriceFormat());
        mStockMinPriceTV.setText(stockIndex.getLowestPriceFormat());
        mStockVolumeTV.setText(stockIndex.getTradePriceFormat());

        mUpCountTV.setText(stockIndex.getUpCountFormat());
        mDeuceCountTV.setText(stockIndex.getDeuceCountFormat());
        mDownCountTV.setText(stockIndex.getDownCountFormat());
    }



    public int getTitleTop(){
        return mStockPriceTV.getTop();
    }

    public int getTitleBottom(){
        return mStockPriceTV.getBottom();
    }

}
