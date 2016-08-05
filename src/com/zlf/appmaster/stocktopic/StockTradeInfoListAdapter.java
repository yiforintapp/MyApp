package com.zlf.appmaster.stocktopic;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.stock.StockTradeInfo;
import com.zlf.appmaster.ui.stock.StockTextView;

import java.util.List;

public class StockTradeInfoListAdapter extends BaseAdapter {

	Context mContext;
	List<StockTradeInfo> mListData;
	LayoutInflater mInflater;
	
	public StockTradeInfoListAdapter(Context context, List<StockTradeInfo> list) {
		mContext = context;
		mListData = list;
		mInflater = LayoutInflater.from(context);
	}
	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_stock_favorite, null);
            viewHolder = new ViewHolder();
            //viewHolder.StockIcon = (StockImageView) convertView.findViewById(R.id.stock_image);
            viewHolder.StockName = (TextView)convertView.findViewById(R.id.stock_name);
            viewHolder.StockCode = (TextView)convertView.findViewById(R.id.stock_code);
            viewHolder.StockPrice = (StockTextView)convertView.findViewById(R.id.stock_price);
            viewHolder.StockPercent = (StockTextView)convertView.findViewById(R.id.stock_percent);
            //viewHolder.StockPercentComment = (TextView)convertView.findViewById(R.id.stock_item_content_comment);
            viewHolder.StockSuspendedPrompt = (TextView)convertView.findViewById(R.id.stock_trade_suspended);
            //viewHolder.deleteBtn = (Button)convertView.findViewById(R.id.recent_del_btn);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        StockTradeInfo item = (StockTradeInfo)getItem(position);
        if(null != item){
            String stockCode = item.getCode();
            //viewHolder.StockIcon.setImage(stockCode);
            viewHolder.StockCode.setText(stockCode);

            viewHolder.StockName.setText(item.getName());
            if(item.isStockSuspended()){

                viewHolder.StockPercent.setVisibility(View.GONE);
                //viewHolder.StockPercentComment.setVisibility(View.GONE);
                viewHolder.StockSuspendedPrompt.setVisibility(View.VISIBLE);

                viewHolder.StockPrice.setTextColor(Color.BLACK);
            }
            else{
                viewHolder.StockPercent.setVisibility(View.VISIBLE);
                //viewHolder.StockPercentComment.setVisibility(View.VISIBLE);
                viewHolder.StockSuspendedPrompt.setVisibility(View.GONE);

                viewHolder.StockPercent.setRiseInfo(item.getRiseInfo());
                viewHolder.StockPrice.setRiseInfo(item.getRiseInfo());
            }



            viewHolder.StockPrice.setText(item.getCurPriceFormat());
            viewHolder.StockPercent.setText(item.getCurPercentFormat());

        }


		return convertView;
	}

    class ViewHolder {
        //StockImageView StockIcon;
        TextView StockName, StockCode;
        StockTextView StockPrice, StockPercent;
        TextView StockSuspendedPrompt;
    }
}
