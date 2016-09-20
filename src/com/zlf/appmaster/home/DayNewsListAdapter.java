
package com.zlf.appmaster.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.DayNewsItem;
import com.zlf.appmaster.model.WinTopItem;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class DayNewsListAdapter extends BaseAdapter {
    Context mContext;
    List<DayNewsItem> mList;
    LayoutInflater mInflater;
    private int mWidth;

    public DayNewsListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mWidth = Utilities.getScreenSize(context)[0];
        mList = new ArrayList<DayNewsItem>();
    }

    public void setList(List<DayNewsItem> list) {
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.daynewslist_item, null);
            viewHolder = new ViewHolder();
            viewHolder.year = (TextView) convertView.findViewById(R.id.tv_year);
            viewHolder.month = (TextView) convertView.findViewById(R.id.tv_date);
            viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.desc = (TextView) convertView.findViewById(R.id.tv_desc);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        DayNewsItem item = mList.get(position);

        viewHolder.year.setText(AppUtil.getDateTime(Long.parseLong(item.getTime()),1));
        viewHolder.month.setText(AppUtil.getDateTime(Long.parseLong(item.getTime()),2));
        viewHolder.title.setText(item.getTitle());
        viewHolder.desc.setText(item.getDesc());
        return convertView;
    }


    class ViewHolder {
        TextView year, month, title, desc;
    }

}
