package com.zlf.appmaster.stockindustry;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.xlistview.XListView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.stocktrade.StockTradeDetailActivity;
import com.zlf.appmaster.utils.LiveRecordingUtil;
import com.zlf.appmaster.utils.Utils;

import java.util.ArrayList;

public class IndustryStockListActivity extends ListActivity {
	public static final String INTENT_FLAG_INDUSTRYID = "industry_id";
	public static final String INTENT_FLAG_INDUSTRYNAME = "industry_name";
	public static final String INTENT_FLAG_DISPLAY_TYPE = "industry_display_type";
	private static final String TAG = "IndustryStockListActivity";
	
	private String mIndustryId;
	private String mIndustryName;
	private int mType;
	private TextView mTitleTV;
	
	private StockClient mStockClient;
	
	private ProgressBar mProgressBar;
	
	private ArrayList<StockTradeInfo> mStockArray;
	private StockTradeInfoListAdapter mStockListAdapter;
	private XListView mListView;
	private LiveRecordingUtil mLiveRecordingUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_industry_stocklist);

		mLiveRecordingUtil = LiveRecordingUtil.getInstance();

		initViews();
		
		if (Utils.GetNetWorkStatus(this)) {
			mProgressBar.setVisibility(View.VISIBLE);
			requestData();
		}
		else {
			Toast.makeText(this, getResources().getString(R.string.network_unconnected),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void initViews() {
		mStockClient = new StockClient(this);
		Intent getIntent = getIntent();
		mIndustryId = getIntent.getStringExtra(INTENT_FLAG_INDUSTRYID);
		mIndustryName = getIntent.getStringExtra(INTENT_FLAG_INDUSTRYNAME);
		mType = getIntent.getIntExtra(INTENT_FLAG_DISPLAY_TYPE, 1);
		mTitleTV = (TextView) findViewById(R.id.title);
		mTitleTV.setText(mIndustryName);
		
		mProgressBar = (ProgressBar) findViewById(R.id.content_loading);
		
		mListView = (XListView) this.getListView();
		mListView.setPullLoadEnable(false); // 关闭下拉加载
		mListView.setPullRefreshEnable(false);//关闭上拉

		if(mLiveRecordingUtil.isLiveRecording() ){
			findViewById(R.id.live_space).setVisibility(View.VISIBLE);
		}
	}
	
	private void updateViews() {
		
		setListAdapter(mStockListAdapter);

	}
	
	private void requestData(){
		mStockClient.requestIndustryPopularStock(mIndustryId, mType, new OnRequestListener() {
			
			@Override
			public void onError(int errorCode, String errorString) {
				// TODO Auto-generated method stub
				mProgressBar.setVisibility(View.GONE);
			}
			
			@Override
			public void onDataFinish(Object object) {
				// TODO Auto-generated method stub
				mStockArray = (ArrayList<StockTradeInfo>) object;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mProgressBar.setVisibility(View.GONE);
						mStockListAdapter = new  StockTradeInfoListAdapter(IndustryStockListActivity.this, mStockArray);
						updateViews();
					}
				});

			}
		});
	}
	
	
	public void onBack(View view){
		finish();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);

//		if(!mLiveRecordingUtil.isLiveRecording()) {
			StockTradeInfo stockTradeInfo = (StockTradeInfo) mStockListAdapter.getItem(position - 1);
			Intent intent = new Intent(IndustryStockListActivity.this, StockTradeDetailActivity.class);
			intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKNAME, stockTradeInfo.getName());
			intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKCODE, stockTradeInfo.getCode());
			startActivity(intent);
//		}else{
//			StockTradeInfo stockTradeInfo = (StockTradeInfo) mStockListAdapter.getItem(position - 1);
//			Intent intent = new Intent(IndustryStockListActivity.this, LiveAnchorLectureActivity.class);
//			mLiveRecordingUtil.setStockName(stockTradeInfo.getName());
//			mLiveRecordingUtil.setStockCode(stockTradeInfo.getName());
//			mLiveRecordingUtil.setStock(true);
//			intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
//		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		MobclickAgent.onResume(this);
//		LiveOperationControlAgent.getInstance().onResume(this);

	}

	@Override
	protected void onPause() {
		super.onPause();
//		MobclickAgent.onPause(this);
//		LiveOperationControlAgent.getInstance().onPause(this);
	}
}
