package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.topic.TopicItem;
import com.zlf.appmaster.stocktopic.TopicDetailActivity;
import com.zlf.appmaster.utils.LiveRecordingUtil;
import com.zlf.appmaster.utils.QLog;

import java.util.ArrayList;
import java.util.List;

public class StockDetailTopicTagLayout extends LinearLayout {
    private static final String TAG = StockDetailTopicTagLayout.class.getSimpleName();
    private Context mContext;

    private List<TopicItem> mData = new ArrayList<TopicItem>();

    private LinearLayout mContentLayout;        // 内容布局
    private Boolean isLiveDisplay = false;
    private LiveRecordingUtil mLiveRecordingUtil;

    public StockDetailTopicTagLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs);
        initViews(context);
    }
    public StockDetailTopicTagLayout(Context context) {
        super(context);
        initViews(context);

    }

    private void initAttributes(Context context, AttributeSet attr){
        if(attr == null)
            return;
        TypedArray b = context.obtainStyledAttributes(attr, R.styleable.LiveDisplay);
        isLiveDisplay = b.getBoolean(R.styleable.LiveDisplay_is_live, false); //默认为普通模式

    }
	private void initViews(Context context){

        LayoutInflater inflater = LayoutInflater.from(context);
        mContext = context;
        if (isInEditMode()) {
            return;
        }
        View view = inflater.inflate(R.layout.layout_stock_detail_topic_tag, this,true);

        mContentLayout = (LinearLayout)view.findViewById(R.id.content_list);
    }

    public void updateViews(List<TopicItem> data){
        mData.clear();
        mData.addAll(data);

        int len = data.size();
        boolean isOddCount = false;
        if (len % 2 != 0){  // 奇数个情况
            len = len - 1;
            isOddCount = true;
        }

        mContentLayout.removeAllViews();
        for (int i = 1; i < len; i += 2) {
            TopicTagViewItem itemView = new TopicTagViewItem(mContext);
            TopicItem topicItems[] = new TopicItem[2];
            topicItems[0] = mData.get(i - 1);
            topicItems[1] = mData.get(i);
            itemView.setData(topicItems);
            mContentLayout.addView(itemView);
        }

        if (isOddCount){    // 最后一个
            TopicTagViewItem itemView = new TopicTagViewItem(mContext);
            TopicItem topicItems[] = new TopicItem[1];
            topicItems[0] = mData.get(len);
            itemView.setData(topicItems);
            mContentLayout.addView(itemView);
        }
    }

    /**
     * 每个标签项实际包含两个标签
     */
    class TopicTagViewItem extends LinearLayout {
        private TextView mTopicTV[] = new TextView[2];

        public TopicTagViewItem(Context context, AttributeSet attrs) {
            super(context, attrs);
            initViews(context);
        }
        public TopicTagViewItem(Context context) {
            super(context);
            initViews(context);

        }
        private void initViews(Context context){

            LayoutInflater inflater = LayoutInflater.from(context);
            if (isInEditMode()) {
                return;
            }
            mContext = context;

            View view = inflater.inflate(R.layout.view_stock_detail_topic_tag, this,true);

            mTopicTV[0] = (TextView)view.findViewById(R.id.topic_item_txt_1);
            mTopicTV[1] = (TextView)view.findViewById(R.id.topic_item_txt_2);

        }

        public void setData(TopicItem[] data) {

            if (null != data) {
                if (data.length == 1){  // 只有1个数据则隐藏标签2
                    mTopicTV[1].setVisibility(View.INVISIBLE);
                }
                else if(data.length > 2){
                    QLog.e(TAG, "");
                    return;
                }

                for (int i = 0; i < data.length; i ++){
                    int color = data[i].getColor();
                    // 设置线框颜色
                    GradientDrawable myGrad = (GradientDrawable) mTopicTV[i].getBackground();
                    myGrad.setStroke(2, color);

                    mTopicTV[i].setText(data[i].getName());
                    mTopicTV[i].setTextColor(color);

                    mTopicTV[i].setOnClickListener(new OnTagClickListener(data[i].getID(), data[i].getName()));
                }


            }
        }
        private class OnTagClickListener implements OnClickListener {
            private String mTopicId;
            private String mTopicName;
            public OnTagClickListener(String topicId, String topicName){
                mTopicId = topicId;
                mTopicName = topicName;
            }
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TopicDetailActivity.class);
                intent.putExtra(TopicDetailActivity.INTENT_FLAG_TOPIC_ID, mTopicId);
                intent.putExtra(TopicDetailActivity.INTENT_FLAG_TOPIC_NAME, mTopicName);
                mContext.startActivity(intent);
            }
        }
    }

}
