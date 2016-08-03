package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.news.NewsFlashItem;
import com.zlf.appmaster.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class StockReportsView extends LinearLayout {
    private Context mContext;
    private final int MAX_COUNT = 5;    // 最大行数

    private List<NewsFlashItem> mData = new ArrayList<NewsFlashItem>();

    private LinearLayout mContentLayout;        // 内容布局
    private View mMoreView;                     // 更多

    public StockReportsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }
    public StockReportsView(Context context) {
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
//                    Intent intent = new Intent(mContext, StockTradeDetailNewsActivity.class);
//                    intent.putExtra(StockTradeDetailNewsActivity.INTENT_FLAG_STOCK_CODE, stockCode);
//                    intent.putExtra(StockTradeDetailNewsActivity.INTENT_FLAG_LABEL, NewsClient.STOCK_NEWS_LABEL_REPORT);
//                    mContext.startActivity(intent);
                }
            });
            len = MAX_COUNT;
        }

        mContentLayout.removeAllViews();
        for (int i = 0; i < len; i++){
            StockReportViewItem itemView = new StockReportViewItem(mContext);
            itemView.setData(mData.get(i));
            itemView.setOnClickListener(itemView);
            mContentLayout.addView(itemView);
        }
    }


    /**
     * 研报项
     */
    class StockReportViewItem extends LinearLayout implements OnClickListener {
        private TextView title, time;
        NewsFlashItem mSubData;

        public StockReportViewItem(Context context, AttributeSet attrs) {
            super(context, attrs);
            initViews(context);
        }
        public StockReportViewItem(Context context) {
            super(context);
            initViews(context);

        }
        private void initViews(Context context){

            LayoutInflater inflater = LayoutInflater.from(context);
            if (isInEditMode()) {
                return;
            }
            View view = inflater.inflate(R.layout.list_item_newshome, this,true);

            title = (TextView)view.findViewById(R.id.news_title);
            time = (TextView)view.findViewById(R.id.news_time);
        }

        public void setData(NewsFlashItem data){
            mSubData = data;
            title.setText(data.getTitle());
            time.setText(TimeUtil.getNewsSimpleTime(data.getTime()));
        }


        @Override
        public void onClick(View view) {

            if (null != mSubData){
//                Intent intent = new Intent(mContext,
//                        NewsDetailActivity.class);
//                intent.putExtra(
//                        NewsDetailActivity.KEY_INTENT_NEWS_CATALOGUE,
//                        "研报详情");
//                intent.putExtra(NewsDetailActivity.KEY_INTENT_NEWS_ID,
//                        mSubData.getId());
//                intent.putExtra(NewsDetailActivity.KEY_INTENT_NEWS_TYPE,
//                        mSubData.getClassify());

//                mContext.startActivity(intent);
            }

        }

    }


}
