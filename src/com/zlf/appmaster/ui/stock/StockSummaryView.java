package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.bean.StockShares;
import com.zlf.appmaster.bean.StockSummary;

import java.util.List;

public class StockSummaryView extends LinearLayout {
    private Context mContext;
    private TextView mCompanyView = null; // 公司名称
    private TextView mMarketStartDateView = null; // 上市日期
    private TextView mMarketStartPriceView = null; // 发行价格
    private TextView mMarketStartNumView = null; // 发行数量
    private TextView mStockArea = null; // 所属地区
    private TextView mStockIndustry = null; // 所属行业
    private TextView mStockMainBussiness = null; // 主营业务
    // 股东信息
    private TextView mTotalShareView = null; // 总股本
    private TextView mCircleShareView = null; // 流通A股
    private TextView mTopHolderDataSourceView = null; // 十大股东信息来源
    private TextView mFundHolderDataSourceView = null; // 基金持股信息来源

    // 十大流通股东
    private LinearLayout mTopHolderList = null;

    public StockSummaryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }
    public StockSummaryView(Context context) {
        super(context);
        initViews(context);

    }
	private void initViews(Context context){

        LayoutInflater inflater = LayoutInflater.from(context);
        mContext = context;
        if (isInEditMode()) {
            return;
        }
        View view = inflater.inflate(R.layout.stock_summary_layout, this,true);

        mCompanyView = (TextView) view.findViewById(R.id.stock_company_name);
        mMarketStartDateView = (TextView) view.findViewById(R.id.stock_market_start_date);
        mMarketStartPriceView = (TextView) view.findViewById(R.id.stock_market_start_price);
        mMarketStartNumView = (TextView) view.findViewById(R.id.stock_market_start_num);
        mStockArea = (TextView) view.findViewById(R.id.stock_area);
        mStockIndustry = (TextView) view.findViewById(R.id.stock_industry);
        mStockMainBussiness = (TextView) view.findViewById(R.id.stock_main_business);

        mTotalShareView = (TextView) view.findViewById(R.id.stock_total_share);
        mCircleShareView = (TextView) view.findViewById(R.id.stock_circulation_share);
        mTopHolderDataSourceView = (TextView) view.findViewById(R.id.stock_top_holders_data_source);
        mFundHolderDataSourceView = (TextView) view.findViewById(R.id.stock_fund_holders_data_source);

        mTopHolderList = (LinearLayout)view.findViewById(R.id.stock_top_ten_shareholders_list);
    }

    public void updateViews(StockSummary stockSummary){

        //  mStockSummaryLayout.setVisibility(View.VISIBLE);

        mCompanyView.setText(stockSummary.getCompanyName());
        mMarketStartDateView.setText(stockSummary.getMarketStartDateFormat());
        mMarketStartPriceView.setText(stockSummary.getMarketStartPriceFormat());
        mMarketStartNumView.setText(stockSummary.getMarketStartNumFormat());
        mStockArea.setText(stockSummary.getArea());
        mStockIndustry.setText(stockSummary.getIndustry());
        mStockMainBussiness.setText(stockSummary.getMainBussiness());

        // 刷新股东信息
        StockShares stockShares = stockSummary.getStockShares();
        if(stockShares != null){
            mTotalShareView.setText(stockShares.getTotalNumFormat());
            mCircleShareView.setText(stockShares.getCircleNumFormat());
            mTopHolderDataSourceView.setText(stockShares.getTopHolderDataSource());
            List<StockShares.StockShareHolder> topHolderArray = stockShares.getTopHolderArray();
            if(topHolderArray != null){
                //mTopHolderListView.setAdapter(new StockShareHolderListAdapter(mContext, stockShares.getTopHolderArray()));
                mTopHolderList.removeAllViews();
                for (int i = 0; i < topHolderArray.size(); i++){
                    TopHolderViewItem itemView = new TopHolderViewItem(mContext);
                    itemView.setData(topHolderArray.get(i));
                    mTopHolderList.addView(itemView);
                }
            }

        }

    }


    /**
     * 新闻项
     */
    class TopHolderViewItem extends LinearLayout {
        private TextView name, percent, change;
        private Context mContext;

        public TopHolderViewItem(Context context, AttributeSet attrs) {
            super(context, attrs);
            initViews(context);
        }
        public TopHolderViewItem(Context context) {
            super(context);
            initViews(context);

        }
        private void initViews(Context context){

            LayoutInflater inflater = LayoutInflater.from(context);
            if (isInEditMode()) {
                return;
            }
            mContext = context;

            View view = inflater.inflate(R.layout.list_item_stock_holders, this,true);

            name = (TextView)view.findViewById(R.id.stock_holder_name);
            percent = (TextView)view.findViewById(R.id.stock_holder_percent);
            change = (TextView)view.findViewById(R.id.stock_holder_change);
        }

        public void setData(StockShares.StockShareHolder data){
            name.setText(data.getName());
            percent.setText(data.getProportionFormat());
            change.setText(data.getChangeFormat());
        }


    }

}
