package com.zlf.appmaster.tradetab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.model.topic.TopicInfo;
import com.zlf.appmaster.ui.stock.StockTextView;

import java.util.List;

public class TopicListAdapter extends BaseAdapter {

    private Context mContext;
    private List<TopicInfo> mData;
    private LayoutInflater mInflater;

    private static final int TYPE_TILE = 0;
    private static final int TYPE_ITEM = 1;

    public TopicListAdapter(Context context, List<TopicInfo> data) {
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        int len = mData.size();
        if (len > 0) {
            len += 1;
        }

        return len;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        if (position > 0) {
            return mData.get(position - 1);
        }


        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_TILE;
        }

        return TYPE_ITEM;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        int viewType = getItemViewType(position);

        switch (viewType) {
            case TYPE_TILE: {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_header_topic_info, null);
                }
            }
            break;
            case TYPE_ITEM: {
                ViewHolder viewHolder = null;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_topic_info, null);
                    viewHolder = new ViewHolder();
                    viewHolder.TopicName = (TextView) convertView.findViewById(R.id.topic_name);
                    viewHolder.StockName = (TextView) convertView.findViewById(R.id.stock_name);
                    // viewHolder.StockCode = (TextView)convertView.findViewById(R.id.stock_code);
                    viewHolder.StockPercent = (StockTextView) convertView.findViewById(R.id.stock_percent);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                TopicInfo topicInfo = (TopicInfo) getItem(position);
                if (null != topicInfo) {
                    viewHolder.TopicName.setText(topicInfo.getName());
                    viewHolder.StockPercent.setText(topicInfo.getPercentFormat());
                    viewHolder.StockPercent.setRiseInfo(topicInfo.getRiseInfo());
                    StockTradeInfo stockInfoItem = topicInfo.getLedStock();
                    if (stockInfoItem != null) {
                        viewHolder.StockName.setText(stockInfoItem.getName());
                        // viewHolder.StockCode.setText(stockInfoItem.getCode());
                    }
                }


            }
            break;
        }

        return convertView;
    }

    class ViewHolder {
        TextView TopicName;
        StockTextView StockPercent;
        TextView StockName, StockCode;
    }
}
