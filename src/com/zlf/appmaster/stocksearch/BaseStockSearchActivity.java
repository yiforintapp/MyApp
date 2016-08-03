package com.zlf.appmaster.stocksearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.zlf.appmaster.R;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockMarketClient;
import com.zlf.appmaster.db.stock.StockSearchHistoryTable;
import com.zlf.appmaster.db.stock.StockSearchTool;
import com.zlf.appmaster.db.stock.StockTable;
import com.zlf.appmaster.model.search.StockSearchItem;
import com.zlf.appmaster.model.stock.StockItem;
import com.zlf.appmaster.stocksearch.StockSearchAdapter.OnAddListener;
import com.zlf.appmaster.utils.KeyboardUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class BaseStockSearchActivity extends Activity {

	public interface OnStockItemClickListener{
		void onClick(int position, String stockCode, String stockName, int codeType);
	}

	private final static String TAG = "BaseStockSearchActivity";

	protected StockSearchAdapter mlistAdapter;
	//private TextView mTitle;
	private EditText mEditText;
    private View mBtnCancel;
    private KeyboardUtil mKeyboardUtil;
    protected ProgressBar mProgressBar;

	//protected Cursor mCursor;
	protected ListView mListView;
    //protected View mListFootView;
	protected StockSearchTool mStockTable;

    // 股票item
    private List<StockSearchItem> mData = null;
    // 搜索历史
    private List<StockSearchItem> mHistoryData = null;

    // 是否处于历史搜索界面
    private boolean mIsHistoryList = true;

	private StockMarketClient mStockMarketClient;
	private OnStockItemClickListener mOnStockItemClickListener;
	
	private static final int MSG_LOAD_ALL_ITEMS = 1;
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_LOAD_ALL_ITEMS:
				initCacheDataToDB();
				break;
			default:
				break;
			}
			
			super.handleMessage(msg);
		}
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_search);

