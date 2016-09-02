package com.zlf.appmaster.stocktrade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.StockKLine;
import com.zlf.appmaster.chartview.bean.StockMinutes;
import com.zlf.appmaster.chartview.chart.KLineChart;
import com.zlf.appmaster.chartview.chart.MinuteLine;
import com.zlf.appmaster.chartview.view.OnTouchChartListener;
import com.zlf.appmaster.chartview.view.StockChartView;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.db.stock.IndexKLineTable;
import com.zlf.appmaster.db.stock.IndexMinuteTable;
import com.zlf.appmaster.db.stock.StockKLineTable;
import com.zlf.appmaster.db.stock.StockMinuteTable;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.ui.stock.StockTextView;
import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.utils.TimeUtil;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 股票图表详情--（分时线与K线） 
 * @author Yushian
 *
 */
public class StockChartDetailActivity extends Activity {

	private static final String TAG = "StockChartDetailActivity";

	public static final String INTENT_FROM_GUOXIN = "from_guoxin";
	public static final String INTENT_EXTRA_STOCK_ID = "stockId";
	public static final String INTENT_EXTRA_IS_STOCK = "isStock";
	public static final String INTENT_EXTRA_LINE_TYPE = "lineType";
	public static final String INTENT_EXTRA_STOCK_NAME = "stockName";
    public static final String INTENT_EXTRA_STOCK_STATE = "stockState";//股票状态
    public static final String INTENT_EXTRA_STOCK_PRICE = "stockPrice";//当前价

	public static final String INTENT_EXTRA_MINUTE_DATA_LIST = "minute_data_list";// 分时数据集
	public static final String INTENT_EXTRA_KLINE_DATA_LIST = "kline_data_list";// K线数据集
	public static final String INTENT_EXTRA_KLINE_DATA_LIST_TAG = "kline_data_list_tag";// K线数据集tag
	
	private final int[] KLINE_TAB_ID = {R.id.min_line, R.id.daily_line, R.id.weekly_line, R.id.monthly_line};
	private View[] mKLineTab = new View[4];
	private OnKLineRadioChangeListener mOnKLineRadioChangeListener = new OnKLineRadioChangeListener();
	
	//跳转类型
	public static final int KLINE_TYPE_MIN = 0; // 分时
	public static final int KLINE_TYPE_DAILY = 1; 
	public static final int KLINE_TYPE_WEEKLY = 2; 
	public static final int KLINE_TYPE_MONTHLY = 3;
	
	private static final int KLINE_GETDATA_NULL = 0;
	private static final int KLINE_GETDATA_LOADING = 1;
	private static final int KLINE_GETDATA_FINISH = 2;
	
	private String mStockCode;
	private boolean mIsStock;
	private int mLineType,mStockState;
    private float mCurPrice;
	private KLineChart mDailyKLineChart,mWeeklyKLineChart,mMonthlyKLineChart;
	private MinuteLine mMinuteLine;
    private View mHandicapView;

	private StockClient mStockClient;
	private CircularProgressView mProgressBar;
	private Context mContext;
	private ArrayList<StockKLine> mDailyKLines;//,mWeeklyKlines,mMonthlyKlines;
    private ArrayList<StockMinutes> mMinutes;
	private int getDailyDataStatus;
	private TextView mCodeTextView,mNameTextView,mPriceTextView,mPercentTextView,mVolumeTextView,mTimeTextView;
    //时间、价格、涨跌、成交、均价
    private TextView mMinTimeView,mMinPriceView,mMinPercentView,mMinVolumeView,mMinMaValueView;
    //时间、开、高、低、收 涨跌
    private TextView mKTimeView,mKOpenView,mKCloseView,mKHighView,mKLowView,mKPercentView;
	private View mMinMaLayout;

	private ImageButton mExitButton;
	private DataHandler mDataHandler;
    private View mNormalTitleView, mMinTitleView,mKLineTitleView;

	//默认今天数据
	private String mPriceString,mPercentString,mVolumeString,mTimeString;
	private int stockColor;//涨跌颜色

    // 买卖手信息
    private StockTextView[] mBuyInfoPriceTV = new StockTextView[5];
    private TextView[] mBuyInfoCountTV = new TextView[5];
    private StockTextView[] mSellInfoPriceTV = new StockTextView[5];
    private TextView[] mSellInfoCountTV = new TextView[5];

