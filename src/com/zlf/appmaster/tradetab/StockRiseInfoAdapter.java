package com.zlf.appmaster.tradetab;

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

/**
 * Created by Huang on 2015/3/10.
 */
public class StockRiseInfoAdapter extends BaseAdapter {

    public static final int TYPE_TILE = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_SUB_TITLE = 2;


    private Context mContext;
    private List<StockTradeInfo> mStockLedUpArray;
    private List<StockTradeInfo> mStockLedDownArray;

    LayoutInflater mInflater;

    public StockRiseInfoAdapter(Context context, List<StockTradeInfo> stockLedUpArray,
                                List<StockTradeInfo> stockLedDownArray){
        mContext = context;
        mStockLedUpArray = stockLedUpArray;
        mStockLedDownArray = stockLedDownArray;
        mInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        int count = mStockLedUpArray.size();
        if (count != 0)
            count += 1;   // 加标题

        count += mStockLedDownArray.size();

        if (count != 0)
            count += 1;   // 加标题

        return count;
    }

    @Override
    public Object getItem(int position) {
        int countLedUp = mStockLedUpArray.size();

        // 有领涨行业
        if (countLedUp > 0) {
            if (position > 0 && position < countLedUp + 1){
                return mStockLedUpArray.get(position - 1);
            }
            else if (position > countLedUp){
                return mStockLedDownArray.get(position - countLedUp - 2);
            }
        }
        else {
           return mStockLedDownArray.get(position - 2);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        int countLedUp = mStockLedUpArray.size();

        if (position == 0){
            return  TYPE_TILE;
        }
//        else if (position == 1){
//            return TYPE_SUB_TITLE;
//        }
        else if(position == countLedUp + 1){
            return  TYPE_TILE;
        }
//        else if(position == countLedUp + 3){
//            return  TYPE_SUB_TITLE;
//        }

        return TYPE_ITEM;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup viewGroup) {
        int viewType = getItemViewType(position);

        switch (viewType){
            case TYPE_TILE:{
                ViewTitleHolder  viewHolder = null;
                if (null == contentView) {
                    contentView = mInflater.inflate(R.layout.list_header_stock_tradeinfo, null);
                    viewHolder = new ViewTitleHolder();
                    viewHolder.layoutView = contentView.findViewById(R.id.layout_header);
                    viewHolder.headerTV = (TextView) contentView.findViewById(R.id.header);
                    viewHolder.moreTV = (TextView) contentView.findViewById(R.id.more_info);
                    viewHolder.moreTV.setVisibility(View.VISIBLE);

                    contentView.setTag(viewHolder);
                }
                else {
                    viewHolder = (ViewTitleHolder) contentView.getTag();
                }

//                if (position == 0){
//                    viewHolder.headerTV.setText(R.string.stock_led_up_list);
//                    viewHolder.layoutView.setOnClickListener(new View.OnClickListener(){
//                        @Override
//                        public void onClick(View view) {
//                            Intent intent = new Intent(mContext, PopularStockListActivity.class);
//                            intent.putExtra(PopularStockListActivity.INTENT_FLAG_TYPE, 1);
//                            intent.putExtra(PopularStockListActivity.INTENT_FLAG_NAME, mContext.getString(R.string.stock_led_up_list));
//                            mContext.startActivity(intent);
//                        }
//                    });
//                }
//                else {
//                    viewHolder.headerTV.setText(R.string.stock_led_down_list);
//                    viewHolder.layoutView.setOnClickListener(new View.OnClickListener(){
//                        @Override
//                        public void onClick(View view) {
//                            Intent intent = new Intent(mContext, PopularStockListActivity.class);
//                            intent.putExtra(PopularStockListActivity.INTENT_FLAG_TYPE, -1);
//                            intent.putExtra(PopularStockListActivity.INTENT_FLAG_NAME, mContext.getString(R.string.stock_led_down_list));
//                            mContext.startActivity(intent);
//                        }
//                    });
//                }
            }
                break;
            case TYPE_SUB_TITLE:
                if (contentView == null){
                    contentView = mInflater.inflate(R.layout.list_header_stock_info, null);
                }
                break;
            case TYPE_ITEM:{
                ViewHolder viewHolder = null;
                if (contentView == null) {
                    contentView = mInflater.inflate(R.layout.list_item_stock_favorite, null);
                    viewHolder = new ViewHolder();
                    //viewHolder.StockIcon = (StockImageView) convertView.findViewById(R.id.stock_image);
                    viewHolder.StockName = (TextView) contentView.findViewById(R.id.stock_name);
                    viewHolder.StockCode = (TextView) contentView.findViewById(R.id.stock_code);
                    viewHolder.StcokPrice = (StockTextView) contentView.findViewById(R.id.stock_price);
                    viewHolder.StockPercent = (StockTextView) contentView.findViewById(R.id.stock_percent);
                    //viewHolder.StockPercentComment = (TextView)convertView.findViewById(R.id.stock_item_content_comment);
                    viewHolder.StockSuspendedPrompt = (TextView) contentView.findViewById(R.id.stock_trade_suspended);
                    //viewHolder.deleteBtn = (Button)convertView.findViewById(R.id.recent_del_btn);
                    contentView.setTag(viewHolder);
                }
                else {
                    viewHolder = (ViewHolder) contentView.getTag();
                }
                StockTradeInfo stockTradeInfo = (StockTradeInfo)getItem(position);
                if (null != stockTradeInfo){
                    viewHolder.StockName.setText(stockTradeInfo.getName());
                    viewHolder.StockCode.setText(stockTradeInfo.getCode());
                    if(stockTradeInfo.isStockSuspended()){

                        viewHolder.StockPercent.setVisibility(View.GONE);
                        //viewHolder.StockPercentComment.setVisibility(View.GONE);
                        viewHolder.StockSuspendedPrompt.setVisibility(View.VISIBLE);

                        viewHolder.StcokPrice.setTextColor(Color.BLACK);
                    }
                    else{
                        viewHolder.StockPercent.setVisibility(View.VISIBLE);
                        //viewHolder.StockPercentComment.setVisibility(View.VISIBLE);
                        viewHolder.StockSuspendedPrompt.setVisibility(View.GONE);

                        viewHolder.StockPercent.setRiseInfo(stockTradeInfo.getRiseInfo());
                        viewHolder.StcokPrice.setRiseInfo(stockTradeInfo.getRiseInfo());
                    }



                    viewHolder.StcokPrice.setText(stockTradeInfo.getCurPriceFormat());
                    viewHolder.StockPercent.setText(stockTradeInfo.getCurPercentFormat());
                }
            }
                break;
        }
        return contentView;
    }



    // 标题
    class ViewTitleHolder {
        TextView headerTV;
        TextView moreTV;
        View layoutView;
    }

    class ViewHolder {
        //StockImageView StockIcon;
        TextView StockName, StockCode;
        StockTextView  StcokPrice, StockPercent;
        TextView StockSuspendedPrompt;
        //	Button deleteBtn;
    }
}
