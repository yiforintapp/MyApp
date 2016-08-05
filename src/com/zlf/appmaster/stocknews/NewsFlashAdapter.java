package com.zlf.appmaster.stocknews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.news.NewsFlashItem;
import com.zlf.appmaster.utils.TimeUtil;

import java.util.List;

public class NewsFlashAdapter extends ArrayAdapter<NewsFlashItem> {
    private  int mResourceId;

    public NewsFlashAdapter(Context context, int textViewResourceId, List<NewsFlashItem> objects){
        super(context, textViewResourceId, objects);
        mResourceId = textViewResourceId;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
/*        NewsFlashItem news = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        topNewsLinearLayout = (LinearLayout)view.findViewById(R.id.top_linear_layout);
        //显示第一行新闻时增加上方空白部分
        if(position == 0){
            topNewsLinearLayout.setVisibility(View.VISIBLE);
        }
        TextView time = (TextView)view.findViewById(R.id.news_time);
        TextView content = (TextView)view.findViewById(R.id.news_summary);
        String s = "";
        Long t = news.getTime();
        Date d = new Date(t);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        s = String.valueOf(c.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(
                c.get(Calendar.MINUTE)) + ":" + String.valueOf(c.get(Calendar.SECOND));
        time.setText(s);
        content.setText(news.getSummary());
        return view;*/


        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.topEmptyView = convertView.findViewById(R.id.top_linear_layout);
            viewHolder.timeView = (TextView)convertView.findViewById(R.id.news_time);
            viewHolder.contentView = (TextView)convertView.findViewById(R.id.news_summary);

        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        NewsFlashItem item = getItem(position);
        if (item != null) {

            if(position == 0){  //显示第一行新闻时增加上方空白部分
                viewHolder.topEmptyView.setVisibility(View.VISIBLE);
            }
            else{
                viewHolder.topEmptyView.setVisibility(View.GONE);
            }

            viewHolder.timeView.setText(TimeUtil.getChatTime(item.getTime()));
            viewHolder.contentView.setText(item.getTitle());

        }
        return convertView;
    }


    class ViewHolder{
        View topEmptyView;
        TextView contentView,timeView;
    }
}