package com.zlf.appmaster.zhibo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.WordAdviceItem;
import com.zlf.appmaster.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/4.
 */
public class WordAdviceFragmentAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context mContext;
    private List<WordAdviceItem> mList;


    public WordAdviceFragmentAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<WordAdviceItem>();
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

    public void setList(List list) {
        mList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.word_advice_item, null);
            holder = new ViewHolder();
            holder.mContent = (TextView) convertView.findViewById(R.id.advice);
            holder.mTime = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mContent.setText(mList.get(position).getContent());
        holder.mTime.setText(TimeUtil.getTime(Long.parseLong(mList.get(position).getDate())));

        return convertView;
    }

    class ViewHolder {
        TextView mContent;
        TextView mTime;
    }
}
