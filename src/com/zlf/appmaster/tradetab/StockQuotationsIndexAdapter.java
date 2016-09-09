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
    private List<StockIndex> mForeignDelayIndexItems;      // 国外的延迟指数

    // 布局类型数量
    private static class ViewType {
        private final static int TITLE_INDEX = 0;
        private final static int TITLE_FOREIGN_INDEX = 1;
        private final static int ITEM = 2;

        public static int getCount() {
            return 3;
        }
    }

    public StockQuotationsIndexAdapter(Context context, List<StockIndex> indexItems, List<StockIndex> foreignDelayIndexItems){
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mIndexItems = indexItems;
        mForeignDelayIndexItems = foreignDelayIndexItems;
    }
    @Override
    public int getCount() {
        int itemCount = 0;

//       if (mIndexItems.size() != 0){
//           itemCount += mIndexItems.size() + 1;
//       }

//       if (mForeignDelayIndexItems.size() != 0) {
//           itemCount += mForeignDelayIndexItems.size() + 1;
//       }
       return mIndexItems.size();
    }

    @Override
    public Object getItem(int position) {
//        int indexItemLen = mIndexItems.size();
//
//        if (indexItemLen > 0){
//            if (position > 0 && position <= indexItemLen){
//                return mIndexItems.get(position - 1);
//            }
//            else if (position > indexItemLen && position <= mForeignDelayIndexItems.size() + indexItemLen + 1){
//                return mForeignDelayIndexItems.get(position - indexItemLen - 2);
//            }
//        }
//        else {  // 只有国外指数的情况
//            if (position > 0 && position <= mForeignDelayIndexItems.size()){
//                return mForeignDelayIndexItems.get(position - 1);
//            }
//        }

        return mIndexItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
//        int len = mIndexItems.size();
//
//        if (len > 0){
//            if (position == 0)
//                return ViewType.TITLE_INDEX;
//            else if (position == len + 1){
//                return ViewType.TITLE_FOREIGN_INDEX;
//            }
//            else
//                return ViewType.ITEM;
//        }
//        else{
//            if (position == 0)
//                return ViewType.TITLE_FOREIGN_INDEX;
//            else
//                return ViewType.ITEM;
//        }
 return 1;
    }

    @Override
    public int getViewTypeCount() {
//        return ViewType.getCount();
        return 1;
    }

    @Override
    public boolean isEnabled(int position) {
//        int indexItemLen = mIndexItems.size();
//        if (indexItemLen > 0){
//            if (position > 0 && position <= indexItemLen){
//                return true;
//            }
//            else if (position > indexItemLen && position <= mForeignDelayIndexItems.size() + indexItemLen + 1){
//                return false;
//            }
//        }
//        else {  // 只有国外指数的情况
//            if (position > 0 && position <= mForeignDelayIndexItems.size()){
//                return false;
//            }
//        }

        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        int viewType = getItemViewType(position);
        if(viewType == ViewType.TITLE_INDEX || viewType == ViewType.TITLE_FOREIGN_INDEX){
            viewType = ViewType.ITEM;
        }
        switch (viewType) {
            case ViewType.TITLE_INDEX: {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_header_index_info, null);
                }
            }
            break;
            case ViewType.TITLE_FOREIGN_INDEX: {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_header_foreign_index_info, null);
                }
            }
            break;
            case ViewType.ITEM: {
                ViewHolder viewHolder = null;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_quotations_index, null);
                    viewHolder = new ViewHolder();
                    viewHolder.name = (TextView)convertView.findViewById(R.id.index_name);
                    viewHolder.price = (StockTextView) convertView.findViewById(R.id.index_price);
                    viewHolder.percentPrompt = (StockTextView) convertView.findViewById(R.id.index_percent);
                    convertView.setTag(viewHolder);
                }
                else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }


                StockIndex item = (StockIndex)getItem(position);
                if (null != item){
                    viewHolder.name.setText(item.getName());

                    int riseInfo = item.getRiseInfo();
                    viewHolder.price.setRiseInfo(riseInfo);
//                    viewHolder.percentPrompt.setRiseInfo(riseInfo);
                    if(riseInfo > 0){
                        viewHolder.percentPrompt.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.stock_rise_text_bg));

                    } else if(riseInfo < 0){
                        viewHolder.percentPrompt.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.stock_drop_text_bg));
                    } else {
                        viewHolder.percentPrompt.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.stock_drop_text_bg));
                    }

                    viewHolder.price.setText(item.getCurPriceFormat());
                    viewHolder.percentPrompt.setText(item.getCurPercentFormat());
                }
            }
            break;
        }

        return convertView;
    }

    class  ViewHolder{
        TextView name;
        StockTextView price;
        StockTextView percentPrompt;
    }
}
