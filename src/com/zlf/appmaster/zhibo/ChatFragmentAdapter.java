package com.zlf.appmaster.zhibo;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.ChatItem;
import com.zlf.appmaster.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huang on 2015/3/6.
 */
public class ChatFragmentAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context mContext;
    private List<ChatItem> mList;


    public ChatFragmentAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<ChatItem>();
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
            convertView = mInflater.inflate(R.layout.zhibo_chat_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.chat_name);
            holder.text = (TextView) convertView.findViewById(R.id.chat_text);
            holder.time = (TextView) convertView.findViewById(R.id.chat_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ChatItem item = mList.get(position);
        String text = item.getText();

        String one = "<html><body>";
        String two = "</body></html>";
        holder.text.setText(Html.fromHtml(one + text + two));
        holder.name.setText(item.getName());
        holder.time.setText(TimeUtil.getFormatChatTime(item.getDate()));
        return convertView;
    }

    class ViewHolder {
        TextView name, text, time;
    }
}
