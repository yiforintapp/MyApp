package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.client.NewsClient;
import com.zlf.appmaster.model.news.NewsFlashItem;
import com.zlf.appmaster.stocknews.NewsDetailActivity;
import com.zlf.appmaster.stocktrade.StockTradeDetailNewsActivity;
import com.zlf.appmaster.utils.LiveRecordingUtil;
import com.zlf.appmaster.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class StockNewsView extends LinearLayout {
    private Context mContext;
    private int MAX_COUNT = 5;    // 最大行数 default

    private List<NewsFlashItem> mData = new ArrayList<NewsFlashItem>();

    private LinearLayout mContentLayout;        // 内容布局
    private View mMoreView;                     // 更多
    private Boolean isLiveDisplay = false;
    private LiveRecordingUtil mLiveRecordingUtil;

    public StockNewsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs);
        initViews(context);
    }
    public StockNewsView(Context context) {
        super(context);
        initViews(context);

    }

    //从布局文件中获取需要显示多少行
    private void initAttributes(Context context, AttributeSet attr){
        if(attr == null)
            return;
        TypedArray a = context.obtainStyledAttributes(attr, R.styleable.StockNews);
        MAX_COUNT = a.getInt(R.styleable.StockNews_max_item, 5); //默认为普通模式

        TypedArray b = context.obtainStyledAttributes(attr, R.styleable.LiveDisplay);
        isLiveDisplay = b.getBoolean(R.styleable.LiveDisplay_is_live, false); //默认为普通模式

    }


	private void initViews(Context context){

        LayoutInflater inflater = LayoutInflater.from(context);
        mContext = context;
        if (isInEditMode()) {
            return;
        }
        View view = inflater.inflate(R.layout.layout_stock_news_list, this,true);

        mContentLayout = (LinearLayout)view.findViewById(R.id.list_content);
        mMoreView = view.findViewById(R.id.list_item_more);
    }

    public void updateViews(final String stockCode, List<NewsFlashItem> data){
        mData.clear();
        mData.addAll(data);

        int len = mData.size();

        if(len > MAX_COUNT){    // 显示更多
            mMoreView.setVisibility(View.VISIBLE);
            mMoreView.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, StockTradeDetailNewsActivity.class);
                    intent.putExtra(StockTradeDetailNewsActivity.INTENT_FLAG_STOCK_CODE, stockCode);
                    intent.putExtra(StockTradeDetailNewsActivity.INTENT_FLAG_LABEL, NewsClient.STOCK_NEWS_LABEL_NEWS);
                    intent.putExtra(StockTradeDetailNewsActivity.INTENT_FLAG_LIVE, true);
                    mContext.startActivity(intent);
                }
            });
            len = MAX_COUNT;
        }

        mContentLayout.removeAllViews();
        for (int i = 0; i < len; i++){
            StockNewsViewItem itemView = new StockNewsViewItem(mContext);
            itemView.setData(mData.get(i));
            itemView.setOnClickListener(itemView);
            mContentLayout.addView(itemView);
        }
    }

    /**
     * 新闻项
     */
    class StockNewsViewItem extends LinearLayout implements OnClickListener {
        private TextView NewsTitle, NewsTime;
        private Context mContext;

        private NewsFlashItem mItemData;
        public StockNewsViewItem(Context context, AttributeSet attrs) {
            super(context, attrs);
            initViews(context);
        }
        public StockNewsViewItem(Context context) {
            super(context);
            initViews(context);

        }
        private void initViews(Context context){

            LayoutInflater inflater = LayoutInflater.from(context);
            if (isInEditMode()) {
                return;
            }
            mContext = context;

            View view = inflater.inflate(R.layout.list_item_newshome, this,true);

            NewsTitle = (TextView)view.findViewById(R.id.news_title);
            NewsTime = (TextView)view.findViewById(R.id.news_time);
        }

        public void setData(NewsFlashItem data){
            mItemData = data;
            NewsTitle.setText(data.getTitle());
            NewsTime.setText(TimeUtil.getMonthAndDay(data.getTime()));
        }

        @Override
        public void onClick(View view) {
            if (null != mItemData){
                Intent intent = new Intent(mContext,
                        NewsDetailActivity.class);
                intent.putExtra(
                        NewsDetailActivity.KEY_INTENT_NEWS_CATALOGUE,
                        "新闻详情");
                intent.putExtra(NewsDetailActivity.KEY_INTENT_NEWS_ID,
                        mItemData.getId());
                mContext.startActivity(intent);
            }
        }
    }
}