//        AndroidBug5497Workaround.assistActivity(this, R.id.search_view);

        mStockMarketClient = StockMarketClient.getInstance(this);

		initViews();
		
		//mProgressBar.setVisibility(View.VISIBLE);
		mHandler.sendEmptyMessageDelayed(MSG_LOAD_ALL_ITEMS, 50);


	}
	
	protected void initViews(){
		mStockTable = new StockSearchTool(this);
		mEditText = (EditText)findViewById(R.id.search_stock);
		mEditText.addTextChangedListener(filterTextWatcher);

        mBtnCancel = findViewById(R.id.btn_search_cancel);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBack(view);
            }
        });

        // 自定义键盘
        mKeyboardUtil = new KeyboardUtil(this, mEditText);

        mKeyboardUtil.hideSystemKeyBoard();
        mKeyboardUtil.showKeyboard();
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mKeyboardUtil.hideSystemKeyBoard();
                mKeyboardUtil.showKeyboard();

                return false;
            }
        });

		//mTitle = (TextView)findViewById(R.id.title);
		
		mProgressBar = (ProgressBar)findViewById(R.id.content_loading);
		mListView = (ListView) findViewById(R.id.all_stock_list);
        mListView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mKeyboardUtil.hideSystemKeyBoard();
                mKeyboardUtil.hideKeyboard();
                return false;
            }
        });
		mListView.setOnItemClickListener(new OnSearchListItemClick());

        // ----- 股票历史列表 -------//
        StockSearchHistoryTable cacheTable = new StockSearchHistoryTable(this);
        mHistoryData = cacheTable.getAllItem();
        boolean isShowHottestItem = false;
        if (mHistoryData.isEmpty()){// 如果没有历史搜索的，显示最热个股
            isShowHottestItem = true;
        }
        mlistAdapter = new StockSearchHistoryAdapter(this, mHistoryData, isShowHottestItem);

        // 加载最热个股
        if (mHistoryData.isEmpty()) {
            mStockMarketClient.requestHottestSearch(0, 3, new OnRequestListener() {
                @Override
                public void onDataFinish(Object object) {
                    JSONObject response = (JSONObject)object;
                    if (null != response){
                        try {
                            List<StockSearchItem> items = StockSearchItem.resolveByStockHottest(response);
                            mHistoryData.clear();
                            mHistoryData.addAll(items);
                            mlistAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }

                @Override
                public void onError(int errorCode, String errorString) {

                }
            });
        }

        mIsHistoryList = true;
        //initHistoryFootView();
        mListView.setAdapter(mlistAdapter);

        if (mOnListDataChangeListener != null){
            mOnListDataChangeListener.onChange();
        }

        //和直播间备选股用的同一个布局，所以这里要隐藏
        findViewById(R.id.live_space).setVisibility(View.GONE);
	}

    //delete by liuhm,需求当中去掉该功能
   /* private void initHistoryFootView(){
        mListFootView = getLayoutInflater().inflate(R.layout.list_item_stock_search_add_by_topic, null);
        Button btn = (Button)mListFootView.findViewById(R.id.btn_add_by_topic);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BaseStockSearchActivity.this, CombinationTopicSelectedActivity.class);
                intent.putExtra(CombinationTopicSelectedActivity.INTENT_FLAG_STOCK_FAVORITE_ADD, true);
                startActivity(intent);
            }
        });
        mListView.addFooterView(mListFootView);
    }*/
	
	private TextWatcher filterTextWatcher = new TextWatcher(){

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
			// TODO Auto-generated method stub
			filterData(s.toString());
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}
		
	};
	
	public void onBack(View view){
		setResult(RESULT_CANCELED);
		finish();
	}

	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mStockTable.close();

		super.onDestroy();
	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * @param filterStr
	 */
	private void filterData(String filterStr){
		//Log.i(TAG, "filterData:"+filterStr);
        if (null == mData){
            mData = mStockTable.getAllSearchItems(filterStr);
           // mListView.removeFooterView(mListFootView);
            mlistAdapter = new StockSearchAdapter(this, mData);
            mListView.setAdapter(mlistAdapter);
            mIsHistoryList = false;


            if (mOnListDataChangeListener != null){
                mOnListDataChangeListener.onChange();
            }
        }
        else{
            mData.clear();
            mData.addAll(mStockTable.getAllSearchItems(filterStr));
            mlistAdapter.notifyDataSetChanged();
        }

	}

    /**
     * 数据改变回调
     */
    public interface OnListDataChangeListener{
        void onChange();
    }
    private OnListDataChangeListener mOnListDataChangeListener;
    public void setOnListDataChangeListener(OnListDataChangeListener listener){
        mOnListDataChangeListener = listener;
    }
		
	/**
	 * 点击listItem
	 */
	private class OnSearchListItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id)  {
			// TODO Auto-generated method stub


			StockSearchItem item = (StockSearchItem)mlistAdapter.getItem(position);
            if (null != item){
                String stockCode = item.getStockCode();
                String stockName = item.getStockName();
                int  codeType = item.getType();
                if(null == mOnStockItemClickListener){
                    if (codeType == StockItem.CODE_TYPE_STOCK){
//                        Intent intent = new Intent(BaseStockSearchActivity.this, StockTradeDetailActivity.class);
//                        intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKNAME, stockName);
//                        intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_STOCKCODE, stockCode);
//                        intent.putExtra(StockTradeDetailActivity.INTENT_FLAG_SEARCH_INDEX, position);
//                        startActivityForResult(intent,0);
                    }
                    else if (codeType == StockItem.CODE_TYPE_INDEX){
//                        Intent intent = new Intent(BaseStockSearchActivity.this, StockIndexDetailActivity.class);
//                        intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_INDEXCODE, stockCode);
//                        intent.putExtra(StockIndexDetailActivity.INTENT_FLAG_INDEXNAME, stockName);
//                        startActivity(intent);
                    }

                }
                else{
                    mOnStockItemClickListener.onClick(position, stockCode, stockName, codeType);
                }

                // 添加到历史搜索
                StockSearchHistoryTable cacheTable = new StockSearchHistoryTable(BaseStockSearchActivity.this);
                cacheTable.saveItem(stockCode,stockName, codeType);
            }


		}
	}

    protected void onAddItemClick(int position){
        StockSearchItem item = null;
        if (mIsHistoryList){
            item = mHistoryData.get(position - 1); // 有一行头
            item.setFavorite(true);
        }
        else {
            item = mData.get(position);
            item.setFavorite(true);
        }

        if (null != item){
            // 添加到历史搜索
            StockSearchHistoryTable cacheTable = new StockSearchHistoryTable(BaseStockSearchActivity.this);
            cacheTable.saveItem(item.getStockCode(), item.getStockName(), item.getType());

            mlistAdapter.notifyDataSetChanged();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
//        if(resultCode == QConstants.RESULTCODE_UPDATE_STOCKFAVORITE){
//            int position = data.getIntExtra(StockTradeDetailActivity.INTENT_FLAG_SEARCH_INDEX, -1);
//            boolean isFavorite = data.getBooleanExtra(StockTradeDetailActivity.INTENT_FLAG_IS_FAVORITE, false);
//            if (position != -1){
//               // QLog.i(TAG,"接收自选改变:"+position + "isFavorite:"+isFavorite);
//                StockSearchItem item = null;
//                if (mIsHistoryList){
//                    item = mHistoryData.get(position - 1); // 有一行头
//                    item.setFavorite(isFavorite);
//                }
//                else {
//                    item = mData.get(position);
//                    item.setFavorite(isFavorite);
//                }
//                mlistAdapter.notifyDataSetChanged();
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
    }
	
	
	
	/**
	 * 加载预置数据
	 * 1.数据不存在则从缓存中写到数据库中
	 * 2.若缓存中没有，则从assert中读取一份写到数据库中。
	 */
	private void initCacheDataToDB(){

		StockTable.loadPreInitData(this);

		//mProgressBar.setVisibility(View.GONE);
	}
	
	/**
	 * 设置搜索adpter的类型
	 * @param adapterType 参见StockSearchAdapter中的定义
	 */
	public void setSearchAdapterType(int adapterType){
		mlistAdapter.setAdapterType(adapterType);
	}

	public void setStockAddListener(OnAddListener stockAddListener) {
		mlistAdapter.setOnAddListener(stockAddListener);
	}

	
	/**
	 * 设置股票点击listener
	 * @param mOnStockItemClickListener
	 */
	public void setOnStockItemClickListener(OnStockItemClickListener mOnStockItemClickListener) {
		this.mOnStockItemClickListener = mOnStockItemClickListener;
	}
	
	
	public void setTitle(String title){
		//mTitle.setText(title);
	}
	
}
