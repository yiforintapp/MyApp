package com.zlf.appmaster.userTab;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.industry.IndustryItem;
import com.zlf.appmaster.model.stock.StockFavoriteItem;
import com.zlf.appmaster.ui.stock.StockTextView;

import java.util.List;

/**
 * Created by Huang on 2015/3/10.
 */
public class StockFavoriteIndustryAdapter extends BaseAdapter {

    public static final int TYPE_TILE = 0;
    public static final int TYPE_ITEM = 1;

    private Context mContext;
    private List<IndustryItem> mData;

    LayoutInflater mInflater;

    public StockFavoriteIndustryAdapter(Context context, List<IndustryItem> data){
        mContext = context;
        mInflater = LayoutInflater.from(context);

        mData  = data;
    }


    @Override
    public int getCount() {
        int count = 0;
        if (null != mData) {
            //  所有分类中item的总和是ListVIew  Item的总个数
            for (IndustryItem category : mData) {
                count += category.getItemCount();
            }
        }

        return count;
    }

    @Override
    public Object getItem(int position) {

        // 异常情况处理
        if (null == mData || position <  0|| position > getCount()) {
            return null;
        }

        // 同一分类内，第一个元素的索引值
        int categroyFirstIndex = 0;

        for (IndustryItem category : mData) {
            int size = category.getItemCount();
            // 在当前分类中的索引值
            int categoryIndex = position - categroyFirstIndex;
            // item在当前分类内
            if (categoryIndex < size) {
                return  category.getItem( categoryIndex );
            }

            // 索引移动到当前分类结尾，即下一个分类第一个元素索引
            categroyFirstIndex += size;
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
        // 异常情况处理
        if (null == mData || position <  0|| position > getCount()) {
            return TYPE_ITEM;
        }


        int categroyFirstIndex = 0;

        for (IndustryItem category : mData) {
            int size = category.getItemCount();
            // 在当前分类中的索引值
            int categoryIndex = position - categroyFirstIndex;
            if (categoryIndex == 0) {
                return TYPE_TILE;
            }

            categroyFirstIndex += size;
        }

        return TYPE_ITEM;
    }

    @Override
    public boolean isEnabled(int position) {
        if (getItemViewType(position) == TYPE_TILE){        // title 禁止点击
            return false;
        }
        return super.isEnabled(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        int viewType = getItemViewType(position);

        switch (viewType){
            case TYPE_TILE:{
                ViewTitleHolder  viewHolder = null;
                if (null == convertView) {
                    convertView = mInflater.inflate(R.layout.list_header_stock_favorite_industry_title, null);
                    viewHolder = new ViewTitleHolder();
                    viewHolder.span = convertView.findViewById(R.id.span_view);
                    viewHolder.color = (ImageView)convertView.findViewById(R.id.industry_color);
                    viewHolder.name = (TextView) convertView.findViewById(R.id.industry_name);
                    viewHolder.count = (TextView) convertView.findViewById(R.id.industry_count);

                    convertView.setTag(viewHolder);
                }
                else {
                    viewHolder = (ViewTitleHolder) convertView.getTag();
                }

                if (position == 0){ // 第一个不显示间隙
                    viewHolder.span.setVisibility(View.GONE);
                }
                else{
                    viewHolder.span.setVisibility(View.VISIBLE);
                }

                IndustryItem item = (IndustryItem)getItem(position);
                if (null != item){
                    viewHolder.color.setBackgroundColor(item.getColor());
                    viewHolder.name.setText(item.getName());
                    viewHolder.count.setText(String.format("%d只",item.getSubStockCount()));
                }

            }
                break;

            case TYPE_ITEM:{
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
                StockFavoriteItem item = (StockFavoriteItem)getItem(position);
                if(null != item){
                    String stockCode = item.getStockCode();
                    //viewHolder.StockIcon.setImage(stockCode);
                    viewHolder.StockCode.setText(stockCode);

                    viewHolder.StockName.setText(item.getStockName());
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
            }
                break;
        }
        return convertView;
    }



    // 标题
    class ViewTitleHolder {
        View span;
        ImageView color;
        TextView name, count;
    }

    class ViewHolder {
        //StockImageView StockIcon;
        TextView StockName, StockCode;
        StockTextView StockPrice, StockPercent;
        TextView StockSuspendedPrompt;
    }
}
