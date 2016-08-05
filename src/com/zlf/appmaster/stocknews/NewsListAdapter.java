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

public class NewsListAdapter extends BaseAdapter {
	Context mContext;
	List<NewsFlashItem> mList;
	LayoutInflater mInflater;
	
	public NewsListAdapter(Context context, List<NewsFlashItem> list) {
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
			viewHolder.NewsTitle = (TextView)convertView.findViewById(R.id.news_title);
			viewHolder.NewsTime = (TextView)convertView.findViewById(R.id.news_time);
			
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		NewsFlashItem newsHomeSub = (NewsFlashItem)getItem(position);
		viewHolder.NewsTitle.setText(newsHomeSub.getTitle());
		//QLog.i("tttttt","getTitle:"+newsHomeSub.getTitle());
		viewHolder.NewsTime.setText(TimeUtil.getMonthAndDay(newsHomeSub.getTime()));
		return convertView;
	}
	
	class ViewHolder {
		TextView NewsTitle, NewsTime;
	}
}