    private final int[] BUYINFO_PRICE_ID = {R.id.stock_buy_1_price,R.id.stock_buy_2_price,R.id.stock_buy_3_price,
            R.id.stock_buy_4_price,R.id.stock_buy_5_price};
    private final int[] BUYINFO_COUNT_ID = {R.id.stock_buy_1_count,R.id.stock_buy_2_count,R.id.stock_buy_3_count,
            R.id.stock_buy_4_count,R.id.stock_buy_5_count};
    private final int[] SELLINFO_PRICE_ID = {R.id.stock_sell_1_price, R.id.stock_sell_2_price, R.id.stock_sell_3_price,
            R.id.stock_sell_4_price, R.id.stock_sell_5_price};
    private final int[] SELLINFO_COUNT_ID = {R.id.stock_sell_1_count, R.id.stock_sell_2_count, R.id.stock_sell_3_count,
            R.id.stock_sell_4_count, R.id.stock_sell_5_count};


    private int mCurAdjustType = 2;//复权类型
    private int mCurExtraType = 2;//指标类型

	private boolean mFromGuoXin;
	private ArrayList<StockMinutes> mMinuteDataList;
	private ArrayList<StockKLine> mKLineDataList;
	private String mKLineTag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stockchartdetail);
		initView();
		initData();
	}
	
	private void initView(){

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mNormalTitleView = findViewById(R.id.layout_title_normal);
        mMinTitleView = findViewById(R.id.layout_title_min);
        mNormalTitleView.setVisibility(View.VISIBLE);
        mMinTitleView.setVisibility(View.GONE);

        mMinTimeView = (TextView)findViewById(R.id.tv_min_time);
        mMinPriceView = (TextView)findViewById(R.id.tv_min_price);
        mMinPercentView = (TextView)findViewById(R.id.tv_min_percent);
        mMinVolumeView = (TextView)findViewById(R.id.tv_min_volume);
		mMinVolumeView.setVisibility(View.GONE);
        mMinMaValueView = (TextView)findViewById(R.id.tv_min_ma);
		mMinMaLayout = findViewById(R.id.layout_min_ma);

        mKLineTitleView = findViewById(R.id.layout_title_kline);
        mKLineTitleView.setVisibility(View.GONE);

        mKTimeView = (TextView)findViewById(R.id.tv_kline_time);
        mKCloseView = (TextView)findViewById(R.id.tv_kline_close);
        mKOpenView = (TextView)findViewById(R.id.tv_kline_open);
        mKHighView = (TextView)findViewById(R.id.tv_kline_high);
        mKLowView = (TextView)findViewById(R.id.tv_kline_low);
        mKPercentView = (TextView)findViewById(R.id.tv_kline_percent);

		mDailyKLineChart = (KLineChart) findViewById(R.id.kline_daily);
		mDailyKLineChart.setKLineType(StockChartView.KLINE_TYPE_DAILY);
		mDailyKLineChart.setInBigPicMode();
		
		mWeeklyKLineChart = (KLineChart) findViewById(R.id.kline_weekly);
		mWeeklyKLineChart.setKLineType(StockChartView.KLINE_TYPE_WEEKLY);
		mWeeklyKLineChart.setInBigPicMode();
		
		mMonthlyKLineChart = (KLineChart) findViewById(R.id.kline_monthly);
		mMonthlyKLineChart.setKLineType(StockChartView.KLINE_TYPE_MONTHLY);
		mMonthlyKLineChart.setInBigPicMode();

        mDailyKLineChart.setOnTouchChartListener(new OnTouchStockChartListener(KLINE_TYPE_DAILY));
        mWeeklyKLineChart.setOnTouchChartListener(new OnTouchStockChartListener(KLINE_TYPE_WEEKLY));
        mMonthlyKLineChart.setOnTouchChartListener(new OnTouchStockChartListener(KLINE_TYPE_MONTHLY));
		
		mMinuteLine = (MinuteLine)findViewById(R.id.mline);
		mMinuteLine.setInBigPicMode();
		mMinuteLine.setOnTouchChartListener(new OnTouchStockChartListener(KLINE_TYPE_MIN));

        mHandicapView = findViewById(R.id.handicap_layout);

		
		mProgressBar = (CircularProgressView)findViewById(R.id.content_loading);
		
		for(int i = 0; i < 4; i++) {
			mKLineTab[i] = findViewById(KLINE_TAB_ID[i]);
			mKLineTab[i].setOnClickListener(mOnKLineRadioChangeListener);
		}
		
		//股票相关信息
		mCodeTextView = (TextView)findViewById(R.id.tv_stockcode);
		mNameTextView = (TextView)findViewById(R.id.tv_stockname);
		mPriceTextView = (TextView)findViewById(R.id.tv_stockprice);
		mPercentTextView = (TextView)findViewById(R.id.tv_stockpercent);
		mVolumeTextView = (TextView)findViewById(R.id.tv_stockvolume);
		mTimeTextView = (TextView)findViewById(R.id.tv_stocktime);
	
		mExitButton = (ImageButton)findViewById(R.id.btn_exit);
		mExitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});

        for(int i = 0; i<5; i++) {
            mBuyInfoPriceTV[i] = (StockTextView)findViewById(BUYINFO_PRICE_ID[i]);
            mBuyInfoCountTV[i] = (TextView)findViewById(BUYINFO_COUNT_ID[i]);
            mSellInfoPriceTV[i] = (StockTextView)findViewById(SELLINFO_PRICE_ID[i]);
            mSellInfoCountTV[i] = (TextView)findViewById(SELLINFO_COUNT_ID[i]);
        }
	}

	private void changeIndexTextSize(){
		mNameTextView.setTextSize(18);
		mCodeTextView.setTextSize(18);
		mPriceTextView.setTextSize(22);
		mPercentTextView.setTextSize(22);

		mMinPriceView.setTextSize(14);
		mMinPercentView.setTextSize(14);
		mMinVolumeView.setTextSize(14);

		mKCloseView.setTextSize(14);
		mKOpenView.setTextSize(14);
		mKHighView.setTextSize(14);
		mKLowView.setTextSize(14);
		mKPercentView.setTextSize(14);

		mMinMaLayout.setVisibility(View.GONE);
	}


	private void initData() {
		
		mDataHandler = new DataHandler(this);
		
		Intent intent = getIntent();
		
		//股票id、是否为股指、类型
		mStockCode = intent.getStringExtra(INTENT_EXTRA_STOCK_ID);
		mIsStock = intent.getBooleanExtra(INTENT_EXTRA_IS_STOCK, true);
		mLineType = intent.getIntExtra(INTENT_EXTRA_LINE_TYPE, KLINE_TYPE_MIN);
        mStockState = intent.getIntExtra(INTENT_EXTRA_STOCK_STATE, 0);
        mCurPrice = intent.getFloatExtra(INTENT_EXTRA_STOCK_PRICE, 0);
		mFromGuoXin = intent.getBooleanExtra(INTENT_FROM_GUOXIN, false);
		mMinuteDataList = (ArrayList<StockMinutes>) intent.getSerializableExtra(INTENT_EXTRA_MINUTE_DATA_LIST);
		mKLineDataList = (ArrayList<StockKLine>) intent.getSerializableExtra(INTENT_EXTRA_KLINE_DATA_LIST);
		mKLineTag = (String) intent.getStringExtra(INTENT_EXTRA_KLINE_DATA_LIST_TAG);

		mNameTextView.setText(intent.getStringExtra(INTENT_EXTRA_STOCK_NAME));
		mCodeTextView.setText(mStockCode);
		
		mStockClient = new StockClient(this);
		mContext = this;
		
		getDailyDataStatus = KLINE_GETDATA_NULL;
		
		mKLineTab[mLineType].setSelected(true);
		mKLineTab[mLineType].setEnabled(false);


		if (!mIsStock){
			//指数则修改字号显示
			changeIndexTextSize();

			mDailyKLineChart.setShowRepairView(false);
			mWeeklyKLineChart.setShowRepairView(false);
			mMonthlyKLineChart.setShowRepairView(false);
		}

		//请求数据
		getData();
	}


	private void hideCurChart(int KLineType) {
		switch (KLineType) {
		case KLINE_TYPE_MIN:
			mHandicapView.setVisibility(View.GONE);
			mMinuteLine.setVisibility(View.GONE);
			break;
		case KLINE_TYPE_DAILY:
			mDailyKLineChart.setVisibility(View.GONE);
			break;
		case KLINE_TYPE_WEEKLY:
			mWeeklyKLineChart.setVisibility(View.GONE);
			break;
		case KLINE_TYPE_MONTHLY:
			mMonthlyKLineChart.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}
	
	private void showCurChart(int KLineType) {
		switch (KLineType) {
		case KLINE_TYPE_MIN:
            if (mIsStock){
                mHandicapView.setVisibility(View.VISIBLE);
            }
			mMinuteLine.setVisibility(View.VISIBLE);
			break;
		case KLINE_TYPE_DAILY:
			mDailyKLineChart.setVisibility(View.VISIBLE);
			break;
		case KLINE_TYPE_WEEKLY:
			mWeeklyKLineChart.setVisibility(View.VISIBLE);
			break;
		case KLINE_TYPE_MONTHLY:
			mMonthlyKLineChart.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

    class OnTouchStockChartListener implements OnTouchChartListener {
        private int mType;
        OnTouchStockChartListener(int type) {
            mType = type;
        }

        @Override
        public void onTouchDown(int no) {
            StockKLine info = null;

            switch (mType) {
                case KLINE_TYPE_MIN:
                    StockMinutes itemMinutes = (StockMinutes) mMinuteLine.getData().get(no);
                    setTradeInfo(itemMinutes);
                    return;
                case KLINE_TYPE_DAILY:
                    info = mDailyKLineChart.getItem(no);
                    break;
                case KLINE_TYPE_WEEKLY:
                    info = mWeeklyKLineChart.getItem(no);
                    break;
                case KLINE_TYPE_MONTHLY:
                    info = mMonthlyKLineChart.getItem(no);
                    break;
                default:
                    break;
            }

            if (info != null){
                setKlineInfo(info);
            }

        }

        @Override
        public void onTouchUp() {
            mMinTitleView.setVisibility(View.GONE);
            mKLineTitleView.setVisibility(View.GONE);
            mNormalTitleView.setVisibility(View.VISIBLE);
        }

		@Override
		public void onMove(int no) {

		}

		@Override
		public void onChange() {

		}
	}

    /**
     * 保存当前复权类型
     */
    private void getCurAdjustAndExtraType(){
        switch (mLineType){
            case KLINE_TYPE_DAILY:
                mCurAdjustType = mDailyKLineChart.getAdjustType();
                mCurExtraType = mDailyKLineChart.getExtraType();
                break;

            case KLINE_TYPE_WEEKLY:
                mCurAdjustType = mWeeklyKLineChart.getAdjustType();
                mCurExtraType = mWeeklyKLineChart.getExtraType();
                break;

            case KLINE_TYPE_MONTHLY:
                mCurAdjustType = mMonthlyKLineChart.getAdjustType();
                mCurExtraType = mMonthlyKLineChart.getExtraType();
                break;
        }


    }

    private void setCurAdjustAndExtraType(){
        switch (mLineType){
            case KLINE_TYPE_DAILY:
                mDailyKLineChart.setAdjustType(mCurAdjustType);
                mDailyKLineChart.setExtraType(mCurExtraType);
                break;

            case KLINE_TYPE_WEEKLY:
                mWeeklyKLineChart.setAdjustType(mCurAdjustType);
                mWeeklyKLineChart.setExtraType(mCurExtraType);
                break;

            case KLINE_TYPE_MONTHLY:
                mMonthlyKLineChart.setAdjustType(mCurAdjustType);
                mMonthlyKLineChart.setExtraType(mCurExtraType);
                break;
        }
    }

	private class OnKLineRadioChangeListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			for(int i = 0; i < mKLineTab.length; i++) {
				mKLineTab[i].setSelected(false);
				mKLineTab[i].setEnabled(true);
			}
			v.setSelected(true);
			v.setEnabled(false);
			
//			QLog.i(TAG, "隐藏所有view");
			hideCurChart(mLineType);
            getCurAdjustAndExtraType();

			int id = v.getId();
			if (id == R.id.min_line) {
				mLineType = KLINE_TYPE_MIN;
			}else {
				if (id == R.id.daily_line) {
					mLineType = KLINE_TYPE_DAILY;
				}else if (id == R.id.weekly_line) {
					mLineType = KLINE_TYPE_WEEKLY;
				}else if (id == R.id.monthly_line) {
					mLineType = KLINE_TYPE_MONTHLY;
				}
			}

			getData();

            setCurAdjustAndExtraType();
			showCurChart(mLineType);
		}
	}
	
	private void showKLine() {
		if (mLineType == KLINE_TYPE_DAILY) {
			mDailyKLineChart.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
		}else if (mLineType == KLINE_TYPE_WEEKLY) {
			mWeeklyKLineChart.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
		}else if (mLineType == KLINE_TYPE_MONTHLY) {
			mMonthlyKLineChart.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
		}
	}
	
	private void setDataAndShowKLine() {
        if (mDailyKLines == null || mDailyKLines.size() == 0){
            QLog.e(TAG,"setDataAndShowKLine:mDailyKLines == null || mDailyKLines.size() == 0");
            return;
        }

		if (getDailyDataStatus == KLINE_GETDATA_FINISH){
			showKLine();
			return;
		}

        mDailyKLineChart.setKLineData(mDailyKLines);
        mWeeklyKLineChart.setKLineData(StockKLine.changeDaily2Week(mDailyKLines));
        mMonthlyKLineChart.setKLineData(StockKLine.changeDaily2Month(mDailyKLines));

        StockKLine.calcAdjust(mDailyKLines);
        ArrayList<StockKLine> forwardList = StockKLine.getForwardList(mDailyKLines);
        ArrayList<StockKLine> backwardList = StockKLine.getBackwardList(mDailyKLines);

        mDailyKLineChart.setForwardData(forwardList);
        mWeeklyKLineChart.setForwardData(StockKLine.changeDaily2Week(forwardList));
        mMonthlyKLineChart.setForwardData(StockKLine.changeDaily2Month(forwardList));

        mDailyKLineChart.setBackwardData(backwardList);
        mWeeklyKLineChart.setBackwardData(StockKLine.changeDaily2Week(backwardList));
        mMonthlyKLineChart.setBackwardData(StockKLine.changeDaily2Month(backwardList));

        //是否显示
        showKLine();

        getDailyDataStatus = KLINE_GETDATA_FINISH;
	}

    /**
     * 通过StockKLine设置交易信息
     */
	private void setKlineInfo(StockKLine info){
        mKLineTitleView.setVisibility(View.VISIBLE);
        mNormalTitleView.setVisibility(View.GONE);

        mKTimeView.setText(TimeUtil.getMonthAndDay(info.getDataTime()));

        mKOpenView.setText(String.format("%.2f", info.getOpen()));
        mKHighView.setText(String.format("%.2f", info.getHigh()));
        mKLowView.setText(String.format("%.2f", info.getLow()));

        mKCloseView.setText(String.format("%.2f", info.getClose()));
        mKPercentView.setText(String.format("%.2f", (info.getPercent() * 100))+"%");

        if (info.getStockIsUp()){ //与昨收比涨跌
            mKCloseView.setTextColor(mContext.getResources().getColor(R.color.stock_rise));
            mKPercentView.setTextColor(mContext.getResources().getColor(R.color.stock_rise));
        }else{
            mKCloseView.setTextColor(mContext.getResources().getColor(R.color.stock_slumped));
            mKPercentView.setTextColor(mContext.getResources().getColor(R.color.stock_slumped));
        }
    }


	/**
	 * 通过StockMinute设置交易信息
     *
	 */
	private void setTradeInfo(StockMinutes info) {
		mMinTitleView.setVisibility(View.VISIBLE);
        mNormalTitleView.setVisibility(View.GONE);

		float now = info.getNowPrice();
		float yesterday = info.getYestodayPrice();
		
		
		float percent;
		int color;
        String sign;
		if (now > yesterday) {
			//涨
			percent = (now-yesterday)*100/yesterday;
			color = mContext.getResources().getColor(R.color.stock_rise);
            sign = "+";
		}else if (now < yesterday) {
			//跌
			percent = (yesterday-now)*100/yesterday;
			color = mContext.getResources().getColor(R.color.stock_slumped);
            sign = "-";
		}else {
			percent = 0;
			color = mContext.getResources().getColor(R.color.black);
            sign = "";
		}
		
		mMinPriceView.setText(String.format(Locale.getDefault(),"%.2f", now));
		mMinPercentView.setText(sign + String.format(Locale.getDefault(), "%.2f", percent) + "%");

        mMinPriceView.setTextColor(color);
        mMinPercentView.setTextColor(color);
		
		long totalVoume = info.getTradeCount()/100;
		String volumeString,timeString;
		if (totalVoume > 100000000L) {
			volumeString = String.format(Locale.getDefault(),"%.2f亿手", (double)totalVoume/100000000D);
		}else if (totalVoume > 10000) {
			volumeString = String.format(Locale.getDefault(),"%.2f万手", (double)totalVoume/10000D);
		}else {
			volumeString = totalVoume+"手";//"成交 "
		}
		timeString = TimeUtil.getHourAndMin(info.getDataTime());//"时间 "+
		
		mMinVolumeView.setText(volumeString);
		mMinTimeView.setText(timeString);

        mMinMaValueView.setText(String.format("%.2f",info.getMaValue()));
	}
	
	
	/**
	 * 设置默认交易信息
	 */
	private void setDefaultTradeInfo() {

		mPriceTextView.setText(mPriceString);
		mPercentTextView.setText(mPercentString);
		mPriceTextView.setTextColor(stockColor);
		mPercentTextView.setTextColor(stockColor);
		
		mVolumeTextView.setText(mVolumeString);
		mTimeTextView.setText(mTimeString);
	}

	/**
	 * 通过分时数据 初始化股票交易信息
	 */
	private void initDefaultInfoByMin(ArrayList<StockMinutes> data) {
        if (data == null || data.size() == 0){
            return;
        }

		StockMinutes info = data.get(data.size() - 1);//最后一笔
		float now = info.getNowPrice();
		float yesterday = info.getYestodayPrice();
		
		
		float percent;
        String sign;
		if (now > yesterday) {
			//涨
            sign = "+";
			percent = (now-yesterday)*100/yesterday;
			stockColor = mContext.getResources().getColor(R.color.stock_rise);
		}else if (now < yesterday) {
			//跌
            sign = "-";
			percent = (yesterday-now)*100/yesterday;
			stockColor = mContext.getResources().getColor(R.color.stock_slumped);
		}else {
			percent = 0;

            sign = "";
			stockColor = mContext.getResources().getColor(R.color.black);
		}
		mPercentString = sign + String.format(Locale.getDefault(),"%.2f", percent)+"%";
		mPriceString = String.format(Locale.getDefault(),"%.2f",now);

		mTimeString = TimeUtil.getTimeWithoutSec(info.getDataTime());
		setDefaultTradeInfo();
	}
	
	
	private void getData() {
		mProgressBar.setVisibility(View.VISIBLE);
        getMinuteData();
        getKLineAndShow();
        getHandicapData();
	}

    //获取盘口数据
    private void getHandicapData(){

        if (!mIsStock){
            return;
        }

        if (mStockState == StockTradeInfo.STATUS_OPEN_PREPARE
                || mStockState == StockTradeInfo.STATUS_STOP){
            //停牌 或 清数据时间
            return;
        }

        //判断状态
        mStockClient.getStockHandicap(mStockCode,new OnRequestListener(){

            @Override
            public void onDataFinish(Object object) {
                //保存到数据库
                StockMinuteTable table = new StockMinuteTable(mContext);
                table.saveHandicapData(mStockCode,object.toString());

                //获取数据
                setHandicapFromStockInfo(StockTradeInfo.resloveHandicapData((JSONObject) object));
            }

            @Override
            public void onError(int errorCode, String errorString) {

            }
        });

        //先从本地读取
        StockMinuteTable table = new StockMinuteTable(mContext);
        String jsonString = table.getHandicapData(mStockCode);
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                setHandicapFromStockInfo(StockTradeInfo.resloveHandicapData(new JSONObject(jsonString)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void setHandicapFromStockInfo(StockTradeInfo info){
        StockTradeInfo.BuyOrSellInfo[] buyInfo = info.getBuyInfo();
        StockTradeInfo.BuyOrSellInfo[] sellInfo = info.getSellInfo();
        if (buyInfo == null || sellInfo == null){
            return;
        }

        for(int i = 0; i < 5; i++) {
            mBuyInfoPriceTV[i].setRiseInfo(buyInfo[i].getRiseInfo());
            mBuyInfoPriceTV[i].setText(buyInfo[i].getPriceFormat());
            mBuyInfoCountTV[i].setText(buyInfo[i].getCountFormat());
            mSellInfoPriceTV[i].setRiseInfo(sellInfo[i].getRiseInfo());
            mSellInfoPriceTV[i].setText(sellInfo[i].getPriceFormat());
            mSellInfoCountTV[i].setText(sellInfo[i].getCountFormat());
        }
    }
	
	private void getMinuteData() {
        if (mIsStock){
            if (mStockState == StockTradeInfo.STATUS_OPEN_PREPARE
                    || mStockState == StockTradeInfo.STATUS_STOP){
                //停牌 或 清数据时间
                mMinuteLine.clearData(mCurPrice);

                if (mLineType == KLINE_TYPE_MIN) {
                    mProgressBar.setVisibility(View.GONE);
                    mMinuteLine.setVisibility(View.VISIBLE);
                    mHandicapView.setVisibility(View.VISIBLE);
                }

                mPercentString = "0.00%";
                mPriceString = String.format("%.2f",mCurPrice);
                stockColor = mContext.getResources().getColor(R.color.black);

                setDefaultTradeInfo();
                return;
            }
        }

		if (mFromGuoXin) {
//			mStockClient.requestNewMinuteData(new OnRequestListener() {
//				@Override
//				public void onDataFinish(Object object) {
//					if (mLineType == KLINE_TYPE_MIN) {
//						mMinuteLine.setVisibility(View.VISIBLE);
//						mProgressBar.setVisibility(View.GONE);
//
//						if (mIsStock){
//							mHandicapView.setVisibility(View.VISIBLE);
//						}
//					}
//
//					mMinutes = ((ArrayList<StockMinutes>)object);
//					if (mMinutes == null || mMinutes.size() == 0) {
//						//没有获取到数据
//						return;
//					}
//
//					mDataHandler.sendEmptyMessage(1);
//
//				}
//
//				@Override
//				public void onError(int errorCode, String errorString) {
//					if (mLineType == KLINE_TYPE_MIN) {
//						mMinuteLine.setVisibility(View.VISIBLE);
//						mProgressBar.setVisibility(View.GONE);
//
//						if (mIsStock){
//							mHandicapView.setVisibility(View.VISIBLE);
//						}
//					}
//				}
//			}, Constants.JIN_GUI_INFO_MINUTE.concat(mStockCode));
			if (mLineType == KLINE_TYPE_MIN) {
				mMinuteLine.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.GONE);

				if (mIsStock){
					mHandicapView.setVisibility(View.VISIBLE);
				}
			}
			mMinutes = mMinuteDataList;
			if (mMinutes == null || mMinutes.size() == 0) {
				//没有获取到数据
				return;
			}
			mDataHandler.sendEmptyMessage(1);
			return;
		}

		long lastTime = 0;//去掉毫秒
		//先从数据库中读取显示
		byte[] b = null;
		int type = StockClient.TYPE_STOCK;
		if (mIsStock) {
			StockMinuteTable table = new StockMinuteTable(mContext);
			b = table.getMintueData(mStockCode);
			table.close();
		}else {
			type = StockClient.TYPE_INDEX;
			IndexMinuteTable table = new IndexMinuteTable(mContext);
			b = table.getMintueData(mStockCode);
			table.close();
		}
		
		
		if (b != null && b.length > 0) {
			ArrayList<StockMinutes> dataArrayList = StockMinutes.resloveByteArray(b);
			if (dataArrayList != null && dataArrayList.size() > 0) {
				mMinuteLine.setMinuteData(dataArrayList);

				//修改为数据库的时间
				StockMinutes lastItem = dataArrayList.get(dataArrayList.size()-1);
				lastTime = lastItem.getDataTime();
				initDefaultInfoByMin(dataArrayList);

                if (mLineType == KLINE_TYPE_MIN) {
                    mProgressBar.setVisibility(View.GONE);
                    mMinuteLine.setVisibility(View.VISIBLE);

                    if (mIsStock){
                        mHandicapView.setVisibility(View.VISIBLE);
                    }
                }


			}
		}
		
		//再去读取网络的
		//设置最后一笔的时间点
		final long endTime = 99999999999999L;
		
		mStockClient.getMinuteInfoByLastTime(mStockCode, type,
				lastTime,
				new OnRequestListener() {
			
			@Override
			public void onError(int errorCode, String errorString) {
				if (mLineType == KLINE_TYPE_MIN) {
					mMinuteLine.setVisibility(View.VISIBLE);
					mProgressBar.setVisibility(View.GONE);

                    if (mIsStock){
                        mHandicapView.setVisibility(View.VISIBLE);
                    }
				}
				QLog.e(TAG, "errorCode:"+errorCode+",errorString:"+errorString);
			}
			
			@Override
			public void onDataFinish(Object object) {
				if (mLineType == KLINE_TYPE_MIN) {
					mMinuteLine.setVisibility(View.VISIBLE);
					mProgressBar.setVisibility(View.GONE);

                    if (mIsStock){
                        mHandicapView.setVisibility(View.VISIBLE);
                    }
				}

                mMinutes = StockMinutes.resloveMinutesData(object,mIsStock,endTime);
				if (mMinutes == null || mMinutes.size() == 0) {
					//没有获取到数据
					return;
				}
				
				mDataHandler.sendEmptyMessage(1);
				//保存到数据库
				if (mIsStock) {
					StockMinuteTable table = new StockMinuteTable(mContext);
					table.saveMintueData(mStockCode, StockMinutes.toByteArray(mMinutes), endTime);
					table.close();
				}else {
					IndexMinuteTable table = new IndexMinuteTable(mContext);
					table.saveMintueData(mStockCode, StockMinutes.toByteArray(mMinutes), endTime);
					table.close();
				}
			}
		});
	}
	
	
	/**
	 * 获取K线
	 */
	private void getKLineAndShow() {
		if (getDailyDataStatus == KLINE_GETDATA_FINISH) {
            mDataHandler.sendEmptyMessage(0);
			return;
		}else if (getDailyDataStatus == KLINE_GETDATA_LOADING) {
			return;
		}
		
		getDailyDataStatus = KLINE_GETDATA_LOADING;

		if (mFromGuoXin) {
			if (mKLineDataList != null && mKLineDataList.size() > 0) {
				mDailyKLines = mKLineDataList;
				mDataHandler.sendEmptyMessage(0);
			} else {
				StringBuilder s = new StringBuilder(Constants.MY_DATA_URL);
				s.append(mKLineTag).append("&code=").append(mStockCode);
				mStockClient.requestNewKLineData(new OnRequestListener() {
					@Override
					public void onDataFinish(Object object) {
						ArrayList<StockKLine> dataArrayList = new ArrayList<StockKLine>();
						dataArrayList.addAll((ArrayList<StockKLine>)object);
						if (dataArrayList == null || dataArrayList.size() == 0) {
							//没有获取到K线数据
							mDataHandler.sendEmptyMessage(0);
							return;
						}
						if (mDailyKLines != null) {
							mDailyKLines = StockKLine.addKLine(mDailyKLines, dataArrayList);
						} else {
							mDailyKLines = dataArrayList;
						}
						mDataHandler.sendEmptyMessage(0);
//                    if (mDailyKLines != null) {
//                        mDailyKLines = StockKLine.addKLine(mDailyKLines, dataArrayList);
//                    } else {
//                        mDailyKLines = dataArrayList;
//                    }
					}

					@Override
					public void onError(int errorCode, String errorString) {
						mDataHandler.sendEmptyMessage(0);
					}
				}, s.toString());
			}
			return;
		}
		
		long startTime = 1;
		byte[] b = null;
		int type = StockClient.TYPE_STOCK;
		if (!mIsStock) {
			type = StockClient.TYPE_INDEX;
			IndexKLineTable mKLineTable = new IndexKLineTable(mContext);
			b = mKLineTable.getKLineData(mStockCode);	
		}else {
			StockKLineTable mKLineTable = new StockKLineTable(mContext);
			b = mKLineTable.getKLineData(mStockCode);
		}
		
		if (b != null && b.length > 0) {
			mDailyKLines = StockKLine.resloveKLineFromSql(b, mContext);
			if (mDailyKLines != null && mDailyKLines.size() > 0) {
				startTime = mDailyKLines.get(mDailyKLines.size() - 1).getDataTime();
				mDataHandler.sendEmptyMessage(0);
			}
		}
		
		//获取新的数据
		long endTime = 32500886400000L;//2999/11/30 0:0:0

		//获取新的数据
		mStockClient.getKlineInfo(mStockCode, StockClient.KLINE_TYPE_DAILY, type, startTime, endTime, new OnRequestListener() {

			@Override
			public void onError(int errorCode, String errorString) {
                mDataHandler.sendEmptyMessage(0);
				QLog.e(TAG, "errorCode:"+errorCode+",errorString:"+errorString);
			}

			@Override
			public void onDataFinish(Object object) {
				ArrayList<StockKLine> netKLines = StockKLine.resolveKLineZip(object, mContext);

				if (netKLines == null || netKLines.size() == 0) {
					//没有获取到K线数据
                    mDataHandler.sendEmptyMessage(0);
					return;
				}

				if (mDailyKLines == null) {
					mDailyKLines = netKLines;
				}else {
					mDailyKLines = StockKLine.addKLine(mDailyKLines, netKLines);
				}


				//
				byte[] data = StockKLine.getKLineBytes(mDailyKLines);

				//保存起来
				if (!mIsStock) {
					IndexKLineTable mKLineTable = new IndexKLineTable(mContext);
					mKLineTable.saveKLineData(mStockCode, data, mDailyKLines.get(mDailyKLines.size()-1).getDataTime());
				}else {
					StockKLineTable mKLineTable = new StockKLineTable(mContext);
					mKLineTable.saveKLineData(mStockCode, data, mDailyKLines.get(mDailyKLines.size()-1).getDataTime());
				}

				mDataHandler.sendEmptyMessage(0);
			}
		});
		
		
	}
	
	
	static class DataHandler extends Handler {
		WeakReference<StockChartDetailActivity> mActivityReference;
		
		public DataHandler(StockChartDetailActivity activity) {
			super();
			mActivityReference = new WeakReference<StockChartDetailActivity>(activity);
		}



		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			StockChartDetailActivity activity = mActivityReference.get();
			if (activity == null) {
				return;
			}
            if (msg.what == 0){
//                activity.initDefaultInfoByKLine(activity.mDailyKLines);
                activity.setDataAndShowKLine();
            }else {
                activity.initDefaultInfoByMin(activity.mMinutes);
                activity.mMinuteLine.setMinuteData(activity.mMinutes);

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
	
}

