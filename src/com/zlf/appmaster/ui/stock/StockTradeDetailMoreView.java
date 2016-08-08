package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.bean.StockFinance;
import com.zlf.appmaster.bean.StockSummary;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.client.NewsClient;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.StockClient;
import com.zlf.appmaster.model.news.NewsFlashItem;
import com.zlf.appmaster.stocktrade.StockTradeDetailActivity;
import com.zlf.appmaster.utils.QLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Deping Huang on 2014/12/27.
 */
public class StockTradeDetailMoreView extends LinearLayout {

    private static String TAG = StockTradeDetailMoreView.class.getSimpleName();

    private static final int TAB_COUNT = 5;
    private static final int[] TAB_ID = {R.id.stock_news, R.id.stock_announcement, R.id.stock_report, R.id.stock_summary, R.id.stock_finance};

    private View[] mTab = new View[TAB_COUNT];
    private OnRadioChangeListener mOnRadioChangeListener = new OnRadioChangeListener();

    private static Context mContext;
    private String mStockCode;

    private View mStockExtraInfoTabSelectedII;	//记录当前选中的tab
    private ProgressBar mExtraInfoProgressBar_II;
    private TextView mExtraInfoDataPromptViewII; // tab模块数据出错提示

    private StockNewsView mStockNewsView;
    private StockAnnouncementView mStockAnnouncementView;
    private StockFinanceView mStockFinanceView;
    private StockSummaryView mStockSummaryView;
    private StockReportsView mStockReportsView;

    //是否已经请求过数据的标记
    private boolean mbLoadedNewsView = false;
    private boolean mbLoadedAnnouncementView = false;
    private boolean mbLoadedFinanceView = false;
    private boolean mbLoadedSummaryView = false;
    private boolean mbLoadedReportsView = false;


    private StockClient mStockClient;
    private NewsClient mNewsClient;

    // 请求刷新某个局部信息
    private static final int MSG_UPDATE_STOCK_NEWS = 1;
    private static final int MSG_UPDATE_STOCK_ANNOUNCEMENT = 2;
    private static final int MSG_UPDATE_STOCK_REPORT = 3;
    private static final int MSG_UPDATE_STOCK_SUMMARY = 5;
    private static final int MSG_UPDATE_STOCK_FINANCE = 6;
    public Handler mHandler = new Handler(){

        @Override
        public  void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case MSG_UPDATE_STOCK_NEWS:
                    requestStockNews();
                    break;
                case MSG_UPDATE_STOCK_ANNOUNCEMENT:
                    requestStockAnnouncement();
                    break;
                case MSG_UPDATE_STOCK_REPORT:
                    requestStockReport();
                    break;
                case MSG_UPDATE_STOCK_SUMMARY:
                    requestStockSummary();
                    break;
                case MSG_UPDATE_STOCK_FINANCE:
                    requestStockFinance();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };

