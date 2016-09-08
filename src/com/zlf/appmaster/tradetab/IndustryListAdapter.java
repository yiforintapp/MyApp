package com.zlf.appmaster.tradetab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.industry.IndustryInfo;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.ui.stock.StockTextView;

import java.util.List;

public class IndustryListAdapter extends BaseAdapter {

    private static final int TYPE_TILE = 0;
    private static final int TYPE_ITEM = 1;

	Context mContext;
	List<IndustryInfo> mListData;
	LayoutInflater mInflater;
	
	public IndustryListAdapter(Context context, List<IndustryInfo> list) {
		mContext = context;
		mListData = list;
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
        if (mListData.size() != 0)
		    return mListData.size() + 1;


        return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
        if (position > 0){
            return mListData.get(position - 1);
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
        if (position == 0){
            return TYPE_TILE;
        }

        return TYPE_ITEM;
    }
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
        int viewType = getItemViewType(position);

        switch (viewType){
            case TYPE_TILE:{
                if (convertView == null){
                    convertView =  mInflater.inflate(R.layout.list_header_industry_info, null);
                }
            }
            break;
            case TYPE_ITEM:{
                ViewHolder viewHolder = null;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_industry_info, null);
                    viewHolder = new ViewHolder();
                    viewHolder.IndustryName = (TextView)convertView.findViewById(R.id.industry_name);
                    viewHolder.StockName = (TextView)convertView.findViewById(R.id.stock_name);
                    viewHolder.StockCode = (TextView)convertView.findViewById(R.id.stock_code);
                    viewHolder.StockPercent = (StockTextView)convertView.findViewById(R.id.stock_percent);
                    convertView.setTag(viewHolder);
                }
                else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                IndustryInfo industryInfo = (IndustryInfo)getItem(position);
                viewHolder.IndustryName.setText(industryInfo.getName());
                viewHolder.StockPercent.setText(industryInfo.getPercentFormat());
                viewHolder.StockPercent.setRiseInfo(industryInfo.getRiseInfo());
                StockTradeInfo stockInfoItem= industryInfo.getLedStock();
                if(stockInfoItem != null) {
                    viewHolder.StockName.setText(stockInfoItem.getName());
                    viewHolder.StockCode.setText(stockInfoItem.getCode());
                }
            }
            break;
        }

		
		
		return convertView;
	}

	
	class ViewHolder {
		TextView IndustryName;
		StockTextView   StockPercent;
		TextView StockName, StockCode;
	}
}
