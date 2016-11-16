package com.zlf.appmaster.zhibo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.WordChatItem;
import com.zlf.appmaster.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/15.
 */
public class WordPointFragmentAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context mContext;
    private List<WordChatItem> mList;


    public WordPointFragmentAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<WordChatItem>();
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
        WordChatItem wordChatItem;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.word_point_item, null);
            holder = new ViewHolder();
            holder.mMsg = (TextView) convertView.findViewById(R.id.content);
            holder.mTime = (TextView) convertView.findViewById(R.id.time);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        wordChatItem = mList.get(position);

        holder.mMsg.setText(wordChatItem.getAnswer());
        if (!TextUtils.isEmpty(wordChatItem.getAnswerTime())) {
            holder.mTime.setText(TimeUtil.getSimpleTime(Long.parseLong(wordChatItem.getAnswerTime())));
        }

        return convertView;
    }

    class ViewHolder {
        TextView mName;
        TextView mMsg;
        TextView mTime;
    }

}
