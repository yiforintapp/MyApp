package com.zlf.appmaster.stocktrade;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.iqiniu.qiniu.bean.StockKLine;
import com.iqiniu.qiniu.bean.StockMinutes;
import com.iqiniu.qiniu.tool.OnGetDailyKLinesListener;
import com.iqiniu.qiniu.tool.OnGetHandicapListener;
import com.iqiniu.qiniu.tool.OnGetMinuteDataListener;
import com.iqiniu.qiniu.tool.StockChartTool;
import com.iqiniu.qiniu.view.OnClickChartListener;
import com.iqiniu.qiniu.view.StockChartView;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.db.stock.StockKLineTable;
import com.zlf.appmaster.db.stock.StockMinuteTable;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.ui.stock.StockBaseInfoView;
import com.zlf.appmaster.ui.stock.StockTradeDetailMoreView;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by Huang on 2014/12/25.
 */
public class StockTradeDetailAdapter extends BaseAdapter {

//    private final static String TAG = "StockTradeDetailAdapter";

    private String mStockCode;
    private String mStockName;
    private StockClient mStockClient;

    private StockTradeInfo mStockTradeInfo;
    private LayoutInflater mInflater;
    private Context mContext;

    // K线部分
    private StockChartView mStockChartView;
    private ArrayList<StockKLine> mDailyKLines;
    private StockKLineTable mKLineTable;


    // 布局类型数量
    private static class ViewType{
        private final static int STOCK_SUMMARY      = 0;         //  股票摘要信息
        private final static int STOCK_KLINE        = 1;         //  K线区域
        private final static int STOCK_MORE_INFO    = 2;         //  附加信息

        public static int getCount(){
            return 3;
        }
    }

    // 列表显示的行数
    private int mRowCount = 3;     //  股票摘要信息 + K线区域 + ..
    public final static int STOCK_STOCK_SUMMARY_START_POS = 0;     // 摘要信息起始位置


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

    public StockTradeDetailAdapter(Context context, StockClient stockClient, StockTradeInfo stockTradeInfo){
        mContext = context;
        mStockClient = stockClient;
        mStockCode = stockTradeInfo.getCode();
        mStockName = stockTradeInfo.getName();
        mInflater = LayoutInflater.from(context);
        mStockTradeInfo = stockTradeInfo;
        mKLineTable = new StockKLineTable(mContext);
        // 初始化K线view
        mStockChartView = new StockChartView(context);
        initStockChart(mStockChartView, context);

    }

    @Override
    public int getViewTypeCount() {
        return ViewType.getCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return ViewType.STOCK_SUMMARY;
        else if (position == 1)
            return ViewType.STOCK_KLINE;
        else
            return ViewType.STOCK_MORE_INFO;

    }

