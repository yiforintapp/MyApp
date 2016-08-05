package com.zlf.appmaster.stocknews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.news.NewsFlashItem;
import com.zlf.appmaster.utils.TimeUtil;

import java.util.List;

public class StockReportListAdapter extends BaseAdapter {
	Context mContext;
	List<NewsFlashItem> mList;
	LayoutInflater mInflater;
	
	public StockReportListAdapter(Context context, List<NewsFlashItem> list) {
		mContext = context;
		mList = list;
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_newshome, null);
			viewHolder = new ViewHolder();
			viewHolder.title = (TextView)convertView.findViewById(R.id.news_title);
			viewHolder.time = (TextView)convertView.findViewById(R.id.news_time);
			
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		NewsFlashItem item = (NewsFlashItem)getItem(position);
		viewHolder.title.setText(item.getTitle());
		viewHolder.time.setText(TimeUtil.getNewsSimpleTime(item.getTime()));
		return convertView;
	}
	
	class ViewHolder {
		TextView title, time;
	}
}
