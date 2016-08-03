package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.bean.StockFinance;
import com.zlf.appmaster.bean.StockFinance.BalanceSheet;
import com.zlf.appmaster.bean.StockFinance.CashFlowSheet;
import com.zlf.appmaster.bean.StockFinance.ProfitSheet;

public class StockFinanceView extends LinearLayout {
    private Context mContext;
	private TextView mEPSView = null;							// 每股收益
	private TextView mOperatingRevenueView;				// 营业收入
	private TextView mInvestIncomeView;					// 投资净收益
	private TextView mNetprofitView;					// 净利润
	private TextView mStockPofitInfoSourceView;			// 利润表信息来源
	
	private TextView mNonCurrentAssetsView;				// 非流动资产
	private TextView mCurrentAssetsView;				// 流动字长
	private TextView mTotalAssetsView;					// 资产总计
	private TextView mCurrentLiabilityView;				// 流动负债
	private TextView mTotalLiabilityView;				// 负债总计
	private TextView mShareholderEquityView;			// 股东权益
	private TextView mLongTermLoanView;					// 长期借款
	private TextView mBalanceInfoSourceView;			// 资产负债表信息来源
	
	private TextView mNetOperateCashFlowView;			// 经营现金流净额
	private TextView mNetInvestCashFlowView;			// 投资现金流净额
	private TextView mNetFinanceCashFlowView;			// 筹资现金流净额
	private TextView mCashFlowInfoSourceView;			// 现金表信息来源
    public StockFinanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public StockFinanceView(Context context) {
        super(context);
        initViews(context);

    }
	private void initViews(Context context){

        LayoutInflater inflater = LayoutInflater.from(context);
        mContext = context;
        if (isInEditMode()) {
            return;
        }
        View view = inflater.inflate(R.layout.stock_finance_layout, this,true);

    	if(mEPSView == null){ // mEPSView加载过则认为其它也加载过
    		mStockPofitInfoSourceView = (TextView) view.findViewById(R.id.stock_profit_data_source);
    		mEPSView = (TextView) view.findViewById(R.id.earnings_per_share);
    		mOperatingRevenueView = (TextView) view.findViewById(R.id.operating_revenue);
    		mInvestIncomeView = (TextView) view.findViewById(R.id.invest_income);
    		mNetprofitView = (TextView) view.findViewById(R.id.net_profit);
    		
    		mBalanceInfoSourceView = (TextView) view.findViewById(R.id.stock_balance_data_source);
    		mNonCurrentAssetsView = (TextView) view.findViewById(R.id.non_current_assets);
    		mCurrentAssetsView = (TextView) view.findViewById(R.id.current_assets);
    		mTotalAssetsView = (TextView) view.findViewById(R.id.total_assets);
    		mCurrentLiabilityView = (TextView) view.findViewById(R.id.current_liability);
    		mTotalLiabilityView = (TextView) view.findViewById(R.id.total_liability);
    		mShareholderEquityView = (TextView) view.findViewById(R.id.shareholder_equity);
    		mLongTermLoanView = (TextView) view.findViewById(R.id.long_termloan);
    		
    		mCashFlowInfoSourceView = (TextView) view.findViewById(R.id.stock_cashflow_data_source);
    		mNetOperateCashFlowView = (TextView) view.findViewById(R.id.net_operate_cashflow);
    		mNetInvestCashFlowView = (TextView) view.findViewById(R.id.net_invest_cashflow);
    		mNetFinanceCashFlowView = (TextView) view.findViewById(R.id.net_finance_cashflow);
	
    	}
    }
    public void updateViews(StockFinance stockFinace){

    	ProfitSheet profitSheet = stockFinace.getProfitSheet();
    	if(profitSheet != null){
    		mStockPofitInfoSourceView.setText(profitSheet.getInfoSource());
    		mEPSView.setText(profitSheet.getEPS());
    		mOperatingRevenueView.setText(profitSheet.getOperatingRevenue());
    		mInvestIncomeView.setText(profitSheet.getInvestIncome());
    		mNetprofitView.setText(profitSheet.getNetprofit());	
    	}
    	
    	BalanceSheet balanceSheet = stockFinace.getBalanceSheet();
		if(balanceSheet != null){
			mBalanceInfoSourceView.setText(balanceSheet.getInfoSource());
			mNonCurrentAssetsView.setText(balanceSheet.getNonCurrentAssets());
			mCurrentAssetsView.setText(balanceSheet.getCurrentAssets());
			mTotalAssetsView.setText(balanceSheet.getTotalAssets());
			mCurrentLiabilityView.setText(balanceSheet.getCurrentLiability());
			mTotalLiabilityView.setText(balanceSheet.getTotalLiability());
			mShareholderEquityView.setText(balanceSheet.getShareholderEquity());
			mLongTermLoanView.setText(balanceSheet.getLongTermLoan());
		}
		
		CashFlowSheet cashFlowSheet = stockFinace.getCashFlowSheet();
		if(cashFlowSheet != null){
			mCashFlowInfoSourceView.setText(cashFlowSheet.getInfoSource());
			mNetOperateCashFlowView.setText(cashFlowSheet.getNetOperateCashFlow());
			mNetInvestCashFlowView.setText(cashFlowSheet.getNetInvestCashFlow());
			mNetFinanceCashFlowView.setText(cashFlowSheet.getNetFinanceCashFlow());
		}
		
    }
}
