package com.zlf.appmaster.stocksearch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.StockMinutes;
import com.zlf.appmaster.chartview.chart.MinLineChart;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.client.StockMarketClient;
import com.zlf.appmaster.db.stock.StockMinuteTable;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.ui.stock.StockDealPromptDialog;
import com.zlf.appmaster.ui.stock.StockTextView;
import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.utils.UrlConstants;
import com.zlf.appmaster.utils.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;

;

public class StockDealActivity extends Activity {
	public static final String INTENT_FLAG_STOCKCODE = "intent_flag_stockcode";
	private static final String TAG = "StockDealActivity";
	private static final int MSG_SCROLL_END = 1;
	
	private View mBuyTab;
	private View mSellTab;
	
	private TextView mActivityTitle;
	private TextView mNameTV;
	private TextView mCodeTV;
	private TextView mNowPriceTV;
	private TextView mPercnetTV;
	private MinLineChart mMinLineChart;//分时线
	
	
	private TextView mStockMoneyTV;
	private TextView mStockMaxTV;
	private TextView mStockBuyorsellCountTV;
	private TextView mStockValueTV;
	
	private TextView mStockDealPromptTV;
	
	private SeekBar mStockNumSeekbar;
	private RadioGroup mStockNumRadiaoGroup;
	
	private Button mBtnStockDeal;	//市价 买入/卖出按钮
	
	// 买卖手信息
	private StockTextView[] mBuyInfoPriceTV = new StockTextView[5];
	private TextView[] mBuyInfoCountTV = new TextView[5];
	private StockTextView[] mSellInfoPriceTV = new StockTextView[5];
	private TextView[] mSellInfoCountTV = new TextView[5];
	private final int[] BUYINFO_PRICE_ID = { R.id.stock_buy_1_price,
			R.id.stock_buy_2_price, R.id.stock_buy_3_price,
			R.id.stock_buy_4_price, R.id.stock_buy_5_price };
	private final int[] BUYINFO_COUNT_ID = { R.id.stock_buy_1_count,
			R.id.stock_buy_2_count, R.id.stock_buy_3_count,
			R.id.stock_buy_4_count, R.id.stock_buy_5_count };
	private final int[] SELLINFO_PRICE_ID = { R.id.stock_sell_1_price,
			R.id.stock_sell_2_price, R.id.stock_sell_3_price,
			R.id.stock_sell_4_price, R.id.stock_sell_5_price };
	private final int[] SELLINFO_COUNT_ID = { R.id.stock_sell_1_count,
			R.id.stock_sell_2_count, R.id.stock_sell_3_count,
			R.id.stock_sell_4_count, R.id.stock_sell_5_count, };
	
	private String mStockCode;
	private StockClient mStockClient;
	private StockTradeInfo mStockTradeInfo = null;
	
	private OnTabListener mOnTabListener;
	
	private boolean mbMarketPriceDealFlag = true;		// 市价或限价
	private boolean mbBuyFlag = true;					// 买或卖
	private long mBuyNum = 0;
	private long mSellNum = 0;
	
	private ProgressBar mProgressBar;
	private TextView mActionBarSwitchTV;
	private View mBuyOrSellTab;
	private View mMarketPriceLayout;
	private View mUserPriceLayout;
	
	// 限价委托操作控件
	private EditText mStockDealPriceEdit;
	private EditText mStockDealCountEdit;
	private Button mStockDealSellBtn;
	private Button mStockDealBuyBtn;
	
