package com.zlf.appmaster.userTab;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.industry.IndustryItem;
import com.zlf.appmaster.model.stock.StockFavoriteItem;
import com.zlf.appmaster.model.topic.TopicItem;
import com.zlf.appmaster.ui.stock.IndustryPieView;
import com.zlf.appmaster.ui.stock.StockTextView;
import com.zlf.appmaster.ui.stock.TopicPieView;

import java.util.List;

public class StockFavoriteListAdapter extends BaseAdapter {

	private String TAG = "StockFavoriteListAdapter";
    private Context mContext;
    private List<StockFavoriteItem> mListData;
    private List<IndustryItem> mIndustryItems;
    private List<TopicItem> mTopicItems;
	LayoutInflater mInflater;
    boolean mIsShowPie;

    // 布局类型数量
    private static class ViewType{
        private final static int STOCK_SEARCH = 0;
        private final static int TITLE             = 1;              //  标题
        private final static int STOCK_ITEM        = 2;              //  股票项
        private final static int INDUSTRY_PIE      = 3;             //  行业标示的饼图

        public static int getCount(){
            return 4;
        }
    }

	
	public StockFavoriteListAdapter(Context context, List<StockFavoriteItem> list, List<IndustryItem> industryItems, List<TopicItem> topicItems) {
        this(context,list,industryItems,topicItems, true);
	}

    public StockFavoriteListAdapter(Context context, List<StockFavoriteItem> list, List<IndustryItem> industryItems, List<TopicItem> topicItems, boolean isShowPie) {
        mContext = context;
        mListData = list;
        mIndustryItems = industryItems;
        mTopicItems = topicItems;
        mInflater = LayoutInflater.from(context);
        mIsShowPie = isShowPie;
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
        int dataLen = mListData.size();
        int industryLen = 0;
        if (mIndustryItems != null)
            industryLen = mIndustryItems.size();

        if (dataLen > 0){
            if (mIsShowPie && industryLen > 0)
                return dataLen + 3;     // 加上标题、底部行业图
            else
                return dataLen + 2;
        }

		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
        int dataLen = mListData.size();
        if (position > 1 && position < dataLen + 2) {
            return mListData.get(position - 2);
        }

		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}


    @Override
    public int getItemViewType(int position) {
        int dataLen = mListData.size();
        if (position == 0)
            return ViewType.STOCK_SEARCH;
        else if(position == 1){
            return ViewType.TITLE;
        }
        else if (position > 1 && position < dataLen + 2)
            return ViewType.STOCK_ITEM;
        else
            return ViewType.INDUSTRY_PIE;
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.getCount();
    }


    @Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
        int viewType = getItemViewType(position);
        switch (viewType){
            case ViewType.STOCK_SEARCH: {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_msg_search_stock, null);
                }
            }
            case ViewType.TITLE: {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_header_stock_info, null);
                }
            }
                break;
            case ViewType.STOCK_ITEM: {
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
            case ViewType.INDUSTRY_PIE:{
                ViewPieHolder viewPieHolder = null;
                if (convertView == null) {
                    viewPieHolder = new ViewPieHolder();
                    convertView = mInflater.inflate(R.layout.view_stock_favorite_hold_stock_pie, null);
                    viewPieHolder.industryPieView = (IndustryPieView)convertView.findViewById(R.id.stock_favorite_industry);
                    viewPieHolder.topicPieView = (TopicPieView)convertView.findViewById(R.id.stock_favorite_topic);
                    viewPieHolder.radioGroup = (RadioGroup)convertView.findViewById(R.id.radioGroup);
                    viewPieHolder.radioGroup.setOnCheckedChangeListener(new OnPieCheckedChangeListener( viewPieHolder.topicPieView,viewPieHolder.industryPieView));
                    viewPieHolder.industryPieView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (null != mIndustryItems && !mIndustryItems.isEmpty()){
//                                Intent intent = new Intent(mContext, StockFavoriteIndustryActivity.class);
//                                intent.putExtra(StockFavoriteIndustryActivity.INTENT_FLAG_DATA, (Serializable) mIndustryItems);
//                                intent.putExtra(StockFavoriteIndustryActivity.INTENT_FLAG_TITLE, mContext.getString(R.string.stock_favorite_industry_title));
//                                mContext.startActivity(intent);
                            }

                        }
                    });
                    viewPieHolder.topicPieView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (null != mTopicItems && !mTopicItems.isEmpty()) {
//                                Intent intent = new Intent(mContext, StockFavoriteIndustryActivity.class);
//                                intent.putExtra(StockFavoriteIndustryActivity.INTENT_FLAG_DATA, (Serializable) mTopicItems);
//                                intent.putExtra(StockFavoriteIndustryActivity.INTENT_FLAG_TITLE, mContext.getString(R.string.stock_favorite_topic_title));
//                                mContext.startActivity(intent);
                            }

                        }
                    });
                    convertView.setTag(viewPieHolder);
                }
                else{
                    viewPieHolder = (ViewPieHolder)convertView.getTag();
                }

                if (null != mIndustryItems){
                    Log.i(TAG, "mIndustryItems size:"+ mIndustryItems.size());
                    viewPieHolder.industryPieView.setData(mIndustryItems);
                }

                if (null != mTopicItems ){
                    viewPieHolder.topicPieView.setData(mTopicItems);
                }
            }
                break;
        }


		return convertView;
	}

	
	class ViewHolder {
		//StockImageView StockIcon;
		TextView StockName, StockCode;
		StockTextView StockPrice, StockPercent;
		TextView StockSuspendedPrompt;
	}

    class ViewPieHolder{
        IndustryPieView industryPieView;
        TopicPieView topicPieView;
        RadioGroup radioGroup;
    }

    private class OnPieCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{
        private View mControlView1, mControlView2;
        public OnPieCheckedChangeListener(View controlView1, View controlView2){
            mControlView1 = controlView1;
            mControlView2 = controlView2;
        }
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            Log.i(TAG, "OnPieCheckedChangeListener");
            int radioButtonId = radioGroup.getCheckedRadioButtonId();
            if (radioButtonId == R.id.radio_topic){
                mControlView1.setVisibility(View.VISIBLE);
                mControlView2.setVisibility(View.GONE);
            }
            else if (radioButtonId == R.id.radio_industry){
                mControlView1.setVisibility(View.GONE);
                mControlView2.setVisibility(View.VISIBLE);
            }
        }
    }

}
