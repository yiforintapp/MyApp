package com.zlf.appmaster.bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class StockFinance {
	
	private final static String DEFAULT_DATA = "--";
	
	private ProfitSheet mProfitSheet;
	private BalanceSheet mBalanceSheet;
	private CashFlowSheet mCashFlowSheet;
	
	
	public ProfitSheet getProfitSheet() {
		return mProfitSheet;
	}

	public void setProfitSheet(ProfitSheet mProfitSheet) {
		this.mProfitSheet = mProfitSheet;
	}

	public BalanceSheet getBalanceSheet() {
		return mBalanceSheet;
	}

	public void setBalanceSheet(BalanceSheet mBalanceSheet) {
		this.mBalanceSheet = mBalanceSheet;
	}

	public CashFlowSheet getCashFlowSheet() {
		return mCashFlowSheet;
	}

	public void setCashFlowSheet(CashFlowSheet mCashFlowSheet) {
		this.mCashFlowSheet = mCashFlowSheet;
	}

	public static StockFinance resolveJSONObject(JSONObject response) throws JSONException {
		JSONObject data = response.getJSONObject("data");
		StockFinance stockFinance = new StockFinance();
		stockFinance.setProfitSheet(ProfitSheet.resolveJSONObject(data.getJSONObject("profit")));
		stockFinance.setBalanceSheet(BalanceSheet.resolveJSONObject(data.getJSONObject("propertyOut")));
		stockFinance.setCashFlowSheet(CashFlowSheet.resolveJSONObject(data.getJSONObject("cashFlow")));
		return stockFinance;
	}
	
	/**
	 * 利润表
	 */
	public static class ProfitSheet{
		private float mEPS;							// 每股收益
		private float mOperatingRevenue;			// 营业收入
		private float mInvestIncome;				// 投资净收益
		private float mNetprofit;					// 净利润
		
		private String mInfoSource;					// 信息来源

		public String getEPS() {
			if(mEPS == 0)
				return DEFAULT_DATA;
			
			return new DecimalFormat("0.00").format(mEPS);
		}

		public void setEPS(float mEPS) {
			this.mEPS = mEPS;
		}

		public String getOperatingRevenue() {
			return FormatFinanceStr(mOperatingRevenue);
		}

		public void setOperatingRevenue(float mOperatingRevenue) {
			this.mOperatingRevenue = mOperatingRevenue;
		}

		public String getInvestIncome() {
			return FormatFinanceStr(mInvestIncome);
		}

		public void setInvestIncome(float mInvestIncome) {
			this.mInvestIncome = mInvestIncome;
		}

		public String getNetprofit() {
			return FormatFinanceStr(mNetprofit);
		}

		public void setNetprofit(float mNetprofit) {
			this.mNetprofit = mNetprofit;
		}

		public String getInfoSource() {
			return mInfoSource;
		}

		public void setInfoSource(String mInfoSource) {
			this.mInfoSource = mInfoSource;
		}
		
		
		// 解析JSON
		public static ProfitSheet resolveJSONObject(JSONObject profit) throws JSONException {
			ProfitSheet profitSheet = new ProfitSheet();
			profitSheet.setInfoSource(profit.getString("infosource"));
			
			JSONObject content = profit.getJSONObject("content");
			profitSheet.setEPS((float)content.getDouble("basiceps"));
			profitSheet.setInvestIncome((float)content.getDouble("investincome"));
			profitSheet.setNetprofit((float)content.getDouble("netprofit"));
			profitSheet.setOperatingRevenue((float)content.getDouble("operatingrevenue"));
			
			return profitSheet;
		}
	}
	/**
	 * 负债表
	 */
	public static class BalanceSheet{
		private float mNonCurrentAssets;			// 非流动资产
		private float mCurrentAssets;				// 流动字长
		private float mTotalAssets;					// 资产总计
		private float mCurrentLiability;			// 流动负债
		private float mTotalLiability;				// 负债总计
		private float mShareholderEquity;			// 股东权益
		private float mLongTermLoan;				// 长期借款
		
		private String mInfoSource;					// 信息来源

		public String getNonCurrentAssets() {
			return FormatFinanceStr(mNonCurrentAssets);
		}

		public void setNonCurrentAssets(float mNonCurrentAssets) {
			this.mNonCurrentAssets = mNonCurrentAssets;
		}

		public String getCurrentAssets() {
			return FormatFinanceStr(mCurrentAssets);
		}

		public void setCurrentAssets(float mCurrentAssets) {
			this.mCurrentAssets = mCurrentAssets;
		}

		public String getTotalAssets() {
			return FormatFinanceStr(mTotalAssets);
		}

		public void setTotalAssets(float mTotalAssets) {
			this.mTotalAssets = mTotalAssets;
		}

		public String getCurrentLiability() {
			return FormatFinanceStr(mCurrentLiability);
		}

		public void setCurrentLiability(float mCurrentLiability) {
			this.mCurrentLiability = mCurrentLiability;
		}

		public String getTotalLiability() {
			return FormatFinanceStr(mTotalLiability);
		}

		public void setTotalLiability(float mTotalLiability) {
			this.mTotalLiability = mTotalLiability;
		}

		public String getShareholderEquity() {
			return FormatFinanceStr(mShareholderEquity);
		}

		public void setShareholderEquity(float mShareholderEquity) {
			this.mShareholderEquity = mShareholderEquity;
		}

		public String getLongTermLoan() {
			return FormatFinanceStr(mLongTermLoan);
		}

		public void setLongTermLoan(float mLogTermLoan) {
			this.mLongTermLoan = mLogTermLoan;
		}
		public String getInfoSource() {
			return mInfoSource;
		}

		public void setInfoSource(String mInfoSource) {
			this.mInfoSource = mInfoSource;
		}
		
		// 解析JSON
		public static BalanceSheet resolveJSONObject(JSONObject propertyOut)
				throws JSONException {
			BalanceSheet balanceSheet = new BalanceSheet();
			balanceSheet.setInfoSource(propertyOut.getString("infosource"));

			JSONObject content = propertyOut.getJSONObject("content");
			balanceSheet.setNonCurrentAssets((float) content.getDouble("totalNonCurrentAssets"));
			balanceSheet.setCurrentAssets((float) content.getDouble("totalCurrentAssets"));
			balanceSheet.setTotalAssets((float) content.getDouble("totalAssets"));
			balanceSheet.setCurrentLiability((float) content.getDouble("totalCurrentLiability"));
			balanceSheet.setTotalLiability((float) content.getDouble("totalLiability"));
			balanceSheet.setShareholderEquity((float) content.getDouble("totalShareholderEquity"));
			balanceSheet.setLongTermLoan((float) content.getDouble("longtermLoan"));

			return balanceSheet;
		}
				
	}
	
	/**
	 * 现金流表
	 */
	public static class CashFlowSheet{
		private float mNetOperateCashFlow;			// 经营现金流净额
		private float mNetInvestCashFlow;			// 投资现金流净额
		private float mNetFinanceCashFlow;			// 筹资现金流净额
		
		private String mInfoSource;					// 信息来源

		public String getNetOperateCashFlow() {
			return FormatFinanceStr(mNetOperateCashFlow);
		}

		public void setNetOperateCashFlow(float mNetOperateCashFlow) {
			this.mNetOperateCashFlow = mNetOperateCashFlow;
		}

		public String getNetInvestCashFlow() {
			return FormatFinanceStr(mNetInvestCashFlow);
		}

		public void setNetInvestCashFlow(float mNetInvestCashFlow) {
			this.mNetInvestCashFlow = mNetInvestCashFlow;
		}

		public String getNetFinanceCashFlow() {
			return FormatFinanceStr(mNetFinanceCashFlow);
		}

		public void setNetFinanceCashFlow(float mNetFinanceCashFlow) {
			this.mNetFinanceCashFlow = mNetFinanceCashFlow;
		}

		public String getInfoSource() {
			return mInfoSource;
		}

		public void setInfoSource(String mInfoSource) {
			this.mInfoSource = mInfoSource;
		}
		
		
		// 解析JSON
		public static CashFlowSheet resolveJSONObject(JSONObject profit)
						throws JSONException {
			CashFlowSheet cashFlowSheet = new CashFlowSheet();
			cashFlowSheet.setInfoSource(profit.getString("infosource"));
			
			JSONObject content = profit.getJSONObject("content");
			cashFlowSheet.setNetOperateCashFlow((float)content.getDouble("netOperateCashFlow"));
			cashFlowSheet.setNetInvestCashFlow((float)content.getDouble("netInvestCashFlow"));
			cashFlowSheet.setNetFinanceCashFlow((float)content.getDouble("netFinanceCashFlow"));
			
			return cashFlowSheet;
		}		
	}
	
	
	//	tool
	public static String FormatFinanceStr(float finance){
		
		float absFinance = Math.abs(finance);
		
		if(absFinance == 0)
			return DEFAULT_DATA;
		
		if(absFinance > 99999999){
			return new DecimalFormat("0.00").format(finance/100000000) + "亿元";
		}
		else if(absFinance > 999999){
			return new DecimalFormat("0.00").format(finance/1000000) + "百万元";
		}
		else if(absFinance > 9999){
			return new DecimalFormat("0.00").format(finance/10000) + "万元";
		}
		else{
			return new DecimalFormat("0.00").format(finance);
		}
		
	}
}