	private ScrollView mSVLayout;
	private StockDealPromptDialog mShowAlertDialog;
    private Context mContext;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case MSG_SCROLL_END:
//				Log.i(TAG, "滚至底部");
//				mSVLayout.fullScroll(ScrollView.FOCUS_DOWN);
//				int offset = mStockDealSellBtn.getMeasuredHeight() - mSVLayout.getHeight();
//				if (offset < 0) {
//				offset = 0;
//				}
//
//				mSVLayout.scrollTo(0, offset);
				mSVLayout.fullScroll(ScrollView.FOCUS_DOWN);
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_deal);

        mContext = this;

		initViews();
		
		Intent intent = getIntent();
		mStockCode = intent.getStringExtra(INTENT_FLAG_STOCKCODE);
		mStockClient = new StockClient(this);
		
		
		if (Utils.GetNetWorkStatus(this)) {
			mProgressBar.setVisibility(View.VISIBLE);
			requestData();
		}
		else{
			Toast.makeText(this, getResources().getString(R.string.network_unconnected),
					Toast.LENGTH_SHORT).show();
		}
		
	}

	
	private void initViews() {
		
		mProgressBar = (ProgressBar)findViewById(R.id.content_loading);
		
		mSVLayout = (ScrollView)findViewById(R.id.sv_layout);
		
		mActivityTitle = (TextView) findViewById(R.id.title);
		mActionBarSwitchTV = (TextView) findViewById(R.id.tv_title_next);
		mActionBarSwitchTV.setVisibility(View.VISIBLE);
		mActionBarSwitchTV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mbMarketPriceDealFlag = !mbMarketPriceDealFlag;
				switchActionBar(mbMarketPriceDealFlag);
			}
		});
		
		mBuyOrSellTab = findViewById(R.id.stock_deal_sellorbuy_tab);
		mMarketPriceLayout = findViewById(R.id.market_price_layout);
		mUserPriceLayout = findViewById(R.id.user_price_layout);

		
		// tab
		mBuyTab = findViewById(R.id.stock_buy_tab);
		mSellTab = findViewById(R.id.stock_sell_tab);
		mOnTabListener = new OnTabListener();
		mBuyTab.setSelected(true);
		mBuyTab.setOnClickListener(mOnTabListener);
		mSellTab.setOnClickListener(mOnTabListener);
		
		// 标题
		mNameTV = (TextView)findViewById(R.id.stock_name);
		mCodeTV = (TextView)findViewById(R.id.stock_code);
		mNowPriceTV = (TextView)findViewById(R.id.now_price);
		mPercnetTV = (TextView)findViewById(R.id.now_percent);
		
		for(int i = 0; i<5; i++) {
			mBuyInfoPriceTV[i] = (StockTextView)findViewById(BUYINFO_PRICE_ID[i]);
			mBuyInfoCountTV[i] = (TextView)findViewById(BUYINFO_COUNT_ID[i]);
			mSellInfoPriceTV[i] = (StockTextView)findViewById(SELLINFO_PRICE_ID[i]);
			mSellInfoCountTV[i] = (TextView)findViewById(SELLINFO_COUNT_ID[i]);
		}
		mMinLineChart = (MinLineChart) findViewById(R.id.stock_k_line_view);
		
		mStockMoneyTV = (TextView)findViewById(R.id.stock_money);
		mStockMaxTV = (TextView)findViewById(R.id.stock_max);
		mStockBuyorsellCountTV = (TextView)findViewById(R.id.stock_buyorsell_count);
		mStockValueTV = (TextView)findViewById(R.id.stock_value);
		mStockDealPromptTV = (TextView)findViewById(R.id.stock_deal_prompt1);
		
		mStockNumSeekbar = (SeekBar)findViewById(R.id.stock_num_seekbar);
		mStockNumRadiaoGroup = (RadioGroup)findViewById(R.id.stock_num_radiogroup);
		
		mBtnStockDeal = (Button) findViewById(R.id.btn_stock_deal);
		mBtnStockDeal.setOnClickListener(new OnDealBtnClickListener());
		
		mStockNumSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				refreshBuyNumber(progress);
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		mStockNumRadiaoGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				int value = 0;
				switch(checkedId){
				case R.id.radio_one_fifth:
					value = mStockNumSeekbar.getMax()/5;
					break;
				case R.id.radio_one_fourth:
					value = mStockNumSeekbar.getMax()/4;
					break;
				case R.id.radio_one_third:
					value = mStockNumSeekbar.getMax()/3;
					break;
				case R.id.radio_half:
					value = mStockNumSeekbar.getMax()/2;
					break;
				case R.id.radio_all:
					value = mStockNumSeekbar.getMax();
					break;
				}
				mStockNumSeekbar.setProgress(value);
			}
		});
		
		
		mStockDealPriceEdit = (EditText) findViewById(R.id.stock_deal_price_edit);
		mStockDealCountEdit = (EditText) findViewById(R.id.stock_deal_count_edit);
		mStockDealPriceEdit.setOnFocusChangeListener(new OnEditFocusListener());
		//mStockDealCountEdit.setOnFocusChangeListener(new OnEditFocusListener());
		mStockDealSellBtn = (Button) findViewById(R.id.btn_stock_deal_sell);
		mStockDealBuyBtn = (Button) findViewById(R.id.btn_stock_deal_buy);
		mStockDealSellBtn.setOnClickListener(new OnDealBtnClickListener());
		mStockDealBuyBtn.setOnClickListener(new OnDealBtnClickListener());
		
		mShowAlertDialog = new StockDealPromptDialog(this);
		mShowAlertDialog.setCanceledOnTouchOutside(false);
		mShowAlertDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		switchActionBar(mbMarketPriceDealFlag);
	}
	
	private void updateViews() {
        getStockMinuteData(mMinLineChart,mStockCode);

		mNameTV.setText(mStockCode);
		if (mStockTradeInfo != null) {
			mCodeTV.setText(mStockTradeInfo.getName());
			mNowPriceTV.setText(mStockTradeInfo.getCurPriceFormat());
			mPercnetTV.setText(mStockTradeInfo.getCurPercentFormat());

			StockTradeInfo.BuyOrSellInfo[] buyInfo = mStockTradeInfo.getBuyInfo();
			StockTradeInfo.BuyOrSellInfo[] sellInfo = mStockTradeInfo.getSellInfo();
			for (int i = 0; i < 5; i++) {
				mBuyInfoPriceTV[i].setRiseInfo(buyInfo[i].getRiseInfo());
				mBuyInfoPriceTV[i].setText(buyInfo[i].getPriceFormat());
				mBuyInfoCountTV[i].setText(buyInfo[i].getCountFormat());
				mSellInfoPriceTV[i].setRiseInfo(sellInfo[i].getRiseInfo());
				mSellInfoPriceTV[i].setText(sellInfo[i].getPriceFormat());
				mSellInfoCountTV[i].setText(sellInfo[i].getCountFormat());
			}
			
			swithBuyTab(mbBuyFlag);
			
			int marketStatus = mStockTradeInfo.getMarketStatus();
			if(marketStatus != StockTradeInfo.MARKET_STATUS_NORMAL) {
				String prompt = "非交易时段，只能进行限价交易";
				if(marketStatus == StockTradeInfo.MARKET_STATUS_CLOSE){
					prompt = "休市中，只能进行限价交易";
				}
				
				mBtnStockDeal.setEnabled(false);
				showPrompt(prompt);
				return ;
			}
		}
	}
	
	private void requestData(){
		mSVLayout.setEnabled(false);	//请求数据时不能操作界面
		mStockClient.requestStockHoldersInfo(mStockCode, new OnRequestListener() {
			
			@Override
			public void onError(int errorCode, String errorString) {
				// TODO Auto-generated method stub
				mProgressBar.setVisibility(View.GONE);
				mSVLayout.setEnabled(true);
				UrlConstants.showUrlErrorCode(StockDealActivity.this, errorCode, errorString);
			}
			
			@Override
			public void onDataFinish(Object object) {
				// TODO Auto-generated method stub
				 mProgressBar.setVisibility(View.GONE);
				 mSVLayout.setEnabled(true);
				 mStockTradeInfo = (StockTradeInfo) object;
				 updateViews();
			}
		});
		
	}
	
	// userPrice和userCount当为限价时有效
	private void requestStockDeal(float userPrice, long userCount) {
		
		if (mStockTradeInfo == null)
			return;
		
		/**
		 * 默认数据为以市价买入的情况
		 */
		int thradeType; // 1买 -1卖
		int orderType; // 1 限价 2市价
		String stockCode = mStockTradeInfo.getCode();
		float stockPrice;
		long stockNum;
		String reason = "";

		if (mbMarketPriceDealFlag) { 
			orderType = 2; 
			stockPrice = mStockTradeInfo.getCurPrice();
			
			if (mbBuyFlag) {
				thradeType = 1;
				stockNum = mBuyNum;
			}
			else { // 卖出
				thradeType = -1;
				stockNum = mSellNum;
			}
		}
		else {// 限价
			orderType = 1;
			stockPrice = userPrice;
			stockNum = userCount;
			
			if (mbBuyFlag) {
				thradeType = 1;
			}
			else { // 卖出
				thradeType = -1;
			}
		}
		
		mProgressBar.setVisibility(View.VISIBLE);
		mBtnStockDeal.setEnabled(false);
		// 请求网络
		StockMarketClient.getInstance(this).requestTradeOrder(thradeType,
				orderType, stockCode, (int) stockNum, stockPrice, reason,
				new OnRequestListener() {

					@Override
					public void onError(int errorCode, String errorString) {
						// TODO Auto-generated method stub
						mProgressBar.setVisibility(View.GONE);
						mBtnStockDeal.setEnabled(true);
						QLog.i(TAG, "errorString:"+errorString);
						UrlConstants.showUrlErrorCode(StockDealActivity.this, errorCode, UrlConstants.getServerErrorString(errorString));
					}

					@Override
					public void onDataFinish(Object object) {
						// TODO Auto-generated method stub
						mProgressBar.setVisibility(View.GONE);
						showAlert("委托成功");
						mBtnStockDeal.setEnabled(true);
					}
				});
	}
	
	
	
	private class OnTabListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			clearAllTabSelected();
			v.setSelected(true);
			mStockNumRadiaoGroup.check(R.id.radio_one_fifth);
			if(v == mBuyTab){
				mbBuyFlag = true;
			}
			else {
				mbBuyFlag = false;
			}
			swithBuyTab(mbBuyFlag);
		}
	} 
	
	private void swithBuyTab(boolean bBuyTab){
	
		if(mStockTradeInfo == null)	return;
		
		if(bBuyTab){
			float accountMoney = mStockTradeInfo.getAccountMoney();
			float idleMoney = (float) (accountMoney - accountMoney*0.0003);
			float maxNumber = 0;
			
			if(mStockTradeInfo.isSHCode()){
				//maxNumber = (float) (idleMoney/(mStockTradeInfo.getNowPrice()+0.1));
				// 上证每1000股收取1元（相当于每股多了0.001元）
				maxNumber = (float) (idleMoney/(formatStockPrice(mStockTradeInfo.getYestodayPrice()*1.1)+0.001)); // 昨日收盘价上浮10%为基准
			}
			else{
				//maxNumber = idleMoney/mStockTradeInfo.getNowPrice();
				maxNumber = (float) (idleMoney/(formatStockPrice(mStockTradeInfo.getYestodayPrice()*1.1)));
			}
			maxNumber = 100*((int)maxNumber/100);
			mStockNumSeekbar.setMax((int) maxNumber);
			mStockNumSeekbar.setProgress((int)maxNumber/5);
			
			mStockMoneyTV.setText(String.format("可用资金: %.0f", accountMoney));
			mStockMaxTV.setText(String.format("最多可买: %.0f股", maxNumber));
			mStockDealPromptTV.setVisibility(View.VISIBLE);
			
			mBtnStockDeal.setText(getResources().getString(R.string.stock_buy));
			mBtnStockDeal.setBackgroundResource(R.drawable.btn_stock_deal_buy);
			
		}
		else {
			long maxNumber = mStockTradeInfo.getCanHoldNum();
			mStockNumSeekbar.setMax((int) maxNumber);
			mStockNumSeekbar.setProgress(0);
			
			mStockMoneyTV.setText(String.format("现有持仓: %d股", mStockTradeInfo.getHoldNum()));
			mStockMaxTV.setText(String.format("最多可卖: %d股", maxNumber));
			mStockDealPromptTV.setVisibility(View.GONE);
			
			mBtnStockDeal.setText(getResources().getString(R.string.stock_sell));
			mBtnStockDeal.setBackgroundResource(R.drawable.btn_stock_deal_sell);
		}
		
	}
	
	private void switchActionBar(boolean bMarketPrice){
		if(bMarketPrice){
			mActionBarSwitchTV.setText(getResources().getString(R.string.stock_user_price));
			mActivityTitle.setText(getResources().getString(R.string.stock_market_price_deal));
			
			mBuyOrSellTab.setVisibility(View.VISIBLE);
			mMarketPriceLayout.setVisibility(View.VISIBLE);
			mUserPriceLayout.setVisibility(View.GONE);
			if (mStockTradeInfo != null) {
				swithBuyTab(mbBuyFlag);
				if(mbBuyFlag){
					refreshBuyNumber(100);
				}
				else {
					refreshBuyNumber(0);
				}
				
			}
		}
		else{
			mActionBarSwitchTV.setText(getResources().getString(R.string.stock_market_price));
			mActivityTitle.setText(getResources().getString(R.string.stock_price_deal));
			
			mBuyOrSellTab.setVisibility(View.GONE);
			mMarketPriceLayout.setVisibility(View.GONE);
			mUserPriceLayout.setVisibility(View.VISIBLE);
			if(mStockTradeInfo != null){
				float accountMoney = mStockTradeInfo.getAccountMoney();
				float idleMoney = (float) (accountMoney - accountMoney*0.0003);
				float buyMaxNumber = 0;
				if(mStockTradeInfo.isSHCode()){
					buyMaxNumber = (float) (idleMoney/(mStockTradeInfo.getNowPrice()+0.001));
					//buyMaxNumber = (float) (idleMoney/(mStockTradeInfo.getYestodayPrice()*1.1+0.1)); // 昨日收盘价上浮10%为基准
				}
				else{
					buyMaxNumber = idleMoney/mStockTradeInfo.getNowPrice();
					//buyMaxNumber = (float) (idleMoney/(mStockTradeInfo.getYestodayPrice()*1.1));
				}
				buyMaxNumber = 100*((int)buyMaxNumber/100);
				mStockMoneyTV.setText(String.format("可用资金: %.0f", accountMoney));
				mStockBuyorsellCountTV.setText(String.format("最多可买: %.0f股", buyMaxNumber));
				mStockMaxTV.setText(String.format("现有持仓: %d股", mStockTradeInfo.getHoldNum()));
				
				long sellMaxNumber = mStockTradeInfo.getCanHoldNum();
				mStockValueTV.setText(String.format("最多可卖: %d股", sellMaxNumber));
			}
			
		}
		
	}
	
	
	private void refreshBuyNumber(int value){
		
		//Log.i(TAG, "refreshBuyNumber:"+mbBuyFlag+"("+value+")");
		if(mStockTradeInfo == null)	return;
		
		if (mbBuyFlag) {
			mBuyNum = 100*(value/100);
			mStockBuyorsellCountTV.setText(String.format("买入数量: %d股", mBuyNum));
			mStockValueTV.setText(String.format("市        值: %.2f元", mStockTradeInfo.getNowPrice()*mBuyNum));
	    }
	    else
	    {
	        mSellNum = 1*(value/1);
	        mStockBuyorsellCountTV.setText(String.format("卖出数量: %d股", mSellNum));
			float percent = (mStockTradeInfo.getNowPrice() - mStockTradeInfo
					.getCostPrice()) * mSellNum;
			if (percent >= 0.000000 && percent <= -0.000000) {
				mStockValueTV.setText("盈        亏: 0元");
			} else {
				mStockValueTV.setText(String.format("盈        亏: %.2f元",
						percent));
			}
	    }
	}
	
	
	private  void clearAllTabSelected(){
		mBuyTab.setSelected(false);
		mSellTab.setSelected(false);
	}
	
	private void showPrompt(String content){
		//Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
		Toast toast = Toast.makeText(this, content, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 10);
		toast.show();
	}
	
	private void showAlert(String content){
		mShowAlertDialog.show();
	}
	
	
	private class OnDealBtnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v == mStockDealSellBtn || v == mStockDealBuyBtn){
				if(v == mStockDealSellBtn){
					mbBuyFlag = false;
				}
				else {
					mbBuyFlag = true;
				}
				
				String priceString = mStockDealPriceEdit.getText().toString();
				double price = 0.0;
				if(TextUtils.isEmpty(priceString)){
					showPrompt("请输入价格");
					return;
				}
				else {
					price = Double.valueOf(priceString);
				}
	
				String countString = mStockDealCountEdit.getText().toString();
				long count = 0;
				if (TextUtils.isEmpty(countString)) {
					showPrompt("请确认输入数量");
					return;
				} else {
					count = Integer.valueOf(countString);
					if(mbBuyFlag){
						if (count == 0 || count % 100 != 0) {
							showPrompt("数量需为100的倍数");
							return;
						}
					}
					else{
						
						if(null != mStockTradeInfo){
							// 大于0小于等于最大可卖数
							long maxNumber = mStockTradeInfo.getCanHoldNum();
							if(count <= 0){
								showPrompt("数量不能为0");
								return;
							}
							else if(count > maxNumber){
								showPrompt("数量超过最多可卖数");
								return;
							}
						}
						
					}
			
				}

				// 限价请求
				requestStockDeal((float)price, count);
			}
			else if(v == mBtnStockDeal){
				
				// 市价请求 
				requestStockDeal(0,0);
			}


			
		}
		
	}
	
	
	private class OnEditFocusListener implements OnFocusChangeListener {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			  if(hasFocus) {
				  //如果不延时，系统的输入法一起弹出则无法滚动至底部，原因未知
				  	mHandler.sendEmptyMessageDelayed(MSG_SCROLL_END, 500);
				} else {


				}
			 
		}
		
	}
	
	/**
	 * 格式化股票价格，四舍五入保留小数后两位
	 * @return
	 */
	private static float formatStockPrice(double d){
		return new BigDecimal(d).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
	}


    /**
     * 获取分时数据
     * @param mLine,mStockIndexID
     */
    private void getStockMinuteData(final MinLineChart mLine,final String mStockID) {

        QLog.i(TAG,"getStockMinuteData");
        long lastTime = 0;//去掉毫秒
        //先从数据库中读取显示
        StockMinuteTable table = new StockMinuteTable(mContext);//用指数的！！
        byte[] b = table.getMintueData(mStockID);
        if (b != null && b.length > 0) {
            ArrayList<StockMinutes> dataArrayList = StockMinutes.resloveByteArray(b);
            if (dataArrayList != null && dataArrayList.size() > 0) {
                mLine.setLineData(dataArrayList,dataArrayList.get(0).getYestodayPrice());
				//修改为数据库的时间
				lastTime = dataArrayList.get(dataArrayList.size()-1).getDataTime();
            }
        }
        table.close();

        //再去读取网络的
        //设置最后一笔的时间点
        final long endTime = 99999999999999L;

        mStockClient.getMinuteInfoByLastTime(mStockID, StockClient.TYPE_STOCK,
                lastTime,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                        QLog.i(TAG,"getMinuteInfoByLastTime:onError");
                    }

                    @Override
                    public void onDataFinish(Object object) {

                        QLog.i(TAG,"getMinuteInfoByLastTime:onDataFinish");

                        ArrayList<StockMinutes> dataArrayList = StockMinutes.resloveMinutesData(object,true,endTime);
                        if (dataArrayList == null || dataArrayList.size() == 0) {
                            //没有获取到数据
//                            listener.onError();
                            return;
                        }
                        mLine.setLineData(dataArrayList, dataArrayList.get(0).getYestodayPrice());

                        //保存到数据库
                        StockMinuteTable table = new StockMinuteTable(mContext);
                        table.saveMintueData(mStockID, StockMinutes.toByteArray(dataArrayList), endTime);
                        table.close();
                    }
                });

    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		super.onDestroy();
	}
	
	public void onBack(View view){
		finish();
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
}