package com.zlf.appmaster.stocksearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.search.StockSearchItem;

import java.util.List;

public class StockSearchAdapter extends BaseAdapter {
    public static final int ADAPTER_TYPE_DEFAULT = 0;
    public static final int ADAPTER_TYPE_STOCK_GROUP = 1;
    public static final int ADAPTER_TYPE_STOCK_FAVORITE = 2;

    protected Context mContext;
    protected List<StockSearchItem> mData;
    protected OnAddListener mOnAddListener;
    protected LayoutInflater mInflater;

    protected int mAdapterType;

    public StockSearchAdapter(Context context, List<StockSearchItem> data){
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mData = data;
        setAdapterType(ADAPTER_TYPE_DEFAULT);
    }

    public interface OnAddListener{
        void onAdd(int position);
    }
    public void setOnAddListener(OnAddListener listener) {
        mOnAddListener = listener;
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {

        return mData.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.list_item_search_stock, null);
            viewHolder.code = (TextView) convertView.findViewById(R.id.stock_code);
            viewHolder.name = (TextView) convertView.findViewById(R.id.stock_name);
            viewHolder.addBtn = convertView.findViewById(R.id.stock_add);
            viewHolder.addedPrompt = convertView.findViewById(R.id.stock_add_succeed);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        StockSearchItem item = (StockSearchItem) getItem(position);
        if (null != item){
            if (mAdapterType == ADAPTER_TYPE_DEFAULT) {
                viewHolder.addBtn.setVisibility(View.GONE);
                viewHolder.addedPrompt.setVisibility(View.GONE);
            }
            else {
//			int colIndex = StockTable.COLNAME_FAVORITE_INDEX;
//			if(mAdapterType == ADAPTER_TYPE_STOCK_GROUP){
//				colIndex = StockTable.COLNAME_GROUP_INDEX;
//			}

                if (null != item) {
                    if (item.isFavorite()) { // 已添加
                        viewHolder.addBtn.setVisibility(View.GONE);
                        viewHolder.addedPrompt.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.addBtn.setVisibility(View.VISIBLE);
                        viewHolder.addedPrompt.setVisibility(View.GONE);
                        viewHolder.addBtn.setOnClickListener(new OnAddBtnClickListener(position));
                    }
                }

            }

            viewHolder.code.setText(item.getStockCode());
            viewHolder.name.setText(item.getStockName());
        }

        return convertView;
    }

    public void setAdapterType(int adapterType) {
        this.mAdapterType = adapterType;
    }

    private class ViewHolder{
        TextView code;
        TextView name;
        View addBtn;
        View addedPrompt;
    }


    class OnAddBtnClickListener implements View.OnClickListener {

        private int mPosition;

        public OnAddBtnClickListener(int position){
            mPosition = position;
        }
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if(mOnAddListener != null){
                mOnAddListener.onAdd(mPosition);
            }
        }
    }
}
