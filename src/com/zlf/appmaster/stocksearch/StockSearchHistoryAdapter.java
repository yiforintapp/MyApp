package com.zlf.appmaster.stocksearch;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.db.stock.StockSearchHistoryTable;
import com.zlf.appmaster.model.search.StockSearchItem;

import java.util.List;


/**
 * Created by Deping on 2015/1/3.
 */
public class StockSearchHistoryAdapter extends StockSearchAdapter {

    private boolean mIsShowHottestItem = false;     // 是否是显示最热个股

    public StockSearchHistoryAdapter(Context context, List<StockSearchItem> data, boolean isShowHottestItem) {
        super(context, data);
        mIsShowHottestItem = isShowHottestItem;
    }

    @Override
    public int getCount() {
        int dataSize = mData.size();
        if (dataSize  == 0){
            return 0;
        }
        return mData.size() + 1;    // 加title
    }

    @Override
    public Object getItem(int position) {
        if (position == 0)
            return null;
        return mData.get(position - 1);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return 0;
        }
        else {
            return 1;
        }
    }

    @Override
    public boolean isEnabled(int position) {

        if (position == 0){     // 标题禁点
            return false;
        }

        return super.isEnabled(position);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewTitleHolder viewTitleHolder = null;
        if (position == 0){     // title
            if (convertView == null){
                viewTitleHolder = new ViewTitleHolder();
                convertView = mInflater.inflate(R.layout.list_item_search_stock_title, null);
                viewTitleHolder.cleanRecord = convertView.findViewById(R.id.clean_all_history);
                viewTitleHolder.cleanRecord.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        mData.clear();
                        notifyDataSetChanged();
                        // 表中记录
                        StockSearchHistoryTable cacheTable = new StockSearchHistoryTable(mContext);
                        cacheTable.clearAll();
                    }
                });

                viewTitleHolder.title = (TextView)convertView.findViewById(R.id.history_search_stock_title);
                convertView.setTag(viewTitleHolder);
            }
            else{
                viewTitleHolder = (ViewTitleHolder)convertView.getTag();
            }

            if (mIsShowHottestItem){
                viewTitleHolder.title.setText(R.string.all_search);
            }
            else {
                viewTitleHolder.title.setText(R.string.search_history);
            }

        }
        else{
            ViewHolder viewHolder = null;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.list_item_search_stock, null);
                viewHolder.code = (TextView)convertView.findViewById(R.id.stock_code);
                viewHolder.name = (TextView)convertView.findViewById(R.id.stock_name);
                viewHolder.addBtn = convertView.findViewById(R.id.stock_add);
                viewHolder.addedPrompt = convertView.findViewById(R.id.stock_add_succeed);

                convertView.setTag(viewHolder);
            }
            else{
                viewHolder = (ViewHolder)convertView.getTag();
            }
            StockSearchItem item = (StockSearchItem)getItem(position);
            if (null != item){

                if (mAdapterType == ADAPTER_TYPE_DEFAULT) {
                    viewHolder.addBtn.setVisibility(View.GONE);
                    viewHolder.addedPrompt.setVisibility(View.GONE);
                }
                else {
                    if(item.isFavorite()){ // 已添加
                        viewHolder.addBtn.setVisibility(View.GONE);
                        viewHolder.addedPrompt.setVisibility(View.VISIBLE);
                    }
                    else {
                        viewHolder.addBtn.setVisibility(View.VISIBLE);
                        viewHolder.addedPrompt.setVisibility(View.GONE);
                        viewHolder.addBtn.setOnClickListener(new OnAddBtnClickListener(position));
                    }
                }

                viewHolder.code.setText(item.getStockCode());
                viewHolder.name.setText(item.getStockName());


            }

        }
        return convertView;
    }


    private class ViewTitleHolder{
        View cleanRecord;
        TextView title;
    }
    private class ViewHolder{
        TextView code;
        TextView name;
        View addBtn;
        View addedPrompt;

    }




}
