package com.zlf.appmaster.stockIndex;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.StockKLine;
import com.zlf.appmaster.chartview.bean.StockMinutes;
import com.zlf.appmaster.chartview.tool.OnGetDailyKLinesListener;
import com.zlf.appmaster.chartview.tool.OnGetHandicapListener;
import com.zlf.appmaster.chartview.tool.OnGetMinuteDataListener;
import com.zlf.appmaster.chartview.tool.StockChartTool;
import com.zlf.appmaster.chartview.view.OnClickChartListener;
import com.zlf.appmaster.chartview.view.StockChartView;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.db.stock.IndexKLineTable;
import com.zlf.appmaster.db.stock.IndexMinuteTable;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.stocktrade.StockChartDetailActivity;
import com.zlf.appmaster.ui.stock.IndexBaseInfoView;
import com.zlf.appmaster.ui.stock.StockTextView;
import com.zlf.appmaster.ui.stock.TabButton;
import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.utils.QToast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StockIndexDetailListAdapter extends BaseAdapter {
	/**
	 * list条目分类
	 */
	public static final int ITEM_TYPE_TRADE_INFO	= 0;	// 交易信息
	public static final int ITEM_TYPE_KLINE_AREA 	= 1;	// K线区域
	public static final int ITEM_TYPE_STOCK_TITLE 	= 2;	// 标题
	public static final int ITEM_TYPE_STOCK_INFO 	= 3;	// 股票信息

    private Context mContext;
    private StockIndex mStockIndex;
    private List<StockTradeInfo> mListData;
    private LayoutInflater mInflater;

    private StockClient mStockClient;

    // K线
    private StockChartView mStockChartView;
    private ArrayList<StockKLine> mDailyKLines;

    private String mStockIndexID;
    private String mStockIndexName;

    private boolean mAdapterNotify;  // 涨跌是否正在刷新

    private boolean mFromGuoXin;
    private ArrayList<StockMinutes> mMinuteDataList;
    private String mTabMinuteTag;
    private String mTabKLineTag;

    private final static int REFRESH_KLINE_DATA = 1;
    public Handler mHandler = new Handler(){

        @Override
        public  void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what){
                case REFRESH_KLINE_DATA:
                    if (null != mStockChartView){
                        mStockChartView.refreshData();
                    }
                    break;
                default:
                    break;
            }
        }

    };

    public StockIndexDetailListAdapter(Context context, StockClient stockClient, StockIndex index,
                                       List<StockTradeInfo> data, boolean fromGuoXin, String tabMinuteTag, String tabKLineTag) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        mListData = data;
        mStockIndex = index;

        mStockIndexID = mStockIndex.getCode();
        mStockIndexName = mStockIndex.getName();

        mStockClient = stockClient;

        mFromGuoXin = fromGuoXin;

        mTabMinuteTag = tabMinuteTag;
        mTabKLineTag = tabKLineTag;

        // 初始化K线view
        mStockChartView = new StockChartView(context);
        initStockChart(mStockChartView);
    }
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
        int dataLen = mListData.size();

		return 3 + dataLen;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
        if (position > 2){
            return mListData.get(position - 3);
        }
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return ITEM_TYPE_TRADE_INFO;
        else if (position == 1)
            return ITEM_TYPE_KLINE_AREA;
        else if (position == 2)
            return ITEM_TYPE_STOCK_TITLE;
        else
            return ITEM_TYPE_STOCK_INFO;

    }

    @Override
    public boolean isEnabled(int position) {
        if (position < 3){
            return false;
        }
        return super.isEnabled(position);
    }

    public void refreshKLineView(){
        mHandler.sendEmptyMessageDelayed(REFRESH_KLINE_DATA, 1000);
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
        int viewType = getItemViewType(position);
        switch (viewType){
            case ITEM_TYPE_TRADE_INFO:
                if (null == convertView) {
                    convertView = new IndexBaseInfoView(mContext);
                }
                ((IndexBaseInfoView)convertView).updateViews(mStockIndex);

                break;
            case ITEM_TYPE_KLINE_AREA:
                if (null == convertView) {
                    convertView = mStockChartView;
                }
                break;
            case ITEM_TYPE_STOCK_TITLE:{
                ViewStockTitleHolder viewHolder = null;
                if (null == convertView){
                    viewHolder = new ViewStockTitleHolder();
                    convertView = mInflater.inflate(R.layout.list_item_stock_index_detail_more_info_title,null);
                    viewHolder.btnLedUp = (TabButton)convertView.findViewById(R.id.stock_led_up_list);
                    viewHolder.btnLedDown = (TabButton)convertView.findViewById(R.id.stock_led_down_list);
                    viewHolder.btnLedUp.setSelected(true);
                    viewHolder.btnLedUp.setEnabled(false);
                    viewHolder.btnLedUp.setOnClickListener(new OnTabChangeListener(viewHolder));
                    viewHolder.btnLedDown.setOnClickListener(new OnTabChangeListener(viewHolder));
                }
            }
                break;
            case ITEM_TYPE_STOCK_INFO:{
                ViewHolder viewHolder = null;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_stock_favorite, null);
                    viewHolder = new ViewHolder();
                    viewHolder.StockName = (TextView) convertView.findViewById(R.id.stock_name);
                    viewHolder.StockCode = (TextView) convertView.findViewById(R.id.stock_code);
                    viewHolder.StcokPrice = (StockTextView) convertView.findViewById(R.id.stock_price);
                    viewHolder.StockPercent = (StockTextView) convertView.findViewById(R.id.stock_percent);
                    viewHolder.StockSuspendedPrompt = (TextView) convertView.findViewById(R.id.stock_trade_suspended);
                    convertView.setTag(viewHolder);
                }
                else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                StockTradeInfo stockTradeInfo = (StockTradeInfo)getItem(position);
                if (null != stockTradeInfo){
                    viewHolder.StockName.setText(stockTradeInfo.getName());
                    viewHolder.StockCode.setText(stockTradeInfo.getCode());
                    if(stockTradeInfo.isStockSuspended()){

                        viewHolder.StockPercent.setVisibility(View.GONE);
                        viewHolder.StockSuspendedPrompt.setVisibility(View.VISIBLE);

                        viewHolder.StcokPrice.setTextColor(Color.BLACK);
                    }
                    else{
                        viewHolder.StockPercent.setVisibility(View.VISIBLE);
                        viewHolder.StockSuspendedPrompt.setVisibility(View.GONE);

                        viewHolder.StockPercent.setRiseInfo(stockTradeInfo.getRiseInfo());
                        viewHolder.StcokPrice.setRiseInfo(stockTradeInfo.getRiseInfo());
                    }

                    viewHolder.StcokPrice.setText(stockTradeInfo.getCurPriceFormat());
                    viewHolder.StockPercent.setText(stockTradeInfo.getCurPercentFormat());
                }
            }
            break;

        }

        return convertView;
	}





    // ------ 涨幅榜、跌幅榜 ----------//
    private class ViewStockTitleHolder{
        TabButton btnLedUp, btnLedDown;
    }


    public interface  OnTabChange{
        void onChange(int type);    // type 为 0领涨 1领跌
    }
    private OnTabChange mOnTabChange;
    public void setOnTabChange(OnTabChange onTabChange){
        mOnTabChange = onTabChange;
    }
    private class OnTabChangeListener implements View.OnClickListener{
        ViewStockTitleHolder mViewHolder;
        public  OnTabChangeListener(ViewStockTitleHolder viewHolder){
            mViewHolder = viewHolder;
        }
        @Override
        public void onClick(View view) {
            QLog.e("onChange", mAdapterNotify + ": mAdapterNotify");
            if (mAdapterNotify) {
                QToast.show(mContext, "当前正在刷新,sha", Toast.LENGTH_SHORT);
                return;
            }
            // 清除状态
            mViewHolder.btnLedDown.setEnabled(true);
            mViewHolder.btnLedDown.setSelected(false);
            mViewHolder.btnLedUp.setEnabled(true);
            mViewHolder.btnLedUp.setSelected(false);

            // 设置选中
            view.setEnabled(false);
            view.setSelected(true);

            int viewId = view.getId();
            int type = 1;
            if (viewId == R.id.stock_led_up_list){  // 领涨
                type = 0;
            }

            if (mOnTabChange != null)
                mOnTabChange.onChange(type);

        }
    }

    // ------ 股票项 ----------//
    class ViewHolder {
        //StockImageView StockIcon;
        TextView StockName, StockCode;
        StockTextView StcokPrice, StockPercent;
        TextView StockSuspendedPrompt;
        //	Button deleteBtn;
    }





    // ------ K线模块 ----------//
    private void initStockChart(StockChartView stockChartView) {
        stockChartView.setOnClickChartListener(new OnClickChartListener() {

            @Override
            public void onClick(int type) {
                enterChartDetail(type);
            }
        });

        StockChartTool sTool = new StockChartTool() {

            @Override
            public void getMinuteData(final OnGetMinuteDataListener listener) {
                getStockMinuteData(listener, mFromGuoXin);
            }

            @Override
            public void getDailyKLines(final OnGetDailyKLinesListener listener) {
                getStockDailyKLines(listener, mFromGuoXin);
            }

            @Override
            public void getHandicapData(OnGetHandicapListener listener) {

            }
        };

        stockChartView.initData(mStockIndexID, mStockIndexName, false, sTool);
    }

    /**
     * 进入分时线、K线大图
     * @param type
     */
    private void enterChartDetail(int type) {
        //
        if ((type == 0 && mMinuteDataList != null && mMinuteDataList.size() > 0)||
                (type != 0 && mDailyKLines != null && mDailyKLines.size() > 0)) {
            Intent intent = new Intent(mContext, StockChartDetailActivity.class);
            intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_LINE_TYPE, type);
            intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_IS_STOCK, false);
            intent.putExtra(StockChartDetailActivity.INTENT_FROM_GUOXIN, mFromGuoXin);
            intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_STOCK_ID, mStockIndexID);
            intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_STOCK_NAME, mStockIndexName);
            intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_MINUTE_DATA_LIST, (Serializable) mMinuteDataList);
            intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_KLINE_DATA_LIST, (Serializable) mDailyKLines);
            intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_KLINE_DATA_LIST_TAG, mTabKLineTag);
            mContext.startActivity(intent);
        }
    }

    /**
     * 获取分时数据
     * @param listener
     */
    private void getStockMinuteData(final OnGetMinuteDataListener listener, boolean fromGuoXin) {
        if (fromGuoXin) {
            StringBuilder s = new StringBuilder(Constants.MY_DATA_URL);
            s.append(mTabMinuteTag).append("&code=").append(mStockIndexID);
            mStockClient.requestNewMinuteData(new OnRequestListener() {
                @Override
                public void onDataFinish(Object object) {
                    mMinuteDataList = new ArrayList<StockMinutes>();
                    if (object != null && ((ArrayList<StockMinutes>)object).size() > 0) {
                        mMinuteDataList.addAll((ArrayList<StockMinutes>) object);
                    }
                    if (mMinuteDataList == null || mMinuteDataList.size() == 0) {
                        //没有获取到数据
                        listener.onError();
                        return;
                    }
                    listener.onDataFinish(mMinuteDataList);
                }

                @Override
                public void onError(int errorCode, String errorString) {

                }
            }, s.toString());
        } else {
            long lastTime = 0;//去掉毫秒
            //先从数据库中读取显示
            IndexMinuteTable table = new IndexMinuteTable(mContext);//用指数的！！
            byte[] b = table.getMintueData(mStockIndexID);
            if (b != null && b.length > 0) {
                ArrayList<StockMinutes> dataArrayList = StockMinutes.resloveByteArray(b);
                if (dataArrayList != null && dataArrayList.size() > 0) {
                    listener.onDataFinish(dataArrayList);
                    //修改为数据库的时间
                    lastTime = dataArrayList.get(dataArrayList.size() - 1).getDataTime();
                }
            }
            table.close();

            int type = StockClient.TYPE_INDEX;
            //再去读取网络的
            //设置最后一笔的时间点
            final long endTime = 99999999999999L;

            mStockClient.getMinuteInfoByLastTime(mStockIndexID, type,
                    lastTime,
                    new OnRequestListener() {

                        @Override
                        public void onError(int errorCode, String errorString) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onDataFinish(Object object) {

                            ArrayList<StockMinutes> dataArrayList = StockMinutes.resloveMinutesData(object, false, endTime);
                            if (dataArrayList == null || dataArrayList.size() == 0) {
                                //没有获取到数据
                                listener.onError();
                                return;
                            }
                            listener.onDataFinish(dataArrayList);

                            //保存到数据库
                            IndexMinuteTable table = new IndexMinuteTable(mContext);
                            table.saveMintueData(mStockIndexID, StockMinutes.toByteArray(dataArrayList), endTime);
                            table.close();
                        }
                    });
        }

    }

    /**
     * 获取日K数据
     * @param listener
     */
    public void getStockDailyKLines(final OnGetDailyKLinesListener listener, boolean fromGuoXin) {
        if (mFromGuoXin) {
            StringBuilder s = new StringBuilder(Constants.MY_DATA_URL);
            s.append(mTabKLineTag).append("&code=").append(mStockIndexID);
            mStockClient.requestNewKLineData(new OnRequestListener() {
                @Override
                public void onDataFinish(Object object) {
                    ArrayList<StockKLine> dataArrayList = new ArrayList<StockKLine>();
                    if (object != null && ((ArrayList<StockKLine>)object).size() > 0) {
                        dataArrayList.addAll((ArrayList<StockKLine>) object);
                    }
                    if (dataArrayList == null || dataArrayList.size() == 0) {
                        //没有获取到K线数据
                        listener.onError();
                        return;
                    }
                    if (mDailyKLines != null) {
                        mDailyKLines = StockKLine.addKLine(mDailyKLines, dataArrayList);
                    } else {
                        mDailyKLines = dataArrayList;
                    }
                    listener.onDataFinish(mDailyKLines);
//                    if (mDailyKLines != null) {
//                        mDailyKLines = StockKLine.addKLine(mDailyKLines, dataArrayList);
//                    } else {
//                        mDailyKLines = dataArrayList;
//                    }
                }

                @Override
                public void onError(int errorCode, String errorString) {

                }
            }, s.toString());
        } else {
            //获取下起始时间点 大于1则说明有数据，可直接显示
            long startTime = 1;

            //从数据库中获取
            IndexKLineTable mKLineTable = new IndexKLineTable(mContext);
            byte[] b = mKLineTable.getKLineData(mStockIndexID);
            if (b != null && b.length > 0) {
                mDailyKLines = StockKLine.resloveKLineFromSql(b, mContext);
                startTime = mDailyKLines.get(mDailyKLines.size() - 1).getDataTime();//最后一条时间取起

                if (listener != null) {
                    listener.onDataFinish(mDailyKLines);
                }
            }

            //获取新的数据
            //获取下起始时间点 大于1则说明有数据，可直接显示
            long endTime = 32500886400000L;//2999/11/30 0:0:0

            int type = StockClient.TYPE_INDEX;

            //获取新的数据
            mStockClient.getKlineInfo(mStockIndexID, StockClient.KLINE_TYPE_DAILY, type, startTime, endTime, new OnRequestListener() {

                @Override
                public void onError(int errorCode, String errorString) {
                    listener.onError();
                }

                @Override
                public void onDataFinish(Object object) {

                    ArrayList<StockKLine> dataLines = StockKLine.resolveKLineZip(object, mContext);

                    if (dataLines == null || dataLines.size() == 0) {
                        //没有获取到K线数据
                        listener.onDataFinish(mDailyKLines);
                        return;
                    }
                    if (mDailyKLines != null) {
                        mDailyKLines = StockKLine.addKLine(mDailyKLines, dataLines);
                    } else {
                        mDailyKLines = dataLines;
                    }

                    listener.onDataFinish(mDailyKLines);

                    //要把之前的加进去

                    byte[] data = StockKLine.getKLineBytes(mDailyKLines);

                    //保存起来
                    IndexKLineTable mKLineTable = new IndexKLineTable(mContext);
                    mKLineTable.saveKLineData(mStockIndexID, data, mDailyKLines.get(mDailyKLines.size() - 1).getDataTime());
                }
            });
        }

    }

    /** 防止刷新途中再次点击切换 */
    public void setAdapterNotify(boolean b) {
        this.mAdapterNotify = b;
    }
}