    public StockTradeDetailMoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context,"");
    }

    /**
     * 传入stockCode和stockName 快速初始化一些东西
     * @param context
     */
    public StockTradeDetailMoreView(Context context, String stockCode) {
        super(context);
        initViews(context, stockCode);

    }


    private void initViews(Context context, String stockCode){
        if (isInEditMode()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        mContext = context;
        mStockCode = stockCode;
        mStockClient = new StockClient(context);
        mNewsClient = NewsClient.getInstance(context);

        View view = inflater.inflate(R.layout.layout_stocktrade_detail_more_info, this,true);
        for(int i = 0; i < TAB_COUNT; i++) {
            mTab[i] = view.findViewById(TAB_ID[i]);
            mTab[i].setOnClickListener(mOnRadioChangeListener);
        }
        mTab[0].setSelected(true);
        mTab[0].setEnabled(false);
        mStockExtraInfoTabSelectedII = mTab[0];

        mExtraInfoProgressBar_II = (ProgressBar)findViewById(R.id.extra_info_loading_II);
        mExtraInfoDataPromptViewII = (TextView)findViewById(R.id.no_extra_info_data_II);

        mExtraInfoDataPromptViewII.setOnClickListener(new OnExtraInfoViewClickListener());
        mExtraInfoDataPromptViewII.setTag(EXTRA_INFO_STATUS_INIT);

        mStockNewsView = (StockNewsView)findViewById(R.id.stock_news_view);
        mStockAnnouncementView = (StockAnnouncementView)findViewById(R.id.stock_announcement_view);
        mStockReportsView = (StockReportsView)findViewById(R.id.stock_report_view);
        mStockFinanceView = (StockFinanceView)findViewById(R.id.stock_finance_view);
        mStockSummaryView = (StockSummaryView)findViewById(R.id.stock_summary_view);

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_STOCK_NEWS, 1000);
    }

    /**
     * 请求个股新闻
     */
    private boolean loadStockNewsFromCache(){
        JSONObject response = StockJsonCache.loadFromFile(mStockCode, mContext, StockJsonCache.CACHEID_EXTRA_INFO_NEWS);
        if(null == response)
            return false;

        try {
            updateStockNewsViews(NewsFlashItem.resolveNewsArray("10-"+mStockCode,response));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    private void requestStockNews(){

        // 这部分数据时效性不强，加载过一次则不重新加载，直接从缓存中读取
        if(loadStockNewsFromCache() && mbLoadedNewsView){
            return;
        }

        mExtraInfoProgressBar_II.setVisibility(View.VISIBLE);
        showExtraInfoPrompt(mExtraInfoDataPromptViewII, EXTRA_INFO_STATUS_INIT);

        mNewsClient.requestStockNewsList(mStockCode, NewsClient.STOCK_NEWS_LABEL_NEWS, 0, 0, 0, 6, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                // TODO Auto-generated method stub
                mExtraInfoProgressBar_II.setVisibility(View.GONE);
                showExtraInfoPrompt(mExtraInfoDataPromptViewII, EXTRA_INFO_STATUS_ERROR);
            }

            @Override
            public void onDataFinish(Object object) {
                final ArrayList<NewsFlashItem> list = (ArrayList<NewsFlashItem>)object;
                // TODO Auto-generated method stub
                mExtraInfoProgressBar_II.setVisibility(View.GONE);
                updateStockNewsViews(list);
                mbLoadedNewsView = true;
            }
        });
    }
    private void updateStockNewsViews(List<NewsFlashItem> newsHomeSubArray){
        if(mStockExtraInfoTabSelectedII.getId()  != R.id.stock_news){
            return;
        }
        mStockNewsView.setVisibility(View.VISIBLE);
        mStockNewsView.updateViews(mStockCode,newsHomeSubArray);
    }

    /**
     *	请求个股研报
     */
    private boolean loadStockReportFromCache(){
        JSONObject response = StockJsonCache.loadFromFile(mStockCode, mContext, StockJsonCache.CACHEID_EXTRA_INFO_REPORT);
        if(null == response)
            return false;

        try {
            updateStockReportViews(NewsFlashItem.resolveNewsArray("10-" + mStockCode, response));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    private void requestStockReport(){
        if(loadStockReportFromCache() && mbLoadedReportsView){
            return;
        }

        mExtraInfoProgressBar_II.setVisibility(View.VISIBLE);
        showExtraInfoPrompt(mExtraInfoDataPromptViewII, EXTRA_INFO_STATUS_INIT);
        mNewsClient.requestStockNewsList(mStockCode, NewsClient.STOCK_NEWS_LABEL_REPORT, 0, 0, 0, 6, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                // TODO Auto-generated method stub
                ((StockTradeDetailActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mExtraInfoProgressBar_II.setVisibility(View.GONE);
                        showExtraInfoPrompt(mExtraInfoDataPromptViewII, EXTRA_INFO_STATUS_ERROR);
                    }
                });
            }

            @Override
            public void onDataFinish(Object object) {
                // TODO Auto-generated method stub
                final ArrayList<NewsFlashItem> list = (ArrayList<NewsFlashItem>)object;
                ((StockTradeDetailActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mExtraInfoProgressBar_II.setVisibility(View.GONE);
                        updateStockReportViews(list);
                    }
                });
                mbLoadedReportsView = true;
            }
        });
    }
    private void updateStockReportViews(List<NewsFlashItem> stockReportItemArray){
        if(mStockExtraInfoTabSelectedII.getId()  != R.id.stock_report){
            return;
        }
        mStockReportsView.setVisibility(View.VISIBLE);
        mStockReportsView.updateViews(mStockCode,stockReportItemArray);
    }



    /**
     * 请求个股公告
     */
    private boolean loadStockAnnouncementFromCache(){
        JSONObject response = StockJsonCache.loadFromFile(mStockCode, mContext, StockJsonCache.CACHEID_EXTRA_INFO_ANNOUCEMENT);
        if(null == response)
            return false;

        try {
            updateStockAnnouncementViews(NewsFlashItem.resolveNewsArray("10-" + mStockCode, response));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    private void requestStockAnnouncement(){

        if(loadStockAnnouncementFromCache() && mbLoadedAnnouncementView) return;

        mExtraInfoProgressBar_II.setVisibility(View.VISIBLE);
        showExtraInfoPrompt(mExtraInfoDataPromptViewII, EXTRA_INFO_STATUS_INIT);
        /*mNewsClient.requestStockAnnoucementList(0, 6, mStockCode, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                // TODO Auto-generated method stub
                mExtraInfoProgressBar_II.setVisibility(View.GONE);
                showExtraInfoPrompt(mExtraInfoDataPromptViewII, EXTRA_INFO_STATUS_ERROR);
            }

            @Override
            public void onDataFinish(Object object) {
                // TODO Auto-generated method stub
                mExtraInfoProgressBar_II.setVisibility(View.GONE);
                updateStockAnnouncementViews((ArrayList<StockAnnoucementItem>) object);

                mbLoadedAnnouncementView = true;
            }
        });*/
        mNewsClient.requestStockNewsList(mStockCode, NewsClient.STOCK_NEWS_LABEL_ANNOUNCEMENT, 0, 0, 0, 6, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                // TODO Auto-generated method stub
                mExtraInfoProgressBar_II.setVisibility(View.GONE);
                showExtraInfoPrompt(mExtraInfoDataPromptViewII, EXTRA_INFO_STATUS_ERROR);
            }

            @Override
            public void onDataFinish(Object object) {
                // TODO Auto-generated method stub
                mExtraInfoProgressBar_II.setVisibility(View.GONE);
                updateStockAnnouncementViews((List<NewsFlashItem>) object);

                mbLoadedReportsView = true;
            }
        });
    }

    private void updateStockAnnouncementViews(List<NewsFlashItem> data){
        if(mStockExtraInfoTabSelectedII.getId() != R.id.stock_announcement){
            QLog.e(TAG, "tab getId != R.id.stock_announcement");
            return;
        }
        mStockAnnouncementView.setVisibility(View.VISIBLE);
        mStockAnnouncementView.updateViews(mStockCode, data);
    }


    /**
     * 请求个股概况
     */
    private boolean loadStockSummaryFromCache(){
        JSONObject response = StockJsonCache.loadFromFile(mStockCode, mContext, StockJsonCache.CACHEID_EXTRA_INFO_SUMMARY);
        if(null == response)
            return false;

        try {
            updateSummaryView(StockSummary.resolveJSONObject(response));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    private void requestStockSummary(){

        if(loadStockSummaryFromCache() && mbLoadedSummaryView) return;

        mExtraInfoProgressBar_II.setVisibility(View.VISIBLE);
        showExtraInfoPrompt(mExtraInfoDataPromptViewII, EXTRA_INFO_STATUS_INIT);
        mStockClient.requestStockSummaryInfo(mStockCode, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                // TODO Auto-generated method stub
                mExtraInfoProgressBar_II.setVisibility(View.GONE);
                showExtraInfoPrompt(mExtraInfoDataPromptViewII, EXTRA_INFO_STATUS_ERROR);
            }

            @Override
            public void onDataFinish(Object object) {
                // TODO Auto-generated method stub
                mExtraInfoProgressBar_II.setVisibility(View.GONE);
                updateSummaryView((StockSummary) object);
                mbLoadedSummaryView = true;
            }
        });
    }
    private void updateSummaryView(StockSummary data){
        if(mStockExtraInfoTabSelectedII.getId() != R.id.stock_summary){
            QLog.e(TAG, "tab getId != R.id.stock_summary");
            return;
        }
        mStockSummaryView.setVisibility(View.VISIBLE);
        mStockSummaryView.updateViews(data);
    }

    /**
     * 请求个股财务
     */
    private boolean loadStockFinanceFromCache(){
        JSONObject response = StockJsonCache.loadFromFile(mStockCode, mContext, StockJsonCache.CACHEID_EXTRA_INFO_FINANCE);
        if(null == response)
            return false;

        try {
            updateFinanceView(StockFinance.resolveJSONObject(response));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    private void requestStockFinance(){

        if(loadStockFinanceFromCache() && mbLoadedFinanceView)	return;

        mExtraInfoProgressBar_II.setVisibility(View.VISIBLE);
        showExtraInfoPrompt(mExtraInfoDataPromptViewII, EXTRA_INFO_STATUS_INIT);
        mStockClient.requestStockFinanceInfo(mStockCode, new OnRequestListener() {

            @Override
            public void onError(int errorCode, String errorString) {
                // TODO Auto-generated method stub
                mExtraInfoProgressBar_II.setVisibility(View.GONE);
                showExtraInfoPrompt(mExtraInfoDataPromptViewII, EXTRA_INFO_STATUS_ERROR);
            }

            @Override
            public void onDataFinish(Object object) {
                // TODO Auto-generated method stub
                mExtraInfoProgressBar_II.setVisibility(View.GONE);
                updateFinanceView((StockFinance)object);

                mbLoadedFinanceView = true;
            }
        });
    }

    private void updateFinanceView(StockFinance stockFinance){
        if(mStockExtraInfoTabSelectedII.getId() != R.id.stock_finance){
            return;
        }
        mStockFinanceView.setVisibility(View.VISIBLE);
        mStockFinanceView.updateViews(stockFinance);
    }

    /**
     * ExtraInfo tab 数据提示
     */
    private static final int EXTRA_INFO_STATUS_INIT = 1;
    private static final int EXTRA_INFO_STATUS_ERROR = 2;
    private  static void showExtraInfoPrompt(TextView v, int status){
        v.setTag(status);
        switch(status){
            case EXTRA_INFO_STATUS_INIT:
                v.setText("");
                break;
            case EXTRA_INFO_STATUS_ERROR:
                v.setText("更新失败，请点击重试");
                break;
        }
    }

    /**
     * 点击重试
     */
    private class OnExtraInfoViewClickListener implements OnClickListener {

        @Override
        public  void onClick(View v) {
            // TODO Auto-generated method stub
            if((null != v) && (Integer)v.getTag() != EXTRA_INFO_STATUS_ERROR){
                return ;
            }

            if(v == mExtraInfoDataPromptViewII){
                // 跳转
                jumpToExtraView(mStockExtraInfoTabSelectedII.getId());
            }
        }

    }


    /**
     * 点击tab
     */
    private class OnRadioChangeListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            // 隐藏内容layout
            mStockNewsView.setVisibility(View.GONE);
            mStockAnnouncementView.setVisibility(View.GONE);
            mStockReportsView.setVisibility(View.GONE);
            mStockFinanceView.setVisibility(View.GONE);
            mStockSummaryView.setVisibility(View.GONE);

            // 设置选中
            for(int i = 0; i < mTab.length; i++) {
                mTab[i].setSelected(false);
                mTab[i].setEnabled(true);
            }
            v.setSelected(true);
            v.setEnabled(false);

            mStockExtraInfoTabSelectedII = v;

            // 跳转
            jumpToExtraView(v.getId());
        }
    }

    private void jumpToExtraView(int viewID){
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        switch (viewID){
            case R.id.stock_news:
                mHandler.sendEmptyMessage(MSG_UPDATE_STOCK_NEWS);
                break;
            case R.id.stock_announcement:
                mHandler.sendEmptyMessage(MSG_UPDATE_STOCK_ANNOUNCEMENT);
                break;
            case R.id.stock_report:
                mHandler.sendEmptyMessage(MSG_UPDATE_STOCK_REPORT);
                break;
            case R.id.stock_summary:
                mHandler.sendEmptyMessage(MSG_UPDATE_STOCK_SUMMARY);
                break;
            case R.id.stock_finance:
                mHandler.sendEmptyMessage(MSG_UPDATE_STOCK_FINANCE);
                break;
        }
    }

}
