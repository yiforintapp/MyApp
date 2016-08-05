package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.client.NewsClient;
import com.zlf.appmaster.model.news.NewsFlashItem;
import com.zlf.appmaster.stocknews.AnnouncementDetailActivity;
import com.zlf.appmaster.stocktrade.StockTradeDetailNewsActivity;
import com.zlf.appmaster.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class StockAnnouncementView extends LinearLayout {
    private final static String TAG = StockAnnouncementView.class.getSimpleName();
    private Context mContext;
    private final int MAX_COUNT = 5;    // 最大行数

    private List<NewsFlashItem> mData = new ArrayList<NewsFlashItem>();

    private LinearLayout mContentLayout;        // 内容布局
    private View mMoreView;                     // 更多

    public StockAnnouncementView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }
    public StockAnnouncementView(Context context) {
        super(context);
        initViews(context);

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
                    intent.putExtra(StockTradeDetailNewsActivity.INTENT_FLAG_LABEL, NewsClient.STOCK_NEWS_LABEL_ANNOUNCEMENT);
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
        private TextView title,time;
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

            title = (TextView)view.findViewById(R.id.news_title);
            time = (TextView)view.findViewById(R.id.news_time);
        }

        public void setData(NewsFlashItem data){
            mItemData = data;
            title.setText(data.getTitle());
            time.setText(TimeUtil.getMonthAndDay(data.getTime()));
        }

        @Override
        public void onClick(View view) {
            if (null != mItemData){
                Intent intent = new Intent(mContext, AnnouncementDetailActivity.class);
                intent.putExtra(AnnouncementDetailActivity.INTENT_FLAG_DATA, mItemData);
                mContext.startActivity(intent);
            }

        }
    }


}
