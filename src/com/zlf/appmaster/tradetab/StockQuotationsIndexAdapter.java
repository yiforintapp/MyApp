package com.zlf.appmaster.tradetab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.ui.stock.StockTextView;

import java.util.List;

/**
 * Created by Huang on 2015/3/6.
 */
public class StockQuotationsIndexAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context mContext;
    private List<StockIndex> mIndexItems;           // A股的指数

    // 布局类型数量
    private static class ViewType {
        private final static int TYPE_1 = 0;
        private final static int TYPE_2 = 1;

        public static int getCount() {
            return 2;
        }
    }

    public StockQuotationsIndexAdapter(Context context, List<StockIndex> indexItems, List<StockIndex> foreignDelayIndexItems) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mIndexItems = indexItems;
    }

    @Override
    public int getCount() {
        return mIndexItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mIndexItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {

        int p = position;
        if (p == 0) {
            return ViewType.TYPE_1;
        } else {
            return ViewType.TYPE_2;
        }
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.getCount();
    }

    @Override
    public boolean isEnabled(int position) {

        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        int viewType = getItemViewType(position);
        ViewHolderEmpty holderEmpty = null;
        ViewHolder viewHolder = null;
        if (convertView == null) {
            switch (viewType) {
                case ViewType.TYPE_1: {
                    holderEmpty = new ViewHolderEmpty();
                    convertView = mInflater.inflate(R.layout.list_header_index_info, null);
                    convertView.setTag(holderEmpty);
                }
                break;
                case ViewType.TYPE_2: {
                    convertView = mInflater.inflate(R.layout.list_item_quotations_index, null);
                    viewHolder = new ViewHolder();
                    viewHolder.name = (TextView) convertView.findViewById(R.id.index_name);
                    viewHolder.price = (StockTextView) convertView.findViewById(R.id.index_price);
                    viewHolder.percentPrompt = (StockTextView) convertView.findViewById(R.id.index_percent);
                    convertView.setTag(viewHolder);
                }
                break;
            }
        } else {
            switch (viewType) {
                case ViewType.TYPE_1:
                    holderEmpty = (ViewHolderEmpty) convertView.getTag();
                    break;
                case ViewType.TYPE_2:
                    viewHolder = (ViewHolder) convertView.getTag();
                    break;
            }
        }

        switch (viewType){
            case ViewType.TYPE_2:

                StockIndex item = (StockIndex) getItem(position);
                if (null != item) {
                    viewHolder.name.setText(item.getName());

                    int riseInfo = item.getRiseInfo();
                    viewHolder.price.setRiseInfo(riseInfo);
                    if (riseInfo > 0) {
                        viewHolder.percentPrompt.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.stock_rise_text_bg));
                    } else if (riseInfo < 0) {
                        viewHolder.percentPrompt.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.stock_drop_text_bg));
                    } else {
                        viewHolder.percentPrompt.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.stock_equal_text_bg));
                    }

                    viewHolder.price.setText(item.getCurPriceFormat());
                    viewHolder.percentPrompt.setText(item.getCurPercentFormat());
                }
                break;
        }


        return convertView;
    }

    class ViewHolderEmpty {

    }

    class ViewHolder {
        TextView name;
        StockTextView price;
        StockTextView percentPrompt;
    }
}