    @Override
    public int getCount() {

        if (mStockTradeInfo != null){
            return mRowCount;
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    public void refreshKLineDataView(){
        mHandler.sendEmptyMessageDelayed(REFRESH_KLINE_DATA, 1000);
    }

    @Override
    public boolean isEnabled(int position) {
        if (position == STOCK_STOCK_SUMMARY_START_POS){     // 禁用第一项的点击
            return false;
        }
        return super.isEnabled(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);

        switch (viewType){
            case ViewType.STOCK_SUMMARY:
                if (null == convertView) {
                    convertView = new StockBaseInfoView(mContext, mStockCode, mStockName);
                }
                ((StockBaseInfoView)convertView).updateViews(mStockTradeInfo);

                break;
            case ViewType.STOCK_KLINE:
                if (null == convertView) {
                    convertView = mStockChartView;
                }
                break;
            case ViewType.STOCK_MORE_INFO:
                if (null == convertView){
                    convertView = new StockTradeDetailMoreView(mContext, mStockCode);
                }
                break;

        }

        return convertView;
    }




    private void initStockChart(StockChartView stockChartView, Context context) {

        stockChartView.setOnClickChartListener(new OnClickChartListener() {

            @Override
            public void onClick(int type) {
                enterChartDetail(type);
            }
        });

        StockChartTool sTool = new StockChartTool() {

            @Override
            public void getMinuteData(final OnGetMinuteDataListener listener) {
                getStockMinuteData(listener);
            }

            @Override
            public void getDailyKLines(final OnGetDailyKLinesListener listener) {
                getStockDailyKLines(listener);
            }

            @Override
            public void getHandicapData(final OnGetHandicapListener listener) {
                getStockHandicapData(listener);
            }
        };

        stockChartView.initData(mStockCode, "", true, sTool);
    }

    public void getStockHandicapData(final OnGetHandicapListener listener) {

        if (mStockTradeInfo != null ){
            if (mStockTradeInfo.getStockStatus() == StockTradeInfo.STATUS_OPEN_PREPARE
                    || mStockTradeInfo.getStockStatus() == StockTradeInfo.STATUS_STOP){
                //停牌 或 清数据时间
                listener.onError();
                return;
            }
        }

        //先从本地读取
        StockMinuteTable table = new StockMinuteTable(mContext);
        String jsonString = table.getHandicapData(mStockCode);
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                listener.onDataFinish(new JSONObject(jsonString));
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }

        mStockClient.getStockHandicap(mStockCode, new OnRequestListener() {

            @Override
            public void onDataFinish(Object object) {
                //保存到数据库
                StockMinuteTable table = new StockMinuteTable(mContext);
                table.saveHandicapData(mStockCode, object.toString());

                //获取数据
                listener.onDataFinish((JSONObject) object);
            }

            @Override
            public void onError(int errorCode, String errorString) {

            }
        });
    }

    /**
     * 获取日K数据
     * @param listener
     */
    public void getStockDailyKLines(final OnGetDailyKLinesListener listener) {
        //获取下起始时间点 大于1则说明有数据，可直接显示
        long startTime = 1;

        //从数据库中获取
        byte[] b = mKLineTable.getKLineData(mStockCode);
        if (b != null && b.length > 0) {
            mDailyKLines = StockKLine.resloveKLineFromSql(b, mContext);
            startTime = mDailyKLines.get(mDailyKLines.size()-1).getDataTime();//最后一条时间取起

            if (listener != null) {
                listener.onDataFinish(mDailyKLines);
            }
        }

        //获取新的数据
        //获取下起始时间点 大于1则说明有数据，可直接显示
        long endTime = 32500886400000L;//2999/11/30 0:0:0

        int type = StockClient.TYPE_STOCK;

        //获取新的数据
        mStockClient.getKlineInfo(mStockCode, StockClient.KLINE_TYPE_DAILY, type, startTime, endTime, new OnRequestListener() {

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
                }else {
                    mDailyKLines = dataLines;
                }


                listener.onDataFinish(mDailyKLines);

                //要把之前的加进去

                byte[] data = StockKLine.getKLineBytes(mDailyKLines);

                //保存起来
                mKLineTable.saveKLineData(mStockCode, data, mDailyKLines.get(mDailyKLines.size()-1).getDataTime());
            }
        });

    }

    /**
     * 获取分时数据
     * @param listener
     */
    private void getStockMinuteData(final OnGetMinuteDataListener listener) {
        if (mStockTradeInfo != null ){
            if (mStockTradeInfo.getStockStatus() == StockTradeInfo.STATUS_OPEN_PREPARE
                    || mStockTradeInfo.getStockStatus() == StockTradeInfo.STATUS_STOP){
                //停牌 或 清数据时间
                listener.onError();
                mStockChartView.clearMinuteData(mStockTradeInfo.getCurPrice());
                return;
            }
        }
        long lastTime = 0;//去掉毫秒
        //先从数据库中读取显示
        StockMinuteTable table = new StockMinuteTable(mContext);
        byte[] b = table.getMintueData(mStockCode);
        if (b != null && b.length > 0) {
            ArrayList<StockMinutes> dataArrayList = StockMinutes.resloveByteArray(b);
            if (dataArrayList != null && dataArrayList.size() > 0) {
                listener.onDataFinish(dataArrayList);
                //修改为数据库的时间
                lastTime = dataArrayList.get(dataArrayList.size()-1).getDataTime();
            }
        }

        //再去读取网络的
        //设置最后一笔的时间点
        final long endTime = 99999999999999L;

        mStockClient.getMinuteInfoByLastTime(mStockCode, 0,
                lastTime,
                new OnRequestListener() {

                    @Override
                    public void onError(int errorCode, String errorString) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onDataFinish(Object object) {

                        ArrayList<StockMinutes> dataArrayList = StockMinutes.resloveMinutesData(object,true,endTime);
                        if (dataArrayList == null || dataArrayList.size() == 0) {
                            //没有获取到数据
                            listener.onError();
                            return;
                        }
                        listener.onDataFinish(dataArrayList);

                        //保存到数据库
                        StockMinuteTable table = new StockMinuteTable(mContext);
                        table.saveMintueData(mStockCode, StockMinutes.toByteArray(dataArrayList), endTime);
                        table.close();
                    }
                });

    }

    /**
     * 进入分时线、K线大图
     * @param type
     */
    private void enterChartDetail(int type) {

//        Intent intent = new Intent(mContext,StockChartDetailActivity.class);
//        intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_LINE_TYPE, type);
//        intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_IS_STOCK, true);
//        intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_STOCK_ID, mStockCode);
//        intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_STOCK_NAME, mStockName);
//        intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_STOCK_STATE,
//                mStockTradeInfo.getStockStatus());
//        intent.putExtra(StockChartDetailActivity.INTENT_EXTRA_STOCK_PRICE,
//                mStockTradeInfo.getCurPrice());
//        mContext.startActivity(intent);
    }

}
